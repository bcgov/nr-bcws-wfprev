package ca.bc.gov.nrs.wfprev.services.reportjobs;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The store against a real S3 API (the same mock local development uses): upload, the existence
 * check the sweep relies on, and a presigned download the browser would follow.
 */
@Testcontainers
class ReportExportStoreS3MockTest {

    private static final String BUCKET = "wfprev-test-report-exports";

    @Container
    static final GenericContainer<?> S3 = new GenericContainer<>("adobe/s3mock:5.2.3")
            .withEnv("COM_ADOBE_TESTING_S3MOCK_STORE_INITIAL_BUCKETS", BUCKET)
            .withExposedPorts(9090)
            .waitingFor(Wait.forHttp("/").forPort(9090).forStatusCodeMatching(code -> code < 500));

    private static S3Client s3;
    private static S3Presigner presigner;
    private static ReportExportStore store;

    /** Clients built the way the local profile builds them. */
    @BeforeAll
    static void createStore() {
        ReportJobProperties properties = new ReportJobProperties();
        properties.setBucket(BUCKET);
        properties.setRegion("ca-central-1");
        String endpoint = "http://" + S3.getHost() + ":" + S3.getMappedPort(9090);
        LocalReportJobConfig local = new LocalReportJobConfig();
        s3 = local.reportExportS3Client(properties, endpoint);
        presigner = local.reportExportS3Presigner(properties, endpoint);
        store = new ReportExportStore(properties, s3, presigner);
    }

    @AfterAll
    static void closeClients() {
        s3.close();
        presigner.close();
    }

    @Test
    void uploadedFile_existsAndDownloadsThroughAPresignedLink() throws Exception {
        String key = "jobs/0f6c1a2e-0000-0000-0000-000000000001/ReMi_RESULTS_Spatial.zip";
        byte[] content = "PK-fake-zip".getBytes(StandardCharsets.UTF_8);
        Path file = Files.createTempFile("store-test-", ".zip");
        Files.write(file, content);

        assertFalse(store.exists(key));
        store.putFile(key, file, "application/zip");
        assertTrue(store.exists(key));

        String url = store.presignDownload(key, "ReMi_RESULTS_Spatial.zip", "application/zip", Duration.ofMinutes(5));
        HttpResponse<byte[]> response = HttpClient.newHttpClient()
                .send(HttpRequest.newBuilder(URI.create(url)).GET().build(), HttpResponse.BodyHandlers.ofByteArray());

        assertEquals(200, response.statusCode());
        assertArrayEquals(content, response.body());
        assertEquals("attachment; filename=\"ReMi_RESULTS_Spatial.zip\"",
                response.headers().firstValue("Content-Disposition").orElse(""));
        Files.deleteIfExists(file);
    }

    @Test
    void putBytes_writesJsonInput() {
        String key = "jobs/0f6c1a2e-0000-0000-0000-000000000002/input.json";

        store.putBytes(key, "{\"reports\":[]}".getBytes(StandardCharsets.UTF_8), "application/json");

        assertTrue(store.exists(key));
    }
}
