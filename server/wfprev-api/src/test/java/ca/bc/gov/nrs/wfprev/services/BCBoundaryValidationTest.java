package ca.bc.gov.nrs.wfprev.services;

import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.WKTReader;
import org.locationtech.jts.operation.valid.IsValidOp;
import org.locationtech.jts.operation.valid.TopologyValidationError;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

class BCBoundaryValidationTest {

    @Test
    void testBCBoundaryValidity() throws Exception {
        // Access the private BC_BOUNDARY_WKT from SpatialValidationService
        Field field = SpatialValidationService.class.getDeclaredField("BC_BOUNDARY_WKT");
        field.setAccessible(true);
        String bcBoundaryWkt = (String) field.get(null);

        assertNotNull(bcBoundaryWkt, "BC_BOUNDARY_WKT should not be null");

        WKTReader reader = new WKTReader();
        try {
            org.locationtech.jts.geom.Geometry bcBoundary = reader.read(bcBoundaryWkt);
            
            assertTrue(bcBoundary.isValid(), "BC Boundary WKT is topologically invalid: " + 
                new org.locationtech.jts.operation.valid.IsValidOp(bcBoundary).getValidationError());
        } catch (Exception e) {
            fail("Failed to validate BC Boundary WKT: " + e.getMessage());
        }
    }
}
