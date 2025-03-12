package ca.bc.gov.nrs.wfprev.common.serializers;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.locationtech.jts.geom.Point;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.when;

class PointDeserializerTest {

    private PointDeserializer deserializer;
    private ObjectMapper objectMapper;

    @Mock
    private JsonParser jsonParser;

    @Mock
    private DeserializationContext deserializationContext;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        deserializer = new PointDeserializer();
        objectMapper = new ObjectMapper();
    }

    @Test
    void testDeserialize_ValidPoint() throws IOException {
        // Mock the JSON input as a double array
        when(jsonParser.readValueAs(double[].class)).thenReturn(new double[]{-124.0, 49.0});

        // Call deserializer
        Point result = deserializer.deserialize(jsonParser, deserializationContext);

        // Validate output
        assertNotNull(result);
        Assertions.assertEquals(-124.0, result.getX());
        Assertions.assertEquals(49.0, result.getY());
    }

    @Test
    void testDeserialize_InvalidPoint_TooFewCoordinates() {
        // Mock input with an insufficient number of coordinates
        try {
            when(jsonParser.readValueAs(double[].class)).thenReturn(new double[]{-124.0});
            Executable executable = () -> deserializer.deserialize(jsonParser, deserializationContext);
            assertThrows(IOException.class, executable, "Invalid point data. Expected [longitude, latitude].");
        } catch (IOException e) {
            fail("Unexpected IOException during test setup.");
        }
    }

    @Test
    void testDeserialize_InvalidPoint_EmptyArray() {
        // Mock input as an empty array
        try {
            when(jsonParser.readValueAs(double[].class)).thenReturn(new double[]{});
            Executable executable = () -> deserializer.deserialize(jsonParser, deserializationContext);
            assertThrows(IOException.class, executable, "Invalid point data. Expected [longitude, latitude].");
        } catch (IOException e) {
            fail("Unexpected IOException during test setup.");
        }
    }
}
