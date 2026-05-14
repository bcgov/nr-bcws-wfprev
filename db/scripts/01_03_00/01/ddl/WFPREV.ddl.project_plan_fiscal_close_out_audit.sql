CREATE TABLE "wfprev"."project_plan_fiscal_close_out_audit"
(
	"audit_table_sequence" decimal(15) NOT NULL,    -- audit_table_sequence is a sequence used to maintain the order of audit records.
	"audit_action_code" varchar(10)	 NOT NULL,    -- audit_action_code contains the reason the audit record is written (Insert/Update/Delete).
	"project_plan_fiscal_close_out_guid" UUID NULL,    -- project_plan_fiscal_close_out_guid is a unique identifier for the record.
	"project_plan_fiscal_guid" UUID NOT NULL,    -- project_plan_fiscal_guid: Foreign key to project_plan_fiscal.
	"outcome_comment" varchar(4000)	 NULL,    -- Outcome Comment is used to document the final outcomes and comments for the fiscal year close-out.
	"revision_count" decimal(10) NOT NULL DEFAULT 0,    -- REVISION_COUNT is the number of times that the row of data has been changed.
	"create_user" varchar(64)	 NOT NULL,    -- CREATE_USER is an audit column that indicates the user that created the record.
	"create_date" DATE NOT NULL DEFAULT CURRENT_TIMESTAMP,    -- CREATE_DATE is the date and time the row of data was created.
	"update_user" varchar(64)	 NOT NULL,    -- UPDATE_USER is an audit column that indicates the user that updated the record.
	"update_date" DATE NOT NULL DEFAULT CURRENT_TIMESTAMP    -- UPDATE_DATE is the date and time the row of data was updated.
)
TABLESPACE	PG_DEFAULT
;

/* Create Table Comments */

COMMENT ON TABLE "wfprev"."project_plan_fiscal_close_out_audit"
	IS 'Project Plan Fiscal Close Out Audit tracks changes to the close-out records for a project''s fiscal year plan.'
;

COMMENT ON COLUMN "wfprev"."project_plan_fiscal_close_out_audit"."audit_table_sequence"
	IS 'audit_table_sequence is a sequence used to maintain the order of audit records.'
;

COMMENT ON COLUMN "wfprev"."project_plan_fiscal_close_out_audit"."audit_action_code"
	IS 'audit_action_code contains the reason the audit record is written (Insert/Update/Delete).'
;

COMMENT ON COLUMN "wfprev"."project_plan_fiscal_close_out_audit"."project_plan_fiscal_close_out_guid"
	IS 'project_plan_fiscal_close_out_guid is a unique identifier for the record.'
;

COMMENT ON COLUMN "wfprev"."project_plan_fiscal_close_out_audit"."project_plan_fiscal_guid"
	IS 'project_plan_fiscal_guid: Is a foreign key to project_plan_fiscal.'
;

COMMENT ON COLUMN "wfprev"."project_plan_fiscal_close_out_audit"."outcome_comment"
	IS 'Outcome Comment is used to document the final outcomes and comments for the fiscal year close-out.'
;

COMMENT ON COLUMN "wfprev"."project_plan_fiscal_close_out_audit"."revision_count"
	IS 'REVISION_COUNT is the number of times that the row of data has been changed.'
;

COMMENT ON COLUMN "wfprev"."project_plan_fiscal_close_out_audit"."create_user"
	IS 'CREATE_USER is an audit column that indicates the user that created the record.'
;

COMMENT ON COLUMN "wfprev"."project_plan_fiscal_close_out_audit"."create_date"
	IS 'CREATE_DATE is the date and time the row of data was created.'
;

COMMENT ON COLUMN "wfprev"."project_plan_fiscal_close_out_audit"."update_user"
	IS 'UPDATE_USER is an audit column that indicates the user that updated the record.'
;

COMMENT ON COLUMN "wfprev"."project_plan_fiscal_close_out_audit"."update_date"
	IS 'UPDATE_DATE is the date and time the row of data was updated.'
;

/* Create Primary Keys, Indexes, Uniques, Checks */

ALTER TABLE "wfprev"."project_plan_fiscal_close_out_audit" ADD CONSTRAINT "ppfyco_aud_pk"
	PRIMARY KEY ("audit_table_sequence")
;
