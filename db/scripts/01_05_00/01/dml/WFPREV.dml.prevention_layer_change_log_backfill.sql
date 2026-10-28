-- WFPREV-1307
-- Backfill existing projects into prevention_layer_change_log so initial sync picks up all projects.

INSERT INTO wfprev.prevention_layer_change_log (project_guid, last_updated)
SELECT p.project_guid, CURRENT_TIMESTAMP
FROM wfprev.project p
ON CONFLICT (project_guid) DO NOTHING;

