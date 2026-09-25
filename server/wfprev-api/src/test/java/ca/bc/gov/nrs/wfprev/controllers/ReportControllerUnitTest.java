package ca.bc.gov.nrs.wfprev.controllers;

import ca.bc.gov.nrs.wfone.common.service.api.ServiceException;
import ca.bc.gov.nrs.wfprev.data.models.ReportRequestModel;
import ca.bc.gov.nrs.wfprev.data.models.ReportType;
import ca.bc.gov.nrs.wfprev.services.ReportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

class ReportControllerUnitTest {

    private ReportService reportService;
    private ReportController controller;

    @BeforeEach
    void setUp() {
        reportService = mock(ReportService.class);
        controller = new ReportController(reportService);
    }

    @Test
    void testGenerateReport_ProjectXlsx() throws Exception {
        byte[] expectedBytes = "fake-xlsx-content".getBytes(StandardCharsets.UTF_8);
        doAnswer(invocation -> {
            OutputStream os = invocation.getArgument(1);
            os.write(expectedBytes);
            return null;
        }).when(reportService).exportXlsx(any(ReportRequestModel.class), any(OutputStream.class));

        ReportRequestModel request = new ReportRequestModel();
        request.setReportType(ReportType.PROJECT_XLSX);

        ResponseEntity<StreamingResponseBody> response = controller.generateReport(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("attachment; filename=ReMi_Fiscal.xlsx", response.getHeaders().getFirst("Content-Disposition"));
        assertEquals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                response.getHeaders().getContentType().toString());

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        response.getBody().writeTo(baos);
        assertArrayEquals(expectedBytes, baos.toByteArray());

        verify(reportService, times(1)).exportXlsx(eq(request), any(OutputStream.class));
    }

    @Test
    void testGenerateReport_ProjectCsv() throws Exception {
        byte[] expectedBytes = "fake-csv-zip-content".getBytes(StandardCharsets.UTF_8);
        doAnswer(invocation -> {
            OutputStream os = invocation.getArgument(1);
            os.write(expectedBytes);
            return null;
        }).when(reportService).writeCsvZipFromEntities(any(ReportRequestModel.class), any(OutputStream.class));

        ReportRequestModel request = new ReportRequestModel();
        request.setReportType(ReportType.PROJECT_CSV);

        ResponseEntity<StreamingResponseBody> response = controller.generateReport(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("attachment; filename=ReMi_Fiscal.zip", response.getHeaders().getFirst("Content-Disposition"));
        assertEquals("application/zip", response.getHeaders().getContentType().toString());

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        response.getBody().writeTo(baos);
        assertArrayEquals(expectedBytes, baos.toByteArray());

        verify(reportService, times(1)).writeCsvZipFromEntities(eq(request), any(OutputStream.class));
    }

    @Test
    void testGenerateReport_ResultsXlsx() throws Exception {
        byte[] expectedBytes = "fake-results-xlsx-content".getBytes(StandardCharsets.UTF_8);
        doAnswer(invocation -> {
            OutputStream os = invocation.getArgument(1);
            os.write(expectedBytes);
            return null;
        }).when(reportService).exportXlsx(any(ReportRequestModel.class), any(OutputStream.class));

        ReportRequestModel request = new ReportRequestModel();
        request.setReportType(ReportType.RESULTS_XLSX);

        ResponseEntity<StreamingResponseBody> response = controller.generateReport(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("attachment; filename=ReMi_RESULTS.xlsx", response.getHeaders().getFirst("Content-Disposition"));
        assertEquals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                response.getHeaders().getContentType().toString());

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        response.getBody().writeTo(baos);
        assertArrayEquals(expectedBytes, baos.toByteArray());

        verify(reportService, times(1)).exportXlsx(eq(request), any(OutputStream.class));
    }

    @Test
    void testGenerateReport_ResultsCsv() throws Exception {
        byte[] expectedBytes = "fake-results-csv-zip-content".getBytes(StandardCharsets.UTF_8);
        doAnswer(invocation -> {
            OutputStream os = invocation.getArgument(1);
            os.write(expectedBytes);
            return null;
        }).when(reportService).writeCsvZipFromEntities(any(ReportRequestModel.class), any(OutputStream.class));

        ReportRequestModel request = new ReportRequestModel();
        request.setReportType(ReportType.RESULTS_CSV);

        ResponseEntity<StreamingResponseBody> response = controller.generateReport(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("attachment; filename=results-report.zip", response.getHeaders().getFirst("Content-Disposition"));
        assertEquals("application/zip", response.getHeaders().getContentType().toString());

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        response.getBody().writeTo(baos);
        assertArrayEquals(expectedBytes, baos.toByteArray());

        verify(reportService, times(1)).writeCsvZipFromEntities(eq(request), any(OutputStream.class));
    }

    @Test
    void testGenerateReport_ResultsSpatial() throws Exception {
        byte[] expectedBytes = "fake-spatial-zip-content".getBytes(StandardCharsets.UTF_8);
        doAnswer(invocation -> {
            OutputStream os = invocation.getArgument(1);
            os.write(expectedBytes);
            return true;
        }).when(reportService).exportResultsSpatialZip(any(ReportRequestModel.class), any(OutputStream.class));

        ReportRequestModel request = new ReportRequestModel();
        request.setReportType(ReportType.RESULTS_SPATIAL);

        ResponseEntity<StreamingResponseBody> response = controller.generateReport(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("attachment; filename=ReMi_RESULTS_Spatial.zip", response.getHeaders().getFirst("Content-Disposition"));
        assertEquals("application/zip", response.getHeaders().getContentType().toString());

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        response.getBody().writeTo(baos);
        assertArrayEquals(expectedBytes, baos.toByteArray());
    }

    @Test
    void testGenerateReport_ResultsSpatial_NoFiles_ReturnsNoContent() throws Exception {
        doAnswer(invocation -> false)
                .when(reportService).exportResultsSpatialZip(any(ReportRequestModel.class), any(OutputStream.class));

        ReportRequestModel request = new ReportRequestModel();
        request.setReportType(ReportType.RESULTS_SPATIAL);

        ResponseEntity<StreamingResponseBody> response = controller.generateReport(request);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void testGenerateReport_NullType_ReturnsBadRequest() throws Exception {
        ReportRequestModel request = new ReportRequestModel();
        request.setReportType(null);

        ResponseEntity<StreamingResponseBody> response = controller.generateReport(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        response.getBody().writeTo(baos);
        assertEquals("Only reportType=PROJECT_XLSX, RESULTS_XLSX, PROJECT_CSV, RESULTS_CSV, or RESULTS_SPATIAL is supported.", baos.toString(StandardCharsets.UTF_8));
        verifyNoInteractions(reportService);
    }

    @ParameterizedTest
    @ValueSource(strings = {"PROJECT_XLSX", "project_xlsx", "Project_Xlsx", "PROJECT_CSV", "project_csv", "RESULTS_XLSX", "results_xlsx", "RESULTS_CSV", "results_csv", "RESULTS_SPATIAL", "results_spatial"})
    void testReportType_FromString_CaseInsensitive(String value) {
        ReportType type = ReportType.fromString(value);
        assertNotNull(type);
        assertEquals(value.toUpperCase(), type.name());
    }

    @Test
    void testReportType_FromString_Null() {
        assertNull(ReportType.fromString(null));
    }

    @Test
    void testReportType_FromString_Invalid() {
        assertThrows(IllegalArgumentException.class, () -> ReportType.fromString("UNKNOWN"));
    }

    @ParameterizedTest
    @EnumSource(ReportType.class)
    void testReportTypeEnumValues(ReportType type) {
        assertNotNull(type);
    }
}
