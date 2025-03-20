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
import org.locationtech.jts.geom.Point;
import org.mockito.ArgumentCaptor;
import org.postgresql.geometric.PGpoint;
import org.postgresql.geometric.PGpolygon;
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
import static org.mockito.ArgumentMatchers.eq;
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
        // GIVEN
        String projectGuid = "789e1234-e89b-12d3-a456-426614174002";

        ProjectBoundaryModel resource = new ProjectBoundaryModel();
        resource.setProjectGuid(projectGuid);
        resource.setProjectBoundaryGuid(UUID.randomUUID().toString());

        ProjectEntity projectEntity = new ProjectEntity();
        projectEntity.setProjectGuid(UUID.fromString(projectGuid));

        PGpolygon polygon = new PGpolygon(new PGpoint[]{
                new PGpoint(0.0, 0.0),
                new PGpoint(1.0, 0.0),
                new PGpoint(1.0, 1.0),
                new PGpoint(0.0, 1.0)
        });

        ProjectBoundaryEntity entity = new ProjectBoundaryEntity();
        entity.setProjectGuid(UUID.fromString(projectGuid));
        entity.setBoundaryGeometry(polygon);

        ProjectModel mockProject = new ProjectModel();
        mockProject.setProjectGuid(projectGuid);
        mockProject.setLatitude(BigDecimal.ZERO);
        mockProject.setLongitude(BigDecimal.ZERO);

        when(validator.validate(resource)).thenReturn(Collections.emptySet());
        when(projectRepository.findById(UUID.fromString(projectGuid))).thenReturn(Optional.of(projectEntity));
        when(projectBoundaryResourceAssembler.toEntity(any(ProjectBoundaryModel.class))).thenReturn(entity);
        when(projectBoundaryRepository.save(any(ProjectBoundaryEntity.class))).thenReturn(entity);
        when(projectBoundaryResourceAssembler.toModel(any(ProjectBoundaryEntity.class))).thenReturn(resource);
        when(projectService.getProjectById(eq(projectGuid))).thenReturn(mockProject);

        // WHEN
        ProjectBoundaryModel result = projectBoundaryService.createProjectBoundary(projectGuid, resource);

        // THEN
        assertNotNull(result);

        // Verify that updateProject was called with coordinates set to the centroid (0.5, 0.5)
        ArgumentCaptor<ProjectModel> projectCaptor = ArgumentCaptor.forClass(ProjectModel.class);
        verify(projectService).updateProject(projectCaptor.capture());

        ProjectModel updatedProject = projectCaptor.getValue();
        assertEquals(BigDecimal.valueOf(0.5), updatedProject.getLatitude());
        assertEquals(BigDecimal.valueOf(0.5), updatedProject.getLongitude());

        verify(projectBoundaryRepository).save(any(ProjectBoundaryEntity.class));
    }


    @Test
    void testCreateProjectBoundary_ValidationFails() {
        String projectGuid = UUID.randomUUID().toString();
        ProjectBoundaryModel resource = new ProjectBoundaryModel();
        Set<ConstraintViolation<ProjectBoundaryModel>> violations = Set.of(mock(ConstraintViolation.class));

        when(validator.validate(resource)).thenReturn(violations);

        assertThrows(ConstraintViolationException.class, () -> projectBoundaryService.createProjectBoundary(projectGuid, resource));
    }

    @Test
    void testUpdateProjectBoundary_Success() {
        // Arrange
        String projectGuid = UUID.randomUUID().toString();
        String boundaryGuid = UUID.randomUUID().toString();

        ProjectBoundaryModel resource = new ProjectBoundaryModel();
        resource.setProjectGuid(projectGuid);
        resource.setProjectBoundaryGuid(boundaryGuid);

        // Create a polygon for the boundary geometry
        PGpolygon polygon = new PGpolygon(new PGpoint[]{
                new PGpoint(0.0, 0.0),
                new PGpoint(1.0, 0.0),
                new PGpoint(1.0, 1.0),
                new PGpoint(0.0, 1.0)
        });

        ProjectBoundaryEntity existingEntity = new ProjectBoundaryEntity();
        existingEntity.setProjectGuid(UUID.fromString(projectGuid));
        existingEntity.setBoundaryGeometry(null);

        ProjectBoundaryEntity updatedEntity = new ProjectBoundaryEntity();
        updatedEntity.setProjectGuid(UUID.fromString(projectGuid));
        updatedEntity.setBoundaryGeometry(polygon);

        ProjectModel mockProject = new ProjectModel();
        mockProject.setProjectGuid(projectGuid);
        mockProject.setLatitude(BigDecimal.ZERO);
        mockProject.setLongitude(BigDecimal.ZERO);

        when(validator.validate(resource)).thenReturn(Collections.emptySet());
        when(projectRepository.findById(UUID.fromString(projectGuid))).thenReturn(Optional.of(new ProjectEntity()));
        when(projectBoundaryRepository.findByProjectBoundaryGuid(UUID.fromString(boundaryGuid)))
                .thenReturn(Optional.of(existingEntity));
        when(projectBoundaryResourceAssembler.updateEntity(resource, existingEntity)).thenReturn(updatedEntity);
        when(projectBoundaryRepository.saveAndFlush(any())).thenReturn(updatedEntity);
        when(projectBoundaryResourceAssembler.toModel(updatedEntity)).thenReturn(resource);
        when(projectService.getProjectById(eq(projectGuid))).thenReturn(mockProject);

        // Act
        ProjectBoundaryModel result = projectBoundaryService.updateProjectBoundary(projectGuid, resource);

        // Assert
        assertNotNull(result, "Resulting ProjectBoundaryModel should not be null");

        // Verify project coordinates were updated with the centroid values
        ArgumentCaptor<ProjectModel> projectCaptor = ArgumentCaptor.forClass(ProjectModel.class);
        verify(projectService).updateProject(projectCaptor.capture());

        ProjectModel updatedProject = projectCaptor.getValue();
        assertEquals(BigDecimal.valueOf(0.5), updatedProject.getLatitude());
        assertEquals(BigDecimal.valueOf(0.5), updatedProject.getLongitude());
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
        UUID projectGuid = UUID.randomUUID();

        ProjectBoundaryEntity entity = new ProjectBoundaryEntity();
        entity.setProjectGuid(projectGuid);

        // Create a valid polygon for boundaryGeometry instead of just a Point
        PGpoint[] points = new PGpoint[] {
                new PGpoint(0.0, 0.0),
                new PGpoint(1.0, 0.0),
                new PGpoint(1.0, 1.0),
                new PGpoint(0.0, 1.0),
                new PGpoint(0.0, 0.0)
        };
        PGpolygon polygon = new PGpolygon(points);
        entity.setBoundaryGeometry(polygon);

        GeometryFactory geometryFactory = new GeometryFactory();
        Point locationGeometry = geometryFactory.createPoint(new Coordinate(-123.3656, 48.4284));
        entity.setLocationGeometry(locationGeometry);

        ProjectBoundaryModel expectedModel = new ProjectBoundaryModel();

        ProjectModel projectModel = new ProjectModel();
        projectModel.setProjectGuid(projectGuid.toString());
        projectModel.setLatitude(BigDecimal.ZERO);
        projectModel.setLongitude(BigDecimal.ZERO);

        when(projectService.getProjectById(projectGuid.toString())).thenReturn(projectModel);
        when(projectService.updateProject(any(ProjectModel.class))).thenReturn(projectModel);
        when(projectBoundaryRepository.saveAndFlush(entity)).thenReturn(entity);
        when(projectBoundaryResourceAssembler.toModel(entity)).thenReturn(expectedModel);

        // Act
        ProjectBoundaryModel result = projectBoundaryService.saveProjectBoundary(entity);

        // Assert
        assertNotNull(result);
        assertEquals(expectedModel, result);

        verify(projectBoundaryRepository).saveAndFlush(entity);
        verify(projectBoundaryResourceAssembler).toModel(entity);

        // Verify projectService.updateProject was called
        ArgumentCaptor<ProjectModel> projectCaptor = ArgumentCaptor.forClass(ProjectModel.class);
        verify(projectService).updateProject(projectCaptor.capture());

        // The centroid of a square from (0,0) to (1,1) should be (0.5, 0.5)
        ProjectModel updatedProject = projectCaptor.getValue();
        assertEquals(BigDecimal.valueOf(0.5), updatedProject.getLatitude());
        assertEquals(BigDecimal.valueOf(0.5), updatedProject.getLongitude());
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
    void testUpdateProjectCoordinates_Success() {
        // Arrange
        UUID projectGuid = UUID.randomUUID();

        ProjectBoundaryEntity boundary = new ProjectBoundaryEntity();
        boundary.setProjectGuid(projectGuid);

        // Create a valid polygon for boundaryGeometry
        PGpoint[] points = new PGpoint[] {
                new PGpoint(-123.37, 48.42),
                new PGpoint(-123.36, 48.42),
                new PGpoint(-123.36, 48.43),
                new PGpoint(-123.37, 48.43),
                new PGpoint(-123.37, 48.42) // close the polygon
        };
        PGpolygon polygon = new PGpolygon(points);
        boundary.setBoundaryGeometry(polygon);

        // Still include locationGeometry if your implementation uses it somewhere
        GeometryFactory geometryFactory = new GeometryFactory();
        Point locationGeometry = geometryFactory.createPoint(new Coordinate(-123.3656, 48.4284));
        boundary.setLocationGeometry(locationGeometry);

        ProjectModel project = new ProjectModel();
        project.setProjectGuid(projectGuid.toString());
        project.setLatitude(BigDecimal.ZERO);
        project.setLongitude(BigDecimal.ZERO);

        when(projectService.getProjectById(projectGuid.toString())).thenReturn(project);
        when(projectService.updateProject(any(ProjectModel.class))).thenReturn(project);

        // Act
        projectBoundaryService.updateProjectCoordinates(boundary);

        // Assert
        // Use assertEquals with a delta for floating-point comparison
        assertEquals(48.425, project.getLatitude().doubleValue(), 0.0001, "Latitude should be approximately 48.425");
        assertEquals(-123.365, project.getLongitude().doubleValue(), 0.0001, "Longitude should be approximately -123.365");

        // Verify projectService.updateProject was called
        verify(projectService).updateProject(project);
    }
    @Test
    void testUpdateProjectCoordinates_ProjectNotFound() {
        // Arrange
        ProjectBoundaryEntity boundary = new ProjectBoundaryEntity();
        boundary.setProjectGuid(UUID.randomUUID());

        when(projectService.getProjectById(boundary.getProjectGuid().toString())).thenReturn(null);

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> projectBoundaryService.updateProjectCoordinates(boundary));
    }

    @Test
    void testUpdateProjectCoordinates_LocationGeometryNull() {
        // Arrange
        ProjectBoundaryEntity boundary = new ProjectBoundaryEntity();
        boundary.setProjectGuid(UUID.randomUUID());
        boundary.setLocationGeometry(null); // No coordinates provided

        ProjectModel project = new ProjectModel();
        project.setProjectGuid(boundary.getProjectGuid().toString());

        when(projectService.getProjectById(boundary.getProjectGuid().toString())).thenReturn(project);

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> projectBoundaryService.updateProjectCoordinates(boundary));
    }



}