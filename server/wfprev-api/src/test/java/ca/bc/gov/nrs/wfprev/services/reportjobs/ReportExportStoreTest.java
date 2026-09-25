package ca.bc.gov.nrs.wfprev.services.reportjobs;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ReportExportStoreTest {

    private S3Client s3;
    private ReportExportStore store;

    @BeforeEach
    void setUp() {
        ReportJobProperties properties = new ReportJobProperties();
        properties.setBucket("wfprev-test-report-exports");
        properties.setRegion("ca-central-1");
        s3 = mock(S3Client.class);
        S3Presigner presigner = S3Presigner.builder()
                .region(Region.CA_CENTRAL_1)
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create("key", "secret")))
                .build();
        store = new ReportExportStore(properties, s3, presigner);
    }

    @Test
    void exists_whenTheObjectIsThere() {
        when(s3.headObject(any(HeadObjectRequest.class))).thenReturn(HeadObjectResponse.builder().build());

        assertTrue(store.exists("jobs/a/ReMi_RESULTS.xlsx"));

        ArgumentCaptor<HeadObjectRequest> head = ArgumentCaptor.forClass(HeadObjectRequest.class);
        verify(s3).headObject(head.capture());
        assertEquals("wfprev-test-report-exports", head.getValue().bucket());
        assertEquals("jobs/a/ReMi_RESULTS.xlsx", head.getValue().key());
    }

    @Test
    void exists_isFalseForNoSuchKey() {
        when(s3.headObject(any(HeadObjectRequest.class))).thenThrow(NoSuchKeyException.builder().build());

        assertFalse(store.exists("jobs/a/missing.zip"));
    }

    @Test
    void exists_isFalseForA404() {
        when(s3.headObject(any(HeadObjectRequest.class)))
                .thenThrow(S3Exception.builder().statusCode(404).build());

        assertFalse(store.exists("jobs/a/missing.zip"));
    }

    @Test
    void exists_rethrowsOtherErrors() {
        // A 403 means a permission problem, not a missing file; the sweep must not fail the job over it.
        when(s3.headObject(any(HeadObjectRequest.class)))
                .thenThrow(S3Exception.builder().statusCode(403).build());

        assertThrows(S3Exception.class, () -> store.exists("jobs/a/ReMi_RESULTS.xlsx"));
    }

    @Test
    void putBytes_writesToTheBucketWithTheContentType() {
        store.putBytes("jobs/a/input.json", "{}".getBytes(StandardCharsets.UTF_8), "application/json");

        ArgumentCaptor<PutObjectRequest> put = ArgumentCaptor.forClass(PutObjectRequest.class);
        verify(s3).putObject(put.capture(), any(RequestBody.class));
        assertEquals("wfprev-test-report-exports", put.getValue().bucket());
        assertEquals("jobs/a/input.json", put.getValue().key());
        assertEquals("application/json", put.getValue().contentType());
    }

    @Test
    void presignDownload_savesUnderTheFileNameAndExpires() {
        String url = store.presignDownload("jobs/a/ReMi_RESULTS.xlsx", "ReMi_RESULTS.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", Duration.ofMinutes(5));
        String decoded = URLDecoder.decode(url, StandardCharsets.UTF_8);

        assertTrue(url.startsWith("https://wfprev-test-report-exports.s3.ca-central-1.amazonaws.com/jobs/a/ReMi_RESULTS.xlsx?"), url);
        assertTrue(decoded.contains("response-content-disposition=attachment; filename=\"ReMi_RESULTS.xlsx\""), decoded);
        assertTrue(decoded.contains("X-Amz-Expires=300"), decoded);
    }

    @Test
    void anyCall_withoutABucket_failsClearly() {
        ReportJobProperties unset = new ReportJobProperties();
        ReportExportStore unconfigured = new ReportExportStore(unset, s3, null);

        assertThrows(IllegalStateException.class, () -> unconfigured.exists("jobs/a/x"));
    }
}
