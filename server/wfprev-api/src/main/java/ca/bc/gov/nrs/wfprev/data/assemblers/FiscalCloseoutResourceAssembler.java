package ca.bc.gov.nrs.wfprev.data.assemblers;

import ca.bc.gov.nrs.wfprev.controllers.ProjectFiscalController;
import ca.bc.gov.nrs.wfprev.data.entities.ProjectFiscalEntity;
import ca.bc.gov.nrs.wfprev.data.entities.FiscalCloseoutEntity;
import ca.bc.gov.nrs.wfprev.data.models.FiscalCloseoutResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
public class FiscalCloseoutResourceAssembler extends RepresentationModelAssemblerSupport<FiscalCloseoutEntity, FiscalCloseoutResponse> {

    public FiscalCloseoutResourceAssembler() {
        super(ProjectFiscalController.class, FiscalCloseoutResponse.class);
    }

    @Override
    public FiscalCloseoutResponse toModel(FiscalCloseoutEntity entity) {
        FiscalCloseoutResponse model = instantiateModel(entity);

        model.setProjectPlanFiscalCloseoutGuid(entity.getProjectPlanFiscalCloseoutGuid() != null ?
                entity.getProjectPlanFiscalCloseoutGuid().toString() : null);
        model.setProjectPlanFiscalGuid(entity.getProjectFiscal() != null ?
                entity.getProjectFiscal().getProjectPlanFiscalGuid().toString() : null);
        model.setOutcomeComment(entity.getOutcomeComment());
        model.setSubmittedByName(entity.getSubmittedByName());
        model.setSubmittedByUserid(entity.getSubmittedByUserid());
        model.setSubmittedByGuid(entity.getSubmittedByGuid());
        model.setRevisionCount(entity.getRevisionCount());
        model.setCreateUser(entity.getCreateUser());
        model.setCreateDate(entity.getCreateDate());
        model.setUpdateUser(entity.getUpdateUser());
        model.setUpdateDate(entity.getUpdateDate());

        return model;
    }

    public FiscalCloseoutEntity toEntity(FiscalCloseoutResponse model, ProjectFiscalEntity projectFiscalEntity) {
        FiscalCloseoutEntity entity = new FiscalCloseoutEntity();

        if (model.getProjectPlanFiscalCloseoutGuid() != null) {
            entity.setProjectPlanFiscalCloseoutGuid(UUID.fromString(model.getProjectPlanFiscalCloseoutGuid()));
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

    public FiscalCloseoutEntity updateEntity(FiscalCloseoutResponse model, FiscalCloseoutEntity existingEntity) {
        existingEntity.setOutcomeComment(model.getOutcomeComment());
        existingEntity.setRevisionCount(model.getRevisionCount());
        existingEntity.setUpdateUser(model.getUpdateUser());
        existingEntity.setUpdateDate(model.getUpdateDate());

        return existingEntity;
    }
}
