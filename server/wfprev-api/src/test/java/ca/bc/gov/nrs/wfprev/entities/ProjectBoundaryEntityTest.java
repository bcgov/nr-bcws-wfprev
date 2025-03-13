package ca.bc.gov.nrs.wfprev.entities;

import ca.bc.gov.nrs.wfprev.data.entities.ProjectBoundaryEntity;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.postgresql.geometric.PGpolygon;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Date;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ProjectBoundaryEntityTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    private ProjectBoundaryEntity createValidProjectBoundaryEntity() throws SQLException {
        return ProjectBoundaryEntity.builder()
                .projectBoundaryGuid(UUID.randomUUID())
                .projectGuid(UUID.randomUUID())
                .systemStartTimestamp(new Date())
                .systemEndTimestamp(new Date())
                .mappingLabel("Test Mapping Label")
                .collectionDate(new Date())
                .collectionMethod("Test Collection Method")
                .collectorName("Test Collector")
                .boundarySizeHa(BigDecimal.valueOf(100.0000))
                .boundaryComment("Test Boundary Comment")
                .boundaryGeometry(new PGpolygon("((-123.3656,48.4284),(-123.3657,48.4285),(-123.3658,48.4284),(-123.3656,48.4284))"))
                .locationGeometry(new PGpolygon("((-123.3656,48.4284),(-123.3657,48.4285),(-123.3658,48.4284),(-123.3656,48.4284))"))
                .revisionCount(0)
                .createUser("tester")
                .createDate(new Date())
                .updateUser("tester")
                .updateDate(new Date())
                .build();
    }

    @Test
    void testProjectGuid_IsNull() throws SQLException {
        ProjectBoundaryEntity entity = createValidProjectBoundaryEntity();
        entity.setProjectGuid(null);

        Set<ConstraintViolation<ProjectBoundaryEntity>> violations = validator.validate(entity);

        assertThat(violations)
                .hasSize(1)
                .anyMatch(violation ->
                        violation.getPropertyPath().toString().equals("projectGuid") &&
                                violation.getMessage().equals("must not be null"));
    }

    @Test
    void testSystemStartTimestamp_IsNull() throws SQLException {
        ProjectBoundaryEntity entity = createValidProjectBoundaryEntity();
        entity.setSystemStartTimestamp(null);

        Set<ConstraintViolation<ProjectBoundaryEntity>> violations = validator.validate(entity);

        assertThat(violations)
                .hasSize(1)
                .anyMatch(violation ->
                        violation.getPropertyPath().toString().equals("systemStartTimestamp") &&
                                violation.getMessage().equals("must not be null"));
    }

    @Test
    void testSystemEndTimestamp_IsNull() throws SQLException {
        ProjectBoundaryEntity entity = createValidProjectBoundaryEntity();
        entity.setSystemEndTimestamp(null);

        Set<ConstraintViolation<ProjectBoundaryEntity>> violations = validator.validate(entity);

        assertThat(violations)
                .hasSize(1)
                .anyMatch(violation ->
                        violation.getPropertyPath().toString().equals("systemEndTimestamp") &&
                                violation.getMessage().equals("must not be null"));
    }

    @Test
    void testCollectionDate_IsNull() throws SQLException {
        ProjectBoundaryEntity entity = createValidProjectBoundaryEntity();
        entity.setCollectionDate(null);

        Set<ConstraintViolation<ProjectBoundaryEntity>> violations = validator.validate(entity);

        assertThat(violations)
                .hasSize(1)
                .anyMatch(violation ->
                        violation.getPropertyPath().toString().equals("collectionDate") &&
                                violation.getMessage().equals("must not be null"));
    }

    @Test
    void testBoundarySizeHa_IsNull() throws SQLException {
        ProjectBoundaryEntity entity = createValidProjectBoundaryEntity();
        entity.setBoundarySizeHa(null);

        Set<ConstraintViolation<ProjectBoundaryEntity>> violations = validator.validate(entity);

        assertThat(violations)
                .hasSize(1)
                .anyMatch(violation ->
                        violation.getPropertyPath().toString().equals("boundarySizeHa") &&
                                violation.getMessage().equals("must not be null"));
    }

    @Test
    void testCreateUser_IsNull() throws SQLException {
        ProjectBoundaryEntity entity = createValidProjectBoundaryEntity();
        entity.setCreateUser(null);

        Set<ConstraintViolation<ProjectBoundaryEntity>> violations = validator.validate(entity);

        assertThat(violations)
                .hasSize(1)
                .anyMatch(violation ->
                        violation.getPropertyPath().toString().equals("createUser") &&
                                violation.getMessage().equals("must not be null"));
    }

    @Test
    void testCreateDate_IsNull() throws SQLException {
        ProjectBoundaryEntity entity = createValidProjectBoundaryEntity();
        entity.setCreateDate(null);

        Set<ConstraintViolation<ProjectBoundaryEntity>> violations = validator.validate(entity);

        assertThat(violations)
                .hasSize(1)
                .anyMatch(violation ->
                        violation.getPropertyPath().toString().equals("createDate") &&
                                violation.getMessage().equals("must not be null"));
    }

    @Test
    void testUpdateUser_IsNull() throws SQLException {
        ProjectBoundaryEntity entity = createValidProjectBoundaryEntity();
        entity.setUpdateUser(null);

        Set<ConstraintViolation<ProjectBoundaryEntity>> violations = validator.validate(entity);

        assertThat(violations)
                .hasSize(1)
                .anyMatch(violation ->
                        violation.getPropertyPath().toString().equals("updateUser") &&
                                violation.getMessage().equals("must not be null"));
    }

    @Test
    void testUpdateDate_IsNull() throws SQLException {
        ProjectBoundaryEntity entity = createValidProjectBoundaryEntity();
        entity.setUpdateDate(null);

        Set<ConstraintViolation<ProjectBoundaryEntity>> violations = validator.validate(entity);

        assertThat(violations)
                .hasSize(1)
                .anyMatch(violation ->
                        violation.getPropertyPath().toString().equals("updateDate") &&
                                violation.getMessage().equals("must not be null"));
    }

    @Test
    void testRevisionCount_IsNull() throws SQLException {
        ProjectBoundaryEntity entity = createValidProjectBoundaryEntity();
        entity.setRevisionCount(null);

        Set<ConstraintViolation<ProjectBoundaryEntity>> violations = validator.validate(entity);

        assertThat(violations)
                .hasSize(1)
                .anyMatch(violation ->
                        violation.getPropertyPath().toString().equals("revisionCount") &&
                                violation.getMessage().equals("must not be null"));
    }

    @Test
    void testAllFields_AreValid() throws SQLException {
        ProjectBoundaryEntity entity = createValidProjectBoundaryEntity();

        Set<ConstraintViolation<ProjectBoundaryEntity>> violations = validator.validate(entity);

        assertThat(violations).isEmpty();
    }
}