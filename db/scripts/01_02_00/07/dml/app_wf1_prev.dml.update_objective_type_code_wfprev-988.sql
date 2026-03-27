-- WFPREV-988 : Fix capitalization of "Purposes Identified by Indigenous Peoples"

UPDATE wfprev.objective_type_code 
SET description = 'Purposes Identified by Indigenous Peoples', update_user = 'WFPREV-988', update_date = CURRENT_DATE, revision_count = revision_count + 1 
WHERE objective_type_code = 'RECONCIL';
