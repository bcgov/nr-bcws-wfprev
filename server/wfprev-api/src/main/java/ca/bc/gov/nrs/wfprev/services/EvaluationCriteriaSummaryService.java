package ca.bc.gov.nrs.wfprev.services;

import ca.bc.gov.nrs.wfone.common.service.api.ServiceException;
import ca.bc.gov.nrs.wfprev.common.services.CommonService;
import ca.bc.gov.nrs.wfprev.data.assemblers.EvaluationCriteriaSummaryResourceAssembler;
import ca.bc.gov.nrs.wfprev.data.entities.EvaluationCriteriaSectionSummaryEntity;
import ca.bc.gov.nrs.wfprev.data.entities.EvaluationCriteriaSelectedEntity;
import ca.bc.gov.nrs.wfprev.data.entities.EvaluationCriteriaSummaryEntity;
import ca.bc.gov.nrs.wfprev.data.entities.WUIRiskClassRankEntity;
import ca.bc.gov.nrs.wfprev.data.models.EvaluationCriteriaSectionSummaryModel;
import ca.bc.gov.nrs.wfprev.data.models.EvaluationCriteriaSelectedModel;
import ca.bc.gov.nrs.wfprev.data.models.EvaluationCriteriaSummaryModel;
import ca.bc.gov.nrs.wfprev.data.repositories.EvaluationCriteriaSummaryRepository;
import ca.bc.gov.nrs.wfprev.data.repositories.WUIRiskClassCodeRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.hateoas.CollectionModel;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class EvaluationCriteriaSummaryService implements CommonService {

    private final EvaluationCriteriaSummaryRepository evaluationCriteriaSummaryRepository;
    private final EvaluationCriteriaSummaryResourceAssembler evaluationCriteriaSummaryResourceAssembler;
    private final WUIRiskClassCodeRepository wuiRiskClassCodeRepository;

    public CollectionModel<EvaluationCriteriaSummaryModel> getAllEvaluationCriteriaSummaries(String projectId) throws ServiceException {
        try {
            UUID projectGuid = UUID.fromString(projectId);
            List<EvaluationCriteriaSummaryEntity> evaluationCriteriaSummaries = evaluationCriteriaSummaryRepository.findAllByProjectGuid(projectGuid);
            return evaluationCriteriaSummaryResourceAssembler.toCollectionModel(evaluationCriteriaSummaries);
        } catch (Exception e) {
            throw new EntityNotFoundException("Evaluation criteria summaries not found for project" + projectId, e);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public EvaluationCriteriaSummaryModel createEvaluationCriteriaSummary(EvaluationCriteriaSummaryModel resource) {
        try {
            EvaluationCriteriaSummaryEntity entity = evaluationCriteriaSummaryResourceAssembler.createNewParentSummaryEntity(resource);
            // Save the summary parent
            EvaluationCriteriaSummaryEntity savedParent = evaluationCriteriaSummaryRepository.save(entity);
            // Update related entities
            evaluationCriteriaSummaryResourceAssembler.attachSectionSummary(resource, savedParent);
            return evaluationCriteriaSummaryResourceAssembler.toModel(savedParent);
        } catch (Exception e) {
            log.error("Failed to create evaluation criteria summary. Rolling back.", e);
            throw new IllegalArgumentException("Failed to create evaluation criteria summary", e);
        }
    }

    public EvaluationCriteriaSummaryModel updateEvaluationCriteriaSummary(EvaluationCriteriaSummaryModel resource) {
        try {
            UUID guid = UUID.fromString(resource.getEvaluationCriteriaSummaryGuid());
            EvaluationCriteriaSummaryEntity existingEntity = evaluationCriteriaSummaryRepository.findById(guid)
                    .orElseThrow(() -> new EntityNotFoundException("Evaluation criteria Summary not found: " + resource.getEvaluationCriteriaSummaryGuid()));

            EvaluationCriteriaSummaryEntity entity = evaluationCriteriaSummaryResourceAssembler.updateEntity(resource, existingEntity);
            assignAssociatedEntities(resource, entity);
            linkChildSummariesToParent(entity, resource);
            return saveEvaluationCriteriaSummary(entity);
        } catch (Exception e) {
            log.error("Failed to update evaluation criteria summary", e);
            throw new IllegalArgumentException("Failed to update evaluation criteria summary", e);
        }
    }

    private EvaluationCriteriaSummaryModel saveEvaluationCriteriaSummary(EvaluationCriteriaSummaryEntity entity) {
        try {
            EvaluationCriteriaSummaryEntity savedEntity = evaluationCriteriaSummaryRepository.saveAndFlush(entity);
            return evaluationCriteriaSummaryResourceAssembler.toModel(savedEntity);
        } catch (DataIntegrityViolationException | ConstraintViolationException e) {
            log.error("Data integrity or constraint violation: {}", e.getMessage(), e);
            throw e;
        } catch (EntityNotFoundException e) {
            log.error("Invalid reference data: {}", e.getMessage(), e);
            throw e;
        }
    }

    public EvaluationCriteriaSummaryModel getEvaluationCriteriaSummary(String id) {
        UUID guid = UUID.fromString(id);
        EvaluationCriteriaSummaryEntity entity = evaluationCriteriaSummaryRepository.findById(guid)
                .orElseThrow(() -> new EntityNotFoundException("evaluationCriteriaSummary not found: " + id));
        return evaluationCriteriaSummaryResourceAssembler.toModel(entity);
    }

    public void deleteEvaluationCriteriaSummary(String uuid) {
        UUID guid = UUID.fromString(uuid);

        evaluationCriteriaSummaryRepository.findById(guid)
                .orElseThrow(() -> new EntityNotFoundException("evaluationCriteriaSummary with ID: " + uuid));

        evaluationCriteriaSummaryRepository.deleteById(guid);
    }

    public void assignAssociatedEntities(EvaluationCriteriaSummaryModel resource, EvaluationCriteriaSummaryEntity entity) {
        if (resource.getWuiRiskClassCode() != null) {
            String wuiRiskClassCode = resource.getWuiRiskClassCode().getWuiRiskClassCode();
            WUIRiskClassRankEntity wuiRiskClassCodeEntity = wuiRiskClassCodeRepository
                .findByWuiRiskClassCode(wuiRiskClassCode)
                .orElseThrow(() -> new IllegalArgumentException("WUIRiskClassCode not found: " + wuiRiskClassCode));
            entity.setWuiRiskClassCode(wuiRiskClassCodeEntity);
        }

        if (resource.getLocalWuiRiskClassCode() != null) {
            String localWuiRiskClassCode = resource.getLocalWuiRiskClassCode().getWuiRiskClassCode();
            WUIRiskClassRankEntity localWuiRiskClassCodeEntity = wuiRiskClassCodeRepository
                .findByWuiRiskClassCode(localWuiRiskClassCode)
                .orElseThrow(() -> new IllegalArgumentException("Local WUIRiskClassCode not found: " + localWuiRiskClassCode));
            entity.setLocalWuiRiskClassCode(localWuiRiskClassCodeEntity);
        }
    }

    public void linkChildSummariesToParent(EvaluationCriteriaSummaryEntity parentEntity, EvaluationCriteriaSummaryModel resource) {
        if (parentEntity.getEvaluationCriteriaSectionSummaries() != null &&
                resource.getEvaluationCriteriaSectionSummaries() != null) {

            // Link section summaries to parent criteria summary
            Map<String, EvaluationCriteriaSectionSummaryModel> modelSectionMap = resource.getEvaluationCriteriaSectionSummaries().stream()
                    .filter(m -> m.getEvaluationCriteriaSectionCode() != null)
                    .collect(Collectors.toMap(
                            m -> m.getEvaluationCriteriaSectionCode().getEvaluationCriteriaSectionCode(),
                            Function.identity()
                    ));

            for (int i = 0; i < parentEntity.getEvaluationCriteriaSectionSummaries().size(); i++) {
                EvaluationCriteriaSectionSummaryEntity sectionEntity = parentEntity.getEvaluationCriteriaSectionSummaries().get(i);

                sectionEntity.setEvaluationCriteriaSummary(parentEntity);
                sectionEntity.setEvaluationCriteriaSummaryGuid(parentEntity.getEvaluationCriteriaSummaryGuid());

                String code = sectionEntity.getEvaluationCriteriaSectionCode().getEvaluationCriteriaSectionCode();
                EvaluationCriteriaSectionSummaryModel sectionModel = modelSectionMap.get(code);
                if (sectionModel == null) continue;

                // Link selected items in section summary
                if (sectionEntity.getEvaluationCriteriaSelected() != null &&
                        sectionModel.getEvaluationCriteriaSelected() != null) {

                    Map<UUID, EvaluationCriteriaSelectedModel> selectedModelMap = sectionModel.getEvaluationCriteriaSelected().stream()
                            .collect(Collectors.toMap(
                                    m -> UUID.fromString(m.getEvaluationCriteriaGuid()),
                                    Function.identity()
                            ));

                    for (EvaluationCriteriaSelectedEntity selectedEntity : sectionEntity.getEvaluationCriteriaSelected()) {
                        EvaluationCriteriaSelectedModel model = selectedModelMap.get(selectedEntity.getEvaluationCriteriaGuid());
                        if (model != null) {
                            selectedEntity.setIsEvaluationCriteriaSelectedInd(model.getIsEvaluationCriteriaSelectedInd());
                        }
                    }
                }
            }
        }
    }

}
