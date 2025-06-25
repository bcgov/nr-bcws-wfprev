package ca.bc.gov.nrs.wfprev.data.assemblers;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

import ca.bc.gov.nrs.wfprev.common.enums.CodeTables;
import ca.bc.gov.nrs.wfprev.controllers.CodesController;
import ca.bc.gov.nrs.wfprev.data.entities.FuelManagementObjectiveCodeEntity;
import ca.bc.gov.nrs.wfprev.data.models.FuelManagementObjectiveCodeModel;

@Component
public class FuelManagementObjectiveCodeResourceAssembler extends RepresentationModelAssemblerSupport<FuelManagementObjectiveCodeEntity, FuelManagementObjectiveCodeModel> {

    public FuelManagementObjectiveCodeResourceAssembler() {
        super(CodesController.class, FuelManagementObjectiveCodeModel.class);
    }

    public FuelManagementObjectiveCodeEntity toEntity(FuelManagementObjectiveCodeModel model) {
        if (model == null) {
            return null;
        }

        FuelManagementObjectiveCodeEntity entity = new FuelManagementObjectiveCodeEntity();
        entity.setFuelManagementObjectiveGuid(model.getFuelManagementObjectiveGuid());
        entity.setFuelManagementObjectiveTypeCode(model.getFuelManagementObjectiveTypeCode());
        entity.setObjectiveLabel(model.getObjectiveLabel());
        entity.setWeightedRank(model.getWeightedRank());
        return entity;
    }

    @Override
    public FuelManagementObjectiveCodeModel toModel(FuelManagementObjectiveCodeEntity entity) {
        FuelManagementObjectiveCodeModel model = instantiateModel(entity);

        model.add(linkTo(
                methodOn(CodesController.class)
                        .getCodeById(CodeTables.FUEL_MANAGEMENT_OBJECTIVE_CODE, entity.getFuelManagementObjectiveGuid()))
                .withSelfRel());

        model.setFuelManagementObjectiveGuid(entity.getFuelManagementObjectiveGuid());
        model.setFuelManagementObjectiveTypeCode(entity.getFuelManagementObjectiveTypeCode());
        model.setObjectiveLabel(entity.getObjectiveLabel());
        model.setWeightedRank(entity.getWeightedRank());

        return model;
    }

    @Override
    public CollectionModel<FuelManagementObjectiveCodeModel> toCollectionModel(Iterable<? extends FuelManagementObjectiveCodeEntity> entities) {
        CollectionModel<FuelManagementObjectiveCodeModel> resources = super.toCollectionModel(entities);

        resources.add(linkTo(methodOn(CodesController.class).getCodes(CodeTables.FUEL_MANAGEMENT_OBJECTIVE_CODE)).withSelfRel());

        return resources;
    }
}
