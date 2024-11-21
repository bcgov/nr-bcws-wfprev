package ca.bc.gov.nrs.wfprev.data.assemblers;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

import ca.bc.gov.nrs.wfprev.data.model.GeneralScopeCodeEntity;
import ca.bc.gov.nrs.wfprev.common.enums.CodeTables;
import ca.bc.gov.nrs.wfprev.controllers.CodesController;
import ca.bc.gov.nrs.wfprev.data.resources.GeneralScopeCodeModel;

@Component
public class GeneralScopeCodeResourceAssembler extends RepresentationModelAssemblerSupport<GeneralScopeCodeEntity, GeneralScopeCodeModel> {

  public GeneralScopeCodeResourceAssembler() {
    super(CodesController.class, GeneralScopeCodeModel.class);
  }

  public GeneralScopeCodeEntity toEntity(GeneralScopeCodeModel resource) {
    GeneralScopeCodeEntity entity = new GeneralScopeCodeEntity();

    entity.setGeneralScopeCode(resource.getGeneralScopeCode());
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
  public GeneralScopeCodeModel toModel(GeneralScopeCodeEntity entity) {
    GeneralScopeCodeModel resource = instantiateModel(entity);
     
    resource.add(linkTo(
        methodOn(CodesController.class)
        .getCodeById(CodeTables.GENERAL_SCOPE_CODE, entity.getGeneralScopeCode()))
        .withSelfRel());
     
      resource.setGeneralScopeCode(entity.getGeneralScopeCode());
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
  public CollectionModel<GeneralScopeCodeModel> toCollectionModel(Iterable<? extends GeneralScopeCodeEntity> entities) 
  {
    CollectionModel<GeneralScopeCodeModel> resources = super.toCollectionModel(entities);
     
    resources.add(linkTo(methodOn(CodesController.class).getCodes(CodeTables.GENERAL_SCOPE_CODE)).withSelfRel());
     
    return resources;
  }
}
