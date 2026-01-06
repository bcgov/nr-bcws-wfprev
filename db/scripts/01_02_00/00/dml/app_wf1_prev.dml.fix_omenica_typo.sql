UPDATE wfprev.forest_org_unit
SET org_unit_name = REPLACE(org_unit_name, 'Omenica', 'Omineca')
WHERE org_unit_name LIKE '%Omenica%';
