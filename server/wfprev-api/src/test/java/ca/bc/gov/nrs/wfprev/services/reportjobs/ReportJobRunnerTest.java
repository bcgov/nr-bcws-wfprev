package ca.bc.gov.nrs.wfprev.services.reportjobs;

import ca.bc.gov.nrs.wfprev.data.entities.ReportExportJobEntity;
import ca.bc.gov.nrs.wfprev.data.models.ReportRequestModel;
import ca.bc.gov.nrs.wfprev.data.models.ReportType;
import ca.bc.gov.nrs.wfprev.data.repositories.ReportExportJobRepository;
import ca.bc.gov.nrs.wfprev.services.ReportService;
import ca.bc.gov.nrs.wfprev.services.XlsxReportGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.RejectedExecutionException;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class ReportJobRunnerTest {

    private static final Instant NOW = Instant.parse("2026-09-24T21:14:03Z");

    private ReportExportJobRepository repository;
    private ReportService reportService;
    private ReportExportStore store;
    private ReportLambdaInvoker lambdaInvoker;
    private ReportJobRunner runner;

    @BeforeEach
    void setUp() {
        repository = mock(ReportExportJobRepository.class);
        reportService = mock(ReportService.class);
        store = mock(ReportExportStore.class);
        lambdaInvoker = mock(ReportLambdaInvoker.class);
        runner = new ReportJobRunner(repository, reportService, store, lambdaInvoker, new ObjectMapper(),
                Clock.fixed(NOW, ZoneOffset.UTC), Runnable::run);
    }

    private ReportExportJobEntity givenClaimedJob(ReportType type) {
        UUID guid = UUID.randomUUID();
        ReportExportJobEntity job = ReportExportJobEntity.builder()
                .reportExportJobGuid(guid)
                .exportGroupGuid(UUID.randomUUID())
                .ownerUserId("IDIR\\PLANNER")
                .reportTypeCode(type.name())
                .requestJson("{\"reportType\":\"" + type.name() + "\",\"projectFilter\":{\"fiscalYears\":[\"2026\"]}}")
                .description("FY 2026/27")
                .fileName(ReportFiles.fileName(type))
                .statusCode("PREPARING")
                .createUser("u").createDate(new Date()).updateUser("u").updateDate(new Date())
                .build();
        when(repository.claim(eq(guid), any(), anyString(), any())).thenReturn(1);
        when(repository.findById(guid)).thenReturn(Optional.of(job));
        return job;
    }

    @Test
    void run_whenAnotherWorkerClaimedTheJob_doesNothing() {
        UUID guid = UUID.randomUUID();
        when(repository.claim(eq(guid), any(), anyString(), any())).thenReturn(0);

        runner.submit(guid);

        verify(repository, never()).findById(any());
        verifyNoInteractions(reportService, store, lambdaInvoker);
    }

    @Test
    void run_xlsx_hasTheLambdaWriteTheFileThenMarksItReady() throws Exception {
        ReportExportJobEntity job = givenClaimedJob(ReportType.RESULTS_XLSX);
        UUID guid = job.getReportExportJobGuid();
        XlsxReportGenerator.LambdaReportRequest lambdaRequest = new XlsxReportGenerator.LambdaReportRequest();
        when(reportService.prepareXlsxLambdaRequest(any())).thenReturn(lambdaRequest);

        runner.submit(guid);

        ArgumentCaptor<ReportRequestModel> request = ArgumentCaptor.forClass(ReportRequestModel.class);
        verify(reportService).prepareXlsxLambdaRequest(request.capture());
        assertEquals(ReportType.RESULTS_XLSX, request.getValue().getReportType());
        assertEquals("2026", request.getValue().getProjectFilter().getFiscalYears().get(0));
        String key = "jobs/" + guid + "/ReMi_RESULTS.xlsx";
        verify(lambdaInvoker).generateXlsx(guid, lambdaRequest, key);
        verify(repository).markReady(eq(guid), eq(key), any(), eq(ReportJobRunner.SYSTEM_USER), any());
        verify(repository, never()).markFailed(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void run_csv_writesAZipAndUploadsItInOnePut() throws Exception {
        ReportExportJobEntity job = givenClaimedJob(ReportType.PROJECT_CSV);
        UUID guid = job.getReportExportJobGuid();
        byte[] zip = "zip-bytes".getBytes(StandardCharsets.UTF_8);
        doAnswer(inv -> {
            inv.<OutputStream>getArgument(1).write(zip);
            return null;
        }).when(reportService).writeCsvZipFromEntities(any(), any());
        byte[][] uploaded = new byte[1][];
        Path[] tempFile = new Path[1];
        doAnswer(inv -> {
            tempFile[0] = inv.getArgument(1);
            uploaded[0] = Files.readAllBytes(tempFile[0]);
            return null;
        }).when(store).putFile(anyString(), any(), anyString());

        runner.submit(guid);

        String key = "jobs/" + guid + "/ReMi_Fiscal.zip";
        verify(store).putFile(eq(key), any(), eq("application/zip"));
        assertArrayEquals(zip, uploaded[0]);
        assertFalse(Files.exists(tempFile[0]), "the temp file is deleted");
        verify(repository).markReady(eq(guid), eq(key), any(), any(), any());
        verifyNoInteractions(lambdaInvoker);
    }

    @Test
    void run_spatialWithNoFiles_marksNoFilesAndUploadsNothing() throws Exception {
        ReportExportJobEntity job = givenClaimedJob(ReportType.RESULTS_SPATIAL);
        when(reportService.exportResultsSpatialZip(any(), any())).thenReturn(false);

        runner.submit(job.getReportExportJobGuid());

        verify(store, never()).putFile(any(), any(), any());
        verify(repository).markNoFiles(eq(job.getReportExportJobGuid()), any(), any(), any());
        verify(repository, never()).markReady(any(), any(), any(), any(), any());
    }

    @Test
    void run_whenTheLambdaFails_marksTheJobFailedAndRetryable() throws Exception {
        ReportExportJobEntity job = givenClaimedJob(ReportType.PROJECT_XLSX);
        when(reportService.prepareXlsxLambdaRequest(any())).thenReturn(new XlsxReportGenerator.LambdaReportRequest());
        doThrow(new IllegalStateException("Report Lambda failed")).when(lambdaInvoker).generateXlsx(any(), any(), any());

        runner.submit(job.getReportExportJobGuid());

        verify(repository).markFailed(eq(job.getReportExportJobGuid()), eq("TEMPORARY"),
                eq("Something went wrong. Try again."), eq(true), any(), any(), any());
        verify(repository, never()).markReady(any(), any(), any(), any(), any());
    }

    @Test
    void run_whenNoFiscalDataMatches_failsAsADataProblemThatRetryingWontFix() throws Exception {
        ReportExportJobEntity job = givenClaimedJob(ReportType.PROJECT_XLSX);
        when(reportService.prepareXlsxLambdaRequest(any()))
                .thenThrow(new IllegalArgumentException("No fiscal data found for the provided projects"));

        runner.submit(job.getReportExportJobGuid());

        verify(repository).markFailed(eq(job.getReportExportJobGuid()), eq("DATA"),
                eq("No fiscal data matches these filters."), eq(false), any(), any(), any());
        verifyNoInteractions(lambdaInvoker);
    }

    @Test
    void run_whenTheUploadFails_marksTheJobFailed() throws Exception {
        ReportExportJobEntity job = givenClaimedJob(ReportType.RESULTS_SPATIAL);
        when(reportService.exportResultsSpatialZip(any(), any())).thenReturn(true);
        doThrow(new RuntimeException("S3 unavailable")).when(store).putFile(any(), any(), any());

        runner.submit(job.getReportExportJobGuid());

        verify(repository).markFailed(eq(job.getReportExportJobGuid()), eq("TEMPORARY"), any(), eq(true),
                any(), any(), any());
    }

    @Test
    void submit_whenTheQueueIsFull_leavesTheJobForTheSweep() {
        ReportJobRunner full = new ReportJobRunner(repository, reportService, store, lambdaInvoker, new ObjectMapper(),
                Clock.fixed(NOW, ZoneOffset.UTC), task -> {
                    throw new RejectedExecutionException("full");
                });

        full.submit(UUID.randomUUID());

        verifyNoInteractions(repository);
    }
}
