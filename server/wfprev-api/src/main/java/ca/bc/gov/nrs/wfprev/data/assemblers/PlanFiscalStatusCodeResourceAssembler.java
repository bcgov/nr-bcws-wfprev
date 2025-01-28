package ca.bc.gov.nrs.wfprev.data.assemblers;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

import ca.bc.gov.nrs.wfprev.common.enums.CodeTables;
import ca.bc.gov.nrs.wfprev.controllers.CodesController;
import ca.bc.gov.nrs.wfprev.data.entities.PlanFiscalStatusCodeEntity;
import ca.bc.gov.nrs.wfprev.data.models.PlanFiscalStatusCodeModel;

@Component
public class PlanFiscalStatusCodeResourceAssembler extends RepresentationModelAssemblerSupport<PlanFiscalStatusCodeEntity, PlanFiscalStatusCodeModel> {

    public PlanFiscalStatusCodeResourceAssembler() {
        super(CodesController.class, PlanFiscalStatusCodeModel.class);
    }

    public PlanFiscalStatusCodeEntity toEntity(PlanFiscalStatusCodeModel model) {
        if (model == null) {
            return null;
        }
        PlanFiscalStatusCodeEntity entity = new PlanFiscalStatusCodeEntity();

        entity.setPlanFiscalStatusCode(model.getPlanFiscalStatusCode());
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
    public PlanFiscalStatusCodeModel toModel(PlanFiscalStatusCodeEntity entity) {
        PlanFiscalStatusCodeModel model = instantiateModel(entity);

        model.add(linkTo(
                methodOn(CodesController.class)
                        .getCodeById(CodeTables.PLAN_FISCAL_STATUS_CODE, entity.getPlanFiscalStatusCode()))
                .withSelfRel());

        model.setPlanFiscalStatusCode(entity.getPlanFiscalStatusCode());
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
    public CollectionModel<PlanFiscalStatusCodeModel> toCollectionModel(Iterable<? extends PlanFiscalStatusCodeEntity> entities) {
        CollectionModel<PlanFiscalStatusCodeModel> resources = super.toCollectionModel(entities);

        resources.add(linkTo(methodOn(CodesController.class).getCodes(CodeTables.PLAN_FISCAL_STATUS_CODE)).withSelfRel());

        return resources;
    }
}