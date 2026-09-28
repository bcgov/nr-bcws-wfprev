package ca.bc.gov.nrs.wfprev.services.reportjobs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.awscore.retry.AwsRetryStrategy;
import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.lambda.LambdaClientBuilder;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.time.Clock;
import java.time.Duration;

/**
 * Beans for report export jobs. The AWS clients use the ECS task role's credentials; local
 * development replaces them with {@link LocalReportJobConfig}. The native image is built without
 * that profile, so it only ever contains these.
 */
@Configuration
public class ReportJobConfig {

    /** Extra wait beyond the Lambda timeout, for the invoke round trip. */
    private static final Duration LAMBDA_CALL_MARGIN = Duration.ofSeconds(60);

    /** Job timestamps are UTC. */
    @Bean
    Clock reportJobClock() {
        return Clock.systemUTC();
    }

    @Bean
    @Profile("!local")
    S3Client reportExportS3Client(ReportJobProperties properties) {
        return s3Builder(properties).build();
    }

    @Bean
    @Profile("!local")
    S3Presigner reportExportS3Presigner(ReportJobProperties properties) {
        return presignerBuilder(properties).build();
    }

    @Bean
    @Profile("!local")
    LambdaClient reportLambdaClient(ReportJobProperties properties) {
        return lambdaBuilder(properties).build();
    }

    static S3ClientBuilder s3Builder(ReportJobProperties properties) {
        return S3Client.builder()
                .region(Region.of(properties.getRegion()))
                .httpClientBuilder(ApacheHttpClient.builder());
    }

    static S3Presigner.Builder presignerBuilder(ReportJobProperties properties) {
        return S3Presigner.builder().region(Region.of(properties.getRegion()));
    }

    /**
     * Invokes are synchronous and wait a little longer than the Lambda's own timeout. They are
     * never retried: a retried invoke would build the file twice; a failure surfaces as Retry instead.
     */
    static LambdaClientBuilder lambdaBuilder(ReportJobProperties properties) {
        Duration timeout = properties.lambdaTimeout().plus(LAMBDA_CALL_MARGIN);
        return LambdaClient.builder()
                .region(Region.of(properties.getRegion()))
                .httpClientBuilder(ApacheHttpClient.builder()
                        .socketTimeout(timeout)
                        .connectionTimeout(Duration.ofSeconds(10)))
                .overrideConfiguration(ClientOverrideConfiguration.builder()
                        .apiCallTimeout(timeout)
                        .apiCallAttemptTimeout(timeout)
                        .retryStrategy(AwsRetryStrategy.doNotRetry())
                        .build());
    }
}
