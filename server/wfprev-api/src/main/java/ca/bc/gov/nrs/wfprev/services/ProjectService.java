package ca.bc.gov.nrs.wfprev.services;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import ca.bc.gov.nrs.wfprev.data.assemblers.ProjectStatusCodeResourceAssembler;
import ca.bc.gov.nrs.wfprev.data.entities.ForestAreaCodeEntity;
import ca.bc.gov.nrs.wfprev.data.entities.GeneralScopeCodeEntity;
import ca.bc.gov.nrs.wfprev.data.entities.ProjectStatusCodeEntity;
import ca.bc.gov.nrs.wfprev.data.entities.ProjectTypeCodeEntity;
import ca.bc.gov.nrs.wfprev.data.models.ProjectStatusCodeModel;
import ca.bc.gov.nrs.wfprev.data.repositories.ForestAreaCodeRepository;
import ca.bc.gov.nrs.wfprev.data.repositories.GeneralScopeCodeRepository;
import ca.bc.gov.nrs.wfprev.data.repositories.ProjectStatusCodeRepository;
import ca.bc.gov.nrs.wfprev.data.repositories.ProjectTypeCodeRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.hateoas.CollectionModel;
import org.springframework.stereotype.Component;

import ca.bc.gov.nrs.wfone.common.service.api.ServiceException;
import ca.bc.gov.nrs.wfprev.common.services.CommonService;
import ca.bc.gov.nrs.wfprev.data.assemblers.ProjectResourceAssembler;
import ca.bc.gov.nrs.wfprev.data.entities.ProjectEntity;
import ca.bc.gov.nrs.wfprev.data.models.ProjectModel;
import ca.bc.gov.nrs.wfprev.data.repositories.ProjectRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class ProjectService implements CommonService {
  private final ProjectStatusCodeResourceAssembler projectStatusCodeAssembler;
  private ProjectRepository projectRepository;
  private ProjectResourceAssembler projectResourceAssembler;

  @Autowired
  private ForestAreaCodeRepository forestAreaCodeRepository;

  @Autowired
  private ProjectTypeCodeRepository projectTypeCodeRepository;

  @Autowired
  private GeneralScopeCodeRepository generalScopeCodeRepository;

  @Autowired
  private ProjectStatusCodeRepository projectStatusCodeRepository;

  public ProjectService(ProjectRepository projectRepository, ProjectResourceAssembler projectResourceAssembler, ProjectStatusCodeResourceAssembler projectStatusCodeAssembler) {
    this.projectRepository = projectRepository;
    this.projectResourceAssembler = projectResourceAssembler;
    this.projectStatusCodeAssembler = projectStatusCodeAssembler;
  }
  
  public CollectionModel<ProjectModel> getAllProjects() throws ServiceException {
    try {
      List<ProjectEntity> all = projectRepository.findAll();
      return projectResourceAssembler.toCollectionModel(all);
    } catch(Exception e) {
      throw new ServiceException(e.getLocalizedMessage(), e);
    }
  }

  public ProjectModel getProjectById(String id) throws ServiceException {
    try {
      return projectRepository.findById(id).map(projectResourceAssembler::toModel).orElse(null);
    } catch(Exception e) {
      throw new ServiceException(e.getLocalizedMessage(), e);
    }
  }

  @Transactional
  public ProjectModel createOrUpdateProject(ProjectModel resource) throws ServiceException {
    try {
      if (resource.getProjectGuid() == null) {
        resource.setCreateDate(new Date());
        //TODO - Fix to use proper user
        resource.setCreateUser("SYSTEM");
        resource.setProjectGuid(UUID.randomUUID().toString());
        resource.setCreateDate(new Date());
        resource.setCreateUser("SYSTEM");
        resource.setRevisionCount(0); // Initialize revision count for new records
      }
      // Set audit fields
      resource.setUpdateDate(new Date());
      //TODO - Fix to use proper user
      resource.setUpdateUser("SYSTEM");

      ProjectEntity entity = projectResourceAssembler.toEntity(resource);

      // Load ForestAreaCode with null checks
      if (resource.getForestAreaCode() != null) {
        String forestAreaCode = resource.getForestAreaCode().getForestAreaCode();
        Optional<ForestAreaCodeEntity> forestAreaCodeEntityOpt = forestAreaCodeRepository.findById(forestAreaCode);
        if (forestAreaCodeEntityOpt.isPresent()) {
          entity.setForestAreaCode(forestAreaCodeEntityOpt.get());
        } else {
          throw new IllegalArgumentException("ForestAreaCode not found: " + forestAreaCode);
        }
      }

      // Load ProjectTypeCode with null checks
      if (resource.getProjectTypeCode() != null) {
        String projectTypeCode = resource.getProjectTypeCode().getProjectTypeCode();
        if (projectTypeCode != null) {
          ProjectTypeCodeEntity projectTypeCodeEntity = projectTypeCodeRepository
                  .findById(projectTypeCode)
                  .orElseThrow(() -> new EntityNotFoundException(
                          "Project Type Code not found: " + projectTypeCode));
          entity.setProjectTypeCode(projectTypeCodeEntity);
        }
      }

      // Load GeneralScopeCode with null checks
      if (resource.getGeneralScopeCode() != null) {
        String generalScopeCode = resource.getGeneralScopeCode().getGeneralScopeCode();
        if (generalScopeCode != null) {
          GeneralScopeCodeEntity generalScopeCodeEntity = generalScopeCodeRepository
                  .findById(generalScopeCode)
                  .orElseThrow(() -> new EntityNotFoundException(
                          "General Scope Code not found: " + generalScopeCode));
          entity.setGeneralScopeCode(generalScopeCodeEntity);
        }
      }

      if (resource.getProjectStatusCode() == null) {
        ProjectStatusCodeEntity activeStatus = projectStatusCodeRepository.findById("ACTIVE")
                .orElseThrow(() -> new EntityNotFoundException("Project Status Code 'ACTIVE' not found"));
        entity.setProjectStatusCode(activeStatus);
      } else {
        String projectStatusCode = resource.getProjectStatusCode().getProjectStatusCode();
        if (projectStatusCode != null) {
          ProjectStatusCodeEntity projectStatusCodeEntity = projectStatusCodeRepository
                  .findById(projectStatusCode)
                  .orElseThrow(() -> new EntityNotFoundException(
                          "Project Status Code not found: " + projectStatusCode));
          entity.setProjectStatusCode(projectStatusCodeEntity);
        }
      }

      ProjectEntity savedEntity = projectRepository.saveAndFlush(entity);
      return projectResourceAssembler.toModel(savedEntity);

    } catch (EntityNotFoundException e) {
      throw new ServiceException("Invalid reference data: " + e.getMessage(), e);
    } catch (DataIntegrityViolationException e) {
      throw new DataIntegrityViolationException(e.getMessage(), e);
    } catch (ConstraintViolationException e) {
      throw e;
    } catch (Exception e) {
      log.error("Error creating/updating project", e);  // Add logging
      throw new ServiceException(e.getLocalizedMessage(), e);
    }
  }

  @Transactional
  public ProjectModel deleteProject(String id) throws ServiceException {
    try {
      ProjectModel model = getProjectById(id);

      ProjectEntity entity = projectResourceAssembler.toEntity(model);
      projectRepository.delete(entity);

      return projectResourceAssembler.toModel(entity);
    } catch(Exception e) {
      throw new ServiceException(e.getLocalizedMessage(), e);
    }
  }
}

