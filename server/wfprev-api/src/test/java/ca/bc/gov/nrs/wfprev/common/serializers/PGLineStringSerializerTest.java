package ca.bc.gov.nrs.wfprev.common.serializers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.postgresql.geometric.PGpoint;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class PGLineStringSerializerTest {
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addSerializer(PGpoint[].class, new PGLineStringSerializer());
        objectMapper.registerModule(module);
    }

    @Test
    void testSerializeValidLineString() throws JsonProcessingException {
        PGpoint[] lineString = {
                new PGpoint(-124.0, 49.0),
                new PGpoint(-125.0, 50.0)
        };

        String json = objectMapper.writeValueAsString(lineString);
        assertTrue(json.contains("[[-124.0,49.0],[-125.0,50.0]]"));
    }
}
