package ca.bc.gov.nrs.wfprev.entities;

import ca.bc.gov.nrs.wfprev.data.entities.ObjectiveTypeCodeEntity;
import ca.bc.gov.nrs.wfprev.data.entities.ProjectEntity;
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

class ProjectEntityTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    private ProjectEntity createValidProjectEntity() {
        ObjectiveTypeCodeEntity objectiveTypeCodeEntity = new ObjectiveTypeCodeEntity();
        objectiveTypeCodeEntity.setObjectiveTypeCode("WRR");
        return ProjectEntity.builder()
                .projectGuid(UUID.randomUUID())
                .siteUnitName("Valid Site Unit")
                .programAreaGuid(UUID.randomUUID())
                .forestRegionOrgUnitId(100)
                .projectName("Valid Project Name")
                .totalActualAmount(BigDecimal.valueOf(1000.00))
                .isMultiFiscalYearProj(true)
                .primaryObjectiveTypeCode(objectiveTypeCodeEntity)
                .revisionCount(1)
                .createUser("tester")
                .createDate(new Date())
                .updateUser("tester")
                .updateDate(new Date())
                .build();
    }

    @Test
    void testSiteUnitName_IsNull() {
        ProjectEntity project = createValidProjectEntity();
        project.setSiteUnitName(null);

        Set<ConstraintViolation<ProjectEntity>> violations = validator.validate(project);

        assertThat(violations)
                .hasSize(1)
                .anyMatch(violation ->
                        violation.getPropertyPath().toString().equals("siteUnitName") &&
                                violation.getMessage().equals("must not be null"));
    }

    @Test
    void testProgramAreaGuid_IsNull() {
        ProjectEntity project = createValidProjectEntity();
        project.setProgramAreaGuid(null);

        Set<ConstraintViolation<ProjectEntity>> violations = validator.validate(project);

        assertThat(violations)
                .hasSize(1)
                .anyMatch(violation ->
                        violation.getPropertyPath().toString().equals("programAreaGuid") &&
                                violation.getMessage().equals("must not be null"));
    }

    @Test
    void testForestRegionOrgUnitId_IsNull() {
        ProjectEntity project = createValidProjectEntity();
        project.setForestRegionOrgUnitId(null);

        Set<ConstraintViolation<ProjectEntity>> violations = validator.validate(project);

        assertThat(violations)
                .hasSize(1)
                .anyMatch(violation ->
                        violation.getPropertyPath().toString().equals("forestRegionOrgUnitId") &&
                                violation.getMessage().equals("must not be null"));
    }

    @Test
    void testProjectName_IsNull() {
        ProjectEntity project = createValidProjectEntity();
        project.setProjectName(null);

        Set<ConstraintViolation<ProjectEntity>> violations = validator.validate(project);

        assertThat(violations)
                .hasSize(1)
                .anyMatch(violation ->
                        violation.getPropertyPath().toString().equals("projectName") &&
                                violation.getMessage().equals("must not be null"));
    }


    @Test
    void testTotalActualAmount_IsNull() {
        ProjectEntity project = createValidProjectEntity();
        project.setTotalActualAmount(null);

        Set<ConstraintViolation<ProjectEntity>> violations = validator.validate(project);

        assertThat(violations)
                .hasSize(1)
                .anyMatch(violation ->
                        violation.getPropertyPath().toString().equals("totalActualAmount") &&
                                violation.getMessage().equals("must not be null"));
    }

    @Test
    void testIsMultiFiscalYearProj_IsNull() {
        ProjectEntity project = createValidProjectEntity();
        project.setIsMultiFiscalYearProj(null);

        Set<ConstraintViolation<ProjectEntity>> violations = validator.validate(project);

        assertThat(violations)
                .hasSize(1)
                .anyMatch(violation ->
                        violation.getPropertyPath().toString().equals("isMultiFiscalYearProj") &&
                                violation.getMessage().equals("must not be null"));
    }

    @Test
    void testLatitude_IsOutOfRange() {
        ProjectEntity project = createValidProjectEntity();
        project.setLatitude(BigDecimal.valueOf(95.000000));

        Set<ConstraintViolation<ProjectEntity>> violations = validator.validate(project);

        assertThat(violations)
                .hasSize(1)
                .anyMatch(violation ->
                        violation.getPropertyPath().toString().equals("latitude") &&
                                violation.getMessage().contains("must be between -90 and 90"));
    }

    @Test
    void testLongitude_IsOutOfRange() {
        ProjectEntity project = createValidProjectEntity();
        project.setLongitude(BigDecimal.valueOf(200.000000));

        Set<ConstraintViolation<ProjectEntity>> violations = validator.validate(project);

        assertThat(violations)
                .hasSize(1)
                .anyMatch(violation ->
                        violation.getPropertyPath().toString().equals("longitude") &&
                                violation.getMessage().contains("must be between -180 and 180"));
    }

    @Test
    void testCreateUser_IsNull() {
        ProjectEntity project = createValidProjectEntity();
        project.setCreateUser(null);

        Set<ConstraintViolation<ProjectEntity>> violations = validator.validate(project);

        assertThat(violations)
                .hasSize(1)
                .anyMatch(violation ->
                        violation.getPropertyPath().toString().equals("createUser") &&
                                violation.getMessage().equals("must not be null"));
    }

    @Test
    void testCreateDate_IsNull() {
        ProjectEntity project = createValidProjectEntity();
        project.setCreateDate(null);

        Set<ConstraintViolation<ProjectEntity>> violations = validator.validate(project);

        assertThat(violations)
                .hasSize(1)
                .anyMatch(violation ->
                        violation.getPropertyPath().toString().equals("createDate") &&
                                violation.getMessage().equals("must not be null"));
    }

    @Test
    void testRevisionCount_IsNull() {
        ProjectEntity project = createValidProjectEntity();
        project.setRevisionCount(null);

        Set<ConstraintViolation<ProjectEntity>> violations = validator.validate(project);

        assertThat(violations)
                .hasSize(1)
                .anyMatch(violation ->
                        violation.getPropertyPath().toString().equals("revisionCount") &&
                                violation.getMessage().equals("must not be null"));
    }

    @Test
    void testUpdateUser_IsNull() {
        ProjectEntity project = createValidProjectEntity();
        project.setUpdateUser(null);

        Set<ConstraintViolation<ProjectEntity>> violations = validator.validate(project);

        assertThat(violations)
                .hasSize(1)
                .anyMatch(violation ->
                        violation.getPropertyPath().toString().equals("updateUser") &&
                                violation.getMessage().equals("must not be null"));
    }

    @Test
    void testUpdateDate_IsNull() {
        ProjectEntity project = createValidProjectEntity();
        project.setUpdateDate(null);

        Set<ConstraintViolation<ProjectEntity>> violations = validator.validate(project);

        assertThat(violations)
                .hasSize(1)
                .anyMatch(violation ->
                        violation.getPropertyPath().toString().equals("updateDate") &&
                                violation.getMessage().equals("must not be null"));
    }

    @Test
    void testPrimaryObjectiveTypeCode_IsNull() {
        ProjectEntity project = createValidProjectEntity();
        project.setPrimaryObjectiveTypeCode(null);

        Set<ConstraintViolation<ProjectEntity>> violations = validator.validate(project);

        assertThat(violations)
                .hasSize(1)
                .anyMatch(violation ->
                        violation.getPropertyPath().toString().equals("primaryObjectiveTypeCode") &&
                                violation.getMessage().equals("must not be null"));
    }

    @Test
    void testAllFields_AreValid() {
        ProjectEntity project = createValidProjectEntity();

        Set<ConstraintViolation<ProjectEntity>> violations = validator.validate(project);

        assertThat(violations).isEmpty();
    }
}