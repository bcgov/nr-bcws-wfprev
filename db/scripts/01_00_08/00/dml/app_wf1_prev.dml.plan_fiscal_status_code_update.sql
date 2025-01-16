
INSERT INTO wfprev.plan_fiscal_status_code(plan_fiscal_status_code, description, display_order,  effective_date, expiry_date, revision_count,  create_user, create_date, update_user, update_date)
  VALUES ('PREPARED', 'Prepared', 2, to_date('01-01-2000', 'dd-mm-yyyy'), to_date('31-12-9999', 'dd-mm-yyyy'), 0, 'DATA_LOAD', to_date('01-12-2024', 'dd-mm-yyyy'), 'DATA_LOAD', to_date('01-12-2024', 'dd-mm-yyyy')); 


UPDATE wfprev.project_plan_fiscal
	SET plan_fiscal_status_code = 'PREPARED'
	WHERE plan_fiscal_status_code = 'PLANNED';

UPDATE wfprev.project_plan_fiscal_audit
	SET plan_fiscal_status_code = 'PREPARED'
	WHERE plan_fiscal_status_code = 'PLANNED';

DELETE FROM wfprev.plan_fiscal_status_code WHERE plan_fiscal_status_code = 'PLANNED';
