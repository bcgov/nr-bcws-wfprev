package ca.bc.gov.nrs.wfprev.common.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import lombok.extern.slf4j.Slf4j;
import org.postgresql.geometric.PGpoint;
import org.postgresql.geometric.PGpolygon;

import java.io.IOException;

@Slf4j
public class GeoJsonJacksonSerializer extends JsonSerializer<PGpolygon> {

    @Override
    public void serialize(PGpolygon polygon, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (polygon == null) {
            gen.writeNull();
            return;
        }

        try {
            gen.writeStartObject();

            // Write GeoJSON type
            gen.writeStringField("type", "Polygon");

            // Start coordinates array
            gen.writeFieldName("coordinates");
            gen.writeStartArray(); // Outer array for polygon
            gen.writeStartArray(); // Inner array for ring

            // Convert each point to coordinates
            PGpoint[] points = polygon.points;
            for (PGpoint point : points) {
                gen.writeStartArray();
                gen.writeNumber(point.x);
                gen.writeNumber(point.y);
                gen.writeEndArray();
            }

            // If the first and last points aren't the same, close the ring
            if (points.length > 0 && !pointsEqual(points[0], points[points.length - 1])) {
                gen.writeStartArray();
                gen.writeNumber(points[0].x);
                gen.writeNumber(points[0].y);
                gen.writeEndArray();
            }

            gen.writeEndArray(); // End ring array
            gen.writeEndArray(); // End polygon array

            gen.writeEndObject();

        } catch (Exception e) {
            log.error("Error serializing polygon: {}", e.getMessage(), e);
            throw new IOException("Error serializing polygon: " + e.getMessage(), e);
        }
    }

    private boolean pointsEqual(PGpoint p1, PGpoint p2) {
        return p1.x == p2.x && p1.y == p2.y;
    }
}