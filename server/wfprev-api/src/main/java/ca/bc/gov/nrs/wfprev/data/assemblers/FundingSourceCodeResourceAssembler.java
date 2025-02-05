package ca.bc.gov.nrs.wfprev.data.assemblers;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.util.UUID;

import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

import ca.bc.gov.nrs.wfprev.common.enums.CodeTables;
import ca.bc.gov.nrs.wfprev.controllers.CodesController;
import ca.bc.gov.nrs.wfprev.data.entities.FundingSourceCodeEntity;
import ca.bc.gov.nrs.wfprev.data.models.FundingSourceCodeModel;

@Component
public class FundingSourceCodeResourceAssembler extends RepresentationModelAssemblerSupport<FundingSourceCodeEntity, FundingSourceCodeModel> {

    public FundingSourceCodeResourceAssembler() {
        super(CodesController.class, FundingSourceCodeModel.class);
    }

    public FundingSourceCodeEntity toEntity(FundingSourceCodeModel model) {
        if (model == null) {
            return null;
        }
        FundingSourceCodeEntity entity = new FundingSourceCodeEntity();

        entity.setFundingSourceGuid(UUID.fromString(model.getFundingSourceGuid()));
        entity.setFundingSourceName(model.getFundingSourceName());
        entity.setFundingSourceAbbreviation(model.getFundingSourceAbbreviation());
        entity.setRevisionCount(model.getRevisionCount());
        entity.setCreateUser(model.getCreateUser());
        entity.setCreateDate(model.getCreateDate());
        entity.setUpdateUser(model.getUpdateUser());
        entity.setUpdateDate(model.getUpdateDate());

        return entity;
    }

    @Override
    public FundingSourceCodeModel toModel(FundingSourceCodeEntity entity) {
        FundingSourceCodeModel model = instantiateModel(entity);

        model.add(linkTo(
                methodOn(CodesController.class)
                        .getCodeById(CodeTables.FUNDING_SOURCE_CODE, entity.getFundingSourceGuid().toString()))
                .withSelfRel());

        model.setFundingSourceGuid(entity.getFundingSourceGuid().toString());
        model.setFundingSourceName(entity.getFundingSourceName());
        model.setFundingSourceAbbreviation(entity.getFundingSourceAbbreviation());
        model.setRevisionCount(entity.getRevisionCount());
        model.setCreateUser(entity.getCreateUser());
        model.setCreateDate(entity.getCreateDate());
        model.setUpdateUser(entity.getUpdateUser());
        model.setUpdateDate(entity.getUpdateDate());

        return model;
    }

    @Override
    public CollectionModel<FundingSourceCodeModel> toCollectionModel(Iterable<? extends FundingSourceCodeEntity> entities) {
        CollectionModel<FundingSourceCodeModel> resources = super.toCollectionModel(entities);

        resources.add(linkTo(methodOn(CodesController.class).getCodes(CodeTables.FUNDING_SOURCE_CODE)).withSelfRel());

        return resources;
    }
}
