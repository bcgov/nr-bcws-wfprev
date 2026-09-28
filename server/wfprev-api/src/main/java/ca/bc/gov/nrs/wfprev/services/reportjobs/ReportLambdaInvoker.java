package ca.bc.gov.nrs.wfprev.services.reportjobs;

import ca.bc.gov.nrs.wfprev.services.XlsxReportGenerator.LambdaReportRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.lambda.model.InvocationType;
import software.amazon.awssdk.services.lambda.model.InvokeRequest;
import software.amazon.awssdk.services.lambda.model.InvokeResponse;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Has the report Lambda build an XLSX: writes its input to S3, invokes it synchronously and waits.
 * This is a direct call to the Lambda service, so API Gateway's 30 s limit doesn't apply; the
 * client waits a little longer than the Lambda's own timeout. Input and output both go through S3,
 * so neither is limited by Lambda's payload size.
 */
@Slf4j
@Component
public class ReportLambdaInvoker {

    private final ReportJobProperties properties;
    private final ReportExportStore store;
    private final LambdaClient lambda;
    // Same serialization as the old Function URL call, so the Lambda sees identical rows.
    private final ObjectMapper mapper = new ObjectMapper();

    public ReportLambdaInvoker(ReportJobProperties properties, ReportExportStore store, LambdaClient lambda) {
        this.properties = properties;
        this.store = store;
        this.lambda = lambda;
    }

    /** Returns once the XLSX is in S3 at {@code outputKey}; throws otherwise. */
    public void generateXlsx(UUID jobGuid, LambdaReportRequest request, String outputKey) {
        String inputKey = ReportFiles.inputKey(jobGuid);
        try {
            store.putBytes(inputKey, mapper.writeValueAsBytes(request), "application/json");
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Couldn't serialize report rows for job " + jobGuid, e);
        }

        Map<String, String> event = new LinkedHashMap<>();
        event.put("jobGuid", jobGuid.toString());
        event.put("bucket", properties.getBucket());
        event.put("inputKey", inputKey);
        event.put("outputKey", outputKey);

        String eventJson;
        try {
            eventJson = mapper.writeValueAsString(event);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Couldn't serialize the Lambda event for job " + jobGuid, e);
        }

        long started = System.currentTimeMillis();
        InvokeResponse response = lambda.invoke(InvokeRequest.builder()
                .functionName(functionName())
                .invocationType(InvocationType.REQUEST_RESPONSE)
                .payload(SdkBytes.fromUtf8String(eventJson))
                .build());
        String payload = response.payload() == null ? "" : response.payload().asString(StandardCharsets.UTF_8);
        log.info("Report job {}: Lambda returned in {} ms (status {}, functionError {})",
                jobGuid, System.currentTimeMillis() - started, response.statusCode(), response.functionError());

        if (response.functionError() != null) {
            throw new IllegalStateException("Report Lambda failed for job " + jobGuid + ": " + payload);
        }
        if (!isOk(payload)) {
            throw new IllegalStateException("Report Lambda didn't confirm the file for job " + jobGuid + ": " + payload);
        }
    }

    private boolean isOk(String payload) {
        try {
            JsonNode root = mapper.readTree(payload);
            return root != null && root.path("ok").asBoolean(false);
        } catch (JsonProcessingException e) {
            return false;
        }
    }

    private String functionName() {
        String name = properties.getLambdaFunctionName();
        if (name == null || name.isBlank()) {
            throw new IllegalStateException("REPORT_GENERATOR_FUNCTION_NAME is not set");
        }
        return name;
    }
}
