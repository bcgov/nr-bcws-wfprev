package ca.bc.gov.nrs.reportgenerator;

import ca.bc.gov.nrs.reportgenerator.model.LambdaEvent;
import ca.bc.gov.nrs.reportgenerator.model.ProjectFuelManagementReportData;
import ca.bc.gov.nrs.reportgenerator.model.Report;
import ca.bc.gov.nrs.reportgenerator.model.ReportType;
import ca.bc.gov.nrs.reportgenerator.model.XlsxReportData;
import ca.bc.gov.nrs.reportgenerator.service.XlsxReportBuilder;
import ca.bc.gov.nrs.reportgenerator.service.XlsxReportBuilder.GeneratedXlsx;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** The report job path: rows in from S3, one XLSX out to S3. */
class LambdaHandlerExportJobTest {

    private final ObjectMapper mapper = new ObjectMapper();
    private S3Client s3;
    private XlsxReportBuilder builder;
    private LambdaHandler handler;

    @BeforeEach
    void setUp() {
        s3 = mock(S3Client.class);
        builder = mock(XlsxReportBuilder.class);
        handler = new LambdaHandler();
        handler.s3 = s3;
        handler.xlsxReportBuilder = builder;
    }

    private byte[] rows() throws Exception {
        XlsxReportData data = new XlsxReportData();
        data.setProjectFuelManagementReportData(List.of(new ProjectFuelManagementReportData()));
        Report report = new Report();
        report.setReportType(ReportType.XLSX);
        report.setReportName("ReMi_Fiscal");
        report.setXlsxReportData(data);
        LambdaEvent event = new LambdaEvent();
        event.setReports(List.of(report));
        return mapper.writeValueAsBytes(event);
    }

    private ByteArrayOutputStream invoke(Map<String, String> event) throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        handler.handleRequest(new ByteArrayInputStream(mapper.writeValueAsBytes(event)), output, null);
        return output;
    }

    @Test
    void exportJob_readsTheRowsFromS3AndWritesTheXlsxBack() throws Exception {
        when(s3.getObjectAsBytes(any(GetObjectRequest.class)))
                .thenReturn(ResponseBytes.fromByteArray(GetObjectResponse.builder().build(), rows()));
        byte[] xlsx = "xlsx-bytes".getBytes(StandardCharsets.UTF_8);
        when(builder.build(any())).thenReturn(List.of(new GeneratedXlsx("ReMi_Fiscal.xlsx", xlsx)));

        ByteArrayOutputStream output = invoke(Map.of(
                "jobGuid", "abc",
                "bucket", "wfprev-test-report-exports",
                "inputKey", "jobs/abc/input.json",
                "outputKey", "jobs/abc/ReMi_Fiscal.xlsx"));

        ArgumentCaptor<GetObjectRequest> get = ArgumentCaptor.forClass(GetObjectRequest.class);
        verify(s3).getObjectAsBytes(get.capture());
        assertEquals("wfprev-test-report-exports", get.getValue().bucket());
        assertEquals("jobs/abc/input.json", get.getValue().key());

        ArgumentCaptor<LambdaEvent> event = ArgumentCaptor.forClass(LambdaEvent.class);
        verify(builder).build(event.capture());
        assertEquals("ReMi_Fiscal", event.getValue().getReports().get(0).getReportName());

        ArgumentCaptor<PutObjectRequest> put = ArgumentCaptor.forClass(PutObjectRequest.class);
        ArgumentCaptor<RequestBody> body = ArgumentCaptor.forClass(RequestBody.class);
        verify(s3).putObject(put.capture(), body.capture());
        assertEquals("wfprev-test-report-exports", put.getValue().bucket());
        assertEquals("jobs/abc/ReMi_Fiscal.xlsx", put.getValue().key());
        assertTrue(put.getValue().contentType().contains("spreadsheetml"));
        assertArrayEquals(xlsx, body.getValue().contentStreamProvider().newStream().readAllBytes());

        JsonNode reply = mapper.readTree(output.toByteArray());
        assertTrue(reply.path("ok").asBoolean());
        assertEquals("jobs/abc/ReMi_Fiscal.xlsx", reply.path("outputKey").asText());
        assertEquals(xlsx.length, reply.path("bytes").asInt());
    }

    @Test
    void exportJob_whenNothingIsGenerated_throwsSoTheApiSeesAFunctionError() throws Exception {
        when(s3.getObjectAsBytes(any(GetObjectRequest.class)))
                .thenReturn(ResponseBytes.fromByteArray(GetObjectResponse.builder().build(), rows()));
        when(builder.build(any())).thenReturn(List.of());

        assertThrows(IllegalStateException.class, () -> invoke(Map.of(
                "bucket", "wfprev-test-report-exports",
                "inputKey", "jobs/abc/input.json",
                "outputKey", "jobs/abc/ReMi_Fiscal.xlsx")));
        verify(s3, never()).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    @Test
    void exportJob_withoutAnOutputKey_isRejected() {
        assertThrows(IllegalArgumentException.class, () -> invoke(Map.of(
                "bucket", "wfprev-test-report-exports",
                "inputKey", "jobs/abc/input.json")));
    }

    @Test
    void functionUrlRequest_stillReturnsTheFileInline() throws Exception {
        byte[] xlsx = "inline".getBytes(StandardCharsets.UTF_8);
        when(builder.build(any())).thenReturn(List.of(new GeneratedXlsx("ReMi_Fiscal.xlsx", xlsx)));

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        handler.handleRequest(new ByteArrayInputStream(rows()), output, null);

        JsonNode reply = mapper.readTree(output.toByteArray());
        assertEquals(200, reply.path("statusCode").asInt());
        JsonNode files = mapper.readTree(reply.path("body").asText()).path("files");
        assertEquals("ReMi_Fiscal.xlsx", files.get(0).path("filename").asText());
        verify(s3, never()).getObjectAsBytes(any(GetObjectRequest.class));
    }
}
