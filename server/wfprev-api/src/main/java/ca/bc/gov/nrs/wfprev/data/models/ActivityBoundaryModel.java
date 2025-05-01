package ca.bc.gov.nrs.wfprev.data.models;

import ca.bc.gov.nrs.wfprev.common.entities.CommonModel;
import ca.bc.gov.nrs.wfprev.common.validators.ActivityBoundaryTimestamps;
import ca.bc.gov.nrs.wfprev.common.validators.NotEmptyActivityBoundary;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonRootName;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.MultiPolygon;
import org.springframework.hateoas.server.core.Relation;

import java.math.BigDecimal;
import java.util.Date;

@NotEmptyActivityBoundary
@ActivityBoundaryTimestamps
@Data
@EqualsAndHashCode(callSuper = false)
@JsonRootName(value = "activityBoundary")
@Relation(collectionRelation = "activityBoundary")
@JsonInclude(Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ActivityBoundaryModel extends CommonModel<ActivityBoundaryModel> {
    private String activityBoundaryGuid;
    @NotNull(message = "ActivityBoundary activityGuid must not be null")
    private String activityGuid;
    @NotNull(message = "ActivityBoundary systemStartTimestamp must not be null")
    private Date systemStartTimestamp;
    @NotNull(message = "ActivityBoundary systemEndTimestamp must not be null")
    private Date systemEndTimestamp;
    private String mappingLabel;
    @NotNull(message = "ActivityBoundary collectionDate must not be null")
    private Date collectionDate;
    private String collectionMethod;
    private String collectorName;
    @NotNull(message = "ActivityBoundary boundarySizeHa must not be null")
    private BigDecimal boundarySizeHa;
    private String boundaryComment;
    @NotNull(message = "ActivityBoundary geometry must not be null")
    private MultiPolygon geometry;
}
