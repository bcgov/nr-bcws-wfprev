-- WFPREV-836: Add Silviculture Techniques

-- JS -> MA
INSERT INTO wfprev.silviculture_technique(silviculture_technique_guid, silviculture_base_guid, silviculture_technique_code, system_start_timestamp, system_end_timestamp, revision_count, create_user, create_date, update_user, update_date)
VALUES ('af57abc7-e971-40e1-b46d-4af5ed73e5e1', '83e66b50-46f0-4b06-8d5e-16bbb6e8e7cc', 'MA', CURRENT_DATE, '9999-12-31', 0, 'WFPREV-836', CURRENT_DATE, 'WFPREV-836', CURRENT_DATE);

-- LBP -> GR, OF
INSERT INTO wfprev.silviculture_technique(silviculture_technique_guid, silviculture_base_guid, silviculture_technique_code, system_start_timestamp, system_end_timestamp, revision_count, create_user, create_date, update_user, update_date)
VALUES 
('3d1dafc9-160d-4655-90f2-7a933bac449a', '18027a2f-ac74-4308-b9fc-4f1ee9d79326', 'GR', CURRENT_DATE, '9999-12-31', 0, 'WFPREV-836', CURRENT_DATE, 'WFPREV-836', CURRENT_DATE),
('c3d4e5f6-a7b8-4c7d-0e1f-2a3b4c5d6e7f', '18027a2f-ac74-4308-b9fc-4f1ee9d79326', 'OF', CURRENT_DATE, '9999-12-31', 0, 'WFPREV-836', CURRENT_DATE, 'WFPREV-836', CURRENT_DATE);

-- SP -> BI, CG
INSERT INTO wfprev.silviculture_technique(silviculture_technique_guid, silviculture_base_guid, silviculture_technique_code, system_start_timestamp, system_end_timestamp, revision_count, create_user, create_date, update_user, update_date)
VALUES 
('a5f4cd29-6ec8-4797-8672-a27313a5911e', '49edce8b-506c-4ec7-9e7d-573a70d3511e', 'BI', CURRENT_DATE, '9999-12-31', 0, 'WFPREV-836', CURRENT_DATE, 'WFPREV-836', CURRENT_DATE),
('f6a7b8c9-d0e1-4f0a-3b4c-8a9d0e1f2b3c', '49edce8b-506c-4ec7-9e7d-573a70d3511e', 'CG', CURRENT_DATE, '9999-12-31', 0, 'WFPREV-836', CURRENT_DATE, 'WFPREV-836', CURRENT_DATE);

-- SU -> FG, HZ, PH, SM
INSERT INTO wfprev.silviculture_technique(silviculture_technique_guid, silviculture_base_guid, silviculture_technique_code, system_start_timestamp, system_end_timestamp, revision_count, create_user, create_date, update_user, update_date)
VALUES 
('fad19705-9729-4af4-a0f6-116ef3ee2a59', 'fd94ccda-c7fa-4084-8f02-6710848d0342', 'FG', CURRENT_DATE, '9999-12-31', 0, 'WFPREV-836', CURRENT_DATE, 'WFPREV-836', CURRENT_DATE),
('2c3d4e5f-6a7b-4c8d-0e1f-2a3b4c5d6e7f', 'fd94ccda-c7fa-4084-8f02-6710848d0342', 'HZ', CURRENT_DATE, '9999-12-31', 0, 'WFPREV-836', CURRENT_DATE, 'WFPREV-836', CURRENT_DATE),
('5f6a7b8c-9d0e-1f2a-3b4c-8a9d0e1f2b3c', 'fd94ccda-c7fa-4084-8f02-6710848d0342', 'PH', CURRENT_DATE, '9999-12-31', 0, 'WFPREV-836', CURRENT_DATE, 'WFPREV-836', CURRENT_DATE),
('aa7578a4-5aec-43a9-b8ce-cd77fd461722', 'fd94ccda-c7fa-4084-8f02-6710848d0342', 'SM', CURRENT_DATE, '9999-12-31', 0, 'WFPREV-836', CURRENT_DATE, 'WFPREV-836', CURRENT_DATE);
