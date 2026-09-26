package ca.bc.gov.nrs.reportgenerator;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.List;
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
 * Builds one report XLSX for a report export job. The API invokes it synchronously with
 * {@code {"jobGuid","bucket","inputKey","outputKey"}}: the rows are read from S3, the XLSX is
 * written back to S3, and the reply is {@code {"ok":true,...}}. Any failure is thrown, so the API
 * sees it as a function error. The scheduled {@code {"warmup":true}} ping returns straight away.
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
        JsonNode job = mapper.readTree(input.readAllBytes());
        // Scheduled keep-warm ping from EventBridge (terraform/lambda.tf): nothing to generate
        if (job != null && job.path("warmup").asBoolean(false)) {
            LOG.debug("Warm-up ping");
            mapper.writeValue(output, Map.of("statusCode", 200, "body", "{\"warmup\":true}"));
            return;
        }
        if (job == null || !job.hasNonNull("inputKey")) {
            throw new IllegalArgumentException("Expected a report job event with an inputKey");
        }

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
}
