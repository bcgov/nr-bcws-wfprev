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

    @BeforeEach
    void setup() {
        forestAreaCodeRepository = mock(ForestAreaCodeRepository.class);
        forestAreaCodeResourceAssembler = mock(ForestAreaCodeResourceAssembler.class);
        generalScopeCodeRepository = mock(GeneralScopeCodeRepository.class);
        generalScopeCodeResourceAssembler = mock(GeneralScopeCodeResourceAssembler.class);
        projectTypeCodeRepository = mock(ProjectTypeCodeRepository.class);
        projectTypeCodeResourceAssembler = mock(ProjectTypeCodeResourceAssembler.class);
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

        codesService = new CodesService(forestAreaCodeRepository, forestAreaCodeResourceAssembler,
                generalScopeCodeRepository, generalScopeCodeResourceAssembler,
                projectTypeCodeRepository, projectTypeCodeResourceAssembler, programAreaRepository, programAreaResourceAssembler,
                forestOrgUnitCodeRepository, forestRegionUnitCodeResourceAssembler, forestDistrictUnitCodeResourceAssembler, bcParksOrgUnitCodeRepository, bcParksRegionCodeResourceAssembler,
                bcParksSectionCodeResourceAssembler, objectiveTypeCodeResourceAssembler, objectiveTypeCodeRepository, projectPlanStatusCodeResourceAssembler, projectPlanStatusCodeRepository,
                activityStatusCodeResourceAssembler, activityStatusCodeRepository);
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

}