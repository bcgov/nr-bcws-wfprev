package ca.bc.gov.nrs.wfprev.common.serializers;

import ca.bc.gov.nrs.wfprev.WfprevApiApplication;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.postgresql.geometric.PGpolygon;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class PGMultiPolygonDeserializerTest {
    private final ObjectMapper mapper = new WfprevApiApplication().registerObjectMapper();

    @Test
    void testDeserializePGMultiPolygon() throws Exception {
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
    }


}
