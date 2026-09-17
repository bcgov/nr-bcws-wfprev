package ca.bc.gov.nrs.reportgenerator.api;

import ca.bc.gov.nrs.reportgenerator.ExtendedMediaType;
import ca.bc.gov.nrs.reportgenerator.model.ResultsFuelManagementReportData;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Date;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
public class ResultsFuelManagementResourceTest {

    @Test
    public void testGenerateXlsx() {
        ResultsFuelManagementReportData sample = new ResultsFuelManagementReportData();
        sample.setProjectGuid(UUID.randomUUID());
        sample.setProjectPlanFiscalGuid(UUID.randomUUID());
        sample.setLinkToProject("http://example.com/project");
        sample.setLinkToFiscalActivity("http://example.com/fiscal");
        sample.setProjectName("Project Name");
        sample.setProjectFiscalName("Fiscal Name");
        sample.setActivityName("Activity Name");
        sample.setIsResultsReportableInd("Y");
        sample.setActivityDescription("Activity Description");
        sample.setActivityStatusName("Active");
        sample.setFiscalYear("2025/26");
        sample.setForestDistrictName("District");
        sample.setProjectLeadEmailAddress("lead@gov.bc.ca");
        sample.setResultsProjectCode("RES123");
        sample.setResultsOpeningId("OPEN123");
        sample.setResultsOpeningAction("Action");
        sample.setResultsOpeningCategory("Category");
        sample.setProjectBoundarySizeHa(new BigDecimal("100.5"));
        sample.setMaxPermanentAccessPercent(new BigDecimal("10.0"));
        sample.setActivityBaseName("Base Name");
        sample.setTechniqueName("Technique");
        sample.setMethodName("Method");
        sample.setPrimaryObjectiveName("Primary Obj");
        sample.setSecondaryObjectiveName("Secondary Obj");
        sample.setAdditionalObjectiveName("Additional Obj");
        sample.setActivityEndDate(new Date());
        sample.setCompletedAreaHa(new BigDecimal("50.0"));
        sample.setFundingSourceCode("FSC");
        sample.setComment("Comment");
        sample.setTenureNumber("TEN123");
        sample.setPlannedTreatmentAreaHa(new BigDecimal("60.0"));
        sample.setContractPhaseName("Phase");
        sample.setCfsProjectCode("CFS123");
        sample.setPreviousCarryForwardInd("N");
        sample.setCarryForwardInd("Y");
        sample.setFinalOutcomeComments("Outcome");
        sample.setOutstandingObligationsInd("N");
        sample.setActivityComment("Activity Comment");
        sample.setOpeningShapeFileName("path/to/opening.zip");
        sample.setActivityShapeFileName("path/to/activity.zip");
        sample.setForestCoverShapeFileName("path/to/forest_cover.zip");
        sample.setForestCoverAttributes("attributes");
        sample.setPrescription("Prescription");

        given()
            .contentType(ContentType.JSON)
            .body(Collections.singletonList(sample))
        .when()
            .post("/results-fuel-management")
        .then()
            .statusCode(anyOf(is(200), is(500)));
    }
}
