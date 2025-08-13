package ca.bc.gov.nrs.wfprev.services;

import ca.bc.gov.nrs.wfprev.common.exceptions.ServiceException;
import ca.bc.gov.nrs.wfprev.data.entities.CulturalPrescribedFireReportEntity;
import ca.bc.gov.nrs.wfprev.data.entities.FuelManagementReportEntity;
import ca.bc.gov.nrs.wfprev.data.entities.ProjectEntity;
import ca.bc.gov.nrs.wfprev.data.entities.ProjectFiscalEntity;
import ca.bc.gov.nrs.wfprev.data.repositories.CulturalPrescribedFireReportRepository;
import ca.bc.gov.nrs.wfprev.data.repositories.FuelManagementReportRepository;
import ca.bc.gov.nrs.wfprev.data.repositories.ProgramAreaRepository;
import ca.bc.gov.nrs.wfprev.data.repositories.ProjectRepository;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.SimpleXlsxReportConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Slf4j
@Component
public class ReportService {

    private volatile JasperReport fuelReport;
    private volatile JasperReport crxReport;

    @Value("${spring.application.baseUrl}")
    private String baseUrl;

    private static final String PROJECT_URL_PREFIX = "/edit-project?projectGuid=";

    private static final String FISCAL_QUERY_STRING = "&tab=fiscal&fiscalGuid=";

    DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            .withZone(ZoneId.systemDefault());

    private final FuelManagementReportRepository fuelManagementRepository;
    private final CulturalPrescribedFireReportRepository culturalPrescribedFireReportRepository;
    private final ProjectRepository projectRepository;
    private final ProgramAreaRepository programAreaRepository;

    public ReportService(FuelManagementReportRepository fuelManagementRepository, CulturalPrescribedFireReportRepository culturalPrescribedFireReportRepository,
                         ProjectRepository projectRepository, ProgramAreaRepository programAreaRepository) {
        this.fuelManagementRepository = fuelManagementRepository;
        this.culturalPrescribedFireReportRepository = culturalPrescribedFireReportRepository;
        this.projectRepository = projectRepository;
        this.programAreaRepository = programAreaRepository;
    }

    public void exportXlsx(List<UUID> projectGuids, OutputStream outputStream) throws ServiceException {
        try {
            List<ProjectEntity> projects = projectRepository.findByProjectGuidIn(projectGuids);
            List<UUID> projectPlanFiscalGuids = projects.stream()
                    .flatMap(p -> p.getProjectFiscals().stream())
                    .map(ProjectFiscalEntity::getProjectPlanFiscalGuid)
                    .distinct()
                    .toList();

            List<FuelManagementReportEntity> fuelData =
                    fuelManagementRepository.findByProjectPlanFiscalGuidIn(projectPlanFiscalGuids);
            List<CulturalPrescribedFireReportEntity> crxData =
                    culturalPrescribedFireReportRepository.findByProjectPlanFiscalGuidIn(projectPlanFiscalGuids);

            if (fuelData.isEmpty() && crxData.isEmpty()) {
                throw new IllegalArgumentException("No data found for the provided projectGuids.");
            }

            fuelData.forEach(this::setFuelManagementFields);
            crxData.forEach(this::setCrxFields);

            Map<String, Object> params = new HashMap<>();

            JasperPrint fuelPrint;
            try {
                fuelPrint = JasperFillManager.fillReport(
                        getFuelReport(), params, new JRBeanCollectionDataSource(fuelData)
                );
                log.debug("Filled FUEL report with {} records", fuelData.size());
            } catch (JRException e) {
                log.error("JRException filling FUEL report. Records: {}. First item: {}",
                        fuelData.size(), fuelData.isEmpty() ? "<none>" : fuelData.get(0), e);
                throw new ServiceException("Failed to fill FUEL report", e);
            }

            JasperPrint crxPrint;
            try {
                crxPrint = JasperFillManager.fillReport(
                        getCrxReport(), params, new JRBeanCollectionDataSource(crxData)
                );
                log.debug("Filled CRX report with {} records", crxData.size());
            } catch (JRException e) {
                log.error("JRException filling CRX report. Records: {}. First item: {}",
                        crxData.size(), crxData.isEmpty() ? "<none>" : crxData.get(0), e);
                throw new ServiceException("Failed to fill CRX report", e);
            }

            JRXlsxExporter exporter = new JRXlsxExporter();
            exporter.setExporterInput(SimpleExporterInput.getInstance(List.of(fuelPrint, crxPrint)));
            exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(outputStream));

            SimpleXlsxReportConfiguration config = new SimpleXlsxReportConfiguration();
            config.setDetectCellType(true);
            config.setRemoveEmptySpaceBetweenRows(true);
            config.setRemoveEmptySpaceBetweenColumns(true);
            config.setCollapseRowSpan(true);
            config.setWhitePageBackground(false);
            config.setSheetNames(new String[]{"FM XLS Download", "CRx XLS Download"});
            exporter.setConfiguration(config);

            try {
                exporter.exportReport(); // may throw JRException
                outputStream.flush();
                log.info("XLSX export completed successfully with two sheets.");
            } catch (JRException e) {
                log.error("JRException during XLSX export: {}", e.getMessage(), e);
                throw new ServiceException("Failed exporting XLSX", e);
            }
        } catch (ServiceException e) {
            // already logged above with details
            throw e;
        } catch (Exception e) {
            log.error("Failed to generate XLSX report (unexpected): {}", e.getMessage(), e);
            throw new ServiceException("Failed to generate XLSX report", e);
        }
    }

    public void writeCsvZipFromEntities(List<UUID> projectGuids, OutputStream zipOutStream) throws ServiceException {
        List<FuelManagementReportEntity> fuelRecords = fuelManagementRepository.findByProjectGuidIn(projectGuids);
        List<CulturalPrescribedFireReportEntity> crxRecords = culturalPrescribedFireReportRepository.findByProjectGuidIn(projectGuids);

        for (FuelManagementReportEntity f : fuelRecords) {
            setFuelManagementFields(f);
        }
        for (CulturalPrescribedFireReportEntity c : crxRecords) {
            setCrxFields(c);
        }

        try (ZipOutputStream zipOut = new ZipOutputStream(zipOutStream)) {

            ByteArrayOutputStream fuelCsvOut = new ByteArrayOutputStream();
            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fuelCsvOut))) {
                writer.write(getFuelCsvHeader());
                writer.newLine();

                for (FuelManagementReportEntity e : fuelRecords) {
                    writer.write(String.join(",", getFuelCsvRow(e)));
                    writer.newLine();
                }
            }
            addToZip(zipOut, "fuel-management-projects.csv", fuelCsvOut.toByteArray());

            ByteArrayOutputStream crxCsvOut = new ByteArrayOutputStream();
            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(crxCsvOut))) {
                writer.write(getCrxCsvHeader());
                writer.newLine();

                for (CulturalPrescribedFireReportEntity c : crxRecords) {
                    writer.write(String.join(",", getCrxCsvRow(c)));
                    writer.newLine();
                }
            }
            addToZip(zipOut, "cultural-prescribed-fire-projects.csv", crxCsvOut.toByteArray());

        } catch (Exception e) {
            log.error("Failed to generate CSV report: {}", e.getMessage(), e);
            throw new ServiceException("Failed to generate CSV report", e);
        }
    }

    private String safe(Object value) {
        if (value == null) return "";
        String str = value.toString().replace("\"", "\"\"");
        return "\"" + str + "\"";
    }

    private void setFuelManagementFields(FuelManagementReportEntity entity) {
        String urlPrefix = baseUrl + PROJECT_URL_PREFIX;
        if (entity.getProjectGuid() != null) {
            entity.setLinkToProject(urlPrefix + entity.getProjectGuid());
            if (entity.getProjectPlanFiscalGuid() != null) {
                entity.setLinkToFiscalActivity(urlPrefix  + entity.getProjectGuid() + FISCAL_QUERY_STRING + entity.getProjectPlanFiscalGuid());

            }

            if (entity.getProgramAreaGuid() != null) {
                programAreaRepository.findById(entity.getProgramAreaGuid()).ifPresent(programArea ->
                        entity.setBusinessArea(programArea.getProgramAreaName())
                );
            }

            // 2025 -> 2025/26 format
            String fiscalYear = formatFiscalYearIfNumeric(entity.getFiscalYear());
            if (fiscalYear != null) {
                entity.setFiscalYear(fiscalYear);
            }
        }
    }

    private void setCrxFields(CulturalPrescribedFireReportEntity entity) {
        String urlPrefix = baseUrl + PROJECT_URL_PREFIX;
            if (entity.getProjectGuid() != null) {
                entity.setLinkToProject(urlPrefix + entity.getProjectGuid());
                if (entity.getProjectPlanFiscalGuid() != null) {
                    entity.setLinkToFiscalActivity(urlPrefix + entity.getProjectGuid() + FISCAL_QUERY_STRING + entity.getProjectPlanFiscalGuid());

                }
                }

        if (entity.getProgramAreaGuid() != null) {
            programAreaRepository.findById(entity.getProgramAreaGuid()).ifPresent(programArea ->
                    entity.setBusinessArea(programArea.getProgramAreaName())
            );
        }

        // 2025 -> 2025/26 format
        String fiscalYear = formatFiscalYearIfNumeric(entity.getFiscalYear());
        if (fiscalYear != null) {
            entity.setFiscalYear(fiscalYear);
        }
    }

    private void addToZip(ZipOutputStream zipOut, String fileName, byte[] content) throws IOException {
        ZipEntry entry = new ZipEntry(fileName);
        zipOut.putNextEntry(entry);
        zipOut.write(content);
        zipOut.closeEntry();
    }

    private String getFuelCsvHeader() {
        return String.join(",", List.of(
                "Link to Project (within Prevention application)", "Link to Fiscal Activity (within Prevention application)",
                "Project Type", "Project Name", "FOR Region", "FOR District", "BC Parks Region",
                "BC Parks Section", "Fire Centre", "Business Area", "Planning Unit",
                "Gross Project Area (Ha calculated from spatial file)", "Closest Community", "Project Lead", "Proposal Type",
                "Fiscal Activity Name", "Fiscal Activity Description", "Fiscal Year", "Activity Category",
                "Fiscal Status", "Original Cost Estimate",
                "Ancillary Funding Amount", "Final Reported Spend", "CFS Actual Spend",
                "Planned Hectares", "Completed Hectares", "Spatial Submitted",
                "First Nation Engagement (Y/N)", "First Nation Co-Delivery (Y/N)", "First Nation Co-Delivery Partners",
                "Other Partners", "CFS Code", "RESULTS Project Code", "RESULTS Opening ID",
                "Primary Objective", "Secondary Objective (Optional)", "Endorsement Date",
                "Approval Date", "WUI Risk Class", "Local WUI Risk Class", "Local WUI Risk Class Rationale",
                "Total Point Value for Coarse Filter", "Total Point Value for Medium Filters", "Additional Comments/Notes on Medium Filters",
                "Total Point Value of Fine Filters", "Additional Comments/Notes on Fine Filters", "Total Filter Value"
        ));
    }

    private List<String> getFuelCsvRow(FuelManagementReportEntity e) {
        return List.of(
                safe(String.format("=HYPERLINK(\"%s\", \"%s Project Link\")",
                        e.getLinkToProject(), e.getProjectName())
                ),
                safe(String.format("=HYPERLINK(\"%s\", \"%s Fiscal Activity Link\")",
                        e.getLinkToFiscalActivity(), e.getProjectFiscalName())
                ), safe(e.getProjectTypeDescription()),
                safe(e.getProjectName()), safe(e.getForestRegionOrgUnitName()), safe(e.getForestDistrictOrgUnitName()),
                safe(e.getBcParksRegionOrgUnitName()), safe(e.getBcParksSectionOrgUnitName()), safe(e.getFireCentreOrgUnitName()),
                safe(e.getBusinessArea()), safe(e.getPlanningUnitName()), safe(e.getGrossProjectAreaHa() != null
                        ? String.format("%,d ha", e.getGrossProjectAreaHa().intValue())
                        : ""), safe(e.getClosestCommunityName()),
                safe(e.getProjectLead()), safe(e.getProposalTypeDescription()), safe(e.getProjectFiscalName()),
                safe(e.getProjectFiscalDescription()), safe(e.getFiscalYear()), safe(e.getActivityCategoryDescription()),
                safe(e.getPlanFiscalStatusDescription()), safe(formatMonetaryFields(e.getTotalEstimatedCostAmount())),
                safe(formatMonetaryFields(e.getFiscalAncillaryFundAmount())), safe(formatMonetaryFields(e.getFiscalReportedSpendAmount())), safe(formatMonetaryFields(e.getFiscalActualAmount())),
                safe(e.getFiscalPlannedProjectSizeHa() != null
                                ? String.format("%,d ha", e.getFiscalPlannedProjectSizeHa().intValue())
                                : ""),
                safe(e.getFiscalCompletedSizeHa() != null
                        ? String.format("%,d ha", e.getFiscalCompletedSizeHa().intValue())
                        : ""),
                safe(String.format("=\"%s\"", e.getSpatialSubmitted())),
                safe(e.getFirstNationsEngagement()), safe(e.getFirstNationsDelivPartners()), safe(e.getFirstNationsPartner()),
                safe(e.getOtherPartner()), safe(e.getCfsProjectCode()), safe(e.getResultsProjectCode()), safe(e.getResultsOpeningId()),
                safe(e.getPrimaryObjectiveTypeDescription()), safe(e.getSecondaryObjectiveTypeDescription()),
                safe(e.getEndorsementTimestamp() != null
                        ? DATE_FORMAT.format(e.getEndorsementTimestamp().toInstant())
                        : ""),
                safe(e.getApprovedTimestamp() != null
                        ? DATE_FORMAT.format(e.getApprovedTimestamp().toInstant())
                        : ""),
                safe(e.getWuiRiskClassDescription()), safe(e.getLocalWuiRiskClassDescription()), safe(e.getLocalWuiRiskClassRationale()),
                safe(e.getTotalCoarseFilterSectionScore()), safe(e.getTotalMediumFilterSectionScore()), safe(e.getMediumFilterSectionComment()),
                safe(e.getTotalFineFilterSectionScore()), safe(e.getFineFilterSectionComment()), safe(e.getTotalFilterSectionScore())
        );
    }

    private String getCrxCsvHeader() {
        return String.join(",", List.of(
                "Link to Project (within Prevention application)", "Link to Fiscal Activity (within Prevention application)",
                "Project Type", "Project Name", "FOR Region", "FOR District", "BC Parks Region",
                "BC Parks Section", "Fire Centre", "Business Area", "Planning Unit",
                "Gross Project Area (Ha calculated from spatial file)", "Closest Community", "Project Lead", "Proposal Type",
                "Fiscal Activity Name", "Fiscal Activity Description", "Fiscal Year", "Activity Category",
                "Fiscal Status", "Original Cost Estimate",
                "Ancillary Funding Amount", "Final Reported Spend", "CFS Actual Spend",
                "Planned Hectares", "Completed Hectares", "Spatial Submitted",
                "First Nation Engagement (Y/N)", "First Nation Co-Delivery (Y/N)", "First Nation Co-Delivery Partners",
                "Other Partners", "CFS Code", "RESULTS Project Code", "RESULTS Opening ID",
                "Primary Objective", "Secondary Objective (Optional)", "Endorsement Date",
                "Approval Date", "Outside WUI (Y/N)", "WUI Risk Class", "Local WUI Risk Class",
                "Risk Class & Location Total Point Value", "Additional Comments/Notes on risk class or outside WUI rationale",
                "Burn Development and Feasibility Total Point Value", "Additional Comments/Notes on Burn Development and Feasibility",
                "Collective Impact Total Point Value", "Additional Comments/Notes on Collective Impact", "Calculated Total"
        ));
    }

    private List<String> getCrxCsvRow(CulturalPrescribedFireReportEntity c) {
        return List.of(
                safe(String.format("=HYPERLINK(\"%s\", \"%s Project Link\")",
                        c.getLinkToProject(), c.getProjectName())
                ),
                safe(String.format("=HYPERLINK(\"%s\", \"%s Fiscal Activity Link\")",
                        c.getLinkToFiscalActivity(), c.getProjectFiscalName())
                ), safe(c.getProjectTypeDescription()),
                safe(c.getProjectName()), safe(c.getForestRegionOrgUnitName()), safe(c.getForestDistrictOrgUnitName()),
                safe(c.getBcParksRegionOrgUnitName()), safe(c.getBcParksSectionOrgUnitName()), safe(c.getFireCentreOrgUnitName()),
                safe(c.getBusinessArea()), safe(c.getPlanningUnitName()), safe(c.getGrossProjectAreaHa() != null
                        ? String.format("%,d ha", c.getGrossProjectAreaHa().intValue())
                        : ""), safe(c.getClosestCommunityName()),
                safe(c.getProjectLead()), safe(c.getProposalTypeDescription()), safe(c.getProjectFiscalName()),
                safe(c.getProjectFiscalDescription()), safe(c.getFiscalYear()), safe(c.getActivityCategoryDescription()),
                safe(c.getPlanFiscalStatusDescription()), safe(formatMonetaryFields(c.getTotalEstimatedCostAmount())),
                safe(formatMonetaryFields(c.getFiscalAncillaryFundAmount())), safe(formatMonetaryFields(c.getFiscalReportedSpendAmount())), safe(formatMonetaryFields(c.getFiscalActualAmount())),
                safe(c.getFiscalPlannedProjectSizeHa() != null
                        ? String.format("%,d ha", c.getFiscalPlannedProjectSizeHa().intValue())
                        : ""),
                safe(c.getFiscalCompletedSizeHa() != null
                        ? String.format("%,d ha", c.getFiscalCompletedSizeHa().intValue())
                        : ""), safe(String.format("=\"%s\"", c.getSpatialSubmitted())),
                safe(c.getFirstNationsEngagement()), safe(c.getFirstNationsDelivPartners()), safe(c.getFirstNationsPartner()),
                safe(c.getOtherPartner()), safe(c.getCfsProjectCode()), safe(c.getResultsProjectCode()), safe(c.getResultsOpeningId()),
                safe(c.getPrimaryObjectiveTypeDescription()), safe(c.getSecondaryObjectiveTypeDescription()),
                safe(c.getEndorsementTimestamp() != null
                        ? DATE_FORMAT.format(c.getEndorsementTimestamp().toInstant())
                        : ""),
                safe(c.getApprovedTimestamp() != null
                        ? DATE_FORMAT.format(c.getApprovedTimestamp().toInstant())
                        : ""), safe(c.getOutsideWuiInd() ? "Y" : "N"),
                safe(c.getWuiRiskClassDescription()), safe(c.getLocalWuiRiskClassDescription()), safe(c.getTotalRclFilterSectionScore()),
                safe(c.getRclFilterSectionComment()), safe(c.getTotalBdfFilterSectionScore()), safe(c.getBdfFilterSectionComment()),
                safe(c.getTotalCollimpFilterSectionScore()), safe(c.getCollimpFilterSectionComment()), safe(c.getTotalFilterSectionScore())
        );
    }

    private static String formatMonetaryFields(Number n) {
        if (n == null) return "";
        return new java.text.DecimalFormat("$#,##0").format(n);
    }

    private String formatFiscalYearIfNumeric(Object fiscalYear) {
        if (fiscalYear == null) return null;
        try {
            int year = Integer.parseInt(fiscalYear.toString());
            return year + "/" + String.format("%02d", (year + 1) % 100);
        } catch (NumberFormatException e) {
            return null; // keep original value unchanged
        }
    }

    private JasperReport loadOrCompile(String jasperPath, String jrxmlPath) {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();

        log.info("JR runtime version = {}",
                net.sf.jasperreports.engine.util.JRLoader.class.getPackage().getImplementationVersion());

        // 1) Try precompiled .jasper
        try (InputStream in = cl.getResourceAsStream(jasperPath)) {
            if (in != null) {
                Object obj = JRLoader.loadObject(in); // may throw JRException
                if (obj instanceof JasperReport jr) {
                    log.debug("Loaded precompiled Jasper report: {}", jasperPath);
                    return jr;
                } else {
                    log.warn("Object at {} is not a JasperReport: {}", jasperPath, obj);
                }
            } else {
                log.debug("Precompiled report not found on classpath: {}", jasperPath);
            }
        } catch (JRException e) {
            log.error("JRException while loading precompiled report {}: {}", jasperPath, e.getMessage(), e);
            throw new RuntimeException("Failed loading precompiled report: " + jasperPath, e);
        } catch (Exception e) {
            log.warn("Unexpected error loading precompiled report {}: {}", jasperPath, e.getMessage(), e);
        }

        // 2) Fall back to compiling .jrxml
        try (InputStream in = cl.getResourceAsStream(jrxmlPath)) {
            if (in == null) {
                throw new IllegalStateException("JRXML not found on classpath: " + jrxmlPath);
            }
            JasperReport compiled = JasperCompileManager.compileReport(in); // throws JRException
            log.debug("Compiled JRXML successfully: {}", jrxmlPath);
            return compiled;
        } catch (JRException e) {
            log.error("JRException compiling JRXML {}: {}", jrxmlPath, e.getMessage(), e);
            throw new RuntimeException("Failed to compile JRXML: " + jrxmlPath, e);
        } catch (Exception e) {
            log.error("Unexpected error compiling JRXML {}: {}", jrxmlPath, e.getMessage(), e);
            throw new RuntimeException("Failed to initialize Jasper report: " + jrxmlPath, e);
        }
    }


    private JasperReport getFuelReport() {
        JasperReport report = fuelReport;
        if (report == null) {
            synchronized (this) {
                report = fuelReport;
                if (report == null) {
                    fuelReport = report = loadOrCompile(
                            "jasper-template/WFPREV_FUEL_MANAGEMENT_JASPER.jasper",
                            "jasper-template/WFPREV_FUEL_MANAGEMENT_JASPER.jrxml"
                    );
                }
            }
        }
        return report;
    }

    private JasperReport getCrxReport() {
        JasperReport report = crxReport;
        if (report == null) {
            synchronized (this) {
                report = crxReport;
                if (report == null) {
                    crxReport = report = loadOrCompile(
                            "jasper-template/WFPREV_CULTURE_PRESCRIBED_FIRE_JASPER.jasper",
                            "jasper-template/WFPREV_CULTURE_PRESCRIBED_FIRE_JASPER.jrxml"
                    );
                }
            }
        }
        return report;
    }



}
