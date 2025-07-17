package ca.bc.gov.nrs.wfprev.data.assemblers;

import ca.bc.gov.nrs.wfprev.controllers.CodesController;
import ca.bc.gov.nrs.wfprev.data.entities.WildfireOrgUnitTypeCodeEntity;
import ca.bc.gov.nrs.wfprev.data.models.WildfireOrgUnitTypeCodeModel;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

@Component
public class WildfireOrgUnitTypeCodeResourceAssembler extends RepresentationModelAssemblerSupport<WildfireOrgUnitTypeCodeEntity, WildfireOrgUnitTypeCodeModel> {

    public WildfireOrgUnitTypeCodeResourceAssembler() {
        super(CodesController.class, WildfireOrgUnitTypeCodeModel.class);
    }

    public WildfireOrgUnitTypeCodeEntity toEntity(WildfireOrgUnitTypeCodeModel model) {
        if(model == null) {
            return null;
        }
        WildfireOrgUnitTypeCodeEntity entity = new WildfireOrgUnitTypeCodeEntity();

        entity.setWildfireOrgUnitTypeCode(model.getWildfireOrgUnitTypeCode());
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
    public WildfireOrgUnitTypeCodeModel toModel(WildfireOrgUnitTypeCodeEntity entity) {
        WildfireOrgUnitTypeCodeModel model = instantiateModel(entity);

        model.setWildfireOrgUnitTypeCode(entity.getWildfireOrgUnitTypeCode());
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
}
