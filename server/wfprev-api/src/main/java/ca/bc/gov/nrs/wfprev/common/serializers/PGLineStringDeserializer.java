package ca.bc.gov.nrs.wfprev.common.serializers;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import org.postgresql.geometric.PGpoint;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PGLineStringDeserializer extends JsonDeserializer<PGpoint[]> {
    @Override
    public PGpoint[] deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonNode coordinatesNode = (JsonNode) p.getCodec().readTree(p).get("coordinates");

        List<PGpoint> points = new ArrayList<>();
        for (JsonNode pointNode : coordinatesNode) {
            if (!pointNode.isArray() || pointNode.size() != 2) {
                throw new IllegalArgumentException("Invalid Point in LineString");
            }
            points.add(new PGpoint(pointNode.get(0).asDouble(), pointNode.get(1).asDouble()));
        }

        return points.toArray(new PGpoint[0]);
    }
}
