package ca.bc.gov.nrs.wfprev.data.repositories;

import ca.bc.gov.nrs.wfprev.data.entities.CulturalPrescribedFireReportEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Repository
public interface CulturalPrescribedFireReportRepository extends JpaRepository<CulturalPrescribedFireReportEntity, UUID> {

    @Query(value =
            "select " +
                    "  project_guid                   as projectGuid, " +
                    "  project_plan_fiscal_guid       as projectPlanFiscalGuid, " +
                    "  project_type_description       as projectTypeDescription, " +
                    "  project_name                   as projectName, " +
                    "  forest_region_org_unit_name    as forestRegionOrgUnitName, " +
                    "  forest_district_org_unit_name  as forestDistrictOrgUnitName, " +
                    "  bc_parks_region_org_unit_name  as bcParksRegionOrgUnitName, " +
                    "  bc_parks_section_org_unit_name as bcParksSectionOrgUnitName, " +
                    "  fire_centre_org_unit_name      as fireCentreOrgUnitName, " +
                    "  program_area_guid              as programAreaGuid, " +
                    "  planning_unit_name             as planningUnitName, " +
                    "  gross_project_area_ha          as grossProjectAreaHa, " +
                    "  closest_community_name         as closestCommunityName, " +
                    "  project_lead                   as projectLead, " +
                    "  proposal_type_description      as proposalTypeDescription, " +
                    "  project_fiscal_name            as projectFiscalName, " +
                    "  project_fiscal_description     as projectFiscalDescription, " +
                    "  fiscal_year                    as fiscalYear, " +
                    "  activity_category_description  as activityCategoryDescription, " +
                    "  plan_fiscal_status_description as planFiscalStatusDescription, " +
                    "  total_cost_estimate_amount     as totalEstimatedCostAmount, " +
                    "  fiscal_ancillary_fund_amount   as fiscalAncillaryFundAmount, " +
                    "  fiscal_reported_spend_amount   as fiscalReportedSpendAmount, " +
                    "  fiscal_actual_amount           as fiscalActualAmount, " +
                    "  fiscal_planned_project_size_ha as fiscalPlannedProjectSizeHa, " +
                    "  fiscal_completed_size_ha       as fiscalCompletedSizeHa, " +
                    "  spatial_submitted              as spatialSubmitted, " +
                    "  first_nations_engagement       as firstNationsEngagement, " +
                    "  first_nations_deliv_partners   as firstNationsDelivPartners, " +
                    "  first_nations_partner          as firstNationsPartner, " +
                    "  other_partner                  as otherPartner, " +
                    "  cfs_project_code               as cfsProjectCode, " +
                    "  results_project_code           as resultsProjectCode, " +
                    "  results_opening_id             as resultsOpeningId, " +
                    "  primary_objective_type_description   as primaryObjectiveTypeDescription, " +
                    "  secondary_objective_type_description as secondaryObjectiveTypeDescription, " +
                    "  endorsement_timestamp          as endorsementTimestamp, " +
                    "  approved_timestamp             as approvedTimestamp, " +
                    "  outside_wui_ind                as outsideWuiInd, " +
                    "  wui_risk_class_description     as wuiRiskClassDescription, " +
                    "  local_wui_risk_class_description as localWuiRiskClassDescription, " +
                    "  total_rcl_filter_section_score as totalRclFilterSectionScore, " +
                    "  rcl_filter_section_comment     as rclFilterSectionComment, " +
                    "  total_bdf_filter_section_score as totalBdfFilterSectionScore, " +
                    "  bdf_filter_section_comment     as bdfFilterSectionComment, " +
                    "  total_collimp_filter_section_score as totalCollimpFilterSectionScore, " +
                    "  collimp_filter_section_comment as collimpFilterSectionComment, " +
                    "  total_filter_section_score     as totalFilterSectionScore " +
                    "from wfprev.project_crx_vw " +
                    "where project_guid = :pg",
            nativeQuery = true)
    List<CulturalPrescribedFireReportRow> findCrxByProjectGuidNative(@Param("pg") UUID projectGuid);

    @Query(value =
            "select " +
                    "  project_guid                   as projectGuid, " +
                    "  project_plan_fiscal_guid       as projectPlanFiscalGuid, " +
                    "  project_type_description       as projectTypeDescription, " +
                    "  project_name                   as projectName, " +
                    "  forest_region_org_unit_name    as forestRegionOrgUnitName, " +
                    "  forest_district_org_unit_name  as forestDistrictOrgUnitName, " +
                    "  bc_parks_region_org_unit_name  as bcParksRegionOrgUnitName, " +
                    "  bc_parks_section_org_unit_name as bcParksSectionOrgUnitName, " +
                    "  fire_centre_org_unit_name      as fireCentreOrgUnitName, " +
                    "  program_area_guid              as programAreaGuid, " +
                    "  planning_unit_name             as planningUnitName, " +
                    "  gross_project_area_ha          as grossProjectAreaHa, " +
                    "  closest_community_name         as closestCommunityName, " +
                    "  project_lead                   as projectLead, " +
                    "  proposal_type_description      as proposalTypeDescription, " +
                    "  project_fiscal_name            as projectFiscalName, " +
                    "  project_fiscal_description     as projectFiscalDescription, " +
                    "  fiscal_year                    as fiscalYear, " +
                    "  activity_category_description  as activityCategoryDescription, " +
                    "  plan_fiscal_status_description as planFiscalStatusDescription, " +
                    "  total_cost_estimate_amount     as totalEstimatedCostAmount, " +
                    "  fiscal_ancillary_fund_amount   as fiscalAncillaryFundAmount, " +
                    "  fiscal_reported_spend_amount   as fiscalReportedSpendAmount, " +
                    "  fiscal_actual_amount           as fiscalActualAmount, " +
                    "  fiscal_planned_project_size_ha as fiscalPlannedProjectSizeHa, " +
                    "  fiscal_completed_size_ha       as fiscalCompletedSizeHa, " +
                    "  spatial_submitted              as spatialSubmitted, " +
                    "  first_nations_engagement       as firstNationsEngagement, " +
                    "  first_nations_deliv_partners   as firstNationsDelivPartners, " +
                    "  first_nations_partner          as firstNationsPartner, " +
                    "  other_partner                  as otherPartner, " +
                    "  cfs_project_code               as cfsProjectCode, " +
                    "  results_project_code           as resultsProjectCode, " +
                    "  results_opening_id             as resultsOpeningId, " +
                    "  primary_objective_type_description   as primaryObjectiveTypeDescription, " +
                    "  secondary_objective_type_description as secondaryObjectiveTypeDescription, " +
                    "  endorsement_timestamp          as endorsementTimestamp, " +
                    "  approved_timestamp             as approvedTimestamp, " +
                    "  outside_wui_ind                as outsideWuiInd, " +
                    "  wui_risk_class_description     as wuiRiskClassDescription, " +
                    "  local_wui_risk_class_description as localWuiRiskClassDescription, " +
                    "  total_rcl_filter_section_score as totalRclFilterSectionScore, " +
                    "  rcl_filter_section_comment     as rclFilterSectionComment, " +
                    "  total_bdf_filter_section_score as totalBdfFilterSectionScore, " +
                    "  bdf_filter_section_comment     as bdfFilterSectionComment, " +
                    "  total_collimp_filter_section_score as totalCollimpFilterSectionScore, " +
                    "  collimp_filter_section_comment as collimpFilterSectionComment, " +
                    "  total_filter_section_score     as totalFilterSectionScore " +
                    "from wfprev.project_crx_vw " +
                    "where project_guid = :pg " +
                    "  and project_plan_fiscal_guid in (:fiscals)",
            nativeQuery = true)
    List<CulturalPrescribedFireReportRow> findCrxByProjectGuidAndFiscalInNative(
            @Param("pg") UUID projectGuid,
            @Param("fiscals") List<UUID> fiscals
    );

    interface CulturalPrescribedFireReportRow {
        UUID getProjectGuid();
        UUID getProjectPlanFiscalGuid();
        String getProjectTypeDescription();
        String getProjectName();
        String getForestRegionOrgUnitName();
        String getForestDistrictOrgUnitName();
        String getBcParksRegionOrgUnitName();
        String getBcParksSectionOrgUnitName();
        String getFireCentreOrgUnitName();
        UUID getProgramAreaGuid();
        String getPlanningUnitName();
        BigDecimal getGrossProjectAreaHa();
        String getClosestCommunityName();
        String getProjectLead();
        String getProposalTypeDescription();
        String getProjectFiscalName();
        String getProjectFiscalDescription();
        String getFiscalYear();
        String getActivityCategoryDescription();
        String getPlanFiscalStatusDescription();
        BigDecimal getTotalEstimatedCostAmount();
        BigDecimal getFiscalAncillaryFundAmount();
        BigDecimal getFiscalReportedSpendAmount();
        BigDecimal getFiscalActualAmount();
        BigDecimal getFiscalPlannedProjectSizeHa();
        BigDecimal getFiscalCompletedSizeHa();
        String getSpatialSubmitted();
        String getFirstNationsEngagement();
        String getFirstNationsDelivPartners();
        String getFirstNationsPartner();
        String getOtherPartner();
        String getCfsProjectCode();
        String getResultsProjectCode();
        String getResultsOpeningId();
        String getPrimaryObjectiveTypeDescription();
        String getSecondaryObjectiveTypeDescription();
        Date getEndorsementTimestamp();
        Date getApprovedTimestamp();
        Boolean getOutsideWuiInd();
        String getWuiRiskClassDescription();
        String getLocalWuiRiskClassDescription();
        BigDecimal getTotalRclFilterSectionScore();
        String getRclFilterSectionComment();
        BigDecimal getTotalBdfFilterSectionScore();
        String getBdfFilterSectionComment();
        BigDecimal getTotalCollimpFilterSectionScore();
        String getCollimpFilterSectionComment();
        BigDecimal getTotalFilterSectionScore();
    }
}
