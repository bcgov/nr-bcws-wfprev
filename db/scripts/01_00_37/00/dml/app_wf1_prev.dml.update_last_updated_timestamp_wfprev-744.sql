UPDATE wfprev.activity
set last_updated_timestamp = update_date;

UPDATE wfprev.project
set last_updated_timestamp = update_date;

UPDATE wfprev.eval_criteria_summary
set last_updated_timestamp = update_date;