package ca.bc.gov.nrs.wfprev.common.serializers;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;

import java.io.IOException;

public class PointDeserializer extends JsonDeserializer<Point> {
    private static final GeometryFactory geometryFactory = new GeometryFactory();

    @Override
    public Point deserialize(JsonParser jsonParser, DeserializationContext context) throws IOException {
        double[] coordinates = jsonParser.readValueAs(double[].class);
        if (coordinates.length < 2) {
            throw new IOException("Invalid point data. Expected [longitude, latitude].");
        }
        return geometryFactory.createPoint(new Coordinate(coordinates[0], coordinates[1]));
    }
}