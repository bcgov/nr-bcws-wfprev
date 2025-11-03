-- Add public to proxy_wf1_prev_rest's search_path so PostGIS functions in public schema resolve correctly
ALTER ROLE proxy_wf1_prev_rest IN DATABASE '${WFPREV_DATABASE_NAME}'
SET search_path = wfprev, public;