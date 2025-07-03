package ca.bc.gov.nrs.wfprev.services;

import ca.bc.gov.nrs.wfprev.data.assemblers.EvaluationCriteriaSelectedResourceAssembler;
import ca.bc.gov.nrs.wfprev.data.assemblers.EvaluationCriteriaSummaryResourceAssembler;
import ca.bc.gov.nrs.wfprev.data.entities.EvaluationCriteriaSummaryEntity;
import ca.bc.gov.nrs.wfprev.data.models.EvaluationCriteriaSummaryModel;
import ca.bc.gov.nrs.wfprev.data.repositories.EvaluationCriteriaSummaryRepository;
import ca.bc.gov.nrs.wfprev.data.repositories.WUIRiskClassCodeRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.hateoas.CollectionModel;

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
    void testGetEvaluationCriteriaSummary_success() {
        UUID guid = UUID.randomUUID();
        EvaluationCriteriaSummaryEntity entity = new EvaluationCriteriaSummaryEntity();
        entity.setEvaluationCriteriaSummaryGuid(guid);
        EvaluationCriteriaSummaryModel model = new EvaluationCriteriaSummaryModel();
        model.setEvaluationCriteriaSummaryGuid(guid.toString());

        when(summaryRepository.findById(guid)).thenReturn(Optional.of(entity));
        when(summaryAssembler.toModel(entity)).thenReturn(model);

        EvaluationCriteriaSummaryModel result = service.getEvaluationCriteriaSummary(guid.toString());

        assertEquals(guid.toString(), result.getEvaluationCriteriaSummaryGuid());
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
}
