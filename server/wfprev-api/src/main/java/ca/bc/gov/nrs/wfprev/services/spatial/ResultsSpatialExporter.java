package ca.bc.gov.nrs.wfprev.services.spatial;

import ca.bc.gov.nrs.wfprev.data.entities.ResultsCulturalPrescribedFireReportEntity;
import ca.bc.gov.nrs.wfprev.data.entities.ResultsFuelManagementReportEntity;
import ca.bc.gov.nrs.wfprev.data.repositories.ActivityBoundaryRepository;
import ca.bc.gov.nrs.wfprev.data.repositories.ProjectBoundaryRepository;
import ca.bc.gov.nrs.wfprev.services.spatial.ShapefileWriter.Feature;
import ca.bc.gov.nrs.wfprev.services.spatial.ShapefileWriter.Field;
import ca.bc.gov.nrs.wfprev.services.spatial.SpatialFileNamer.Activity;
import ca.bc.gov.nrs.wfprev.services.spatial.SpatialFileNamer.NamedFile;
import ca.bc.gov.nrs.wfprev.services.spatial.SpatialFileNamer.NamedFiles;
import ca.bc.gov.nrs.wfprev.services.spatial.SpatialFileNamer.NamedProjectFile;
import ca.bc.gov.nrs.wfprev.services.spatial.SpatialFileNamer.Project;
import ca.bc.gov.nrs.wfprev.services.spatial.SpatialFileNamer.ProjectSpatialFile;
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
import java.util.Objects;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * The spatial side of the RESULTS export: the spatial file names shown in the workbook, and the
 * ZIP of Shapefiles (EPSG:3005) downloaded next to it. Both come from {@link SpatialFileNamer}, so the names
 * in the workbook match the files in the ZIP.
 */
@Slf4j
@Component
public class ResultsSpatialExporter {

    static final List<Field> PROJECT_FIELDS = List.of(
            Field.text("PROJ_NAME", 254),
            Field.text("OPENING_ID", 50),
            Field.number("AREA_HA", 19, 4),
            Field.text("SRC_FILE", 254));

    static final List<Field> FIELDS = List.of(
            Field.text("PROJ_NAME", 254),
            Field.text("FISCAL_YR", 7),
            Field.text("ACTV_NAME", 254),
            Field.text("OPENING_ID", 50),
            Field.number("AREA_HA", 19, 4),
            Field.text("SRC_FILE", 254));

    private final ActivityBoundaryRepository activityBoundaryRepository;
    private final ProjectBoundaryRepository projectBoundaryRepository;

    public ResultsSpatialExporter(ActivityBoundaryRepository activityBoundaryRepository,
                                  ProjectBoundaryRepository projectBoundaryRepository) {
        this.activityBoundaryRepository = activityBoundaryRepository;
        this.projectBoundaryRepository = projectBoundaryRepository;
    }

    /**
     * Sets each row's project and activity spatial file names to the exported names of all of their files,
     * one per line, or to null when none exist.
     */
    public void applyFileNames(List<ResultsFuelManagementReportEntity> fuel,
                               List<ResultsCulturalPrescribedFireReportEntity> crx) {
        List<ExportRow> rows = exportRows(fuel, crx);
        if (rows.isEmpty()) {
            return;
        }

        UUID[] projectGuids = projectGuids(rows);
        List<ProjectSpatialFile> projectFiles = new ArrayList<>();
        for (Object[] row : projectBoundaryRepository.findResultsSpatialFiles(projectGuids)) {
            projectFiles.add(new ProjectSpatialFile(uuid(row[0]), uuid(row[1]), (String) row[2]));
        }

        UUID[] activityGuids = activityGuids(rows);
        List<SpatialFile> activityFiles = new ArrayList<>();
        for (Object[] row : activityBoundaryRepository.findResultsSpatialFiles(activityGuids)) {
            activityFiles.add(new SpatialFile(uuid(row[0]), uuid(row[1]), (String) row[2]));
        }

        NamedFiles named = SpatialFileNamer.assign(projects(rows), projectFiles, activities(rows), activityFiles);

        Map<UUID, List<String>> namesByProject = new HashMap<>();
        for (NamedProjectFile namedProject : named.projectFiles()) {
            namesByProject.computeIfAbsent(namedProject.project().projectGuid(), k -> new ArrayList<>())
                    .add(namedProject.shapefileName());
        }

        Map<UUID, List<String>> namesByActivity = new HashMap<>();
        for (NamedFile namedActivity : named.activityFiles()) {
            namesByActivity.computeIfAbsent(namedActivity.activity().activityGuid(), k -> new ArrayList<>())
                    .add(namedActivity.shapefileName());
        }

        for (ExportRow row : rows) {
            List<String> projectNames = namesByProject.get(row.activity().projectGuid());
            row.openingFileNameSetter().set(projectNames == null ? null : String.join("\n", projectNames));

            List<String> activityNames = namesByActivity.get(row.activity().activityGuid());
            row.activityFileNameSetter().set(activityNames == null ? null : String.join("\n", activityNames));
        }
    }

    /**
     * Writes the ZIP of Shapefiles for the rows' projects and activities.
     *
     * @return false, having written nothing, when none of the projects or activities has a spatial file
     */
    public boolean writeZip(List<ResultsFuelManagementReportEntity> fuel,
                            List<ResultsCulturalPrescribedFireReportEntity> crx,
                            OutputStream outputStream) throws IOException {
        List<ExportRow> rows = exportRows(fuel, crx);
        if (rows.isEmpty()) {
            return false;
        }

        UUID[] projectGuids = projectGuids(rows);
        List<ProjectSpatialFile> projectFiles = new ArrayList<>();
        Map<ProjectSpatialFile, Object[]> dataByProjectFile = new IdentityHashMap<>();
        for (Object[] row : projectBoundaryRepository.findResultsSpatialFilesWithGeometry(projectGuids)) {
            ProjectSpatialFile file = new ProjectSpatialFile(uuid(row[0]), uuid(row[1]), (String) row[2]);
            projectFiles.add(file);
            dataByProjectFile.put(file, row);
        }

        UUID[] activityGuids = activityGuids(rows);
        List<SpatialFile> activityFiles = new ArrayList<>();
        Map<SpatialFile, Object[]> dataByActivityFile = new IdentityHashMap<>();
        for (Object[] row : activityBoundaryRepository.findResultsSpatialFilesWithGeometry(activityGuids)) {
            SpatialFile file = new SpatialFile(uuid(row[0]), uuid(row[1]), (String) row[2]);
            activityFiles.add(file);
            dataByActivityFile.put(file, row);
        }

        NamedFiles named = SpatialFileNamer.assign(projects(rows), projectFiles, activities(rows), activityFiles);
        if (named.isEmpty()) {
            return false;
        }

        Map<UUID, String> openingIdByProject = new HashMap<>();
        rows.forEach(r -> openingIdByProject.putIfAbsent(r.activity().projectGuid(), r.openingId()));

        Map<UUID, String> openingIdByActivity = new HashMap<>();
        rows.forEach(r -> openingIdByActivity.putIfAbsent(r.activity().activityGuid(), r.openingId()));

        WKBReader wkbReader = new WKBReader();
        ZipOutputStream zip = new ZipOutputStream(outputStream);

        for (NamedProjectFile namedProject : named.projectFiles()) {
            Object[] data = dataByProjectFile.get(namedProject.file());
            Project project = namedProject.project();
            List<Object> values = Arrays.asList(
                    project.projectName(),
                    openingIdByProject.get(project.projectGuid()),
                    data[3] == null ? null : new BigDecimal(data[3].toString()),
                    namedProject.file().documentPath());
            Feature feature = new Feature(multiPolygon((byte[]) data[4], wkbReader, namedProject.zipPath(SpatialFileNamer.SHAPEFILE_EXTENSION)), values);

            ShapefileWriter.Parts parts = ShapefileWriter.write(PROJECT_FIELDS, List.of(feature), ShapefileWriter.BC_ALBERS_PRJ);
            for (ShapefileWriter.Part part : parts.asList()) {
                zip.putNextEntry(new ZipEntry(namedProject.zipPath(part.extension())));
                zip.write(part.content());
                zip.closeEntry();
            }
        }

        for (NamedFile namedActivity : named.activityFiles()) {
            Object[] data = dataByActivityFile.get(namedActivity.file());
            Activity activity = namedActivity.activity();
            List<Object> values = Arrays.asList(
                    activity.projectName(),
                    activity.fiscalYear(),
                    activity.activityName(),
                    openingIdByActivity.get(activity.activityGuid()),
                    data[3] == null ? null : new BigDecimal(data[3].toString()),
                    namedActivity.file().documentPath());
            Feature feature = new Feature(multiPolygon((byte[]) data[4], wkbReader, namedActivity.zipPath(SpatialFileNamer.SHAPEFILE_EXTENSION)), values);

            ShapefileWriter.Parts parts = ShapefileWriter.write(FIELDS, List.of(feature), ShapefileWriter.BC_ALBERS_PRJ);
            for (ShapefileWriter.Part part : parts.asList()) {
                zip.putNextEntry(new ZipEntry(namedActivity.zipPath(part.extension())));
                zip.write(part.content());
                zip.closeEntry();
            }
        }

        zip.finish();
        int totalShapefiles = named.projectFiles().size() + named.activityFiles().size();
        log.info("RESULTS spatial export: {} Shapefiles for {} rows", totalShapefiles, rows.size());
        return true;
    }

    private static MultiPolygon multiPolygon(byte[] wkb, WKBReader reader, String zipPath) {
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
                    zipPath, geometry.getGeometryType());
        } catch (ParseException e) {
            log.warn("RESULTS spatial export: unreadable geometry for {}, written without geometry",
                    zipPath, e);
        }
        return null;
    }

    @FunctionalInterface
    private interface FileNameSetter {
        void set(String fileNames);
    }

    private record ExportRow(Activity activity, String openingId,
                            FileNameSetter openingFileNameSetter,
                            FileNameSetter activityFileNameSetter) {
    }

    private static List<ExportRow> exportRows(List<ResultsFuelManagementReportEntity> fuel,
                                              List<ResultsCulturalPrescribedFireReportEntity> crx) {
        List<ExportRow> rows = new ArrayList<>();
        for (ResultsFuelManagementReportEntity e : fuel) {
            if (e.getActivityGuid() != null) {
                rows.add(new ExportRow(new Activity(e.getProjectGuid(), e.getProjectName(),
                        e.getProjectPlanFiscalGuid(), e.getFiscalYear(), e.getProjectFiscalName(),
                        e.getActivityGuid(), e.getActivityName()),
                        e.getResultsOpeningId(), e::setOpeningShapeFileName, e::setActivityShapeFileName));
            }
        }
        for (ResultsCulturalPrescribedFireReportEntity e : crx) {
            if (e.getActivityGuid() != null) {
                rows.add(new ExportRow(new Activity(e.getProjectGuid(), e.getProjectName(),
                        e.getProjectPlanFiscalGuid(), e.getFiscalYear(), e.getProjectFiscalName(),
                        e.getActivityGuid(), e.getActivityName()),
                        e.getResultsOpeningId(), e::setOpeningShapeFileName, e::setActivityShapeFileName));
            }
        }
        return rows;
    }

    private static List<Project> projects(List<ExportRow> rows) {
        return rows.stream()
                .map(ExportRow::activity)
                .map(a -> new Project(a.projectGuid(), a.projectName()))
                .distinct()
                .toList();
    }

    private static List<Activity> activities(List<ExportRow> rows) {
        return rows.stream().map(ExportRow::activity).toList();
    }

    private static UUID[] projectGuids(List<ExportRow> rows) {
        return rows.stream().map(r -> r.activity().projectGuid()).filter(Objects::nonNull).distinct().toArray(UUID[]::new);
    }

    private static UUID[] activityGuids(List<ExportRow> rows) {
        return rows.stream().map(r -> r.activity().activityGuid()).distinct().toArray(UUID[]::new);
    }

    private static UUID uuid(Object value) {
        return value instanceof UUID u ? u : UUID.fromString(value.toString());
    }
}
