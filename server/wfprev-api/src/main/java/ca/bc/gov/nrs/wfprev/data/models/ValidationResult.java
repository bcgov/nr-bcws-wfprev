package ca.bc.gov.nrs.wfprev.data.models;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.Point;
import ca.bc.gov.nrs.wfprev.common.serializers.PointSerializer;
import ca.bc.gov.nrs.wfprev.common.serializers.PointDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ValidationResult implements Serializable {
    private boolean valid;
    private String message;
    
    @JsonDeserialize(using = PointDeserializer.class)
    @JsonSerialize(using = PointSerializer.class)
    private Point violationLocation;
}
