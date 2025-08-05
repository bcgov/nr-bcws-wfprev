DROP VIEW IF EXISTS wfprev.project_crx_vw;

CREATE or REPLACE VIEW wfprev.project_crx_vw AS 
SELECT p.project_guid, 
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
       fc.org_unit_name AS fire_centre_org_unit_name, -- todo does not match data returned by API
       p.site_unit_name AS planning_unit_name,
       p.total_actual_project_size_ha AS gross_project_area_ha,
       p.closest_community_name, 
       p.project_lead,
       ppf.project_plan_fiscal_guid,
       ppf.business_area_comment,
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
       p.total_estimated_cost_amount, 
       ppf.fiscal_ancillary_fund_amount, 
       ppf.fiscal_reported_spend_amount, ppf.fiscal_actual_amount,
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
       ecs.outside_wui_ind,
       ecs.wui_risk_class_code, 
       rcc.description AS wui_risk_class_description,
       ecs.local_wui_risk_class_code, 
       lrcc.description AS local_wui_risk_class_description,
       (SELECT COALESCE(SUM(COALESCE(filter_section_score,0)),0) 
        FROM wfprev.eval_criteria_sect_summ ecss 
          JOIN wfprev.eval_criteria_selected ecsel ON ecsel.eval_criteria_sect_summ_guid = ecss.eval_criteria_sect_summ_guid 
                                                  AND ecsel.evaluation_criteria_select_ind = true
          JOIN wfprev.evaluation_criteria ec ON ec.evaluation_criteria_guid = ecsel.evaluation_criteria_guid 
                                            AND ec.project_type_code = 'CULT_RX_FR'
        WHERE ecss.eval_criteria_summary_guid = ecs.eval_criteria_summary_guid 
          AND ecss.eval_criteria_sect_code = 'RCL'
       ) AS total_rcl_filter_section_score,
       (SELECT string_agg(filter_section_comment, '| ') 
        FROM wfprev.eval_criteria_sect_summ ecss 
          JOIN wfprev.eval_criteria_selected ecsel ON ecsel.eval_criteria_sect_summ_guid = ecss.eval_criteria_sect_summ_guid 
                                                  AND ecsel.evaluation_criteria_select_ind = true
          JOIN wfprev.evaluation_criteria ec ON ec.evaluation_criteria_guid = ecsel.evaluation_criteria_guid 
                                            AND ec.project_type_code = 'CULT_RX_FR'
        WHERE ecss.eval_criteria_summary_guid = ecs.eval_criteria_summary_guid 
          AND ecss.eval_criteria_sect_code = 'RCL'
       ) AS rcl_filter_section_comment,
       (SELECT COALESCE(SUM(COALESCE(filter_section_score,0)),0) 
        FROM wfprev.eval_criteria_sect_summ ecss 
          JOIN wfprev.eval_criteria_selected ecsel ON ecsel.eval_criteria_sect_summ_guid = ecss.eval_criteria_sect_summ_guid 
                                                  AND ecsel.evaluation_criteria_select_ind = true
          JOIN wfprev.evaluation_criteria ec ON ec.evaluation_criteria_guid = ecsel.evaluation_criteria_guid 
                                            AND ec.project_type_code = 'CULT_RX_FR'
        WHERE ecss.eval_criteria_summary_guid = ecs.eval_criteria_summary_guid 
          AND ecss.eval_criteria_sect_code = 'BDF'
       ) AS total_bdf_filter_section_score,
       (SELECT string_agg(filter_section_comment, '| ') 
        FROM wfprev.eval_criteria_sect_summ ecss 
        WHERE ecss.eval_criteria_summary_guid = ecs.eval_criteria_summary_guid 
          AND ecss.eval_criteria_sect_code = 'BDF'
       ) AS bdf_filter_section_comment,
       (SELECT COALESCE(SUM(COALESCE(filter_section_score,0)),0) 
        FROM wfprev.eval_criteria_sect_summ ecss 
          JOIN wfprev.eval_criteria_selected ecsel ON ecsel.eval_criteria_sect_summ_guid = ecss.eval_criteria_sect_summ_guid 
                                                  AND ecsel.evaluation_criteria_select_ind = true
          JOIN wfprev.evaluation_criteria ec ON ec.evaluation_criteria_guid = ecsel.evaluation_criteria_guid 
                                            AND ec.project_type_code = 'CULT_RX_FR'
        WHERE ecss.eval_criteria_summary_guid = ecs.eval_criteria_summary_guid 
          AND ecss.eval_criteria_sect_code = 'COLL_IMP'
       ) AS total_collimp_filter_section_score,
       (SELECT string_agg(filter_section_comment, '| ') 
        FROM wfprev.eval_criteria_sect_summ ecss 
        WHERE ecss.eval_criteria_summary_guid = ecs.eval_criteria_summary_guid 
          AND ecss.eval_criteria_sect_code = 'COLL_IMP'
       ) AS collimp_filter_section_comment,
       (SELECT COALESCE(SUM(COALESCE(filter_section_score,0)),0) 
        FROM wfprev.eval_criteria_sect_summ ecss 
          JOIN wfprev.eval_criteria_selected ecsel ON ecsel.eval_criteria_sect_summ_guid = ecss.eval_criteria_sect_summ_guid 
                                                  AND ecsel.evaluation_criteria_select_ind = true
          JOIN wfprev.evaluation_criteria ec ON ec.evaluation_criteria_guid = ecsel.evaluation_criteria_guid 
                                            AND ec.project_type_code = 'CULT_RX_FR'
        WHERE ecss.eval_criteria_summary_guid = ecs.eval_criteria_summary_guid 
          AND ecss.eval_criteria_sect_code IN ( 'COLL_IMP', 'BDF', 'RCL')
       ) AS total_filter_section_score
FROM wfprev.project p 
  JOIN wfprev.project_type_code ptc           ON ptc.project_type_code      = p.project_type_code
  JOIN wfprev.forest_org_unit frou            ON frou.org_unit_identifier   = p.forest_region_org_unit_id
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
WHERE p.project_type_code = 'CULT_RX_FR'
ORDER BY p.project_guid, p.project_name, ppf.fiscal_year;