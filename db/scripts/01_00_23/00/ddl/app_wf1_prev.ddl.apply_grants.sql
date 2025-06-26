--*****************************************************************************
-- Purpose: Apply grants against roles
-- Author:  Vivid Solutions Inc.
-- Note: Query to generate grants 
--    
--    SELECT 1 display_order,
--           t.table_name,
--           'GRANT SELECT, INSERT, UPDATE, DELETE ON wfprev.' || lower(t.table_name) || ' TO app_wf1_prev_rest_proxy;'
--    FROM information_schema.tables  t
--    WHERE t.table_schema='wfprev'
--    UNION
--    SELECT 2 display_order,
--           t.table_name,
--           'GRANT SELECT, INSERT, UPDATE, DELETE ON wfprev.' || lower(t.table_name) || ' TO app_wf1_prev_custodian;'
--    FROM information_schema.tables  t
--    WHERE t.table_schema='wfprev'
--    ORDER BY 1, 2;
--*****************************************************************************

--Table and View Grants
GRANT SELECT, INSERT, UPDATE, DELETE ON wfprev.activity TO app_wf1_prev_rest_proxy;
GRANT SELECT, INSERT, UPDATE, DELETE ON wfprev.activity_audit TO app_wf1_prev_rest_proxy;
GRANT SELECT, INSERT, UPDATE, DELETE ON wfprev.activity_boundary TO app_wf1_prev_rest_proxy;
GRANT SELECT, INSERT, UPDATE, DELETE ON wfprev.activity_category_code TO app_wf1_prev_rest_proxy;
GRANT SELECT, INSERT, UPDATE, DELETE ON wfprev.activity_funding_source TO app_wf1_prev_rest_proxy;
GRANT SELECT, INSERT, UPDATE, DELETE ON wfprev.activity_progress TO app_wf1_prev_rest_proxy;
GRANT SELECT, INSERT, UPDATE, DELETE ON wfprev.activity_progress_audit TO app_wf1_prev_rest_proxy;
GRANT SELECT, INSERT, UPDATE, DELETE ON wfprev.activity_status_code TO app_wf1_prev_rest_proxy;
GRANT SELECT, INSERT, UPDATE, DELETE ON wfprev.attachment_content_type_code TO app_wf1_prev_rest_proxy;
GRANT SELECT, INSERT, UPDATE, DELETE ON wfprev.bc_parks_org_unit TO app_wf1_prev_rest_proxy;
GRANT SELECT, INSERT, UPDATE, DELETE ON wfprev.bc_parks_org_unit_type_code TO app_wf1_prev_rest_proxy;
GRANT SELECT, INSERT, UPDATE, DELETE ON wfprev.burn_impl_season_code TO app_wf1_prev_rest_proxy;
GRANT SELECT, INSERT, UPDATE, DELETE ON wfprev.contract_phase_code TO app_wf1_prev_rest_proxy;
GRANT SELECT, INSERT, UPDATE, DELETE ON wfprev.cultural_rx_fire_plan TO app_wf1_prev_rest_proxy;
GRANT SELECT, INSERT, UPDATE, DELETE ON wfprev.cultural_rx_fire_plan_audit TO app_wf1_prev_rest_proxy;
GRANT SELECT, INSERT, UPDATE, DELETE ON wfprev.endorsement_code TO app_wf1_prev_rest_proxy;
GRANT SELECT, INSERT, UPDATE, DELETE ON wfprev.eval_criteria_sect_code TO app_wf1_prev_rest_proxy;
GRANT SELECT, INSERT, UPDATE, DELETE ON wfprev.eval_criteria_sect_summ TO app_wf1_prev_rest_proxy;
GRANT SELECT, INSERT, UPDATE, DELETE ON wfprev.eval_criteria_selected TO app_wf1_prev_rest_proxy;
GRANT SELECT, INSERT, UPDATE, DELETE ON wfprev.eval_criteria_summary TO app_wf1_prev_rest_proxy;
GRANT SELECT, INSERT, UPDATE, DELETE ON wfprev.evaluation_criteria TO app_wf1_prev_rest_proxy;
GRANT SELECT, INSERT, UPDATE, DELETE ON wfprev.event_history TO app_wf1_prev_rest_proxy;
GRANT SELECT, INSERT, UPDATE, DELETE ON wfprev.event_history_type_code TO app_wf1_prev_rest_proxy;
GRANT SELECT, INSERT, UPDATE, DELETE ON wfprev.file_attachment TO app_wf1_prev_rest_proxy;
GRANT SELECT, INSERT, UPDATE, DELETE ON wfprev.forest_area_code TO app_wf1_prev_rest_proxy;
GRANT SELECT, INSERT, UPDATE, DELETE ON wfprev.forest_org_unit TO app_wf1_prev_rest_proxy;
GRANT SELECT, INSERT, UPDATE, DELETE ON wfprev.forest_org_unit_type_code TO app_wf1_prev_rest_proxy;
GRANT SELECT, INSERT, UPDATE, DELETE ON wfprev.fuel_management_plan TO app_wf1_prev_rest_proxy;
GRANT SELECT, INSERT, UPDATE, DELETE ON wfprev.fuel_management_plan_audit TO app_wf1_prev_rest_proxy;
GRANT SELECT, INSERT, UPDATE, DELETE ON wfprev.funding_stream TO app_wf1_prev_rest_proxy;
GRANT SELECT, INSERT, UPDATE, DELETE ON wfprev.general_scope_code TO app_wf1_prev_rest_proxy;
GRANT SELECT, INSERT, UPDATE, DELETE ON wfprev.objective_type_code TO app_wf1_prev_rest_proxy;
GRANT SELECT, INSERT, UPDATE, DELETE ON wfprev.plan_fiscal_status_code TO app_wf1_prev_rest_proxy;
GRANT SELECT, INSERT, UPDATE, DELETE ON wfprev.program_area TO app_wf1_prev_rest_proxy;
GRANT SELECT, INSERT, UPDATE, DELETE ON wfprev.project TO app_wf1_prev_rest_proxy;
GRANT SELECT, INSERT, UPDATE, DELETE ON wfprev.project_audit TO app_wf1_prev_rest_proxy;
GRANT SELECT, INSERT, UPDATE, DELETE ON wfprev.project_boundary TO app_wf1_prev_rest_proxy;
GRANT SELECT, INSERT, UPDATE, DELETE ON wfprev.project_boundary_audit TO app_wf1_prev_rest_proxy;
GRANT SELECT, INSERT, UPDATE, DELETE ON wfprev.project_plan_fiscal TO app_wf1_prev_rest_proxy;
GRANT SELECT, INSERT, UPDATE, DELETE ON wfprev.project_plan_fiscal_audit TO app_wf1_prev_rest_proxy;
GRANT SELECT, INSERT, UPDATE, DELETE ON wfprev.project_plan_fiscal_perf TO app_wf1_prev_rest_proxy;
GRANT SELECT, INSERT, UPDATE, DELETE ON wfprev.project_plan_fiscal_perf_audit TO app_wf1_prev_rest_proxy;
GRANT SELECT, INSERT, UPDATE, DELETE ON wfprev.project_plan_status_code TO app_wf1_prev_rest_proxy;
GRANT SELECT, INSERT, UPDATE, DELETE ON wfprev.project_status_code TO app_wf1_prev_rest_proxy;
GRANT SELECT, INSERT, UPDATE, DELETE ON wfprev.project_type_act_cat_xref TO app_wf1_prev_rest_proxy;
GRANT SELECT, INSERT, UPDATE, DELETE ON wfprev.project_type_code TO app_wf1_prev_rest_proxy;
GRANT SELECT, INSERT, UPDATE, DELETE ON wfprev.proposal_type_code TO app_wf1_prev_rest_proxy;
GRANT SELECT, INSERT, UPDATE, DELETE ON wfprev.reporting_period_code TO app_wf1_prev_rest_proxy;
GRANT SELECT, INSERT, UPDATE, DELETE ON wfprev.risk_rating_code TO app_wf1_prev_rest_proxy;
GRANT SELECT, INSERT, UPDATE, DELETE ON wfprev.silviculture_base TO app_wf1_prev_rest_proxy;
GRANT SELECT, INSERT, UPDATE, DELETE ON wfprev.silviculture_base_code TO app_wf1_prev_rest_proxy;
GRANT SELECT, INSERT, UPDATE, DELETE ON wfprev.silviculture_method TO app_wf1_prev_rest_proxy;
GRANT SELECT, INSERT, UPDATE, DELETE ON wfprev.silviculture_method_code TO app_wf1_prev_rest_proxy;
GRANT SELECT, INSERT, UPDATE, DELETE ON wfprev.silviculture_technique TO app_wf1_prev_rest_proxy;
GRANT SELECT, INSERT, UPDATE, DELETE ON wfprev.silviculture_technique_code TO app_wf1_prev_rest_proxy;
GRANT SELECT, INSERT, UPDATE, DELETE ON wfprev.source_object_name_code TO app_wf1_prev_rest_proxy;
GRANT SELECT, INSERT, UPDATE, DELETE ON wfprev.wildfire_org_unit TO app_wf1_prev_rest_proxy;
GRANT SELECT, INSERT, UPDATE, DELETE ON wfprev.wildfire_org_unit_type_code TO app_wf1_prev_rest_proxy;
GRANT SELECT, INSERT, UPDATE, DELETE ON wfprev.wui_risk_class_code TO app_wf1_prev_rest_proxy;
GRANT SELECT, INSERT, UPDATE, DELETE ON wfprev.wui_risk_class_rank TO app_wf1_prev_rest_proxy;
GRANT SELECT, INSERT, UPDATE, DELETE ON wfprev.activity TO app_wf1_prev_custodian;
GRANT SELECT, INSERT, UPDATE, DELETE ON wfprev.activity_audit TO app_wf1_prev_custodian;
GRANT SELECT, INSERT, UPDATE, DELETE ON wfprev.activity_boundary TO app_wf1_prev_custodian;
GRANT SELECT, INSERT, UPDATE, DELETE ON wfprev.activity_category_code TO app_wf1_prev_custodian;
GRANT SELECT, INSERT, UPDATE, DELETE ON wfprev.activity_funding_source TO app_wf1_prev_custodian;
GRANT SELECT, INSERT, UPDATE, DELETE ON wfprev.activity_progress TO app_wf1_prev_custodian;
GRANT SELECT, INSERT, UPDATE, DELETE ON wfprev.activity_progress_audit TO app_wf1_prev_custodian;
GRANT SELECT, INSERT, UPDATE, DELETE ON wfprev.activity_status_code TO app_wf1_prev_custodian;
GRANT SELECT, INSERT, UPDATE, DELETE ON wfprev.attachment_content_type_code TO app_wf1_prev_custodian;
GRANT SELECT, INSERT, UPDATE, DELETE ON wfprev.bc_parks_org_unit TO app_wf1_prev_custodian;
GRANT SELECT, INSERT, UPDATE, DELETE ON wfprev.bc_parks_org_unit_type_code TO app_wf1_prev_custodian;
GRANT SELECT, INSERT, UPDATE, DELETE ON wfprev.burn_impl_season_code TO app_wf1_prev_custodian;
GRANT SELECT, INSERT, UPDATE, DELETE ON wfprev.contract_phase_code TO app_wf1_prev_custodian;
GRANT SELECT, INSERT, UPDATE, DELETE ON wfprev.cultural_rx_fire_plan TO app_wf1_prev_custodian;
GRANT SELECT, INSERT, UPDATE, DELETE ON wfprev.cultural_rx_fire_plan_audit TO app_wf1_prev_custodian;
GRANT SELECT, INSERT, UPDATE, DELETE ON wfprev.endorsement_code TO app_wf1_prev_custodian;
GRANT SELECT, INSERT, UPDATE, DELETE ON wfprev.eval_criteria_sect_code TO app_wf1_prev_custodian;
GRANT SELECT, INSERT, UPDATE, DELETE ON wfprev.eval_criteria_sect_summ TO app_wf1_prev_custodian;
GRANT SELECT, INSERT, UPDATE, DELETE ON wfprev.eval_criteria_selected TO app_wf1_prev_custodian;
GRANT SELECT, INSERT, UPDATE, DELETE ON wfprev.eval_criteria_summary TO app_wf1_prev_custodian;
GRANT SELECT, INSERT, UPDATE, DELETE ON wfprev.evaluation_criteria TO app_wf1_prev_custodian;
GRANT SELECT, INSERT, UPDATE, DELETE ON wfprev.event_history TO app_wf1_prev_custodian;
GRANT SELECT, INSERT, UPDATE, DELETE ON wfprev.event_history_type_code TO app_wf1_prev_custodian;
GRANT SELECT, INSERT, UPDATE, DELETE ON wfprev.file_attachment TO app_wf1_prev_custodian;
GRANT SELECT, INSERT, UPDATE, DELETE ON wfprev.forest_area_code TO app_wf1_prev_custodian;
GRANT SELECT, INSERT, UPDATE, DELETE ON wfprev.forest_org_unit TO app_wf1_prev_custodian;
GRANT SELECT, INSERT, UPDATE, DELETE ON wfprev.forest_org_unit_type_code TO app_wf1_prev_custodian;
GRANT SELECT, INSERT, UPDATE, DELETE ON wfprev.fuel_management_plan TO app_wf1_prev_custodian;
GRANT SELECT, INSERT, UPDATE, DELETE ON wfprev.fuel_management_plan_audit TO app_wf1_prev_custodian;
GRANT SELECT, INSERT, UPDATE, DELETE ON wfprev.funding_stream TO app_wf1_prev_custodian;
GRANT SELECT, INSERT, UPDATE, DELETE ON wfprev.general_scope_code TO app_wf1_prev_custodian;
GRANT SELECT, INSERT, UPDATE, DELETE ON wfprev.objective_type_code TO app_wf1_prev_custodian;
GRANT SELECT, INSERT, UPDATE, DELETE ON wfprev.plan_fiscal_status_code TO app_wf1_prev_custodian;
GRANT SELECT, INSERT, UPDATE, DELETE ON wfprev.program_area TO app_wf1_prev_custodian;
GRANT SELECT, INSERT, UPDATE, DELETE ON wfprev.project TO app_wf1_prev_custodian;
GRANT SELECT, INSERT, UPDATE, DELETE ON wfprev.project_audit TO app_wf1_prev_custodian;
GRANT SELECT, INSERT, UPDATE, DELETE ON wfprev.project_boundary TO app_wf1_prev_custodian;
GRANT SELECT, INSERT, UPDATE, DELETE ON wfprev.project_boundary_audit TO app_wf1_prev_custodian;
GRANT SELECT, INSERT, UPDATE, DELETE ON wfprev.project_plan_fiscal TO app_wf1_prev_custodian;
GRANT SELECT, INSERT, UPDATE, DELETE ON wfprev.project_plan_fiscal_audit TO app_wf1_prev_custodian;
GRANT SELECT, INSERT, UPDATE, DELETE ON wfprev.project_plan_fiscal_perf TO app_wf1_prev_custodian;
GRANT SELECT, INSERT, UPDATE, DELETE ON wfprev.project_plan_fiscal_perf_audit TO app_wf1_prev_custodian;
GRANT SELECT, INSERT, UPDATE, DELETE ON wfprev.project_plan_status_code TO app_wf1_prev_custodian;
GRANT SELECT, INSERT, UPDATE, DELETE ON wfprev.project_status_code TO app_wf1_prev_custodian;
GRANT SELECT, INSERT, UPDATE, DELETE ON wfprev.project_type_act_cat_xref TO app_wf1_prev_custodian;
GRANT SELECT, INSERT, UPDATE, DELETE ON wfprev.project_type_code TO app_wf1_prev_custodian;
GRANT SELECT, INSERT, UPDATE, DELETE ON wfprev.proposal_type_code TO app_wf1_prev_custodian;
GRANT SELECT, INSERT, UPDATE, DELETE ON wfprev.reporting_period_code TO app_wf1_prev_custodian;
GRANT SELECT, INSERT, UPDATE, DELETE ON wfprev.risk_rating_code TO app_wf1_prev_custodian;
GRANT SELECT, INSERT, UPDATE, DELETE ON wfprev.silviculture_base TO app_wf1_prev_custodian;
GRANT SELECT, INSERT, UPDATE, DELETE ON wfprev.silviculture_base_code TO app_wf1_prev_custodian;
GRANT SELECT, INSERT, UPDATE, DELETE ON wfprev.silviculture_method TO app_wf1_prev_custodian;
GRANT SELECT, INSERT, UPDATE, DELETE ON wfprev.silviculture_method_code TO app_wf1_prev_custodian;
GRANT SELECT, INSERT, UPDATE, DELETE ON wfprev.silviculture_technique TO app_wf1_prev_custodian;
GRANT SELECT, INSERT, UPDATE, DELETE ON wfprev.silviculture_technique_code TO app_wf1_prev_custodian;
GRANT SELECT, INSERT, UPDATE, DELETE ON wfprev.source_object_name_code TO app_wf1_prev_custodian;
GRANT SELECT, INSERT, UPDATE, DELETE ON wfprev.wildfire_org_unit TO app_wf1_prev_custodian;
GRANT SELECT, INSERT, UPDATE, DELETE ON wfprev.wildfire_org_unit_type_code TO app_wf1_prev_custodian;
GRANT SELECT, INSERT, UPDATE, DELETE ON wfprev.wui_risk_class_code TO app_wf1_prev_custodian;
GRANT SELECT, INSERT, UPDATE, DELETE ON wfprev.wui_risk_class_rank TO app_wf1_prev_custodian;





















