package ca.bc.gov.nrs.wfprev.data.resources;

import java.math.BigDecimal;
import java.util.Date;

import org.springframework.hateoas.server.core.Relation;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import ca.bc.gov.nrs.wfprev.common.entities.CommonModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = false)
@JsonRootName(value = "projectBoundary")
@Relation(collectionRelation = "projectBoundary")
@JsonInclude(Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProjectBoundaryModel extends CommonModel<ProjectModel> {
  private String projectBoundaryGuid;
	private String projectGuid;
	private Date systemStartTimestamp;
	private Date systemEndTimestamp;
	private String mappingLabel;
	private Date collectionDate;
	private String collectionMethod;
	private String collectorName;
	private BigDecimal boundarySizeHa;
	private String boundaryComment;
	private Point locationGeometry;
	private Geometry boundaryGeometry;
}
