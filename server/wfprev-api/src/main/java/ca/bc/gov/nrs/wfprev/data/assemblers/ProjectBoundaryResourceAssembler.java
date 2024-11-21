package ca.bc.gov.nrs.wfprev.data.assemblers;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

import ca.bc.gov.nrs.wfprev.data.model.ProjectBoundaryEntity;
import ca.bc.gov.nrs.wfprev.controllers.ProjectBoundaryController;
import ca.bc.gov.nrs.wfprev.data.resources.ProjectBoundaryModel;

@Component
public class ProjectBoundaryResourceAssembler extends RepresentationModelAssemblerSupport<ProjectBoundaryEntity, ProjectBoundaryModel> {

  public ProjectBoundaryResourceAssembler() {
    super(ProjectBoundaryController.class, ProjectBoundaryModel.class);
  }

  public ProjectBoundaryEntity toEntity(ProjectBoundaryModel resource) {
    ProjectBoundaryEntity entity = new ProjectBoundaryEntity();

    entity.setProjectBoundaryGuid(resource.getProjectBoundaryGuid());
    entity.setProjectGuid(resource.getProjectGuid());
    entity.setSystemStartTimestamp(resource.getSystemStartTimestamp());
    entity.setSystemEndTimestamp(resource.getSystemEndTimestamp());
    entity.setMappingLabel(resource.getMappingLabel());
    entity.setCollectionDate(resource.getCollectionDate());
    entity.setCollectionMethod(resource.getCollectionMethod());
    entity.setCollectorName(resource.getCollectorName());
    entity.setBoundarySizeHa(resource.getBoundarySizeHa());
    entity.setBoundaryComment(resource.getBoundaryComment());
    entity.setLocationGeometry(resource.getLocationGeometry());
    entity.setBoundaryGeometry(resource.getBoundaryGeometry());
    entity.setRevisionCount(resource.getRevisionCount());
    entity.setCreateUser(resource.getCreateUser());
    entity.setCreateDate(resource.getCreateDate());
    entity.setUpdateUser(resource.getUpdateUser());
    entity.setUpdateDate(resource.getUpdateDate());
    
    return entity;
  }  

  @Override
  public ProjectBoundaryModel toModel(ProjectBoundaryEntity entity) {
    ProjectBoundaryModel resource = instantiateModel(entity);
     
    resource.add(linkTo(
        methodOn(ProjectBoundaryController.class)
        .getById(entity.getProjectBoundaryGuid()))
        .withSelfRel());
     
        resource.setProjectBoundaryGuid(entity.getProjectBoundaryGuid());
        resource.setProjectGuid(entity.getProjectGuid());
        resource.setSystemStartTimestamp(entity.getSystemStartTimestamp());
        resource.setSystemEndTimestamp(entity.getSystemEndTimestamp());
        resource.setMappingLabel(entity.getMappingLabel());
        resource.setCollectionDate(entity.getCollectionDate());
        resource.setCollectionMethod(entity.getCollectionMethod());
        resource.setCollectorName(entity.getCollectorName());
        resource.setBoundarySizeHa(entity.getBoundarySizeHa());
        resource.setBoundaryComment(entity.getBoundaryComment());
        resource.setLocationGeometry(entity.getLocationGeometry());
        resource.setBoundaryGeometry(entity.getBoundaryGeometry());
        resource.setRevisionCount(entity.getRevisionCount());
        resource.setRevisionCount(entity.getRevisionCount());
        resource.setCreateUser(entity.getCreateUser());
        resource.setCreateDate(entity.getCreateDate());
        resource.setUpdateUser(entity.getUpdateUser());
        resource.setUpdateDate(entity.getUpdateDate());

    return resource;
  }
  
  @Override
  public CollectionModel<ProjectBoundaryModel> toCollectionModel(Iterable<? extends ProjectBoundaryEntity> entities) 
  {
    CollectionModel<ProjectBoundaryModel> resources = super.toCollectionModel(entities);
     
    resources.add(linkTo(methodOn(ProjectBoundaryController.class).getAllProjectBoundaries()).withSelfRel());
     
    return resources;
  }
}
