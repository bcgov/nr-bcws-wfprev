package ca.bc.gov.nrs.reportgenerator.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ResultsCulturePrescribedFireReportDataTest {

    @Test
    void testGettersAndSetters() {
        ResultsCulturePrescribedFireReportData data = new ResultsCulturePrescribedFireReportData();

        UUID projectGuid = UUID.randomUUID();
        UUID fiscalGuid = UUID.randomUUID();
        BigDecimal decimal = new BigDecimal("123.45");
        Date now = new Date();

        data.setProjectGuid(projectGuid);
        data.setProjectPlanFiscalGuid(fiscalGuid);
        data.setLinkToProject("http://example.com/project");
        data.setLinkToFiscalActivity("http://example.com/fiscal");
        data.setProjectName("Project Name");
        data.setProjectFiscalName("Fiscal Name");
        data.setActivityName("Activity Name");
        data.setIsResultsReportableInd("Y");
        data.setActivityDescription("Activity Description");
        data.setActivityStatusName("Active");
        data.setFiscalYear("2025/26");
        data.setForestDistrictName("District");
        data.setProjectLeadEmailAddress("lead@gov.bc.ca");
        data.setResultsProjectCode("RES123");
        data.setResultsOpeningId("OPEN123");
        data.setResultsOpeningAction("Action");
        data.setResultsOpeningCategory("Category");
        data.setProjectBoundarySizeHa(decimal);
        data.setMaxPermanentAccessPercent(decimal);
        data.setActivityBaseName("Base Name");
        data.setTechniqueName("Technique");
        data.setMethodName("Method");
        data.setPrimaryObjectiveName("Primary Obj");
        data.setSecondaryObjectiveName("Secondary Obj");
        data.setAdditionalObjectiveName("Additional Obj");
        data.setActivityEndDate(now);
        data.setCompletedAreaHa(decimal);
        data.setFundingSourceCode("FSC");
        data.setComment("Comment");
        data.setTenureNumber("TEN123");
        data.setPlannedTreatmentAreaHa(decimal);
        data.setContractPhaseName("Phase");
        data.setCfsProjectCode("CFS123");
        data.setPreviousCarryForwardInd("N");
        data.setCarryForwardInd("Y");
        data.setFinalOutcomeComments("Outcome");
        data.setOutstandingObligationsInd("N");
        data.setActivityComment("Activity Comment");
        data.setOpeningShapeFileName("path/to/opening.zip");
        data.setActivityShapeFileName("path/to/activity.zip");
        data.setForestCoverShapeFileName("path/to/forest_cover.zip");
        data.setForestCoverAttributes("attributes");
        data.setPrescription("Prescription");

        assertEquals(projectGuid, data.getProjectGuid());
        assertEquals(fiscalGuid, data.getProjectPlanFiscalGuid());
        assertEquals("http://example.com/project", data.getLinkToProject());
        assertEquals("http://example.com/fiscal", data.getLinkToFiscalActivity());
        assertEquals("Project Name", data.getProjectName());
        assertEquals("Fiscal Name", data.getProjectFiscalName());
        assertEquals("Activity Name", data.getActivityName());
        assertEquals("Y", data.getIsResultsReportableInd());
        assertEquals("Activity Description", data.getActivityDescription());
        assertEquals("Active", data.getActivityStatusName());
        assertEquals("2025/26", data.getFiscalYear());
        assertEquals("District", data.getForestDistrictName());
        assertEquals("lead@gov.bc.ca", data.getProjectLeadEmailAddress());
        assertEquals("RES123", data.getResultsProjectCode());
        assertEquals("OPEN123", data.getResultsOpeningId());
        assertEquals("Action", data.getResultsOpeningAction());
        assertEquals("Category", data.getResultsOpeningCategory());
        assertEquals(decimal, data.getProjectBoundarySizeHa());
        assertEquals(decimal, data.getMaxPermanentAccessPercent());
        assertEquals("Base Name", data.getActivityBaseName());
        assertEquals("Technique", data.getTechniqueName());
        assertEquals("Method", data.getMethodName());
        assertEquals("Primary Obj", data.getPrimaryObjectiveName());
        assertEquals("Secondary Obj", data.getSecondaryObjectiveName());
        assertEquals("Additional Obj", data.getAdditionalObjectiveName());
        assertEquals(now, data.getActivityEndDate());
        assertEquals(decimal, data.getCompletedAreaHa());
        assertEquals("FSC", data.getFundingSourceCode());
        assertEquals("Comment", data.getComment());
        assertEquals("TEN123", data.getTenureNumber());
        assertEquals(decimal, data.getPlannedTreatmentAreaHa());
        assertEquals("Phase", data.getContractPhaseName());
        assertEquals("CFS123", data.getCfsProjectCode());
        assertEquals("N", data.getPreviousCarryForwardInd());
        assertEquals("Y", data.getCarryForwardInd());
        assertEquals("Outcome", data.getFinalOutcomeComments());
        assertEquals("N", data.getOutstandingObligationsInd());
        assertEquals("Activity Comment", data.getActivityComment());
        assertEquals("path/to/opening.zip", data.getOpeningShapeFileName());
        assertEquals("path/to/activity.zip", data.getActivityShapeFileName());
        assertEquals("path/to/forest_cover.zip", data.getForestCoverShapeFileName());
        assertEquals("attributes", data.getForestCoverAttributes());
        assertEquals("Prescription", data.getPrescription());
    }
}
