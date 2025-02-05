package ca.bc.gov.nrs.wfprev.entities;

import ca.bc.gov.nrs.wfprev.data.entities.ActivityBoundaryEntity;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.geolatte.geom.Geometry;
import org.geolatte.geom.builder.DSL;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.geolatte.geom.builder.DSL.g;
import static org.geolatte.geom.crs.CoordinateReferenceSystems.WGS84;

class ActivityBoundaryEntityTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    private Geometry createSamplePolygon() {
        // Create a simple square polygon
        return DSL.polygon(WGS84,
                DSL.ring(
                        g(0.0, 0.0),
                        g(0.0, 1.0),
                        g(1.0, 1.0),
                        g(1.0, 0.0),
                        g(0.0, 0.0)
                )
        );
    }

    private ActivityBoundaryEntity createValidActivityBoundaryEntity() {
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
                .geometry(createSamplePolygon())
                .revisionCount(0)
                .createUser("tester")
                .createDate(new Date())
                .updateUser("tester")
                .updateDate(new Date())
                .build();
    }

    @Test
    void testActivityGuid_IsNull() {
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
    void testSystemStartTimestamp_IsNull() {
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
    void testSystemEndTimestamp_IsNull() {
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
    void testCollectionDate_IsNull() {
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
    void testBoundarySizeHa_IsNull() {
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
    void testGeometry_IsNull() {
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
    void testCreateUser_IsNull() {
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
    void testCreateDate_IsNull() {
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
    void testUpdateUser_IsNull() {
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
    void testUpdateDate_IsNull() {
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
    void testRevisionCount_IsNull() {
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
    void testAllFields_AreValid() {
        ActivityBoundaryEntity entity = createValidActivityBoundaryEntity();

        Set<ConstraintViolation<ActivityBoundaryEntity>> violations = validator.validate(entity);

        assertThat(violations).isEmpty();
    }
}