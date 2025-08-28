package ca.bc.gov.nrs.wfprev.services;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import ca.bc.gov.nrs.wfprev.common.exceptions.ServiceException;
import ca.bc.gov.nrs.wfprev.data.entities.CulturalPrescribedFireReportEntity;
import ca.bc.gov.nrs.wfprev.data.entities.FuelManagementReportEntity;
import ca.bc.gov.nrs.wfprev.data.entities.ProjectEntity;
import ca.bc.gov.nrs.wfprev.data.entities.ProjectFiscalEntity;
import ca.bc.gov.nrs.wfprev.data.repositories.CulturalPrescribedFireReportRepository;
import ca.bc.gov.nrs.wfprev.data.repositories.FuelManagementReportRepository;
import ca.bc.gov.nrs.wfprev.data.repositories.ProgramAreaRepository;
import ca.bc.gov.nrs.wfprev.data.repositories.ProjectRepository;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class ReportService {

    // @Value("${spring.application.baseUrl}")
    private String baseUrl = "http://localhost:4200";

    private static final String PROJECT_URL_PREFIX = "/edit-project?projectGuid=";

    private static final String FISCAL_QUERY_STRING = "&tab=fiscal&fiscalGuid=";

    DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            .withZone(ZoneId.systemDefault());

    private final FuelManagementReportRepository fuelManagementRepository;
    private final CulturalPrescribedFireReportRepository culturalPrescribedFireReportRepository;
    private final ProjectRepository projectRepository;
    private final ProgramAreaRepository programAreaRepository;

    public ReportService(FuelManagementReportRepository fuelManagementRepository,
            CulturalPrescribedFireReportRepository culturalPrescribedFireReportRepository,
            ProjectRepository projectRepository, ProgramAreaRepository programAreaRepository) {
        this.fuelManagementRepository = fuelManagementRepository;
        this.culturalPrescribedFireReportRepository = culturalPrescribedFireReportRepository;
        this.projectRepository = projectRepository;
        this.programAreaRepository = programAreaRepository;
    }

    public void exportXlsx(List<UUID> projectGuids, OutputStream outputStream, String rid)
            throws ServiceException, IOException, InterruptedException {
        long tStart = System.currentTimeMillis();
        log.info("[{}] [xlsx] start projectGuids={}", rid, projectGuids.size());

        // Fetch projects and fiscal GUIDs
        List<ProjectEntity> projects = projectRepository.findByProjectGuidIn(projectGuids);
        List<UUID> projectPlanFiscalGuids = projects.stream()
                .flatMap(p -> p.getProjectFiscals().stream())
                .map(ProjectFiscalEntity::getProjectPlanFiscalGuid)
                .distinct()
                .toList();

        // Fetch report rows
        List<FuelManagementReportEntity> fuelData = fuelManagementRepository
                .findByProjectPlanFiscalGuidIn(projectPlanFiscalGuids);
        List<CulturalPrescribedFireReportEntity> crxData = culturalPrescribedFireReportRepository
                .findByProjectPlanFiscalGuidIn(projectPlanFiscalGuids);

        if (fuelData.isEmpty() && crxData.isEmpty()) {
            log.info("[{}] [xlsx] no data for given projectGuids", rid);
            throw new IllegalArgumentException("No data found for the provided projectGuids.");
        }

        // Set computed fields
        fuelData.forEach(this::setFuelManagementFields);
        crxData.forEach(this::setCrxFields);

        // Build Lambda request model
        LambdaReportRequest lambdaRequest = new LambdaReportRequest();
        LambdaReportRequest.Report report = new LambdaReportRequest.Report();
        report.setReportType("XLSX");
        report.setReportName("project-report");
        LambdaReportRequest.XlsxReportData xlsxData = new LambdaReportRequest.XlsxReportData();
        xlsxData.setFuelManagementReportData(fuelData);
        xlsxData.setCulturePrescribedFireReportData(crxData);
        report.setXlsxReportData(xlsxData);
        lambdaRequest.setReports(java.util.List.of(report));

        // Call Lambda
        String lambdaUrl = System.getenv("REPORT_GENERATOR_LAMBDA_URL");
        if (lambdaUrl == null || lambdaUrl.isBlank()) {
            throw new ServiceException("REPORT_GENERATOR_LAMBDA_URL environment variable is not set");
        }

        ObjectMapper mapper = new ObjectMapper();
        String requestJson = mapper.writeValueAsString(lambdaRequest);

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(lambdaUrl))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestJson))
                .build();

        HttpResponse<String> response = client.send(httpRequest,
                HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new ServiceException("Lambda returned error: " + response.body());
        }

        // Parse Lambda response and write first file to outputStream
        LambdaReportResponse lambdaResponse = mapper.readValue(response.body(), LambdaReportResponse.class);
        if (lambdaResponse.getFiles() == null || lambdaResponse.getFiles().isEmpty()) {
            throw new ServiceException("No files returned from Lambda");
        }
        LambdaReportResponse.File file = lambdaResponse.getFiles().get(0);
        byte[] xlsxBytes = java.util.Base64.getDecoder().decode(file.getContent());
        outputStream.write(xlsxBytes);
        outputStream.flush();
        log.info("[{}] [xlsx] export complete ({} ms)", rid, (System.currentTimeMillis() - tStart));

    }

    public void writeCsvZipFromEntities(List<UUID> projectGuids, OutputStream zipOutStream) throws ServiceException {
        List<FuelManagementReportEntity> fuelRecords = fuelManagementRepository.findByProjectGuidIn(projectGuids);
        List<CulturalPrescribedFireReportEntity> crxRecords = culturalPrescribedFireReportRepository
                .findByProjectGuidIn(projectGuids);

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
            log.info("Failed to generate CSV report: {}", e.getMessage(), e);
            throw new ServiceException("Failed to generate CSV report", e);
        }
    }

    private String safe(Object value) {
        if (value == null)
            return "";
        String str = value.toString().replace("\"", "\"\"");
        return "\"" + str + "\"";
    }

    private void setFuelManagementFields(FuelManagementReportEntity entity) {
        String urlPrefix = baseUrl + PROJECT_URL_PREFIX;
        if (entity.getProjectGuid() != null) {
            entity.setLinkToProject(urlPrefix + entity.getProjectGuid());
            if (entity.getProjectPlanFiscalGuid() != null) {
                entity.setLinkToFiscalActivity(
                        urlPrefix + entity.getProjectGuid() + FISCAL_QUERY_STRING + entity.getProjectPlanFiscalGuid());

            }

            if (entity.getProgramAreaGuid() != null) {
                programAreaRepository.findById(entity.getProgramAreaGuid())
                        .ifPresent(programArea -> entity.setBusinessArea(programArea.getProgramAreaName()));
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
                entity.setLinkToFiscalActivity(
                        urlPrefix + entity.getProjectGuid() + FISCAL_QUERY_STRING + entity.getProjectPlanFiscalGuid());

            }
        }

        if (entity.getProgramAreaGuid() != null) {
            programAreaRepository.findById(entity.getProgramAreaGuid())
                    .ifPresent(programArea -> entity.setBusinessArea(programArea.getProgramAreaName()));
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
                "Link to Project (within Prevention application)",
                "Link to Fiscal Activity (within Prevention application)",
                "Project Type", "Project Name", "FOR Region", "FOR District", "BC Parks Region",
                "BC Parks Section", "Fire Centre", "Business Area", "Planning Unit",
                "Gross Project Area (Ha calculated from spatial file)", "Closest Community", "Project Lead",
                "Proposal Type",
                "Fiscal Activity Name", "Fiscal Activity Description", "Fiscal Year", "Activity Category",
                "Fiscal Status", "Original Cost Estimate",
                "Ancillary Funding Amount", "Final Reported Spend", "CFS Actual Spend",
                "Planned Hectares", "Completed Hectares", "Spatial Submitted",
                "First Nation Engagement (Y/N)", "First Nation Co-Delivery (Y/N)", "First Nation Co-Delivery Partners",
                "Other Partners", "CFS Code", "RESULTS Project Code", "RESULTS Opening ID",
                "Primary Objective", "Secondary Objective (Optional)", "Endorsement Date",
                "Approval Date", "WUI Risk Class", "Local WUI Risk Class", "Local WUI Risk Class Rationale",
                "Total Point Value for Coarse Filter", "Total Point Value for Medium Filters",
                "Additional Comments/Notes on Medium Filters",
                "Total Point Value of Fine Filters", "Additional Comments/Notes on Fine Filters",
                "Total Filter Value"));
    }

    private List<String> getFuelCsvRow(FuelManagementReportEntity e) {
        return List.of(
                safe(String.format("=HYPERLINK(\"%s\", \"%s Project Link\")",
                        e.getLinkToProject(), e.getProjectName())),
                safe(String.format("=HYPERLINK(\"%s\", \"%s Fiscal Activity Link\")",
                        e.getLinkToFiscalActivity(), e.getProjectFiscalName())),
                safe(e.getProjectTypeDescription()),
                safe(e.getProjectName()), safe(e.getForestRegionOrgUnitName()), safe(e.getForestDistrictOrgUnitName()),
                safe(e.getBcParksRegionOrgUnitName()), safe(e.getBcParksSectionOrgUnitName()),
                safe(e.getFireCentreOrgUnitName()),
                safe(e.getBusinessArea()), safe(e.getPlanningUnitName()), safe(e.getGrossProjectAreaHa() != null
                        ? String.format("%,d ha", e.getGrossProjectAreaHa().intValue())
                        : ""),
                safe(e.getClosestCommunityName()),
                safe(e.getProjectLead()), safe(e.getProposalTypeDescription()), safe(e.getProjectFiscalName()),
                safe(e.getProjectFiscalDescription()), safe(e.getFiscalYear()),
                safe(e.getActivityCategoryDescription()),
                safe(e.getPlanFiscalStatusDescription()), safe(formatMonetaryFields(e.getTotalEstimatedCostAmount())),
                safe(formatMonetaryFields(e.getFiscalAncillaryFundAmount())),
                safe(formatMonetaryFields(e.getFiscalReportedSpendAmount())),
                safe(formatMonetaryFields(e.getFiscalActualAmount())),
                safe(e.getFiscalPlannedProjectSizeHa() != null
                        ? String.format("%,d ha", e.getFiscalPlannedProjectSizeHa().intValue())
                        : ""),
                safe(e.getFiscalCompletedSizeHa() != null
                        ? String.format("%,d ha", e.getFiscalCompletedSizeHa().intValue())
                        : ""),
                safe(String.format("=\"%s\"", e.getSpatialSubmitted())),
                safe(e.getFirstNationsEngagement()), safe(e.getFirstNationsDelivPartners()),
                safe(e.getFirstNationsPartner()),
                safe(e.getOtherPartner()), safe(e.getCfsProjectCode()), safe(e.getResultsProjectCode()),
                safe(e.getResultsOpeningId()),
                safe(e.getPrimaryObjectiveTypeDescription()), safe(e.getSecondaryObjectiveTypeDescription()),
                safe(e.getEndorsementTimestamp() != null
                        ? DATE_FORMAT.format(e.getEndorsementTimestamp().toInstant())
                        : ""),
                safe(e.getApprovedTimestamp() != null
                        ? DATE_FORMAT.format(e.getApprovedTimestamp().toInstant())
                        : ""),
                safe(e.getWuiRiskClassDescription()), safe(e.getLocalWuiRiskClassDescription()),
                safe(e.getLocalWuiRiskClassRationale()),
                safe(e.getTotalCoarseFilterSectionScore()), safe(e.getTotalMediumFilterSectionScore()),
                safe(e.getMediumFilterSectionComment()),
                safe(e.getTotalFineFilterSectionScore()), safe(e.getFineFilterSectionComment()),
                safe(e.getTotalFilterSectionScore()));
    }

    private String getCrxCsvHeader() {
        return String.join(",", List.of(
                "Link to Project (within Prevention application)",
                "Link to Fiscal Activity (within Prevention application)",
                "Project Type", "Project Name", "FOR Region", "FOR District", "BC Parks Region",
                "BC Parks Section", "Fire Centre", "Business Area", "Planning Unit",
                "Gross Project Area (Ha calculated from spatial file)", "Closest Community", "Project Lead",
                "Proposal Type",
                "Fiscal Activity Name", "Fiscal Activity Description", "Fiscal Year", "Activity Category",
                "Fiscal Status", "Original Cost Estimate",
                "Ancillary Funding Amount", "Final Reported Spend", "CFS Actual Spend",
                "Planned Hectares", "Completed Hectares", "Spatial Submitted",
                "First Nation Engagement (Y/N)", "First Nation Co-Delivery (Y/N)", "First Nation Co-Delivery Partners",
                "Other Partners", "CFS Code", "RESULTS Project Code", "RESULTS Opening ID",
                "Primary Objective", "Secondary Objective (Optional)", "Endorsement Date",
                "Approval Date", "Outside WUI (Y/N)", "WUI Risk Class", "Local WUI Risk Class",
                "Risk Class & Location Total Point Value",
                "Additional Comments/Notes on risk class or outside WUI rationale",
                "Burn Development and Feasibility Total Point Value",
                "Additional Comments/Notes on Burn Development and Feasibility",
                "Collective Impact Total Point Value", "Additional Comments/Notes on Collective Impact",
                "Calculated Total"));
    }

    private List<String> getCrxCsvRow(CulturalPrescribedFireReportEntity c) {
        return List.of(
                safe(String.format("=HYPERLINK(\"%s\", \"%s Project Link\")",
                        c.getLinkToProject(), c.getProjectName())),
                safe(String.format("=HYPERLINK(\"%s\", \"%s Fiscal Activity Link\")",
                        c.getLinkToFiscalActivity(), c.getProjectFiscalName())),
                safe(c.getProjectTypeDescription()),
                safe(c.getProjectName()), safe(c.getForestRegionOrgUnitName()), safe(c.getForestDistrictOrgUnitName()),
                safe(c.getBcParksRegionOrgUnitName()), safe(c.getBcParksSectionOrgUnitName()),
                safe(c.getFireCentreOrgUnitName()),
                safe(c.getBusinessArea()), safe(c.getPlanningUnitName()), safe(c.getGrossProjectAreaHa() != null
                        ? String.format("%,d ha", c.getGrossProjectAreaHa().intValue())
                        : ""),
                safe(c.getClosestCommunityName()),
                safe(c.getProjectLead()), safe(c.getProposalTypeDescription()), safe(c.getProjectFiscalName()),
                safe(c.getProjectFiscalDescription()), safe(c.getFiscalYear()),
                safe(c.getActivityCategoryDescription()),
                safe(c.getPlanFiscalStatusDescription()), safe(formatMonetaryFields(c.getTotalEstimatedCostAmount())),
                safe(formatMonetaryFields(c.getFiscalAncillaryFundAmount())),
                safe(formatMonetaryFields(c.getFiscalReportedSpendAmount())),
                safe(formatMonetaryFields(c.getFiscalActualAmount())),
                safe(c.getFiscalPlannedProjectSizeHa() != null
                        ? String.format("%,d ha", c.getFiscalPlannedProjectSizeHa().intValue())
                        : ""),
                safe(c.getFiscalCompletedSizeHa() != null
                        ? String.format("%,d ha", c.getFiscalCompletedSizeHa().intValue())
                        : ""),
                safe(String.format("=\"%s\"", c.getSpatialSubmitted())),
                safe(c.getFirstNationsEngagement()), safe(c.getFirstNationsDelivPartners()),
                safe(c.getFirstNationsPartner()),
                safe(c.getOtherPartner()), safe(c.getCfsProjectCode()), safe(c.getResultsProjectCode()),
                safe(c.getResultsOpeningId()),
                safe(c.getPrimaryObjectiveTypeDescription()), safe(c.getSecondaryObjectiveTypeDescription()),
                safe(c.getEndorsementTimestamp() != null
                        ? DATE_FORMAT.format(c.getEndorsementTimestamp().toInstant())
                        : ""),
                safe(c.getApprovedTimestamp() != null
                        ? DATE_FORMAT.format(c.getApprovedTimestamp().toInstant())
                        : ""),
                safe(c.getOutsideWuiInd() ? "Y" : "N"),
                safe(c.getWuiRiskClassDescription()), safe(c.getLocalWuiRiskClassDescription()),
                safe(c.getTotalRclFilterSectionScore()),
                safe(c.getRclFilterSectionComment()), safe(c.getTotalBdfFilterSectionScore()),
                safe(c.getBdfFilterSectionComment()),
                safe(c.getTotalCollimpFilterSectionScore()), safe(c.getCollimpFilterSectionComment()),
                safe(c.getTotalFilterSectionScore()));
    }

    private static String formatMonetaryFields(Number n) {
        if (n == null)
            return "";
        return new java.text.DecimalFormat("$#,##0").format(n);
    }

    private String formatFiscalYearIfNumeric(Object fiscalYear) {
        if (fiscalYear == null)
            return null;
        try {
            int year = Integer.parseInt(fiscalYear.toString());
            return year + "/" + String.format("%02d", (year + 1) % 100);
        } catch (NumberFormatException e) {
            return null; // keep original value unchanged
        }
    }

    // POJO for Lambda response
    public static class LambdaReportResponse {
        private java.util.List<File> files;

        public java.util.List<File> getFiles() {
            return files;
        }

        public void setFiles(java.util.List<File> files) {
            this.files = files;
        }

        public static class File {
            private String filename;
            private String content;

            public String getFilename() {
                return filename;
            }

            public void setFilename(String filename) {
                this.filename = filename;
            }

            public String getContent() {
                return content;
            }

            public void setContent(String content) {
                this.content = content;
            }
        }
    }

    // POJO for Lambda request
    public static class LambdaReportRequest {
        private java.util.List<Report> reports;

        public java.util.List<Report> getReports() {
            return reports;
        }

        public void setReports(java.util.List<Report> reports) {
            this.reports = reports;
        }

        public static class Report {
            private String reportType;
            private String reportName;
            private XlsxReportData xlsxReportData;

            public String getReportType() {
                return reportType;
            }

            public void setReportType(String reportType) {
                this.reportType = reportType;
            }

            public String getReportName() {
                return reportName;
            }

            public void setReportName(String reportName) {
                this.reportName = reportName;
            }

            public XlsxReportData getXlsxReportData() {
                return xlsxReportData;
            }

            public void setXlsxReportData(XlsxReportData xlsxReportData) {
                this.xlsxReportData = xlsxReportData;
            }
        }

        public static class XlsxReportData {
            private java.util.List<?> culturePrescribedFireReportData;
            private java.util.List<?> fuelManagementReportData;

            public java.util.List<?> getCulturePrescribedFireReportData() {
                return culturePrescribedFireReportData;
            }

            public void setCulturePrescribedFireReportData(java.util.List<?> data) {
                this.culturePrescribedFireReportData = data;
            }

            public java.util.List<?> getFuelManagementReportData() {
                return fuelManagementReportData;
            }

            public void setFuelManagementReportData(java.util.List<?> data) {
                this.fuelManagementReportData = data;
            }
        }
    }
}
