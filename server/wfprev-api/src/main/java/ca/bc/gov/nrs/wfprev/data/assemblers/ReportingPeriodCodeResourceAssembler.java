package ca.bc.gov.nrs.wfprev.data.assemblers;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;
import ca.bc.gov.nrs.wfprev.common.enums.CodeTables;
import ca.bc.gov.nrs.wfprev.controllers.CodesController;
import ca.bc.gov.nrs.wfprev.data.entities.ReportingPeriodCodeEntity;
import ca.bc.gov.nrs.wfprev.data.models.ReportingPeriodCodeModel;

@Component
public class ReportingPeriodCodeResourceAssembler extends RepresentationModelAssemblerSupport<ReportingPeriodCodeEntity, ReportingPeriodCodeModel> {

  public ReportingPeriodCodeResourceAssembler() {
    super(CodesController.class, ReportingPeriodCodeModel.class);
  }

  public ReportingPeriodCodeEntity toEntity(ReportingPeriodCodeModel resource) {
    if (resource == null) {
        return null;
    }
    ReportingPeriodCodeEntity entity = new ReportingPeriodCodeEntity();

    entity.setReportingPeriodCode(resource.getReportingPeriodCode());
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
  public ReportingPeriodCodeModel toModel(ReportingPeriodCodeEntity entity) {
    ReportingPeriodCodeModel resource = instantiateModel(entity);
     
    resource.add(linkTo(
        methodOn(CodesController.class)
        .getCodeById(CodeTables.REPORTING_PERIOD_CODE, entity.getReportingPeriodCode()))
        .withSelfRel());
     
      resource.setReportingPeriodCode(entity.getReportingPeriodCode());
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
  public CollectionModel<ReportingPeriodCodeModel> toCollectionModel(Iterable<? extends ReportingPeriodCodeEntity> entities) 
  {
    CollectionModel<ReportingPeriodCodeModel> resources = super.toCollectionModel(entities);
     
    resources.add(linkTo(methodOn(CodesController.class).getCodes(CodeTables.REPORTING_PERIOD_CODE)).withSelfRel());
     
    return resources;
  }
}
