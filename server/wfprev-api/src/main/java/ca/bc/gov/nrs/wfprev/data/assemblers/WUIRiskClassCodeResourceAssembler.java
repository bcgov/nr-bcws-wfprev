package ca.bc.gov.nrs.wfprev.data.assemblers;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

import ca.bc.gov.nrs.wfprev.common.enums.CodeTables;
import ca.bc.gov.nrs.wfprev.controllers.CodesController;
import ca.bc.gov.nrs.wfprev.data.entities.WUIRiskClassCodeEntity;
import ca.bc.gov.nrs.wfprev.data.models.WUIRiskClassCodeModel;

@Component
public class WUIRiskClassCodeResourceAssembler extends RepresentationModelAssemblerSupport<WUIRiskClassCodeEntity, WUIRiskClassCodeModel> {

    public WUIRiskClassCodeResourceAssembler() {
        super(CodesController.class, WUIRiskClassCodeModel.class);
    }

    public WUIRiskClassCodeEntity toEntity(WUIRiskClassCodeModel model) {
        if(model == null) {
            return null;
        }
        WUIRiskClassCodeEntity entity = new WUIRiskClassCodeEntity();

        entity.setWuiRiskClassCode(model.getWuiRiskClassCode());
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
    public WUIRiskClassCodeModel toModel(WUIRiskClassCodeEntity entity) {
        WUIRiskClassCodeModel model = instantiateModel(entity);

        model.add(linkTo(
                methodOn(CodesController.class)
                        .getCodeById(CodeTables.WUI_RISK_CLASS_CODE, entity.getWuiRiskClassCode()))
                .withSelfRel());

        model.setWuiRiskClassCode(entity.getWuiRiskClassCode());
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
    public CollectionModel<WUIRiskClassCodeModel> toCollectionModel(Iterable<? extends WUIRiskClassCodeEntity> entities)
    {
        CollectionModel<WUIRiskClassCodeModel> resources = super.toCollectionModel(entities);

        resources.add(linkTo(methodOn(CodesController.class).getCodes(CodeTables.WUI_RISK_CLASS_CODE)).withSelfRel());

        return resources;
    }
}
