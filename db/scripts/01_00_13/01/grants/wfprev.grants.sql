GRANT SELECT  ON ALL SEQUENCES IN SCHEMA wfprev TO app_wf1_prev_custodian;
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA wfprev TO app_wf1_prev_custodian;
ALTER DEFAULT PRIVILEGES IN SCHEMA wfprev GRANT SELECT, INSERT, UPDATE, DELETE ON tables TO app_wf1_prev_custodian;
ALTER DEFAULT PRIVILEGES IN SCHEMA wfprev GRANT SELECT  ON SEQUENCES TO app_wf1_prev_custodian;