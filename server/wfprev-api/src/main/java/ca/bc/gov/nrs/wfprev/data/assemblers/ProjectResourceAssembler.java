package ca.bc.gov.nrs.wfprev.data.assemblers;

import ca.bc.gov.nrs.wfprev.controllers.ProjectController;
import ca.bc.gov.nrs.wfprev.data.entities.ForestAreaCodeEntity;
import ca.bc.gov.nrs.wfprev.data.entities.GeneralScopeCodeEntity;
import ca.bc.gov.nrs.wfprev.data.entities.ProjectEntity;
import ca.bc.gov.nrs.wfprev.data.entities.ProjectTypeCodeEntity;
import ca.bc.gov.nrs.wfprev.data.models.ForestAreaCodeModel;
import ca.bc.gov.nrs.wfprev.data.models.GeneralScopeCodeModel;
import ca.bc.gov.nrs.wfprev.data.models.ProjectModel;
import ca.bc.gov.nrs.wfprev.data.models.ProjectTypeCodeModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

import java.util.UUID;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Slf4j
@Component
public class ProjectResourceAssembler extends RepresentationModelAssemblerSupport<ProjectEntity, ProjectModel> {

    public ProjectResourceAssembler() {
        super(ProjectController.class, ProjectModel.class);
    }

    public ProjectEntity toEntity(ProjectModel resource) {
        ProjectEntity entity = new ProjectEntity();

        entity.setProjectGuid(UUID.fromString(resource.getProjectGuid()));
        if (resource.getProjectTypeCode() != null) {
            entity.setProjectTypeCode(toProjectTypeCodeEntity(resource.getProjectTypeCode()));
        }
        // Only set project number if it exists (for updates)
        if (resource.getProjectNumber() != null) {
            entity.setProjectNumber(resource.getProjectNumber());
        }
        entity.setSiteUnitName(resource.getSiteUnitName());
        if (resource.getForestAreaCode() != null) {
            entity.setForestAreaCode(toForestAreaCodeEntity(resource.getForestAreaCode()));
        }
        if (resource.getGeneralScopeCode() != null) {
            entity.setGeneralScopeCode(toGeneralScopeCodeEntity(resource.getGeneralScopeCode()));
        }
        if (resource.getProgramAreaGuid() != null) {
            entity.setProgramAreaGuid(UUID.fromString(resource.getProgramAreaGuid()));
        }
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
        if (entity.getGeneralScopeCode() != null) {
            resource.setGeneralScopeCode(toGeneralScopeCodeModel(entity.getGeneralScopeCode()));
        }
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
        if (code == null) return null;
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

    public ProjectEntity updateEntity(ProjectModel model, ProjectEntity existingEntity) {
        log.debug(">> updateEntity");
        System.out.println("In updateEntity");
        ProjectEntity entity = new ProjectEntity();
        entity.setProjectGuid(existingEntity.getProjectGuid());
        entity.setProjectName(nonNullOrDefault(model.getProjectName(), existingEntity.getProjectName()));
        entity.setProjectDescription(nonNullOrDefault(model.getProjectDescription(), existingEntity.getProjectDescription()));
        entity.setTotalActualProjectSizeHa(nonNullOrDefault(model.getTotalActualProjectSizeHa(), existingEntity.getTotalActualProjectSizeHa()));
        entity.setTotalActualCostPerHectareAmount(nonNullOrDefault(model.getTotalActualCostPerHectareAmount(), existingEntity.getTotalActualCostPerHectareAmount()));
        entity.setTotalActualAmount(nonNullOrDefault(model.getTotalActualAmount(), existingEntity.getTotalActualAmount()));
        entity.setTotalAllocatedAmount(nonNullOrDefault(model.getTotalAllocatedAmount(), existingEntity.getTotalAllocatedAmount()));
        entity.setTotalFundingRequestAmount(nonNullOrDefault(model.getTotalFundingRequestAmount(), existingEntity.getTotalFundingRequestAmount()));
        entity.setTotalPlannedCostPerHectare(nonNullOrDefault(model.getTotalPlannedCostPerHectare(), existingEntity.getTotalPlannedCostPerHectare()));
        entity.setTotalPlannedProjectSizeHa(nonNullOrDefault(model.getTotalPlannedProjectSizeHa(), existingEntity.getTotalPlannedProjectSizeHa()));
        entity.setIsMultiFiscalYearProj(nonNullOrDefault(model.getIsMultiFiscalYearProj(), existingEntity.getIsMultiFiscalYearProj()));
        entity.setLatitude(nonNullOrDefault(model.getLatitude(), existingEntity.getLatitude()));
        entity.setLongitude(nonNullOrDefault(model.getLongitude(), existingEntity.getLongitude()));
        entity.setForestAreaCode(nonNullOrDefault(toForestAreaCodeEntity(model.getForestAreaCode()), existingEntity.getForestAreaCode()));
        entity.setRevisionCount(nonNullOrDefault(model.getRevisionCount(), existingEntity.getRevisionCount()));
        entity.setProjectStatusCode(existingEntity.getProjectStatusCode());
        entity.setSiteUnitName(nonNullOrDefault(model.getSiteUnitName(), existingEntity.getSiteUnitName()));
        entity.setProgramAreaGuid(
            nonNullOrDefault(
                model.getProgramAreaGuid() != null ? UUID.fromString(model.getProgramAreaGuid()) : null,
                existingEntity.getProgramAreaGuid()
            )
        );
        entity.setForestRegionOrgUnitId(nonNullOrDefault(model.getForestRegionOrgUnitId(), existingEntity.getForestRegionOrgUnitId()));
        entity.setForestDistrictOrgUnitId(nonNullOrDefault(model.getForestDistrictOrgUnitId(), existingEntity.getForestDistrictOrgUnitId()));
        entity.setFireCentreOrgUnitId(nonNullOrDefault(model.getFireCentreOrgUnitId(), existingEntity.getFireCentreOrgUnitId()));
        entity.setBcParksRegionOrgUnitId(nonNullOrDefault(model.getBcParksRegionOrgUnitId(), existingEntity.getBcParksRegionOrgUnitId()));
        entity.setBcParksSectionOrgUnitId(nonNullOrDefault(model.getBcParksSectionOrgUnitId(), existingEntity.getBcParksSectionOrgUnitId()));
        entity.setProjectLead(nonNullOrDefault(model.getProjectLead(), existingEntity.getProjectLead()));
        entity.setProjectLeadEmailAddress(nonNullOrDefault(model.getProjectLeadEmailAddress(), existingEntity.getProjectLeadEmailAddress()));
        entity.setClosestCommunityName(nonNullOrDefault(model.getClosestCommunityName(), existingEntity.getClosestCommunityName()));
        entity.setLastProgressUpdateTimestamp(nonNullOrDefault(model.getLastProgressUpdateTimestamp(), existingEntity.getLastProgressUpdateTimestamp()));
        entity.setCreateUser(existingEntity.getCreateUser());
        entity.setCreateDate(existingEntity.getCreateDate());
        entity.setUpdateUser(existingEntity.getUpdateUser());
        entity.setUpdateDate(existingEntity.getUpdateDate());
        entity.setProjectTypeCode(existingEntity.getProjectTypeCode());
        entity.setGeneralScopeCode(existingEntity.getGeneralScopeCode());
        entity.setProjectNumber(existingEntity.getProjectNumber());

        log.error("Updated entity: " + entity);
        return entity;
    }

    private <T> T nonNullOrDefault(T newValue, T existingValue) {
        return newValue != null ? newValue : existingValue;
    }
}