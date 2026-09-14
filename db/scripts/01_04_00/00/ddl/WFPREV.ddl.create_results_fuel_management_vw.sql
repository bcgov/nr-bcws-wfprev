DROP VIEW IF EXISTS wfprev.results_fuel_management_vw;

CREATE OR REPLACE VIEW wfprev.results_fuel_management_vw AS
SELECT p.project_name,
       ppf.project_fiscal_name,
       a.activity_name,
       CASE WHEN a.is_results_reportable_ind THEN 'Y' ELSE 'N' END AS is_results_reportable_ind,
       a.activity_description,
       a.activity_status_code,
       CASE
         WHEN ppf.fiscal_year IS NOT NULL
         THEN ppf.fiscal_year::int::text || '/' || right((ppf.fiscal_year::int + 1)::text, 2)
       END AS fiscal_year,
       fdou.org_unit_name AS forest_district_name,
       p.project_lead_email_address,
       p.results_project_code,
       ppf.results_opening_id,
       NULL AS results_opening_action,
       NULL AS results_opening_category,
       ab.boundary_size_ha,
       NULL AS max_permanent_access_percent,
       sbc.description AS activity_base_name,
       stc.description AS technique_name,
       smc.description AS method_name,
       potc.description AS primary_objective_name,
       sotc.description AS secondary_objective_name,
       NULL AS additional_objective_name,
       a.activity_end_date,
       a.completed_area_ha,
       afs.funding_source_abbreviation AS funding_source_code,
       NULL AS comment,
       NULL AS tenure_number,
       a.planned_treatment_area_ha,
       cpc.description AS contract_phase_name,
       ppf.cfs_project_code,
       CASE WHEN a.previous_carry_forward_ind THEN 'Y' ELSE 'N' END AS previous_carry_forward_ind,
       CASE WHEN a.carry_forward_ind THEN 'Y' ELSE 'N' END AS carry_forward_ind,
       a.final_outcome_comments,
       CASE WHEN a.outstanding_obligations_ind THEN 'Y' ELSE 'N' END AS outstanding_obligations_ind
FROM wfprev.project p
  LEFT JOIN wfprev.forest_org_unit fdou             ON fdou.org_unit_identifier = p.forest_district_org_unit_id
  LEFT JOIN wfprev.project_plan_fiscal ppf          ON ppf.project_guid = p.project_guid
  LEFT JOIN wfprev.activity a                       ON a.project_plan_fiscal_guid = ppf.project_plan_fiscal_guid
  LEFT JOIN wfprev.activity_boundary ab             ON ab.activity_guid = a.activity_guid
  LEFT JOIN wfprev.silviculture_base sb             ON sb.silviculture_base_guid = a.silviculture_base_guid
  LEFT JOIN wfprev.silviculture_base_code sbc       ON sbc.silviculture_base_code = sb.silviculture_base_code
  LEFT JOIN wfprev.silviculture_technique st        ON st.silviculture_technique_guid = a.silviculture_technique_guid
  LEFT JOIN wfprev.silviculture_technique_code stc  ON stc.silviculture_technique_code = st.silviculture_technique_code
  LEFT JOIN wfprev.silviculture_method sm           ON sm.silviculture_method_guid = a.silviculture_method_guid
  LEFT JOIN wfprev.silviculture_method_code smc     ON smc.silviculture_method_code = sm.silviculture_method_code
  LEFT JOIN wfprev.objective_type_code potc         ON potc.objective_type_code = p.primary_objective_type_code
  LEFT JOIN wfprev.objective_type_code sotc         ON sotc.objective_type_code = p.secondary_objective_type_code
  LEFT JOIN wfprev.activity_funding_source afs      ON afs.activity_funding_source_guid = a.activity_funding_source_guid
  LEFT JOIN wfprev.contract_phase_code cpc          ON cpc.contract_phase_code = a.contract_phase_code
WHERE p.project_type_code = 'FUEL_MGMT';