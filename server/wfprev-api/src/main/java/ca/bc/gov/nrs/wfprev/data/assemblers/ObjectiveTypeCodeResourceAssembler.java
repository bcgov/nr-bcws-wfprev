package ca.bc.gov.nrs.wfprev.data.assemblers;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

import ca.bc.gov.nrs.wfprev.common.enums.CodeTables;
import ca.bc.gov.nrs.wfprev.controllers.CodesController;
import ca.bc.gov.nrs.wfprev.data.entities.ObjectiveTypeCodeEntity;
import ca.bc.gov.nrs.wfprev.data.models.ObjectiveTypeCodeModel;

@Component
public class ObjectiveTypeCodeResourceAssembler extends RepresentationModelAssemblerSupport<ObjectiveTypeCodeEntity, ObjectiveTypeCodeModel> {

    public ObjectiveTypeCodeResourceAssembler() {
        super(CodesController.class, ObjectiveTypeCodeModel.class);
    }

    public ObjectiveTypeCodeEntity toEntity(ObjectiveTypeCodeModel resource) {
        ObjectiveTypeCodeEntity entity = new ObjectiveTypeCodeEntity();

        entity.setObjectiveTypeCode(resource.getObjectiveTypeCode());
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
    public ObjectiveTypeCodeModel toModel(ObjectiveTypeCodeEntity entity) {
        ObjectiveTypeCodeModel resource = instantiateModel(entity);

        resource.add(linkTo(
                methodOn(CodesController.class)
                        .getCodeById(CodeTables.OBJECTIVE_TYPE_CODE, entity.getObjectiveTypeCode()))
                .withSelfRel());

        resource.setObjectiveTypeCode(entity.getObjectiveTypeCode());
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
    public CollectionModel<ObjectiveTypeCodeModel> toCollectionModel(Iterable<? extends ObjectiveTypeCodeEntity> entities)
    {
        CollectionModel<ObjectiveTypeCodeModel> resources = super.toCollectionModel(entities);

        resources.add(linkTo(methodOn(CodesController.class).getCodes(CodeTables.OBJECTIVE_TYPE_CODE)).withSelfRel());

        return resources;
    }
}
