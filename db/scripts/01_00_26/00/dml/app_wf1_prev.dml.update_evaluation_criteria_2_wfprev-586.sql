UPDATE wfprev.evaluation_criteria 
SET weighted_rank = 10
WHERE criteria_label = 'Prescription Complete - Next phase Rx Development';

UPDATE wfprev.evaluation_criteria 
SET weighted_rank = 9
WHERE criteria_label = 'Tactical Plan Complete - New Phase Rx Development';