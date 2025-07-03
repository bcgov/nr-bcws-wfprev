package ca.bc.gov.nrs.wfprev.entities;

import ca.bc.gov.nrs.wfprev.data.entities.EvaluationCriteriaSelectedEntity;
import ca.bc.gov.nrs.wfprev.data.entities.EvaluationCriteriaSectionSummaryEntity;
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

class EvaluationCriteriaSelectedEntityTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    private EvaluationCriteriaSelectedEntity createValidEntity() {
        EvaluationCriteriaSectionSummaryEntity section = new EvaluationCriteriaSectionSummaryEntity();
        section.setEvaluationCriteriaSectionSummaryGuid(UUID.randomUUID());

        return EvaluationCriteriaSelectedEntity.builder()
                .evaluationCriteriaSelectedGuid(UUID.randomUUID())
                .evaluationCriteriaGuid(UUID.randomUUID())
                .evaluationCriteriaSectionSummaryGuid(section.getEvaluationCriteriaSectionSummaryGuid())
                .isEvaluationCriteriaSelectedInd(true)
                .evaluationCriteriaSectionSummary(section)
                .revisionCount(0)
                .createUser("tester")
                .createDate(new Date())
                .updateUser("tester")
                .updateDate(new Date())
                .build();
    }

    @Test
    void testEvaluationCriteriaGuid_IsNull() {
        EvaluationCriteriaSelectedEntity entity = createValidEntity();
        entity.setEvaluationCriteriaGuid(null);

        Set<ConstraintViolation<EvaluationCriteriaSelectedEntity>> violations = validator.validate(entity);

        assertThat(violations)
                .anyMatch(v -> v.getPropertyPath().toString().equals("evaluationCriteriaGuid")
                        && v.getMessage().equals("must not be null"));
    }

    @Test
    void testIsEvaluationCriteriaSelectedInd_IsNull() {
        EvaluationCriteriaSelectedEntity entity = createValidEntity();
        entity.setIsEvaluationCriteriaSelectedInd(null);

        Set<ConstraintViolation<EvaluationCriteriaSelectedEntity>> violations = validator.validate(entity);

        assertThat(violations)
                .anyMatch(v -> v.getPropertyPath().toString().equals("isEvaluationCriteriaSelectedInd")
                        && v.getMessage().equals("must not be null"));
    }

    @Test
    void testRevisionCount_IsNull() {
        EvaluationCriteriaSelectedEntity entity = createValidEntity();
        entity.setRevisionCount(null);

        Set<ConstraintViolation<EvaluationCriteriaSelectedEntity>> violations = validator.validate(entity);

        assertThat(violations)
                .anyMatch(v -> v.getPropertyPath().toString().equals("revisionCount")
                        && v.getMessage().equals("must not be null"));
    }

    @Test
    void testCreateUser_IsNull() {
        EvaluationCriteriaSelectedEntity entity = createValidEntity();
        entity.setCreateUser(null);

        Set<ConstraintViolation<EvaluationCriteriaSelectedEntity>> violations = validator.validate(entity);

        assertThat(violations)
                .anyMatch(v -> v.getPropertyPath().toString().equals("createUser")
                        && v.getMessage().equals("must not be null"));
    }

    @Test
    void testCreateDate_IsNull() {
        EvaluationCriteriaSelectedEntity entity = createValidEntity();
        entity.setCreateDate(null);

        Set<ConstraintViolation<EvaluationCriteriaSelectedEntity>> violations = validator.validate(entity);

        assertThat(violations)
                .anyMatch(v -> v.getPropertyPath().toString().equals("createDate")
                        && v.getMessage().equals("must not be null"));
    }

    @Test
    void testUpdateUser_IsNull() {
        EvaluationCriteriaSelectedEntity entity = createValidEntity();
        entity.setUpdateUser(null);

        Set<ConstraintViolation<EvaluationCriteriaSelectedEntity>> violations = validator.validate(entity);

        assertThat(violations)
                .anyMatch(v -> v.getPropertyPath().toString().equals("updateUser")
                        && v.getMessage().equals("must not be null"));
    }

    @Test
    void testUpdateDate_IsNull() {
        EvaluationCriteriaSelectedEntity entity = createValidEntity();
        entity.setUpdateDate(null);

        Set<ConstraintViolation<EvaluationCriteriaSelectedEntity>> violations = validator.validate(entity);

        assertThat(violations)
                .anyMatch(v -> v.getPropertyPath().toString().equals("updateDate")
                        && v.getMessage().equals("must not be null"));
    }

    @Test
    void testAllFields_AreValid() {
        EvaluationCriteriaSelectedEntity entity = createValidEntity();

        Set<ConstraintViolation<EvaluationCriteriaSelectedEntity>> violations = validator.validate(entity);

        assertThat(violations).isEmpty();
    }
}
