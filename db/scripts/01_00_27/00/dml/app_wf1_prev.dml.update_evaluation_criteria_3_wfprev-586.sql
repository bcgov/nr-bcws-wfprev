UPDATE wfprev.evaluation_criteria 
SET criteria_label = 'Prescription Complete - Next Phase is Treatment'
WHERE criteria_label = 'Prescription Complete - Next phase Rx Development';
