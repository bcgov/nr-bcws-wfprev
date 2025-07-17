package ca.bc.gov.nrs.wfprev.data.assemblers;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import ca.bc.gov.nrs.wfprev.common.enums.CodeTables;
import ca.bc.gov.nrs.wfprev.controllers.CodesController;
import ca.bc.gov.nrs.wfprev.data.entities.WildfireOrgUnitTypeCodeEntity;
import ca.bc.gov.nrs.wfprev.data.entities.WildfireOrgUnitTypeCodeEntity;
import ca.bc.gov.nrs.wfprev.data.models.WildfireOrgUnitTypeCodeModel;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

import ca.bc.gov.nrs.wfprev.data.entities.WildfireOrgUnitEntity;
import ca.bc.gov.nrs.wfprev.data.models.WildfireOrgUnitModel;

@Component
public class WildfireOrgUnitResourceAssembler extends RepresentationModelAssemblerSupport<WildfireOrgUnitEntity, WildfireOrgUnitModel> {

    public WildfireOrgUnitResourceAssembler() {
        super(CodesController.class, WildfireOrgUnitModel.class);
    }

    public WildfireOrgUnitEntity toEntity(WildfireOrgUnitModel model) {
        if (model == null) {
            return null;
        }

        WildfireOrgUnitEntity entity = new WildfireOrgUnitEntity();

        entity.setOrgUnitIdentifier(model.getOrgUnitIdentifier());
        entity.setEffectiveDate(model.getEffectiveDate());
        entity.setExpiryDate(model.getExpiryDate());
        if(model.getWildfireOrgUnitTypeCode() != null) {
            entity.setWildfireOrgUnitTypeCode(toWildfireOrgUnitTypeCodeEntity(model.getWildfireOrgUnitTypeCode()));
        }
        entity.setParentOrgUnitIdentifier(model.getParentOrgUnitIdentifier());
        entity.setOrgUnitName(model.getOrgUnitName());
        entity.setIntegerAlias(model.getIntegerAlias());
        entity.setCharacterAlias(model.getCharacterAlias());
        entity.setRevisionCount(model.getRevisionCount());
        entity.setCreateUser(model.getCreateUser());
        entity.setCreateDate(model.getCreateDate());
        entity.setUpdateUser(model.getUpdateUser());
        entity.setUpdateDate(model.getUpdateDate());

        return entity;
    }

    @Override
    public WildfireOrgUnitModel toModel(WildfireOrgUnitEntity entity) {
        WildfireOrgUnitModel model = instantiateModel(entity);

        model.add(linkTo(
                methodOn(CodesController.class)
                        .getCodeById(CodeTables.WILDFIRE_ORG_UNIT, String.valueOf(entity.getOrgUnitIdentifier())))
                .withSelfRel());

        model.setOrgUnitIdentifier(entity.getOrgUnitIdentifier());
        model.setEffectiveDate(entity.getEffectiveDate());
        model.setExpiryDate(entity.getExpiryDate());
        if(entity.getWildfireOrgUnitTypeCode() != null){
            model.setWildfireOrgUnitTypeCode(toWildfireOrgUnitTypeCodeModel(entity.getWildfireOrgUnitTypeCode()));
        }
        model.setParentOrgUnitIdentifier(entity.getParentOrgUnitIdentifier());
        model.setOrgUnitName(entity.getOrgUnitName());
        model.setIntegerAlias(entity.getIntegerAlias());
        model.setCharacterAlias(entity.getCharacterAlias());
        model.setRevisionCount(entity.getRevisionCount());
        model.setCreateUser(entity.getCreateUser());
        model.setCreateDate(entity.getCreateDate());
        model.setUpdateUser(entity.getUpdateUser());
        model.setUpdateDate(entity.getUpdateDate());

        return model;
    }

    @Override
    public CollectionModel<WildfireOrgUnitModel> toCollectionModel(Iterable<? extends WildfireOrgUnitEntity> entities) {
        CollectionModel<WildfireOrgUnitModel> resources = super.toCollectionModel(entities);

        resources.add(linkTo(methodOn(CodesController.class).getCodes(CodeTables.WILDFIRE_ORG_UNIT)).withSelfRel());

        return resources;
    }

    private WildfireOrgUnitTypeCodeEntity toWildfireOrgUnitTypeCodeEntity(WildfireOrgUnitTypeCodeModel code) {
        if (code == null) return null;
        WildfireOrgUnitTypeCodeResourceAssembler ra = new WildfireOrgUnitTypeCodeResourceAssembler();
        return ra.toEntity(code);
    }

    private WildfireOrgUnitTypeCodeModel toWildfireOrgUnitTypeCodeModel(WildfireOrgUnitTypeCodeEntity code) {
        if (code == null) return null;
        WildfireOrgUnitTypeCodeResourceAssembler ra = new WildfireOrgUnitTypeCodeResourceAssembler();
        return ra.toModel(code);
    }
}
