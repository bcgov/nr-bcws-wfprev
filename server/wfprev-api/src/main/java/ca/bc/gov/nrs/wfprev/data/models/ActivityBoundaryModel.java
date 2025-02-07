package ca.bc.gov.nrs.wfprev.data.models;

import ca.bc.gov.nrs.wfprev.common.entities.CommonModel;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.postgresql.geometric.PGpolygon;
import org.springframework.hateoas.server.core.Relation;

import java.math.BigDecimal;
import java.util.Date;

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
    private String activityGuid;
    private Date systemStartTimestamp;
    private Date systemEndTimestamp;
    private String mappingLabel;
    private Date collectionDate;
    private String collectionMethod;
    private String collectorName;
    private BigDecimal boundarySizeHa;
    private String boundaryComment;
    private PGpolygon geometry;
}
