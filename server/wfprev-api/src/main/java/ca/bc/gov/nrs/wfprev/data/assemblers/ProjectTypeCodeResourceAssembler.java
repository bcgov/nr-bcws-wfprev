package ca.bc.gov.nrs.wfprev.data.assemblers;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

import ca.bc.gov.nrs.wfprev.data.model.ProjectTypeCodeEntity;
import ca.bc.gov.nrs.wfprev.common.enums.CodeTables;
import ca.bc.gov.nrs.wfprev.controllers.CodesController;
import ca.bc.gov.nrs.wfprev.data.resources.ProjectTypeCodeModel;

@Component
public class ProjectTypeCodeResourceAssembler extends RepresentationModelAssemblerSupport<ProjectTypeCodeEntity, ProjectTypeCodeModel> {

  public ProjectTypeCodeResourceAssembler() {
    super(CodesController.class, ProjectTypeCodeModel.class);
  }

  public ProjectTypeCodeEntity toEntity(ProjectTypeCodeModel resource) {
    ProjectTypeCodeEntity entity = new ProjectTypeCodeEntity();

    entity.setProjectTypeCode(resource.getProjectTypeCode());
    entity.setDescription(resource.getDescription());
    entity.setDisplayOrder(resource.getDisplayOrder());
    entity.setEffectiveDate(resource.getEffectiveDate());
    entity.setExpiryDate(resource.getExpiryDate());
    entity.setRevisionCount(resource.getRevisionCount());
    entity.setCreateUser(resource.getCreateUser());
    entity.setCreateDate(resource.getCreateDate());
    entity.setUpdateUser(resource.getUpdateUser());
    entity.setUpdateDate(resource.getUpdateDate());
    
    return entity;
  }  

  @Override
  public ProjectTypeCodeModel toModel(ProjectTypeCodeEntity entity) {
    ProjectTypeCodeModel resource = instantiateModel(entity);
     
    resource.add(linkTo(
        methodOn(CodesController.class)
        .getCodeById(CodeTables.PROJECT_TYPE.toString(), entity.getProjectTypeCode()))
        .withSelfRel());
     
      resource.setProjectTypeCode(entity.getProjectTypeCode());
      resource.setDescription(entity.getDescription());
      resource.setDisplayOrder(entity.getDisplayOrder());
      resource.setEffectiveDate(entity.getEffectiveDate());
      resource.setExpiryDate(entity.getExpiryDate());
      resource.setRevisionCount(entity.getRevisionCount());
      resource.setCreateUser(entity.getCreateUser());
      resource.setCreateDate(entity.getCreateDate());
      resource.setUpdateUser(entity.getUpdateUser());
      resource.setUpdateDate(entity.getUpdateDate());

    return resource;
  }
  
  @Override
  public CollectionModel<ProjectTypeCodeModel> toCollectionModel(Iterable<? extends ProjectTypeCodeEntity> entities) 
  {
    CollectionModel<ProjectTypeCodeModel> resources = super.toCollectionModel(entities);
     
    resources.add(linkTo(methodOn(CodesController.class).getCodes(CodeTables.PROJECT_TYPE.toString())).withSelfRel());
     
    return resources;
  }
}
