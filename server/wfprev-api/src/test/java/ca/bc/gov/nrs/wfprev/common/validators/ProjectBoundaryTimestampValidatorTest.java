package ca.bc.gov.nrs.wfprev.common.validators;

import ca.bc.gov.nrs.wfprev.data.models.ProjectBoundaryModel;
import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ProjectBoundaryTimestampValidatorTest {

    private ProjectBoundaryTimestampValidator validator;
    private ConstraintValidatorContext context;

    @BeforeEach
    void setUp() {
        validator = new ProjectBoundaryTimestampValidator();
        context = mock(ConstraintValidatorContext.class);
        ConstraintValidatorContext.ConstraintViolationBuilder builder = mock(ConstraintValidatorContext.ConstraintViolationBuilder.class);
        when(context.buildConstraintViolationWithTemplate(anyString())).thenReturn(builder);
    }

    @Test
    void testValidTimestamps() {
        // Given
        ProjectBoundaryModel projectBoundary = new ProjectBoundaryModel();
        projectBoundary.setSystemStartTimestamp(new Date(1700000000000L)); // Some timestamp
        projectBoundary.setSystemEndTimestamp(new Date(1800000000000L)); // Later timestamp

        // When
        boolean result = validator.isValid(projectBoundary, context);

        // Then
        assertTrue(result);
    }

    @Test
    void testNullProjectBoundary() {
        // When
        boolean result = validator.isValid(null, context);

        // Then
        assertTrue(result);
    }

    @Test
    void testNullTimestamps() {
        // Given
        ProjectBoundaryModel projectBoundary = new ProjectBoundaryModel();
        projectBoundary.setSystemStartTimestamp(null);
        projectBoundary.setSystemEndTimestamp(null);

        // When
        boolean result = validator.isValid(projectBoundary, context);

        // Then
        assertTrue(result);
    }

    @Test
    void testNullEndTimestamp() {
        // Given
        ProjectBoundaryModel projectBoundary = new ProjectBoundaryModel();
        projectBoundary.setSystemStartTimestamp(new Date());
        projectBoundary.setSystemEndTimestamp(null);

        // When
        boolean result = validator.isValid(projectBoundary, context);

        // Then
        assertTrue(result);
    }

    @Test
    void testNullStartTimestamp() {
        // Given
        ProjectBoundaryModel projectBoundary = new ProjectBoundaryModel();
        projectBoundary.setSystemStartTimestamp(null);
        projectBoundary.setSystemEndTimestamp(new Date());

        // When
        boolean result = validator.isValid(projectBoundary, context);

        // Then
        assertTrue(result);
    }

    @Test
    void testInvalidTimestamps_EndTimestampNotAfterStartTimestamp() {
        // Given
        ProjectBoundaryModel boundary = new ProjectBoundaryModel();
        Date startTimestamp = new Date(1700000000000L);
        Date endTimestamp = new Date(1700000000000L); // Same timestamp as start
        boundary.setSystemStartTimestamp(startTimestamp);
        boundary.setSystemEndTimestamp(endTimestamp);

        // Mock the chain for constraint violation building
        ConstraintValidatorContext.ConstraintViolationBuilder builder = mock(ConstraintValidatorContext.ConstraintViolationBuilder.class);
        ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext nodeBuilder =
                mock(ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext.class);

        // Set up the complete mock chain
        when(context.buildConstraintViolationWithTemplate(anyString())).thenReturn(builder);
        when(builder.addPropertyNode(anyString())).thenReturn(nodeBuilder);
        when(nodeBuilder.addConstraintViolation()).thenReturn(context);

        // When
        boolean result = validator.isValid(boundary, context);

        // Then
        assertFalse(result);
        verify(context).disableDefaultConstraintViolation();
        verify(context).buildConstraintViolationWithTemplate("systemEndTimestamp must be after systemStartTimestamp");
        verify(builder).addPropertyNode("systemEndTimestamp");
        verify(nodeBuilder).addConstraintViolation();
    }
}
