-- Updates password for environments where the role already exists: in INT and TEST, the password was hardcoded
ALTER ROLE "app_wf1_prev" WITH PASSWORD '${APP_WF1_PREV_PASSWORD}';
ALTER ROLE "proxy_wf1_prev_rest" WITH PASSWORD '${PROXY_WF1_PREV_REST_PASSWORD}'