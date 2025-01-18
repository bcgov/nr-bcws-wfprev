package ca.bc.gov.nrs.wfprev.entities;

import ca.bc.gov.nrs.wfprev.data.entities.ProjectEntity;
import ca.bc.gov.nrs.wfprev.data.entities.ProjectFiscalEntity;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ProjectFiscalEntityTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    private ProjectFiscalEntity createValidProjectFiscalEntity() {
        return ProjectFiscalEntity.builder()
                .projectPlanFiscalGuid(UUID.randomUUID())
                .project(ProjectEntity.builder().projectGuid(UUID.randomUUID()).build())
                .activityCategoryCode("CATEGORY")
                .fiscalYear(BigDecimal.valueOf(2024))
                .projectPlanStatusCode("STATUS")
                .planFiscalStatusCode("FISCAL")
                .projectFiscalName("Valid Fiscal Name")
                .firstNationsDelivPartInd(true)
                .firstNationsEngagementInd(false)
                .isApprovedInd(true)
                .isDelayedInd(false)
                .revisionCount(1)
                .createUser("tester")
                .createDate(new Date())
                .updateUser("tester")
                .updateDate(new Date())
                .build();
    }

    @Test
    void testProjectGuid_IsNull() {
        ProjectFiscalEntity entity = createValidProjectFiscalEntity();
        entity.setProject(null);

        Set<ConstraintViolation<ProjectFiscalEntity>> violations = validator.validate(entity);

        assertThat(violations)
                .hasSize(1)
                .anyMatch(violation ->
                        violation.getPropertyPath().toString().equals("project") &&
                                violation.getMessage().equals("must not be null"));
    }

    @Test
    void testActivityCategoryCode_IsNull() {
        ProjectFiscalEntity entity = createValidProjectFiscalEntity();
        entity.setActivityCategoryCode(null);

        Set<ConstraintViolation<ProjectFiscalEntity>> violations = validator.validate(entity);

        assertThat(violations)
                .hasSize(1)
                .anyMatch(violation ->
                        violation.getPropertyPath().toString().equals("activityCategoryCode") &&
                                violation.getMessage().equals("must not be null"));
    }

    @Test
    void testFiscalYear_IsNull() {
        ProjectFiscalEntity entity = createValidProjectFiscalEntity();
        entity.setFiscalYear(null);

        Set<ConstraintViolation<ProjectFiscalEntity>> violations = validator.validate(entity);

        assertThat(violations)
                .hasSize(1)
                .anyMatch(violation ->
                        violation.getPropertyPath().toString().equals("fiscalYear") &&
                                violation.getMessage().equals("must not be null"));
    }

    @Test
    void testProjectPlanStatusCode_NotNull() {
        ProjectFiscalEntity entity = createValidProjectFiscalEntity();
        entity.setProjectPlanStatusCode(null);

        Set<ConstraintViolation<ProjectFiscalEntity>> violations = validator.validate(entity);

        assertThat(violations)
                .hasSize(1)
                .anyMatch(violation ->
                        violation.getPropertyPath().toString().equals("projectPlanStatusCode") &&
                                violation.getMessage().equals("must not be null"));
    }

    @Test
    void testFirstNationsDelivPartInd_IsNull() {
        ProjectFiscalEntity entity = createValidProjectFiscalEntity();
        entity.setFirstNationsDelivPartInd(null);

        Set<ConstraintViolation<ProjectFiscalEntity>> violations = validator.validate(entity);

        assertThat(violations)
                .hasSize(1)
                .anyMatch(violation ->
                        violation.getPropertyPath().toString().equals("firstNationsDelivPartInd") &&
                                violation.getMessage().equals("must not be null"));
    }

    @Test
    void testProjectFiscalName_IsNull() {
        ProjectFiscalEntity entity = createValidProjectFiscalEntity();
        entity.setProjectFiscalName(null);

        Set<ConstraintViolation<ProjectFiscalEntity>> violations = validator.validate(entity);

        assertThat(violations)
                .hasSize(1)
                .anyMatch(violation ->
                        violation.getPropertyPath().toString().equals("projectFiscalName") &&
                                violation.getMessage().equals("must not be null"));
    }

    @Test
    void testFirstNationsEngagementInd_isNull() {
        ProjectFiscalEntity entity = createValidProjectFiscalEntity();
        entity.setFirstNationsEngagementInd(null);

        Set<ConstraintViolation<ProjectFiscalEntity>> violations = validator.validate(entity);

        assertThat(violations)
                .hasSize(1)
                .anyMatch(violation ->
                        violation.getPropertyPath().toString().equals("firstNationsEngagementInd") &&
                                violation.getMessage().equals("must not be null"));
    }

    @Test
    void testIsDelayedInd_IsNull() {
        ProjectFiscalEntity entity = createValidProjectFiscalEntity();
        entity.setIsDelayedInd(null);

        Set<ConstraintViolation<ProjectFiscalEntity>> violations = validator.validate(entity);

        assertThat(violations)
                .hasSize(1)
                .anyMatch(violation ->
                        violation.getPropertyPath().toString().equals("isDelayedInd") &&
                                violation.getMessage().equals("must not be null"));
    }

    @Test
    void testCreateUser_IsNull() {
        ProjectFiscalEntity entity = createValidProjectFiscalEntity();
        entity.setCreateUser(null);

        Set<ConstraintViolation<ProjectFiscalEntity>> violations = validator.validate(entity);

        assertThat(violations)
                .hasSize(1)
                .anyMatch(violation ->
                        violation.getPropertyPath().toString().equals("createUser") &&
                                violation.getMessage().equals("must not be null"));
    }

    @Test
    void testCreateDate_IsNull() {
        ProjectFiscalEntity entity = createValidProjectFiscalEntity();
        entity.setCreateDate(null);

        Set<ConstraintViolation<ProjectFiscalEntity>> violations = validator.validate(entity);

        assertThat(violations)
                .hasSize(1)
                .anyMatch(violation ->
                        violation.getPropertyPath().toString().equals("createDate") &&
                                violation.getMessage().equals("must not be null"));
    }

    @Test
    void testUpdateUser_IsNull() {
        ProjectFiscalEntity entity = createValidProjectFiscalEntity();
        entity.setUpdateUser(null);

        Set<ConstraintViolation<ProjectFiscalEntity>> violations = validator.validate(entity);

        assertThat(violations)
                .hasSize(1)
                .anyMatch(violation ->
                        violation.getPropertyPath().toString().equals("updateUser") &&
                                violation.getMessage().equals("must not be null"));
    }

    @Test
    void testUpdateDate_IsNull() {
        ProjectFiscalEntity entity = createValidProjectFiscalEntity();
        entity.setUpdateDate(null);

        Set<ConstraintViolation<ProjectFiscalEntity>> violations = validator.validate(entity);

        assertThat(violations)
                .hasSize(1)
                .anyMatch(violation ->
                        violation.getPropertyPath().toString().equals("updateDate") &&
                                violation.getMessage().equals("must not be null"));
    }

    @Test
    void testRevisionCount_IsNull() {
        ProjectFiscalEntity entity = createValidProjectFiscalEntity();
        entity.setRevisionCount(null);

        Set<ConstraintViolation<ProjectFiscalEntity>> violations = validator.validate(entity);

        assertThat(violations)
                .hasSize(1)
                .anyMatch(violation ->
                        violation.getPropertyPath().toString().equals("revisionCount") &&
                                violation.getMessage().equals("must not be null"));
    }

    @Test
    void testAllFields_AreValid() {
        ProjectFiscalEntity entity = createValidProjectFiscalEntity();

        Set<ConstraintViolation<ProjectFiscalEntity>> violations = validator.validate(entity);

        assertThat(violations).isEmpty();
    }

    @Test
    void testProject_IsNull() {
        ProjectFiscalEntity entity = createValidProjectFiscalEntity();
        entity.setProject(null);

        Set<ConstraintViolation<ProjectFiscalEntity>> violations = validator.validate(entity);

        assertThat(violations)
                .hasSize(1)
                .anyMatch(violation ->
                        violation.getPropertyPath().toString().equals("project") &&
                                violation.getMessage().equals("must not be null"));
    }
}