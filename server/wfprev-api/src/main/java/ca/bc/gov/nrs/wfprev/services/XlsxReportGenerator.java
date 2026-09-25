package ca.bc.gov.nrs.wfprev.services;

import ca.bc.gov.nrs.wfone.common.service.api.ServiceException;
import ca.bc.gov.nrs.wfprev.data.entities.ProjectCulturalPrescribedFireReportEntity;
import ca.bc.gov.nrs.wfprev.data.entities.ProjectFuelManagementReportEntity;
import ca.bc.gov.nrs.wfprev.data.entities.ResultsCulturalPrescribedFireReportEntity;
import ca.bc.gov.nrs.wfprev.data.entities.ResultsFuelManagementReportEntity;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;
import java.util.List;

@Slf4j
@Component
public class XlsxReportGenerator {

    @Value("${REPORT_GENERATOR_LAMBDA_URL}")
    private String reportGeneratorLambdaUrl;

    public void setReportGeneratorLambdaUrl(String reportGeneratorLambdaUrl) {
        this.reportGeneratorLambdaUrl = reportGeneratorLambdaUrl;
    }

    public String getReportGeneratorLambdaUrl() {
        return reportGeneratorLambdaUrl;
    }

    public void generateXlsx(List<ProjectFuelManagementReportEntity> fuelEntities,
                             List<ProjectCulturalPrescribedFireReportEntity> crxEntities,
                             OutputStream outputStream)
            throws ServiceException, IOException, InterruptedException {
        callLambdaAndWriteStream(buildProjectRequest(fuelEntities, crxEntities), outputStream);
    }

    public void generateResultsXlsx(List<ResultsFuelManagementReportEntity> fuelEntities,
                                    List<ResultsCulturalPrescribedFireReportEntity> crxEntities,
                                    OutputStream outputStream)
            throws ServiceException, IOException, InterruptedException {
        callLambdaAndWriteStream(buildResultsRequest(fuelEntities, crxEntities), outputStream);
    }

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

    private void callLambdaAndWriteStream(LambdaReportRequest lambdaRequest, OutputStream outputStream)
            throws ServiceException, IOException, InterruptedException {
        // Call Lambda
        if (reportGeneratorLambdaUrl == null || reportGeneratorLambdaUrl.isBlank()) {
            throw new ServiceException("REPORT_GENERATOR_LAMBDA_URL environment variable is not set");
        }

        ObjectMapper mapper = new ObjectMapper();
        String requestJson = mapper.writeValueAsString(lambdaRequest);

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(reportGeneratorLambdaUrl))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestJson))
                .build();

        HttpResponse<String> response = client.send(httpRequest,
                HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new ServiceException("Lambda returned error: " + response.body());
        }

        // Parse Lambda response and write first file to outputStream
        String responseJson = response.body();
        LambdaReportResponse lambdaResponse;
        try {
            JsonNode root = mapper.readTree(responseJson);
            if (root.has("body")) {
                // API Gateway or Lambda Function URL wrapper
                String bodyJson = root.get("body").asText();
                lambdaResponse = mapper.readValue(bodyJson, LambdaReportResponse.class);
            } else {
                lambdaResponse = mapper.readValue(responseJson, LambdaReportResponse.class);
            }
        } catch (Exception e) {
            throw new ServiceException("Failed to parse Lambda response: " + e.getMessage(), e);
        }

        if (lambdaResponse.getFiles() == null || lambdaResponse.getFiles().isEmpty()) {
            throw new ServiceException("No files returned from Lambda");
        }
        LambdaReportResponse.File file = lambdaResponse.getFiles().get(0);
        byte[] xlsxBytes = Base64.getDecoder().decode(file.getContent());
        outputStream.write(xlsxBytes);
        outputStream.flush();
    }

    // POJO for Lambda response
    public static class LambdaReportResponse {
        private List<File> files;

        public List<File> getFiles() {
            return files;
        }

        public void setFiles(List<File> files) {
            this.files = files;
        }

        public static class File {
            private String filename;
            private String content;

            public String getFilename() {
                return filename;
            }

            public void setFilename(String filename) {
                this.filename = filename;
            }

            public String getContent() {
                return content;
            }

            public void setContent(String content) {
                this.content = content;
            }
        }
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
