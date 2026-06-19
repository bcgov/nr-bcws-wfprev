ALTER TABLE wfprev.project_plan_fiscal_audit
    ADD COLUMN is_bcws_hq_approved_ind boolean NOT NULL DEFAULT false,
    ADD COLUMN bcws_hq_approver_name varchar(100) NULL,
    ADD COLUMN bcws_hq_approver_user_guid varchar(100) NULL,
    ADD COLUMN bcws_hq_approver_user_userid varchar(100) NULL,
    ADD COLUMN bcws_hq_approved_timestamp timestamp without time zone NULL,
    ADD COLUMN bcws_hq_approved_comment varchar(4000) NULL;

COMMENT ON COLUMN wfprev.project_plan_fiscal_audit.is_bcws_hq_approved_ind IS 'Is BCWS HQ Approved Ind indicates whether the project plan for the fiscal year is approved by the BC Wildfire Service Headquarters (Y) or not (N). An approved plan has an approved budget and work can begin on the project.';
COMMENT ON COLUMN wfprev.project_plan_fiscal_audit.bcws_hq_approver_name IS 'BCWS HQ Approver Name is the name of the person from BC Wildfire Service HQ that approved the project plan for the fiscal year.';
COMMENT ON COLUMN wfprev.project_plan_fiscal_audit.bcws_hq_approver_user_guid IS 'BCWS HQ Approver User Guid is the user GUID identifying the person from BCWS Headquarters who approves the project plan  The user guid is stored in this field corresponds to a user authorized in the WebADE platform.';
COMMENT ON COLUMN wfprev.project_plan_fiscal_audit.bcws_hq_approver_user_userid IS 'BCWS HQ Approver User Userid is the IDIR or BCEID userid of the person from BCWS Headquarters that approves the project plan.';
COMMENT ON COLUMN wfprev.project_plan_fiscal_audit.bcws_hq_approved_timestamp IS 'Approved Timestamp records the date and time the plan was approved by an BCWS HQ employee.';
COMMENT ON COLUMN wfprev.project_plan_fiscal_audit.bcws_hq_approved_comment IS 'BCWS HQ Approved Comment are comments a BCWS HQ staff person may record regarding the approval of the project plan for the fiscal year.';