package ca.bc.gov.nrs.wfprev.data.models;

import lombok.Data;

import java.util.List;
import ca.bc.gov.nrs.wfprev.data.params.FeatureQueryParams;
import java.util.UUID;

@Data
public class ReportRequestModel {
    private String reportType;
    private FeatureQueryParams projectFilter;
    private List<Project> projects;

    @Data
    public static class Project {
        private UUID projectGuid;
        private List<UUID> projectFiscalGuids;
    }
}