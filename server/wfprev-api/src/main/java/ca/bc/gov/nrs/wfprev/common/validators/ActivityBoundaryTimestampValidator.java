package ca.bc.gov.nrs.wfprev.common.validators;

import ca.bc.gov.nrs.wfprev.data.models.ActivityBoundaryModel;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Date;

public class ActivityBoundaryTimestampValidator implements ConstraintValidator<ActivityBoundaryTimestamps, ActivityBoundaryModel> {

    @Override
    public boolean isValid(ActivityBoundaryModel activityBoundary, ConstraintValidatorContext context) {
        // Null values are handled by @NotNull
        if (activityBoundary == null) {
            return true;
        }

        Date startDate = activityBoundary.getSystemStartTimestamp();
        Date endDate = activityBoundary.getSystemEndTimestamp();

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