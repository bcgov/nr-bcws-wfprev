package ca.bc.gov.nrs.wfprev;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Properties;

import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationManagerResolver;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AccessToken.TokenType;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint;

import ca.bc.gov.nrs.wfone.common.webade.oauth2.authentication.WebAdeOAuth2Authentication;
import jakarta.servlet.http.HttpServletRequest;

@TestConfiguration
public class TestSpringSecurity {
  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
        .csrf(AbstractHttpConfigurer::disable)
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/actuator/health").permitAll()
            .anyRequest().authenticated()
        )
        .oauth2ResourceServer(oauth2 -> oauth2
            .authenticationManagerResolver(authenticationManagerResolver())
        )
        .httpBasic()
        .and()
        .exceptionHandling(exceptionHandling -> exceptionHandling
            .authenticationEntryPoint(authenticationEntryPoint()));

    return http.build();
  }

  @Bean
  AuthenticationEntryPoint authenticationEntryPoint() {
      BasicAuthenticationEntryPoint result;

      result = new BasicAuthenticationEntryPoint();
      result.setRealmName("wfprev-api");

      return result;
  }

  @Bean
  public AuthenticationManagerResolver<HttpServletRequest> authenticationManagerResolver() {
      return new AuthenticationManagerResolver<HttpServletRequest>() {
          @Override
          public AuthenticationManager resolve(HttpServletRequest httpServletRequest) {
              return new AuthenticationManager() {
                  @Override
                  public Authentication authenticate(Authentication authentication) throws AuthenticationException {
                    // Add different Authentications based on roles here
                    // can be filtered via the fake token passed in
                    OAuth2AuthenticatedPrincipal principal = Mockito.mock(OAuth2AuthenticatedPrincipal.class);
                    Collection<? extends GrantedAuthority> authorities = new ArrayList<>();
                    OAuth2AccessToken token = new OAuth2AccessToken(TokenType.BEARER, "admin-token", Instant.now(), Instant.MAX);
                    Authentication basicUser = new WebAdeOAuth2Authentication(principal, token, authorities,"WFPREV", "test", "test", "GOV", "test'", "test", "test", (long) 0, null, null, null, "businessNumber", "businessGuid");

                    return basicUser;
                  }
              };
          }
      };
  }

  @Bean
  @Primary
  public InMemoryUserDetailsManager authUserService() {
    UserDetails admin = User.withUsername("Admin").password("admin").roles("WFPREV", "READ", "WRITE", "ADMIN").build();
    UserDetails write = User.withUsername("Write").password("write").roles("WFPREV","READ", "WRITE").build();
    UserDetails read = User.withUsername("Read").password("read").roles("WFPREV","READ").build();

    return new InMemoryUserDetailsManager(Arrays.asList(
      read, write, admin
    ));
  }
}
