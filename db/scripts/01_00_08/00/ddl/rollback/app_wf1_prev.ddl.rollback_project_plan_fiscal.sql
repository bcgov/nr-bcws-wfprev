ALTER TABLE wfprev.project_plan_fiscal ADD COLUMN IF NOT EXISTS fiscal_funding_request_amount decimal(15,2) NULL;
COMMENT ON COLUMN "wfprev"."project_plan_fiscal"."fiscal_funding_request_amount"
	IS 'Fiscal Funding Request Amount is the dollar amount requested to fund the completion of the proposed project activities for the fiscal year.';

ALTER TABLE wfprev.project_plan_fiscal_audit ADD COLUMN IF NOT EXISTS fiscal_funding_request_amount decimal(15,2) NULL;
COMMENT ON COLUMN "wfprev"."project_plan_fiscal_audit"."fiscal_funding_request_amount"
	IS 'Fiscal Funding Request Amount is the dollar amount requested to fund the completion of the proposed project activities for the fiscal year.';


ALTER TABLE wfprev.project_plan_fiscal ADD COLUMN IF NOT EXISTS fiscal_funding_alloc_rationale varchar(4000)	 NULL;
COMMENT ON COLUMN "wfprev"."project_plan_fiscal"."fiscal_funding_alloc_rationale"
	IS 'Fiscal Funding Allocation Rationale allows for a rationale to be provided for a funding allocation request.';

ALTER TABLE wfprev.project_plan_fiscal_audit ADD COLUMN IF NOT EXISTS fiscal_funding_alloc_rationale varchar(4000)	 NULL;
COMMENT ON COLUMN "wfprev"."project_plan_fiscal_audit"."fiscal_funding_alloc_rationale"
	IS 'Fiscal Funding Allocation Rationale allows for a rationale to be provided for a funding allocation request.';


ALTER TABLE wfprev.project_plan_fiscal ADD COLUMN IF NOT EXISTS fiscal_allocated_amount decimal(15,2) NULL;
COMMENT ON COLUMN "wfprev"."project_plan_fiscal"."fiscal_allocated_amount"
	IS 'Fiscal Allocated Amount is the total budget that has been allocated to the project for the specified fiscal year.';

ALTER TABLE wfprev.project_plan_fiscal_audit ADD COLUMN IF NOT EXISTS fiscal_allocated_amount decimal(15,2) NULL;
COMMENT ON COLUMN "wfprev"."project_plan_fiscal_audit"."fiscal_allocated_amount"
	IS 'Fiscal Allocated Amount is the total budget that has been allocated to the project for the specified fiscal year.';


ALTER TABLE "wfprev"."project_plan_fiscal" DROP COLUMN IF EXISTS "fiscal_forecast_amount";
ALTER TABLE "wfprev"."project_plan_fiscal_audit" DROP COLUMN IF EXISTS "fiscal_forecast_amount";
