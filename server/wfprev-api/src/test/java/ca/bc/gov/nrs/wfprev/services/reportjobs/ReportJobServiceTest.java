package ca.bc.gov.nrs.wfprev.services.reportjobs;

import ca.bc.gov.nrs.wfprev.data.entities.ReportExportJobEntity;
import ca.bc.gov.nrs.wfprev.data.models.ReportJobDownloadUrlModel;
import ca.bc.gov.nrs.wfprev.data.models.ReportJobModel;
import ca.bc.gov.nrs.wfprev.data.models.ReportJobRequestModel;
import ca.bc.gov.nrs.wfprev.data.models.ReportRequestModel;
import ca.bc.gov.nrs.wfprev.data.models.ReportType;
import ca.bc.gov.nrs.wfprev.data.params.FeatureQueryParams;
import ca.bc.gov.nrs.wfprev.data.repositories.ReportExportJobRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ReportJobServiceTest {

    private static final String OWNER = "IDIR\\PLANNER";
    private static final Instant NOW = Instant.parse("2026-09-24T21:14:03Z");
    private static final LocalDateTime NOW_UTC = LocalDateTime.ofInstant(NOW, ZoneOffset.UTC);

    private ReportExportJobRepository repository;
    private ReportJobRunner runner;
    private ReportExportStore store;
    private ReportJobProperties properties;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private ReportJobService service;

    @BeforeEach
    void setUp() {
        repository = mock(ReportExportJobRepository.class);
        runner = mock(ReportJobRunner.class);
        store = mock(ReportExportStore.class);
        properties = new ReportJobProperties();
        properties.setRetentionHours(24);
        properties.setListWindowHours(48);
        properties.setDownloadUrlMinutes(5);
        service = new ReportJobService(repository, runner, store, properties, objectMapper,
                Clock.fixed(NOW, ZoneOffset.UTC));
    }

    private static ReportJobRequestModel request(ReportType type) {
        FeatureQueryParams filter = new FeatureQueryParams();
        filter.setFireCentreOrgUnitIds(List.of("3"));
        filter.setFiscalYears(List.of("2026"));
        ReportJobRequestModel request = new ReportJobRequestModel();
        request.setReportType(type);
        request.setProjectFilter(filter);
        request.setDescription("Coastal Fire Centre · FY 2026/27");
        return request;
    }

    private static ReportExportJobEntity job(String status, LocalDateTime requested) {
        UUID guid = UUID.randomUUID();
        return ReportExportJobEntity.builder()
                .reportExportJobGuid(guid)
                .exportGroupGuid(UUID.randomUUID())
                .ownerUserId(OWNER)
                .reportTypeCode(ReportType.RESULTS_XLSX.name())
                .requestJson("{\"reportType\":\"RESULTS_XLSX\",\"projectFilter\":{\"fireCentreOrgUnitIds\":[\"3\"]}}")
                .description("Coastal Fire Centre · FY 2026/27")
                .fileName("ReMi_RESULTS.xlsx")
                .statusCode(status)
                .s3ObjectKey("READY".equals(status) ? ReportFiles.outputKey(guid, "ReMi_RESULTS.xlsx") : null)
                .requestTimestamp(requested)
                .createUser(OWNER).createDate(new Date()).updateUser(OWNER).updateDate(new Date())
                .build();
    }

    @Test
    void create_savesAPreparingJobAndQueuesIt() throws Exception {
        ReportJobRequestModel request = request(ReportType.RESULTS_SPATIAL);
        UUID group = UUID.randomUUID();
        request.setExportGroupGuid(group);

        ReportJobModel model = service.create(request, OWNER);

        ArgumentCaptor<ReportExportJobEntity> saved = ArgumentCaptor.forClass(ReportExportJobEntity.class);
        verify(repository).save(saved.capture());
        ReportExportJobEntity job = saved.getValue();
        assertEquals("PREPARING", job.getStatusCode());
        assertEquals(OWNER, job.getOwnerUserId());
        assertEquals(group, job.getExportGroupGuid());
        assertEquals("ReMi_RESULTS_Spatial.zip", job.getFileName());
        assertEquals("RESULTS_SPATIAL", job.getReportTypeCode());
        assertEquals(NOW_UTC, job.getRequestTimestamp());
        assertNull(job.getStartedTimestamp());
        ReportRequestModel stored = objectMapper.readValue(job.getRequestJson(), ReportRequestModel.class);
        assertEquals(List.of("3"), stored.getProjectFilter().getFireCentreOrgUnitIds());
        assertEquals(ReportType.RESULTS_SPATIAL, stored.getReportType());

        verify(runner).submit(job.getReportExportJobGuid());
        assertEquals(job.getReportExportJobGuid(), model.getJobGuid());
        assertEquals("2026-09-24T21:14:03Z", model.getRequestTimestamp());
        assertEquals(job.getReportExportJobGuid().toString().substring(0, 8).toUpperCase(), model.getReference());
    }

    @Test
    void create_withoutAGroup_startsANewOne() {
        service.create(request(ReportType.PROJECT_CSV), OWNER);

        ArgumentCaptor<ReportExportJobEntity> saved = ArgumentCaptor.forClass(ReportExportJobEntity.class);
        verify(repository).save(saved.capture());
        assertNotNull(saved.getValue().getExportGroupGuid());
    }

    @Test
    void create_trimsAndCapsTheDescription() {
        ReportJobRequestModel request = request(ReportType.PROJECT_CSV);
        request.setDescription("  " + "x".repeat(5000) + "  ");

        service.create(request, OWNER);

        ArgumentCaptor<ReportExportJobEntity> saved = ArgumentCaptor.forClass(ReportExportJobEntity.class);
        verify(repository).save(saved.capture());
        assertEquals(ReportJobService.MAX_DESCRIPTION_LENGTH, saved.getValue().getDescription().length());
    }

    @Test
    void create_withoutProjectsOrFilter_isRejected() {
        ReportJobRequestModel request = new ReportJobRequestModel();
        request.setReportType(ReportType.PROJECT_CSV);

        assertThrows(IllegalArgumentException.class, () -> service.create(request, OWNER));
        verify(repository, never()).save(any());
        verify(runner, never()).submit(any());
    }

    @Test
    void list_marksReadyJobsOlderThanRetentionAsExpired() {
        ReportExportJobEntity fresh = job("READY", NOW_UTC.minusHours(2));
        ReportExportJobEntity old = job("READY", NOW_UTC.minusHours(25));
        ReportExportJobEntity failedOld = job("FAILED", NOW_UTC.minusHours(25));
        when(repository.findByOwnerUserIdAndDismissedIndFalseAndRequestTimestampAfterOrderByRequestTimestampDesc(
                OWNER, NOW_UTC.minusHours(48))).thenReturn(List.of(fresh, old, failedOld));

        List<ReportJobModel> jobs = service.list(OWNER);

        assertFalse(jobs.get(0).isExpired());
        assertTrue(jobs.get(1).isExpired());
        assertFalse(jobs.get(2).isExpired(), "only READY jobs expire; a failed one still offers Retry");
    }

    @Test
    void downloadUrl_signsALinkAndMarksTheJobDownloaded() {
        ReportExportJobEntity ready = job("READY", NOW_UTC.minusMinutes(3));
        when(repository.findByReportExportJobGuidAndOwnerUserId(ready.getReportExportJobGuid(), OWNER))
                .thenReturn(Optional.of(ready));
        when(store.presignDownload(eq(ready.getS3ObjectKey()), eq("ReMi_RESULTS.xlsx"), anyString(), any()))
                .thenReturn("https://s3.example/signed");

        ReportJobDownloadUrlModel url = service.downloadUrl(ready.getReportExportJobGuid(), OWNER);

        assertEquals("https://s3.example/signed", url.getUrl());
        assertEquals("ReMi_RESULTS.xlsx", url.getFileName());
        verify(repository).markDownloaded(eq(ready.getReportExportJobGuid()), eq(NOW_UTC), eq(OWNER), any());
        verify(store).presignDownload(ready.getS3ObjectKey(), "ReMi_RESULTS.xlsx",
                ReportFiles.contentType(ReportType.RESULTS_XLSX), properties.downloadUrlLifetime());
    }

    @Test
    void downloadUrl_forAnotherUsersJob_isNotFound() {
        UUID guid = UUID.randomUUID();
        when(repository.findByReportExportJobGuidAndOwnerUserId(guid, OWNER)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> service.downloadUrl(guid, OWNER));
        verify(store, never()).presignDownload(any(), any(), any(), any());
    }

    @Test
    void downloadUrl_forAnExpiredFile_isRefused() {
        ReportExportJobEntity old = job("READY", NOW_UTC.minusHours(25));
        when(repository.findByReportExportJobGuidAndOwnerUserId(old.getReportExportJobGuid(), OWNER))
                .thenReturn(Optional.of(old));

        assertThrows(IllegalStateException.class, () -> service.downloadUrl(old.getReportExportJobGuid(), OWNER));
        verify(store, never()).presignDownload(any(), any(), any(), any());
    }

    @Test
    void downloadUrl_forAJobStillPreparing_isRefused() {
        ReportExportJobEntity preparing = job("PREPARING", NOW_UTC);
        when(repository.findByReportExportJobGuidAndOwnerUserId(preparing.getReportExportJobGuid(), OWNER))
                .thenReturn(Optional.of(preparing));

        assertThrows(IllegalStateException.class, () -> service.downloadUrl(preparing.getReportExportJobGuid(), OWNER));
    }

    @Test
    void retry_startsANewJobFromTheOriginalRequestInTheSameGroup() {
        ReportExportJobEntity failed = job("FAILED", NOW_UTC.minusHours(1));
        failed.setReportTypeCode(ReportType.RESULTS_SPATIAL.name());
        failed.setFileName("ReMi_RESULTS_Spatial.zip");
        when(repository.findByReportExportJobGuidAndOwnerUserId(failed.getReportExportJobGuid(), OWNER))
                .thenReturn(Optional.of(failed));

        ReportJobModel model = service.retry(failed.getReportExportJobGuid(), OWNER);

        ArgumentCaptor<ReportExportJobEntity> saved = ArgumentCaptor.forClass(ReportExportJobEntity.class);
        verify(repository).save(saved.capture());
        ReportExportJobEntity retry = saved.getValue();
        assertNotEquals(failed.getReportExportJobGuid(), retry.getReportExportJobGuid());
        assertEquals(failed.getExportGroupGuid(), retry.getExportGroupGuid());
        assertEquals(failed.getReportExportJobGuid(), retry.getRetriedFromJobGuid());
        assertEquals(failed.getRequestJson(), retry.getRequestJson());
        assertEquals(failed.getDescription(), retry.getDescription());
        assertEquals("RESULTS_SPATIAL", retry.getReportTypeCode());
        assertEquals("PREPARING", retry.getStatusCode());
        assertEquals(NOW_UTC, retry.getRequestTimestamp());
        verify(repository).dismiss(eq(failed.getReportExportJobGuid()), eq(OWNER), any());
        verify(runner).submit(retry.getReportExportJobGuid());
        assertEquals(retry.getReportExportJobGuid(), model.getJobGuid());
    }

    @Test
    void retry_ofAJobStillPreparing_isRefused() {
        ReportExportJobEntity preparing = job("PREPARING", NOW_UTC);
        when(repository.findByReportExportJobGuidAndOwnerUserId(preparing.getReportExportJobGuid(), OWNER))
                .thenReturn(Optional.of(preparing));

        assertThrows(IllegalStateException.class, () -> service.retry(preparing.getReportExportJobGuid(), OWNER));
        verify(repository, never()).save(any());
    }

    @Test
    void retry_ofAnotherUsersJob_isNotFound() {
        UUID guid = UUID.randomUUID();
        when(repository.findByReportExportJobGuidAndOwnerUserId(guid, OWNER)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> service.retry(guid, OWNER));
    }

    @Test
    void clearFinished_dismissesOnlyTheCallersFinishedJobs() {
        when(repository.dismissFinished(eq(OWNER), eq(OWNER), any())).thenReturn(3);

        assertEquals(3, service.clearFinished(OWNER));
    }
}
