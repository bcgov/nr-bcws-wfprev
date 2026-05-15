package ca.bc.gov.nrs.wfprev.data.assemblers;

import ca.bc.gov.nrs.wfprev.controllers.ProjectFiscalController;
import ca.bc.gov.nrs.wfprev.data.entities.ProjectFiscalEntity;
import ca.bc.gov.nrs.wfprev.data.entities.FiscalCloseOutEntity;
import ca.bc.gov.nrs.wfprev.data.models.FiscalCloseOutModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
public class FiscalCloseOutResourceAssembler extends RepresentationModelAssemblerSupport<FiscalCloseOutEntity, FiscalCloseOutModel> {

    public FiscalCloseOutResourceAssembler() {
        super(ProjectFiscalController.class, FiscalCloseOutModel.class);
    }

    @Override
    public FiscalCloseOutModel toModel(FiscalCloseOutEntity entity) {
        FiscalCloseOutModel model = instantiateModel(entity);

        model.setProjectPlanFiscalCloseOutGuid(entity.getProjectPlanFiscalCloseOutGuid().toString());
        model.setProjectPlanFiscalGuid(entity.getProjectFiscal() != null ?
                entity.getProjectFiscal().getProjectPlanFiscalGuid().toString() : null);
        model.setOutcomeComment(entity.getOutcomeComment());

        model.setRevisionCount(entity.getRevisionCount());
        model.setCreateUser(entity.getCreateUser());
        model.setCreateDate(entity.getCreateDate());
        model.setUpdateUser(entity.getUpdateUser());
        model.setUpdateDate(entity.getUpdateDate());

        return model;
    }

    public FiscalCloseOutEntity toEntity(FiscalCloseOutModel model, ProjectFiscalEntity projectFiscalEntity) {
        FiscalCloseOutEntity entity = new FiscalCloseOutEntity();

        if (model.getProjectPlanFiscalCloseOutGuid() != null) {
            entity.setProjectPlanFiscalCloseOutGuid(UUID.fromString(model.getProjectPlanFiscalCloseOutGuid()));
        }
        entity.setProjectFiscal(projectFiscalEntity);
        entity.setOutcomeComment(model.getOutcomeComment());

        entity.setRevisionCount(model.getRevisionCount());
        entity.setCreateUser(model.getCreateUser());
        entity.setCreateDate(model.getCreateDate());
        entity.setUpdateUser(model.getUpdateUser());
        entity.setUpdateDate(model.getUpdateDate());

        return entity;
    }

    public FiscalCloseOutEntity updateEntity(FiscalCloseOutModel model, FiscalCloseOutEntity existingEntity) {
        existingEntity.setOutcomeComment(model.getOutcomeComment());
        existingEntity.setRevisionCount(model.getRevisionCount());
        existingEntity.setUpdateUser(model.getUpdateUser());
        existingEntity.setUpdateDate(model.getUpdateDate());

        return existingEntity;
    }
}
