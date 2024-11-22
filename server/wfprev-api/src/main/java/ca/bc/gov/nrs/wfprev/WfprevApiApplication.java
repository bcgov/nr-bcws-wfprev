package ca.bc.gov.nrs.wfprev;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.web.servlet.ConditionalOnMissingFilterBean;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.web.filter.ForwardedHeaderFilter;

import jakarta.servlet.DispatcherType;

@SpringBootApplication
public class WfprevApiApplication {
	/*
	 * Run the application as a JAR
	 */
	public static void main(String[] args) {
		System.out.println("simple test to validate scanning is running");
		SpringApplication.run(WfprevApiApplication.class, args);
	}

	/**
	 * Header Forwarding filter, to replace the baseUri replacements
	 * for Hateoas links behind a proxy passing X-Forwarded-Host.
	 * This is how we get the links to match the URL people hit
	 * vs the URL of the internal service.
	 * 
	 * Alternative here would be to supply an environment variable override.
	 * 
	 * Note, the application.properties has a configuration to handle this in place
	 * server.forward-headers-strategy
	 * 
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
}
