package ca.bc.gov.nrs.wfprev.common.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.postgresql.geometric.PGpoint;
import org.postgresql.geometric.PGpolygon;

import java.io.IOException;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class GeoJsonJacksonSerializerTest {

    private GeoJsonJacksonSerializer serializer;
    private JsonGenerator jsonGenerator;
    private SerializerProvider serializerProvider;

    @BeforeEach
    void setUp() {
        serializer = new GeoJsonJacksonSerializer();
        jsonGenerator = mock(JsonGenerator.class);
        serializerProvider = mock(SerializerProvider.class);
    }

    @Test
    void testSerialize_ValidPolygon() throws IOException {
        // Create a mock PGpolygon
        PGpoint[] points = {
                new PGpoint(-123.3656, 48.4284),
                new PGpoint(-123.3657, 48.4285),
                new PGpoint(-123.3658, 48.4284),
                new PGpoint(-123.3656, 48.4284) // Closing the ring
        };
        PGpolygon polygon = new PGpolygon(points);

        // Serialize the polygon
        serializer.serialize(polygon, jsonGenerator, serializerProvider);

        // Verify JSON output structure
        verify(jsonGenerator).writeStartObject();
        verify(jsonGenerator).writeStringField("type", "Polygon");

        verify(jsonGenerator).writeFieldName("coordinates");

        verify(jsonGenerator).writeEndObject();
    }

    @Test
    void testSerialize_NullPolygon() throws IOException {
        serializer.serialize(null, jsonGenerator, serializerProvider);
        verify(jsonGenerator).writeNull();
    }

    @Test
    void testSerialize_ExceptionHandling() throws IOException {
        // Create a mock PGpolygon
        PGpoint[] points = {new PGpoint(-123.3656, 48.4284)};
        PGpolygon polygon = new PGpolygon(points);

        doThrow(new IOException("Test Exception")).when(jsonGenerator).writeStartObject();

        try {
            serializer.serialize(polygon, jsonGenerator, serializerProvider);
        } catch (IOException e) {
            // Expected exception
        }

        verify(jsonGenerator).writeStartObject();
    }
}
