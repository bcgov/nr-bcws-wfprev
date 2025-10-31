CREATE EXTENSION IF NOT EXISTS postgis WITH SCHEMA wfprev;
GRANT USAGE ON SCHEMA wfprev TO app_wf1_prev, proxy_wf1_prev_rest;