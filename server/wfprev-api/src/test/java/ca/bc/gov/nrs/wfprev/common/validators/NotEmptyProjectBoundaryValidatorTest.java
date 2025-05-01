package ca.bc.gov.nrs.wfprev.common.validators;

import ca.bc.gov.nrs.wfprev.data.models.ProjectBoundaryModel;
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

import java.util.Date;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;

class NotEmptyProjectBoundaryValidatorTest {

    private Validator validator;
    private final GeometryFactory geometryFactory = new GeometryFactory();

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void whenBoundaryGeometryIsNull_thenViolation() {
        ProjectBoundaryModel model = ProjectBoundaryModel.builder()
                .boundaryGeometry(null)
                .build();

        Set<ConstraintViolation<ProjectBoundaryModel>> violations = validator.validate(model);
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("must not be empty")));
    }

    @Test
    void whenBoundaryGeometryIsEmpty_thenViolation() {
        MultiPolygon emptyMultiPolygon = geometryFactory.createMultiPolygon(null);

        ProjectBoundaryModel model = ProjectBoundaryModel.builder()
                .boundaryGeometry(emptyMultiPolygon)
                .build();

        Set<ConstraintViolation<ProjectBoundaryModel>> violations = validator.validate(model);
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("must not be empty")));
    }

    @Test
    void whenBoundaryGeometryIsValid_thenNoViolation() {
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

        ProjectBoundaryModel model = ProjectBoundaryModel.builder()
                .projectGuid(String.valueOf(UUID.randomUUID()))
                .systemStartTimestamp(new Date())
                .systemEndTimestamp(new Date(new Date().getTime() + 1000))
                .collectionDate(new Date())
                .boundaryGeometry(multiPolygon)
                .build();

        Set<ConstraintViolation<ProjectBoundaryModel>> violations = validator.validate(model);

        assertTrue(violations.isEmpty());
    }

}
