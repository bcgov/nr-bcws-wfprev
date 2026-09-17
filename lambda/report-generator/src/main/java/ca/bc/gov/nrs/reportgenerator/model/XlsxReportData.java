package ca.bc.gov.nrs.reportgenerator.model;

import io.quarkus.runtime.annotations.RegisterForReflection;
import java.util.List;

@RegisterForReflection
public class XlsxReportData {
  private List<ProjectCulturePrescribedFireReportData> projectCulturePrescribedFireReportData;
  private List<ProjectFuelManagementReportData> projectFuelManagementReportData;

  // Getters and setters
  public List<ProjectCulturePrescribedFireReportData> getProjectCulturePrescribedFireReportData() {
      return projectCulturePrescribedFireReportData;
  }

  public void setProjectCulturePrescribedFireReportData(List<ProjectCulturePrescribedFireReportData> projectCulturePrescribedFireReportData) {
      this.projectCulturePrescribedFireReportData = projectCulturePrescribedFireReportData;
  }

  public List<ProjectFuelManagementReportData> getProjectFuelManagementReportData() {
      return projectFuelManagementReportData;
  }

  public void setProjectFuelManagementReportData(List<ProjectFuelManagementReportData> projectFuelManagementReportData) {
      this.projectFuelManagementReportData = projectFuelManagementReportData;
  }
}
