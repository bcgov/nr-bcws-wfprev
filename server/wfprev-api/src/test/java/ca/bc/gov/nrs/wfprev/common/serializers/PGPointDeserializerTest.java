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

public class PGPointDeserializerTest {
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addDeserializer(PGpoint.class, new PGPointDeserializer());
        objectMapper.registerModule(module);
    }

    @Test
    void testDeserializeValidPoint() throws JsonProcessingException {
        String json = "{ \"type\": \"Point\", \"coordinates\": [-124.0, 49.0] }";
        PGpoint point = objectMapper.readValue(json, PGpoint.class);

        assertNotNull(point);
        assertEquals(-124.0, point.x, 0.0001);
        assertEquals(49.0, point.y, 0.0001);
    }

    @Test
    void testDeserializeInvalidPoint() {
        String json = "{ \"type\": \"Point\", \"coordinates\": [-124.0] }";
        assertThrows(IllegalArgumentException.class, () -> objectMapper.readValue(json, PGpoint.class));
    }
}
