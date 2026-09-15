package ca.bc.gov.nrs.reportgenerator.model;

import java.math.BigDecimal;
import java.util.Date;

import io.quarkus.runtime.annotations.RegisterForReflection;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * One row of the RESULTS export (FM or CRx tab). Yes/No columns are pre-formatted as "Y"/"N"
 * by the caller and left blank when unknown; the template never defaults them.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@RegisterForReflection
public class ResultsReportData {

    private String linkToProject;
    private String linkToFiscalActivity;
    private String projectName;
    private String projectFiscalName;
    private String activityName;
    private String resultsReportable;
    private String activityDescription;
    private String activityStatusDescription;
    private String fiscalYear;
    private String forestDistrictOrgUnitName;
    private String projectLeadEmailAddress;
    private String resultsProjectCode;
    private String resultsOpeningId;
    private BigDecimal projectBoundarySizeHa;
    private String silvicultureBaseDescription;
    private String silvicultureTechniqueDescription;
    private String silvicultureMethodDescription;
    private String primaryObjectiveTypeDescription;
    private String secondaryObjectiveTypeDescription;
    private Date completedDate;
    private BigDecimal completedAreaHa;
    private String fundingSourceAbbreviation;
    private BigDecimal plannedTreatmentAreaHa;
    private String contractPhaseDescription;
    private String cfsProjectCode;
    private String previousCarryForward;
    private String carryForward;
    private String finalOutcomeComments;
    private String outstandingObligations;
    private String outstandingObligationsPlan;
    private String openingSpatialFileName;
    private String activitySpatialFileName;

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

    public String getResultsReportable() {
        return resultsReportable;
    }

    public void setResultsReportable(String resultsReportable) {
        this.resultsReportable = resultsReportable;
    }

    public String getActivityDescription() {
        return activityDescription;
    }

    public void setActivityDescription(String activityDescription) {
        this.activityDescription = activityDescription;
    }

    public String getActivityStatusDescription() {
        return activityStatusDescription;
    }

    public void setActivityStatusDescription(String activityStatusDescription) {
        this.activityStatusDescription = activityStatusDescription;
    }

    public String getFiscalYear() {
        return fiscalYear;
    }

    public void setFiscalYear(String fiscalYear) {
        this.fiscalYear = fiscalYear;
    }

    public String getForestDistrictOrgUnitName() {
        return forestDistrictOrgUnitName;
    }

    public void setForestDistrictOrgUnitName(String forestDistrictOrgUnitName) {
        this.forestDistrictOrgUnitName = forestDistrictOrgUnitName;
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

    public BigDecimal getProjectBoundarySizeHa() {
        return projectBoundarySizeHa;
    }

    public void setProjectBoundarySizeHa(BigDecimal projectBoundarySizeHa) {
        this.projectBoundarySizeHa = projectBoundarySizeHa;
    }

    public String getSilvicultureBaseDescription() {
        return silvicultureBaseDescription;
    }

    public void setSilvicultureBaseDescription(String silvicultureBaseDescription) {
        this.silvicultureBaseDescription = silvicultureBaseDescription;
    }

    public String getSilvicultureTechniqueDescription() {
        return silvicultureTechniqueDescription;
    }

    public void setSilvicultureTechniqueDescription(String silvicultureTechniqueDescription) {
        this.silvicultureTechniqueDescription = silvicultureTechniqueDescription;
    }

    public String getSilvicultureMethodDescription() {
        return silvicultureMethodDescription;
    }

    public void setSilvicultureMethodDescription(String silvicultureMethodDescription) {
        this.silvicultureMethodDescription = silvicultureMethodDescription;
    }

    public String getPrimaryObjectiveTypeDescription() {
        return primaryObjectiveTypeDescription;
    }

    public void setPrimaryObjectiveTypeDescription(String primaryObjectiveTypeDescription) {
        this.primaryObjectiveTypeDescription = primaryObjectiveTypeDescription;
    }

    public String getSecondaryObjectiveTypeDescription() {
        return secondaryObjectiveTypeDescription;
    }

    public void setSecondaryObjectiveTypeDescription(String secondaryObjectiveTypeDescription) {
        this.secondaryObjectiveTypeDescription = secondaryObjectiveTypeDescription;
    }

    public Date getCompletedDate() {
        return completedDate;
    }

    public void setCompletedDate(Date completedDate) {
        this.completedDate = completedDate;
    }

    public BigDecimal getCompletedAreaHa() {
        return completedAreaHa;
    }

    public void setCompletedAreaHa(BigDecimal completedAreaHa) {
        this.completedAreaHa = completedAreaHa;
    }

    public String getFundingSourceAbbreviation() {
        return fundingSourceAbbreviation;
    }

    public void setFundingSourceAbbreviation(String fundingSourceAbbreviation) {
        this.fundingSourceAbbreviation = fundingSourceAbbreviation;
    }

    public BigDecimal getPlannedTreatmentAreaHa() {
        return plannedTreatmentAreaHa;
    }

    public void setPlannedTreatmentAreaHa(BigDecimal plannedTreatmentAreaHa) {
        this.plannedTreatmentAreaHa = plannedTreatmentAreaHa;
    }

    public String getContractPhaseDescription() {
        return contractPhaseDescription;
    }

    public void setContractPhaseDescription(String contractPhaseDescription) {
        this.contractPhaseDescription = contractPhaseDescription;
    }

    public String getCfsProjectCode() {
        return cfsProjectCode;
    }

    public void setCfsProjectCode(String cfsProjectCode) {
        this.cfsProjectCode = cfsProjectCode;
    }

    public String getPreviousCarryForward() {
        return previousCarryForward;
    }

    public void setPreviousCarryForward(String previousCarryForward) {
        this.previousCarryForward = previousCarryForward;
    }

    public String getCarryForward() {
        return carryForward;
    }

    public void setCarryForward(String carryForward) {
        this.carryForward = carryForward;
    }

    public String getFinalOutcomeComments() {
        return finalOutcomeComments;
    }

    public void setFinalOutcomeComments(String finalOutcomeComments) {
        this.finalOutcomeComments = finalOutcomeComments;
    }

    public String getOutstandingObligations() {
        return outstandingObligations;
    }

    public void setOutstandingObligations(String outstandingObligations) {
        this.outstandingObligations = outstandingObligations;
    }

    public String getOutstandingObligationsPlan() {
        return outstandingObligationsPlan;
    }

    public void setOutstandingObligationsPlan(String outstandingObligationsPlan) {
        this.outstandingObligationsPlan = outstandingObligationsPlan;
    }

    public String getOpeningSpatialFileName() {
        return openingSpatialFileName;
    }

    public void setOpeningSpatialFileName(String openingSpatialFileName) {
        this.openingSpatialFileName = openingSpatialFileName;
    }

    public String getActivitySpatialFileName() {
        return activitySpatialFileName;
    }

    public void setActivitySpatialFileName(String activitySpatialFileName) {
        this.activitySpatialFileName = activitySpatialFileName;
    }
}
