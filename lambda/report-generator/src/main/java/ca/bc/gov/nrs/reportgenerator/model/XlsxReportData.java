package ca.bc.gov.nrs.reportgenerator.model;

import io.quarkus.runtime.annotations.RegisterForReflection;
import java.util.List;

@RegisterForReflection
public class XlsxReportData {
  private List<CulturePrescribedFireReportData> culturePrescribedFireReportData;
  private List<ProjectFuelManagementReportData> projectFuelManagementReportData;

  // Getters and setters
  public List<CulturePrescribedFireReportData> getCulturePrescribedFireReportData() {
      return culturePrescribedFireReportData;
  }

  public void setCulturePrescribedFireReportData(List<CulturePrescribedFireReportData> culturePrescribedFireReportData) {
      this.culturePrescribedFireReportData = culturePrescribedFireReportData;
  }

  public List<ProjectFuelManagementReportData> getProjectFuelManagementReportData() {
      return projectFuelManagementReportData;
  }

  public void setProjectFuelManagementReportData(List<ProjectFuelManagementReportData> projectFuelManagementReportData) {
      this.projectFuelManagementReportData = projectFuelManagementReportData;
  }
}
