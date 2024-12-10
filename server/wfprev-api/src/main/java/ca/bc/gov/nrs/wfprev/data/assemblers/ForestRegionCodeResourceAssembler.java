package ca.bc.gov.nrs.wfprev.data.assemblers;

import ca.bc.gov.nrs.wfprev.common.enums.CodeTables;
import ca.bc.gov.nrs.wfprev.controllers.CodesController;
import ca.bc.gov.nrs.wfprev.data.entities.ForestAreaCodeEntity;
import ca.bc.gov.nrs.wfprev.data.models.ForestAreaCodeModel;
import ca.bc.gov.nrs.wfprev.data.models.ForestRegionCodeModel;
import ca.bc.gov.nrs.wfprev.data.entities.ForestRegionCodeEntity;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class ForestRegionCodeResourceAssembler extends RepresentationModelAssemblerSupport<ForestRegionCodeEntity, ForestRegionCodeModel> {
    public ForestRegionCodeResourceAssembler() {
        super(CodesController.class, ForestRegionCodeModel.class);
    }

    @Override
    public ForestRegionCodeModel toModel(ForestRegionCodeEntity entity) {

        ForestRegionCodeModel resource = instantiateModel(entity);
        resource.add(linkTo(
                methodOn(CodesController.class)
                        .getCodeById(CodeTables.FOREST_REGION_CODE, entity.getOrgUnitIdentifier()))
                .withSelfRel());
        resource.setOrgunitId(entity.getOrgUnitIdentifier());
        resource.setEffectiveDate(entity.getEffectiveDate());
        resource.setExpiryDate(entity.getExpiryDate());
        resource.setForestOrgUnitTypeCode(entity.getForestOrgUnitTypeCode());
        resource.setParentOrgUnitId(entity.getParentOrgUnitIdentifier());
        resource.setOrgUnitName(entity.getOrgUnitName());
        resource.setIntegerAlias(entity.getIntegerAlias());
        resource.setCharacterAlias(entity.getCharacterAlias());

        return resource;
    }

    @Override
    public CollectionModel<ForestRegionCodeModel> toCollectionModel(Iterable<? extends ForestRegionCodeEntity> entities)
    {
        CollectionModel<ForestRegionCodeModel> resources = super.toCollectionModel(entities);

        resources.add(linkTo(methodOn(CodesController.class).getCodes(CodeTables.FOREST_REGION_CODE)).withSelfRel());

        return resources;
    }
}
