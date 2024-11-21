package ca.bc.gov.nrs.wfprev.services;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.hateoas.CollectionModel;
import org.springframework.stereotype.Component;

import ca.bc.gov.nrs.wfone.common.service.api.ServiceException;
import ca.bc.gov.nrs.wfprev.common.services.CommonService;
import ca.bc.gov.nrs.wfprev.data.assemblers.ProjectResourceAssembler;
import ca.bc.gov.nrs.wfprev.data.model.ProjectEntity;
import ca.bc.gov.nrs.wfprev.data.repositories.ProjectRepository;
import ca.bc.gov.nrs.wfprev.data.resources.ProjectModel;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class ProjectService implements CommonService {
  private ProjectRepository projectRepository;
  private ProjectResourceAssembler projectResourceAssembler;

  public ProjectService(ProjectRepository projectRepository, ProjectResourceAssembler projectResourceAssembler) {
    this.projectRepository = projectRepository;
    this.projectResourceAssembler = projectResourceAssembler;
  }
  
  public CollectionModel<ProjectModel> getAllProjects() throws ServiceException {
    try {
      List<ProjectEntity> entities = new ArrayList<>();
      return projectResourceAssembler.toCollectionModel(entities);
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
      resource.setUpdateDate(new Date());

      ProjectEntity oldEntity = projectResourceAssembler.toEntity(resource);
      ProjectEntity newEntity = projectRepository.saveAndFlush(oldEntity);

      return projectResourceAssembler.toModel(newEntity);
    } catch(Exception e) {
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

