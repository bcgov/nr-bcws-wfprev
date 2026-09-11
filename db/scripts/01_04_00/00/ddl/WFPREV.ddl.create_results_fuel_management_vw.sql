DROP VIEW IF EXISTS wfprev.results_fuel_management_vw;

CREATE OR REPLACE VIEW wfprev.results_fuel_management_vw AS
SELECT p.project_name,
       ppf.project_fiscal_name,
       a.activity_name,
       CASE WHEN a.is_results_reportable_ind THEN 'Y' ELSE 'N' END AS is_results_reportable_ind
FROM wfprev.project p
  LEFT JOIN wfprev.project_plan_fiscal ppf ON ppf.project_guid = p.project_guid
  LEFT JOIN wfprev.activity a              ON a.project_plan_fiscal_guid = ppf.project_plan_fiscal_guid
WHERE p.project_type_code = 'FUEL_MGMT';