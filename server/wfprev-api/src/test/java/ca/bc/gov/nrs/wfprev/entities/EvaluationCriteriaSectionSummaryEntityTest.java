package ca.bc.gov.nrs.wfprev.entities;

import ca.bc.gov.nrs.wfprev.data.entities.EvaluationCriteriaSectionCodeEntity;
import ca.bc.gov.nrs.wfprev.data.entities.EvaluationCriteriaSectionSummaryEntity;
import ca.bc.gov.nrs.wfprev.data.entities.EvaluationCriteriaSummaryEntity;
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

class EvaluationCriteriaSectionSummaryEntityTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    private EvaluationCriteriaSectionSummaryEntity createValidEntity() {
        EvaluationCriteriaSectionCodeEntity sectionCode = new EvaluationCriteriaSectionCodeEntity();
        sectionCode.setEvaluationCriteriaSectionCode("RISK_CLASS");

        EvaluationCriteriaSummaryEntity summary = new EvaluationCriteriaSummaryEntity();
        UUID summaryGuid = UUID.randomUUID();
        summary.setEvaluationCriteriaSummaryGuid(summaryGuid);

        EvaluationCriteriaSectionSummaryEntity entity = new EvaluationCriteriaSectionSummaryEntity();
        entity.setEvaluationCriteriaSectionSummaryGuid(UUID.randomUUID());
        entity.setEvaluationCriteriaSectionCode(sectionCode);
        entity.setEvaluationCriteriaSummaryGuid(summaryGuid);
        entity.setEvaluationCriteriaSummary(summary);
        entity.setFilterSectionScore(BigDecimal.valueOf(3));
        entity.setFilterSectionComment("Comment about risk assessment");
        entity.setRevisionCount(0);
        entity.setCreateUser("tester");
        entity.setCreateDate(new Date());
        entity.setUpdateUser("tester");
        entity.setUpdateDate(new Date());

        return entity;
    }

    @Test
    void testEvaluationCriteriaSummaryGuid_IsNull() {
        EvaluationCriteriaSectionSummaryEntity entity = createValidEntity();
        entity.setEvaluationCriteriaSummaryGuid(null);

        Set<ConstraintViolation<EvaluationCriteriaSectionSummaryEntity>> violations = validator.validate(entity);

        assertThat(violations)
                .anyMatch(v -> v.getPropertyPath().toString().equals("evaluationCriteriaSummaryGuid")
                        && v.getMessage().equals("must not be null"));
    }

    @Test
    void testFilterSectionScore_IsNull() {
        EvaluationCriteriaSectionSummaryEntity entity = createValidEntity();
        entity.setFilterSectionScore(null);

        Set<ConstraintViolation<EvaluationCriteriaSectionSummaryEntity>> violations = validator.validate(entity);

        assertThat(violations)
                .anyMatch(v -> v.getPropertyPath().toString().equals("filterSectionScore")
                        && v.getMessage().equals("must not be null"));
    }

    @Test
    void testRevisionCount_IsNull() {
        EvaluationCriteriaSectionSummaryEntity entity = createValidEntity();
        entity.setRevisionCount(null);

        Set<ConstraintViolation<EvaluationCriteriaSectionSummaryEntity>> violations = validator.validate(entity);

        assertThat(violations)
                .anyMatch(v -> v.getPropertyPath().toString().equals("revisionCount")
                        && v.getMessage().equals("must not be null"));
    }

    @Test
    void testCreateUser_IsNull() {
        EvaluationCriteriaSectionSummaryEntity entity = createValidEntity();
        entity.setCreateUser(null);

        Set<ConstraintViolation<EvaluationCriteriaSectionSummaryEntity>> violations = validator.validate(entity);

        assertThat(violations)
                .anyMatch(v -> v.getPropertyPath().toString().equals("createUser")
                        && v.getMessage().equals("must not be null"));
    }

    @Test
    void testCreateDate_IsNull() {
        EvaluationCriteriaSectionSummaryEntity entity = createValidEntity();
        entity.setCreateDate(null);

        Set<ConstraintViolation<EvaluationCriteriaSectionSummaryEntity>> violations = validator.validate(entity);

        assertThat(violations)
                .anyMatch(v -> v.getPropertyPath().toString().equals("createDate")
                        && v.getMessage().equals("must not be null"));
    }

    @Test
    void testUpdateUser_IsNull() {
        EvaluationCriteriaSectionSummaryEntity entity = createValidEntity();
        entity.setUpdateUser(null);

        Set<ConstraintViolation<EvaluationCriteriaSectionSummaryEntity>> violations = validator.validate(entity);

        assertThat(violations)
                .anyMatch(v -> v.getPropertyPath().toString().equals("updateUser")
                        && v.getMessage().equals("must not be null"));
    }

    @Test
    void testUpdateDate_IsNull() {
        EvaluationCriteriaSectionSummaryEntity entity = createValidEntity();
        entity.setUpdateDate(null);

        Set<ConstraintViolation<EvaluationCriteriaSectionSummaryEntity>> violations = validator.validate(entity);

        assertThat(violations)
                .anyMatch(v -> v.getPropertyPath().toString().equals("updateDate")
                        && v.getMessage().equals("must not be null"));
    }

    @Test
    void testAllFields_AreValid() {
        EvaluationCriteriaSectionSummaryEntity entity = createValidEntity();

        Set<ConstraintViolation<EvaluationCriteriaSectionSummaryEntity>> violations = validator.validate(entity);

        assertThat(violations).isEmpty();
    }
}
