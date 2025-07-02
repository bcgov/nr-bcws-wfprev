package ca.bc.gov.nrs.wfprev.data.assemblers;

import ca.bc.gov.nrs.wfprev.controllers.EvaluationCriteriaSummaryController;
import ca.bc.gov.nrs.wfprev.data.entities.EvaluationCriteriaSelectedEntity;
import ca.bc.gov.nrs.wfprev.data.models.EvaluationCriteriaSelectedModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
public class EvaluationCriteriaSelectedResourceAssembler extends RepresentationModelAssemblerSupport<EvaluationCriteriaSelectedEntity, EvaluationCriteriaSelectedModel> {

    public EvaluationCriteriaSelectedResourceAssembler() {
        super(EvaluationCriteriaSummaryController.class, EvaluationCriteriaSelectedModel.class);
    }

    public EvaluationCriteriaSelectedEntity toEntity(EvaluationCriteriaSelectedModel resource) {
        if (resource == null) {
            return null;
        }

        EvaluationCriteriaSelectedEntity entity = new EvaluationCriteriaSelectedEntity();
        entity.setEvaluationCriteriaGuid(UUID.fromString(resource.getEvaluationCriteriaGuid()));
        entity.setEvaluationCriteriaSectionSummaryGuid(resource.getEvaluationCriteriaSectionSummaryGuid() != null ? UUID.fromString(resource.getEvaluationCriteriaSectionSummaryGuid()) : null);
        entity.setIsEvaluationCriteriaSelectedInd(resource.getIsEvaluationCriteriaSelectedInd());
        entity.setRevisionCount(resource.getRevisionCount());
        entity.setCreateUser(resource.getCreateUser());
        entity.setCreateDate(resource.getCreateDate());
        entity.setUpdateUser(resource.getUpdateUser());
        entity.setUpdateDate(resource.getUpdateDate());

        return entity;
    }

    @Override
    public EvaluationCriteriaSelectedModel toModel(EvaluationCriteriaSelectedEntity entity) {
        EvaluationCriteriaSelectedModel resource = instantiateModel(entity);

        resource.setEvaluationCriteriaSelectedGuid(String.valueOf(entity.getEvaluationCriteriaSelectedGuid()));
        resource.setEvaluationCriteriaGuid(String.valueOf(entity.getEvaluationCriteriaGuid()));
        resource.setEvaluationCriteriaSectionSummaryGuid(String.valueOf(entity.getEvaluationCriteriaSectionSummaryGuid()));
        resource.setIsEvaluationCriteriaSelectedInd(entity.getIsEvaluationCriteriaSelectedInd());
        resource.setRevisionCount(entity.getRevisionCount());
        resource.setCreateUser(entity.getCreateUser());
        resource.setCreateDate(entity.getCreateDate());
        resource.setUpdateUser(entity.getUpdateUser());
        resource.setUpdateDate(entity.getUpdateDate());
        return resource;
    }

    public EvaluationCriteriaSelectedEntity updateEntity(EvaluationCriteriaSelectedModel model, EvaluationCriteriaSelectedEntity existingEntity) {
        log.debug(">> updateEntity");

        existingEntity.setEvaluationCriteriaSelectedGuid(existingEntity.getEvaluationCriteriaSelectedGuid());
        existingEntity.setEvaluationCriteriaGuid(UUID.fromString(model.getEvaluationCriteriaGuid()));
        existingEntity.setEvaluationCriteriaSectionSummaryGuid(UUID.fromString(model.getEvaluationCriteriaSectionSummaryGuid()));
        existingEntity.setIsEvaluationCriteriaSelectedInd(model.getIsEvaluationCriteriaSelectedInd());
        existingEntity.setRevisionCount(model.getRevisionCount());
        existingEntity.setCreateUser(existingEntity.getCreateUser());
        existingEntity.setCreateDate(existingEntity.getCreateDate());
        existingEntity.setUpdateUser(model.getUpdateUser());
        existingEntity.setUpdateDate(model.getUpdateDate());

        return existingEntity;
    }

}

