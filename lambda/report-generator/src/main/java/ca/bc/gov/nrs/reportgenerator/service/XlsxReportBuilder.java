package ca.bc.gov.nrs.reportgenerator.service;

import ca.bc.gov.nrs.reportgenerator.model.LambdaEvent;
import ca.bc.gov.nrs.reportgenerator.model.Report;
import ca.bc.gov.nrs.reportgenerator.model.XlsxReportData;
import io.quarkiverse.jasperreports.repository.ReadOnlyStreamingService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.SimpleXlsxReportConfiguration;
import org.jboss.logging.Logger;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/** Fills the Jasper templates and exports one XLSX per report in the event. */
@ApplicationScoped
public class XlsxReportBuilder {
    private static final Logger LOG = Logger.getLogger(XlsxReportBuilder.class);

    /** One finished workbook. */
    public record GeneratedXlsx(String filename, byte[] content) {
    }

    @Inject
    ReadOnlyStreamingService repo;

    /** Reports with no data, or whose export fails, are skipped, so the result can be empty. */
    public List<GeneratedXlsx> build(LambdaEvent event) {
        List<GeneratedXlsx> files = new ArrayList<>();
        if (event == null || event.getReports() == null) {
            return files;
        }
        for (Report report : event.getReports()) {
            XlsxReportData data = report.getXlsxReportData();
            LOG.infof("Report '%s': %s", report.getReportName(), data == null ? "no xlsxReportData" : rowCounts(data));
            if (data == null) continue;
            List<JasperPrint> prints = new ArrayList<>();
            List<String> sheetNames = new ArrayList<>();
            if (data.getProjectFuelManagementReportData() != null && !data.getProjectFuelManagementReportData().isEmpty()) {
                try {
                    JRDataSource fuelDataSource = new JRBeanCollectionDataSource(data.getProjectFuelManagementReportData());
                    JasperPrint fuelPrint = JasperFillManager.getInstance(repo.getContext())
                        .fillFromRepo("WFPREV_FUEL_MANAGEMENT_JASPER.jasper", new HashMap<>(), fuelDataSource);
                    prints.add(fuelPrint);
                    sheetNames.add("FM XLS Download");
                } catch (net.sf.jasperreports.engine.JRException e) {
                    LOG.error("Error filling Fuel Management Jasper report", e);
                }
            }
            if (data.getResultsFuelManagementReportData() != null && !data.getResultsFuelManagementReportData().isEmpty()) {
                try {
                    JRDataSource resultsFuelDataSource = new JRBeanCollectionDataSource(data.getResultsFuelManagementReportData());
                    JasperPrint resultsFuelPrint = JasperFillManager.getInstance(repo.getContext())
                        .fillFromRepo("WFPREV_RESULTS_JASPER.jasper", new HashMap<>(), resultsFuelDataSource);
                    prints.add(resultsFuelPrint);
                    sheetNames.add("FM XLS Download");
                } catch (Exception e) {
                    LOG.error("Error filling Results Fuel Management Jasper report", e);
                }
            }
            if (data.getProjectCulturePrescribedFireReportData() != null && !data.getProjectCulturePrescribedFireReportData().isEmpty()) {
                try {
                    JRDataSource cultureDataSource = new JRBeanCollectionDataSource(data.getProjectCulturePrescribedFireReportData());
                    JasperPrint culturePrint = JasperFillManager.getInstance(repo.getContext())
                        .fillFromRepo("WFPREV_CULTURE_PRESCRIBED_FIRE_JASPER.jasper", new HashMap<>(), cultureDataSource);
                    prints.add(culturePrint);
                    sheetNames.add("CRx XLS Download");
                } catch (net.sf.jasperreports.engine.JRException e) {
                    LOG.error("Error filling Culture Prescribed Fire Jasper report", e);
                }
            }
            if (data.getResultsCulturePrescribedFireReportData() != null && !data.getResultsCulturePrescribedFireReportData().isEmpty()) {
                try {
                    JRDataSource resultsCultureDataSource = new JRBeanCollectionDataSource(data.getResultsCulturePrescribedFireReportData());
                    JasperPrint resultsCulturePrint = JasperFillManager.getInstance(repo.getContext())
                        .fillFromRepo("WFPREV_RESULTS_JASPER.jasper", new HashMap<>(), resultsCultureDataSource);
                    prints.add(resultsCulturePrint);
                    sheetNames.add("CRx XLS Download");
                } catch (Exception e) {
                    LOG.error("Error filling Results Culture Prescribed Fire Jasper report", e);
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
            files.add(new GeneratedXlsx(filename, xlsxBytes));
        }
        return files;
    }

    private static String rowCounts(XlsxReportData data) {
        return "rows projectFuelManagement=" + size(data.getProjectFuelManagementReportData())
            + ", projectCulturePrescribedFire=" + size(data.getProjectCulturePrescribedFireReportData())
            + ", resultsFuelManagement=" + size(data.getResultsFuelManagementReportData())
            + ", resultsCulturePrescribedFire=" + size(data.getResultsCulturePrescribedFireReportData());
    }

    private static int size(List<?> rows) {
        return rows == null ? 0 : rows.size();
    }
}
