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
import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.SimpleXlsxReportConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UncheckedIOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Slf4j
@Component
public class ReportService {

    @Value("${spring.application.baseUrl}")
    private String baseUrl;

    private static final String PROJECT_URL_PREFIX = "/edit-project?projectGuid=";

    private static final String FISCAL_QUERY_STRING = "&tab=fiscal&fiscalGuid=";

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
                    .flatMap(project -> project.getProjectFiscals().stream())
                    .map(ProjectFiscalEntity::getProjectPlanFiscalGuid)
                    .distinct()
                    .toList();

            // 2. Fetch both datasets
            List<FuelManagementReportEntity> fuelData = fuelManagementRepository.findByProjectPlanFiscalGuidIn(projectPlanFiscalGuids);
            List<CulturalPrescribedFireReportEntity> crxData = culturalPrescribedFireReportRepository.findByProjectPlanFiscalGuidIn(projectPlanFiscalGuids);

            if (fuelData.isEmpty() && crxData.isEmpty()) {
                throw new IllegalArgumentException("No data found for the provided projectGuids.");
            }

            for (FuelManagementReportEntity entities: fuelData) {
                setFuelManagementFields(entities);
            }

            for (CulturalPrescribedFireReportEntity entities : crxData) {
                setCrxFields(entities);
            }

            JasperReport fuelReport = JasperCompileManager.compileReport(
                    getClass().getResourceAsStream("/jasper-template/WFPREV_FUEL_MANAGEMENT_JASPER.jrxml")
            );
            JasperReport crxReport = JasperCompileManager.compileReport(
                    getClass().getResourceAsStream("/jasper-template/WFPREV_CULTURE_PRESCRIBED_FIRE_JASPER.jrxml")
            );

            Map<String, Object> params = new HashMap<>();
            JasperPrint fuelPrint = JasperFillManager.fillReport(fuelReport, params, new JRBeanCollectionDataSource(fuelData));
            JasperPrint crxPrint = JasperFillManager.fillReport(crxReport, params, new JRBeanCollectionDataSource(crxData));

            JRXlsxExporter exporter = new JRXlsxExporter();
            exporter.setExporterInput(SimpleExporterInput.getInstance(List.of(fuelPrint, crxPrint)));
            exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(outputStream));

            SimpleXlsxReportConfiguration config = new SimpleXlsxReportConfiguration();
            config.setRemoveEmptySpaceBetweenRows(false);
            config.setWhitePageBackground(false);
            config.setDetectCellType(true);
            config.setCollapseRowSpan(false);
            config.setSheetNames(new String[]{"FM XLS Download", "CRx XLS Download"});
            exporter.setConfiguration(config);

            exporter.exportReport();
            log.info("XLSX export completed successfully with two sheets.");

        } catch (Exception e) {
            log.error("Failed to generate XLSX report: {}", e.getMessage(), e);
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
                "Fiscal Status", "Funding Stream", "Original Cost Estimate",
                "Ancillary Funding Amount", "Final Reported Spend", "CFS Actual Spend",
                "Planned Hectares", "Completed Hectares", "Spatial Submitted",
                "First Nation Engagement (Y/N)", "First Nation Co-Delivery (Y/N)", "First Nation Co-Delivery Partners",
                "Other Partners", "CFS Code", "RESULTS Project Code", "RESULTS Opening ID",
                "Primary Objective", "Secondary  Objective (Optional)", "Endorsement Date",
                "Approval Date", "WUI Risk Class", "Local WUI Risk Class Rationale",
                "Total Point Value for Coarse Filter", "Total Point Value for Medium Filters", "Additional Comments/Notes on Medium Filters",
                "Total Point Value of Fine Filters", "Additional Comments/Notes on Fine Filters", "Total Filter Value"
        ));
    }

    private List<String> getFuelCsvRow(FuelManagementReportEntity e) {
        return List.of(
                safe(e.getLinkToProject()), safe(e.getLinkToFiscalActivity()), safe(e.getProjectTypeDescription()),
                safe(e.getProjectName()), safe(e.getForestRegionOrgUnitName()), safe(e.getForestDistrictOrgUnitName()),
                safe(e.getBcParksRegionOrgUnitName()), safe(e.getBcParksSectionOrgUnitName()), safe(e.getFireCentreOrgUnitName()),
                safe(e.getBusinessArea()), safe(e.getPlanningUnitName()), safe(e.getGrossProjectAreaHa()), safe(e.getClosestCommunityName()),
                safe(e.getProjectLead()), safe(e.getProposalTypeDescription()), safe(e.getProjectFiscalName()),
                safe(e.getProjectFiscalDescription()), safe(e.getFiscalYear()), safe(e.getActivityCategoryDescription()),
                safe(e.getPlanFiscalStatusDescription()), safe(e.getFundingStream()), safe(e.getTotalEstimatedCostAmount()),
                safe(e.getFiscalAncillaryFundAmount()), safe(e.getFiscalReportedSpendAmount()), safe(e.getFiscalActualAmount()),
                safe(e.getFiscalPlannedProjectSizeHa()), safe(e.getFiscalCompletedSizeHa()), safe(e.getSpatialSubmitted()),
                safe(e.getFirstNationsEngagement()), safe(e.getFirstNationsDelivPartners()), safe(e.getFirstNationsPartner()),
                safe(e.getOtherPartner()), safe(e.getCfsProjectCode()), safe(e.getResultsProjectCode()), safe(e.getResultsOpeningId()),
                safe(e.getPrimaryObjectiveTypeDescription()), safe(e.getSecondaryObjectiveTypeDescription()),
                safe(e.getEndorsementTimestamp()), safe(e.getApprovedTimestamp()), safe(e.getWuiRiskClassDescription()),
                safe(e.getLocalWuiRiskClassRationale()), safe(e.getLocalWuiRiskClassRationale()),
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
                "Fiscal Status", "Funding Stream", "Original Cost Estimate",
                "Ancillary Funding Amount", "Final Reported Spend", "CFS Actual Spend",
                "Planned Hectares", "Completed Hectares", "Spatial Submitted",
                "First Nation Engagement (Y/N)", "First Nation Co-Delivery (Y/N)", "First Nation Co-Delivery Partners",
                "Other Partners", "CFS Code", "RESULTS Project Code", "RESULTS Opening ID",
                "Primary Objective", "Secondary  Objective (Optional)", "Endorsement Date",
                "Approval Date", "Outside WUI (Y/N)", "WUI Risk Class", "Local WUI Risk Class",
                "Risk Class & Location Total Point Value", "Additional Comments/Notes on risk class or outside WUI rationale",
                "Burn Development and Feasibility Total Point Value", "Additional Comments/Notes on Burn Development and Feasibility",
                "Collective Impact Total Point Value", "Additional Comments/Notes on Collective Impact", "Calculated Total"
        ));
    }

    private List<String> getCrxCsvRow(CulturalPrescribedFireReportEntity c) {
        return List.of(
                safe(c.getLinkToProject()), safe(c.getLinkToFiscalActivity()), safe(c.getProjectTypeDescription()),
                safe(c.getProjectName()), safe(c.getForestRegionOrgUnitName()), safe(c.getForestDistrictOrgUnitName()),
                safe(c.getBcParksRegionOrgUnitName()), safe(c.getBcParksSectionOrgUnitName()), safe(c.getFireCentreOrgUnitName()),
                safe(c.getBusinessArea()), safe(c.getPlanningUnitName()), safe(c.getGrossProjectAreaHa()), safe(c.getClosestCommunityName()),
                safe(c.getProjectLead()), safe(c.getProposalTypeDescription()), safe(c.getProjectFiscalName()),
                safe(c.getProjectFiscalDescription()), safe(c.getFiscalYear()), safe(c.getActivityCategoryDescription()),
                safe(c.getPlanFiscalStatusDescription()), safe(c.getFundingStream()), safe(c.getTotalEstimatedCostAmount()),
                safe(c.getFiscalAncillaryFundAmount()), safe(c.getFiscalReportedSpendAmount()), safe(c.getFiscalActualAmount()),
                safe(c.getFiscalPlannedProjectSizeHa()), safe(c.getFiscalCompletedSizeHa()), safe(c.getSpatialSubmitted()),
                safe(c.getFirstNationsEngagement()), safe(c.getFirstNationsDelivPartners()), safe(c.getFirstNationsPartner()),
                safe(c.getOtherPartner()), safe(c.getCfsProjectCode()), safe(c.getResultsProjectCode()), safe(c.getResultsOpeningId()),
                safe(c.getPrimaryObjectiveTypeDescription()), safe(c.getSecondaryObjectiveTypeDescription()),
                safe(c.getEndorsementTimestamp()), safe(c.getApprovedTimestamp()), safe(c.getOutsideWuiInd()),
                safe(c.getWuiRiskClassDescription()), safe(c.getLocalWuiRiskClassDescription()), safe(c.getTotalRclFilterSectionScore()),
                safe(c.getRclFilterSectionComment()), safe(c.getTotalBdfFilterSectionScore()), safe(c.getBdfFilterSectionComment()),
                safe(c.getTotalCollimpFilterSectionScore()), safe(c.getCollimpFilterSectionComment()), safe(c.getTotalFilterSectionScore())
        );
    }

}
