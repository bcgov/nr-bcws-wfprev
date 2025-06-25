-- Alter the column to allow decimal precision
ALTER TABLE wfprev.fuel_management_objective
ALTER COLUMN weighted_rank TYPE DECIMAL(4,2);

UPDATE wfprev.fuel_management_objective
SET weighted_rank = 0.39
WHERE objective_label = 'PSTA Class Greater than 7';

UPDATE wfprev.fuel_management_objective
SET weighted_rank = 0.3
WHERE objective_label = 'Part of Larger Risk Reduction Strategy';

UPDATE wfprev.fuel_management_objective
SET weighted_rank = 0.2
WHERE objective_label = 'Cost Effectiveness';

UPDATE wfprev.fuel_management_objective
SET weighted_rank = 0.1
WHERE objective_label = 'WUI WRR Plans with AOI that expands outside of the WUI';
