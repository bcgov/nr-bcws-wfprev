package ca.bc.gov.nrs.wfprev.data.assemblers;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.util.UUID;

import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

import ca.bc.gov.nrs.wfprev.common.enums.CodeTables;
import ca.bc.gov.nrs.wfprev.controllers.CodesController;
import ca.bc.gov.nrs.wfprev.data.entities.WUIRiskClassRankEntity;
import ca.bc.gov.nrs.wfprev.data.models.WUIRiskClassRankModel;

@Component
public class WUIRiskClassCodeResourceAssembler extends RepresentationModelAssemblerSupport<WUIRiskClassRankEntity, WUIRiskClassRankModel> {

    public WUIRiskClassCodeResourceAssembler() {
        super(CodesController.class, WUIRiskClassRankModel.class);
    }

    public WUIRiskClassRankEntity toEntity(WUIRiskClassRankModel model) {
        if(model == null) {
            return null;
        }
        WUIRiskClassRankEntity entity = new WUIRiskClassRankEntity();

        entity.setWuiRiskClassCode(model.getWuiRiskClassCode());
        if (model.getWuiRiskClassRankGuid() != null) {
            entity.setWuiRiskClassRankGuid(UUID.fromString(model.getWuiRiskClassRankGuid()));
        } else {
            entity.setWuiRiskClassRankGuid(null);
        }
        entity.setWeightedRank(model.getWeightedRank());
        entity.setRevisionCount(model.getRevisionCount());
        entity.setCreateUser(model.getCreateUser());
        entity.setCreateDate(model.getCreateDate());
        entity.setUpdateUser(model.getUpdateUser());
        entity.setUpdateDate(model.getUpdateDate());

        return entity;
    }

    @Override
    public WUIRiskClassRankModel toModel(WUIRiskClassRankEntity entity) {
        WUIRiskClassRankModel model = instantiateModel(entity);

        model.add(linkTo(
                methodOn(CodesController.class)
                        .getCodeById(CodeTables.WUI_RISK_CLASS_CODE, entity.getWuiRiskClassCode()))
                .withSelfRel());

        model.setWuiRiskClassCode(entity.getWuiRiskClassCode());
        model.setWuiRiskClassRankGuid(
            entity.getWuiRiskClassRankGuid() != null
                ? entity.getWuiRiskClassRankGuid().toString()
                : null
        );
        model.setWeightedRank(entity.getWeightedRank());
        model.setRevisionCount(entity.getRevisionCount());
        model.setCreateUser(entity.getCreateUser());
        model.setCreateDate(entity.getCreateDate());
        model.setUpdateUser(entity.getUpdateUser());
        model.setUpdateDate(entity.getUpdateDate());

        return model;
    }

    @Override
    public CollectionModel<WUIRiskClassRankModel> toCollectionModel(Iterable<? extends WUIRiskClassRankEntity> entities)
    {
        CollectionModel<WUIRiskClassRankModel> resources = super.toCollectionModel(entities);

        resources.add(linkTo(methodOn(CodesController.class).getCodes(CodeTables.WUI_RISK_CLASS_CODE)).withSelfRel());

        return resources;
    }
}
