package ca.bc.gov.nrs.wfprev.data.assemblers;

import ca.bc.gov.nrs.wfprev.controllers.ProjectController;
import ca.bc.gov.nrs.wfprev.controllers.ProjectFiscalController;
import ca.bc.gov.nrs.wfprev.data.entities.ProjectEntity;
import ca.bc.gov.nrs.wfprev.data.entities.ProjectFiscalEntity;
import ca.bc.gov.nrs.wfprev.data.models.ProjectFiscalModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Slf4j
@Component
public class ProjectFiscalResourceAssembler extends RepresentationModelAssemblerSupport<ProjectFiscalEntity, ProjectFiscalModel> {

    public ProjectFiscalResourceAssembler() {
        super(ProjectFiscalController.class, ProjectFiscalModel.class);
    }

    @Override
    public ProjectFiscalModel toModel(final ProjectFiscalEntity entity) {
        if (entity == null) {
            return null;
        }

        return ProjectFiscalModel.builder()
                .projectPlanFiscalGuid(entity.getProjectPlanFiscalGuid() != null ? entity.getProjectPlanFiscalGuid().toString() : null)
                .projectGuid(entity.getProject() != null && entity.getProject().getProjectGuid() != null
                        ? entity.getProject().getProjectGuid().toString()
                        : null)
                .activityCategoryCode(entity.getActivityCategoryCode())
                .fiscalYear(entity.getFiscalYear() != null ? entity.getFiscalYear().longValue() : null)
                .ancillaryFundingSourceGuid(entity.getAncillaryFundingSource() != null
                        ? entity.getAncillaryFundingSource().getAncillaryFundingSourceGuid().toString()
                        : null)
                .projectPlanStatusCode(entity.getProjectPlanStatusCode())
                .planFiscalStatusCode(entity.getPlanFiscalStatusCode())
                .endorsementCode(entity.getEndorsementCode())
                .projectFiscalName(entity.getProjectFiscalName())
                .projectFiscalDescription(entity.getProjectFiscalDescription())
                .businessAreaComment(entity.getBusinessAreaComment())
                .estimatedClwrrAllocAmount(entity.getEstimatedClwrrAllocAmount())
                .totalCostEstimateAmount(entity.getTotalCostEstimateAmount())
                .cfsProjectCode(entity.getCfsProjectCode())
                .fiscalFundingRequestAmount(entity.getFiscalFundingRequestAmount())
                .fiscalFundingAllocRationale(entity.getFiscalFundingAllocRationale())
                .fiscalAllocatedAmount(entity.getFiscalAllocatedAmount())
                .fiscalAncillaryFundAmount(entity.getFiscalAncillaryFundAmount())
                .fiscalPlannedProjectSizeHa(entity.getFiscalPlannedProjectSizeHa())
                .fiscalPlannedCostPerHaAmt(entity.getFiscalPlannedCostPerHaAmt())
                .fiscalReportedSpendAmount(entity.getFiscalReportedSpendAmount())
                .fiscalActualAmount(entity.getFiscalActualAmount())
                .fiscalCompletedSizeHa(entity.getFiscalCompletedSizeHa())
                .fiscalActualCostPerHaAmt(entity.getFiscalActualCostPerHaAmt())
                .firstNationsDelivPartInd(entity.getFirstNationsDelivPartInd())
                .firstNationsEngagementInd(entity.getFirstNationsEngagementInd())
                .firstNationsPartner(entity.getFirstNationsPartner())
                .otherPartner(entity.getOtherPartner())
                .resultsNumber(entity.getResultsNumber())
                .resultsOpeningId(entity.getResultsOpeningId())
                .resultsContactEmail(entity.getResultsContactEmail())
                .submittedByName(entity.getSubmittedByName())
                .submittedByUserGuid(entity.getSubmittedByUserGuid())
                .submittedByUserUserid(entity.getSubmittedByUserUserid())
                .submissionTimestamp(entity.getSubmissionTimestamp())
                .endorsementEvalTimestamp(entity.getEndorsementEvalTimestamp())
                .endorserName(entity.getEndorserName())
                .endorserUserGuid(entity.getEndorserUserGuid())
                .endorserUserUserid(entity.getEndorserUserUserid())
                .endorsementTimestamp(entity.getEndorsementTimestamp())
                .endorsementComment(entity.getEndorsementComment())
                .isApprovedInd(entity.getIsApprovedInd())
                .approverName(entity.getApproverName())
                .approverUserGuid(entity.getApproverUserGuid())
                .approverUserUserid(entity.getApproverUserUserid())
                .approvedTimestamp(entity.getApprovedTimestamp())
                .accomplishmentsComment(entity.getAccomplishmentsComment())
                .isDelayedInd(entity.getIsDelayedInd())
                .delayRationale(entity.getDelayRationale())
                .abandonedRationale(entity.getAbandonedRationale())
                .lastProgressUpdateTimestamp(entity.getLastProgressUpdateTimestamp())
                .build();
    }

    @Override
    public CollectionModel<ProjectFiscalModel> toCollectionModel(final Iterable<? extends ProjectFiscalEntity> entities) {
        CollectionModel<ProjectFiscalModel> resources = super.toCollectionModel(entities);
        resources.add(linkTo(methodOn(ProjectController.class).getAllProjects()).withSelfRel());
        return resources;
    }


    public ProjectFiscalEntity toEntity(ProjectFiscalModel model) {
        if (model == null) {
            return null;
        }

        ProjectFiscalEntity entity = new ProjectFiscalEntity();
        entity.setProjectPlanFiscalGuid(model.getProjectPlanFiscalGuid() != null
                ? UUID.fromString(model.getProjectPlanFiscalGuid())
                : null);
        entity.setActivityCategoryCode(model.getActivityCategoryCode());
        entity.setFiscalYear(model.getFiscalYear() != null
                ? BigDecimal.valueOf(model.getFiscalYear())
                : null);
        entity.setProjectPlanStatusCode(model.getProjectPlanStatusCode());
        entity.setPlanFiscalStatusCode(model.getPlanFiscalStatusCode());
        entity.setEndorsementCode(model.getEndorsementCode());
        entity.setProjectFiscalName(model.getProjectFiscalName());
        entity.setProjectFiscalDescription(model.getProjectFiscalDescription());
        entity.setBusinessAreaComment(model.getBusinessAreaComment());
        entity.setEstimatedClwrrAllocAmount(model.getEstimatedClwrrAllocAmount());
        entity.setTotalCostEstimateAmount(model.getTotalCostEstimateAmount());
        entity.setCfsProjectCode(model.getCfsProjectCode());
        entity.setFiscalFundingRequestAmount(model.getFiscalFundingRequestAmount());
        entity.setFiscalFundingAllocRationale(model.getFiscalFundingAllocRationale());
        entity.setFiscalAllocatedAmount(model.getFiscalAllocatedAmount());
        entity.setFiscalAncillaryFundAmount(model.getFiscalAncillaryFundAmount());
        entity.setFiscalPlannedProjectSizeHa(model.getFiscalPlannedProjectSizeHa());
        entity.setFiscalPlannedCostPerHaAmt(model.getFiscalPlannedCostPerHaAmt());
        entity.setFiscalReportedSpendAmount(model.getFiscalReportedSpendAmount());
        entity.setFiscalActualAmount(model.getFiscalActualAmount());
        entity.setFiscalCompletedSizeHa(model.getFiscalCompletedSizeHa());
        entity.setFiscalActualCostPerHaAmt(model.getFiscalActualCostPerHaAmt());
        entity.setFirstNationsDelivPartInd(model.getFirstNationsDelivPartInd());
        entity.setFirstNationsEngagementInd(model.getFirstNationsEngagementInd());
        entity.setFirstNationsPartner(model.getFirstNationsPartner());
        entity.setOtherPartner(model.getOtherPartner());
        entity.setResultsNumber(model.getResultsNumber());
        entity.setResultsOpeningId(model.getResultsOpeningId());
        entity.setResultsContactEmail(model.getResultsContactEmail());
        entity.setSubmittedByName(model.getSubmittedByName());
        entity.setSubmittedByUserGuid(model.getSubmittedByUserGuid());
        entity.setSubmittedByUserUserid(model.getSubmittedByUserUserid());
        entity.setSubmissionTimestamp(model.getSubmissionTimestamp());
        entity.setEndorsementEvalTimestamp(model.getEndorsementEvalTimestamp());
        entity.setEndorserName(model.getEndorserName());
        entity.setEndorserUserGuid(model.getEndorserUserGuid());
        entity.setEndorserUserUserid(model.getEndorserUserUserid());
        entity.setEndorsementTimestamp(model.getEndorsementTimestamp());
        entity.setEndorsementComment(model.getEndorsementComment());
        entity.setIsApprovedInd(model.getIsApprovedInd());
        entity.setApproverName(model.getApproverName());
        entity.setApproverUserGuid(model.getApproverUserGuid());
        entity.setApproverUserUserid(model.getApproverUserUserid());
        entity.setApprovedTimestamp(model.getApprovedTimestamp());
        entity.setAccomplishmentsComment(model.getAccomplishmentsComment());
        entity.setIsDelayedInd(model.getIsDelayedInd());
        entity.setDelayRationale(model.getDelayRationale());
        entity.setAbandonedRationale(model.getAbandonedRationale());
        entity.setLastProgressUpdateTimestamp(model.getLastProgressUpdateTimestamp());

        return entity;
    }

    public ProjectFiscalEntity updateEntity(ProjectFiscalModel projectFiscalModel, ProjectFiscalEntity existingEntity) {
        if (projectFiscalModel == null || existingEntity == null) {
            throw new IllegalArgumentException("Model and entity must not be null");
        }

        existingEntity.setActivityCategoryCode(
                nonNullOrDefault(projectFiscalModel.getActivityCategoryCode(), existingEntity.getActivityCategoryCode()));
        existingEntity.setFiscalYear(
                projectFiscalModel.getFiscalYear() != null
                        ? BigDecimal.valueOf(projectFiscalModel.getFiscalYear())
                        : existingEntity.getFiscalYear());
        existingEntity.setProjectPlanStatusCode(
                nonNullOrDefault(projectFiscalModel.getProjectPlanStatusCode(), existingEntity.getProjectPlanStatusCode()));
        existingEntity.setPlanFiscalStatusCode(
                nonNullOrDefault(projectFiscalModel.getPlanFiscalStatusCode(), existingEntity.getPlanFiscalStatusCode()));
        existingEntity.setEndorsementCode(
                nonNullOrDefault(projectFiscalModel.getEndorsementCode(), existingEntity.getEndorsementCode()));
        existingEntity.setProjectFiscalName(
                nonNullOrDefault(projectFiscalModel.getProjectFiscalName(), existingEntity.getProjectFiscalName()));
        existingEntity.setProjectFiscalDescription(
                nonNullOrDefault(projectFiscalModel.getProjectFiscalDescription(), existingEntity.getProjectFiscalDescription()));
        existingEntity.setBusinessAreaComment(
                nonNullOrDefault(projectFiscalModel.getBusinessAreaComment(), existingEntity.getBusinessAreaComment()));
        existingEntity.setEstimatedClwrrAllocAmount(
                nonNullOrDefault(projectFiscalModel.getEstimatedClwrrAllocAmount(), existingEntity.getEstimatedClwrrAllocAmount()));
        existingEntity.setTotalCostEstimateAmount(
                nonNullOrDefault(projectFiscalModel.getTotalCostEstimateAmount(), existingEntity.getTotalCostEstimateAmount()));
        existingEntity.setCfsProjectCode(
                nonNullOrDefault(projectFiscalModel.getCfsProjectCode(), existingEntity.getCfsProjectCode()));
        existingEntity.setFiscalFundingRequestAmount(
                nonNullOrDefault(projectFiscalModel.getFiscalFundingRequestAmount(), existingEntity.getFiscalFundingRequestAmount()));
        existingEntity.setFiscalFundingAllocRationale(
                nonNullOrDefault(projectFiscalModel.getFiscalFundingAllocRationale(), existingEntity.getFiscalFundingAllocRationale()));
        existingEntity.setFiscalAllocatedAmount(
                nonNullOrDefault(projectFiscalModel.getFiscalAllocatedAmount(), existingEntity.getFiscalAllocatedAmount()));
        existingEntity.setFiscalAncillaryFundAmount(
                nonNullOrDefault(projectFiscalModel.getFiscalAncillaryFundAmount(), existingEntity.getFiscalAncillaryFundAmount()));
        existingEntity.setFiscalPlannedProjectSizeHa(
                nonNullOrDefault(projectFiscalModel.getFiscalPlannedProjectSizeHa(), existingEntity.getFiscalPlannedProjectSizeHa()));
        existingEntity.setFiscalPlannedCostPerHaAmt(
                nonNullOrDefault(projectFiscalModel.getFiscalPlannedCostPerHaAmt(), existingEntity.getFiscalPlannedCostPerHaAmt()));
        existingEntity.setFiscalReportedSpendAmount(
                nonNullOrDefault(projectFiscalModel.getFiscalReportedSpendAmount(), existingEntity.getFiscalReportedSpendAmount()));
        existingEntity.setFiscalActualAmount(
                nonNullOrDefault(projectFiscalModel.getFiscalActualAmount(), existingEntity.getFiscalActualAmount()));
        existingEntity.setFiscalCompletedSizeHa(
                nonNullOrDefault(projectFiscalModel.getFiscalCompletedSizeHa(), existingEntity.getFiscalCompletedSizeHa()));
        existingEntity.setFiscalActualCostPerHaAmt(
                nonNullOrDefault(projectFiscalModel.getFiscalActualCostPerHaAmt(), existingEntity.getFiscalActualCostPerHaAmt()));
        existingEntity.setFirstNationsDelivPartInd(
                nonNullOrDefault(projectFiscalModel.getFirstNationsDelivPartInd(), existingEntity.getFirstNationsDelivPartInd()));
        existingEntity.setFirstNationsEngagementInd(
                nonNullOrDefault(projectFiscalModel.getFirstNationsEngagementInd(), existingEntity.getFirstNationsEngagementInd()));
        existingEntity.setFirstNationsPartner(
                nonNullOrDefault(projectFiscalModel.getFirstNationsPartner(), existingEntity.getFirstNationsPartner()));
        existingEntity.setOtherPartner(
                nonNullOrDefault(projectFiscalModel.getOtherPartner(), existingEntity.getOtherPartner()));
        existingEntity.setResultsNumber(
                nonNullOrDefault(projectFiscalModel.getResultsNumber(), existingEntity.getResultsNumber()));
        existingEntity.setResultsOpeningId(
                nonNullOrDefault(projectFiscalModel.getResultsOpeningId(), existingEntity.getResultsOpeningId()));
        existingEntity.setResultsContactEmail(
                nonNullOrDefault(projectFiscalModel.getResultsContactEmail(), existingEntity.getResultsContactEmail()));
        existingEntity.setSubmittedByName(
                nonNullOrDefault(projectFiscalModel.getSubmittedByName(), existingEntity.getSubmittedByName()));
        existingEntity.setSubmittedByUserGuid(
                nonNullOrDefault(projectFiscalModel.getSubmittedByUserGuid(), existingEntity.getSubmittedByUserGuid()));
        existingEntity.setSubmittedByUserUserid(
                nonNullOrDefault(projectFiscalModel.getSubmittedByUserUserid(), existingEntity.getSubmittedByUserUserid()));
        existingEntity.setSubmissionTimestamp(
                nonNullOrDefault(projectFiscalModel.getSubmissionTimestamp(), existingEntity.getSubmissionTimestamp()));
        existingEntity.setEndorsementEvalTimestamp(
                nonNullOrDefault(projectFiscalModel.getEndorsementEvalTimestamp(), existingEntity.getEndorsementEvalTimestamp()));
        existingEntity.setEndorserName(
                nonNullOrDefault(projectFiscalModel.getEndorserName(), existingEntity.getEndorserName()));
        existingEntity.setEndorserUserGuid(
                nonNullOrDefault(projectFiscalModel.getEndorserUserGuid(), existingEntity.getEndorserUserGuid()));
        existingEntity.setEndorserUserUserid(
                nonNullOrDefault(projectFiscalModel.getEndorserUserUserid(), existingEntity.getEndorserUserUserid()));
        existingEntity.setEndorsementTimestamp(
                nonNullOrDefault(projectFiscalModel.getEndorsementTimestamp(), existingEntity.getEndorsementTimestamp()));
        existingEntity.setEndorsementComment(
                nonNullOrDefault(projectFiscalModel.getEndorsementComment(), existingEntity.getEndorsementComment()));
        existingEntity.setIsApprovedInd(
                nonNullOrDefault(projectFiscalModel.getIsApprovedInd(), existingEntity.getIsApprovedInd()));
        existingEntity.setApproverName(
                nonNullOrDefault(projectFiscalModel.getApproverName(), existingEntity.getApproverName()));
        existingEntity.setApproverUserGuid(
                nonNullOrDefault(projectFiscalModel.getApproverUserGuid(), existingEntity.getApproverUserGuid()));
        existingEntity.setApproverUserUserid(
                nonNullOrDefault(projectFiscalModel.getApproverUserUserid(), existingEntity.getApproverUserUserid()));
        existingEntity.setApprovedTimestamp(
                nonNullOrDefault(projectFiscalModel.getApprovedTimestamp(), existingEntity.getApprovedTimestamp()));
        existingEntity.setAccomplishmentsComment(
                nonNullOrDefault(projectFiscalModel.getAccomplishmentsComment(), existingEntity.getAccomplishmentsComment()));
        existingEntity.setIsDelayedInd(
                nonNullOrDefault(projectFiscalModel.getIsDelayedInd(), existingEntity.getIsDelayedInd()));
        existingEntity.setDelayRationale(
                nonNullOrDefault(projectFiscalModel.getDelayRationale(), existingEntity.getDelayRationale()));
        existingEntity.setAbandonedRationale(
                nonNullOrDefault(projectFiscalModel.getAbandonedRationale(), existingEntity.getAbandonedRationale()));
        existingEntity.setLastProgressUpdateTimestamp(
                nonNullOrDefault(projectFiscalModel.getLastProgressUpdateTimestamp(), existingEntity.getLastProgressUpdateTimestamp()));

        return existingEntity;
    }

    private <T> T nonNullOrDefault(T newValue, T existingValue) {
        return newValue != null ? newValue : existingValue;
    }
}
