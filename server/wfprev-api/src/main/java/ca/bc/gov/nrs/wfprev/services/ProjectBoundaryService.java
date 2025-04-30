package ca.bc.gov.nrs.wfprev.services;

import ca.bc.gov.nrs.wfone.common.service.api.ServiceException;
import ca.bc.gov.nrs.wfprev.common.services.CommonService;
import ca.bc.gov.nrs.wfprev.data.assemblers.ProjectBoundaryResourceAssembler;
import ca.bc.gov.nrs.wfprev.data.entities.ProjectBoundaryEntity;
import ca.bc.gov.nrs.wfprev.data.entities.ProjectEntity;
import ca.bc.gov.nrs.wfprev.data.models.ProjectBoundaryModel;
import ca.bc.gov.nrs.wfprev.data.repositories.ProjectBoundaryRepository;
import ca.bc.gov.nrs.wfprev.data.repositories.ProjectRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Component
public class ProjectBoundaryService implements CommonService {

  @Autowired
  private ObjectMapper objectMapper;

  private static final String PROJECT_NOT_FOUND = "Project not found";
  private static final String BOUNDARY_NOT_FOUND = "Project Boundary not found";
  private static final String BOUNDARY = "Project Boundary";
  private static final String DOES_NOT_BELONG_PROJECT = "does not belong to Project";
  private static final String KEY_FORMAT = "{0}: {1}";
  private static final String EXTENDED_KEY_FORMAT = "{0}: {1} {2}: {3}";

  private final ProjectBoundaryRepository projectBoundaryRepository;
  private final ProjectBoundaryResourceAssembler projectBoundaryResourceAssembler;
  private final ProjectRepository projectRepository;
  private final ProjectService projectService;
  private final Validator validator;

  public ProjectBoundaryService(
          ProjectBoundaryRepository projectBoundaryRepository,
          ProjectBoundaryResourceAssembler projectBoundaryResourceAssembler,
          ProjectRepository projectRepository,
          ProjectService projectService,
          Validator validator) {
    this.projectBoundaryRepository = projectBoundaryRepository;
    this.projectBoundaryResourceAssembler = projectBoundaryResourceAssembler;
    this.projectRepository = projectRepository;
    this.projectService = projectService;
    this.validator = validator;
  }

  public CollectionModel<ProjectBoundaryModel> getAllProjectBoundaries(String projectGuid) throws ServiceException {
    try {
      // Verify project exists and belongs to the correct hierarchy
      if (projectService.getProjectById(projectGuid) == null) {
        throw new EntityNotFoundException(MessageFormat.format(KEY_FORMAT, PROJECT_NOT_FOUND, projectGuid));
      }

      // Find all boundaries for this project
      List<ProjectBoundaryEntity> boundaries = projectBoundaryRepository
              .findByProjectGuid(UUID.fromString(projectGuid));

      // Convert entities to models
      List<ProjectBoundaryModel> boundaryModels = boundaries.stream()
              .map(projectBoundaryResourceAssembler::toModel)
              .toList();

      // DEBUG LOG each model
      boundaryModels.forEach(b -> log.info("Mapped ProjectBoundaryModel: {}", b));

      return CollectionModel.of(boundaryModels);

    } catch (IllegalArgumentException e) {
      throw new ServiceException("Invalid GUID format", e);
    }
  }

  @Transactional
  public ProjectBoundaryModel createOrUpdateProjectBoundary(
          String projectGuid, ProjectBoundaryModel resource) {
    Set<ConstraintViolation<ProjectBoundaryModel>> violations = validator.validate(resource);
    if (!violations.isEmpty()) {
      throw new ConstraintViolationException(violations);
    }

    // Verify project exists and belongs to the correct hierarchy
    if (resource.getProjectGuid() != null && !resource.getProjectGuid().equals(projectGuid)) {
      throw new EntityNotFoundException(MessageFormat.format(KEY_FORMAT, PROJECT_NOT_FOUND, projectGuid));
    }

    ProjectEntity projectEntity = projectRepository.findById(UUID.fromString(projectGuid))
            .orElseThrow(() -> new EntityNotFoundException(MessageFormat.format(KEY_FORMAT, PROJECT_NOT_FOUND, projectGuid)));

    updateFieldsFromBoundaryGeometry(resource);

    List<ProjectBoundaryEntity> existingEntityList = projectBoundaryRepository.findByProjectGuid(UUID.fromString(projectGuid));

    if (!existingEntityList.isEmpty() && existingEntityList.getFirst() != null) {
      // Update existing boundary
      ProjectBoundaryEntity existingEntity = existingEntityList.getFirst();
      ProjectBoundaryEntity updatedEntity = projectBoundaryResourceAssembler.updateEntity(resource, existingEntity);
      return saveProjectBoundary(updatedEntity);
    } else {
      // Create new boundary
      resource.setProjectBoundaryGuid(UUID.randomUUID().toString());
      ProjectBoundaryEntity newEntity = projectBoundaryResourceAssembler.toEntity(resource);
      newEntity.setProjectGuid(projectEntity.getProjectGuid());
      ProjectBoundaryEntity savedEntity = projectBoundaryRepository.save(newEntity);
      return projectBoundaryResourceAssembler.toModel(savedEntity);
    }
  }

  @Transactional
  public ProjectBoundaryModel updateProjectBoundary(
          String projectGuid, ProjectBoundaryModel resource) {
    Set<ConstraintViolation<ProjectBoundaryModel>> violations = validator.validate(resource);
    if (!violations.isEmpty()) {
      throw new ConstraintViolationException(violations);
    }

    // Verify project exists and belongs to the correct hierarchy
    if (resource.getProjectGuid() != null && !resource.getProjectGuid().equals(projectGuid)) {
      throw new EntityNotFoundException(MessageFormat.format(KEY_FORMAT, PROJECT_NOT_FOUND, projectGuid));
    }

    // Verify boundary exists
    if(resource.getProjectGuid() != null) {
      UUID boundaryGuid = UUID.fromString(resource.getProjectBoundaryGuid());
      ProjectBoundaryEntity existingEntity = projectBoundaryRepository.findByProjectBoundaryGuid(boundaryGuid)
              .orElseThrow(() -> new EntityNotFoundException(MessageFormat.format(KEY_FORMAT, BOUNDARY_NOT_FOUND, resource.getProjectBoundaryGuid())));

      // Verify boundary belongs to the specified project
      if (!existingEntity.getProjectGuid().toString().equals(projectGuid)) {
        throw new EntityNotFoundException(MessageFormat.format(EXTENDED_KEY_FORMAT, BOUNDARY, boundaryGuid, DOES_NOT_BELONG_PROJECT, projectGuid));
      }

      updateFieldsFromBoundaryGeometry(resource);
      ProjectBoundaryEntity entity = projectBoundaryResourceAssembler.updateEntity(resource, existingEntity);
      return saveProjectBoundary(entity);
    } else throw new IllegalArgumentException("ProjectBoundaryModel resource to be updated cannot be null");
  }

  public ProjectBoundaryModel saveProjectBoundary(ProjectBoundaryEntity entity) {
    try {
      ProjectBoundaryEntity savedEntity = projectBoundaryRepository.saveAndFlush(entity);
      return projectBoundaryResourceAssembler.toModel(savedEntity);
    } catch (IllegalArgumentException e) {
      log.error("IllegalArgumentException for Project Boundary: {}", e.getMessage(), e);
      throw e;
    } catch (EntityNotFoundException e) {
      log.error("EntityNotFoundException for Project Boundary: {}", e.getMessage(), e);
      throw e;
    } catch (Exception e) {
      log.error("Exception for Project Boundary: {}", e.getMessage(), e);
      throw e;
    }
  }

  public ProjectBoundaryModel getProjectBoundary(String projectGuid, String boundaryGuid) {
    // Verify project exists and belongs to the correct hierarchy
    if (projectService.getProjectById(projectGuid) == null) {
      throw new EntityNotFoundException(MessageFormat.format(KEY_FORMAT, PROJECT_NOT_FOUND, projectGuid));
    }

    // Get the boundary
    ProjectBoundaryEntity boundary = projectBoundaryRepository.findByProjectBoundaryGuid(UUID.fromString(boundaryGuid))
            .orElseThrow(() -> new EntityNotFoundException(MessageFormat.format(KEY_FORMAT, BOUNDARY_NOT_FOUND, boundaryGuid)));

    // Verify boundary belongs to the specified project
    if (!boundary.getProjectGuid().toString().equals(projectGuid)) {
      throw new EntityNotFoundException(MessageFormat.format(EXTENDED_KEY_FORMAT, BOUNDARY, boundaryGuid, DOES_NOT_BELONG_PROJECT, projectGuid));
    }

    return projectBoundaryResourceAssembler.toModel(boundary);
  }

  @Transactional
  public void deleteProjectBoundary(
          String projectGuid, String boundaryGuid) {
    // Verify project exists and belongs to the correct hierarchy
    if (projectService.getProjectById(projectGuid) == null) {
      throw new EntityNotFoundException(MessageFormat.format(KEY_FORMAT, PROJECT_NOT_FOUND, projectGuid));
    }

    // Get the boundary
    ProjectBoundaryEntity boundary = projectBoundaryRepository.findByProjectBoundaryGuid(UUID.fromString(boundaryGuid))
            .orElseThrow(() -> new EntityNotFoundException(MessageFormat.format(KEY_FORMAT, BOUNDARY_NOT_FOUND, boundaryGuid)));

    // Verify boundary belongs to the specified project
    if (!boundary.getProjectGuid().toString().equals(projectGuid)) {
      throw new EntityNotFoundException(MessageFormat.format(EXTENDED_KEY_FORMAT, BOUNDARY, boundaryGuid, DOES_NOT_BELONG_PROJECT, projectGuid));
    }

    projectBoundaryRepository.deleteByProjectBoundaryGuid(UUID.fromString(boundaryGuid));
  }

  private ProjectBoundaryModel updateFieldsFromBoundaryGeometry(ProjectBoundaryModel model) {
    if(model.getBoundaryGeometry() != null) {
      model.setBoundarySizeHa(BigDecimal.valueOf(model.getBoundaryGeometry().getArea() / 10000.0));
      if(model.getLocationGeometry() == null) {
        model.setLocationGeometry(model.getBoundaryGeometry().getCentroid());
      }
    } return model;
  }
}