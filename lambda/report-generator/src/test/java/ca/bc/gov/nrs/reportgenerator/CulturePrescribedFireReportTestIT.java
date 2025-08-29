package ca.bc.gov.nrs.reportgenerator;

import org.junit.jupiter.api.Test;
import java.io.InputStream;

public class CulturePrescribedFireReportTestIT {
    @Test
    public void testCulturePrescribedFireReportCompiledResourceExists() {
        InputStream is = getClass().getClassLoader().getResourceAsStream("jasperreports/WFPREV_CULTURE_PRESCRIBED_FIRE_JASPER.jasper");
        org.junit.jupiter.api.Assertions.assertNotNull(is, "Compiled report (.jasper) not found");
    }
}
