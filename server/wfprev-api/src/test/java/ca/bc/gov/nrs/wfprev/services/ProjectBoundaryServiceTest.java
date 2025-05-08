package ca.bc.gov.nrs.wfprev.services;

import ca.bc.gov.nrs.wfone.common.service.api.ServiceException;
import ca.bc.gov.nrs.wfprev.data.assemblers.ProjectBoundaryResourceAssembler;
import ca.bc.gov.nrs.wfprev.data.entities.ProjectBoundaryEntity;
import ca.bc.gov.nrs.wfprev.data.entities.ProjectEntity;
import ca.bc.gov.nrs.wfprev.data.models.ProjectBoundaryModel;
import ca.bc.gov.nrs.wfprev.data.models.ProjectModel;
import ca.bc.gov.nrs.wfprev.data.repositories.ProjectBoundaryRepository;
import ca.bc.gov.nrs.wfprev.data.repositories.ProjectRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;
import org.springframework.hateoas.CollectionModel;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProjectBoundaryServiceTest {
    private ProjectBoundaryService projectBoundaryService;
    private ProjectBoundaryRepository projectBoundaryRepository;
    private ProjectBoundaryResourceAssembler projectBoundaryResourceAssembler;
    private ProjectRepository projectRepository;
    private ProjectService projectService;
    private Validator validator;

    @BeforeEach
    void setup() {
        projectBoundaryRepository = mock(ProjectBoundaryRepository.class);
        projectBoundaryResourceAssembler = mock(ProjectBoundaryResourceAssembler.class);
        projectRepository = mock(ProjectRepository.class);
        projectService = mock(ProjectService.class);
        validator = mock(Validator.class);

        projectBoundaryService = new ProjectBoundaryService(projectBoundaryRepository, projectBoundaryResourceAssembler,
                projectRepository, projectService, validator);
    }

    @Test
    void testGetAllProjectBoundaries_Success() throws ServiceException {
        String projectGuid = UUID.randomUUID().toString();
        List<ProjectBoundaryEntity> entities = List.of(new ProjectBoundaryEntity());
        List<ProjectBoundaryModel> models = List.of(new ProjectBoundaryModel());

        when(projectService.getProjectById(projectGuid)).thenReturn(new ProjectModel());
        when(projectBoundaryRepository.findByProjectGuid(UUID.fromString(projectGuid))).thenReturn(entities);
        when(projectBoundaryResourceAssembler.toModel(any())).thenReturn(models.get(0));

        CollectionModel<ProjectBoundaryModel> result = projectBoundaryService.getAllProjectBoundaries(projectGuid);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
    }

    @Test
    void testGetAllProjectBoundaries_ProjectNotFound() {
        String projectGuid = UUID.randomUUID().toString();
        when(projectService.getProjectById(projectGuid)).thenReturn(null);

        assertThrows(EntityNotFoundException.class, () -> projectBoundaryService.getAllProjectBoundaries(projectGuid));
    }

    @Test
    void testCreateProjectBoundary_Success() {
        // Arrange
        String projectGuid = UUID.randomUUID().toString();
        ProjectBoundaryModel resource = new ProjectBoundaryModel();
        resource.setProjectGuid(projectGuid);
        resource.setProjectBoundaryGuid(UUID.randomUUID().toString());

        ProjectBoundaryEntity entity = new ProjectBoundaryEntity();
        entity.setProjectGuid(UUID.fromString(projectGuid));
        ProjectEntity projectEntity = new ProjectEntity();
        projectEntity.setProjectGuid(UUID.fromString(projectGuid));

        // Ensure Validator Passes
        when(validator.validate(resource)).thenReturn(Collections.emptySet());

        // Ensure Project Exists
        when(projectRepository.findById(UUID.fromString(projectGuid))).thenReturn(Optional.of(projectEntity));

        // Ensure Assembler Converts Model to Entity
        when(projectBoundaryResourceAssembler.toEntity(resource)).thenReturn(entity);

        // Ensure Repository Saves Entity
        when(projectBoundaryRepository.save(any())).thenReturn(entity);
        when(projectBoundaryResourceAssembler.toModel(entity)).thenReturn(resource);

        // Act
        ProjectBoundaryModel result = projectBoundaryService.createOrUpdateProjectBoundary(projectGuid, resource);

        // Assert
        assertNotNull(result, "Resulting ProjectBoundaryModel should not be null");
    }

    @Test
    void testCreateProjectBoundary_ValidationFails() {
        String projectGuid = UUID.randomUUID().toString();
        ProjectBoundaryModel resource = new ProjectBoundaryModel();
        Set<ConstraintViolation<ProjectBoundaryModel>> violations = Set.of(mock(ConstraintViolation.class));

        when(validator.validate(resource)).thenReturn(violations);

        assertThrows(ConstraintViolationException.class, () -> projectBoundaryService.createOrUpdateProjectBoundary(projectGuid, resource));
    }

    @Test
    void testUpdateProjectBoundary_Success() {
        // Arrange
        String projectGuid = UUID.randomUUID().toString();
        String boundaryGuid = UUID.randomUUID().toString();

        ProjectBoundaryModel resource = new ProjectBoundaryModel();
        resource.setProjectGuid(projectGuid);
        resource.setProjectBoundaryGuid(boundaryGuid);

        ProjectBoundaryEntity existingEntity = new ProjectBoundaryEntity();
        existingEntity.setProjectGuid(UUID.fromString(projectGuid));

        ProjectBoundaryEntity updatedEntity = new ProjectBoundaryEntity();
        updatedEntity.setProjectGuid(UUID.fromString(projectGuid));

        // Ensure Validator Passes
        when(validator.validate(resource)).thenReturn(Collections.emptySet());

        // Ensure Project Exists
        when(projectRepository.findById(UUID.fromString(projectGuid))).thenReturn(Optional.of(new ProjectEntity()));

        // Ensure Boundary Exists
        when(projectBoundaryRepository.findByProjectBoundaryGuid(UUID.fromString(boundaryGuid)))
                .thenReturn(Optional.of(existingEntity));

        // Ensure Assembler Updates Entity
        when(projectBoundaryResourceAssembler.updateEntity(resource, existingEntity)).thenReturn(updatedEntity);

        // Ensure Repository Saves Updated Entity
        when(projectBoundaryRepository.saveAndFlush(any())).thenReturn(updatedEntity);
        when(projectBoundaryResourceAssembler.toModel(updatedEntity)).thenReturn(resource);

        // Act
        ProjectBoundaryModel result = projectBoundaryService.updateProjectBoundary(projectGuid, resource);

        // Assert
        assertNotNull(result, "Resulting ProjectBoundaryModel should not be null");
    }

    @Test
    void testUpdateProjectBoundary_ValidationFails() {
        // Arrange
        String projectGuid = UUID.randomUUID().toString();
        ProjectBoundaryModel resource = new ProjectBoundaryModel();
        resource.setProjectGuid(projectGuid);

        // Mock Constraint Violations
        Set<ConstraintViolation<ProjectBoundaryModel>> violations = Set.of(mock(ConstraintViolation.class));
        when(validator.validate(resource)).thenReturn(violations);

        // Act & Assert
        assertThrows(ConstraintViolationException.class, () -> {
            projectBoundaryService.updateProjectBoundary(projectGuid, resource);
        });
    }

    @Test
    void testUpdateProjectBoundary_ProjectNotFound() {
        // Arrange
        String projectGuid = UUID.randomUUID().toString();
        String boundaryGuid = UUID.randomUUID().toString();
        ProjectBoundaryModel resource = new ProjectBoundaryModel();
        resource.setProjectGuid(projectGuid);
        resource.setProjectBoundaryGuid(boundaryGuid);

        // Mock Project Not Found
        when(projectRepository.findById(UUID.fromString(projectGuid))).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> {
            projectBoundaryService.updateProjectBoundary(projectGuid, resource);
        });
    }

    @Test
    void testUpdateProjectBoundary_BoundaryNotFound() {
        // Arrange
        String projectGuid = UUID.randomUUID().toString();
        String boundaryGuid = UUID.randomUUID().toString();
        ProjectBoundaryModel resource = new ProjectBoundaryModel();
        resource.setProjectGuid(projectGuid);
        resource.setProjectBoundaryGuid(boundaryGuid);

        // Mock Project Exists
        when(projectRepository.findById(UUID.fromString(projectGuid))).thenReturn(Optional.of(new ProjectEntity()));

        // Mock Boundary Not Found
        when(projectBoundaryRepository.findByProjectBoundaryGuid(UUID.fromString(boundaryGuid)))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> {
            projectBoundaryService.updateProjectBoundary(projectGuid, resource);
        });
    }

    @Test
    void testUpdateProjectBoundary_BoundaryDoesNotBelongToProject() {
        // Arrange
        String projectGuid = UUID.randomUUID().toString();
        String boundaryGuid = UUID.randomUUID().toString();
        ProjectBoundaryModel resource = new ProjectBoundaryModel();
        resource.setProjectGuid(projectGuid);
        resource.setProjectBoundaryGuid(boundaryGuid);

        ProjectBoundaryEntity existingEntity = new ProjectBoundaryEntity();
        existingEntity.setProjectGuid(UUID.randomUUID()); // Different Project GUID

        // Mock Project Exists
        when(projectRepository.findById(UUID.fromString(projectGuid))).thenReturn(Optional.of(new ProjectEntity()));

        // Mock Boundary Exists but belongs to another project
        when(projectBoundaryRepository.findByProjectBoundaryGuid(UUID.fromString(boundaryGuid)))
                .thenReturn(Optional.of(existingEntity));

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> {
            projectBoundaryService.updateProjectBoundary(projectGuid, resource);
        });
    }


    @Test
    void testGetProjectBoundary_Success() {
        String projectGuid = UUID.randomUUID().toString();
        String boundaryGuid = UUID.randomUUID().toString();
        ProjectBoundaryEntity entity = new ProjectBoundaryEntity();
        entity.setProjectGuid(UUID.fromString(projectGuid));

        when(projectService.getProjectById(projectGuid)).thenReturn(new ProjectModel());
        when(projectBoundaryRepository.findByProjectBoundaryGuid(UUID.fromString(boundaryGuid))).thenReturn(Optional.of(entity));
        when(projectBoundaryResourceAssembler.toModel(entity)).thenReturn(new ProjectBoundaryModel());

        ProjectBoundaryModel result = projectBoundaryService.getProjectBoundary(projectGuid, boundaryGuid);

        assertNotNull(result);
    }

    @Test
    void testDeleteProjectBoundary_Success() {
        String projectGuid = UUID.randomUUID().toString();
        String boundaryGuid = UUID.randomUUID().toString();
        ProjectBoundaryEntity entity = new ProjectBoundaryEntity();
        entity.setProjectGuid(UUID.fromString(projectGuid));

        when(projectService.getProjectById(projectGuid)).thenReturn(new ProjectModel());
        when(projectBoundaryRepository.findByProjectBoundaryGuid(UUID.fromString(boundaryGuid))).thenReturn(Optional.of(entity));

        projectBoundaryService.deleteProjectBoundary(projectGuid, boundaryGuid);

        verify(projectBoundaryRepository, times(1)).deleteByProjectBoundaryGuid(UUID.fromString(boundaryGuid));
    }

    @Test
    void testDeleteProjectBoundary_NotFound() {
        String projectGuid = UUID.randomUUID().toString();
        String boundaryGuid = UUID.randomUUID().toString();

        when(projectService.getProjectById(projectGuid)).thenReturn(new ProjectModel());
        when(projectBoundaryRepository.findByProjectBoundaryGuid(UUID.fromString(boundaryGuid))).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> projectBoundaryService.deleteProjectBoundary(projectGuid, boundaryGuid));
    }

    @Test
    void testSaveProjectBoundary_Success() {
        // Arrange
        ProjectBoundaryEntity entity = new ProjectBoundaryEntity();
        ProjectBoundaryModel expectedModel = new ProjectBoundaryModel();

        when(projectBoundaryRepository.saveAndFlush(entity)).thenReturn(entity);
        when(projectBoundaryResourceAssembler.toModel(entity)).thenReturn(expectedModel);

        // Act
        ProjectBoundaryModel result = projectBoundaryService.saveProjectBoundary(entity);

        // Assert
        assertNotNull(result);
        assertEquals(expectedModel, result);
        verify(projectBoundaryRepository, times(1)).saveAndFlush(entity);
        verify(projectBoundaryResourceAssembler, times(1)).toModel(entity);
    }

    @Test
    void testSaveProjectBoundary_ThrowsIllegalArgumentException() {
        // Arrange
        ProjectBoundaryEntity entity = new ProjectBoundaryEntity();

        when(projectBoundaryRepository.saveAndFlush(entity)).thenThrow(new IllegalArgumentException("Invalid argument"));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            projectBoundaryService.saveProjectBoundary(entity);
        });

        assertEquals("Invalid argument", exception.getMessage());
        verify(projectBoundaryRepository, times(1)).saveAndFlush(entity);
    }

    @Test
    void testSaveProjectBoundary_ThrowsEntityNotFoundException() {
        // Arrange
        ProjectBoundaryEntity entity = new ProjectBoundaryEntity();

        when(projectBoundaryRepository.saveAndFlush(entity)).thenThrow(new EntityNotFoundException("Entity not found"));

        // Act & Assert
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            projectBoundaryService.saveProjectBoundary(entity);
        });

        assertEquals("Entity not found", exception.getMessage());
        verify(projectBoundaryRepository, times(1)).saveAndFlush(entity);
    }

    @Test
    void testSaveProjectBoundary_ThrowsGenericException() {
        // Arrange
        ProjectBoundaryEntity entity = new ProjectBoundaryEntity();

        when(projectBoundaryRepository.saveAndFlush(entity)).thenThrow(new RuntimeException("Unexpected error"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            projectBoundaryService.saveProjectBoundary(entity);
        });

        assertEquals("Unexpected error", exception.getMessage());
        verify(projectBoundaryRepository, times(1)).saveAndFlush(entity);
    }

    @Test
    void testCreateOrUpdateProjectBoundary_UpdatesExistingBoundary() {
        // Arrange
        String projectGuid = UUID.randomUUID().toString();
        ProjectBoundaryModel resource = new ProjectBoundaryModel();
        resource.setProjectGuid(projectGuid);

        ProjectEntity projectEntity = new ProjectEntity();
        projectEntity.setProjectGuid(UUID.fromString(projectGuid));

        ProjectBoundaryEntity existingEntity = new ProjectBoundaryEntity();
        existingEntity.setProjectGuid(UUID.fromString(projectGuid));
        List<ProjectBoundaryEntity> existingEntities = List.of(existingEntity);

        ProjectBoundaryEntity updatedEntity = new ProjectBoundaryEntity();
        updatedEntity.setProjectGuid(UUID.fromString(projectGuid));

        // Mock validations pass
        when(validator.validate(resource)).thenReturn(Collections.emptySet());

        // Mock project exists
        when(projectRepository.findById(UUID.fromString(projectGuid))).thenReturn(Optional.of(projectEntity));

        // Mock existing boundary found
        when(projectBoundaryRepository.findByProjectGuid(UUID.fromString(projectGuid))).thenReturn(existingEntities);

        // Setup update of existing entity
        when(projectBoundaryResourceAssembler.updateEntity(resource, existingEntity)).thenReturn(updatedEntity);

        // Mock save operation
        when(projectBoundaryRepository.saveAndFlush(updatedEntity)).thenReturn(updatedEntity);

        // Mock conversion back to model
        ProjectBoundaryModel expectedModel = new ProjectBoundaryModel();
        when(projectBoundaryResourceAssembler.toModel(updatedEntity)).thenReturn(expectedModel);

        // Act
        ProjectBoundaryModel result = projectBoundaryService.createOrUpdateProjectBoundary(projectGuid, resource);

        // Assert
        assertNotNull(result);
        assertEquals(expectedModel, result);

        // Verify the update path was taken
        verify(projectBoundaryResourceAssembler, times(1)).updateEntity(resource, existingEntity);
        verify(projectBoundaryRepository, times(1)).saveAndFlush(updatedEntity);
        verify(projectBoundaryResourceAssembler, times(1)).toModel(updatedEntity);

        // Verify the create path was not taken
        verify(projectBoundaryResourceAssembler, times(0)).toEntity(any());
        verify(projectBoundaryRepository, times(0)).save(any());
    }

    @Test
    void testConvertMultiPolygonAreaToHectares() {
        GeometryFactory geometryFactory = new GeometryFactory();

        Coordinate[] coords = new Coordinate[] {
                new Coordinate(0.0, 0.0),
                new Coordinate(0.01, 0.0),
                new Coordinate(0.01, 0.01),
                new Coordinate(0.0, 0.01),
                new Coordinate(0.0, 0.0)
        };
        LinearRing shell = geometryFactory.createLinearRing(coords);
        Polygon polygon = geometryFactory.createPolygon(shell, null);

        MultiPolygon multiPolygon = geometryFactory.createMultiPolygon(new Polygon[]{ polygon });

        BigDecimal result = projectBoundaryService.convertMultiPolygonAreaToHectares(multiPolygon);

        BigDecimal expected = new BigDecimal("123.6431");

        assertEquals(expected, result, "Area in hectares should match expected conversion");
    }


}