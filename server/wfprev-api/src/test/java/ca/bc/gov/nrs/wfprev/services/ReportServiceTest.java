package ca.bc.gov.nrs.wfprev.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ca.bc.gov.nrs.wfprev.common.exceptions.ServiceException;
import ca.bc.gov.nrs.wfprev.data.entities.CulturalPrescribedFireReportEntity;
import ca.bc.gov.nrs.wfprev.data.entities.FuelManagementReportEntity;
import ca.bc.gov.nrs.wfprev.data.repositories.CulturalPrescribedFireReportRepository;
import ca.bc.gov.nrs.wfprev.data.repositories.FuelManagementReportRepository;
import ca.bc.gov.nrs.wfprev.data.repositories.ProgramAreaRepository;
import ca.bc.gov.nrs.wfprev.data.repositories.ProjectRepository;

class ReportServiceTest {

	private FuelManagementReportRepository fuelRepo;
	private CulturalPrescribedFireReportRepository crxRepo;
	private ProjectRepository projectRepo;
	private ProgramAreaRepository programAreaRepo;
	private ReportService reportService;

	@BeforeEach
	void setup() {
		fuelRepo = mock(FuelManagementReportRepository.class);
		crxRepo = mock(CulturalPrescribedFireReportRepository.class);
		projectRepo = mock(ProjectRepository.class);
		programAreaRepo = mock(ProgramAreaRepository.class);
		reportService = new ReportService(fuelRepo, crxRepo, projectRepo, programAreaRepo);
	}

	@Test
	void exportXlsx_throwsIfNoEnvVar() {
		List<UUID> guids = List.of(UUID.randomUUID());
		OutputStream out = new ByteArrayOutputStream();
		Exception ex = assertThrows(ServiceException.class, () -> reportService.exportXlsx(guids, out, "RID"));
		assertTrue(ex.getMessage().contains("REPORT_GENERATOR_LAMBDA_URL"));
	}

	@Test
	void writeCsvZipFromEntities_outputsValidZip() throws Exception {
		UUID projectGuid = UUID.randomUUID();
		FuelManagementReportEntity fuel = new FuelManagementReportEntity();
		fuel.setProjectGuid(projectGuid);
		fuel.setProjectTypeDescription("FUEL_MGMT");
		fuel.setProjectName("Alpha Project");
		fuel.setGrossProjectAreaHa(BigDecimal.valueOf(123.45));
		fuel.setFiscalYear("2025");
		fuel.setTotalEstimatedCostAmount(BigDecimal.valueOf(1234567));
		fuel.setResultsOpeningId("OPEN-1");
		fuel.setPrimaryObjectiveTypeDescription("Primary F");

		CulturalPrescribedFireReportEntity crx = new CulturalPrescribedFireReportEntity();
		crx.setProjectGuid(projectGuid);
		crx.setProjectTypeDescription("CRX");
		crx.setProjectName("Beta Project");
		crx.setGrossProjectAreaHa(BigDecimal.valueOf(987.8));
		crx.setFiscalYear("2025");
		crx.setTotalEstimatedCostAmount(BigDecimal.valueOf(2000000));
		crx.setPrimaryObjectiveTypeDescription("Primary C");
		crx.setSecondaryObjectiveTypeDescription("Secondary C");
		crx.setOutsideWuiInd(true);
		crx.setWuiRiskClassDescription("WUI Class");
		crx.setLocalWuiRiskClassDescription("Local WUI");

		when(fuelRepo.findByProjectGuidIn(anyList())).thenReturn(List.of(fuel));
		when(crxRepo.findByProjectGuidIn(anyList())).thenReturn(List.of(crx));

		ByteArrayOutputStream zipOut = new ByteArrayOutputStream();
		reportService.writeCsvZipFromEntities(List.of(projectGuid), zipOut);

		Map<String, List<String>> csvs = unzipToFirstDataLine(zipOut.toByteArray());
		assertTrue(csvs.containsKey("fuel-management-projects.csv"));
		assertTrue(csvs.containsKey("cultural-prescribed-fire-projects.csv"));
		assertEquals("Alpha Project", unquote(csvs.get("fuel-management-projects.csv").get(3)));
		assertEquals("Beta Project", unquote(csvs.get("cultural-prescribed-fire-projects.csv").get(3)));
	}

	@Test
	void writeCsvZipFromEntities_noData_throws() {
		when(fuelRepo.findByProjectGuidIn(anyList())).thenReturn(List.of());
		when(crxRepo.findByProjectGuidIn(anyList())).thenReturn(List.of());
		ByteArrayOutputStream zipOut = new ByteArrayOutputStream();
		Exception ex = assertThrows(ServiceException.class, () -> reportService.writeCsvZipFromEntities(List.of(UUID.randomUUID()), zipOut));
		assertTrue(ex.getMessage().contains("Failed to generate CSV report"));
	}

  private static Map<String, List<String>> unzipToFirstDataLine(byte[] zipBytes) throws Exception {
    try (var zis = new ZipInputStream(new java.io.ByteArrayInputStream(zipBytes))) {
      Map<String, List<String>> csvs = new java.util.HashMap<>();
      ZipEntry entry;
      while ((entry = zis.getNextEntry()) != null) {
        String name = entry.getName();
        java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(zis));
        List<String> lines = reader.lines().toList();
        if (lines.size() < 2) {
          throw new IllegalStateException("Expected at least 2 lines in CSV " + name);
        }
        // Keep only header and first data line
        csvs.put(name, Arrays.asList(lines.get(0), lines.get(1), lines.get(2), lines.get(3)));
      }
      return csvs;
    }
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
