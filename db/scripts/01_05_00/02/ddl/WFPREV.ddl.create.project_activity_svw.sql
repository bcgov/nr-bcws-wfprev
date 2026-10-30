DROP VIEW IF EXISTS wfprev.project_activity_svw;

CREATE OR REPLACE VIEW wfprev.project_activity_svw AS
SELECT COALESCE(a.activity_guid, p.project_guid) AS row_id,
       p.project_guid,
       p.project_number,
       p.project_type_code,
       ptc.description AS project_type_desc,
       p.primary_objective_type_code,
       potc.description AS primary_objective_type_desc,
       p.secondary_objective_type_code,
       sotc.description AS secondary_objective_type_desc,
       p.results_project_code,
       ppf.project_plan_fiscal_guid,
       ppf.fiscal_year,
       ppf.project_fiscal_name,
       a.activity_guid,
       a.activity_name,
       a.activity_status_code,
       ascd.description AS activity_status_desc,
       a.activity_start_date,
       a.activity_end_date,
       a.completed_area_ha,
       a.outstanding_obligations_ind,
       ab.geometry,
       plcl.last_updated
FROM wfprev.project p
  JOIN wfprev.project_type_code ptc          ON ptc.project_type_code = p.project_type_code
  LEFT JOIN wfprev.objective_type_code potc  ON potc.objective_type_code = p.primary_objective_type_code
  LEFT JOIN wfprev.objective_type_code sotc  ON sotc.objective_type_code = p.secondary_objective_type_code
  JOIN wfprev.project_plan_fiscal ppf        ON ppf.project_guid = p.project_guid
  JOIN wfprev.activity a                     ON a.project_plan_fiscal_guid = ppf.project_plan_fiscal_guid
  JOIN wfprev.activity_status_code ascd      ON ascd.activity_status_code = a.activity_status_code
  LEFT JOIN wfprev.prevention_layer_change_log plcl ON plcl.project_guid = p.project_guid
  JOIN LATERAL (
    SELECT lab.geometry
    FROM wfprev.activity_boundary lab
    WHERE lab.activity_guid = a.activity_guid
    ORDER BY lab.system_start_timestamp DESC, lab.activity_boundary_guid DESC
    LIMIT 1
  ) ab ON true
WHERE (a.activity_status_code = 'ACTIVE'
       AND ppf.fiscal_year >= EXTRACT(YEAR FROM CURRENT_DATE - INTERVAL '3 months'))
   OR a.activity_status_code = 'COMPLETED'

UNION ALL

SELECT plcl.project_guid AS row_id,
       plcl.project_guid,
       NULL::integer AS project_number,
       NULL::varchar AS project_type_code,
       NULL::varchar AS project_type_desc,
       NULL::varchar AS primary_objective_type_code,
       NULL::varchar AS primary_objective_type_desc,
       NULL::varchar AS secondary_objective_type_code,
       NULL::varchar AS secondary_objective_type_desc,
       NULL::varchar AS results_project_code,
       NULL::uuid AS project_plan_fiscal_guid,
       NULL::numeric AS fiscal_year,
       NULL::varchar AS project_fiscal_name,
       NULL::uuid AS activity_guid,
       NULL::varchar AS activity_name,
       NULL::varchar AS activity_status_code,
       NULL::varchar AS activity_status_desc,
       NULL::timestamp AS activity_start_date,
       NULL::timestamp AS activity_end_date,
       NULL::numeric AS completed_area_ha,
       NULL::boolean AS outstanding_obligations_ind,
       NULL::geometry(MultiPolygon, 4326) AS geometry,
       plcl.last_updated
FROM wfprev.prevention_layer_change_log plcl
WHERE NOT EXISTS (
  SELECT 1 FROM wfprev.project p WHERE p.project_guid = plcl.project_guid
);
