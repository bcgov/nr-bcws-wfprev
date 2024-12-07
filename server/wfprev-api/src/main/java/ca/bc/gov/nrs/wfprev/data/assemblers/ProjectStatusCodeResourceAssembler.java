package ca.bc.gov.nrs.wfprev.data.assemblers;

import ca.bc.gov.nrs.wfprev.controllers.CodesController;
import ca.bc.gov.nrs.wfprev.data.entities.ProjectStatusCodeEntity;
import ca.bc.gov.nrs.wfprev.data.models.ProjectStatusCodeModel;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

@Component
public class ProjectStatusCodeResourceAssembler extends RepresentationModelAssemblerSupport<ProjectStatusCodeEntity, ProjectStatusCodeModel> {
    public ProjectStatusCodeResourceAssembler() {
        super(CodesController.class, ProjectStatusCodeModel.class);
    }

    @Override
    public ProjectStatusCodeModel toModel(ProjectStatusCodeEntity entity) {
        ProjectStatusCodeModel model = instantiateModel(entity);

        model.setProjectStatusCode(entity.getProjectStatusCode());
        model.setDescription(entity.getDescription());
        model.setDisplayOrder(entity.getDisplayOrder());
        model.setEffectiveDate(entity.getEffectiveDate());
        model.setExpiryDate(entity.getExpiryDate());

        return model;
    }

    public ProjectStatusCodeEntity toEntity(ProjectStatusCodeModel model) {
        if (model == null) {
            return null;
        }

        ProjectStatusCodeEntity entity = new ProjectStatusCodeEntity();

        entity.setProjectStatusCode(model.getProjectStatusCode());
        entity.setDescription(model.getDescription());
        entity.setDisplayOrder(model.getDisplayOrder());
        entity.setEffectiveDate(model.getEffectiveDate());
        entity.setExpiryDate(model.getExpiryDate());

        return entity;
    }
}
