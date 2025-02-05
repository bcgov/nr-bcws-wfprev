package ca.bc.gov.nrs.wfprev.common.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.geolatte.geom.Polygon;
import org.geolatte.geom.crs.CoordinateReferenceSystems;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class GeoJsonJacksonSerializerTest {

    @Mock
    private JsonGenerator jsonGenerator;

    @Mock
    private SerializerProvider serializerProvider;

    private GeoJsonJacksonSerializer serializer;

    @BeforeEach
    void setUp() {
        serializer = new GeoJsonJacksonSerializer();
    }

    @Test
    void serialize_NullGeometry_WritesNull() throws IOException {
        // WHEN
        serializer.serialize(null, jsonGenerator, serializerProvider);

        // THEN
        verify(jsonGenerator).writeNull();
    }

    @Test
    void serialize_ValidGeometry_WritesGeoJson() throws IOException {
        // GIVEN: A simple Polygon
        Polygon polygon = org.geolatte.geom.builder.DSL.polygon(
                CoordinateReferenceSystems.WGS84,
                org.geolatte.geom.builder.DSL.ring(
                        org.geolatte.geom.builder.DSL.g(0.0, 0.0),
                        org.geolatte.geom.builder.DSL.g(1.0, 0.0),
                        org.geolatte.geom.builder.DSL.g(1.0, 1.0),
                        org.geolatte.geom.builder.DSL.g(0.0, 1.0),
                        org.geolatte.geom.builder.DSL.g(0.0, 0.0) // Close the ring
                )
        );

        // WHEN: Call serialize method
        serializer.serialize(polygon, jsonGenerator, serializerProvider);

        // THEN: Verify that writeObject was called on jsonGenerator
        verify(jsonGenerator).writeObject(org.mockito.ArgumentMatchers.any());
    }
}
