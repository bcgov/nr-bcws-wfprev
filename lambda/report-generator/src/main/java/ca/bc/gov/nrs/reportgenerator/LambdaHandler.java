package ca.bc.gov.nrs.reportgenerator;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import org.jboss.logging.Logger;
import ca.bc.gov.nrs.reportgenerator.service.XlsxReportBuilder;
import ca.bc.gov.nrs.reportgenerator.service.XlsxReportBuilder.GeneratedXlsx;
import ca.bc.gov.nrs.reportgenerator.model.LambdaEvent;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import jakarta.inject.Inject;

/**
 * Builds report XLSX files. Two event shapes:
 * <ul>
 *   <li>A report job from the API: {@code {"jobGuid","bucket","inputKey","outputKey"}}. The rows are
 *       read from S3, the XLSX is written back to S3, and the reply is {@code {"ok":true,...}}. Any
 *       failure is thrown, so the API sees it as a function error.</li>
 *   <li>The old Function URL request carrying the rows inline, answered with the files base64-encoded.</li>
 * </ul>
 */
public class LambdaHandler implements RequestStreamHandler {
    private static final Logger LOG = Logger.getLogger(LambdaHandler.class);
    private static final String XLSX_CONTENT_TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    private final ObjectMapper mapper = new ObjectMapper();

    @Inject
    XlsxReportBuilder xlsxReportBuilder;

    @Inject
    S3Client s3;

    @Override
    public void handleRequest(InputStream input, OutputStream output, Context context) throws IOException {
        byte[] payload = input.readAllBytes();
        String inputJson = new String(payload);
        JsonNode root;
        try {
            root = mapper.readTree(inputJson);
        } catch (Exception e) {
            LOG.error("Failed to parse " + payload.length + "-byte input", e);
            writeErrorResponse(output, "Invalid input: " + e.getMessage());
            return;
        }

        // Scheduled keep-warm ping from EventBridge (terraform/lambda.tf): nothing to generate
        if (root != null && root.path("warmup").asBoolean(false)) {
            LOG.debug("Warm-up ping");
            mapper.writeValue(output, Map.of("statusCode", 200, "body", "{\"warmup\":true}"));
            return;
        }

        if (root != null && root.hasNonNull("inputKey")) {
            handleExportJob(root, output);
            return;
        }

        LambdaEvent event;
        try {
            // Try to parse as wrapper object first
            if (root.has("body")) {
                String bodyJson = root.get("body").asText();
                event = mapper.readValue(bodyJson, LambdaEvent.class);
            } else {
                event = mapper.readValue(inputJson, LambdaEvent.class);
            }
        } catch (Exception e) {
            LOG.error("Failed to deserialize " + payload.length + "-byte input", e);
            writeErrorResponse(output, "Invalid input: " + e.getMessage());
            return;
        }

        // Input validation
        if (event.getReports() == null || event.getReports().isEmpty()) {
            LOG.warn("No reports provided");
            writeErrorResponse(output, "No reports provided");
            return;
        }

        // Log a summary only: the rows hold user data (emails, names) and run to hundreds of KB
        LOG.infof("Received request to generate %d report(s), payload %d bytes", event.getReports().size(), payload.length);

        List<Map<String, String>> files = new ArrayList<>();
        for (GeneratedXlsx file : xlsxReportBuilder.build(event)) {
            files.add(Map.of(
                "filename", file.filename(),
                "content", Base64.getEncoder().encodeToString(file.content())
            ));
        }

        if (files.isEmpty()) {
            LOG.error("No valid XLSX files generated");
            writeErrorResponse(output, "No valid XLSX files generated");
            return;
        }

        Map<String, Object> response = new HashMap<>();
        response.put("statusCode", 200);
        response.put("headers", Map.of("Content-Type", "application/json"));
        response.put("body", mapper.writeValueAsString(Map.of("files", files)));
        response.put("isBase64Encoded", false);

        mapper.writeValue(output, response);
    }

    /** A report job: rows from S3 in, one XLSX to S3 out. Failures are thrown, not answered. */
    private void handleExportJob(JsonNode job, OutputStream output) throws IOException {
        String jobGuid = job.path("jobGuid").asText("");
        String bucket = job.hasNonNull("bucket") && !job.get("bucket").asText().isBlank()
                ? job.get("bucket").asText()
                : System.getenv("REPORT_EXPORT_BUCKET");
        String inputKey = job.get("inputKey").asText();
        String outputKey = job.path("outputKey").asText("");
        if (bucket == null || bucket.isBlank() || outputKey.isBlank()) {
            throw new IllegalArgumentException("Report job " + jobGuid + " needs a bucket, inputKey and outputKey");
        }
        LOG.infof("Report job %s: reading %s from %s", jobGuid, inputKey, bucket);

        long started = System.currentTimeMillis();
        byte[] rows = s3.getObjectAsBytes(GetObjectRequest.builder().bucket(bucket).key(inputKey).build()).asByteArray();
        LambdaEvent event = mapper.readValue(rows, LambdaEvent.class);
        if (event.getReports() == null || event.getReports().isEmpty()) {
            throw new IllegalArgumentException("Report job " + jobGuid + ": no reports in " + inputKey);
        }

        List<GeneratedXlsx> files = xlsxReportBuilder.build(event);
        if (files.isEmpty()) {
            throw new IllegalStateException("Report job " + jobGuid + ": no XLSX was generated");
        }
        GeneratedXlsx file = files.get(0);

        s3.putObject(PutObjectRequest.builder().bucket(bucket).key(outputKey).contentType(XLSX_CONTENT_TYPE).build(),
                RequestBody.fromBytes(file.content()));
        LOG.infof("Report job %s: wrote %d bytes to %s in %d ms", jobGuid, file.content().length, outputKey,
                System.currentTimeMillis() - started);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("ok", true);
        response.put("outputKey", outputKey);
        response.put("bytes", file.content().length);
        mapper.writeValue(output, response);
    }

    private void writeErrorResponse(OutputStream output, String message) throws IOException {
        Map<String, Object> response = new HashMap<>();
        response.put("statusCode", 400);
        response.put("error", true);
        response.put("message", message);
        response.put("headers", Map.of("Content-Type", "text/plain"));
        response.put("isBase64Encoded", false);
        mapper.writeValue(output, response);
    }
}
