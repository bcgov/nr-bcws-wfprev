package ca.bc.gov.nrs.wfprev.data.assemblers;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

import ca.bc.gov.nrs.wfprev.common.enums.CodeTables;
import ca.bc.gov.nrs.wfprev.controllers.CodesController;
import ca.bc.gov.nrs.wfprev.data.entities.AncillaryFundingSourceCodeEntity;
import ca.bc.gov.nrs.wfprev.data.models.AncillaryFundingSourceCodeModel;

@Component
public class AncillaryFundingSourceCodeResourceAssembler extends RepresentationModelAssemblerSupport<AncillaryFundingSourceCodeEntity, AncillaryFundingSourceCodeModel> {

    public AncillaryFundingSourceCodeResourceAssembler() {
        super(CodesController.class, AncillaryFundingSourceCodeModel.class);
    }

    public AncillaryFundingSourceCodeEntity toEntity(AncillaryFundingSourceCodeModel model) {
        if (model == null) {
            return null;
        }
        AncillaryFundingSourceCodeEntity entity = new AncillaryFundingSourceCodeEntity();

        entity.setAncillaryFundingSourceCode(model.getAncillaryFundingSourceCode());
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
    public AncillaryFundingSourceCodeModel toModel(AncillaryFundingSourceCodeEntity entity) {
        AncillaryFundingSourceCodeModel model = instantiateModel(entity);

        model.add(linkTo(
                methodOn(CodesController.class)
                        .getCodeById(CodeTables.ANCILLARY_FUNDING_SOURCE_CODE, entity.getFundingSourceCode()))
                .withSelfRel());

        model.setFundingSourceCode(entity.getFundingSourceCode());
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
    public CollectionModel<AncillaryFundingSourceCodeModel> toCollectionModel(Iterable<? extends AncillaryFundingSourceCodeEntity> entities) {
        CollectionModel<AncillaryFundingSourceCodeModel> resources = super.toCollectionModel(entities);

        resources.add(linkTo(methodOn(CodesController.class).getCodes(CodeTables.ANCILLARY_FUNDING_SOURCE_CODE)).withSelfRel());

        return resources;
    }
}
