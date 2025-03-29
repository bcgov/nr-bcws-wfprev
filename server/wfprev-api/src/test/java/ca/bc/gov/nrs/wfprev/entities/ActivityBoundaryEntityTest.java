package ca.bc.gov.nrs.wfprev.entities;

import ca.bc.gov.nrs.wfprev.data.entities.ActivityBoundaryEntity;
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

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Date;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ActivityBoundaryEntityTest {

    private Validator validator;

    private MultiPolygon multiPolygon;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();

        GeometryFactory geometryFactory = new GeometryFactory();

        Coordinate[] coordinates = {
                new Coordinate(-123.3656, 48.4284),
                new Coordinate(-123.3657, 48.4285),
                new Coordinate(-123.3658, 48.4284),
                new Coordinate(-123.3656, 48.4284) // Closing the polygon
        };

        LinearRing shell = geometryFactory.createLinearRing(coordinates);
        Polygon polygon = geometryFactory.createPolygon(shell);

        multiPolygon = geometryFactory.createMultiPolygon(new Polygon[]{polygon});
    }

    private ActivityBoundaryEntity createValidActivityBoundaryEntity() throws SQLException {
        return ActivityBoundaryEntity.builder()
                .activityBoundaryGuid(UUID.randomUUID())
                .activityGuid(UUID.randomUUID())
                .systemStartTimestamp(new Date())
                .systemEndTimestamp(new Date())
                .mappingLabel("Test Mapping Label")
                .collectionDate(new Date())
                .collectionMethod("Test Collection Method")
                .collectorName("Test Collector")
                .boundarySizeHa(BigDecimal.valueOf(100.0000))
                .boundaryComment("Test Boundary Comment")
                .geometry(multiPolygon)
                .revisionCount(0)
                .createUser("tester")
                .createDate(new Date())
                .updateUser("tester")
                .updateDate(new Date())
                .build();
    }

    @Test
    void testActivityGuid_IsNull() throws SQLException {
        ActivityBoundaryEntity entity = createValidActivityBoundaryEntity();
        entity.setActivityGuid(null);

        Set<ConstraintViolation<ActivityBoundaryEntity>> violations = validator.validate(entity);

        assertThat(violations)
                .hasSize(1)
                .anyMatch(violation ->
                        violation.getPropertyPath().toString().equals("activityGuid") &&
                                violation.getMessage().equals("must not be null"));
    }

    @Test
    void testSystemStartTimestamp_IsNull() throws SQLException {
        ActivityBoundaryEntity entity = createValidActivityBoundaryEntity();
        entity.setSystemStartTimestamp(null);

        Set<ConstraintViolation<ActivityBoundaryEntity>> violations = validator.validate(entity);

        assertThat(violations)
                .hasSize(1)
                .anyMatch(violation ->
                        violation.getPropertyPath().toString().equals("systemStartTimestamp") &&
                                violation.getMessage().equals("must not be null"));
    }

    @Test
    void testSystemEndTimestamp_IsNull() throws SQLException {
        ActivityBoundaryEntity entity = createValidActivityBoundaryEntity();
        entity.setSystemEndTimestamp(null);

        Set<ConstraintViolation<ActivityBoundaryEntity>> violations = validator.validate(entity);

        assertThat(violations)
                .hasSize(1)
                .anyMatch(violation ->
                        violation.getPropertyPath().toString().equals("systemEndTimestamp") &&
                                violation.getMessage().equals("must not be null"));
    }

    @Test
    void testCollectionDate_IsNull() throws SQLException {
        ActivityBoundaryEntity entity = createValidActivityBoundaryEntity();
        entity.setCollectionDate(null);

        Set<ConstraintViolation<ActivityBoundaryEntity>> violations = validator.validate(entity);

        assertThat(violations)
                .hasSize(1)
                .anyMatch(violation ->
                        violation.getPropertyPath().toString().equals("collectionDate") &&
                                violation.getMessage().equals("must not be null"));
    }

    @Test
    void testBoundarySizeHa_IsNull() throws SQLException {
        ActivityBoundaryEntity entity = createValidActivityBoundaryEntity();
        entity.setBoundarySizeHa(null);

        Set<ConstraintViolation<ActivityBoundaryEntity>> violations = validator.validate(entity);

        assertThat(violations)
                .hasSize(1)
                .anyMatch(violation ->
                        violation.getPropertyPath().toString().equals("boundarySizeHa") &&
                                violation.getMessage().equals("must not be null"));
    }

    @Test
    void testGeometry_IsNull() throws SQLException {
        ActivityBoundaryEntity entity = createValidActivityBoundaryEntity();
        entity.setGeometry(null);

        Set<ConstraintViolation<ActivityBoundaryEntity>> violations = validator.validate(entity);

        assertThat(violations)
                .hasSize(1)
                .anyMatch(violation ->
                        violation.getPropertyPath().toString().equals("geometry") &&
                                violation.getMessage().equals("must not be null"));
    }

    @Test
    void testCreateUser_IsNull() throws SQLException {
        ActivityBoundaryEntity entity = createValidActivityBoundaryEntity();
        entity.setCreateUser(null);

        Set<ConstraintViolation<ActivityBoundaryEntity>> violations = validator.validate(entity);

        assertThat(violations)
                .hasSize(1)
                .anyMatch(violation ->
                        violation.getPropertyPath().toString().equals("createUser") &&
                                violation.getMessage().equals("must not be null"));
    }

    @Test
    void testCreateDate_IsNull() throws SQLException {
        ActivityBoundaryEntity entity = createValidActivityBoundaryEntity();
        entity.setCreateDate(null);

        Set<ConstraintViolation<ActivityBoundaryEntity>> violations = validator.validate(entity);

        assertThat(violations)
                .hasSize(1)
                .anyMatch(violation ->
                        violation.getPropertyPath().toString().equals("createDate") &&
                                violation.getMessage().equals("must not be null"));
    }

    @Test
    void testUpdateUser_IsNull() throws SQLException {
        ActivityBoundaryEntity entity = createValidActivityBoundaryEntity();
        entity.setUpdateUser(null);

        Set<ConstraintViolation<ActivityBoundaryEntity>> violations = validator.validate(entity);

        assertThat(violations)
                .hasSize(1)
                .anyMatch(violation ->
                        violation.getPropertyPath().toString().equals("updateUser") &&
                                violation.getMessage().equals("must not be null"));
    }

    @Test
    void testUpdateDate_IsNull() throws SQLException {
        ActivityBoundaryEntity entity = createValidActivityBoundaryEntity();
        entity.setUpdateDate(null);

        Set<ConstraintViolation<ActivityBoundaryEntity>> violations = validator.validate(entity);

        assertThat(violations)
                .hasSize(1)
                .anyMatch(violation ->
                        violation.getPropertyPath().toString().equals("updateDate") &&
                                violation.getMessage().equals("must not be null"));
    }

    @Test
    void testRevisionCount_IsNull() throws SQLException {
        ActivityBoundaryEntity entity = createValidActivityBoundaryEntity();
        entity.setRevisionCount(null);

        Set<ConstraintViolation<ActivityBoundaryEntity>> violations = validator.validate(entity);

        assertThat(violations)
                .hasSize(1)
                .anyMatch(violation ->
                        violation.getPropertyPath().toString().equals("revisionCount") &&
                                violation.getMessage().equals("must not be null"));
    }

    @Test
    void testAllFields_AreValid() throws SQLException {
        ActivityBoundaryEntity entity = createValidActivityBoundaryEntity();

        Set<ConstraintViolation<ActivityBoundaryEntity>> violations = validator.validate(entity);

        assertThat(violations).isEmpty();
    }
}