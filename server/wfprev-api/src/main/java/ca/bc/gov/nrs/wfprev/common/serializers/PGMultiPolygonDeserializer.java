package ca.bc.gov.nrs.wfprev.common.serializers;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.postgresql.geometric.PGpolygon;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class PGMultiPolygonDeserializer extends JsonDeserializer<PGpolygon[]> {

    @Override
    public PGpolygon[] deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonNode node = p.getCodec().readTree(p);

        if (node == null || !node.has("coordinates") || !node.get("coordinates").isArray()) {
            throw new IllegalArgumentException("Invalid MultiPolygon format");
        }

        List<PGpolygon> polygons = new ArrayList<>();

        for (JsonNode polygonNode : node.get("coordinates")) {
            String polygonString = convertJsonArrayToPGPolygonString(polygonNode);
            try {
                polygons.add(new PGpolygon(polygonString));
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        return polygons.toArray(new PGpolygon[0]);
    }

    /**
     * Converts a JSON array of coordinates into a string formatted for PGpolygon.
     * Example output: "((0.0,0.0), (5.0,0.0), (5.0,5.0), (0.0,5.0), (0.0,0.0))"
     */
    private String convertJsonArrayToPGPolygonString(JsonNode polygonNode) {
        StringBuilder polygonString = new StringBuilder("(");

        for (JsonNode ringNode : polygonNode) { // Iterate over outer and inner rings
            polygonString.append("("); // Start a new ring
            for (JsonNode pointNode : ringNode) {
                double x = pointNode.get(0).asDouble();
                double y = pointNode.get(1).asDouble();
                polygonString.append(x).append(",").append(y).append(", ");
            }
            polygonString.setLength(polygonString.length() - 2); // Remove last comma and space
            polygonString.append("), "); // Close ring
        }

        polygonString.setLength(polygonString.length() - 2); // Remove last comma and space
        polygonString.append(")"); // Close entire polygon

        return polygonString.toString();
    }
}
