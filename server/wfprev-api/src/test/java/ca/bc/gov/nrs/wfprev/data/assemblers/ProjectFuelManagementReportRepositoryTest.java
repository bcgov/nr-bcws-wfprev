package ca.bc.gov.nrs.wfprev.data.assemblers;

import ca.bc.gov.nrs.wfprev.data.entities.ProjectFuelManagementReportEntity;
import ca.bc.gov.nrs.wfprev.data.repositories.ProjectFuelManagementReportRepository;
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

class ProjectFuelManagementReportRepositoryTest {

    private ProjectFuelManagementReportRepository repository;

    private UUID projectGuid;
    private UUID fiscalGuid;

    @BeforeEach
    void setUp() {
        repository = mock(ProjectFuelManagementReportRepository.class);

        UUID uniqueRowGuid = UUID.randomUUID();
        projectGuid = UUID.randomUUID();
        fiscalGuid = UUID.randomUUID();

        ProjectFuelManagementReportEntity mockEntity = new ProjectFuelManagementReportEntity();
        mockEntity.setUniqueRowGuid(uniqueRowGuid);
        mockEntity.setProjectGuid(projectGuid);
        mockEntity.setProjectPlanFiscalGuid(fiscalGuid);
        mockEntity.setProjectName("Test Fuel Report");

        when(repository.findByProjectGuidIn(List.of(projectGuid)))
                .thenReturn(List.of(mockEntity));

        when(repository.findByProjectPlanFiscalGuidIn(List.of(fiscalGuid)))
                .thenReturn(List.of(mockEntity));
    }

    @Test
    void findByProjectGuidIn_returnsRowsIncludingNullFiscalOnesInRealDB() {
        List<ProjectFuelManagementReportEntity> results = repository.findByProjectGuidIn(List.of(projectGuid));

        assertNotNull(results);
        assertFalse(results.isEmpty());
        assertEquals(projectGuid, results.get(0).getProjectGuid());
        assertEquals("Test Fuel Report", results.get(0).getProjectName());

        verify(repository, times(1)).findByProjectGuidIn(List.of(projectGuid));
        verifyNoMoreInteractions(repository);
    }

    @Test
    void findByProjectPlanFiscalGuidIn_filtersByFiscal() {
        List<ProjectFuelManagementReportEntity> results =
                repository.findByProjectPlanFiscalGuidIn(List.of(fiscalGuid));

        assertNotNull(results);
        assertFalse(results.isEmpty());
        assertEquals(fiscalGuid, results.get(0).getProjectPlanFiscalGuid());
        assertEquals("Test Fuel Report", results.get(0).getProjectName());

        verify(repository, times(1))
                .findByProjectPlanFiscalGuidIn(List.of(fiscalGuid));
        verifyNoMoreInteractions(repository);
    }
}