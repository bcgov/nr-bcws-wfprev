package ca.bc.gov.nrs.wfprev.common.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.geojson.GeoJsonWriter;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.StringWriter;

@Slf4j
public class GeoJsonJacksonSerializer extends StdSerializer<Geometry> {

   public GeoJsonJacksonSerializer() {
    super(Geometry.class);
   }

   public void serialize(Geometry geometry, JsonGenerator generator, SerializerProvider provider) throws IOException, JsonProcessingException {
      log.trace("<serialize");

      try {
         GeoJsonWriter geoJsonWriter = new GeoJsonWriter();
         StringWriter writer = new StringWriter();
         geoJsonWriter.write(geometry, writer);
         String json = writer.toString();
         generator.writeRawValue(json);
      } catch (RuntimeException e) {
         log.error("Failed to serialize Geometry: " + geometry, e);
      }

      log.trace(">serialize");
   }
}
