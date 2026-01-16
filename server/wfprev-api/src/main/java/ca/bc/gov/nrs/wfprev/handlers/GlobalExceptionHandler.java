package ca.bc.gov.nrs.wfprev.handlers;

import java.util.HashMap;
import java.util.Map;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;


import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.ValidationException;
import org.springframework.security.access.AccessDeniedException;

import ca.bc.gov.nrs.wfone.common.service.api.ServiceException;
@ControllerAdvice
public class GlobalExceptionHandler {

   private static final String ERROR = "error";

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
           errors.put(ERROR, "Data integrity violation: " + ex.getMessage());
       }

       return ResponseEntity
               .status(HttpStatus.UNPROCESSABLE_ENTITY)
               .body(errors);
   }

   @ExceptionHandler(HttpMessageNotReadableException.class)
   public ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
       Map<String, String> errors = new HashMap<>();
       errors.put(ERROR, "Invalid JSON format");

       return ResponseEntity
               .status(HttpStatus.BAD_REQUEST)
               .body(errors);
   }

   @ExceptionHandler(MethodArgumentNotValidException.class)
   public ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
       Map<String, String> errors = new HashMap<>();

       // Field-level errors
       ex.getBindingResult().getFieldErrors().forEach(error -> {
           String fieldName = error.getField();
           String errorMessage = error.getDefaultMessage();
           errors.put(fieldName, errorMessage);
       });

       // Class-level (global) errors
       ex.getBindingResult().getGlobalErrors().forEach(error -> {
           String objectName = error.getObjectName();
           String errorMessage = error.getDefaultMessage();
           errors.put(objectName, errorMessage);
       });

       return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
   }

   @ExceptionHandler(EntityNotFoundException.class)
   public ResponseEntity<Object> handleEntityNotFoundException(EntityNotFoundException ex) {
       Map<String, String> error = new HashMap<>();
       error.put(ERROR, ex.getMessage());

       return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
   }

   @ExceptionHandler(ValidationException.class)
   public ResponseEntity<Object> handleValidationException(ValidationException ex) {
       Map<String, String> error = new HashMap<>();
       error.put(ERROR, ex.getMessage());

       return ResponseEntity
               .status(HttpStatus.CONFLICT)
               .body(error);
   }

   @ExceptionHandler(HttpMessageNotWritableException.class)
   public ResponseEntity<Object> handleHttpMessageNotWritable(HttpMessageNotWritableException ex) {
       Map<String, String> errors = new HashMap<>();
       errors.put(ERROR, "Unable to serialize response");

       return ResponseEntity
               .status(HttpStatus.INTERNAL_SERVER_ERROR)
               .body(errors);
   }

   @ExceptionHandler(IllegalStateException.class)
   public ResponseEntity<Object> handleIllegalStateException(IllegalStateException ex) {
       Map<String, String> errors = new HashMap<>();
       errors.put(ERROR, ex.getMessage());

       return ResponseEntity
               .status(HttpStatus.CONFLICT)
               .body(errors);
   }

   @ExceptionHandler(IllegalArgumentException.class)
   public ResponseEntity<Object> handleIllegalArgumentException(IllegalArgumentException ex) {
       Map<String, String> error = new HashMap<>();
       error.put(ERROR, ex.getMessage());
       return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
   }

   @ExceptionHandler(RuntimeException.class)
   public ResponseEntity<Object> handleRuntimeException(RuntimeException ex) {
       Map<String, String> error = new HashMap<>();
       error.put(ERROR, "Unexpected error occurred: " + ex.getMessage());
       return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
   }


    
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Object> handleAccessDeniedException(AccessDeniedException ex) {
        Map<String, String> error = new HashMap<>();
        error.put(ERROR, "Access Denied.");
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    @ExceptionHandler(ServiceException.class)
    public ResponseEntity<Object> handleServiceException(ServiceException ex) {
        if (ex.getCause() instanceof EntityNotFoundException) {
            return handleEntityNotFoundException((EntityNotFoundException) ex.getCause());
        }
        Map<String, String> error = new HashMap<>();
        error.put(ERROR, ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}