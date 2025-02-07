package ca.bc.gov.nrs.wfprev.common.serializers;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.postgresql.geometric.PGpolygon;

import java.io.IOException;

@Slf4j
public class GeoJsonJacksonDeserializer extends JsonDeserializer<PGpolygon> {

    @Override
    public PGpolygon deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        try {
            JsonNode node = p.getCodec().readTree(p);
             log.debug("Received JSON node: {}", node);

            // Check if node is null or empty
            if (node == null || node.isEmpty()) {
                 log.error("Received null or empty JSON node");
                return null;
            }

            // Check for type field
            JsonNode typeNode = node.get("type");
            if (typeNode == null) {
                 log.error("Missing 'type' field in JSON");
                throw new IllegalArgumentException("Missing 'type' field in GeoJSON");
            }

            String type = typeNode.asText();
            if (!"Polygon".equals(type)) {
                 log.error("Invalid GeoJSON type: {}", type);
                throw new IllegalArgumentException("Invalid GeoJSON type. Expected 'Polygon', got: " + type);
            }

            // Check for coordinates field
            JsonNode coordinatesNode = node.get("coordinates");
            if (coordinatesNode == null || !coordinatesNode.isArray() || coordinatesNode.isEmpty()) {
                 log.error("Invalid or missing 'coordinates' field");
                throw new IllegalArgumentException("Invalid or missing 'coordinates' field");
            }

            // Get the first ring of coordinates (outer ring)
            JsonNode ring = coordinatesNode.get(0);
            if (!ring.isArray() || ring.size() < 3) {
                 log.error("Invalid polygon ring. Must have at least 3 points");
                throw new IllegalArgumentException("Invalid polygon ring. Must have at least 3 points");
            }

            // Build the PostgreSQL polygon string format with the required outer parentheses
            StringBuilder polygonStr = new StringBuilder("(");
            for (int i = 0; i < ring.size(); i++) {
                JsonNode point = ring.get(i);
                if (!point.isArray() || point.size() != 2) {
                     log.error("Invalid point format at index {}: {}", i, point);
                    throw new IllegalArgumentException("Invalid point format at index " + i);
                }

                double x = point.get(0).asDouble();
                double y = point.get(1).asDouble();

                if (i > 0) {
                    polygonStr.append(",");
                }
                polygonStr.append("(").append(x).append(",").append(y).append(")");
            }
            polygonStr.append(")");

            String finalPolygonStr = polygonStr.toString();
             log.debug("Generated PostgreSQL polygon string: {}", finalPolygonStr);
            return new PGpolygon(finalPolygonStr);

        } catch (Exception e) {
             log.error("Error deserializing polygon: {}", e.getMessage(), e);
            throw new IllegalArgumentException("Error deserializing polygon: " + e.getMessage(), e);
        }
    }
}