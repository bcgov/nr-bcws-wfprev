//package ca.bc.gov.nrs.wfprev.data.assemblers;
//
//import ca.bc.gov.nrs.wfprev.data.entities.CulturalPrescribedFireReportEntity;
//import ca.bc.gov.nrs.wfprev.data.repositories.CulturalPrescribedFireReportRepository;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//
//import java.util.List;
//import java.util.UUID;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.*;
//
//class CulturalPrescribedFireReportRepositoryTest {
//
//    private CulturalPrescribedFireReportRepository repository;
//
//    private UUID projectGuid;
//    private UUID fiscalGuid;
//
//    @BeforeEach
//    void setUp() {
//        repository = mock(CulturalPrescribedFireReportRepository.class);
//
//        projectGuid = UUID.randomUUID();
//        fiscalGuid = UUID.randomUUID();
//
//        CulturalPrescribedFireReportEntity mockEntity = new CulturalPrescribedFireReportEntity();
//        mockEntity.setProjectGuid(projectGuid);
//        mockEntity.setProjectPlanFiscalGuid(fiscalGuid);
//        mockEntity.setProjectName("Test Cultural Report");
//
//        when(repository.findByProjectGuidIn(List.of(projectGuid)))
//                .thenReturn(List.of(mockEntity));
//
//        when(repository.findByProjectPlanFiscalGuidIn(List.of(fiscalGuid)))
//                .thenReturn(List.of(mockEntity));
//    }
//
//    @Test
//    void testFindByProjectGuidIn_ReturnsExpectedResult() {
//        List<CulturalPrescribedFireReportEntity> results = repository.findByProjectGuidIn(List.of(projectGuid));
//
//        assertNotNull(results, "Result list should not be null");
//        assertFalse(results.isEmpty(), "Result list should not be empty");
//        assertEquals(projectGuid, results.get(0).getProjectGuid(), "Project GUID should match");
//        assertEquals("Test Cultural Report", results.get(0).getProjectName(), "Project name should match");
//
//        verify(repository, times(1)).findByProjectGuidIn(List.of(projectGuid));
//    }
//
//    @Test
//    void testFindByProjectPlanFiscalGuidIn_ReturnsExpectedResult() {
//        List<CulturalPrescribedFireReportEntity> results = repository.findByProjectPlanFiscalGuidIn(List.of(fiscalGuid));
//
//        assertNotNull(results, "Result list should not be null");
//        assertFalse(results.isEmpty(), "Result list should not be empty");
//        assertEquals(fiscalGuid, results.get(0).getProjectPlanFiscalGuid(), "Fiscal GUID should match");
//        assertEquals("Test Cultural Report", results.get(0).getProjectName(), "Project name should match");
//
//        verify(repository, times(1)).findByProjectPlanFiscalGuidIn(List.of(fiscalGuid));
//    }
//}
