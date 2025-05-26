ALTER TABLE "wfprev"."project_plan_fiscal" ADD COLUMN "ancillary_funding_provider" varchar(100)  NULL;

COMMENT ON COLUMN "wfprev"."project_plan_fiscal"."ancillary_funding_provider"
  IS 'Ancillary Funding Provider allows for additional (ancillary) funding providers to be identified, who will provide funds to pay for the project for the fiscal year.'
;

ALTER TABLE "wfprev"."project_plan_fiscal" DROP COLUMN "ancillary_funding_source_guid";

ALTER TABLE "wfprev"."project_plan_fiscal_audit" ADD COLUMN "ancillary_funding_provider" varchar(100)  NULL;

COMMENT ON COLUMN "wfprev"."project_plan_fiscal_audit"."ancillary_funding_provider"
  IS 'Ancillary Funding Provider allows for additional (ancillary) funding providers to be identified, who will provide funds to pay for the project for the fiscal year.'
;

ALTER TABLE "wfprev"."project_plan_fiscal_audit" DROP COLUMN "ancillary_funding_source_guid";
