package ca.bc.gov.nrs.reportgenerator.model;

import ca.bc.gov.nrs.reportgenerator.model.ReportType;
import ca.bc.gov.nrs.reportgenerator.model.XlsxReportData;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class Report {
  private ReportType reportType;
  private String reportName;
  private XlsxReportData xlsxReportData;

  public ReportType getReportType() {
    return reportType;
  }

  public void setReportType(ReportType reportType) {
    this.reportType = reportType;
  }

  public XlsxReportData getXlsxReportData() {
    return xlsxReportData;
  }

  public void setXlsxReportData(XlsxReportData xlsxReportData) {
    this.xlsxReportData = xlsxReportData;
  }

  public String getReportName() {
    return reportName;
  }

  public void setReportName(String reportName) {
    this.reportName = reportName;
  }
}
