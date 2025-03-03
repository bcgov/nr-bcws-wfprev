package ca.bc.gov.nrs.wfprev.common.validators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.extern.slf4j.Slf4j;
import org.postgresql.geometric.PGpolygon;
import org.postgresql.geometric.PGpoint;

@Slf4j
public class GeometryValidator implements ConstraintValidator<Geometry, PGpolygon> {

    @Override
    public boolean isValid(PGpolygon polygon, ConstraintValidatorContext context) {
        if (polygon == null) {
            log.error("Geometry is null");
            return false;
        }

        PGpoint[] points = polygon.points;
        if (points == null || points.length < 3) {
            log.error("Polygon must have at least 3 points");
            return false;
        }

        for (PGpoint point : points) {
            if (!isValidCoordinate(point)) {
                log.error("Invalid coordinate: ({}, {})", point.x, point.y);
                return false;
            }
        }

        return true;
    }

    private boolean isValidCoordinate(PGpoint point) {
        double lat = point.y;
        double lon = point.x;

        // Latitude should be between -90 and 90, Longitude should be between -180 and 180
        return lat >= -90 && lat <= 90 && lon >= -180 && lon <= 180;
    }
}
