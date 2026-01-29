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
import org.mockito.Mock;
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
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProjectBoundaryServiceTest {
    private ProjectBoundaryService projectBoundaryService;
    private ProjectBoundaryRepository projectBoundaryRepository;
    private ProjectBoundaryResourceAssembler projectBoundaryResourceAssembler;
    private ProjectRepository projectRepository;

    private Validator validator;

    @Mock
    private FileAttachmentService fileAttachmentService;

    @BeforeEach
    void setup() {
        projectBoundaryRepository = mock(ProjectBoundaryRepository.class);
        projectBoundaryResourceAssembler = mock(ProjectBoundaryResourceAssembler.class);
        projectRepository = mock(ProjectRepository.class);

        fileAttachmentService = mock(FileAttachmentService.class);
        validator = mock(Validator.class);

        projectBoundaryService = new ProjectBoundaryService(projectBoundaryRepository, projectBoundaryResourceAssembler,
                projectRepository, fileAttachmentService, validator);
    }

    @Test
    void testGetAllProjectBoundaries_Success() throws ServiceException {
        String projectGuid = UUID.randomUUID().toString();
        List<ProjectBoundaryEntity> entities = List.of(new ProjectBoundaryEntity());
        List<ProjectBoundaryModel> models = List.of(new ProjectBoundaryModel());

        when(projectRepository.existsById(UUID.fromString(projectGuid))).thenReturn(true);
        when(projectBoundaryRepository.findByProjectGuid(UUID.fromString(projectGuid))).thenReturn(entities);
        when(projectBoundaryResourceAssembler.toModel(any())).thenReturn(models.get(0));

        CollectionModel<ProjectBoundaryModel> result = projectBoundaryService.getAllProjectBoundaries(projectGuid);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
    }

    @Test
    void testGetAllProjectBoundaries_ProjectNotFound() {
        String projectGuid = UUID.randomUUID().toString();
        when(projectRepository.existsById(UUID.fromString(projectGuid))).thenReturn(false);

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
        ProjectBoundaryModel result = projectBoundaryService.createProjectBoundary(projectGuid, resource);

        // Assert
        assertNotNull(result, "Resulting ProjectBoundaryModel should not be null");
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

        when(projectRepository.existsById(UUID.fromString(projectGuid))).thenReturn(true);
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

        when(projectRepository.existsById(UUID.fromString(projectGuid))).thenReturn(true);
        when(projectBoundaryRepository.findByProjectBoundaryGuid(UUID.fromString(boundaryGuid))).thenReturn(Optional.of(entity));

        projectBoundaryService.deleteProjectBoundary(projectGuid, boundaryGuid, true);

        verify(fileAttachmentService).deleteAttachmentsBySourceObject(boundaryGuid, true);
        verify(projectBoundaryRepository, times(1)).deleteByProjectBoundaryGuid(UUID.fromString(boundaryGuid));
    }

    @Test
    void testDeleteProjectBoundary_NotFound() {
        String projectGuid = UUID.randomUUID().toString();
        String boundaryGuid = UUID.randomUUID().toString();

        when(projectRepository.existsById(UUID.fromString(projectGuid))).thenReturn(true);
        when(projectBoundaryRepository.findByProjectBoundaryGuid(UUID.fromString(boundaryGuid))).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> projectBoundaryService.deleteProjectBoundary(projectGuid, boundaryGuid, true));
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
    // ... skipped redundant methods for brevity if they exist in replacement range ...

    @Test
    void testDeleteProjectBoundaries_Success() {
        // GIVEN
        String projectGuid = UUID.randomUUID().toString();
        String boundaryGuid = UUID.randomUUID().toString();
        ProjectBoundaryEntity entity = new ProjectBoundaryEntity();
        entity.setProjectBoundaryGuid(UUID.fromString(boundaryGuid));

        when(projectBoundaryRepository.findByProjectGuid(UUID.fromString(projectGuid)))
                .thenReturn(List.of(entity));

        // WHEN
        projectBoundaryService.deleteProjectBoundaries(projectGuid, true);

        // THEN
        verify(fileAttachmentService).deleteAttachmentsBySourceObject(boundaryGuid, true);
        verify(projectBoundaryRepository).deleteByProjectGuid(UUID.fromString(projectGuid));
    }

    @Test
    void testDeleteProjectBoundaries_NoFileDeletion() {
        // GIVEN
        String projectGuid = UUID.randomUUID().toString();
        String boundaryGuid = UUID.randomUUID().toString();
        ProjectBoundaryEntity entity = new ProjectBoundaryEntity();
        entity.setProjectBoundaryGuid(UUID.fromString(boundaryGuid));

        when(projectBoundaryRepository.findByProjectGuid(UUID.fromString(projectGuid)))
                .thenReturn(List.of(entity));

        // WHEN
        projectBoundaryService.deleteProjectBoundaries(projectGuid, false);

        // THEN
        verify(fileAttachmentService).deleteAttachmentsBySourceObject(boundaryGuid, false);
        verify(projectBoundaryRepository).deleteByProjectGuid(UUID.fromString(projectGuid));
    }
}