package ca.bc.gov.nrs.wfprev.services.spatial;

import ca.bc.gov.nrs.wfprev.data.entities.ResultsCulturalPrescribedFireReportEntity;
import ca.bc.gov.nrs.wfprev.data.entities.ResultsFuelManagementReportEntity;
import ca.bc.gov.nrs.wfprev.data.repositories.ActivityBoundaryRepository;
import ca.bc.gov.nrs.wfprev.services.spatial.ShapefileWriter.Feature;
import ca.bc.gov.nrs.wfprev.services.spatial.ShapefileWriter.Field;
import ca.bc.gov.nrs.wfprev.services.spatial.SpatialFileNamer.Activity;
import ca.bc.gov.nrs.wfprev.services.spatial.SpatialFileNamer.NamedFile;
import ca.bc.gov.nrs.wfprev.services.spatial.SpatialFileNamer.SpatialFile;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKBReader;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * The spatial side of the RESULTS export: the activity spatial file names shown in the workbook, and the
 * ZIP of Shapefiles (EPSG:3005) downloaded next to it. Both come from {@link SpatialFileNamer}, so the names
 * in the workbook match the files in the ZIP.
 */
@Slf4j
@Component
public class ResultsSpatialExporter {

    static final List<Field> FIELDS = List.of(
            Field.text("PROJ_NAME", 254),
            Field.text("FISCAL_YR", 7),
            Field.text("ACTV_NAME", 254),
            Field.text("OPENING_ID", 50),
            Field.number("AREA_HA", 19, 4),
            Field.text("SRC_FILE", 254));

    private final ActivityBoundaryRepository activityBoundaryRepository;

    public ResultsSpatialExporter(ActivityBoundaryRepository activityBoundaryRepository) {
        this.activityBoundaryRepository = activityBoundaryRepository;
    }

    /**
     * Sets each row's activity spatial file name to the exported names of all of its files, one per line,
     * or to null when the activity has none.
     */
    public void applyFileNames(List<ResultsFuelManagementReportEntity> fuel,
                               List<ResultsCulturalPrescribedFireReportEntity> crx) {
        List<ExportRow> rows = exportRows(fuel, crx);
        if (rows.isEmpty()) {
            return;
        }

        List<SpatialFile> files = new ArrayList<>();
        for (Object[] row : activityBoundaryRepository.findResultsSpatialFiles(activityGuids(rows))) {
            files.add(new SpatialFile(uuid(row[0]), uuid(row[1]), (String) row[2]));
        }

        Map<UUID, List<String>> namesByActivity = new HashMap<>();
        for (NamedFile named : SpatialFileNamer.assign(activities(rows), files)) {
            namesByActivity.computeIfAbsent(named.activity().activityGuid(), k -> new ArrayList<>())
                    .add(named.shapefileName());
        }

        for (ExportRow row : rows) {
            List<String> names = namesByActivity.get(row.activity().activityGuid());
            row.fileNameSetter().set(names == null ? null : String.join("\n", names));
        }
    }

    /**
     * Writes the ZIP of Shapefiles for the rows' activities.
     *
     * @return false, having written nothing, when none of the activities has a spatial file
     */
    public boolean writeZip(List<ResultsFuelManagementReportEntity> fuel,
                            List<ResultsCulturalPrescribedFireReportEntity> crx,
                            OutputStream outputStream) throws IOException {
        List<ExportRow> rows = exportRows(fuel, crx);
        if (rows.isEmpty()) {
            return false;
        }

        List<SpatialFile> files = new ArrayList<>();
        Map<SpatialFile, Object[]> dataByFile = new IdentityHashMap<>();
        for (Object[] row : activityBoundaryRepository.findResultsSpatialFilesWithGeometry(activityGuids(rows))) {
            SpatialFile file = new SpatialFile(uuid(row[0]), uuid(row[1]), (String) row[2]);
            files.add(file);
            dataByFile.put(file, row);
        }

        List<NamedFile> namedFiles = SpatialFileNamer.assign(activities(rows), files);
        if (namedFiles.isEmpty()) {
            return false;
        }

        Map<UUID, String> openingIdByActivity = new HashMap<>();
        rows.forEach(r -> openingIdByActivity.putIfAbsent(r.activity().activityGuid(), r.openingId()));

        WKBReader wkbReader = new WKBReader();
        ZipOutputStream zip = new ZipOutputStream(outputStream);
        for (NamedFile named : namedFiles) {
            Object[] data = dataByFile.get(named.file());
            Activity activity = named.activity();
            List<Object> values = Arrays.asList(
                    activity.projectName(),
                    activity.fiscalYear(),
                    activity.activityName(),
                    openingIdByActivity.get(activity.activityGuid()),
                    data[3] == null ? null : new BigDecimal(data[3].toString()),
                    named.file().documentPath());
            Feature feature = new Feature(multiPolygon((byte[]) data[4], wkbReader, named), values);

            ShapefileWriter.Parts parts = ShapefileWriter.write(FIELDS, List.of(feature), ShapefileWriter.BC_ALBERS_PRJ);
            for (ShapefileWriter.Part part : parts.asList()) {
                zip.putNextEntry(new ZipEntry(named.zipPath(part.extension())));
                zip.write(part.content());
                zip.closeEntry();
            }
        }
        zip.finish();
        log.info("RESULTS spatial export: {} Shapefiles for {} rows", namedFiles.size(), rows.size());
        return true;
    }

    private static MultiPolygon multiPolygon(byte[] wkb, WKBReader reader, NamedFile named) {
        if (wkb == null) {
            return null;
        }
        try {
            Geometry geometry = reader.read(wkb);
            if (geometry instanceof MultiPolygon multiPolygon) {
                return multiPolygon;
            }
            if (geometry instanceof Polygon polygon) {
                return geometry.getFactory().createMultiPolygon(new Polygon[]{polygon});
            }
            log.warn("RESULTS spatial export: {} is a {}, written without geometry",
                    named.zipPath(SpatialFileNamer.SHAPEFILE_EXTENSION), geometry.getGeometryType());
        } catch (ParseException e) {
            log.warn("RESULTS spatial export: unreadable geometry for {}, written without geometry",
                    named.zipPath(SpatialFileNamer.SHAPEFILE_EXTENSION), e);
        }
        return null;
    }

    @FunctionalInterface
    private interface FileNameSetter {
        void set(String fileNames);
    }

    private record ExportRow(Activity activity, String openingId, FileNameSetter fileNameSetter) {
    }

    private static List<ExportRow> exportRows(List<ResultsFuelManagementReportEntity> fuel,
                                              List<ResultsCulturalPrescribedFireReportEntity> crx) {
        List<ExportRow> rows = new ArrayList<>();
        for (ResultsFuelManagementReportEntity e : fuel) {
            if (e.getActivityGuid() != null) {
                rows.add(new ExportRow(new Activity(e.getProjectGuid(), e.getProjectName(),
                        e.getProjectPlanFiscalGuid(), e.getFiscalYear(), e.getProjectFiscalName(),
                        e.getActivityGuid(), e.getActivityName()),
                        e.getResultsOpeningId(), e::setActivityShapeFileName));
            }
        }
        for (ResultsCulturalPrescribedFireReportEntity e : crx) {
            if (e.getActivityGuid() != null) {
                rows.add(new ExportRow(new Activity(e.getProjectGuid(), e.getProjectName(),
                        e.getProjectPlanFiscalGuid(), e.getFiscalYear(), e.getProjectFiscalName(),
                        e.getActivityGuid(), e.getActivityName()),
                        e.getResultsOpeningId(), e::setActivityShapeFileName));
            }
        }
        return rows;
    }

    private static List<Activity> activities(List<ExportRow> rows) {
        return rows.stream().map(ExportRow::activity).toList();
    }

    private static UUID[] activityGuids(List<ExportRow> rows) {
        return rows.stream().map(r -> r.activity().activityGuid()).distinct().toArray(UUID[]::new);
    }

    private static UUID uuid(Object value) {
        return value instanceof UUID u ? u : UUID.fromString(value.toString());
    }
}
