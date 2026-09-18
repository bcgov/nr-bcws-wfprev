package ca.bc.gov.nrs.wfprev.entities;

import ca.bc.gov.nrs.wfprev.data.entities.ResultsCulturalPrescribedFireReportEntity;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ResultsCulturalPrescribedFireReportEntityTest {

    @Test
    void testAllFields() {
        UUID uniqueRowGuid = UUID.randomUUID();
        UUID projectGuid = UUID.randomUUID();
        UUID fiscalGuid = UUID.randomUUID();
        BigDecimal decimal = new BigDecimal("123.45");
        Date now = new Date();

        ResultsCulturalPrescribedFireReportEntity entity = new ResultsCulturalPrescribedFireReportEntity();

        entity.setUniqueRowGuid(uniqueRowGuid);
        entity.setProjectGuid(projectGuid);
        entity.setProjectPlanFiscalGuid(fiscalGuid);
        entity.setLinkToProject("http://example.com/project");
        entity.setLinkToFiscalActivity("http://example.com/fiscal");
        entity.setProjectName("Project Name");
        entity.setProjectFiscalName("Fiscal Name");
        entity.setActivityName("Activity Name");
        entity.setIsResultsReportableInd("Y");
        entity.setActivityDescription("Activity Description");
        entity.setActivityStatusName("Active");
        entity.setFiscalYear("2025/26");
        entity.setForestDistrictName("District");
        entity.setProjectLeadEmailAddress("lead@gov.bc.ca");
        entity.setResultsProjectCode("RES123");
        entity.setResultsOpeningId("OPEN123");
        entity.setResultsOpeningAction("Action");
        entity.setResultsOpeningCategory("Category");
        entity.setProjectBoundarySizeHa(decimal);
        entity.setMaxPermanentAccessPercent(decimal);
        entity.setActivityBaseName("Base Name");
        entity.setTechniqueName("Technique");
        entity.setMethodName("Method");
        entity.setPrimaryObjectiveName("Primary Obj");
        entity.setSecondaryObjectiveName("Secondary Obj");
        entity.setAdditionalObjectiveName("Additional Obj");
        entity.setActivityEndDate(now);
        entity.setCompletedAreaHa(decimal);
        entity.setFundingSourceCode("FSC");
        entity.setComment("Comment");
        entity.setTenureNumber("TEN123");
        entity.setPlannedTreatmentAreaHa(decimal);
        entity.setContractPhaseName("Phase");
        entity.setCfsProjectCode("CFS123");
        entity.setPreviousCarryForwardInd("N");
        entity.setCarryForwardInd("Y");
        entity.setFinalOutcomeComments("Outcome");
        entity.setOutstandingObligationsInd("N");
        entity.setActivityComment("Activity Comment");
        entity.setOpeningShapeFileName("path/to/opening.zip");
        entity.setActivityShapeFileName("path/to/activity.zip");
        entity.setForestCoverShapeFileName("path/to/forest_cover.zip");
        entity.setForestCoverAttributes("attributes");
        entity.setPrescription("Prescription");

        assertThat(entity.getUniqueRowGuid()).isEqualTo(uniqueRowGuid);
        assertThat(entity.getProjectGuid()).isEqualTo(projectGuid);
        assertThat(entity.getProjectPlanFiscalGuid()).isEqualTo(fiscalGuid);
        assertThat(entity.getLinkToProject()).isEqualTo("http://example.com/project");
        assertThat(entity.getLinkToFiscalActivity()).isEqualTo("http://example.com/fiscal");
        assertThat(entity.getProjectName()).isEqualTo("Project Name");
        assertThat(entity.getProjectFiscalName()).isEqualTo("Fiscal Name");
        assertThat(entity.getActivityName()).isEqualTo("Activity Name");
        assertThat(entity.getIsResultsReportableInd()).isEqualTo("Y");
        assertThat(entity.getActivityDescription()).isEqualTo("Activity Description");
        assertThat(entity.getActivityStatusName()).isEqualTo("Active");
        assertThat(entity.getFiscalYear()).isEqualTo("2025/26");
        assertThat(entity.getForestDistrictName()).isEqualTo("District");
        assertThat(entity.getProjectLeadEmailAddress()).isEqualTo("lead@gov.bc.ca");
        assertThat(entity.getResultsProjectCode()).isEqualTo("RES123");
        assertThat(entity.getResultsOpeningId()).isEqualTo("OPEN123");
        assertThat(entity.getResultsOpeningAction()).isEqualTo("Action");
        assertThat(entity.getResultsOpeningCategory()).isEqualTo("Category");
        assertThat(entity.getProjectBoundarySizeHa()).isEqualTo(decimal);
        assertThat(entity.getMaxPermanentAccessPercent()).isEqualTo(decimal);
        assertThat(entity.getActivityBaseName()).isEqualTo("Base Name");
        assertThat(entity.getTechniqueName()).isEqualTo("Technique");
        assertThat(entity.getMethodName()).isEqualTo("Method");
        assertThat(entity.getPrimaryObjectiveName()).isEqualTo("Primary Obj");
        assertThat(entity.getSecondaryObjectiveName()).isEqualTo("Secondary Obj");
        assertThat(entity.getAdditionalObjectiveName()).isEqualTo("Additional Obj");
        assertThat(entity.getActivityEndDate()).isEqualTo(now);
        assertThat(entity.getCompletedAreaHa()).isEqualTo(decimal);
        assertThat(entity.getFundingSourceCode()).isEqualTo("FSC");
        assertThat(entity.getComment()).isEqualTo("Comment");
        assertThat(entity.getTenureNumber()).isEqualTo("TEN123");
        assertThat(entity.getPlannedTreatmentAreaHa()).isEqualTo(decimal);
        assertThat(entity.getContractPhaseName()).isEqualTo("Phase");
        assertThat(entity.getCfsProjectCode()).isEqualTo("CFS123");
        assertThat(entity.getPreviousCarryForwardInd()).isEqualTo("N");
        assertThat(entity.getCarryForwardInd()).isEqualTo("Y");
        assertThat(entity.getFinalOutcomeComments()).isEqualTo("Outcome");
        assertThat(entity.getOutstandingObligationsInd()).isEqualTo("N");
        assertThat(entity.getActivityComment()).isEqualTo("Activity Comment");
        assertThat(entity.getOpeningShapeFileName()).isEqualTo("path/to/opening.zip");
        assertThat(entity.getActivityShapeFileName()).isEqualTo("path/to/activity.zip");
        assertThat(entity.getForestCoverShapeFileName()).isEqualTo("path/to/forest_cover.zip");
        assertThat(entity.getForestCoverAttributes()).isEqualTo("attributes");
        assertThat(entity.getPrescription()).isEqualTo("Prescription");
    }
}
