package ca.bc.gov.nrs.wfprev.common.validators;

import org.hibernate.validator.constraints.Range;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.Digits;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.TYPE_USE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/** example attribute validation */

@Range(min = -90, max = 90)
@Digits(integer = 2, fraction = 7)
@Target({ METHOD, FIELD, ANNOTATION_TYPE, TYPE_USE })
@Retention(RUNTIME)
@Constraint(validatedBy = {})
@Documented
public @interface Latitude {
	Class<?>[] groups() default {};
	Class<? extends Payload>[] payload() default {};
	String message() default "Latitude value must be between -90 and 90.";
}
