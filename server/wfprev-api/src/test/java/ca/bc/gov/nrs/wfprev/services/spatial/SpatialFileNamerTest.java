package ca.bc.gov.nrs.wfprev.services.spatial;

import ca.bc.gov.nrs.wfprev.services.spatial.SpatialFileNamer.Activity;
import ca.bc.gov.nrs.wfprev.services.spatial.SpatialFileNamer.NamedFile;
import ca.bc.gov.nrs.wfprev.services.spatial.SpatialFileNamer.SpatialFile;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SpatialFileNamerTest {

    private static Activity activity(String project, String fiscalYear, String fiscalName, String activityName) {
        return new Activity(guid("p" + project), project, guid("f" + project + fiscalYear + fiscalName),
                fiscalYear, fiscalName, guid("a" + project + fiscalYear + fiscalName + activityName), activityName);
    }

    private static UUID guid(String seed) {
        return UUID.nameUUIDFromBytes(seed.getBytes());
    }

    private static SpatialFile file(Activity activity, String documentPath) {
        return new SpatialFile(activity.activityGuid(), UUID.randomUUID(), documentPath);
    }

    private static List<String> paths(List<NamedFile> named) {
        return named.stream().map(n -> n.zipPath(".shp")).toList();
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
            "Fuel Treatment Area_4109638.kml     | Fuel Treatment Area_4109638",
            "Fuel Treatment Area_4109638 (1).KMZ | Fuel Treatment Area_4109638 (1)",
            "3891602.zip                         | 3891602",
            "boundary.shp.zip                    | boundary",
            "treatments.gdb.zip                  | treatments",
            "notes.geojson                       | notes",
            "no_extension                        | no_extension",
            "C:\\\\uploads\\\\area.kml           | area",
            "a:b?c*.kml                          | a_b_c_",
            ".kml                                | spatial_file",
            "'   .kml'                           | spatial_file",
            "CON.kml                             | CON_",
            "trailing dots...kml                 | trailing dots",
    })
    void baseName_keepsTheUploadedNameWithoutItsExtension(String documentPath, String expected) {
        assertEquals(expected, SpatialFileNamer.baseName(documentPath));
    }

    @Test
    void baseName_null_fallsBack() {
        assertEquals("spatial_file", SpatialFileNamer.baseName(null));
    }

    @Test
    void assign_foldersAreProjectFiscalActivity() {
        Activity a = activity("WUI Project", "2025/26", "Treatments", "Chip and Haul");
        List<NamedFile> named = SpatialFileNamer.assign(List.of(a), List.of(file(a, "area.kml")));

        assertEquals(List.of("WUI Project/2025-26 Treatments/Chip and Haul/area.shp"), paths(named));
        assertEquals("area.shp", named.get(0).shapefileName());
    }

    @Test
    void assign_sameNameAcrossProjects_getsExportWideSuffixes() {
        Activity a = activity("Alpha", "2025/26", "F", "Burn");
        Activity b = activity("Bravo", "2025/26", "F", "Burn");
        Activity c = activity("Charlie", "2024/25", "F", "Mulch");
        List<NamedFile> named = SpatialFileNamer.assign(List.of(c, b, a),
                List.of(file(c, "Area.kml"), file(b, "Area.kml"), file(a, "Area.kml")));

        assertEquals(List.of(
                "Alpha/2025-26 F/Burn/Area.shp",
                "Bravo/2025-26 F/Burn/Area_1.shp",
                "Charlie/2024-25 F/Mulch/Area_2.shp"), paths(named));
    }

    @Test
    void assign_clashesIgnoreCase() {
        Activity a = activity("Alpha", "2025/26", "F", "Burn");
        List<NamedFile> named = SpatialFileNamer.assign(List.of(a),
                List.of(file(a, "AREA.kml"), file(a, "area.kmz")));

        assertEquals(List.of("AREA", "area_1"), named.stream().map(NamedFile::baseName).toList());
    }

    @Test
    void assign_suffixSkipsAUserFileWithTheSameName() {
        Activity a = activity("Alpha", "2025/26", "F", "Burn");
        Activity b = activity("Bravo", "2025/26", "F", "Burn");
        Activity c = activity("Charlie", "2025/26", "F", "Burn");

        List<NamedFile> userFileLater = SpatialFileNamer.assign(List.of(a, b, c),
                List.of(file(a, "x.kml"), file(b, "x_1.kml"), file(c, "x.kml")));
        assertEquals(List.of("x", "x_1", "x_2"), userFileLater.stream().map(NamedFile::baseName).toList());

        List<NamedFile> userFileFirst = SpatialFileNamer.assign(List.of(a, b, c),
                List.of(file(a, "x_1.kml"), file(b, "x.kml"), file(c, "x.kml")));
        assertEquals(List.of("x_1", "x", "x_2"), userFileFirst.stream().map(NamedFile::baseName).toList());
    }

    @Test
    void assign_isIndependentOfInputOrder() {
        List<Activity> activities = new ArrayList<>();
        List<SpatialFile> files = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            Activity activity = activity("Project " + (i % 4), "2025/26", "Fiscal " + (i % 2), "Activity " + i);
            activities.add(activity);
            files.add(file(activity, "shared.kml"));
            files.add(file(activity, "own " + i + ".kml"));
        }
        List<String> expected = paths(SpatialFileNamer.assign(activities, files));

        Random random = new Random(42);
        for (int run = 0; run < 5; run++) {
            List<Activity> shuffled = new ArrayList<>(activities);
            Collections.shuffle(shuffled, random);
            // Only the order of files within an activity is significant, so shuffle whole activities' files.
            List<SpatialFile> shuffledFiles = new ArrayList<>();
            for (Activity activity : shuffled) {
                files.stream().filter(f -> f.activityGuid().equals(activity.activityGuid())).forEach(shuffledFiles::add);
            }
            assertEquals(expected, paths(SpatialFileNamer.assign(shuffled, shuffledFiles)));
        }
    }

    @Test
    void assign_projectsWithTheSameName_getSeparateFolders() {
        Activity a = new Activity(UUID.randomUUID(), "Same", UUID.randomUUID(), "2025/26", "F", UUID.randomUUID(), "Burn");
        Activity b = new Activity(UUID.randomUUID(), "Same", UUID.randomUUID(), "2025/26", "F", UUID.randomUUID(), "Burn");
        List<NamedFile> named = SpatialFileNamer.assign(List.of(a, b), List.of(file(a, "one.kml"), file(b, "two.kml")));

        List<String> folders = named.stream().map(NamedFile::folder).sorted().toList();
        assertEquals(List.of("Same/2025-26 F/Burn", "Same_1/2025-26 F/Burn"), folders);
    }

    @Test
    void assign_activitiesWithTheSameNameInOneFiscal_getSeparateFolders() {
        UUID project = UUID.randomUUID();
        UUID fiscal = UUID.randomUUID();
        Activity a = new Activity(project, "P", fiscal, "2025/26", "F", UUID.randomUUID(), "Burn");
        Activity b = new Activity(project, "P", fiscal, "2025/26", "F", UUID.randomUUID(), "Burn");
        List<NamedFile> named = SpatialFileNamer.assign(List.of(a, b), List.of(file(a, "one.kml"), file(b, "two.kml")));

        List<String> folders = named.stream().map(NamedFile::folder).sorted().toList();
        assertEquals(List.of("P/2025-26 F/Burn", "P/2025-26 F/Burn_1"), folders);
    }

    @Test
    void assign_missingNames_useFallbackFolders() {
        Activity a = new Activity(UUID.randomUUID(), null, UUID.randomUUID(), null, null, UUID.randomUUID(), "  ");
        List<NamedFile> named = SpatialFileNamer.assign(List.of(a), List.of(file(a, "x.kml")));

        assertEquals("Project/Fiscal/Activity", named.get(0).folder());
    }

    @Test
    void assign_longFolderNames_areShortened() {
        Activity a = activity("P".repeat(200), "2025/26", "F", "A");
        List<NamedFile> named = SpatialFileNamer.assign(List.of(a), List.of(file(a, "x.kml")));

        assertEquals(80, named.get(0).folder().split("/")[0].length());
    }

    @Test
    void assign_ignoresFilesOfOtherActivitiesAndActivitiesWithoutFiles() {
        Activity a = activity("Alpha", "2025/26", "F", "Burn");
        Activity withoutFiles = activity("Bravo", "2025/26", "F", "Burn");
        SpatialFile stray = new SpatialFile(UUID.randomUUID(), UUID.randomUUID(), "stray.kml");

        List<NamedFile> named = SpatialFileNamer.assign(List.of(a, withoutFiles), List.of(stray, file(a, "x.kml")));

        assertEquals(List.of("Alpha/2025-26 F/Burn/x.shp"), paths(named));
        assertTrue(named.stream().noneMatch(n -> n.file() == stray));
    }

    @Test
    void assign_keepsTheInputOrderOfAnActivitysFiles() {
        Activity a = activity("Alpha", "2025/26", "F", "Burn");
        List<NamedFile> named = SpatialFileNamer.assign(List.of(a),
                List.of(file(a, "zulu.kml"), file(a, "alpha.kml"), file(a, "zulu.kmz")));

        assertEquals(List.of("zulu", "alpha", "zulu_1"), named.stream().map(NamedFile::baseName).toList());
    }
}
