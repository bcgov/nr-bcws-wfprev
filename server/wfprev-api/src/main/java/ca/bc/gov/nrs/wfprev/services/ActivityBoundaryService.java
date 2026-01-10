package ca.bc.gov.nrs.wfprev.services;

import ca.bc.gov.nrs.wfone.common.service.api.ServiceException;
import ca.bc.gov.nrs.wfprev.common.services.CommonService;
import ca.bc.gov.nrs.wfprev.data.assemblers.ActivityBoundaryResourceAssembler;
import ca.bc.gov.nrs.wfprev.data.entities.ActivityBoundaryEntity;
import ca.bc.gov.nrs.wfprev.data.entities.ActivityEntity;
import ca.bc.gov.nrs.wfprev.data.models.ActivityBoundaryModel;
import ca.bc.gov.nrs.wfprev.data.models.ActivityModel;
import ca.bc.gov.nrs.wfprev.data.repositories.ActivityBoundaryRepository;
import ca.bc.gov.nrs.wfprev.data.repositories.ActivityRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.hateoas.CollectionModel;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Component
public class ActivityBoundaryService implements CommonService {

    private static final String ACTIVITY_NOT_FOUND = "Activity not found";
    private static final String BOUNDARY_NOT_FOUND = "Activity Boundary not found";
    private static final String BOUNDARY = "Activity Boundary";
    private static final String DOES_NOT_BELONG_ACTIVITY = "does not belong to Activity";
    private static final String KEY_FORMAT = "{0}: {1}";
    private static final String EXTENDED_KEY_FORMAT = "{0}: {1} {2}: {3}";

    private final ActivityBoundaryRepository activityBoundaryRepository;
    private final ActivityBoundaryResourceAssembler activityBoundaryResourceAssembler;
    private final ActivityRepository activityRepository;
    private final ActivityService activityService;
    private final ProjectBoundaryService projectBoundaryService;
    private final Validator validator;

    public ActivityBoundaryService(
            ActivityBoundaryRepository activityBoundaryRepository,
            ActivityBoundaryResourceAssembler activityBoundaryResourceAssembler,
            ActivityRepository activityRepository,
            ActivityService activityService,
            ProjectBoundaryService projectBoundaryService,
            Validator validator) {
        this.activityBoundaryRepository = activityBoundaryRepository;
        this.activityBoundaryResourceAssembler = activityBoundaryResourceAssembler;
        this.activityRepository = activityRepository;
        this.activityService = activityService;
        this.projectBoundaryService = projectBoundaryService;
        this.validator = validator;
    }

    public CollectionModel<ActivityBoundaryModel> getAllActivityBoundaries(
            String projectGuid, String fiscalGuid, String activityGuid) throws ServiceException {
        try {
            // Verify activity exists and belongs to the correct hierarchy
            activityService.getActivity(projectGuid, fiscalGuid, activityGuid);

            // Find all boundaries for this activity
            List<ActivityBoundaryEntity> boundaries = activityBoundaryRepository
                    .findByActivityGuid(UUID.fromString(activityGuid));

            // Convert entities to models
            List<ActivityBoundaryModel> boundaryModels = boundaries.stream()
                    .map(activityBoundaryResourceAssembler::toModel)
                    .toList();

            return CollectionModel.of(boundaryModels);

        } catch (IllegalArgumentException e) {
            throw new ServiceException("Invalid GUID format", e);
        }
    }

    @Transactional
    public ActivityBoundaryModel createActivityBoundary(
            String projectGuid, String fiscalGuid, String activityGuid, ActivityBoundaryModel resource) {
        Set<ConstraintViolation<ActivityBoundaryModel>> violations = validator.validate(resource);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }

        // Verify activity exists and belongs to the correct hierarchy
        activityService.getActivity(projectGuid, fiscalGuid, activityGuid);

        initializeNewActivityBoundary(resource, activityGuid);
        ActivityEntity activityEntity = activityRepository.findById(UUID.fromString(activityGuid))
                .orElseThrow(() -> new EntityNotFoundException(MessageFormat.format(KEY_FORMAT, ACTIVITY_NOT_FOUND, activityGuid)));

        ActivityBoundaryEntity entity = activityBoundaryResourceAssembler.toEntity(resource);
        if(entity != null && entity.getActivityGuid() != null) {
            entity.setActivityGuid(activityEntity.getActivityGuid());
            entity.setBoundarySizeHa(projectBoundaryService.convertMultiPolygonAreaToHectares(entity.getGeometry()));
            ActivityBoundaryEntity savedEntity = activityBoundaryRepository.save(entity);
            return activityBoundaryResourceAssembler.toModel(savedEntity);
        } else throw new IllegalArgumentException("ActivityBoundaryModel resource to be created cannot be null");
    }

    private void initializeNewActivityBoundary(ActivityBoundaryModel resource, String activityGuid) {
        resource.setActivityBoundaryGuid(UUID.randomUUID().toString());
        resource.setActivityGuid(activityGuid);
        resource.setCreateDate(new Date());
        resource.setRevisionCount(0);
    }

    @Transactional
    public ActivityBoundaryModel updateActivityBoundary(
            String projectGuid, String fiscalGuid, String activityGuid, ActivityBoundaryModel resource) {
        Set<ConstraintViolation<ActivityBoundaryModel>> violations = validator.validate(resource);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }

        // Verify activity exists and belongs to the correct hierarchy
        activityService.getActivity(projectGuid, fiscalGuid, activityGuid);

        // Verify boundary exists
        if(resource != null && resource.getActivityGuid() != null) {
            UUID boundaryGuid = UUID.fromString(resource.getActivityBoundaryGuid());
            ActivityBoundaryEntity existingEntity = activityBoundaryRepository.findByActivityBoundaryGuid(boundaryGuid)
                    .orElseThrow(() -> new EntityNotFoundException(MessageFormat.format(KEY_FORMAT, BOUNDARY_NOT_FOUND, resource.getActivityBoundaryGuid())));

            // Verify boundary belongs to the specified activity
            if (!existingEntity.getActivityGuid().toString().equals(activityGuid)) {
                throw new EntityNotFoundException(MessageFormat.format(EXTENDED_KEY_FORMAT, BOUNDARY, boundaryGuid, DOES_NOT_BELONG_ACTIVITY, activityGuid));
            }

            if(resource.getGeometry()!= null && !resource.getGeometry().isEmpty()) {
                resource.setBoundarySizeHa(projectBoundaryService.convertMultiPolygonAreaToHectares(resource.getGeometry()));
            }

            ActivityBoundaryEntity entity = activityBoundaryResourceAssembler.updateEntity(resource, existingEntity);
            return saveActivityBoundary(entity);
        } else throw new IllegalArgumentException("ActivityBoundaryModel resource to be updated cannot be null");
    }

    public ActivityBoundaryModel saveActivityBoundary(ActivityBoundaryEntity entity) {
        try {
            ActivityBoundaryEntity savedEntity = activityBoundaryRepository.saveAndFlush(entity);
            return activityBoundaryResourceAssembler.toModel(savedEntity);
        } catch (IllegalArgumentException e) {
            log.error("IllegalArgumentException for Activity Boundary: {}", e.getMessage(), e);
            throw e;
        } catch (EntityNotFoundException e) {
            log.error("EntityNotFoundException for Activity Boundary: {}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("Exception for Activity Boundary: {}", e.getMessage(), e);
            throw e;
        }
    }

    public ActivityBoundaryModel getActivityBoundary(
            String projectGuid, String fiscalGuid, String activityGuid, String boundaryGuid) {
        // Verify activity exists and belongs to the correct hierarchy
        activityService.getActivity(projectGuid, fiscalGuid, activityGuid);

        // Get the boundary
        ActivityBoundaryEntity boundary = activityBoundaryRepository.findByActivityBoundaryGuid(UUID.fromString(boundaryGuid))
                .orElseThrow(() -> new EntityNotFoundException(MessageFormat.format(KEY_FORMAT, BOUNDARY_NOT_FOUND, boundaryGuid)));

        // Verify boundary belongs to the specified activity
        if (!boundary.getActivityGuid().toString().equals(activityGuid)) {
            throw new EntityNotFoundException(MessageFormat.format(EXTENDED_KEY_FORMAT, BOUNDARY, boundaryGuid, DOES_NOT_BELONG_ACTIVITY, activityGuid));
        }

        return activityBoundaryResourceAssembler.toModel(boundary);
    }

    @Transactional
    public void deleteActivityBoundary(
            String projectGuid, String fiscalGuid, String activityGuid, String boundaryGuid) {
        // Verify activity exists and belongs to the correct hierarchy
        activityService.getActivity(projectGuid, fiscalGuid, activityGuid);

        // Get the boundary
        ActivityBoundaryEntity boundary = activityBoundaryRepository.findByActivityBoundaryGuid(UUID.fromString(boundaryGuid))
                .orElseThrow(() -> new EntityNotFoundException(MessageFormat.format(KEY_FORMAT, BOUNDARY_NOT_FOUND, boundaryGuid)));

        // Verify boundary belongs to the specified activity
        if (!boundary.getActivityGuid().toString().equals(activityGuid)) {
            throw new EntityNotFoundException(MessageFormat.format(EXTENDED_KEY_FORMAT, BOUNDARY, boundaryGuid, DOES_NOT_BELONG_ACTIVITY, activityGuid));
        }

        activityBoundaryRepository.deleteByActivityBoundaryGuid(UUID.fromString(boundaryGuid));
    }
    
    @Transactional
    public void deleteActivityBoundaries(String activityGuid) {
        activityBoundaryRepository.deleteByActivityGuid(UUID.fromString(activityGuid));
    }
}