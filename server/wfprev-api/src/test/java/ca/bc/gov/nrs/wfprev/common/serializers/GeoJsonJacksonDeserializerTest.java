package ca.bc.gov.nrs.wfprev.common.serializers;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.geolatte.geom.Geometry;
import org.geolatte.geom.Point;
import org.geolatte.geom.json.GeolatteGeomModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GeoJsonJacksonDeserializerTest {

    @Mock
    private JsonParser jsonParser;

    @Mock
    private ObjectCodec objectCodec;

    @Mock
    private JsonNode jsonNode;

    @Mock
    private DeserializationContext deserializationContext;

    @InjectMocks
    private GeoJsonJacksonDeserializer geoJsonJacksonDeserializer;

    private ObjectMapper geolatteMapper;

    @BeforeEach
    void setup() {
        geolatteMapper = new ObjectMapper();
        geolatteMapper.registerModule(new GeolatteGeomModule());
    }

    @Test
    void testDeserialize_Success() throws IOException {
        // Given a valid GeoJSON Point
        String geoJsonString = "{\"type\":\"Point\",\"coordinates\":[10.0,20.0]}";
        jsonNode = geolatteMapper.readTree(geoJsonString);
        Geometry expectedGeometry = geolatteMapper.treeToValue(jsonNode, Point.class);

        when(jsonParser.getCodec()).thenReturn(objectCodec);
        when(objectCodec.readTree(jsonParser)).thenReturn(jsonNode);

        // When
        Geometry result = geoJsonJacksonDeserializer.deserialize(jsonParser, deserializationContext);

        // Then
        assertNotNull(result);
        assertEquals(expectedGeometry, result);
    }

    @Test
    void testDeserialize_InvalidGeoJson() throws IOException {
        // Given an invalid GeoJSON structure
        String invalidGeoJsonString = "{\"type\":\"InvalidType\",\"coordinates\":[10.0,20.0]}";
        jsonNode = geolatteMapper.readTree(invalidGeoJsonString);

        when(jsonParser.getCodec()).thenReturn(objectCodec);
        when(objectCodec.readTree(jsonParser)).thenReturn(jsonNode);

        // Expect IOException because it cannot map to a valid Geometry
        assertThrows(IOException.class, () -> geoJsonJacksonDeserializer.deserialize(jsonParser, deserializationContext));
    }

    @Test
    void testDeserialize_RuntimeException() throws IOException {
        // Given an exception during parsing
        when(jsonParser.getCodec()).thenReturn(objectCodec);
        when(objectCodec.readTree(jsonParser)).thenThrow(new IOException("Test exception"));

        // Expect IOException when deserializing
        assertThrows(IOException.class, () -> geoJsonJacksonDeserializer.deserialize(jsonParser, deserializationContext));
    }
}
