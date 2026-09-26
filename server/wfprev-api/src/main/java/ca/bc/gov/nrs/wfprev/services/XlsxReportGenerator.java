package ca.bc.gov.nrs.wfprev.services;

import ca.bc.gov.nrs.wfprev.data.entities.ProjectCulturalPrescribedFireReportEntity;
import ca.bc.gov.nrs.wfprev.data.entities.ProjectFuelManagementReportEntity;
import ca.bc.gov.nrs.wfprev.data.entities.ResultsCulturalPrescribedFireReportEntity;
import ca.bc.gov.nrs.wfprev.data.entities.ResultsFuelManagementReportEntity;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Builds the report Lambda's input for the XLSX exports. The Lambda fills the Jasper templates;
 * {@code ReportLambdaInvoker} passes it this input through S3.
 */
@Component
public class XlsxReportGenerator {

    /** The report Lambda's input for the Fiscal XLSX. */
    public LambdaReportRequest buildProjectRequest(List<ProjectFuelManagementReportEntity> fuelEntities,
                                                   List<ProjectCulturalPrescribedFireReportEntity> crxEntities) {
        LambdaReportRequest.XlsxReportData xlsxData = new LambdaReportRequest.XlsxReportData();
        xlsxData.setProjectFuelManagementReportData(fuelEntities);
        xlsxData.setProjectCulturePrescribedFireReportData(crxEntities);
        return buildRequest("ReMi_Fiscal", xlsxData);
    }

    /** The report Lambda's input for the RESULTS XLSX. */
    public LambdaReportRequest buildResultsRequest(List<ResultsFuelManagementReportEntity> fuelEntities,
                                                   List<ResultsCulturalPrescribedFireReportEntity> crxEntities) {
        LambdaReportRequest.XlsxReportData xlsxData = new LambdaReportRequest.XlsxReportData();
        xlsxData.setResultsFuelManagementReportData(fuelEntities);
        xlsxData.setResultsCulturePrescribedFireReportData(crxEntities);
        return buildRequest("ReMi_RESULTS", xlsxData);
    }

    private static LambdaReportRequest buildRequest(String reportName, LambdaReportRequest.XlsxReportData xlsxData) {
        LambdaReportRequest lambdaRequest = new LambdaReportRequest();
        LambdaReportRequest.Report report = new LambdaReportRequest.Report();
        report.setReportType("XLSX");
        report.setReportName(reportName);
        report.setXlsxReportData(xlsxData);
        lambdaRequest.setReports(List.of(report));
        return lambdaRequest;
    }

    // POJO for Lambda request
    public static class LambdaReportRequest {
        private List<Report> reports;

        public List<Report> getReports() {
            return reports;
        }

        public void setReports(List<Report> reports) {
            this.reports = reports;
        }

        public static class Report {
            private String reportType;
            private String reportName;
            private XlsxReportData xlsxReportData;

            public String getReportType() {
                return reportType;
            }

            public void setReportType(String reportType) {
                this.reportType = reportType;
            }

            public String getReportName() {
                return reportName;
            }

            public void setReportName(String reportName) {
                this.reportName = reportName;
            }

            public XlsxReportData getXlsxReportData() {
                return xlsxReportData;
            }

            public void setXlsxReportData(XlsxReportData xlsxReportData) {
                this.xlsxReportData = xlsxReportData;
            }
        }

        public static class XlsxReportData {
            private List<ProjectCulturalPrescribedFireReportEntity> projectCulturePrescribedFireReportData;
            private List<ProjectFuelManagementReportEntity> projectFuelManagementReportData;
            private List<ResultsCulturalPrescribedFireReportEntity> resultsCulturePrescribedFireReportData;
            private List<ResultsFuelManagementReportEntity> resultsFuelManagementReportData;

            public List<ProjectCulturalPrescribedFireReportEntity> getProjectCulturePrescribedFireReportData() {
                return projectCulturePrescribedFireReportData;
            }

            public void setProjectCulturePrescribedFireReportData(List<ProjectCulturalPrescribedFireReportEntity> data) {
                this.projectCulturePrescribedFireReportData = data;
            }

            public List<ProjectFuelManagementReportEntity> getProjectFuelManagementReportData() {
                return projectFuelManagementReportData;
            }

            public void setProjectFuelManagementReportData(List<ProjectFuelManagementReportEntity> data) {
                this.projectFuelManagementReportData = data;
            }

            public List<ResultsCulturalPrescribedFireReportEntity> getResultsCulturePrescribedFireReportData() {
                return resultsCulturePrescribedFireReportData;
            }

            public void setResultsCulturePrescribedFireReportData(List<ResultsCulturalPrescribedFireReportEntity> data) {
                this.resultsCulturePrescribedFireReportData = data;
            }

            public List<ResultsFuelManagementReportEntity> getResultsFuelManagementReportData() {
                return resultsFuelManagementReportData;
            }

            public void setResultsFuelManagementReportData(List<ResultsFuelManagementReportEntity> data) {
                this.resultsFuelManagementReportData = data;
            }
        }
    }
}
