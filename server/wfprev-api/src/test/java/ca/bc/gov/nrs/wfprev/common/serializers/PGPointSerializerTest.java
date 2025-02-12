package ca.bc.gov.nrs.wfprev.common.serializers;

import ca.bc.gov.nrs.wfprev.WfprevApiApplication;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.postgresql.geometric.PGpoint;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PGPointSerializerTest {
    private final ObjectMapper mapper = new WfprevApiApplication().registerObjectMapper();

    @Test
    void testSerializePGPoint() throws Exception {
        PGpoint point = new PGpoint(10.5, -20.3);
        String json = mapper.writeValueAsString(point);

        assertEquals("{\"type\":\"Point\",\"coordinates\":[10.5,-20.3]}", json);
    }
}
