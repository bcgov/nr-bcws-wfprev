package ca.bc.gov.nrs.wfprev.entities;

import ca.bc.gov.nrs.wfprev.data.entities.EvaluationCriteriaSummaryEntity;
import ca.bc.gov.nrs.wfprev.data.entities.WUIRiskClassCodeEntity;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class EvaluationCriteriaSummaryEntityTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    private EvaluationCriteriaSummaryEntity createValidEntity() {
        WUIRiskClassCodeEntity riskClassCode = new WUIRiskClassCodeEntity();
        riskClassCode.setWuiRiskClassCode("WUI_RC_1");

        return EvaluationCriteriaSummaryEntity.builder()
                .evaluationCriteriaSummaryGuid(UUID.randomUUID())
                .projectGuid(UUID.randomUUID())
                .wuiRiskClassCode(riskClassCode)
                .localWuiRiskClassCode(riskClassCode)
                .wuiRiskClassComment("Sample comment")
                .localWuiRiskClassRationale("Sample rationale")
                .isOutsideWuiInd(true)
                .totalFilterScore(5)
                .revisionCount(0)
                .createUser("tester")
                .createDate(new Date())
                .updateUser("tester")
                .updateDate(new Date())
                .build();
    }

    @Test
    void testProjectGuid_IsNull() {
        EvaluationCriteriaSummaryEntity entity = createValidEntity();
        entity.setProjectGuid(null);

        Set<ConstraintViolation<EvaluationCriteriaSummaryEntity>> violations = validator.validate(entity);

        assertThat(violations)
                .hasSize(1)
                .anyMatch(v -> v.getPropertyPath().toString().equals("projectGuid") &&
                        v.getMessage().equals("must not be null"));
    }

    @Test
    void testOutsideWuiInd_IsNull() {
        EvaluationCriteriaSummaryEntity entity = createValidEntity();
        entity.setIsOutsideWuiInd(null);

        Set<ConstraintViolation<EvaluationCriteriaSummaryEntity>> violations = validator.validate(entity);

        assertThat(violations)
                .hasSize(1)
                .anyMatch(v -> v.getPropertyPath().toString().equals("isOutsideWuiInd") &&
                        v.getMessage().equals("must not be null"));
    }

    @Test
    void testTotalFilterScore_IsNull() {
        EvaluationCriteriaSummaryEntity entity = createValidEntity();
        entity.setTotalFilterScore(null);

        Set<ConstraintViolation<EvaluationCriteriaSummaryEntity>> violations = validator.validate(entity);

        assertThat(violations)
                .hasSize(1)
                .anyMatch(v -> v.getPropertyPath().toString().equals("totalFilterScore") &&
                        v.getMessage().equals("must not be null"));
    }

    @Test
    void testCreateUser_IsNull() {
        EvaluationCriteriaSummaryEntity entity = createValidEntity();
        entity.setCreateUser(null);

        Set<ConstraintViolation<EvaluationCriteriaSummaryEntity>> violations = validator.validate(entity);

        assertThat(violations)
                .hasSize(1)
                .anyMatch(v -> v.getPropertyPath().toString().equals("createUser") &&
                        v.getMessage().equals("must not be null"));
    }

    @Test
    void testCreateDate_IsNull() {
        EvaluationCriteriaSummaryEntity entity = createValidEntity();
        entity.setCreateDate(null);

        Set<ConstraintViolation<EvaluationCriteriaSummaryEntity>> violations = validator.validate(entity);

        assertThat(violations)
                .hasSize(1)
                .anyMatch(v -> v.getPropertyPath().toString().equals("createDate") &&
                        v.getMessage().equals("must not be null"));
    }

    @Test
    void testUpdateUser_IsNull() {
        EvaluationCriteriaSummaryEntity entity = createValidEntity();
        entity.setUpdateUser(null);

        Set<ConstraintViolation<EvaluationCriteriaSummaryEntity>> violations = validator.validate(entity);

        assertThat(violations)
                .hasSize(1)
                .anyMatch(v -> v.getPropertyPath().toString().equals("updateUser") &&
                        v.getMessage().equals("must not be null"));
    }

    @Test
    void testUpdateDate_IsNull() {
        EvaluationCriteriaSummaryEntity entity = createValidEntity();
        entity.setUpdateDate(null);

        Set<ConstraintViolation<EvaluationCriteriaSummaryEntity>> violations = validator.validate(entity);

        assertThat(violations)
                .hasSize(1)
                .anyMatch(v -> v.getPropertyPath().toString().equals("updateDate") &&
                        v.getMessage().equals("must not be null"));
    }

    @Test
    void testRevisionCount_IsNull() {
        EvaluationCriteriaSummaryEntity entity = createValidEntity();
        entity.setRevisionCount(null);

        Set<ConstraintViolation<EvaluationCriteriaSummaryEntity>> violations = validator.validate(entity);

        assertThat(violations)
                .hasSize(1)
                .anyMatch(v -> v.getPropertyPath().toString().equals("revisionCount") &&
                        v.getMessage().equals("must not be null"));
    }

    @Test
    void testAllFields_AreValid() {
        EvaluationCriteriaSummaryEntity entity = createValidEntity();

        Set<ConstraintViolation<EvaluationCriteriaSummaryEntity>> violations = validator.validate(entity);

        assertThat(violations).isEmpty();
    }
}
