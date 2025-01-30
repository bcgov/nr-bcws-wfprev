package ca.bc.gov.nrs.wfprev.services;

import ca.bc.gov.nrs.wfone.common.service.api.ServiceException;
import ca.bc.gov.nrs.wfprev.common.services.CommonService;
import ca.bc.gov.nrs.wfprev.data.assemblers.ActivityResourceAssembler;
import ca.bc.gov.nrs.wfprev.data.entities.ActivityEntity;
import ca.bc.gov.nrs.wfprev.data.entities.ActivityStatusCodeEntity;
import ca.bc.gov.nrs.wfprev.data.entities.ContractPhaseCodeEntity;
import ca.bc.gov.nrs.wfprev.data.entities.ProjectFiscalEntity;
import ca.bc.gov.nrs.wfprev.data.entities.RiskRatingCodeEntity;
import ca.bc.gov.nrs.wfprev.data.models.ActivityModel;
import ca.bc.gov.nrs.wfprev.data.models.ProjectFiscalModel;
import ca.bc.gov.nrs.wfprev.data.repositories.ActivityRepository;
import ca.bc.gov.nrs.wfprev.data.repositories.ActivityStatusCodeRepository;
import ca.bc.gov.nrs.wfprev.data.repositories.ContractPhaseCodeRepository;
import ca.bc.gov.nrs.wfprev.data.repositories.ProjectFiscalRepository;
import ca.bc.gov.nrs.wfprev.data.repositories.RiskRatingCodeRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.hateoas.CollectionModel;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
public class ActivityService implements CommonService {

    private static final String FISCAL_NOT_FOUND = "Project Fiscal not found";
    private static final String PROJECT_FISCAL = "Project Fiscal";
    private static final String DOES_NOT_BELONG_PROJECT = "does not belong to Project";
    private static final String DOES_NOT_BELONG_FISCAL = "does not belong to Project Fiscal";
    private static final String ACTIVITY_NOT_FOUND = "Activity not found";
    private static final String ACTIVITY = "Activity";
    private static final String KEY_FORMAT = "{0}: {1}";
    private static final String EXTENDED_KEY_FORMAT = "{0}: {1} {2}: {3}";

    private final ActivityRepository activityRepository;
    private final ActivityResourceAssembler activityResourceAssembler;
    private final ProjectFiscalRepository projectFiscalRepository;
    private final ProjectFiscalService projectFiscalService;
    private final ActivityStatusCodeRepository activityStatusCodeRepository;
    private final ContractPhaseCodeRepository contractPhaseCodeRepository;
    private final RiskRatingCodeRepository riskRatingCodeRepository;

    public ActivityService(
            ActivityRepository activityRepository,
            ActivityResourceAssembler activityResourceAssembler,
            ProjectFiscalRepository projectFiscalRepository,
            ProjectFiscalService projectFiscalService,
            ActivityStatusCodeRepository activityStatusCodeRepository,
            ContractPhaseCodeRepository contractPhaseCodeRepository,
            RiskRatingCodeRepository riskRatingCodeRepository) {
        this.activityRepository = activityRepository;
        this.activityResourceAssembler = activityResourceAssembler;
        this.projectFiscalRepository = projectFiscalRepository;
        this.projectFiscalService = projectFiscalService;
        this.activityStatusCodeRepository = activityStatusCodeRepository;
        this.contractPhaseCodeRepository = contractPhaseCodeRepository;
        this.riskRatingCodeRepository = riskRatingCodeRepository;
    }

    public CollectionModel<ActivityModel> getAllActivities(String projectGuid, String fiscalGuid) throws ServiceException {
        try {
            // Verify project fiscal exists and belongs to project
            ProjectFiscalEntity projectFiscal = projectFiscalRepository.findById(UUID.fromString(fiscalGuid))
                    .orElseThrow(() -> new EntityNotFoundException(MessageFormat.format(KEY_FORMAT, FISCAL_NOT_FOUND, fiscalGuid)));

            if (!projectFiscal.getProject().getProjectGuid().toString().equals(projectGuid)) {
                throw new EntityNotFoundException(MessageFormat.format(EXTENDED_KEY_FORMAT, PROJECT_FISCAL, fiscalGuid, DOES_NOT_BELONG_PROJECT, projectGuid));
            }

            // Find all activities for this project fiscal
            List<ActivityEntity> activities = activityRepository.findByProjectPlanFiscalGuid(UUID.fromString(fiscalGuid));

            // Convert entities to models
            List<ActivityModel> activityModels = activities.stream()
                    .map(activityResourceAssembler::toModel)
                    .toList();

            return CollectionModel.of(activityModels);

        } catch (IllegalArgumentException e) {
            throw new ServiceException("Invalid GUID format", e);
        }
    }

    @Transactional
    public ActivityModel createActivity(String projectGuid, String fiscalGuid, ActivityModel resource) {
        // Verify project fiscal exists and belongs to project
        ProjectFiscalModel projectFiscal = projectFiscalService.getProjectFiscal(fiscalGuid);
        if (!projectFiscal.getProjectGuid().equals(projectGuid)) {
            throw new EntityNotFoundException(MessageFormat.format(EXTENDED_KEY_FORMAT,PROJECT_FISCAL, fiscalGuid, DOES_NOT_BELONG_PROJECT, projectGuid));
        }

        initializeNewActivity(resource, fiscalGuid);
        ProjectFiscalEntity projectFiscalEntity = projectFiscalRepository.findById(UUID.fromString(fiscalGuid))
                .orElseThrow(() -> new EntityNotFoundException(MessageFormat.format(KEY_FORMAT,FISCAL_NOT_FOUND, fiscalGuid)));

        ActivityEntity entity = activityResourceAssembler.toEntity(resource);
        entity.setProjectPlanFiscalGuid(projectFiscalEntity.getProjectPlanFiscalGuid());

        assignAssociatedEntities(resource, entity);

        ActivityEntity savedEntity = activityRepository.save(entity);
        return activityResourceAssembler.toModel(savedEntity);
    }

    private void initializeNewActivity(ActivityModel resource, String fiscalGuid) {
        resource.setActivityGuid(UUID.randomUUID().toString());
        resource.setProjectPlanFiscalGuid(fiscalGuid);
        resource.setCreateDate(new Date());
        resource.setRevisionCount(0);
    }

    @Transactional
    public ActivityModel updateActivity(String projectGuid, String fiscalGuid, ActivityModel resource) {
        // Verify activity exists
        UUID activityGuid = UUID.fromString(resource.getActivityGuid());
        ActivityEntity existingEntity = (ActivityEntity) activityRepository.findById(activityGuid)
                .orElseThrow(() -> new EntityNotFoundException(MessageFormat.format(KEY_FORMAT, ACTIVITY_NOT_FOUND, resource.getActivityGuid())));

        // Verify activity belongs to the specified project fiscal
        if (!existingEntity.getProjectPlanFiscalGuid().toString().equals(fiscalGuid)) {
            throw new EntityNotFoundException(MessageFormat.format(EXTENDED_KEY_FORMAT,ACTIVITY, activityGuid, DOES_NOT_BELONG_FISCAL, fiscalGuid));
        }

        // Verify project fiscal belongs to project
        ProjectFiscalEntity projectFiscal = projectFiscalRepository.findById(UUID.fromString(fiscalGuid))
                .orElseThrow(() -> new EntityNotFoundException(MessageFormat.format(KEY_FORMAT,FISCAL_NOT_FOUND, fiscalGuid)));
        if (!projectFiscal.getProject().getProjectGuid().toString().equals(projectGuid)) {
            throw new EntityNotFoundException(MessageFormat.format(EXTENDED_KEY_FORMAT,PROJECT_FISCAL, fiscalGuid, DOES_NOT_BELONG_PROJECT, projectGuid));
        }

        ActivityEntity entity = activityResourceAssembler.updateEntity(resource, existingEntity);
        assignAssociatedEntities(resource, entity);
        return saveActivity(entity);
    }

    private ActivityModel saveActivity(ActivityEntity entity) {
        try {
            ActivityEntity savedEntity = activityRepository.saveAndFlush(entity);
            return activityResourceAssembler.toModel(savedEntity);
        } catch (DataIntegrityViolationException | ConstraintViolationException e) {
            log.error("Data integrity or constraint violation: {}", e.getMessage(), e);
            throw e;
        } catch (EntityNotFoundException e) {
            log.error("Invalid reference data: {}", e.getMessage(), e);
            throw e;
        }
    }

    public ActivityModel getActivity(String projectGuid, String fiscalGuid, String activityGuid) {
        // Get the activity
        ActivityEntity activity = activityRepository.findById(UUID.fromString(activityGuid))
                .orElseThrow(() -> new EntityNotFoundException(MessageFormat.format(KEY_FORMAT,ACTIVITY_NOT_FOUND, activityGuid)));

        // Verify activity belongs to the specified project fiscal
        if (!activity.getProjectPlanFiscalGuid().toString().equals(fiscalGuid)) {
            throw new EntityNotFoundException(MessageFormat.format(EXTENDED_KEY_FORMAT,ACTIVITY, activityGuid, DOES_NOT_BELONG_FISCAL, fiscalGuid));
        }

        // Verify project fiscal belongs to project
        ProjectFiscalEntity projectFiscal = projectFiscalRepository.findById(UUID.fromString(fiscalGuid))
                .orElseThrow(() -> new EntityNotFoundException(MessageFormat.format(KEY_FORMAT, FISCAL_NOT_FOUND, fiscalGuid)));
        if (!projectFiscal.getProject().getProjectGuid().toString().equals(projectGuid)) {
            throw new EntityNotFoundException(MessageFormat.format(EXTENDED_KEY_FORMAT, PROJECT_FISCAL, fiscalGuid, DOES_NOT_BELONG_PROJECT, projectGuid));
        }

        return activityResourceAssembler.toModel(activity);
    }

    public void deleteActivity(String projectGuid, String fiscalGuid, String activityGuid) {
        // Get the activity
        ActivityEntity activity = activityRepository.findById(UUID.fromString(activityGuid))
                .orElseThrow(() -> new EntityNotFoundException(MessageFormat.format(KEY_FORMAT, ACTIVITY_NOT_FOUND, activityGuid)));

        // Verify activity belongs to the specified project fiscal
        if (!activity.getProjectPlanFiscalGuid().toString().equals(fiscalGuid)) {
            throw new EntityNotFoundException(MessageFormat.format(EXTENDED_KEY_FORMAT, ACTIVITY, activityGuid, DOES_NOT_BELONG_FISCAL, fiscalGuid));
        }

        // Verify project fiscal belongs to project
        ProjectFiscalEntity projectFiscal = projectFiscalRepository.findById(UUID.fromString(fiscalGuid))
                .orElseThrow(() -> new EntityNotFoundException(MessageFormat.format(KEY_FORMAT, FISCAL_NOT_FOUND, fiscalGuid)));
        if (!projectFiscal.getProject().getProjectGuid().toString().equals(projectGuid)) {
            throw new EntityNotFoundException(MessageFormat.format(EXTENDED_KEY_FORMAT, PROJECT_FISCAL, fiscalGuid, DOES_NOT_BELONG_PROJECT, projectGuid));
        }

        activityRepository.deleteById(UUID.fromString(activityGuid));
    }

    public void assignAssociatedEntities(ActivityModel resource, ActivityEntity entity) {
        if (resource.getActivityStatusCode() != null) {
            String activityStatusCode = resource.getActivityStatusCode().getActivityStatusCode();
            ActivityStatusCodeEntity activityStatusCodeEntity = activityStatusCodeRepository
                    .findById(activityStatusCode)
                    .orElseThrow(() -> new IllegalArgumentException("ActivityStatusCode not found: " + activityStatusCode));
            entity.setActivityStatusCode(activityStatusCodeEntity);
        }

        if(resource.getRiskRatingCode() != null) {
            String riskRatingCode = resource.getRiskRatingCode().getRiskRatingCode();
            RiskRatingCodeEntity riskRatingCodeEntity = riskRatingCodeRepository
                    .findById(riskRatingCode)
                    .orElseThrow(() -> new IllegalArgumentException("RiskRatingCode not found: " + riskRatingCode));
            entity.setRiskRatingCode(riskRatingCodeEntity);
        }

        if(resource.getContractPhaseCode() != null) {
            String contractPhaseCode = resource.getContractPhaseCode().getContractPhaseCode();
            ContractPhaseCodeEntity contractPhaseCodeEntity = contractPhaseCodeRepository
                    .findById(contractPhaseCode)
                    .orElseThrow(() -> new IllegalArgumentException("ContractPhaseCode not found: " + contractPhaseCode));
            entity.setContractPhaseCode(contractPhaseCodeEntity);
        }
    }
}