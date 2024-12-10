package ca.bc.gov.nrs.wfprev.common.serializers;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.JsonNode;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.geojson.GeoJsonReader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GeoJsonJacksonDeserializerTest {

    @Mock
    private JsonParser jsonParser;

    @Mock
    private ObjectCodec objectCodec;

    @Mock
    private JsonNode jsonNode;

    @Mock
    private GeoJsonReader geoJsonReader;

    @InjectMocks
    private GeoJsonJacksonDeserializer geoJsonJacksonDeserializer;

    @Test
    void testDeserialize_Success() throws IOException, JsonProcessingException, ParseException {
        // Given
        String geoJsonString = "{\"type\":\"Point\",\"coordinates\":[10.0,20.0]}";
        Geometry expectedGeometry = new GeoJsonReader().read(new StringReader(geoJsonString));

        when(jsonParser.getCodec()).thenReturn(objectCodec);
        when(objectCodec.readTree(jsonParser)).thenReturn(jsonNode);
        when(jsonNode.toString()).thenReturn(geoJsonString);
        when(geoJsonReader.read(any(Reader.class))).thenReturn(expectedGeometry);

        // When
        Geometry result = geoJsonJacksonDeserializer.deserialize(jsonParser, null);

        // Then
        Assertions.assertNotNull(result);
        Assertions.assertEquals(expectedGeometry.toString(), result.toString());
    }

    @Test
    void testDeserialize_ParseException() throws IOException, ParseException {
        // Given
        String invalidGeoJsonString = "{\"type\":\"InvalidType\",\"coordinates\":[10.0,20.0]}";

        when(jsonParser.getCodec()).thenReturn(objectCodec);
        when(objectCodec.readTree(jsonParser)).thenReturn(jsonNode);
        when(jsonNode.toString()).thenReturn(invalidGeoJsonString);
        when(geoJsonReader.read(any(Reader.class))).thenThrow(new ParseException("Invalid GeoJson", new Throwable()));

        // When
        Assertions.assertThrows(IOException.class, () -> geoJsonJacksonDeserializer.deserialize(jsonParser, null));
    }

    @Test
    void testDeserialize_RuntimeException() throws IOException, ParseException {
        // Given
        String geoJsonString = "{\"type\":\"Point\",\"coordinates\":[10.0,20.0]}";

        when(jsonParser.getCodec()).thenReturn(objectCodec);
        when(objectCodec.readTree(jsonParser)).thenReturn(jsonNode);
        when(jsonNode.toString()).thenReturn(geoJsonString);

        // When
        Assertions.assertDoesNotThrow(() -> geoJsonJacksonDeserializer.deserialize(jsonParser, null));
    }
}