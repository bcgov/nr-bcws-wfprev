drop view if exists wfprev.results_fuel_management_vw;

CREATE or REPLACE VIEW wfprev.results_fuel_management_vw AS
select ppf.project_fiscal_name
from wfprev.project_plan_fiscal ppf;
