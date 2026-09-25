
package ca.bc.gov.nrs.reportgenerator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import ca.bc.gov.nrs.reportgenerator.model.ProjectCulturePrescribedFireReportData;
import ca.bc.gov.nrs.reportgenerator.model.ProjectFuelManagementReportData;
import ca.bc.gov.nrs.reportgenerator.model.ResultsCulturePrescribedFireReportData;
import ca.bc.gov.nrs.reportgenerator.model.ResultsFuelManagementReportData;
import ca.bc.gov.nrs.reportgenerator.model.LambdaEvent;
import ca.bc.gov.nrs.reportgenerator.model.Report;
import ca.bc.gov.nrs.reportgenerator.model.ReportType;
import ca.bc.gov.nrs.reportgenerator.model.XlsxReportData;

import static org.junit.jupiter.api.Assertions.*;

// @QuarkusTest so the handler gets the jasperreports repo, which serves the templates compiled at build time
@QuarkusTest
class LambdaHandlerTest {

    @Inject
    LambdaHandler handler;

    @Test
    void testHandleRequestWithCultureOnly() throws Exception {
        LambdaEvent event = new LambdaEvent();
        XlsxReportData xlsxData = new XlsxReportData();
        xlsxData.setProjectCulturePrescribedFireReportData(List.of(new ProjectCulturePrescribedFireReportData()));
        Report report = new Report();
        report.setReportType(ReportType.XLSX);
        report.setReportName("culture-prescribed-fire-report");
        report.setXlsxReportData(xlsxData);
        event.setReports(List.of(report));
        ObjectMapper mapper = new ObjectMapper();
        ByteArrayInputStream input = new ByteArrayInputStream(mapper.writeValueAsBytes(event));
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        handler.handleRequest(input, output, null);
        String response = output.toString();
        assertTrue(response.contains("files"));
        assertTrue(response.contains("culture-prescribed-fire-report.xlsx"));
        assertFalse(response.contains("fuel-management-report.xlsx"));
    }


    @Test
    void testHandleRequestWithFuelOnly() throws Exception {
        LambdaEvent event = new LambdaEvent();
        XlsxReportData xlsxData = new XlsxReportData();
        xlsxData.setProjectFuelManagementReportData(List.of(new ProjectFuelManagementReportData()));
        Report report = new Report();
        report.setReportType(ReportType.XLSX);
        report.setReportName("fuel-management-report");
        report.setXlsxReportData(xlsxData);
        event.setReports(List.of(report));
        ObjectMapper mapper = new ObjectMapper();
        ByteArrayInputStream input = new ByteArrayInputStream(mapper.writeValueAsBytes(event));
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        handler.handleRequest(input, output, null);
        String response = output.toString();
        assertTrue(response.contains("files"));
        assertTrue(response.contains("fuel-management-report.xlsx"));
        assertFalse(response.contains("culture-prescribed-fire-report.xlsx"));
    }


    @Test
    void testHandleRequestWithBothReports() throws Exception {
        LambdaEvent event = new LambdaEvent();
        XlsxReportData xlsxData1 = new XlsxReportData();
        xlsxData1.setProjectCulturePrescribedFireReportData(List.of(new ProjectCulturePrescribedFireReportData()));
        Report report1 = new Report();
        report1.setReportType(ReportType.XLSX);
        report1.setReportName("culture-prescribed-fire-report");
        report1.setXlsxReportData(xlsxData1);

        XlsxReportData xlsxData2 = new XlsxReportData();
        xlsxData2.setProjectFuelManagementReportData(List.of(new ProjectFuelManagementReportData()));
        Report report2 = new Report();
        report2.setReportType(ReportType.XLSX);
        report2.setReportName("fuel-management-report");
        report2.setXlsxReportData(xlsxData2);

        event.setReports(List.of(report1, report2));
        ObjectMapper mapper = new ObjectMapper();
        ByteArrayInputStream input = new ByteArrayInputStream(mapper.writeValueAsBytes(event));
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        handler.handleRequest(input, output, null);
        String response = output.toString();
        assertTrue(response.contains("files"));
        assertTrue(response.contains("culture-prescribed-fire-report.xlsx"));
        assertTrue(response.contains("fuel-management-report.xlsx"));
    }


    @Test
    void testHandleRequestWithResultsReports() throws Exception {
        ResultsFuelManagementReportData fuel = new ResultsFuelManagementReportData();
        fuel.setProjectName("Fuel Management Project");
        ResultsCulturePrescribedFireReportData culture = new ResultsCulturePrescribedFireReportData();
        culture.setProjectName("Culture Prescribed Fire Project");
        XlsxReportData xlsxData = new XlsxReportData();
        xlsxData.setResultsFuelManagementReportData(List.of(fuel));
        xlsxData.setResultsCulturePrescribedFireReportData(List.of(culture));
        Report report = new Report();
        report.setReportType(ReportType.XLSX);
        report.setReportName("ReMi_RESULTS");
        report.setXlsxReportData(xlsxData);
        LambdaEvent event = new LambdaEvent();
        event.setReports(List.of(report));
        ObjectMapper mapper = new ObjectMapper();
        ByteArrayInputStream input = new ByteArrayInputStream(mapper.writeValueAsBytes(event));
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        handler.handleRequest(input, output, null);
        JsonNode response = mapper.readTree(output.toByteArray());
        assertEquals(200, response.get("statusCode").asInt());
        JsonNode file = mapper.readTree(response.get("body").asText()).get("files").get(0);
        assertEquals("ReMi_RESULTS.xlsx", file.get("filename").asText());
        String workbook = readZipEntry(Base64.getDecoder().decode(file.get("content").asText()), "xl/workbook.xml");
        assertTrue(workbook.contains("name=\"FM XLS Download\""));
        assertTrue(workbook.contains("name=\"CRx XLS Download\""));
    }


    @Test
    void testWarmupReturns200WithoutTouchingJasper() throws Exception {
        // Deliberately not the injected handler: repo is null here, so reaching the Jasper fill would throw
        LambdaHandler handler = new LambdaHandler();
        ByteArrayInputStream input = new ByteArrayInputStream("{\"warmup\":true}".getBytes());
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        handler.handleRequest(input, output, null);
        JsonNode response = new ObjectMapper().readTree(output.toByteArray());
        assertEquals(200, response.get("statusCode").asInt());
        assertEquals("{\"warmup\":true}", response.get("body").asText());
        assertFalse(response.has("error"));
    }


    @Test
    void testHandleRequestWithNoReportData() throws Exception {
        LambdaEvent event = new LambdaEvent();
        ObjectMapper mapper = new ObjectMapper();
        ByteArrayInputStream input = new ByteArrayInputStream(mapper.writeValueAsBytes(event));
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        handler.handleRequest(input, output, null);
        String response = output.toString();
        assertTrue(response.contains("No reports provided") || response.contains("No valid XLSX files generated"));
        assertTrue(response.contains("error") || response.contains("statusCode"));
    }

    private static String readZipEntry(byte[] zip, String name) throws IOException {
        try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(zip))) {
            for (ZipEntry entry; (entry = zis.getNextEntry()) != null; ) {
                if (entry.getName().equals(name)) {
                    return new String(zis.readAllBytes(), StandardCharsets.UTF_8);
                }
            }
        }
        return fail("No " + name + " in the XLSX");
    }
}
