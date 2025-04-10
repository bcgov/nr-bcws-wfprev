package ca.bc.gov.nrs.wfprev.data.assemblers;

import ca.bc.gov.nrs.wfprev.controllers.ProjectBoundaryController;
import ca.bc.gov.nrs.wfprev.data.entities.ProjectBoundaryEntity;
import ca.bc.gov.nrs.wfprev.data.models.ProjectBoundaryModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.UUID;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Slf4j
@Component
public class ProjectBoundaryResourceAssembler extends RepresentationModelAssemblerSupport<ProjectBoundaryEntity, ProjectBoundaryModel> {

  public ProjectBoundaryResourceAssembler() {
    super(ProjectBoundaryController.class, ProjectBoundaryModel.class);
  }

  public ProjectBoundaryEntity toEntity(ProjectBoundaryModel resource) {
    ProjectBoundaryEntity entity = new ProjectBoundaryEntity();

    entity.setProjectBoundaryGuid(UUID.fromString(resource.getProjectBoundaryGuid()));
    entity.setProjectGuid(UUID.fromString(resource.getProjectGuid()));
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
        .getProjectBoundary(String.valueOf(entity.getProjectGuid()), String.valueOf(entity.getProjectBoundaryGuid())))
        .withSelfRel());
     
        resource.setProjectBoundaryGuid(String.valueOf(entity.getProjectBoundaryGuid()));
        resource.setProjectGuid(String.valueOf(entity.getProjectGuid()));
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

      Iterator<? extends ProjectBoundaryEntity> iterator = entities.iterator();
      if (iterator.hasNext()) {
          String projectGuid = String.valueOf(iterator.next().getProjectGuid());
          resources.add(linkTo(methodOn(ProjectBoundaryController.class).getAllProjectBoundaries(projectGuid)).withSelfRel());
      }
     
    return resources;
  }

    public ProjectBoundaryEntity updateEntity(ProjectBoundaryModel model, ProjectBoundaryEntity existingEntity) {
        log.debug(">> updateEntity");
        ProjectBoundaryEntity entity = new ProjectBoundaryEntity();

        entity.setProjectBoundaryGuid(existingEntity.getProjectBoundaryGuid());
        entity.setProjectGuid(existingEntity.getProjectGuid());
        entity.setSystemStartTimestamp(nonNullOrDefault(model.getSystemStartTimestamp(), existingEntity.getSystemStartTimestamp()));
        entity.setSystemEndTimestamp(nonNullOrDefault(model.getSystemEndTimestamp(), existingEntity.getSystemEndTimestamp()));
        entity.setMappingLabel(nonNullOrDefault(model.getMappingLabel(), existingEntity.getMappingLabel()));
        entity.setCollectionDate(nonNullOrDefault(model.getCollectionDate(), existingEntity.getCollectionDate()));
        entity.setCollectionMethod(nonNullOrDefault(model.getCollectionMethod(), existingEntity.getCollectionMethod()));
        entity.setCollectorName(nonNullOrDefault(model.getCollectorName(), existingEntity.getCollectorName()));
        entity.setBoundarySizeHa(nonNullOrDefault(model.getBoundarySizeHa(), existingEntity.getBoundarySizeHa()));
        entity.setBoundaryComment(nonNullOrDefault(model.getBoundaryComment(), existingEntity.getBoundaryComment()));
        entity.setRevisionCount(existingEntity.getRevisionCount());
        entity.setBoundaryGeometry(nonNullOrDefault(model.getBoundaryGeometry(), existingEntity.getBoundaryGeometry()));
        entity.setLocationGeometry(nonNullOrDefault(model.getLocationGeometry(), existingEntity.getLocationGeometry()));
        entity.setCreateUser(existingEntity.getCreateUser());
        entity.setCreateDate(existingEntity.getCreateDate());
        entity.setUpdateUser(model.getUpdateUser());
        entity.setUpdateDate(model.getUpdateDate());

        return entity;
    }

    private <T> T nonNullOrDefault(T newValue, T existingValue) {
        return newValue != null ? newValue : existingValue;
    }
}
