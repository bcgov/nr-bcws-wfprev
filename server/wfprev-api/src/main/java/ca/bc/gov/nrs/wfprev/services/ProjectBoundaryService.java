package ca.bc.gov.nrs.wfprev.services;

import ca.bc.gov.nrs.wfone.common.service.api.ServiceException;
import ca.bc.gov.nrs.wfprev.common.services.CommonService;
import ca.bc.gov.nrs.wfprev.data.assemblers.ProjectBoundaryResourceAssembler;
import ca.bc.gov.nrs.wfprev.data.entities.ProjectBoundaryEntity;
import ca.bc.gov.nrs.wfprev.data.models.ProjectBoundaryModel;
import ca.bc.gov.nrs.wfprev.data.repositories.ProjectBoundaryRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.hateoas.CollectionModel;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Slf4j
@Component
public class ProjectBoundaryService implements CommonService {
  private ProjectBoundaryRepository projectBoundaryRepository;
  private ProjectBoundaryResourceAssembler projectBoundaryResourceAssembler;

  public ProjectBoundaryService(ProjectBoundaryRepository projectBoundaryRepository, ProjectBoundaryResourceAssembler projectBoundaryResourceAssembler) {
    this.projectBoundaryRepository = projectBoundaryRepository;
    this.projectBoundaryResourceAssembler = projectBoundaryResourceAssembler;
  }
  
  public CollectionModel<ProjectBoundaryModel> getAllProjectBoundaries() throws ServiceException {
    try {
        List<ProjectBoundaryEntity> entities = projectBoundaryRepository.findAll();
      return projectBoundaryResourceAssembler.toCollectionModel(entities);
    } catch(Exception e) {
      throw new ServiceException(e.getLocalizedMessage(), e);
    }
  }

  public ProjectBoundaryModel getProjectBoundaryById(String id) throws ServiceException {
    try {
      return projectBoundaryRepository.findById(id).map(projectBoundaryResourceAssembler::toModel).orElse(null);
    } catch(Exception e) {
      throw new ServiceException(e.getLocalizedMessage(), e);
    }
  }

  @Transactional
  public ProjectBoundaryModel createOrUpdateProjectBoundary(ProjectBoundaryModel resource) throws ServiceException {
    try {
      resource.setUpdateDate(new Date());

      ProjectBoundaryEntity oldEntity = projectBoundaryResourceAssembler.toEntity(resource);
      ProjectBoundaryEntity newEntity = projectBoundaryRepository.saveAndFlush(oldEntity);

      return projectBoundaryResourceAssembler.toModel(newEntity);
    } catch(Exception e) {
      throw new ServiceException(e.getLocalizedMessage(), e);
    }
  }

  @Transactional
  public ProjectBoundaryModel deleteProjectBoundary(String id) throws ServiceException {
    try {
      ProjectBoundaryModel model = getProjectBoundaryById(id);

      ProjectBoundaryEntity entity = projectBoundaryResourceAssembler.toEntity(model);
      projectBoundaryRepository.delete(entity);

      return projectBoundaryResourceAssembler.toModel(entity);
    } catch(Exception e) {
      throw new ServiceException(e.getLocalizedMessage(), e);
    }
  }
}
