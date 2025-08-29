package ca.bc.gov.nrs.wfprev;

import org.locationtech.jts.geom.Point;
import org.n52.jackson.datatype.jts.JtsModule;
import org.postgresql.geometric.PGpoint;
import org.postgresql.geometric.PGpolygon;
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
import com.fasterxml.jackson.databind.module.SimpleModule;

import ca.bc.gov.nrs.wfprev.common.serializers.PGLineStringDeserializer;
import ca.bc.gov.nrs.wfprev.common.serializers.PGLineStringSerializer;
import ca.bc.gov.nrs.wfprev.common.serializers.PGMultiPolygonDeserializer;
import ca.bc.gov.nrs.wfprev.common.serializers.PGMultiPolygonSerializer;
import ca.bc.gov.nrs.wfprev.common.serializers.PGPointDeserializer;
import ca.bc.gov.nrs.wfprev.common.serializers.PGPointSerializer;
import ca.bc.gov.nrs.wfprev.common.serializers.PGPolygonDeserializer;
import ca.bc.gov.nrs.wfprev.common.serializers.PGPolygonSerializer;
import ca.bc.gov.nrs.wfprev.common.serializers.PointDeserializer;
import ca.bc.gov.nrs.wfprev.common.serializers.PointSerializer;
import jakarta.servlet.DispatcherType;

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

		module.addSerializer(PGpoint.class, new PGPointSerializer());
		module.addSerializer(PGpoint[].class, new PGLineStringSerializer());
		module.addSerializer(PGpolygon.class, new PGPolygonSerializer());
		module.addSerializer(PGpolygon[].class, new PGMultiPolygonSerializer());
		module.addSerializer(Point.class, new PointSerializer());

		module.addDeserializer(PGpoint.class, new PGPointDeserializer());
		module.addDeserializer(PGpoint[].class, new PGLineStringDeserializer());
		module.addDeserializer(PGpolygon.class, new PGPolygonDeserializer());
		module.addDeserializer(PGpolygon[].class, new PGMultiPolygonDeserializer());
		module.addDeserializer(Point.class, new PointDeserializer());

		mapper.registerModule(new JtsModule());

		mapper.registerModule(module);
		return mapper;
	}
}
