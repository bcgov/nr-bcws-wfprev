package ca.bc.gov.nrs.wfprev.data.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.MultiPolygon;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = false)
@JsonRootName(value = "projectActivity")
@Relation(collectionRelation = "projectActivities", itemRelation = "projectActivity")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProjectActivityModel extends RepresentationModel<ProjectActivityModel> implements Serializable {
    private static final long serialVersionUID = 1L;

    private UUID projectGuid;
    private Integer projectNumber;
    private String projectTypeCode;
    private String projectTypeDesc;
    private String primaryObjectiveTypeCode;
    private String primaryObjectiveTypeDesc;
    private String secondaryObjectiveTypeCode;
    private String secondaryObjectiveTypeDesc;
    private String resultsProjectCode;
    private UUID projectPlanFiscalGuid;
    private BigDecimal fiscalYear;
    private String projectFiscalName;
    private UUID activityGuid;
    private String activityName;
    private String activityStatusCode;
    private String activityStatusDesc;
    private Date activityStartDate;
    private Date activityEndDate;
    private BigDecimal completedAreaHa;
    private Boolean outstandingObligationsInd;
    private MultiPolygon geometry;
    private Date lastUpdated;
}

