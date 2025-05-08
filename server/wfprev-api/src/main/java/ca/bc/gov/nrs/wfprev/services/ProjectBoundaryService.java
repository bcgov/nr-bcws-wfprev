package ca.bc.gov.nrs.wfprev.services;

import ca.bc.gov.nrs.wfone.common.service.api.ServiceException;
import ca.bc.gov.nrs.wfprev.common.services.CommonService;
import ca.bc.gov.nrs.wfprev.data.assemblers.ProjectBoundaryResourceAssembler;
import ca.bc.gov.nrs.wfprev.data.entities.ProjectBoundaryEntity;
import ca.bc.gov.nrs.wfprev.data.entities.ProjectEntity;
import ca.bc.gov.nrs.wfprev.data.models.ProjectBoundaryModel;
import ca.bc.gov.nrs.wfprev.data.repositories.ProjectBoundaryRepository;
import ca.bc.gov.nrs.wfprev.data.repositories.ProjectRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;
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
      model.setBoundarySizeHa(convertMultiPolygonAreaToHectares(model.getBoundaryGeometry()));
      model.setLocationGeometry(model.getBoundaryGeometry().getCentroid());
    } return model;
  }

  public BigDecimal convertMultiPolygonAreaToHectares(MultiPolygon multiPolygon) {
    double totalAreaHectares = 0.0;

    // Process each polygon in the MultiPolygon separately
    for (int i = 0; i < multiPolygon.getNumGeometries(); i++) {
      Polygon polygon = (Polygon) multiPolygon.getGeometryN(i);

      // Get centroid of this specific polygon for more accurate conversion
      double latitude = polygon.getCentroid().getY();
      double latRad = Math.toRadians(latitude);

      // Earth's radius in meters
      double earthRadius = 6371000;

      // Length of 1 degree in meters at this latitude
      double metersPerLatDegree = Math.PI * earthRadius / 180.0;
      double metersPerLonDegree = metersPerLatDegree * Math.cos(latRad);

      // Area of 1 square degree in square meters at this latitude
      double squareMetersPerSquareDegree = metersPerLatDegree * metersPerLonDegree;

      // Convert this polygon's area to hectares
      double polygonAreaSqDegrees = polygon.getArea();
      double polygonAreaSqMeters = polygonAreaSqDegrees * squareMetersPerSquareDegree;
      double polygonAreaHectares = polygonAreaSqMeters / 10000.0;

      totalAreaHectares += polygonAreaHectares;
    }

    // Round to 4 decimal places
    return BigDecimal.valueOf(totalAreaHectares)
            .setScale(4, RoundingMode.HALF_UP);
  }
}