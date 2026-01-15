package ca.bc.gov.nrs.wfprev.services;

import ca.bc.gov.nrs.wfone.common.service.api.ServiceException;
import ca.bc.gov.nrs.wfprev.common.services.CommonService;
import ca.bc.gov.nrs.wfprev.data.assemblers.ProjectResourceAssembler;
import ca.bc.gov.nrs.wfprev.data.entities.ForestAreaCodeEntity;
import ca.bc.gov.nrs.wfprev.data.entities.GeneralScopeCodeEntity;
import ca.bc.gov.nrs.wfprev.data.entities.ObjectiveTypeCodeEntity;
import ca.bc.gov.nrs.wfprev.data.entities.ProjectEntity;
import ca.bc.gov.nrs.wfprev.data.entities.ProjectStatusCodeEntity;
import ca.bc.gov.nrs.wfprev.data.entities.ProjectTypeCodeEntity;
import ca.bc.gov.nrs.wfprev.data.models.ProjectModel;
import ca.bc.gov.nrs.wfprev.data.models.ProjectStatusCodeModel;
import ca.bc.gov.nrs.wfprev.data.repositories.ForestAreaCodeRepository;
import ca.bc.gov.nrs.wfprev.data.repositories.GeneralScopeCodeRepository;
import ca.bc.gov.nrs.wfprev.data.repositories.ObjectiveTypeCodeRepository;
import ca.bc.gov.nrs.wfprev.data.repositories.ProjectRepository;
import ca.bc.gov.nrs.wfprev.data.repositories.ProjectStatusCodeRepository;
import ca.bc.gov.nrs.wfprev.data.repositories.ProjectTypeCodeRepository;

import ca.bc.gov.nrs.wfprev.data.entities.EvaluationCriteriaSummaryEntity;
import ca.bc.gov.nrs.wfprev.data.entities.EvaluationCriteriaSectionSummaryEntity;
import ca.bc.gov.nrs.wfprev.data.repositories.EvaluationCriteriaSectionSummaryRepository;
import ca.bc.gov.nrs.wfprev.data.repositories.EvaluationCriteriaSelectedRepository;
import ca.bc.gov.nrs.wfprev.data.repositories.EvaluationCriteriaSummaryRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.hateoas.CollectionModel;
import org.springframework.stereotype.Component;
import org.springframework.context.annotation.Lazy;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
public class ProjectService implements CommonService {

    private final ProjectRepository projectRepository;
    private final ProjectResourceAssembler projectResourceAssembler;
    private final ForestAreaCodeRepository forestAreaCodeRepository;
    private final ProjectTypeCodeRepository projectTypeCodeRepository;
    private final GeneralScopeCodeRepository generalScopeCodeRepository;
    private final ProjectStatusCodeRepository projectStatusCodeRepository;
    private final ObjectiveTypeCodeRepository objectiveTypeCodeRepository;
    private final ProjectBoundaryService projectBoundaryService;
    private final ProjectFiscalService projectFiscalService;
    private final EvaluationCriteriaSummaryRepository evaluationCriteriaSummaryRepository;
    private final EvaluationCriteriaSectionSummaryRepository evaluationCriteriaSectionSummaryRepository;
    private final EvaluationCriteriaSelectedRepository evaluationCriteriaSelectedRepository;


    public ProjectService(
            ProjectRepository projectRepository,
            ProjectResourceAssembler projectResourceAssembler,
            ForestAreaCodeRepository forestAreaCodeRepository,
            ProjectTypeCodeRepository projectTypeCodeRepository,
            GeneralScopeCodeRepository generalScopeCodeRepository,
            ProjectStatusCodeRepository projectStatusCodeRepository,
            ObjectiveTypeCodeRepository objectiveTypeCodeRepository,
            ProjectBoundaryService projectBoundaryService,
            @Lazy ProjectFiscalService projectFiscalService,
            EvaluationCriteriaSummaryRepository evaluationCriteriaSummaryRepository,
            EvaluationCriteriaSectionSummaryRepository evaluationCriteriaSectionSummaryRepository,
            EvaluationCriteriaSelectedRepository evaluationCriteriaSelectedRepository) {
        this.projectRepository = projectRepository;
        this.projectResourceAssembler = projectResourceAssembler;
        this.forestAreaCodeRepository = forestAreaCodeRepository;
        this.projectTypeCodeRepository = projectTypeCodeRepository;
        this.generalScopeCodeRepository = generalScopeCodeRepository;
        this.projectStatusCodeRepository = projectStatusCodeRepository;
        this.objectiveTypeCodeRepository = objectiveTypeCodeRepository;
        this.projectBoundaryService = projectBoundaryService;
        this.projectFiscalService = projectFiscalService;
        this.evaluationCriteriaSummaryRepository = evaluationCriteriaSummaryRepository;
        this.evaluationCriteriaSectionSummaryRepository = evaluationCriteriaSectionSummaryRepository;
        this.evaluationCriteriaSelectedRepository = evaluationCriteriaSelectedRepository;
    }

    public CollectionModel<ProjectModel> getAllProjects() throws ServiceException {
        try {
            List<ProjectEntity> all = projectRepository.findAll();
            return projectResourceAssembler.toCollectionModel(all);
        } catch (Exception e) {
            throw new ServiceException(e.getLocalizedMessage(), e);
        }
    }

    public ProjectModel getProjectById(String id) throws ServiceException {
        try {
            return projectRepository.findById(UUID.fromString(id))
                    .map(projectResourceAssembler::toModel)
                    .orElse(null);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid UUID: " + id, e);
        } catch (Exception e) {
            throw new ServiceException(e.getLocalizedMessage(), e);
        }
    }


    @Transactional
    public ProjectModel createProject(ProjectModel resource) throws ServiceException {
            initializeNewProject(resource); // sets GUID, createDate, revisionCount = 0
            ProjectEntity entity = projectResourceAssembler.toEntity(resource);
        return saveProject(resource, entity);
    }
    @Transactional
    public ProjectModel updateProject(ProjectModel resource) {
        UUID guid = UUID.fromString(resource.getProjectGuid());
        ProjectEntity existingEntity = projectRepository.findById(guid)
                .orElseThrow(() -> new EntityNotFoundException("Project not found: " + resource.getProjectGuid()));

        ProjectEntity entity = projectResourceAssembler.updateEntity(resource, existingEntity);
        return saveProject(resource, entity);
    }

    public ProjectModel saveProject(ProjectModel resource, ProjectEntity entity) {
        try {
            if (resource == null || entity == null) {
                throw new IllegalArgumentException("Resource and entity must not be null");
            }
                assignAssociatedEntities(resource, entity);

                if (entity.getProjectName() != null && !entity.getProjectName().isEmpty()) {
                    String incomingName = entity.getProjectName().trim();

                    if (entity.getProjectGuid() != null) {
                        // For updates, check if another project has this name (case-insensitive)
                        boolean duplicateExists = projectRepository.findByProjectNameIgnoreCase(incomingName)
                                .stream()
                                .anyMatch(existing -> !existing.getProjectGuid().equals(entity.getProjectGuid()));

                        if (duplicateExists) {
                            throw new ValidationException("Project name already exists: " + incomingName);
                        }
                    } else {
                        // For new projects, case-insensitive exists check
                        if (projectRepository.existsByProjectNameIgnoreCase(incomingName)) {
                            throw new ValidationException("Project name already exists: " + incomingName);
                        }
                    }
                }

                ProjectEntity savedEntity = projectRepository.saveAndFlush(entity);
                return projectResourceAssembler.toModel(savedEntity);

        } catch (EntityNotFoundException e) {
            throw new ServiceException("Invalid reference data: " + e.getMessage(), e);
        } catch (DataIntegrityViolationException | ConstraintViolationException e) {
            throw e;
        }
    }

    private void initializeNewProject(ProjectModel resource) {
        resource.setProjectGuid(UUID.randomUUID().toString());
        // always set status to active on create
        resource.setProjectStatusCode(toProjectStatusCodeModel(loadOrSetDefaultProjectStatusCode("ACTIVE")));
        resource.setRevisionCount(0);
    }

    private void assignAssociatedEntities(ProjectModel resource, ProjectEntity entity) {
        if (resource.getForestAreaCode() != null) {
            String forestAreaCode1 = resource.getForestAreaCode().getForestAreaCode();
            ForestAreaCodeEntity forestAreaCode = loadForestAreaCode(forestAreaCode1);
            entity.setForestAreaCode(forestAreaCode);
        }

        if (resource.getProjectTypeCode() != null && resource.getProjectTypeCode().getProjectTypeCode() != null) {
            entity.setProjectTypeCode(loadProjectTypeCode(resource.getProjectTypeCode().getProjectTypeCode()));
        }

        if (resource.getGeneralScopeCode() != null && resource.getGeneralScopeCode().getGeneralScopeCode() != null) {
            entity.setGeneralScopeCode(loadGeneralScopeCode(resource.getGeneralScopeCode().getGeneralScopeCode()));
        }

        if (resource.getPrimaryObjectiveTypeCode() != null && resource.getPrimaryObjectiveTypeCode().getObjectiveTypeCode() != null) {
            entity.setPrimaryObjectiveTypeCode(loadObjectiveTypeCode(resource.getPrimaryObjectiveTypeCode().getObjectiveTypeCode()));
        }

        if (resource.getSecondaryObjectiveTypeCode() != null && resource.getSecondaryObjectiveTypeCode().getObjectiveTypeCode() != null) {
            entity.setSecondaryObjectiveTypeCode(loadObjectiveTypeCode(resource.getSecondaryObjectiveTypeCode().getObjectiveTypeCode()));
        }
        if (resource.getTertiaryObjectiveTypeCode() != null && resource.getTertiaryObjectiveTypeCode().getObjectiveTypeCode() != null) {
            entity.setTertiaryObjectiveTypeCode(loadObjectiveTypeCode(resource.getTertiaryObjectiveTypeCode().getObjectiveTypeCode()));
        }

        String projectStatusCode1 = resource.getProjectStatusCode() != null
                ? resource.getProjectStatusCode().getProjectStatusCode()
                : null;
        ProjectStatusCodeEntity projectStatusCode = loadOrSetDefaultProjectStatusCode(
                projectStatusCode1);
        entity.setProjectStatusCode(
                projectStatusCode);
    }

    private ForestAreaCodeEntity loadForestAreaCode(String forestAreaCode) {
        return forestAreaCodeRepository
                .findById(forestAreaCode)
                .orElseThrow(() -> new IllegalArgumentException("ForestAreaCode not found: " + forestAreaCode));
    }

    private ProjectTypeCodeEntity loadProjectTypeCode(String projectTypeCode) {
        return projectTypeCodeRepository
                .findById(projectTypeCode)
                .orElseThrow(() -> new EntityNotFoundException("Project Type Code not found: " + projectTypeCode));
    }

    private GeneralScopeCodeEntity loadGeneralScopeCode(String generalScopeCode) {
        return generalScopeCodeRepository
                .findById(generalScopeCode)
                .orElseThrow(() -> new EntityNotFoundException("General Scope Code not found: " + generalScopeCode));
    }

    private ObjectiveTypeCodeEntity loadObjectiveTypeCode(String objectiveTypeCode) {
        return objectiveTypeCodeRepository
                .findById(objectiveTypeCode)
                .orElseThrow(() -> new EntityNotFoundException("Objective Type Code not found: " + objectiveTypeCode));
    }

    private ProjectStatusCodeEntity loadOrSetDefaultProjectStatusCode(String projectStatusCode) {
        if (projectStatusCode == null) {
            return projectStatusCodeRepository
                    .findById("ACTIVE")
                    .orElseThrow(() -> new EntityNotFoundException("Project Status Code 'ACTIVE' not found"));
        }
        return projectStatusCodeRepository
                .findById(projectStatusCode)
                .orElseThrow(() -> new EntityNotFoundException("Project Status Code not found: " + projectStatusCode));
    }

    private ProjectStatusCodeModel toProjectStatusCodeModel(ProjectStatusCodeEntity entity) {
        if (entity == null) return null;
        ProjectStatusCodeModel model = new ProjectStatusCodeModel();
        model.setProjectStatusCode(entity.getProjectStatusCode());
        model.setDescription(entity.getDescription());
        return model;
    }

    @Transactional
    public ProjectModel deleteProject(String id, boolean deleteFiles) throws ServiceException {
        try {
            UUID projectGuid = UUID.fromString(id);
            ProjectEntity entity = projectRepository.findById(projectGuid)
                    .orElseThrow(() -> new EntityNotFoundException("Project not found: " + id));
            
            // Manual cleanup of dependent entities
            projectBoundaryService.deleteProjectBoundaries(id, deleteFiles);
            projectFiscalService.deleteProjectFiscals(id, deleteFiles);
            
            List<EvaluationCriteriaSummaryEntity> summaries = evaluationCriteriaSummaryRepository.findAllByProjectGuid(projectGuid);
            for (EvaluationCriteriaSummaryEntity summary : summaries) {
                List<EvaluationCriteriaSectionSummaryEntity> sectionSummaries = evaluationCriteriaSectionSummaryRepository.findAllByEvaluationCriteriaSummaryGuid(summary.getEvaluationCriteriaSummaryGuid());
                for (EvaluationCriteriaSectionSummaryEntity sectionSummary : sectionSummaries) {
                    evaluationCriteriaSelectedRepository.deleteByEvaluationCriteriaSectionSummaryGuid(sectionSummary.getEvaluationCriteriaSectionSummaryGuid());
                }
                evaluationCriteriaSectionSummaryRepository.deleteByEvaluationCriteriaSummaryGuid(summary.getEvaluationCriteriaSummaryGuid());
            }
            evaluationCriteriaSummaryRepository.deleteByProjectGuid(projectGuid);
            
            projectRepository.delete(entity);

            return projectResourceAssembler.toModel(entity);
        } catch (Exception e) {
            throw new ServiceException(e.getLocalizedMessage(), e);
        }
    }
}