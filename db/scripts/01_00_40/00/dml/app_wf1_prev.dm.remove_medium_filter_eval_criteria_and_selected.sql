-- First delete from wfprev.eval_criteria_selected to avoid foreign key violations.
DELETE FROM wfprev.eval_criteria_selected ecs
USING wfprev.evaluation_criteria ec
WHERE ecs.evaluation_criteria_guid = ec.evaluation_criteria_guid
  AND ec.eval_criteria_sect_code = 'MEDIUM_FLT'
  AND ec.project_type_code = 'FUEL_MGMT'
  AND ec.criteria_label IN (
    'Treatment with several TUs moving onto next Unit',
    'Prescription Complete - Next Phase is Treatment',
    'Tactical Plan Complete - New Phase Rx Development'
  );

DELETE FROM wfprev.evaluation_criteria
WHERE eval_criteria_sect_code = 'MEDIUM_FLT'
  AND project_type_code = 'FUEL_MGMT'
  AND criteria_label IN (
    'Treatment with several TUs moving onto next Unit',
    'Prescription Complete - Next Phase is Treatment',
    'Tactical Plan Complete - New Phase Rx Development'
  );