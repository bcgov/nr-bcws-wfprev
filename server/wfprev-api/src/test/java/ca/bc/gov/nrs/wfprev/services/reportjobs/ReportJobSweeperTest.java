package ca.bc.gov.nrs.wfprev.services.reportjobs;

import ca.bc.gov.nrs.wfprev.data.entities.ReportExportJobEntity;
import ca.bc.gov.nrs.wfprev.data.repositories.ReportExportJobRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ReportJobSweeperTest {

    private static final Instant NOW = Instant.parse("2026-09-24T21:14:03Z");
    private static final LocalDateTime NOW_UTC = LocalDateTime.ofInstant(NOW, ZoneOffset.UTC);

    private ReportExportJobRepository repository;
    private ReportExportStore store;
    private ReportJobRunner runner;
    private ReportJobProperties properties;
    private ReportJobSweeper sweeper;

    @BeforeEach
    void setUp() {
        repository = mock(ReportExportJobRepository.class);
        store = mock(ReportExportStore.class);
        runner = mock(ReportJobRunner.class);
        properties = new ReportJobProperties();
        properties.setLambdaTimeoutSeconds(300);
        properties.setSweepStartedMarginSeconds(300);
        properties.setSweepResubmitAfterSeconds(600);
        properties.setSweepFailUnstartedAfterSeconds(1800);
        sweeper = new ReportJobSweeper(repository, store, runner, properties, Clock.fixed(NOW, ZoneOffset.UTC));
    }

    private static ReportExportJobEntity preparing(LocalDateTime requested, LocalDateTime started) {
        return ReportExportJobEntity.builder()
                .reportExportJobGuid(UUID.randomUUID())
                .reportTypeCode("RESULTS_XLSX")
                .fileName("ReMi_RESULTS.xlsx")
                .statusCode("PREPARING")
                .requestTimestamp(requested)
                .startedTimestamp(started)
                .createUser("u").createDate(new Date()).updateUser("u").updateDate(new Date())
                .build();
    }

    @Test
    void startedJobs_areOnlyCheckedOnceTheLambdaTimeoutPlusMarginHasPassed() {
        sweeper.sweepStarted();

        verify(repository).findByStatusCodeAndStartedTimestampBefore("PREPARING", NOW_UTC.minusSeconds(600));
    }

    @Test
    void startedJob_whoseFileIsInS3_isRecoveredAsReady() {
        ReportExportJobEntity job = preparing(NOW_UTC.minusMinutes(20), NOW_UTC.minusMinutes(19));
        when(repository.findByStatusCodeAndStartedTimestampBefore(eq("PREPARING"), any())).thenReturn(List.of(job));
        String key = "jobs/" + job.getReportExportJobGuid() + "/ReMi_RESULTS.xlsx";
        when(store.exists(key)).thenReturn(true);

        sweeper.sweepStarted();

        verify(repository).markReady(eq(job.getReportExportJobGuid()), eq(key), eq(NOW_UTC),
                eq(ReportJobRunner.SYSTEM_USER), any());
        verify(repository, never()).markFailed(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void startedJob_withNoFile_failsAsRetryable() {
        ReportExportJobEntity job = preparing(NOW_UTC.minusMinutes(20), NOW_UTC.minusMinutes(19));
        when(repository.findByStatusCodeAndStartedTimestampBefore(eq("PREPARING"), any())).thenReturn(List.of(job));
        when(store.exists(any())).thenReturn(false);

        sweeper.sweepStarted();

        verify(repository).markFailed(eq(job.getReportExportJobGuid()), eq("TEMPORARY"),
                eq("Something went wrong. Try again."), eq(true), eq(NOW_UTC), eq(ReportJobRunner.SYSTEM_USER), any());
        verify(repository, never()).markReady(any(), any(), any(), any(), any());
    }

    @Test
    void startedJob_whenS3CantBeReached_isLeftForTheNextSweep() {
        ReportExportJobEntity job = preparing(NOW_UTC.minusMinutes(20), NOW_UTC.minusMinutes(19));
        when(repository.findByStatusCodeAndStartedTimestampBefore(eq("PREPARING"), any())).thenReturn(List.of(job));
        when(store.exists(any())).thenThrow(new RuntimeException("S3 unavailable"));

        sweeper.sweepStarted();

        verify(repository, never()).markReady(any(), any(), any(), any(), any());
        verify(repository, never()).markFailed(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void unstartedJob_isResubmitted() {
        ReportExportJobEntity job = preparing(NOW_UTC.minusMinutes(12), null);
        when(repository.findByStatusCodeAndStartedTimestampIsNullAndRequestTimestampBefore("PREPARING",
                NOW_UTC.minusSeconds(600))).thenReturn(List.of(job));

        sweeper.sweepUnstarted();

        verify(runner).submit(job.getReportExportJobGuid());
        verify(repository, never()).markFailed(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void unstartedJob_pastTheStuckQueueThreshold_fails() {
        ReportExportJobEntity job = preparing(NOW_UTC.minusMinutes(31), null);
        when(repository.findByStatusCodeAndStartedTimestampIsNullAndRequestTimestampBefore(eq("PREPARING"), any()))
                .thenReturn(List.of(job));

        sweeper.sweepUnstarted();

        verify(repository).markFailed(eq(job.getReportExportJobGuid()), eq("TEMPORARY"), any(), eq(true),
                any(), any(), any());
        verify(runner, never()).submit(any());
    }

    @Test
    void sweep_keepsRunningAfterAnError() {
        when(repository.findByStatusCodeAndStartedTimestampBefore(any(), any())).thenThrow(new RuntimeException("db down"));

        sweeper.sweep();
    }
}
