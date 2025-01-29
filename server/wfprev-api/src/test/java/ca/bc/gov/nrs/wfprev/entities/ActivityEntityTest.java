package ca.bc.gov.nrs.wfprev.entities;

import ca.bc.gov.nrs.wfprev.data.entities.ActivityEntity;
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

class ActivityEntityTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    private ActivityEntity createValidActivityEntity() {
        return ActivityEntity.builder()
                .activityGuid(UUID.randomUUID())
                .projectPlanFiscalGuid(UUID.randomUUID())
                .silvicultureBaseGuid(UUID.randomUUID())
                .silvicultureTechniqueGuid(UUID.randomUUID())
                .silvicultureMethodGuid(UUID.randomUUID())
                .activityFundingSourceGuid(UUID.randomUUID())
                .activityName("Valid Activity Name")
                .activityDescription("Valid Activity Description")
                .activityStartDate(new Date())
                .activityEndDate(new Date())
                .plannedSpendAmount(BigDecimal.valueOf(10000.00))
                .plannedTreatmentAreaHa(BigDecimal.valueOf(50.0000))
                .reportedSpendAmount(BigDecimal.valueOf(9000.00))
                .completedAreaHa(BigDecimal.valueOf(45.0000))
                .isResultsReportableInd(true)
                .outstandingObligationsInd(false)
                .isSpatialAddedInd(true)
                .revisionCount(1)
                .createUser("tester")
                .createDate(new Date())
                .updateUser("tester")
                .updateDate(new Date())
                .build();
    }

    @Test
    void testActivityName_IsNull() {
        ActivityEntity entity = createValidActivityEntity();
        entity.setActivityName(null);

        Set<ConstraintViolation<ActivityEntity>> violations = validator.validate(entity);

        assertThat(violations)
                .hasSize(1)
                .anyMatch(violation ->
                        violation.getPropertyPath().toString().equals("activityName") &&
                                violation.getMessage().equals("must not be null"));
    }

    @Test
    void testActivityDescription_IsNull() {
        ActivityEntity entity = createValidActivityEntity();
        entity.setActivityDescription(null);

        Set<ConstraintViolation<ActivityEntity>> violations = validator.validate(entity);

        assertThat(violations)
                .hasSize(1)
                .anyMatch(violation ->
                        violation.getPropertyPath().toString().equals("activityDescription") &&
                                violation.getMessage().equals("must not be null"));
    }

    @Test
    void testActivityStartDate_IsNull() {
        ActivityEntity entity = createValidActivityEntity();
        entity.setActivityStartDate(null);

        Set<ConstraintViolation<ActivityEntity>> violations = validator.validate(entity);

        assertThat(violations)
                .hasSize(1)
                .anyMatch(violation ->
                        violation.getPropertyPath().toString().equals("activityStartDate") &&
                                violation.getMessage().equals("must not be null"));
    }

    @Test
    void testActivityEndDate_IsNull() {
        ActivityEntity entity = createValidActivityEntity();
        entity.setActivityEndDate(null);

        Set<ConstraintViolation<ActivityEntity>> violations = validator.validate(entity);

        assertThat(violations)
                .hasSize(1)
                .anyMatch(violation ->
                        violation.getPropertyPath().toString().equals("activityEndDate") &&
                                violation.getMessage().equals("must not be null"));
    }

    @Test
    void testIsResultsReportableInd_IsNull() {
        ActivityEntity entity = createValidActivityEntity();
        entity.setIsResultsReportableInd(null);

        Set<ConstraintViolation<ActivityEntity>> violations = validator.validate(entity);

        assertThat(violations)
                .hasSize(1)
                .anyMatch(violation ->
                        violation.getPropertyPath().toString().equals("isResultsReportableInd") &&
                                violation.getMessage().equals("must not be null"));
    }

    @Test
    void testOutstandingObligationsInd_IsNull() {
        ActivityEntity entity = createValidActivityEntity();
        entity.setOutstandingObligationsInd(null);

        Set<ConstraintViolation<ActivityEntity>> violations = validator.validate(entity);

        assertThat(violations)
                .hasSize(1)
                .anyMatch(violation ->
                        violation.getPropertyPath().toString().equals("outstandingObligationsInd") &&
                                violation.getMessage().equals("must not be null"));
    }

    @Test
    void testCreateUser_IsNull() {
        ActivityEntity entity = createValidActivityEntity();
        entity.setCreateUser(null);

        Set<ConstraintViolation<ActivityEntity>> violations = validator.validate(entity);

        assertThat(violations)
                .hasSize(1)
                .anyMatch(violation ->
                        violation.getPropertyPath().toString().equals("createUser") &&
                                violation.getMessage().equals("must not be null"));
    }

    @Test
    void testCreateDate_IsNull() {
        ActivityEntity entity = createValidActivityEntity();
        entity.setCreateDate(null);

        Set<ConstraintViolation<ActivityEntity>> violations = validator.validate(entity);

        assertThat(violations)
                .hasSize(1)
                .anyMatch(violation ->
                        violation.getPropertyPath().toString().equals("createDate") &&
                                violation.getMessage().equals("must not be null"));
    }

    @Test
    void testUpdateUser_IsNull() {
        ActivityEntity entity = createValidActivityEntity();
        entity.setUpdateUser(null);

        Set<ConstraintViolation<ActivityEntity>> violations = validator.validate(entity);

        assertThat(violations)
                .hasSize(1)
                .anyMatch(violation ->
                        violation.getPropertyPath().toString().equals("updateUser") &&
                                violation.getMessage().equals("must not be null"));
    }

    @Test
    void testUpdateDate_IsNull() {
        ActivityEntity entity = createValidActivityEntity();
        entity.setUpdateDate(null);

        Set<ConstraintViolation<ActivityEntity>> violations = validator.validate(entity);

        assertThat(violations)
                .hasSize(1)
                .anyMatch(violation ->
                        violation.getPropertyPath().toString().equals("updateDate") &&
                                violation.getMessage().equals("must not be null"));
    }

    @Test
    void testRevisionCount_IsNull() {
        ActivityEntity entity = createValidActivityEntity();
        entity.setRevisionCount(null);

        Set<ConstraintViolation<ActivityEntity>> violations = validator.validate(entity);

        assertThat(violations)
                .hasSize(1)
                .anyMatch(violation ->
                        violation.getPropertyPath().toString().equals("revisionCount") &&
                                violation.getMessage().equals("must not be null"));
    }

    @Test
    void testAllFields_AreValid() {
        ActivityEntity entity = createValidActivityEntity();

        Set<ConstraintViolation<ActivityEntity>> violations = validator.validate(entity);

        assertThat(violations).isEmpty();
    }
}
