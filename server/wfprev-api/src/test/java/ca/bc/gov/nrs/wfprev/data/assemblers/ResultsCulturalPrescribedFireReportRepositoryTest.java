package ca.bc.gov.nrs.wfprev.data.assemblers;

import ca.bc.gov.nrs.wfprev.data.entities.ResultsCulturalPrescribedFireReportEntity;
import ca.bc.gov.nrs.wfprev.data.repositories.ResultsCulturalPrescribedFireReportRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class ResultsCulturalPrescribedFireReportRepositoryTest {

    private ResultsCulturalPrescribedFireReportRepository repository;

    private UUID projectGuid;
    private UUID fiscalGuid;

    private ResultsCulturalPrescribedFireReportEntity mockEntity;

    @BeforeEach
    void setUp() {
        repository = mock(ResultsCulturalPrescribedFireReportRepository.class);

        projectGuid = UUID.randomUUID();
        fiscalGuid = UUID.randomUUID();

        mockEntity = new ResultsCulturalPrescribedFireReportEntity();
        mockEntity.setUniqueRowGuid(UUID.randomUUID());
        mockEntity.setProjectGuid(projectGuid);
        mockEntity.setProjectPlanFiscalGuid(fiscalGuid);
        mockEntity.setProjectName("Test Results Cultural Report");

        when(repository.findByProjectGuidIn(List.of(projectGuid)))
                .thenReturn(List.of(mockEntity));

        when(repository.findByProjectPlanFiscalGuidIn(any(Collection.class)))
                .thenAnswer(inv -> {
                    Collection<UUID> guids = inv.getArgument(0);
                    return guids != null && guids.contains(fiscalGuid)
                            ? List.of(mockEntity)
                            : Collections.emptyList();
                });
    }

    @Test
    void findByProjectGuidIn_returnsExpectedRows() {
        List<ResultsCulturalPrescribedFireReportEntity> results =
                repository.findByProjectGuidIn(List.of(projectGuid));

        assertNotNull(results, "Result list should not be null");
        assertFalse(results.isEmpty(), "Result list should not be empty");

        ResultsCulturalPrescribedFireReportEntity e = results.get(0);
        assertNotNull(e.getUniqueRowGuid(), "unique_row_guid should be present");
        assertEquals(projectGuid, e.getProjectGuid(), "Project GUID should match");
        assertEquals("Test Results Cultural Report", e.getProjectName(), "Project name should match");

        verify(repository, times(1)).findByProjectGuidIn(List.of(projectGuid));
        verifyNoMoreInteractions(repository);
    }

    @Test
    void findByProjectPlanFiscalGuidIn_returnsExpectedRows() {
        List<ResultsCulturalPrescribedFireReportEntity> results =
                repository.findByProjectPlanFiscalGuidIn(
                        Collections.singleton(fiscalGuid));

        assertNotNull(results, "Result list should not be null");
        assertFalse(results.isEmpty(), "Result list should not be empty");

        ResultsCulturalPrescribedFireReportEntity e = results.get(0);
        assertEquals(fiscalGuid, e.getProjectPlanFiscalGuid(), "Fiscal GUID should match");
        assertEquals("Test Results Cultural Report", e.getProjectName(), "Project name should match");

        verify(repository, times(1))
                .findByProjectPlanFiscalGuidIn(any(Collection.class));
        verifyNoMoreInteractions(repository);
    }

    @Test
    void findByProjectPlanFiscalGuidIn_emptyCollection_returnsEmpty() {
        List<ResultsCulturalPrescribedFireReportEntity> results =
                repository.findByProjectPlanFiscalGuidIn(Collections.emptyList());

        assertNotNull(results, "Result list should not be null");
        assertTrue(results.isEmpty(), "Result list should be empty");

        verify(repository, times(1))
                .findByProjectPlanFiscalGuidIn(any(Collection.class));
        verifyNoMoreInteractions(repository);
    }
}
