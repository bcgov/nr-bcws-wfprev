package ca.bc.gov.nrs.wfprev.services;

import ca.bc.gov.nrs.wfone.common.service.api.ServiceException;
import ca.bc.gov.nrs.wfprev.common.services.CommonService;
import ca.bc.gov.nrs.wfprev.data.assemblers.ProjectFiscalResourceAssembler;
import ca.bc.gov.nrs.wfprev.data.assemblers.ProjectResourceAssembler;
import ca.bc.gov.nrs.wfprev.data.entities.ProjectEntity;
import ca.bc.gov.nrs.wfprev.data.entities.ProjectFiscalEntity;
import ca.bc.gov.nrs.wfprev.data.models.ProjectFiscalModel;
import ca.bc.gov.nrs.wfprev.data.models.ProjectModel;
import ca.bc.gov.nrs.wfprev.data.repositories.ProjectFiscalRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.hateoas.CollectionModel;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
public class ProjectFiscalService implements CommonService {

    private final ProjectFiscalRepository projectFiscalRepository;
    private final ProjectFiscalResourceAssembler projectFiscalResourceAssembler;

    private final ProjectService projectService;

    private final ProjectResourceAssembler projectResourceAssembler;

    public ProjectFiscalService(ProjectFiscalRepository projectFiscalRepository, ProjectFiscalResourceAssembler projectFiscalResourceAssembler, ProjectService projectService, ProjectResourceAssembler projectResourceAssembler) {
        this.projectFiscalRepository = projectFiscalRepository;
        this.projectFiscalResourceAssembler = projectFiscalResourceAssembler;
        this.projectService = projectService;
        this.projectResourceAssembler = projectResourceAssembler;
    }

    public CollectionModel<ProjectFiscalModel> getAllProjectFiscals() throws ServiceException {
        List<ProjectFiscalEntity> all = projectFiscalRepository.findAll();
        return projectFiscalResourceAssembler.toCollectionModel(all);
    }

    public ProjectFiscalModel createProjectFiscal(ProjectFiscalModel projectFiscalModel) {
        initializeNewProjectFiscal(projectFiscalModel);
        ProjectModel projectById = projectService.getProjectById(projectFiscalModel.getProjectGuid());
        ProjectEntity projectEntity = projectResourceAssembler.toEntity(projectById);
        ProjectFiscalEntity entity = projectFiscalResourceAssembler.toEntity(projectFiscalModel, projectEntity);
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
                .orElseThrow(() -> new EntityNotFoundException("Project not found: " + projectFiscalModel.getProjectPlanFiscalGuid()));

        ProjectFiscalEntity entity = projectFiscalResourceAssembler.updateEntity(projectFiscalModel, existingEntity);
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
                .orElseThrow(() -> new EntityNotFoundException("Project not found: " + uuid));
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
}
