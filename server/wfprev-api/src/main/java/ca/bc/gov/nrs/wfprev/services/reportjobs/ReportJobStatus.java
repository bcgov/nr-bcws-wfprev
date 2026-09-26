package ca.bc.gov.nrs.wfprev.services.reportjobs;

/** Values of {@code report_export_job.status_code}. Only PREPARING changes; the rest are final. */
public enum ReportJobStatus {
    PREPARING,
    READY,
    /** Finished with nothing to include, e.g. a spatial export for projects with no spatial files. */
    NO_FILES,
    FAILED
}
