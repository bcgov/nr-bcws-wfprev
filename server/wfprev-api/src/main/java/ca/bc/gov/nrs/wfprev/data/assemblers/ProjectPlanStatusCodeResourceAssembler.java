package ca.bc.gov.nrs.wfprev.data.assemblers;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

import ca.bc.gov.nrs.wfprev.common.enums.CodeTables;
import ca.bc.gov.nrs.wfprev.controllers.CodesController;
import ca.bc.gov.nrs.wfprev.data.entities.ProjectPlanStatusCodeEntity;
import ca.bc.gov.nrs.wfprev.data.models.ProjectPlanStatusCodeModel;

@Component
public class ProjectPlanStatusCodeResourceAssembler extends RepresentationModelAssemblerSupport<ProjectPlanStatusCodeEntity, ProjectPlanStatusCodeModel> {

    public ProjectPlanStatusCodeResourceAssembler() {
        super(CodesController.class, ProjectPlanStatusCodeModel.class);
    }

    public ProjectPlanStatusCodeEntity toEntity(ProjectPlanStatusCodeModel resource) {
        ProjectPlanStatusCodeEntity entity = new ProjectPlanStatusCodeEntity();

        entity.setProjectPlanStatusCode(resource.getProjectPlanStatusCode());
        entity.setDescription(resource.getDescription());
        entity.setDisplayOrder(resource.getDisplayOrder());
        entity.setEffectiveDate(resource.getEffectiveDate());
        entity.setExpiryDate(resource.getExpiryDate());
        entity.setRevisionCount(resource.getRevisionCount());
        entity.setCreateUser(resource.getCreateUser());
        entity.setCreateDate(resource.getCreateDate());
        entity.setUpdateUser(resource.getUpdateUser());
        entity.setUpdateDate(resource.getUpdateDate());

        return entity;
    }

    @Override
    public ProjectPlanStatusCodeModel toModel(ProjectPlanStatusCodeEntity entity) {
        ProjectPlanStatusCodeModel resource = instantiateModel(entity);

        resource.add(linkTo(
                methodOn(CodesController.class)
                        .getCodeById(CodeTables.PROJECT_PLAN_STATUS_CODE, entity.getProjectPlanStatusCode()))
                .withSelfRel());

        resource.setProjectPlanStatusCode(entity.getProjectPlanStatusCode());
        resource.setDescription(entity.getDescription());
        resource.setDisplayOrder(entity.getDisplayOrder());
        resource.setEffectiveDate(entity.getEffectiveDate());
        resource.setExpiryDate(entity.getExpiryDate());
        resource.setRevisionCount(entity.getRevisionCount());
        resource.setCreateUser(entity.getCreateUser());
        resource.setCreateDate(entity.getCreateDate());
        resource.setUpdateUser(entity.getUpdateUser());
        resource.setUpdateDate(entity.getUpdateDate());

        return resource;
    }

    @Override
    public CollectionModel<ProjectPlanStatusCodeModel> toCollectionModel(Iterable<? extends ProjectPlanStatusCodeEntity> entities)
    {
        CollectionModel<ProjectPlanStatusCodeModel> resources = super.toCollectionModel(entities);

        resources.add(linkTo(methodOn(CodesController.class).getCodes(CodeTables.PROJECT_PLAN_STATUS_CODE)).withSelfRel());

        return resources;
    }
}
