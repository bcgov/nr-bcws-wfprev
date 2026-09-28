package ca.bc.gov.nrs.wfprev.services.reportjobs;

import ca.bc.gov.nrs.wfprev.services.XlsxReportGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.lambda.model.InvocationType;
import software.amazon.awssdk.services.lambda.model.InvokeRequest;
import software.amazon.awssdk.services.lambda.model.InvokeResponse;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ReportLambdaInvokerTest {

    private final ObjectMapper mapper = new ObjectMapper();
    private ReportExportStore store;
    private LambdaClient lambda;
    private ReportLambdaInvoker invoker;

    @BeforeEach
    void setUp() {
        ReportJobProperties properties = new ReportJobProperties();
        properties.setBucket("wfprev-test-report-exports");
        properties.setLambdaFunctionName("report-generator-test");
        store = mock(ReportExportStore.class);
        lambda = mock(LambdaClient.class);
        invoker = new ReportLambdaInvoker(properties, store, lambda);
    }

    private static XlsxReportGenerator.LambdaReportRequest request() {
        XlsxReportGenerator.LambdaReportRequest request = new XlsxReportGenerator.LambdaReportRequest();
        XlsxReportGenerator.LambdaReportRequest.Report report = new XlsxReportGenerator.LambdaReportRequest.Report();
        report.setReportType("XLSX");
        report.setReportName("ReMi_RESULTS");
        request.setReports(List.of(report));
        return request;
    }

    private void lambdaReturns(String payload, String functionError) {
        when(lambda.invoke(any(InvokeRequest.class))).thenReturn(InvokeResponse.builder()
                .statusCode(200)
                .functionError(functionError)
                .payload(SdkBytes.fromUtf8String(payload))
                .build());
    }

    @Test
    void generateXlsx_writesTheInputToS3AndInvokesTheLambdaSynchronously() throws Exception {
        UUID guid = UUID.randomUUID();
        String outputKey = ReportFiles.outputKey(guid, "ReMi_RESULTS.xlsx");
        lambdaReturns("{\"ok\":true}", null);

        invoker.generateXlsx(guid, request(), outputKey);

        ArgumentCaptor<byte[]> input = ArgumentCaptor.forClass(byte[].class);
        verify(store).putBytes(eq("jobs/" + guid + "/input.json"), input.capture(), eq("application/json"));
        JsonNode inputJson = mapper.readTree(input.getValue());
        assertEquals("ReMi_RESULTS", inputJson.path("reports").get(0).path("reportName").asText());

        ArgumentCaptor<InvokeRequest> invoke = ArgumentCaptor.forClass(InvokeRequest.class);
        verify(lambda).invoke(invoke.capture());
        assertEquals("report-generator-test", invoke.getValue().functionName());
        assertEquals(InvocationType.REQUEST_RESPONSE, invoke.getValue().invocationType());
        JsonNode event = mapper.readTree(invoke.getValue().payload().asUtf8String());
        assertEquals("wfprev-test-report-exports", event.path("bucket").asText());
        assertEquals("jobs/" + guid + "/input.json", event.path("inputKey").asText());
        assertEquals(outputKey, event.path("outputKey").asText());
        assertEquals(guid.toString(), event.path("jobGuid").asText());
    }

    @Test
    void generateXlsx_whenTheFunctionErrors_throws() {
        lambdaReturns("{\"errorMessage\":\"boom\"}", "Unhandled");

        assertThrows(IllegalStateException.class,
                () -> invoker.generateXlsx(UUID.randomUUID(), request(), "jobs/x/ReMi_RESULTS.xlsx"));
    }

    @Test
    void generateXlsx_whenTheLambdaDoesntConfirmTheFile_throws() {
        // e.g. an old Lambda build that still answers with the Function URL response
        lambdaReturns("{\"statusCode\":400,\"message\":\"No reports provided\"}", null);

        assertThrows(IllegalStateException.class,
                () -> invoker.generateXlsx(UUID.randomUUID(), request(), "jobs/x/ReMi_RESULTS.xlsx"));
    }
}
