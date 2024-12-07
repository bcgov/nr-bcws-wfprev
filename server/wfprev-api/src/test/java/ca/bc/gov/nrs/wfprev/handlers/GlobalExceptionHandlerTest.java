package ca.bc.gov.nrs.wfprev.handlers;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.hibernate.validator.internal.engine.path.PathImpl;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class GlobalExceptionHandlerTest {

    @InjectMocks
    private GlobalExceptionHandler handler;

    @Test
    public void testHandleConstraintViolation_SingleError() {
        // Given
        Set<ConstraintViolation<?>> violations = new HashSet<>();
        ConstraintViolation<?> violation = mock(ConstraintViolation.class);
        when(violation.getPropertyPath()).thenReturn(PathImpl.createPathFromString("siteUnitName"));
        when(violation.getMessage()).thenReturn("must not be null");
        violations.add(violation);

        ConstraintViolationException ex = new ConstraintViolationException("Test violation", violations);

        // When
        ResponseEntity<Object> response = handler.handleValidationExceptions(ex);

        // Then
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
        assertTrue(response.getBody() instanceof Map);

        @SuppressWarnings("unchecked")
        Map<String, String> errors = (Map<String, String>) response.getBody();
        assertEquals(1, errors.size());
        assertEquals("must not be null", errors.get("siteUnitName"));
    }

    @Test
    public void testHandleConstraintViolation_MultipleErrors() {
        // Given
        Set<ConstraintViolation<?>> violations = new HashSet<>();

        ConstraintViolation<?> violation1 = mock(ConstraintViolation.class);
        when(violation1.getPropertyPath()).thenReturn(PathImpl.createPathFromString("siteUnitName"));
        when(violation1.getMessage()).thenReturn("must not be null");
        violations.add(violation1);

        ConstraintViolation<?> violation2 = mock(ConstraintViolation.class);
        when(violation2.getPropertyPath()).thenReturn(PathImpl.createPathFromString("projectLead"));
        when(violation2.getMessage()).thenReturn("must not be empty");
        violations.add(violation2);

        ConstraintViolationException ex = new ConstraintViolationException("Test violations", violations);

        // When
        ResponseEntity<Object> response = handler.handleValidationExceptions(ex);

        // Then
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
        assertTrue(response.getBody() instanceof Map);

        @SuppressWarnings("unchecked")
        Map<String, String> errors = (Map<String, String>) response.getBody();
        assertEquals(2, errors.size());
        assertEquals("must not be null", errors.get("siteUnitName"));
        assertEquals("must not be empty", errors.get("projectLead"));
    }

    @Test
    public void testHandleConstraintViolation_NoViolations() {
        // Given
        Set<ConstraintViolation<?>> violations = new HashSet<>();
        ConstraintViolationException ex = new ConstraintViolationException("Test with no violations", violations);

        // When
        ResponseEntity<Object> response = handler.handleValidationExceptions(ex);

        // Then
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
        assertTrue(response.getBody() instanceof Map);

        @SuppressWarnings("unchecked")
        Map<String, String> errors = (Map<String, String>) response.getBody();
        assertTrue(errors.isEmpty());
    }

    @Test
    public void testHandleDataIntegrityViolation() {
        // Given
        String errorMessage = "null value in column \"forest_region_org_unit_id\" of relation \"project\" violates not-null constraint";
        DataIntegrityViolationException ex = new DataIntegrityViolationException(errorMessage);

        // When
        ResponseEntity<Object> response = handler.handleValidationExceptions(ex);

        // Then
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
        assertTrue(response.getBody() instanceof Map);

        @SuppressWarnings("unchecked")
        Map<String, String> errors = (Map<String, String>) response.getBody();
        assertEquals(1, errors.size());
        assertEquals("Data integrity violation: " + errorMessage, errors.get("error"));
    }

    @Test
    public void testHandleDataIntegrityViolation_NullMessage() {
        // Given
        DataIntegrityViolationException ex = new DataIntegrityViolationException(null);

        // When
        ResponseEntity<Object> response = handler.handleValidationExceptions(ex);

        // Then
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
        assertTrue(response.getBody() instanceof Map);

        @SuppressWarnings("unchecked")
        Map<String, String> errors = (Map<String, String>) response.getBody();
        assertEquals(1, errors.size());
        assertEquals("Data integrity violation: null", errors.get("error"));
    }

    @Test
    public void testHandleConstraintViolation_NullPath() {
        // Given
        Set<ConstraintViolation<?>> violations = new HashSet<>();
        ConstraintViolation<?> violation = mock(ConstraintViolation.class);
        when(violation.getPropertyPath()).thenReturn(null);
        when(violation.getMessage()).thenReturn("must not be null");
        violations.add(violation);

        ConstraintViolationException ex = new ConstraintViolationException("Test violation", violations);

        // When
        ResponseEntity<Object> response = handler.handleValidationExceptions(ex);

        // Then
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
        assertTrue(response.getBody() instanceof Map);

        @SuppressWarnings("unchecked")
        Map<String, String> errors = (Map<String, String>) response.getBody();
        assertEquals(1, errors.size());
        assertEquals("must not be null", errors.get("unknown_field"));
    }

    @Test
    public void testHandleConstraintViolation_NullMessage() {
        // Given
        Set<ConstraintViolation<?>> violations = new HashSet<>();
        ConstraintViolation<?> violation = mock(ConstraintViolation.class);
        when(violation.getPropertyPath()).thenReturn(PathImpl.createPathFromString("siteUnitName"));
        when(violation.getMessage()).thenReturn(null);
        violations.add(violation);

        ConstraintViolationException ex = new ConstraintViolationException("Test violation", violations);

        // When
        ResponseEntity<Object> response = handler.handleValidationExceptions(ex);

        // Then
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
        assertTrue(response.getBody() instanceof Map);

        @SuppressWarnings("unchecked")
        Map<String, String> errors = (Map<String, String>) response.getBody();
        assertEquals(1, errors.size());
        assertEquals("unknown error", errors.get("siteUnitName"));  // Changed from "null" to "unknown error"
    }
}