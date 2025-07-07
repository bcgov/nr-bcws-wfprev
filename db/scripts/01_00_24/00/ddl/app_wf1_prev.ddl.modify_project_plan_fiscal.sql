ALTER TABLE wfprev.project_plan_fiscal ADD COLUMN "approved_comment" varchar(4000)   NULL;

COMMENT ON COLUMN "wfprev"."project_plan_fiscal"."approved_comment"
  IS 'Approved Comment are comments a staff person may record regarding the approval of the project plan for the fiscal year.'
;

ALTER TABLE wfprev.project_plan_fiscal_audit ADD COLUMN "approved_comment" varchar(4000)   NULL;

COMMENT ON COLUMN "wfprev"."project_plan_fiscal_audit"."approved_comment"
  IS 'Approved Comment are comments a staff person may record regarding the approval of the project plan for the fiscal year.'
;