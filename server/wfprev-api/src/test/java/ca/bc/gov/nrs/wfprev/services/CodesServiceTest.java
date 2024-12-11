package ca.bc.gov.nrs.wfprev.services;

import ca.bc.gov.nrs.wfone.common.service.api.ServiceException;
import ca.bc.gov.nrs.wfprev.data.assemblers.ForestAreaCodeResourceAssembler;
import ca.bc.gov.nrs.wfprev.data.assemblers.ForestDistrictUnitCodeResourceAssembler;
import ca.bc.gov.nrs.wfprev.data.assemblers.ForestRegionUnitCodeResourceAssembler;
import ca.bc.gov.nrs.wfprev.data.assemblers.GeneralScopeCodeResourceAssembler;
import ca.bc.gov.nrs.wfprev.data.assemblers.ProgramAreaResourceAssembler;
import ca.bc.gov.nrs.wfprev.data.assemblers.ProjectTypeCodeResourceAssembler;
import ca.bc.gov.nrs.wfprev.data.entities.ForestAreaCodeEntity;
import ca.bc.gov.nrs.wfprev.data.entities.GeneralScopeCodeEntity;
import ca.bc.gov.nrs.wfprev.data.entities.ProgramAreaEntity;
import ca.bc.gov.nrs.wfprev.data.entities.ProjectTypeCodeEntity;
import ca.bc.gov.nrs.wfprev.data.models.ForestAreaCodeModel;
import ca.bc.gov.nrs.wfprev.data.models.ForestDistrictUnitCodeModel;
import ca.bc.gov.nrs.wfprev.data.models.ForestRegionUnitCodeModel;
import ca.bc.gov.nrs.wfprev.data.models.GeneralScopeCodeModel;
import ca.bc.gov.nrs.wfprev.data.models.ProgramAreaModel;
import ca.bc.gov.nrs.wfprev.data.models.ProjectTypeCodeModel;
import ca.bc.gov.nrs.wfprev.data.repositories.ForestAreaCodeRepository;
import ca.bc.gov.nrs.wfprev.data.repositories.ForestOrgUnitCodeRepository;
import ca.bc.gov.nrs.wfprev.data.repositories.GeneralScopeCodeRepository;
import ca.bc.gov.nrs.wfprev.data.repositories.ProgramAreaRepository;
import ca.bc.gov.nrs.wfprev.data.repositories.ProjectTypeCodeRepository;
import ca.bc.gov.nrs.wfprev.data.entities.ForestOrgUnitCodeEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.hateoas.CollectionModel;

import java.util.ArrayList;
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

    private ProgramAreaRepository programAreaRepository;
    private ProgramAreaResourceAssembler programAreaResourceAssembler;
    private ForestOrgUnitCodeRepository forestOrgUnitCodeRepository;
    private ForestRegionUnitCodeResourceAssembler forestRegionUnitCodeResourceAssembler;
    private ForestDistrictUnitCodeResourceAssembler forestDistrictUnitCodeResourceAssembler;

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

        codesService = new CodesService(forestAreaCodeRepository, forestAreaCodeResourceAssembler,
                generalScopeCodeRepository, generalScopeCodeResourceAssembler,
                projectTypeCodeRepository, projectTypeCodeResourceAssembler, programAreaRepository, programAreaResourceAssembler,
                forestOrgUnitCodeRepository, forestRegionUnitCodeResourceAssembler, forestDistrictUnitCodeResourceAssembler);
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
        assertEquals("Error fetching forest area codes",exception.getMessage());
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
    public void getAllProjectTypeCodes_Success() throws ServiceException {
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
        assertEquals("Error fetching general scope codes",exception.getMessage());
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
        List<ForestOrgUnitCodeEntity> entities = List.of(new ForestOrgUnitCodeEntity(), new ForestOrgUnitCodeEntity());
        CollectionModel<ForestRegionUnitCodeModel> expectedModel = CollectionModel.empty();

        when(forestOrgUnitCodeRepository.findAll()).thenReturn(entities);
        when(forestRegionUnitCodeResourceAssembler.toCollectionModel(entities)).thenReturn(expectedModel);

        // When
        CollectionModel<ForestRegionUnitCodeModel> result = codesService.getAllForestRegionCodes();

        // Then
        assertEquals(expectedModel, result);
        verify(forestOrgUnitCodeRepository, times(1)).findAll();
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
}