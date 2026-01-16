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

        setField(service, "baseUrl", "https://example.gov.bc.ca");
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
