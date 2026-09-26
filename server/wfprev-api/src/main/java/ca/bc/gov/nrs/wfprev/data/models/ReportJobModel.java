package ca.bc.gov.nrs.wfprev.data.models;

import lombok.Builder;
import lombok.Value;

import java.util.UUID;

/** A report export job as the download tray sees it. */
@Value
@Builder
public class ReportJobModel {
    UUID jobGuid;
    UUID exportGroupGuid;
    ReportType reportType;
    String fileName;
    String description;
    /** PREPARING, READY, NO_FILES or FAILED. */
    String status;
    /** READY, but the file has passed its retention period and is gone. */
    boolean expired;
    boolean downloaded;
    String errorCode;
    String errorMessage;
    Boolean retryable;
    /** Short job reference for support requests; it is also in the logs. */
    String reference;
    /** ISO-8601 UTC, e.g. 2026-09-24T21:14:03Z. The API's ObjectMapper has no java.time module. */
    String requestTimestamp;
}
