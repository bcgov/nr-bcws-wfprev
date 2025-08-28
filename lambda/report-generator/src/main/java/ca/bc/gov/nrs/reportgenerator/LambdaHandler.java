package ca.bc.gov.nrs.reportgenerator;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.*;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import ca.bc.gov.nrs.reportgenerator.model.CulturePrescribedFireReportData;
import ca.bc.gov.nrs.reportgenerator.model.FuelManagementReportData;
import org.jboss.logging.Logger;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperFillManager;
import ca.bc.gov.nrs.reportgenerator.service.JasperReportService;
import ca.bc.gov.nrs.reportgenerator.model.LambdaEvent;
import io.quarkiverse.jasperreports.repository.ReadOnlyStreamingService;

import jakarta.inject.Inject;

public class LambdaHandler implements RequestStreamHandler {
    private static final Logger LOG = Logger.getLogger(LambdaHandler.class);

    private final ObjectMapper mapper = new ObjectMapper();

    @Inject
    JasperReportService jasperReportService;

    @Inject
    ReadOnlyStreamingService repo;

    @Override
    public void handleRequest(InputStream input, OutputStream output, Context context) throws IOException {
        LambdaEvent event;
        try {
            event = mapper.readValue(input, LambdaEvent.class);
        } catch (Exception e) {
            LOG.error("Failed to deserialize input", e);
            writeErrorResponse(output, "Invalid input: " + e.getMessage());
            return;
        }

        // Input validation
        boolean hasCulture = event.getCulturePrescribedFireReportData() != null && !event.getCulturePrescribedFireReportData().isEmpty();
        boolean hasFuel = event.getFuelManagementReportData() != null && !event.getFuelManagementReportData().isEmpty();
        if (!hasCulture && !hasFuel) {
            LOG.warn("No valid report data provided");
            writeErrorResponse(output, "No valid report data provided");
            return;
        }

        List<Map<String, String>> files = new java.util.ArrayList<>();

        if (hasCulture) {
            byte[] xlsxBytes = generateCulturePrescribedFireReport(event.getCulturePrescribedFireReportData());
            if (xlsxBytes != null && xlsxBytes.length > 0) {
                files.add(Map.of(
                    "filename", "culture-prescribed-fire-report.xlsx",
                    "content", Base64.getEncoder().encodeToString(xlsxBytes)
                ));
            }
        }
        if (hasFuel) {
            byte[] xlsxBytes = generateFuelManagementReport(event.getFuelManagementReportData());
            if (xlsxBytes != null && xlsxBytes.length > 0) {
                files.add(Map.of(
                    "filename", "fuel-management-report.xlsx",
                    "content", Base64.getEncoder().encodeToString(xlsxBytes)
                ));
            }
        }

        if (files.isEmpty()) {
            LOG.error("Report generation failed or returned empty XLSX");
            writeErrorResponse(output, "Report generation failed or returned empty XLSX");
            return;
        }

        Map<String, Object> response = new HashMap<>();
        response.put("statusCode", 200);
        response.put("headers", Map.of("Content-Type", "application/json"));
        response.put("files", files);
        response.put("isBase64Encoded", true);

        mapper.writeValue(output, response);
    }

    private void writeErrorResponse(OutputStream output, String message) throws IOException {
        Map<String, Object> response = new HashMap<>();
        response.put("statusCode", 400);
        response.put("error", true);
        response.put("message", message);
        response.put("headers", Map.of("Content-Type", "text/plain"));
        response.put("isBase64Encoded", false);
        mapper.writeValue(output, response);
    }

    // XLSX generation using JasperReportService

    private byte[] generateCulturePrescribedFireReport(List<CulturePrescribedFireReportData> reportData) {
        try {
            JRDataSource dataSource = new JRBeanCollectionDataSource(reportData);
            JasperPrint jasperPrint = JasperFillManager.getInstance(repo.getContext())
                .fillFromRepo("WFPREV_CULTURE_PRESCRIBED_FIRE_JASPER.jasper", new HashMap<>(), dataSource);
            return jasperReportService.exportXlsx(jasperPrint).toByteArray();
        } catch (Exception e) {
            LOG.error("Error generating culture prescribed fire report", e);
            return new byte[0];
        }
    }

    private byte[] generateFuelManagementReport(List<FuelManagementReportData> reportData) {
        try {
            JRDataSource dataSource = new JRBeanCollectionDataSource(reportData);
            JasperPrint jasperPrint = JasperFillManager.getInstance(repo.getContext())
                .fillFromRepo("WFPREV_FUEL_MANAGEMENT_JASPER.jasper", new HashMap<>(), dataSource);
            return jasperReportService.exportXlsx(jasperPrint).toByteArray();
        } catch (Exception e) {
            LOG.error("Error generating fuel management report", e);
            return new byte[0];
        }
    }
}