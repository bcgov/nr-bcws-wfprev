package ca.bc.gov.nrs.wfprev.data.assemblers;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

import ca.bc.gov.nrs.wfprev.data.model.ExampleEntity;
import ca.bc.gov.nrs.wfprev.controllers.ExampleController;
import ca.bc.gov.nrs.wfprev.data.resources.ExampleModel;

/**
 * Resource Assmebler. This will construct the Resources from the Model objects
 * This does the translations needed to take a Resource (the json implementation)
 * and convert it to the DB representation, or vice versa. It's a basic DAO/DTO
 * transformer factory. This allows you to define Collection resources as well
 */

@Component
public class ExampleResourceAssembler extends RepresentationModelAssemblerSupport<ExampleEntity, ExampleModel>{

  public ExampleResourceAssembler() {
    super(ExampleController.class, ExampleModel.class);
  }

  public ExampleEntity toEntity(ExampleModel resource) {
    ExampleEntity entity = new ExampleEntity();

    entity.setExampleGuid(resource.getExampleGuid());
    entity.setExampleCode(resource.getExampleCode());
    entity.setExampleText(resource.getExampleText());
    entity.setExampleVar(resource.getExampleVar());
    entity.setExampleNum(resource.getExampleNum());
    entity.setExampleInd(resource.getExampleInd());
    entity.setLastUpdatedTimestamp(resource.getLastUpdatedTimestamp());
    entity.setRevisionCount(resource.getRevisionCount());
    entity.setCreatedBy(resource.getCreatedBy());
    entity.setCreateDate(resource.getCreateDate());
    entity.setUpdatedBy(resource.getUpdatedBy());
    entity.setUpdateDate(resource.getUpdateDate());

    return entity;
  }

  @Override
  public ExampleModel toModel(ExampleEntity entity) {
    ExampleModel resource = instantiateModel(entity);
     
    resource.add(linkTo(
        methodOn(ExampleController.class)
        .getExampleById(entity.getExampleGuid()))
        .withSelfRel());
     
      resource.setExampleGuid(entity.getExampleGuid());
      resource.setExampleCode(entity.getExampleCode());
      resource.setExampleText(entity.getExampleText());
      resource.setExampleVar(entity.getExampleVar());
      resource.setExampleNum(entity.getExampleNum());
      resource.setExampleInd(entity.getExampleInd());
      resource.setLastUpdatedTimestamp(entity.getLastUpdatedTimestamp());
      resource.setRevisionCount(entity.getRevisionCount());
      resource.setCreatedBy(entity.getCreatedBy());
      resource.setCreateDate(entity.getCreateDate());
      resource.setUpdatedBy(entity.getUpdatedBy());
      resource.setUpdateDate(entity.getUpdateDate());

    return resource;
  }
  
  @Override
  public CollectionModel<ExampleModel> toCollectionModel(Iterable<? extends ExampleEntity> entities) 
  {
    CollectionModel<ExampleModel> resources = super.toCollectionModel(entities);
     
    resources.add(linkTo(methodOn(ExampleController.class).getAllExamples()).withSelfRel());
     
    return resources;
  }
 
  private List<ExampleModel> toExampleModel(List<ExampleEntity> examples) {
    if (examples.isEmpty())
      return Collections.emptyList();
 
    return examples.stream()
        .map(example -> ExampleModel.builder()
          .exampleGuid(example.getExampleGuid())
          .createDate(example.getCreateDate())
          .createdBy(example.getCreatedBy())
          .exampleCode(example.getExampleCode())
          .exampleInd(example.getExampleInd())
          .exampleNum(example.getExampleNum())
          .exampleText(example.getExampleText())
          .exampleVar(example.getExampleVar())
          .lastUpdatedTimestamp(example.getLastUpdatedTimestamp())
          .revisionCount(example.getRevisionCount())
          .updateDate(example.getUpdateDate())
          .updatedBy(example.getUpdatedBy())
          .build()
          .add(linkTo(methodOn(ExampleController.class)
                .getExampleById(example.getExampleGuid()))
                .withSelfRel()))
        .collect(Collectors.toList());
  }
}
