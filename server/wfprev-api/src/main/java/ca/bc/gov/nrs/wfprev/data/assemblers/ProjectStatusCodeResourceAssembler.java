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
        model.setDisplayOrder(entity.getDisplayOrder());
        model.setCreateDate(entity.getCreateDate());
        model.setCreateUser(entity.getCreateUser());
        model.setUpdateDate(entity.getUpdateDate());
        model.setUpdateUser(entity.getUpdateUser());
        model.setRevisionCount(entity.getRevisionCount());

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
        entity.setCreateDate(model.getCreateDate());
        entity.setCreateUser(model.getCreateUser());
        entity.setUpdateDate(model.getUpdateDate());
        entity.setUpdateUser(model.getUpdateUser());
        entity.setRevisionCount(model.getRevisionCount());

        return entity;
    }
}
