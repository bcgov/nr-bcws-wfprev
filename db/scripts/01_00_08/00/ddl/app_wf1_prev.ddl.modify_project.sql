ALTER TABLE wfprev.project DROP COLUMN total_funding_request_amount;
ALTER TABLE wfprev.project DROP COLUMN total_allocated_amount;

ALTER TABLE wfprev.project_audit DROP COLUMN total_funding_request_amount;
ALTER TABLE wfprev.project_audit DROP COLUMN total_allocated_amount;

ALTER TABLE wfprev.project ADD COLUMN "total_estimated_cost_amount" decimal(15,2) NULL DEFAULT 0;
ALTER TABLE wfprev.project ADD COLUMN "total_forecast_amount" decimal(15,2) NULL DEFAULT 0;

COMMENT ON COLUMN "wfprev"."project"."total_forecast_amount"
  IS 'Total Forecast Amount is the total amount that is forecast to be spent on the project, and is the sum of the forecast amounts for each fiscal year.'
;

COMMENT ON COLUMN "wfprev"."project"."total_planned_project_size_ha"
  IS 'Total Planned Project Size Ha is the total land size in hectares that are planned to be treated by the project. This will include land that is planned to be treated in the future.'
;

ALTER TABLE wfprev.project_audit ADD COLUMN "total_estimated_cost_amount" decimal(15,2) NULL DEFAULT 0;
ALTER TABLE wfprev.project_audit ADD COLUMN "total_forecast_amount" decimal(15,2) NULL DEFAULT 0;

COMMENT ON COLUMN "wfprev"."project_audit"."total_forecast_amount"
  IS 'Total Forecast Amount is the total amount that is forecast to be spent on the project, and is the sum of the forecast amounts for each fiscal year.'
;

COMMENT ON COLUMN "wfprev"."project_audit"."total_planned_project_size_ha"
  IS 'Total Planned Project Size Ha is the total land size in hectares that are planned to be treated by the project. This will include land that is planned to be treated in the future.'
;