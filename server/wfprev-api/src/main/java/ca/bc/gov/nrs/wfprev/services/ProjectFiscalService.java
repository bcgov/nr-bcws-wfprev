package ca.bc.gov.nrs.wfprev.services;

import ca.bc.gov.nrs.wfone.common.service.api.ServiceException;
import ca.bc.gov.nrs.wfprev.common.services.CommonService;
import ca.bc.gov.nrs.wfprev.data.assemblers.ProjectFiscalResourceAssembler;
import ca.bc.gov.nrs.wfprev.data.assemblers.ProjectResourceAssembler;
import ca.bc.gov.nrs.wfprev.data.entities.EndorsementCodeEntity;
import ca.bc.gov.nrs.wfprev.data.entities.ForestAreaCodeEntity;
import ca.bc.gov.nrs.wfprev.data.entities.PlanFiscalStatusCodeEntity;
import ca.bc.gov.nrs.wfprev.data.entities.ProjectEntity;
import ca.bc.gov.nrs.wfprev.data.entities.ProjectFiscalEntity;
import ca.bc.gov.nrs.wfprev.data.entities.ProjectStatusCodeEntity;
import ca.bc.gov.nrs.wfprev.data.models.ProjectFiscalModel;
import ca.bc.gov.nrs.wfprev.data.models.ProjectModel;
import ca.bc.gov.nrs.wfprev.data.repositories.EndorsementCodeRepository;
import ca.bc.gov.nrs.wfprev.data.repositories.PlanFiscalStatusCodeRepository;
import ca.bc.gov.nrs.wfprev.data.repositories.ProjectFiscalRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.hateoas.CollectionModel;
import org.springframework.stereotype.Component;

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
    private final ProjectService projectService;
    private final ProjectResourceAssembler projectResourceAssembler;
    private final PlanFiscalStatusCodeRepository planFiscalStatusCodeRepository;
    private final EndorsementCodeRepository endorsementCodeRepository;

    private static final String DRAFT = "DRAFT";
    private static final String PROPOSED = "PROPOSED";
    private static final String PREPARED = "PREPARED";
    private static final String IN_PROG = "IN_PROG";
    private static final String COMPLETE = "COMPLETE";
    private static final String CANCELLED = "CANCELLED";
    private static final String ENDORSED = "ENDORSED";

    private static final Map<String, Set<String>> VALID_TRANSITIONS = Map.of(
            DRAFT, Set.of(PROPOSED, CANCELLED),
            PROPOSED, Set.of(DRAFT, PREPARED, CANCELLED),
            PREPARED, Set.of(DRAFT, IN_PROG, CANCELLED),
            IN_PROG, Set.of(COMPLETE, CANCELLED),
            COMPLETE, Set.of(),
            CANCELLED, Set.of()
    );

    private void validateStatusTransition(String currentStatus, String newStatus) {
        if (currentStatus == null || newStatus == null || currentStatus.equals(newStatus)) return;

        Set<String> allowedTransitions = VALID_TRANSITIONS.getOrDefault(currentStatus, Set.of());

        if (!allowedTransitions.contains(newStatus)) {
            throw new IllegalStateException("Invalid fiscal status transition from " + currentStatus + " to " + newStatus);
        }
    }

    public ProjectFiscalService(ProjectFiscalRepository projectFiscalRepository, ProjectFiscalResourceAssembler projectFiscalResourceAssembler,
                                ProjectService projectService, ProjectResourceAssembler projectResourceAssembler, PlanFiscalStatusCodeRepository planFiscalStatusCodeRepository,
                                EndorsementCodeRepository endorsementCodeRepository) {
        this.projectFiscalRepository = projectFiscalRepository;
        this.projectFiscalResourceAssembler = projectFiscalResourceAssembler;
        this.projectService = projectService;
        this.projectResourceAssembler = projectResourceAssembler;
        this.planFiscalStatusCodeRepository = planFiscalStatusCodeRepository;
        this.endorsementCodeRepository = endorsementCodeRepository;
    }

    public CollectionModel<ProjectFiscalModel> getAllProjectFiscals(String projectId) throws ServiceException {
        UUID projectGuid = UUID.fromString(projectId);
        List<ProjectFiscalEntity> projectFiscals = projectFiscalRepository.findAllByProject_ProjectGuid(projectGuid);
        return projectFiscalResourceAssembler.toCollectionModel(projectFiscals);
    }

    public ProjectFiscalModel createProjectFiscal(ProjectFiscalModel projectFiscalModel) {
        initializeNewProjectFiscal(projectFiscalModel);
        ProjectModel projectById = projectService.getProjectById(projectFiscalModel.getProjectGuid());
        ProjectEntity projectEntity = projectResourceAssembler.toEntity(projectById);
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
                .orElseThrow(() -> new EntityNotFoundException("Project fiscal not found: " + projectFiscalModel.getProjectPlanFiscalGuid()));

        String currentStatus = existingEntity.getPlanFiscalStatusCode().getPlanFiscalStatusCode();
        String newStatus = projectFiscalModel.getPlanFiscalStatusCode().getPlanFiscalStatusCode();
        validateStatusTransition(currentStatus, newStatus);

        // only allow PROPOSED → PREPARED if endorsed and approved
        if (PROPOSED.equals(currentStatus) && PREPARED.equals(newStatus)) {
            boolean isApproved = Boolean.TRUE.equals(existingEntity.getIsApprovedInd());
            boolean isEndorsed = existingEntity.getEndorsementCode() != null &&
                    ENDORSED.equalsIgnoreCase(existingEntity.getEndorsementCode().getEndorsementCode());

            if (!isApproved || !isEndorsed) {
                throw new IllegalStateException("Cannot transition to PREPARED without both approval and endorsement.");
            }
        }

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

    public void deleteProjectFiscal(String uuid) {
        UUID guid = UUID.fromString(uuid);

        // Check if the entity exists, throw EntityNotFoundException if not
        projectFiscalRepository.findById(guid)
                .orElseThrow(() -> new EntityNotFoundException("Project Fiscal not found with ID: " + uuid));

        // Proceed with deletion
        projectFiscalRepository.deleteById(guid);
    }

    private void assignAssociatedEntities(ProjectFiscalModel resource, ProjectFiscalEntity entity) {
        if (resource.getPlanFiscalStatusCode() != null && resource.getPlanFiscalStatusCode().getPlanFiscalStatusCode() != null) {
            entity.setPlanFiscalStatusCode(loadPlanFiscalStatusCode(resource.getPlanFiscalStatusCode().getPlanFiscalStatusCode()));
        }

        if (resource.getEndorsementCode() != null && resource.getEndorsementCode().getEndorsementCode() != null) {
            entity.setEndorsementCode(loadEndorsementCode(resource.getEndorsementCode().getEndorsementCode()));
        }

    }

    private PlanFiscalStatusCodeEntity loadPlanFiscalStatusCode(String planFiscalStatusCode) {
        return planFiscalStatusCodeRepository
                .findById(planFiscalStatusCode)
                .orElseThrow(() -> new IllegalArgumentException("PlanFiscalStatusCode not found: " + planFiscalStatusCode));
    }

    private EndorsementCodeEntity loadEndorsementCode(String endorsementCode) {
        return endorsementCodeRepository
                .findById(endorsementCode)
                .orElseThrow(() -> new IllegalArgumentException("EndorsementCode not found: " + endorsementCode));
    }
}
