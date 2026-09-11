drop view if exists wfprev.results_fuel_management_vw;

CREATE or REPLACE VIEW wfprev.results_fuel_management_vw AS
select ppf.project_fiscal_name,
       a.activity_name
from wfprev.project_plan_fiscal ppf
left join wfprev.activity a on a.project_plan_fiscal_guid = ppf.project_plan_fiscal_guid;
