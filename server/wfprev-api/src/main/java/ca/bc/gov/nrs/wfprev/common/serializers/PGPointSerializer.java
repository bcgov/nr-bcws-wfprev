package ca.bc.gov.nrs.wfprev.common.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.postgresql.geometric.PGpoint;

import java.io.IOException;

public class PGPointSerializer extends JsonSerializer<PGpoint> {
    @Override
    public void serialize(PGpoint point, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (point == null) {
            gen.writeNull();
            return;
        }
        gen.writeStartObject();
        gen.writeStringField("type", "Point");
        gen.writeFieldName("coordinates");
        gen.writeStartArray();
        gen.writeNumber(point.x);
        gen.writeNumber(point.y);
        gen.writeEndArray();
        gen.writeEndObject();
    }
}
