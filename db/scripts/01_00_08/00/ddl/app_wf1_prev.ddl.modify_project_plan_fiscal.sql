ALTER TABLE wfprev.project_plan_fiscal DROP COLUMN fiscal_funding_request_amount;
ALTER TABLE wfprev.project_plan_fiscal_audit DROP COLUMN fiscal_funding_request_amount;

ALTER TABLE wfprev.project_plan_fiscal DROP COLUMN fiscal_funding_alloc_rationale;
ALTER TABLE wfprev.project_plan_fiscal_audit DROP COLUMN fiscal_funding_alloc_rationale;

ALTER TABLE wfprev.project_plan_fiscal DROP COLUMN fiscal_allocated_amount;
ALTER TABLE wfprev.project_plan_fiscal_audit DROP COLUMN fiscal_allocated_amount;

ALTER TABLE "wfprev"."project_plan_fiscal" ADD COLUMN "fiscal_forecast_amount" decimal(15,2) NULL DEFAULT 0;

COMMENT ON COLUMN "wfprev"."project_plan_fiscal"."fiscal_forecast_amount"
  IS 'Fiscal Forecast Amount is the total budget that has been allocated to the project for the specified fiscal year.'
;

ALTER TABLE "wfprev"."project_plan_fiscal_audit" ADD COLUMN "fiscal_forecast_amount" decimal(15,2) NULL DEFAULT 0;

COMMENT ON COLUMN "wfprev"."project_plan_fiscal_audit"."fiscal_forecast_amount"
  IS 'Fiscal Forecast Amount is the total budget that has been allocated to the project for the specified fiscal year.'
;