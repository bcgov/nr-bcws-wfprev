package ca.bc.gov.nrs.wfprev.common.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.postgresql.geometric.PGpolygon;

import java.io.IOException;

public class PGMultiPolygonSerializer extends JsonSerializer<PGpolygon[]> {
    @Override
    public void serialize(PGpolygon[] multiPolygon, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (multiPolygon == null || multiPolygon.length == 0) {
            gen.writeNull();
            return;
        }

        gen.writeStartObject();
        gen.writeStringField("type", "MultiPolygon");
        gen.writeFieldName("coordinates");
        gen.writeStartArray(); // MultiPolygon array

        for (PGpolygon polygon : multiPolygon) {
            gen.writeStartArray(); // Each polygon
            gen.writeStartArray(); // Exterior ring

            for (var point : polygon.points) {
                gen.writeStartArray();
                gen.writeNumber(point.x);
                gen.writeNumber(point.y);
                gen.writeEndArray();
            }

            // Ensure polygon is closed
            if (polygon.points.length > 0 &&
                    (polygon.points[0].x != polygon.points[polygon.points.length - 1].x ||
                            polygon.points[0].y != polygon.points[polygon.points.length - 1].y)) {
                gen.writeStartArray();
                gen.writeNumber(polygon.points[0].x);
                gen.writeNumber(polygon.points[0].y);
                gen.writeEndArray();
            }

            gen.writeEndArray(); // End exterior ring
            gen.writeEndArray(); // End polygon
        }

        gen.writeEndArray(); // End MultiPolygon array
        gen.writeEndObject();
    }
}
