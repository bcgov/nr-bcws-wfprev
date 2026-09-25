package ca.bc.gov.nrs.wfprev.services;

import ca.bc.gov.nrs.wfprev.data.entities.ProjectCulturalPrescribedFireReportEntity;
import ca.bc.gov.nrs.wfprev.data.entities.ProjectFuelManagementReportEntity;
import ca.bc.gov.nrs.wfprev.data.entities.ResultsCulturalPrescribedFireReportEntity;
import ca.bc.gov.nrs.wfprev.data.entities.ResultsFuelManagementReportEntity;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class XlsxReportGeneratorTest {

    private final XlsxReportGenerator generator = new XlsxReportGenerator();

    @Test
    void buildProjectRequest_carriesTheFiscalRowsAsOneXlsxReport() {
        List<ProjectFuelManagementReportEntity> fuel = List.of(new ProjectFuelManagementReportEntity());
        List<ProjectCulturalPrescribedFireReportEntity> crx = List.of(new ProjectCulturalPrescribedFireReportEntity());

        XlsxReportGenerator.LambdaReportRequest request = generator.buildProjectRequest(fuel, crx);

        assertEquals(1, request.getReports().size());
        XlsxReportGenerator.LambdaReportRequest.Report report = request.getReports().get(0);
        assertEquals("XLSX", report.getReportType());
        assertEquals("ReMi_Fiscal", report.getReportName());
        assertSame(fuel, report.getXlsxReportData().getProjectFuelManagementReportData());
        assertSame(crx, report.getXlsxReportData().getProjectCulturePrescribedFireReportData());
        assertNull(report.getXlsxReportData().getResultsFuelManagementReportData());
    }

    @Test
    void buildResultsRequest_carriesTheResultsRowsAsOneXlsxReport() {
        List<ResultsFuelManagementReportEntity> fuel = List.of(new ResultsFuelManagementReportEntity());
        List<ResultsCulturalPrescribedFireReportEntity> crx = List.of(new ResultsCulturalPrescribedFireReportEntity());

        XlsxReportGenerator.LambdaReportRequest request = generator.buildResultsRequest(fuel, crx);

        XlsxReportGenerator.LambdaReportRequest.Report report = request.getReports().get(0);
        assertEquals("ReMi_RESULTS", report.getReportName());
        assertSame(fuel, report.getXlsxReportData().getResultsFuelManagementReportData());
        assertSame(crx, report.getXlsxReportData().getResultsCulturePrescribedFireReportData());
        assertNull(report.getXlsxReportData().getProjectFuelManagementReportData());
    }

    @Test
    void request_serializesToTheShapeTheLambdaReads() throws Exception {
        ResultsFuelManagementReportEntity row = new ResultsFuelManagementReportEntity();
        row.setProjectName("Test project");

        JsonNode json = new ObjectMapper().valueToTree(generator.buildResultsRequest(List.of(row), List.of()));

        JsonNode report = json.path("reports").get(0);
        assertEquals("XLSX", report.path("reportType").asText());
        assertEquals("Test project", report.path("xlsxReportData").path("resultsFuelManagementReportData")
                .get(0).path("projectName").asText());
    }
}
