package ca.bc.gov.nrs.wfprev.common.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.postgresql.geometric.PGpoint;

import java.io.IOException;

public class PGLineStringSerializer extends JsonSerializer<PGpoint[]> {
    @Override
    public void serialize(PGpoint[] lineString, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (lineString == null || lineString.length == 0) {
            gen.writeNull();
            return;
        }

        gen.writeStartObject();
        gen.writeStringField("type", "LineString");
        gen.writeFieldName("coordinates");
        gen.writeStartArray();

        for (PGpoint point : lineString) {
            gen.writeStartArray();
            gen.writeNumber(point.x);
            gen.writeNumber(point.y);
            gen.writeEndArray();
        }

        gen.writeEndArray();
        gen.writeEndObject();
    }
}
