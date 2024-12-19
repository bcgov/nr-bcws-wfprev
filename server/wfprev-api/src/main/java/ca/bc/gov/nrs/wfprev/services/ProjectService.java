package ca.bc.gov.nrs.wfprev.services;

import ca.bc.gov.nrs.wfone.common.service.api.ServiceException;
import ca.bc.gov.nrs.wfprev.common.services.CommonService;
import ca.bc.gov.nrs.wfprev.data.assemblers.ProjectResourceAssembler;
import ca.bc.gov.nrs.wfprev.data.entities.ForestAreaCodeEntity;
import ca.bc.gov.nrs.wfprev.data.entities.GeneralScopeCodeEntity;
import ca.bc.gov.nrs.wfprev.data.entities.ProjectEntity;
import ca.bc.gov.nrs.wfprev.data.entities.ProjectStatusCodeEntity;
import ca.bc.gov.nrs.wfprev.data.entities.ProjectTypeCodeEntity;
import ca.bc.gov.nrs.wfprev.data.models.ProjectModel;
import ca.bc.gov.nrs.wfprev.data.repositories.ForestAreaCodeRepository;
import ca.bc.gov.nrs.wfprev.data.repositories.GeneralScopeCodeRepository;
import ca.bc.gov.nrs.wfprev.data.repositories.ProjectRepository;
import ca.bc.gov.nrs.wfprev.data.repositories.ProjectStatusCodeRepository;
import ca.bc.gov.nrs.wfprev.data.repositories.ProjectTypeCodeRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.hateoas.CollectionModel;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Optional;
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

  public ProjectService(
          ProjectRepository projectRepository,
          ProjectResourceAssembler projectResourceAssembler,
          ForestAreaCodeRepository forestAreaCodeRepository,
          ProjectTypeCodeRepository projectTypeCodeRepository,
          GeneralScopeCodeRepository generalScopeCodeRepository,
          ProjectStatusCodeRepository projectStatusCodeRepository) {
    this.projectRepository = projectRepository;
    this.projectResourceAssembler = projectResourceAssembler;
    this.forestAreaCodeRepository = forestAreaCodeRepository;
    this.projectTypeCodeRepository = projectTypeCodeRepository;
    this.generalScopeCodeRepository = generalScopeCodeRepository;
    this.projectStatusCodeRepository = projectStatusCodeRepository;
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
    try {
      initializeNewProject(resource); // sets GUID, createDate, revisionCount = 0
      ProjectEntity entity = projectResourceAssembler.toEntity(resource);

      assignAssociatedEntities(resource, entity);

      ProjectEntity savedEntity = projectRepository.saveAndFlush(entity);
      return projectResourceAssembler.toModel(savedEntity);
    } catch (EntityNotFoundException e) {
      throw new ServiceException("Invalid reference data: " + e.getMessage(), e);
    } catch (DataIntegrityViolationException | ConstraintViolationException e) {
      throw e;
    } catch (Exception e) {
      log.error("Error creating project", e);
      throw new ServiceException(e.getLocalizedMessage(), e);
    }
  }

  @Transactional
  public ProjectModel updateProject(ProjectModel resource) throws ServiceException {
    try {
      UUID guid = UUID.fromString(resource.getProjectGuid());
      ProjectEntity existingEntity = projectRepository.findById(guid)
              .orElseThrow(() -> new EntityNotFoundException("Project not found: " + resource.getProjectGuid()));

      ProjectEntity entity = projectResourceAssembler.updateEntity(resource, existingEntity);
      assignAssociatedEntities(resource, entity);

      ProjectEntity savedEntity = projectRepository.saveAndFlush(entity);
      return projectResourceAssembler.toModel(savedEntity);
    } catch (EntityNotFoundException e) {
      throw new ServiceException("Invalid reference data: " + e.getMessage(), e);
    } catch (DataIntegrityViolationException | ConstraintViolationException e) {
      throw e;
    } catch (Exception e) {
      log.error("Error updating project", e);
      throw new ServiceException(e.getLocalizedMessage(), e);
    }
  }

  private void initializeNewProject(ProjectModel resource) {
    resource.setCreateDate(new Date());
    resource.setProjectGuid(UUID.randomUUID().toString());
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

  @Transactional
  public ProjectModel deleteProject(String id) throws ServiceException {
    try {
      ProjectModel model = getProjectById(id);

      if (model == null) {
        throw new EntityNotFoundException("Project not found: " + id);
      }

      ProjectEntity entity = projectResourceAssembler.toEntity(model);
      projectRepository.delete(entity);

      return projectResourceAssembler.toModel(entity);
    } catch (Exception e) {
      throw new ServiceException(e.getLocalizedMessage(), e);
    }
  }
}