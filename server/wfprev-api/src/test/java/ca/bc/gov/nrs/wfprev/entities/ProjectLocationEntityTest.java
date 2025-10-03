package ca.bc.gov.nrs.wfprev.entities;

import ca.bc.gov.nrs.wfprev.data.entities.ProjectLocationEntity;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ProjectLocationEntityTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    private ProjectLocationEntity createValid() {
        return ProjectLocationEntity.builder()
                .projectGuid(UUID.randomUUID())
                .latitude(new BigDecimal("49.282700"))
                .longitude(new BigDecimal("-123.120700"))
                .build();
    }

    @Test
    void testAllFields_AreValid() {
        ProjectLocationEntity entity = createValid();

        Set<ConstraintViolation<ProjectLocationEntity>> violations = validator.validate(entity);

        assertThat(violations).isEmpty();
    }

    @Test
    void testLatitude_IsOutOfRange_High() {
        ProjectLocationEntity entity = createValid();
        entity.setLatitude(new BigDecimal("95.000000"));

        Set<ConstraintViolation<ProjectLocationEntity>> violations = validator.validate(entity);

        assertThat(violations)
                .hasSize(1)
                .anyMatch(v -> v.getPropertyPath().toString().equals("latitude")
                        && v.getMessage().toLowerCase().contains("between -90 and 90"));
    }

    @Test
    void testLatitude_IsOutOfRange_Low() {
        ProjectLocationEntity entity = createValid();
        entity.setLatitude(new BigDecimal("-91.000000"));

        Set<ConstraintViolation<ProjectLocationEntity>> violations = validator.validate(entity);

        assertThat(violations)
                .hasSize(1)
                .anyMatch(v -> v.getPropertyPath().toString().equals("latitude")
                        && v.getMessage().toLowerCase().contains("between -90 and 90"));
    }

    @Test
    void testLongitude_IsOutOfRange_High() {
        ProjectLocationEntity entity = createValid();
        entity.setLongitude(new BigDecimal("181.000000"));

        Set<ConstraintViolation<ProjectLocationEntity>> violations = validator.validate(entity);

        assertThat(violations)
                .hasSize(1)
                .anyMatch(v -> v.getPropertyPath().toString().equals("longitude")
                        && v.getMessage().toLowerCase().contains("between -180 and 180"));
    }

    @Test
    void testLongitude_IsOutOfRange_Low() {
        ProjectLocationEntity entity = createValid();
        entity.setLongitude(new BigDecimal("-181.000000"));

        Set<ConstraintViolation<ProjectLocationEntity>> violations = validator.validate(entity);

        assertThat(violations)
                .hasSize(1)
                .anyMatch(v -> v.getPropertyPath().toString().equals("longitude")
                        && v.getMessage().toLowerCase().contains("between -180 and 180"));
    }

    @Test
    void testLatitude_Null_AllowsNullAtBeanValidationLayer() {
        ProjectLocationEntity entity = createValid();
        entity.setLatitude(null);

        Set<ConstraintViolation<ProjectLocationEntity>> violations = validator.validate(entity);

        assertThat(violations)
                .noneMatch(v -> v.getPropertyPath().toString().equals("latitude"));
    }

    @Test
    void testLongitude_Null_AllowsNullAtBeanValidationLayer() {
        ProjectLocationEntity entity = createValid();
        entity.setLongitude(null);

        Set<ConstraintViolation<ProjectLocationEntity>> violations = validator.validate(entity);

        assertThat(violations)
                .noneMatch(v -> v.getPropertyPath().toString().equals("longitude"));
    }

}
