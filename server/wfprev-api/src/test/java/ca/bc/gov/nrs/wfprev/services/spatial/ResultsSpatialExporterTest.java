package ca.bc.gov.nrs.wfprev.services.spatial;

import ca.bc.gov.nrs.wfprev.data.entities.ResultsCulturalPrescribedFireReportEntity;
import ca.bc.gov.nrs.wfprev.data.entities.ResultsFuelManagementReportEntity;
import ca.bc.gov.nrs.wfprev.data.repositories.ActivityBoundaryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.io.WKBWriter;
import org.locationtech.jts.io.WKTReader;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ResultsSpatialExporterTest {

    private ActivityBoundaryRepository repository;
    private ResultsSpatialExporter exporter;

    private final UUID projectA = UUID.randomUUID();
    private final UUID projectB = UUID.randomUUID();
    private ResultsFuelManagementReportEntity fuelA;
    private ResultsFuelManagementReportEntity fuelNoFiles;
    private ResultsCulturalPrescribedFireReportEntity crxB;

    @BeforeEach
    void setup() {
        repository = mock(ActivityBoundaryRepository.class);
        exporter = new ResultsSpatialExporter(repository);

        fuelA = new ResultsFuelManagementReportEntity();
        fuelA.setProjectGuid(projectA);
        fuelA.setProjectName("Alpha");
        fuelA.setProjectPlanFiscalGuid(UUID.randomUUID());
        fuelA.setFiscalYear("2025/26");
        fuelA.setProjectFiscalName("Treatments");
        fuelA.setActivityGuid(UUID.randomUUID());
        fuelA.setActivityName("Burn");
        fuelA.setResultsOpeningId("OPEN-1");
        fuelA.setActivityShapeFileName("Area.kml");

        fuelNoFiles = new ResultsFuelManagementReportEntity();
        fuelNoFiles.setProjectGuid(projectA);
        fuelNoFiles.setProjectName("Alpha");
        fuelNoFiles.setProjectPlanFiscalGuid(fuelA.getProjectPlanFiscalGuid());
        fuelNoFiles.setFiscalYear("2025/26");
        fuelNoFiles.setProjectFiscalName("Treatments");
        fuelNoFiles.setActivityGuid(UUID.randomUUID());
        fuelNoFiles.setActivityName("Mulch");

        crxB = new ResultsCulturalPrescribedFireReportEntity();
        crxB.setProjectGuid(projectB);
        crxB.setProjectName("Bravo");
        crxB.setProjectPlanFiscalGuid(UUID.randomUUID());
        crxB.setFiscalYear("2024/25");
        crxB.setProjectFiscalName("Cultural burn");
        crxB.setActivityGuid(UUID.randomUUID());
        crxB.setActivityName("Burn");
    }

    private static Object[] fileRow(UUID activityGuid, String documentPath) {
        return new Object[]{activityGuid, UUID.randomUUID(), documentPath};
    }

    private static Object[] geometryRow(UUID activityGuid, String documentPath, String wkt) throws Exception {
        byte[] wkb = new WKBWriter().write(new WKTReader().read(wkt));
        return new Object[]{activityGuid, UUID.randomUUID(), documentPath, new BigDecimal("1.2345"), wkb};
    }

    @Test
    void applyFileNames_listsEveryExportedNameOfEachActivity() {
        when(repository.findResultsSpatialFiles(any())).thenReturn(List.of(
                fileRow(fuelA.getActivityGuid(), "Area.kml"),
                fileRow(fuelA.getActivityGuid(), "Area.kmz"),
                fileRow(crxB.getActivityGuid(), "Area.kml")));

        exporter.applyFileNames(List.of(fuelA, fuelNoFiles), List.of(crxB));

        assertEquals("Area.shp\nArea_1.shp", fuelA.getActivityShapeFileName());
        assertEquals("Area_2.shp", crxB.getActivityShapeFileName());
        assertNull(fuelNoFiles.getActivityShapeFileName());
    }

    @Test
    void applyFileNames_queriesEachActivityOnce() {
        when(repository.findResultsSpatialFiles(any())).thenReturn(List.of());

        exporter.applyFileNames(List.of(fuelA, fuelNoFiles), List.of(crxB));

        verify(repository).findResultsSpatialFiles(org.mockito.ArgumentMatchers.argThat(guids ->
                List.of(guids).equals(List.of(fuelA.getActivityGuid(), fuelNoFiles.getActivityGuid(), crxB.getActivityGuid()))));
    }

    @Test
    void applyFileNames_noRows_doesNotQuery() {
        exporter.applyFileNames(List.of(), List.of());

        verify(repository, never()).findResultsSpatialFiles(any());
    }

    @Test
    void writeZip_writesOneShapefilePerFileInProjectFiscalActivityFolders() throws Exception {
        String square = "MULTIPOLYGON(((1000000 500000, 1000000 500100, 1000100 500100, 1000100 500000, 1000000 500000)))";
        when(repository.findResultsSpatialFilesWithGeometry(any())).thenReturn(List.of(
                geometryRow(fuelA.getActivityGuid(), "Area.kml", square),
                geometryRow(crxB.getActivityGuid(), "Area.kml", "POLYGON((0 0, 0 1, 1 1, 1 0, 0 0))")));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        assertTrue(exporter.writeZip(List.of(fuelA, fuelNoFiles), List.of(crxB), out));

        Map<String, byte[]> entries = unzip(out.toByteArray());
        assertEquals(List.of(
                "Alpha/2025-26 Treatments/Burn/Area.shp",
                "Alpha/2025-26 Treatments/Burn/Area.shx",
                "Alpha/2025-26 Treatments/Burn/Area.dbf",
                "Alpha/2025-26 Treatments/Burn/Area.prj",
                "Alpha/2025-26 Treatments/Burn/Area.cpg",
                "Bravo/2024-25 Cultural burn/Burn/Area_1.shp",
                "Bravo/2024-25 Cultural burn/Burn/Area_1.shx",
                "Bravo/2024-25 Cultural burn/Burn/Area_1.dbf",
                "Bravo/2024-25 Cultural burn/Burn/Area_1.prj",
                "Bravo/2024-25 Cultural burn/Burn/Area_1.cpg"), List.copyOf(entries.keySet()));

        byte[] shp = entries.get("Alpha/2025-26 Treatments/Burn/Area.shp");
        ByteBuffer header = ByteBuffer.wrap(shp).order(ByteOrder.LITTLE_ENDIAN);
        assertEquals(5, header.getInt(32));
        assertEquals(1000000.0, header.getDouble(36));
        assertEquals(500100.0, header.getDouble(60));

        // A Polygon from the database is written as a one-part MultiPolygon, not dropped.
        byte[] polygonShp = entries.get("Bravo/2024-25 Cultural burn/Burn/Area_1.shp");
        assertEquals(5, ByteBuffer.wrap(polygonShp).order(ByteOrder.LITTLE_ENDIAN).getInt(108));

        String dbf = new String(entries.get("Alpha/2025-26 Treatments/Burn/Area.dbf"), StandardCharsets.UTF_8);
        assertTrue(dbf.contains("Alpha"));
        assertTrue(dbf.contains("2025/26"));
        assertTrue(dbf.contains("Burn"));
        assertTrue(dbf.contains("OPEN-1"));
        assertTrue(dbf.contains("1.2345"));
        assertTrue(dbf.contains("Area.kml"));

        assertTrue(new String(entries.get("Alpha/2025-26 Treatments/Burn/Area.prj"), StandardCharsets.US_ASCII)
                .contains("BC_Environment_Albers"));
    }

    @Test
    void writeZip_namesMatchApplyFileNames() throws Exception {
        String square = "MULTIPOLYGON(((0 0, 0 1, 1 1, 1 0, 0 0)))";
        Object[] a1 = geometryRow(fuelA.getActivityGuid(), "x.kml", square);
        Object[] a2 = geometryRow(fuelA.getActivityGuid(), "X.kmz", square);
        Object[] b1 = geometryRow(crxB.getActivityGuid(), "x.kml", square);
        when(repository.findResultsSpatialFilesWithGeometry(any())).thenReturn(List.of(a1, a2, b1));
        when(repository.findResultsSpatialFiles(any())).thenReturn(List.of(
                new Object[]{a1[0], a1[1], a1[2]}, new Object[]{a2[0], a2[1], a2[2]}, new Object[]{b1[0], b1[1], b1[2]}));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        exporter.writeZip(List.of(fuelA), List.of(crxB), out);
        exporter.applyFileNames(List.of(fuelA), List.of(crxB));

        List<String> shapefiles = unzip(out.toByteArray()).keySet().stream()
                .filter(name -> name.endsWith(".shp"))
                .map(name -> name.substring(name.lastIndexOf('/') + 1))
                .toList();
        List<String> listed = new java.util.ArrayList<>();
        listed.addAll(List.of(fuelA.getActivityShapeFileName().split("\n")));
        listed.addAll(List.of(crxB.getActivityShapeFileName().split("\n")));
        assertEquals(shapefiles, listed);
    }

    @Test
    void writeZip_noSpatialFiles_writesNothing() throws Exception {
        when(repository.findResultsSpatialFilesWithGeometry(any())).thenReturn(List.of());

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        assertFalse(exporter.writeZip(List.of(fuelA), List.of(crxB), out));
        assertEquals(0, out.size());
    }

    @Test
    void writeZip_rowsWithoutActivityGuid_areIgnored() throws Exception {
        fuelA.setActivityGuid(null);

        assertFalse(exporter.writeZip(List.of(fuelA), List.of(), new ByteArrayOutputStream()));
        verify(repository, never()).findResultsSpatialFilesWithGeometry(any());
    }

    private static Map<String, byte[]> unzip(byte[] zip) throws Exception {
        Map<String, byte[]> entries = new LinkedHashMap<>();
        try (ZipInputStream in = new ZipInputStream(new ByteArrayInputStream(zip))) {
            ZipEntry entry;
            while ((entry = in.getNextEntry()) != null) {
                entries.put(entry.getName(), in.readAllBytes());
            }
        }
        return entries;
    }
}
