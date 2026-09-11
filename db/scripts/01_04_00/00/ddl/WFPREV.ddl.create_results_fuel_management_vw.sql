DROP VIEW IF EXISTS wfprev.results_fuel_management_vw;

CREATE OR REPLACE VIEW wfprev.results_fuel_management_vw AS
SELECT p.project_name,
       ppf.project_fiscal_name,
       a.activity_name,
       CASE WHEN a.is_results_reportable_ind THEN 'Y' ELSE 'N' END AS is_results_reportable_ind,
       a.activity_description,
       a.activity_status_code,
       CASE
         WHEN ppf.fiscal_year IS NOT NULL
         THEN ppf.fiscal_year::int::text || '/' || right((ppf.fiscal_year::int + 1)::text, 2)
       END AS fiscal_year,
       fdou.org_unit_name AS forest_district_name,
       p.project_lead_email_address,
       p.results_project_code
FROM wfprev.project p
  LEFT JOIN wfprev.forest_org_unit fdou    ON fdou.org_unit_identifier = p.forest_district_org_unit_id
  LEFT JOIN wfprev.project_plan_fiscal ppf ON ppf.project_guid = p.project_guid
  LEFT JOIN wfprev.activity a              ON a.project_plan_fiscal_guid = ppf.project_plan_fiscal_guid
WHERE p.project_type_code = 'FUEL_MGMT';