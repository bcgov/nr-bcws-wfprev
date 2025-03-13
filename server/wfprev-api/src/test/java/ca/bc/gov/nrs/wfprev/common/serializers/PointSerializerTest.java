package ca.bc.gov.nrs.wfprev.common.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;


class PointSerializerTest {

    private PointSerializer pointSerializer;
    private JsonGenerator jsonGenerator;
    private SerializerProvider serializerProvider;
    private GeometryFactory geometryFactory;

    @BeforeEach
    void setUp() {
        pointSerializer = new PointSerializer();
        jsonGenerator = mock(JsonGenerator.class);
        serializerProvider = mock(SerializerProvider.class);
        geometryFactory = new GeometryFactory();
    }

    @Test
    void testSerialize() throws IOException {
        Point point = geometryFactory.createPoint(new org.locationtech.jts.geom.Coordinate(1.23, 4.56));

        pointSerializer.serialize(point, jsonGenerator, serializerProvider);

        verify(jsonGenerator).writeStartArray();
        verify(jsonGenerator).writeNumber(1.23);
        verify(jsonGenerator).writeNumber(4.56);
        verify(jsonGenerator).writeEndArray();
    }

    @Test
    void testIntegrationWithObjectMapper() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new com.fasterxml.jackson.databind.module.SimpleModule().addSerializer(Point.class, new PointSerializer()));

        Point point = geometryFactory.createPoint(new org.locationtech.jts.geom.Coordinate(1.23, 4.56));
        String json = objectMapper.writeValueAsString(point);

        assertEquals("[1.23,4.56]", json);
    }
}
