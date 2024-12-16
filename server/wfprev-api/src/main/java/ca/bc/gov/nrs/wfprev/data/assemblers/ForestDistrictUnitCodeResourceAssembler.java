package ca.bc.gov.nrs.wfprev.data.assemblers;

import ca.bc.gov.nrs.wfprev.common.enums.CodeTables;
import ca.bc.gov.nrs.wfprev.controllers.CodesController;
import ca.bc.gov.nrs.wfprev.data.entities.ForestOrgUnitCodeEntity;
import ca.bc.gov.nrs.wfprev.data.models.ForestDistrictUnitCodeModel;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class ForestDistrictUnitCodeResourceAssembler extends RepresentationModelAssemblerSupport<ForestOrgUnitCodeEntity, ForestDistrictUnitCodeModel> {
    public ForestDistrictUnitCodeResourceAssembler() {
        super(CodesController.class, ForestDistrictUnitCodeModel.class);
    }

    @Override
    public ForestDistrictUnitCodeModel toModel(ForestOrgUnitCodeEntity entity) {

        ForestDistrictUnitCodeModel resource = instantiateModel(entity);
        resource.add(linkTo(
                methodOn(CodesController.class)
                        .getCodeById(CodeTables.FOREST_DISTRICT_CODE, entity.getOrgUnitIdentifier().toString()))
                .withSelfRel());
        resource.setOrgUnitId(entity.getOrgUnitIdentifier());
        resource.setEffectiveDate(entity.getEffectiveDate());
        resource.setExpiryDate(entity.getExpiryDate());
        resource.setForestOrgUnitTypeCode(entity.getForestOrgUnitTypeCode());
        if (entity.getParentOrgUnitIdentifier() != null) {
            resource.setParentOrgUnitId(entity.getParentOrgUnitIdentifier().toString());
        }
        resource.setOrgUnitName(entity.getOrgUnitName());
        resource.setIntegerAlias(entity.getIntegerAlias());
        resource.setCharacterAlias(entity.getCharacterAlias());

        return resource;
    }

    @Override
    public CollectionModel<ForestDistrictUnitCodeModel> toCollectionModel(Iterable<? extends ForestOrgUnitCodeEntity> entities) {
        CollectionModel<ForestDistrictUnitCodeModel> resources = super.toCollectionModel(entities);

        resources.add(linkTo(methodOn(CodesController.class).getCodes(CodeTables.FOREST_DISTRICT_CODE)).withSelfRel());

        return resources;
    }
}
