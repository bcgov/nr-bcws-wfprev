package ca.bc.gov.nrs.wfprev.handlers;

import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler({ConstraintViolationException.class, DataIntegrityViolationException.class})
    public ResponseEntity<Object> handleValidationExceptions(Exception ex) {
        Map<String, String> errors = new HashMap<>();

        if (ex instanceof ConstraintViolationException) {
            ConstraintViolationException cve = (ConstraintViolationException) ex;
            cve.getConstraintViolations().forEach(violation -> {
                String fieldName = violation.getPropertyPath() != null ?
                        violation.getPropertyPath().toString() :
                        "unknown_field";
                String errorMessage = violation.getMessage() != null ?
                        violation.getMessage() :
                        "unknown error";
                errors.put(fieldName, errorMessage);
            });
        } else {
            // DataIntegrityViolationException
            errors.put("error", "Data integrity violation: " + ex.getMessage());
        }

        return ResponseEntity
                .status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(errors);
    }
}