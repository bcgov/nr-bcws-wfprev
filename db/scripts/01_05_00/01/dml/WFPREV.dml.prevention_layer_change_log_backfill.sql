-- WFPREV-1307
-- Backfill existing projects into prevention_layer_change_log using the latest update_date
-- across the project and all its child records (boundaries, fiscals, activities).

INSERT INTO wfprev.prevention_layer_change_log (project_guid, last_updated)
SELECT project_guid, MAX(update_date)::timestamp
FROM (
  SELECT p.project_guid, COALESCE(p.update_date, p.create_date) AS update_date
  FROM wfprev.project p

  UNION ALL

  SELECT pb.project_guid, COALESCE(pb.update_date, pb.create_date) AS update_date
  FROM wfprev.project_boundary pb

  UNION ALL

  SELECT ppf.project_guid, COALESCE(ppf.update_date, ppf.create_date) AS update_date
  FROM wfprev.project_plan_fiscal ppf

  UNION ALL

  SELECT ppf.project_guid, COALESCE(a.update_date, a.create_date) AS update_date
  FROM wfprev.activity a
  JOIN wfprev.project_plan_fiscal ppf ON ppf.project_plan_fiscal_guid = a.project_plan_fiscal_guid

  UNION ALL

  SELECT ppf.project_guid, COALESCE(ab.update_date, ab.create_date) AS update_date
  FROM wfprev.activity_boundary ab
  JOIN wfprev.activity a ON a.activity_guid = ab.activity_guid
  JOIN wfprev.project_plan_fiscal ppf ON ppf.project_plan_fiscal_guid = a.project_plan_fiscal_guid
) all_updates
GROUP BY project_guid
ON CONFLICT (project_guid) DO NOTHING;
