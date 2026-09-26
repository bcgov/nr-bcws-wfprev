package ca.bc.gov.nrs.wfprev.services.reportjobs;

import ca.bc.gov.nrs.wfprev.data.entities.ReportExportJobEntity;
import ca.bc.gov.nrs.wfprev.data.repositories.ReportExportJobRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * Finishes jobs whose worker went away (a deploy or crash replaced the API task).
 *
 * <ul>
 *   <li>Started jobs older than the Lambda timeout plus a margin can no longer be running. If the
 *       file is in S3 (the Lambda finished after its caller died, or the upload completed just
 *       before), the job is READY; otherwise it FAILED and can be retried.</li>
 *   <li>Unclaimed jobs were queued on a task that went away: they are submitted again. The claim
 *       step stops two tasks running the same job. Jobs still unclaimed after the stuck-queue
 *       threshold fail, so the user sees the problem.</li>
 * </ul>
 *
 * <p>Runs on every API task. All updates are conditional on the row still being PREPARING.
 */
@Slf4j
@Component
public class ReportJobSweeper {

    private static final String PREPARING = ReportJobStatus.PREPARING.name();

    private final ReportExportJobRepository repository;
    private final ReportExportStore store;
    private final ReportJobRunner runner;
    private final ReportJobProperties properties;
    private final Clock clock;

    @Autowired
    public ReportJobSweeper(ReportExportJobRepository repository, ReportExportStore store, ReportJobRunner runner,
                            ReportJobProperties properties) {
        this(repository, store, runner, properties, Clock.systemUTC());
    }

    ReportJobSweeper(ReportExportJobRepository repository, ReportExportStore store, ReportJobRunner runner,
                     ReportJobProperties properties, Clock clock) {
        this.repository = repository;
        this.store = store;
        this.runner = runner;
        this.properties = properties;
        this.clock = clock;
    }

    @Scheduled(initialDelayString = "${reportJobs.sweep.initialDelayMillis:60000}",
            fixedDelayString = "${reportJobs.sweep.intervalMillis:120000}")
    public void sweep() {
        try {
            sweepStarted();
            sweepUnstarted();
        } catch (RuntimeException e) {
            log.error("Report job sweep failed; it will run again", e);
        }
    }

    void sweepStarted() {
        LocalDateTime cutoff = now().minus(properties.startedCutoff());
        for (ReportExportJobEntity job : repository.findByStatusCodeAndStartedTimestampBefore(PREPARING, cutoff)) {
            String key = ReportFiles.outputKey(job.getReportExportJobGuid(), job.getFileName());
            boolean exists;
            try {
                exists = store.exists(key);
            } catch (RuntimeException e) {
                log.warn("Report job {}: couldn't check S3, will try again next sweep", job.getReportExportJobGuid(), e);
                continue;
            }
            if (exists) {
                if (repository.markReady(job.getReportExportJobGuid(), key, now(), ReportJobRunner.SYSTEM_USER, new Date()) > 0) {
                    log.info("Report job {} ({}) RECOVERED by the sweep: its file is in S3", job.getReportExportJobGuid(),
                            job.getReportTypeCode());
                }
            } else {
                failTemporary(job, "its worker stopped and no file was written");
            }
        }
    }

    void sweepUnstarted() {
        LocalDateTime now = now();
        LocalDateTime failBefore = now.minus(properties.failUnstartedAfter());
        LocalDateTime resubmitBefore = now.minus(properties.resubmitAfter());
        for (ReportExportJobEntity job : repository.findByStatusCodeAndStartedTimestampIsNullAndRequestTimestampBefore(
                PREPARING, resubmitBefore)) {
            if (job.getRequestTimestamp().isBefore(failBefore)) {
                failTemporary(job, "it was never started");
            } else {
                log.info("Report job {} ({}) was never started; submitting it again", job.getReportExportJobGuid(),
                        job.getReportTypeCode());
                runner.submit(job.getReportExportJobGuid());
            }
        }
    }

    private void failTemporary(ReportExportJobEntity job, String reason) {
        ReportJobFailure failure = ReportJobFailure.temporary();
        if (repository.markFailed(job.getReportExportJobGuid(), failure.code().name(), failure.message(),
                failure.retryable(), now(), ReportJobRunner.SYSTEM_USER, new Date()) > 0) {
            log.warn("Report job {} ({}) FAILED by the sweep: {} (ref {})", job.getReportExportJobGuid(),
                    job.getReportTypeCode(), reason, ReportFiles.reference(job.getReportExportJobGuid()));
        }
    }

    private LocalDateTime now() {
        return LocalDateTime.now(clock);
    }
}
