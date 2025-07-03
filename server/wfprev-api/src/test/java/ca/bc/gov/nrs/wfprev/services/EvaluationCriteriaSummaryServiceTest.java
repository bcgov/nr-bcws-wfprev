package ca.bc.gov.nrs.wfprev.services;

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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.hateoas.CollectionModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EvaluationCriteriaSummaryServiceTest {

    private EvaluationCriteriaSummaryRepository summaryRepository;
    private EvaluationCriteriaSummaryResourceAssembler summaryAssembler;
    private EvaluationCriteriaSelectedResourceAssembler selectedAssembler;
    private WUIRiskClassCodeRepository wuiRepo;

    private EvaluationCriteriaSummaryService service;

    @BeforeEach
    void setup() {
        summaryRepository = mock(EvaluationCriteriaSummaryRepository.class);
        summaryAssembler = mock(EvaluationCriteriaSummaryResourceAssembler.class);
        selectedAssembler = mock(EvaluationCriteriaSelectedResourceAssembler.class);
        wuiRepo = mock(WUIRiskClassCodeRepository.class);

        service = new EvaluationCriteriaSummaryService(summaryRepository, summaryAssembler, wuiRepo, selectedAssembler);
    }

    @Test
    void testGetAllEvaluationCriteriaSummaries() {
        UUID projectGuid = UUID.randomUUID();
        EvaluationCriteriaSummaryEntity entity = new EvaluationCriteriaSummaryEntity();
        entity.setProjectGuid(projectGuid);
        List<EvaluationCriteriaSummaryEntity> entities = List.of(entity);

        when(summaryRepository.findAllByProjectGuid(projectGuid)).thenReturn(entities);
        when(summaryAssembler.toCollectionModel(entities)).thenReturn(CollectionModel.empty());

        CollectionModel<EvaluationCriteriaSummaryModel> result = service.getAllEvaluationCriteriaSummaries(projectGuid.toString());

        assertNotNull(result);
        verify(summaryRepository).findAllByProjectGuid(projectGuid);
    }

    @Test
    void testGetAllEvaluationCriteriaSummaries_invalidUUID() {
        assertThrows(EntityNotFoundException.class, () ->
                service.getAllEvaluationCriteriaSummaries("invalid-uuid"));
    }

    @Test
    void testCreateEvaluationCriteriaSummary_success() {
        // Setup GUIDs
        UUID summaryGuid = UUID.randomUUID();
        UUID projectGuid = UUID.randomUUID();
        String wuiCode = "WUI1";
        String localWuiCode = "WUI2";
        UUID sectionGuid = UUID.randomUUID();
        UUID selectedGuid = UUID.randomUUID();

        // Setup model
        EvaluationCriteriaSelectedModel selectedModel = new EvaluationCriteriaSelectedModel();
        selectedModel.setEvaluationCriteriaGuid(selectedGuid.toString());
        selectedModel.setIsEvaluationCriteriaSelectedInd(true);

        EvaluationCriteriaSectionSummaryModel sectionModel = new EvaluationCriteriaSectionSummaryModel();
        sectionModel.setEvaluationCriteriaSelected(List.of(selectedModel));
        sectionModel.setEvaluationCriteriaSectionCode(new ca.bc.gov.nrs.wfprev.data.models.EvaluationCriteriaSectionCodeModel());
        sectionModel.getEvaluationCriteriaSectionCode().setEvaluationCriteriaSectionCode("SEC1");

        EvaluationCriteriaSummaryModel summaryModel = new EvaluationCriteriaSummaryModel();
        summaryModel.setProjectGuid(projectGuid.toString());
        summaryModel.setWuiRiskClassCode(new ca.bc.gov.nrs.wfprev.data.models.WUIRiskClassCodeModel());
        summaryModel.getWuiRiskClassCode().setWuiRiskClassCode(wuiCode);
        summaryModel.setLocalWuiRiskClassCode(new ca.bc.gov.nrs.wfprev.data.models.WUIRiskClassCodeModel());
        summaryModel.getLocalWuiRiskClassCode().setWuiRiskClassCode(localWuiCode);
        summaryModel.setEvaluationCriteriaSectionSummaries(List.of(sectionModel));

        // Setup entity
        EvaluationCriteriaSummaryEntity entity = new EvaluationCriteriaSummaryEntity();
        entity.setEvaluationCriteriaSectionSummaries(new ArrayList<>());
        entity.setEvaluationCriteriaSummaryGuid(summaryGuid);
        entity.setProjectGuid(projectGuid);

        EvaluationCriteriaSummaryEntity savedParent = new EvaluationCriteriaSummaryEntity();
        savedParent.setEvaluationCriteriaSummaryGuid(summaryGuid);
        savedParent.setEvaluationCriteriaSectionSummaries(new ArrayList<>());

        EvaluationCriteriaSummaryEntity savedWithChildren = new EvaluationCriteriaSummaryEntity();
        savedWithChildren.setEvaluationCriteriaSummaryGuid(summaryGuid);

        // Setup mocks
        when(summaryAssembler.toEntity(any())).thenReturn(entity);
        when(summaryRepository.saveAndFlush(any())).thenReturn(savedParent);
        when(summaryRepository.save(any())).thenReturn(savedWithChildren);
        when(summaryAssembler.toModel(savedWithChildren)).thenReturn(summaryModel);

        WUIRiskClassCodeEntity wuiEntity = new WUIRiskClassCodeEntity();
        WUIRiskClassCodeEntity localWuiEntity = new WUIRiskClassCodeEntity();
        when(wuiRepo.findById(wuiCode)).thenReturn(Optional.of(wuiEntity));
        when(wuiRepo.findById(localWuiCode)).thenReturn(Optional.of(localWuiEntity));

        when(selectedAssembler.toEntity(any())).thenAnswer(invocation -> {
            EvaluationCriteriaSelectedModel m = invocation.getArgument(0);
            EvaluationCriteriaSelectedEntity e = new EvaluationCriteriaSelectedEntity();
            e.setEvaluationCriteriaGuid(UUID.fromString(m.getEvaluationCriteriaGuid()));
            return e;
        });

        // Call service
        EvaluationCriteriaSummaryModel result = service.createEvaluationCriteriaSummary(summaryModel);

        // Assert and verify
        assertNotNull(result);
        verify(summaryAssembler).toEntity(any());
        verify(wuiRepo, times(2)).findById(any());
        verify(summaryRepository, times(1)).save(any());
        verify(summaryAssembler).toModel(savedWithChildren);
    }

    @Test
    void testGetEvaluationCriteriaSummary_notFound() {
        UUID guid = UUID.randomUUID();
        when(summaryRepository.findById(guid)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> service.getEvaluationCriteriaSummary(guid.toString()));
    }

    @Test
    void testDeleteEvaluationCriteriaSummary_success() {
        UUID guid = UUID.randomUUID();
        EvaluationCriteriaSummaryEntity entity = new EvaluationCriteriaSummaryEntity();
        when(summaryRepository.findById(guid)).thenReturn(Optional.of(entity));

        service.deleteEvaluationCriteriaSummary(guid.toString());

        verify(summaryRepository).deleteById(guid);
    }

    @Test
    void testDeleteEvaluationCriteriaSummary_notFound() {
        UUID guid = UUID.randomUUID();
        when(summaryRepository.findById(guid)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> service.deleteEvaluationCriteriaSummary(guid.toString()));
    }

    @Test
    void testUpdateEvaluationCriteriaSummary_notFound() {
        UUID guid = UUID.randomUUID();
        EvaluationCriteriaSummaryModel model = new EvaluationCriteriaSummaryModel();
        model.setEvaluationCriteriaSummaryGuid(guid.toString());

        when(summaryRepository.findById(guid)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                service.updateEvaluationCriteriaSummary(model));
        assertTrue(ex.getCause() instanceof EntityNotFoundException);
    }

    @Test
    void testUpdateEvaluationCriteriaSummary_success() {
        UUID guid = UUID.randomUUID();
        EvaluationCriteriaSummaryModel model = new EvaluationCriteriaSummaryModel();
        model.setEvaluationCriteriaSummaryGuid(guid.toString());

        EvaluationCriteriaSummaryEntity existing = new EvaluationCriteriaSummaryEntity();
        EvaluationCriteriaSummaryEntity updated = new EvaluationCriteriaSummaryEntity();

        when(summaryRepository.findById(guid)).thenReturn(Optional.of(existing));
        when(summaryAssembler.updateEntity(model, existing)).thenReturn(updated);
        when(summaryRepository.saveAndFlush(updated)).thenReturn(updated);
        when(summaryAssembler.toModel(updated)).thenReturn(model);

        EvaluationCriteriaSummaryModel result = service.updateEvaluationCriteriaSummary(model);

        assertNotNull(result);
        verify(summaryRepository).saveAndFlush(updated);
    }

    @Test
    void testLinkChildSummariesToParent_setsParentAndSelectedInd() {
        EvaluationCriteriaSelectedEntity selectedEntity = new EvaluationCriteriaSelectedEntity();
        UUID selectedGuid = UUID.randomUUID();
        selectedEntity.setEvaluationCriteriaGuid(selectedGuid);

        EvaluationCriteriaSectionSummaryEntity sectionEntity = new EvaluationCriteriaSectionSummaryEntity();
        sectionEntity.setEvaluationCriteriaSelected(List.of(selectedEntity));
        sectionEntity.setEvaluationCriteriaSectionCode(new ca.bc.gov.nrs.wfprev.data.entities.EvaluationCriteriaSectionCodeEntity());
        sectionEntity.getEvaluationCriteriaSectionCode().setEvaluationCriteriaSectionCode("SEC1");

        EvaluationCriteriaSummaryEntity parent = new EvaluationCriteriaSummaryEntity();
        parent.setEvaluationCriteriaSummaryGuid(UUID.randomUUID());
        parent.setEvaluationCriteriaSectionSummaries(List.of(sectionEntity));

        EvaluationCriteriaSelectedModel selectedModel = new EvaluationCriteriaSelectedModel();
        selectedModel.setEvaluationCriteriaGuid(selectedGuid.toString());
        selectedModel.setIsEvaluationCriteriaSelectedInd(true);

        EvaluationCriteriaSectionSummaryModel sectionModel = new EvaluationCriteriaSectionSummaryModel();
        sectionModel.setEvaluationCriteriaSectionCode(new ca.bc.gov.nrs.wfprev.data.models.EvaluationCriteriaSectionCodeModel());
        sectionModel.getEvaluationCriteriaSectionCode().setEvaluationCriteriaSectionCode("SEC1");
        sectionModel.setEvaluationCriteriaSelected(List.of(selectedModel));

        EvaluationCriteriaSummaryModel model = new EvaluationCriteriaSummaryModel();
        model.setEvaluationCriteriaSectionSummaries(List.of(sectionModel));

        // Call method
        service.linkChildSummariesToParent(parent, model);

        // Verify link was made
        assertEquals(parent, sectionEntity.getEvaluationCriteriaSummary());
        assertEquals(parent.getEvaluationCriteriaSummaryGuid(), sectionEntity.getEvaluationCriteriaSummaryGuid());
        assertTrue(sectionEntity.getEvaluationCriteriaSelected().get(0).getIsEvaluationCriteriaSelectedInd());
    }
}
