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
      update_date,
      last_updated_timestamp 
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
      NEW.update_date,
      NEW.last_updated_timestamp 
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
      update_date,
      last_updated_timestamp 
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
      OLD.update_date,
      OLD.last_updated_timestamp 
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


