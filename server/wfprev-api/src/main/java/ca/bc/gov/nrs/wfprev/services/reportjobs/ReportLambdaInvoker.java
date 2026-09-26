package ca.bc.gov.nrs.wfprev.services.reportjobs;

import ca.bc.gov.nrs.wfprev.services.XlsxReportGenerator.LambdaReportRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.awscore.retry.AwsRetryStrategy;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.lambda.LambdaClientBuilder;
import software.amazon.awssdk.services.lambda.model.InvocationType;
import software.amazon.awssdk.services.lambda.model.InvokeRequest;
import software.amazon.awssdk.services.lambda.model.InvokeResponse;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
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

    /** Extra wait beyond the Lambda timeout, for the invoke round trip. */
    private static final Duration CALL_MARGIN = Duration.ofSeconds(60);

    private final ReportJobProperties properties;
    private final ReportExportStore store;
    // Same serialization as the old Function URL call, so the Lambda sees identical rows.
    private final ObjectMapper mapper = new ObjectMapper();
    private LambdaClient lambda;

    @Autowired
    public ReportLambdaInvoker(ReportJobProperties properties, ReportExportStore store) {
        this.properties = properties;
        this.store = store;
    }

    /** For tests. */
    ReportLambdaInvoker(ReportJobProperties properties, ReportExportStore store, LambdaClient lambda) {
        this(properties, store);
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
        InvokeResponse response = client().invoke(InvokeRequest.builder()
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

    private synchronized LambdaClient client() {
        if (lambda == null) {
            Duration timeout = properties.lambdaTimeout().plus(CALL_MARGIN);
            LambdaClientBuilder builder = LambdaClient.builder()
                    .region(Region.of(properties.getRegion()))
                    .credentialsProvider(credentials())
                    .httpClientBuilder(ApacheHttpClient.builder()
                            .socketTimeout(timeout)
                            .connectionTimeout(Duration.ofSeconds(10)))
                    .overrideConfiguration(ClientOverrideConfiguration.builder()
                            .apiCallTimeout(timeout)
                            .apiCallAttemptTimeout(timeout)
                            // A retried invoke would build the file twice; a failure surfaces as Retry instead.
                            .retryStrategy(AwsRetryStrategy.doNotRetry())
                            .build());
            if (isSet(properties.getLambdaEndpoint())) {
                builder.endpointOverride(URI.create(properties.getLambdaEndpoint()));
            }
            lambda = builder.build();
        }
        return lambda;
    }

    private AwsCredentialsProvider credentials() {
        if (isSet(properties.getLambdaEndpoint()) && System.getenv("AWS_ACCESS_KEY_ID") == null) {
            return StaticCredentialsProvider.create(AwsBasicCredentials.create("local", "local"));
        }
        return DefaultCredentialsProvider.builder().build();
    }

    private static boolean isSet(String value) {
        return value != null && !value.isBlank();
    }

    @PreDestroy
    synchronized void close() {
        if (lambda != null) {
            lambda.close();
        }
    }
}
