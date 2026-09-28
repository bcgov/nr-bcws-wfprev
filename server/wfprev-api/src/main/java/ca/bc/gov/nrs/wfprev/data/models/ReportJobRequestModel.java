package ca.bc.gov.nrs.wfprev.data.models;

import ca.bc.gov.nrs.wfprev.data.params.FeatureQueryParams;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.UUID;

/** Starts one report export job: one file. */
@Data
public class ReportJobRequestModel {
    @NotNull
    private ReportType reportType;
    private FeatureQueryParams projectFilter;
    private List<ReportRequestModel.Project> projects;
    /** Filter summary shown in the download tray. */
    private String description;
    /** Shared by the jobs started by one download request; generated when missing. */
    private UUID exportGroupGuid;

    public ReportRequestModel toReportRequest() {
        ReportRequestModel request = new ReportRequestModel();
        request.setReportType(reportType);
        request.setProjectFilter(projectFilter);
        request.setProjects(projects);
        return request;
    }
}
