CREATE OR REPLACE FUNCTION wfprev.fnc_audit_project_plan_fiscal_closeout() RETURNS TRIGGER AS $$
DECLARE
  v_audit_action VARCHAR(10);
BEGIN
  v_audit_action := TG_OP;
  IF ((TG_OP = 'INSERT') OR (TG_OP = 'UPDATE')) THEN
    INSERT INTO wfprev.project_plan_fiscal_closeout_audit(
      audit_table_sequence,
      audit_action_code,
      project_plan_fiscal_closeout_guid,
      project_plan_fiscal_guid,
      outcome_comment,
      submitted_by_name,
      submitted_by_userid,
      submitted_by_guid,
      revision_count,
      create_user,
      create_date,
      update_user,
      update_date
    )
    VALUES (
      nextval('wfprev.prev_audit_table_seq'),
      v_audit_action,
      NEW.project_plan_fiscal_closeout_guid,
      NEW.project_plan_fiscal_guid,
      NEW.outcome_comment,
      NEW.submitted_by_name,
      NEW.submitted_by_userid,
      NEW.submitted_by_guid,
      NEW.revision_count,
      NEW.create_user,
      NEW.create_date,
      NEW.update_user,
      NEW.update_date
    );
    RETURN NEW;

  ELSIF (TG_OP = 'DELETE') THEN
    INSERT INTO wfprev.project_plan_fiscal_closeout_audit(
      audit_table_sequence,
      audit_action_code,
      project_plan_fiscal_closeout_guid,
      project_plan_fiscal_guid,
      outcome_comment,
      submitted_by_name,
      submitted_by_userid,
      submitted_by_guid,
      revision_count,
      create_user,
      create_date,
      update_user,
      update_date
    )
    VALUES (
      nextval('wfprev.prev_audit_table_seq'),
      v_audit_action,
      OLD.project_plan_fiscal_closeout_guid,
      OLD.project_plan_fiscal_guid,
      OLD.outcome_comment,
      OLD.submitted_by_name,
      OLD.submitted_by_userid,
      OLD.submitted_by_guid,
      OLD.revision_count,
      OLD.create_user,
      OLD.create_date,
      OLD.update_user,
      OLD.update_date
    );
    RETURN OLD;

  ELSE
    RAISE WARNING '[wfprev.fnc_audit_project_plan_fiscal_closeout] - Other action occurred: %, at %',TG_OP,now();
    RETURN NULL;
  END IF;

EXCEPTION
  WHEN data_exception THEN
      RAISE WARNING '[wfprev.fnc_audit_project_plan_fiscal_closeout] - UDF ERROR [DATA EXCEPTION] - SQLSTATE: %, SQLERRM: %',SQLSTATE,SQLERRM;
      RETURN NULL;
  WHEN unique_violation THEN
      RAISE WARNING '[wfprev.fnc_audit_project_plan_fiscal_closeout] - UDF ERROR [UNIQUE] - SQLSTATE: %, SQLERRM: %',SQLSTATE,SQLERRM;
      RETURN NULL;
  WHEN others THEN
      RAISE WARNING '[wfprev.fnc_audit_project_plan_fiscal_closeout] - UDF ERROR [OTHER] - SQLSTATE: %, SQLERRM: %',SQLSTATE,SQLERRM;
      RETURN NULL;
END;
$$ LANGUAGE plpgsql;


CREATE OR REPLACE TRIGGER trigger_audit_project_plan_fiscal_closeout
AFTER INSERT OR UPDATE OR DELETE ON wfprev.project_plan_fiscal_closeout
FOR EACH ROW EXECUTE PROCEDURE wfprev.fnc_audit_project_plan_fiscal_closeout();
