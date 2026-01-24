package ca.bc.gov.nrs.wfprev.services;

import ca.bc.gov.nrs.wfone.common.service.api.ServiceException;
import ca.bc.gov.nrs.wfprev.data.entities.CulturalPrescribedFireReportEntity;
import ca.bc.gov.nrs.wfprev.data.entities.FuelManagementReportEntity;
import ca.bc.gov.nrs.wfprev.data.models.ReportRequestModel;
import ca.bc.gov.nrs.wfprev.data.repositories.CulturalPrescribedFireReportRepository;
import ca.bc.gov.nrs.wfprev.data.repositories.FuelManagementReportRepository;
import ca.bc.gov.nrs.wfprev.data.repositories.ProgramAreaRepository;
import ca.bc.gov.nrs.wfprev.data.entities.ProjectEntity;
import ca.bc.gov.nrs.wfprev.data.params.FeatureQueryParams;
import ca.bc.gov.nrs.wfprev.services.FeaturesService;
import ca.bc.gov.nrs.wfprev.data.entities.ProjectFiscalEntity;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.math.BigDecimal;
import java.util.Date;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ReportServiceTest {

    private FuelManagementReportRepository fuelRepo;
    private CulturalPrescribedFireReportRepository crxRepo;
    private ProgramAreaRepository programAreaRepo;
    private FeaturesService featuresService;

    private ReportService service;

    @BeforeEach
    void setup() throws Exception {
        fuelRepo = mock(FuelManagementReportRepository.class);
        crxRepo = mock(CulturalPrescribedFireReportRepository.class);
        programAreaRepo = mock(ProgramAreaRepository.class);

        featuresService = mock(FeaturesService.class);
        service = new ReportService(fuelRepo, crxRepo, programAreaRepo, featuresService);

        setField(service, "baseUrl", "https://example.com");
        setField(service, "reportGeneratorLambdaUrl", "http://invalid/override-me-in-test");
    }

    @Test
    void resolveReportData_nullRequest_throwsIllegalArgument() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.writeCsvZipFromEntities(null, new ByteArrayOutputStream())
        );
        assertTrue(ex.getMessage().contains("At least one project or a filter is required"));
    }

    @Test
    void resolveReportData_emptyProjects_throwsIllegalArgument() {
        ReportRequestModel req = new ReportRequestModel();
        IllegalArgumentException ex1 = assertThrows(
                IllegalArgumentException.class,
                () -> service.writeCsvZipFromEntities(req, new ByteArrayOutputStream())
        );
        assertTrue(ex1.getMessage().contains("At least one project or a filter is required"));

        req.setProjects(Collections.emptyList());
        IllegalArgumentException ex2 = assertThrows(
                IllegalArgumentException.class,
                () -> service.writeCsvZipFromEntities(req, new ByteArrayOutputStream())
        );
        assertTrue(ex2.getMessage().contains("At least one project or a filter is required"));
    }

    @Test
    void resolveReportData_noFiscalGuids_usesFindByProjectGuid() throws Exception {
        UUID proj = UUID.randomUUID();

        when(fuelRepo.findByProjectGuid(proj))
                .thenReturn(List.of(fuel(proj,null, "Fuel N")));
        when(crxRepo.findByProjectGuid(proj))
                .thenReturn(List.of(crx(proj,null, "CRX N")));

        ReportRequestModel req = requestWithProjects(List.of(project(proj, /*fiscals*/ null)));

        service.writeCsvZipFromEntities(req, new ByteArrayOutputStream());

        verify(fuelRepo, times(1)).findByProjectGuid(proj);
        verify(crxRepo,  times(1)).findByProjectGuid(proj);
        verify(fuelRepo, never()).findByProjectGuidAndProjectPlanFiscalGuidIn(any(), any());
        verify(crxRepo,  never()).findByProjectGuidAndProjectPlanFiscalGuidIn(any(), any());
    }

    @Test
    void resolveReportData_withFilters_usesFeaturesService() throws Exception {
        ReportRequestModel req = new ReportRequestModel();
        FeatureQueryParams params = new FeatureQueryParams();
        params.setFiscalYears(List.of("2025"));
        req.setProjectFilter(params);

        UUID proj = UUID.randomUUID();
        UUID fiscal = UUID.randomUUID();
        
        ProjectEntity entity = new ProjectEntity();
        entity.setProjectGuid(proj);

        ProjectFiscalEntity fiscalEntity = new ProjectFiscalEntity();
        fiscalEntity.setProjectPlanFiscalGuid(fiscal);

        when(featuresService.findFilteredProjects(eq(params), eq(1), eq(Integer.MAX_VALUE), any(), any()))
                .thenReturn(List.of(entity));
        
        when(featuresService.findFilteredProjectFiscals(eq(proj), eq(List.of("2025")), any(), any()))
                .thenReturn(List.of(fiscalEntity));

        when(fuelRepo.findByProjectGuidAndProjectPlanFiscalGuidIn(eq(proj), eq(List.of(fiscal))))
                .thenReturn(List.of(fuel(proj, fiscal, "Fuel Filtered")));
        when(crxRepo.findByProjectGuidAndProjectPlanFiscalGuidIn(eq(proj), eq(List.of(fiscal))))
                .thenReturn(Collections.emptyList());

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        service.writeCsvZipFromEntities(req, out);

        verify(featuresService).findFilteredProjects(eq(params), eq(1), eq(Integer.MAX_VALUE), any(), any());
        verify(featuresService).findFilteredProjectFiscals(eq(proj), eq(List.of("2025")), any(), any());
        verify(fuelRepo).findByProjectGuidAndProjectPlanFiscalGuidIn(eq(proj), eq(List.of(fiscal)));
    }

    @Test
    void resolveReportData_withFiscalGuids_usesFindByProjectGuidAndFiscal() throws Exception {
        UUID proj = UUID.randomUUID();
        UUID fiscal = UUID.randomUUID();
        List<UUID> fiscals = List.of(fiscal);

        when(fuelRepo.findByProjectGuidAndProjectPlanFiscalGuidIn(eq(proj), eq(fiscals)))
                .thenReturn(List.of(fuel(proj, fiscal, "Fuel F")));
        when(crxRepo.findByProjectGuidAndProjectPlanFiscalGuidIn(eq(proj), eq(fiscals)))
                .thenReturn(List.of(crx(proj, fiscal, "CRX F")));

        ReportRequestModel req = requestWithProjects(List.of(project(proj, fiscals)));

        service.writeCsvZipFromEntities(req, new ByteArrayOutputStream());

        verify(fuelRepo, times(1)).findByProjectGuidAndProjectPlanFiscalGuidIn(eq(proj), eq(fiscals));
        verify(crxRepo,  times(1)).findByProjectGuidAndProjectPlanFiscalGuidIn(eq(proj), eq(fiscals));
        verify(fuelRepo, never()).findByProjectGuid(proj);
        verify(crxRepo,  never()).findByProjectGuid(proj);
    }
    

    @Test
    void writeCsvZip_onlyFuel_present_writesOneCsv() throws Exception {
        UUID projectGuid = UUID.randomUUID();
        UUID fiscalGuid = UUID.randomUUID();

        when(fuelRepo.findByProjectGuid(projectGuid)).thenReturn(List.of(fuel(projectGuid, fiscalGuid, "Fuel A")));
        when(crxRepo.findByProjectGuid(projectGuid)).thenReturn(Collections.emptyList());
        when(programAreaRepo.findById(any())).thenReturn(Optional.empty());

        ReportRequestModel req = requestWithProjects(List.of(project(projectGuid, null)));

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        service.writeCsvZipFromEntities(req, out);

        Set<String> entries = zipEntries(out.toByteArray());
        assertTrue(entries.contains("fuel-management-projects.csv"));
        assertFalse(entries.contains("cultural-prescribed-fire-projects.csv"));
    }

    @Test
    void writeCsvZip_onlyCrx_present_writesOneCsv() throws Exception {
        UUID projectGuid = UUID.randomUUID();
        UUID fiscalGuid = UUID.randomUUID();

        when(fuelRepo.findByProjectGuid(projectGuid)).thenReturn(Collections.emptyList());
        when(crxRepo.findByProjectGuid(projectGuid)).thenReturn(List.of(crx(projectGuid, fiscalGuid, "CRX A")));
        when(programAreaRepo.findById(any())).thenReturn(Optional.empty());

        ReportRequestModel req = requestWithProjects(List.of(project(projectGuid, null)));

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        service.writeCsvZipFromEntities(req, out);

        Set<String> entries = zipEntries(out.toByteArray());
        assertFalse(entries.contains("fuel-management-projects.csv"));
        assertTrue(entries.contains("cultural-prescribed-fire-projects.csv"));
    }

    @Test
    void writeCsvZip_noData_throwsIllegalArgument() {
        UUID projectGuid = UUID.randomUUID();
        when(fuelRepo.findByProjectGuid(projectGuid)).thenReturn(Collections.emptyList());
        when(crxRepo.findByProjectGuid(projectGuid)).thenReturn(Collections.emptyList());

        ReportRequestModel req = requestWithProjects(List.of(project(projectGuid, null)));

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.writeCsvZipFromEntities(req, new ByteArrayOutputStream())
        );
        assertTrue(ex.getMessage().toLowerCase().contains("no fiscal data"));
    }


    @Test
    void exportXlsx_success_writesReturnedBytes() throws Exception {
        byte[] xlsxBytes = "test-xlsx-contents".getBytes(StandardCharsets.UTF_8);
        String payload = lambdaResponseWithSingleFile(
                "project-report.xlsx",
                Base64.getEncoder().encodeToString(xlsxBytes)
        );

        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/lambda", (HttpExchange ex) -> {
            byte[] response = payload.getBytes(StandardCharsets.UTF_8);
            ex.getResponseHeaders().add("Content-Type", "application/json");
            ex.sendResponseHeaders(200, response.length);
            try (OutputStream os = ex.getResponseBody()) {
                os.write(response);
            }
        });

        try (var ignored = start(server)) {
            String url = "http://localhost:" + server.getAddress().getPort() + "/lambda";
            setField(service, "reportGeneratorLambdaUrl", url);

            UUID projectGuid = UUID.randomUUID();
            UUID fiscalGuid = UUID.randomUUID();
            when(fuelRepo.findByProjectGuid(projectGuid)).thenReturn(List.of(fuel(projectGuid, fiscalGuid, "Fuel X")));
            when(crxRepo.findByProjectGuid(projectGuid)).thenReturn(List.of(crx(projectGuid, fiscalGuid, "CRX X")));
            when(programAreaRepo.findById(any())).thenReturn(Optional.empty());

            ReportRequestModel req = requestWithProjects(List.of(project(projectGuid, null)));

            try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                service.exportXlsx(req, out);
                assertArrayEquals(xlsxBytes, out.toByteArray(),
                        "Should write exactly the XLSX bytes returned by Lambda");
            }
        }
    }

    @Test
    void exportXlsx_lambdaNon200_throwsServiceException() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/lambda", (HttpExchange ex) -> {
            byte[] response = "boom".getBytes(StandardCharsets.UTF_8);
            ex.getResponseHeaders().add("Content-Type", "text/plain");
            ex.sendResponseHeaders(500, response.length);
            try (OutputStream os = ex.getResponseBody()) {
                os.write(response);
            }
        });
        server.start();
        try {
            String url = "http://localhost:" + server.getAddress().getPort() + "/lambda";
            setField(service, "reportGeneratorLambdaUrl", url);

            UUID proj = UUID.randomUUID();
            when(fuelRepo.findByProjectGuid(proj)).thenReturn(List.of(fuel(proj, null, "Fuel Y")));
            when(crxRepo.findByProjectGuid(proj)).thenReturn(Collections.emptyList());

            ReportRequestModel req = requestWithProjects(List.of(project(proj, null)));

            ServiceException ex = assertThrows(
                    ServiceException.class,
                    () -> service.exportXlsx(req, new ByteArrayOutputStream())
            );
            assertTrue(ex.getMessage().contains("Lambda returned error"));
        } finally {
            server.stop(0);
        }
    }

    @Test
    void exportXlsx_lambdaReturnsNoFiles_throwsServiceException() throws Exception {
        String payload = "{\"files\":[]}";

        com.sun.net.httpserver.HttpServer server = com.sun.net.httpserver.HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/lambda", ex -> {
            byte[] resp = payload.getBytes(StandardCharsets.UTF_8);
            ex.getResponseHeaders().add("Content-Type", "application/json");
            ex.sendResponseHeaders(200, resp.length);
            try (OutputStream os = ex.getResponseBody()) { os.write(resp); }
        });

        try (var ignored = start(server)) {
            String url = "http://localhost:" + server.getAddress().getPort() + "/lambda";
            setField(service, "reportGeneratorLambdaUrl", url);

            UUID proj = UUID.randomUUID();
            when(fuelRepo.findByProjectGuid(proj)).thenReturn(List.of(fuel(proj, null, "Fuel Q")));
            when(crxRepo.findByProjectGuid(proj)).thenReturn(Collections.emptyList());

            ReportRequestModel req = requestWithProjects(List.of(project(proj, null)));

            ServiceException ex = assertThrows(
                    ServiceException.class,
                    () -> service.exportXlsx(req, new ByteArrayOutputStream())
            );
            assertTrue(ex.getMessage().contains("No files returned"));
        }
    }

    @Test
    void exportXlsx_noData_throwsIllegalArgument() throws Exception {
        UUID proj = UUID.randomUUID();
        when(fuelRepo.findByProjectGuid(proj)).thenReturn(Collections.emptyList());
        when(crxRepo.findByProjectGuid(proj)).thenReturn(Collections.emptyList());

        ReportRequestModel req = requestWithProjects(List.of(project(proj, null)));

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.exportXlsx(req, new ByteArrayOutputStream())
        );
        assertTrue(ex.getMessage().toLowerCase().contains("no fiscal data"));
    }

    @Test
    void exportXlsx_missingLambdaUrl_throwsServiceException() throws Exception {
        UUID proj = UUID.randomUUID();
        UUID fiscal = UUID.randomUUID();

        when(fuelRepo.findByProjectGuid(proj)).thenReturn(List.of(fuel(proj, fiscal, "Fuel Z")));
        when(crxRepo.findByProjectGuid(proj)).thenReturn(List.of(crx(proj, fiscal, "CRX Z")));

        // Blank URL triggers the ServiceException
        setField(service, "reportGeneratorLambdaUrl", "");

        ReportRequestModel req = requestWithProjects(List.of(project(proj, null)));

        ServiceException ex = assertThrows(
                ServiceException.class,
                () -> service.exportXlsx(req, new ByteArrayOutputStream())
        );
        assertTrue(ex.getMessage().contains("REPORT_GENERATOR_LAMBDA_URL"));
    }


    @Test
    void writeCsvZip_includesAllFields() throws Exception {
        UUID projectGuid = UUID.randomUUID();
        UUID fiscalGuid = UUID.randomUUID();

        // Setup Fuel Entity with ALL fields populated
        FuelManagementReportEntity fuelEntity = new FuelManagementReportEntity();
        fuelEntity.setUniqueRowGuid(UUID.randomUUID());
        fuelEntity.setProjectGuid(projectGuid);
        fuelEntity.setProjectPlanFiscalGuid(fiscalGuid);
        fuelEntity.setLinkToProject("http://project/fuel");
        fuelEntity.setLinkToFiscalActivity("http://fiscal/fuel");
        fuelEntity.setProjectTypeDescription("Fuel Mgmt");
        fuelEntity.setProjectName("Fuel Alpha");
        fuelEntity.setForestRegionOrgUnitName("Region A");
        fuelEntity.setForestDistrictOrgUnitName("District A");
        fuelEntity.setBcParksRegionOrgUnitName("Parks Region A");
        fuelEntity.setBcParksSectionOrgUnitName("Parks Section A");
        fuelEntity.setFireCentreOrgUnitName("Fire Centre A");
        fuelEntity.setBusinessArea("Business Area A");
        fuelEntity.setPlanningUnitName("Planning Unit A");
        fuelEntity.setGrossProjectAreaHa(new BigDecimal("123.45"));
        fuelEntity.setClosestCommunityName("Community A");
        fuelEntity.setProjectLead("Lead A");
        fuelEntity.setProposalTypeDescription("Proposal A");
        fuelEntity.setProjectFiscalName("Fiscal A");
        fuelEntity.setProjectFiscalDescription("Desc A");
        fuelEntity.setFiscalYear("2025");
        fuelEntity.setActivityCategoryDescription("Activity A");
        fuelEntity.setPlanFiscalStatusDescription("Status A");
        fuelEntity.setTotalEstimatedCostAmount(new BigDecimal("1000.00"));
        fuelEntity.setFiscalForecastAmount(new BigDecimal("1100.00"));
        fuelEntity.setFiscalAncillaryFundAmount(new BigDecimal("500.00"));
        fuelEntity.setAncillaryFundingProvider("Provider A");
        fuelEntity.setFiscalReportedSpendAmount(new BigDecimal("900.00"));
        fuelEntity.setFiscalActualAmount(new BigDecimal("950.00"));
        fuelEntity.setFiscalPlannedProjectSizeHa(new BigDecimal("50.5"));
        fuelEntity.setFiscalCompletedSizeHa(new BigDecimal("40.5"));
        fuelEntity.setSpatialSubmitted("1/1");
        fuelEntity.setFirstNationsEngagement("Y");
        fuelEntity.setFirstNationsDelivPartners("N");
        fuelEntity.setFirstNationsPartner("FN Partner A");
        fuelEntity.setOtherPartner("Other Partner A");
        fuelEntity.setCfsProjectCode("CFS-A");
        fuelEntity.setResultsProjectCode("RES-A");
        fuelEntity.setResultsOpeningId("OPEN-A");
        fuelEntity.setPrimaryObjectiveTypeDescription("Obj A");
        fuelEntity.setSecondaryObjectiveTypeDescription("Obj B");
        fuelEntity.setEndorsementTimestamp(Date.from(Instant.parse("2025-01-01T20:00:00Z")));
        fuelEntity.setApprovedTimestamp(Date.from(Instant.parse("2025-02-01T20:00:00Z")));
        fuelEntity.setWuiRiskClassDescription("Risk A");
        fuelEntity.setLocalWuiRiskClassDescription("Local Risk A");
        fuelEntity.setLocalWuiRiskClassRationale("Rationale A");
        fuelEntity.setTotalCoarseFilterSectionScore(new BigDecimal("10"));
        fuelEntity.setTotalMediumFilterSectionScore(new BigDecimal("20"));
        fuelEntity.setMediumFilterSectionComment("Medium Comment A");
        fuelEntity.setTotalFineFilterSectionScore(new BigDecimal("30"));
        fuelEntity.setFineFilterSectionComment("Fine Comment A");
        fuelEntity.setTotalFilterSectionScore(new BigDecimal("60"));
        fuelEntity.setQ1SubmittedTimestamp(Date.from(Instant.parse("2025-04-15T12:00:00Z")));
        fuelEntity.setQ1ProgressStatusCode("ON_TRACK");
        fuelEntity.setQ1GeneralUpdateComment("Q1 Comment A");
        fuelEntity.setQ1ForecastAmount(new BigDecimal("100.00"));
        fuelEntity.setQ1ForecastAdjustmentAmount(new BigDecimal("10.00"));
        fuelEntity.setQ1ForecastAdjustmentRationale("Adj Q1 A");
        fuelEntity.setQ1BudgetHighRiskAmount(new BigDecimal("1.00"));
        fuelEntity.setQ1BudgetHighRiskRationale("High Q1 A");
        fuelEntity.setQ1BudgetMediumRiskAmount(new BigDecimal("2.00"));
        fuelEntity.setQ1BudgetMediumRiskRationale("Med Q1 A");
        fuelEntity.setQ1BudgetLowRiskAmount(new BigDecimal("3.00"));
        fuelEntity.setQ1BudgetLowRiskRationale("Low Q1 A");
        fuelEntity.setQ1BudgetCompletedAmount(new BigDecimal("4.00"));
        fuelEntity.setQ1BudgetCompletedDescription("Comp Q1 A");
        fuelEntity.setQ2SubmittedTimestamp(Date.from(Instant.parse("2025-07-15T12:00:00Z")));
        fuelEntity.setQ2ProgressStatusCode("DELAYED");
        fuelEntity.setQ2GeneralUpdateComment("Q2 Comment A");
        fuelEntity.setQ2ForecastAmount(new BigDecimal("200.00"));
        fuelEntity.setQ2ForecastAdjustmentAmount(new BigDecimal("20.00"));
        fuelEntity.setQ2ForecastAdjustmentRationale("Adj Q2 A");
        fuelEntity.setQ2BudgetHighRiskAmount(new BigDecimal("11.00"));
        fuelEntity.setQ2BudgetHighRiskRationale("High Q2 A");
        fuelEntity.setQ2BudgetMediumRiskAmount(new BigDecimal("12.00"));
        fuelEntity.setQ2BudgetMediumRiskRationale("Med Q2 A");
        fuelEntity.setQ2BudgetLowRiskAmount(new BigDecimal("13.00"));
        fuelEntity.setQ2BudgetLowRiskRationale("Low Q2 A");
        fuelEntity.setQ2BudgetCompletedAmount(new BigDecimal("14.00"));
        fuelEntity.setQ2BudgetCompletedDescription("Comp Q2 A");
        fuelEntity.setQ3SubmittedTimestamp(Date.from(Instant.parse("2025-10-15T12:00:00Z")));
        fuelEntity.setQ3ProgressStatusCode("At Risk");
        fuelEntity.setQ3GeneralUpdateComment("Q3 Comment A");
        fuelEntity.setQ3ForecastAmount(new BigDecimal("300.00"));
        fuelEntity.setQ3ForecastAdjustmentAmount(new BigDecimal("30.00"));
        fuelEntity.setQ3ForecastAdjustmentRationale("Adj Q3 A");
        fuelEntity.setQ3BudgetHighRiskAmount(new BigDecimal("21.00"));
        fuelEntity.setQ3BudgetHighRiskRationale("High Q3 A");
        fuelEntity.setQ3BudgetMediumRiskAmount(new BigDecimal("22.00"));
        fuelEntity.setQ3BudgetMediumRiskRationale("Med Q3 A");
        fuelEntity.setQ3BudgetLowRiskAmount(new BigDecimal("23.00"));
        fuelEntity.setQ3BudgetLowRiskRationale("Low Q3 A");
        fuelEntity.setQ3BudgetCompletedAmount(new BigDecimal("24.00"));
        fuelEntity.setQ3BudgetCompletedDescription("Comp Q3 A");
        fuelEntity.setMarch7SubmittedTimestamp(Date.from(Instant.parse("2026-03-07T12:00:00Z")));
        fuelEntity.setMarch7ProgressStatusCode("Complete");
        fuelEntity.setMarch7GeneralUpdateComment("Mar7 Comment A");
        fuelEntity.setMarch7ForecastAmount(new BigDecimal("400.00"));
        fuelEntity.setMarch7ForecastAdjustmentAmount(new BigDecimal("40.00"));
        fuelEntity.setMarch7ForecastAdjustmentRationale("Adj Mar7 A");
        fuelEntity.setMarch7BudgetHighRiskAmount(new BigDecimal("41.00"));
        fuelEntity.setMarch7BudgetHighRiskRationale("High Mar7 A");
        fuelEntity.setMarch7BudgetMediumRiskAmount(new BigDecimal("42.00"));
        fuelEntity.setMarch7BudgetMediumRiskRationale("Med Mar7 A");
        fuelEntity.setMarch7BudgetLowRiskAmount(new BigDecimal("43.00"));
        fuelEntity.setMarch7BudgetLowRiskRationale("Low Mar7 A");
        fuelEntity.setMarch7BudgetCompletedAmount(new BigDecimal("44.00"));
        fuelEntity.setMarch7BudgetCompletedDescription("Comp Mar7 A");
        fuelEntity.setOtherSubmittedTimestamp(Date.from(Instant.parse("2026-05-15T12:00:00Z")));
        fuelEntity.setOtherProgressStatusCode("Post-Project");
        fuelEntity.setOtherGeneralUpdateComment("Other Comment A");
        fuelEntity.setOtherForecastAmount(new BigDecimal("500.00"));
        fuelEntity.setOtherForecastAdjustmentAmount(new BigDecimal("50.00"));
        fuelEntity.setOtherForecastAdjustmentRationale("Adj Other A");
        fuelEntity.setOtherBudgetHighRiskAmount(new BigDecimal("51.00"));
        fuelEntity.setOtherBudgetHighRiskRationale("High Other A");
        fuelEntity.setOtherBudgetMediumRiskAmount(new BigDecimal("52.00"));
        fuelEntity.setOtherBudgetMediumRiskRationale("Med Other A");
        fuelEntity.setOtherBudgetLowRiskAmount(new BigDecimal("53.00"));
        fuelEntity.setOtherBudgetLowRiskRationale("Low Other A");
        fuelEntity.setOtherBudgetCompletedAmount(new BigDecimal("54.00"));
        fuelEntity.setOtherBudgetCompletedDescription("Comp Other A");

        CulturalPrescribedFireReportEntity crxEntity = new CulturalPrescribedFireReportEntity();
        crxEntity.setUniqueRowGuid(UUID.randomUUID());
        crxEntity.setProjectGuid(projectGuid);
        crxEntity.setProjectPlanFiscalGuid(fiscalGuid);
        crxEntity.setLinkToProject("http://project/crx");
        crxEntity.setLinkToFiscalActivity("http://fiscal/crx");
        crxEntity.setProjectTypeDescription("Cultural Fire");
        crxEntity.setProjectName("CRX Beta");
        crxEntity.setForestRegionOrgUnitName("Region B");
        crxEntity.setForestDistrictOrgUnitName("District B");
        crxEntity.setBcParksRegionOrgUnitName("Parks Region B");
        crxEntity.setBcParksSectionOrgUnitName("Parks Section B");
        crxEntity.setFireCentreOrgUnitName("Fire Centre B");
        crxEntity.setBusinessArea("Business Area B");
        crxEntity.setPlanningUnitName("Planning Unit B");
        crxEntity.setGrossProjectAreaHa(new BigDecimal("234.56"));
        crxEntity.setClosestCommunityName("Community B");
        crxEntity.setProjectLead("Lead B");
        crxEntity.setProposalTypeDescription("Proposal B");
        crxEntity.setProjectFiscalName("Fiscal B");
        crxEntity.setProjectFiscalDescription("Desc B");
        crxEntity.setFiscalYear("2025");
        crxEntity.setActivityCategoryDescription("Activity B");
        crxEntity.setPlanFiscalStatusDescription("Status B");
        crxEntity.setTotalEstimatedCostAmount(new BigDecimal("2000.00"));
        crxEntity.setFiscalForecastAmount(new BigDecimal("2100.00"));
        crxEntity.setFiscalAncillaryFundAmount(new BigDecimal("600.00"));
        crxEntity.setAncillaryFundingProvider("Provider B");
        crxEntity.setFiscalReportedSpendAmount(new BigDecimal("1900.00"));
        crxEntity.setFiscalActualAmount(new BigDecimal("1950.00"));
        crxEntity.setFiscalPlannedProjectSizeHa(new BigDecimal("60.5"));
        crxEntity.setFiscalCompletedSizeHa(new BigDecimal("50.5"));
        crxEntity.setSpatialSubmitted("1/2");
        crxEntity.setFirstNationsEngagement("N");
        crxEntity.setFirstNationsDelivPartners("Y");
        crxEntity.setFirstNationsPartner("FN Partner B");
        crxEntity.setOtherPartner("Other Partner B");
        crxEntity.setCfsProjectCode("CFS-B");
        crxEntity.setResultsProjectCode("RES-B");
        crxEntity.setResultsOpeningId("OPEN-B");
        crxEntity.setPrimaryObjectiveTypeDescription("Obj C");
        crxEntity.setSecondaryObjectiveTypeDescription("Obj D");
        crxEntity.setEndorsementTimestamp(Date.from(Instant.parse("2025-03-01T20:00:00Z")));
        crxEntity.setApprovedTimestamp(Date.from(Instant.parse("2025-04-01T20:00:00Z")));
        crxEntity.setOutsideWuiInd(true);
        crxEntity.setWuiRiskClassDescription("Risk B");
        crxEntity.setLocalWuiRiskClassDescription("Local Risk B");
        crxEntity.setTotalRclFilterSectionScore(new BigDecimal("5"));
        crxEntity.setRclFilterSectionComment("RCL Comment B");
        crxEntity.setTotalBdfFilterSectionScore(new BigDecimal("15"));
        crxEntity.setBdfFilterSectionComment("BDF Comment B");
        crxEntity.setTotalCollimpFilterSectionScore(new BigDecimal("25"));
        crxEntity.setCollimpFilterSectionComment("Coll Imp Comment B");
        crxEntity.setTotalFilterSectionScore(new BigDecimal("45"));
        crxEntity.setQ1SubmittedTimestamp(Date.from(Instant.parse("2025-04-20T12:00:00Z")));
        crxEntity.setQ1ProgressStatusCode("ON_TRACK");
        crxEntity.setQ1GeneralUpdateComment("Q1 Comment B");
        crxEntity.setQ1ForecastAmount(new BigDecimal("300.00"));
        crxEntity.setQ1ForecastAdjustmentAmount(new BigDecimal("30.00"));
        crxEntity.setQ1ForecastAdjustmentRationale("Adj Q1 B");
        crxEntity.setQ1BudgetHighRiskAmount(new BigDecimal("31.00"));
        crxEntity.setQ1BudgetHighRiskRationale("High Q1 B");
        crxEntity.setQ1BudgetMediumRiskAmount(new BigDecimal("32.00"));
        crxEntity.setQ1BudgetMediumRiskRationale("Med Q1 B");
        crxEntity.setQ1BudgetLowRiskAmount(new BigDecimal("33.00"));
        crxEntity.setQ1BudgetLowRiskRationale("Low Q1 B");
        crxEntity.setQ1BudgetCompletedAmount(new BigDecimal("34.00"));
        crxEntity.setQ1BudgetCompletedDescription("Comp Q1 B");
        crxEntity.setQ2SubmittedTimestamp(Date.from(Instant.parse("2025-07-20T12:00:00Z")));
        crxEntity.setQ2ProgressStatusCode("DELAYED");
        crxEntity.setQ2GeneralUpdateComment("Q2 Comment B");
        crxEntity.setQ2ForecastAmount(new BigDecimal("302.00"));
        crxEntity.setQ2ForecastAdjustmentAmount(new BigDecimal("30.00"));
        crxEntity.setQ2ForecastAdjustmentRationale("Adj Q2 B");
        crxEntity.setQ2BudgetHighRiskAmount(new BigDecimal("31.00"));
        crxEntity.setQ2BudgetHighRiskRationale("High Q2 B");
        crxEntity.setQ2BudgetMediumRiskAmount(new BigDecimal("32.00"));
        crxEntity.setQ2BudgetMediumRiskRationale("Med Q2 B");
        crxEntity.setQ2BudgetLowRiskAmount(new BigDecimal("33.00"));
        crxEntity.setQ2BudgetLowRiskRationale("Low Q2 B");
        crxEntity.setQ2BudgetCompletedAmount(new BigDecimal("34.00"));
        crxEntity.setQ2BudgetCompletedDescription("Comp Q2 B");
        crxEntity.setQ3SubmittedTimestamp(Date.from(Instant.parse("2025-10-20T12:00:00Z")));
        crxEntity.setQ3ProgressStatusCode("At Risk");
        crxEntity.setQ3GeneralUpdateComment("Q3 Comment B");
        crxEntity.setQ3ForecastAmount(new BigDecimal("303.00"));
        crxEntity.setQ3ForecastAdjustmentAmount(new BigDecimal("30.00"));
        crxEntity.setQ3ForecastAdjustmentRationale("Adj Q3 B");
        crxEntity.setQ3BudgetHighRiskAmount(new BigDecimal("31.00"));
        crxEntity.setQ3BudgetHighRiskRationale("High Q3 B");
        crxEntity.setQ3BudgetMediumRiskAmount(new BigDecimal("32.00"));
        crxEntity.setQ3BudgetMediumRiskRationale("Med Q3 B");
        crxEntity.setQ3BudgetLowRiskAmount(new BigDecimal("33.00"));
        crxEntity.setQ3BudgetLowRiskRationale("Low Q3 B");
        crxEntity.setQ3BudgetCompletedAmount(new BigDecimal("34.00"));
        crxEntity.setQ3BudgetCompletedDescription("Comp Q3 B");
        crxEntity.setMarch7SubmittedTimestamp(Date.from(Instant.parse("2026-03-07T12:00:00Z")));
        crxEntity.setMarch7ProgressStatusCode("Complete");
        crxEntity.setMarch7GeneralUpdateComment("Mar7 Comment B");
        crxEntity.setMarch7ForecastAmount(new BigDecimal("304.00"));
        crxEntity.setMarch7ForecastAdjustmentAmount(new BigDecimal("30.00"));
        crxEntity.setMarch7ForecastAdjustmentRationale("Adj Mar7 B");
        crxEntity.setMarch7BudgetHighRiskAmount(new BigDecimal("31.00"));
        crxEntity.setMarch7BudgetHighRiskRationale("High Mar7 B");
        crxEntity.setMarch7BudgetMediumRiskAmount(new BigDecimal("32.00"));
        crxEntity.setMarch7BudgetMediumRiskRationale("Med Mar7 B");
        crxEntity.setMarch7BudgetLowRiskAmount(new BigDecimal("33.00"));
        crxEntity.setMarch7BudgetLowRiskRationale("Low Mar7 B");
        crxEntity.setMarch7BudgetCompletedAmount(new BigDecimal("34.00"));
        crxEntity.setMarch7BudgetCompletedDescription("Comp Mar7 B");
        crxEntity.setOtherSubmittedTimestamp(Date.from(Instant.parse("2026-05-20T12:00:00Z")));
        crxEntity.setOtherProgressStatusCode("Post-Project");
        crxEntity.setOtherGeneralUpdateComment("Other Comment B");
        crxEntity.setOtherForecastAmount(new BigDecimal("305.00"));
        crxEntity.setOtherForecastAdjustmentAmount(new BigDecimal("30.00"));
        crxEntity.setOtherForecastAdjustmentRationale("Adj Other B");
        crxEntity.setOtherBudgetHighRiskAmount(new BigDecimal("31.00"));
        crxEntity.setOtherBudgetHighRiskRationale("High Other B");
        crxEntity.setOtherBudgetMediumRiskAmount(new BigDecimal("32.00"));
        crxEntity.setOtherBudgetMediumRiskRationale("Med Other B");
        crxEntity.setOtherBudgetLowRiskAmount(new BigDecimal("33.00"));
        crxEntity.setOtherBudgetLowRiskRationale("Low Other B");
        crxEntity.setOtherBudgetCompletedAmount(new BigDecimal("34.00"));
        crxEntity.setOtherBudgetCompletedDescription("Comp Other B");

        when(fuelRepo.findByProjectGuid(projectGuid)).thenReturn(List.of(fuelEntity));
        when(crxRepo.findByProjectGuid(projectGuid)).thenReturn(List.of(crxEntity));
        when(programAreaRepo.findById(any())).thenReturn(Optional.empty());

        ReportRequestModel req = requestWithProjects(List.of(project(projectGuid, null)));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        service.writeCsvZipFromEntities(req, out);

        // Read zip and verify CSV content
        try (ZipInputStream zin = new ZipInputStream(new ByteArrayInputStream(out.toByteArray()))) {
            ZipEntry entry;
            while ((entry = zin.getNextEntry()) != null) {
                byte[] content = zin.readAllBytes();
                String csv = new String(content, StandardCharsets.UTF_8);
                String[] lines = csv.split("\\r?\\n");
                
                if (entry.getName().contains("fuel")) {
                    String row = lines[1];
                    String expectedProjectLink = "\"=HYPERLINK(\"\"https://example.com/edit-project?projectGuid=" + projectGuid + "\"\", \"\"Fuel Alpha Project Link\"\")\"";
                    String expectedFiscalLink = "\"=HYPERLINK(\"\"https://example.com/edit-project?projectGuid=" + projectGuid + "&tab=fiscal&fiscalGuid=" + fiscalGuid + "\"\", \"\"Fiscal A Fiscal Activity Link\"\")\"";
                    
                    assertTrue(row.contains(expectedProjectLink), "Row missing correct Project Link: " + expectedProjectLink);
                    assertTrue(row.contains(expectedFiscalLink), "Row missing correct Fiscal Link: " + expectedFiscalLink);

                    assertTrue(row.contains("Fuel Mgmt"));
                    assertTrue(row.contains("Fuel Alpha"));
                    assertTrue(row.contains("Region A"));
                    assertTrue(row.contains("District A"));
                    assertTrue(row.contains("Parks Region A"));
                    assertTrue(row.contains("Parks Section A"));
                    assertTrue(row.contains("Fire Centre A"));
                    assertTrue(row.contains("Business Area A"));
                    assertTrue(row.contains("Planning Unit A"));
                    assertTrue(row.contains("123 ha"));
                    assertTrue(row.contains("Community A"));
                    assertTrue(row.contains("Lead A"));
                    assertTrue(row.contains("Proposal A"));
                    assertTrue(row.contains("Fiscal A"));
                    assertTrue(row.contains("Desc A"));
                    assertTrue(row.contains("2025/26"));
                    assertTrue(row.contains("Activity A"));
                    assertTrue(row.contains("Status A"));
                    assertTrue(row.contains("$1,000"));
                    assertTrue(row.contains("$1,100"));
                    assertTrue(row.contains("$500"));
                    assertTrue(row.contains("Provider A"));
                    assertTrue(row.contains("$900"));
                    assertTrue(row.contains("$950"));
                    assertTrue(row.contains("50 ha"));
                    assertTrue(row.contains("40 ha"));
                    assertTrue(row.contains("\"=\"\"1/1\"\"\""));
                    assertTrue(row.contains("Y"));
                    assertTrue(row.contains("N"));
                    assertTrue(row.contains("FN Partner A"));
                    assertTrue(row.contains("Other Partner A"));
                    assertTrue(row.contains("CFS-A"));
                    assertTrue(row.contains("RES-A"));
                    assertTrue(row.contains("OPEN-A"));
                    assertTrue(row.contains("Obj A"));
                    assertTrue(row.contains("Obj B"));
                    assertTrue(row.contains("2025-01-01"));
                    assertTrue(row.contains("2025-02-01"));
                    assertTrue(row.contains("Risk A"));
                    assertTrue(row.contains("Local Risk A"));
                    assertTrue(row.contains("Rationale A"));
                    assertTrue(row.contains("10"));
                    assertTrue(row.contains("20"));
                    assertTrue(row.contains("Medium Comment A"));
                    assertTrue(row.contains("30"));
                    assertTrue(row.contains("Fine Comment A"));
                    assertTrue(row.contains("60"));
                    assertTrue(row.contains("2025-04-15"));
                    assertTrue(row.contains("ON_TRACK"));
                    assertTrue(row.contains("Q1 Comment A"));
                    assertTrue(row.contains("$100"));
                    assertTrue(row.contains("$10"));
                    assertTrue(row.contains("Adj Q1 A"));
                    assertTrue(row.contains("$1"));
                    assertTrue(row.contains("High Q1 A"));
                    assertTrue(row.contains("$2"));
                    assertTrue(row.contains("Med Q1 A"));
                    assertTrue(row.contains("$3"));
                    assertTrue(row.contains("Low Q1 A"));
                    assertTrue(row.contains("$4"));
                    assertTrue(row.contains("Comp Q1 A"));
                    assertTrue(row.contains("2025-07-15"));
                    assertTrue(row.contains("DELAYED"));
                    assertTrue(row.contains("Q2 Comment A"));
                    assertTrue(row.contains("$200"));
                    assertTrue(row.contains("$20"));
                    assertTrue(row.contains("Adj Q2 A"));
                    assertTrue(row.contains("$11"));
                    assertTrue(row.contains("High Q2 A"));
                    assertTrue(row.contains("$12"));
                    assertTrue(row.contains("Med Q2 A"));
                    assertTrue(row.contains("$13"));
                    assertTrue(row.contains("Low Q2 A"));
                    assertTrue(row.contains("$14"));
                    assertTrue(row.contains("Comp Q2 A"));
                    assertTrue(row.contains("2025-10-15"));
                    assertTrue(row.contains("At Risk"));
                    assertTrue(row.contains("Q3 Comment A"));
                    assertTrue(row.contains("$300"));
                    assertTrue(row.contains("$30"));
                    assertTrue(row.contains("Adj Q3 A"));
                    assertTrue(row.contains("$21"));
                    assertTrue(row.contains("High Q3 A"));
                    assertTrue(row.contains("$22"));
                    assertTrue(row.contains("Med Q3 A"));
                    assertTrue(row.contains("$23"));
                    assertTrue(row.contains("Low Q3 A"));
                    assertTrue(row.contains("$24"));
                    assertTrue(row.contains("Comp Q3 A"));
                    assertTrue(row.contains("2026-03-07"));
                    assertTrue(row.contains("Complete"));
                    assertTrue(row.contains("Mar7 Comment A"));
                    assertTrue(row.contains("$400"));
                    assertTrue(row.contains("$40"));
                    assertTrue(row.contains("Adj Mar7 A"));
                    assertTrue(row.contains("$41"));
                    assertTrue(row.contains("High Mar7 A"));
                    assertTrue(row.contains("$42"));
                    assertTrue(row.contains("Med Mar7 A"));
                    assertTrue(row.contains("$43"));
                    assertTrue(row.contains("Low Mar7 A"));
                    assertTrue(row.contains("$44"));
                    assertTrue(row.contains("Comp Mar7 A"));
                    assertTrue(row.contains("2026-05-15"));
                    assertTrue(row.contains("Post-Project"));
                    assertTrue(row.contains("Other Comment A"));
                    assertTrue(row.contains("$500"));
                    assertTrue(row.contains("$50"));
                    assertTrue(row.contains("Adj Other A"));
                    assertTrue(row.contains("$51"));
                    assertTrue(row.contains("High Other A"));
                    assertTrue(row.contains("$52"));
                    assertTrue(row.contains("Med Other A"));
                    assertTrue(row.contains("$53"));
                    assertTrue(row.contains("Low Other A"));
                    assertTrue(row.contains("$54"));
                    assertTrue(row.contains("Comp Other A"));

                } else if (entry.getName().contains("cultural")) {
                     String row = lines[1];
                     // Verify ALL CRX Fields
                    String expectedProjectLink = "\"=HYPERLINK(\"\"https://example.com/edit-project?projectGuid=" + projectGuid + "\"\", \"\"CRX Beta Project Link\"\")\"";
                    String expectedFiscalLink = "\"=HYPERLINK(\"\"https://example.com/edit-project?projectGuid=" + projectGuid + "&tab=fiscal&fiscalGuid=" + fiscalGuid + "\"\", \"\"Fiscal B Fiscal Activity Link\"\")\"";

                    assertTrue(row.contains(expectedProjectLink), "Row missing correct Project Link: " + expectedProjectLink);
                    assertTrue(row.contains(expectedFiscalLink), "Row missing correct Fiscal Link: " + expectedFiscalLink);

                    assertTrue(row.contains("Cultural Fire"));
                    assertTrue(row.contains("CRX Beta"));
                    assertTrue(row.contains("Region B"));
                    assertTrue(row.contains("District B"));
                    assertTrue(row.contains("Parks Region B"));
                    assertTrue(row.contains("Parks Section B"));
                    assertTrue(row.contains("Fire Centre B"));
                    assertTrue(row.contains("Business Area B"));
                    assertTrue(row.contains("Planning Unit B"));
                    assertTrue(row.contains("234 ha"));
                    assertTrue(row.contains("Community B"));
                    assertTrue(row.contains("Lead B"));
                    assertTrue(row.contains("Proposal B"));
                    assertTrue(row.contains("Fiscal B"));
                    assertTrue(row.contains("Desc B"));
                    assertTrue(row.contains("2025/26"));
                    assertTrue(row.contains("Activity B"));
                    assertTrue(row.contains("Status B"));
                    assertTrue(row.contains("$2,000"));
                    assertTrue(row.contains("$2,100"));
                    assertTrue(row.contains("$600"));
                    assertTrue(row.contains("Provider B"));
                    assertTrue(row.contains("$1,900"));
                    assertTrue(row.contains("$1,950"));
                    assertTrue(row.contains("60 ha"));
                    assertTrue(row.contains("50 ha"));
                    assertTrue(row.contains("\"=\"\"1/2\"\"\""));
                    assertTrue(row.contains("N"));
                    assertTrue(row.contains("Y"));
                    assertTrue(row.contains("FN Partner B"));
                    assertTrue(row.contains("Other Partner B"));
                    assertTrue(row.contains("CFS-B"));
                    assertTrue(row.contains("RES-B"));
                    assertTrue(row.contains("OPEN-B"));
                    assertTrue(row.contains("Obj C"));
                    assertTrue(row.contains("Obj D"));
                    assertTrue(row.contains("2025-03-01"));
                    assertTrue(row.contains("2025-04-01"));
                    assertTrue(row.contains("Y"));
                    assertTrue(row.contains("Risk B"));
                    assertTrue(row.contains("Local Risk B"));
                    assertTrue(row.contains("5"));
                    assertTrue(row.contains("RCL Comment B"));
                    assertTrue(row.contains("15"));
                    assertTrue(row.contains("BDF Comment B"));
                    assertTrue(row.contains("25"));
                    assertTrue(row.contains("Coll Imp Comment B"));
                    assertTrue(row.contains("45"));
                    assertTrue(row.contains("2025-04-20"));
                    assertTrue(row.contains("ON_TRACK"));
                    assertTrue(row.contains("Q1 Comment B"));
                    assertTrue(row.contains("$300"));
                    assertTrue(row.contains("$30"));
                    assertTrue(row.contains("Adj Q1 B"));
                    assertTrue(row.contains("$31"));
                    assertTrue(row.contains("High Q1 B"));
                    assertTrue(row.contains("$32"));
                    assertTrue(row.contains("Med Q1 B"));
                    assertTrue(row.contains("$33"));
                    assertTrue(row.contains("Low Q1 B"));
                    assertTrue(row.contains("$34"));
                    assertTrue(row.contains("Comp Q1 B"));
                    assertTrue(row.contains("2025-07-20"));
                    assertTrue(row.contains("DELAYED"));
                    assertTrue(row.contains("Q2 Comment B"));
                    assertTrue(row.contains("$302"));
                    assertTrue(row.contains("$30"));
                    assertTrue(row.contains("Adj Q2 B"));
                    assertTrue(row.contains("$31"));
                    assertTrue(row.contains("High Q2 B"));
                    assertTrue(row.contains("$32"));
                    assertTrue(row.contains("Med Q2 B"));
                    assertTrue(row.contains("$33"));
                    assertTrue(row.contains("Low Q2 B"));
                    assertTrue(row.contains("$34"));
                    assertTrue(row.contains("Comp Q2 B"));
                    assertTrue(row.contains("2025-10-20"));
                    assertTrue(row.contains("At Risk"));
                    assertTrue(row.contains("Q3 Comment B"));
                    assertTrue(row.contains("$303"));
                    assertTrue(row.contains("$30"));
                    assertTrue(row.contains("Adj Q3 B"));
                    assertTrue(row.contains("$31"));
                    assertTrue(row.contains("High Q3 B"));
                    assertTrue(row.contains("$32"));
                    assertTrue(row.contains("Med Q3 B"));
                    assertTrue(row.contains("$33"));
                    assertTrue(row.contains("Low Q3 B"));
                    assertTrue(row.contains("$34"));
                    assertTrue(row.contains("Comp Q3 B"));
                    assertTrue(row.contains("2026-03-07"));
                    assertTrue(row.contains("Complete"));
                    assertTrue(row.contains("Mar7 Comment B"));
                    assertTrue(row.contains("$304"));
                    assertTrue(row.contains("$30"));
                    assertTrue(row.contains("Adj Mar7 B"));
                    assertTrue(row.contains("$31"));
                    assertTrue(row.contains("High Mar7 B"));
                    assertTrue(row.contains("$32"));
                    assertTrue(row.contains("Med Mar7 B"));
                    assertTrue(row.contains("$33"));
                    assertTrue(row.contains("Low Mar7 B"));
                    assertTrue(row.contains("$34"));
                    assertTrue(row.contains("Comp Mar7 B"));
                    assertTrue(row.contains("2026-05-20"));
                    assertTrue(row.contains("Post-Project"));
                    assertTrue(row.contains("Other Comment B"));
                    assertTrue(row.contains("$305"));
                    assertTrue(row.contains("$30"));
                    assertTrue(row.contains("Adj Other B"));
                    assertTrue(row.contains("$31"));
                    assertTrue(row.contains("High Other B"));
                    assertTrue(row.contains("$32"));
                    assertTrue(row.contains("Med Other B"));
                    assertTrue(row.contains("$33"));
                    assertTrue(row.contains("Low Other B"));
                    assertTrue(row.contains("$34"));
                    assertTrue(row.contains("Comp Other B"));
                }
            }
        }
    }

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Field f = target.getClass().getDeclaredField(fieldName);
        f.setAccessible(true);
        f.set(target, value);
    }

    private static Set<String> zipEntries(byte[] zipBytes) throws IOException {
        Set<String> names = new HashSet<>();
        try (ZipInputStream zin = new ZipInputStream(new ByteArrayInputStream(zipBytes))) {
            ZipEntry e;
            while ((e = zin.getNextEntry()) != null) {
                names.add(e.getName());
            }
        }
        return names;
    }

    private static ReportRequestModel requestWithProjects(List<ReportRequestModel.Project> projects) {
        ReportRequestModel req = new ReportRequestModel();
        req.setProjects(projects);
        return req;
    }

    private static ReportRequestModel.Project project(UUID projectGuid, List<UUID> fiscalsOrNull) {
        ReportRequestModel.Project p = new ReportRequestModel.Project();
        p.setProjectGuid(projectGuid);
        if (fiscalsOrNull != null) {
            p.setProjectFiscalGuids(fiscalsOrNull);
        }
        return p;
    }

    private static FuelManagementReportEntity fuel(UUID projectGuid, UUID fiscalGuid, String name) {
        FuelManagementReportEntity e = new FuelManagementReportEntity();
        e.setUniqueRowGuid(UUID.randomUUID());
        e.setProjectGuid(projectGuid);
        e.setProjectPlanFiscalGuid(fiscalGuid);
        e.setProjectName(name);
        e.setProjectFiscalName("Fiscal " + name);
        e.setFiscalYear("2025");
        return e;
    }

    private static CulturalPrescribedFireReportEntity crx(UUID projectGuid, UUID fiscalGuid, String name) {
        CulturalPrescribedFireReportEntity e = new CulturalPrescribedFireReportEntity();
        e.setUniqueRowGuid(UUID.randomUUID());
        e.setProjectGuid(projectGuid);
        e.setProjectPlanFiscalGuid(fiscalGuid);
        e.setProjectName(name);
        e.setProjectFiscalName("Fiscal " + name);
        e.setFiscalYear("2025");
        return e;
    }

    private static AutoCloseable start(HttpServer server) {
        server.start();
        return () -> server.stop(0);
    }

    private static String lambdaResponseWithSingleFile(String filename, String base64) {
        return "{\n" +
                "  \"files\": [\n" +
                "    {\n" +
                "      \"filename\": \"" + filename + "\",\n" +
                "      \"content\": \"" + base64 + "\"\n" +
                "    }\n" +
                "  ]\n" +
                "}";
    }

}
