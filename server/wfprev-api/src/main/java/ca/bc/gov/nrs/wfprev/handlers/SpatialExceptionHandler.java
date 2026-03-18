package ca.bc.gov.nrs.wfprev.handlers;

import ca.bc.gov.nrs.wfprev.controllers.SpatialController;
import ca.bc.gov.nrs.wfprev.data.models.ValidationResult;
import org.locationtech.jts.geom.TopologyException;
import org.locationtech.jts.io.ParseException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice(assignableTypes = SpatialController.class)
public class SpatialExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ValidationResult> handleIllegalArgumentException(IllegalArgumentException ex) {
        String message = ex.getMessage();
        
        if (isSpatialException(ex)) {
            return ResponseEntity.ok(ValidationResult.builder()
                    .valid(false)
                    .message("Spatial structure error: " + message)
                    .errorType("STRUCTURAL_ERROR")
                    .build());
        }

        return ResponseEntity.ok(ValidationResult.builder()
                .valid(false)
                .message("Input error: " + message)
                .errorType("INPUT_ERROR")
                .build());
    }

    @ExceptionHandler({TopologyException.class, ParseException.class})
    public ResponseEntity<ValidationResult> handleSpatialExceptions(Exception ex) {
        return ResponseEntity.ok(ValidationResult.builder()
                .valid(false)
                .message("Invalid spatial data: " + ex.getMessage())
                .errorType("STRUCTURAL_ERROR")
                .build());
    }

    private boolean isSpatialException(Throwable ex) {
        Throwable cause = ex;
        while (cause != null) {
            for (StackTraceElement element : cause.getStackTrace()) {
                if (element.getClassName().startsWith("org.locationtech.jts")) {
                    return true;
                }
            }
            cause = cause.getCause();
        }
        return false;
    }
}
