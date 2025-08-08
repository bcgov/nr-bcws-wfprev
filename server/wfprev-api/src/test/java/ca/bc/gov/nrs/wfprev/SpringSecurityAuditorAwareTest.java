package ca.bc.gov.nrs.wfprev;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.DefaultOAuth2AuthenticatedPrincipal;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SpringSecurityAuditorAwareTest {

    private SpringSecurityAuditorAware auditorAware;

    private MockedStatic<SecurityContextHolder> securityContextHolderMock;

    @BeforeEach
    void setUp() {
        auditorAware = new SpringSecurityAuditorAware();
        securityContextHolderMock = Mockito.mockStatic(SecurityContextHolder.class);
    }

    @AfterEach
    void tearDown() {
        securityContextHolderMock.close();
    }

    @Test
    void getCurrentAuditor_authenticatedUser_returnsUsername() {
        // Given: A valid SecurityContext with an authenticated user
        SecurityContext mockSecurityContext = mock(SecurityContext.class);
        Authentication mockAuthentication = mock(Authentication.class);
        DefaultOAuth2AuthenticatedPrincipal mockPrincipal = mock(DefaultOAuth2AuthenticatedPrincipal.class);

        when(SecurityContextHolder.getContext()).thenReturn(mockSecurityContext);
        when(mockSecurityContext.getAuthentication()).thenReturn(mockAuthentication);
        when(mockAuthentication.isAuthenticated()).thenReturn(true);
        when(mockAuthentication.getPrincipal()).thenReturn(mockPrincipal);
        when(mockPrincipal.getAttribute("userId")).thenReturn("test_user");
        when(mockPrincipal.getAttribute("sub")).thenReturn(null);
        when(mockPrincipal.getAttribute("clientId")).thenReturn(null);

        // When: getCurrentAuditor is called
        Optional<String> result = auditorAware.getCurrentAuditor();

        // Then: The correct username is returned
        assertEquals(Optional.of("test_user"), result);
    }

    @Test
    void getCurrentAuditor_userNotAuthenticated_returnsEmptyOptional() {
        // Given: A valid SecurityContext with an unauthenticated user
        SecurityContext mockSecurityContext = mock(SecurityContext.class);
        Authentication mockAuthentication = mock(Authentication.class);

        when(SecurityContextHolder.getContext()).thenReturn(mockSecurityContext);
        when(mockSecurityContext.getAuthentication()).thenReturn(mockAuthentication);
        when(mockAuthentication.isAuthenticated()).thenReturn(false);

        // When: getCurrentAuditor is called
        Optional<String> result = auditorAware.getCurrentAuditor();

        // Then: An empty Optional is returned
        assertEquals(Optional.empty(), result);
    }

    @Test
    void getCurrentAuditor_invalidPrincipalType_fallsBackToAuthName() {
        SecurityContext ctx = mock(SecurityContext.class);
        Authentication auth = mock(Authentication.class);

        when(SecurityContextHolder.getContext()).thenReturn(ctx);
        when(ctx.getAuthentication()).thenReturn(auth);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getPrincipal()).thenReturn("InvalidPrincipal");
        when(auth.getName()).thenReturn("fallbackUser");

        assertEquals(Optional.of("fallbackUser"), auditorAware.getCurrentAuditor());
    }

    @Test
    void getCurrentAuditor_nullAuthentication_returnsEmptyOptional() {
        // Given: A valid SecurityContext with a null Authentication
        SecurityContext mockSecurityContext = mock(SecurityContext.class);

        when(SecurityContextHolder.getContext()).thenReturn(mockSecurityContext);
        when(mockSecurityContext.getAuthentication()).thenReturn(null);

        // When: getCurrentAuditor is called
        Optional<String> result = auditorAware.getCurrentAuditor();

        // Then: An empty Optional is returned
        assertEquals(Optional.empty(), result);
    }

    @Test
    void getCurrentAuditor_claimFallbackOrder_userId_then_sub_then_clientId() {
        SecurityContext ctx = mock(SecurityContext.class);
        Authentication auth = mock(Authentication.class);
        DefaultOAuth2AuthenticatedPrincipal p = mock(DefaultOAuth2AuthenticatedPrincipal.class);

        when(SecurityContextHolder.getContext()).thenReturn(ctx);
        when(ctx.getAuthentication()).thenReturn(auth);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getPrincipal()).thenReturn(p);

        when(p.getAttribute("userId")).thenReturn("UID");
        when(p.getAttribute("sub")).thenReturn("SUB");
        when(p.getAttribute("clientId")).thenReturn("CID");
        assertEquals(Optional.of("UID"), auditorAware.getCurrentAuditor());

        when(p.getAttribute("userId")).thenReturn(null);
        when(p.getAttribute("sub")).thenReturn("SUB");
        when(p.getAttribute("clientId")).thenReturn("CID");
        assertEquals(Optional.of("SUB"), auditorAware.getCurrentAuditor());

        when(p.getAttribute("userId")).thenReturn(null);
        when(p.getAttribute("sub")).thenReturn(null);
        when(p.getAttribute("clientId")).thenReturn("CID");
        assertEquals(Optional.of("CID"), auditorAware.getCurrentAuditor());
    }

    @Test
    void getCurrentAuditor_ignoresBlankValues_returnsEmptyWhenAllBlankOrNull() {
        SecurityContext ctx = mock(SecurityContext.class);
        Authentication auth = mock(Authentication.class);
        DefaultOAuth2AuthenticatedPrincipal p = mock(DefaultOAuth2AuthenticatedPrincipal.class);

        when(SecurityContextHolder.getContext()).thenReturn(ctx);
        when(ctx.getAuthentication()).thenReturn(auth);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getPrincipal()).thenReturn(p);

        when(p.getAttribute("userId")).thenReturn("   ");
        when(p.getAttribute("sub")).thenReturn("");
        when(p.getAttribute("clientId")).thenReturn(null);

        assertEquals(Optional.empty(), auditorAware.getCurrentAuditor());
    }

}