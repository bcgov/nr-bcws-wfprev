package ca.bc.gov.nrs.wfprev.data.assemblers;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

import ca.bc.gov.nrs.wfprev.data.model.ProjectEntity;
import ca.bc.gov.nrs.wfprev.controllers.ProjectController;
import ca.bc.gov.nrs.wfprev.data.resources.ProjectModel;

@Component
public class ProjectResourceAssembler extends RepresentationModelAssemblerSupport<ProjectEntity, ProjectModel> {

  public ProjectResourceAssembler() {
    super(ProjectController.class, ProjectModel.class);
  }

  public ProjectEntity toEntity(ProjectModel resource) {
    ProjectEntity entity = new ProjectEntity();

    entity.setProjectGuid(resource.getProjectGuid());
    entity.setProjectTypeCode(resource.getProjectTypeCode());
    entity.setProjectNumber(resource.getProjectNumber());
    entity.setSiteUnitName(resource.getSiteUnitName());
    entity.setForestAreaCode(resource.getForestAreaCode());
    entity.setGeneralScopeCode(resource.getGeneralScopeCode());
    entity.setProgramAreaGuid(resource.getProgramAreaGuid());
    entity.setForestRegionOrgUnitId(resource.getForestRegionOrgUnitId());
    entity.setForestDistrictOrgUnitId(resource.getForestDistrictOrgUnitId());
    entity.setFireCentreOrgUnitId(resource.getFireCentreOrgUnitId());
    entity.setBcParksRegionOrgUnitId(resource.getBcParksRegionOrgUnitId());
    entity.setBcParksSectionOrgUnitId(resource.getBcParksSectionOrgUnitId());
    entity.setProjectName(resource.getProjectName());
    entity.setProjectLead(resource.getProjectLead());
    entity.setProjectLeadEmailAddress(resource.getProjectLeadEmailAddress());
    entity.setProjectDescription(resource.getProjectDescription());
    entity.setClosestCommunityName(resource.getClosestCommunityName());
    entity.setTotalFundingRequestAmount(resource.getTotalFundingRequestAmount());
    entity.setTotalAllocatedAmount(resource.getTotalAllocatedAmount());
    entity.setTotalPlannedProjectSizeHa(resource.getTotalPlannedProjectSizeHa());
    entity.setTotalPlannedCostPerHectare(resource.getTotalPlannedCostPerHectare());
    entity.setTotalActualAmount(resource.getTotalActualAmount());
    entity.setTotalProjectSizeHa(resource.getTotalProjectSizeHa());
    entity.setTotalCostPerHectareAmount(resource.getTotalCostPerHectareAmount());
    entity.setIsMultiFiscalYearProj(resource.getIsMultiFiscalYearProj());
    entity.setLatitude(resource.getLatitude());
    entity.setLongitude(resource.getLongitude());
    entity.setLastProgressUpdateTimestamp(resource.getLastProgressUpdateTimestamp());
    entity.setRevisionCount(resource.getRevisionCount());
    entity.setCreateUser(resource.getCreateUser());
    entity.setCreateDate(resource.getCreateDate());
    entity.setUpdateUser(resource.getUpdateUser());
    entity.setUpdateDate(resource.getUpdateDate());
    
    return entity;
  }  

  @Override
  public ProjectModel toModel(ProjectEntity entity) {
    ProjectModel resource = instantiateModel(entity);
     
    resource.add(linkTo(
        methodOn(ProjectController.class)
        .getById(entity.getProgramAreaGuid()))
        .withSelfRel());
     
        resource.setProjectGuid(entity.getProjectGuid());
        resource.setProjectTypeCode(entity.getProjectTypeCode());
        resource.setProjectNumber(entity.getProjectNumber());
        resource.setSiteUnitName(entity.getSiteUnitName());
        resource.setForestAreaCode(entity.getForestAreaCode());
        resource.setGeneralScopeCode(entity.getGeneralScopeCode());
        resource.setProgramAreaGuid(entity.getProgramAreaGuid());
        resource.setForestRegionOrgUnitId(entity.getForestRegionOrgUnitId());
        resource.setForestDistrictOrgUnitId(entity.getForestDistrictOrgUnitId());
        resource.setFireCentreOrgUnitId(entity.getFireCentreOrgUnitId());
        resource.setBcParksRegionOrgUnitId(entity.getBcParksRegionOrgUnitId());
        resource.setBcParksSectionOrgUnitId(entity.getBcParksSectionOrgUnitId());
        resource.setProjectName(entity.getProjectName());
        resource.setProjectLead(entity.getProjectLead());
        resource.setProjectLeadEmailAddress(entity.getProjectLeadEmailAddress());
        resource.setProjectDescription(entity.getProjectDescription());
        resource.setClosestCommunityName(entity.getClosestCommunityName());
        resource.setTotalFundingRequestAmount(entity.getTotalFundingRequestAmount());
        resource.setTotalAllocatedAmount(entity.getTotalAllocatedAmount());
        resource.setTotalPlannedProjectSizeHa(entity.getTotalPlannedProjectSizeHa());
        resource.setTotalPlannedCostPerHectare(entity.getTotalPlannedCostPerHectare());
        resource.setTotalActualAmount(entity.getTotalActualAmount());
        resource.setTotalProjectSizeHa(entity.getTotalProjectSizeHa());
        resource.setTotalCostPerHectareAmount(entity.getTotalCostPerHectareAmount());
        resource.setIsMultiFiscalYearProj(entity.getIsMultiFiscalYearProj());
        resource.setLatitude(entity.getLatitude());
        resource.setLongitude(entity.getLongitude());
        resource.setLastProgressUpdateTimestamp(entity.getLastProgressUpdateTimestamp());
        resource.setRevisionCount(entity.getRevisionCount());
        resource.setCreateUser(entity.getCreateUser());
        resource.setCreateDate(entity.getCreateDate());
        resource.setUpdateUser(entity.getUpdateUser());
        resource.setUpdateDate(entity.getUpdateDate());

    return resource;
  }
  
  @Override
  public CollectionModel<ProjectModel> toCollectionModel(Iterable<? extends ProjectEntity> entities) 
  {
    CollectionModel<ProjectModel> resources = super.toCollectionModel(entities);
     
    resources.add(linkTo(methodOn(ProjectController.class).getAllProjects()).withSelfRel());
     
    return resources;
  }
}
