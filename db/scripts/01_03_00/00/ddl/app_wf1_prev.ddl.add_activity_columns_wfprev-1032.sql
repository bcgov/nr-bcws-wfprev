ALTER TABLE wfprev.activity ADD COLUMN IF NOT EXISTS carry_forward_ind BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE wfprev.activity ADD COLUMN IF NOT EXISTS final_outcome_comments VARCHAR(500);