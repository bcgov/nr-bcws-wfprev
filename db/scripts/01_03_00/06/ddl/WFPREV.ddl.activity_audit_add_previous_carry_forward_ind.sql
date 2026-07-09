ALTER TABLE wfprev.activity_audit ADD COLUMN IF NOT EXISTS previous_carry_forward_ind BOOLEAN NOT NULL DEFAULT FALSE;

COMMENT ON COLUMN wfprev.activity_audit.previous_carry_forward_ind IS 'Indicates if an activity has been carried forward from the previous fiscal year.';