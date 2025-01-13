ALTER TABLE wfprev.project ADD  COLUMN total_funding_request_amount decimal(15,2) NULL;
COMMENT ON COLUMN "wfprev"."project"."total_funding_request_amount"
	IS 'Total Funding Request Amount is the dollar amount requested to fund the completion of the proposed project activities for all fiscal years.';

ALTER TABLE wfprev.project_audit ADD COLUMN total_funding_request_amount decimal(15,2) NULL;
COMMENT ON COLUMN "wfprev"."project_audit"."total_funding_request_amount"
	IS 'Total Funding Request Amount is the dollar amount requested to fund the completion of the proposed project activities for all fiscal years.';


ALTER TABLE wfprev.project ADD COLUMN total_allocated_amount decimal(15,2) NULL;
COMMENT ON COLUMN "wfprev"."project"."total_allocated_amount"
	IS 'Total Allocated Amount is the total budget that has been allocated to the project across all fiscal years.';

ALTER TABLE wfprev.project_audit ADD COLUMN total_allocated_amount decimal(15,2) NULL;
COMMENT ON COLUMN "wfprev"."project_audit"."total_allocated_amount"
	IS 'Total Allocated Amount is the total budget that has been allocated to the project across all fiscal years.';


ALTER TABLE wfprev.project DROP COLUMN "total_estimated_cost_amount";
ALTER TABLE wfprev.project DROP COLUMN "total_forecast_amount";

ALTER TABLE wfprev.project_audit DROP COLUMN "total_estimated_cost_amount";
ALTER TABLE wfprev.project_audit DROP COLUMN "total_forecast_amount";