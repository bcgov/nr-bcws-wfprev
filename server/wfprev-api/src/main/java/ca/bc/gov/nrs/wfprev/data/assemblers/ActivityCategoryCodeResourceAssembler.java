package ca.bc.gov.nrs.wfprev.data.assemblers;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

import ca.bc.gov.nrs.wfprev.common.enums.CodeTables;
import ca.bc.gov.nrs.wfprev.controllers.CodesController;
import ca.bc.gov.nrs.wfprev.data.entities.ActivityCategoryCodeEntity;
import ca.bc.gov.nrs.wfprev.data.models.ActivityCategoryCodeModel;

@Component
public class ActivityCategoryCodeResourceAssembler extends RepresentationModelAssemblerSupport<ActivityCategoryCodeEntity, ActivityCategoryCodeModel> {

    public ActivityCategoryCodeResourceAssembler() {
        super(CodesController.class, ActivityCategoryCodeModel.class);
    }

    public ActivityCategoryCodeEntity toEntity(ActivityCategoryCodeModel model) {
        if (model == null) {
            return null;
        }
        ActivityCategoryCodeEntity entity = new ActivityCategoryCodeEntity();

        entity.setActivityCategoryCode(model.getActivityCategoryCode());
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
    public ActivityCategoryCodeModel toModel(ActivityCategoryCodeEntity entity) {
        ActivityCategoryCodeModel model = instantiateModel(entity);

        model.add(linkTo(
                methodOn(CodesController.class)
                        .getCodeById(CodeTables.ACTIVITY_CATEGORY_CODE, entity.getActivityCategoryCode()))
                .withSelfRel());

        model.setActivityCategoryCode(entity.getActivityCategoryCode());
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
    public CollectionModel<ActivityCategoryCodeModel> toCollectionModel(Iterable<? extends ActivityCategoryCodeEntity> entities) {
        CollectionModel<ActivityCategoryCodeModel> resources = super.toCollectionModel(entities);

        resources.add(linkTo(methodOn(CodesController.class).getCodes(CodeTables.ACTIVITY_CATEGORY_CODE)).withSelfRel());

        return resources;
    }
}