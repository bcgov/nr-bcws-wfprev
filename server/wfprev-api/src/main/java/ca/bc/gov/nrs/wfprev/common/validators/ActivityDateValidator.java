package ca.bc.gov.nrs.wfprev.common.validators;

import ca.bc.gov.nrs.wfprev.data.models.ActivityModel;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Date;

public class ActivityDateValidator implements ConstraintValidator<ActivityDates, ActivityModel> {

    @Override
    public boolean isValid(ActivityModel activity, ConstraintValidatorContext context) {
        // Null values are handled by @NotNull
        if (activity == null) {
            return true;
        }

        Date startDate = activity.getActivityStartDate();
        Date endDate = activity.getActivityEndDate();

        // @NotNull handles these separately
        if (startDate == null || endDate == null) {
            return true;
        }

        if (!endDate.after(startDate)) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("activityEndDate must be after activityStartDate")
                    .addPropertyNode("activityEndDate")
                    .addConstraintViolation();
            return false;
        }

        return true;
    }
}