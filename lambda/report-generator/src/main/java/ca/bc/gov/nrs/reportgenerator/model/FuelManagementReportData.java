package ca.bc.gov.nrs.reportgenerator.model;

import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

import io.quarkus.runtime.annotations.RegisterForReflection;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
@RegisterForReflection
public class FuelManagementReportData {

    private UUID projectPlanFiscalGuid;
    private String linkToFiscalActivity;
    private String projectFiscalName;
    private String projectFiscalDescription;
    private String fiscalYear;
    private String activityCategoryDescription;
    private String planFiscalStatusDescription;
    private BigDecimal totalEstimatedCostAmount;
    private BigDecimal fiscalForecastAmount;
    private String ancillaryFundingProvider;
    private BigDecimal fiscalAncillaryFundAmount;
    private BigDecimal fiscalReportedSpendAmount;
    private BigDecimal fiscalActualAmount;
    private BigDecimal fiscalPlannedProjectSizeHa;
    private BigDecimal fiscalCompletedSizeHa;
    private String spatialSubmitted;
    private String firstNationsEngagement;
    private String firstNationsDelivPartners;
    private String firstNationsPartner;
    private String otherPartner;
    private String cfsProjectCode;
    private String resultsOpeningId;
    private Date endorsementTimestamp;
    private Date approvedTimestamp;
    private BigDecimal totalFilterSectionScore;
    private String projectTypeDescription;
    private String projectName;
    private String forestRegionOrgUnitName;
    private String linkToProject;
    private String forestDistrictOrgUnitName;
    private String bcParksRegionOrgUnitName;
    private String bcParksSectionOrgUnitName;
    private String fireCentreOrgUnitName;
    private String planningUnitName;
    private BigDecimal grossProjectAreaHa;
    private String closestCommunityName;
    private String projectLead;
    private String proposalTypeDescription;
    private String resultsProjectCode;
    private String primaryObjectiveTypeDescription;
    private String secondaryObjectiveTypeDescription;
    private String wuiRiskClassDescription;
    private String localWuiRiskClassDescription;
    private String businessArea;
    private String localWuiRiskClassRationale;
    private BigDecimal totalCoarseFilterSectionScore;
    private BigDecimal totalMediumFilterSectionScore;
    private String mediumFilterSectionComment;
    private BigDecimal totalFineFilterSectionScore;
    private String fineFilterSectionComment;
    private UUID projectGuid;
    private UUID programAreaGuid;
    private String fundingStream;
    private Date q1SubmittedTimestamp;
    private String q1ProgressStatusCode;
    private String q1GeneralUpdateComment;
    private BigDecimal q1ForecastAmount;
    private BigDecimal q1ForecastAdjustmentAmount;
    private String q1ForecastAdjustmentRationale;
    private BigDecimal q1BudgetHighRiskAmount;
    private String q1BudgetHighRiskRationale;
    private BigDecimal q1BudgetMediumRiskAmount;
    private String q1BudgetMediumRiskRationale;
    private BigDecimal q1BudgetLowRiskAmount;
    private String q1BudgetLowRiskRationale;
    private BigDecimal q1BudgetCompletedAmount;
    private String q1BudgetCompletedDescription;
    private Date q2SubmittedTimestamp;
    private String q2ProgressStatusCode;
    private String q2GeneralUpdateComment;
    private BigDecimal q2ForecastAmount;
    private BigDecimal q2ForecastAdjustmentAmount;
    private String q2ForecastAdjustmentRationale;
    private BigDecimal q2BudgetHighRiskAmount;
    private String q2BudgetHighRiskRationale;
    private BigDecimal q2BudgetMediumRiskAmount;
    private String q2BudgetMediumRiskRationale;
    private BigDecimal q2BudgetLowRiskAmount;
    private String q2BudgetLowRiskRationale;
    private BigDecimal q2BudgetCompletedAmount;
    private String q2BudgetCompletedDescription;
    private Date q3SubmittedTimestamp;
    private String q3ProgressStatusCode;
    private String q3GeneralUpdateComment;
    private BigDecimal q3ForecastAmount;
    private BigDecimal q3ForecastAdjustmentAmount;
    private String q3ForecastAdjustmentRationale;
    private BigDecimal q3BudgetHighRiskAmount;
    private String q3BudgetHighRiskRationale;
    private BigDecimal q3BudgetMediumRiskAmount;
    private String q3BudgetMediumRiskRationale;
    private BigDecimal q3BudgetLowRiskAmount;
    private String q3BudgetLowRiskRationale;
    private BigDecimal q3BudgetCompletedAmount;
    private String q3BudgetCompletedDescription;
    private Date march7SubmittedTimestamp;
    private String march7ProgressStatusCode;
    private String march7GeneralUpdateComment;
    private BigDecimal march7ForecastAmount;
    private BigDecimal march7ForecastAdjustmentAmount;
    private String march7ForecastAdjustmentRationale;
    private BigDecimal march7BudgetHighRiskAmount;
    private String march7BudgetHighRiskRationale;
    private BigDecimal march7BudgetMediumRiskAmount;
    private String march7BudgetMediumRiskRationale;
    private BigDecimal march7BudgetLowRiskAmount;
    private String march7BudgetLowRiskRationale;
    private BigDecimal march7BudgetCompletedAmount;
    private String march7BudgetCompletedDescription;
    private Date otherSubmittedTimestamp;
    private String otherProgressStatusCode;
    private String otherGeneralUpdateComment;
    private BigDecimal otherForecastAmount;
    private BigDecimal otherForecastAdjustmentAmount;
    private String otherForecastAdjustmentRationale;
    private BigDecimal otherBudgetHighRiskAmount;
    private String otherBudgetHighRiskRationale;
    private BigDecimal otherBudgetMediumRiskAmount;
    private String otherBudgetMediumRiskRationale;
    private BigDecimal otherBudgetLowRiskAmount;
    private String otherBudgetLowRiskRationale;
    private BigDecimal otherBudgetCompletedAmount;
    private String otherBudgetCompletedDescription;

    // Getters and setters
    public BigDecimal getTotalFilterSectionScore() { return totalFilterSectionScore; }
    public void setTotalFilterSectionScore(BigDecimal totalFilterSectionScore) { this.totalFilterSectionScore = totalFilterSectionScore; }
    public String getProjectTypeDescription() { return projectTypeDescription; }
    public void setProjectTypeDescription(String projectTypeDescription) { this.projectTypeDescription = projectTypeDescription; }
    public String getProjectName() { return projectName; }
    public void setProjectName(String projectName) { this.projectName = projectName; }
    public String getForestRegionOrgUnitName() { return forestRegionOrgUnitName; }
    public void setForestRegionOrgUnitName(String forestRegionOrgUnitName) { this.forestRegionOrgUnitName = forestRegionOrgUnitName; }
    public String getLinkToProject() { return linkToProject; }
    public void setLinkToProject(String linkToProject) { this.linkToProject = linkToProject; }
    public String getForestDistrictOrgUnitName() { return forestDistrictOrgUnitName; }
    public void setForestDistrictOrgUnitName(String forestDistrictOrgUnitName) { this.forestDistrictOrgUnitName = forestDistrictOrgUnitName; }
    public String getBcParksRegionOrgUnitName() { return bcParksRegionOrgUnitName; }
    public void setBcParksRegionOrgUnitName(String bcParksRegionOrgUnitName) { this.bcParksRegionOrgUnitName = bcParksRegionOrgUnitName; }
    public String getBcParksSectionOrgUnitName() { return bcParksSectionOrgUnitName; }
    public void setBcParksSectionOrgUnitName(String bcParksSectionOrgUnitName) { this.bcParksSectionOrgUnitName = bcParksSectionOrgUnitName; }
    public String getFireCentreOrgUnitName() { return fireCentreOrgUnitName; }
    public void setFireCentreOrgUnitName(String fireCentreOrgUnitName) { this.fireCentreOrgUnitName = fireCentreOrgUnitName; }
    public String getPlanningUnitName() { return planningUnitName; }
    public void setPlanningUnitName(String planningUnitName) { this.planningUnitName = planningUnitName; }
    public BigDecimal getGrossProjectAreaHa() { return grossProjectAreaHa; }
    public void setGrossProjectAreaHa(BigDecimal grossProjectAreaHa) { this.grossProjectAreaHa = grossProjectAreaHa; }
    public String getClosestCommunityName() { return closestCommunityName; }
    public void setClosestCommunityName(String closestCommunityName) { this.closestCommunityName = closestCommunityName; }
    public String getProjectLead() { return projectLead; }
    public void setProjectLead(String projectLead) { this.projectLead = projectLead; }
    public String getProposalTypeDescription() { return proposalTypeDescription; }
    public void setProposalTypeDescription(String proposalTypeDescription) { this.proposalTypeDescription = proposalTypeDescription; }
    public String getResultsProjectCode() { return resultsProjectCode; }
    public void setResultsProjectCode(String resultsProjectCode) { this.resultsProjectCode = resultsProjectCode; }
    public String getPrimaryObjectiveTypeDescription() { return primaryObjectiveTypeDescription; }
    public void setPrimaryObjectiveTypeDescription(String primaryObjectiveTypeDescription) { this.primaryObjectiveTypeDescription = primaryObjectiveTypeDescription; }
    public String getSecondaryObjectiveTypeDescription() { return secondaryObjectiveTypeDescription; }
    public void setSecondaryObjectiveTypeDescription(String secondaryObjectiveTypeDescription) { this.secondaryObjectiveTypeDescription = secondaryObjectiveTypeDescription; }
    public String getWuiRiskClassDescription() { return wuiRiskClassDescription; }
    public void setWuiRiskClassDescription(String wuiRiskClassDescription) { this.wuiRiskClassDescription = wuiRiskClassDescription; }
    public String getLocalWuiRiskClassDescription() { return localWuiRiskClassDescription; }
    public void setLocalWuiRiskClassDescription(String localWuiRiskClassDescription) { this.localWuiRiskClassDescription = localWuiRiskClassDescription; }
    public String getBusinessArea() { return businessArea; }
    public void setBusinessArea(String businessArea) { this.businessArea = businessArea; }
    public String getLocalWuiRiskClassRationale() { return localWuiRiskClassRationale; }
    public void setLocalWuiRiskClassRationale(String localWuiRiskClassRationale) { this.localWuiRiskClassRationale = localWuiRiskClassRationale; }
    public BigDecimal getTotalCoarseFilterSectionScore() { return totalCoarseFilterSectionScore; }
    public void setTotalCoarseFilterSectionScore(BigDecimal totalCoarseFilterSectionScore) { this.totalCoarseFilterSectionScore = totalCoarseFilterSectionScore; }
    public BigDecimal getTotalMediumFilterSectionScore() { return totalMediumFilterSectionScore; }
    public void setTotalMediumFilterSectionScore(BigDecimal totalMediumFilterSectionScore) { this.totalMediumFilterSectionScore = totalMediumFilterSectionScore; }
    public String getMediumFilterSectionComment() { return mediumFilterSectionComment; }
    public void setMediumFilterSectionComment(String mediumFilterSectionComment) { this.mediumFilterSectionComment = mediumFilterSectionComment; }
    public BigDecimal getTotalFineFilterSectionScore() { return totalFineFilterSectionScore; }
    public void setTotalFineFilterSectionScore(BigDecimal totalFineFilterSectionScore) { this.totalFineFilterSectionScore = totalFineFilterSectionScore; }
    public String getFineFilterSectionComment() { return fineFilterSectionComment; }
    public void setFineFilterSectionComment(String fineFilterSectionComment) { this.fineFilterSectionComment = fineFilterSectionComment; }
    public String getLinkToFiscalActivity() { return linkToFiscalActivity; }
    public void setLinkToFiscalActivity(String linkToFiscalActivity) { this.linkToFiscalActivity = linkToFiscalActivity; }
    public String getProjectFiscalName() { return projectFiscalName; }
    public void setProjectFiscalName(String projectFiscalName) { this.projectFiscalName = projectFiscalName; }
    public String getProjectFiscalDescription() { return projectFiscalDescription; }
    public void setProjectFiscalDescription(String projectFiscalDescription) { this.projectFiscalDescription = projectFiscalDescription; }
    public String getFiscalYear() { return fiscalYear; }
    public void setFiscalYear(String fiscalYear) { this.fiscalYear = fiscalYear; }
    public String getActivityCategoryDescription() { return activityCategoryDescription; }
    public void setActivityCategoryDescription(String activityCategoryDescription) { this.activityCategoryDescription = activityCategoryDescription; }
    public String getPlanFiscalStatusDescription() { return planFiscalStatusDescription; }
    public void setPlanFiscalStatusDescription(String planFiscalStatusDescription) { this.planFiscalStatusDescription = planFiscalStatusDescription; }
    public BigDecimal getTotalEstimatedCostAmount() { return totalEstimatedCostAmount; }
    public void setTotalEstimatedCostAmount(BigDecimal totalEstimatedCostAmount) { this.totalEstimatedCostAmount = totalEstimatedCostAmount; }
    public BigDecimal getFiscalForecastAmount() { return fiscalForecastAmount; }
    public void setFiscalForecastAmount(BigDecimal fiscalForecastAmount) { this.fiscalForecastAmount = fiscalForecastAmount; }
    public String getAncillaryFundingProvider() { return ancillaryFundingProvider; }
    public void setAncillaryFundingProvider(String ancillaryFundingProvider) { this.ancillaryFundingProvider = ancillaryFundingProvider; }
    public BigDecimal getFiscalAncillaryFundAmount() { return fiscalAncillaryFundAmount; }
    public void setFiscalAncillaryFundAmount(BigDecimal fiscalAncillaryFundAmount) { this.fiscalAncillaryFundAmount = fiscalAncillaryFundAmount; }
    public BigDecimal getFiscalReportedSpendAmount() { return fiscalReportedSpendAmount; }
    public void setFiscalReportedSpendAmount(BigDecimal fiscalReportedSpendAmount) { this.fiscalReportedSpendAmount = fiscalReportedSpendAmount; }
    public BigDecimal getFiscalActualAmount() { return fiscalActualAmount; }
    public void setFiscalActualAmount(BigDecimal fiscalActualAmount) { this.fiscalActualAmount = fiscalActualAmount; }
    public BigDecimal getFiscalPlannedProjectSizeHa() { return fiscalPlannedProjectSizeHa; }
    public void setFiscalPlannedProjectSizeHa(BigDecimal fiscalPlannedProjectSizeHa) { this.fiscalPlannedProjectSizeHa = fiscalPlannedProjectSizeHa; }
    public BigDecimal getFiscalCompletedSizeHa() { return fiscalCompletedSizeHa; }
    public void setFiscalCompletedSizeHa(BigDecimal fiscalCompletedSizeHa) { this.fiscalCompletedSizeHa = fiscalCompletedSizeHa; }
    public String getSpatialSubmitted() { return spatialSubmitted; }
    public void setSpatialSubmitted(String spatialSubmitted) { this.spatialSubmitted = spatialSubmitted; }
    public String getFirstNationsEngagement() { return firstNationsEngagement; }
    public void setFirstNationsEngagement(String firstNationsEngagement) { this.firstNationsEngagement = firstNationsEngagement; }
    public String getFirstNationsDelivPartners() { return firstNationsDelivPartners; }
    public void setFirstNationsDelivPartners(String firstNationsDelivPartners) { this.firstNationsDelivPartners = firstNationsDelivPartners; }
    public String getFirstNationsPartner() { return firstNationsPartner; }
    public void setFirstNationsPartner(String firstNationsPartner) { this.firstNationsPartner = firstNationsPartner; }
    public String getOtherPartner() { return otherPartner; }
    public void setOtherPartner(String otherPartner) { this.otherPartner = otherPartner; }
    public String getCfsProjectCode() { return cfsProjectCode; }
    public void setCfsProjectCode(String cfsProjectCode) { this.cfsProjectCode = cfsProjectCode; }
    public String getResultsOpeningId() { return resultsOpeningId; }
    public void setResultsOpeningId(String resultsOpeningId) { this.resultsOpeningId = resultsOpeningId; }
    public Date getEndorsementTimestamp() { return endorsementTimestamp; }
    public void setEndorsementTimestamp(Date endorsementTimestamp) { this.endorsementTimestamp = endorsementTimestamp; }
    public Date getApprovedTimestamp() { return approvedTimestamp; }
    public void setApprovedTimestamp(Date approvedTimestamp) { this.approvedTimestamp = approvedTimestamp; }

    public UUID getProjectPlanFiscalGuid() {
        return projectPlanFiscalGuid;
    }

    public void setProjectPlanFiscalGuid(UUID projectPlanFiscalGuid) {
        this.projectPlanFiscalGuid = projectPlanFiscalGuid;
    }

    public UUID getProjectGuid() {
        return projectGuid;
    }

    public void setProjectGuid(UUID projectGuid) {
        this.projectGuid = projectGuid;
    }

    public UUID getProgramAreaGuid() {
        return programAreaGuid;
    }

    public void setProgramAreaGuid(UUID programAreaGuid) {
        this.programAreaGuid = programAreaGuid;
    }

    public String getFundingStream() {
        return fundingStream;
    }

    public void setFundingStream(String fundingStream) {
        this.fundingStream = fundingStream;
    }

    public Date getQ1SubmittedTimestamp() { return q1SubmittedTimestamp; }
    public void setQ1SubmittedTimestamp(Date q1SubmittedTimestamp) { this.q1SubmittedTimestamp = q1SubmittedTimestamp; }
    public String getQ1ProgressStatusCode() { return q1ProgressStatusCode; }
    public void setQ1ProgressStatusCode(String q1ProgressStatusCode) { this.q1ProgressStatusCode = q1ProgressStatusCode; }
    public String getQ1GeneralUpdateComment() { return q1GeneralUpdateComment; }
    public void setQ1GeneralUpdateComment(String q1GeneralUpdateComment) { this.q1GeneralUpdateComment = q1GeneralUpdateComment; }
    public BigDecimal getQ1ForecastAmount() { return q1ForecastAmount; }
    public void setQ1ForecastAmount(BigDecimal q1ForecastAmount) { this.q1ForecastAmount = q1ForecastAmount; }
    public BigDecimal getQ1ForecastAdjustmentAmount() { return q1ForecastAdjustmentAmount; }
    public void setQ1ForecastAdjustmentAmount(BigDecimal q1ForecastAdjustmentAmount) { this.q1ForecastAdjustmentAmount = q1ForecastAdjustmentAmount; }
    public String getQ1ForecastAdjustmentRationale() { return q1ForecastAdjustmentRationale; }
    public void setQ1ForecastAdjustmentRationale(String q1ForecastAdjustmentRationale) { this.q1ForecastAdjustmentRationale = q1ForecastAdjustmentRationale; }
    public BigDecimal getQ1BudgetHighRiskAmount() { return q1BudgetHighRiskAmount; }
    public void setQ1BudgetHighRiskAmount(BigDecimal q1BudgetHighRiskAmount) { this.q1BudgetHighRiskAmount = q1BudgetHighRiskAmount; }
    public String getQ1BudgetHighRiskRationale() { return q1BudgetHighRiskRationale; }
    public void setQ1BudgetHighRiskRationale(String q1BudgetHighRiskRationale) { this.q1BudgetHighRiskRationale = q1BudgetHighRiskRationale; }
    public BigDecimal getQ1BudgetMediumRiskAmount() { return q1BudgetMediumRiskAmount; }
    public void setQ1BudgetMediumRiskAmount(BigDecimal q1BudgetMediumRiskAmount) { this.q1BudgetMediumRiskAmount = q1BudgetMediumRiskAmount; }
    public String getQ1BudgetMediumRiskRationale() { return q1BudgetMediumRiskRationale; }
    public void setQ1BudgetMediumRiskRationale(String q1BudgetMediumRiskRationale) { this.q1BudgetMediumRiskRationale = q1BudgetMediumRiskRationale; }
    public BigDecimal getQ1BudgetLowRiskAmount() { return q1BudgetLowRiskAmount; }
    public void setQ1BudgetLowRiskAmount(BigDecimal q1BudgetLowRiskAmount) { this.q1BudgetLowRiskAmount = q1BudgetLowRiskAmount; }
    public String getQ1BudgetLowRiskRationale() { return q1BudgetLowRiskRationale; }
    public void setQ1BudgetLowRiskRationale(String q1BudgetLowRiskRationale) { this.q1BudgetLowRiskRationale = q1BudgetLowRiskRationale; }
    public BigDecimal getQ1BudgetCompletedAmount() { return q1BudgetCompletedAmount; }
    public void setQ1BudgetCompletedAmount(BigDecimal q1BudgetCompletedAmount) { this.q1BudgetCompletedAmount = q1BudgetCompletedAmount; }
    public String getQ1BudgetCompletedDescription() { return q1BudgetCompletedDescription; }
    public void setQ1BudgetCompletedDescription(String q1BudgetCompletedDescription) { this.q1BudgetCompletedDescription = q1BudgetCompletedDescription; }
    public Date getQ2SubmittedTimestamp() { return q2SubmittedTimestamp; }
    public void setQ2SubmittedTimestamp(Date q2SubmittedTimestamp) { this.q2SubmittedTimestamp = q2SubmittedTimestamp; }
    public String getQ2ProgressStatusCode() { return q2ProgressStatusCode; }
    public void setQ2ProgressStatusCode(String q2ProgressStatusCode) { this.q2ProgressStatusCode = q2ProgressStatusCode; }
    public String getQ2GeneralUpdateComment() { return q2GeneralUpdateComment; }
    public void setQ2GeneralUpdateComment(String q2GeneralUpdateComment) { this.q2GeneralUpdateComment = q2GeneralUpdateComment; }
    public BigDecimal getQ2ForecastAmount() { return q2ForecastAmount; }
    public void setQ2ForecastAmount(BigDecimal q2ForecastAmount) { this.q2ForecastAmount = q2ForecastAmount; }
    public BigDecimal getQ2ForecastAdjustmentAmount() { return q2ForecastAdjustmentAmount; }
    public void setQ2ForecastAdjustmentAmount(BigDecimal q2ForecastAdjustmentAmount) { this.q2ForecastAdjustmentAmount = q2ForecastAdjustmentAmount; }
    public String getQ2ForecastAdjustmentRationale() { return q2ForecastAdjustmentRationale; }
    public void setQ2ForecastAdjustmentRationale(String q2ForecastAdjustmentRationale) { this.q2ForecastAdjustmentRationale = q2ForecastAdjustmentRationale; }
    public BigDecimal getQ2BudgetHighRiskAmount() { return q2BudgetHighRiskAmount; }
    public void setQ2BudgetHighRiskAmount(BigDecimal q2BudgetHighRiskAmount) { this.q2BudgetHighRiskAmount = q2BudgetHighRiskAmount; }
    public String getQ2BudgetHighRiskRationale() { return q2BudgetHighRiskRationale; }
    public void setQ2BudgetHighRiskRationale(String q2BudgetHighRiskRationale) { this.q2BudgetHighRiskRationale = q2BudgetHighRiskRationale; }
    public BigDecimal getQ2BudgetMediumRiskAmount() { return q2BudgetMediumRiskAmount; }
    public void setQ2BudgetMediumRiskAmount(BigDecimal q2BudgetMediumRiskAmount) { this.q2BudgetMediumRiskAmount = q2BudgetMediumRiskAmount; }
    public String getQ2BudgetMediumRiskRationale() { return q2BudgetMediumRiskRationale; }
    public void setQ2BudgetMediumRiskRationale(String q2BudgetMediumRiskRationale) { this.q2BudgetMediumRiskRationale = q2BudgetMediumRiskRationale; }
    public BigDecimal getQ2BudgetLowRiskAmount() { return q2BudgetLowRiskAmount; }
    public void setQ2BudgetLowRiskAmount(BigDecimal q2BudgetLowRiskAmount) { this.q2BudgetLowRiskAmount = q2BudgetLowRiskAmount; }
    public String getQ2BudgetLowRiskRationale() { return q2BudgetLowRiskRationale; }
    public void setQ2BudgetLowRiskRationale(String q2BudgetLowRiskRationale) { this.q2BudgetLowRiskRationale = q2BudgetLowRiskRationale; }
    public BigDecimal getQ2BudgetCompletedAmount() { return q2BudgetCompletedAmount; }
    public void setQ2BudgetCompletedAmount(BigDecimal q2BudgetCompletedAmount) { this.q2BudgetCompletedAmount = q2BudgetCompletedAmount; }
    public String getQ2BudgetCompletedDescription() { return q2BudgetCompletedDescription; }
    public void setQ2BudgetCompletedDescription(String q2BudgetCompletedDescription) { this.q2BudgetCompletedDescription = q2BudgetCompletedDescription; }
    public Date getQ3SubmittedTimestamp() { return q3SubmittedTimestamp; }
    public void setQ3SubmittedTimestamp(Date q3SubmittedTimestamp) { this.q3SubmittedTimestamp = q3SubmittedTimestamp; }
    public String getQ3ProgressStatusCode() { return q3ProgressStatusCode; }
    public void setQ3ProgressStatusCode(String q3ProgressStatusCode) { this.q3ProgressStatusCode = q3ProgressStatusCode; }
    public String getQ3GeneralUpdateComment() { return q3GeneralUpdateComment; }
    public void setQ3GeneralUpdateComment(String q3GeneralUpdateComment) { this.q3GeneralUpdateComment = q3GeneralUpdateComment; }
    public BigDecimal getQ3ForecastAmount() { return q3ForecastAmount; }
    public void setQ3ForecastAmount(BigDecimal q3ForecastAmount) { this.q3ForecastAmount = q3ForecastAmount; }
    public BigDecimal getQ3ForecastAdjustmentAmount() { return q3ForecastAdjustmentAmount; }
    public void setQ3ForecastAdjustmentAmount(BigDecimal q3ForecastAdjustmentAmount) { this.q3ForecastAdjustmentAmount = q3ForecastAdjustmentAmount; }
    public String getQ3ForecastAdjustmentRationale() { return q3ForecastAdjustmentRationale; }
    public void setQ3ForecastAdjustmentRationale(String q3ForecastAdjustmentRationale) { this.q3ForecastAdjustmentRationale = q3ForecastAdjustmentRationale; }
    public BigDecimal getQ3BudgetHighRiskAmount() { return q3BudgetHighRiskAmount; }
    public void setQ3BudgetHighRiskAmount(BigDecimal q3BudgetHighRiskAmount) { this.q3BudgetHighRiskAmount = q3BudgetHighRiskAmount; }
    public String getQ3BudgetHighRiskRationale() { return q3BudgetHighRiskRationale; }
    public void setQ3BudgetHighRiskRationale(String q3BudgetHighRiskRationale) { this.q3BudgetHighRiskRationale = q3BudgetHighRiskRationale; }
    public BigDecimal getQ3BudgetMediumRiskAmount() { return q3BudgetMediumRiskAmount; }
    public void setQ3BudgetMediumRiskAmount(BigDecimal q3BudgetMediumRiskAmount) { this.q3BudgetMediumRiskAmount = q3BudgetMediumRiskAmount; }
    public String getQ3BudgetMediumRiskRationale() { return q3BudgetMediumRiskRationale; }
    public void setQ3BudgetMediumRiskRationale(String q3BudgetMediumRiskRationale) { this.q3BudgetMediumRiskRationale = q3BudgetMediumRiskRationale; }
    public BigDecimal getQ3BudgetLowRiskAmount() { return q3BudgetLowRiskAmount; }
    public void setQ3BudgetLowRiskAmount(BigDecimal q3BudgetLowRiskAmount) { this.q3BudgetLowRiskAmount = q3BudgetLowRiskAmount; }
    public String getQ3BudgetLowRiskRationale() { return q3BudgetLowRiskRationale; }
    public void setQ3BudgetLowRiskRationale(String q3BudgetLowRiskRationale) { this.q3BudgetLowRiskRationale = q3BudgetLowRiskRationale; }
    public BigDecimal getQ3BudgetCompletedAmount() { return q3BudgetCompletedAmount; }
    public void setQ3BudgetCompletedAmount(BigDecimal q3BudgetCompletedAmount) { this.q3BudgetCompletedAmount = q3BudgetCompletedAmount; }
    public String getQ3BudgetCompletedDescription() { return q3BudgetCompletedDescription; }
    public void setQ3BudgetCompletedDescription(String q3BudgetCompletedDescription) { this.q3BudgetCompletedDescription = q3BudgetCompletedDescription; }
    public Date getMarch7SubmittedTimestamp() { return march7SubmittedTimestamp; }
    public void setMarch7SubmittedTimestamp(Date march7SubmittedTimestamp) { this.march7SubmittedTimestamp = march7SubmittedTimestamp; }
    public String getMarch7ProgressStatusCode() { return march7ProgressStatusCode; }
    public void setMarch7ProgressStatusCode(String march7ProgressStatusCode) { this.march7ProgressStatusCode = march7ProgressStatusCode; }
    public String getMarch7GeneralUpdateComment() { return march7GeneralUpdateComment; }
    public void setMarch7GeneralUpdateComment(String march7GeneralUpdateComment) { this.march7GeneralUpdateComment = march7GeneralUpdateComment; }
    public BigDecimal getMarch7ForecastAmount() { return march7ForecastAmount; }
    public void setMarch7ForecastAmount(BigDecimal march7ForecastAmount) { this.march7ForecastAmount = march7ForecastAmount; }
    public BigDecimal getMarch7ForecastAdjustmentAmount() { return march7ForecastAdjustmentAmount; }
    public void setMarch7ForecastAdjustmentAmount(BigDecimal march7ForecastAdjustmentAmount) { this.march7ForecastAdjustmentAmount = march7ForecastAdjustmentAmount; }
    public String getMarch7ForecastAdjustmentRationale() { return march7ForecastAdjustmentRationale; }
    public void setMarch7ForecastAdjustmentRationale(String march7ForecastAdjustmentRationale) { this.march7ForecastAdjustmentRationale = march7ForecastAdjustmentRationale; }
    public BigDecimal getMarch7BudgetHighRiskAmount() { return march7BudgetHighRiskAmount; }
    public void setMarch7BudgetHighRiskAmount(BigDecimal march7BudgetHighRiskAmount) { this.march7BudgetHighRiskAmount = march7BudgetHighRiskAmount; }
    public String getMarch7BudgetHighRiskRationale() { return march7BudgetHighRiskRationale; }
    public void setMarch7BudgetHighRiskRationale(String march7BudgetHighRiskRationale) { this.march7BudgetHighRiskRationale = march7BudgetHighRiskRationale; }
    public BigDecimal getMarch7BudgetMediumRiskAmount() { return march7BudgetMediumRiskAmount; }
    public void setMarch7BudgetMediumRiskAmount(BigDecimal march7BudgetMediumRiskAmount) { this.march7BudgetMediumRiskAmount = march7BudgetMediumRiskAmount; }
    public String getMarch7BudgetMediumRiskRationale() { return march7BudgetMediumRiskRationale; }
    public void setMarch7BudgetMediumRiskRationale(String march7BudgetMediumRiskRationale) { this.march7BudgetMediumRiskRationale = march7BudgetMediumRiskRationale; }
    public BigDecimal getMarch7BudgetLowRiskAmount() { return march7BudgetLowRiskAmount; }
    public void setMarch7BudgetLowRiskAmount(BigDecimal march7BudgetLowRiskAmount) { this.march7BudgetLowRiskAmount = march7BudgetLowRiskAmount; }
    public String getMarch7BudgetLowRiskRationale() { return march7BudgetLowRiskRationale; }
    public void setMarch7BudgetLowRiskRationale(String march7BudgetLowRiskRationale) { this.march7BudgetLowRiskRationale = march7BudgetLowRiskRationale; }
    public BigDecimal getMarch7BudgetCompletedAmount() { return march7BudgetCompletedAmount; }
    public void setMarch7BudgetCompletedAmount(BigDecimal march7BudgetCompletedAmount) { this.march7BudgetCompletedAmount = march7BudgetCompletedAmount; }
    public String getMarch7BudgetCompletedDescription() { return march7BudgetCompletedDescription; }
    public void setMarch7BudgetCompletedDescription(String march7BudgetCompletedDescription) { this.march7BudgetCompletedDescription = march7BudgetCompletedDescription; }
    public Date getOtherSubmittedTimestamp() { return otherSubmittedTimestamp; }
    public void setOtherSubmittedTimestamp(Date otherSubmittedTimestamp) { this.otherSubmittedTimestamp = otherSubmittedTimestamp; }
    public String getOtherProgressStatusCode() { return otherProgressStatusCode; }
    public void setOtherProgressStatusCode(String otherProgressStatusCode) { this.otherProgressStatusCode = otherProgressStatusCode; }
    public String getOtherGeneralUpdateComment() { return otherGeneralUpdateComment; }
    public void setOtherGeneralUpdateComment(String otherGeneralUpdateComment) { this.otherGeneralUpdateComment = otherGeneralUpdateComment; }
    public BigDecimal getOtherForecastAmount() { return otherForecastAmount; }
    public void setOtherForecastAmount(BigDecimal otherForecastAmount) { this.otherForecastAmount = otherForecastAmount; }
    public BigDecimal getOtherForecastAdjustmentAmount() { return otherForecastAdjustmentAmount; }
    public void setOtherForecastAdjustmentAmount(BigDecimal otherForecastAdjustmentAmount) { this.otherForecastAdjustmentAmount = otherForecastAdjustmentAmount; }
    public String getOtherForecastAdjustmentRationale() { return otherForecastAdjustmentRationale; }
    public void setOtherForecastAdjustmentRationale(String otherForecastAdjustmentRationale) { this.otherForecastAdjustmentRationale = otherForecastAdjustmentRationale; }
    public BigDecimal getOtherBudgetHighRiskAmount() { return otherBudgetHighRiskAmount; }
    public void setOtherBudgetHighRiskAmount(BigDecimal otherBudgetHighRiskAmount) { this.otherBudgetHighRiskAmount = otherBudgetHighRiskAmount; }
    public String getOtherBudgetHighRiskRationale() { return otherBudgetHighRiskRationale; }
    public void setOtherBudgetHighRiskRationale(String otherBudgetHighRiskRationale) { this.otherBudgetHighRiskRationale = otherBudgetHighRiskRationale; }
    public BigDecimal getOtherBudgetMediumRiskAmount() { return otherBudgetMediumRiskAmount; }
    public void setOtherBudgetMediumRiskAmount(BigDecimal otherBudgetMediumRiskAmount) { this.otherBudgetMediumRiskAmount = otherBudgetMediumRiskAmount; }
    public String getOtherBudgetMediumRiskRationale() { return otherBudgetMediumRiskRationale; }
    public void setOtherBudgetMediumRiskRationale(String otherBudgetMediumRiskRationale) { this.otherBudgetMediumRiskRationale = otherBudgetMediumRiskRationale; }
    public BigDecimal getOtherBudgetLowRiskAmount() { return otherBudgetLowRiskAmount; }
    public void setOtherBudgetLowRiskAmount(BigDecimal otherBudgetLowRiskAmount) { this.otherBudgetLowRiskAmount = otherBudgetLowRiskAmount; }
    public String getOtherBudgetLowRiskRationale() { return otherBudgetLowRiskRationale; }
    public void setOtherBudgetLowRiskRationale(String otherBudgetLowRiskRationale) { this.otherBudgetLowRiskRationale = otherBudgetLowRiskRationale; }
    public BigDecimal getOtherBudgetCompletedAmount() { return otherBudgetCompletedAmount; }
    public void setOtherBudgetCompletedAmount(BigDecimal otherBudgetCompletedAmount) { this.otherBudgetCompletedAmount = otherBudgetCompletedAmount; }
    public String getOtherBudgetCompletedDescription() { return otherBudgetCompletedDescription; }
    public void setOtherBudgetCompletedDescription(String otherBudgetCompletedDescription) { this.otherBudgetCompletedDescription = otherBudgetCompletedDescription; }
}
