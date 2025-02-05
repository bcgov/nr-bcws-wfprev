package ca.bc.gov.nrs.wfprev.common.serializers;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.geolatte.geom.Geometry;
import org.geolatte.geom.Polygon;
import org.geolatte.geom.json.GeolatteGeomModule;

import java.io.IOException;

public class GeoJsonJacksonDeserializer extends JsonDeserializer<Geometry> {
    private final ObjectMapper geolatteMapper;

    public GeoJsonJacksonDeserializer() {
        this.geolatteMapper = new ObjectMapper();
        this.geolatteMapper.registerModule(new GeolatteGeomModule());
    }

    @Override
    public Geometry deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        ObjectNode node = p.getCodec().readTree(p);
        return geolatteMapper.treeToValue(node, Polygon.class);
    }
}