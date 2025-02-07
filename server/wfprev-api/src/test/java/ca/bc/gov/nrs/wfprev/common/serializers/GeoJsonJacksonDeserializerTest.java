package ca.bc.gov.nrs.wfprev.common.serializers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.postgresql.geometric.PGpolygon;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GeoJsonJacksonDeserializerTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addDeserializer(PGpolygon.class, new GeoJsonJacksonDeserializer());
        objectMapper.registerModule(module);
    }

    @Test
    void testValidPolygonDeserialization() throws JsonProcessingException {
        String validGeoJson = """
        {
          "type": "Polygon",
          "coordinates": [
            [
              [-125.0000, 50.0000],
              [-125.0001, 50.0001],
              [-125.0002, 50.0000],
              [-125.0000, 50.0000]
            ]
          ]
        }
        """;

        PGpolygon polygon = objectMapper.readValue(validGeoJson, PGpolygon.class);
        assertNotNull(polygon);
        assertEquals("((-125.0,50.0),(-125.0001,50.0001),(-125.0002,50.0),(-125.0,50.0))", polygon.toString());
    }

    @Test
    void testMissingTypeField() {
        String missingTypeGeoJson = """
        {
          "coordinates": [
            [
              [-125.0000, 50.0000],
              [-125.0001, 50.0001],
              [-125.0002, 50.0000],
              [-125.0000, 50.0000]
            ]
          ]
        }
        """;

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            objectMapper.readValue(missingTypeGeoJson, PGpolygon.class);
        });

        assertTrue(exception.getMessage().contains("Missing 'type' field in GeoJSON"));
    }

    @Test
    void testInvalidTypeField() {
        String invalidTypeGeoJson = """
        {
          "type": "Point",
          "coordinates": [
            [-125.0000, 50.0000]
          ]
        }
        """;

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            objectMapper.readValue(invalidTypeGeoJson, PGpolygon.class);
        });

        assertTrue(exception.getMessage().contains("Invalid GeoJSON type"));
    }

    @Test
    void testMissingCoordinatesField() {
        String missingCoordinatesGeoJson = """
        {
          "type": "Polygon"
        }
        """;

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            objectMapper.readValue(missingCoordinatesGeoJson, PGpolygon.class);
        });

        assertTrue(exception.getMessage().contains("Invalid or missing 'coordinates' field"));
    }

    @Test
    void testInvalidCoordinatesFormat() {
        String invalidCoordinatesGeoJson = """
        {
          "type": "Polygon",
          "coordinates": "INVALID"
        }
        """;

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            objectMapper.readValue(invalidCoordinatesGeoJson, PGpolygon.class);
        });

        assertTrue(exception.getMessage().contains("Invalid or missing 'coordinates' field"));
    }
}
