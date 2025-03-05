package ca.bc.gov.nrs.wfprev.common.validators;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = ActivityBoundaryTimestampValidator.class)
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface ActivityBoundaryTimestamps {
    String message() default "systemEndTimestamp must be after systemStartTimestamp";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
