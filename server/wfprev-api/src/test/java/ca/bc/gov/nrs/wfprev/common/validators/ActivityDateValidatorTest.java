package ca.bc.gov.nrs.wfprev.common.validators;

import ca.bc.gov.nrs.wfprev.data.models.ActivityModel;
import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

public class ActivityDateValidatorTest {

    private ActivityDateValidator validator;
    private ConstraintValidatorContext context;

    @BeforeEach
    void setUp() {
        validator = new ActivityDateValidator();
        context = mock(ConstraintValidatorContext.class);
        ConstraintValidatorContext.ConstraintViolationBuilder builder = mock(ConstraintValidatorContext.ConstraintViolationBuilder.class);
        when(context.buildConstraintViolationWithTemplate(anyString())).thenReturn(builder);
    }

    @Test
    void testValidDates() {
        // Given
        ActivityModel activity = new ActivityModel();
        activity.setActivityStartDate(new Date(1700000000000L)); // Some timestamp
        activity.setActivityEndDate(new Date(1800000000000L)); // Later timestamp

        // When
        boolean result = validator.isValid(activity, context);

        // Then
        assertTrue(result);
    }

    @Test
    void testNullActivityModel() {
        // When
        boolean result = validator.isValid(null, context);

        // Then
        assertTrue(result);
    }

    @Test
    void testNullDates() {
        // Given
        ActivityModel activity = new ActivityModel();
        activity.setActivityStartDate(null);
        activity.setActivityEndDate(null);

        // When
        boolean result = validator.isValid(activity, context);

        // Then
        assertTrue(result);
    }

    @Test
    void testNullEndDate() {
        // Given
        ActivityModel activity = new ActivityModel();
        activity.setActivityStartDate(new Date());
        activity.setActivityEndDate(null);

        // When
        boolean result = validator.isValid(activity, context);

        // Then
        assertTrue(result);
    }

    @Test
    void testNullStartDate() {
        // Given
        ActivityModel activity = new ActivityModel();
        activity.setActivityStartDate(null);
        activity.setActivityEndDate(new Date());

        // When
        boolean result = validator.isValid(activity, context);

        // Then
        assertTrue(result);
    }
}
