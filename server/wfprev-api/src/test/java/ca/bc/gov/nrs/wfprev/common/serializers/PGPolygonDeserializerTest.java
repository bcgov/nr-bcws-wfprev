package ca.bc.gov.nrs.wfprev.common.serializers;

import ca.bc.gov.nrs.wfprev.WfprevApiApplication;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.postgresql.geometric.PGpolygon;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PGPolygonDeserializerTest {
    private final ObjectMapper mapper = new WfprevApiApplication().registerObjectMapper();
    private static final double BUFFER_SIZE = 0.0001;
    private final PGPolygonDeserializer deserializer = new PGPolygonDeserializer(); // Adjust based on actual class name

    @Test
    void testDeserializePGPolygon() throws Exception {
        String json = "{\"type\":\"Polygon\",\"coordinates\":[[[0.0,0.0],[10.0,0.0],[10.0,10.0],[0.0,10.0],[0.0,0.0]]]}";
        PGpolygon polygon = mapper.readValue(json, PGpolygon.class);

        assertNotNull(polygon);
        assertEquals(5, polygon.points.length);
        assertEquals(0.0, polygon.points[0].x);
        assertEquals(0.0, polygon.points[0].y);
    }

    @Test
    void testConvertSinglePointToBufferedPolygon_ValidPoint() throws Exception {
        // Given
        String json = "[10.0, 20.0]";
        JsonNode pointNode = mapper.readTree(json);

        // When
        PGpolygon polygon = deserializer.convertSinglePointToBufferedPolygon(pointNode);

        // Then
        assertNotNull(polygon);
        assertEquals(5, polygon.points.length); // 4 corners + closing point

        // Check corner points
        assertEquals(10.0 - BUFFER_SIZE, polygon.points[0].x); // Bottom-left x
        assertEquals(20.0 - BUFFER_SIZE, polygon.points[0].y); // Bottom-left y
        assertEquals(10.0 + BUFFER_SIZE, polygon.points[1].x); // Bottom-right x
        assertEquals(20.0 - BUFFER_SIZE, polygon.points[1].y); // Bottom-right y
        assertEquals(10.0 + BUFFER_SIZE, polygon.points[2].x); // Top-right x
        assertEquals(20.0 + BUFFER_SIZE, polygon.points[2].y); // Top-right y
        assertEquals(10.0 - BUFFER_SIZE, polygon.points[3].x); // Top-left x
        assertEquals(20.0 + BUFFER_SIZE, polygon.points[3].y); // Top-left y
        assertEquals(10.0 - BUFFER_SIZE, polygon.points[4].x); // Closing point x
        assertEquals(20.0 - BUFFER_SIZE, polygon.points[4].y); // Closing point y
    }

    @Test
    void testConvertSinglePointToBufferedPolygon_InvalidFormat_NotAnArray() throws Exception {
        // Given
        String json = "{\"x\": 10.0, \"y\": 20.0}"; // Not an array
        JsonNode pointNode = mapper.readTree(json);

        // When & Then
        IllegalArgumentException thrown = assertThrows(
                IllegalArgumentException.class,
                () -> deserializer.convertSinglePointToBufferedPolygon(pointNode)
        );

        assertEquals("Invalid point format in polygon ring", thrown.getMessage());
    }

    @Test
    void testConvertSinglePointToBufferedPolygon_InvalidFormat_WrongArraySize() throws Exception {
        // Given
        String json = "[10.0]"; // Not a valid point (needs 2 values)
        JsonNode pointNode = mapper.readTree(json);

        // When & Then
        IllegalArgumentException thrown = assertThrows(
                IllegalArgumentException.class,
                () -> deserializer.convertSinglePointToBufferedPolygon(pointNode)
        );

        assertEquals("Invalid point format in polygon ring", thrown.getMessage());
    }
}

