package ca.bc.gov.nrs.wfprev.services.reportjobs;

/**
 * Why a job failed, as the user is told it. The technical detail is logged with the job GUID and
 * never reaches the client.
 */
public record ReportJobFailure(ErrorCode code, String message, boolean retryable) {

    public enum ErrorCode {
        /** Something that may work on a retry: S3, the Lambda, anything unrecognised, work lost in a restart. */
        TEMPORARY,
        /** A problem with the data being exported. Retrying gives the same result. */
        DATA,
        /** The export is bigger than the server allows. */
        LIMIT
    }

    static final String TEMPORARY_MESSAGE = "Something went wrong. Try again.";
    static final String NO_DATA_MESSAGE = "No fiscal data matches these filters.";
    static final String INVALID_REQUEST_MESSAGE = "This export request isn't valid. Change the filters and try again.";

    public static ReportJobFailure temporary() {
        return new ReportJobFailure(ErrorCode.TEMPORARY, TEMPORARY_MESSAGE, true);
    }

    /**
     * Maps an exception from running a job to what the user sees. ReportService signals a request
     * with nothing to export, or no filter at all, with {@link IllegalArgumentException}.
     */
    public static ReportJobFailure from(Throwable error) {
        if (error instanceof ReportJobException jobException) {
            return jobException.getFailure();
        }
        if (error instanceof IllegalArgumentException) {
            String detail = error.getMessage() == null ? "" : error.getMessage();
            if (detail.startsWith("No fiscal data found")) {
                return new ReportJobFailure(ErrorCode.DATA, NO_DATA_MESSAGE, false);
            }
            return new ReportJobFailure(ErrorCode.DATA, INVALID_REQUEST_MESSAGE, false);
        }
        return temporary();
    }

    /** Thrown by job steps that already know which failure the user should see. */
    public static class ReportJobException extends RuntimeException {
        private final transient ReportJobFailure failure;

        public ReportJobException(ReportJobFailure failure, String detail, Throwable cause) {
            super(detail, cause);
            this.failure = failure;
        }

        public ReportJobFailure getFailure() {
            return failure;
        }
    }
}
