package ca.bc.gov.nrs.wfprev.common.serializers;

import ca.bc.gov.nrs.wfprev.WfprevApiApplication;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.postgresql.geometric.PGpolygon;
import org.postgresql.geometric.PGpoint;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class PGMultiPolygonSerializerTest {
    private final ObjectMapper mapper = new WfprevApiApplication().registerObjectMapper();

    @Test
    void testSerializePGMultiPolygon() throws Exception {
        PGpoint[] poly1 = { new PGpoint(0, 0), new PGpoint(5, 0), new PGpoint(5, 5), new PGpoint(0, 5), new PGpoint(0, 0) };
        PGpoint[] poly2 = { new PGpoint(10, 10), new PGpoint(15, 10), new PGpoint(15, 15), new PGpoint(10, 15), new PGpoint(10, 10) };

        PGpolygon[] multiPolygon = { new PGpolygon(poly1), new PGpolygon(poly2) };
        String json = mapper.writeValueAsString(multiPolygon);

        assertTrue(json.contains("\"type\":\"MultiPolygon\""));
        assertTrue(json.contains("[0.0,0.0]"));
        assertTrue(json.contains("[15.0,10.0]"));
    }
}
