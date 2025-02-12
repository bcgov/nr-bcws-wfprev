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

        // Each element in coordinates array represents a polygon (which may have holes)
        for (JsonNode polygonNode : node.get("coordinates")) {
            // For each polygon, we need to create separate PGpolygon instances for exterior and interior rings
            for (JsonNode ringNode : polygonNode) {
                String polygonString = convertJsonArrayToPGPolygonString(ringNode);
                try {
                    polygons.add(new PGpolygon(polygonString));
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        return polygons.toArray(new PGpolygon[0]);
    }

    private String convertJsonArrayToPGPolygonString(JsonNode ringNode) {
        StringBuilder polygonString = new StringBuilder("(");

        for (JsonNode pointNode : ringNode) {
            double x = pointNode.get(0).asDouble();
            double y = pointNode.get(1).asDouble();
            if (polygonString.length() > 1) {
                polygonString.append(",");
            }
            polygonString.append("(").append(x).append(",").append(y).append(")");
        }

        polygonString.append(")");
        return polygonString.toString();
    }
}