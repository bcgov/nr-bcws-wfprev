
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
      progress_status_code,
      plan_fiscal_status_code,
      reporting_period_code,
      submitted_by_name,
      submitted_by_userid,
      submitted_by_guid,
      previous_forecast_amount,
      forecast_amount,
      forecast_adjustment_amount,
      forecast_adjustment_rationale,
      general_update_comment,
      budget_high_risk_amount,
      budget_high_risk_rationale,
      budget_medium_risk_amount,
      budget_medium_risk_rationale,
      budget_low_risk_amount,
      budget_low_risk_rationale,
      budget_completed_amount,
      budget_completed_description,
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
      NEW.progress_status_code,
      NEW.plan_fiscal_status_code,
      NEW.reporting_period_code,
      NEW.submitted_by_name,
      NEW.submitted_by_userid,
      NEW.submitted_by_guid,
      NEW.previous_forecast_amount,
      NEW.forecast_amount,
      NEW.forecast_adjustment_amount,
      NEW.forecast_adjustment_rationale,
      NEW.general_update_comment,
      NEW.budget_high_risk_amount,
      NEW.budget_high_risk_rationale,
      NEW.budget_medium_risk_amount,
      NEW.budget_medium_risk_rationale,
      NEW.budget_low_risk_amount,
      NEW.budget_low_risk_rationale,
      NEW.budget_completed_amount,
      NEW.budget_completed_description,
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
      progress_status_code,
      plan_fiscal_status_code,
      reporting_period_code,
      submitted_by_name,
      submitted_by_userid,
      submitted_by_guid,
      previous_forecast_amount,
      forecast_amount,
      forecast_adjustment_amount,
      forecast_adjustment_rationale,
      general_update_comment,
      budget_high_risk_amount,
      budget_high_risk_rationale,
      budget_medium_risk_amount,
      budget_medium_risk_rationale,
      budget_low_risk_amount,
      budget_low_risk_rationale,
      budget_completed_amount,
      budget_completed_description,
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
      OLD.progress_status_code,
      OLD.plan_fiscal_status_code,
      OLD.reporting_period_code,
      OLD.submitted_by_name,
      OLD.submitted_by_userid,
      OLD.submitted_by_guid,
      OLD.previous_forecast_amount,
      OLD.forecast_amount,
      OLD.forecast_adjustment_amount,
      OLD.forecast_adjustment_rationale,
      OLD.general_update_comment,
      OLD.budget_high_risk_amount,
      OLD.budget_high_risk_rationale,
      OLD.budget_medium_risk_amount,
      OLD.budget_medium_risk_rationale,
      OLD.budget_low_risk_amount,
      OLD.budget_low_risk_rationale,
      OLD.budget_completed_amount,
      OLD.budget_completed_description,
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

