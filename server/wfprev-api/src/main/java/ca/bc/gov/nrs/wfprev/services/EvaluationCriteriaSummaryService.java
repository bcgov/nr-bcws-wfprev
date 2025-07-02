package ca.bc.gov.nrs.wfprev.services;

import ca.bc.gov.nrs.wfone.common.service.api.ServiceException;
import ca.bc.gov.nrs.wfprev.common.services.CommonService;
import ca.bc.gov.nrs.wfprev.data.assemblers.EvaluationCriteriaSelectedResourceAssembler;
import ca.bc.gov.nrs.wfprev.data.assemblers.EvaluationCriteriaSummaryResourceAssembler;
import ca.bc.gov.nrs.wfprev.data.entities.EvaluationCriteriaSectionSummaryEntity;
import ca.bc.gov.nrs.wfprev.data.entities.EvaluationCriteriaSelectedEntity;
import ca.bc.gov.nrs.wfprev.data.entities.EvaluationCriteriaSummaryEntity;
import ca.bc.gov.nrs.wfprev.data.entities.WUIRiskClassCodeEntity;
import ca.bc.gov.nrs.wfprev.data.models.EvaluationCriteriaSectionSummaryModel;
import ca.bc.gov.nrs.wfprev.data.models.EvaluationCriteriaSelectedModel;
import ca.bc.gov.nrs.wfprev.data.models.EvaluationCriteriaSummaryModel;
import ca.bc.gov.nrs.wfprev.data.repositories.EvaluationCriteriaSummaryRepository;
import ca.bc.gov.nrs.wfprev.data.repositories.WUIRiskClassCodeRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.hateoas.CollectionModel;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
public class EvaluationCriteriaSummaryService implements CommonService {

    private final EvaluationCriteriaSummaryRepository evaluationCriteriaSummaryRepository;
    private final EvaluationCriteriaSummaryResourceAssembler evaluationCriteriaSummaryResourceAssembler;
    private final EvaluationCriteriaSelectedResourceAssembler evaluationCriteriaSelectedResourceAssembler;
    private final WUIRiskClassCodeRepository wuiRiskClassCodeRepository;


    public EvaluationCriteriaSummaryService(EvaluationCriteriaSummaryRepository evaluationCriteriaSummaryRepository, EvaluationCriteriaSummaryResourceAssembler evaluationCriteriaSummaryResourceAssembler, WUIRiskClassCodeRepository wuiRiskClassCodeRepository,
                                            EvaluationCriteriaSelectedResourceAssembler evaluationCriteriaSelectedResourceAssembler) {
        this.evaluationCriteriaSummaryRepository = evaluationCriteriaSummaryRepository;
        this.evaluationCriteriaSummaryResourceAssembler = evaluationCriteriaSummaryResourceAssembler;
        this.evaluationCriteriaSelectedResourceAssembler = evaluationCriteriaSelectedResourceAssembler;
        this.wuiRiskClassCodeRepository = wuiRiskClassCodeRepository;
    }

    public CollectionModel<EvaluationCriteriaSummaryModel> getAllEvaluationCriteriaSummaries(String projectId) throws ServiceException {
        UUID projectGuid = UUID.fromString(projectId);
        List<EvaluationCriteriaSummaryEntity> evaluationCriteriaSummaries = evaluationCriteriaSummaryRepository.findAllByProjectGuid(projectGuid);
        return evaluationCriteriaSummaryResourceAssembler.toCollectionModel(evaluationCriteriaSummaries);
    }

    @Transactional(rollbackFor = Exception.class)
    public EvaluationCriteriaSummaryModel createEvaluationCriteriaSummary(EvaluationCriteriaSummaryModel resource) {
        try {
        initializeNewEvaluationCriteriaSummary(resource);

        EvaluationCriteriaSummaryEntity entity = evaluationCriteriaSummaryResourceAssembler.toEntity(resource);

        // Remove children temporarily before saving parent in order set correct guid in children
        List<EvaluationCriteriaSectionSummaryEntity> sectionSummaries = entity.getEvaluationCriteriaSectionSummaries();
        entity.setEvaluationCriteriaSectionSummaries(null);
        assignAssociatedEntities(resource, entity);

        // Save the summary parent
        EvaluationCriteriaSummaryEntity savedParent = evaluationCriteriaSummaryRepository.saveAndFlush(entity);

        // Reattach section summaries, populate new GUIDs, link to parent
        if (sectionSummaries != null) {
            for (int i = 0; i < sectionSummaries.size(); i++) {
                EvaluationCriteriaSectionSummaryEntity section = sectionSummaries.get(i);

                section.setEvaluationCriteriaSummary(savedParent);
                section.setEvaluationCriteriaSummaryGuid(savedParent.getEvaluationCriteriaSummaryGuid());
                section.setEvaluationCriteriaSectionSummaryGuid(UUID.randomUUID());
                section.setRevisionCount(0);

                EvaluationCriteriaSectionSummaryModel sectionModel =
                        resource.getEvaluationCriteriaSectionSummaries().get(i);

                if (sectionModel.getEvaluationCriteriaSelected() != null) {
                    List<EvaluationCriteriaSelectedEntity> selectedEntities = new ArrayList<>();

                    for (EvaluationCriteriaSelectedModel selectedModel : sectionModel.getEvaluationCriteriaSelected()) {
                        EvaluationCriteriaSelectedEntity selectedEntity = evaluationCriteriaSelectedResourceAssembler.toEntity(selectedModel);
                        selectedEntity.setEvaluationCriteriaSectionSummary(section);
                        selectedEntity.setEvaluationCriteriaSectionSummaryGuid(section.getEvaluationCriteriaSectionSummaryGuid());
                        selectedEntity.setRevisionCount(0);
                        selectedEntities.add(selectedEntity);
                    }

                    section.setEvaluationCriteriaSelected(selectedEntities);
                }
            }

            savedParent.setEvaluationCriteriaSectionSummaries(sectionSummaries);
        }

        // Save parent again (cascades to children)
        EvaluationCriteriaSummaryEntity savedWithChildren = evaluationCriteriaSummaryRepository.save(savedParent);

        return evaluationCriteriaSummaryResourceAssembler.toModel(savedWithChildren);
        } catch (Exception ex) {
            log.error("Failed to create evaluation criteria summary. Rolling back.", ex);
            throw ex;
        }
    }


    private void initializeNewEvaluationCriteriaSummary(EvaluationCriteriaSummaryModel resource) {
        resource.setEvaluationCriteriaSummaryGuid(UUID.randomUUID().toString());
        resource.setCreateDate(new Date());
        resource.setRevisionCount(0);
    }

    public EvaluationCriteriaSummaryModel updateEvaluationCriteriaSummary(EvaluationCriteriaSummaryModel resource) {
        UUID guid = UUID.fromString(resource.getEvaluationCriteriaSummaryGuid());
        EvaluationCriteriaSummaryEntity existingEntity = evaluationCriteriaSummaryRepository.findById(guid)
                .orElseThrow(() -> new EntityNotFoundException("evaluationCriteriaSummary not found: " + resource.getEvaluationCriteriaSummaryGuid()));

        EvaluationCriteriaSummaryEntity entity = evaluationCriteriaSummaryResourceAssembler.updateEntity(resource, existingEntity);
        assignAssociatedEntities(resource, entity);
        linkChildSummariesToParent(entity, resource);
        return saveEvaluationCriteriaSummary(entity);
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
            WUIRiskClassCodeEntity wuiRiskClassCodeEntity = wuiRiskClassCodeRepository
                    .findById(wuiRiskClassCode)
                    .orElseThrow(() -> new IllegalArgumentException("WUIRiskClassCode not found: " + wuiRiskClassCode));
            entity.setWuiRiskClassCode(wuiRiskClassCodeEntity);
        }

        if (resource.getLocalWuiRiskClassCode() != null) {
            String localWuiRiskClassCode = resource.getLocalWuiRiskClassCode().getWuiRiskClassCode();
            WUIRiskClassCodeEntity localWuiRiskClassCodeEntity = wuiRiskClassCodeRepository
                    .findById(localWuiRiskClassCode)
                    .orElseThrow(() -> new IllegalArgumentException("Local WUIRiskClassCode not found: " + localWuiRiskClassCode));
            entity.setLocalWuiRiskClassCode(localWuiRiskClassCodeEntity);
        }
    }

    private void linkChildSummariesToParent(EvaluationCriteriaSummaryEntity parentEntity, EvaluationCriteriaSummaryModel resource) {
        if (parentEntity.getEvaluationCriteriaSectionSummaries() != null &&
                resource.getEvaluationCriteriaSectionSummaries() != null) {

            // Link section summaries to parent criteria summary
            Map<String, EvaluationCriteriaSectionSummaryModel> modelSectionMap = resource.getEvaluationCriteriaSectionSummaries().stream()
                    .filter(m -> m.getEvaluationCriteriaSectionCode() != null)
                    .collect(Collectors.toMap(
                            m -> m.getEvaluationCriteriaSectionCode().getEvaluationCriteriaSectionCode(),
                            Function.identity()
                    ));

            for (EvaluationCriteriaSectionSummaryEntity sectionEntity : parentEntity.getEvaluationCriteriaSectionSummaries()) {
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
