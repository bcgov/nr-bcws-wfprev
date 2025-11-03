package ca.bc.gov.nrs.wfprev.common.services.health;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.FOUND;
import static org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@ExtendWith(MockitoExtension.class)
class ExternalServiceHealthIndicatorTest {

    @Mock
    private RestTemplateBuilder builder;

    @Mock
    private RestTemplate restTemplate;

    private ExternalServiceHealthIndicator indicator;

    private static final String URL = "https://example.com";

    @BeforeEach
    void setUp() {
        when(builder.setConnectTimeout(any(Duration.class))).thenReturn(builder);
        when(builder.setReadTimeout(any(Duration.class))).thenReturn(builder);
        when(builder.build()).thenReturn(restTemplate);

        indicator = new ExternalServiceHealthIndicator(builder, URL);
    }

    @Test
    void health_when2xx_shouldBeUp() {
        when(restTemplate.getForEntity(URL, Void.class))
                .thenReturn(ResponseEntity.ok().build());

        Health health = indicator.health();

        assertThat(health.getStatus()).isEqualTo(Status.UP);
        assertThat(health.getDetails()).containsEntry("url", URL);
        assertThat(health.getDetails()).containsEntry("status", 200);
        assertThat(health.getDetails()).containsEntry("External_Service", "Service is Up and Running ✅");
    }

    @Test
    void health_when5xx_shouldBeDown() {
        when(restTemplate.getForEntity(URL, Void.class))
                .thenReturn(ResponseEntity.status(SERVICE_UNAVAILABLE).build());

        Health health = indicator.health();

        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
        assertThat(health.getDetails()).containsEntry("url", URL);
        assertThat(health.getDetails()).containsEntry("status", 503);
        assertThat(health.getDetails()).containsEntry("External_Service", "Service is Down 🔻");
    }

    @Test
    void health_when3xx_shouldBeUnknown() {
        when(restTemplate.getForEntity(URL, Void.class))
                .thenReturn(ResponseEntity.status(FOUND).build());

        Health health = indicator.health();

        assertThat(health.getStatus()).isEqualTo(Status.UNKNOWN);
        assertThat(health.getDetails()).containsEntry("url", URL);
        assertThat(health.getDetails()).containsEntry("status", 302);
    }

    @Test
    void health_when4xx_shouldBeUnknown() {
        when(restTemplate.getForEntity(URL, Void.class))
                .thenReturn(ResponseEntity.status(UNAUTHORIZED).build());

        Health health = indicator.health();

        assertThat(health.getStatus()).isEqualTo(Status.UNKNOWN);
        assertThat(health.getDetails()).containsEntry("url", URL);
        assertThat(health.getDetails()).containsEntry("status", 401);
    }

    @Test
    void health_whenRestClientResponseException4xx_shouldBeUnknown() {
        when(restTemplate.getForEntity(URL, Void.class))
                .thenThrow(HttpClientErrorException.create(
                        UNAUTHORIZED, "Unauthorized", HttpHeaders.EMPTY, new byte[0], StandardCharsets.UTF_8));

        Health health = indicator.health();

        assertThat(health.getStatus()).isEqualTo(Status.UNKNOWN);
        assertThat(health.getDetails()).containsEntry("url", URL);
        assertThat(health.getDetails()).containsEntry("status", 401);
        assertThat(health.getDetails()).containsKey("error");
    }

    @Test
    void health_whenRestClientResponseException5xx_shouldBeDown() {
        when(restTemplate.getForEntity(URL, Void.class))
                .thenThrow(HttpServerErrorException.create(
                        SERVICE_UNAVAILABLE, "Service Unavailable", HttpHeaders.EMPTY, new byte[0], StandardCharsets.UTF_8));

        Health health = indicator.health();

        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
        assertThat(health.getDetails()).containsEntry("url", URL);
        assertThat(health.getDetails()).containsEntry("status", 503);
        assertThat(health.getDetails()).containsKey("error");
    }

    @Test
    void health_whenGenericException_shouldBeDown() {
        when(restTemplate.getForEntity(URL, Void.class))
                .thenThrow(new RuntimeException("boom"));

        Health health = indicator.health();

        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
        assertThat(health.getDetails()).containsEntry("url", URL);
        assertThat(health.getDetails()).containsKey("error");
    }
}
