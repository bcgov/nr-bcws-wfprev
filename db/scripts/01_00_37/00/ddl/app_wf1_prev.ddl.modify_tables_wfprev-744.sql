ALTER TABLE wfprev.project ADD COLUMN "last_updated_timestamp" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE wfprev.project_audit ADD COLUMN "last_updated_timestamp" TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP;
COMMENT ON COLUMN "wfprev"."project"."last_updated_timestamp"
  IS 'Last Updated Timestamp is the date and time the record was changed.'
;
COMMENT ON COLUMN "wfprev"."project_audit"."last_updated_timestamp"
  IS 'Last Updated Timestamp is the date and time the record was changed.'
;

ALTER TABLE wfprev.eval_criteria_summary ADD COLUMN "last_updated_timestamp" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
COMMENT ON COLUMN "wfprev"."eval_criteria_summary"."last_updated_timestamp"
  IS 'Last Updated Timestamp is the date and time the record was changed.'
;

ALTER TABLE wfprev.activity ADD COLUMN "last_updated_timestamp" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE wfprev.activity_audit ADD COLUMN "last_updated_timestamp" TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP;
COMMENT ON COLUMN "wfprev"."activity"."last_updated_timestamp"
  IS 'Last Updated Timestamp is the date and time the record was changed.'
;
COMMENT ON COLUMN "wfprev"."activity_audit"."last_updated_timestamp"
  IS 'Last Updated Timestamp is the date and time the record was changed.'
;