package ca.bc.gov.nrs.wfprev.common.serializers;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import org.postgresql.geometric.PGpoint;
import org.postgresql.geometric.PGpolygon;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PGPolygonDeserializer extends JsonDeserializer<PGpolygon> {
    private static final double BUFFER_SIZE = 0.0001;

    @Override
    public PGpolygon deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonNode coordinatesNode = (JsonNode) p.getCodec().readTree(p).get("coordinates");

        // Only take the first array as the **exterior ring**
        JsonNode outerRing = coordinatesNode.get(0);

        // Check if it's a single-point case that needs buffering
        if (outerRing.isArray() && outerRing.size() == 1) {
            return convertSinglePointToBufferedPolygon(outerRing.get(0));
        } else {
            // Use existing functionality for normal polygons
            return new PGpolygon(convertJsonArrayToPGpoints(outerRing));
        }
    }

    private PGpoint[] convertJsonArrayToPGpoints(JsonNode ring) {
        if (!ring.isArray() || ring.size() < 3) {
            throw new IllegalArgumentException("Invalid polygon ring. Must have at least 3 points");
        }

        List<PGpoint> points = new ArrayList<>();
        for (JsonNode pointNode : ring) {
            if (!pointNode.isArray() || pointNode.size() != 2) {
                throw new IllegalArgumentException("Invalid point format in polygon ring");
            }
            points.add(new PGpoint(pointNode.get(0).asDouble(), pointNode.get(1).asDouble()));
        }

        return points.toArray(new PGpoint[0]);
    }

    private PGpolygon convertSinglePointToBufferedPolygon(JsonNode pointNode) {
        if (!pointNode.isArray() || pointNode.size() != 2) {
            throw new IllegalArgumentException("Invalid point format in polygon ring");
        }

        double x = pointNode.get(0).asDouble();
        double y = pointNode.get(1).asDouble();

        // Create a square buffer around the point
        PGpoint[] bufferPoints = new PGpoint[5]; // 4 corners + closing point
        bufferPoints[0] = new PGpoint(x - BUFFER_SIZE, y - BUFFER_SIZE); // Bottom-left
        bufferPoints[1] = new PGpoint(x + BUFFER_SIZE, y - BUFFER_SIZE); // Bottom-right
        bufferPoints[2] = new PGpoint(x + BUFFER_SIZE, y + BUFFER_SIZE); // Top-right
        bufferPoints[3] = new PGpoint(x - BUFFER_SIZE, y + BUFFER_SIZE); // Top-left
        bufferPoints[4] = new PGpoint(x - BUFFER_SIZE, y - BUFFER_SIZE); // Close the polygon

        return new PGpolygon(bufferPoints);
    }
}
