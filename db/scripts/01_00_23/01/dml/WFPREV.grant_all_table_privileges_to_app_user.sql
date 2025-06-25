-- Ensure the role can use the schema
GRANT USAGE ON SCHEMA wfprev TO app_wf1_prev_rest_proxy;

-- Grant all necessary privileges on existing sequences
GRANT SELECT ON ALL SEQUENCES IN SCHEMA wfprev TO app_wf1_prev_rest_proxy;

-- Grant all necessary privileges on existing tables
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA wfprev TO app_wf1_prev_rest_proxy;

-- Grant all necessary privileges on future tables in this schema
ALTER DEFAULT PRIVILEGES IN SCHEMA wfprev
GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO app_wf1_prev_rest_proxy;

-- Grant select on future sequences in this schema
ALTER DEFAULT PRIVILEGES IN SCHEMA wfprev
GRANT SELECT ON SEQUENCES TO app_wf1_prev_rest_proxy;

-- (Optional) Grant execute on functions in case any stored procs or functions are added
GRANT EXECUTE ON ALL FUNCTIONS IN SCHEMA wfprev TO app_wf1_prev_rest_proxy;

-- (Optional) For future functions
ALTER DEFAULT PRIVILEGES IN SCHEMA wfprev
GRANT EXECUTE ON FUNCTIONS TO app_wf1_prev_rest_proxy;
