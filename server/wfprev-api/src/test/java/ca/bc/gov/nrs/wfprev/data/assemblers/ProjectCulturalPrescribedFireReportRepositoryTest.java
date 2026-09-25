package ca.bc.gov.nrs.wfprev.data.assemblers;

import ca.bc.gov.nrs.wfprev.data.entities.ProjectCulturalPrescribedFireReportEntity;
import ca.bc.gov.nrs.wfprev.data.repositories.ProjectCulturalPrescribedFireReportRepository;
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

class ProjectCulturalPrescribedFireReportRepositoryTest {

    private ProjectCulturalPrescribedFireReportRepository repository;

    private UUID projectGuid;
    private UUID fiscalGuid;

    private ProjectCulturalPrescribedFireReportEntity mockEntity;

    @BeforeEach
    void setUp() {
        repository = mock(ProjectCulturalPrescribedFireReportRepository.class);

        projectGuid = UUID.randomUUID();
        fiscalGuid  = UUID.randomUUID();

        mockEntity = new ProjectCulturalPrescribedFireReportEntity();
        mockEntity.setUniqueRowGuid(UUID.randomUUID());
        mockEntity.setProjectGuid(projectGuid);
        mockEntity.setProjectPlanFiscalGuid(fiscalGuid);
        mockEntity.setProjectName("Test Cultural Report");

        when(repository.findByProjectGuidIn(List.of(projectGuid)))
                .thenReturn(List.of(mockEntity));

        when(repository.findByProjectPlanFiscalGuidIn(any(Collection.class)))
                .thenAnswer(inv -> {
                    Collection<UUID> guids = inv.getArgument(0);
                    return guids != null && guids.contains(fiscalGuid)
                            ? List.of(mockEntity)
                            : Collections.emptyList();
                });

        when(repository.findByProjectGuidIn(any(Collection.class)))
                .thenReturn(Collections.emptyList());
        when(repository.findByProjectGuidIn(List.of(projectGuid)))
                .thenReturn(List.of(mockEntity));
    }

    @Test
    void findByProjectGuidIn_returnsExpectedRows() {
        List<ProjectCulturalPrescribedFireReportEntity> results =
                repository.findByProjectGuidIn(List.of(projectGuid));

        assertNotNull(results, "Result list should not be null");
        assertFalse(results.isEmpty(), "Result list should not be empty");

        ProjectCulturalPrescribedFireReportEntity e = results.get(0);
        assertNotNull(e.getUniqueRowGuid(), "unique_row_guid should be present");
        assertEquals(projectGuid, e.getProjectGuid(), "Project GUID should match");
        assertEquals("Test Cultural Report", e.getProjectName(), "Project name should match");

        verify(repository, times(1)).findByProjectGuidIn(List.of(projectGuid));
        verifyNoMoreInteractions(repository);
    }

    @Test
    void findByProjectPlanFiscalGuidIn_returnsExpectedRows() {
        List<ProjectCulturalPrescribedFireReportEntity> results =
                repository.findByProjectPlanFiscalGuidIn(
                        Collections.singleton(fiscalGuid));

        assertNotNull(results, "Result list should not be null");
        assertFalse(results.isEmpty(), "Result list should not be empty");

        ProjectCulturalPrescribedFireReportEntity e = results.get(0);
        assertEquals(fiscalGuid, e.getProjectPlanFiscalGuid(), "Fiscal GUID should match");
        assertEquals("Test Cultural Report", e.getProjectName(), "Project name should match");

        verify(repository, times(1))
                .findByProjectPlanFiscalGuidIn(any(Collection.class));
        verifyNoMoreInteractions(repository);
    }

    @Test
    void findByProjectPlanFiscalGuidIn_emptyCollection_returnsEmpty() {
        List<ProjectCulturalPrescribedFireReportEntity> results =
                repository.findByProjectPlanFiscalGuidIn(Collections.emptyList());

        assertNotNull(results, "Result list should not be null");
        assertTrue(results.isEmpty(), "Result list should be empty");

        verify(repository, times(1))
                .findByProjectPlanFiscalGuidIn(any(Collection.class));
        verifyNoMoreInteractions(repository);
    }
}
