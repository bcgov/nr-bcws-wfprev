package ca.bc.gov.nrs.wfprev.common.validators;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = NotEmptyActivityBoundaryValidator.class)
@Target({ ElementType.FIELD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface NotEmptyActivityBoundary {
    String message() default "Activity Boundary must not be empty";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}