-- WFPREV-895: Rename Survey to Surveys

UPDATE wfprev.silviculture_base_code
SET description = 'Surveys',
    update_user = 'WFPREV-895',
    update_date = CURRENT_DATE,
    revision_count = revision_count + 1
WHERE silviculture_base_code = 'SU';
