package ca.bc.gov.nrs.wfprev.data.models;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum ReportType {
    PROJECT_XLSX,
    RESULTS_XLSX,
    PROJECT_CSV,
    RESULTS_CSV;

    @JsonCreator
    public static ReportType fromString(String value) {
        if (value == null) {
            return null;
        }
        for (ReportType type : ReportType.values()) {
            if (type.name().equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown report type: " + value);
    }
}
