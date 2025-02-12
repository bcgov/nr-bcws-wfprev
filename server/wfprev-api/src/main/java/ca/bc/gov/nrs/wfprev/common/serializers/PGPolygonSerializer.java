package ca.bc.gov.nrs.wfprev.common.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.postgresql.geometric.PGpoint;
import org.postgresql.geometric.PGpolygon;

import java.io.IOException;

public class PGPolygonSerializer extends JsonSerializer<PGpolygon> {
    @Override
    public void serialize(PGpolygon polygon, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (polygon == null || polygon.points == null) {
            gen.writeNull();
            return;
        }

        gen.writeStartObject();
        gen.writeStringField("type", "Polygon");
        gen.writeFieldName("coordinates");
        gen.writeStartArray(); // Outer array for polygon
        gen.writeStartArray(); // Inner array for exterior ring

        for (PGpoint point : polygon.points) {
            gen.writeStartArray();
            gen.writeNumber(point.x);
            gen.writeNumber(point.y);
            gen.writeEndArray();
        }

        // Ensure polygon is closed
        if (polygon.points.length > 0 && !pointsEqual(polygon.points[0], polygon.points[polygon.points.length - 1])) {
            gen.writeStartArray();
            gen.writeNumber(polygon.points[0].x);
            gen.writeNumber(polygon.points[0].y);
            gen.writeEndArray();
        }

        gen.writeEndArray(); // End exterior ring
        gen.writeEndArray(); // End polygon array
        gen.writeEndObject();
    }

    private boolean pointsEqual(PGpoint p1, PGpoint p2) {
        return p1.x == p2.x && p1.y == p2.y;
    }
}
