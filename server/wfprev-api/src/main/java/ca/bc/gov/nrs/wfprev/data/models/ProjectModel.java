package ca.bc.gov.nrs.wfprev.data.models;

import java.math.BigDecimal;
import java.util.Date;

import org.springframework.hateoas.server.core.Relation;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import ca.bc.gov.nrs.wfprev.common.entities.CommonModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = false)
@JsonRootName(value = "project")
@Relation(collectionRelation = "project")
@JsonInclude(Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProjectModel extends CommonModel<ProjectModel> {
  private String projectGuid;
	private ProjectTypeCodeModel projectTypeCode; // CODE
	private Integer projectNumber;
	private String siteUnitName;
	private ForestAreaCodeModel forestAreaCode; // CODE
	private GeneralScopeCodeModel generalScopeCode; // CODE
	private String programAreaGuid; // FK, not Modeled
	private Integer forestRegionOrgUnitId;
	private Integer forestDistrictOrgUnitId;
	private Integer fireCentreOrgUnitId;
	private Integer bcParksRegionOrgUnitId;
	private Integer bcParksSectionOrgUnitId;
	private String projectName;
	private String projectLead;
	private String projectLeadEmailAddress;
	private String projectDescription;
	private String closestCommunityName;
	private BigDecimal totalFundingRequestAmount;
	private BigDecimal totalAllocatedAmount;
	private BigDecimal totalPlannedProjectSizeHa;
	private BigDecimal totalPlannedCostPerHectare;
	private BigDecimal totalActualAmount;
	private BigDecimal totalProjectSizeHa;
	private BigDecimal totalCostPerHectareAmount;
	private Boolean isMultiFiscalYearProj;
	private BigDecimal latitude;
	private BigDecimal longitude;
	private Date lastProgressUpdateTimestamp;
}
