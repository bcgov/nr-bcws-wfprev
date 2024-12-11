package ca.bc.gov.nrs.wfprev.data.assemblers;

import ca.bc.gov.nrs.wfprev.common.enums.CodeTables;
import ca.bc.gov.nrs.wfprev.controllers.CodesController;
import ca.bc.gov.nrs.wfprev.data.entities.BCParksOrgUnitEntity;
import ca.bc.gov.nrs.wfprev.data.entities.ForestAreaCodeEntity;
import ca.bc.gov.nrs.wfprev.data.models.BCParksRegionCodeModel;
import ca.bc.gov.nrs.wfprev.data.models.ForestAreaCodeModel;
import ca.bc.gov.nrs.wfprev.data.models.ForestDistrictUnitCodeModel;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class BCParksRegionCodeResourceAssembler extends RepresentationModelAssemblerSupport<BCParksOrgUnitEntity, BCParksRegionCodeModel> {

    public BCParksRegionCodeResourceAssembler() {
        super(CodesController.class, BCParksRegionCodeModel.class);
    }


    @Override
    public BCParksRegionCodeModel toModel(BCParksOrgUnitEntity entity) {
        BCParksRegionCodeModel resource = instantiateModel(entity);
        resource.add(linkTo(
                methodOn(CodesController.class)
                        .getCodeById(CodeTables.BC_PARKS_REGION_CODE, entity.getOrgUnitIdentifier().toString()))
                .withSelfRel());
        resource.setOrgUnitId(entity.getOrgUnitIdentifier());
        resource.setEffectiveDate(entity.getEffectiveDate());
        resource.setExpiryDate(entity.getExpiryDate());
        resource.setBcParksOrgUnitTypeCode(entity.getBcParksOrgUnitTypeCode());
        if (entity.getParentOrgUnitIdentifier() != null) {
            resource.setParentOrgUnitId(entity.getParentOrgUnitIdentifier().toString());
        }
        resource.setOrgUnitName(entity.getOrgUnitName());
        resource.setIntegerAlias(entity.getIntegerAlias());
        resource.setCharacterAlias(entity.getCharacterAlias());
        return resource;
    }
}
