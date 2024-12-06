package ca.bc.gov.nrs.wfprev.data.assemblers;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

import ca.bc.gov.nrs.wfprev.controllers.ProgramAreaController;
import ca.bc.gov.nrs.wfprev.data.entities.ProgramAreaEntity;
import ca.bc.gov.nrs.wfprev.data.models.ProgramAreaModel;

@Component
public class ProgramAreaResourceAssembler extends RepresentationModelAssemblerSupport<ProgramAreaEntity, ProgramAreaModel> {

  public ProgramAreaResourceAssembler() {
    super(ProgramAreaController.class, ProgramAreaModel.class);
  }

  public ProgramAreaEntity toEntity(ProgramAreaModel resource) {
    ProgramAreaEntity entity = new ProgramAreaEntity();

    entity.setProgramAreaGuid(resource.getProgramAreaGuid());
    entity.setProgramAreaName(resource.getProgramAreaName());
    entity.setRevisionCount(resource.getRevisionCount());
    entity.setCreateUser(resource.getCreateUser());
    entity.setCreateDate(resource.getCreateDate());
    entity.setUpdateUser(resource.getUpdateUser());
    entity.setUpdateDate(resource.getUpdateDate());
    
    return entity;
  }  

  @Override
  public ProgramAreaModel toModel(ProgramAreaEntity entity) {
    ProgramAreaModel resource = instantiateModel(entity);
     
    resource.add(linkTo(
        methodOn(ProgramAreaController.class)
        .getById(entity.getProgramAreaGuid()))
        .withSelfRel());
     
      resource.setProgramAreaGuid(entity.getProgramAreaGuid());
      resource.setProgramAreaName(entity.getProgramAreaName());
      resource.setRevisionCount(entity.getRevisionCount());
      resource.setCreateUser(entity.getCreateUser());
      resource.setCreateDate(entity.getCreateDate());
      resource.setUpdateUser(entity.getUpdateUser());
      resource.setUpdateDate(entity.getUpdateDate());

    return resource;
  }
  
  @Override
  public CollectionModel<ProgramAreaModel> toCollectionModel(Iterable<? extends ProgramAreaEntity> entities) 
  {
    CollectionModel<ProgramAreaModel> resources = super.toCollectionModel(entities);
     
    resources.add(linkTo(methodOn(ProgramAreaController.class).getAllProgramAreas()).withSelfRel());
     
    return resources;
  }
}
