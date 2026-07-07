package ca.bc.gov.nrs.reportgenerator;

import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperReport;
import org.junit.jupiter.api.Test;
import java.io.InputStream;
import static org.junit.jupiter.api.Assertions.*;

public class FuelManagementReportTest {
    @Test
    public void testFuelManagementReportTemplateCompiles() throws Exception {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("jasperreports/WFPREV_FUEL_MANAGEMENT_JASPER.jrxml")) {
            assertNotNull(is, "Template not found");
            JasperReport report = JasperCompileManager.compileReport(is);
            assertNotNull(report, "Report compilation failed");
        }
    }
}
