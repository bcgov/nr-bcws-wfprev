package ca.bc.gov.nrs.wfprev.services.reportjobs;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.net.URI;
import java.nio.file.Path;
import java.time.Duration;

/**
 * The export bucket. Objects appear only when completely written, so {@link #exists} is also a
 * "the file is complete" check.
 */
@Slf4j
@Component
public class ReportExportStore {

    private final ReportJobProperties properties;
    private S3Client s3;
    private S3Presigner presigner;

    @Autowired
    public ReportExportStore(ReportJobProperties properties) {
        this.properties = properties;
    }

    /** For tests. */
    ReportExportStore(ReportJobProperties properties, S3Client s3, S3Presigner presigner) {
        this.properties = properties;
        this.s3 = s3;
        this.presigner = presigner;
    }

    public void putBytes(String key, byte[] content, String contentType) {
        client().putObject(putRequest(key, contentType), RequestBody.fromBytes(content));
    }

    public void putFile(String key, Path file, String contentType) {
        client().putObject(putRequest(key, contentType), RequestBody.fromFile(file));
    }

    public boolean exists(String key) {
        try {
            client().headObject(HeadObjectRequest.builder().bucket(bucket()).key(key).build());
            return true;
        } catch (NoSuchKeyException e) {
            return false;
        } catch (S3Exception e) {
            if (e.statusCode() == 404) {
                return false;
            }
            throw e;
        }
    }

    /** A GET link that saves the file under {@code fileName}. */
    public String presignDownload(String key, String fileName, String contentType, Duration lifetime) {
        GetObjectRequest get = GetObjectRequest.builder()
                .bucket(bucket())
                .key(key)
                .responseContentDisposition("attachment; filename=\"" + fileName + "\"")
                .responseContentType(contentType)
                .build();
        return presigner().presignGetObject(GetObjectPresignRequest.builder()
                        .signatureDuration(lifetime)
                        .getObjectRequest(get)
                        .build())
                .url()
                .toString();
    }

    private PutObjectRequest putRequest(String key, String contentType) {
        return PutObjectRequest.builder().bucket(bucket()).key(key).contentType(contentType).build();
    }

    private String bucket() {
        String bucket = properties.getBucket();
        if (bucket == null || bucket.isBlank()) {
            throw new IllegalStateException("REPORT_EXPORT_BUCKET is not set");
        }
        return bucket;
    }

    private synchronized S3Client client() {
        if (s3 == null) {
            S3ClientBuilder builder = S3Client.builder()
                    .region(Region.of(properties.getRegion()))
                    .credentialsProvider(credentials())
                    .httpClientBuilder(ApacheHttpClient.builder());
            if (isSet(properties.getS3Endpoint())) {
                builder.endpointOverride(URI.create(properties.getS3Endpoint())).forcePathStyle(true);
            }
            s3 = builder.build();
        }
        return s3;
    }

    private synchronized S3Presigner presigner() {
        if (presigner == null) {
            S3Presigner.Builder builder = S3Presigner.builder()
                    .region(Region.of(properties.getRegion()))
                    .credentialsProvider(credentials());
            String endpoint = isSet(properties.getS3PublicEndpoint()) ? properties.getS3PublicEndpoint() : properties.getS3Endpoint();
            if (isSet(endpoint)) {
                builder.endpointOverride(URI.create(endpoint))
                        .serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(true).build());
            }
            presigner = builder.build();
        }
        return presigner;
    }

    /** The task role in AWS; any key works against the local S3 mock. */
    private AwsCredentialsProvider credentials() {
        if (isSet(properties.getS3Endpoint()) && System.getenv("AWS_ACCESS_KEY_ID") == null) {
            return StaticCredentialsProvider.create(AwsBasicCredentials.create("local", "local"));
        }
        return DefaultCredentialsProvider.builder().build();
    }

    private static boolean isSet(String value) {
        return value != null && !value.isBlank();
    }

    @PreDestroy
    synchronized void close() {
        if (s3 != null) {
            s3.close();
        }
        if (presigner != null) {
            presigner.close();
        }
    }
}
