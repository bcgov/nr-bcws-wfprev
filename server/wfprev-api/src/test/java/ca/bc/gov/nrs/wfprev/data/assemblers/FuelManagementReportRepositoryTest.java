package ca.bc.gov.nrs.wfprev.data.assemblers;

import ca.bc.gov.nrs.wfprev.data.entities.FuelManagementReportEntity;
import ca.bc.gov.nrs.wfprev.data.repositories.FuelManagementReportRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class FuelManagementReportRepositoryTest {

    private FuelManagementReportRepository repository;

    private UUID projectGuid;
    private UUID fiscalGuid;

    @BeforeEach
    void setUp() {
        repository = mock(FuelManagementReportRepository.class);

        UUID uniqueRowGuid = UUID.randomUUID();
        projectGuid = UUID.randomUUID();
        fiscalGuid = UUID.randomUUID();

        FuelManagementReportEntity mockEntity = new FuelManagementReportEntity();
        mockEntity.setUniqueRowGuid(uniqueRowGuid);
        mockEntity.setProjectGuid(projectGuid);
        mockEntity.setProjectPlanFiscalGuid(fiscalGuid);
        mockEntity.setProjectName("Test Fuel Report");

        when(repository.findByProjectGuid(projectGuid))
                .thenReturn(List.of(mockEntity));

        when(repository.findByProjectGuidAndProjectPlanFiscalGuidIn(projectGuid, List.of(fiscalGuid)))
                .thenReturn(List.of(mockEntity));
    }

    @Test
    void findByProjectGuid_returnsRowsIncludingNullFiscalOnesInRealDB() {
        List<FuelManagementReportEntity> results = repository.findByProjectGuid(projectGuid);

        assertNotNull(results);
        assertFalse(results.isEmpty());
        assertEquals(projectGuid, results.get(0).getProjectGuid());
        assertEquals("Test Fuel Report", results.get(0).getProjectName());

        verify(repository, times(1)).findByProjectGuid(projectGuid);
        verifyNoMoreInteractions(repository);
    }

    @Test
    void findByProjectGuidAndProjectPlanFiscalGuidIn_filtersByFiscal() {
        List<FuelManagementReportEntity> results =
                repository.findByProjectGuidAndProjectPlanFiscalGuidIn(projectGuid, List.of(fiscalGuid));

        assertNotNull(results);
        assertFalse(results.isEmpty());
        assertEquals(fiscalGuid, results.get(0).getProjectPlanFiscalGuid());
        assertEquals("Test Fuel Report", results.get(0).getProjectName());

        verify(repository, times(1))
                .findByProjectGuidAndProjectPlanFiscalGuidIn(projectGuid, List.of(fiscalGuid));
        verifyNoMoreInteractions(repository);
    }
}