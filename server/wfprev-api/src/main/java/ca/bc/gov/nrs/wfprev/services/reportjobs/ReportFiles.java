package ca.bc.gov.nrs.wfprev.services.reportjobs;

import ca.bc.gov.nrs.wfprev.data.models.ReportType;

import java.util.UUID;

/** File names, content types and S3 keys for exported files. */
public final class ReportFiles {

    public static final String KEY_PREFIX = "jobs/";

    private ReportFiles() {
    }

    public static String fileName(ReportType type) {
        return switch (type) {
            case PROJECT_CSV -> "ReMi_Fiscal.zip";
            case PROJECT_XLSX -> "ReMi_Fiscal.xlsx";
            case RESULTS_CSV -> "results-report.zip";
            case RESULTS_XLSX -> "ReMi_RESULTS.xlsx";
            case RESULTS_SPATIAL -> "ReMi_RESULTS_Spatial.zip";
        };
    }

    public static String contentType(ReportType type) {
        return switch (type) {
            case PROJECT_XLSX, RESULTS_XLSX -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            case PROJECT_CSV, RESULTS_CSV, RESULTS_SPATIAL -> "application/zip";
        };
    }

    public static boolean isXlsx(ReportType type) {
        return type == ReportType.PROJECT_XLSX || type == ReportType.RESULTS_XLSX;
    }

    /** Where the finished file goes. Fixed per job, so the sweep can check for it. */
    public static String outputKey(UUID jobGuid, String fileName) {
        return KEY_PREFIX + jobGuid + "/" + fileName;
    }

    /** The rows the report Lambda builds the XLSX from. */
    public static String inputKey(UUID jobGuid) {
        return KEY_PREFIX + jobGuid + "/input.json";
    }

    /** The short reference users quote to support: the start of the job GUID. */
    public static String reference(UUID jobGuid) {
        return jobGuid.toString().substring(0, 8).toUpperCase();
    }
}
