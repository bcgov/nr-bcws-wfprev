DROP VIEW IF EXISTS wfprev.project_fuel_management_vw;

CREATE or REPLACE VIEW wfprev.project_fuel_management_vw AS 
SELECT
  -- unique_row_guid is transient and only used to satisfy repository methods.
  -- It cannot be used to look up records in code; we don’t query by this field.
uuid_generate_v5(
    wfprev.uuid_namespace(),
    concat_ws('|',
      coalesce(ppf.project_plan_fiscal_guid::text, 'NULL'),
      coalesce(p.project_guid::text, 'NULL'),
      coalesce(ppf.fiscal_year::text, 'NULL'),
      coalesce(ppf.project_fiscal_name, 'NULL'),
      coalesce(ppf.project_fiscal_description, 'NULL')
    )
  ) AS unique_row_guid,
       p.project_guid,
       p.project_type_code, 
       ptc.description AS project_type_description,
       p.project_name, 
       p.program_area_guid,
       p.forest_region_org_unit_id, 
       frou.org_unit_name AS forest_region_org_unit_name,
       p.forest_district_org_unit_id, 
       fdou.org_unit_name AS forest_district_org_unit_name,
       p.bc_parks_region_org_unit_id, 
       bcpr.org_unit_name AS bc_parks_region_org_unit_name,
       p.bc_parks_section_org_unit_id, 
       bcps.org_unit_name AS bc_parks_section_org_unit_name,
       p.fire_centre_org_unit_id, 
       fc.org_unit_name AS fire_centre_org_unit_name,
       ppf.business_area_comment,
       p.site_unit_name AS planning_unit_name,
       p.total_actual_project_size_ha AS gross_project_area_ha,
       p.closest_community_name, p.project_lead,
       ppf.proposal_type_code, 
       prtc.description AS proposal_type_description,
       ppf.project_fiscal_name, 
       ppf.project_fiscal_description,
       ppf.fiscal_year,
       ppf.activity_category_code, 
       acc.description AS activity_category_description,
       ppf.plan_fiscal_status_code, 
       pfsc.description AS plan_fiscal_status_description,
       fs.funding_stream, 
       ppf.total_cost_estimate_amount,
       ppf.fiscal_forecast_amount,
       ppf.ancillary_funding_provider,
       ppf.project_plan_fiscal_guid,
       ppf.fiscal_ancillary_fund_amount, 
       ppf.fiscal_reported_spend_amount, 
       ppf.fiscal_actual_amount,
       ppf.fiscal_planned_project_size_ha, 
       ppf.fiscal_completed_size_ha,
       (SELECT SUM(CASE WHEN is_spatial_added_ind THEN 1 ELSE 0 END)||'/'||COUNT(*) 
        FROM wfprev.activity act 
        WHERE act.project_plan_fiscal_guid = ppf.project_plan_fiscal_guid    
       ) AS spatial_submitted,
       CASE 
        WHEN ppf.first_nations_engagement_ind THEN 'Y' 
        ELSE 'N' 
       END AS first_nations_engagement,
       CASE 
         WHEN ppf.first_nations_deliv_part_ind THEN 'Y' 
         ELSE 'N' 
       END AS first_nations_deliv_partners,
       ppf.first_nations_partner, 
       ppf.other_partner,
       ppf.cfs_project_code,
       p.results_project_code, 
       ppf.results_opening_id,
       p.primary_objective_type_code, 
       potc.description AS primary_objective_type_description,
       p.secondary_objective_type_code, 
       sotc.description AS secondary_objective_type_description,
       ppf.endorsement_timestamp, 
       ppf.approved_timestamp,
       ecs.wui_risk_class_code, 
       rcc.description AS wui_risk_class_description,
       ecs.local_wui_risk_class_code, 
       lrcc.description AS local_wui_risk_class_description,
       ecs.local_wui_risk_class_rationale,
       (SELECT COALESCE(SUM(COALESCE(filter_section_score,0)),0) 
        FROM wfprev.eval_criteria_sect_summ ecss
           WHERE ecss.eval_criteria_summary_guid = ecs.eval_criteria_summary_guid 
             AND ecss.eval_criteria_sect_code = 'COARSE_FLT'
       ) AS total_coarse_filter_section_score,
       (SELECT COALESCE(SUM(COALESCE(filter_section_score,0)),0) 
        FROM wfprev.eval_criteria_sect_summ ecss 
        WHERE ecss.eval_criteria_summary_guid = ecs.eval_criteria_summary_guid 
          AND ecss.eval_criteria_sect_code = 'MEDIUM_FLT'
       ) AS total_medium_filter_section_score,
       (SELECT string_agg(filter_section_comment, '| ') 
        FROM wfprev.eval_criteria_sect_summ ecss 
        WHERE ecss.eval_criteria_summary_guid = ecs.eval_criteria_summary_guid 
          AND ecss.eval_criteria_sect_code = 'MEDIUM_FLT'
       ) AS medium_filter_section_comment,
       (SELECT COALESCE(SUM(COALESCE(filter_section_score,0)),0) 
        FROM wfprev.eval_criteria_sect_summ ecss 
        WHERE ecss.eval_criteria_summary_guid = ecs.eval_criteria_summary_guid 
          AND ecss.eval_criteria_sect_code = 'FINE_FLT'
       ) AS total_fine_filter_section_score,
       (SELECT string_agg(filter_section_comment, '| ') 
        FROM wfprev.eval_criteria_sect_summ ecss 
        WHERE ecss.eval_criteria_summary_guid = ecs.eval_criteria_summary_guid 
          AND ecss.eval_criteria_sect_code = 'FINE_FLT'
       ) AS fine_filter_section_comment,
       (SELECT COALESCE(SUM(COALESCE(filter_section_score,0)),0) 
        FROM wfprev.eval_criteria_sect_summ ecss 
        WHERE ecss.eval_criteria_summary_guid = ecs.eval_criteria_summary_guid 
          AND ecss.eval_criteria_sect_code IN ( 'FINE_FLT', 'MEDIUM_FLT', 'COARSE_FLT' )
       ) AS total_filter_section_score,
       ppfp_q1.submitted_timestamp AS q1_submitted_timestamp,
       ppfp_q1.general_update_comment AS q1_general_update_comment,
       ppfp_q1.progress_status_code AS q1_progress_status_code,
       ppfp_q1.forecast_amount AS q1_forecast_amount,
       ppfp_q1.forecast_adjustment_amount AS q1_forecast_adjustment_amount,
       ppfp_q1.forecast_adjustment_rationale AS q1_forecast_adjustment_rationale,
       ppfp_q1.budget_high_risk_amount AS q1_budget_high_risk_amount,
       ppfp_q1.budget_high_risk_rationale AS q1_budget_high_risk_rationale,
       ppfp_q1.budget_medium_risk_amount AS q1_budget_medium_risk_amount,
       ppfp_q1.budget_medium_risk_rationale AS q1_budget_medium_risk_rationale,
       ppfp_q1.budget_low_risk_amount AS q1_budget_low_risk_amount,
       ppfp_q1.budget_low_risk_rationale AS q1_budget_low_risk_rationale,
       ppfp_q1.budget_completed_amount AS q1_budget_completed_amount,
       ppfp_q1.budget_completed_description AS q1_budget_completed_description,
       ppfp_q2.submitted_timestamp AS q2_submitted_timestamp,
       ppfp_q2.general_update_comment AS q2_general_update_comment,
       ppfp_q2.progress_status_code AS q2_progress_status_code,
       ppfp_q2.forecast_amount AS q2_forecast_amount,
       ppfp_q2.forecast_adjustment_amount AS q2_forecast_adjustment_amount,
       ppfp_q2.forecast_adjustment_rationale AS q2_forecast_adjustment_rationale,
       ppfp_q2.budget_high_risk_amount AS q2_budget_high_risk_amount,
       ppfp_q2.budget_high_risk_rationale AS q2_budget_high_risk_rationale,
       ppfp_q2.budget_medium_risk_amount AS q2_budget_medium_risk_amount,
       ppfp_q2.budget_medium_risk_rationale AS q2_budget_medium_risk_rationale,
       ppfp_q2.budget_low_risk_amount AS q2_budget_low_risk_amount,
       ppfp_q2.budget_low_risk_rationale AS q2_budget_low_risk_rationale,
       ppfp_q2.budget_completed_amount AS q2_budget_completed_amount,
       ppfp_q2.budget_completed_description AS q2_budget_completed_description,
       ppfp_q3.submitted_timestamp AS q3_submitted_timestamp,
       ppfp_q3.general_update_comment AS q3_general_update_comment,
       ppfp_q3.progress_status_code AS q3_progress_status_code,
       ppfp_q3.forecast_amount AS q3_forecast_amount,
       ppfp_q3.forecast_adjustment_amount AS q3_forecast_adjustment_amount,
       ppfp_q3.forecast_adjustment_rationale AS q3_forecast_adjustment_rationale,
       ppfp_q3.budget_high_risk_amount AS q3_budget_high_risk_amount,
       ppfp_q3.budget_high_risk_rationale AS q3_budget_high_risk_rationale,
       ppfp_q3.budget_medium_risk_amount AS q3_budget_medium_risk_amount,
       ppfp_q3.budget_medium_risk_rationale AS q3_budget_medium_risk_rationale,
       ppfp_q3.budget_low_risk_amount AS q3_budget_low_risk_amount,
       ppfp_q3.budget_low_risk_rationale AS q3_budget_low_risk_rationale,
       ppfp_q3.budget_completed_amount AS q3_budget_completed_amount,
       ppfp_q3.budget_completed_description AS q3_budget_completed_description,
       ppfp_m7.submitted_timestamp AS march7_submitted_timestamp,
       ppfp_m7.general_update_comment AS march7_general_update_comment,
       ppfp_m7.progress_status_code AS march7_progress_status_code,
       ppfp_m7.forecast_amount AS march7_forecast_amount,
       ppfp_m7.forecast_adjustment_amount AS march7_forecast_adjustment_amount,
       ppfp_m7.forecast_adjustment_rationale AS march7_forecast_adjustment_rationale,
       ppfp_m7.budget_high_risk_amount AS march7_budget_high_risk_amount,
       ppfp_m7.budget_high_risk_rationale AS march7_budget_high_risk_rationale,
       ppfp_m7.budget_medium_risk_amount AS march7_budget_medium_risk_amount,
       ppfp_m7.budget_medium_risk_rationale AS march7_budget_medium_risk_rationale,
       ppfp_m7.budget_low_risk_amount AS march7_budget_low_risk_amount,
       ppfp_m7.budget_low_risk_rationale AS march7_budget_low_risk_rationale,
       ppfp_m7.budget_completed_amount AS march7_budget_completed_amount,
       ppfp_m7.budget_completed_description AS march7_budget_completed_description,
       ppfp_oth.submitted_timestamp AS other_submitted_timestamp,
       ppfp_oth.general_update_comment AS other_general_update_comment,
       ppfp_oth.progress_status_code AS other_progress_status_code,
       ppfp_oth.forecast_amount AS other_forecast_amount,
       ppfp_oth.forecast_adjustment_amount AS other_forecast_adjustment_amount,
       ppfp_oth.forecast_adjustment_rationale AS other_forecast_adjustment_rationale,
       ppfp_oth.budget_high_risk_amount AS other_budget_high_risk_amount,
       ppfp_oth.budget_high_risk_rationale AS other_budget_high_risk_rationale,
       ppfp_oth.budget_medium_risk_amount AS other_budget_medium_risk_amount,
       ppfp_oth.budget_medium_risk_rationale AS other_budget_medium_risk_rationale,
       ppfp_oth.budget_low_risk_amount AS other_budget_low_risk_amount,
       ppfp_oth.budget_low_risk_rationale AS other_budget_low_risk_rationale,
       ppfp_oth.budget_completed_amount AS other_budget_completed_amount,
       ppfp_oth.budget_completed_description AS other_budget_completed_description
FROM wfprev.project p 
  LEFT JOIN wfprev.project_type_code ptc      ON ptc.project_type_code      = p.project_type_code
  LEFT JOIN wfprev.forest_org_unit frou       ON frou.org_unit_identifier   = p.forest_region_org_unit_id
  LEFT JOIN wfprev.forest_org_unit fdou       ON fdou.org_unit_identifier   = p.forest_district_org_unit_id
  LEFT JOIN wfprev.bc_parks_org_unit bcpr     ON bcpr.org_unit_identifier   = p.bc_parks_region_org_unit_id
  LEFT JOIN wfprev.bc_parks_org_unit bcps     ON bcps.org_unit_identifier   = p.bc_parks_section_org_unit_id
  LEFT JOIN wfprev.wildfire_org_unit fc       ON fc.org_unit_identifier     = p.fire_centre_org_unit_id
  LEFT JOIN wfprev.project_plan_fiscal ppf    ON ppf.project_guid           = p.project_guid
  LEFT JOIN wfprev.proposal_type_code prtc    ON prtc.proposal_type_code    = ppf.proposal_type_code
  LEFT JOIN wfprev.activity_category_code acc ON acc.activity_category_code = ppf.activity_category_code
  LEFT JOIN wfprev.plan_fiscal_status_code pfsc ON pfsc.plan_fiscal_status_code = ppf.plan_fiscal_status_code
  LEFT JOIN wfprev.funding_stream fs          ON fs.funding_stream_guid     = p.funding_stream_guid
  LEFT JOIN wfprev.objective_type_code potc   ON potc.objective_type_code   = p.primary_objective_type_code
  LEFT JOIN wfprev.objective_type_code sotc   ON sotc.objective_type_code   = p.secondary_objective_type_code
  LEFT JOIN wfprev.eval_criteria_summary ecs  ON ecs.project_guid           = p.project_guid
  LEFT JOIN wfprev.wui_risk_class_code rcc    ON rcc.wui_risk_class_code    = ecs.wui_risk_class_code
  LEFT JOIN wfprev.wui_risk_class_code lrcc   ON lrcc.wui_risk_class_code   = ecs.local_wui_risk_class_code
  LEFT JOIN LATERAL (
    SELECT * 
    FROM wfprev.project_plan_fiscal_perf sub
    WHERE sub.project_plan_fiscal_guid = ppf.project_plan_fiscal_guid
      AND sub.reporting_period_code = 'Q1'
    ORDER BY sub.submitted_timestamp DESC
    LIMIT 1
  ) ppfp_q1 ON true
  LEFT JOIN LATERAL (
    SELECT * 
    FROM wfprev.project_plan_fiscal_perf sub
    WHERE sub.project_plan_fiscal_guid = ppf.project_plan_fiscal_guid
      AND sub.reporting_period_code = 'Q2'
    ORDER BY sub.submitted_timestamp DESC
    LIMIT 1
  ) ppfp_q2 ON true
  LEFT JOIN LATERAL (
    SELECT * 
    FROM wfprev.project_plan_fiscal_perf sub
    WHERE sub.project_plan_fiscal_guid = ppf.project_plan_fiscal_guid
      AND sub.reporting_period_code = 'Q3'
    ORDER BY sub.submitted_timestamp DESC
    LIMIT 1
  ) ppfp_q3 ON true
  LEFT JOIN LATERAL (
    SELECT * 
    FROM wfprev.project_plan_fiscal_perf sub
    WHERE sub.project_plan_fiscal_guid = ppf.project_plan_fiscal_guid
      AND sub.reporting_period_code = 'MARCH7'
    ORDER BY sub.submitted_timestamp DESC
    LIMIT 1
  ) ppfp_m7 ON true
  LEFT JOIN LATERAL (
    SELECT * 
    FROM wfprev.project_plan_fiscal_perf sub
    WHERE sub.project_plan_fiscal_guid = ppf.project_plan_fiscal_guid
      AND sub.reporting_period_code = 'CUSTOM'
    ORDER BY sub.submitted_timestamp DESC
    LIMIT 1
  ) ppfp_oth ON true
WHERE  p.project_type_code = 'FUEL_MGMT'
ORDER BY p.project_guid, p.project_name, ppf.fiscal_year;
