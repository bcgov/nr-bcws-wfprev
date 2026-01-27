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
}
