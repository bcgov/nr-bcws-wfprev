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

    @Test
    void testInvalidDates_EndDateNotAfterStartDate() {
        // Given
        ActivityModel activity = new ActivityModel();
        Date startDate = new Date(1700000000000L);
        Date endDate = new Date(1700000000000L); // Same timestamp as start date
        activity.setActivityStartDate(startDate);
        activity.setActivityEndDate(endDate);

        // Mock the chain for constraint violation building with proper types
        ConstraintValidatorContext.ConstraintViolationBuilder builder = mock(ConstraintValidatorContext.ConstraintViolationBuilder.class);
        ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext nodeBuilder =
                mock(ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext.class);

        // Set up the complete mock chain
        when(context.buildConstraintViolationWithTemplate(anyString())).thenReturn(builder);
        when(builder.addPropertyNode(anyString())).thenReturn(nodeBuilder);
        when(nodeBuilder.addConstraintViolation()).thenReturn(context);

        // When
        boolean result = validator.isValid(activity, context);

        // Then
        assertFalse(result);
        verify(context).disableDefaultConstraintViolation();
        verify(context).buildConstraintViolationWithTemplate("activityEndDate must be after activityStartDate");
        verify(builder).addPropertyNode("activityEndDate");
        verify(nodeBuilder).addConstraintViolation();
    }
}
