package ca.bc.gov.nrs.wfprev.common.serializers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.postgresql.geometric.PGpoint;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class PGLineStringDeserializerTest {
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addDeserializer(PGpoint[].class, new PGLineStringDeserializer());
        objectMapper.registerModule(module);
    }

    @Test
    void testDeserializeValidLineString() throws JsonProcessingException {
        String json = "{ \"type\": \"LineString\", \"coordinates\": [[-124.0, 49.0], [-125.0, 50.0]] }";
        PGpoint[] lineString = objectMapper.readValue(json, PGpoint[].class);

        assertNotNull(lineString);
        assertEquals(2, lineString.length);
        assertEquals(-124.0, lineString[0].x, 0.0001);
        assertEquals(49.0, lineString[0].y, 0.0001);
        assertEquals(-125.0, lineString[1].x, 0.0001);
        assertEquals(50.0, lineString[1].y, 0.0001);
    }

    @Test
    void testDeserializeInvalidLineString() {
        String json = "{ \"type\": \"LineString\", \"coordinates\": [[-124.0, 49.0], [-125.0]] }";
        assertThrows(IllegalArgumentException.class, () -> objectMapper.readValue(json, PGpoint[].class));
    }
}
