package ca.bc.gov.nrs.reportgenerator.model;

import io.quarkus.runtime.annotations.RegisterForReflection;
import java.util.List;

@RegisterForReflection
public class LambdaEvent {
  private List<CulturePrescribedFireReportData> culturePrescribedFireReportData;
  private List<FuelManagementReportData> fuelManagementReportData;

  // Getters and setters
  public List<CulturePrescribedFireReportData> getCulturePrescribedFireReportData() {
      return culturePrescribedFireReportData;
  }

  public void setCulturePrescribedFireReportData(List<CulturePrescribedFireReportData> culturePrescribedFireReportData) {
      this.culturePrescribedFireReportData = culturePrescribedFireReportData;
  }

  public List<FuelManagementReportData> getFuelManagementReportData() {
      return fuelManagementReportData;
  }

  public void setFuelManagementReportData(List<FuelManagementReportData> fuelManagementReportData) {
      this.fuelManagementReportData = fuelManagementReportData;
  }
}
