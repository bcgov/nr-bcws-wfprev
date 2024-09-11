package ca.bc.gov.nrs.wfprev.common.services.health;

import org.hibernate.mapping.Map;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.ReactiveHealthIndicator;
import org.springframework.stereotype.Component;

import reactor.core.publisher.Mono;

@Component
public class DownstreamHealthCheck implements ReactiveHealthIndicator {

    @Override
    public Mono<Health> health() {
        return checkDownstreamServiceHealth().onErrorResume(
          ex -> Mono.just(new Health.Builder().down(ex).build())
        );
    }

    private Mono<Health> checkDownstreamServiceHealth() {
        // Any other custom things we want to check that
        // don't make sense in their own class?

        // when building the check the builder can do the following
        // up(): Creates a Health instance with an UP status, indicating that the application is in a healthy state.
        // down(): Creates a Health instance with a DOWN status.
        // unknown(): Creates a Health instance with an UNKNOWN status.
        // status(Status status): Creates a Health instance with a custom status. The Status enum represents the health status, and it can be UP, DOWN, or UNKNOWN.
        // withDetail(String key, Object value): Adds additional details to the Health instance. These details provide more information about the health status and can be useful for troubleshooting and monitoring.
        // withDetails(Map details):

        return Mono.just(new Health.Builder().up().build());
    }
}
