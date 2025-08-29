package ca.bc.gov.nrs.reportgenerator.model;

import io.quarkus.runtime.annotations.RegisterForReflection;
import java.util.List;

@RegisterForReflection
public class LambdaEvent {
    private List<Report> reports;

    public List<Report> getReports() {
        return reports;
    }
    public void setReports(List<Report> reports) {
        this.reports = reports;
    }
}
