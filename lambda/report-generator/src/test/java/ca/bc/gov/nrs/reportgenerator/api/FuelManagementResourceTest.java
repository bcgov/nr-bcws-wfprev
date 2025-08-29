package ca.bc.gov.nrs.reportgenerator.api;

import ca.bc.gov.nrs.reportgenerator.model.FuelManagementReportData;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import java.util.Collections;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import ca.bc.gov.nrs.reportgenerator.ExtendedMediaType;

@QuarkusTest
public class FuelManagementResourceTest {
    @Test
    public void testGenerateXlsx() {
            FuelManagementReportData sample = new FuelManagementReportData();
            sample.setLinkToFiscalActivity("http://example.com/activity");
            sample.setProjectFiscalName("Test Project");
            sample.setProjectFiscalDescription("Test Description");
            sample.setFiscalYear("2025");
            sample.setActivityCategoryDescription("Fuel Management");
            sample.setPlanFiscalStatusDescription("Approved");
            sample.setTotalEstimatedCostAmount(new java.math.BigDecimal("20000.00"));
            sample.setFiscalAncillaryFundAmount(new java.math.BigDecimal("1000.00"));
            sample.setFiscalReportedSpendAmount(new java.math.BigDecimal("19500.00"));
            sample.setFiscalActualAmount(new java.math.BigDecimal("20000.00"));
            sample.setFiscalPlannedProjectSizeHa(new java.math.BigDecimal("200.0"));
            sample.setFiscalCompletedSizeHa(new java.math.BigDecimal("180.0"));
            sample.setSpatialSubmitted("Yes");
            sample.setFirstNationsEngagement("Engaged");
            sample.setFirstNationsDelivPartners("Partner B");
            sample.setFirstNationsPartner("FN Partner");
            sample.setOtherPartner("Other Partner");
            sample.setCfsProjectCode("CFS456");
            sample.setResultsOpeningId("OPEN456");
            sample.setEndorsementTimestamp(new java.util.Date());
            sample.setApprovedTimestamp(new java.util.Date());
            sample.setTotalFilterSectionScore(new java.math.BigDecimal("30.0"));
            sample.setProjectTypeDescription("Type B");
            sample.setProjectName("Project Name");
            sample.setForestRegionOrgUnitName("Region 2");
            sample.setLinkToProject("http://example.com/project");
            sample.setForestDistrictOrgUnitName("District 2");
            sample.setBcParksRegionOrgUnitName("Parks Region");
            sample.setBcParksSectionOrgUnitName("Parks Section");
            sample.setFireCentreOrgUnitName("Fire Centre");
            sample.setPlanningUnitName("Planning Unit");
            sample.setGrossProjectAreaHa(new java.math.BigDecimal("220.0"));
            sample.setClosestCommunityName("Community");
            sample.setProjectLead("Lead Name");
            sample.setProposalTypeDescription("Proposal Type");
            sample.setResultsProjectCode("RESULT456");
            sample.setPrimaryObjectiveTypeDescription("Primary Objective");
            sample.setSecondaryObjectiveTypeDescription("Secondary Objective");
            sample.setWuiRiskClassDescription("Risk Class");
            sample.setLocalWuiRiskClassDescription("Local Risk Class");
            sample.setBusinessArea("Business Area");
            sample.setLocalWuiRiskClassRationale("Rationale");
            sample.setTotalCoarseFilterSectionScore(new java.math.BigDecimal("5.0"));
            sample.setTotalMediumFilterSectionScore(new java.math.BigDecimal("10.0"));
            sample.setMediumFilterSectionComment("Medium Comment");
            sample.setTotalFineFilterSectionScore(new java.math.BigDecimal("15.0"));
            sample.setFineFilterSectionComment("Fine Comment");

            given()
                .contentType(ContentType.JSON)
                .body(Collections.singletonList(sample))
            .when()
                .post("/fuel-management")
            .then()
                .statusCode(200)
                .contentType(ExtendedMediaType.APPLICATION_XLSX)
                .header("Content-Disposition", containsString("wfprev_fuel_management.xlsx"))
                .body(not(emptyOrNullString()));
    }
}
