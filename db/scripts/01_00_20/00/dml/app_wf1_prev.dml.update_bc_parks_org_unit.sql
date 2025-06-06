
--Add Cariboo Chilcotin Coast Region
INSERT INTO wfprev.bc_parks_org_unit(org_unit_identifier, effective_date, expiry_date, bc_parks_org_unit_type_code, parent_org_unit_identifier, org_unit_name, integer_alias, character_alias, revision_count, create_user, create_date, update_user, update_date) 
VALUES (7, to_date('01-01-2000', 'dd-mm-yyyy'), to_date('31-12-9999', 'dd-mm-yyyy'), 'REGION', NULL, 'Cariboo Chilcotin Coast', 7, 'RCCC', 0, 'DATA_LOAD', to_date('01-12-2024', 'dd-mm-yyyy'), 'DATA_LOAD', to_date('01-12-2024', 'dd-mm-yyyy') );

--Rename Kootenay Region
UPDATE wfprev.bc_parks_org_unit
SET org_unit_name='Kootenay',
    character_alias = 'RKO'    
WHERE org_unit_identifier=1;    

--Rename Thompson Okanagan Region
UPDATE wfprev.bc_parks_org_unit
SET org_unit_name='Thompson Okanagan',
    character_alias = 'RTO'
WHERE org_unit_identifier=5;    

--remap Cariboo section to Cariboo Chilcotin Coast Region 
UPDATE wfprev.bc_parks_org_unit
SET parent_org_unit_identifier = 7
WHERE org_unit_identifier=19;

--remap Okanagan section to Thompson Okanagan Region 
UPDATE wfprev.bc_parks_org_unit
SET parent_org_unit_identifier = 5
WHERE org_unit_identifier=11;


--remap Okanagan section to Thompson Okanagan Region 
UPDATE wfprev.bc_parks_org_unit
SET parent_org_unit_identifier = 5
WHERE org_unit_identifier=11;

--rename South Island Section
UPDATE wfprev.bc_parks_org_unit
SET org_unit_name='South Island',
    character_alias = 'SSI'
WHERE org_unit_identifier=20;   

--rename Mid Island Section
UPDATE wfprev.bc_parks_org_unit
SET org_unit_name='Mid Island',
    character_alias = 'SMI'
WHERE org_unit_identifier=21;  

--Add South Island Section
INSERT INTO wfprev.bc_parks_org_unit(org_unit_identifier, effective_date, expiry_date, bc_parks_org_unit_type_code, parent_org_unit_identifier, org_unit_name, integer_alias, character_alias, revision_count, create_user, create_date, update_user, update_date) 
VALUES (22, to_date('01-01-2000', 'dd-mm-yyyy'), to_date('31-12-9999', 'dd-mm-yyyy'), 'SECTION', 6, 'North Island', 22, 'SNI', 0, 'DATA_LOAD', to_date('01-12-2024', 'dd-mm-yyyy'), 'DATA_LOAD', to_date('01-12-2024', 'dd-mm-yyyy') );

