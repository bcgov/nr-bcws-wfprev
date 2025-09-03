package ca.bc.gov.nrs.wfprev.data.assemblers;

import ca.bc.gov.nrs.wfprev.data.entities.CulturalPrescribedFireReportEntity;
import ca.bc.gov.nrs.wfprev.data.repositories.CulturalPrescribedFireReportRepository;
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

class CulturalPrescribedFireReportRepositoryTest {

    private CulturalPrescribedFireReportRepository repository;

    private UUID projectGuid;
    private UUID fiscalGuid;

    private CulturalPrescribedFireReportEntity mockEntity;

    @BeforeEach
    void setUp() {
        repository = mock(CulturalPrescribedFireReportRepository.class);

        projectGuid = UUID.randomUUID();
        fiscalGuid  = UUID.randomUUID();

        mockEntity = new CulturalPrescribedFireReportEntity();
        mockEntity.setUniqueRowGuid(UUID.randomUUID());
        mockEntity.setProjectGuid(projectGuid);
        mockEntity.setProjectPlanFiscalGuid(fiscalGuid);
        mockEntity.setProjectName("Test Cultural Report");

        when(repository.findByProjectGuid(projectGuid))
                .thenReturn(List.of(mockEntity));

        when(repository.findByProjectGuidAndProjectPlanFiscalGuidIn(eq(projectGuid), any(Collection.class)))
                .thenAnswer(inv -> {
                    Collection<UUID> guids = inv.getArgument(1);
                    return guids != null && guids.contains(fiscalGuid)
                            ? List.of(mockEntity)
                            : Collections.emptyList();
                });

        when(repository.findByProjectGuid(any(UUID.class)))
                .thenReturn(Collections.emptyList());
        when(repository.findByProjectGuid(projectGuid))
                .thenReturn(List.of(mockEntity));
    }

    @Test
    void findByProjectGuid_returnsExpectedRows() {
        List<CulturalPrescribedFireReportEntity> results =
                repository.findByProjectGuid(projectGuid);

        assertNotNull(results, "Result list should not be null");
        assertFalse(results.isEmpty(), "Result list should not be empty");

        CulturalPrescribedFireReportEntity e = results.get(0);
        assertNotNull(e.getUniqueRowGuid(), "unique_row_guid should be present");
        assertEquals(projectGuid, e.getProjectGuid(), "Project GUID should match");
        assertEquals("Test Cultural Report", e.getProjectName(), "Project name should match");

        verify(repository, times(1)).findByProjectGuid(projectGuid);
        verifyNoMoreInteractions(repository);
    }

    @Test
    void findByProjectGuidAndProjectPlanFiscalGuidIn_returnsExpectedRows() {
        List<CulturalPrescribedFireReportEntity> results =
                repository.findByProjectGuidAndProjectPlanFiscalGuidIn(
                        projectGuid, Collections.singleton(fiscalGuid));

        assertNotNull(results, "Result list should not be null");
        assertFalse(results.isEmpty(), "Result list should not be empty");

        CulturalPrescribedFireReportEntity e = results.get(0);
        assertEquals(fiscalGuid, e.getProjectPlanFiscalGuid(), "Fiscal GUID should match");
        assertEquals("Test Cultural Report", e.getProjectName(), "Project name should match");

        verify(repository, times(1))
                .findByProjectGuidAndProjectPlanFiscalGuidIn(eq(projectGuid), any(Collection.class));
        verifyNoMoreInteractions(repository);
    }

    @Test
    void findByProjectGuidAndProjectPlanFiscalGuidIn_emptyCollection_returnsEmpty() {
        List<CulturalPrescribedFireReportEntity> results =
                repository.findByProjectGuidAndProjectPlanFiscalGuidIn(projectGuid, Collections.emptyList());

        assertNotNull(results, "Result list should not be null");
        assertTrue(results.isEmpty(), "Result list should be empty");

        verify(repository, times(1))
                .findByProjectGuidAndProjectPlanFiscalGuidIn(eq(projectGuid), any(Collection.class));
        verifyNoMoreInteractions(repository);
    }
}
