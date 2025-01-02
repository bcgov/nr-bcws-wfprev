package ca.bc.gov.nrs.wfprev.services;

import ca.bc.gov.nrs.wfone.common.service.api.ServiceException;
import ca.bc.gov.nrs.wfprev.common.services.CommonService;
import ca.bc.gov.nrs.wfprev.data.assemblers.ProjectFiscalResourceAssembler;
import ca.bc.gov.nrs.wfprev.data.entities.ProjectFiscalEntity;
import ca.bc.gov.nrs.wfprev.data.models.ProjectFiscalModel;
import ca.bc.gov.nrs.wfprev.data.repositories.ProjectFiscalRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.hateoas.CollectionModel;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
public class ProjectFiscalService implements CommonService {

    private final ProjectFiscalRepository projectFiscalRepository;
    private final ProjectFiscalResourceAssembler projectFiscalResourceAssembler;

    public ProjectFiscalService(ProjectFiscalRepository projectFiscalRepository, ProjectFiscalResourceAssembler projectFiscalResourceAssembler) {
        this.projectFiscalRepository = projectFiscalRepository;
        this.projectFiscalResourceAssembler = projectFiscalResourceAssembler;
    }

    public CollectionModel<ProjectFiscalModel> getAllProjectFiscals() throws ServiceException {
        List<ProjectFiscalEntity> all = projectFiscalRepository.findAll();
        return projectFiscalResourceAssembler.toCollectionModel(all);
    }

    public ProjectFiscalModel createProjectFiscal(ProjectFiscalModel projectFiscalModel) {
        ProjectFiscalEntity entity = projectFiscalResourceAssembler.toEntity(projectFiscalModel);
        ProjectFiscalEntity savedEntity = projectFiscalRepository.save(entity);
        return projectFiscalResourceAssembler.toModel(savedEntity);
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
}
