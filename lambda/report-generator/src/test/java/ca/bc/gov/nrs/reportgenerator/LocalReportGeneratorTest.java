package ca.bc.gov.nrs.reportgenerator;

import ca.bc.gov.nrs.reportgenerator.model.CulturePrescribedFireReportData;
import ca.bc.gov.nrs.reportgenerator.model.FuelManagementReportData;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.SimpleXlsxReportConfiguration;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class LocalReportGeneratorTest {

    private static final String OUTPUT_DIR = "target/generated-test-reports";

    @Test
    public void generateFuelManagementReport() throws Exception {
        generateReport(
                "jasperreports/WFPREV_FUEL_MANAGEMENT_JASPER.jrxml",
                "src/main/jasperreports/WFPREV_FUEL_MANAGEMENT_JASPER.jrxml",
                generateMockFuelManagementData(20), // Generate 20 rows
                "FuelManagementReport_Mock"
        );
    }

    @Test
    public void generateCulturePrescribedFireReport() throws Exception {
        generateReport(
                "jasperreports/WFPREV_CULTURE_PRESCRIBED_FIRE_JASPER.jrxml",
                "src/main/jasperreports/WFPREV_CULTURE_PRESCRIBED_FIRE_JASPER.jrxml",
                generateMockCulturePrescribedFireData(20), // Generate 20 rows
                "CulturePrescribedFireReport_Mock"
        );
    }

    private void generateReport(String resourcePath, String fsPath, List<?> data, String baseFileName) throws Exception {
        // 1. Load Report Template
        InputStream is = getClass().getClassLoader().getResourceAsStream(resourcePath);
        if (is == null) {
            System.out.println("Template not found in classpath (" + resourcePath + "), trying file system (" + fsPath + ")...");
            File file = new File(fsPath);
            if (file.exists()) {
                is = new FileInputStream(file);
            }
        }
        assertNotNull(is, "Report template not found: " + resourcePath);

        // 2. Compile and Fill Report
        JasperReport report = JasperCompileManager.compileReport(is);
        JRDataSource dataSource = new JRBeanCollectionDataSource(data);
        Map<String, Object> parameters = new HashMap<>();
        JasperPrint jasperPrint = JasperFillManager.fillReport(report, parameters, dataSource);

        // 3. Create Output Directory
        Files.createDirectories(Paths.get(OUTPUT_DIR));

        // 4. Export to PDF
        String pdfPath = OUTPUT_DIR + "/" + baseFileName + ".pdf";
        JasperExportManager.exportReportToPdfFile(jasperPrint, pdfPath);
        System.out.println("Generated PDF: " + new File(pdfPath).getAbsolutePath());

        // 5. Export to XLSX
        String xlsxPath = OUTPUT_DIR + "/" + baseFileName + ".xlsx";
        try (FileOutputStream os = new FileOutputStream(xlsxPath)) {
            JRXlsxExporter exporter = new JRXlsxExporter();
            exporter.setExporterInput(SimpleExporterInput.getInstance(Collections.singletonList(jasperPrint)));
            exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(os));

            SimpleXlsxReportConfiguration config = new SimpleXlsxReportConfiguration();
            config.setDetectCellType(true);
            config.setRemoveEmptySpaceBetweenRows(true);
            config.setRemoveEmptySpaceBetweenColumns(true);
            config.setCollapseRowSpan(true);
            config.setWhitePageBackground(false);
            config.setSheetNames(new String[]{baseFileName});
            exporter.setConfiguration(config);

            exporter.exportReport();
        }
        System.out.println("Generated XLSX: " + new File(xlsxPath).getAbsolutePath());

        // Assert files exist
        assertTrue(Files.exists(Paths.get(pdfPath)));
        assertTrue(Files.exists(Paths.get(xlsxPath)));
    }

    private List<FuelManagementReportData> generateMockFuelManagementData(int count) {
        List<FuelManagementReportData> list = new ArrayList<>();
        Random random = new Random();
        String[] regions = {"Cariboo", "Coastal", "Kamloops", "Kootenay", "Northwest", "Prince George", "Southeast", "Peace"};
        String[] statuses = {"INITIATED", "PLANNED", "ON_TRACK", "DELAYED", "COMPLETED", "CANCELLED"};
        String[] yesNo = {"Yes", "No"};
        String[] fundingStreams = {"Stream A", "Stream B", "Stream C"};

        for (int i = 1; i <= count; i++) {
            FuelManagementReportData data = new FuelManagementReportData();
            data.setProjectFiscalName("2025/26 - FM - Project " + i);
            data.setProjectFiscalDescription("Description for project " + i + ". Managing fuel loads in sector " + (char)('A' + (i % 26)));
            data.setFiscalYear("2025/2026");
            data.setProjectName("Project Alpha " + i);
            data.setForestRegionOrgUnitName(regions[random.nextInt(regions.length)] + " Fire Centre");
            data.setForestDistrictOrgUnitName("District " + (100 + i));
            data.setProjectLead("Lead " + i);
            data.setGrossProjectAreaHa(new BigDecimal(random.nextInt(500) + 10 + ".55"));
            data.setTotalEstimatedCostAmount(new BigDecimal(10000 + random.nextInt(90000) + ".55"));
            data.setFiscalForecastAmount(new BigDecimal(10000 + random.nextInt(40000) + ".55"));
            data.setFiscalActualAmount(new BigDecimal(5000 + random.nextInt(10000) + ".55"));
            
            // New Fields
            data.setLinkToFiscalActivity("http://link.to.activity/" + i);
            data.setActivityCategoryDescription("Fuel Management Category " + (i % 5));
            data.setPlanFiscalStatusDescription(statuses[random.nextInt(statuses.length)]);
            data.setAncillaryFundingProvider("Provider " + (char)('A' + random.nextInt(5)));
            data.setFiscalAncillaryFundAmount(new BigDecimal(random.nextInt(5000) + ".55"));
            data.setFiscalReportedSpendAmount(new BigDecimal(random.nextInt(8000) + ".55"));
            data.setFiscalPlannedProjectSizeHa(new BigDecimal(random.nextInt(100) + 10 + ".55"));
            data.setFiscalCompletedSizeHa(new BigDecimal(random.nextInt(50) + ".55"));
            data.setSpatialSubmitted(yesNo[random.nextInt(yesNo.length)]);
            data.setFirstNationsEngagement("Engagement Level " + (i % 3));
            data.setFirstNationsDelivPartners("Partner Band " + (i % 10));
            data.setFirstNationsPartner("FN Partner " + i);
            data.setOtherPartner("Other Partner " + i);
            data.setCfsProjectCode("CFS-" + (1000 + i));
            data.setResultsOpeningId("OPEN-" + (500 + i));
            data.setEndorsementTimestamp(new Date());
            data.setApprovedTimestamp(new Date());
            data.setTotalFilterSectionScore(new BigDecimal(random.nextInt(100)));
            data.setProjectTypeDescription("Type " + (char)('A' + (i % 3)));
            data.setLinkToProject("http://link.to.project/" + i);
            data.setBcParksRegionOrgUnitName("Parks Region " + (i % 4));
            data.setBcParksSectionOrgUnitName("Parks Section " + (i % 6));
            data.setFireCentreOrgUnitName("Fire Centre " + (i % 3));
            data.setPlanningUnitName("Planning Unit " + (i % 10));
            data.setClosestCommunityName("Community " + (i % 20));
            data.setProposalTypeDescription("Proposal Type " + (i % 3));
            data.setResultsProjectCode("RES-" + (i * 10));
            data.setPrimaryObjectiveTypeDescription("Primary Obj " + (i % 4));
            data.setSecondaryObjectiveTypeDescription("Secondary Obj " + (i % 4));
            data.setWuiRiskClassDescription("Risk Class " + (i % 5));
            data.setLocalWuiRiskClassDescription("Local Risk " + (i % 5));
            data.setBusinessArea("Business Area " + (i % 3));
            data.setLocalWuiRiskClassRationale("Rationale for risk assignment " + i);
            data.setTotalCoarseFilterSectionScore(new BigDecimal(random.nextInt(50)));
            data.setTotalMediumFilterSectionScore(new BigDecimal(random.nextInt(50)));
            data.setMediumFilterSectionComment("Medium filter comments for " + i);
            data.setTotalFineFilterSectionScore(new BigDecimal(random.nextInt(50)));
            data.setFineFilterSectionComment("Fine filter comments for " + i);
            data.setProjectGuid(UUID.randomUUID());
            data.setProgramAreaGuid(UUID.randomUUID());
            data.setProjectPlanFiscalGuid(UUID.randomUUID());
            data.setFundingStream(fundingStreams[random.nextInt(fundingStreams.length)]);

            // Quarterly Updates - Q1
            data.setQ1SubmittedTimestamp(new Date());
            data.setQ1ProgressStatusCode(statuses[random.nextInt(statuses.length)]);
            data.setQ1GeneralUpdateComment("Q1 Update for item " + i);
            data.setQ1ForecastAmount(new BigDecimal(random.nextInt(10000) + ".55"));
            // data.setQ1ForecastAdjustmentAmount(new BigDecimal(random.nextInt(1000)));
            data.setQ1ForecastAdjustmentRationale("Adjustment rationale Q1");
            data.setQ1BudgetHighRiskAmount(new BigDecimal(random.nextInt(5000) + ".55"));
            data.setQ1BudgetHighRiskRationale("High risk rationale Q1");
            data.setQ1BudgetMediumRiskAmount(new BigDecimal(random.nextInt(5000) + ".55"));
            data.setQ1BudgetMediumRiskRationale("Medium risk rationale Q1");
            data.setQ1BudgetLowRiskAmount(new BigDecimal(random.nextInt(5000) + ".55"));
            data.setQ1BudgetLowRiskRationale("Low risk rationale Q1");
            data.setQ1BudgetCompletedAmount(new BigDecimal(random.nextInt(2000) + ".55"));
            data.setQ1BudgetCompletedDescription("Completion desc Q1");

            // Q2
            data.setQ2SubmittedTimestamp(new Date());
            data.setQ2ProgressStatusCode(statuses[random.nextInt(statuses.length)]);
            data.setQ2GeneralUpdateComment("Q2 Update: " + (random.nextBoolean() ? "Proceeding well" : "Minor delays"));
            data.setQ2ForecastAmount(new BigDecimal(random.nextInt(20000) + ".55"));
            // data.setQ2ForecastAdjustmentAmount(new BigDecimal(random.nextInt(1000)));
            data.setQ2ForecastAdjustmentRationale("Adjustment rationale Q2");
            data.setQ2BudgetHighRiskAmount(new BigDecimal(random.nextInt(5000) + ".55"));
            data.setQ2BudgetHighRiskRationale("High risk rationale Q2");
            data.setQ2BudgetMediumRiskAmount(new BigDecimal(random.nextInt(5000) + ".55"));
            data.setQ2BudgetMediumRiskRationale("Medium risk rationale Q2");
            data.setQ2BudgetLowRiskAmount(new BigDecimal(random.nextInt(5000) + ".55"));
            data.setQ2BudgetLowRiskRationale("Low risk rationale Q2");
            data.setQ2BudgetCompletedAmount(new BigDecimal(random.nextInt(2000) + ".55"));
            data.setQ2BudgetCompletedDescription("Completion desc Q2");

            // Q3
            data.setQ3SubmittedTimestamp(new Date());
            data.setQ3ProgressStatusCode(statuses[random.nextInt(statuses.length)]);
            data.setQ3GeneralUpdateComment("Q3 Status check.");
            data.setQ3ForecastAmount(new BigDecimal(random.nextInt(30000) + ".55"));
            // data.setQ3ForecastAdjustmentAmount(new BigDecimal(random.nextInt(1000)));
            data.setQ3ForecastAdjustmentRationale("Adjustment rationale Q3");
            data.setQ3BudgetHighRiskAmount(new BigDecimal(random.nextInt(5000) + ".55"));
            data.setQ3BudgetHighRiskRationale("High risk rationale Q3");
            data.setQ3BudgetMediumRiskAmount(new BigDecimal(random.nextInt(5000) + ".55"));
            data.setQ3BudgetMediumRiskRationale("Medium risk rationale Q3");
            data.setQ3BudgetLowRiskAmount(new BigDecimal(random.nextInt(5000) + ".55"));
            data.setQ3BudgetLowRiskRationale("Low risk rationale Q3");
            data.setQ3BudgetCompletedAmount(new BigDecimal(random.nextInt(2000) + ".55"));
            data.setQ3BudgetCompletedDescription("Completion desc Q3");

            // March 7
            data.setMarch7SubmittedTimestamp(new Date());
            data.setMarch7ProgressStatusCode(statuses[random.nextInt(statuses.length)]);
            data.setMarch7GeneralUpdateComment("March 7th Snapshot.");
            data.setMarch7ForecastAmount(new BigDecimal(random.nextInt(35000) + ".55"));
            // data.setMarch7ForecastAdjustmentAmount(new BigDecimal(random.nextInt(1000)));
            data.setMarch7ForecastAdjustmentRationale("Adjustment rationale Mar7");
            data.setMarch7BudgetHighRiskAmount(new BigDecimal(random.nextInt(5000) + ".55"));
            data.setMarch7BudgetHighRiskRationale("High risk rationale Mar7");
            data.setMarch7BudgetMediumRiskAmount(new BigDecimal(random.nextInt(5000) + ".55"));
            data.setMarch7BudgetMediumRiskRationale("Medium risk rationale Mar7");
            data.setMarch7BudgetLowRiskAmount(new BigDecimal(random.nextInt(5000) + ".55"));
            data.setMarch7BudgetLowRiskRationale("Low risk rationale Mar7");
            data.setMarch7BudgetCompletedAmount(new BigDecimal(random.nextInt(2000) + ".55"));
            data.setMarch7BudgetCompletedDescription("Completion desc Mar7");
            
            // Other
            data.setOtherSubmittedTimestamp(new Date());
            data.setOtherProgressStatusCode(statuses[random.nextInt(statuses.length)]);
            data.setOtherGeneralUpdateComment("Other Snapshot.");
            data.setOtherForecastAmount(new BigDecimal(random.nextInt(35000) + ".55"));
            // data.setOtherForecastAdjustmentAmount(new BigDecimal(random.nextInt(1000)));
            data.setOtherForecastAdjustmentRationale("Adjustment rationale Other");
            data.setOtherBudgetHighRiskAmount(new BigDecimal(random.nextInt(5000) + ".55"));
            data.setOtherBudgetHighRiskRationale("High risk rationale Other");
            data.setOtherBudgetMediumRiskAmount(new BigDecimal(random.nextInt(5000) + ".55"));
            data.setOtherBudgetMediumRiskRationale("Medium risk rationale Other");
            data.setOtherBudgetLowRiskAmount(new BigDecimal(random.nextInt(5000) + ".55"));
            data.setOtherBudgetLowRiskRationale("Low risk rationale Other");
            data.setOtherBudgetCompletedAmount(new BigDecimal(random.nextInt(2000) + ".55"));
            data.setOtherBudgetCompletedDescription("Completion desc Other");

            list.add(data);
        }
        return list;
    }

    private List<CulturePrescribedFireReportData> generateMockCulturePrescribedFireData(int count) {
        List<CulturePrescribedFireReportData> list = new ArrayList<>();
        Random random = new Random();
        String[] regions = {"Cariboo", "Coastal", "Kamloops", "Kootenay", "Northwest", "Prince George", "Southeast", "Peace"};
        String[] statuses = {"INITIATED", "PLANNED", "ON_TRACK", "DELAYED", "COMPLETED", "CANCELLED"};
        String[] yesNo = {"Yes", "No"};
        String[] fundingStreams = {"Stream A", "Stream B", "Stream C"};

        for (int i = 1; i <= count; i++) {
            CulturePrescribedFireReportData data = new CulturePrescribedFireReportData();
            data.setProjectFiscalName("2025/26 - CRX - Burn " + i);
            data.setProjectFiscalDescription("Cultural burning activity for site " + i);
            data.setFiscalYear("2025/2026");
            data.setProjectName("Cultural Fire Project " + i);
            data.setForestRegionOrgUnitName(regions[random.nextInt(regions.length)] + " Fire Centre");
            data.setForestDistrictOrgUnitName("District " + (200 + i));
            data.setProjectLead("Elder " + i);
            data.setGrossProjectAreaHa(new BigDecimal(random.nextInt(200) + 5 + ".55"));
            data.setTotalEstimatedCostAmount(new BigDecimal(5000 + random.nextInt(50000) + ".55"));
            data.setFiscalForecastAmount(new BigDecimal(4000 + random.nextInt(40000) + ".55"));
            data.setFiscalActualAmount(new BigDecimal(1000 + random.nextInt(10000) + ".55"));
            
            // New Fields
            data.setLinkToFiscalActivity("http://link.to.activity/" + i);
            data.setActivityCategoryDescription("Cultural Burning Category " + (i % 5));
            data.setPlanFiscalStatusDescription(statuses[random.nextInt(statuses.length)]);
            data.setAncillaryFundingProvider("Provider " + (char)('A' + random.nextInt(5)));
            data.setFiscalAncillaryFundAmount(new BigDecimal(random.nextInt(5000) + ".55"));
            data.setFiscalReportedSpendAmount(new BigDecimal(random.nextInt(8000) + ".55"));
            data.setFiscalPlannedProjectSizeHa(new BigDecimal(random.nextInt(100) + 10 + ".55"));
            data.setFiscalCompletedSizeHa(new BigDecimal(random.nextInt(50) + ".55"));
            data.setSpatialSubmitted(yesNo[random.nextInt(yesNo.length)]);
            data.setFirstNationsEngagement("Engagement Level " + (i % 3));
            data.setFirstNationsDelivPartners("Partner Band " + (i % 10));
            data.setFirstNationsPartner("FN Partner " + i);
            data.setOtherPartner("Other Partner " + i);
            data.setCfsProjectCode("CFS-" + (1000 + i));
            data.setResultsOpeningId("OPEN-" + (500 + i));
            data.setEndorsementTimestamp(new Date());
            data.setApprovedTimestamp(new Date());
            data.setOutsideWuiInd(random.nextBoolean());
            data.setProjectTypeDescription("Type " + (char)('A' + (i % 3)));
            data.setLinkToProject("http://link.to.project/" + i);
            data.setBcParksRegionOrgUnitName("Parks Region " + (i % 4));
            data.setBcParksSectionOrgUnitName("Parks Section " + (i % 6));
            data.setFireCentreOrgUnitName("Fire Centre " + (i % 3));
            data.setPlanningUnitName("Planning Unit " + (i % 10));
            data.setClosestCommunityName("Community " + (i % 20));
            data.setProposalTypeDescription("Proposal Type " + (i % 3));
            data.setResultsProjectCode("RES-" + (i * 10));
            data.setPrimaryObjectiveTypeDescription("Primary Obj " + (i % 4));
            data.setSecondaryObjectiveTypeDescription("Secondary Obj " + (i % 4));
            data.setWuiRiskClassDescription("Risk Class " + (i % 5));
            data.setLocalWuiRiskClassDescription("Local Risk " + (i % 5));
            data.setBusinessArea("Business Area " + (i % 3));
            data.setProjectGuid(UUID.randomUUID());
            data.setProgramAreaGuid(UUID.randomUUID());
            data.setProjectPlanFiscalGuid(UUID.randomUUID());
            data.setFundingStream(fundingStreams[random.nextInt(fundingStreams.length)]);

            // Filter Scores
            data.setTotalFilterSectionScore(new BigDecimal(random.nextInt(100)));
            data.setTotalRclFilterSectionScore(new BigDecimal(random.nextInt(50)));
            data.setRclFilterSectionComment("RCL filter comments for " + i);
            data.setTotalBdfFilterSectionScore(new BigDecimal(random.nextInt(50)));
            data.setBdfFilterSectionComment("BDF filter comments for " + i);
            data.setTotalCollimpFilterSectionScore(new BigDecimal(random.nextInt(50)));
            data.setCollimpFilterSectionComment("Coll Imp filter comments for " + i);

            // Quarterly Updates - Q1
            data.setQ1SubmittedTimestamp(new Date());
            data.setQ1ProgressStatusCode(statuses[random.nextInt(statuses.length)]);
            data.setQ1GeneralUpdateComment("Q1 Preparation phase " + i);
            data.setQ1ForecastAmount(new BigDecimal(random.nextInt(5000) + ".55"));
            data.setQ1ForecastAdjustmentAmount(new BigDecimal(random.nextInt(1000) + ".55"));
            data.setQ1ForecastAdjustmentRationale("Adjustment rationale Q1");
            data.setQ1BudgetHighRiskAmount(new BigDecimal(random.nextInt(5000) + ".55"));
            data.setQ1BudgetHighRiskRationale("High risk rationale Q1");
            data.setQ1BudgetMediumRiskAmount(new BigDecimal(random.nextInt(5000) + ".55"));
            data.setQ1BudgetMediumRiskRationale("Medium risk rationale Q1");
            data.setQ1BudgetLowRiskAmount(new BigDecimal(random.nextInt(5000) + ".55"));
            data.setQ1BudgetLowRiskRationale("Low risk rationale Q1");
            data.setQ1BudgetCompletedAmount(new BigDecimal(random.nextInt(2000) + ".55"));
            data.setQ1BudgetCompletedDescription("Completion desc Q1");
            
            // Q2
            data.setQ2SubmittedTimestamp(new Date());
            data.setQ2ProgressStatusCode(statuses[random.nextInt(statuses.length)]);
            data.setQ2GeneralUpdateComment("Q2 Engagement ongoing");
            data.setQ2ForecastAmount(new BigDecimal(random.nextInt(10000) + ".55"));
            data.setQ2ForecastAdjustmentAmount(new BigDecimal(random.nextInt(1000) + ".55"));
            data.setQ2ForecastAdjustmentRationale("Adjustment rationale Q2");
            data.setQ2BudgetHighRiskAmount(new BigDecimal(random.nextInt(5000) + ".55"));
            data.setQ2BudgetHighRiskRationale("High risk rationale Q2");
            data.setQ2BudgetMediumRiskAmount(new BigDecimal(random.nextInt(5000) + ".55"));
            data.setQ2BudgetMediumRiskRationale("Medium risk rationale Q2");
            data.setQ2BudgetLowRiskAmount(new BigDecimal(random.nextInt(5000) + ".55"));
            data.setQ2BudgetLowRiskRationale("Low risk rationale Q2");
            data.setQ2BudgetCompletedAmount(new BigDecimal(random.nextInt(2000) + ".55"));
            data.setQ2BudgetCompletedDescription("Completion desc Q2");

            // Q3
            data.setQ3SubmittedTimestamp(new Date());
            data.setQ3ProgressStatusCode(statuses[random.nextInt(statuses.length)]);
            data.setQ3GeneralUpdateComment("Q3 Burn window assessment");
            data.setQ3ForecastAmount(new BigDecimal(random.nextInt(15000) + ".55"));
            data.setQ3ForecastAdjustmentAmount(new BigDecimal(random.nextInt(1000) + ".55"));
            data.setQ3ForecastAdjustmentRationale("Adjustment rationale Q3");
            data.setQ3BudgetHighRiskAmount(new BigDecimal(random.nextInt(5000) + ".55"));
            data.setQ3BudgetHighRiskRationale("High risk rationale Q3");
            data.setQ3BudgetMediumRiskAmount(new BigDecimal(random.nextInt(5000) + ".55"));
            data.setQ3BudgetMediumRiskRationale("Medium risk rationale Q3");
            data.setQ3BudgetLowRiskAmount(new BigDecimal(random.nextInt(5000) + ".55"));
            data.setQ3BudgetLowRiskRationale("Low risk rationale Q3");
            data.setQ3BudgetCompletedAmount(new BigDecimal(random.nextInt(2000) + ".55"));
            data.setQ3BudgetCompletedDescription("Completion desc Q3");

            // March 7
            data.setMarch7SubmittedTimestamp(new Date());
            data.setMarch7ProgressStatusCode(statuses[random.nextInt(statuses.length)]);
            data.setMarch7GeneralUpdateComment("March 7 Update");
            data.setMarch7ForecastAmount(new BigDecimal(random.nextInt(35000) + ".55"));
            data.setMarch7ForecastAdjustmentAmount(new BigDecimal(random.nextInt(1000) + ".55"));
            data.setMarch7ForecastAdjustmentRationale("Adjustment rationale Mar7");
            data.setMarch7BudgetHighRiskAmount(new BigDecimal(random.nextInt(5000) + ".55"));
            data.setMarch7BudgetHighRiskRationale("High risk rationale Mar7");
            data.setMarch7BudgetMediumRiskAmount(new BigDecimal(random.nextInt(5000) + ".55"));
            data.setMarch7BudgetMediumRiskRationale("Medium risk rationale Mar7");
            data.setMarch7BudgetLowRiskAmount(new BigDecimal(random.nextInt(5000) + ".55"));
            data.setMarch7BudgetLowRiskRationale("Low risk rationale Mar7");
            data.setMarch7BudgetCompletedAmount(new BigDecimal(random.nextInt(2000) + ".55"));
            data.setMarch7BudgetCompletedDescription("Completion desc Mar7");

            // Other
            data.setOtherSubmittedTimestamp(new Date());
            data.setOtherProgressStatusCode(statuses[random.nextInt(statuses.length)]);
            data.setOtherGeneralUpdateComment("Other Snapshot.");
            data.setOtherForecastAmount(new BigDecimal(random.nextInt(35000) + ".55"));
            data.setOtherForecastAdjustmentAmount(new BigDecimal(random.nextInt(1000) + ".55"));
            data.setOtherForecastAdjustmentRationale("Adjustment rationale Other");
            data.setOtherBudgetHighRiskAmount(new BigDecimal(random.nextInt(5000) + ".55"));
            data.setOtherBudgetHighRiskRationale("High risk rationale Other");
            data.setOtherBudgetMediumRiskAmount(new BigDecimal(random.nextInt(5000) + ".55"));
            data.setOtherBudgetMediumRiskRationale("Medium risk rationale Other");
            data.setOtherBudgetLowRiskAmount(new BigDecimal(random.nextInt(5000) + ".55"));
            data.setOtherBudgetLowRiskRationale("Low risk rationale Other");
            data.setOtherBudgetCompletedAmount(new BigDecimal(random.nextInt(2000) + ".55"));
            data.setOtherBudgetCompletedDescription("Completion desc Other");

            list.add(data);
        }
        return list;
    }
}
