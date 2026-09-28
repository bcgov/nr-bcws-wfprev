package ca.bc.gov.nrs.wfprev.data.models;

import lombok.Value;

/** A short-lived link to a finished export file. */
@Value
public class ReportJobDownloadUrlModel {
    String url;
    String fileName;
}
