package ca.bc.gov.nrs.wfprev.data.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Data;
import org.hibernate.annotations.Immutable;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

@Data
@Entity
@Immutable
@Table(name = "results_crx_vw", schema = "wfprev")
public class ResultsCulturalPrescribedFireReportEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "unique_row_guid")
    private UUID uniqueRowGuid;

    @Column(name = "project_guid")
    private UUID projectGuid;

    @Column(name = "project_plan_fiscal_guid")
    private UUID projectPlanFiscalGuid;

    @Transient
    private String linkToProject;

    @Transient
    private String linkToFiscalActivity;

    @Column(name = "project_name")
    private String projectName;

    @Column(name = "project_fiscal_name")
    private String projectFiscalName;

    @Column(name = "activity_name")
    private String activityName;

    @Column(name = "is_results_reportable_ind")
    private String isResultsReportableInd;

    @Column(name = "activity_description")
    private String activityDescription;

    @Column(name = "activity_status_name")
    private String activityStatusName;

    @Column(name = "fiscal_year")
    private String fiscalYear;

    @Column(name = "forest_district_name")
    private String forestDistrictName;

    @Column(name = "project_lead_email_address")
    private String projectLeadEmailAddress;

    @Column(name = "results_project_code")
    private String resultsProjectCode;

    @Column(name = "results_opening_id")
    private String resultsOpeningId;

    @Column(name = "results_opening_action")
    private String resultsOpeningAction;

    @Column(name = "results_opening_category")
    private String resultsOpeningCategory;

    @Column(name = "project_boundary_size_ha")
    private BigDecimal projectBoundarySizeHa;

    @Column(name = "max_permanent_access_percent")
    private BigDecimal maxPermanentAccessPercent;

    @Column(name = "activity_base_name")
    private String activityBaseName;

    @Column(name = "technique_name")
    private String techniqueName;

    @Column(name = "method_name")
    private String methodName;

    @Column(name = "primary_objective_name")
    private String primaryObjectiveName;

    @Column(name = "secondary_objective_name")
    private String secondaryObjectiveName;

    @Column(name = "additional_objective_name")
    private String additionalObjectiveName;

    @Column(name = "activity_end_date")
    private Date activityEndDate;

    @Column(name = "completed_area_ha")
    private BigDecimal completedAreaHa;

    @Column(name = "funding_source_code")
    private String fundingSourceCode;

    @Column(name = "comment")
    private String comment;

    @Column(name = "tenure_number")
    private String tenureNumber;

    @Column(name = "planned_treatment_area_ha")
    private BigDecimal plannedTreatmentAreaHa;

    @Column(name = "contract_phase_name")
    private String contractPhaseName;

    @Column(name = "cfs_project_code")
    private String cfsProjectCode;

    @Column(name = "previous_carry_forward_ind")
    private String previousCarryForwardInd;

    @Column(name = "carry_forward_ind")
    private String carryForwardInd;

    @Column(name = "final_outcome_comments")
    private String finalOutcomeComments;

    @Column(name = "outstanding_obligations_ind")
    private String outstandingObligationsInd;

    @Column(name = "activity_comment")
    private String activityComment;

    @Column(name = "opening_shape_file_name")
    private String openingShapeFileName;

    @Column(name = "activity_shape_file_name")
    private String activityShapeFileName;

    @Column(name = "forest_cover_shape_file_name")
    private String forestCoverShapeFileName;

    @Column(name = "forest_cover_attributes")
    private String forestCoverAttributes;

    @Column(name = "prescription")
    private String prescription;
}
