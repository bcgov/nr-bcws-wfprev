package ca.bc.gov.nrs.wfprev.services;

import ca.bc.gov.nrs.wfprev.data.models.ValidationResult;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.operation.valid.IsValidOp;
import org.locationtech.jts.operation.valid.TopologyValidationError;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SpatialValidationService {

    public ValidationResult validateGeometry(Geometry geometry) {
        if (geometry == null || geometry.isEmpty()) {
            return ValidationResult.builder()
                    .valid(false)
                    .message("Geometry cannot be null or empty")
                    .build();
        }

        IsValidOp isValidOp = new IsValidOp(geometry);
        TopologyValidationError error = isValidOp.getValidationError();

        if (error != null) {
            String message = String.format("%s at (%f, %f)", 
                error.getMessage(), 
                error.getCoordinate().x, 
                error.getCoordinate().y);
            
            // Create a point from the error coordinate
            Point errorLocation = geometry.getFactory().createPoint(error.getCoordinate());
            
            log.warn("Spatial validation failed: {}", message);
            
            return ValidationResult.builder()
                    .valid(false)
                    .message(message)
                    .violationLocation(errorLocation)
                    .build();
        }

        return ValidationResult.builder()
                .valid(true)
                .message("Geometry is valid")
                .build();
    }
}
