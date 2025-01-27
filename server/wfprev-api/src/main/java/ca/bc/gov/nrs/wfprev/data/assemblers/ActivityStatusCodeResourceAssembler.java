package ca.bc.gov.nrs.wfprev.data.assemblers;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

import ca.bc.gov.nrs.wfprev.common.enums.CodeTables;
import ca.bc.gov.nrs.wfprev.controllers.CodesController;
import ca.bc.gov.nrs.wfprev.data.entities.ActivityStatusCodeEntity;
import ca.bc.gov.nrs.wfprev.data.models.ActivityStatusCodeModel;

@Component
public class ActivityStatusCodeResourceAssembler extends RepresentationModelAssemblerSupport<ActivityStatusCodeEntity, ActivityStatusCodeModel> {

    public ActivityStatusCodeResourceAssembler() {
        super(CodesController.class, ActivityStatusCodeModel.class);
    }

    public ActivityStatusCodeEntity toEntity(ActivityStatusCodeModel model) {
        if(model == null) {
            return null;
        }
        ActivityStatusCodeEntity entity = new ActivityStatusCodeEntity();

        entity.setActivityStatusCode(model.getActivityStatusCode());
        entity.setDescription(model.getDescription());
        entity.setDisplayOrder(model.getDisplayOrder());
        entity.setEffectiveDate(model.getEffectiveDate());
        entity.setExpiryDate(model.getExpiryDate());
        entity.setRevisionCount(model.getRevisionCount());
        entity.setCreateUser(model.getCreateUser());
        entity.setCreateDate(model.getCreateDate());
        entity.setUpdateUser(model.getUpdateUser());
        entity.setUpdateDate(model.getUpdateDate());

        return entity;
    }

    @Override
    public ActivityStatusCodeModel toModel(ActivityStatusCodeEntity entity) {
        ActivityStatusCodeModel model = instantiateModel(entity);

        model.add(linkTo(
                methodOn(CodesController.class)
                        .getCodeById(CodeTables.ACTIVITY_STATUS_CODE, entity.getActivityStatusCode()))
                .withSelfRel());

        model.setActivityStatusCode(entity.getActivityStatusCode());
        model.setDescription(entity.getDescription());
        model.setDisplayOrder(entity.getDisplayOrder());
        model.setEffectiveDate(entity.getEffectiveDate());
        model.setExpiryDate(entity.getExpiryDate());
        model.setRevisionCount(entity.getRevisionCount());
        model.setCreateUser(entity.getCreateUser());
        model.setCreateDate(entity.getCreateDate());
        model.setUpdateUser(entity.getUpdateUser());
        model.setUpdateDate(entity.getUpdateDate());

        return model;
    }

    @Override
    public CollectionModel<ActivityStatusCodeModel> toCollectionModel(Iterable<? extends ActivityStatusCodeEntity> entities)
    {
        CollectionModel<ActivityStatusCodeModel> resources = super.toCollectionModel(entities);

        resources.add(linkTo(methodOn(CodesController.class).getCodes(CodeTables.ACTIVITY_STATUS_CODE)).withSelfRel());

        return resources;
    }
}
