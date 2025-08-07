package ca.bc.gov.nrs.wfprev;

import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.DefaultOAuth2AuthenticatedPrincipal;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class SpringSecurityAuditorAware implements AuditorAware<String> {
    // Provides the current authenticated user's identifier for Spring Data auditing.
    // Checks OAuth2 principal attributes (userId > sub > clientId) in order and falling back to Authentication.getName() if not an OAuth2 principal.
    @Override
    public Optional<String> getCurrentAuditor() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return Optional.empty();
        }

        Object principal = auth.getPrincipal();
        if (principal instanceof DefaultOAuth2AuthenticatedPrincipal p) {
            String auditor =
                firstNonBlank(
                    (String) p.getAttribute("userId"),
                    (String) p.getAttribute("sub"),
                    (String) p.getAttribute("clientId")
                );

            return Optional.ofNullable(auditor);
        }

        return Optional.ofNullable(auth.getName());
    }

    private static String firstNonBlank(String... vals) {
        if (vals == null) return null;
        for (String v : vals) {
            if (v != null && !v.isBlank()) return v;
        }
        return null;
    }
}