
CREATE OR REPLACE FUNCTION wfprev.fnc_audit_activity() RETURNS TRIGGER AS $$
DECLARE 
  v_audit_action VARCHAR(10); 
BEGIN 
  v_audit_action := TG_OP; 
  IF ((TG_OP = 'INSERT') OR (TG_OP = 'UPDATE')) THEN 
    INSERT INTO wfprev.activity_audit( 
      audit_table_sequence, 
      audit_action_code, 
      activity_guid,
      project_plan_fiscal_guid,
      activity_status_code,
      silviculture_base_guid,
      silviculture_technique_guid,
      silviculture_method_guid,
      risk_rating_code,
      contract_phase_code,
      activity_funding_source_guid,
      activity_name,
      activity_description,
      activity_start_date,
      activity_end_date,
      planned_spend_amount,
      planned_treatment_area_ha,
      reported_spend_amount,
      completed_area_ha,
      is_results_reportable_ind,
      outstanding_obligations_ind,
      activity_comment,
      is_spatial_added_ind,
      revision_count,
      create_user,
      create_date,
      update_user,
      update_date 
    ) 
    VALUES ( 
      nextval('wfprev.prev_audit_table_seq'),
      v_audit_action, 
      NEW.activity_guid,
      NEW.project_plan_fiscal_guid,
      NEW.activity_status_code,
      NEW.silviculture_base_guid,
      NEW.silviculture_technique_guid,
      NEW.silviculture_method_guid,
      NEW.risk_rating_code,
      NEW.contract_phase_code,
      NEW.activity_funding_source_guid,
      NEW.activity_name,
      NEW.activity_description,
      NEW.activity_start_date,
      NEW.activity_end_date,
      NEW.planned_spend_amount,
      NEW.planned_treatment_area_ha,
      NEW.reported_spend_amount,
      NEW.completed_area_ha,
      NEW.is_results_reportable_ind,
      NEW.outstanding_obligations_ind,
      NEW.activity_comment,
      NEW.is_spatial_added_ind,
      NEW.revision_count,
      NEW.create_user,
      NEW.create_date,
      NEW.update_user,
      NEW.update_date 
    ); 
    RETURN NEW; 

  ELSIF (TG_OP = 'DELETE') THEN 
    INSERT INTO wfprev.activity_audit( 
      audit_table_sequence, 
      audit_action_code, 
      activity_guid,
      project_plan_fiscal_guid,
      activity_status_code,
      silviculture_base_guid,
      silviculture_technique_guid,
      silviculture_method_guid,
      risk_rating_code,
      contract_phase_code,
      activity_funding_source_guid,
      activity_name,
      activity_description,
      activity_start_date,
      activity_end_date,
      planned_spend_amount,
      planned_treatment_area_ha,
      reported_spend_amount,
      completed_area_ha,
      is_results_reportable_ind,
      outstanding_obligations_ind,
      activity_comment,
      is_spatial_added_ind,
      revision_count,
      create_user,
      create_date,
      update_user,
      update_date 
    ) 
    VALUES ( 
      nextval('wfprev.prev_audit_table_seq'),
      v_audit_action, 
      OLD.activity_guid,
      OLD.project_plan_fiscal_guid,
      OLD.activity_status_code,
      OLD.silviculture_base_guid,
      OLD.silviculture_technique_guid,
      OLD.silviculture_method_guid,
      OLD.risk_rating_code,
      OLD.contract_phase_code,
      OLD.activity_funding_source_guid,
      OLD.activity_name,
      OLD.activity_description,
      OLD.activity_start_date,
      OLD.activity_end_date,
      OLD.planned_spend_amount,
      OLD.planned_treatment_area_ha,
      OLD.reported_spend_amount,
      OLD.completed_area_ha,
      OLD.is_results_reportable_ind,
      OLD.outstanding_obligations_ind,
      OLD.activity_comment,
      OLD.is_spatial_added_ind,
      OLD.revision_count,
      OLD.create_user,
      OLD.create_date,
      OLD.update_user,
      OLD.update_date 
    ); 
    RETURN OLD; 

  ELSE 
    RAISE WARNING '[wfprev.fnc_audit_activity] - Other action occurred: %, at %',TG_OP,now(); 
    RETURN NULL; 
  END IF; 

EXCEPTION 
  WHEN data_exception THEN 
      RAISE WARNING '[wfprev.fnc_audit_activity] - UDF ERROR [DATA EXCEPTION] - SQLSTATE: %, SQLERRM: %',SQLSTATE,SQLERRM; 
      RETURN NULL; 
  WHEN unique_violation THEN 
      RAISE WARNING '[wfprev.fnc_audit_activity] - UDF ERROR [UNIQUE] - SQLSTATE: %, SQLERRM: %',SQLSTATE,SQLERRM; 
      RETURN NULL; 
  WHEN others THEN 
      RAISE WARNING '[wfprev.fnc_audit_activity] - UDF ERROR [OTHER] - SQLSTATE: %, SQLERRM: %',SQLSTATE,SQLERRM; 
      RETURN NULL; 
END; 
$$ LANGUAGE plpgsql; 


CREATE OR REPLACE TRIGGER trigger_audit_activity
AFTER INSERT OR UPDATE OR DELETE ON wfprev.activity 
FOR EACH ROW EXECUTE PROCEDURE wfprev.fnc_audit_activity();


CREATE OR REPLACE FUNCTION wfprev.fnc_audit_activity_progress() RETURNS TRIGGER AS $$
DECLARE 
  v_audit_action VARCHAR(10); 
BEGIN 
  v_audit_action := TG_OP; 
  IF ((TG_OP = 'INSERT') OR (TG_OP = 'UPDATE')) THEN 
    INSERT INTO wfprev.activity_progress_audit( 
      audit_table_sequence, 
      audit_action_code, 
      activity_progress_guid,
      activity_guid,
      project_plan_fiscal_perf_guid,
      entered_timestamp,
      activity_status_code,
      contract_phase_code,
      risk_rating_code,
      planned_spend_amount,
      reported_spend_amount,
      completed_area_ha,
      progress_comment,
      commenter_userid,
      commenter_guid,
      system_generated_ind,
      outstanding_obligations_ind,
      activity_comment,
      revision_count,
      create_user,
      create_date,
      update_user,
      update_date 
    ) 
    VALUES ( 
      nextval('wfprev.prev_audit_table_seq'),
      v_audit_action, 
      NEW.activity_progress_guid,
      NEW.activity_guid,
      NEW.project_plan_fiscal_perf_guid,
      NEW.entered_timestamp,
      NEW.activity_status_code,
      NEW.contract_phase_code,
      NEW.risk_rating_code,
      NEW.planned_spend_amount,
      NEW.reported_spend_amount,
      NEW.completed_area_ha,
      NEW.progress_comment,
      NEW.commenter_userid,
      NEW.commenter_guid,
      NEW.system_generated_ind,
      NEW.outstanding_obligations_ind,
      NEW.activity_comment,
      NEW.revision_count,
      NEW.create_user,
      NEW.create_date,
      NEW.update_user,
      NEW.update_date 
    ); 
    RETURN NEW; 

  ELSIF (TG_OP = 'DELETE') THEN 
    INSERT INTO wfprev.activity_progress_audit( 
      audit_table_sequence, 
      audit_action_code, 
      activity_progress_guid,
      activity_guid,
      project_plan_fiscal_perf_guid,
      entered_timestamp,
      activity_status_code,
      contract_phase_code,
      risk_rating_code,
      planned_spend_amount,
      reported_spend_amount,
      completed_area_ha,
      progress_comment,
      commenter_userid,
      commenter_guid,
      system_generated_ind,
      outstanding_obligations_ind,
      activity_comment,
      revision_count,
      create_user,
      create_date,
      update_user,
      update_date 
    ) 
    VALUES ( 
      nextval('wfprev.prev_audit_table_seq'),
      v_audit_action, 
      OLD.activity_progress_guid,
      OLD.activity_guid,
      OLD.project_plan_fiscal_perf_guid,
      OLD.entered_timestamp,
      OLD.activity_status_code,
      OLD.contract_phase_code,
      OLD.risk_rating_code,
      OLD.planned_spend_amount,
      OLD.reported_spend_amount,
      OLD.completed_area_ha,
      OLD.progress_comment,
      OLD.commenter_userid,
      OLD.commenter_guid,
      OLD.system_generated_ind,
      OLD.outstanding_obligations_ind,
      OLD.activity_comment,
      OLD.revision_count,
      OLD.create_user,
      OLD.create_date,
      OLD.update_user,
      OLD.update_date 
    ); 
    RETURN OLD; 

  ELSE 
    RAISE WARNING '[wfprev.fnc_audit_activity_progress] - Other action occurred: %, at %',TG_OP,now(); 
    RETURN NULL; 
  END IF; 

EXCEPTION 
  WHEN data_exception THEN 
      RAISE WARNING '[wfprev.fnc_audit_activity_progress] - UDF ERROR [DATA EXCEPTION] - SQLSTATE: %, SQLERRM: %',SQLSTATE,SQLERRM; 
      RETURN NULL; 
  WHEN unique_violation THEN 
      RAISE WARNING '[wfprev.fnc_audit_activity_progress] - UDF ERROR [UNIQUE] - SQLSTATE: %, SQLERRM: %',SQLSTATE,SQLERRM; 
      RETURN NULL; 
  WHEN others THEN 
      RAISE WARNING '[wfprev.fnc_audit_activity_progress] - UDF ERROR [OTHER] - SQLSTATE: %, SQLERRM: %',SQLSTATE,SQLERRM; 
      RETURN NULL; 
END; 
$$ LANGUAGE plpgsql; 


CREATE OR REPLACE TRIGGER trigger_audit_activity_progress
AFTER INSERT OR UPDATE OR DELETE ON wfprev.activity_progress 
FOR EACH ROW EXECUTE PROCEDURE wfprev.fnc_audit_activity_progress();


CREATE OR REPLACE FUNCTION wfprev.fnc_audit_cultural_rx_fire_plan() RETURNS TRIGGER AS $$
DECLARE 
  v_audit_action VARCHAR(10); 
BEGIN 
  v_audit_action := TG_OP; 
  IF ((TG_OP = 'INSERT') OR (TG_OP = 'UPDATE')) THEN 
    INSERT INTO wfprev.cultural_rx_fire_plan_audit( 
      audit_table_sequence, 
      audit_action_code, 
      cultural_rx_fire_plan_guid,
      project_plan_fiscal_guid,
      burn_impl_season_code,
      developed_with_first_natio_ind,
      first_nations_partnership_ind,
      maximizes_funding_integr_ind,
      capacity_development_opp_ind,
      revision_count,
      create_user,
      create_date,
      update_user,
      update_date 
    ) 
    VALUES ( 
      nextval('wfprev.prev_audit_table_seq'),
      v_audit_action, 
      NEW.cultural_rx_fire_plan_guid,
      NEW.project_plan_fiscal_guid,
      NEW.burn_impl_season_code,
      NEW.developed_with_first_natio_ind,
      NEW.first_nations_partnership_ind,
      NEW.maximizes_funding_integr_ind,
      NEW.capacity_development_opp_ind,
      NEW.revision_count,
      NEW.create_user,
      NEW.create_date,
      NEW.update_user,
      NEW.update_date 
    ); 
    RETURN NEW; 

  ELSIF (TG_OP = 'DELETE') THEN 
    INSERT INTO wfprev.cultural_rx_fire_plan_audit( 
      audit_table_sequence, 
      audit_action_code, 
      cultural_rx_fire_plan_guid,
      project_plan_fiscal_guid,
      burn_impl_season_code,
      developed_with_first_natio_ind,
      first_nations_partnership_ind,
      maximizes_funding_integr_ind,
      capacity_development_opp_ind,
      revision_count,
      create_user,
      create_date,
      update_user,
      update_date 
    ) 
    VALUES ( 
      nextval('wfprev.prev_audit_table_seq'),
      v_audit_action, 
      OLD.cultural_rx_fire_plan_guid,
      OLD.project_plan_fiscal_guid,
      OLD.burn_impl_season_code,
      OLD.developed_with_first_natio_ind,
      OLD.first_nations_partnership_ind,
      OLD.maximizes_funding_integr_ind,
      OLD.capacity_development_opp_ind,
      OLD.revision_count,
      OLD.create_user,
      OLD.create_date,
      OLD.update_user,
      OLD.update_date 
    ); 
    RETURN OLD; 

  ELSE 
    RAISE WARNING '[wfprev.fnc_audit_cultural_rx_fire_plan] - Other action occurred: %, at %',TG_OP,now(); 
    RETURN NULL; 
  END IF; 

EXCEPTION 
  WHEN data_exception THEN 
      RAISE WARNING '[wfprev.fnc_audit_cultural_rx_fire_plan] - UDF ERROR [DATA EXCEPTION] - SQLSTATE: %, SQLERRM: %',SQLSTATE,SQLERRM; 
      RETURN NULL; 
  WHEN unique_violation THEN 
      RAISE WARNING '[wfprev.fnc_audit_cultural_rx_fire_plan] - UDF ERROR [UNIQUE] - SQLSTATE: %, SQLERRM: %',SQLSTATE,SQLERRM; 
      RETURN NULL; 
  WHEN others THEN 
      RAISE WARNING '[wfprev.fnc_audit_cultural_rx_fire_plan] - UDF ERROR [OTHER] - SQLSTATE: %, SQLERRM: %',SQLSTATE,SQLERRM; 
      RETURN NULL; 
END; 
$$ LANGUAGE plpgsql; 


CREATE OR REPLACE TRIGGER trigger_audit_cultural_rx_fire_plan
AFTER INSERT OR UPDATE OR DELETE ON wfprev.cultural_rx_fire_plan 
FOR EACH ROW EXECUTE PROCEDURE wfprev.fnc_audit_cultural_rx_fire_plan();


CREATE OR REPLACE FUNCTION wfprev.fnc_audit_cultural_rx_fire_project() RETURNS TRIGGER AS $$
DECLARE 
  v_audit_action VARCHAR(10); 
BEGIN 
  v_audit_action := TG_OP; 
  IF ((TG_OP = 'INSERT') OR (TG_OP = 'UPDATE')) THEN 
    INSERT INTO wfprev.cultural_rx_fire_prj_audit( 
      audit_table_sequence, 
      audit_action_code, 
      cultural_rx_fire_project_guid,
      project_guid,
      wui_risk_class_code,
      local_wui_risk_class_code,
      wui_risk_class_comment,
      local_wui_risk_class_rationale,
      coarse_filter_comment,
      revision_count,
      create_user,
      create_date,
      update_user,
      update_date 
    ) 
    VALUES ( 
      nextval('wfprev.prev_audit_table_seq'),
      v_audit_action, 
      NEW.cultural_rx_fire_project_guid,
      NEW.project_guid,
      NEW.wui_risk_class_code,
      NEW.local_wui_risk_class_code,
      NEW.wui_risk_class_comment,
      NEW.local_wui_risk_class_rationale,
      NEW.coarse_filter_comment,
      NEW.revision_count,
      NEW.create_user,
      NEW.create_date,
      NEW.update_user,
      NEW.update_date 
    ); 
    RETURN NEW; 

  ELSIF (TG_OP = 'DELETE') THEN 
    INSERT INTO wfprev.cultural_rx_fire_prj_audit( 
      audit_table_sequence, 
      audit_action_code, 
      cultural_rx_fire_project_guid,
      project_guid,
      wui_risk_class_code,
      local_wui_risk_class_code,
      wui_risk_class_comment,
      local_wui_risk_class_rationale,
      coarse_filter_comment,
      revision_count,
      create_user,
      create_date,
      update_user,
      update_date 
    ) 
    VALUES ( 
      nextval('wfprev.prev_audit_table_seq'),
      v_audit_action, 
      OLD.cultural_rx_fire_project_guid,
      OLD.project_guid,
      OLD.wui_risk_class_code,
      OLD.local_wui_risk_class_code,
      OLD.wui_risk_class_comment,
      OLD.local_wui_risk_class_rationale,
      OLD.coarse_filter_comment,
      OLD.revision_count,
      OLD.create_user,
      OLD.create_date,
      OLD.update_user,
      OLD.update_date 
    ); 
    RETURN OLD; 

  ELSE 
    RAISE WARNING '[wfprev.fnc_audit_cultural_rx_fire_project] - Other action occurred: %, at %',TG_OP,now(); 
    RETURN NULL; 
  END IF; 

EXCEPTION 
  WHEN data_exception THEN 
      RAISE WARNING '[wfprev.fnc_audit_cultural_rx_fire_project] - UDF ERROR [DATA EXCEPTION] - SQLSTATE: %, SQLERRM: %',SQLSTATE,SQLERRM; 
      RETURN NULL; 
  WHEN unique_violation THEN 
      RAISE WARNING '[wfprev.fnc_audit_cultural_rx_fire_project] - UDF ERROR [UNIQUE] - SQLSTATE: %, SQLERRM: %',SQLSTATE,SQLERRM; 
      RETURN NULL; 
  WHEN others THEN 
      RAISE WARNING '[wfprev.fnc_audit_cultural_rx_fire_project] - UDF ERROR [OTHER] - SQLSTATE: %, SQLERRM: %',SQLSTATE,SQLERRM; 
      RETURN NULL; 
END; 
$$ LANGUAGE plpgsql; 


CREATE OR REPLACE TRIGGER trigger_audit_cultural_rx_fire_project
AFTER INSERT OR UPDATE OR DELETE ON wfprev.cultural_rx_fire_project 
FOR EACH ROW EXECUTE PROCEDURE wfprev.fnc_audit_cultural_rx_fire_project();


CREATE OR REPLACE FUNCTION wfprev.fnc_audit_fuel_management_plan() RETURNS TRIGGER AS $$
DECLARE 
  v_audit_action VARCHAR(10); 
BEGIN 
  v_audit_action := TG_OP; 
  IF ((TG_OP = 'INSERT') OR (TG_OP = 'UPDATE')) THEN 
    INSERT INTO wfprev.fuel_management_plan_audit( 
      audit_table_sequence, 
      audit_action_code, 
      fuel_management_plan_guid,
      project_plan_fiscal_guid,
      tactical_plans_outside_wui,
      revision_count,
      create_user,
      create_date,
      update_user,
      update_date 
    ) 
    VALUES ( 
      nextval('wfprev.prev_audit_table_seq'),
      v_audit_action, 
      NEW.fuel_management_plan_guid,
      NEW.project_plan_fiscal_guid,
      NEW.tactical_plans_outside_wui,
      NEW.revision_count,
      NEW.create_user,
      NEW.create_date,
      NEW.update_user,
      NEW.update_date 
    ); 
    RETURN NEW; 

  ELSIF (TG_OP = 'DELETE') THEN 
    INSERT INTO wfprev.fuel_management_plan_audit( 
      audit_table_sequence, 
      audit_action_code, 
      fuel_management_plan_guid,
      project_plan_fiscal_guid,
      tactical_plans_outside_wui,
      revision_count,
      create_user,
      create_date,
      update_user,
      update_date 
    ) 
    VALUES ( 
      nextval('wfprev.prev_audit_table_seq'),
      v_audit_action, 
      OLD.fuel_management_plan_guid,
      OLD.project_plan_fiscal_guid,
      OLD.tactical_plans_outside_wui,
      OLD.revision_count,
      OLD.create_user,
      OLD.create_date,
      OLD.update_user,
      OLD.update_date 
    ); 
    RETURN OLD; 

  ELSE 
    RAISE WARNING '[wfprev.fnc_audit_fuel_management_plan] - Other action occurred: %, at %',TG_OP,now(); 
    RETURN NULL; 
  END IF; 

EXCEPTION 
  WHEN data_exception THEN 
      RAISE WARNING '[wfprev.fnc_audit_fuel_management_plan] - UDF ERROR [DATA EXCEPTION] - SQLSTATE: %, SQLERRM: %',SQLSTATE,SQLERRM; 
      RETURN NULL; 
  WHEN unique_violation THEN 
      RAISE WARNING '[wfprev.fnc_audit_fuel_management_plan] - UDF ERROR [UNIQUE] - SQLSTATE: %, SQLERRM: %',SQLSTATE,SQLERRM; 
      RETURN NULL; 
  WHEN others THEN 
      RAISE WARNING '[wfprev.fnc_audit_fuel_management_plan] - UDF ERROR [OTHER] - SQLSTATE: %, SQLERRM: %',SQLSTATE,SQLERRM; 
      RETURN NULL; 
END; 
$$ LANGUAGE plpgsql; 


CREATE OR REPLACE TRIGGER trigger_audit_fuel_management_plan
AFTER INSERT OR UPDATE OR DELETE ON wfprev.fuel_management_plan 
FOR EACH ROW EXECUTE PROCEDURE wfprev.fnc_audit_fuel_management_plan();


CREATE OR REPLACE FUNCTION wfprev.fnc_audit_fuel_management_project() RETURNS TRIGGER AS $$
DECLARE 
  v_audit_action VARCHAR(10); 
BEGIN 
  v_audit_action := TG_OP; 
  IF ((TG_OP = 'INSERT') OR (TG_OP = 'UPDATE')) THEN 
    INSERT INTO wfprev.fuel_management_project_audit( 
      audit_table_sequence, 
      audit_action_code, 
      fuel_management_project_guid,
      project_guid,
      wui_risk_class_code,
      local_wui_risk_class_code,
      wui_risk_class_comment,
      local_wui_risk_class_rationale,
      coarse_filter_comment,
      government_obj_align_score,
      medium_filter_comment,
      fine_filter_score,
      fine_filter_comment,
      total_filter_score,
      revision_count,
      create_user,
      create_date,
      update_user,
      update_date 
    ) 
    VALUES ( 
      nextval('wfprev.prev_audit_table_seq'),
      v_audit_action, 
      NEW.fuel_management_project_guid,
      NEW.project_guid,
      NEW.wui_risk_class_code,
      NEW.local_wui_risk_class_code,
      NEW.wui_risk_class_comment,
      NEW.local_wui_risk_class_rationale,
      NEW.coarse_filter_comment,
      NEW.government_obj_align_score,
      NEW.medium_filter_comment,
      NEW.fine_filter_score,
      NEW.fine_filter_comment,
      NEW.total_filter_score,
      NEW.revision_count,
      NEW.create_user,
      NEW.create_date,
      NEW.update_user,
      NEW.update_date 
    ); 
    RETURN NEW; 

  ELSIF (TG_OP = 'DELETE') THEN 
    INSERT INTO wfprev.fuel_management_project_audit( 
      audit_table_sequence, 
      audit_action_code, 
      fuel_management_project_guid,
      project_guid,
      wui_risk_class_code,
      local_wui_risk_class_code,
      wui_risk_class_comment,
      local_wui_risk_class_rationale,
      coarse_filter_comment,
      government_obj_align_score,
      medium_filter_comment,
      fine_filter_score,
      fine_filter_comment,
      total_filter_score,
      revision_count,
      create_user,
      create_date,
      update_user,
      update_date 
    ) 
    VALUES ( 
      nextval('wfprev.prev_audit_table_seq'),
      v_audit_action, 
      OLD.fuel_management_project_guid,
      OLD.project_guid,
      OLD.wui_risk_class_code,
      OLD.local_wui_risk_class_code,
      OLD.wui_risk_class_comment,
      OLD.local_wui_risk_class_rationale,
      OLD.coarse_filter_comment,
      OLD.government_obj_align_score,
      OLD.medium_filter_comment,
      OLD.fine_filter_score,
      OLD.fine_filter_comment,
      OLD.total_filter_score,
      OLD.revision_count,
      OLD.create_user,
      OLD.create_date,
      OLD.update_user,
      OLD.update_date 
    ); 
    RETURN OLD; 

  ELSE 
    RAISE WARNING '[wfprev.fnc_audit_fuel_management_project] - Other action occurred: %, at %',TG_OP,now(); 
    RETURN NULL; 
  END IF; 

EXCEPTION 
  WHEN data_exception THEN 
      RAISE WARNING '[wfprev.fnc_audit_fuel_management_project] - UDF ERROR [DATA EXCEPTION] - SQLSTATE: %, SQLERRM: %',SQLSTATE,SQLERRM; 
      RETURN NULL; 
  WHEN unique_violation THEN 
      RAISE WARNING '[wfprev.fnc_audit_fuel_management_project] - UDF ERROR [UNIQUE] - SQLSTATE: %, SQLERRM: %',SQLSTATE,SQLERRM; 
      RETURN NULL; 
  WHEN others THEN 
      RAISE WARNING '[wfprev.fnc_audit_fuel_management_project] - UDF ERROR [OTHER] - SQLSTATE: %, SQLERRM: %',SQLSTATE,SQLERRM; 
      RETURN NULL; 
END; 
$$ LANGUAGE plpgsql; 


CREATE OR REPLACE TRIGGER trigger_audit_fuel_management_project
AFTER INSERT OR UPDATE OR DELETE ON wfprev.fuel_management_project 
FOR EACH ROW EXECUTE PROCEDURE wfprev.fnc_audit_fuel_management_project();


CREATE OR REPLACE FUNCTION wfprev.fnc_audit_project() RETURNS TRIGGER AS $$
DECLARE 
  v_audit_action VARCHAR(10); 
BEGIN 
  v_audit_action := TG_OP; 
  IF ((TG_OP = 'INSERT') OR (TG_OP = 'UPDATE')) THEN 
    INSERT INTO wfprev.project_audit( 
      audit_table_sequence, 
      audit_action_code, 
      project_guid,
      project_type_code,
      project_number,
      site_unit_name,
      primary_objective_type_code,
      primary_funding_source_guid,
      forest_area_code,
      general_scope_code,
      secondary_objective_type_code,
      tertiary_objective_type_code,
      project_status_code,
      program_area_guid,
      forest_region_org_unit_id,
      forest_district_org_unit_id,
      fire_centre_org_unit_id,
      bc_parks_region_org_unit_id,
      bc_parks_section_org_unit_id,
      project_name,
      project_lead,
      project_lead_email_address,
      project_description,
      closest_community_name,
      total_planned_project_size_ha,
      total_planned_cost_per_hectare,
      total_actual_amount,
      total_actual_project_size_ha,
      total_actual_cost_per_hectare_,
      secondary_objective_rationale,
      tertiary_objective_rationale,
      is_multi_fiscal_year_proj_ind,
      last_progress_update_timestamp,
      latitude,
      longitude,
      revision_count,
      create_user,
      create_date,
      update_user,
      update_date,
      total_estimated_cost_amount,
      total_forecast_amount 
    ) 
    VALUES ( 
      nextval('wfprev.prev_audit_table_seq'),
      v_audit_action, 
      NEW.project_guid,
      NEW.project_type_code,
      NEW.project_number,
      NEW.site_unit_name,
      NEW.primary_objective_type_code,
      NEW.primary_funding_source_guid,
      NEW.forest_area_code,
      NEW.general_scope_code,
      NEW.secondary_objective_type_code,
      NEW.tertiary_objective_type_code,
      NEW.project_status_code,
      NEW.program_area_guid,
      NEW.forest_region_org_unit_id,
      NEW.forest_district_org_unit_id,
      NEW.fire_centre_org_unit_id,
      NEW.bc_parks_region_org_unit_id,
      NEW.bc_parks_section_org_unit_id,
      NEW.project_name,
      NEW.project_lead,
      NEW.project_lead_email_address,
      NEW.project_description,
      NEW.closest_community_name,
      NEW.total_planned_project_size_ha,
      NEW.total_planned_cost_per_hectare,
      NEW.total_actual_amount,
      NEW.total_actual_project_size_ha,
      NEW.total_actual_cost_per_hectare_,
      NEW.secondary_objective_rationale,
      NEW.tertiary_objective_rationale,
      NEW.is_multi_fiscal_year_proj_ind,
      NEW.last_progress_update_timestamp,
      NEW.latitude,
      NEW.longitude,
      NEW.revision_count,
      NEW.create_user,
      NEW.create_date,
      NEW.update_user,
      NEW.update_date,
      NEW.total_estimated_cost_amount,
      NEW.total_forecast_amount 
    ); 
    RETURN NEW; 

  ELSIF (TG_OP = 'DELETE') THEN 
    INSERT INTO wfprev.project_audit( 
      audit_table_sequence, 
      audit_action_code, 
      project_guid,
      project_type_code,
      project_number,
      site_unit_name,
      primary_objective_type_code,
      primary_funding_source_guid,
      forest_area_code,
      general_scope_code,
      secondary_objective_type_code,
      tertiary_objective_type_code,
      project_status_code,
      program_area_guid,
      forest_region_org_unit_id,
      forest_district_org_unit_id,
      fire_centre_org_unit_id,
      bc_parks_region_org_unit_id,
      bc_parks_section_org_unit_id,
      project_name,
      project_lead,
      project_lead_email_address,
      project_description,
      closest_community_name,
      total_planned_project_size_ha,
      total_planned_cost_per_hectare,
      total_actual_amount,
      total_actual_project_size_ha,
      total_actual_cost_per_hectare_,
      secondary_objective_rationale,
      tertiary_objective_rationale,
      is_multi_fiscal_year_proj_ind,
      last_progress_update_timestamp,
      latitude,
      longitude,
      revision_count,
      create_user,
      create_date,
      update_user,
      update_date,
      total_estimated_cost_amount,
      total_forecast_amount 
    ) 
    VALUES ( 
      nextval('wfprev.prev_audit_table_seq'),
      v_audit_action, 
      OLD.project_guid,
      OLD.project_type_code,
      OLD.project_number,
      OLD.site_unit_name,
      OLD.primary_objective_type_code,
      OLD.primary_funding_source_guid,
      OLD.forest_area_code,
      OLD.general_scope_code,
      OLD.secondary_objective_type_code,
      OLD.tertiary_objective_type_code,
      OLD.project_status_code,
      OLD.program_area_guid,
      OLD.forest_region_org_unit_id,
      OLD.forest_district_org_unit_id,
      OLD.fire_centre_org_unit_id,
      OLD.bc_parks_region_org_unit_id,
      OLD.bc_parks_section_org_unit_id,
      OLD.project_name,
      OLD.project_lead,
      OLD.project_lead_email_address,
      OLD.project_description,
      OLD.closest_community_name,
      OLD.total_planned_project_size_ha,
      OLD.total_planned_cost_per_hectare,
      OLD.total_actual_amount,
      OLD.total_actual_project_size_ha,
      OLD.total_actual_cost_per_hectare_,
      OLD.secondary_objective_rationale,
      OLD.tertiary_objective_rationale,
      OLD.is_multi_fiscal_year_proj_ind,
      OLD.last_progress_update_timestamp,
      OLD.latitude,
      OLD.longitude,
      OLD.revision_count,
      OLD.create_user,
      OLD.create_date,
      OLD.update_user,
      OLD.update_date,
      OLD.total_estimated_cost_amount,
      OLD.total_forecast_amount 
    ); 
    RETURN OLD; 

  ELSE 
    RAISE WARNING '[wfprev.fnc_audit_project] - Other action occurred: %, at %',TG_OP,now(); 
    RETURN NULL; 
  END IF; 

EXCEPTION 
  WHEN data_exception THEN 
      RAISE WARNING '[wfprev.fnc_audit_project] - UDF ERROR [DATA EXCEPTION] - SQLSTATE: %, SQLERRM: %',SQLSTATE,SQLERRM; 
      RETURN NULL; 
  WHEN unique_violation THEN 
      RAISE WARNING '[wfprev.fnc_audit_project] - UDF ERROR [UNIQUE] - SQLSTATE: %, SQLERRM: %',SQLSTATE,SQLERRM; 
      RETURN NULL; 
  WHEN others THEN 
      RAISE WARNING '[wfprev.fnc_audit_project] - UDF ERROR [OTHER] - SQLSTATE: %, SQLERRM: %',SQLSTATE,SQLERRM; 
      RETURN NULL; 
END; 
$$ LANGUAGE plpgsql; 


CREATE OR REPLACE TRIGGER trigger_audit_project
AFTER INSERT OR UPDATE OR DELETE ON wfprev.project 
FOR EACH ROW EXECUTE PROCEDURE wfprev.fnc_audit_project();


CREATE OR REPLACE FUNCTION wfprev.fnc_audit_project_boundary() RETURNS TRIGGER AS $$
DECLARE 
  v_audit_action VARCHAR(10); 
BEGIN 
  v_audit_action := TG_OP; 
  IF ((TG_OP = 'INSERT') OR (TG_OP = 'UPDATE')) THEN 
    INSERT INTO wfprev.project_boundary_audit( 
      audit_table_sequence, 
      audit_action_code, 
      project_boundary_guid,
      project_guid,
      system_start_timestamp,
      system_end_timestamp,
      mapping_label,
      collection_date,
      collection_method,
      collector_name,
      boundary_size_ha,
      boundary_comment,
      revision_count,
      create_user,
      create_date,
      update_user,
      update_date 
    ) 
    VALUES ( 
      nextval('wfprev.prev_audit_table_seq'),
      v_audit_action, 
      NEW.project_boundary_guid,
      NEW.project_guid,
      NEW.system_start_timestamp,
      NEW.system_end_timestamp,
      NEW.mapping_label,
      NEW.collection_date,
      NEW.collection_method,
      NEW.collector_name,
      NEW.boundary_size_ha,
      NEW.boundary_comment,
      NEW.revision_count,
      NEW.create_user,
      NEW.create_date,
      NEW.update_user,
      NEW.update_date 
    ); 
    RETURN NEW; 

  ELSIF (TG_OP = 'DELETE') THEN 
    INSERT INTO wfprev.project_boundary_audit( 
      audit_table_sequence, 
      audit_action_code, 
      project_boundary_guid,
      project_guid,
      system_start_timestamp,
      system_end_timestamp,
      mapping_label,
      collection_date,
      collection_method,
      collector_name,
      boundary_size_ha,
      boundary_comment,
      revision_count,
      create_user,
      create_date,
      update_user,
      update_date 
    ) 
    VALUES ( 
      nextval('wfprev.prev_audit_table_seq'),
      v_audit_action, 
      OLD.project_boundary_guid,
      OLD.project_guid,
      OLD.system_start_timestamp,
      OLD.system_end_timestamp,
      OLD.mapping_label,
      OLD.collection_date,
      OLD.collection_method,
      OLD.collector_name,
      OLD.boundary_size_ha,
      OLD.boundary_comment,
      OLD.revision_count,
      OLD.create_user,
      OLD.create_date,
      OLD.update_user,
      OLD.update_date 
    ); 
    RETURN OLD; 

  ELSE 
    RAISE WARNING '[wfprev.fnc_audit_project_boundary] - Other action occurred: %, at %',TG_OP,now(); 
    RETURN NULL; 
  END IF; 

EXCEPTION 
  WHEN data_exception THEN 
      RAISE WARNING '[wfprev.fnc_audit_project_boundary] - UDF ERROR [DATA EXCEPTION] - SQLSTATE: %, SQLERRM: %',SQLSTATE,SQLERRM; 
      RETURN NULL; 
  WHEN unique_violation THEN 
      RAISE WARNING '[wfprev.fnc_audit_project_boundary] - UDF ERROR [UNIQUE] - SQLSTATE: %, SQLERRM: %',SQLSTATE,SQLERRM; 
      RETURN NULL; 
  WHEN others THEN 
      RAISE WARNING '[wfprev.fnc_audit_project_boundary] - UDF ERROR [OTHER] - SQLSTATE: %, SQLERRM: %',SQLSTATE,SQLERRM; 
      RETURN NULL; 
END; 
$$ LANGUAGE plpgsql; 


CREATE OR REPLACE TRIGGER trigger_audit_project_boundary
AFTER INSERT OR UPDATE OR DELETE ON wfprev.project_boundary 
FOR EACH ROW EXECUTE PROCEDURE wfprev.fnc_audit_project_boundary();


CREATE OR REPLACE FUNCTION wfprev.fnc_audit_project_plan_fiscal() RETURNS TRIGGER AS $$
DECLARE 
  v_audit_action VARCHAR(10); 
BEGIN 
  v_audit_action := TG_OP; 
  IF ((TG_OP = 'INSERT') OR (TG_OP = 'UPDATE')) THEN 
    INSERT INTO wfprev.project_plan_fiscal_audit( 
      audit_table_sequence, 
      audit_action_code, 
      project_plan_fiscal_guid,
      project_guid,
      activity_category_code,
      fiscal_year,
      ancillary_funding_source_guid,
      project_plan_status_code,
      plan_fiscal_status_code,
      endorsement_code,
      project_fiscal_name,
      project_fiscal_description,
      business_area_comment,
      estimated_clwrr_alloc_amount,
      total_cost_estimate_amount,
      cfs_project_code,
      fiscal_ancillary_fund_amount,
      fiscal_planned_project_size_ha,
      fiscal_planned_cost_per_ha_amt,
      fiscal_reported_spend_amount,
      fiscal_actual_amount,
      fiscal_completed_size_ha,
      fiscal_actual_cost_per_ha_amt,
      first_nations_deliv_part_ind,
      first_nations_engagement_ind,
      first_nations_partner,
      other_partner,
      results_number,
      results_opening_id,
      results_contact_email,
      submitted_by_name,
      submitted_by_user_guid,
      submitted_by_user_userid,
      submission_timestamp,
      endorsement_eval_timestamp,
      endorser_name,
      endorser_user_guid,
      endorser_user_userid,
      endorsement_timestamp,
      endorsement_comment,
      is_approved_ind,
      approver_name,
      approver_user_guid,
      approver_user_userid,
      approved_timestamp,
      accomplishments_comment,
      is_delayed_ind,
      delay_rationale,
      abandoned_rationale,
      last_progress_update_timestamp,
      revision_count,
      create_user,
      create_date,
      update_user,
      update_date,
      fiscal_forecast_amount 
    ) 
    VALUES ( 
      nextval('wfprev.prev_audit_table_seq'),
      v_audit_action, 
      NEW.project_plan_fiscal_guid,
      NEW.project_guid,
      NEW.activity_category_code,
      NEW.fiscal_year,
      NEW.ancillary_funding_source_guid,
      NEW.project_plan_status_code,
      NEW.plan_fiscal_status_code,
      NEW.endorsement_code,
      NEW.project_fiscal_name,
      NEW.project_fiscal_description,
      NEW.business_area_comment,
      NEW.estimated_clwrr_alloc_amount,
      NEW.total_cost_estimate_amount,
      NEW.cfs_project_code,
      NEW.fiscal_ancillary_fund_amount,
      NEW.fiscal_planned_project_size_ha,
      NEW.fiscal_planned_cost_per_ha_amt,
      NEW.fiscal_reported_spend_amount,
      NEW.fiscal_actual_amount,
      NEW.fiscal_completed_size_ha,
      NEW.fiscal_actual_cost_per_ha_amt,
      NEW.first_nations_deliv_part_ind,
      NEW.first_nations_engagement_ind,
      NEW.first_nations_partner,
      NEW.other_partner,
      NEW.results_number,
      NEW.results_opening_id,
      NEW.results_contact_email,
      NEW.submitted_by_name,
      NEW.submitted_by_user_guid,
      NEW.submitted_by_user_userid,
      NEW.submission_timestamp,
      NEW.endorsement_eval_timestamp,
      NEW.endorser_name,
      NEW.endorser_user_guid,
      NEW.endorser_user_userid,
      NEW.endorsement_timestamp,
      NEW.endorsement_comment,
      NEW.is_approved_ind,
      NEW.approver_name,
      NEW.approver_user_guid,
      NEW.approver_user_userid,
      NEW.approved_timestamp,
      NEW.accomplishments_comment,
      NEW.is_delayed_ind,
      NEW.delay_rationale,
      NEW.abandoned_rationale,
      NEW.last_progress_update_timestamp,
      NEW.revision_count,
      NEW.create_user,
      NEW.create_date,
      NEW.update_user,
      NEW.update_date,
      NEW.fiscal_forecast_amount 
    ); 
    RETURN NEW; 

  ELSIF (TG_OP = 'DELETE') THEN 
    INSERT INTO wfprev.project_plan_fiscal_audit( 
      audit_table_sequence, 
      audit_action_code, 
      project_plan_fiscal_guid,
      project_guid,
      activity_category_code,
      fiscal_year,
      ancillary_funding_source_guid,
      project_plan_status_code,
      plan_fiscal_status_code,
      endorsement_code,
      project_fiscal_name,
      project_fiscal_description,
      business_area_comment,
      estimated_clwrr_alloc_amount,
      total_cost_estimate_amount,
      cfs_project_code,
      fiscal_ancillary_fund_amount,
      fiscal_planned_project_size_ha,
      fiscal_planned_cost_per_ha_amt,
      fiscal_reported_spend_amount,
      fiscal_actual_amount,
      fiscal_completed_size_ha,
      fiscal_actual_cost_per_ha_amt,
      first_nations_deliv_part_ind,
      first_nations_engagement_ind,
      first_nations_partner,
      other_partner,
      results_number,
      results_opening_id,
      results_contact_email,
      submitted_by_name,
      submitted_by_user_guid,
      submitted_by_user_userid,
      submission_timestamp,
      endorsement_eval_timestamp,
      endorser_name,
      endorser_user_guid,
      endorser_user_userid,
      endorsement_timestamp,
      endorsement_comment,
      is_approved_ind,
      approver_name,
      approver_user_guid,
      approver_user_userid,
      approved_timestamp,
      accomplishments_comment,
      is_delayed_ind,
      delay_rationale,
      abandoned_rationale,
      last_progress_update_timestamp,
      revision_count,
      create_user,
      create_date,
      update_user,
      update_date,
      fiscal_forecast_amount 
    ) 
    VALUES ( 
      nextval('wfprev.prev_audit_table_seq'),
      v_audit_action, 
      OLD.project_plan_fiscal_guid,
      OLD.project_guid,
      OLD.activity_category_code,
      OLD.fiscal_year,
      OLD.ancillary_funding_source_guid,
      OLD.project_plan_status_code,
      OLD.plan_fiscal_status_code,
      OLD.endorsement_code,
      OLD.project_fiscal_name,
      OLD.project_fiscal_description,
      OLD.business_area_comment,
      OLD.estimated_clwrr_alloc_amount,
      OLD.total_cost_estimate_amount,
      OLD.cfs_project_code,
      OLD.fiscal_ancillary_fund_amount,
      OLD.fiscal_planned_project_size_ha,
      OLD.fiscal_planned_cost_per_ha_amt,
      OLD.fiscal_reported_spend_amount,
      OLD.fiscal_actual_amount,
      OLD.fiscal_completed_size_ha,
      OLD.fiscal_actual_cost_per_ha_amt,
      OLD.first_nations_deliv_part_ind,
      OLD.first_nations_engagement_ind,
      OLD.first_nations_partner,
      OLD.other_partner,
      OLD.results_number,
      OLD.results_opening_id,
      OLD.results_contact_email,
      OLD.submitted_by_name,
      OLD.submitted_by_user_guid,
      OLD.submitted_by_user_userid,
      OLD.submission_timestamp,
      OLD.endorsement_eval_timestamp,
      OLD.endorser_name,
      OLD.endorser_user_guid,
      OLD.endorser_user_userid,
      OLD.endorsement_timestamp,
      OLD.endorsement_comment,
      OLD.is_approved_ind,
      OLD.approver_name,
      OLD.approver_user_guid,
      OLD.approver_user_userid,
      OLD.approved_timestamp,
      OLD.accomplishments_comment,
      OLD.is_delayed_ind,
      OLD.delay_rationale,
      OLD.abandoned_rationale,
      OLD.last_progress_update_timestamp,
      OLD.revision_count,
      OLD.create_user,
      OLD.create_date,
      OLD.update_user,
      OLD.update_date,
      OLD.fiscal_forecast_amount 
    ); 
    RETURN OLD; 

  ELSE 
    RAISE WARNING '[wfprev.fnc_audit_project_plan_fiscal] - Other action occurred: %, at %',TG_OP,now(); 
    RETURN NULL; 
  END IF; 

EXCEPTION 
  WHEN data_exception THEN 
      RAISE WARNING '[wfprev.fnc_audit_project_plan_fiscal] - UDF ERROR [DATA EXCEPTION] - SQLSTATE: %, SQLERRM: %',SQLSTATE,SQLERRM; 
      RETURN NULL; 
  WHEN unique_violation THEN 
      RAISE WARNING '[wfprev.fnc_audit_project_plan_fiscal] - UDF ERROR [UNIQUE] - SQLSTATE: %, SQLERRM: %',SQLSTATE,SQLERRM; 
      RETURN NULL; 
  WHEN others THEN 
      RAISE WARNING '[wfprev.fnc_audit_project_plan_fiscal] - UDF ERROR [OTHER] - SQLSTATE: %, SQLERRM: %',SQLSTATE,SQLERRM; 
      RETURN NULL; 
END; 
$$ LANGUAGE plpgsql; 


CREATE OR REPLACE TRIGGER trigger_audit_project_plan_fiscal
AFTER INSERT OR UPDATE OR DELETE ON wfprev.project_plan_fiscal 
FOR EACH ROW EXECUTE PROCEDURE wfprev.fnc_audit_project_plan_fiscal();


CREATE OR REPLACE FUNCTION wfprev.fnc_audit_project_plan_fiscal_perf() RETURNS TRIGGER AS $$
DECLARE 
  v_audit_action VARCHAR(10); 
BEGIN 
  v_audit_action := TG_OP; 
  IF ((TG_OP = 'INSERT') OR (TG_OP = 'UPDATE')) THEN 
    INSERT INTO wfprev.project_plan_fiscal_perf_audit( 
      audit_table_sequence, 
      audit_action_code, 
      project_plan_fiscal_perf_guid,
      project_plan_fiscal_guid,
      submitted_timestamp,
      reporting_period_code,
      plan_fiscal_status_code,
      submitted_by_name,
      submitted_by_userid,
      submitted_by_guid,
      previous_forecast_amount,
      forecast_amount,
      forecast_adjustment_amount,
      forecast_adjustment_rationale,
      progress_comment,
      is_delayed_ind,
      delay_rationale,
      revision_count,
      create_user,
      create_date,
      update_user,
      update_date 
    ) 
    VALUES ( 
      nextval('wfprev.prev_audit_table_seq'),
      v_audit_action, 
      NEW.project_plan_fiscal_perf_guid,
      NEW.project_plan_fiscal_guid,
      NEW.submitted_timestamp,
      NEW.reporting_period_code,
      NEW.plan_fiscal_status_code,
      NEW.submitted_by_name,
      NEW.submitted_by_userid,
      NEW.submitted_by_guid,
      NEW.previous_forecast_amount,
      NEW.forecast_amount,
      NEW.forecast_adjustment_amount,
      NEW.forecast_adjustment_rationale,
      NEW.progress_comment,
      NEW.is_delayed_ind,
      NEW.delay_rationale,
      NEW.revision_count,
      NEW.create_user,
      NEW.create_date,
      NEW.update_user,
      NEW.update_date 
    ); 
    RETURN NEW; 

  ELSIF (TG_OP = 'DELETE') THEN 
    INSERT INTO wfprev.project_plan_fiscal_perf_audit( 
      audit_table_sequence, 
      audit_action_code, 
      project_plan_fiscal_perf_guid,
      project_plan_fiscal_guid,
      submitted_timestamp,
      reporting_period_code,
      plan_fiscal_status_code,
      submitted_by_name,
      submitted_by_userid,
      submitted_by_guid,
      previous_forecast_amount,
      forecast_amount,
      forecast_adjustment_amount,
      forecast_adjustment_rationale,
      progress_comment,
      is_delayed_ind,
      delay_rationale,
      revision_count,
      create_user,
      create_date,
      update_user,
      update_date 
    ) 
    VALUES ( 
      nextval('wfprev.prev_audit_table_seq'),
      v_audit_action, 
      OLD.project_plan_fiscal_perf_guid,
      OLD.project_plan_fiscal_guid,
      OLD.submitted_timestamp,
      OLD.reporting_period_code,
      OLD.plan_fiscal_status_code,
      OLD.submitted_by_name,
      OLD.submitted_by_userid,
      OLD.submitted_by_guid,
      OLD.previous_forecast_amount,
      OLD.forecast_amount,
      OLD.forecast_adjustment_amount,
      OLD.forecast_adjustment_rationale,
      OLD.progress_comment,
      OLD.is_delayed_ind,
      OLD.delay_rationale,
      OLD.revision_count,
      OLD.create_user,
      OLD.create_date,
      OLD.update_user,
      OLD.update_date 
    ); 
    RETURN OLD; 

  ELSE 
    RAISE WARNING '[wfprev.fnc_audit_project_plan_fiscal_perf] - Other action occurred: %, at %',TG_OP,now(); 
    RETURN NULL; 
  END IF; 

EXCEPTION 
  WHEN data_exception THEN 
      RAISE WARNING '[wfprev.fnc_audit_project_plan_fiscal_perf] - UDF ERROR [DATA EXCEPTION] - SQLSTATE: %, SQLERRM: %',SQLSTATE,SQLERRM; 
      RETURN NULL; 
  WHEN unique_violation THEN 
      RAISE WARNING '[wfprev.fnc_audit_project_plan_fiscal_perf] - UDF ERROR [UNIQUE] - SQLSTATE: %, SQLERRM: %',SQLSTATE,SQLERRM; 
      RETURN NULL; 
  WHEN others THEN 
      RAISE WARNING '[wfprev.fnc_audit_project_plan_fiscal_perf] - UDF ERROR [OTHER] - SQLSTATE: %, SQLERRM: %',SQLSTATE,SQLERRM; 
      RETURN NULL; 
END; 
$$ LANGUAGE plpgsql; 


CREATE OR REPLACE TRIGGER trigger_audit_project_plan_fiscal_perf
AFTER INSERT OR UPDATE OR DELETE ON wfprev.project_plan_fiscal_perf 
FOR EACH ROW EXECUTE PROCEDURE wfprev.fnc_audit_project_plan_fiscal_perf();