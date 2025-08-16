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
      total_forecast_amount,
      funding_stream_guid,
      results_project_code,
      last_updated_timestamp 
    ) 
    VALUES ( 
      nextval('wfprev.prev_audit_table_seq'),
      v_audit_action, 
      NEW.project_guid,
      NEW.project_type_code,
      NEW.project_number,
      NEW.site_unit_name,
      NEW.primary_objective_type_code,
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
      NEW.total_forecast_amount,
      NEW.funding_stream_guid,
      NEW.results_project_code,
      NEW.last_updated_timestamp 
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
      total_forecast_amount,
      funding_stream_guid,
      results_project_code,
      last_updated_timestamp 
    ) 
    VALUES ( 
      nextval('wfprev.prev_audit_table_seq'),
      v_audit_action, 
      OLD.project_guid,
      OLD.project_type_code,
      OLD.project_number,
      OLD.site_unit_name,
      OLD.primary_objective_type_code,
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
      OLD.total_forecast_amount,
      OLD.funding_stream_guid,
      OLD.results_project_code,
      OLD.last_updated_timestamp 
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