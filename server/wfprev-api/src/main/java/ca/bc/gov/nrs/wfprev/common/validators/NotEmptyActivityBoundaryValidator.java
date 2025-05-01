package ca.bc.gov.nrs.wfprev.common.validators;

import ca.bc.gov.nrs.wfprev.data.models.ActivityBoundaryModel;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class NotEmptyActivityBoundaryValidator implements ConstraintValidator<NotEmptyActivityBoundary, ActivityBoundaryModel> {

    @Override
    public boolean isValid(ActivityBoundaryModel model, ConstraintValidatorContext context) {
        if (model == null || model.getGeometry() == null) {
            return false;
        }
        return !model.getGeometry().isEmpty();
    }
}

