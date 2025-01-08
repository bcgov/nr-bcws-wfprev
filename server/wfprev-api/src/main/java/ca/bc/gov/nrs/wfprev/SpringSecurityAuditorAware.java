package ca.bc.gov.nrs.wfprev;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.DefaultOAuth2AuthenticatedPrincipal;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@Slf4j
public class SpringSecurityAuditorAware implements AuditorAware<String> {

    @Override
    public Optional<String> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        log.debug("Authentication: {}", authentication);

        if (authentication == null || !authentication.isAuthenticated()) {
            log.warn("No authenticated user found.");
            return Optional.empty();
        }

        log.debug("Principal: {}", authentication.getPrincipal());
        if (authentication.getPrincipal() instanceof DefaultOAuth2AuthenticatedPrincipal) {
            String userGuid = (String) ((DefaultOAuth2AuthenticatedPrincipal) authentication.getPrincipal())
                    .getAttribute("user_guid");
            log.info("Current Auditor (user_guid): {}", userGuid);
            return Optional.ofNullable("SYSTEM");
        }

        log.warn("Unexpected principal type: {}", authentication.getPrincipal().getClass().getName());
        return Optional.empty();
    }
}