-- SCHEMA: wfprev

-- DROP SCHEMA "wfprev" ;

CREATE SCHEMA "wfprev";

GRANT ALL ON SCHEMA "wfprev" TO "app_wf1_prev";

GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO "app_wf1_prev";

GRANT USAGE ON SCHEMA "wfprev" TO "app_wf1_prev_rest_proxy";