package ca.bc.gov.nrs.reportgenerator;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;
import ca.bc.gov.nrs.reportgenerator.model.CulturePrescribedFireReportData;
import ca.bc.gov.nrs.reportgenerator.model.FuelManagementReportData;
import ca.bc.gov.nrs.reportgenerator.model.LambdaEvent;
import ca.bc.gov.nrs.reportgenerator.LambdaHandler;

import static org.junit.jupiter.api.Assertions.*;


class LambdaHandlerTest {
    @Test
    void testHandleRequestWithCultureOnly() throws Exception {
        LambdaHandler handler = new LambdaHandler();
        LambdaEvent event = new LambdaEvent();
        event.setCulturePrescribedFireReportData(List.of(new CulturePrescribedFireReportData()));
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
        event.setFuelManagementReportData(List.of(new FuelManagementReportData()));
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
        event.setCulturePrescribedFireReportData(List.of(new CulturePrescribedFireReportData()));
        event.setFuelManagementReportData(List.of(new FuelManagementReportData()));
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
        assertTrue(response.contains("No valid report data provided"));
        assertTrue(response.contains("error"));
    }
}
