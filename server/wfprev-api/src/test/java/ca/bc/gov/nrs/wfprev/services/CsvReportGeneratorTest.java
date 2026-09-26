package ca.bc.gov.nrs.wfprev.services;

import ca.bc.gov.nrs.wfprev.data.entities.ProjectCulturalPrescribedFireReportEntity;
import ca.bc.gov.nrs.wfprev.data.entities.ProjectFuelManagementReportEntity;
import ca.bc.gov.nrs.wfprev.data.entities.ResultsCulturalPrescribedFireReportEntity;
import ca.bc.gov.nrs.wfprev.data.entities.ResultsFuelManagementReportEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CsvReportGeneratorTest {

    private CsvReportGenerator generator;

    @BeforeEach
    void setUp() {
        generator = new CsvReportGenerator();
    }

    @Test
    void generateCsvZip_emptyLists_producesEmptyZip() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        generator.generateCsvZip(Collections.emptyList(), Collections.emptyList(), out);

        Set<String> entries = getZipEntries(out.toByteArray());
        assertTrue(entries.isEmpty(), "No entries should be in the zip if both lists are empty");
    }

    @Test
    void generateCsvZip_onlyFuel_producesFuelCsvOnly() throws Exception {
        ProjectFuelManagementReportEntity fuel = new ProjectFuelManagementReportEntity();
        fuel.setUniqueRowGuid(UUID.randomUUID());
        fuel.setProjectName("Test Project");
        fuel.setLinkToProject("https://example.com/project");
        fuel.setFiscalPlannedProjectSizeHa(new BigDecimal("123.456"));
        fuel.setTotalEstimatedCostAmount(new BigDecimal("5000"));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        generator.generateCsvZip(List.of(fuel), Collections.emptyList(), out);

        Set<String> entries = getZipEntries(out.toByteArray());
        assertTrue(entries.contains("fuel-management-projects.csv"));
        assertFalse(entries.contains("cultural-prescribed-fire-projects.csv"));

        String content = getZipEntryContent(out.toByteArray(), "fuel-management-projects.csv");
        assertTrue(content.contains("Link to Project (within Prevention application)"));
        assertTrue(content.contains("Test Project"));
        assertTrue(content.contains("$5,000"));
        assertTrue(content.contains("\"123.456\""));
    }

    @Test
    void generateCsvZip_onlyCrx_producesCrxCsvOnly() throws Exception {
        ProjectCulturalPrescribedFireReportEntity crx = new ProjectCulturalPrescribedFireReportEntity();
        crx.setUniqueRowGuid(UUID.randomUUID());
        crx.setProjectName("CRX Project");
        crx.setLinkToProject("https://example.com/crx");
        crx.setGrossProjectAreaHa(new BigDecimal("78.9"));
        crx.setOutsideWuiInd(true);
        crx.setEndorsementTimestamp(Date.from(Instant.parse("2025-06-01T12:00:00Z")));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        generator.generateCsvZip(Collections.emptyList(), List.of(crx), out);

        Set<String> entries = getZipEntries(out.toByteArray());
        assertFalse(entries.contains("fuel-management-projects.csv"));
        assertTrue(entries.contains("cultural-prescribed-fire-projects.csv"));

        String content = getZipEntryContent(out.toByteArray(), "cultural-prescribed-fire-projects.csv");
        assertTrue(content.contains("Link to Project (within Prevention application)"));
        assertTrue(content.contains("CRX Project"));
        assertTrue(content.contains("\"78.9\""));
        assertTrue(content.contains("2025-06-01"));
        assertTrue(content.contains("Y")); // Outside WUI
    }

    @Test
    void generateCsvZip_bothPresent_producesBothCsvs() throws Exception {
        ProjectFuelManagementReportEntity fuel = new ProjectFuelManagementReportEntity();
        fuel.setProjectName("Fuel 1");

        ProjectCulturalPrescribedFireReportEntity crx = new ProjectCulturalPrescribedFireReportEntity();
        crx.setProjectName("CRX 1");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        generator.generateCsvZip(List.of(fuel), List.of(crx), out);

        Set<String> entries = getZipEntries(out.toByteArray());
        assertTrue(entries.contains("fuel-management-projects.csv"));
        assertTrue(entries.contains("cultural-prescribed-fire-projects.csv"));
    }

    @Test
    void generateResultsCsvZip_emptyLists_producesEmptyZip() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        generator.generateResultsCsvZip(Collections.emptyList(), Collections.emptyList(), out);

        Set<String> entries = getZipEntries(out.toByteArray());
        assertTrue(entries.isEmpty(), "No entries should be in the zip if both lists are empty");
    }

    @Test
    void generateResultsCsvZip_onlyFuel_producesFuelCsvOnly() throws Exception {
        ResultsFuelManagementReportEntity fuel = new ResultsFuelManagementReportEntity();
        fuel.setUniqueRowGuid(UUID.randomUUID());
        fuel.setProjectName("Results Fuel Project");
        fuel.setLinkToProject("https://example.com/project");
        fuel.setLinkToFiscalActivity("https://example.com/activity");
        fuel.setActivityName("Piling");
        fuel.setIsResultsReportableInd("Y");
        fuel.setCompletedAreaHa(new BigDecimal("45.65"));
        fuel.setProjectBoundarySizeHa(new BigDecimal("126.468"));
        fuel.setActivityStatusName("Completed");
        fuel.setFiscalYear("2024/25");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        generator.generateResultsCsvZip(List.of(fuel), Collections.emptyList(), out);

        Set<String> entries = getZipEntries(out.toByteArray());
        assertTrue(entries.contains("results-fuel-management-projects.csv"));
        assertFalse(entries.contains("results-cultural-prescribed-fire-projects.csv"));

        String content = getZipEntryContent(out.toByteArray(), "results-fuel-management-projects.csv");
        assertTrue(content.contains("Link to Project (within within ReMi Planner)"));
        assertTrue(content.contains("Link to Fiscal Activity (within ReMi Planner)"));
        assertTrue(content.contains("Results Fuel Project"));
        assertTrue(content.contains("Piling"));
        // Opening Area and Treatment Area are exported to one decimal place, rounding half up
        assertTrue(content.contains("\"45.7\""));
        assertTrue(content.contains("\"126.5\""));
        assertTrue(content.contains("2024/25"));
    }

    @Test
    void generateResultsCsvZip_onlyCrx_producesCrxCsvOnly() throws Exception {
        ResultsCulturalPrescribedFireReportEntity crx = new ResultsCulturalPrescribedFireReportEntity();
        crx.setUniqueRowGuid(UUID.randomUUID());
        crx.setProjectName("Results CRX Project");
        crx.setLinkToProject("https://example.com/project");
        crx.setLinkToFiscalActivity("https://example.com/activity");
        crx.setActivityName("Broadcast Burn");
        crx.setIsResultsReportableInd("Y");
        crx.setCompletedAreaHa(new BigDecimal("89.12"));
        crx.setProjectBoundarySizeHa(new BigDecimal("3"));
        crx.setActivityStatusName("Completed");
        crx.setFiscalYear("2024/25");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        generator.generateResultsCsvZip(Collections.emptyList(), List.of(crx), out);

        Set<String> entries = getZipEntries(out.toByteArray());
        assertFalse(entries.contains("results-fuel-management-projects.csv"));
        assertTrue(entries.contains("results-cultural-prescribed-fire-projects.csv"));

        String content = getZipEntryContent(out.toByteArray(), "results-cultural-prescribed-fire-projects.csv");
        assertTrue(content.contains("Link to Project (within within ReMi Planner)"));
        assertTrue(content.contains("Link to Fiscal Activity (within ReMi Planner)"));
        assertTrue(content.contains("Results CRX Project"));
        assertTrue(content.contains("Broadcast Burn"));
        assertTrue(content.contains("\"89.1\""));
        assertTrue(content.contains("\"3.0\""));
        assertTrue(content.contains("2024/25"));
    }

    @Test
    void generateResultsCsvZip_bothPresent_producesBothCsvs() throws Exception {
        ResultsFuelManagementReportEntity fuel = new ResultsFuelManagementReportEntity();
        fuel.setProjectName("Results Fuel 1");

        ResultsCulturalPrescribedFireReportEntity crx = new ResultsCulturalPrescribedFireReportEntity();
        crx.setProjectName("Results CRX 1");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        generator.generateResultsCsvZip(List.of(fuel), List.of(crx), out);

        Set<String> entries = getZipEntries(out.toByteArray());
        assertTrue(entries.contains("results-fuel-management-projects.csv"));
        assertTrue(entries.contains("results-cultural-prescribed-fire-projects.csv"));
    }

    private static Set<String> getZipEntries(byte[] zipBytes) throws IOException {
        Set<String> names = new HashSet<>();
        try (ZipInputStream zin = new ZipInputStream(new ByteArrayInputStream(zipBytes))) {
            ZipEntry e;
            while ((e = zin.getNextEntry()) != null) {
                names.add(e.getName());
            }
        }
        return names;
    }

    private static String getZipEntryContent(byte[] zipBytes, String entryName) throws IOException {
        try (ZipInputStream zin = new ZipInputStream(new ByteArrayInputStream(zipBytes))) {
            ZipEntry e;
            while ((e = zin.getNextEntry()) != null) {
                if (e.getName().equals(entryName)) {
                    return new String(zin.readAllBytes(), StandardCharsets.UTF_8);
                }
            }
        }
        return null;
    }
}
