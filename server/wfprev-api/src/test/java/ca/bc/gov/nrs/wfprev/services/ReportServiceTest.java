package ca.bc.gov.nrs.wfprev.services;

import ca.bc.gov.nrs.wfprev.data.entities.CulturalPrescribedFireReportEntity;
import ca.bc.gov.nrs.wfprev.data.entities.FuelManagementReportEntity;
import ca.bc.gov.nrs.wfprev.data.entities.ProgramAreaEntity;
import ca.bc.gov.nrs.wfprev.data.entities.ProjectEntity;
import ca.bc.gov.nrs.wfprev.data.entities.ProjectFiscalEntity;
import ca.bc.gov.nrs.wfprev.data.repositories.CulturalPrescribedFireReportRepository;
import ca.bc.gov.nrs.wfprev.data.repositories.FuelManagementReportRepository;
import ca.bc.gov.nrs.wfprev.data.repositories.ProgramAreaRepository;
import ca.bc.gov.nrs.wfprev.data.repositories.ProjectRepository;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.export.ExporterInput;
import net.sf.jasperreports.export.OutputStreamExporterOutput;
import net.sf.jasperreports.export.XlsxReportConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ReportServiceTest {

    private ReportService reportService;

    private FuelManagementReportRepository fuelRepo;
    private CulturalPrescribedFireReportRepository crxRepo;
    private ProjectRepository projectRepo;
    private ProgramAreaRepository programAreaRepo;

    @BeforeEach
    void setup() {
        fuelRepo = mock(FuelManagementReportRepository.class);
        crxRepo = mock(CulturalPrescribedFireReportRepository.class);
        projectRepo = mock(ProjectRepository.class);
        programAreaRepo = mock(ProgramAreaRepository.class);

        reportService = new ReportService(fuelRepo, crxRepo, projectRepo, programAreaRepo);
        ReflectionTestUtils.setField(reportService, "baseUrl", "http://localhost");
    }

    @Test
    void testExportXlsx_MockedJasper() throws Exception {
        UUID projectGuid = UUID.randomUUID();
        UUID fiscalGuid = UUID.randomUUID();

        ProjectEntity project = new ProjectEntity();
        ProjectFiscalEntity fiscal = new ProjectFiscalEntity();
        fiscal.setProjectPlanFiscalGuid(fiscalGuid);
        project.setProjectFiscals(List.of(fiscal));
        project.setProjectGuid(projectGuid);

        FuelManagementReportEntity fuel = new FuelManagementReportEntity();
        fuel.setProjectGuid(projectGuid);
        fuel.setProjectPlanFiscalGuid(fiscalGuid);
        fuel.setProgramAreaGuid(UUID.randomUUID());
        fuel.setFiscalYear("2024");

        CulturalPrescribedFireReportEntity crx = new CulturalPrescribedFireReportEntity();
        crx.setProjectGuid(projectGuid);
        crx.setProjectPlanFiscalGuid(fiscalGuid);
        crx.setProgramAreaGuid(UUID.randomUUID());
        crx.setFiscalYear("2025");

        ProgramAreaEntity pa = new ProgramAreaEntity();
        pa.setProgramAreaName("Mock Program Area");

        when(projectRepo.findByProjectGuidIn(List.of(projectGuid))).thenReturn(List.of(project));
        when(fuelRepo.findByProjectPlanFiscalGuidIn(List.of(fiscalGuid))).thenReturn(List.of(fuel));
        when(crxRepo.findByProjectPlanFiscalGuidIn(List.of(fiscalGuid))).thenReturn(List.of(crx));
        when(programAreaRepo.findById(any())).thenReturn(Optional.of(pa));

        JasperReport mockJasperReport = mock(JasperReport.class);
        JasperPrint mockJasperPrint = mock(JasperPrint.class);

        try (
                MockedStatic<JasperCompileManager> compileManager = mockStatic(JasperCompileManager.class);
                MockedStatic<JasperFillManager> fillManager = mockStatic(JasperFillManager.class);
                MockedConstruction<JRXlsxExporter> exporterConstruction = mockConstruction(JRXlsxExporter.class,
                        (exporter, context) -> {
                            doNothing().when(exporter).exportReport();
                        });
        ) {
            compileManager.when(() ->
                    JasperCompileManager.compileReport(any(InputStream.class))
            ).thenReturn(mockJasperReport);

            fillManager.when(() ->
                    JasperFillManager.fillReport(any(JasperReport.class), anyMap(), any(JRDataSource.class))
            ).thenReturn(mockJasperPrint);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            reportService.exportXlsx(List.of(projectGuid), outputStream);

            JRXlsxExporter createdExporter = exporterConstruction.constructed().get(0);
            verify(createdExporter).setExporterInput(any(ExporterInput.class));
            verify(createdExporter).setExporterOutput(any(OutputStreamExporterOutput.class));
            verify(createdExporter).setConfiguration(any(XlsxReportConfiguration.class));
            verify(createdExporter).exportReport();

            assertTrue(outputStream.size() >= 0);
        }
    }

    @Test
    void testWriteCsvZipFromEntities_AllFieldsPopulated() throws Exception {
        UUID projectGuid = UUID.randomUUID();
        UUID fiscalGuid  = UUID.randomUUID();
        UUID programGuid = UUID.randomUUID();

        ProgramAreaEntity pa = new ProgramAreaEntity();
        pa.setProgramAreaName("Business Area X");
        when(programAreaRepo.findById(programGuid)).thenReturn(Optional.of(pa));

        Date endorse = Date.from(LocalDate.of(2025, 8, 1).atStartOfDay(ZoneOffset.UTC).toInstant());
        Date approve = Date.from(LocalDate.of(2025, 8, 11).atStartOfDay(ZoneOffset.UTC).toInstant());

        FuelManagementReportEntity fuel = new FuelManagementReportEntity();
        fuel.setProjectGuid(projectGuid);
        fuel.setProjectPlanFiscalGuid(fiscalGuid);
        fuel.setProgramAreaGuid(programGuid);
        fuel.setProjectTypeDescription("FUEL_MGMT");
        fuel.setProjectName("Alpha Project");
        fuel.setForestRegionOrgUnitName("Region A");
        fuel.setForestDistrictOrgUnitName("District A");
        fuel.setBcParksRegionOrgUnitName("Parks Region A");
        fuel.setBcParksSectionOrgUnitName("Parks Section A");
        fuel.setFireCentreOrgUnitName("Fire Centre A");
        fuel.setPlanningUnitName("Planning Unit A");
        fuel.setGrossProjectAreaHa(BigDecimal.valueOf(123.45));
        fuel.setClosestCommunityName("Closestville");
        fuel.setProjectLead("Jane Doe");
        fuel.setProposalTypeDescription("Proposal X");
        fuel.setProjectFiscalName("Fiscal Name F");
        fuel.setProjectFiscalDescription("Fiscal Desc F");
        fuel.setFiscalYear("2025");
        fuel.setActivityCategoryDescription("Category F");
        fuel.setPlanFiscalStatusDescription("APPROVED");
        fuel.setTotalEstimatedCostAmount(BigDecimal.valueOf(1234567));
        fuel.setFiscalAncillaryFundAmount(BigDecimal.valueOf(890123));
        fuel.setFiscalReportedSpendAmount(BigDecimal.valueOf(456789));
        fuel.setFiscalActualAmount(BigDecimal.valueOf(111222));
        fuel.setFiscalPlannedProjectSizeHa(BigDecimal.valueOf(555.9));
        fuel.setFiscalCompletedSizeHa(BigDecimal.valueOf(42.0));
        fuel.setSpatialSubmitted("1/1");
        fuel.setFirstNationsEngagement("Y");
        fuel.setFirstNationsDelivPartners("N");
        fuel.setFirstNationsPartner("Partner A");
        fuel.setOtherPartner("Other P");
        fuel.setCfsProjectCode("CFS-1");
        fuel.setResultsProjectCode("R-PROJ");
        fuel.setResultsOpeningId("OPEN-1");
        fuel.setPrimaryObjectiveTypeDescription("Primary F");
        fuel.setSecondaryObjectiveTypeDescription("Secondary F");
        fuel.setEndorsementTimestamp(endorse);
        fuel.setApprovedTimestamp(approve);
        fuel.setLocalWuiRiskClassRationale("Local rationale F");
        fuel.setTotalCoarseFilterSectionScore(BigDecimal.valueOf(10));
        fuel.setTotalMediumFilterSectionScore(BigDecimal.valueOf(20));
        fuel.setMediumFilterSectionComment("Medium comment");
        fuel.setTotalFineFilterSectionScore(BigDecimal.valueOf(30));
        fuel.setFineFilterSectionComment("Fine comment");
        fuel.setTotalFilterSectionScore(BigDecimal.valueOf(60));

        CulturalPrescribedFireReportEntity crx = new CulturalPrescribedFireReportEntity();
        crx.setProjectGuid(projectGuid);
        crx.setProjectPlanFiscalGuid(fiscalGuid);
        crx.setProgramAreaGuid(programGuid);
        crx.setProjectTypeDescription("CRX");
        crx.setProjectName("Beta Project");
        crx.setForestRegionOrgUnitName("Region B");
        crx.setForestDistrictOrgUnitName("District B");
        crx.setBcParksRegionOrgUnitName("Parks Region B");
        crx.setBcParksSectionOrgUnitName("Parks Section B");
        crx.setFireCentreOrgUnitName("Fire Centre B");
        crx.setPlanningUnitName("Planning Unit B");
        crx.setGrossProjectAreaHa(BigDecimal.valueOf(987.8));
        crx.setClosestCommunityName("Townsville");
        crx.setProjectLead("John Roe");
        crx.setProposalTypeDescription("Proposal Y");
        crx.setProjectFiscalName("Fiscal Name C");
        crx.setProjectFiscalDescription("Fiscal Desc C");
        crx.setFiscalYear("2025");
        crx.setActivityCategoryDescription("Category C");
        crx.setPlanFiscalStatusDescription("ENDORSED");
        crx.setTotalEstimatedCostAmount(BigDecimal.valueOf(2000000));
        crx.setFiscalAncillaryFundAmount(BigDecimal.valueOf(100000));
        crx.setFiscalReportedSpendAmount(BigDecimal.valueOf(250000));
        crx.setFiscalActualAmount(BigDecimal.valueOf(300000));
        crx.setFiscalPlannedProjectSizeHa(BigDecimal.valueOf(1000.0));
        crx.setFiscalCompletedSizeHa(BigDecimal.valueOf(100.0));
        crx.setSpatialSubmitted("2/2");
        crx.setFirstNationsEngagement("N");
        crx.setFirstNationsDelivPartners("Y");
        crx.setFirstNationsPartner("Partner B");
        crx.setOtherPartner("Other C");
        crx.setCfsProjectCode("CFS-2");
        crx.setResultsProjectCode("R-PROJ-2");
        crx.setResultsOpeningId("OPEN-2");
        crx.setPrimaryObjectiveTypeDescription("Primary C");
        crx.setSecondaryObjectiveTypeDescription("Secondary C");
        crx.setEndorsementTimestamp(endorse);
        crx.setApprovedTimestamp(approve);
        crx.setOutsideWuiInd(true);
        crx.setWuiRiskClassDescription("WUI Class");
        crx.setLocalWuiRiskClassDescription("Local WUI");
        crx.setTotalRclFilterSectionScore(BigDecimal.valueOf(5));
        crx.setRclFilterSectionComment("RCL comment");
        crx.setTotalBdfFilterSectionScore(BigDecimal.valueOf(7));
        crx.setBdfFilterSectionComment("BDF comment");
        crx.setTotalCollimpFilterSectionScore(BigDecimal.valueOf(9));
        crx.setCollimpFilterSectionComment("COL comment");
        crx.setTotalFilterSectionScore(BigDecimal.valueOf(21));

        when(fuelRepo.findByProjectGuidIn(List.of(projectGuid))).thenReturn(List.of(fuel));
        when(crxRepo.findByProjectGuidIn(List.of(projectGuid))).thenReturn(List.of(crx));

        ByteArrayOutputStream zipOut = new ByteArrayOutputStream();
        reportService.writeCsvZipFromEntities(List.of(projectGuid), zipOut);

        Map<String, List<String>> csvs = unzipToFirstDataLine(zipOut.toByteArray());

        List<String> fuelRow = csvs.get("fuel-management-projects.csv");
        assertNotNull(fuelRow);
        assertEquals("=HYPERLINK(\"http://localhost/edit-project?projectGuid=" + projectGuid + "\", \"Alpha Project Project Link\")", unquote(fuelRow.get(0)));
        assertEquals("=HYPERLINK(\"http://localhost/edit-project?projectGuid=" + projectGuid + "&tab=fiscal&fiscalGuid=" + fiscalGuid + "\", \"Fiscal Name F Fiscal Activity Link\")", unquote(fuelRow.get(1)));
        assertEquals("Business Area X", unquote(fuelRow.get(9)));
        assertEquals("123 ha", unquote(fuelRow.get(11)));
        assertEquals("2025/26", unquote(fuelRow.get(17)));
        assertEquals("$1,234,567", unquote(fuelRow.get(20)));
        assertEquals("=\"1/1\"", unquote(fuelRow.get(26)));
        assertEquals("OPEN-1", unquote(fuelRow.get(33)));
        assertEquals("Primary F", unquote(fuelRow.get(34)));

        List<String> crxRow = csvs.get("cultural-prescribed-fire-projects.csv");
        assertNotNull(crxRow);
        assertEquals("=HYPERLINK(\"http://localhost/edit-project?projectGuid=" + projectGuid + "\", \"Beta Project Project Link\")", unquote(crxRow.get(0)));
        assertEquals("Business Area X", unquote(crxRow.get(9)));
        assertEquals("987 ha", unquote(crxRow.get(11)));
        assertEquals("2025/26", unquote(crxRow.get(17)));
        assertEquals("$2,000,000", unquote(crxRow.get(20)));
        assertEquals("=\"2/2\"", unquote(crxRow.get(26)));
        assertEquals("Secondary C", unquote(crxRow.get(35)));
        assertEquals("Y", unquote(crxRow.get(38)));
        assertEquals("WUI Class", unquote(crxRow.get(39)));
        assertEquals("Local WUI", unquote(crxRow.get(40)));
    }

    @Test
    void testWriteCsvZipFromEntities_NullsBecomeBlanks_AndBooleans() throws Exception {
        UUID projectGuid = UUID.randomUUID();
        FuelManagementReportEntity fuel = new FuelManagementReportEntity();
        fuel.setProjectGuid(projectGuid);
        CulturalPrescribedFireReportEntity crx = new CulturalPrescribedFireReportEntity();
        crx.setProjectGuid(projectGuid);
        crx.setOutsideWuiInd(false);
        when(fuelRepo.findByProjectGuidIn(List.of(projectGuid))).thenReturn(List.of(fuel));
        when(crxRepo.findByProjectGuidIn(List.of(projectGuid))).thenReturn(List.of(crx));
        ByteArrayOutputStream zipOut = new ByteArrayOutputStream();
        reportService.writeCsvZipFromEntities(List.of(projectGuid), zipOut);
        Map<String, List<String>> csvs = unzipToFirstDataLine(zipOut.toByteArray());
        List<String> fuelRow = csvs.get("fuel-management-projects.csv");
        List<String> crxRow = csvs.get("cultural-prescribed-fire-projects.csv");
        assertEquals("", unquote(fuelRow.get(11)));
        assertEquals("", unquote(fuelRow.get(33)));
        assertEquals("", unquote(fuelRow.get(34)));
        assertEquals("", unquote(crxRow.get(35)));
    }

    private static Map<String, List<String>> unzipToFirstDataLine(byte[] zipBytes) throws Exception {
        Map<String, List<String>> map = new HashMap<>();
        try (ZipInputStream zin = new ZipInputStream(new ByteArrayInputStream(zipBytes))) {
            for (ZipEntry e; (e = zin.getNextEntry()) != null; ) {
                String name = e.getName();
                String content = readAll(new BufferedReader(new InputStreamReader(zin, StandardCharsets.UTF_8)));
                String[] lines = content.split("\\R", -1);
                List<String> row = parseCsvLine(lines[1]);
                map.put(name, row);
            }
        }
        return map;
    }

    private static String readAll(BufferedReader br) throws Exception {
        StringBuilder sb = new StringBuilder();
        String s;
        while ((s = br.readLine()) != null) {
            sb.append(s).append("\n");
        }
        return sb.toString();
    }

    private static List<String> parseCsvLine(String line) {
        List<String> out = new ArrayList<>();
        String[] parts = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
        out.addAll(Arrays.asList(parts));
        return out;
    }

    private static String unquote(String quoted) {
        if (quoted == null) return null;
        String s = quoted;
        if (s.startsWith("\"") && s.endsWith("\"") && s.length() >= 2) {
            s = s.substring(1, s.length() - 1);
        }
        return s.replace("\"\"", "\"");
    }

}
