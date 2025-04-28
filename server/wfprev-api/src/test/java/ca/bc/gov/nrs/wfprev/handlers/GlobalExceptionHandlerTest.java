package ca.bc.gov.nrs.wfprev.handlers;

import com.fasterxml.jackson.core.JsonParseException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.ValidationException;
import org.hibernate.validator.internal.engine.path.PathImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @InjectMocks
    private GlobalExceptionHandler handler;

    @Test
    void testHandleConstraintViolation_SingleError() {
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
    void testHandleConstraintViolation_MultipleErrors() {
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
    void testHandleConstraintViolation_NoViolations() {
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
    void testHandleDataIntegrityViolation() {
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
    void testHandleDataIntegrityViolation_NullMessage() {
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
    void testHandleConstraintViolation_NullPath() {
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
    void testHandleConstraintViolation_NullMessage() {
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
    @Test
    void testHandleHttpMessageNotReadable() {
        // Given
        HttpMessageNotReadableException ex = new HttpMessageNotReadableException("Test message");

        // When
        ResponseEntity<Object> response = handler.handleHttpMessageNotReadable(ex);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody() instanceof Map);

        @SuppressWarnings("unchecked")
        Map<String, String> errors = (Map<String, String>) response.getBody();
        assertEquals(1, errors.size());
        assertEquals("Invalid JSON format", errors.get("error"));
    }

    @Test
    void testHandleHttpMessageNotReadable_NullMessage() {
        // Given
        HttpMessageNotReadableException ex = new HttpMessageNotReadableException(null);

        // When
        ResponseEntity<Object> response = handler.handleHttpMessageNotReadable(ex);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody() instanceof Map);

        @SuppressWarnings("unchecked")
        Map<String, String> errors = (Map<String, String>) response.getBody();
        assertEquals(1, errors.size());
        assertEquals("Invalid JSON format", errors.get("error"));
    }

    @Test
    void testHandleHttpMessageNotReadable_JsonParseError() {
        // Given
        JsonParseException jsonEx = new JsonParseException(null, "Invalid JSON");
        HttpMessageNotReadableException ex = new HttpMessageNotReadableException("Test message", jsonEx);

        // When
        ResponseEntity<Object> response = handler.handleHttpMessageNotReadable(ex);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody() instanceof Map);

        @SuppressWarnings("unchecked")
        Map<String, String> errors = (Map<String, String>) response.getBody();
        assertEquals(1, errors.size());
        assertEquals("Invalid JSON format", errors.get("error"));
    }

    @Test
    void testHandleMethodArgumentNotValid() {
        // Given
        BindingResult bindingResult = mock(BindingResult.class);
        List<FieldError> fieldErrors = List.of(
                new FieldError("activityModel", "activityName", "must not be empty"),
                new FieldError("activityModel", "activityStartDate", "must be a valid date")
        );

        when(bindingResult.getFieldErrors()).thenReturn(fieldErrors);

        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, bindingResult);

        // When
        ResponseEntity<Object> response = handler.handleMethodArgumentNotValid(ex);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody() instanceof Map);

        @SuppressWarnings("unchecked")
        Map<String, String> errors = (Map<String, String>) response.getBody();
        assertEquals(2, errors.size());
        assertEquals("must not be empty", errors.get("activityName"));
        assertEquals("must be a valid date", errors.get("activityStartDate"));
    }

    @Test
    void testHandleEntityNotFoundException() {
        // Given
        EntityNotFoundException ex = new EntityNotFoundException("Project Boundary not found");

        // When
        ResponseEntity<Object> response = handler.handleEntityNotFoundException(ex);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertTrue(response.getBody() instanceof Map);

        @SuppressWarnings("unchecked")
        Map<String, String> errors = (Map<String, String>) response.getBody();
        assertEquals(1, errors.size());
        assertEquals("Project Boundary not found", errors.get("error"));
    }

    @Test
    void testHandleValidationException() {
        // Given
        String errorMessage = "Validation failed";
        ValidationException ex = new ValidationException(errorMessage);

        // When
        ResponseEntity<Object> response = handler.handleValidationException(ex);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody() instanceof Map);

        @SuppressWarnings("unchecked")
        Map<String, String> error = (Map<String, String>) response.getBody();
        assertEquals(1, error.size());
        assertEquals(errorMessage, error.get("error"));
    }
}