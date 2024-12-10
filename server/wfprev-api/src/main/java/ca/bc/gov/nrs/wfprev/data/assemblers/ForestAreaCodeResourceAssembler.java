package ca.bc.gov.nrs.wfprev.data.assemblers;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

import ca.bc.gov.nrs.wfprev.common.enums.CodeTables;
import ca.bc.gov.nrs.wfprev.controllers.CodesController;
import ca.bc.gov.nrs.wfprev.data.entities.ForestAreaCodeEntity;
import ca.bc.gov.nrs.wfprev.data.models.ForestAreaCodeModel;

@Component
public class ForestAreaCodeResourceAssembler extends RepresentationModelAssemblerSupport<ForestAreaCodeEntity, ForestAreaCodeModel> {

  public ForestAreaCodeResourceAssembler() {
    super(CodesController.class, ForestAreaCodeModel.class);
  }

  public ForestAreaCodeEntity toEntity(ForestAreaCodeModel resource) {
    ForestAreaCodeEntity entity = new ForestAreaCodeEntity();

    entity.setForestAreaCode(resource.getForestAreaCode());
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
  public ForestAreaCodeModel toModel(ForestAreaCodeEntity entity) {
    ForestAreaCodeModel resource = instantiateModel(entity);
     
    resource.add(linkTo(
        methodOn(CodesController.class)
        .getCodeById(CodeTables.FOREST_AREA_CODE, entity.getForestAreaCode()))
        .withSelfRel());
     
      resource.setForestAreaCode(entity.getForestAreaCode());
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
  public CollectionModel<ForestAreaCodeModel> toCollectionModel(Iterable<? extends ForestAreaCodeEntity> entities) 
  {
    CollectionModel<ForestAreaCodeModel> resources = super.toCollectionModel(entities);
     
    resources.add(linkTo(methodOn(CodesController.class).getCodes(CodeTables.FOREST_AREA_CODE)).withSelfRel());
     
    return resources;
  }
}
