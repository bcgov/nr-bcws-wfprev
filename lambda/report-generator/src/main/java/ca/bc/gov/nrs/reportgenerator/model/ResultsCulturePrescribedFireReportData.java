package ca.bc.gov.nrs.reportgenerator.model;

import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

import io.quarkus.runtime.annotations.RegisterForReflection;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
@RegisterForReflection
public class ResultsCulturePrescribedFireReportData {

    private UUID projectGuid;
    private UUID projectPlanFiscalGuid;
    private String linkToProject;
    private String linkToFiscalActivity;
    private String projectName;
    private String projectFiscalName;
    private String activityName;
    private String isResultsReportableInd;
    private String activityDescription;
    private String activityStatusName;
    private String fiscalYear;
    private String forestDistrictName;
    private String projectLeadEmailAddress;
    private String resultsProjectCode;
    private String resultsOpeningId;
    private String resultsOpeningAction;
    private String resultsOpeningCategory;
    private BigDecimal projectBoundarySizeHa;
    private BigDecimal maxPermanentAccessPercent;
    private String activityBaseName;
    private String techniqueName;
    private String methodName;
    private String primaryObjectiveName;
    private String secondaryObjectiveName;
    private String additionalObjectiveName;
    private Date activityEndDate;
    private BigDecimal completedAreaHa;
    private String fundingSourceCode;
    private String comment;
    private String tenureNumber;
    private BigDecimal plannedTreatmentAreaHa;
    private String contractPhaseName;
    private String cfsProjectCode;
    private String previousCarryForwardInd;
    private String carryForwardInd;
    private String finalOutcomeComments;
    private String outstandingObligationsInd;
    private String activityComment;
    private String openingShapeFileName;
    private String activityShapeFileName;
    private String forestCoverShapeFileName;
    private String forestCoverAttributes;
    private String prescription;

    // Getters and setters
    public UUID getProjectGuid() {
        return projectGuid;
    }

    public void setProjectGuid(UUID projectGuid) {
        this.projectGuid = projectGuid;
    }

    public UUID getProjectPlanFiscalGuid() {
        return projectPlanFiscalGuid;
    }

    public void setProjectPlanFiscalGuid(UUID projectPlanFiscalGuid) {
        this.projectPlanFiscalGuid = projectPlanFiscalGuid;
    }

    public String getLinkToProject() {
        return linkToProject;
    }

    public void setLinkToProject(String linkToProject) {
        this.linkToProject = linkToProject;
    }

    public String getLinkToFiscalActivity() {
        return linkToFiscalActivity;
    }

    public void setLinkToFiscalActivity(String linkToFiscalActivity) {
        this.linkToFiscalActivity = linkToFiscalActivity;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getProjectFiscalName() {
        return projectFiscalName;
    }

    public void setProjectFiscalName(String projectFiscalName) {
        this.projectFiscalName = projectFiscalName;
    }

    public String getActivityName() {
        return activityName;
    }

    public void setActivityName(String activityName) {
        this.activityName = activityName;
    }

    public String getIsResultsReportableInd() {
        return isResultsReportableInd;
    }

    public void setIsResultsReportableInd(String isResultsReportableInd) {
        this.isResultsReportableInd = isResultsReportableInd;
    }

    public String getActivityDescription() {
        return activityDescription;
    }

    public void setActivityDescription(String activityDescription) {
        this.activityDescription = activityDescription;
    }

    public String getActivityStatusName() {
        return activityStatusName;
    }

    public void setActivityStatusName(String activityStatusName) {
        this.activityStatusName = activityStatusName;
    }

    public String getFiscalYear() {
        return fiscalYear;
    }

    public void setFiscalYear(String fiscalYear) {
        this.fiscalYear = fiscalYear;
    }

    public String getForestDistrictName() {
        return forestDistrictName;
    }

    public void setForestDistrictName(String forestDistrictName) {
        this.forestDistrictName = forestDistrictName;
    }

    public String getProjectLeadEmailAddress() {
        return projectLeadEmailAddress;
    }

    public void setProjectLeadEmailAddress(String projectLeadEmailAddress) {
        this.projectLeadEmailAddress = projectLeadEmailAddress;
    }

    public String getResultsProjectCode() {
        return resultsProjectCode;
    }

    public void setResultsProjectCode(String resultsProjectCode) {
        this.resultsProjectCode = resultsProjectCode;
    }

    public String getResultsOpeningId() {
        return resultsOpeningId;
    }

    public void setResultsOpeningId(String resultsOpeningId) {
        this.resultsOpeningId = resultsOpeningId;
    }

    public String getResultsOpeningAction() {
        return resultsOpeningAction;
    }

    public void setResultsOpeningAction(String resultsOpeningAction) {
        this.resultsOpeningAction = resultsOpeningAction;
    }

    public String getResultsOpeningCategory() {
        return resultsOpeningCategory;
    }

    public void setResultsOpeningCategory(String resultsOpeningCategory) {
        this.resultsOpeningCategory = resultsOpeningCategory;
    }

    public BigDecimal getProjectBoundarySizeHa() {
        return projectBoundarySizeHa;
    }

    public void setProjectBoundarySizeHa(BigDecimal projectBoundarySizeHa) {
        this.projectBoundarySizeHa = projectBoundarySizeHa;
    }

    public BigDecimal getMaxPermanentAccessPercent() {
        return maxPermanentAccessPercent;
    }

    public void setMaxPermanentAccessPercent(BigDecimal maxPermanentAccessPercent) {
        this.maxPermanentAccessPercent = maxPermanentAccessPercent;
    }

    public String getActivityBaseName() {
        return activityBaseName;
    }

    public void setActivityBaseName(String activityBaseName) {
        this.activityBaseName = activityBaseName;
    }

    public String getTechniqueName() {
        return techniqueName;
    }

    public void setTechniqueName(String techniqueName) {
        this.techniqueName = techniqueName;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String getPrimaryObjectiveName() {
        return primaryObjectiveName;
    }

    public void setPrimaryObjectiveName(String primaryObjectiveName) {
        this.primaryObjectiveName = primaryObjectiveName;
    }

    public String getSecondaryObjectiveName() {
        return secondaryObjectiveName;
    }

    public void setSecondaryObjectiveName(String secondaryObjectiveName) {
        this.secondaryObjectiveName = secondaryObjectiveName;
    }

    public String getAdditionalObjectiveName() {
        return additionalObjectiveName;
    }

    public void setAdditionalObjectiveName(String additionalObjectiveName) {
        this.additionalObjectiveName = additionalObjectiveName;
    }

    public Date getActivityEndDate() {
        return activityEndDate;
    }

    public void setActivityEndDate(Date activityEndDate) {
        this.activityEndDate = activityEndDate;
    }

    public BigDecimal getCompletedAreaHa() {
        return completedAreaHa;
    }

    public void setCompletedAreaHa(BigDecimal completedAreaHa) {
        this.completedAreaHa = completedAreaHa;
    }

    public String getFundingSourceCode() {
        return fundingSourceCode;
    }

    public void setFundingSourceCode(String fundingSourceCode) {
        this.fundingSourceCode = fundingSourceCode;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getTenureNumber() {
        return tenureNumber;
    }

    public void setTenureNumber(String tenureNumber) {
        this.tenureNumber = tenureNumber;
    }

    public BigDecimal getPlannedTreatmentAreaHa() {
        return plannedTreatmentAreaHa;
    }

    public void setPlannedTreatmentAreaHa(BigDecimal plannedTreatmentAreaHa) {
        this.plannedTreatmentAreaHa = plannedTreatmentAreaHa;
    }

    public String getContractPhaseName() {
        return contractPhaseName;
    }

    public void setContractPhaseName(String contractPhaseName) {
        this.contractPhaseName = contractPhaseName;
    }

    public String getCfsProjectCode() {
        return cfsProjectCode;
    }

    public void setCfsProjectCode(String cfsProjectCode) {
        this.cfsProjectCode = cfsProjectCode;
    }

    public String getPreviousCarryForwardInd() {
        return previousCarryForwardInd;
    }

    public void setPreviousCarryForwardInd(String previousCarryForwardInd) {
        this.previousCarryForwardInd = previousCarryForwardInd;
    }

    public String getCarryForwardInd() {
        return carryForwardInd;
    }

    public void setCarryForwardInd(String carryForwardInd) {
        this.carryForwardInd = carryForwardInd;
    }

    public String getFinalOutcomeComments() {
        return finalOutcomeComments;
    }

    public void setFinalOutcomeComments(String finalOutcomeComments) {
        this.finalOutcomeComments = finalOutcomeComments;
    }

    public String getOutstandingObligationsInd() {
        return outstandingObligationsInd;
    }

    public void setOutstandingObligationsInd(String outstandingObligationsInd) {
        this.outstandingObligationsInd = outstandingObligationsInd;
    }

    public String getActivityComment() {
        return activityComment;
    }

    public void setActivityComment(String activityComment) {
        this.activityComment = activityComment;
    }

    public String getOpeningShapeFileName() {
        return openingShapeFileName;
    }

    public void setOpeningShapeFileName(String openingShapeFileName) {
        this.openingShapeFileName = openingShapeFileName;
    }

    public String getActivityShapeFileName() {
        return activityShapeFileName;
    }

    public void setActivityShapeFileName(String activityShapeFileName) {
        this.activityShapeFileName = activityShapeFileName;
    }

    public String getForestCoverShapeFileName() {
        return forestCoverShapeFileName;
    }

    public void setForestCoverShapeFileName(String forestCoverShapeFileName) {
        this.forestCoverShapeFileName = forestCoverShapeFileName;
    }

    public String getForestCoverAttributes() {
        return forestCoverAttributes;
    }

    public void setForestCoverAttributes(String forestCoverAttributes) {
        this.forestCoverAttributes = forestCoverAttributes;
    }

    public String getPrescription() {
        return prescription;
    }

    public void setPrescription(String prescription) {
        this.prescription = prescription;
    }
}
