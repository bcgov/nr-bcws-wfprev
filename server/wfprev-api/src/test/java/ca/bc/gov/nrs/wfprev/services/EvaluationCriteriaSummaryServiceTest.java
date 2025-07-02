package ca.bc.gov.nrs.wfprev.services;

import ca.bc.gov.nrs.wfprev.data.assemblers.EvaluationCriteriaSelectedResourceAssembler;
import ca.bc.gov.nrs.wfprev.data.assemblers.EvaluationCriteriaSummaryResourceAssembler;
import ca.bc.gov.nrs.wfprev.data.entities.EvaluationCriteriaSummaryEntity;
import ca.bc.gov.nrs.wfprev.data.models.EvaluationCriteriaSummaryModel;
import ca.bc.gov.nrs.wfprev.data.repositories.EvaluationCriteriaSummaryRepository;
import ca.bc.gov.nrs.wfprev.data.repositories.WUIRiskClassCodeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.hateoas.CollectionModel;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EvaluationCriteriaSummaryServiceTest {

    private EvaluationCriteriaSummaryRepository summaryRepository;
    private EvaluationCriteriaSummaryResourceAssembler summaryAssembler;

    private EvaluationCriteriaSummaryService service;

    @BeforeEach
    void setup() {
        summaryRepository = mock(EvaluationCriteriaSummaryRepository.class);
        summaryAssembler = mock(EvaluationCriteriaSummaryResourceAssembler.class);
        EvaluationCriteriaSelectedResourceAssembler selectedAssembler = mock(EvaluationCriteriaSelectedResourceAssembler.class);
        WUIRiskClassCodeRepository wuiRiskCodeRepo = mock(WUIRiskClassCodeRepository.class);

        service = new EvaluationCriteriaSummaryService(summaryRepository, summaryAssembler, wuiRiskCodeRepo, selectedAssembler);
    }

    @Test
    void testGetAllEvaluationCriteriaSummaries() {
        UUID projectGuid = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");

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
    void testGetEvaluationCriteriaSummary() {
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
    void testDeleteEvaluationCriteriaSummary() {
        UUID guid = UUID.randomUUID();
        EvaluationCriteriaSummaryEntity entity = new EvaluationCriteriaSummaryEntity();
        when(summaryRepository.findById(guid)).thenReturn(Optional.of(entity));

        service.deleteEvaluationCriteriaSummary(guid.toString());

        verify(summaryRepository).deleteById(guid);
    }

    @Test
    void testUpdateEvaluationCriteriaSummary() {
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
