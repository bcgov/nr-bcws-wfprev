GRANT SELECT, INSERT, UPDATE, DELETE ON wfprev.prevention_layer_change_log TO app_wf1_prev_rest_proxy;
GRANT SELECT, INSERT, UPDATE, DELETE ON wfprev.prevention_layer_change_log TO proxy_wf1_prev_rest;
GRANT SELECT, INSERT, UPDATE, DELETE ON wfprev.prevention_layer_change_log TO app_wf1_prev_custodian;

GRANT EXECUTE ON FUNCTION wfprev.fnc_prevention_layer_change_log() TO app_wf1_prev_rest_proxy;
GRANT EXECUTE ON FUNCTION wfprev.fnc_prevention_layer_change_log() TO proxy_wf1_prev_rest;

