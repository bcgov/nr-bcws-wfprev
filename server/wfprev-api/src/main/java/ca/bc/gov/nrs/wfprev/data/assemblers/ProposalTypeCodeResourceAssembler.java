package ca.bc.gov.nrs.wfprev.data.assemblers;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

import ca.bc.gov.nrs.wfprev.common.enums.CodeTables;
import ca.bc.gov.nrs.wfprev.controllers.CodesController;
import ca.bc.gov.nrs.wfprev.data.entities.ProposalTypeCodeEntity;
import ca.bc.gov.nrs.wfprev.data.models.ProposalTypeCodeModel;

@Component
public class ProposalTypeCodeResourceAssembler extends RepresentationModelAssemblerSupport<ProposalTypeCodeEntity, ProposalTypeCodeModel> {

  public ProposalTypeCodeResourceAssembler() {
    super(CodesController.class, ProposalTypeCodeModel.class);
  }

  public ProposalTypeCodeEntity toEntity(ProposalTypeCodeModel resource) {
    ProposalTypeCodeEntity entity = new ProposalTypeCodeEntity();

    entity.setProposalTypeCode(resource.getProposalTypeCode());
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
  public ProposalTypeCodeModel toModel(ProposalTypeCodeEntity entity) {
    ProposalTypeCodeModel resource = instantiateModel(entity);
     
    resource.add(linkTo(
        methodOn(CodesController.class)
        .getCodeById(CodeTables.PROPOSAL_TYPE_CODE, entity.getProposalTypeCode()))
        .withSelfRel());
     
      resource.setProposalTypeCode(entity.getProposalTypeCode());
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
  public CollectionModel<ProposalTypeCodeModel> toCollectionModel(Iterable<? extends ProposalTypeCodeEntity> entities) 
  {
    CollectionModel<ProposalTypeCodeModel> resources = super.toCollectionModel(entities);
     
    resources.add(linkTo(methodOn(CodesController.class).getCodes(CodeTables.PROPOSAL_TYPE_CODE)).withSelfRel());
     
    return resources;
  }
}
