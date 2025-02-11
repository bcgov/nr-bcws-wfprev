package ca.bc.gov.nrs.wfprev.common.serializers;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import org.postgresql.geometric.PGpoint;

import java.io.IOException;

public class PGPointDeserializer extends JsonDeserializer<PGpoint> {
    @Override
    public PGpoint deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonNode coordinatesNode = (JsonNode) p.getCodec().readTree(p).get("coordinates");

        if (!coordinatesNode.isArray() || coordinatesNode.size() != 2) {
            throw new IllegalArgumentException("Invalid Point format");
        }

        return new PGpoint(coordinatesNode.get(0).asDouble(), coordinatesNode.get(1).asDouble());
    }
}
