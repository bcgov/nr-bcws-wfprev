package ca.bc.gov.nrs.wfprev.common.validators;

import ca.bc.gov.nrs.wfprev.data.models.ActivityBoundaryModel;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.PrecisionModel;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;

class NotEmptyActivityBoundaryValidatorTest {

    private Validator validator;
    private final GeometryFactory geometryFactory = new GeometryFactory();

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void whenGeometryIsNull_thenViolation() {
        ActivityBoundaryModel model = ActivityBoundaryModel.builder()
                .geometry(null)
                .build();

        Set<ConstraintViolation<ActivityBoundaryModel>> violations = validator.validate(model);
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("must not be empty")));
    }

    @Test
    void whenGeometryIsEmpty_thenViolation() {
        MultiPolygon emptyMultiPolygon = geometryFactory.createMultiPolygon(null);

        ActivityBoundaryModel model = ActivityBoundaryModel.builder()
                .geometry(emptyMultiPolygon)
                .build();

        Set<ConstraintViolation<ActivityBoundaryModel>> violations = validator.validate(model);
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("must not be empty")));
    }

    @Test
    void whenGeometryIsValid_thenNoViolation() {
        GeometryFactory geomFactory = new GeometryFactory(new PrecisionModel(), 4326);

        Coordinate[] coords = new Coordinate[] {
                new Coordinate(0, 0),
                new Coordinate(1, 0),
                new Coordinate(1, 1),
                new Coordinate(0, 1),
                new Coordinate(0, 0)
        };

        LinearRing shell = geomFactory.createLinearRing(coords);
        Polygon polygon = geomFactory.createPolygon(shell);
        MultiPolygon multiPolygon = geomFactory.createMultiPolygon(new Polygon[]{polygon});

        ActivityBoundaryModel model = ActivityBoundaryModel.builder()
                .activityGuid(String.valueOf(UUID.randomUUID()))
                .systemStartTimestamp(new Date())
                .systemEndTimestamp(new Date(new Date().getTime() + 1000))
                .collectionDate(new Date())
                .boundarySizeHa(BigDecimal.valueOf(100.0))
                .geometry(multiPolygon)
                .build();

        Set<ConstraintViolation<ActivityBoundaryModel>> violations = validator.validate(model);

        violations.forEach(v -> System.out.println(v.getPropertyPath() + " : " + v.getMessage()));

        assertTrue(violations.isEmpty());
    }

}
