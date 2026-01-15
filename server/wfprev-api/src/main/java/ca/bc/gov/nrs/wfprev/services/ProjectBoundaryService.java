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
import java.util.Optional;
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

  private final FileAttachmentService fileAttachmentService;
  private final Validator validator;

  public ProjectBoundaryService(
          ProjectBoundaryRepository projectBoundaryRepository,
          ProjectBoundaryResourceAssembler projectBoundaryResourceAssembler,
          ProjectRepository projectRepository,
          FileAttachmentService fileAttachmentService,
          Validator validator) {
    this.projectBoundaryRepository = projectBoundaryRepository;
    this.projectBoundaryResourceAssembler = projectBoundaryResourceAssembler;
    this.projectRepository = projectRepository;
    this.fileAttachmentService = fileAttachmentService;
    this.validator = validator;
  }

  public CollectionModel<ProjectBoundaryModel> getAllProjectBoundaries(String projectGuid) throws ServiceException {
    try {
      // Verify project exists and belongs to the correct hierarchy
      if (!projectRepository.existsById(UUID.fromString(projectGuid))) {
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

    updateFieldsFromBoundaryGeometry(resource);

    resource.setProjectBoundaryGuid(UUID.randomUUID().toString());

    ProjectBoundaryEntity entity = projectBoundaryResourceAssembler.toEntity(resource);
    if(entity != null && entity.getProjectGuid() != null) {
      entity.setProjectGuid(projectEntity.getProjectGuid());

      ProjectBoundaryEntity savedEntity = projectBoundaryRepository.save(entity);

      if (savedEntity.getProjectGuid() != null && savedEntity.getBoundarySizeHa() != null) {
        updateProjectActualSizeHa(savedEntity.getProjectGuid(), savedEntity.getBoundarySizeHa());
      }

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

      updateFieldsFromBoundaryGeometry(resource);
      ProjectBoundaryEntity entity = projectBoundaryResourceAssembler.updateEntity(resource, existingEntity);

      if(entity.getProjectGuid() != null && entity.getBoundarySizeHa() != null) {
        updateProjectActualSizeHa(entity.getProjectGuid(), entity.getBoundarySizeHa());
      }

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
    if (!projectRepository.existsById(UUID.fromString(projectGuid))) {
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
          String projectGuid, String boundaryGuid, boolean deleteFiles) {
    // Verify project exists and belongs to the correct hierarchy
    if (!projectRepository.existsById(UUID.fromString(projectGuid))) {
      throw new EntityNotFoundException(MessageFormat.format(KEY_FORMAT, PROJECT_NOT_FOUND, projectGuid));
    }

    // Get the boundary
    ProjectBoundaryEntity boundary = projectBoundaryRepository.findByProjectBoundaryGuid(UUID.fromString(boundaryGuid))
            .orElseThrow(() -> new EntityNotFoundException(MessageFormat.format(KEY_FORMAT, BOUNDARY_NOT_FOUND, boundaryGuid)));

    // Verify boundary belongs to the specified project
    if (!boundary.getProjectGuid().toString().equals(projectGuid)) {
      throw new EntityNotFoundException(MessageFormat.format(EXTENDED_KEY_FORMAT, BOUNDARY, boundaryGuid, DOES_NOT_BELONG_PROJECT, projectGuid));
    }

    if (deleteFiles) {
      fileAttachmentService.deleteAttachmentsBySourceObject(boundaryGuid);
    }
    projectBoundaryRepository.deleteByProjectBoundaryGuid(UUID.fromString(boundaryGuid));
  }

  private ProjectBoundaryModel updateFieldsFromBoundaryGeometry(ProjectBoundaryModel model) {
    if(model.getBoundaryGeometry() != null) {
      model.setBoundarySizeHa(convertMultiPolygonAreaToHectares(model.getBoundaryGeometry()));
      model.setLocationGeometry(model.getBoundaryGeometry().getCentroid());
    } return model;
  }

  /**
   * Converts the area of a {@link MultiPolygon} from degrees to hectares using a geodetically accurate method
   * based on the WGS84 ellipsoidal Earth model.
   * <p>
   * For each polygon in the MultiPolygon, the method:
   * <ul>
   *   <li>Calculates the centroid latitude</li>
   *   <li>Computes the meridian and prime vertical radii of curvature</li>
   *   <li>Estimates the size of one square degree at that latitude</li>
   *   <li>Converts the polygon's area from degrees to square meters, then to hectares</li>
   * </ul>
   * This approach improves accuracy compared to spherical approximations, particularly for large or high-latitude geometries.
   *
   * @param multiPolygon the JTS MultiPolygon geometry to convert
   * @return the total area in hectares, rounded to 4 decimal places
   */
  public BigDecimal convertMultiPolygonAreaToHectares(MultiPolygon multiPolygon) {
    double totalAreaHectares = 0.0;

    // Process each polygon in the MultiPolygon separately
    for (int i = 0; i < multiPolygon.getNumGeometries(); i++) {
      Polygon polygon = (Polygon) multiPolygon.getGeometryN(i);

      // Get centroid latitude for this polygon
      double latitude = polygon.getCentroid().getY();
      double latRad = Math.toRadians(latitude);

      // WGS84 ellipsoid parameters
      double semiMajorAxis = 6378137.0; // meters, WGS84 semi-major axis
      double semiMinorAxis = 6356752.314245; // meters, WGS84 semi-minor axis
      double eccentricitySquared = 1 - ((semiMinorAxis * semiMinorAxis) / (semiMajorAxis * semiMajorAxis));

      // Calculate the meridian radius of curvature
      double numerator = semiMajorAxis * (1 - eccentricitySquared);
      double denominatorFactor = 1 - eccentricitySquared * Math.sin(latRad) * Math.sin(latRad);
      double denominator = Math.pow(denominatorFactor, 1.5);
      double meridianRadius = numerator / denominator;

      // Calculate the prime vertical radius of curvature
      double primeVerticalRadius = semiMajorAxis / Math.sqrt(denominatorFactor);

      // Length of 1 degree in meters at this latitude
      double metersPerLatDegree = (Math.PI / 180.0) * meridianRadius;
      double metersPerLonDegree = (Math.PI / 180.0) * primeVerticalRadius * Math.cos(latRad);

      // Area of 1 square degree in square meters at this latitude
      double squareMetersPerSquareDegree = metersPerLatDegree * metersPerLonDegree;

      // Convert this polygon's area to hectares using WGS84 parameters
      double polygonAreaSqDegrees = polygon.getArea();
      double polygonAreaSqMeters = polygonAreaSqDegrees * squareMetersPerSquareDegree;
      double polygonAreaHectares = polygonAreaSqMeters / 10000.0;

      totalAreaHectares += polygonAreaHectares;
    }

    // Round to 4 decimal places
    return BigDecimal.valueOf(totalAreaHectares)
            .setScale(4, RoundingMode.HALF_UP);
  }

  private void updateProjectActualSizeHa(UUID projectGuid, BigDecimal boundarySizeHa) {
    ProjectEntity project = projectRepository.findById(projectGuid)
            .orElseThrow(() -> new EntityNotFoundException("Project not found: " + projectGuid));

    project.setTotalActualProjectSizeHa(boundarySizeHa);
    projectRepository.save(project);
  }

  @Transactional
  public void deleteProjectBoundaries(String projectGuid, boolean deleteFiles) {
      List<ProjectBoundaryEntity> boundaries = projectBoundaryRepository.findByProjectGuid(UUID.fromString(projectGuid));
      for (ProjectBoundaryEntity boundary : boundaries) {
          if (deleteFiles) {
              fileAttachmentService.deleteAttachmentsBySourceObject(boundary.getProjectBoundaryGuid().toString());
          }
      }
      projectBoundaryRepository.deleteByProjectGuid(UUID.fromString(projectGuid));
  }
}