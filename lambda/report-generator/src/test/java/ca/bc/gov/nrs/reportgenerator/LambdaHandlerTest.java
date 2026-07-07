
package ca.bc.gov.nrs.reportgenerator;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;
import ca.bc.gov.nrs.reportgenerator.model.CulturePrescribedFireReportData;
import ca.bc.gov.nrs.reportgenerator.model.FuelManagementReportData;
import ca.bc.gov.nrs.reportgenerator.model.LambdaEvent;
import ca.bc.gov.nrs.reportgenerator.model.Report;
import ca.bc.gov.nrs.reportgenerator.model.ReportType;
import ca.bc.gov.nrs.reportgenerator.model.XlsxReportData;

import static org.junit.jupiter.api.Assertions.*;


class LambdaHandlerTest {

    @Test
    void testHandleRequestWithCultureOnly() throws Exception {
        LambdaHandler handler = new LambdaHandler();
        LambdaEvent event = new LambdaEvent();
        XlsxReportData xlsxData = new XlsxReportData();
        xlsxData.setCulturePrescribedFireReportData(List.of(new CulturePrescribedFireReportData()));
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
        LambdaHandler handler = new LambdaHandler();
        LambdaEvent event = new LambdaEvent();
        XlsxReportData xlsxData = new XlsxReportData();
        xlsxData.setFuelManagementReportData(List.of(new FuelManagementReportData()));
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
        LambdaHandler handler = new LambdaHandler();
        LambdaEvent event = new LambdaEvent();
        XlsxReportData xlsxData1 = new XlsxReportData();
        xlsxData1.setCulturePrescribedFireReportData(List.of(new CulturePrescribedFireReportData()));
        Report report1 = new Report();
        report1.setReportType(ReportType.XLSX);
        report1.setReportName("culture-prescribed-fire-report");
        report1.setXlsxReportData(xlsxData1);

        XlsxReportData xlsxData2 = new XlsxReportData();
        xlsxData2.setFuelManagementReportData(List.of(new FuelManagementReportData()));
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
    void testHandleRequestWithNoReportData() throws Exception {
        LambdaHandler handler = new LambdaHandler();
        LambdaEvent event = new LambdaEvent();
        ObjectMapper mapper = new ObjectMapper();
        ByteArrayInputStream input = new ByteArrayInputStream(mapper.writeValueAsBytes(event));
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        handler.handleRequest(input, output, null);
        String response = output.toString();
        assertTrue(response.contains("No reports provided") || response.contains("No valid XLSX files generated"));
        assertTrue(response.contains("error") || response.contains("statusCode"));
    }
}
