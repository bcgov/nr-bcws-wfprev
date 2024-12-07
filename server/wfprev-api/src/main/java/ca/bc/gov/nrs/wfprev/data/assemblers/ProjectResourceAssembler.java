package ca.bc.gov.nrs.wfprev.data.assemblers;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

import ca.bc.gov.nrs.wfprev.controllers.ProjectController;
import ca.bc.gov.nrs.wfprev.data.entities.ForestAreaCodeEntity;
import ca.bc.gov.nrs.wfprev.data.entities.GeneralScopeCodeEntity;
import ca.bc.gov.nrs.wfprev.data.entities.ProjectEntity;
import ca.bc.gov.nrs.wfprev.data.entities.ProjectTypeCodeEntity;
import ca.bc.gov.nrs.wfprev.data.models.ForestAreaCodeModel;
import ca.bc.gov.nrs.wfprev.data.models.GeneralScopeCodeModel;
import ca.bc.gov.nrs.wfprev.data.models.ProjectModel;
import ca.bc.gov.nrs.wfprev.data.models.ProjectTypeCodeModel;

import java.util.UUID;

@Component
public class ProjectResourceAssembler extends RepresentationModelAssemblerSupport<ProjectEntity, ProjectModel> {

    public ProjectResourceAssembler() {
        super(ProjectController.class, ProjectModel.class);
    }

    public ProjectEntity toEntity(ProjectModel resource) {
        ProjectEntity entity = new ProjectEntity();

        entity.setProjectGuid(UUID.fromString(resource.getProjectGuid()));
        entity.setProjectTypeCode(toProjectTypeCodeEntity(resource.getProjectTypeCode()));
        // Only set project number if it exists (for updates)
        if (resource.getProjectNumber() != null) {
            entity.setProjectNumber(resource.getProjectNumber());
        }
        entity.setSiteUnitName(resource.getSiteUnitName());
        if (resource.getForestAreaCode() != null) {
            entity.setForestAreaCode(toForestAreaCodeEntity(resource.getForestAreaCode()));
        }
        entity.setGeneralScopeCode(toGeneralScopeCodeEntity(resource.getGeneralScopeCode()));
        entity.setProgramAreaGuid(UUID.fromString(resource.getProgramAreaGuid()));
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
        entity.setTotalActualProjectSizeHa(resource.getTotalActualProjectSizeHa());
        entity.setTotalActualCostPerHectareAmount(resource.getTotalActualCostPerHectareAmount());
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
                        .getById(entity.getProgramAreaGuid().toString()))
                .withSelfRel());

        resource.setProjectGuid(entity.getProjectGuid().toString());
        resource.setProjectTypeCode(toProjectTypeCodeModel(entity.getProjectTypeCode()));
        resource.setProjectNumber(entity.getProjectNumber());
        resource.setSiteUnitName(entity.getSiteUnitName());
        if (entity.getForestAreaCode() != null) {
            resource.setForestAreaCode(toForestAreaCodeModel(entity.getForestAreaCode()));
        }
        resource.setGeneralScopeCode(toGeneralScopeCodeModel(entity.getGeneralScopeCode()));
        resource.setProgramAreaGuid(entity.getProgramAreaGuid().toString());
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
        resource.setTotalActualProjectSizeHa(entity.getTotalActualProjectSizeHa());
        resource.setTotalActualCostPerHectareAmount(entity.getTotalActualCostPerHectareAmount());
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
    public CollectionModel<ProjectModel> toCollectionModel(Iterable<? extends ProjectEntity> entities) {
        CollectionModel<ProjectModel> resources = super.toCollectionModel(entities);
        resources.add(linkTo(methodOn(ProjectController.class).getAllProjects()).withSelfRel());
        return resources;
    }

    private ProjectTypeCodeModel toProjectTypeCodeModel(ProjectTypeCodeEntity code) {
        ProjectTypeCodeResourceAssembler ra = new ProjectTypeCodeResourceAssembler();
        return ra.toModel(code);
    }

    private ProjectTypeCodeEntity toProjectTypeCodeEntity(ProjectTypeCodeModel code) {
        ProjectTypeCodeResourceAssembler ra = new ProjectTypeCodeResourceAssembler();
        return ra.toEntity(code);
    }

    private ForestAreaCodeModel toForestAreaCodeModel(ForestAreaCodeEntity code) {
        ForestAreaCodeResourceAssembler ra = new ForestAreaCodeResourceAssembler();
        return ra.toModel(code);
    }

    private ForestAreaCodeEntity toForestAreaCodeEntity(ForestAreaCodeModel code) {
        ForestAreaCodeResourceAssembler ra = new ForestAreaCodeResourceAssembler();
        return ra.toEntity(code);
    }

    private GeneralScopeCodeModel toGeneralScopeCodeModel(GeneralScopeCodeEntity code) {
        GeneralScopeCodeResourceAssembler ra = new GeneralScopeCodeResourceAssembler();
        return ra.toModel(code);
    }

    private GeneralScopeCodeEntity toGeneralScopeCodeEntity(GeneralScopeCodeModel code) {
        GeneralScopeCodeResourceAssembler ra = new GeneralScopeCodeResourceAssembler();
        return ra.toEntity(code);
    }
}