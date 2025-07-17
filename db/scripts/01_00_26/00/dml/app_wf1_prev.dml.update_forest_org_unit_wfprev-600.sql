--Move South Island District to West Coast Region.
UPDATE wfprev.forest_org_unit
SET parent_org_unit_identifier = 1910
where org_unit_identifier = 1619;

--Rename the DVA Org Unit to Stuart Nechako
UPDATE wfprev.forest_org_unit
SET parent_org_unit_identifier = 1906,
    org_unit_name = 'Stuart Nechako District'
WHERE org_unit_identifier = 30;

--Remove org unit Fort St. James Forest District
DELETE FROM wfprev.forest_org_unit
WHERE org_unit_identifier = 34; 

--Remove the word forest from district names
UPDATE wfprev.forest_org_unit
SET org_unit_name = replace (org_unit_name, 'Forest ', '');

