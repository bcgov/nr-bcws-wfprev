package ca.bc.gov.nrs.wfprev.services;

import ca.bc.gov.nrs.wfone.common.service.api.ServiceException;
import ca.bc.gov.nrs.wfprev.data.assemblers.ProjectBoundaryResourceAssembler;
import ca.bc.gov.nrs.wfprev.data.entities.ProjectBoundaryEntity;
import ca.bc.gov.nrs.wfprev.data.models.ProjectBoundaryModel;
import ca.bc.gov.nrs.wfprev.data.repositories.ProjectBoundaryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.hateoas.CollectionModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProjectBoundaryServiceTest {
    private ProjectBoundaryService projectBoundaryService;
    private ProjectBoundaryRepository projectBoundaryRepository;
    private ProjectBoundaryResourceAssembler projectBoundaryResourceAssembler;

    @BeforeEach
    void setup() {
        projectBoundaryRepository = mock(ProjectBoundaryRepository.class);
        projectBoundaryResourceAssembler = mock(ProjectBoundaryResourceAssembler.class);

        projectBoundaryService = new ProjectBoundaryService(projectBoundaryRepository, projectBoundaryResourceAssembler);
    }

    @Test
    void testGetAllProjectBoundaries_Success() throws ServiceException {
        // Given
        List<ProjectBoundaryEntity> entities = new ArrayList<>();
        entities.add(new ProjectBoundaryEntity());
        entities.add(new ProjectBoundaryEntity());
        when(projectBoundaryRepository.findAll()).thenReturn(entities);
        when(projectBoundaryResourceAssembler.toCollectionModel(entities))
                .thenReturn(CollectionModel.of(new ArrayList<>()));

        // When
        CollectionModel<ProjectBoundaryModel> result = projectBoundaryService.getAllProjectBoundaries();

        // Then
        assertNotNull(result);
    }

    @Test
    void testGetAllProjectBoundaries_Exception() {
        // Given
        when(projectBoundaryRepository.findAll()).thenThrow(new RuntimeException("Error fetching project boundaries"));

        // When
        ServiceException exception = assertThrows(
                ServiceException.class,
                () -> projectBoundaryService.getAllProjectBoundaries()
        );

        // Then
        assertEquals("Error fetching project boundaries", exception.getMessage());
    }

    @Test
    void testGetProjectBoundaryById_Success() throws ServiceException {
        // Given
        String exampleId = UUID.randomUUID().toString();
        ProjectBoundaryEntity entity = new ProjectBoundaryEntity();
        when(projectBoundaryRepository.findById(exampleId))
                .thenReturn(Optional.of(entity));
        when(projectBoundaryResourceAssembler.toModel(entity))
                .thenReturn(new ProjectBoundaryModel());

        // When
        ProjectBoundaryModel result = projectBoundaryService.getProjectBoundaryById(exampleId);

        // Then
        assertNotNull(result);
    }

    @Test
    void testGetProjectBoundaryById_NotFound() throws ServiceException {
        // Given
        String nonExistentId = UUID.randomUUID().toString();
        when(projectBoundaryRepository.findById(nonExistentId))
                .thenReturn(Optional.empty());

        // When
        ProjectBoundaryModel result = projectBoundaryService.getProjectBoundaryById(nonExistentId);

        // Then
        assertNull(result);
    }

    @Test
    void testGetProjectBoundaryById_Exception() {
        // Given
        String exampleId = UUID.randomUUID().toString();
        when(projectBoundaryRepository.findById(exampleId))
                .thenThrow(new RuntimeException("Error fetching project boundary"));

        // When
        ServiceException exception = assertThrows(
                ServiceException.class,
                () -> projectBoundaryService.getProjectBoundaryById(exampleId)
        );

        // Then
        assertTrue(exception.getMessage().contains("Error fetching project boundary"));
    }

    @Test
    void testCreateOrUpdateProjectBoundary_Success() throws ServiceException {
        // Given
        ProjectBoundaryModel inputModel = new ProjectBoundaryModel();
        inputModel.setProjectBoundaryGuid(UUID.randomUUID().toString());
        ProjectBoundaryEntity savedEntity = new ProjectBoundaryEntity();
        when(projectBoundaryResourceAssembler.toEntity(inputModel))
                .thenReturn(savedEntity);
        when(projectBoundaryRepository.saveAndFlush(savedEntity))
                .thenReturn(savedEntity);
        when(projectBoundaryResourceAssembler.toModel(savedEntity))
                .thenReturn(inputModel);

        // When
        ProjectBoundaryModel result = projectBoundaryService.createOrUpdateProjectBoundary(inputModel);

        // Then
        assertNotNull(result);
        assertEquals(inputModel.getProjectBoundaryGuid(), result.getProjectBoundaryGuid());
    }

    @Test
    void testCreateOrUpdateProjectBoundary_Exception() {
        // Given
        ProjectBoundaryModel inputModel = new ProjectBoundaryModel();
        when(projectBoundaryResourceAssembler.toEntity(inputModel))
                .thenThrow(new RuntimeException("Error saving project boundary"));

        // When
        ServiceException exception = assertThrows(
                ServiceException.class,
                () -> projectBoundaryService.createOrUpdateProjectBoundary(inputModel)
        );

        // Then
        assertTrue(exception.getMessage().contains("Error saving project boundary"));
    }

    @Test
    void testDeleteProjectBoundary_Success() throws ServiceException {
        // Given
        String exampleId = UUID.randomUUID().toString();
        ProjectBoundaryModel model = new ProjectBoundaryModel();
        model.setProjectBoundaryGuid(exampleId);
        ProjectBoundaryEntity entity = new ProjectBoundaryEntity();
        entity.setProjectBoundaryGuid(exampleId);
        when(projectBoundaryRepository.findById(exampleId))
                .thenReturn(Optional.of(entity))
                .thenReturn(Optional.empty());
        when(projectBoundaryResourceAssembler.toModel(entity))
                .thenReturn(model);
        when(projectBoundaryResourceAssembler.toEntity(any(ProjectBoundaryModel.class)))
                .thenReturn(entity);

        // When
        ProjectBoundaryModel result = projectBoundaryService.deleteProjectBoundary(exampleId);

        // Then
        verify(projectBoundaryRepository).delete(entity);
        ProjectBoundaryModel projectBoundaryById = projectBoundaryService.getProjectBoundaryById(exampleId);
        assertNull(projectBoundaryById);
    }

    @Test
    void testDeleteProjectBoundary_Exception() {
        // Given
        String exampleId = UUID.randomUUID().toString();
        ProjectBoundaryModel model = new ProjectBoundaryModel();
        model.setProjectBoundaryGuid(exampleId);
        ProjectBoundaryEntity entity = new ProjectBoundaryEntity();
        entity.setProjectBoundaryGuid(exampleId);
        when(projectBoundaryRepository.findById(exampleId))
                .thenReturn(Optional.of(entity));
        when(projectBoundaryResourceAssembler.toModel(entity))
                .thenReturn(model);
        when(projectBoundaryResourceAssembler.toEntity(any(ProjectBoundaryModel.class)))
                .thenReturn(entity);
        doThrow(new RuntimeException("Error deleting project boundary"))
                .when(projectBoundaryRepository).delete(entity);

        // When
        ServiceException exception = assertThrows(
                ServiceException.class,
                () -> projectBoundaryService.deleteProjectBoundary(exampleId)
        );

        // Then
        assertTrue(exception.getMessage().contains("Error deleting project boundary"));
    }
}