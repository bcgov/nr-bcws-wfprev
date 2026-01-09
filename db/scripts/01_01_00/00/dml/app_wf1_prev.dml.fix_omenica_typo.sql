-- WFPREV-839
-- Fix Omenica typo in forest_org_unit
UPDATE wfprev.forest_org_unit
SET org_unit_name = REPLACE(org_unit_name, 'Omenica', 'Omineca'),
    update_user = 'WFPREV-839',
    update_date = CURRENT_TIMESTAMP
WHERE org_unit_identifier = 1906;
