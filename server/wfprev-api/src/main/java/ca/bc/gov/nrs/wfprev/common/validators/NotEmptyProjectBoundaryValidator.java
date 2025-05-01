package ca.bc.gov.nrs.wfprev.common.validators;

import ca.bc.gov.nrs.wfprev.data.models.ProjectBoundaryModel;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class NotEmptyProjectBoundaryValidator implements ConstraintValidator<NotEmptyProjectBoundary, ProjectBoundaryModel> {

    @Override
    public boolean isValid(ProjectBoundaryModel model, ConstraintValidatorContext context) {
        if (model == null || model.getBoundaryGeometry() == null) {
            return false;
        }
        return !model.getBoundaryGeometry().isEmpty();
    }
}

