package ca.bc.gov.nrs.wfprev.data.assemblers;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

import ca.bc.gov.nrs.wfprev.common.enums.CodeTables;
import ca.bc.gov.nrs.wfprev.controllers.CodesController;
import ca.bc.gov.nrs.wfprev.data.entities.SourceObjectNameCodeEntity;
import ca.bc.gov.nrs.wfprev.data.models.SourceObjectNameCodeModel;

@Component
public class SourceObjectNameCodeResourceAssembler extends RepresentationModelAssemblerSupport<SourceObjectNameCodeEntity, SourceObjectNameCodeModel> {

    public SourceObjectNameCodeResourceAssembler() {
        super(CodesController.class, SourceObjectNameCodeModel.class);
    }

    public SourceObjectNameCodeEntity toEntity(SourceObjectNameCodeModel model) {
        if(model == null) {
            return null;
        }
        SourceObjectNameCodeEntity entity = new SourceObjectNameCodeEntity();

        entity.setSourceObjectNameCode(model.getSourceObjectNameCode());
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
    public SourceObjectNameCodeModel toModel(SourceObjectNameCodeEntity entity) {
        SourceObjectNameCodeModel model = instantiateModel(entity);

        model.add(linkTo(
                methodOn(CodesController.class)
                        .getCodeById(CodeTables.RISK_RATING_CODE, entity.getSourceObjectNameCode()))
                .withSelfRel());

        model.setSourceObjectNameCode(entity.getSourceObjectNameCode());
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
    public CollectionModel<SourceObjectNameCodeModel> toCollectionModel(Iterable<? extends SourceObjectNameCodeEntity> entities)
    {
        CollectionModel<SourceObjectNameCodeModel> resources = super.toCollectionModel(entities);

        resources.add(linkTo(methodOn(CodesController.class).getCodes(CodeTables.RISK_RATING_CODE)).withSelfRel());

        return resources;
    }
}
