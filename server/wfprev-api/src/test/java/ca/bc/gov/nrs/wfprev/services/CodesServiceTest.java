package ca.bc.gov.nrs.wfprev.services;

import ca.bc.gov.nrs.wfone.common.service.api.ServiceException;
import ca.bc.gov.nrs.wfprev.data.assemblers.ForestAreaCodeResourceAssembler;
import ca.bc.gov.nrs.wfprev.data.assemblers.GeneralScopeCodeResourceAssembler;
import ca.bc.gov.nrs.wfprev.data.assemblers.ProjectTypeCodeResourceAssembler;
import ca.bc.gov.nrs.wfprev.data.entities.ForestAreaCodeEntity;
import ca.bc.gov.nrs.wfprev.data.entities.GeneralScopeCodeEntity;
import ca.bc.gov.nrs.wfprev.data.entities.ProjectTypeCodeEntity;
import ca.bc.gov.nrs.wfprev.data.models.ForestAreaCodeModel;
import ca.bc.gov.nrs.wfprev.data.models.GeneralScopeCodeModel;
import ca.bc.gov.nrs.wfprev.data.models.ProjectTypeCodeModel;
import ca.bc.gov.nrs.wfprev.data.repositories.ForestAreaCodeRepository;
import ca.bc.gov.nrs.wfprev.data.repositories.GeneralScopeCodeRepository;
import ca.bc.gov.nrs.wfprev.data.repositories.ProjectTypeCodeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.hateoas.CollectionModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CodesServiceTest {
    private CodesService codesService;
    private ForestAreaCodeRepository forestAreaCodeRepository;
    private ForestAreaCodeResourceAssembler forestAreaCodeResourceAssembler;
    private GeneralScopeCodeRepository generalScopeCodeRepository;
    private GeneralScopeCodeResourceAssembler generalScopeCodeResourceAssembler;
    private ProjectTypeCodeRepository projectTypeCodeRepository;
    private ProjectTypeCodeResourceAssembler projectTypeCodeResourceAssembler;

    @BeforeEach
    void setup() {
        forestAreaCodeRepository = mock(ForestAreaCodeRepository.class);
        forestAreaCodeResourceAssembler = mock(ForestAreaCodeResourceAssembler.class);
        generalScopeCodeRepository = mock(GeneralScopeCodeRepository.class);
        generalScopeCodeResourceAssembler = mock(GeneralScopeCodeResourceAssembler.class);
        projectTypeCodeRepository = mock(ProjectTypeCodeRepository.class);
        projectTypeCodeResourceAssembler = mock(ProjectTypeCodeResourceAssembler.class);

        codesService = new CodesService(forestAreaCodeRepository, forestAreaCodeResourceAssembler,
                generalScopeCodeRepository, generalScopeCodeResourceAssembler,
                projectTypeCodeRepository, projectTypeCodeResourceAssembler);
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


}