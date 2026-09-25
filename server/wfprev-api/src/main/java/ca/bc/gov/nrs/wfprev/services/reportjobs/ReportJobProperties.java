package ca.bc.gov.nrs.wfprev.services.reportjobs;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;

/** Settings for report export jobs; see {@code reportJobs} in application.yaml. */
@Getter
@Setter
@Component
public class ReportJobProperties {

    /** The private bucket finished files and Lambda input are written to. */
    @Value("${reportJobs.bucket:}")
    private String bucket;

    @Value("${reportJobs.region:ca-central-1}")
    private String region;

    /** Local development only: an S3-compatible endpoint for the API to use. */
    @Value("${reportJobs.s3Endpoint:}")
    private String s3Endpoint;

    /** Local development only: the S3 endpoint as the browser reaches it, used to sign download URLs. */
    @Value("${reportJobs.s3PublicEndpoint:}")
    private String s3PublicEndpoint;

    @Value("${reportJobs.lambdaFunctionName:}")
    private String lambdaFunctionName;

    /** Local development only: the Lambda runtime interface emulator. */
    @Value("${reportJobs.lambdaEndpoint:}")
    private String lambdaEndpoint;

    /** The report Lambda's own timeout. The API waits longer than this, and the sweep's cutoff is based on it. */
    @Value("${reportJobs.lambdaTimeoutSeconds:300}")
    private long lambdaTimeoutSeconds;

    /** Each running XLSX job holds a thread while it waits for the Lambda. */
    @Value("${reportJobs.threads:4}")
    private int threads;

    @Value("${reportJobs.queueCapacity:50}")
    private int queueCapacity;

    /** Must match the bucket's expiry rule. */
    @Value("${reportJobs.retentionHours:24}")
    private long retentionHours;

    /** How far back the download tray lists jobs, so expired exports stay visible for a while. */
    @Value("${reportJobs.listWindowHours:48}")
    private long listWindowHours;

    @Value("${reportJobs.downloadUrlMinutes:5}")
    private long downloadUrlMinutes;

    /** Added to the Lambda timeout: after that, a started job can no longer be running. */
    @Value("${reportJobs.sweep.startedMarginSeconds:300}")
    private long sweepStartedMarginSeconds;

    /** An unclaimed job this old was queued on a task that went away; run it again. */
    @Value("${reportJobs.sweep.resubmitAfterSeconds:600}")
    private long sweepResubmitAfterSeconds;

    /** An unclaimed job this old means the queue is stuck; fail it so the user can see it. */
    @Value("${reportJobs.sweep.failUnstartedAfterSeconds:1800}")
    private long sweepFailUnstartedAfterSeconds;

    public Duration lambdaTimeout() {
        return Duration.ofSeconds(lambdaTimeoutSeconds);
    }

    public Duration startedCutoff() {
        return Duration.ofSeconds(lambdaTimeoutSeconds + sweepStartedMarginSeconds);
    }

    public Duration retention() {
        return Duration.ofHours(retentionHours);
    }

    public Duration listWindow() {
        return Duration.ofHours(listWindowHours);
    }

    public Duration downloadUrlLifetime() {
        return Duration.ofMinutes(downloadUrlMinutes);
    }

    public Duration resubmitAfter() {
        return Duration.ofSeconds(sweepResubmitAfterSeconds);
    }

    public Duration failUnstartedAfter() {
        return Duration.ofSeconds(sweepFailUnstartedAfterSeconds);
    }
}
