GRANT SELECT, INSERT, UPDATE, DELETE ON "wfprev"."example_code" TO "app_wf1_prev_rest_proxy";

GRANT SELECT, INSERT, UPDATE, DELETE ON "wfprev"."example_table" TO "app_wf1_prev_rest_proxy";

GRANT SELECT, INSERT, UPDATE, DELETE ON "wfprev"."example_code" TO "app_wf1_prev_custodian";

GRANT SELECT, INSERT, UPDATE, DELETE ON "wfprev"."example_table" TO "app_wf1_prev_custodian";

GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA "wfprev" TO "app_wf1_prev_rest_proxy";