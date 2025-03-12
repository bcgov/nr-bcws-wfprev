package ca.bc.gov.nrs.wfprev.data.models;

import ca.bc.gov.nrs.wfprev.common.entities.CommonModel;
import ca.bc.gov.nrs.wfprev.common.validators.ProjectBoundaryTimestamps;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonRootName;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.postgresql.geometric.PGpolygon;
import org.springframework.hateoas.server.core.Relation;

import java.math.BigDecimal;
import java.util.Date;

@ProjectBoundaryTimestamps
@Data
@EqualsAndHashCode(callSuper = false)
@JsonRootName(value = "projectBoundary")
@Relation(collectionRelation = "projectBoundary")
@JsonInclude(Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProjectBoundaryModel extends CommonModel<ProjectBoundaryModel> {
  	private String projectBoundaryGuid;
	@NotNull(message = "ProjectBoundary projectGuid must not be null")
	private String projectGuid;
	@NotNull(message = "ProjectBoundary systemStartTimestamp must not be null")
	private Date systemStartTimestamp;
	@NotNull(message = "ProjectBoundary systemEndTimestamp must not be null")
	private Date systemEndTimestamp;
	private String mappingLabel;
	@NotNull(message = "ProjectBoundary collectionDate must not be null")
	private Date collectionDate;
	private String collectionMethod;
	private String collectorName;
	@NotNull(message = "ProjectBoundary boundarySizeHa must not be null")
	private BigDecimal boundarySizeHa;
	private String boundaryComment;
	@NotNull(message = "ProjectBoundary locationGeometry must not be null")
	private PGpolygon locationGeometry;
	@NotNull(message = "ProjectBoundary boundaryGeometry must not be null")
	private PGpolygon boundaryGeometry;
}
