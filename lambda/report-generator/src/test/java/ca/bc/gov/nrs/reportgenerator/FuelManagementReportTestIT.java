package ca.bc.gov.nrs.reportgenerator;

import org.junit.jupiter.api.Test;
import java.io.InputStream;

public class FuelManagementReportTestIT {
    @Test
    public void testFuelManagementReportCompiledResourceExists() {
        InputStream is = getClass().getClassLoader().getResourceAsStream("jasperreports/WFPREV_FUEL_MANAGEMENT_JASPER.jasper");
        org.junit.jupiter.api.Assertions.assertNotNull(is, "Compiled report (.jasper) not found");
    }
}
