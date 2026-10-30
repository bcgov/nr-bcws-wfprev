package ca.bc.gov.nrs.wfprev.data.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.locationtech.jts.geom.MultiPolygon;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "project_activity_svw", schema = "wfprev")
@JsonIgnoreProperties(ignoreUnknown = false)
@Data
@EqualsAndHashCode(callSuper = false)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Immutable
public class ProjectActivityEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "row_id")
    private UUID rowId;

    @Column(name = "project_guid")
    private UUID projectGuid;

    @Column(name = "project_number")
    private Integer projectNumber;

    @Column(name = "project_type_code")
    private String projectTypeCode;

    @Column(name = "project_type_desc")
    private String projectTypeDesc;

    @Column(name = "primary_objective_type_code")
    private String primaryObjectiveTypeCode;

    @Column(name = "primary_objective_type_desc")
    private String primaryObjectiveTypeDesc;

    @Column(name = "secondary_objective_type_code")
    private String secondaryObjectiveTypeCode;

    @Column(name = "secondary_objective_type_desc")
    private String secondaryObjectiveTypeDesc;

    @Column(name = "results_project_code")
    private String resultsProjectCode;

    @Column(name = "project_plan_fiscal_guid")
    private UUID projectPlanFiscalGuid;

    @Column(name = "fiscal_year")
    private BigDecimal fiscalYear;

    @Column(name = "project_fiscal_name")
    private String projectFiscalName;

    @Column(name = "activity_guid")
    private UUID activityGuid;

    @Column(name = "activity_name")
    private String activityName;

    @Column(name = "activity_status_code")
    private String activityStatusCode;

    @Column(name = "activity_status_desc")
    private String activityStatusDesc;

    @Column(name = "activity_start_date")
    private Date activityStartDate;

    @Column(name = "activity_end_date")
    private Date activityEndDate;

    @Column(name = "completed_area_ha")
    private BigDecimal completedAreaHa;

    @Column(name = "outstanding_obligations_ind")
    private Boolean outstandingObligationsInd;

    @Column(name = "geometry", columnDefinition = "geometry(MultiPolygon, 4326)")
    @JdbcTypeCode(SqlTypes.GEOMETRY)
    private MultiPolygon geometry;

    @Column(name = "last_updated")
    private Date lastUpdated;
}
