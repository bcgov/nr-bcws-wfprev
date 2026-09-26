package ca.bc.gov.nrs.reportgenerator.service;

import ca.bc.gov.nrs.reportgenerator.model.LambdaEvent;
import ca.bc.gov.nrs.reportgenerator.model.Report;
import ca.bc.gov.nrs.reportgenerator.model.ReportType;
import ca.bc.gov.nrs.reportgenerator.model.ResultsCulturePrescribedFireReportData;
import ca.bc.gov.nrs.reportgenerator.model.ResultsFuelManagementReportData;
import ca.bc.gov.nrs.reportgenerator.model.XlsxReportData;
import ca.bc.gov.nrs.reportgenerator.service.XlsxReportBuilder.GeneratedXlsx;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

// @QuarkusTest so the builder gets the jasperreports repo, which serves the templates compiled at build time
@QuarkusTest
class XlsxReportBuilderTest {

    @Inject
    XlsxReportBuilder builder;

    @Test
    void resultsReport_hasTheFmAndCrxTabs() throws Exception {
        ResultsFuelManagementReportData fuel = new ResultsFuelManagementReportData();
        fuel.setProjectName("Fuel Management Project");
        ResultsCulturePrescribedFireReportData culture = new ResultsCulturePrescribedFireReportData();
        culture.setProjectName("Culture Prescribed Fire Project");
        XlsxReportData xlsxData = new XlsxReportData();
        xlsxData.setResultsFuelManagementReportData(List.of(fuel));
        xlsxData.setResultsCulturePrescribedFireReportData(List.of(culture));
        Report report = new Report();
        report.setReportType(ReportType.XLSX);
        report.setReportName("ReMi_RESULTS");
        report.setXlsxReportData(xlsxData);
        LambdaEvent event = new LambdaEvent();
        event.setReports(List.of(report));

        List<GeneratedXlsx> files = builder.build(event);

        assertEquals(1, files.size());
        assertEquals("ReMi_RESULTS.xlsx", files.get(0).filename());
        String workbook = readZipEntry(files.get(0).content(), "xl/workbook.xml");
        assertTrue(workbook.contains("name=\"FM XLS Download\""));
        assertTrue(workbook.contains("name=\"CRx XLS Download\""));
    }

    @Test
    void anEventWithNoReportData_buildsNothing() {
        assertTrue(builder.build(new LambdaEvent()).isEmpty());
    }

    private static String readZipEntry(byte[] zip, String name) throws IOException {
        try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(zip))) {
            for (ZipEntry entry; (entry = zis.getNextEntry()) != null; ) {
                if (entry.getName().equals(name)) {
                    return new String(zis.readAllBytes(), StandardCharsets.UTF_8);
                }
            }
        }
        return fail("No " + name + " in the XLSX");
    }
}
