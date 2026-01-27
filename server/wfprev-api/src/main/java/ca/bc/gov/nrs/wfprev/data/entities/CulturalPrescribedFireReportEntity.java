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
@Table(name = "project_crx_vw", schema = "wfprev")
public class CulturalPrescribedFireReportEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "unique_row_guid")
    private UUID uniqueRowGuid;

    @Column(name = "project_plan_fiscal_guid")
    private UUID projectPlanFiscalGuid;

    @Transient
    private String linkToProject;

    @Transient
    private String linkToFiscalActivity;

    @Column(name = "project_guid")
    private UUID projectGuid;

    @Column(name = "project_type_description")
    private String projectTypeDescription;

    @Column(name = "project_name")
    private String projectName;

    @Column(name = "forest_region_org_unit_name")
    private String forestRegionOrgUnitName;

    @Column(name = "forest_district_org_unit_name")
    private String forestDistrictOrgUnitName;

    @Column(name = "bc_parks_region_org_unit_name")
    private String bcParksRegionOrgUnitName;

    @Column(name = "bc_parks_section_org_unit_name")
    private String bcParksSectionOrgUnitName;

    @Column(name = "fire_centre_org_unit_name")
    private String fireCentreOrgUnitName;

    @Transient
    private String businessArea;

    @Column(name = "program_area_guid")
    private UUID programAreaGuid;

    @Column(name = "planning_unit_name")
    private String planningUnitName;

    @Column(name = "gross_project_area_ha")
    private BigDecimal grossProjectAreaHa;

    @Column(name = "closest_community_name")
    private String closestCommunityName;

    @Column(name = "project_lead")
    private String projectLead;

    @Column(name = "proposal_type_description")
    private String proposalTypeDescription;

    @Column(name = "project_fiscal_name")
    private String projectFiscalName;

    @Column(name = "project_fiscal_description")
    private String projectFiscalDescription;

    @Column(name = "fiscal_year")
    private String fiscalYear;

    @Column(name = "activity_category_description")
    private String activityCategoryDescription;

    @Column(name = "plan_fiscal_status_description")
    private String planFiscalStatusDescription;

    @Column(name = "funding_stream")
    private String fundingStream;

    @Column(name = "total_cost_estimate_amount")
    private BigDecimal totalEstimatedCostAmount;

    @Column(name = "fiscal_forecast_amount")
    private BigDecimal fiscalForecastAmount;

    @Column(name = "ancillary_funding_provider", length = 100)
    private String ancillaryFundingProvider;

    @Column(name = "fiscal_ancillary_fund_amount")
    private BigDecimal fiscalAncillaryFundAmount;

    @Column(name = "fiscal_reported_spend_amount")
    private BigDecimal fiscalReportedSpendAmount;

    @Column(name = "fiscal_actual_amount")
    private BigDecimal fiscalActualAmount;

    @Column(name = "fiscal_planned_project_size_ha")
    private BigDecimal fiscalPlannedProjectSizeHa;

    @Column(name = "fiscal_completed_size_ha")
    private BigDecimal fiscalCompletedSizeHa;

    @Column(name = "spatial_submitted")
    private String spatialSubmitted;

    @Column(name = "first_nations_engagement")
    private String firstNationsEngagement;

    @Column(name = "first_nations_deliv_partners")
    private String firstNationsDelivPartners;

    @Column(name = "first_nations_partner")
    private String firstNationsPartner;

    @Column(name = "other_partner")
    private String otherPartner;

    @Column(name = "cfs_project_code")
    private String cfsProjectCode;

    @Column(name = "results_project_code")
    private String resultsProjectCode;

    @Column(name = "results_opening_id")
    private String resultsOpeningId;

    @Column(name = "primary_objective_type_description")
    private String primaryObjectiveTypeDescription;

    @Column(name = "secondary_objective_type_description")
    private String secondaryObjectiveTypeDescription;

    @Column(name = "endorsement_timestamp")
    private Date endorsementTimestamp;

    @Column(name = "approved_timestamp")
    private Date approvedTimestamp;

    @Column(name = "outside_wui_ind")
    private Boolean outsideWuiInd;

    @Column(name = "wui_risk_class_description")
    private String wuiRiskClassDescription;

    @Column(name = "local_wui_risk_class_description")
    private String localWuiRiskClassDescription;

    @Column(name = "total_rcl_filter_section_score")
    private BigDecimal totalRclFilterSectionScore;

    @Column(name = "rcl_filter_section_comment")
    private String rclFilterSectionComment;

    @Column(name = "total_bdf_filter_section_score")
    private BigDecimal totalBdfFilterSectionScore;

    @Column(name = "bdf_filter_section_comment")
    private String bdfFilterSectionComment;

    @Column(name = "total_collimp_filter_section_score")
    private BigDecimal totalCollimpFilterSectionScore;

    @Column(name = "collimp_filter_section_comment")
    private String collimpFilterSectionComment;

    @Column(name = "total_filter_section_score")
    private BigDecimal totalFilterSectionScore;

    // Performance Update Fields
    @Column(name = "q1_submitted_timestamp")
    private Date q1SubmittedTimestamp;

    @Column(name = "q1_general_update_comment")
    private String q1GeneralUpdateComment;

    @Column(name = "q1_progress_status_code")
    private String q1ProgressStatusCode;

    @Column(name = "q1_forecast_amount")
    private BigDecimal q1ForecastAmount;

    @Column(name = "q1_forecast_adjustment_amount")
    private BigDecimal q1ForecastAdjustmentAmount;

    @Column(name = "q1_forecast_adjustment_rationale")
    private String q1ForecastAdjustmentRationale;

    @Column(name = "q1_budget_high_risk_amount")
    private BigDecimal q1BudgetHighRiskAmount;

    @Column(name = "q1_budget_high_risk_rationale")
    private String q1BudgetHighRiskRationale;

    @Column(name = "q1_budget_medium_risk_amount")
    private BigDecimal q1BudgetMediumRiskAmount;

    @Column(name = "q1_budget_medium_risk_rationale")
    private String q1BudgetMediumRiskRationale;

    @Column(name = "q1_budget_low_risk_amount")
    private BigDecimal q1BudgetLowRiskAmount;

    @Column(name = "q1_budget_low_risk_rationale")
    private String q1BudgetLowRiskRationale;

    @Column(name = "q1_budget_completed_amount")
    private BigDecimal q1BudgetCompletedAmount;

    @Column(name = "q1_budget_completed_description")
    private String q1BudgetCompletedDescription;

    @Column(name = "q2_submitted_timestamp")
    private Date q2SubmittedTimestamp;

    @Column(name = "q2_general_update_comment")
    private String q2GeneralUpdateComment;

    @Column(name = "q2_progress_status_code")
    private String q2ProgressStatusCode;

    @Column(name = "q2_forecast_amount")
    private BigDecimal q2ForecastAmount;

    @Column(name = "q2_forecast_adjustment_amount")
    private BigDecimal q2ForecastAdjustmentAmount;

    @Column(name = "q2_forecast_adjustment_rationale")
    private String q2ForecastAdjustmentRationale;

    @Column(name = "q2_budget_high_risk_amount")
    private BigDecimal q2BudgetHighRiskAmount;

    @Column(name = "q2_budget_high_risk_rationale")
    private String q2BudgetHighRiskRationale;

    @Column(name = "q2_budget_medium_risk_amount")
    private BigDecimal q2BudgetMediumRiskAmount;

    @Column(name = "q2_budget_medium_risk_rationale")
    private String q2BudgetMediumRiskRationale;

    @Column(name = "q2_budget_low_risk_amount")
    private BigDecimal q2BudgetLowRiskAmount;

    @Column(name = "q2_budget_low_risk_rationale")
    private String q2BudgetLowRiskRationale;

    @Column(name = "q2_budget_completed_amount")
    private BigDecimal q2BudgetCompletedAmount;

    @Column(name = "q2_budget_completed_description")
    private String q2BudgetCompletedDescription;

    @Column(name = "q3_submitted_timestamp")
    private Date q3SubmittedTimestamp;

    @Column(name = "q3_general_update_comment")
    private String q3GeneralUpdateComment;

    @Column(name = "q3_progress_status_code")
    private String q3ProgressStatusCode;

    @Column(name = "q3_forecast_amount")
    private BigDecimal q3ForecastAmount;

    @Column(name = "q3_forecast_adjustment_amount")
    private BigDecimal q3ForecastAdjustmentAmount;

    @Column(name = "q3_forecast_adjustment_rationale")
    private String q3ForecastAdjustmentRationale;

    @Column(name = "q3_budget_high_risk_amount")
    private BigDecimal q3BudgetHighRiskAmount;

    @Column(name = "q3_budget_high_risk_rationale")
    private String q3BudgetHighRiskRationale;

    @Column(name = "q3_budget_medium_risk_amount")
    private BigDecimal q3BudgetMediumRiskAmount;

    @Column(name = "q3_budget_medium_risk_rationale")
    private String q3BudgetMediumRiskRationale;

    @Column(name = "q3_budget_low_risk_amount")
    private BigDecimal q3BudgetLowRiskAmount;

    @Column(name = "q3_budget_low_risk_rationale")
    private String q3BudgetLowRiskRationale;

    @Column(name = "q3_budget_completed_amount")
    private BigDecimal q3BudgetCompletedAmount;

    @Column(name = "q3_budget_completed_description")
    private String q3BudgetCompletedDescription;

    @Column(name = "march7_submitted_timestamp")
    private Date march7SubmittedTimestamp;

    @Column(name = "march7_general_update_comment")
    private String march7GeneralUpdateComment;

    @Column(name = "march7_progress_status_code")
    private String march7ProgressStatusCode;

    @Column(name = "march7_forecast_amount")
    private BigDecimal march7ForecastAmount;

    @Column(name = "march7_forecast_adjustment_amount")
    private BigDecimal march7ForecastAdjustmentAmount;

    @Column(name = "march7_forecast_adjustment_rationale")
    private String march7ForecastAdjustmentRationale;

    @Column(name = "march7_budget_high_risk_amount")
    private BigDecimal march7BudgetHighRiskAmount;

    @Column(name = "march7_budget_high_risk_rationale")
    private String march7BudgetHighRiskRationale;

    @Column(name = "march7_budget_medium_risk_amount")
    private BigDecimal march7BudgetMediumRiskAmount;

    @Column(name = "march7_budget_medium_risk_rationale")
    private String march7BudgetMediumRiskRationale;

    @Column(name = "march7_budget_low_risk_amount")
    private BigDecimal march7BudgetLowRiskAmount;

    @Column(name = "march7_budget_low_risk_rationale")
    private String march7BudgetLowRiskRationale;

    @Column(name = "march7_budget_completed_amount")
    private BigDecimal march7BudgetCompletedAmount;

    @Column(name = "march7_budget_completed_description")
    private String march7BudgetCompletedDescription;

    @Column(name = "other_submitted_timestamp")
    private Date otherSubmittedTimestamp;

    @Column(name = "other_general_update_comment")
    private String otherGeneralUpdateComment;

    @Column(name = "other_progress_status_code")
    private String otherProgressStatusCode;

    @Column(name = "other_forecast_amount")
    private BigDecimal otherForecastAmount;

    @Column(name = "other_forecast_adjustment_amount")
    private BigDecimal otherForecastAdjustmentAmount;

    @Column(name = "other_forecast_adjustment_rationale")
    private String otherForecastAdjustmentRationale;

    @Column(name = "other_budget_high_risk_amount")
    private BigDecimal otherBudgetHighRiskAmount;

    @Column(name = "other_budget_high_risk_rationale")
    private String otherBudgetHighRiskRationale;

    @Column(name = "other_budget_medium_risk_amount")
    private BigDecimal otherBudgetMediumRiskAmount;

    @Column(name = "other_budget_medium_risk_rationale")
    private String otherBudgetMediumRiskRationale;

    @Column(name = "other_budget_low_risk_amount")
    private BigDecimal otherBudgetLowRiskAmount;

    @Column(name = "other_budget_low_risk_rationale")
    private String otherBudgetLowRiskRationale;

    @Column(name = "other_budget_completed_amount")
    private BigDecimal otherBudgetCompletedAmount;

    @Column(name = "other_budget_completed_description")
    private String otherBudgetCompletedDescription;
}
