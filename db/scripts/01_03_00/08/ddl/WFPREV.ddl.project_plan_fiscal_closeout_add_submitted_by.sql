ALTER TABLE wfprev.project_plan_fiscal_closeout ADD COLUMN IF NOT EXISTS submitted_by_name VARCHAR(100) NOT NULL;
ALTER TABLE wfprev.project_plan_fiscal_closeout ADD COLUMN IF NOT EXISTS submitted_by_userid VARCHAR(64) NULL;
ALTER TABLE wfprev.project_plan_fiscal_closeout ADD COLUMN IF NOT EXISTS submitted_by_guid VARCHAR(36) NULL;
ALTER TABLE wfprev.project_plan_fiscal_closeout ADD COLUMN IF NOT EXISTS submitted_timestamp TIMESTAMP NOT NULL;

COMMENT ON COLUMN wfprev.project_plan_fiscal_closeout.submitted_by_name IS 'Submitted By Name is the display name of the person who entered the fiscal closeout.';
COMMENT ON COLUMN wfprev.project_plan_fiscal_closeout.submitted_by_userid IS 'Submitted By Userid is the IDIR or BCEID userid of the user that entered the fiscal closeout.';
COMMENT ON COLUMN wfprev.project_plan_fiscal_closeout.submitted_by_guid IS 'Submitted By Guid is the user GUID identifying the person who created the fiscal closeout. The user guid stored in this field corresponds to a user authorized in the WebADE platform.';
COMMENT ON COLUMN wfprev.project_plan_fiscal_closeout.submitted_timestamp IS 'Submitted Timestamp is the date and time the fiscal closeout was entered.'