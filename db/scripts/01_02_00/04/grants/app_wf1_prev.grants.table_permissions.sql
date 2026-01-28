GRANT SELECT, INSERT, UPDATE, DELETE ON wfprev.project_plan_fiscal_perf TO app_wf1_prev_rest_proxy;
GRANT SELECT, INSERT, UPDATE, DELETE ON wfprev.project_plan_fiscal_perf_audit TO app_wf1_prev_rest_proxy;
GRANT SELECT, INSERT, UPDATE, DELETE ON wfprev.progress_status_code TO app_wf1_prev_rest_proxy;

GRANT SELECT, INSERT, UPDATE, DELETE ON wfprev.project_plan_fiscal_perf TO proxy_wf1_prev_rest;
GRANT SELECT, INSERT, UPDATE, DELETE ON wfprev.project_plan_fiscal_perf_audit TO proxy_wf1_prev_rest;
GRANT SELECT, INSERT, UPDATE, DELETE ON wfprev.progress_status_code TO proxy_wf1_prev_rest;
