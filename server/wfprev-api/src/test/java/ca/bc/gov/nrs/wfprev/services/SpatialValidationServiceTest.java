package ca.bc.gov.nrs.wfprev.services;

import ca.bc.gov.nrs.wfprev.data.models.ValidationResult;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.PrecisionModel;

import static org.junit.jupiter.api.Assertions.*;

class SpatialValidationServiceTest {

    private final SpatialValidationService service = new SpatialValidationService();
    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    @Test
    void testValidateGeometry_ValidPolygon() {
        // Create a simple valid square polygon
        Coordinate[] coordinates = new Coordinate[]{
                new Coordinate(0, 0),
                new Coordinate(0, 10),
                new Coordinate(10, 10),
                new Coordinate(10, 0),
                new Coordinate(0, 0)
        };
        Polygon polygon = geometryFactory.createPolygon(coordinates);

        ValidationResult result = service.validateGeometry(polygon);

        assertTrue(result.isValid());
        assertEquals("Geometry is valid", result.getMessage());
        assertNull(result.getViolationLocation());
    }

    @Test
    void testValidateGeometry_SelfIntersectingPolygon() {
        // Create a "bowtie" polygon that self-intersects
        Coordinate[] coordinates = new Coordinate[]{
                new Coordinate(0, 0),
                new Coordinate(10, 10),
                new Coordinate(10, 0),
                new Coordinate(0, 10),
                new Coordinate(0, 0)
        };
        Polygon polygon = geometryFactory.createPolygon(coordinates);

        ValidationResult result = service.validateGeometry(polygon);

        assertFalse(result.isValid());
        assertTrue(result.getMessage().contains("Self-intersection"));
        assertNotNull(result.getViolationLocation());
        // The intersection should be at (5, 5)
        assertEquals(5.0, result.getViolationLocation().getX(), 0.001);
        assertEquals(5.0, result.getViolationLocation().getY(), 0.001);
    }

    @Test
    void testValidateGeometry_NullGeometry() {
        ValidationResult result = service.validateGeometry(null);
        
        assertFalse(result.isValid());
        assertEquals("Geometry cannot be null or empty", result.getMessage());
    }

    @Test
    void testValidateGeometry_EmptyGeometry() {
        Polygon emptyPolygon = geometryFactory.createPolygon();
        ValidationResult result = service.validateGeometry(emptyPolygon);

        assertFalse(result.isValid());
        assertEquals("Geometry cannot be null or empty", result.getMessage());
    }
    
    @Test
    void testValidateGeometry_HoleOutsideShell() {
        // Shell
        LinearRing shell = geometryFactory.createLinearRing(new Coordinate[]{
                new Coordinate(0, 0),
                new Coordinate(0, 10),
                new Coordinate(10, 10),
                new Coordinate(10, 0),
                new Coordinate(0, 0)
        });

        // Hole totally outside the shell
        LinearRing hole = geometryFactory.createLinearRing(new Coordinate[]{
                new Coordinate(20, 20),
                new Coordinate(20, 30),
                new Coordinate(30, 30),
                new Coordinate(30, 20),
                new Coordinate(20, 20)
        });

        Polygon polygon = geometryFactory.createPolygon(shell, new LinearRing[]{hole});

        ValidationResult result = service.validateGeometry(polygon);

        assertFalse(result.isValid());
        // JTS error message for this varies but usually mentions "Hole lies outside shell"
        assertNotNull(result.getMessage());
        assertNotNull(result.getViolationLocation());
    }
}
