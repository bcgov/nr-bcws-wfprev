package ca.bc.gov.nrs.wfprev.services;

import ca.bc.gov.nrs.wfone.common.service.api.ServiceException;
import ca.bc.gov.nrs.wfprev.common.services.CommonService;
import ca.bc.gov.nrs.wfprev.data.assemblers.ProjectBoundaryResourceAssembler;
import ca.bc.gov.nrs.wfprev.data.entities.ProjectBoundaryEntity;
import ca.bc.gov.nrs.wfprev.data.entities.ProjectEntity;
import ca.bc.gov.nrs.wfprev.data.models.ProjectBoundaryModel;
import ca.bc.gov.nrs.wfprev.data.models.ProjectModel;
import ca.bc.gov.nrs.wfprev.data.repositories.ProjectBoundaryRepository;
import ca.bc.gov.nrs.wfprev.data.repositories.ProjectRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import lombok.extern.slf4j.Slf4j;
import org.postgresql.geometric.PGpoint;
import org.postgresql.geometric.PGpolygon;
import org.springframework.hateoas.CollectionModel;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.MessageFormat;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Component
public class ProjectBoundaryService implements CommonService {

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

      return CollectionModel.of(boundaryModels);

    } catch (IllegalArgumentException e) {
      throw new ServiceException("Invalid GUID format", e);
    }
  }

  @Transactional
  public ProjectBoundaryModel createProjectBoundary(
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

    resource.setProjectBoundaryGuid(UUID.randomUUID().toString());

    ProjectBoundaryEntity entity = projectBoundaryResourceAssembler.toEntity(resource);
    if(entity != null && entity.getProjectGuid() != null) {
      entity.setProjectGuid(projectEntity.getProjectGuid());

      ProjectBoundaryEntity savedEntity = projectBoundaryRepository.save(entity);

      updateProjectCoordinates(savedEntity);

      return projectBoundaryResourceAssembler.toModel(savedEntity);
    } else throw new IllegalArgumentException("ProjectBoundaryModel resource to be created cannot be null");
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

      ProjectBoundaryEntity entity = projectBoundaryResourceAssembler.updateEntity(resource, existingEntity);
      return saveProjectBoundary(entity);
    } else throw new IllegalArgumentException("ProjectBoundaryModel resource to be updated cannot be null");
  }

  public ProjectBoundaryModel saveProjectBoundary(ProjectBoundaryEntity entity) {
    try {
      ProjectBoundaryEntity savedEntity = projectBoundaryRepository.saveAndFlush(entity);

      updateProjectCoordinates(savedEntity);

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

  public void updateProjectCoordinates(ProjectBoundaryEntity boundary) {
    ProjectModel project = projectService.getProjectById(String.valueOf(boundary.getProjectGuid()));
    PGpolygon polygon = boundary.getBoundaryGeometry();

    if(project != null && boundary.getBoundaryGeometry() != null && !polygon.isNull()) {

      PGpoint centroid = calculateCentroid(polygon);

      BigDecimal latitude = BigDecimal.valueOf(centroid.y);
      BigDecimal longitude = BigDecimal.valueOf(centroid.x);

      project.setLatitude(latitude.scale() > 7 ? latitude.setScale(7, RoundingMode.HALF_UP) : latitude);
      project.setLongitude(longitude.scale() > 7 ? longitude.setScale(7, RoundingMode.HALF_UP) : longitude);
      projectService.updateProject(project);
    } else {
      throw new EntityNotFoundException("Project could not be found while attempting to update coordinates");
    }
  }

  public PGpoint calculateCentroid(PGpolygon polygon) {
      // Extract points from the polygon
      PGpoint[] points = polygon.points;

      // Initialize variables for centroid calculation
      double totalArea = 0;
      double centroidX = 0;
      double centroidY = 0;

      // For simple centroid calculation
      if (points.length <= 2) {
        // Just return the first point if there aren't enough points for a proper polygon
        return points[0];
      }

      // Calculate centroid using the weighted average method
      for (int i = 0; i < points.length; i++) {
        int j = (i + 1) % points.length;
        double crossProduct = points[i].x * points[j].y - points[j].x * points[i].y;

        totalArea += crossProduct;
        centroidX += (points[i].x + points[j].x) * crossProduct;
        centroidY += (points[i].y + points[j].y) * crossProduct;
      }

      // Finalize the centroid calculation
      totalArea /= 2.0;
      centroidX /= (6.0 * totalArea);
      centroidY /= (6.0 * totalArea);

      // Handle the sign of the area (clockwise vs counterclockwise)
      if (totalArea < 0) {
        centroidX = -centroidX;
        centroidY = -centroidY;
      }

      return new PGpoint(centroidX, centroidY);

  }
}