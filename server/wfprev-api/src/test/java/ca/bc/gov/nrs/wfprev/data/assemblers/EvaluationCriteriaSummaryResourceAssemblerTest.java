package ca.bc.gov.nrs.wfprev.data.assemblers;

import ca.bc.gov.nrs.wfprev.data.entities.EvaluationCriteriaSectionCodeEntity;
import ca.bc.gov.nrs.wfprev.data.entities.EvaluationCriteriaSectionSummaryEntity;
import ca.bc.gov.nrs.wfprev.data.entities.EvaluationCriteriaSelectedEntity;
import ca.bc.gov.nrs.wfprev.data.entities.EvaluationCriteriaSummaryEntity;
import ca.bc.gov.nrs.wfprev.data.models.EvaluationCriteriaSectionCodeModel;
import ca.bc.gov.nrs.wfprev.data.models.EvaluationCriteriaSectionSummaryModel;
import ca.bc.gov.nrs.wfprev.data.models.EvaluationCriteriaSelectedModel;
import ca.bc.gov.nrs.wfprev.data.models.EvaluationCriteriaSummaryModel;
import ca.bc.gov.nrs.wfprev.data.repositories.EvaluationCriteriaSectionCodeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EvaluationCriteriaSummaryResourceAssemblerTest {

    private EvaluationCriteriaSectionCodeRepository sectionCodeRepository;
    private EvaluationCriteriaSummaryResourceAssembler assembler;

    @BeforeEach
    void setUp() {
        sectionCodeRepository = mock(EvaluationCriteriaSectionCodeRepository.class);
        assembler = new EvaluationCriteriaSummaryResourceAssembler(sectionCodeRepository);
    }

    @Test
    void testToModel_MapsEntityToModel() {
        // Arrange
        EvaluationCriteriaSummaryEntity entity = new EvaluationCriteriaSummaryEntity();
        UUID summaryGuid = UUID.randomUUID();
        entity.setEvaluationCriteriaSummaryGuid(summaryGuid);
        entity.setProjectGuid(UUID.randomUUID());
        entity.setWuiRiskClassComment("Some comment");
        entity.setLocalWuiRiskClassRationale("Some rationale");
        entity.setIsOutsideWuiInd(true);
        entity.setTotalFilterScore(42);

        EvaluationCriteriaSectionSummaryEntity section = new EvaluationCriteriaSectionSummaryEntity();
        section.setEvaluationCriteriaSectionSummaryGuid(UUID.randomUUID());
        section.setEvaluationCriteriaSummaryGuid(summaryGuid);
        section.setFilterSectionScore(10);
        section.setFilterSectionComment("Section Comment");

        EvaluationCriteriaSelectedEntity selected = new EvaluationCriteriaSelectedEntity();
        selected.setEvaluationCriteriaSelectedGuid(UUID.randomUUID());
        selected.setEvaluationCriteriaGuid(UUID.randomUUID());
        selected.setIsEvaluationCriteriaSelectedInd(true);

        section.setEvaluationCriteriaSelected(List.of(selected));
        entity.setEvaluationCriteriaSectionSummaries(List.of(section));

        // Act
        EvaluationCriteriaSummaryModel model = assembler.toModel(entity);

        // Assert
        assertNotNull(model);
        assertEquals(summaryGuid.toString(), model.getEvaluationCriteriaSummaryGuid());
        assertEquals(1, model.getEvaluationCriteriaSectionSummaries().size());
        assertEquals(1, model.getEvaluationCriteriaSectionSummaries().get(0).getEvaluationCriteriaSelected().size());
    }

    @Test
    void testToEntity_MapsModelToEntity() {
        // Arrange
        EvaluationCriteriaSummaryModel model = new EvaluationCriteriaSummaryModel();
        UUID summaryGuid = UUID.randomUUID();
        model.setEvaluationCriteriaSummaryGuid(summaryGuid.toString());
        model.setProjectGuid(UUID.randomUUID().toString());
        model.setWuiRiskClassComment("Test Comment");
        model.setLocalWuiRiskClassRationale("Rationale");
        model.setIsOutsideWuiInd(true);
        model.setTotalFilterScore(100);

        EvaluationCriteriaSectionCodeEntity codeEntity = new EvaluationCriteriaSectionCodeEntity();
        codeEntity.setEvaluationCriteriaSectionCode("RISK");
        when(sectionCodeRepository.findById("RISK")).thenReturn(Optional.of(codeEntity));

        EvaluationCriteriaSectionCodeModel codeModel = new EvaluationCriteriaSectionCodeModel();
        codeModel.setEvaluationCriteriaSectionCode("RISK");

        EvaluationCriteriaSelectedModel selectedModel = new EvaluationCriteriaSelectedModel();
        selectedModel.setEvaluationCriteriaGuid(UUID.randomUUID().toString());
        selectedModel.setIsEvaluationCriteriaSelectedInd(true);

        EvaluationCriteriaSectionSummaryModel sectionModel = new EvaluationCriteriaSectionSummaryModel();
        sectionModel.setEvaluationCriteriaSectionCode(codeModel);
        sectionModel.setFilterSectionScore(5);
        sectionModel.setEvaluationCriteriaSelected(List.of(selectedModel));

        model.setEvaluationCriteriaSectionSummaries(List.of(sectionModel));

        // Act
        EvaluationCriteriaSummaryEntity entity = assembler.toEntity(model);

        // Assert
        assertNotNull(entity);
        assertEquals(1, entity.getEvaluationCriteriaSectionSummaries().size());
        assertEquals(1, entity.getEvaluationCriteriaSectionSummaries().get(0).getEvaluationCriteriaSelected().size());
    }

    @Test
    void testUpdateEntity_UpdatesOnlyAllowedFields() {
        // Arrange
        EvaluationCriteriaSummaryEntity existingEntity = new EvaluationCriteriaSummaryEntity();
        existingEntity.setEvaluationCriteriaSectionSummaries(new ArrayList<>());

        EvaluationCriteriaSectionCodeEntity codeEntity = new EvaluationCriteriaSectionCodeEntity();
        codeEntity.setEvaluationCriteriaSectionCode("RISK");

        EvaluationCriteriaSectionSummaryEntity sectionEntity = new EvaluationCriteriaSectionSummaryEntity();
        sectionEntity.setEvaluationCriteriaSectionCode(codeEntity);
        sectionEntity.setEvaluationCriteriaSelected(new ArrayList<>());
        EvaluationCriteriaSelectedEntity selectedEntity = new EvaluationCriteriaSelectedEntity();
        UUID criteriaGuid = UUID.randomUUID();
        selectedEntity.setEvaluationCriteriaGuid(criteriaGuid);
        selectedEntity.setIsEvaluationCriteriaSelectedInd(false);
        sectionEntity.setEvaluationCriteriaSelected(List.of(selectedEntity));

        existingEntity.getEvaluationCriteriaSectionSummaries().add(sectionEntity);

        EvaluationCriteriaSelectedModel selectedModel = new EvaluationCriteriaSelectedModel();
        selectedModel.setEvaluationCriteriaGuid(criteriaGuid.toString());
        selectedModel.setIsEvaluationCriteriaSelectedInd(true);

        EvaluationCriteriaSectionCodeModel codeModel = new EvaluationCriteriaSectionCodeModel();
        codeModel.setEvaluationCriteriaSectionCode("RISK");

        EvaluationCriteriaSectionSummaryModel sectionModel = new EvaluationCriteriaSectionSummaryModel();
        sectionModel.setEvaluationCriteriaSectionCode(codeModel);
        sectionModel.setEvaluationCriteriaSelected(List.of(selectedModel));

        EvaluationCriteriaSummaryModel model = new EvaluationCriteriaSummaryModel();
        model.setEvaluationCriteriaSectionSummaries(List.of(sectionModel));

        // Act
        assembler.updateEntity(model, existingEntity);

        // Assert
        assertTrue(existingEntity.getEvaluationCriteriaSectionSummaries()
                .get(0).getEvaluationCriteriaSelected()
                .get(0).getIsEvaluationCriteriaSelectedInd());
    }
}
