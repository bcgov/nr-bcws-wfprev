package ca.bc.gov.nrs.reportgenerator.api;

import ca.bc.gov.nrs.reportgenerator.model.CulturePrescribedFireReportData;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import java.util.Collections;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import ca.bc.gov.nrs.reportgenerator.ExtendedMediaType;

@QuarkusTest
public class CulturePrescribedFireResourceTest {
    @Test
    public void testGenerateXlsx() {
            CulturePrescribedFireReportData sample = new CulturePrescribedFireReportData();
            sample.setLinkToFiscalActivity("http://example.com/activity");
            sample.setProjectFiscalName("Test Project");
            sample.setProjectFiscalDescription("Test Description");
            sample.setFiscalYear("2025");
            sample.setActivityCategoryDescription("Prescribed Fire");
            sample.setPlanFiscalStatusDescription("Approved");
            sample.setTotalEstimatedCostAmount(new java.math.BigDecimal("10000.00"));
            sample.setFiscalAncillaryFundAmount(new java.math.BigDecimal("500.00"));
            sample.setFiscalReportedSpendAmount(new java.math.BigDecimal("9500.00"));
            sample.setFiscalActualAmount(new java.math.BigDecimal("10000.00"));
            sample.setFiscalPlannedProjectSizeHa(new java.math.BigDecimal("100.0"));
            sample.setFiscalCompletedSizeHa(new java.math.BigDecimal("90.0"));
            sample.setSpatialSubmitted("Yes");
            sample.setFirstNationsEngagement("Engaged");
            sample.setFirstNationsDelivPartners("Partner A");
            sample.setFirstNationsPartner("FN Partner");
            sample.setOtherPartner("Other Partner");
            sample.setCfsProjectCode("CFS123");
            sample.setResultsOpeningId("OPEN123");
            sample.setEndorsementTimestamp(new java.util.Date());
            sample.setApprovedTimestamp(new java.util.Date());
            sample.setOutsideWuiInd(Boolean.TRUE);
            sample.setTotalRclFilterSectionScore(new java.math.BigDecimal("10.0"));
            sample.setBdfFilterSectionComment("BDF Comment");
            sample.setTotalBdfFilterSectionScore(new java.math.BigDecimal("9.0"));
            sample.setRclFilterSectionComment("RCL Comment");
            sample.setTotalCollimpFilterSectionScore(new java.math.BigDecimal("8.0"));
            sample.setCollimpFilterSectionComment("Collimp Comment");
            sample.setTotalFilterSectionScore(new java.math.BigDecimal("27.0"));
            sample.setProjectTypeDescription("Type A");
            sample.setProjectName("Project Name");
            sample.setForestRegionOrgUnitName("Region 1");
            sample.setLinkToProject("http://example.com/project");
            sample.setForestDistrictOrgUnitName("District 1");
            sample.setBcParksRegionOrgUnitName("Parks Region");
            sample.setBcParksSectionOrgUnitName("Parks Section");
            sample.setFireCentreOrgUnitName("Fire Centre");
            sample.setPlanningUnitName("Planning Unit");
            sample.setGrossProjectAreaHa(new java.math.BigDecimal("120.0"));
            sample.setClosestCommunityName("Community");
            sample.setProjectLead("Lead Name");
            sample.setProposalTypeDescription("Proposal Type");
            sample.setResultsProjectCode("RESULT123");
            sample.setPrimaryObjectiveTypeDescription("Primary Objective");
            sample.setSecondaryObjectiveTypeDescription("Secondary Objective");
            sample.setWuiRiskClassDescription("Risk Class");
            sample.setLocalWuiRiskClassDescription("Local Risk Class");
            sample.setBusinessArea("Business Area");

            given()
                .contentType(ContentType.JSON)
                .body(Collections.singletonList(sample))
            .when()
                .post("/culture-prescribed-fire")
            .then()
                .statusCode(200)
                .contentType(ExtendedMediaType.APPLICATION_XLSX)
                .header("Content-Disposition", containsString("wfprev_culture_prescribed_fire.xlsx"))
                .body(not(emptyOrNullString()));
    }
}
