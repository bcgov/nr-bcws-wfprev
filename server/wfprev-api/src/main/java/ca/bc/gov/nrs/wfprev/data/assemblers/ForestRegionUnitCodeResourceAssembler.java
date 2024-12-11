package ca.bc.gov.nrs.wfprev.data.assemblers;

import ca.bc.gov.nrs.wfprev.common.enums.CodeTables;
import ca.bc.gov.nrs.wfprev.controllers.CodesController;
import ca.bc.gov.nrs.wfprev.data.models.ForestRegionUnitCodeModel;
import ca.bc.gov.nrs.wfprev.data.entities.ForestOrgUnitCodeEntity;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class ForestRegionUnitCodeResourceAssembler extends RepresentationModelAssemblerSupport<ForestOrgUnitCodeEntity, ForestRegionUnitCodeModel> {
    public ForestRegionUnitCodeResourceAssembler() {
        super(CodesController.class, ForestRegionUnitCodeModel.class);
    }

    @Override
    public ForestRegionUnitCodeModel toModel(ForestOrgUnitCodeEntity entity) {

        ForestRegionUnitCodeModel resource = instantiateModel(entity);
        resource.add(linkTo(
                methodOn(CodesController.class)
                        .getCodeById(CodeTables.FOREST_REGION_CODE, entity.getOrgUnitIdentifier().toString()))
                .withSelfRel());
        resource.setOrgUnitId(entity.getOrgUnitIdentifier());
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
    public CollectionModel<ForestRegionUnitCodeModel> toCollectionModel(Iterable<? extends ForestOrgUnitCodeEntity> entities) {
        CollectionModel<ForestRegionUnitCodeModel> resources = super.toCollectionModel(entities);

        resources.add(linkTo(methodOn(CodesController.class).getCodes(CodeTables.FOREST_REGION_CODE)).withSelfRel());

        return resources;
    }
}
