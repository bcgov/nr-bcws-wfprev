package ca.bc.gov.nrs.wfprev.services.reportjobs;

import ca.bc.gov.nrs.wfprev.data.entities.ReportExportJobEntity;
import ca.bc.gov.nrs.wfprev.data.models.ReportJobDownloadUrlModel;
import ca.bc.gov.nrs.wfprev.data.models.ReportJobModel;
import ca.bc.gov.nrs.wfprev.data.models.ReportJobRequestModel;
import ca.bc.gov.nrs.wfprev.data.models.ReportRequestModel;
import ca.bc.gov.nrs.wfprev.data.models.ReportType;
import ca.bc.gov.nrs.wfprev.data.repositories.ReportExportJobRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Starts, lists, retries and hands out downloads for report export jobs. Every lookup is by owner:
 * another user's job is reported as not found, so its existence isn't revealed.
 */
@Slf4j
@Service
public class ReportJobService {

    static final int MAX_DESCRIPTION_LENGTH = 4000;
    private static final String NOT_FOUND = "Export job not found";

    private final ReportExportJobRepository repository;
    private final ReportJobRunner runner;
    private final ReportExportStore store;
    private final ReportJobProperties properties;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    @Autowired
    public ReportJobService(ReportExportJobRepository repository, ReportJobRunner runner, ReportExportStore store,
                            ReportJobProperties properties, ObjectMapper objectMapper) {
        this(repository, runner, store, properties, objectMapper, Clock.systemUTC());
    }

    ReportJobService(ReportExportJobRepository repository, ReportJobRunner runner, ReportExportStore store,
                     ReportJobProperties properties, ObjectMapper objectMapper, Clock clock) {
        this.repository = repository;
        this.runner = runner;
        this.store = store;
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.clock = clock;
    }

    public ReportJobModel create(ReportJobRequestModel request, String owner) {
        if (request.getReportType() == null) {
            throw new IllegalArgumentException("reportType is required");
        }
        boolean hasProjects = request.getProjects() != null && !request.getProjects().isEmpty();
        if (!hasProjects && request.getProjectFilter() == null) {
            throw new IllegalArgumentException("At least one project or a filter is required");
        }

        ReportRequestModel reportRequest = request.toReportRequest();
        String requestJson;
        try {
            requestJson = objectMapper.writeValueAsString(reportRequest);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("The export request couldn't be read", e);
        }

        UUID group = request.getExportGroupGuid() != null ? request.getExportGroupGuid() : UUID.randomUUID();
        ReportExportJobEntity job = newJob(request.getReportType(), requestJson, description(request.getDescription()),
                group, null, owner);
        repository.save(job);
        log.info("Report job {} ({}) created for {} in group {}", job.getReportExportJobGuid(),
                request.getReportType(), owner, group);
        runner.submit(job.getReportExportJobGuid());
        return toModel(job, now());
    }

    public List<ReportJobModel> list(String owner) {
        LocalDateTime now = now();
        return repository
                .findByOwnerUserIdAndDismissedIndFalseAndRequestTimestampAfterOrderByRequestTimestampDesc(
                        owner, now.minus(properties.listWindow()))
                .stream()
                .map(job -> toModel(job, now))
                .toList();
    }

    public ReportJobDownloadUrlModel downloadUrl(UUID jobGuid, String owner) {
        ReportExportJobEntity job = find(jobGuid, owner);
        if (!ReportJobStatus.READY.name().equals(job.getStatusCode()) || isExpired(job, now())) {
            throw new IllegalStateException("This file is no longer available");
        }
        repository.markDownloaded(jobGuid, now(), owner, new Date());
        ReportType type = ReportType.valueOf(job.getReportTypeCode());
        String url = store.presignDownload(job.getS3ObjectKey(), job.getFileName(), ReportFiles.contentType(type),
                properties.downloadUrlLifetime());
        return new ReportJobDownloadUrlModel(url, job.getFileName());
    }

    /**
     * Runs a finished job again from its original request, in the same export group. Used for both
     * Retry (a failed file) and Run again (an expired export). The old job leaves the tray.
     */
    public ReportJobModel retry(UUID jobGuid, String owner) {
        ReportExportJobEntity previous = find(jobGuid, owner);
        if (ReportJobStatus.PREPARING.name().equals(previous.getStatusCode())) {
            throw new IllegalStateException("This export is still being prepared");
        }
        ReportExportJobEntity job = newJob(ReportType.valueOf(previous.getReportTypeCode()), previous.getRequestJson(),
                previous.getDescription(), previous.getExportGroupGuid(), jobGuid, owner);
        repository.save(job);
        repository.dismiss(jobGuid, owner, new Date());
        log.info("Report job {} ({}) created for {} as a retry of {}", job.getReportExportJobGuid(),
                job.getReportTypeCode(), owner, jobGuid);
        runner.submit(job.getReportExportJobGuid());
        return toModel(job, now());
    }

    /** "Clear finished" in the tray. Jobs still preparing stay. */
    public int clearFinished(String owner) {
        return repository.dismissFinished(owner, owner, new Date());
    }

    private ReportExportJobEntity find(UUID jobGuid, String owner) {
        return repository.findByReportExportJobGuidAndOwnerUserId(jobGuid, owner)
                .orElseThrow(() -> new EntityNotFoundException(NOT_FOUND));
    }

    private ReportExportJobEntity newJob(ReportType type, String requestJson, String description, UUID group,
                                         UUID retriedFrom, String owner) {
        Date today = new Date();
        return ReportExportJobEntity.builder()
                .reportExportJobGuid(UUID.randomUUID())
                .exportGroupGuid(group)
                .retriedFromJobGuid(retriedFrom)
                .ownerUserId(owner)
                .reportTypeCode(type.name())
                .requestJson(requestJson)
                .description(description)
                .fileName(ReportFiles.fileName(type))
                .statusCode(ReportJobStatus.PREPARING.name())
                .requestTimestamp(now())
                .dismissedInd(false)
                .createUser(owner)
                .createDate(today)
                .updateUser(owner)
                .updateDate(today)
                .build();
    }

    ReportJobModel toModel(ReportExportJobEntity job, LocalDateTime now) {
        return ReportJobModel.builder()
                .jobGuid(job.getReportExportJobGuid())
                .exportGroupGuid(job.getExportGroupGuid())
                .reportType(ReportType.valueOf(job.getReportTypeCode()))
                .fileName(job.getFileName())
                .description(job.getDescription())
                .status(job.getStatusCode())
                .expired(isExpired(job, now))
                .downloaded(job.getDownloadedTimestamp() != null)
                .errorCode(job.getErrorCode())
                .errorMessage(job.getErrorMessage())
                .retryable(job.getRetryableInd())
                .reference(ReportFiles.reference(job.getReportExportJobGuid()))
                .requestTimestamp(job.getRequestTimestamp().toInstant(ZoneOffset.UTC).truncatedTo(ChronoUnit.SECONDS).toString())
                .build();
    }

    /** The bucket deletes files after the retention period, so a READY job that old has no file. */
    boolean isExpired(ReportExportJobEntity job, LocalDateTime now) {
        return ReportJobStatus.READY.name().equals(job.getStatusCode())
                && job.getRequestTimestamp().isBefore(now.minus(properties.retention()));
    }

    private static String description(String description) {
        if (description == null) {
            return "";
        }
        String trimmed = description.strip();
        return trimmed.length() > MAX_DESCRIPTION_LENGTH ? trimmed.substring(0, MAX_DESCRIPTION_LENGTH) : trimmed;
    }

    private LocalDateTime now() {
        return LocalDateTime.now(clock);
    }
}
