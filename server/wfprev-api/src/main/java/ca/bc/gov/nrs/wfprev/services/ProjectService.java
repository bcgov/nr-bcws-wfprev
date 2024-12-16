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
  private final ProjectRepository projectRepository;
  private final ProjectResourceAssembler projectResourceAssembler;

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
      return projectRepository.findById(UUID.fromString(id))
              .map(projectResourceAssembler::toModel)
              .orElse(null);
    } catch(Exception e) {
      throw new ServiceException(e.getLocalizedMessage(), e);
    }
  }

  @Transactional
  public ProjectModel createOrUpdateProject(ProjectModel resource) throws ServiceException {
    try {
      ProjectEntity entity;

      if (resource.getProjectGuid() != null) {
        // Fetch the existing entity
        entity = projectRepository.findById(UUID.fromString(resource.getProjectGuid()))
                .orElseThrow(() -> new EntityNotFoundException("Project not found: " + resource.getProjectGuid()));

        // Update fields on the existing entity
        projectResourceAssembler.updateEntity(resource, entity);
      } else {
        // Create a new entity
        resource.setCreateDate(new Date());
        resource.setProjectGuid(UUID.randomUUID().toString());
        resource.setRevisionCount(0); // Initialize revision count for new records
        entity = projectResourceAssembler.toEntity(resource);
      }

      // Handle ForestAreaCode
      if (resource.getForestAreaCode() != null) {
        String forestAreaCode = resource.getForestAreaCode().getForestAreaCode();
        ForestAreaCodeEntity forestAreaCodeEntity = forestAreaCodeRepository
                .findById(forestAreaCode)
                .orElseThrow(() -> new EntityNotFoundException("ForestAreaCode not found: " + forestAreaCode));
        entity.setForestAreaCode(forestAreaCodeEntity);
      }

      // Handle ProjectTypeCode
      if (resource.getProjectTypeCode() != null) {
        String projectTypeCode = resource.getProjectTypeCode().getProjectTypeCode();
        ProjectTypeCodeEntity projectTypeCodeEntity = projectTypeCodeRepository
                .findById(projectTypeCode)
                .orElseThrow(() -> new EntityNotFoundException("ProjectTypeCode not found: " + projectTypeCode));
        entity.setProjectTypeCode(projectTypeCodeEntity);
      }

      // Handle GeneralScopeCode
      if (resource.getGeneralScopeCode() != null) {
        String generalScopeCode = resource.getGeneralScopeCode().getGeneralScopeCode();
        GeneralScopeCodeEntity generalScopeCodeEntity = generalScopeCodeRepository
                .findById(generalScopeCode)
                .orElseThrow(() -> new EntityNotFoundException("GeneralScopeCode not found: " + generalScopeCode));
        entity.setGeneralScopeCode(generalScopeCodeEntity);
      }

      // Handle ProjectStatusCode
      if (resource.getProjectStatusCode() == null) {
        ProjectStatusCodeEntity activeStatus = projectStatusCodeRepository.findById("ACTIVE")
                .orElseThrow(() -> new EntityNotFoundException("ProjectStatusCode 'ACTIVE' not found"));
        entity.setProjectStatusCode(activeStatus);
      } else {
        String projectStatusCode = resource.getProjectStatusCode().getProjectStatusCode();
        ProjectStatusCodeEntity projectStatusCodeEntity = projectStatusCodeRepository
                .findById(projectStatusCode)
                .orElseThrow(() -> new EntityNotFoundException("ProjectStatusCode not found: " + projectStatusCode));
        entity.setProjectStatusCode(projectStatusCodeEntity);
      }

      // Save the entity
      ProjectEntity savedEntity = projectRepository.saveAndFlush(entity);
      return projectResourceAssembler.toModel(savedEntity);

    } catch (EntityNotFoundException e) {
      throw new ServiceException("Invalid reference data: " + e.getMessage(), e);
    } catch (DataIntegrityViolationException e) {
      throw new DataIntegrityViolationException(e.getMessage(), e);
    } catch (ConstraintViolationException e) {
      throw e;
    } catch (Exception e) {
      log.error("Error creating/updating project", e);
      throw new ServiceException(e.getLocalizedMessage(), e);
    }
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
    } catch(Exception e) {
      throw new ServiceException(e.getLocalizedMessage(), e);
    }
  }
}