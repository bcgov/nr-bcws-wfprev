package ca.bc.gov.nrs.wfprev;

import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.DefaultOAuth2AuthenticatedPrincipal;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class SpringSecurityAuditorAware implements AuditorAware<String> {

    @Override
    public Optional<String> getCurrentAuditor() {
        return Optional.ofNullable(SecurityContextHolder.getContext())
                .map(context -> context.getAuthentication())
                .filter(Authentication::isAuthenticated)
                .map(authentication -> {
                    Object principal = authentication.getPrincipal();
                    if (principal instanceof DefaultOAuth2AuthenticatedPrincipal) {
                        // Extract username or preferred identifier
                        DefaultOAuth2AuthenticatedPrincipal oauthPrincipal = (DefaultOAuth2AuthenticatedPrincipal) principal;
                        return (String) oauthPrincipal.getAttribute("client_id"); // Adjust key to match your provider
                    }
                    throw new IllegalStateException("Principal is not of type DefaultOAuth2AuthenticatedPrincipal");
                });
    }
}