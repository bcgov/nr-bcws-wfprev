package ca.bc.gov.nrs.wfprev.services;

import ca.bc.gov.nrs.wfone.common.service.api.ServiceException;
import ca.bc.gov.nrs.wfprev.data.assemblers.*;
import ca.bc.gov.nrs.wfprev.data.entities.*;
import ca.bc.gov.nrs.wfprev.data.models.*;
import ca.bc.gov.nrs.wfprev.data.repositories.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.hateoas.CollectionModel;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class CodesServiceTest {
    private CodesService codesService;
    private ForestAreaCodeRepository forestAreaCodeRepository;
    private ForestAreaCodeResourceAssembler forestAreaCodeResourceAssembler;
    private GeneralScopeCodeRepository generalScopeCodeRepository;
    private GeneralScopeCodeResourceAssembler generalScopeCodeResourceAssembler;
    private ProjectTypeCodeRepository projectTypeCodeRepository;
    private ProjectTypeCodeResourceAssembler projectTypeCodeResourceAssembler;
    private ObjectiveTypeCodeRepository objectiveTypeCodeRepository;
    private ProjectPlanStatusCodeRepository projectPlanStatusCodeRepository;
    private ActivityStatusCodeRepository activityStatusCodeRepository;
    private RiskRatingCodeRepository riskRatingCodeRepository;
    private ContractPhaseCodeRepository contractPhaseCodeRepository;
    private ActivityCategoryCodeRepository activityCategoryCodeRepository;
    private PlanFiscalStatusCodeRepository planFiscalStatusCodeRepository;
    private AncillaryFundingSourceCodeRepository ancillaryFundingSourceCodeRepository;
    private FundingSourceCodeRepository fundingSourceCodeRepository;
    private SourceObjectNameCodeRepository sourceObjectNameCodeRepository;
    private AttachmentContentTypeCodeRepository attachmentContentTypeCodeRepository;
    private SilvicultureBaseCodeRepository silvicultureBaseCodeRepository;
    private SilvicultureMethodCodeRepository silvicultureMethodCodeRepository;
    private SilvicultureTechniqueCodeRepository silvicultureTechniqueCodeRepository;

    private ProgramAreaRepository programAreaRepository;
    private ProgramAreaResourceAssembler programAreaResourceAssembler;
    private ForestOrgUnitCodeRepository forestOrgUnitCodeRepository;
    private ForestRegionUnitCodeResourceAssembler forestRegionUnitCodeResourceAssembler;
    private ForestDistrictUnitCodeResourceAssembler forestDistrictUnitCodeResourceAssembler;
    private BCParksOrgUnitCodeRepository bcParksOrgUnitCodeRepository;
    private BCParksRegionCodeResourceAssembler bcParksRegionCodeResourceAssembler;
    private BCParksSectionCodeResourceAssembler bcParksSectionCodeResourceAssembler;
    private ObjectiveTypeCodeResourceAssembler objectiveTypeCodeResourceAssembler;
    private ProjectPlanStatusCodeResourceAssembler projectPlanStatusCodeResourceAssembler;
    private ActivityStatusCodeResourceAssembler activityStatusCodeResourceAssembler;
    private RiskRatingCodeResourceAssembler riskRatingCodeResourceAssembler;
    private ContractPhaseCodeResourceAssembler contractPhaseCodeResourceAssembler;
    private ActivityCategoryCodeResourceAssembler activityCategoryCodeResourceAssembler;
    private PlanFiscalStatusCodeResourceAssembler planFiscalStatusCodeResourceAssembler;
    private AncillaryFundingSourceCodeResourceAssembler ancillaryFundingSourceCodeResourceAssembler;
    private FundingSourceCodeResourceAssembler fundingSourceCodeResourceAssembler;
    private SourceObjectNameCodeResourceAssembler sourceObjectNameCodeResourceAssembler;
    private AttachmentContentTypeCodeResourceAssembler attachmentContentTypeCodeResourceAssembler;
    private SilvicultureBaseCodeResourceAssembler silvicultureBaseCodeResourceAssembler;
    private SilvicultureMethodCodeResourceAssembler silvicultureMethodCodeResourceAssembler;
    private SilvicultureTechniqueCodeResourceAssembler silvicultureTechniqueCodeResourceAssembler;
    private ProposalTypeCodeRepository proposalTypeCodeRepository;
    private ProposalTypeCodeResourceAssembler proposalTypeCodeResourceAssembler;
    private WUIRiskClassCodeRepository wuiRiskClassCodeRepository;
    private WUIRiskClassCodeResourceAssembler wuiRiskClassCodeResourceAssembler;
    private EvaluationCriteriaCodeRepository evaluationCriteriaCodeRepository;
    private EvaluationCriteriaCodeResourceAssembler evaluationCriteriaCodeResourceAssembler;
    private ProjectStatusCodeRepository projectStatusCodeRepository;
    private ProjectStatusCodeResourceAssembler projectStatusCodeResourceAssembler;
    private WildfireOrgUnitRepository wildfireOrgUnitRepository;
    private WildfireOrgUnitResourceAssembler wildfireOrgUnitResourceAssembler;
    private ReportingPeriodCodeRepository reportingPeriodCodeRepository;
    private ReportingPeriodCodeResourceAssembler reportingPeriodCodeResourceAssembler;

    @BeforeEach
    void setup() {
        forestAreaCodeRepository = mock(ForestAreaCodeRepository.class);
        forestAreaCodeResourceAssembler = mock(ForestAreaCodeResourceAssembler.class);
        generalScopeCodeRepository = mock(GeneralScopeCodeRepository.class);
        generalScopeCodeResourceAssembler = mock(GeneralScopeCodeResourceAssembler.class);
        projectTypeCodeRepository = mock(ProjectTypeCodeRepository.class);
        projectTypeCodeResourceAssembler = mock(ProjectTypeCodeResourceAssembler.class);
        proposalTypeCodeRepository = mock(ProposalTypeCodeRepository.class);
        proposalTypeCodeResourceAssembler = mock(ProposalTypeCodeResourceAssembler.class);
        programAreaRepository = mock(ProgramAreaRepository.class);
        programAreaResourceAssembler = mock(ProgramAreaResourceAssembler.class);
        forestOrgUnitCodeRepository = mock(ForestOrgUnitCodeRepository.class);
        forestRegionUnitCodeResourceAssembler = mock(ForestRegionUnitCodeResourceAssembler.class);
        forestDistrictUnitCodeResourceAssembler = mock(ForestDistrictUnitCodeResourceAssembler.class);
        bcParksOrgUnitCodeRepository = mock(BCParksOrgUnitCodeRepository.class);
        bcParksRegionCodeResourceAssembler = mock(BCParksRegionCodeResourceAssembler.class);
        bcParksSectionCodeResourceAssembler = mock(BCParksSectionCodeResourceAssembler.class);
        objectiveTypeCodeResourceAssembler = mock(ObjectiveTypeCodeResourceAssembler.class);
        objectiveTypeCodeRepository = mock(ObjectiveTypeCodeRepository.class);
        projectPlanStatusCodeResourceAssembler = mock(ProjectPlanStatusCodeResourceAssembler.class);
        projectPlanStatusCodeRepository = mock(ProjectPlanStatusCodeRepository.class);
        activityStatusCodeResourceAssembler = mock(ActivityStatusCodeResourceAssembler.class);
        activityStatusCodeRepository = mock(ActivityStatusCodeRepository.class);
        riskRatingCodeResourceAssembler = mock(RiskRatingCodeResourceAssembler.class);
        riskRatingCodeRepository = mock(RiskRatingCodeRepository.class);
        contractPhaseCodeResourceAssembler = mock(ContractPhaseCodeResourceAssembler.class);
        contractPhaseCodeRepository = mock(ContractPhaseCodeRepository.class);
        activityCategoryCodeResourceAssembler = mock(ActivityCategoryCodeResourceAssembler.class);
        activityCategoryCodeRepository = mock(ActivityCategoryCodeRepository.class);
        planFiscalStatusCodeResourceAssembler = mock(PlanFiscalStatusCodeResourceAssembler.class);
        planFiscalStatusCodeRepository = mock(PlanFiscalStatusCodeRepository.class);
        ancillaryFundingSourceCodeRepository = mock(AncillaryFundingSourceCodeRepository.class);
        ancillaryFundingSourceCodeResourceAssembler = mock(AncillaryFundingSourceCodeResourceAssembler.class);
        fundingSourceCodeRepository = mock(FundingSourceCodeRepository.class);
        fundingSourceCodeResourceAssembler = mock(FundingSourceCodeResourceAssembler.class);
        sourceObjectNameCodeRepository = mock(SourceObjectNameCodeRepository.class);
        sourceObjectNameCodeResourceAssembler = mock(SourceObjectNameCodeResourceAssembler.class);
        attachmentContentTypeCodeRepository = mock(AttachmentContentTypeCodeRepository.class);
        attachmentContentTypeCodeResourceAssembler = mock(AttachmentContentTypeCodeResourceAssembler.class);
        silvicultureBaseCodeRepository = mock(SilvicultureBaseCodeRepository.class);
        silvicultureBaseCodeResourceAssembler = mock(SilvicultureBaseCodeResourceAssembler.class);
        silvicultureMethodCodeRepository = mock(SilvicultureMethodCodeRepository.class);
        silvicultureMethodCodeResourceAssembler = mock(SilvicultureMethodCodeResourceAssembler.class);
        silvicultureTechniqueCodeRepository = mock(SilvicultureTechniqueCodeRepository.class);
        silvicultureTechniqueCodeResourceAssembler = mock(SilvicultureTechniqueCodeResourceAssembler.class);
        wuiRiskClassCodeRepository = mock(WUIRiskClassCodeRepository.class);
        wuiRiskClassCodeResourceAssembler = mock(WUIRiskClassCodeResourceAssembler.class);
        evaluationCriteriaCodeRepository = mock(EvaluationCriteriaCodeRepository.class);
        evaluationCriteriaCodeResourceAssembler = mock(EvaluationCriteriaCodeResourceAssembler.class);
        projectStatusCodeRepository = mock(ProjectStatusCodeRepository.class);
        projectStatusCodeResourceAssembler = mock(ProjectStatusCodeResourceAssembler.class);
        wildfireOrgUnitRepository = mock(WildfireOrgUnitRepository.class);
        wildfireOrgUnitResourceAssembler = mock(WildfireOrgUnitResourceAssembler.class);
        reportingPeriodCodeRepository = mock(ReportingPeriodCodeRepository.class);
        reportingPeriodCodeResourceAssembler = mock(ReportingPeriodCodeResourceAssembler.class);

        codesService = new CodesService(forestAreaCodeRepository, forestAreaCodeResourceAssembler,
                generalScopeCodeRepository, generalScopeCodeResourceAssembler,
                projectTypeCodeRepository, projectTypeCodeResourceAssembler, programAreaRepository, programAreaResourceAssembler,
                forestOrgUnitCodeRepository, forestRegionUnitCodeResourceAssembler, forestDistrictUnitCodeResourceAssembler, bcParksOrgUnitCodeRepository, bcParksRegionCodeResourceAssembler,
                bcParksSectionCodeResourceAssembler, objectiveTypeCodeResourceAssembler, objectiveTypeCodeRepository, projectPlanStatusCodeResourceAssembler, projectPlanStatusCodeRepository,
                activityStatusCodeResourceAssembler, activityStatusCodeRepository, riskRatingCodeResourceAssembler, riskRatingCodeRepository, contractPhaseCodeResourceAssembler, contractPhaseCodeRepository,
                activityCategoryCodeResourceAssembler, activityCategoryCodeRepository, planFiscalStatusCodeResourceAssembler, planFiscalStatusCodeRepository, ancillaryFundingSourceCodeResourceAssembler, ancillaryFundingSourceCodeRepository,
                fundingSourceCodeResourceAssembler, fundingSourceCodeRepository, sourceObjectNameCodeResourceAssembler, sourceObjectNameCodeRepository, attachmentContentTypeCodeResourceAssembler, attachmentContentTypeCodeRepository,
                silvicultureBaseCodeResourceAssembler, silvicultureBaseCodeRepository, silvicultureMethodCodeResourceAssembler, silvicultureMethodCodeRepository, silvicultureTechniqueCodeResourceAssembler, silvicultureTechniqueCodeRepository,
                proposalTypeCodeRepository, proposalTypeCodeResourceAssembler, wuiRiskClassCodeRepository, wuiRiskClassCodeResourceAssembler,evaluationCriteriaCodeRepository,evaluationCriteriaCodeResourceAssembler,
                projectStatusCodeRepository, projectStatusCodeResourceAssembler, wildfireOrgUnitRepository, wildfireOrgUnitResourceAssembler,
                reportingPeriodCodeRepository, reportingPeriodCodeResourceAssembler);
    }

    @Test
    void testGetAllForestAreaCodes_Success() throws ServiceException {
        // Arrange
        List<ForestAreaCodeEntity> entities = new ArrayList<>();
        entities.add(new ForestAreaCodeEntity());
        entities.add(new ForestAreaCodeEntity());

        when(forestAreaCodeRepository.findAll()).thenReturn(entities);
        when(forestAreaCodeResourceAssembler.toCollectionModel(entities))
                .thenReturn(CollectionModel.of(new ArrayList<>()));

        // Act
        CollectionModel<ForestAreaCodeModel> result = codesService.getAllForestAreaCodes();

        // Assert
        assertNotNull(result);
    }

    @Test
    void testGetAllForestAreaCodes_Exception() {
        // Arrange
        when(forestAreaCodeRepository.findAll()).thenThrow(new RuntimeException("Error fetching forest area codes"));

        // Act & Assert
        ServiceException exception = assertThrows(
                ServiceException.class,
                () -> codesService.getAllForestAreaCodes()
        );
        assertEquals("Error fetching forest area codes", exception.getMessage());
    }

    @Test
    void testGetForestAreaCodeById_Success() throws ServiceException {
        // Arrange
        String exampleId = UUID.randomUUID().toString();
        ForestAreaCodeEntity entity = new ForestAreaCodeEntity();
        when(forestAreaCodeRepository.findById(exampleId))
                .thenReturn(Optional.of(entity));
        when(forestAreaCodeResourceAssembler.toModel(entity))
                .thenReturn(new ForestAreaCodeModel());

        // Act
        ForestAreaCodeModel result = codesService.getForestAreaCodeById(exampleId);

        // Assert
        assertNotNull(result);
    }

    @Test
    void testGetForestAreaCodeById_NotFound() throws ServiceException {
        // Arrange
        String nonExistentId = UUID.randomUUID().toString();
        when(forestAreaCodeRepository.findById(nonExistentId))
                .thenReturn(Optional.empty());

        // Act
        ForestAreaCodeModel result = codesService.getForestAreaCodeById(nonExistentId);

        // Assert
        assertNull(result);
    }

    @Test
    void testGetForestAreaCodeById_Exception() {
        // Arrange
        String exampleId = UUID.randomUUID().toString();
        when(forestAreaCodeRepository.findById(exampleId))
                .thenThrow(new RuntimeException("Error fetching forest area code"));

        // Act & Assert
        ServiceException exception = assertThrows(
                ServiceException.class,
                () -> codesService.getForestAreaCodeById(exampleId)
        );
        assertTrue(exception.getMessage().contains("Error fetching forest area code"));
    }

    @Test
    void getAllProjectTypeCodes_Success() throws ServiceException {
        // Arrange
        List<ProjectTypeCodeEntity> entities = new ArrayList<>();
        entities.add(new ProjectTypeCodeEntity());
        entities.add(new ProjectTypeCodeEntity());

        when(projectTypeCodeRepository.findAll()).thenReturn(entities);
        when(projectTypeCodeResourceAssembler.toCollectionModel(entities))
                .thenReturn(CollectionModel.of(new ArrayList<>()));

        // Act
        CollectionModel<ProjectTypeCodeModel> result = codesService.getAllProjectTypeCodes();

        // Assert
        assertNotNull(result);
    }

    @Test
    void testGetAllProjectTypeCodes_Exception() {
        // Arrange
        when(projectTypeCodeRepository.findAll()).thenThrow(new RuntimeException("Error fetching project type codes"));

        // Act & Assert
        ServiceException exception = assertThrows(
                ServiceException.class,
                () -> codesService.getAllProjectTypeCodes()
        );
        assertEquals("Error fetching project type codes", exception.getMessage());
    }

    @Test
    void testGetProjectTypeCodeById_Success() throws ServiceException {
        // Arrange
        String exampleId = UUID.randomUUID().toString();
        ProjectTypeCodeEntity entity = new ProjectTypeCodeEntity();
        when(projectTypeCodeRepository.findById(exampleId))
                .thenReturn(Optional.of(entity));
        when(projectTypeCodeResourceAssembler.toModel(entity))
                .thenReturn(new ProjectTypeCodeModel());

        // Act
        ProjectTypeCodeModel result = codesService.getProjectTypeCodeById(exampleId);

        // Assert
        assertNotNull(result);
    }

    @Test
    void testGetProjectTypeCodeById_NotFound() throws ServiceException {
        // Arrange
        String nonExistentId = UUID.randomUUID().toString();
        when(projectTypeCodeRepository.findById(nonExistentId))
                .thenReturn(Optional.empty());

        // Act
        ProjectTypeCodeModel result = codesService.getProjectTypeCodeById(nonExistentId);

        // Assert
        assertNull(result);
    }

        @Test
        void testGetProposalTypeCodeById_Success() throws ServiceException {
        // Arrange
        String exampleId = UUID.randomUUID().toString();
        ProposalTypeCodeEntity entity = new ProposalTypeCodeEntity();
        when(proposalTypeCodeRepository.findById(exampleId))
                .thenReturn(Optional.of(entity));
        when(proposalTypeCodeResourceAssembler.toModel(entity))
                .thenReturn(new ProposalTypeCodeModel());

        // Act
        ProposalTypeCodeModel result = codesService.getProposalTypeCodeById(exampleId);

        // Assert
        assertNotNull(result);
        }

        @Test
        void testGetProposalTypeCodeById_NotFound() throws ServiceException {
        // Arrange
        String nonExistentId = UUID.randomUUID().toString();
        when(proposalTypeCodeRepository.findById(nonExistentId))
                .thenReturn(Optional.empty());

        // Act
        ProposalTypeCodeModel result = codesService.getProposalTypeCodeById(nonExistentId);

        // Assert
        assertNull(result);
        }

        @Test
        void testGetAllProposalTypeCodes_Success() throws ServiceException {
                // Arrange
                List<ProposalTypeCodeEntity> entities = new ArrayList<>();
                entities.add(new ProposalTypeCodeEntity());
                entities.add(new ProposalTypeCodeEntity());
                when(proposalTypeCodeRepository.findAll()).thenReturn(entities);
                when(proposalTypeCodeResourceAssembler.toCollectionModel(entities))
                        .thenReturn(CollectionModel.of(new ArrayList<>()));

                // Act
                CollectionModel<ProposalTypeCodeModel> result = codesService.getAllProposalTypeCodes();

                // Assert
                assertNotNull(result);
        }


        @Test
        void testGetAllProposalTypeCodes_Exception() {
                // Arrange
                when(proposalTypeCodeRepository.findAll()).thenThrow(new RuntimeException("Error fetching proposal type codes"));

                // Act & Assert
                ServiceException exception = assertThrows(
                        ServiceException.class,
                        () -> codesService.getAllProposalTypeCodes()
                );
                assertEquals("Error fetching proposal type codes", exception.getMessage());
        }


        @Test
        void testGetProposalTypeCodeById_Exception() {
                // Arrange
                String exampleId = UUID.randomUUID().toString();
                when(proposalTypeCodeRepository.findById(exampleId))
                        .thenThrow(new RuntimeException("Error fetching proposal type code"));

                // Act & Assert
                ServiceException exception = assertThrows(
                        ServiceException.class,
                        () -> codesService.getProposalTypeCodeById(exampleId)
                );
                assertTrue(exception.getMessage().contains("Error fetching proposal type code"));
        }

    @Test
    void testGetAllGeneralScopeCodes_Success() throws ServiceException {
        // Arrange
        List<GeneralScopeCodeEntity> entities = new ArrayList<>();
        entities.add(new GeneralScopeCodeEntity());
        entities.add(new GeneralScopeCodeEntity());

        when(generalScopeCodeRepository.findAll()).thenReturn(entities);
        when(generalScopeCodeResourceAssembler.toCollectionModel(entities))
                .thenReturn(CollectionModel.of(new ArrayList<>()));

        // Act
        CollectionModel<GeneralScopeCodeModel> result = codesService.getAllGeneralScopeCodes();

        // Assert
        assertNotNull(result);
    }

    @Test
    void testGetAllGeneralScopeCodes_Exception() {
        // Arrange
        when(generalScopeCodeRepository.findAll()).thenThrow(new RuntimeException("Error fetching general scope codes"));

        // Act & Assert
        ServiceException exception = assertThrows(
                ServiceException.class,
                () -> codesService.getAllGeneralScopeCodes()
        );
        assertEquals("Error fetching general scope codes", exception.getMessage());
    }

    @Test
    void testGetGeneralScopeCodeById_Success() throws ServiceException {
        // Arrange
        String exampleId = UUID.randomUUID().toString();
        GeneralScopeCodeEntity entity = new GeneralScopeCodeEntity();
        when(generalScopeCodeRepository.findById(exampleId))
                .thenReturn(Optional.of(entity));
        when(generalScopeCodeResourceAssembler.toModel(entity))
                .thenReturn(new GeneralScopeCodeModel());

        // Act
        GeneralScopeCodeModel result = codesService.getGeneralScopeCodeById(exampleId);

        // Assert
        assertNotNull(result);
    }

    @Test
    void testGetGeneralScopeCodeById_NotFound() throws ServiceException {
        // Arrange
        String nonExistentId = UUID.randomUUID().toString();
        when(generalScopeCodeRepository.findById(nonExistentId))
                .thenReturn(Optional.empty());

        // Act
        GeneralScopeCodeModel result = codesService.getGeneralScopeCodeById(nonExistentId);

        // Assert
        assertNull(result);
    }


    @Test
    void testGeneralScopeCodeById_Exception() {
        // Arrange
        String exampleId = UUID.randomUUID().toString();
        when(generalScopeCodeRepository.findById(exampleId))
                .thenThrow(new RuntimeException("Error fetching general scope code"));

        // Act & Assert
        ServiceException exception = assertThrows(
                ServiceException.class,
                () -> codesService.getGeneralScopeCodeById(exampleId)
        );
        assertTrue(exception.getMessage().contains("Error fetching general scope code"));
    }

    @Test
    void testProjectTypeAreaCodeById_Exception() {
        // Arrange
        String exampleId = UUID.randomUUID().toString();
        when(projectTypeCodeRepository.findById(exampleId))
                .thenThrow(new RuntimeException("Error fetching project type code"));

        // Act & Assert
        ServiceException exception = assertThrows(
                ServiceException.class,
                () -> codesService.getProjectTypeCodeById(exampleId)
        );
        assertTrue(exception.getMessage().contains("Error fetching project type code"));
    }

    @Test
    void testGetAllProgramAreaCodes_Success() {
        // Arrange
        List<ProgramAreaEntity> entities = List.of(new ProgramAreaEntity(), new ProgramAreaEntity());
        CollectionModel<ProgramAreaModel> expectedModel = CollectionModel.empty();

        when(programAreaRepository.findAll()).thenReturn(entities);
        when(programAreaResourceAssembler.toCollectionModel(entities)).thenReturn(expectedModel);

        // Act
        CollectionModel<ProgramAreaModel> result = codesService.getAllProgramAreaCodes();

        // Assert
        assertEquals(expectedModel, result);
        verify(programAreaRepository, times(1)).findAll();
        verify(programAreaResourceAssembler, times(1)).toCollectionModel(entities);
        verifyNoMoreInteractions(programAreaRepository, programAreaResourceAssembler);
    }

    @Test
    void testGetAllForestRegionCodes_Success() {
        // Given
        ForestOrgUnitCodeEntity entity1 = new ForestOrgUnitCodeEntity();
        entity1.setEffectiveDate(new Date());
        entity1.setExpiryDate(new Date());
        entity1.setForestOrgUnitTypeCode("REGION");
        entity1.setOrgUnitName("orgUnitName");
        entity1.setOrgUnitIdentifier(1);
        entity1.setParentOrgUnitIdentifier(1);
        entity1.setIntegerAlias(1);
        entity1.setCharacterAlias("characterAlias");

        ForestOrgUnitCodeEntity entity2 = new ForestOrgUnitCodeEntity();
        entity2.setEffectiveDate(new Date());
        entity2.setExpiryDate(new Date());
        entity2.setForestOrgUnitTypeCode("REGION");
        entity2.setOrgUnitName("orgUnitName2");
        entity2.setOrgUnitIdentifier(2);
        entity2.setParentOrgUnitIdentifier(2);
        entity2.setIntegerAlias(2);
        entity2.setCharacterAlias("characterAlias2");


        List<ForestOrgUnitCodeEntity> entities = List.of(entity1, entity2);

        ForestRegionUnitCodeModel expectedModel = new ForestRegionUnitCodeModel();
        expectedModel.setOrgUnitId(1);
        expectedModel.setEffectiveDate(new Date());
        expectedModel.setExpiryDate(new Date());
        expectedModel.setForestOrgUnitTypeCode("REGION");
        expectedModel.setOrgUnitName("orgUnitName");
        expectedModel.setParentOrgUnitId(1);
        expectedModel.setIntegerAlias(1);
        expectedModel.setCharacterAlias("characterAlias");

        CollectionModel<ForestRegionUnitCodeModel> expectedModelCollection = CollectionModel.of(List.of(expectedModel));

        when(forestOrgUnitCodeRepository.findByForestOrgUnitTypeCode("REGION")).thenReturn(entities);
        when(forestRegionUnitCodeResourceAssembler.toCollectionModel(entities)).thenReturn(expectedModelCollection);

        when(forestOrgUnitCodeRepository.findAll()).thenReturn(entities);
        when(forestRegionUnitCodeResourceAssembler.toCollectionModel(entities)).thenReturn(expectedModelCollection);

        // When
        CollectionModel<ForestRegionUnitCodeModel> result = codesService.getAllForestRegionCodes();

        // Then
        assertEquals(expectedModelCollection, result);
        verify(forestOrgUnitCodeRepository, times(1)).findByForestOrgUnitTypeCode("REGION");
        verify(forestRegionUnitCodeResourceAssembler, times(1)).toCollectionModel(entities);
        verifyNoMoreInteractions(forestOrgUnitCodeRepository, forestRegionUnitCodeResourceAssembler);
    }

    @Test
    void testGetAllProgramAreaCodes_Exception() {
        // Arrange
        when(programAreaRepository.findAll()).thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        ServiceException exception = assertThrows(ServiceException.class, codesService::getAllProgramAreaCodes);
        assertEquals("Database error", exception.getLocalizedMessage());
        verify(programAreaRepository, times(1)).findAll();
        verifyNoInteractions(programAreaResourceAssembler);
    }

    @Test
    void testGetProgramAreaCodeById_Success() {
        // Arrange
        String id = UUID.randomUUID().toString();
        UUID guid = UUID.fromString(id);
        ProgramAreaEntity entity = new ProgramAreaEntity();
        ProgramAreaModel expectedModel = new ProgramAreaModel();
        expectedModel.setProgramAreaGuid(id);
        expectedModel.setProgramAreaGuid("name");

        when(programAreaRepository.findById(guid)).thenReturn(Optional.of(entity));
        when(programAreaResourceAssembler.toModel(entity)).thenReturn(expectedModel);

        // Act
        ProgramAreaModel result = codesService.getProgramAreaCodeById(id);

        // Assert
        assertEquals(expectedModel, result);
        verify(programAreaRepository, times(1)).findById(guid);
        verify(programAreaResourceAssembler, times(1)).toModel(entity);
        verifyNoMoreInteractions(programAreaRepository, programAreaResourceAssembler);
    }

    @Test
    void testGetProgramAreaCodeById_NotFound() {
        // Arrange
        String id = UUID.randomUUID().toString();
        UUID guid = UUID.fromString(id);

        when(programAreaRepository.findById(guid)).thenReturn(Optional.empty());

        // Act
        ProgramAreaModel result = codesService.getProgramAreaCodeById(id);

        // Assert
        assertNull(result);
        verify(programAreaRepository, times(1)).findById(guid);
        verifyNoInteractions(programAreaResourceAssembler);
    }

    @Test
    void testGetProgramAreaCodeById_InvalidUUID() {
        // Arrange
        String invalidId = "not-a-uuid";

        // Act & Assert
        ServiceException exception = assertThrows(ServiceException.class, () -> codesService.getProgramAreaCodeById(invalidId));
        assertTrue(exception.getLocalizedMessage().contains("Invalid UUID string"));
        verifyNoInteractions(programAreaRepository, programAreaResourceAssembler);
    }

    @Test
    void testGetProgramAreaCodeById_Exception() {
        // Arrange
        String id = UUID.randomUUID().toString();
        UUID guid = UUID.fromString(id);

        when(programAreaRepository.findById(guid)).thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        ServiceException exception = assertThrows(ServiceException.class, () -> codesService.getProgramAreaCodeById(id));
        assertEquals("Database error", exception.getLocalizedMessage());
        verify(programAreaRepository, times(1)).findById(guid);
        verifyNoInteractions(programAreaResourceAssembler);
    }

    @Test
    void testGetAllForestDistrictCodes_Success() {
        // Given
        List<ForestOrgUnitCodeEntity> entities = List.of(new ForestOrgUnitCodeEntity(), new ForestOrgUnitCodeEntity());
        CollectionModel<ForestDistrictUnitCodeModel> expectedModel = CollectionModel.empty();

        when(forestOrgUnitCodeRepository.findByForestOrgUnitTypeCode(anyString())).thenReturn(entities);
        when(forestDistrictUnitCodeResourceAssembler.toCollectionModel(entities)).thenReturn(expectedModel);

        // When
        CollectionModel<ForestDistrictUnitCodeModel> result = codesService.getAllForestDistrictCodes();

        // Then
        assertEquals(expectedModel, result);
        verify(forestOrgUnitCodeRepository, times(1)).findByForestOrgUnitTypeCode("DISTRICT");
        verify(forestDistrictUnitCodeResourceAssembler, times(1)).toCollectionModel(entities);
        verifyNoMoreInteractions(forestOrgUnitCodeRepository, forestDistrictUnitCodeResourceAssembler);
    }

    @Test
    void testGetAllForestDistrictCodes_Exception() {
        // Arrange
        when(forestOrgUnitCodeRepository.findByForestOrgUnitTypeCode("DISTRICT")).thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        ServiceException exception = assertThrows(ServiceException.class, codesService::getAllForestDistrictCodes);
        assertEquals("Database error", exception.getLocalizedMessage());
        verify(forestOrgUnitCodeRepository, times(1)).findByForestOrgUnitTypeCode("DISTRICT");
        verifyNoInteractions(forestDistrictUnitCodeResourceAssembler);
    }

    @Test
    void testGetForestRegionCodeById_Exception() {
        // Arrange
        Integer forestRegionId = 1;
        when(forestOrgUnitCodeRepository.findById(forestRegionId)).thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        ServiceException exception = assertThrows(ServiceException.class, () -> codesService.getForestRegionCodeById(forestRegionId));
        assertEquals("Database error", exception.getLocalizedMessage());
        verify(forestOrgUnitCodeRepository, times(1)).findById(forestRegionId);
    }

    @Test
    void testGetForestDistrictCodeById_Exception() {
        // Arrange
        Integer forestDistrictId = 1;
        when(forestOrgUnitCodeRepository.findById(forestDistrictId)).thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        ServiceException exception = assertThrows(ServiceException.class, () -> codesService.getForestDistrictCodeById(forestDistrictId));
        assertEquals("Database error", exception.getLocalizedMessage());
        verify(forestOrgUnitCodeRepository, times(1)).findById(forestDistrictId);
    }

    @Test
    void testGetForestDistrictCodeById_Success() {
        // Given
        Integer forestDistrictId = 1;
        ForestOrgUnitCodeEntity entity = new ForestOrgUnitCodeEntity();
        ForestDistrictUnitCodeModel expectedModel = new ForestDistrictUnitCodeModel();
        expectedModel.setOrgUnitId(forestDistrictId);
        expectedModel.setEffectiveDate(new Date());
        expectedModel.setExpiryDate(new Date());
        expectedModel.setForestOrgUnitTypeCode("DISTRICT");
        expectedModel.setOrgUnitName("orgUnitName");
        expectedModel.setParentOrgUnitId("1");
        expectedModel.setIntegerAlias(1);
        expectedModel.setCharacterAlias("characterAlias");

        when(forestOrgUnitCodeRepository.findById(forestDistrictId)).thenReturn(Optional.of(entity));
        when(forestDistrictUnitCodeResourceAssembler.toModel(entity)).thenReturn(expectedModel);

        // When
        ForestDistrictUnitCodeModel result = codesService.getForestDistrictCodeById(forestDistrictId);

        // Then
        assertEquals(expectedModel, result);
        verify(forestOrgUnitCodeRepository, times(1)).findById(forestDistrictId);
        verify(forestDistrictUnitCodeResourceAssembler, times(1)).toModel(entity);
        verifyNoMoreInteractions(forestOrgUnitCodeRepository, forestDistrictUnitCodeResourceAssembler);

    }

    @Test
    void testGetForestRegionCodeById_Success() {
        // Given
        Integer forestRegionId = 1;
        ForestOrgUnitCodeEntity entity = new ForestOrgUnitCodeEntity();
        ForestRegionUnitCodeModel expectedModel = new ForestRegionUnitCodeModel();
        expectedModel.setOrgUnitId(forestRegionId);
        expectedModel.setEffectiveDate(new Date());
        expectedModel.setExpiryDate(new Date());
        expectedModel.setForestOrgUnitTypeCode("REGION");
        expectedModel.setOrgUnitName("orgUnitName");
        expectedModel.setParentOrgUnitId(1);
        expectedModel.setIntegerAlias(1);
        expectedModel.setCharacterAlias("characterAlias");

        when(forestOrgUnitCodeRepository.findById(forestRegionId)).thenReturn(Optional.of(entity));
        when(forestRegionUnitCodeResourceAssembler.toModel(entity)).thenReturn(expectedModel);

        // When
        ForestRegionUnitCodeModel result = codesService.getForestRegionCodeById(forestRegionId);

        // Then
        assertEquals(expectedModel, result);
        verify(forestOrgUnitCodeRepository, times(1)).findById(forestRegionId);
        verify(forestRegionUnitCodeResourceAssembler, times(1)).toModel(entity);
        verifyNoMoreInteractions(forestOrgUnitCodeRepository, forestRegionUnitCodeResourceAssembler);

    }

    @Test
    void testGetAllBCParksRegionCodes_Success() {
        // Given
        List<BCParksOrgUnitEntity> entities = List.of(new BCParksOrgUnitEntity(), new BCParksOrgUnitEntity());
        BCParksRegionCodeModel expectedModel = new BCParksRegionCodeModel();
        expectedModel.setBcParksOrgUnitTypeCode("1");
        expectedModel.setEffectiveDate(new Date());
        expectedModel.setExpiryDate(new Date());
        expectedModel.setOrgUnitId(1);
        expectedModel.setOrgUnitName("orgUnitName");
        expectedModel.setParentOrgUnitId("1");
        expectedModel.setIntegerAlias(1);
        expectedModel.setCharacterAlias("characterAlias");

        CollectionModel<BCParksRegionCodeModel> expectedModelCollection = CollectionModel.of(List.of(expectedModel));

        when(bcParksOrgUnitCodeRepository.findByBcParksOrgUnitTypeCode(anyString())).thenReturn(entities);
        when(bcParksRegionCodeResourceAssembler.toCollectionModel(entities)).thenReturn(expectedModelCollection);

        // When
        CollectionModel<BCParksRegionCodeModel> result = codesService.getAllBCParksRegionCodes();

        // Then
        assertEquals(expectedModelCollection, result);
        verify(bcParksOrgUnitCodeRepository, times(1)).findByBcParksOrgUnitTypeCode("REGION");
        verify(bcParksRegionCodeResourceAssembler, times(1)).toCollectionModel(entities);
        verifyNoMoreInteractions(bcParksOrgUnitCodeRepository, bcParksRegionCodeResourceAssembler);
    }

    @Test
    void testGetAllBCParksRegionCodeById_Success() {
        // Given
        Integer bcParksRegionId = 1;
        BCParksOrgUnitEntity entity = new BCParksOrgUnitEntity();
        BCParksRegionCodeModel expectedModel = new BCParksRegionCodeModel();
        expectedModel.setOrgUnitId(bcParksRegionId);
        expectedModel.setEffectiveDate(new Date());
        expectedModel.setExpiryDate(new Date());
        expectedModel.setBcParksOrgUnitTypeCode("REGION");
        expectedModel.setOrgUnitName("orgUnitName");
        expectedModel.setParentOrgUnitId("1");
        expectedModel.setIntegerAlias(1);
        expectedModel.setCharacterAlias("characterAlias");

        when(bcParksOrgUnitCodeRepository.findById(bcParksRegionId)).thenReturn(Optional.of(entity));
        when(bcParksRegionCodeResourceAssembler.toModel(entity)).thenReturn(expectedModel);

        // When
        BCParksRegionCodeModel result = codesService.getBCParksRegionCodeById(bcParksRegionId);

        // Then
        assertEquals(expectedModel, result);
        verify(bcParksOrgUnitCodeRepository, times(1)).findById(bcParksRegionId);
        verify(bcParksRegionCodeResourceAssembler, times(1)).toModel(entity);
        verifyNoMoreInteractions(bcParksOrgUnitCodeRepository, bcParksRegionCodeResourceAssembler);
    }

    @Test
    void testGetBCParksRegionCodeById_NotFound() {
        // Arrange
        Integer bcParksRegionId = 1;
        when(bcParksOrgUnitCodeRepository.findById(bcParksRegionId)).thenReturn(Optional.empty());

        // Act
        BCParksRegionCodeModel result = codesService.getBCParksRegionCodeById(bcParksRegionId);

        // Assert
        assertNull(result);
        verify(bcParksOrgUnitCodeRepository, times(1)).findById(bcParksRegionId);
        verifyNoInteractions(bcParksRegionCodeResourceAssembler);
    }

    @Test
    void testGetBCParksRegionCodeById_Exception() {
        // Arrange
        Integer bcParksRegionId = 1;
        when(bcParksOrgUnitCodeRepository.findById(bcParksRegionId)).thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        ServiceException exception = assertThrows(ServiceException.class, () -> codesService.getBCParksRegionCodeById(bcParksRegionId));
        assertEquals("Database error", exception.getLocalizedMessage());
        verify(bcParksOrgUnitCodeRepository, times(1)).findById(bcParksRegionId);
        verifyNoInteractions(bcParksRegionCodeResourceAssembler);
    }

    @Test
    void testGetAllBCParksSectionCodes_Success() {
        // Given
        List<BCParksOrgUnitEntity> entities = List.of(new BCParksOrgUnitEntity(), new BCParksOrgUnitEntity());
        BCParksSectionCodeModel expectedModel = new BCParksSectionCodeModel();
        expectedModel.setBcParksOrgUnitTypeCode("1");
        expectedModel.setEffectiveDate(new Date());
        expectedModel.setExpiryDate(new Date());
        expectedModel.setOrgUnitId(1);
        expectedModel.setOrgUnitName("orgUnitName");
        expectedModel.setParentOrgUnitId("1");
        expectedModel.setIntegerAlias(1);
        expectedModel.setCharacterAlias("characterAlias");

        CollectionModel<BCParksSectionCodeModel> expectedModelCollection = CollectionModel.of(List.of(expectedModel));

        when(bcParksOrgUnitCodeRepository.findByBcParksOrgUnitTypeCode(anyString())).thenReturn(entities);
        when(bcParksSectionCodeResourceAssembler.toCollectionModel(entities)).thenReturn(expectedModelCollection);

        // When
        CollectionModel<BCParksSectionCodeModel> result = codesService.getAllBCParksSectionCodes();

        // Then
        assertEquals(expectedModelCollection, result);
        verify(bcParksOrgUnitCodeRepository, times(1)).findByBcParksOrgUnitTypeCode("SECTION");
        verify(bcParksSectionCodeResourceAssembler, times(1)).toCollectionModel(entities);
        verifyNoMoreInteractions(bcParksOrgUnitCodeRepository, bcParksRegionCodeResourceAssembler);
    }

    @Test
    void testGetAllBCParksSectionCodeById_Success() {
        // Given
        Integer bcParksSectionId = 1;
        BCParksOrgUnitEntity entity = new BCParksOrgUnitEntity();
        BCParksSectionCodeModel expectedModel = new BCParksSectionCodeModel();
        expectedModel.setOrgUnitId(bcParksSectionId);
        expectedModel.setEffectiveDate(new Date());
        expectedModel.setExpiryDate(new Date());
        expectedModel.setBcParksOrgUnitTypeCode("SECTION");
        expectedModel.setOrgUnitName("orgUnitName");
        expectedModel.setParentOrgUnitId("1");
        expectedModel.setIntegerAlias(1);
        expectedModel.setCharacterAlias("characterAlias");

        when(bcParksOrgUnitCodeRepository.findById(bcParksSectionId)).thenReturn(Optional.of(entity));
        when(bcParksSectionCodeResourceAssembler.toModel(entity)).thenReturn(expectedModel);

        // When
        BCParksSectionCodeModel result = codesService.getBCParksSectionCodeById(bcParksSectionId);

        // Then
        assertEquals(expectedModel, result);
        verify(bcParksOrgUnitCodeRepository, times(1)).findById(bcParksSectionId);
        verify(bcParksSectionCodeResourceAssembler, times(1)).toModel(entity);
        verifyNoMoreInteractions(bcParksOrgUnitCodeRepository, bcParksSectionCodeResourceAssembler);
    }

    @Test
    void testGetBCParksSectionCodeById_NotFound() {
        // Arrange
        Integer bcParksSectionId = 1;
        when(bcParksOrgUnitCodeRepository.findById(bcParksSectionId)).thenReturn(Optional.empty());

        // Act
        BCParksSectionCodeModel result = codesService.getBCParksSectionCodeById(bcParksSectionId);

        // Assert
        assertNull(result);
        verify(bcParksOrgUnitCodeRepository, times(1)).findById(bcParksSectionId);
        verifyNoInteractions(bcParksSectionCodeResourceAssembler);
    }

    @Test
    void testGetBCParksSectionCodeById_Exception() {
        // Arrange
        Integer bcParksSectionId = 1;
        when(bcParksOrgUnitCodeRepository.findById(bcParksSectionId)).thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        ServiceException exception = assertThrows(ServiceException.class, () -> codesService.getBCParksSectionCodeById(bcParksSectionId));
        assertEquals("Database error", exception.getLocalizedMessage());
        verify(bcParksOrgUnitCodeRepository, times(1)).findById(bcParksSectionId);
        verifyNoInteractions(bcParksSectionCodeResourceAssembler);
    }

    @Test
    void testGetAllObjectiveTypeCodes_Success() throws ServiceException {
        // Arrange
        List<ObjectiveTypeCodeEntity> entities = new ArrayList<>();
        entities.add(new ObjectiveTypeCodeEntity());
        entities.add(new ObjectiveTypeCodeEntity());

        when(objectiveTypeCodeRepository.findAll()).thenReturn(entities);
        when(objectiveTypeCodeResourceAssembler.toCollectionModel(entities))
                .thenReturn(CollectionModel.of(new ArrayList<>()));

        // Act
        CollectionModel<ObjectiveTypeCodeModel> result = codesService.getAllObjectiveTypeCodes();

        // Assert
        assertNotNull(result);
    }

    @Test
    void testGetObjectiveTypeCodeById_Success() throws ServiceException {
        // Arrange
        String exampleId = UUID.randomUUID().toString();
        ObjectiveTypeCodeEntity entity = new ObjectiveTypeCodeEntity();
        when(objectiveTypeCodeRepository.findById(exampleId))
                .thenReturn(Optional.of(entity));
        when(objectiveTypeCodeResourceAssembler.toModel(entity))
                .thenReturn(new ObjectiveTypeCodeModel());

        // Act
        ObjectiveTypeCodeModel result = codesService.getObjectiveTypeCodeById(exampleId);

        // Assert
        assertNotNull(result);
    }

    @Test
    void testGetObjectiveTypeCodeById_NotFound() throws ServiceException {
        // Arrange
        String nonExistentId = UUID.randomUUID().toString();
        when(objectiveTypeCodeRepository.findById(nonExistentId))
                .thenReturn(Optional.empty());

        // Act
        ObjectiveTypeCodeModel result = codesService.getObjectiveTypeCodeById(nonExistentId);

        // Assert
        assertNull(result);
    }


    @Test
    void testObjectiveTypeCodeById_Exception() {
        // Arrange
        String exampleId = UUID.randomUUID().toString();
        when(objectiveTypeCodeRepository.findById(exampleId))
                .thenThrow(new RuntimeException("Error fetching objective type code"));

        // Act & Assert
        ServiceException exception = assertThrows(
                ServiceException.class,
                () -> codesService.getObjectiveTypeCodeById(exampleId)
        );
        assertTrue(exception.getMessage().contains("Error fetching objective type code"));
    }

    @Test
    void getAllObjectiveTypeCodes_Success() throws ServiceException {
        // Arrange
        List<ObjectiveTypeCodeEntity> entities = new ArrayList<>();
        entities.add(new ObjectiveTypeCodeEntity());
        entities.add(new ObjectiveTypeCodeEntity());

        when(objectiveTypeCodeRepository.findAll()).thenReturn(entities);
        when(objectiveTypeCodeResourceAssembler.toCollectionModel(entities))
                .thenReturn(CollectionModel.of(new ArrayList<>()));

        // Act
        CollectionModel<ObjectiveTypeCodeModel> result = codesService.getAllObjectiveTypeCodes();

        // Assert
        assertNotNull(result);
    }

    @Test
    void testGetAllObjectiveTypeCodes_Exception() {
        // Arrange
        when(objectiveTypeCodeRepository.findAll()).thenThrow(new RuntimeException("Error fetching objective type codes"));

        // Act & Assert
        ServiceException exception = assertThrows(
                ServiceException.class,
                () -> codesService.getAllObjectiveTypeCodes()
        );
        assertEquals("Error fetching objective type codes", exception.getMessage());
    }

    @Test
    void testGetAllProjectPlanStatusCodes_Success() throws ServiceException {
        // Arrange
        List<ProjectPlanStatusCodeEntity> entities = new ArrayList<>();
        entities.add(new ProjectPlanStatusCodeEntity());
        entities.add(new ProjectPlanStatusCodeEntity());

        when(projectPlanStatusCodeRepository.findAll()).thenReturn(entities);
        when(projectPlanStatusCodeResourceAssembler.toCollectionModel(entities))
                .thenReturn(CollectionModel.of(new ArrayList<>()));

        // Act
        CollectionModel<ProjectPlanStatusCodeModel> result = codesService.getAllProjectPlanStatusCodes();

        // Assert
        assertNotNull(result);
    }

    @Test
    void testGetProjectPlanStatusCodeById_Success() throws ServiceException {
        // Arrange
        String exampleId = UUID.randomUUID().toString();
        ProjectPlanStatusCodeEntity entity = new ProjectPlanStatusCodeEntity();
        when(projectPlanStatusCodeRepository.findById(exampleId))
                .thenReturn(Optional.of(entity));
        when(projectPlanStatusCodeResourceAssembler.toModel(entity))
                .thenReturn(new ProjectPlanStatusCodeModel());

        // Act
        ProjectPlanStatusCodeModel result = codesService.getProjectPlanStatusCodeById(exampleId);

        // Assert
        assertNotNull(result);
    }

    @Test
    void testGetProjectPlanStatusCodeById_NotFound() throws ServiceException {
        // Arrange
        String nonExistentId = UUID.randomUUID().toString();
        when(projectPlanStatusCodeRepository.findById(nonExistentId))
                .thenReturn(Optional.empty());

        // Act
        ProjectPlanStatusCodeModel result = codesService.getProjectPlanStatusCodeById(nonExistentId);

        // Assert
        assertNull(result);
    }


    @Test
    void testProjectPlanStatusCodeById_Exception() {
        // Arrange
        String exampleId = UUID.randomUUID().toString();
        when(projectPlanStatusCodeRepository.findById(exampleId))
                .thenThrow(new RuntimeException("Error fetching project plan status code"));

        // Act & Assert
        ServiceException exception = assertThrows(
                ServiceException.class,
                () -> codesService.getProjectPlanStatusCodeById(exampleId)
        );
        assertTrue(exception.getMessage().contains("Error fetching project plan status code"));
    }

    @Test
    void getAllProjectPlanStatusCodes_Success() throws ServiceException {
        // Arrange
        List<ProjectPlanStatusCodeEntity> entities = new ArrayList<>();
        entities.add(new ProjectPlanStatusCodeEntity());
        entities.add(new ProjectPlanStatusCodeEntity());

        when(projectPlanStatusCodeRepository.findAll()).thenReturn(entities);
        when(projectPlanStatusCodeResourceAssembler.toCollectionModel(entities))
                .thenReturn(CollectionModel.of(new ArrayList<>()));

        // Act
        CollectionModel<ProjectPlanStatusCodeModel> result = codesService.getAllProjectPlanStatusCodes();

        // Assert
        assertNotNull(result);
    }

    @Test
    void testGetAllProjectPlanStatusCodes_Exception() {
        // Arrange
        when(projectPlanStatusCodeRepository.findAll()).thenThrow(new RuntimeException("Error fetching project plan status codes"));

        // Act & Assert
        ServiceException exception = assertThrows(
                ServiceException.class,
                () -> codesService.getAllProjectPlanStatusCodes()
        );
        assertEquals("Error fetching project plan status codes", exception.getMessage());
    }

    @Test
    void testGetAllActivityStatusCodes_Success() throws ServiceException {
        // Arrange
        List<ActivityStatusCodeEntity> entities = new ArrayList<>();
        entities.add(new ActivityStatusCodeEntity());
        entities.add(new ActivityStatusCodeEntity());

        when(activityStatusCodeRepository.findAll()).thenReturn(entities);
        when(activityStatusCodeResourceAssembler.toCollectionModel(entities))
                .thenReturn(CollectionModel.of(new ArrayList<>()));

        // Act
        CollectionModel<ActivityStatusCodeModel> result = codesService.getAllActivityStatusCodes();

        // Assert
        assertNotNull(result);
    }

    @Test
    void testGetAllActivityStatusCodes_Exception() {
        // Arrange
        when(activityStatusCodeRepository.findAll()).thenThrow(new RuntimeException("Error fetching activity status codes"));

        // Act & Assert
        ServiceException exception = assertThrows(
                ServiceException.class,
                () -> codesService.getAllActivityStatusCodes()
        );
        assertEquals("Error fetching activity status codes", exception.getMessage());
    }

    @Test
    void testGetActivityStatusCodeById_Success() throws ServiceException {
        // Arrange
        String exampleId = UUID.randomUUID().toString();
        ActivityStatusCodeEntity entity = new ActivityStatusCodeEntity();
        when(activityStatusCodeRepository.findById(exampleId))
                .thenReturn(Optional.of(entity));
        when(activityStatusCodeResourceAssembler.toModel(entity))
                .thenReturn(new ActivityStatusCodeModel());

        // Act
        ActivityStatusCodeModel result = codesService.getActivityStatusCodeById(exampleId);

        // Assert
        assertNotNull(result);
    }

    @Test
    void testGetActivityStatusCodeById_NotFound() throws ServiceException {
        // Arrange
        String nonExistentId = UUID.randomUUID().toString();
        when(activityStatusCodeRepository.findById(nonExistentId))
                .thenReturn(Optional.empty());

        // Act
        ActivityStatusCodeModel result = codesService.getActivityStatusCodeById(nonExistentId);

        // Assert
        assertNull(result);
    }

    @Test
    void testGetActivityStatusCodeById_Exception() {
        // Arrange
        String exampleId = UUID.randomUUID().toString();
        when(activityStatusCodeRepository.findById(exampleId))
                .thenThrow(new RuntimeException("Error fetching activity status code"));

        // Act & Assert
        ServiceException exception = assertThrows(
                ServiceException.class,
                () -> codesService.getActivityStatusCodeById(exampleId)
        );
        assertTrue(exception.getMessage().contains("Error fetching activity status code"));
    }

    @Test
    void testGetAllRiskRatingCodes_Success() throws ServiceException {
        // Arrange
        List<RiskRatingCodeEntity> entities = new ArrayList<>();
        entities.add(new RiskRatingCodeEntity());
        entities.add(new RiskRatingCodeEntity());

        when(riskRatingCodeRepository.findAll()).thenReturn(entities);
        when(riskRatingCodeResourceAssembler.toCollectionModel(entities))
                .thenReturn(CollectionModel.of(new ArrayList<>()));

        // Act
        CollectionModel<RiskRatingCodeModel> result = codesService.getAllRiskRatingCodes();

        // Assert
        assertNotNull(result);
    }

    void testGetAllActivityCategoryCodes_Success() throws ServiceException {
        // Arrange
        List<ActivityCategoryCodeEntity> entities = new ArrayList<>();
        entities.add(new ActivityCategoryCodeEntity());
        entities.add(new ActivityCategoryCodeEntity());

        when(activityCategoryCodeRepository.findAll()).thenReturn(entities);
        when(activityCategoryCodeResourceAssembler.toCollectionModel(entities))
                .thenReturn(CollectionModel.of(new ArrayList<>()));

        // Act
        CollectionModel<ActivityCategoryCodeModel> result = codesService.getAllActivityCategoryCodes();

        // Assert
        assertNotNull(result);
    }

    @Test
    void testGetAllRiskRatingCodes_Exception() {
        // Arrange
        when(riskRatingCodeRepository.findAll()).thenThrow(new RuntimeException("Error fetching risk rating codes"));

        // Act & Assert
        ServiceException exception = assertThrows(
                ServiceException.class,
                () -> codesService.getAllRiskRatingCodes()
        );
        assertEquals("Error fetching risk rating codes", exception.getMessage());
    }

    @Test
    void testGetRiskRatingCodeById_Success() throws ServiceException {
        // Arrange
        String exampleId = UUID.randomUUID().toString();
        RiskRatingCodeEntity entity = new RiskRatingCodeEntity();
        when(riskRatingCodeRepository.findById(exampleId))
                .thenReturn(Optional.of(entity));
        when(riskRatingCodeResourceAssembler.toModel(entity))
                .thenReturn(new RiskRatingCodeModel());

        // Act
        RiskRatingCodeModel result = codesService.getRiskRatingCodeById(exampleId);

        // Assert
        assertNotNull(result);
    }

    @Test
    void testGetAllActivityCategoryCodes_Exception() {
        // Arrange
        when(activityCategoryCodeRepository.findAll()).thenThrow(new RuntimeException("Error fetching activity category codes"));

        // Act & Assert
        ServiceException exception = assertThrows(
                ServiceException.class,
                () -> codesService.getAllActivityCategoryCodes()
        );
        assertEquals("Error fetching activity category codes", exception.getMessage());
    }

    @Test
    void testGetActivityCategoryCodeById_Success() throws ServiceException {
        // Arrange
        String exampleId = UUID.randomUUID().toString();
        ActivityCategoryCodeEntity entity = new ActivityCategoryCodeEntity();
        when(activityCategoryCodeRepository.findById(exampleId))
                .thenReturn(Optional.of(entity));
        when(activityCategoryCodeResourceAssembler.toModel(entity))
                .thenReturn(new ActivityCategoryCodeModel());

        // Act
        ActivityCategoryCodeModel result = codesService.getActivityCategoryCodeById(exampleId);

        // Assert
        assertNotNull(result);
    }

    @Test
    void testGetRiskRatingCodeById_NotFound() throws ServiceException {
        // Arrange
        String nonExistentId = UUID.randomUUID().toString();
        when(riskRatingCodeRepository.findById(nonExistentId))
                .thenReturn(Optional.empty());

        // Act
        RiskRatingCodeModel result = codesService.getRiskRatingCodeById(nonExistentId);

        // Assert
        assertNull(result);
    }

    @Test
    void testGetActivityCategoryCodeById_NotFound() throws ServiceException {
        // Arrange
        String nonExistentId = UUID.randomUUID().toString();
        when(activityCategoryCodeRepository.findById(nonExistentId))
                .thenReturn(Optional.empty());

        // Act
        ActivityCategoryCodeModel result = codesService.getActivityCategoryCodeById(nonExistentId);

        // Assert
        assertNull(result);
    }

    @Test
    void testGetRiskRatingCodeById_Exception() {
        // Arrange
        String exampleId = UUID.randomUUID().toString();
        when(riskRatingCodeRepository.findById(exampleId))
                .thenThrow(new RuntimeException("Error fetching risk rating code"));

        // Act & Assert
        ServiceException exception = assertThrows(
                ServiceException.class,
                () -> codesService.getRiskRatingCodeById(exampleId)
        );
        assertTrue(exception.getMessage().contains("Error fetching risk rating code"));
    }

    @Test
    void testGetActivityCategoryCodeById_Exception() {
        // Arrange
        String exampleId = UUID.randomUUID().toString();
        when(activityCategoryCodeRepository.findById(exampleId))
                .thenThrow(new RuntimeException("Error fetching activity category code"));

        // Act & Assert
        ServiceException exception = assertThrows(
                ServiceException.class,
                () -> codesService.getActivityCategoryCodeById(exampleId)
        );
        assertTrue(exception.getMessage().contains("Error fetching activity category code"));
    }

    @Test
    void testGetAllContractPhaseCodes_Success() throws ServiceException {
        // Arrange
        List<ContractPhaseCodeEntity> entities = new ArrayList<>();
        entities.add(new ContractPhaseCodeEntity());
        entities.add(new ContractPhaseCodeEntity());

        when(contractPhaseCodeRepository.findAll()).thenReturn(entities);
        when(contractPhaseCodeResourceAssembler.toCollectionModel(entities))
                .thenReturn(CollectionModel.of(new ArrayList<>()));

        // Act
        CollectionModel<ContractPhaseCodeModel> result = codesService.getAllContractPhaseCodes();

        // Assert
        assertNotNull(result);
    }

    @Test
    void testGetAllPlanFiscalStatusCodes_Success() throws ServiceException {
        // Arrange
        List<PlanFiscalStatusCodeEntity> entities = new ArrayList<>();
        entities.add(new PlanFiscalStatusCodeEntity());
        entities.add(new PlanFiscalStatusCodeEntity());

        when(planFiscalStatusCodeRepository.findAll()).thenReturn(entities);
        when(planFiscalStatusCodeResourceAssembler.toCollectionModel(entities))
                .thenReturn(CollectionModel.of(new ArrayList<>()));

        // Act
        CollectionModel<PlanFiscalStatusCodeModel> result = codesService.getAllPlanFiscalStatusCodes();

        // Assert
        assertNotNull(result);
    }

    @Test
    void testGetAllPlanFiscalStatusCodes_Exception() {
        // Arrange
        when(planFiscalStatusCodeRepository.findAll()).thenThrow(new RuntimeException("Error fetching plan fiscal status codes"));

        // Act & Assert
        ServiceException exception = assertThrows(
                ServiceException.class,
                () -> codesService.getAllPlanFiscalStatusCodes()
        );
        assertEquals("Error fetching plan fiscal status codes", exception.getMessage());
    }

    @Test
    void testGetPlanFiscalStatusCodeById_Success() throws ServiceException {
        // Arrange
        String pfsExampleId = UUID.randomUUID().toString();
        PlanFiscalStatusCodeEntity entity = new PlanFiscalStatusCodeEntity();
        when(planFiscalStatusCodeRepository.findById(pfsExampleId))
                .thenReturn(Optional.of(entity));
        when(planFiscalStatusCodeResourceAssembler.toModel(entity))
                .thenReturn(new PlanFiscalStatusCodeModel());

        // Act
        PlanFiscalStatusCodeModel result = codesService.getPlanFiscalStatusCodeById(pfsExampleId);

        // Assert
        assertNotNull(result);
    }

    @Test
    void testGetPlanFiscalStatusCodeById_NotFound() throws ServiceException {
        // Arrange
        String nonExistentId = UUID.randomUUID().toString();
        when(planFiscalStatusCodeRepository.findById(nonExistentId))
                .thenReturn(Optional.empty());

        // Act
        PlanFiscalStatusCodeModel result = codesService.getPlanFiscalStatusCodeById(nonExistentId);

        // Assert
        assertNull(result);
    }

    @Test
    void testGetPlanFiscalStatusCodeById_Exception() {
        // Arrange
        String pfsExampleId = UUID.randomUUID().toString();
        when(planFiscalStatusCodeRepository.findById(pfsExampleId))
                .thenThrow(new RuntimeException("Error fetching plan fiscal status code"));

        // Act & Assert
        ServiceException exception = assertThrows(
                ServiceException.class,
                () -> codesService.getPlanFiscalStatusCodeById(pfsExampleId)
        );
        assertTrue(exception.getMessage().contains("Error fetching plan fiscal status code"));
    }

    @Test
    void testGetAllAncillaryFundingSourceCodes_Success() throws ServiceException {
        // Arrange
        List<AncillaryFundingSourceCodeEntity> entities = new ArrayList<>();
        entities.add(new AncillaryFundingSourceCodeEntity());
        entities.add(new AncillaryFundingSourceCodeEntity());

        when(ancillaryFundingSourceCodeRepository.findAll()).thenReturn(entities);
        when(ancillaryFundingSourceCodeResourceAssembler.toCollectionModel(entities))
                .thenReturn(CollectionModel.of(new ArrayList<>()));

        // Act
        CollectionModel<AncillaryFundingSourceCodeModel> result = codesService.getAllAncillaryFundingSourceCodes();

        // Assert
        assertNotNull(result);
    }

    @Test
    void testGetAllAncillaryFundingSourceCodes_Exception() {
        // Arrange
        when(ancillaryFundingSourceCodeRepository.findAll()).thenThrow(new RuntimeException("Error fetching ancillary funding source codes"));

        // Act & Assert
        ServiceException exception = assertThrows(
                ServiceException.class,
                () -> codesService.getAllAncillaryFundingSourceCodes()
        );
        assertEquals("Error fetching ancillary funding source codes", exception.getMessage());
    }

    @Test
    void testGetAncillaryFundingSourceCodeById_Success() throws ServiceException {
        // Arrange
        String exampleId = UUID.randomUUID().toString();
        AncillaryFundingSourceCodeEntity entity = new AncillaryFundingSourceCodeEntity();
        when(ancillaryFundingSourceCodeRepository.findById(exampleId))
                .thenReturn(Optional.of(entity));
        when(ancillaryFundingSourceCodeResourceAssembler.toModel(entity))
                .thenReturn(new AncillaryFundingSourceCodeModel());

        // Act
        AncillaryFundingSourceCodeModel result = codesService.getAncillaryFundingSourceCodeById(exampleId);

        // Assert
        assertNotNull(result);
    }

    @Test
    void testGetAllContractPhaseCodes_Exception() {
        // Arrange
        when(contractPhaseCodeRepository.findAll()).thenThrow(new RuntimeException("Error fetching contract phase codes"));

        // Act & Assert
        ServiceException exception = assertThrows(
                ServiceException.class,
                () -> codesService.getAllContractPhaseCodes()
        );
        assertEquals("Error fetching contract phase codes", exception.getMessage());
    }

    @Test
    void testGetContractPhaseCodeById_Success() throws ServiceException {
        // Arrange
        String exampleId = UUID.randomUUID().toString();
        ContractPhaseCodeEntity entity = new ContractPhaseCodeEntity();
        when(contractPhaseCodeRepository.findById(exampleId))
                .thenReturn(Optional.of(entity));
        when(contractPhaseCodeResourceAssembler.toModel(entity))
                .thenReturn(new ContractPhaseCodeModel());

        // Act
        ContractPhaseCodeModel result = codesService.getContractPhaseCodeById(exampleId);

        // Assert
        assertNotNull(result);
    }


    @Test
    void testGetContractPhaseCodeById_NotFound() throws ServiceException {
        // Arrange
        String nonExistentId = UUID.randomUUID().toString();
        when(contractPhaseCodeRepository.findById(nonExistentId))
                .thenReturn(Optional.empty());

        // Act
        ContractPhaseCodeModel result = codesService.getContractPhaseCodeById(nonExistentId);

        // Assert
        assertNull(result);
    }

    @Test
    void testGetContractPhaseCodeById_Exception() {
        // Arrange
        String exampleId = UUID.randomUUID().toString();
        when(contractPhaseCodeRepository.findById(exampleId))
                .thenThrow(new RuntimeException("Error fetching contract phase code"));

        // Act & Assert
        ServiceException exception = assertThrows(
                ServiceException.class,
                () -> codesService.getContractPhaseCodeById(exampleId)
        );
        assertTrue(exception.getMessage().contains("Error fetching contract phase code"));
    }
    
    @Test
    void testGetAllFundingSourceCodes_Success() throws ServiceException {
        // Arrange
        List<FundingSourceCodeEntity> entities = new ArrayList<>();
        entities.add(new FundingSourceCodeEntity());
        entities.add(new FundingSourceCodeEntity());
        when(fundingSourceCodeRepository.findAll()).thenReturn(entities);
        when(fundingSourceCodeResourceAssembler.toCollectionModel(entities))
                .thenReturn(CollectionModel.of(new ArrayList<>()));

        // Act
        CollectionModel<FundingSourceCodeModel> result = codesService.getAllFundingSourceCodes();

        // Assert
        assertNotNull(result);
    }

    @Test
    void testGetAllFundingSourceCodes_Exception() {
        // Arrange
        when(fundingSourceCodeRepository.findAll()).thenThrow(new RuntimeException("Error fetching funding source codes"));

        // Act & Assert
        ServiceException exception = assertThrows(
                ServiceException.class,
                () -> codesService.getAllFundingSourceCodes()
        );
        assertEquals("Error fetching funding source codes", exception.getMessage());
    }

    @Test
    void testGetFundingSourceCodeById_Success() throws ServiceException {
        // Arrange
        String exampleId = UUID.randomUUID().toString();
        FundingSourceCodeEntity entity = new FundingSourceCodeEntity();
        when(fundingSourceCodeRepository.findById(exampleId))
                .thenReturn(Optional.of(entity));
        when(fundingSourceCodeResourceAssembler.toModel(entity))
                .thenReturn(new FundingSourceCodeModel());

        // Act
        FundingSourceCodeModel result = codesService.getFundingSourceCodeById(exampleId);

        // Assert
        assertNotNull(result);
    }

    @Test
    void testGetFundingSourceCodeById_NotFound() throws ServiceException {
        // Arrange
        String nonExistentId = UUID.randomUUID().toString();
        when(fundingSourceCodeRepository.findById(nonExistentId))
                .thenReturn(Optional.empty());

        // Act
        FundingSourceCodeModel result = codesService.getFundingSourceCodeById(nonExistentId);

        // Assert
        assertNull(result);
    }

    @Test
    void testGetFundingSourceCodeById_Exception() {
        // Arrange
        String exampleId = UUID.randomUUID().toString();
        when(fundingSourceCodeRepository.findById(exampleId))
                .thenThrow(new RuntimeException("Error fetching funding source code"));

        // Act & Assert
        ServiceException exception = assertThrows(
                ServiceException.class,
                () -> codesService.getFundingSourceCodeById(exampleId)
        );
        assertTrue(exception.getMessage().contains("Error fetching funding source code"));
    }

    @Test
    void testGetAllSourceObjectNameCodes_Success() throws ServiceException {
        // Arrange
        List<SourceObjectNameCodeEntity> entities = new ArrayList<>();
        entities.add(new SourceObjectNameCodeEntity());
        entities.add(new SourceObjectNameCodeEntity());
        when(sourceObjectNameCodeRepository.findAll()).thenReturn(entities);
        when(sourceObjectNameCodeResourceAssembler.toCollectionModel(entities))
                .thenReturn(CollectionModel.of(new ArrayList<>()));

        // Act
        CollectionModel<SourceObjectNameCodeModel> result = codesService.getAllSourceObjectNameCodes();

        // Assert
        assertNotNull(result);
    }

    @Test
    void testGetAllSourceObjectNameCodes_Exception() {
        // Arrange
        when(sourceObjectNameCodeRepository.findAll()).thenThrow(new RuntimeException("Error fetching source object name codes"));

        // Act & Assert
        ServiceException exception = assertThrows(
                ServiceException.class,
                () -> codesService.getAllSourceObjectNameCodes()
        );
        assertEquals("Error fetching source object name codes", exception.getMessage());
    }

    @Test
    void testGetSourceObjectNameCodeById_Success() throws ServiceException {
        // Arrange
        String exampleId = UUID.randomUUID().toString();
        SourceObjectNameCodeEntity entity = new SourceObjectNameCodeEntity();
        when(sourceObjectNameCodeRepository.findById(exampleId))
                .thenReturn(Optional.of(entity));
        when(sourceObjectNameCodeResourceAssembler.toModel(entity))
                .thenReturn(new SourceObjectNameCodeModel());

        // Act
        SourceObjectNameCodeModel result = codesService.getSourceObjectNameCodeById(exampleId);

        // Assert
        assertNotNull(result);
    }

    @Test
    void testGetSourceObjectNameCodeById_NotFound() throws ServiceException {
        // Arrange
        String nonExistentId = UUID.randomUUID().toString();
        when(sourceObjectNameCodeRepository.findById(nonExistentId))
                .thenReturn(Optional.empty());

        // Act
        SourceObjectNameCodeModel result = codesService.getSourceObjectNameCodeById(nonExistentId);

        // Assert
        assertNull(result);
    }

    @Test
    void testGetSourceObjectNameCodeById_Exception() {
        // Arrange
        String exampleId = UUID.randomUUID().toString();
        when(sourceObjectNameCodeRepository.findById(exampleId))
                .thenThrow(new RuntimeException("Error fetching source object name code"));

        // Act & Assert
        ServiceException exception = assertThrows(
                ServiceException.class,
                () -> codesService.getSourceObjectNameCodeById(exampleId)
        );
        assertTrue(exception.getMessage().contains("Error fetching source object name code"));
    }

    @Test
    void testGetAllAttachmentContentTypeCodes_Success() throws ServiceException {
        // Arrange
        List<AttachmentContentTypeCodeEntity> entities = new ArrayList<>();
        entities.add(new AttachmentContentTypeCodeEntity());
        entities.add(new AttachmentContentTypeCodeEntity());
        when(attachmentContentTypeCodeRepository.findAll()).thenReturn(entities);
        when(attachmentContentTypeCodeResourceAssembler.toCollectionModel(entities))
                .thenReturn(CollectionModel.of(new ArrayList<>()));

        // Act
        CollectionModel<AttachmentContentTypeCodeModel> result = codesService.getAllAttachmentContentTypeCodes();

        // Assert
        assertNotNull(result);
    }

    @Test
    void testGetAllAttachmentContentTypeCodes_Exception() {
        // Arrange
        when(attachmentContentTypeCodeRepository.findAll()).thenThrow(new RuntimeException("Error fetching attachment content type name codes"));

        // Act & Assert
        ServiceException exception = assertThrows(
                ServiceException.class,
                () -> codesService.getAllAttachmentContentTypeCodes()
        );
        assertEquals("Error fetching attachment content type name codes", exception.getMessage());
    }

    @Test
    void testGetAttachmentContentTypeCodeById_Success() throws ServiceException {
        // Arrange
        String exampleId = UUID.randomUUID().toString();
        AttachmentContentTypeCodeEntity entity = new AttachmentContentTypeCodeEntity();
        when(attachmentContentTypeCodeRepository.findById(exampleId))
                .thenReturn(Optional.of(entity));
        when(attachmentContentTypeCodeResourceAssembler.toModel(entity))
                .thenReturn(new AttachmentContentTypeCodeModel());

        // Act
        AttachmentContentTypeCodeModel result = codesService.getAttachmentContentTypeCodeById(exampleId);

        // Assert
        assertNotNull(result);
    }

    @Test
    void testGetAttachmentContentTypeCodeById_NotFound() throws ServiceException {
        // Arrange
        String nonExistentId = UUID.randomUUID().toString();
        when(attachmentContentTypeCodeRepository.findById(nonExistentId))
                .thenReturn(Optional.empty());

        // Act
        AttachmentContentTypeCodeModel result = codesService.getAttachmentContentTypeCodeById(nonExistentId);

        // Assert
        assertNull(result);
    }

    @Test
    void testGetAttachmentContentTypeCodeById_Exception() {
        // Arrange
        String exampleId = UUID.randomUUID().toString();
        when(attachmentContentTypeCodeRepository.findById(exampleId))
                .thenThrow(new RuntimeException("Error fetching attachment content type name code"));

        // Act & Assert
        ServiceException exception = assertThrows(
                ServiceException.class,
                () -> codesService.getAttachmentContentTypeCodeById(exampleId)
        );
        assertTrue(exception.getMessage().contains("Error fetching attachment content type name code"));
    }

    @Test
    void testGetAllSilvicultureBaseTypeCodes_Success() throws ServiceException {
        // Arrange
        List<SilvicultureBaseCodeEntity> entities = new ArrayList<>();
        entities.add(new SilvicultureBaseCodeEntity());
        entities.add(new SilvicultureBaseCodeEntity());
        when(silvicultureBaseCodeRepository.findAll()).thenReturn(entities);
        when(silvicultureBaseCodeResourceAssembler.toCollectionModel(entities))
                .thenReturn(CollectionModel.of(new ArrayList<>()));

        // Act
        CollectionModel<SilvicultureBaseCodeModel> result = codesService.getAllSilvicultureBaseCodes();

        // Assert
        assertNotNull(result);
    }

    @Test
    void testGetAllSilvicultureBaseCodes_Exception() {
        // Arrange
        when(silvicultureBaseCodeRepository.findAll()).thenThrow(new RuntimeException("Error fetching silviculture base name codes"));

        // Act & Assert
        ServiceException exception = assertThrows(
                ServiceException.class,
                () -> codesService.getAllSilvicultureBaseCodes()
        );
        assertEquals("Error fetching silviculture base name codes", exception.getMessage());
    }

    @Test
    void testGetSilvicultureBaseCodeById_Success() throws ServiceException {
        // Arrange
        UUID exampleId = UUID.randomUUID();
        SilvicultureBaseCodeEntity entity = new SilvicultureBaseCodeEntity();
        when(silvicultureBaseCodeRepository.findById(exampleId))
                .thenReturn(Optional.of(entity));
        when(silvicultureBaseCodeResourceAssembler.toModel(entity))
                .thenReturn(new SilvicultureBaseCodeModel());

        // Act
        SilvicultureBaseCodeModel result = codesService.getSilvicultureBaseCodeById(String.valueOf(exampleId));

        // Assert
        assertNotNull(result);
    }

    @Test
    void testGetSilvicultureBaseCodeById_NotFound() throws ServiceException {
        // Arrange
        UUID nonExistentId = UUID.randomUUID();
        when(silvicultureBaseCodeRepository.findById(nonExistentId))
                .thenReturn(Optional.empty());

        // Act
        SilvicultureBaseCodeModel result = codesService.getSilvicultureBaseCodeById(String.valueOf(nonExistentId));

        // Assert
        assertNull(result);
    }

    @Test
    void testGetSilvicultureBaseCodeById_Exception() {
        // Arrange
        UUID exampleId = UUID.randomUUID();
        when(silvicultureBaseCodeRepository.findById(exampleId))
                .thenThrow(new RuntimeException("Error fetching silviculture base name code"));

        // Act & Assert
        ServiceException exception = assertThrows(
                ServiceException.class,
                () -> codesService.getSilvicultureBaseCodeById(String.valueOf(exampleId))
        );
        assertTrue(exception.getMessage().contains("Error fetching silviculture base name code"));
    }

    @Test
    void testGetAllSilvicultureMethodTypeCodes_Success() throws ServiceException {
        // Arrange
        List<SilvicultureMethodCodeEntity> entities = new ArrayList<>();
        entities.add(new SilvicultureMethodCodeEntity());
        entities.add(new SilvicultureMethodCodeEntity());
        when(silvicultureMethodCodeRepository.findAll()).thenReturn(entities);
        when(silvicultureMethodCodeResourceAssembler.toCollectionModel(entities))
                .thenReturn(CollectionModel.of(new ArrayList<>()));

        // Act
        CollectionModel<SilvicultureMethodCodeModel> result = codesService.getAllSilvicultureMethodCodes();

        // Assert
        assertNotNull(result);
    }

    @Test
    void testGetAllSilvicultureMethodCodes_Exception() {
        // Arrange
        when(silvicultureMethodCodeRepository.findAll()).thenThrow(new RuntimeException("Error fetching silviculture method name codes"));

        // Act & Assert
        ServiceException exception = assertThrows(
                ServiceException.class,
                () -> codesService.getAllSilvicultureMethodCodes()
        );
        assertEquals("Error fetching silviculture method name codes", exception.getMessage());
    }

    @Test
    void testGetSilvicultureMethodCodeById_Success() throws ServiceException {
        // Arrange
        UUID exampleId = UUID.randomUUID();
        SilvicultureMethodCodeEntity entity = new SilvicultureMethodCodeEntity();
        when(silvicultureMethodCodeRepository.findById(exampleId))
                .thenReturn(Optional.of(entity));
        when(silvicultureMethodCodeResourceAssembler.toModel(entity))
                .thenReturn(new SilvicultureMethodCodeModel());

        // Act
        SilvicultureMethodCodeModel result = codesService.getSilvicultureMethodCodeById(String.valueOf(exampleId));

        // Assert
        assertNotNull(result);
    }

    @Test
    void testGetSilvicultureMethodCodeById_NotFound() throws ServiceException {
        // Arrange
        UUID nonExistentId = UUID.randomUUID();
        when(silvicultureMethodCodeRepository.findById(nonExistentId))
                .thenReturn(Optional.empty());

        // Act
        SilvicultureMethodCodeModel result = codesService.getSilvicultureMethodCodeById(String.valueOf(nonExistentId));

        // Assert
        assertNull(result);
    }

    @Test
    void testGetSilvicultureMethodCodeById_Exception() {
        // Arrange
        UUID exampleId = UUID.randomUUID();
        when(silvicultureMethodCodeRepository.findById(exampleId))
                .thenThrow(new RuntimeException("Error fetching silviculture method name code"));

        // Act & Assert
        ServiceException exception = assertThrows(
                ServiceException.class,
                () -> codesService.getSilvicultureMethodCodeById(String.valueOf(exampleId))
        );
        assertTrue(exception.getMessage().contains("Error fetching silviculture method name code"));
    }

    @Test
    void testGetAllSilvicultureTechniqueTypeCodes_Success() throws ServiceException {
        // Arrange
        List<SilvicultureTechniqueCodeEntity> entities = new ArrayList<>();
        entities.add(new SilvicultureTechniqueCodeEntity());
        entities.add(new SilvicultureTechniqueCodeEntity());
        when(silvicultureTechniqueCodeRepository.findAll()).thenReturn(entities);
        when(silvicultureTechniqueCodeResourceAssembler.toCollectionModel(entities))
                .thenReturn(CollectionModel.of(new ArrayList<>()));

        // Act
        CollectionModel<SilvicultureTechniqueCodeModel> result = codesService.getAllSilvicultureTechniqueCodes();

        // Assert
        assertNotNull(result);
    }

    @Test
    void testGetAllSilvicultureTechniqueCodes_Exception() {
        // Arrange
        when(silvicultureTechniqueCodeRepository.findAll()).thenThrow(new RuntimeException("Error fetching silviculture technique name codes"));

        // Act & Assert
        ServiceException exception = assertThrows(
                ServiceException.class,
                () -> codesService.getAllSilvicultureTechniqueCodes()
        );
        assertEquals("Error fetching silviculture technique name codes", exception.getMessage());
    }

    @Test
    void testGetSilvicultureTechniqueCodeById_Success() throws ServiceException {
        // Arrange
        UUID exampleId = UUID.randomUUID();
        SilvicultureTechniqueCodeEntity entity = new SilvicultureTechniqueCodeEntity();
        when(silvicultureTechniqueCodeRepository.findById(exampleId))
                .thenReturn(Optional.of(entity));
        when(silvicultureTechniqueCodeResourceAssembler.toModel(entity))
                .thenReturn(new SilvicultureTechniqueCodeModel());

        // Act
        SilvicultureTechniqueCodeModel result = codesService.getSilvicultureTechniqueCodeById(String.valueOf(exampleId));

        // Assert
        assertNotNull(result);
    }

    @Test
    void testGetSilvicultureTechniqueCodeById_NotFound() throws ServiceException {
        // Arrange
        UUID nonExistentId = UUID.randomUUID();
        when(silvicultureTechniqueCodeRepository.findById(nonExistentId))
                .thenReturn(Optional.empty());

        // Act
        SilvicultureTechniqueCodeModel result = codesService.getSilvicultureTechniqueCodeById(String.valueOf(nonExistentId));

        // Assert
        assertNull(result);
    }

    @Test
    void testGetSilvicultureTechniqueCodeById_Exception() {
        // Arrange
        UUID exampleId = UUID.randomUUID();
        when(silvicultureTechniqueCodeRepository.findById(exampleId))
                .thenThrow(new RuntimeException("Error fetching silviculture technique name code"));

        // Act & Assert
        ServiceException exception = assertThrows(
                ServiceException.class,
                () -> codesService.getSilvicultureTechniqueCodeById(String.valueOf(exampleId))
        );
        assertTrue(exception.getMessage().contains("Error fetching silviculture technique name code"));
    }

    @Test
    void testGetAllWUIRiskClassCodes_Success() throws ServiceException {
        List<WUIRiskClassRankEntity> entities = new ArrayList<>();
        entities.add(new WUIRiskClassRankEntity());
        entities.add(new WUIRiskClassRankEntity());
        when(wuiRiskClassCodeRepository.findAll()).thenReturn(entities);
        when(wuiRiskClassCodeResourceAssembler.toCollectionModel(entities))
                .thenReturn(CollectionModel.of(new ArrayList<>()));

        CollectionModel<WUIRiskClassRankModel> result = codesService.getAllWuiRiskClassCodes();

        assertNotNull(result);
    }

    @Test
    void testGetAllWUIRiskClassCodes_Exception() {
        when(wuiRiskClassCodeRepository.findAll()).thenThrow(new RuntimeException("Error fetching wild urban interface risk class codes"));

        ServiceException exception = assertThrows(
                ServiceException.class,
                () -> codesService.getAllWuiRiskClassCodes()
        );
        assertEquals("Error fetching wild urban interface risk class codes", exception.getMessage());
    }

    @Test
    void testGetWUIRiskClassCodeById_Success() throws ServiceException {
        UUID exampleId = UUID.randomUUID();
        WUIRiskClassRankEntity entity = new WUIRiskClassRankEntity();
        when(wuiRiskClassCodeRepository.findById(String.valueOf(exampleId)))
                .thenReturn(Optional.of(entity));
        when(wuiRiskClassCodeResourceAssembler.toModel(entity))
                .thenReturn(new WUIRiskClassRankModel());

        WUIRiskClassRankModel result = codesService.getWuiRiskClassCodeById(String.valueOf(exampleId));

        assertNotNull(result);
    }

    @Test
    void testGetWUIRiskClassCodeById_NotFound() throws ServiceException {
        UUID nonExistentId = UUID.randomUUID();
        when(wuiRiskClassCodeRepository.findById(String.valueOf(nonExistentId)))
                .thenReturn(Optional.empty());

        WUIRiskClassRankModel result = codesService.getWuiRiskClassCodeById(String.valueOf(nonExistentId));

        assertNull(result);
    }

        @Test
        void testGetWUIRiskClassCodeById_Exception() {
                UUID exampleId = UUID.randomUUID();
                when(wuiRiskClassCodeRepository.findById(String.valueOf(exampleId)))
                        .thenThrow(new RuntimeException("Error fetching WUI risk class code"));

                ServiceException exception = assertThrows(
                        ServiceException.class,
                        () -> codesService.getWuiRiskClassCodeById(String.valueOf(exampleId))
                );
                assertTrue(exception.getMessage().contains("Error fetching WUI risk class code"));
        }

        @Test
        void testGetAllEvaluationCriteriaCodes_Success() throws ServiceException {
                List<EvaluationCriteriaCodeEntity> entities = new ArrayList<>();
                entities.add(new EvaluationCriteriaCodeEntity());
                entities.add(new EvaluationCriteriaCodeEntity());

                when(evaluationCriteriaCodeRepository.findAll()).thenReturn(entities);
                when(evaluationCriteriaCodeResourceAssembler.toCollectionModel(entities))
                        .thenReturn(CollectionModel.of(new ArrayList<>()));

                CollectionModel<EvaluationCriteriaCodeModel> result = codesService.getAllEvaluationCriteriaCodes();

                assertNotNull(result);
        }

        @Test
        void testGetAllEvaluationCriteriaCodes_Exception() {
                when(evaluationCriteriaCodeRepository.findAll())
                        .thenThrow(new RuntimeException("Error fetching fuel management objective codes"));

                ServiceException exception = assertThrows(
                        ServiceException.class,
                        () -> codesService.getAllEvaluationCriteriaCodes()
                );
                assertEquals("Error fetching fuel management objective codes", exception.getMessage());
        }

        @Test
        void testGetEvaluationCriteriaCodeById_Success() throws ServiceException {
                UUID exampleId = UUID.randomUUID();
                EvaluationCriteriaCodeEntity entity = new EvaluationCriteriaCodeEntity();

                when(evaluationCriteriaCodeRepository.findById(exampleId))
                        .thenReturn(Optional.of(entity));
                when(evaluationCriteriaCodeResourceAssembler.toModel(entity))
                        .thenReturn(new EvaluationCriteriaCodeModel());

                EvaluationCriteriaCodeModel result = codesService.getEvaluationCriteriaCodeById(String.valueOf(exampleId));

                assertNotNull(result);
        }

        @Test
        void testGetEvaluationCriteriaCodeById_NotFound() throws ServiceException {
                UUID nonExistentId = UUID.randomUUID();
                when(evaluationCriteriaCodeRepository.findById(nonExistentId))
                        .thenReturn(Optional.empty());

                EvaluationCriteriaCodeModel result = codesService.getEvaluationCriteriaCodeById(String.valueOf(nonExistentId));

                assertNull(result);
        }

        @Test
        void testGetEvaluationCriteriaCodeById_Exception() {
                UUID exampleId = UUID.randomUUID();
                when(evaluationCriteriaCodeRepository.findById(exampleId))
                        .thenThrow(new RuntimeException("Error fetching fuel management objective code"));

                ServiceException exception = assertThrows(
                        ServiceException.class,
                        () -> codesService.getEvaluationCriteriaCodeById(String.valueOf(exampleId))
                );
                assertTrue(exception.getMessage().contains("Error fetching fuel management objective code"));
        }

    @Test
    void testGetAllProjectStatusCodes_Success() throws ServiceException {
        List<ProjectStatusCodeEntity> entities = new ArrayList<>();
        entities.add(new ProjectStatusCodeEntity());
        entities.add(new ProjectStatusCodeEntity());

        when(projectStatusCodeRepository.findAll()).thenReturn(entities);
        when(projectStatusCodeResourceAssembler.toCollectionModel(entities))
                .thenReturn(CollectionModel.of(new ArrayList<>()));

        CollectionModel<ProjectStatusCodeModel> result = codesService.getAllProjectStatusCodes();

        assertNotNull(result);
    }

    @Test
    void testGetAllProjectStatusCodes_Exception() {
        when(projectStatusCodeRepository.findAll()).thenThrow(new RuntimeException("Error fetching project status codes"));

        ServiceException exception = assertThrows(
                ServiceException.class,
                () -> codesService.getAllProjectStatusCodes()
        );
        assertEquals("Error fetching project status codes", exception.getMessage());
    }

    @Test
    void testGetProjectStatusCodeById_Success() throws ServiceException {
        String exampleId = UUID.randomUUID().toString();
        ProjectStatusCodeEntity entity = new ProjectStatusCodeEntity();
        when(projectStatusCodeRepository.findById(exampleId))
                .thenReturn(Optional.of(entity));
        when(projectStatusCodeResourceAssembler.toModel(entity))
                .thenReturn(new ProjectStatusCodeModel());

        ProjectStatusCodeModel result = codesService.getProjectStatusCodeById(exampleId);

        assertNotNull(result);
    }

    @Test
    void testGetProjectStatusCodeById_NotFound() throws ServiceException {
        String nonExistentId = UUID.randomUUID().toString();
        when(projectStatusCodeRepository.findById(nonExistentId))
                .thenReturn(Optional.empty());

        ProjectStatusCodeModel result = codesService.getProjectStatusCodeById(nonExistentId);

        assertNull(result);
    }

    @Test
    void testGetProjectStatusCodeById_Exception() {
        String exampleId = UUID.randomUUID().toString();
        when(projectStatusCodeRepository.findById(exampleId))
                .thenThrow(new RuntimeException("Error fetching project status code"));

        ServiceException exception = assertThrows(
                ServiceException.class,
                () -> codesService.getProjectStatusCodeById(exampleId)
        );
        assertTrue(exception.getMessage().contains("Error fetching project status code"));
    }

    @Test
    void testGetAllWildfireOrgUnits_Success() throws ServiceException {
        List<WildfireOrgUnitEntity> entities = new ArrayList<>();
        entities.add(new WildfireOrgUnitEntity());
        entities.add(new WildfireOrgUnitEntity());

        when(wildfireOrgUnitRepository.findAll()).thenReturn(entities);
        when(wildfireOrgUnitResourceAssembler.toCollectionModel(entities))
                .thenReturn(CollectionModel.of(new ArrayList<>()));

        CollectionModel<WildfireOrgUnitModel> result = codesService.getAllWildfireOrgUnits();

        assertNotNull(result);
    }

    @Test
    void testGetAllWildfireOrgUnits_Exception() {
        when(wildfireOrgUnitRepository.findAll()).thenThrow(new RuntimeException("Error fetching wildfire org units"));

        ServiceException exception = assertThrows(
                ServiceException.class,
                () -> codesService.getAllWildfireOrgUnits()
        );
        assertEquals("Error fetching wildfire org units", exception.getMessage());
    }

    @Test
    void testGetWildfireOrgUnitById_Success() throws ServiceException {
        String exampleId = UUID.randomUUID().toString();
        WildfireOrgUnitEntity entity = new WildfireOrgUnitEntity();
        when(wildfireOrgUnitRepository.findById(exampleId))
                .thenReturn(Optional.of(entity));
        when(wildfireOrgUnitResourceAssembler.toModel(entity))
                .thenReturn(new WildfireOrgUnitModel());

        WildfireOrgUnitModel result = codesService.getWildfireOrgUnitById(exampleId);

        assertNotNull(result);
    }

    @Test
    void testGetWildfireOrgUnitById_NotFound() throws ServiceException {
        String nonExistentId = UUID.randomUUID().toString();
        when(wildfireOrgUnitRepository.findById(nonExistentId))
                .thenReturn(Optional.empty());

        WildfireOrgUnitModel result = codesService.getWildfireOrgUnitById(nonExistentId);

        assertNull(result);
    }

    @Test
    void testGetWildfireOrgUnitById_Exception() {
        String exampleId = UUID.randomUUID().toString();
        when(wildfireOrgUnitRepository.findById(exampleId))
                .thenThrow(new RuntimeException("Error fetching wildfire org unit"));

        ServiceException exception = assertThrows(
                ServiceException.class,
                        () -> codesService.getWildfireOrgUnitById(exampleId)
        );
        assertTrue(exception.getMessage().contains("Error fetching wildfire org unit"));
    }

    @Test
    void testGetAllReportingPeriodCodes_Success() throws ServiceException {
        List<ReportingPeriodCodeEntity> entities = new ArrayList<>();
        entities.add(new ReportingPeriodCodeEntity());
        when(reportingPeriodCodeRepository.findAll()).thenReturn(entities);
        when(reportingPeriodCodeResourceAssembler.toCollectionModel(entities))
                .thenReturn(CollectionModel.of(new ArrayList<>()));

        CollectionModel<ReportingPeriodCodeModel> result = codesService.getAllReportingPeriodCodes();

        assertNotNull(result);
        verify(reportingPeriodCodeRepository, times(1)).findAll();
        verify(reportingPeriodCodeResourceAssembler, times(1)).toCollectionModel(entities);
    }

    @Test
    void testGetReportingPeriodCodeById_Success() throws ServiceException {
        String exampleId = UUID.randomUUID().toString();
        ReportingPeriodCodeEntity entity = new ReportingPeriodCodeEntity();
        when(reportingPeriodCodeRepository.findById(exampleId))
                .thenReturn(Optional.of(entity));
        when(reportingPeriodCodeResourceAssembler.toModel(entity))
                .thenReturn(new ReportingPeriodCodeModel());

        ReportingPeriodCodeModel result = codesService.getReportingPeriodCodeById(exampleId);

        assertNotNull(result);
        verify(reportingPeriodCodeRepository, times(1)).findById(exampleId);
        verify(reportingPeriodCodeResourceAssembler, times(1)).toModel(entity);
    }

    @Test
    void testGetReportingPeriodCodeById_NotFound() throws ServiceException {
        String nonExistentId = UUID.randomUUID().toString();
        when(reportingPeriodCodeRepository.findById(nonExistentId))
                .thenReturn(Optional.empty());

        ReportingPeriodCodeModel result = codesService.getReportingPeriodCodeById(nonExistentId);
        assertNull(result);
        verify(reportingPeriodCodeRepository, times(1)).findById(nonExistentId);
    }

    @Test
    void testGetReportingPeriodCodeById_Exception() {
        String exampleId = UUID.randomUUID().toString();
        when(reportingPeriodCodeRepository.findById(exampleId))
                .thenThrow(new RuntimeException("Error fetching reporting period code"));

        ServiceException exception = assertThrows(
                ServiceException.class,
                () -> codesService.getReportingPeriodCodeById(exampleId)
        );
        assertTrue(exception.getMessage().contains("Error fetching reporting period code"));
    }
}