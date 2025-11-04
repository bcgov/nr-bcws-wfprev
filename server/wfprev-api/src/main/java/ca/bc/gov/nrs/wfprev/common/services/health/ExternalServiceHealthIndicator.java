package ca.bc.gov.nrs.wfprev.common.services.health;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.health.ConditionalOnEnabledHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Component
@ConditionalOnEnabledHealthIndicator("external_service_health")
public class ExternalServiceHealthIndicator implements HealthIndicator {

    private final RestTemplate restTemplate;
    private final String externalServiceUrl;

    private static final String URL = "url";
    private static final String STATUS = "status";

    public ExternalServiceHealthIndicator(RestTemplateBuilder builder, @Value("${security.oauth.authTokenUrl}") String externalServiceUrl) {
        this.restTemplate = builder
                .setConnectTimeout(Duration.ofSeconds(2))
                .setReadTimeout(Duration.ofSeconds(3))
                .build();
        this.externalServiceUrl = externalServiceUrl;
    }

    @Override
    public Health health() {
        try {
            ResponseEntity<Void> resp = restTemplate.getForEntity(externalServiceUrl, Void.class);
            int code = resp.getStatusCode().value();

            if (code >= 200 && code < 300) {
                return Health.up()
                        .withDetail("External_Service", "Service is Up and Running ✅")
                        .withDetail(URL, externalServiceUrl)
                        .withDetail(STATUS, code)
                        .build();
            } else if (code >= 500) {
                return Health.down()
                        .withDetail("External_Service", "Service is Down 🔻")
                        .withDetail(URL, externalServiceUrl)
                        .withDetail(STATUS, code)
                        .build();
            } else {
                // 3xx/4xx → unknown (could be auth/redirect, etc.)
                return Health.unknown()
                        .withDetail(URL, externalServiceUrl)
                        .withDetail(STATUS, code)
                        .build();
            }
        } catch (RestClientResponseException e) {
            // We got an HTTP response - treat 5xx as DOWN, others UNKNOWN
            int code = e.getStatusCode().value();
            return (code >= 500 ?
                    Health.down(e) :
                    Health.unknown().withException(e))
                    .withDetail(URL, externalServiceUrl)
                    .withDetail(STATUS, code)
                    .build();
        } catch (Exception e) {
            // timeouts / DNS / SSL issues → DOWN
            return Health.down(e)
                    .withDetail(URL, externalServiceUrl)
                    .build();
        }
    }
}

