package ca.bc.gov.nrs.reportgenerator;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import org.jboss.logging.Logger;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.SimpleXlsxReportConfiguration;
import ca.bc.gov.nrs.reportgenerator.service.JasperReportService;
import ca.bc.gov.nrs.reportgenerator.model.LambdaEvent;
import ca.bc.gov.nrs.reportgenerator.model.Report;
import ca.bc.gov.nrs.reportgenerator.model.XlsxReportData;
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
        String inputJson = new String(input.readAllBytes());
        LOG.info("Received request to generate reports: " + inputJson);
        try {
            // Try to parse as wrapper object first
            JsonNode root = mapper.readTree(inputJson);
            if (root.has("body")) {
                String bodyJson = root.get("body").asText();
                event = mapper.readValue(bodyJson, LambdaEvent.class);
            } else {
                event = mapper.readValue(inputJson, LambdaEvent.class);
            }
        } catch (Exception e) {
            LOG.error("Failed to deserialize input", e);
            writeErrorResponse(output, "Invalid input: " + e.getMessage());
            return;
        }

        // Input validation
        if (event.getReports() == null || event.getReports().isEmpty()) {
            LOG.warn("No reports provided");
            writeErrorResponse(output, "No reports provided");
            return;
        }

        // Generate XLSX files for each report
        List<Map<String, String>> files = new ArrayList<>();
        for (Report report : event.getReports()) {
            XlsxReportData data = report.getXlsxReportData();
            if (data == null) continue;
            List<JasperPrint> prints = new ArrayList<>();
            List<String> sheetNames = new ArrayList<>();
            if (data.getFuelManagementReportData() != null && !data.getFuelManagementReportData().isEmpty()) {
                try {
                    JRDataSource fuelDataSource = new JRBeanCollectionDataSource(data.getFuelManagementReportData());
                    JasperPrint fuelPrint = JasperFillManager.getInstance(repo.getContext())
                        .fillFromRepo("WFPREV_FUEL_MANAGEMENT_JASPER.jasper", new HashMap<>(), fuelDataSource);
                    prints.add(fuelPrint);
                    sheetNames.add("FM XLS Download");
                } catch (net.sf.jasperreports.engine.JRException e) {
                    LOG.error("Error filling Fuel Management Jasper report", e);
                }
            }
            if (data.getCulturePrescribedFireReportData() != null && !data.getCulturePrescribedFireReportData().isEmpty()) {
                try {
                    JRDataSource cultureDataSource = new JRBeanCollectionDataSource(data.getCulturePrescribedFireReportData());
                    JasperPrint culturePrint = JasperFillManager.getInstance(repo.getContext())
                        .fillFromRepo("WFPREV_CULTURE_PRESCRIBED_FIRE_JASPER.jasper", new HashMap<>(), cultureDataSource);
                    prints.add(culturePrint);
                    sheetNames.add("CRx XLS Download");
                } catch (net.sf.jasperreports.engine.JRException e) {
                    LOG.error("Error filling Culture Prescribed Fire Jasper report", e);
                }
            }
            if (prints.isEmpty()) continue;

            ByteArrayOutputStream xlsxOut = new ByteArrayOutputStream();
            JRXlsxExporter exporter = new JRXlsxExporter();
            exporter.setExporterInput(SimpleExporterInput.getInstance(prints));
            exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(xlsxOut));

            SimpleXlsxReportConfiguration config = new SimpleXlsxReportConfiguration();
            config.setDetectCellType(true);
            config.setRemoveEmptySpaceBetweenRows(true);
            config.setRemoveEmptySpaceBetweenColumns(true);
            config.setCollapseRowSpan(true);
            config.setWhitePageBackground(false);
            config.setSheetNames(sheetNames.toArray(new String[0]));
            exporter.setConfiguration(config);

            try {
                exporter.exportReport();
                xlsxOut.flush();
            } catch (Exception e) {
                LOG.error("Error exporting XLSX for report", e);
                continue;
            }

            byte[] xlsxBytes = xlsxOut.toByteArray();
            if (xlsxBytes == null || xlsxBytes.length == 0) continue;

            String filename;
            if (report.getReportName() != null && !report.getReportName().isBlank()) {
                filename = report.getReportName() + ".xlsx";
            } else {
                filename = "report-" + report.getReportType().name().toLowerCase() + ".xlsx";
            }
            files.add(Map.of(
                "filename", filename,
                "content", Base64.getEncoder().encodeToString(xlsxBytes)
            ));
        }

        if (files.isEmpty()) {
            LOG.error("No valid XLSX files generated");
            writeErrorResponse(output, "No valid XLSX files generated");
            return;
        }

        Map<String, Object> response = new HashMap<>();
        response.put("statusCode", 200);
        response.put("headers", Map.of("Content-Type", "application/json"));
        response.put("body", mapper.writeValueAsString(Map.of("files", files)));
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
}