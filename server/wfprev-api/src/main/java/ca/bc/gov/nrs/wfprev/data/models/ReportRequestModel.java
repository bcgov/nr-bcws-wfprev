package ca.bc.gov.nrs.wfprev.data.models;

import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class ReportRequestModel {
    private List<UUID> projectGuids;
    private String reportType;
}
