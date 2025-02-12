package ca.bc.gov.nrs.wfprev.common.validators;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = GeometryValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Geometry {
    String message() default "Invalid geometry coordinates";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
