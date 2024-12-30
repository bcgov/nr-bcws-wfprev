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
        when(mockPrincipal.getAttribute("preferred_username")).thenReturn("test_user");

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
    void getCurrentAuditor_invalidPrincipalType_throwsIllegalStateException() {
        // Given: A valid SecurityContext with an invalid principal type
        SecurityContext mockSecurityContext = mock(SecurityContext.class);
        Authentication mockAuthentication = mock(Authentication.class);

        when(SecurityContextHolder.getContext()).thenReturn(mockSecurityContext);
        when(mockSecurityContext.getAuthentication()).thenReturn(mockAuthentication);
        when(mockAuthentication.isAuthenticated()).thenReturn(true);
        when(mockAuthentication.getPrincipal()).thenReturn("InvalidPrincipal");

        // When & Then: getCurrentAuditor throws an IllegalStateException
        IllegalStateException exception = assertThrows(IllegalStateException.class, auditorAware::getCurrentAuditor);
        assertEquals("Principal is not of type DefaultOAuth2AuthenticatedPrincipal", exception.getMessage());
    }

    @Test
    void getCurrentAuditor_nullSecurityContext_returnsEmptyOptional() {
        // Given: A null SecurityContext
        when(SecurityContextHolder.getContext()).thenReturn(null);

        // When: getCurrentAuditor is called
        Optional<String> result = auditorAware.getCurrentAuditor();

        // Then: An empty Optional is returned
        assertEquals(Optional.empty(), result);
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
}