package ca.bc.gov.nrs.wfprev.common.validators;

import ca.bc.gov.nrs.wfprev.data.models.ProjectBoundaryModel;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Date;

public class ProjectBoundaryTimestampValidator implements ConstraintValidator<ProjectBoundaryTimestamps, ProjectBoundaryModel> {

    @Override
    public boolean isValid(ProjectBoundaryModel projectBoundary, ConstraintValidatorContext context) {
        // Null values are handled by @NotNull
        if (projectBoundary == null) {
            return true;
        }

        Date startDate = projectBoundary.getSystemStartTimestamp();
        Date endDate = projectBoundary.getSystemEndTimestamp();

        // @NotNull handles these separately
        if (startDate == null || endDate == null) {
            return true;
        }

        if (!endDate.after(startDate)) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("systemEndTimestamp must be after systemStartTimestamp")
                    .addPropertyNode("systemEndTimestamp")
                    .addConstraintViolation();
            return false;
        }

        return true;
    }
}