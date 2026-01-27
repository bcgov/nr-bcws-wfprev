package ca.bc.gov.nrs.wfprev.services;

import ca.bc.gov.nrs.wfone.common.service.api.ServiceException;
import ca.bc.gov.nrs.wfprev.data.entities.CulturalPrescribedFireReportEntity;
import ca.bc.gov.nrs.wfprev.data.entities.FuelManagementReportEntity;
import ca.bc.gov.nrs.wfprev.data.entities.ProjectFiscalEntity;
import ca.bc.gov.nrs.wfprev.data.models.ReportRequestModel;
import ca.bc.gov.nrs.wfprev.data.params.FeatureQueryParams;
import ca.bc.gov.nrs.wfprev.data.repositories.CulturalPrescribedFireReportRepository;
import ca.bc.gov.nrs.wfprev.data.repositories.FuelManagementReportRepository;
import ca.bc.gov.nrs.wfprev.data.repositories.ProgramAreaRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Slf4j
@Component
public class ReportService {

    @Value("${REPORT_GENERATOR_LAMBDA_URL}")
    private String reportGeneratorLambdaUrl;

     @Value("${spring.application.baseUrl}")
    private String baseUrl;

    private static final String PROJECT_URL_PREFIX = "/edit-project?projectGuid=";

    private static final String FISCAL_QUERY_STRING = "&tab=fiscal&fiscalGuid=";

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            .withZone(ZoneId.systemDefault());

    private static final String HECTARE_FORMAT = "%,d ha";

    private final FuelManagementReportRepository fuelManagementRepository;
    private final CulturalPrescribedFireReportRepository culturalPrescribedFireReportRepository;
    private final ProgramAreaRepository programAreaRepository;
    private final FeaturesService featuresService;

    public ReportService(FuelManagementReportRepository fuelManagementRepository,
            CulturalPrescribedFireReportRepository culturalPrescribedFireReportRepository,
            ProgramAreaRepository programAreaRepository,
            FeaturesService featuresService) {
        this.fuelManagementRepository = fuelManagementRepository;
        this.culturalPrescribedFireReportRepository = culturalPrescribedFireReportRepository;
        this.programAreaRepository = programAreaRepository;
        this.featuresService = featuresService;
    }

    private static class ReportDataBundle {
        List<FuelManagementReportEntity> fuel;
        List<CulturalPrescribedFireReportEntity> crx;

        ReportDataBundle(List<FuelManagementReportEntity> fuel,
                         List<CulturalPrescribedFireReportEntity> crx) {
            this.fuel = fuel;
            this.crx = crx;
        }
    }

    private ReportDataBundle resolveReportData(ReportRequestModel request) {
        List<FuelManagementReportEntity> fuel = new ArrayList<>();
        List<CulturalPrescribedFireReportEntity> crx = new ArrayList<>();

        if (request == null || ((request.getProjects() == null || request.getProjects().isEmpty()) && request.getProjectFilter() == null)) {
            throw new IllegalArgumentException("At least one project or a filter is required");
        }

        List<ReportRequestModel.Project> projectsToReport = request.getProjects();

        if (projectsToReport == null || projectsToReport.isEmpty()) {
            // Fetch projects using filter
            var entities = featuresService.findFilteredProjects(request.getProjectFilter(), 1, Integer.MAX_VALUE, null, null);
            projectsToReport = new ArrayList<>();
            for (var entity : entities) {
                ReportRequestModel.Project p = new ReportRequestModel.Project();
                p.setProjectGuid(entity.getProjectGuid());

                FeatureQueryParams filter = request.getProjectFilter();
                List<ProjectFiscalEntity> fiscals = featuresService.findFilteredProjectFiscals(
                        entity.getProjectGuid(),
                        filter.getFiscalYears(),
                        filter.getActivityCategoryCodes(),
                        filter.getPlanFiscalStatusCodes()
                );

                if (!fiscals.isEmpty()) {
                    p.setProjectFiscalGuids(fiscals.stream()
                            .map(ProjectFiscalEntity::getProjectPlanFiscalGuid)
                            .toList());
                } else { 
                    p.setProjectFiscalGuids(new ArrayList<>());
                }
                
                projectsToReport.add(p);
            }
        }

        for (ReportRequestModel.Project p : projectsToReport) {
            UUID projectGuid = Objects.requireNonNull(p.getProjectGuid(), "projectGuid is required");
            List<UUID> fiscals = p.getProjectFiscalGuids();

            if (fiscals != null && !fiscals.isEmpty()) {
                crx.addAll(culturalPrescribedFireReportRepository
                        .findByProjectGuidAndProjectPlanFiscalGuidIn(projectGuid, fiscals));
                fuel.addAll(fuelManagementRepository
                        .findByProjectGuidAndProjectPlanFiscalGuidIn(projectGuid, fiscals));
            } else {
                // Now includes rows where project_plan_fiscal_guid IS NULL
                crx.addAll(culturalPrescribedFireReportRepository.findByProjectGuid(projectGuid));
                fuel.addAll(fuelManagementRepository.findByProjectGuid(projectGuid));
            }
        }

        return new ReportDataBundle(fuel, crx);
    }


    public void exportXlsx(ReportRequestModel request, OutputStream outputStream)
            throws ServiceException, IOException, InterruptedException {
        ReportDataBundle data = resolveReportData(request);

        // Remove nulls up front (defensive)
        data.fuel.removeIf(Objects::isNull);
        data.crx.removeIf(Objects::isNull);

        // Pre-process
        data.fuel.forEach(this::setFuelManagementFields);
        data.crx.forEach(this::setCrxFields);

        // If absolutely nothing to write, fail early
        if (data.fuel.isEmpty() && data.crx.isEmpty()) {
            throw new IllegalArgumentException("No fiscal data found for the provided projects");
        }

        // Build Lambda request model
        LambdaReportRequest lambdaRequest = new LambdaReportRequest();
        LambdaReportRequest.Report report = new LambdaReportRequest.Report();
        report.setReportType("XLSX");
        report.setReportName("project-report");
        LambdaReportRequest.XlsxReportData xlsxData = new LambdaReportRequest.XlsxReportData();
        xlsxData.setFuelManagementReportData(data.fuel);
        xlsxData.setCulturePrescribedFireReportData(data.crx);
        report.setXlsxReportData(xlsxData);
        lambdaRequest.setReports(java.util.List.of(report));

        // Call Lambda
        if (reportGeneratorLambdaUrl == null || reportGeneratorLambdaUrl.isBlank()) {
            throw new ServiceException("REPORT_GENERATOR_LAMBDA_URL environment variable is not set");
        }

        ObjectMapper mapper = new ObjectMapper();
        String requestJson = mapper.writeValueAsString(lambdaRequest);

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(reportGeneratorLambdaUrl))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestJson))
                .build();

        HttpResponse<String> response = client.send(httpRequest,
                HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new ServiceException("Lambda returned error: " + response.body());
        }

        // Parse Lambda response and write first file to outputStream
        String responseJson = response.body();
        LambdaReportResponse lambdaResponse;
        try {
            JsonNode root = mapper.readTree(responseJson);
            if (root.has("body")) {
                // API Gateway or Lambda Function URL wrapper
                String bodyJson = root.get("body").asText();
                lambdaResponse = mapper.readValue(bodyJson, LambdaReportResponse.class);
            } else {
                lambdaResponse = mapper.readValue(responseJson, LambdaReportResponse.class);
            }
        } catch (Exception e) {
            throw new ServiceException("Failed to parse Lambda response: " + e.getMessage(), e);
        }

        if (lambdaResponse.getFiles() == null || lambdaResponse.getFiles().isEmpty()) {
            throw new ServiceException("No files returned from Lambda");
        }
        LambdaReportResponse.File file = lambdaResponse.getFiles().get(0);
        byte[] xlsxBytes = java.util.Base64.getDecoder().decode(file.getContent());
        outputStream.write(xlsxBytes);
        outputStream.flush();
    }

    public void writeCsvZipFromEntities(ReportRequestModel request, OutputStream zipOutStream) throws ServiceException {
        ReportDataBundle data = resolveReportData(request);

        // Remove nulls up front
        data.fuel.removeIf(Objects::isNull);
        data.crx.removeIf(Objects::isNull);

        // Pre-process
        data.fuel.forEach(this::setFuelManagementFields);
        data.crx.forEach(this::setCrxFields);

        // If absolutely nothing to write, fail early
        if (data.fuel.isEmpty() && data.crx.isEmpty()) {
            throw new IllegalArgumentException("No fiscal data found for the provided projects");
        }

        try (ZipOutputStream zipOut = new ZipOutputStream(zipOutStream)) {

            // Only write Fuel CSV if there are rows
            if (!data.fuel.isEmpty()) {
                ByteArrayOutputStream fuelCsvOut = new ByteArrayOutputStream();
                try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fuelCsvOut))) {
                    writer.write(getFuelCsvHeader());
                    writer.newLine();
                    for (FuelManagementReportEntity e : data.fuel) {
                        writer.write(String.join(",", getFuelCsvRow(e)));
                        writer.newLine();
                    }
                }
                addToZip(zipOut, "fuel-management-projects.csv", fuelCsvOut.toByteArray());
            }

            // Only write Cultural Prescribed CSV if there are rows
            if (!data.crx.isEmpty()) {
                ByteArrayOutputStream crxCsvOut = new ByteArrayOutputStream();
                try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(crxCsvOut))) {
                    writer.write(getCrxCsvHeader());
                    writer.newLine();
                    for (CulturalPrescribedFireReportEntity c : data.crx) {
                        writer.write(String.join(",", getCrxCsvRow(c)));
                        writer.newLine();
                    }
                }
                addToZip(zipOut, "cultural-prescribed-fire-projects.csv", crxCsvOut.toByteArray());
            }
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("No fiscal data found for the provided projects.");
        } catch (IOException e) {
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
        if (entity != null) {
            if (entity.getProjectGuid() != null) {
                entity.setLinkToProject(urlPrefix + entity.getProjectGuid());
            }

            if (entity.getProjectPlanFiscalGuid() != null) {
                entity.setLinkToFiscalActivity(
                        urlPrefix + entity.getProjectGuid() + FISCAL_QUERY_STRING + entity.getProjectPlanFiscalGuid());
            }

            if (entity.getProgramAreaGuid() != null) {
                programAreaRepository.findById(entity.getProgramAreaGuid())
                        .ifPresent(programArea -> entity.setBusinessArea(programArea.getProgramAreaName()));
            }

            // 2025 -> 2025/26 format
            entity.setFiscalYear(formatFiscalYearIfNumeric(entity.getFiscalYear()));
        }
    }

    private void setCrxFields(CulturalPrescribedFireReportEntity entity) {
        String urlPrefix = baseUrl + PROJECT_URL_PREFIX;
        if (entity != null) {
            if(entity.getProjectGuid() != null) {
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
            entity.setFiscalYear(formatFiscalYearIfNumeric(entity.getFiscalYear()));
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
                "Project Type", 
                "Project Name", 
                "FOR Region", 
                "FOR District", 
                "BC Parks Region",
                "BC Parks Section", 
                "Fire Centre", 
                "Business Area", 
                "Planning Unit",
                "Gross Project Area (Ha calculated from spatial file)", 
                "Closest Community", 
                "Project Lead",
                "Proposal Type",
                "Fiscal Activity Name", 
                "Fiscal Activity Description", 
                "Fiscal Year", 
                "Activity Category",
                "Fiscal Status", 
                "Original Cost Estimate", 
                "Forecast Amount", 
                "Ancillary Funding Amount",
                "Ancillary Funding Provider", 
                "Final Reported Spend", 
                "CFS Actual Spend",
                "Planned Hectares", 
                "Completed Hectares", 
                "Spatial Submitted",
                "First Nation Engagement (Y/N)", 
                "First Nation Co-Delivery (Y/N)", 
                "First Nation Co-Delivery Partners",
                "Other Partners", 
                "CFS Code", 
                "RESULTS Project Code", 
                "RESULTS Opening ID",
                "Primary Objective", 
                "Secondary Objective (Optional)", 
                "Endorsement Date",
                "Approval Date", 
                "WUI Risk Class", 
                "Local WUI Risk Class", 
                "Local WUI Risk Class Rationale",
                "Total Point Value for Coarse Filter", 
                "Total Point Value for Medium Filters",
                "Additional Comments/Notes on Medium Filters",
                "Total Point Value of Fine Filters", 
                "Additional Comments/Notes on Fine Filters",
                "Total Filter Value"
            ));
    }

    private List<String> getFuelCsvRow(FuelManagementReportEntity e) {
        return List.of(
                safe(e.getProjectName() != null ? String.format("=HYPERLINK(\"%s\", \"%s Project Link\")",
                        e.getLinkToProject(), e.getProjectName()) : ""),
                safe(e.getProjectFiscalName() != null ? String.format("=HYPERLINK(\"%s\", \"%s Fiscal Activity Link\")",
                        e.getLinkToFiscalActivity(), e.getProjectFiscalName()) : ""),
                safe(e.getProjectTypeDescription()),
                safe(e.getProjectName()), 
                safe(e.getForestRegionOrgUnitName()), 
                safe(e.getForestDistrictOrgUnitName()),
                safe(e.getBcParksRegionOrgUnitName()), 
                safe(e.getBcParksSectionOrgUnitName()),
                safe(e.getFireCentreOrgUnitName()),
                safe(e.getBusinessArea()), 
                safe(e.getPlanningUnitName()), 
                safe(e.getGrossProjectAreaHa() != null
                        ? String.format(HECTARE_FORMAT, e.getGrossProjectAreaHa().intValue())
                        : ""),
                safe(e.getClosestCommunityName()),
                safe(e.getProjectLead()), 
                safe(e.getProposalTypeDescription()), 
                safe(e.getProjectFiscalName()),
                safe(e.getProjectFiscalDescription()), 
                safe(e.getFiscalYear()),
                safe(e.getActivityCategoryDescription()),
                safe(e.getPlanFiscalStatusDescription()), 
                safe(formatMonetaryFields(e.getTotalEstimatedCostAmount())),
                safe(formatMonetaryFields(e.getFiscalForecastAmount())),
                safe(formatMonetaryFields(e.getFiscalAncillaryFundAmount())),
                safe(e.getAncillaryFundingProvider()),
                safe(formatMonetaryFields(e.getFiscalReportedSpendAmount())),
                safe(formatMonetaryFields(e.getFiscalActualAmount())),
                safe(e.getFiscalPlannedProjectSizeHa() != null
                        ? String.format(HECTARE_FORMAT, e.getFiscalPlannedProjectSizeHa().intValue())
                        : ""),
                safe(e.getFiscalCompletedSizeHa() != null
                        ? String.format(HECTARE_FORMAT, e.getFiscalCompletedSizeHa().intValue())
                        : ""),
                safe(String.format("=\"%s\"", e.getSpatialSubmitted())),
                safe(e.getFirstNationsEngagement()), 
                safe(e.getFirstNationsDelivPartners()),
                safe(e.getFirstNationsPartner()),
                safe(e.getOtherPartner()), 
                safe(e.getCfsProjectCode()), 
                safe(e.getResultsProjectCode()),
                safe(e.getResultsOpeningId()),
                safe(e.getPrimaryObjectiveTypeDescription()), 
                safe(e.getSecondaryObjectiveTypeDescription()),
                safe(e.getEndorsementTimestamp() != null
                        ? DATE_FORMAT.format(e.getEndorsementTimestamp().toInstant())
                        : ""),
                safe(e.getApprovedTimestamp() != null
                        ? DATE_FORMAT.format(e.getApprovedTimestamp().toInstant())
                        : ""),
                safe(e.getWuiRiskClassDescription()), 
                safe(e.getLocalWuiRiskClassDescription()),
                safe(e.getLocalWuiRiskClassRationale()),
                safe(e.getTotalCoarseFilterSectionScore()), 
                safe(e.getTotalMediumFilterSectionScore()),
                safe(e.getMediumFilterSectionComment()),
                safe(e.getTotalFineFilterSectionScore()), 
                safe(e.getFineFilterSectionComment()),
                safe(e.getTotalFilterSectionScore()));
    }

    private String getCrxCsvHeader() {
        return String.join(",", List.of(
                "Link to Project (within Prevention application)",
                "Link to Fiscal Activity (within Prevention application)",
                "Project Type", 
                "Project Name", 
                "FOR Region", 
                "FOR District", 
                "BC Parks Region",
                "BC Parks Section", 
                "Fire Centre", 
                "Business Area", 
                "Planning Unit",
                "Gross Project Area (Ha calculated from spatial file)", 
                "Closest Community", 
                "Project Lead",
                "Proposal Type",
                "Fiscal Activity Name", 
                "Fiscal Activity Description", 
                "Fiscal Year", 
                "Activity Category",
                "Fiscal Status", 
                "Original Cost Estimate", 
                "Forecast Amount", 
                "Ancillary Funding Amount",
                "Ancillary Funding Provider",
                "Final Reported Spend", 
                "CFS Actual Spend",
                "Planned Hectares", 
                "Completed Hectares", 
                "Spatial Submitted",
                "First Nation Engagement (Y/N)", 
                "First Nation Co-Delivery (Y/N)", 
                "First Nation Co-Delivery Partners",
                "Other Partners", 
                "CFS Code", 
                "RESULTS Project Code", 
                "RESULTS Opening ID",
                "Primary Objective", 
                "Secondary Objective (Optional)", 
                "Endorsement Date",
                "Approval Date", 
                "Outside WUI (Y/N)", 
                "WUI Risk Class", 
                "Local WUI Risk Class",
                "Risk Class & Location Total Point Value",
                "Additional Comments/Notes on risk class or outside WUI rationale",
                "Burn Development and Feasibility Total Point Value",
                "Additional Comments/Notes on Burn Development and Feasibility",
                "Collective Impact Total Point Value", 
                "Additional Comments/Notes on Collective Impact",
                "Calculated Total"));
    }

    private List<String> getCrxCsvRow(CulturalPrescribedFireReportEntity c) {
        return List.of(
                safe(c.getProjectName() != null ? String.format("=HYPERLINK(\"%s\", \"%s Project Link\")",
                        c.getLinkToProject(), c.getProjectName()) : ""),
                safe(c.getProjectFiscalName() != null ? String.format("=HYPERLINK(\"%s\", \"%s Fiscal Activity Link\")",
                        c.getLinkToFiscalActivity(), c.getProjectFiscalName()) : ""),
                safe(c.getProjectTypeDescription()),
                safe(c.getProjectName()), 
                safe(c.getForestRegionOrgUnitName()), 
                safe(c.getForestDistrictOrgUnitName()),
                safe(c.getBcParksRegionOrgUnitName()), 
                safe(c.getBcParksSectionOrgUnitName()),
                safe(c.getFireCentreOrgUnitName()),
                safe(c.getBusinessArea()), 
                safe(c.getPlanningUnitName()), 
                safe(c.getGrossProjectAreaHa() != null
                        ? String.format(HECTARE_FORMAT, c.getGrossProjectAreaHa().intValue())
                        : ""),
                safe(c.getClosestCommunityName()),
                safe(c.getProjectLead()), 
                safe(c.getProposalTypeDescription()), 
                safe(c.getProjectFiscalName()),
                safe(c.getProjectFiscalDescription()), 
                safe(c.getFiscalYear()),
                safe(c.getActivityCategoryDescription()),
                safe(c.getPlanFiscalStatusDescription()), 
                safe(formatMonetaryFields(c.getTotalEstimatedCostAmount())),
                safe(formatMonetaryFields(c.getFiscalForecastAmount())), 
                safe(formatMonetaryFields(c.getFiscalAncillaryFundAmount())),
                safe(c.getAncillaryFundingProvider()),
                safe(formatMonetaryFields(c.getFiscalReportedSpendAmount())),
                safe(formatMonetaryFields(c.getFiscalActualAmount())),
                safe(c.getFiscalPlannedProjectSizeHa() != null
                        ? String.format(HECTARE_FORMAT, c.getFiscalPlannedProjectSizeHa().intValue())
                        : ""),
                safe(c.getFiscalCompletedSizeHa() != null
                        ? String.format(HECTARE_FORMAT, c.getFiscalCompletedSizeHa().intValue())
                        : ""),
                safe(String.format("=\"%s\"", c.getSpatialSubmitted())),
                safe(c.getFirstNationsEngagement()), 
                safe(c.getFirstNationsDelivPartners()),
                safe(c.getFirstNationsPartner()),
                safe(c.getOtherPartner()), 
                safe(c.getCfsProjectCode()), 
                safe(c.getResultsProjectCode()),
                safe(c.getResultsOpeningId()),
                safe(c.getPrimaryObjectiveTypeDescription()), 
                safe(c.getSecondaryObjectiveTypeDescription()),
                safe(c.getEndorsementTimestamp() != null
                        ? DATE_FORMAT.format(c.getEndorsementTimestamp().toInstant())
                        : ""),
                safe(c.getApprovedTimestamp() != null
                        ? DATE_FORMAT.format(c.getApprovedTimestamp().toInstant())
                        : ""),
                safe((c.getOutsideWuiInd() != null && c.getOutsideWuiInd()) ? "Y" : "N"),
                safe(c.getWuiRiskClassDescription()), 
                safe(c.getLocalWuiRiskClassDescription()),
                safe(c.getTotalRclFilterSectionScore()),
                safe(c.getRclFilterSectionComment()), 
                safe(c.getTotalBdfFilterSectionScore()),
                safe(c.getBdfFilterSectionComment()),
                safe(c.getTotalCollimpFilterSectionScore()), 
                safe(c.getCollimpFilterSectionComment()),
                safe(c.getTotalFilterSectionScore())
            );
    }

    private static String formatMonetaryFields(Number n) {
        if (n == null)
            return "";
        return new java.text.DecimalFormat("$#,##0").format(n);
    }

    private String formatFiscalYearIfNumeric(String fiscalYear) {
        if (fiscalYear == null)
            return "";
        try {
            int year = Integer.parseInt(fiscalYear);
            return year + "/" + String.format("%02d", (year + 1) % 100);
        } catch (NumberFormatException e) {
            return "";
        }
    }

    // POJO for Lambda response
    public static class LambdaReportResponse {
        private List<File> files;

        public List<File> getFiles() {
            return files;
        }

        public void setFiles(List<File> files) {
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
        private List<Report> reports;

        public List<Report> getReports() {
            return reports;
        }

        public void setReports(List<Report> reports) {
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
            private List<CulturalPrescribedFireReportEntity> culturePrescribedFireReportData;
            private List<FuelManagementReportEntity> fuelManagementReportData;

            public List<CulturalPrescribedFireReportEntity> getCulturePrescribedFireReportData() {
                return culturePrescribedFireReportData;
            }

            public void setCulturePrescribedFireReportData(List<CulturalPrescribedFireReportEntity> data) {
                this.culturePrescribedFireReportData = data;
            }

            public List<FuelManagementReportEntity> getFuelManagementReportData() {
                return fuelManagementReportData;
            }

            public void setFuelManagementReportData(List<FuelManagementReportEntity> data) {
                this.fuelManagementReportData = data;
            }
        }
    }

}
