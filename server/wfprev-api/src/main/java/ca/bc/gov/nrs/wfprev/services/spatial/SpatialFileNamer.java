package ca.bc.gov.nrs.wfprev.services.spatial;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Names the Shapefiles of a RESULTS spatial export and the folders that hold them.
 * <p>
 * The XLSX and the spatial ZIP are produced by separate requests, and each one names the files on its own.
 * The workbook's file-name column only matches the ZIP if both produce the same names, so this class is a
 * pure function of its input: the order is fixed by names and GUIDs, never by the order the rows arrive in.
 * <p>
 * Rules:
 * <ul>
 *   <li>A file keeps the name the user uploaded it with, minus its extension. Characters Windows doesn't
 *       allow in file names are replaced with {@code _}.</li>
 *   <li>File names are unique across the whole export, ignoring case, not just within their folder:
 *       RESULTS users copy the files into a single SharePoint district folder, which removes the folders.
 *       Clashes get {@code _1}, {@code _2}, ... in export order.</li>
 *   <li>Folders are {@code Project/Fiscal/Activity}. Sibling folders with the same name get the same
 *       suffixes.</li>
 * </ul>
 */
public final class SpatialFileNamer {

    public static final String SHAPEFILE_EXTENSION = ".shp";

    private static final Pattern ILLEGAL_CHARS = Pattern.compile("[<>:\"/\\\\|?*\\p{Cntrl}]");
    private static final Pattern TRAILING_DOTS_AND_SPACES = Pattern.compile("[. ]+$");
    private static final Pattern RESERVED_WINDOWS_NAMES =
            Pattern.compile("(?i)^(CON|PRN|AUX|NUL|COM[1-9]|LPT[1-9])(\\..*)?$");
    private static final Pattern SPATIAL_EXTENSION = Pattern.compile("(?i)\\.(zip|kml|kmz|shp|gdb)$");
    private static final int MAX_FOLDER_NAME_LENGTH = 80;

    private SpatialFileNamer() {
    }

    /** An exported activity row. */
    public record Activity(UUID projectGuid, String projectName,
                           UUID projectPlanFiscalGuid, String fiscalYear, String fiscalName,
                           UUID activityGuid, String activityName) {
    }

    /** A spatial file of an activity. Within an activity, files are named in the order they are passed in. */
    public record SpatialFile(UUID activityGuid, UUID activityBoundaryGuid, String documentPath) {
    }

    /**
     * A named file.
     *
     * @param folder   {@code Project/Fiscal/Activity}, without a trailing slash
     * @param baseName the file name without extension, unique across the export
     */
    public record NamedFile(Activity activity, SpatialFile file, String folder, String baseName) {

        public String shapefileName() {
            return baseName + SHAPEFILE_EXTENSION;
        }

        /** The path of the file with the given extension (".shp", ".dbf", ...) inside the ZIP. */
        public String zipPath(String extension) {
            return folder + "/" + baseName + extension;
        }
    }

    /**
     * Names every file that belongs to one of the activities. Files of activities not in the list are
     * ignored, and so is an activity listed twice after its first entry.
     *
     * @return the named files in export order: by project, fiscal and activity, then by the input order of
     *         each activity's files
     */
    public static List<NamedFile> assign(Collection<Activity> activities, List<SpatialFile> files) {
        Map<UUID, List<SpatialFile>> filesByActivity = new HashMap<>();
        for (SpatialFile file : files) {
            filesByActivity.computeIfAbsent(file.activityGuid(), k -> new ArrayList<>()).add(file);
        }

        Map<UUID, Activity> uniqueActivities = new LinkedHashMap<>();
        for (Activity activity : activities) {
            uniqueActivities.putIfAbsent(activity.activityGuid(), activity);
        }
        List<Activity> ordered = new ArrayList<>(uniqueActivities.values());
        ordered.sort(EXPORT_ORDER);

        UniqueNames fileNames = new UniqueNames();
        UniqueNames projectFolders = new UniqueNames();
        Map<UUID, String> projectFolderByGuid = new HashMap<>();
        Map<String, UniqueNames> fiscalFoldersByParent = new HashMap<>();
        Map<UUID, String> fiscalFolderByGuid = new HashMap<>();
        Map<String, UniqueNames> activityFoldersByParent = new HashMap<>();

        List<NamedFile> named = new ArrayList<>();
        for (Activity activity : ordered) {
            List<SpatialFile> activityFiles = filesByActivity.get(activity.activityGuid());
            if (activityFiles == null || activityFiles.isEmpty()) {
                continue;
            }

            String projectFolder = projectFolderByGuid.computeIfAbsent(activity.projectGuid(),
                    k -> projectFolders.claim(folderName(activity.projectName(), "Project")));
            String fiscalFolder = fiscalFolderByGuid.computeIfAbsent(activity.projectPlanFiscalGuid(),
                    k -> projectFolder + "/" + fiscalFoldersByParent
                            .computeIfAbsent(projectFolder, p -> new UniqueNames())
                            .claim(folderName(fiscalLabel(activity), "Fiscal")));
            String activityFolder = fiscalFolder + "/" + activityFoldersByParent
                    .computeIfAbsent(fiscalFolder, p -> new UniqueNames())
                    .claim(folderName(activity.activityName(), "Activity"));

            for (SpatialFile file : activityFiles) {
                named.add(new NamedFile(activity, file, activityFolder, fileNames.claim(baseName(file.documentPath()))));
            }
        }
        return named;
    }

    /** The uploaded file name without folders or a spatial extension, made safe for a file name. */
    static String baseName(String documentPath) {
        String name = documentPath == null ? "" : documentPath;
        name = name.substring(Math.max(name.lastIndexOf('/'), name.lastIndexOf('\\')) + 1);
        if (SPATIAL_EXTENSION.matcher(name).find()) {
            // "boundary.shp.zip" becomes "boundary", not "boundary.shp"
            while (SPATIAL_EXTENSION.matcher(name).find()) {
                name = SPATIAL_EXTENSION.matcher(name).replaceFirst("");
            }
        } else if (name.lastIndexOf('.') > 0) {
            name = name.substring(0, name.lastIndexOf('.'));
        }
        return clean(name, "spatial_file");
    }

    static String folderName(String name, String fallback) {
        String cleaned = clean(name, fallback);
        if (cleaned.length() > MAX_FOLDER_NAME_LENGTH) {
            cleaned = clean(cleaned.substring(0, MAX_FOLDER_NAME_LENGTH), fallback);
        }
        return cleaned;
    }

    static String clean(String name, String fallback) {
        String cleaned = ILLEGAL_CHARS.matcher(name == null ? "" : name).replaceAll("_").trim();
        cleaned = TRAILING_DOTS_AND_SPACES.matcher(cleaned).replaceAll("");
        if (cleaned.isEmpty()) {
            return fallback;
        }
        if (RESERVED_WINDOWS_NAMES.matcher(cleaned).matches()) {
            cleaned = cleaned + "_";
        }
        return cleaned;
    }

    private static String fiscalLabel(Activity activity) {
        // "2025/26" can't be a folder name; "2025-26" reads the same.
        String year = activity.fiscalYear() == null ? "" : activity.fiscalYear().replace('/', '-');
        String name = activity.fiscalName() == null ? "" : activity.fiscalName();
        return (year + " " + name).trim();
    }

    private static final Comparator<String> TEXT = Comparator.nullsFirst(
            Comparator.comparing((String s) -> s.toLowerCase(Locale.ROOT)).thenComparing(Comparator.naturalOrder()));
    private static final Comparator<UUID> GUID = Comparator.nullsFirst(Comparator.naturalOrder());

    private static final Comparator<Activity> EXPORT_ORDER = Comparator
            .comparing(Activity::projectName, TEXT)
            .thenComparing(Activity::projectGuid, GUID)
            .thenComparing(Activity::fiscalYear, TEXT)
            .thenComparing(Activity::fiscalName, TEXT)
            .thenComparing(Activity::projectPlanFiscalGuid, GUID)
            .thenComparing(Activity::activityName, TEXT)
            .thenComparing(Activity::activityGuid, GUID);

    /** Hands out names that are unique ignoring case, adding _1, _2, ... on a clash. */
    private static final class UniqueNames {
        private final Set<String> used = new HashSet<>();

        String claim(String name) {
            String candidate = name;
            for (int n = 1; !used.add(candidate.toLowerCase(Locale.ROOT)); n++) {
                candidate = name + "_" + n;
            }
            return candidate;
        }
    }
}
