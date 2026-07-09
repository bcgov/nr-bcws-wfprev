UPDATE wfprev.project_plan_fiscal
SET is_bcws_hq_approved_ind = true,
    bcws_hq_approved_timestamp = now(),
    bcws_hq_approved_comment = 'This was approved during the ReMi 1.3.0 update.'
WHERE is_approved_ind = true AND endorsement_code = 'ENDORSED';