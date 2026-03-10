-- WFPREV-840: Update objective type codes

-- Rename "Reconciliation" to "Purposes Identified By Indigenous Peoples"
UPDATE wfprev.objective_type_code 
SET description = 'Purposes Identified By Indigenous Peoples', update_user = 'WFPREV-840', update_date = CURRENT_DATE, revision_count = revision_count + 1 
WHERE objective_type_code = 'RECONCIL';

-- Rename "Hazard Abatement" to "Fire Hazard Abatement"
UPDATE wfprev.objective_type_code 
SET description = 'Fire Hazard Abatement', update_user = 'WFPREV-840', update_date = CURRENT_DATE, revision_count = revision_count + 1 
WHERE objective_type_code = 'HAZ_ABATE';

-- Add "Silviculture Treatment"
INSERT INTO wfprev.objective_type_code (objective_type_code, description, display_order, effective_date, expiry_date, revision_count, create_user, create_date, update_user, update_date)
VALUES ('SILV_TREAT', 'Silviculture Treatment', 8, to_date('01-01-2000', 'dd-mm-yyyy'), to_date('31-12-9999', 'dd-mm-yyyy'), 0, 'WFPREV-840', CURRENT_DATE, 'WFPREV-840', CURRENT_DATE);

-- Update display orders
UPDATE wfprev.objective_type_code SET display_order = 1, update_user = 'WFPREV-840', update_date = CURRENT_DATE, revision_count = revision_count + 1 WHERE objective_type_code = 'CRIT_INFRA';
UPDATE wfprev.objective_type_code SET display_order = 2, update_user = 'WFPREV-840', update_date = CURRENT_DATE, revision_count = revision_count + 1 WHERE objective_type_code = 'ECO_REST';
UPDATE wfprev.objective_type_code SET display_order = 3, update_user = 'WFPREV-840', update_date = CURRENT_DATE, revision_count = revision_count + 1 WHERE objective_type_code = 'EGRSS_EVAC';
UPDATE wfprev.objective_type_code SET display_order = 4, update_user = 'WFPREV-840', update_date = CURRENT_DATE, revision_count = revision_count + 1 WHERE objective_type_code = 'FOR_HEALTH';
UPDATE wfprev.objective_type_code SET display_order = 5, update_user = 'WFPREV-840', update_date = CURRENT_DATE, revision_count = revision_count + 1 WHERE objective_type_code = 'HAZ_ABATE';
UPDATE wfprev.objective_type_code SET display_order = 6, update_user = 'WFPREV-840', update_date = CURRENT_DATE, revision_count = revision_count + 1 WHERE objective_type_code = 'RECONCIL';
UPDATE wfprev.objective_type_code SET display_order = 7, update_user = 'WFPREV-840', update_date = CURRENT_DATE, revision_count = revision_count + 1 WHERE objective_type_code = 'RNG_HAB_MG';
-- display_order 8 is SILV_TREAT (inserted above)
UPDATE wfprev.objective_type_code SET display_order = 9, update_user = 'WFPREV-840', update_date = CURRENT_DATE, revision_count = revision_count + 1 WHERE objective_type_code = 'WLD_HAB_MG';
UPDATE wfprev.objective_type_code SET display_order = 10, update_user = 'WFPREV-840', update_date = CURRENT_DATE, revision_count = revision_count + 1 WHERE objective_type_code = 'WRR';
UPDATE wfprev.objective_type_code SET display_order = 11, update_user = 'WFPREV-840', update_date = CURRENT_DATE, revision_count = revision_count + 1 WHERE objective_type_code = 'OTHER';
