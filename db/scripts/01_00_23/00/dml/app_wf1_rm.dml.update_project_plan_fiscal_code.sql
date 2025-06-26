insert into wfprev.plan_fiscal_status_code (plan_fiscal_status_code, description, display_order, effective_date, expiry_date, revision_count, create_user, create_date, update_user, update_date)
values ('CANCELLED', 'Cancelled', 6, to_date('01-01-2000', 'dd-mm-yyyy'), to_date('31-12-9999', 'dd-mm-yyyy'), 0, 'DATA_LOAD', to_date('07-01-2025', 'dd-mm-yyyy'), 'DATA_LOAD', to_date('07-01-2025', 'dd-mm-yyyy'));

UPDATE wfprev.project_plan_fiscal 
  SET plan_fiscal_status_code = 'CANCELLED'
WHERE plan_fiscal_status_code = 'ABANDONED';

UPDATE wfprev.project_plan_fiscal_perf 
  SET plan_fiscal_status_code = 'CANCELLED'
WHERE plan_fiscal_status_code = 'ABANDONED';

DELETE FROM wfprev.plan_fiscal_status_code 
WHERE plan_fiscal_status_code = 'ABANDONED'; 