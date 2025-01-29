package ca.bc.gov.nrs.wfprev.data.assemblers;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

import ca.bc.gov.nrs.wfprev.common.enums.CodeTables;
import ca.bc.gov.nrs.wfprev.controllers.CodesController;
import ca.bc.gov.nrs.wfprev.data.entities.ContractPhaseCodeEntity;
import ca.bc.gov.nrs.wfprev.data.models.ContractPhaseCodeModel;

@Component
public class ContractPhaseCodeResourceAssembler extends RepresentationModelAssemblerSupport<ContractPhaseCodeEntity, ContractPhaseCodeModel> {

    public ContractPhaseCodeResourceAssembler() {
        super(CodesController.class, ContractPhaseCodeModel.class);
    }

    public ContractPhaseCodeEntity toEntity(ContractPhaseCodeModel model) {
        if(model == null) {
            return null;
        }
        ContractPhaseCodeEntity entity = new ContractPhaseCodeEntity();

        entity.setContractPhaseCode(model.getContractPhaseCode());
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
    public ContractPhaseCodeModel toModel(ContractPhaseCodeEntity entity) {
        ContractPhaseCodeModel model = instantiateModel(entity);

        model.add(linkTo(
                methodOn(CodesController.class)
                        .getCodeById(CodeTables.CONTRACT_PHASE_CODE, entity.getContractPhaseCode()))
                .withSelfRel());

        model.setContractPhaseCode(entity.getContractPhaseCode());
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
    public CollectionModel<ContractPhaseCodeModel> toCollectionModel(Iterable<? extends ContractPhaseCodeEntity> entities)
    {
        CollectionModel<ContractPhaseCodeModel> resources = super.toCollectionModel(entities);

        resources.add(linkTo(methodOn(CodesController.class).getCodes(CodeTables.CONTRACT_PHASE_CODE)).withSelfRel());

        return resources;
    }
}
