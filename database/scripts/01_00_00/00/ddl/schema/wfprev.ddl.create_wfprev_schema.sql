-- SCHEMA: wfnews

-- DROP SCHEMA "wfnews" ;

CREATE SCHEMA "wfprev";

GRANT ALL ON SCHEMA "wfprev" TO "app_wf1_prev";

GRANT USAGE ON SCHEMA "wfprev" TO "app_wf1_prev_rest_proxy";