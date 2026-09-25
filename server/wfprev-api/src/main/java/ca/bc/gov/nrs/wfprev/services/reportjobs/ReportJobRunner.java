package ca.bc.gov.nrs.wfprev.services.reportjobs;

import ca.bc.gov.nrs.wfprev.data.entities.ReportExportJobEntity;
import ca.bc.gov.nrs.wfprev.data.models.ReportRequestModel;
import ca.bc.gov.nrs.wfprev.data.models.ReportType;
import ca.bc.gov.nrs.wfprev.data.repositories.ReportExportJobRepository;
import ca.bc.gov.nrs.wfprev.services.ReportService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;

/**
 * Runs report export jobs on a small thread pool and records how each one ended. The thread that
 * does the work is the one that updates the row, so nothing has to report back.
 *
 * <p>The pool is private rather than a Spring bean: an {@link Executor} bean would replace Spring
 * Boot's default task executor, which MVC uses for streaming responses.
 */
@Slf4j
@Component
public class ReportJobRunner {

    /** update_user on rows changed by background work. */
    static final String SYSTEM_USER = "WFPREV_REPORT_JOBS";

    private final ReportExportJobRepository repository;
    private final ReportService reportService;
    private final ReportExportStore store;
    private final ReportLambdaInvoker lambdaInvoker;
    private final ObjectMapper objectMapper;
    private final Clock clock;
    private final Executor executor;
    private final ThreadPoolTaskExecutor ownedPool;

    @Autowired
    public ReportJobRunner(ReportExportJobRepository repository, ReportService reportService,
                           ReportExportStore store, ReportLambdaInvoker lambdaInvoker,
                           ObjectMapper objectMapper, ReportJobProperties properties) {
        this(repository, reportService, store, lambdaInvoker, objectMapper, Clock.systemUTC(), createPool(properties));
    }

    /** For tests: pass a direct executor to run jobs on the calling thread. */
    ReportJobRunner(ReportExportJobRepository repository, ReportService reportService,
                    ReportExportStore store, ReportLambdaInvoker lambdaInvoker,
                    ObjectMapper objectMapper, Clock clock, Executor executor) {
        this.repository = repository;
        this.reportService = reportService;
        this.store = store;
        this.lambdaInvoker = lambdaInvoker;
        this.objectMapper = objectMapper;
        this.clock = clock;
        this.executor = executor;
        this.ownedPool = executor instanceof ThreadPoolTaskExecutor pool ? pool : null;
    }

    private static ThreadPoolTaskExecutor createPool(ReportJobProperties properties) {
        ThreadPoolTaskExecutor pool = new ThreadPoolTaskExecutor();
        pool.setCorePoolSize(properties.getThreads());
        pool.setMaxPoolSize(properties.getThreads());
        pool.setQueueCapacity(properties.getQueueCapacity());
        pool.setThreadNamePrefix("report-job-");
        pool.setWaitForTasksToCompleteOnShutdown(false);
        pool.initialize();
        return pool;
    }

    /**
     * Queues a job. If the queue is full the job stays PREPARING and unclaimed, and the sweep
     * submits it again later.
     */
    public void submit(UUID jobGuid) {
        try {
            executor.execute(() -> run(jobGuid));
        } catch (RejectedExecutionException e) { // includes Spring's TaskRejectedException
            log.warn("Report job {}: queue full, the sweep will resubmit it", jobGuid);
        }
    }

    void run(UUID jobGuid) {
        if (repository.claim(jobGuid, now(), SYSTEM_USER, new Date()) == 0) {
            log.info("Report job {}: already claimed or finished, skipping", jobGuid);
            return;
        }
        ReportExportJobEntity job = repository.findById(jobGuid).orElse(null);
        if (job == null) {
            return;
        }

        ReportType type = ReportType.valueOf(job.getReportTypeCode());
        long started = System.currentTimeMillis();
        log.info("Report job {} ({}) started", jobGuid, type);
        try {
            ReportRequestModel request = objectMapper.readValue(job.getRequestJson(), ReportRequestModel.class);
            request.setReportType(type);
            String key = ReportFiles.outputKey(jobGuid, job.getFileName());

            boolean hasFile;
            if (ReportFiles.isXlsx(type)) {
                lambdaInvoker.generateXlsx(jobGuid, reportService.prepareXlsxLambdaRequest(request), key);
                hasFile = true;
            } else {
                hasFile = writeAndUpload(jobGuid, type, request, key);
            }

            int updated = hasFile
                    ? repository.markReady(jobGuid, key, now(), SYSTEM_USER, new Date())
                    : repository.markNoFiles(jobGuid, now(), SYSTEM_USER, new Date());
            log.info("Report job {} ({}) {} in {} ms{}", jobGuid, type, hasFile ? "READY" : "NO_FILES",
                    System.currentTimeMillis() - started, updated == 0 ? " (row already final, not updated)" : "");
        } catch (Throwable error) {
            fail(jobGuid, type, error);
        }
    }

    private void fail(UUID jobGuid, ReportType type, Throwable error) {
        ReportJobFailure failure = ReportJobFailure.from(error);
        log.error("Report job {} ({}) FAILED as {} (ref {})", jobGuid, type, failure.code(),
                ReportFiles.reference(jobGuid), error);
        try {
            repository.markFailed(jobGuid, failure.code().name(), failure.message(), failure.retryable(),
                    now(), SYSTEM_USER, new Date());
        } catch (RuntimeException e) {
            log.error("Report job {}: couldn't record the failure; the sweep will", jobGuid, e);
        }
    }

    /**
     * Builds a CSV or spatial ZIP in a temp file, then uploads it in one PUT, so the object only
     * appears in S3 once it is complete.
     *
     * @return false when there was nothing to write (a spatial export with no spatial files)
     */
    private boolean writeAndUpload(UUID jobGuid, ReportType type, ReportRequestModel request, String key) throws Exception {
        Path temp = Files.createTempFile("report-" + jobGuid + "-", ".zip");
        try {
            boolean hasFile;
            try (OutputStream out = Files.newOutputStream(temp)) {
                if (type == ReportType.RESULTS_SPATIAL) {
                    hasFile = reportService.exportResultsSpatialZip(request, out);
                } else {
                    reportService.writeCsvZipFromEntities(request, out);
                    hasFile = true;
                }
            }
            if (hasFile) {
                store.putFile(key, temp, ReportFiles.contentType(type));
            }
            return hasFile;
        } finally {
            Files.deleteIfExists(temp);
        }
    }

    private LocalDateTime now() {
        return LocalDateTime.now(clock);
    }

    @PreDestroy
    void shutdown() {
        if (ownedPool != null) {
            ownedPool.shutdown();
        }
    }
}
