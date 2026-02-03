package ca.bc.gov.nrs.wfprev.services;

import ca.bc.gov.nrs.wfone.common.service.api.ServiceException;
import ca.bc.gov.nrs.wfprev.common.services.CommonService;
import ca.bc.gov.nrs.wfprev.data.assemblers.PerformanceUpdateResourceAssembler;
import ca.bc.gov.nrs.wfprev.data.assemblers.ProjectFiscalResourceAssembler;
import ca.bc.gov.nrs.wfprev.data.entities.EndorsementCodeEntity;
import ca.bc.gov.nrs.wfprev.data.entities.PlanFiscalStatusCodeEntity;
import ca.bc.gov.nrs.wfprev.data.entities.ProjectEntity;
import ca.bc.gov.nrs.wfprev.data.entities.ProjectFiscalEntity;
import ca.bc.gov.nrs.wfprev.data.entities.ProjectPlanFiscalPerfEntity;
import ca.bc.gov.nrs.wfprev.data.models.PerformanceUpdateModel;
import ca.bc.gov.nrs.wfprev.data.models.ProjectFiscalModel;
import ca.bc.gov.nrs.wfprev.data.repositories.EndorsementCodeRepository;
import ca.bc.gov.nrs.wfprev.data.repositories.PlanFiscalStatusCodeRepository;
import ca.bc.gov.nrs.wfprev.data.repositories.ProjectFiscalRepository;
import ca.bc.gov.nrs.wfprev.data.repositories.ProjectRepository;
import ca.bc.gov.nrs.wfprev.data.repositories.FuelManagementPlanRepository;
import ca.bc.gov.nrs.wfprev.data.repositories.CulturalRxFirePlanRepository;
import ca.bc.gov.nrs.wfprev.data.repositories.ProjectPlanFiscalPerfRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.hateoas.CollectionModel;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Component
public class ProjectFiscalService implements CommonService {

    private final ProjectFiscalRepository projectFiscalRepository;
    private final ProjectFiscalResourceAssembler projectFiscalResourceAssembler;
    private final ProjectRepository projectRepository;
    private final PlanFiscalStatusCodeRepository planFiscalStatusCodeRepository;
    private final EndorsementCodeRepository endorsementCodeRepository;
    private final ActivityService activityService;
    private final FuelManagementPlanRepository fuelManagementPlanRepository;
    private final CulturalRxFirePlanRepository culturalRxFirePlanRepository;
    private final ProjectPlanFiscalPerfRepository projectPlanFiscalPerfRepository;
    private final PerformanceUpdateResourceAssembler performanceUpdateResourceAssembler;

    private static final String DRAFT = "DRAFT";
    private static final String PROPOSED = "PROPOSED";
    private static final String PREPARED = "PREPARED";
    private static final String IN_PROG = "IN_PROG";
    private static final String COMPLETE = "COMPLETE";
    private static final String CANCELLED = "CANCELLED";
    private static final String ENDORSED = "ENDORSED";

    private static final Map<String, Set<String>> VALID_TRANSITIONS = Map.of(
            DRAFT, Set.of(DRAFT, PROPOSED, PREPARED, CANCELLED),
            PROPOSED, Set.of(DRAFT, PROPOSED, PREPARED, CANCELLED),
            PREPARED, Set.of(DRAFT, PROPOSED, PREPARED, IN_PROG, CANCELLED),
            IN_PROG, Set.of(DRAFT, PROPOSED, COMPLETE, CANCELLED, IN_PROG),
            COMPLETE, Set.of(COMPLETE),
            CANCELLED, Set.of(CANCELLED));

    public ProjectFiscalService(ProjectFiscalRepository projectFiscalRepository,
            ProjectFiscalResourceAssembler projectFiscalResourceAssembler,
            ProjectRepository projectRepository, PlanFiscalStatusCodeRepository planFiscalStatusCodeRepository,
            EndorsementCodeRepository endorsementCodeRepository, ActivityService activityService,
            FuelManagementPlanRepository fuelManagementPlanRepository,
            CulturalRxFirePlanRepository culturalRxFirePlanRepository,
            ProjectPlanFiscalPerfRepository projectPlanFiscalPerfRepository,
            PerformanceUpdateResourceAssembler performanceUpdateResourceAssembler) {
        this.projectFiscalRepository = projectFiscalRepository;
        this.projectFiscalResourceAssembler = projectFiscalResourceAssembler;
        this.projectRepository = projectRepository;
        this.planFiscalStatusCodeRepository = planFiscalStatusCodeRepository;
        this.endorsementCodeRepository = endorsementCodeRepository;
        this.activityService = activityService;
        this.fuelManagementPlanRepository = fuelManagementPlanRepository;
        this.culturalRxFirePlanRepository = culturalRxFirePlanRepository;
        this.projectPlanFiscalPerfRepository = projectPlanFiscalPerfRepository;
        this.performanceUpdateResourceAssembler = performanceUpdateResourceAssembler;
    }

    public CollectionModel<ProjectFiscalModel> getAllProjectFiscals(String projectId) throws ServiceException {
        UUID projectGuid = UUID.fromString(projectId);
        List<ProjectFiscalEntity> projectFiscals = projectFiscalRepository.findAllByProject_ProjectGuid(projectGuid);
        return projectFiscalResourceAssembler.toCollectionModel(projectFiscals);
    }

    public ProjectFiscalModel createProjectFiscal(ProjectFiscalModel projectFiscalModel) {
        initializeNewProjectFiscal(projectFiscalModel);
        UUID projectGuid = UUID.fromString(projectFiscalModel.getProjectGuid());
        ProjectEntity projectEntity = projectRepository.findById(projectGuid)
                .orElseThrow(
                        () -> new EntityNotFoundException("Project not found: " + projectFiscalModel.getProjectGuid()));

        ProjectFiscalEntity entity = projectFiscalResourceAssembler.toEntity(projectFiscalModel, projectEntity);
        assignAssociatedEntities(projectFiscalModel, entity);
        ProjectFiscalEntity savedEntity = projectFiscalRepository.save(entity);
        return projectFiscalResourceAssembler.toModel(savedEntity);
    }

    private void initializeNewProjectFiscal(ProjectFiscalModel resource) {
        resource.setProjectPlanFiscalGuid(UUID.randomUUID().toString());
        resource.setCreateDate(new Date());
        resource.setRevisionCount(0);
    }

    public ProjectFiscalModel updateProjectFiscal(ProjectFiscalModel projectFiscalModel) {
        UUID guid = UUID.fromString(projectFiscalModel.getProjectPlanFiscalGuid());
        ProjectFiscalEntity existingEntity = projectFiscalRepository.findById(guid)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Project fiscal not found: " + projectFiscalModel.getProjectPlanFiscalGuid()));

        String incomingStatus = projectFiscalModel.getPlanFiscalStatusCode().getPlanFiscalStatusCode();

        // Check if incoming status is PROPOSED and both approved & endorsed
        boolean isProposed = PROPOSED.equalsIgnoreCase(incomingStatus);
        boolean isApproved = Boolean.TRUE.equals(projectFiscalModel.getIsApprovedInd());
        boolean isEndorsed = projectFiscalModel.getEndorsementCode() != null &&
                ENDORSED.equalsIgnoreCase(projectFiscalModel.getEndorsementCode().getEndorsementCode());

        if (isProposed && isApproved && isEndorsed) {
            projectFiscalModel.getPlanFiscalStatusCode().setPlanFiscalStatusCode(PREPARED);
        }

        // Validate transition between existing and (potentially updated) new status
        validateStatusTransition(projectFiscalModel, existingEntity);

        ProjectFiscalEntity entity = projectFiscalResourceAssembler.updateEntity(projectFiscalModel, existingEntity);
        assignAssociatedEntities(projectFiscalModel, entity);
        return saveProjectFiscal(entity);
    }

    private ProjectFiscalModel saveProjectFiscal(ProjectFiscalEntity entity) {
        try {
            // Save the entity using the repository
            ProjectFiscalEntity savedEntity = projectFiscalRepository.saveAndFlush(entity);

            // Convert the saved entity back to the model
            return projectFiscalResourceAssembler.toModel(savedEntity);
        } catch (DataIntegrityViolationException | ConstraintViolationException e) {
            log.error("Data integrity or constraint violation: {}", e.getMessage(), e);
            throw e;
        } catch (EntityNotFoundException e) {
            log.error("Invalid reference data: {}", e.getMessage(), e);
            throw e;
        }
    }

    public ProjectFiscalModel getProjectFiscal(String uuid) {
        UUID guid = UUID.fromString(uuid);
        ProjectFiscalEntity entity = projectFiscalRepository.findById(guid)
                .orElseThrow(() -> new EntityNotFoundException("Project fiscal not found: " + uuid));
        return projectFiscalResourceAssembler.toModel(entity);
    }

    public void deleteProjectFiscal(String uuid, boolean deleteFiles) {
        UUID guid = UUID.fromString(uuid);

        // Check if the entity exists, throw EntityNotFoundException if not
        projectFiscalRepository.findById(guid)
                .orElseThrow(() -> new EntityNotFoundException("Project Fiscal not found with ID: " + uuid));

        // Proceed with deletion
        activityService.deleteActivities(guid.toString(), deleteFiles);
        fuelManagementPlanRepository.deleteByProjectFiscal_ProjectPlanFiscalGuid(guid);
        culturalRxFirePlanRepository.deleteByProjectFiscal_ProjectPlanFiscalGuid(guid);
        projectPlanFiscalPerfRepository.deleteByProjectFiscal_ProjectPlanFiscalGuid(guid);
        projectFiscalRepository.deleteById(guid);
    }

    @Transactional
    public void deleteProjectFiscals(String projectGuid, boolean deleteFiles) {
        List<ProjectFiscalEntity> fiscals = projectFiscalRepository
                .findAllByProject_ProjectGuid(UUID.fromString(projectGuid));
        for (ProjectFiscalEntity fiscal : fiscals) {
            UUID fiscalGuid = fiscal.getProjectPlanFiscalGuid();
            activityService.deleteActivities(fiscalGuid.toString(), deleteFiles);
            fuelManagementPlanRepository.deleteByProjectFiscal_ProjectPlanFiscalGuid(fiscalGuid);
            culturalRxFirePlanRepository.deleteByProjectFiscal_ProjectPlanFiscalGuid(fiscalGuid);
            projectPlanFiscalPerfRepository.deleteByProjectFiscal_ProjectPlanFiscalGuid(fiscalGuid);
            projectFiscalRepository.delete(fiscal);
        }
    }

    private void assignAssociatedEntities(ProjectFiscalModel resource, ProjectFiscalEntity entity) {
        if (resource.getPlanFiscalStatusCode() != null
                && resource.getPlanFiscalStatusCode().getPlanFiscalStatusCode() != null) {
            entity.setPlanFiscalStatusCode(
                    loadPlanFiscalStatusCode(resource.getPlanFiscalStatusCode().getPlanFiscalStatusCode()));
        }

        if (resource.getEndorsementCode() != null && resource.getEndorsementCode().getEndorsementCode() != null) {
            entity.setEndorsementCode(loadEndorsementCode(resource.getEndorsementCode().getEndorsementCode()));
        }

    }

    private PlanFiscalStatusCodeEntity loadPlanFiscalStatusCode(String planFiscalStatusCode) {
        return planFiscalStatusCodeRepository
                .findById(planFiscalStatusCode)
                .orElseThrow(
                        () -> new IllegalArgumentException("PlanFiscalStatusCode not found: " + planFiscalStatusCode));
    }

    private EndorsementCodeEntity loadEndorsementCode(String endorsementCode) {
        return endorsementCodeRepository
                .findById(endorsementCode)
                .orElseThrow(() -> new IllegalArgumentException("EndorsementCode not found: " + endorsementCode));
    }

    private void validateStatusTransition(ProjectFiscalModel model, ProjectFiscalEntity existingEntity) {
        String currentStatus = existingEntity.getPlanFiscalStatusCode().getPlanFiscalStatusCode();
        String newStatus = model.getPlanFiscalStatusCode().getPlanFiscalStatusCode();

        // Skip validation only if status is unchanged AND NOT PREPARED
        boolean isPrepared = PREPARED.equalsIgnoreCase(currentStatus) && PREPARED.equalsIgnoreCase(newStatus);
        if ((currentStatus == null || newStatus == null) || (currentStatus.equals(newStatus) && !isPrepared)) {
            return;
        }

        // Validate allowed status transitions
        Set<String> allowedTransitions = VALID_TRANSITIONS.getOrDefault(currentStatus, Set.of());
        if (!allowedTransitions.contains(newStatus)) {
            throw new IllegalStateException(
                    "Invalid fiscal status transition from " + currentStatus + " to " + newStatus);
        }
    }

    public CollectionModel<PerformanceUpdateModel> getAllPerformanceUpdates(String uuid) {
        List<ProjectPlanFiscalPerfEntity> performanceUpdates = projectPlanFiscalPerfRepository
                .findAllByProjectFiscal_ProjectPlanFiscalGuid(UUID.fromString(uuid),
                        Sort.by(Sort.Direction.DESC, "submittedTimestamp"));
        return performanceUpdateResourceAssembler.toCollectionModel(performanceUpdates);
    }

    @Transactional
    public PerformanceUpdateModel createPerformanceUpdate(String id, PerformanceUpdateModel resource) {
        UUID projectPlanFiscalGuid = UUID.fromString(id);
        ProjectFiscalEntity projectFiscalEntity = projectFiscalRepository.findById(projectPlanFiscalGuid)
                .orElseThrow(() -> new EntityNotFoundException("Project Fiscal not found: " + projectPlanFiscalGuid));

        ProjectPlanFiscalPerfEntity entity = performanceUpdateResourceAssembler.toEntity(resource, projectFiscalEntity);

        entity.setSubmittedTimestamp(new Date());
        validate(entity);
        ProjectPlanFiscalPerfEntity savedEntity = projectPlanFiscalPerfRepository.save(entity);
        return performanceUpdateResourceAssembler.toModel(savedEntity);
    }

    private void validate(ProjectPlanFiscalPerfEntity entity) {
        BigDecimal sum = entity.getBudgetHighRiskAmount()
                .add(entity.getBudgetMediumRiskAmount())
                .add(entity.getBudgetLowRiskAmount())
                .add(entity.getBudgetCompletedAmount());

        BigDecimal expected = entity.getForecastAmount().compareTo(BigDecimal.ZERO) != 0
                ? entity.getForecastAmount()
                : entity.getPreviousForecastAmount();

        if (sum.compareTo(expected) != 0) {
            throw new ValidationException("Sum must equal forecast amount");
        }

    }
}
