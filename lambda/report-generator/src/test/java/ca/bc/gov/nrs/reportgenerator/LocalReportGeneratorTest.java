package ca.bc.gov.nrs.reportgenerator;

import ca.bc.gov.nrs.reportgenerator.model.ProjectCulturePrescribedFireReportData;
import ca.bc.gov.nrs.reportgenerator.model.ProjectFuelManagementReportData;
import ca.bc.gov.nrs.reportgenerator.model.ResultsCulturePrescribedFireReportData;
import ca.bc.gov.nrs.reportgenerator.model.ResultsFuelManagementReportData;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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

    /**
     * Writes target/generated-test-reports/ReMi_RESULTS_Mock_&lt;timestamp&gt;.xlsx with the FM and CRx
     * tabs, the way the lambda assembles the RESULTS export, then checks the structure that is easy
     * to break when editing the template.
     */
    @Test
    public void generateResultsReport() throws Exception {
        InputStream is = getClass().getClassLoader().getResourceAsStream("jasperreports/WFPREV_RESULTS_JASPER.jrxml");
        if (is == null) {
            is = new FileInputStream("src/main/jasperreports/WFPREV_RESULTS_JASPER.jrxml");
        }
        JasperReport report = JasperCompileManager.compileReport(is);

        List<JasperPrint> prints = List.of(
                JasperFillManager.fillReport(report, new HashMap<>(),
                        new JRBeanCollectionDataSource(generateMockResultsData(10, "FM"))),
                JasperFillManager.fillReport(report, new HashMap<>(),
                        new JRBeanCollectionDataSource(toCulturePrescribedFireData(generateMockResultsData(5, "CRx")))));

        Files.createDirectories(Paths.get(OUTPUT_DIR));
        String xlsxPath = OUTPUT_DIR + "/ReMi_RESULTS_Mock_" + System.currentTimeMillis() + ".xlsx";
        try (FileOutputStream os = new FileOutputStream(xlsxPath)) {
            JRXlsxExporter exporter = new JRXlsxExporter();
            exporter.setExporterInput(SimpleExporterInput.getInstance(prints));
            exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(os));

            SimpleXlsxReportConfiguration config = new SimpleXlsxReportConfiguration();
            config.setDetectCellType(true);
            config.setRemoveEmptySpaceBetweenRows(true);
            config.setRemoveEmptySpaceBetweenColumns(true);
            config.setCollapseRowSpan(true);
            config.setWhitePageBackground(false);
            config.setSheetNames(new String[]{"FM XLS Download", "CRx XLS Download"});
            exporter.setConfiguration(config);

            exporter.exportReport();
        }
        System.out.println("Generated XLSX: " + new File(xlsxPath).getAbsolutePath());

        try (java.util.zip.ZipFile xlsx = new java.util.zip.ZipFile(xlsxPath)) {
            String workbook = readEntry(xlsx, "xl/workbook.xml");
            assertTrue(workbook.contains("name=\"FM XLS Download\""), "FM tab missing");
            assertTrue(workbook.contains("name=\"CRx XLS Download\""), "CRx tab missing");
            assertEquals(2, workbook.split("<sheet ").length - 1, "Only the FM and CRx tabs are expected");

            for (String sheet : List.of("xl/worksheets/sheet1.xml", "xl/worksheets/sheet2.xml")) {
                String xml = readEntry(xlsx, sheet);
                assertTrue(xml.contains("ySplit=\"2\"") && xml.contains("state=\"frozen\""),
                        sheet + ": rows 1 and 2 should be frozen");
            }

            String strings = readEntry(xlsx, "xl/sharedStrings.xml");
            // Line breaks mirror where the source workbook wraps, since Calibri is narrower than BC Sans
            assertTrue(strings.contains("Link to Project (within\nReMi Planner)"), "Row 1 titles missing");
            assertTrue(strings.contains("The district in which the project\nprimarily sits."), "Row 2 explanations missing");
            assertFalse(strings.contains("Reference Fields"), "Developer reference rows must not be exported");
            assertFalse(strings.contains("&lt;style"), "Styled markup should become bold runs, not literal text");

            String styles = readEntry(xlsx, "xl/styles.xml");
            for (String colour : List.of("D9E1F2", "DBDBDB", "FCE4D6", "E2EFDA", "FFF2CC", "C6E0B4")) {
                assertTrue(styles.contains(colour), "Fill colour " + colour + " missing");
            }
            assertTrue(styles.contains("Calibri"), "Calibri font missing");
            assertFalse(styles.contains("Arial"), "Only Calibri should be used");
        }
    }

    private static String readEntry(java.util.zip.ZipFile zip, String name) throws Exception {
        java.util.zip.ZipEntry entry = zip.getEntry(name);
        assertNotNull(entry, name + " not found in workbook");
        try (InputStream in = zip.getInputStream(entry)) {
            return new String(in.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
        }
    }

    // Both RESULTS beans carry the same fields, so the CRx tab reuses the mock rows, converted the same
    // way the lambda deserializes its request.
    private List<ResultsCulturePrescribedFireReportData> toCulturePrescribedFireData(List<ResultsFuelManagementReportData> rows) {
        return new ObjectMapper().convertValue(rows, new TypeReference<List<ResultsCulturePrescribedFireReportData>>() {});
    }

    private List<ResultsFuelManagementReportData> generateMockResultsData(int count, String category) {
        List<ResultsFuelManagementReportData> list = new ArrayList<>();
        String[] statuses = {"In Progress", "Substantially Complete", "Complete", "Deferred", "Cancelled"};
        String[][] silviculture = {
                {"Juvenile Spacing", "Manual", "Power Saw"},
                {"Site Preparation", "Mechanical", "Piling"},
                {"Pruning", "Manual", null}};
        String[] districts = {"100 Mile House District", "Cariboo-Chilcotin District", "Selkirk District"};

        for (int i = 1; i <= count; i++) {
            ResultsFuelManagementReportData data = new ResultsFuelManagementReportData();
            String[] silv = silviculture[i % silviculture.length];
            data.setLinkToProject("http://link.to.project/" + i);
            data.setLinkToFiscalActivity("http://link.to.fiscal/" + i);
            data.setProjectName(category + " Project " + i);
            data.setProjectFiscalName("Sample " + category + " Fiscal Activity " + i);
            data.setActivityName(silv[0] + " - " + silv[1] + (silv[2] == null ? "" : " - " + silv[2]));
            // Row 3 leaves RESULTS Reportable blank to show there is no default "Y"
            data.setIsResultsReportableInd(i == 3 ? null : (i % 4 == 0 ? "N" : "Y"));
            data.setActivityDescription(i == 1
                    ? "This is a separate treatment unit that surrounds a Special Management Area. There was no harvesting in this unit.\nThis unit will be manually thinned, pruned and subsequently abated."
                    : "Activity description " + i);
            data.setActivityStatusName(statuses[i % statuses.length]);
            data.setFiscalYear("2026/27");
            data.setForestDistrictName(districts[i % districts.length]);
            data.setProjectLeadEmailAddress("lead" + i + "@gov.bc.ca");
            data.setResultsProjectCode("WRCA" + String.format("%04d", 70 + i));
            data.setResultsOpeningId(String.valueOf(1796400 + i));
            data.setActivityBaseName(silv[0]);
            data.setTechniqueName(silv[1]);
            data.setMethodName(silv[2]);
            data.setPrimaryObjectiveName("Wildfire Risk Reduction");
            data.setSecondaryObjectiveName(i % 2 == 0 ? "Ecosystem Restoration" : null);
            data.setFundingSourceCode("WRR");
            data.setContractPhaseName(i % 2 == 0 ? "Contract Awarded" : "In Planning");
            data.setCfsProjectCode("719C" + (280 + i));
            data.setPreviousCarryForwardInd(i % 3 == 0 ? "Y" : "N");
            data.setCarryForwardInd(i % 5 == 0 ? "Y" : "N");
            data.setFinalOutcomeComments(i % 2 == 0 ? "Outcome comments for activity " + i : null);
            data.setOutstandingObligationsInd(i % 2 == 0 ? "Y" : "N");
            data.setActivityComment(i % 2 == 0 ? "Plan to address obligations for activity " + i : null);
            data.setOpeningShapeFileName("Project" + i + "_Opening.kmz");
            data.setActivityShapeFileName("Project" + i + "_2026_TU" + i + "_treatment.kmz");
            if (i == 1) {
                data.setProjectBoundarySizeHa(new BigDecimal("261.413"));
                data.setPlannedTreatmentAreaHa(new BigDecimal("10"));
                data.setCompletedAreaHa(new BigDecimal("8.3"));
            } else if (i == 2) {
                data.setProjectBoundarySizeHa(new BigDecimal("1200.00"));
                data.setPlannedTreatmentAreaHa(new BigDecimal("61"));
                data.setCompletedAreaHa(null);
            } else {
                data.setProjectBoundarySizeHa(new BigDecimal(100 + i * 7 + ".5"));
                data.setPlannedTreatmentAreaHa(new BigDecimal(10 + i + ".25"));
                data.setCompletedAreaHa(new BigDecimal(i + ".1"));
            }
            data.setActivityEndDate(i % 3 == 0 ? null : new GregorianCalendar(2027, Calendar.FEBRUARY, i).getTime());
            list.add(data);
        }
        return list;
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

        String outputFileName = baseFileName + "_" + System.currentTimeMillis();
        
        // 3. Create Output Directory
        Files.createDirectories(Paths.get(OUTPUT_DIR));

        // 4. Export to PDF
        String pdfPath = OUTPUT_DIR + "/" + outputFileName + ".pdf";
        JasperExportManager.exportReportToPdfFile(jasperPrint, pdfPath);
        System.out.println("Generated PDF: " + new File(pdfPath).getAbsolutePath());

        // 5. Export to XLSX
        String xlsxPath = OUTPUT_DIR + "/" + outputFileName + ".xlsx";
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
            config.setSheetNames(new String[]{outputFileName});
            exporter.setConfiguration(config);

            exporter.exportReport();
        }
        System.out.println("Generated XLSX: " + new File(xlsxPath).getAbsolutePath());

        // Assert files exist
        assertTrue(Files.exists(Paths.get(pdfPath)));
        assertTrue(Files.exists(Paths.get(xlsxPath)));

        // Headers and data use Calibri 10, the same as the RESULTS export
        try (java.util.zip.ZipFile xlsx = new java.util.zip.ZipFile(xlsxPath)) {
            String styles = readEntry(xlsx, "xl/styles.xml");
            assertFalse(styles.contains("Arial"), "Only Calibri should be used");
            java.util.regex.Matcher sizes = java.util.regex.Pattern.compile("<sz val=\"([^\"]+)\"/>").matcher(styles);
            while (sizes.find()) {
                // "11" is the workbook's built-in default font; everything the report writes is "10.0"
                assertTrue(List.of("10.0", "11").contains(sizes.group(1)), "Unexpected font size " + sizes.group(1));
            }
        }
    }

    private List<ProjectFuelManagementReportData> generateMockFuelManagementData(int count) {
        List<ProjectFuelManagementReportData> list = new ArrayList<>();
        Random random = new Random();
        String[] regions = {"Cariboo", "Coastal", "Kamloops", "Kootenay", "Northwest", "Prince George", "Southeast", "Peace"};
        String[] statuses = {"INITIATED", "PLANNED", "ON_TRACK", "DELAYED", "COMPLETED", "CANCELLED"};
        String[] yesNo = {"Yes", "No"};
        String[] fundingStreams = {"Stream A", "Stream B", "Stream C"};

        for (int i = 1; i <= count; i++) {
            ProjectFuelManagementReportData data = new ProjectFuelManagementReportData();
            data.setProjectFiscalName("2025/26 - FM - Project " + i);
            data.setProjectFiscalDescription("Description for project " + i + ". Managing fuel loads in sector " + (char)('A' + (i % 26)));
            data.setFiscalYear("2025/2026");
            data.setProjectName("Project Alpha " + i);
            data.setForestRegionOrgUnitName(regions[random.nextInt(regions.length)] + " Fire Centre");
            data.setForestDistrictOrgUnitName("District " + (100 + i));
            data.setProjectLead("Lead " + i);
            // Precise Test Values for the first few rows
            if (i == 1) {
                data.setGrossProjectAreaHa(new BigDecimal("200"));
                data.setFiscalPlannedProjectSizeHa(new BigDecimal("0"));
                data.setFiscalCompletedSizeHa(new BigDecimal("1234.5678"));
            } else if (i == 2) {
                data.setGrossProjectAreaHa(new BigDecimal("200.5"));
                data.setFiscalPlannedProjectSizeHa(new BigDecimal("0.123"));
                data.setFiscalCompletedSizeHa(new BigDecimal("1000.00")); // Should be 1,000
            } else if (i == 3) {
                data.setGrossProjectAreaHa(null);
                data.setFiscalPlannedProjectSizeHa(null);
                data.setFiscalCompletedSizeHa(null);
            } else {
                data.setGrossProjectAreaHa(new BigDecimal(random.nextInt(500) + 10 + ".55"));
                data.setFiscalPlannedProjectSizeHa(new BigDecimal(random.nextInt(100) + 10 + ".55"));
                data.setFiscalCompletedSizeHa(new BigDecimal(random.nextInt(50) + ".55"));
            }
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

    private List<ProjectCulturePrescribedFireReportData> generateMockCulturePrescribedFireData(int count) {
        List<ProjectCulturePrescribedFireReportData> list = new ArrayList<>();
        Random random = new Random();
        String[] regions = {"Cariboo", "Coastal", "Kamloops", "Kootenay", "Northwest", "Prince George", "Southeast", "Peace"};
        String[] statuses = {"INITIATED", "PLANNED", "ON_TRACK", "DELAYED", "COMPLETED", "CANCELLED"};
        String[] yesNo = {"Yes", "No"};
        String[] fundingStreams = {"Stream A", "Stream B", "Stream C"};

        for (int i = 1; i <= count; i++) {
            ProjectCulturePrescribedFireReportData data = new ProjectCulturePrescribedFireReportData();
            data.setProjectFiscalName("2025/26 - CRX - Burn " + i);
            data.setProjectFiscalDescription("Cultural burning activity for site " + i);
            data.setFiscalYear("2025/2026");
            data.setProjectName("Cultural Fire Project " + i);
            data.setForestRegionOrgUnitName(regions[random.nextInt(regions.length)] + " Fire Centre");
            data.setForestDistrictOrgUnitName("District " + (200 + i));
            data.setProjectLead("Elder " + i);
            // Precise Test Values for the first few rows
            if (i == 1) {
                data.setGrossProjectAreaHa(new BigDecimal("200"));
                data.setFiscalPlannedProjectSizeHa(new BigDecimal("0"));
                data.setFiscalCompletedSizeHa(new BigDecimal("1234.5678"));
            } else if (i == 2) {
                data.setGrossProjectAreaHa(new BigDecimal("200.5"));
                data.setFiscalPlannedProjectSizeHa(new BigDecimal("0.123"));
                data.setFiscalCompletedSizeHa(new BigDecimal("1000.00"));
            } else if (i == 3) {
                data.setGrossProjectAreaHa(null);
                data.setFiscalPlannedProjectSizeHa(null);
                data.setFiscalCompletedSizeHa(null);
            } else {
                data.setGrossProjectAreaHa(new BigDecimal(random.nextInt(200) + 5 + ".55"));
                data.setFiscalPlannedProjectSizeHa(new BigDecimal(random.nextInt(100) + 10 + ".55"));
                data.setFiscalCompletedSizeHa(new BigDecimal(random.nextInt(50) + ".55"));
            }
            data.setSpatialSubmitted(yesNo[random.nextInt(yesNo.length)]);
            data.setFirstNationsEngagement("Engagement Level " + (i % 3));
            data.setFirstNationsDelivPartners("Partner Band " + (i % 10));
            data.setFirstNationsPartner("FN Partner " + i);
            data.setOtherPartner("Other Partner " + i);
            data.setCfsProjectCode("CFS-" + (1000 + i));
            data.setResultsOpeningId("OPEN-" + (200 + i));
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
