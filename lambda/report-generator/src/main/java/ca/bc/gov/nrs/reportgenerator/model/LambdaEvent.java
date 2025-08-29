package ca.bc.gov.nrs.reportgenerator.model;

import io.quarkus.runtime.annotations.RegisterForReflection;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@RegisterForReflection
@JsonIgnoreProperties(ignoreUnknown = true)
public class LambdaEvent {
    private List<Report> reports;

    public List<Report> getReports() {
        return reports;
    }
    public void setReports(List<Report> reports) {
        this.reports = reports;
    }
}
