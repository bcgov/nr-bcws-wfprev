package ca.bc.gov.nrs.wfprev.data.assemblers;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

import ca.bc.gov.nrs.wfprev.data.model.ExampleCodeEntity;
import ca.bc.gov.nrs.wfprev.controllers.ExampleController;
import ca.bc.gov.nrs.wfprev.data.resources.ExampleCodeModel;

@Component
public class ExampleCodeResourceAssembler extends RepresentationModelAssemblerSupport<ExampleCodeEntity, ExampleCodeModel> {

  public ExampleCodeResourceAssembler() {
    super(ExampleController.class, ExampleCodeModel.class);
  }

  public ExampleCodeEntity toEntity(ExampleCodeModel resource) {
    ExampleCodeEntity entity = new ExampleCodeEntity();

    entity.setExampleCode(resource.getExampleCode());
    entity.setDescription(resource.getDescription());
    entity.setDisplayOrder(resource.getDisplayOrder());
    entity.setEffectiveDate(resource.getEffectiveDate());
    entity.setExpiryDate(resource.getExpiryDate());
    entity.setRevisionCount(resource.getRevisionCount());
    entity.setCreatedBy(resource.getCreatedBy());
    entity.setCreateDate(resource.getCreateDate());
    entity.setUpdatedBy(resource.getUpdatedBy());
    entity.setUpdateDate(resource.getUpdateDate());
    
    return entity;
  }  

  @Override
  public ExampleCodeModel toModel(ExampleCodeEntity entity) {
    ExampleCodeModel resource = instantiateModel(entity);
     
    resource.add(linkTo(
        methodOn(ExampleController.class)
        .getExampleCodeById(entity.getExampleCode()))
        .withSelfRel());
     
      resource.setExampleCode(entity.getExampleCode());
      resource.setDescription(entity.getDescription());
      resource.setDisplayOrder(entity.getDisplayOrder());
      resource.setEffectiveDate(entity.getEffectiveDate());
      resource.setExpiryDate(entity.getExpiryDate());
      resource.setRevisionCount(entity.getRevisionCount());
      resource.setCreatedBy(entity.getCreatedBy());
      resource.setCreateDate(entity.getCreateDate());
      resource.setUpdatedBy(entity.getUpdatedBy());
      resource.setUpdateDate(entity.getUpdateDate());

    return resource;
  }
  
  @Override
  public CollectionModel<ExampleCodeModel> toCollectionModel(Iterable<? extends ExampleCodeEntity> entities) 
  {
    CollectionModel<ExampleCodeModel> resources = super.toCollectionModel(entities);
     
    resources.add(linkTo(methodOn(ExampleController.class).getAllExampleCodes()).withSelfRel());
     
    return resources;
  }
 
  private List<ExampleCodeModel> toExampleCodeModel(List<ExampleCodeEntity> exampleCodes) {
    if (exampleCodes.isEmpty())
      return Collections.emptyList();
 
    return exampleCodes.stream()
        .map(exampleCode -> ExampleCodeModel.builder()
          .exampleCode(exampleCode.getExampleCode())
          .createDate(exampleCode.getCreateDate())
          .createdBy(exampleCode.getCreatedBy())
          .description(exampleCode.getDescription())
          .displayOrder(exampleCode.getDisplayOrder())
          .effectiveDate(exampleCode.getEffectiveDate())
          .expiryDate(exampleCode.getExpiryDate())
          .revisionCount(exampleCode.getRevisionCount())
          .updateDate(exampleCode.getUpdateDate())
          .updatedBy(exampleCode.getUpdatedBy())
          .build().add(linkTo(methodOn(ExampleController.class)
          .getExampleCodeById(exampleCode.getExampleCode()))
          .withSelfRel()))
        .collect(Collectors.toList());
  }
}
