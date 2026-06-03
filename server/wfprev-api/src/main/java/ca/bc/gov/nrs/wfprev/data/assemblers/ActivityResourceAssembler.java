package ca.bc.gov.nrs.wfprev.data.assemblers;

import ca.bc.gov.nrs.wfprev.controllers.ActivityController;
import ca.bc.gov.nrs.wfprev.data.entities.ActivityEntity;
import ca.bc.gov.nrs.wfprev.data.entities.ActivityStatusCodeEntity;
import ca.bc.gov.nrs.wfprev.data.entities.ContractPhaseCodeEntity;
import ca.bc.gov.nrs.wfprev.data.entities.RiskRatingCodeEntity;
import ca.bc.gov.nrs.wfprev.data.models.ActivityModel;
import ca.bc.gov.nrs.wfprev.data.models.ActivityStatusCodeModel;
import ca.bc.gov.nrs.wfprev.data.models.ContractPhaseCodeModel;
import ca.bc.gov.nrs.wfprev.data.models.RiskRatingCodeModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
public class ActivityResourceAssembler extends RepresentationModelAssemblerSupport<ActivityEntity, ActivityModel> {

    public ActivityResourceAssembler() {
        super(ActivityController.class, ActivityModel.class);
    }

    public ActivityEntity toEntity(ActivityModel resource) {
        ActivityEntity entity = new ActivityEntity();

        entity.setActivityGuid(UUID.fromString(resource.getActivityGuid()));
        if (resource.getProjectPlanFiscalGuid() != null) {
            entity.setProjectPlanFiscalGuid(UUID.fromString(resource.getProjectPlanFiscalGuid()));
        }
        if (resource.getActivityStatusCode() != null) {
            entity.setActivityStatusCode(toActivityStatusCodeEntity(resource.getActivityStatusCode()));
        }
        if (resource.getSilvicultureBaseGuid() != null) {
            entity.setSilvicultureBaseGuid(UUID.fromString(resource.getSilvicultureBaseGuid()));
        }
        if (resource.getSilvicultureTechniqueGuid() != null) {
            entity.setSilvicultureTechniqueGuid(UUID.fromString(resource.getSilvicultureTechniqueGuid()));
        }
        if (resource.getSilvicultureMethodGuid() != null) {
            entity.setSilvicultureMethodGuid(UUID.fromString(resource.getSilvicultureMethodGuid()));
        }
        if (resource.getRiskRatingCode() != null) {
            entity.setRiskRatingCode(toRiskRatingCodeEntity(resource.getRiskRatingCode()));
        }
        if (resource.getContractPhaseCode() != null) {
            entity.setContractPhaseCode(toContractPhaseCodeEntity(resource.getContractPhaseCode()));
        }
        if (resource.getActivityFundingSourceGuid() != null) {
            entity.setActivityFundingSourceGuid(UUID.fromString(resource.getActivityFundingSourceGuid()));
        }
        entity.setActivityName(resource.getActivityName());
        entity.setActivityDescription(resource.getActivityDescription());
        entity.setActivityStartDate(resource.getActivityStartDate());
        entity.setActivityEndDate(resource.getActivityEndDate());
        entity.setPlannedSpendAmount(resource.getPlannedSpendAmount());
        entity.setPlannedTreatmentAreaHa(resource.getPlannedTreatmentAreaHa());
        entity.setReportedSpendAmount(resource.getReportedSpendAmount());
        entity.setCompletedAreaHa(resource.getCompletedAreaHa());
        entity.setIsResultsReportableInd(resource.getIsResultsReportableInd());
        entity.setOutstandingObligationsInd(resource.getOutstandingObligationsInd());
        entity.setActivityComment(resource.getActivityComment());
        entity.setIsSpatialAddedInd(resource.getIsSpatialAddedInd());
        entity.setIsCarryForwardInd(resource.getIsCarryForwardInd());
        entity.setFinalOutcomeComments(resource.getFinalOutcomeComments());
        entity.setRevisionCount(resource.getRevisionCount());
        entity.setCreateUser(resource.getCreateUser());
        entity.setCreateDate(resource.getCreateDate());
        entity.setUpdateUser(resource.getUpdateUser());
        entity.setUpdateDate(resource.getUpdateDate());
        entity.setLastUpdatedTimestamp(resource.getLastUpdatedTimestamp());
        return entity;
    }

    @Override
    public ActivityModel toModel(ActivityEntity entity) {
        ActivityModel resource = instantiateModel(entity);

        resource.setActivityGuid(entity.getActivityGuid().toString());
        resource.setProjectPlanFiscalGuid(entity.getProjectPlanFiscalGuid() != null ?
                entity.getProjectPlanFiscalGuid().toString() : null);
        if (entity.getActivityStatusCode() != null) {
            resource.setActivityStatusCode(toActivityStatusCodeModel(entity.getActivityStatusCode()));
        }
        resource.setSilvicultureBaseGuid(entity.getSilvicultureBaseGuid() != null ?
                entity.getSilvicultureBaseGuid().toString() : null);
        resource.setSilvicultureTechniqueGuid(entity.getSilvicultureTechniqueGuid() != null ?
                entity.getSilvicultureTechniqueGuid().toString() : null);
        resource.setSilvicultureMethodGuid(entity.getSilvicultureMethodGuid() != null ?
                entity.getSilvicultureMethodGuid().toString() : null);
        if (entity.getRiskRatingCode() != null) {
            resource.setRiskRatingCode(toRiskRatingCodeModel(entity.getRiskRatingCode()));
        }
        if (entity.getContractPhaseCode() != null) {
            resource.setContractPhaseCode(toContractPhaseCodeModel(entity.getContractPhaseCode()));
        }
        resource.setActivityFundingSourceGuid(entity.getActivityFundingSourceGuid() != null ?
                entity.getActivityFundingSourceGuid().toString() : null);
        resource.setActivityName(entity.getActivityName());
        resource.setActivityDescription(entity.getActivityDescription());
        resource.setActivityStartDate(entity.getActivityStartDate());
        resource.setActivityEndDate(entity.getActivityEndDate());
        resource.setPlannedSpendAmount(entity.getPlannedSpendAmount());
        resource.setPlannedTreatmentAreaHa(entity.getPlannedTreatmentAreaHa());
        resource.setReportedSpendAmount(entity.getReportedSpendAmount());
        resource.setCompletedAreaHa(entity.getCompletedAreaHa());
        resource.setIsResultsReportableInd(entity.getIsResultsReportableInd());
        resource.setOutstandingObligationsInd(entity.getOutstandingObligationsInd());
        resource.setActivityComment(entity.getActivityComment());
        resource.setIsSpatialAddedInd(entity.getIsSpatialAddedInd());
        resource.setIsCarryForwardInd(entity.getIsCarryForwardInd());
        resource.setFinalOutcomeComments(entity.getFinalOutcomeComments());
        resource.setRevisionCount(entity.getRevisionCount());
        resource.setCreateUser(entity.getCreateUser());
        resource.setCreateDate(entity.getCreateDate());
        resource.setUpdateUser(entity.getUpdateUser());
        resource.setUpdateDate(entity.getUpdateDate());
        resource.setLastUpdatedTimestamp(entity.getLastUpdatedTimestamp());

        return resource;
    }

    @Override
    public CollectionModel<ActivityModel> toCollectionModel(Iterable<? extends ActivityEntity> entities) {
         return super.toCollectionModel(entities);
    }

    private ActivityStatusCodeModel toActivityStatusCodeModel(ActivityStatusCodeEntity code) {
        ActivityStatusCodeResourceAssembler ra = new ActivityStatusCodeResourceAssembler();
        return ra.toModel(code);
    }

    private ActivityStatusCodeEntity toActivityStatusCodeEntity(ActivityStatusCodeModel code) {
        if (code == null) return null;
        ActivityStatusCodeResourceAssembler ra = new ActivityStatusCodeResourceAssembler();
        return ra.toEntity(code);
    }

    private RiskRatingCodeModel toRiskRatingCodeModel(RiskRatingCodeEntity code) {
        RiskRatingCodeResourceAssembler ra = new RiskRatingCodeResourceAssembler();
        return ra.toModel(code);
    }

    private RiskRatingCodeEntity toRiskRatingCodeEntity(RiskRatingCodeModel code) {
        if (code == null) return null;
        RiskRatingCodeResourceAssembler ra = new RiskRatingCodeResourceAssembler();
        return ra.toEntity(code);
    }

    private ContractPhaseCodeModel toContractPhaseCodeModel(ContractPhaseCodeEntity code) {
        ContractPhaseCodeResourceAssembler ra = new ContractPhaseCodeResourceAssembler();
        return ra.toModel(code);
    }

    private ContractPhaseCodeEntity toContractPhaseCodeEntity(ContractPhaseCodeModel code) {
        if (code == null) return null;
        ContractPhaseCodeResourceAssembler ra = new ContractPhaseCodeResourceAssembler();
        return ra.toEntity(code);
    }

    public ActivityEntity updateEntity(ActivityModel model, ActivityEntity existingEntity) {
        log.debug(">> updateEntity");

        existingEntity.setProjectPlanFiscalGuid(nonNullOrDefault(
                model.getProjectPlanFiscalGuid() != null ? UUID.fromString(model.getProjectPlanFiscalGuid()) : null,
                existingEntity.getProjectPlanFiscalGuid()
        ));
        existingEntity.setActivityStatusCode(nonNullOrDefault(
                toActivityStatusCodeEntity(model.getActivityStatusCode()),
                existingEntity.getActivityStatusCode()
        ));
        existingEntity.setSilvicultureBaseGuid(model.getSilvicultureBaseGuid() != null ? UUID.fromString(model.getSilvicultureBaseGuid()) : null);
        existingEntity.setSilvicultureTechniqueGuid(model.getSilvicultureTechniqueGuid() != null ? UUID.fromString(model.getSilvicultureTechniqueGuid()) : null);
        existingEntity.setSilvicultureMethodGuid(model.getSilvicultureMethodGuid() != null ? UUID.fromString(model.getSilvicultureMethodGuid()) : null);
        existingEntity.setRiskRatingCode(nonNullOrDefault(
                toRiskRatingCodeEntity(model.getRiskRatingCode()),
                existingEntity.getRiskRatingCode()
        ));
        existingEntity.setContractPhaseCode(nonNullOrDefault(
                toContractPhaseCodeEntity(model.getContractPhaseCode()),
                existingEntity.getContractPhaseCode()
        ));
        existingEntity.setActivityFundingSourceGuid(nonNullOrDefault(
                model.getActivityFundingSourceGuid() != null ? UUID.fromString(model.getActivityFundingSourceGuid()) : null,
                existingEntity.getActivityFundingSourceGuid()
        ));
        existingEntity.setActivityName(nonNullOrDefault(model.getActivityName(), existingEntity.getActivityName()));
        existingEntity.setActivityDescription(nonNullOrDefault(model.getActivityDescription(), existingEntity.getActivityDescription()));
        existingEntity.setActivityStartDate(nonNullOrDefault(model.getActivityStartDate(), existingEntity.getActivityStartDate()));
        existingEntity.setActivityEndDate(nonNullOrDefault(model.getActivityEndDate(), existingEntity.getActivityEndDate()));
        existingEntity.setPlannedSpendAmount(nonNullOrDefault(model.getPlannedSpendAmount(), existingEntity.getPlannedSpendAmount()));
        existingEntity.setPlannedTreatmentAreaHa(nonNullOrDefault(model.getPlannedTreatmentAreaHa(), existingEntity.getPlannedTreatmentAreaHa()));
        existingEntity.setReportedSpendAmount(nonNullOrDefault(model.getReportedSpendAmount(), existingEntity.getReportedSpendAmount()));
        existingEntity.setCompletedAreaHa(nonNullOrDefault(model.getCompletedAreaHa(), existingEntity.getCompletedAreaHa()));
        existingEntity.setIsResultsReportableInd(nonNullOrDefault(model.getIsResultsReportableInd(), existingEntity.getIsResultsReportableInd()));
        existingEntity.setOutstandingObligationsInd(nonNullOrDefault(model.getOutstandingObligationsInd(), existingEntity.getOutstandingObligationsInd()));
        existingEntity.setActivityComment(nonNullOrDefault(model.getActivityComment(), existingEntity.getActivityComment()));
        existingEntity.setIsSpatialAddedInd(nonNullOrDefault(model.getIsSpatialAddedInd(), existingEntity.getIsSpatialAddedInd()));
        existingEntity.setIsCarryForwardInd(nonNullOrDefault(model.getIsCarryForwardInd(), existingEntity.getIsCarryForwardInd()));
        existingEntity.setFinalOutcomeComments(nonNullOrDefault(model.getFinalOutcomeComments(), existingEntity.getFinalOutcomeComments()));
        existingEntity.setCreateUser(existingEntity.getCreateUser());
        existingEntity.setCreateDate(existingEntity.getCreateDate());
        existingEntity.setUpdateUser(model.getUpdateUser());
        existingEntity.setUpdateDate(model.getUpdateDate());
        existingEntity.setLastUpdatedTimestamp(
            nonNullOrDefault(model.getLastUpdatedTimestamp(), existingEntity.getLastUpdatedTimestamp())
        );
        return existingEntity;
    }

    private <T> T nonNullOrDefault(T newValue, T existingValue) {
        return newValue != null ? newValue : existingValue;
    }
}