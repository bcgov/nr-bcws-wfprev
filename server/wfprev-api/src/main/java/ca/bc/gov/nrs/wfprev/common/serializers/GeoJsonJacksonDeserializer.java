package ca.bc.gov.nrs.wfprev.common.serializers;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.geojson.GeoJsonReader;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

@Slf4j
public class GeoJsonJacksonDeserializer extends StdDeserializer<Geometry> {

   private  GeoJsonReader geoJsonReader;

   public GeoJsonJacksonDeserializer() {
      super(Geometry.class);
       geoJsonReader = new GeoJsonReader();
   }

   public GeoJsonJacksonDeserializer(GeoJsonReader geoJsonReader) {
      this();
      this.geoJsonReader = geoJsonReader;
   }
   public Geometry deserialize(JsonParser jsonParser, DeserializationContext context) throws IOException {
      log.trace("<deserialize");
      Geometry result = null;
      ObjectCodec oc = jsonParser.getCodec();
      JsonNode node = (JsonNode)oc.readTree(jsonParser);
      String geoJson = node.toString();

      String failedMessage = "Failed to deserialize geojson: ";
      try {
         Reader reader = new StringReader(geoJson);
         result = geoJsonReader.read(reader);
      } catch (ParseException e) {
         log.error(failedMessage + geoJson, e);
         throw new IOException(failedMessage + geoJson, e);
      } catch (RuntimeException e) {
         log.error(failedMessage + geoJson, e);
      }

      log.trace(">deserialize " + result);
      return result;
   }
}
