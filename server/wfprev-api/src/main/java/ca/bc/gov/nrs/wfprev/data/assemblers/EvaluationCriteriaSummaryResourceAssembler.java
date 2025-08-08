package ca.bc.gov.nrs.wfprev.data.assemblers;

import ca.bc.gov.nrs.wfprev.controllers.EvaluationCriteriaSummaryController;
import ca.bc.gov.nrs.wfprev.data.entities.EvaluationCriteriaSectionCodeEntity;
import ca.bc.gov.nrs.wfprev.data.entities.EvaluationCriteriaSectionSummaryEntity;
import ca.bc.gov.nrs.wfprev.data.entities.EvaluationCriteriaSelectedEntity;
import ca.bc.gov.nrs.wfprev.data.entities.EvaluationCriteriaSummaryEntity;
import ca.bc.gov.nrs.wfprev.data.entities.WUIRiskClassRankEntity;
import ca.bc.gov.nrs.wfprev.data.models.EvaluationCriteriaSectionCodeModel;
import ca.bc.gov.nrs.wfprev.data.models.EvaluationCriteriaSectionSummaryModel;
import ca.bc.gov.nrs.wfprev.data.models.EvaluationCriteriaSelectedModel;
import ca.bc.gov.nrs.wfprev.data.models.EvaluationCriteriaSummaryModel;
import ca.bc.gov.nrs.wfprev.data.models.WUIRiskClassRankModel;
import ca.bc.gov.nrs.wfprev.data.repositories.EvaluationCriteriaSectionCodeRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
public class EvaluationCriteriaSummaryResourceAssembler extends RepresentationModelAssemblerSupport<EvaluationCriteriaSummaryEntity, EvaluationCriteriaSummaryModel> {
    private final EvaluationCriteriaSectionCodeRepository evaluationCriteriaSectionCodeRepository;

    public EvaluationCriteriaSummaryResourceAssembler(EvaluationCriteriaSectionCodeRepository evaluationCriteriaSectionCodeRepository) {
        super(EvaluationCriteriaSummaryController.class, EvaluationCriteriaSummaryModel.class);
        this.evaluationCriteriaSectionCodeRepository = evaluationCriteriaSectionCodeRepository;
    }

    public EvaluationCriteriaSummaryEntity toEntity(EvaluationCriteriaSummaryModel resource) {
        if (resource == null) {
            return null;
        }

        EvaluationCriteriaSummaryEntity entity = new EvaluationCriteriaSummaryEntity();
        entity.setEvaluationCriteriaSummaryGuid(resource.getEvaluationCriteriaSummaryGuid() != null ? UUID.fromString(resource.getEvaluationCriteriaSummaryGuid()) : null);
        entity.setProjectGuid(resource.getProjectGuid() != null ? UUID.fromString(resource.getProjectGuid()) : null);

        if (resource.getWuiRiskClassCode() != null) {
            entity.setWuiRiskClassCode(toWuiRiskClassCodeEntity(resource.getWuiRiskClassCode()));
        }

        if (resource.getLocalWuiRiskClassCode() != null) {
            entity.setLocalWuiRiskClassCode(toWuiRiskClassCodeEntity(resource.getLocalWuiRiskClassCode()));
        }

        if (resource.getEvaluationCriteriaSectionSummaries() != null && !resource.getEvaluationCriteriaSectionSummaries().isEmpty()) {
            List<EvaluationCriteriaSectionSummaryEntity> sectionSummaryEntities = new ArrayList<>();

            // handle evaluationCriteriaSectionSummaries
            for (EvaluationCriteriaSectionSummaryModel childModel : resource.getEvaluationCriteriaSectionSummaries()) {
                EvaluationCriteriaSectionSummaryEntity childEntity = new EvaluationCriteriaSectionSummaryEntity();

                childEntity.setEvaluationCriteriaSummaryGuid(resource.getEvaluationCriteriaSummaryGuid() != null ? UUID.fromString(resource.getEvaluationCriteriaSummaryGuid()) : null);
                childEntity.setEvaluationCriteriaSectionCode(toEvaluationCriteriaSectionCodeEntity(childModel.getEvaluationCriteriaSectionCode()));
                childEntity.setFilterSectionScore(childModel.getFilterSectionScore());
                childEntity.setFilterSectionComment(childModel.getFilterSectionComment());
                childEntity.setRevisionCount(childModel.getRevisionCount());
                childEntity.setCreateUser(childModel.getCreateUser());
                childEntity.setCreateDate(childModel.getCreateDate());
                childEntity.setUpdateUser(childModel.getUpdateUser());
                childEntity.setUpdateDate(childModel.getUpdateDate());
                childEntity.setEvaluationCriteriaSummary(entity);

                // Handle evaluationCriteriaSelected
                if (childModel.getEvaluationCriteriaSelected() != null && !childModel.getEvaluationCriteriaSelected().isEmpty()) {
                    List<EvaluationCriteriaSelectedEntity> selectedEntities = childModel.getEvaluationCriteriaSelected().stream()
                            .map(selectedModel -> {
                                EvaluationCriteriaSelectedEntity selectedEntity = new EvaluationCriteriaSelectedEntity();

                                selectedEntity.setEvaluationCriteriaSelectedGuid(selectedModel.getEvaluationCriteriaSelectedGuid() != null ? UUID.fromString(selectedModel.getEvaluationCriteriaSelectedGuid()) : null);
                                selectedEntity.setEvaluationCriteriaGuid(selectedModel.getEvaluationCriteriaGuid() != null ? UUID.fromString(selectedModel.getEvaluationCriteriaGuid()) : null);
                                selectedEntity.setIsEvaluationCriteriaSelectedInd(selectedModel.getIsEvaluationCriteriaSelectedInd());
                                selectedEntity.setRevisionCount(selectedModel.getRevisionCount());
                                selectedEntity.setCreateUser(selectedModel.getCreateUser());
                                selectedEntity.setCreateDate(selectedModel.getCreateDate());
                                selectedEntity.setUpdateUser(selectedModel.getUpdateUser());
                                selectedEntity.setUpdateDate(selectedModel.getUpdateDate());
                                selectedEntity.setEvaluationCriteriaSectionSummary(childEntity);
                                return selectedEntity;
                            })
                            .toList();

                    childEntity.setEvaluationCriteriaSelected(selectedEntities);
                }

                sectionSummaryEntities.add(childEntity);
            }

            entity.setEvaluationCriteriaSectionSummaries(sectionSummaryEntities);
        }

        entity.setWuiRiskClassComment(resource.getWuiRiskClassComment());
        entity.setLocalWuiRiskClassRationale(resource.getLocalWuiRiskClassRationale());
        entity.setIsOutsideWuiInd(resource.getIsOutsideWuiInd());
        entity.setTotalFilterScore(resource.getTotalFilterScore());
        entity.setRevisionCount(resource.getRevisionCount());
        entity.setCreateUser(resource.getCreateUser());
        entity.setCreateDate(resource.getCreateDate());
        entity.setUpdateUser(resource.getUpdateUser());
        entity.setUpdateDate(resource.getUpdateDate());

        return entity;
    }

    @Override
    public EvaluationCriteriaSummaryModel toModel(EvaluationCriteriaSummaryEntity entity) {
        EvaluationCriteriaSummaryModel resource = instantiateModel(entity);

        resource.setEvaluationCriteriaSummaryGuid(entity.getEvaluationCriteriaSummaryGuid() != null ? entity.getEvaluationCriteriaSummaryGuid().toString() : null);
        resource.setProjectGuid(entity.getProjectGuid() != null ? entity.getProjectGuid().toString() : null);

        if (entity.getWuiRiskClassCode() != null) {
            resource.setWuiRiskClassCode(toWuiRiskClassCodeModel(entity.getWuiRiskClassCode()));
        }
        if (entity.getLocalWuiRiskClassCode() != null) {
            resource.setLocalWuiRiskClassCode(toWuiRiskClassCodeModel(entity.getLocalWuiRiskClassCode()));
        }

        if (entity.getEvaluationCriteriaSectionSummaries() != null && !entity.getEvaluationCriteriaSectionSummaries().isEmpty()) {
            List<EvaluationCriteriaSectionSummaryModel> summaryModels = new ArrayList<>();

            for (EvaluationCriteriaSectionSummaryEntity child : entity.getEvaluationCriteriaSectionSummaries()) {
                EvaluationCriteriaSectionSummaryModel model = new EvaluationCriteriaSectionSummaryModel();

                model.setEvaluationCriteriaSectionSummaryGuid(child.getEvaluationCriteriaSectionSummaryGuid() != null ? child.getEvaluationCriteriaSectionSummaryGuid().toString() : null);
                model.setEvaluationCriteriaSummaryGuid(entity.getEvaluationCriteriaSummaryGuid() != null ? entity.getEvaluationCriteriaSummaryGuid().toString() : null);
                model.setEvaluationCriteriaSectionCode(
                        toEvaluationCriteriaSectionCodeModel(child.getEvaluationCriteriaSectionCode())
                );
                model.setFilterSectionScore(child.getFilterSectionScore());
                model.setFilterSectionComment(child.getFilterSectionComment());
                model.setRevisionCount(child.getRevisionCount());
                model.setCreateUser(child.getCreateUser());
                model.setCreateDate(child.getCreateDate());
                model.setUpdateUser(child.getUpdateUser());
                model.setUpdateDate(child.getUpdateDate());

                if (child.getEvaluationCriteriaSelected() != null && !child.getEvaluationCriteriaSelected().isEmpty()) {
                    List<EvaluationCriteriaSelectedModel> selectedModels = child.getEvaluationCriteriaSelected().stream()
                            .map(selected -> {
                                EvaluationCriteriaSelectedModel selectedModel = new EvaluationCriteriaSelectedModel();
                                selectedModel.setEvaluationCriteriaSelectedGuid(selected.getEvaluationCriteriaSelectedGuid() != null ? selected.getEvaluationCriteriaSelectedGuid().toString() : null);
                                selectedModel.setEvaluationCriteriaGuid(selected.getEvaluationCriteriaGuid() != null ? selected.getEvaluationCriteriaGuid().toString() : null);
                                selectedModel.setEvaluationCriteriaSectionSummaryGuid(
                                        child.getEvaluationCriteriaSectionSummaryGuid() != null ? child.getEvaluationCriteriaSectionSummaryGuid().toString() : null
                                );
                                selectedModel.setIsEvaluationCriteriaSelectedInd(selected.getIsEvaluationCriteriaSelectedInd());
                                selectedModel.setRevisionCount(selected.getRevisionCount());
                                selectedModel.setCreateUser(selected.getCreateUser());
                                selectedModel.setCreateDate(selected.getCreateDate());
                                selectedModel.setUpdateUser(selected.getUpdateUser());
                                selectedModel.setUpdateDate(selected.getUpdateDate());
                                return selectedModel;
                            })
                            .toList();

                    model.setEvaluationCriteriaSelected(selectedModels);
                }

                summaryModels.add(model);
            }

            resource.setEvaluationCriteriaSectionSummaries(summaryModels);
        }

        resource.setWuiRiskClassComment(entity.getWuiRiskClassComment());
        resource.setLocalWuiRiskClassRationale(entity.getLocalWuiRiskClassRationale());
        resource.setIsOutsideWuiInd(entity.getIsOutsideWuiInd());
        resource.setTotalFilterScore(entity.getTotalFilterScore());
        resource.setRevisionCount(entity.getRevisionCount());
        resource.setCreateUser(entity.getCreateUser());
        resource.setCreateDate(entity.getCreateDate());
        resource.setUpdateUser(entity.getUpdateUser());
        resource.setUpdateDate(entity.getUpdateDate());

        return resource;
    }

    public EvaluationCriteriaSummaryEntity updateEntity(EvaluationCriteriaSummaryModel model, EvaluationCriteriaSummaryEntity entity) {
        if (model == null || entity == null) return null;

        // Update top-level fields
        entity.setWuiRiskClassCode(toWuiRiskClassCodeEntity(model.getWuiRiskClassCode()));
        entity.setLocalWuiRiskClassCode(toWuiRiskClassCodeEntity(model.getLocalWuiRiskClassCode()));
        entity.setWuiRiskClassComment(model.getWuiRiskClassComment());
        entity.setLocalWuiRiskClassRationale(model.getLocalWuiRiskClassRationale());
        entity.setTotalFilterScore(model.getTotalFilterScore());
        entity.setIsOutsideWuiInd(model.getIsOutsideWuiInd());

        if (model.getEvaluationCriteriaSectionSummaries() != null) {
            // Map existing section summaries by section code
            Map<String, EvaluationCriteriaSectionSummaryEntity> existingSections = entity.getEvaluationCriteriaSectionSummaries().stream()
                    .collect(Collectors.toMap(
                            s -> s.getEvaluationCriteriaSectionCode().getEvaluationCriteriaSectionCode(),
                            Function.identity()
                    ));

            List<EvaluationCriteriaSectionSummaryEntity> updatedSections = new ArrayList<>();

            for (EvaluationCriteriaSectionSummaryModel sectionModel : model.getEvaluationCriteriaSectionSummaries()) {
                if (sectionModel.getEvaluationCriteriaSectionCode() == null) continue;

                String sectionCode = sectionModel.getEvaluationCriteriaSectionCode().getEvaluationCriteriaSectionCode();
                EvaluationCriteriaSectionSummaryEntity sectionEntity = existingSections.get(sectionCode);

                if (sectionEntity == null) {
                    // Create new section
                    sectionEntity = new EvaluationCriteriaSectionSummaryEntity();
                    sectionEntity.setEvaluationCriteriaSummary(entity);
                    sectionEntity.setEvaluationCriteriaSummaryGuid(entity.getEvaluationCriteriaSummaryGuid());
                    sectionEntity.setEvaluationCriteriaSectionCode(
                            toEvaluationCriteriaSectionCodeEntity(sectionModel.getEvaluationCriteriaSectionCode())
                    );
                } else {
                    // Always update code, even if already exists
                    sectionEntity.setEvaluationCriteriaSectionCode(
                            toEvaluationCriteriaSectionCodeEntity(sectionModel.getEvaluationCriteriaSectionCode())
                    );
                }

                sectionEntity.setFilterSectionScore(sectionModel.getFilterSectionScore());
                sectionEntity.setFilterSectionComment(sectionModel.getFilterSectionComment());

                // Update selected criteria safely
                Map<UUID, EvaluationCriteriaSelectedEntity> existingSelectedMap =
                        sectionEntity.getEvaluationCriteriaSelected().stream()
                                .collect(Collectors.toMap(EvaluationCriteriaSelectedEntity::getEvaluationCriteriaGuid, Function.identity()));

                List<EvaluationCriteriaSelectedEntity> updatedSelected = new ArrayList<>();

                if (sectionModel.getEvaluationCriteriaSelected() != null) {
                    for (EvaluationCriteriaSelectedModel selectedModel : sectionModel.getEvaluationCriteriaSelected()) {
                        UUID criteriaGuid = UUID.fromString(selectedModel.getEvaluationCriteriaGuid());
                        EvaluationCriteriaSelectedEntity selectedEntity = existingSelectedMap.get(criteriaGuid);

                        if (selectedEntity == null) {
                            selectedEntity = EvaluationCriteriaSelectedEntity.builder()
                                    .evaluationCriteriaGuid(criteriaGuid)
                                    .evaluationCriteriaSectionSummary(sectionEntity)
                                    .isEvaluationCriteriaSelectedInd(selectedModel.getIsEvaluationCriteriaSelectedInd())
                                    .build();
                        } else {
                            selectedEntity.setIsEvaluationCriteriaSelectedInd(selectedModel.getIsEvaluationCriteriaSelectedInd());
                        }

                        updatedSelected.add(selectedEntity);
                    }
                }

                sectionEntity.getEvaluationCriteriaSelected().clear();
                sectionEntity.getEvaluationCriteriaSelected().addAll(updatedSelected);

                updatedSections.add(sectionEntity);
            }

            // Replace section summaries with updated ones
            entity.getEvaluationCriteriaSectionSummaries().clear();
            entity.getEvaluationCriteriaSectionSummaries().addAll(updatedSections);
        }

        return entity;
    }

    private WUIRiskClassRankModel toWuiRiskClassCodeModel(WUIRiskClassRankEntity code) {
        WUIRiskClassCodeResourceAssembler ra = new WUIRiskClassCodeResourceAssembler();
        return ra.toModel(code);
    }

    private WUIRiskClassRankEntity toWuiRiskClassCodeEntity(WUIRiskClassRankModel code) {
        if (code == null) return null;
        WUIRiskClassCodeResourceAssembler ra = new WUIRiskClassCodeResourceAssembler();
        return ra.toEntity(code);
    }

    public EvaluationCriteriaSectionCodeEntity toEvaluationCriteriaSectionCodeEntity(EvaluationCriteriaSectionCodeModel model) {
        return evaluationCriteriaSectionCodeRepository
                .findById(model.getEvaluationCriteriaSectionCode())
                .orElseThrow(() -> new IllegalArgumentException("Invalid Section Code: " + model.getEvaluationCriteriaSectionCode()));
    }

    private EvaluationCriteriaSectionCodeModel toEvaluationCriteriaSectionCodeModel(EvaluationCriteriaSectionCodeEntity entity) {
        if (entity == null) return null;

        EvaluationCriteriaSectionCodeModel model = new EvaluationCriteriaSectionCodeModel();
        model.setEvaluationCriteriaSectionCode(entity.getEvaluationCriteriaSectionCode());
        model.setDescription(entity.getDescription());
        model.setDisplayOrder(entity.getDisplayOrder());
        model.setEffectiveDate(entity.getEffectiveDate());
        model.setExpiryDate(entity.getExpiryDate());
        model.setRevisionCount(entity.getRevisionCount());
        model.setCreateDate(entity.getCreateDate());
        model.setCreateUser(entity.getCreateUser());
        model.setUpdateDate(entity.getUpdateDate());
        model.setUpdateUser(entity.getUpdateUser());
        return model;
    }
}
