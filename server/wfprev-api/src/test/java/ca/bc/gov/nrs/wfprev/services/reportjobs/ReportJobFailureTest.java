package ca.bc.gov.nrs.wfprev.services.reportjobs;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReportJobFailureTest {

    @Test
    void noMatchingData_isADataProblem() {
        ReportJobFailure failure = ReportJobFailure.from(
                new IllegalArgumentException("No fiscal data found for the provided projects"));

        assertEquals(ReportJobFailure.ErrorCode.DATA, failure.code());
        assertEquals("No fiscal data matches these filters.", failure.message());
        assertFalse(failure.retryable());
    }

    @Test
    void anInvalidRequest_isADataProblem() {
        ReportJobFailure failure = ReportJobFailure.from(
                new IllegalArgumentException("At least one project or a filter is required"));

        assertEquals(ReportJobFailure.ErrorCode.DATA, failure.code());
        assertFalse(failure.retryable());
    }

    @Test
    void anythingElse_isTemporaryAndRetryable() {
        ReportJobFailure failure = ReportJobFailure.from(new IllegalStateException("Lambda timed out"));

        assertEquals(ReportJobFailure.ErrorCode.TEMPORARY, failure.code());
        assertEquals("Something went wrong. Try again.", failure.message());
        assertTrue(failure.retryable());
    }

    @Test
    void errors_areTemporaryToo() {
        assertEquals(ReportJobFailure.ErrorCode.TEMPORARY, ReportJobFailure.from(new OutOfMemoryError()).code());
    }

    @Test
    void aJobException_keepsItsFailure() {
        ReportJobFailure limit = new ReportJobFailure(ReportJobFailure.ErrorCode.LIMIT, "Too big", false);

        assertEquals(limit, ReportJobFailure.from(new ReportJobFailure.ReportJobException(limit, "detail", null)));
    }
}
