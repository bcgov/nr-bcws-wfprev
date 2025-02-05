package ca.bc.gov.nrs.wfprev;

import ca.bc.gov.nrs.wfprev.common.serializers.GeoJsonJacksonDeserializer;
import ca.bc.gov.nrs.wfprev.common.serializers.GeoJsonJacksonSerializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import jakarta.servlet.DispatcherType;
import org.geolatte.geom.Geometry;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.web.servlet.ConditionalOnMissingFilterBean;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.web.filter.ForwardedHeaderFilter;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootApplication
@EnableJpaAuditing(auditorAwareRef = "springSecurityAuditorAware")
public class WfprevApiApplication {
	/*
	 * Run the application as a JAR
	 */
	public static void main(String[] args) {
		SpringApplication.run(WfprevApiApplication.class, args);
	}

	/**
	 * Header Forwarding filter, to replace the baseUri replacements
	 * for Hateoas links behind a proxy passing X-Forwarded-Host.
	 * This is how we get the links to match the URL people hit
	 * vs the URL of the internal service.
	 * <p>
	 * Alternative here would be to supply an environment variable override.
	 * <p>
	 * Note, the application.properties has a configuration to handle this in place
	 * server.forward-headers-strategy
	 * <p>
	 * This can act as an override
	 *
	 * @return
	 */
	@Bean
	@ConditionalOnMissingFilterBean(ForwardedHeaderFilter.class)
	@ConditionalOnProperty(value = "server.forward-headers-strategy", havingValue = "framework")
	public FilterRegistrationBean<ForwardedHeaderFilter> forwardedHeaderFilter() {
		ForwardedHeaderFilter filter = new ForwardedHeaderFilter();
		FilterRegistrationBean<ForwardedHeaderFilter> registration = new FilterRegistrationBean<>(filter);
		registration.setDispatcherTypes(DispatcherType.REQUEST, DispatcherType.ASYNC, DispatcherType.ERROR);
		registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
		return registration;
	}

	@Bean
	public ObjectMapper registerObjectMapper() {
		ObjectMapper mapper = new ObjectMapper();
		SimpleModule module = new SimpleModule();

		module.addSerializer(Geometry.class, new GeoJsonJacksonSerializer());
		module.addDeserializer(Geometry.class, new GeoJsonJacksonDeserializer());

		mapper.registerModule(module);
		return mapper;
	}
}
