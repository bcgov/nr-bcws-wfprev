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
    void testHandleRequestWithValidCulturePrescribedFireReportData() throws Exception {
        LambdaHandler handler = new LambdaHandler();
        LambdaEvent event = new LambdaEvent();
        event.setCulturePrescribedFireReportData(List.of(new CulturePrescribedFireReportData()));
        ObjectMapper mapper = new ObjectMapper();
        ByteArrayInputStream input = new ByteArrayInputStream(mapper.writeValueAsBytes(event));
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        handler.handleRequest(input, output, null);
        String response = output.toString();
        assertTrue(response.contains("base64"));
        assertTrue(response.contains("statusCode"));
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
