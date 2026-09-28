package ca.bc.gov.nrs.wfprev.services.reportjobs;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.net.URI;

/**
 * Local development only ({@code local} profile, set by docker-compose.local.yml): points the report
 * export clients at the S3 mock and the Lambda runtime interface emulator. Settings are in
 * application-local.yaml.
 */
@Configuration
@Profile("local")
public class LocalReportJobConfig {

    /** The mock and the emulator accept any key. */
    private static final AwsCredentialsProvider LOCAL_CREDENTIALS =
            StaticCredentialsProvider.create(AwsBasicCredentials.create("local", "local"));

    @Bean
    S3Client reportExportS3Client(ReportJobProperties properties,
                                  @Value("${reportJobs.local.s3Endpoint}") String s3Endpoint) {
        return ReportJobConfig.s3Builder(properties)
                .credentialsProvider(LOCAL_CREDENTIALS)
                .endpointOverride(URI.create(s3Endpoint))
                .forcePathStyle(true)
                .build();
    }

    /** Signs download links for the S3 mock as the browser reaches it, which differs from the API's address. */
    @Bean
    S3Presigner reportExportS3Presigner(ReportJobProperties properties,
                                        @Value("${reportJobs.local.s3PublicEndpoint}") String s3PublicEndpoint) {
        return ReportJobConfig.presignerBuilder(properties)
                .credentialsProvider(LOCAL_CREDENTIALS)
                .endpointOverride(URI.create(s3PublicEndpoint))
                .serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(true).build())
                .build();
    }

    @Bean
    LambdaClient reportLambdaClient(ReportJobProperties properties,
                                    @Value("${reportJobs.local.lambdaEndpoint}") String lambdaEndpoint) {
        return ReportJobConfig.lambdaBuilder(properties)
                .credentialsProvider(LOCAL_CREDENTIALS)
                .endpointOverride(URI.create(lambdaEndpoint))
                .build();
    }
}
