package ca.bc.gov.nrs.wfprev.common.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.geolatte.geom.G2D;
import org.geolatte.geom.Geometry;
import org.geolatte.geom.Polygon;
import org.geolatte.geom.json.GeolatteGeomModule;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public class GeoJsonJacksonSerializer extends JsonSerializer<Geometry> {
    private final ObjectMapper geolatteMapper;

    public GeoJsonJacksonSerializer() {
        this.geolatteMapper = new ObjectMapper();
        this.geolatteMapper.registerModule(new GeolatteGeomModule());
    }

    @Override
    public void serialize(Geometry geometry, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (geometry == null) {
            gen.writeNull();
            return;
        }
        ObjectNode geoJson = geolatteMapper.valueToTree(geometry);
        gen.writeObject(geoJson);
    }
}