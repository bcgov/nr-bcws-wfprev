package ca.bc.gov.nrs.wfprev.common.serializers;

import ca.bc.gov.nrs.wfprev.WfprevApiApplication;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.postgresql.geometric.PGpolygon;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class PGPolygonDeserializerTest {
    private final ObjectMapper mapper = new WfprevApiApplication().registerObjectMapper();

    @Test
    void testDeserializePGPolygon() throws Exception {
        String json = "{\"type\":\"Polygon\",\"coordinates\":[[[0.0,0.0],[10.0,0.0],[10.0,10.0],[0.0,10.0],[0.0,0.0]]]}";
        PGpolygon polygon = mapper.readValue(json, PGpolygon.class);

        assertNotNull(polygon);
        assertEquals(5, polygon.points.length);
        assertEquals(0.0, polygon.points[0].x);
        assertEquals(0.0, polygon.points[0].y);
    }
}
