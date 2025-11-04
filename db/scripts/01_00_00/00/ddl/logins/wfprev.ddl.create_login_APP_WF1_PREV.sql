-- Role: "app_wf1_prev"
-- DROP ROLE "app_wf1_prev";

CREATE ROLE "app_wf1_prev" WITH
  LOGIN
  NOSUPERUSER
  INHERIT
  NOCREATEDB
  NOCREATEROLE
  NOREPLICATION
  PASSWORD "${APP_WF1_PREV_PASSWORD}"

COMMENT ON ROLE "app_wf1_prev" IS 'Wildfire Prevention System.';