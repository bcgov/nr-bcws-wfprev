package ca.bc.gov.nrs.wfprev.services.reportjobs;

import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.nio.file.Path;
import java.time.Duration;

/**
 * The export bucket. Objects appear only when completely written, so {@link #exists} is also a
 * "the file is complete" check.
 */
@Component
public class ReportExportStore {

    private final ReportJobProperties properties;
    private final S3Client s3;
    private final S3Presigner presigner;

    public ReportExportStore(ReportJobProperties properties, S3Client s3, S3Presigner presigner) {
        this.properties = properties;
        this.s3 = s3;
        this.presigner = presigner;
    }

    public void putBytes(String key, byte[] content, String contentType) {
        s3.putObject(putRequest(key, contentType), RequestBody.fromBytes(content));
    }

    public void putFile(String key, Path file, String contentType) {
        s3.putObject(putRequest(key, contentType), RequestBody.fromFile(file));
    }

    public boolean exists(String key) {
        try {
            s3.headObject(HeadObjectRequest.builder().bucket(bucket()).key(key).build());
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
        return presigner.presignGetObject(GetObjectPresignRequest.builder()
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
}
