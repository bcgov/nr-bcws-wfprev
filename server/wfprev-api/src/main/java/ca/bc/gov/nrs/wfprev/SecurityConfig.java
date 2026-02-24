package ca.bc.gov.nrs.wfprev;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationManagerResolver;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import ca.bc.gov.nrs.wfone.common.webade.oauth2.token.client.TokenService;
import ca.bc.gov.nrs.wfone.common.webade.oauth2.token.client.impl.TokenServiceImpl;
import ca.bc.gov.nrs.wfprev.common.oauth2.WebadeOauth2AuthenticationProvider;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Default security configuration. Assumes all secure endpoints, bearer token,
 * using existing Webade Authentication
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Value("${security.oauth.clientId}")
    private String oauthClientId;

    @Value("${security.oauth.clientSecret}")
    private String oauthClientSecret;

    @Value("${security.oauth.checkTokenUrl}")
    private String oauthCheckTokenUrl;

    @Value("${security.oauth.authTokenUrl}")
    private String authTokenUrl;

    @Value("${spring.application.baseUrl}")
    private String baseUrl;

    @Bean
    public TokenService tokenServiceImpl() {
        return new TokenServiceImpl(
                oauthClientId,
                oauthClientSecret,
                oauthCheckTokenUrl,
                authTokenUrl);
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        return new WebadeOauth2AuthenticationProvider(tokenServiceImpl(), "WFPREV.*");
    }

    @Bean
    public AuthenticationManagerResolver<HttpServletRequest> authenticationManagerResolver() {
        return new AuthenticationManagerResolver<HttpServletRequest>() {
            @Override
            public AuthenticationManager resolve(HttpServletRequest httpServletRequest) {
                return new AuthenticationManager() {
                    @Override
                    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
                        return authenticationProvider().authenticate(authentication);
                    }
                };
            }
        };
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .authorizeHttpRequests(auth -> auth
                .requestMatchers(AntPathRequestMatcher.antMatcher(HttpMethod.OPTIONS, "/**")).permitAll()
                .requestMatchers(AntPathRequestMatcher.antMatcher("/actuator/health")).permitAll()
                .requestMatchers(AntPathRequestMatcher.antMatcher("/check/checkToken")).permitAll()
                .requestMatchers(AntPathRequestMatcher.antMatcher("/v3/api-docs/**"), 
                                 AntPathRequestMatcher.antMatcher("/swagger-ui/**"), 
                                 AntPathRequestMatcher.antMatcher("/swagger-ui.html")).permitAll()
                .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                .authenticationManagerResolver(authenticationManagerResolver())
                );


        return http.build();
    }

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.addAllowedOrigin(baseUrl);
        config.addAllowedMethod("*");
        config.addAllowedHeader("*");
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return new CorsFilter(source);
    }
}
