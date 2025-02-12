package ca.bc.gov.nrs.wfprev.common.serializers;

import static org.junit.jupiter.api.Assertions.*;

import ca.bc.gov.nrs.wfprev.WfprevApiApplication;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.postgresql.geometric.PGpolygon;

public class PGMultiPolygonDeserializerTest {
    private final ObjectMapper mapper = new WfprevApiApplication().registerObjectMapper();

    @Test
    void testSimpleMultiPolygon() throws Exception {
        String json = """
                {
                  "type": "MultiPolygon",
                  "coordinates": [
                    [[[0.0,0.0],[5.0,0.0],[5.0,5.0],[0.0,5.0],[0.0,0.0]]],
                    [[[10.0,10.0],[15.0,10.0],[15.0,15.0],[10.0,15.0],[10.0,10.0]]]
                  ]
                }
                """;

        PGpolygon[] multiPolygon = mapper.readValue(json, PGpolygon[].class);

        assertNotNull(multiPolygon);
        assertEquals(2, multiPolygon.length);
        assertEquals(0.0, multiPolygon[0].points[0].x);
        assertEquals(10.0, multiPolygon[1].points[0].x);
    }

    @Test
    void testPolygonWithInteriorRing() throws Exception {
        String json = """
                {
                  "type": "MultiPolygon",
                  "coordinates": [
                    [
                      [[0.0,0.0],[10.0,0.0],[10.0,10.0],[0.0,10.0],[0.0,0.0]],
                      [[2.0,2.0],[2.0,8.0],[8.0,8.0],[8.0,2.0],[2.0,2.0]]
                    ]
                  ]
                }
                """;

        PGpolygon[] multiPolygon = mapper.readValue(json, PGpolygon[].class);

        assertNotNull(multiPolygon);
        assertEquals(2, multiPolygon.length); // Now expecting 2 polygons - exterior and interior

        // Verify exterior ring points
        assertEquals(0.0, multiPolygon[0].points[0].x);
        assertEquals(0.0, multiPolygon[0].points[0].y);
        assertEquals(10.0, multiPolygon[0].points[2].x);
        assertEquals(10.0, multiPolygon[0].points[2].y);

        // Verify interior ring points as separate polygon
        assertEquals(2.0, multiPolygon[1].points[0].x);
        assertEquals(2.0, multiPolygon[1].points[0].y);
        assertEquals(8.0, multiPolygon[1].points[2].x);
        assertEquals(8.0, multiPolygon[1].points[2].y);
    }

    @Test
    void testComplexMultiPolygonWithHoles() throws Exception {
        String json = """
                {
                  "type": "MultiPolygon",
                  "coordinates": [
                    [
                      [[0.0,0.0],[10.0,0.0],[10.0,10.0],[0.0,10.0],[0.0,0.0]],
                      [[2.0,2.0],[2.0,4.0],[4.0,4.0],[4.0,2.0],[2.0,2.0]]
                    ],
                    [
                      [[20.0,20.0],[30.0,20.0],[30.0,30.0],[20.0,30.0],[20.0,20.0]],
                      [[22.0,22.0],[22.0,28.0],[28.0,28.0],[28.0,22.0],[22.0,22.0]]
                    ]
                  ]
                }
                """;

        PGpolygon[] multiPolygon = mapper.readValue(json, PGpolygon[].class);

        assertNotNull(multiPolygon);
        assertEquals(4, multiPolygon.length); // Now expecting 4 polygons - 2 exterior and 2 interior

        // Test first polygon and its hole
        assertEquals(0.0, multiPolygon[0].points[0].x);
        assertEquals(2.0, multiPolygon[1].points[0].x);

        // Test second polygon and its hole
        assertEquals(20.0, multiPolygon[2].points[0].x);
        assertEquals(22.0, multiPolygon[3].points[0].x);
    }
}