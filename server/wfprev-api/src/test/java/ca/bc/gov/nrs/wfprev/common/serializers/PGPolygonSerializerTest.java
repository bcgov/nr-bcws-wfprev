package ca.bc.gov.nrs.wfprev.common.serializers;

import ca.bc.gov.nrs.wfprev.WfprevApiApplication;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.postgresql.geometric.PGpolygon;
import org.postgresql.geometric.PGpoint;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class PGPolygonSerializerTest {
    private final ObjectMapper mapper = new WfprevApiApplication().registerObjectMapper();

    @Test
    void testSerializePGPolygon() throws Exception {
        PGpoint[] points = { new PGpoint(0, 0), new PGpoint(10, 0), new PGpoint(10, 10), new PGpoint(0, 10), new PGpoint(0, 0) };
        PGpolygon polygon = new PGpolygon(points);

        String json = mapper.writeValueAsString(polygon);
        assertTrue(json.contains("\"type\":\"Polygon\""));
        assertTrue(json.contains("[0.0,0.0]"));
        assertTrue(json.contains("[10.0,0.0]"));
        assertTrue(json.contains("[0.0,0.0]"));
    }
}
