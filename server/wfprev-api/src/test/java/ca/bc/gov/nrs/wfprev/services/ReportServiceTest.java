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

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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

        CulturalPrescribedFireReportEntity crx = new CulturalPrescribedFireReportEntity();
        crx.setProjectGuid(projectGuid);
        crx.setProjectPlanFiscalGuid(fiscalGuid);
        crx.setProgramAreaGuid(UUID.randomUUID());

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
    void testWriteCsvZipFromEntities_Success()  {
        UUID projectGuid = UUID.randomUUID();
        UUID fiscalGuid = UUID.randomUUID();

        FuelManagementReportEntity fuel = new FuelManagementReportEntity();
        fuel.setProjectGuid(projectGuid);
        fuel.setProjectPlanFiscalGuid(fiscalGuid);
        fuel.setProgramAreaGuid(UUID.randomUUID());

        CulturalPrescribedFireReportEntity crx = new CulturalPrescribedFireReportEntity();
        crx.setProjectGuid(projectGuid);
        crx.setProjectPlanFiscalGuid(fiscalGuid);
        crx.setProgramAreaGuid(UUID.randomUUID());

        ProgramAreaEntity pa = new ProgramAreaEntity();
        pa.setProgramAreaName("Business Area");

        when(fuelRepo.findByProjectGuidIn(List.of(projectGuid))).thenReturn(List.of(fuel));
        when(crxRepo.findByProjectGuidIn(List.of(projectGuid))).thenReturn(List.of(crx));
        when(programAreaRepo.findById(any())).thenReturn(Optional.of(pa));

        ByteArrayOutputStream zipOut = new ByteArrayOutputStream();
        reportService.writeCsvZipFromEntities(List.of(projectGuid), zipOut);

        assertTrue(zipOut.size() > 0);
    }
}
