-- WFPREV-836: Add Silviculture Methods

-- BR -> MA -> MANCT, POWER
INSERT INTO wfprev.silviculture_method(silviculture_method_guid, silviculture_technique_guid, silviculture_method_code, system_start_timestamp, system_end_timestamp, revision_count, create_user, create_date, update_user, update_date)
VALUES 
('edd5b1e0-73a3-4cb8-84d8-57d04a990b5b', '3d7d01dc-4dbd-4f34-adc7-80fe3b975d85', 'MANCT', CURRENT_DATE, '9999-12-31', 0, 'WFPREV-836', CURRENT_DATE, 'WFPREV-836', CURRENT_DATE),
('951a828b-9149-47ca-a018-7c74c7e550f4', '3d7d01dc-4dbd-4f34-adc7-80fe3b975d85', 'POWER', CURRENT_DATE, '9999-12-31', 0, 'WFPREV-836', CURRENT_DATE, 'WFPREV-836', CURRENT_DATE);

-- JS -> MA -> BRUSH, MANCT, POWER
INSERT INTO wfprev.silviculture_method(silviculture_method_guid, silviculture_technique_guid, silviculture_method_code, system_start_timestamp, system_end_timestamp, revision_count, create_user, create_date, update_user, update_date)
VALUES 
('d0c31b9a-38a7-452e-b7a6-4aaba4cc24d1', 'af57abc7-e971-40e1-b46d-4af5ed73e5e1', 'BRUSH', CURRENT_DATE, '9999-12-31', 0, 'WFPREV-836', CURRENT_DATE, 'WFPREV-836', CURRENT_DATE),
('71f5b560-b506-480f-90ab-776e25c136c4', 'af57abc7-e971-40e1-b46d-4af5ed73e5e1', 'MANCT', CURRENT_DATE, '9999-12-31', 0, 'WFPREV-836', CURRENT_DATE, 'WFPREV-836', CURRENT_DATE),
('dfef5d11-2631-4c7e-9d38-a27300dd4e6d', 'af57abc7-e971-40e1-b46d-4af5ed73e5e1', 'POWER', CURRENT_DATE, '9999-12-31', 0, 'WFPREV-836', CURRENT_DATE, 'WFPREV-836', CURRENT_DATE);

-- LBP -> GR -> WALK
INSERT INTO wfprev.silviculture_method(silviculture_method_guid, silviculture_technique_guid, silviculture_method_code, system_start_timestamp, system_end_timestamp, revision_count, create_user, create_date, update_user, update_date)
VALUES ('e79d02a7-5b1f-4216-b222-9653add67166', '3d1dafc9-160d-4655-90f2-7a933bac449a', 'WALK', CURRENT_DATE, '9999-12-31', 0, 'WFPREV-836', CURRENT_DATE, 'WFPREV-836', CURRENT_DATE);

-- LBP -> OF -> FILE
INSERT INTO wfprev.silviculture_method(silviculture_method_guid, silviculture_technique_guid, silviculture_method_code, system_start_timestamp, system_end_timestamp, revision_count, create_user, create_date, update_user, update_date)
VALUES ('bae231fd-a4bc-4ead-b2cf-3eab146a5c96', 'c3d4e5f6-a7b8-4c7d-0e1f-2a3b4c5d6e7f', 'FILE', CURRENT_DATE, '9999-12-31', 0, 'WFPREV-836', CURRENT_DATE, 'WFPREV-836', CURRENT_DATE);

-- PR -> MA -> HANDS, POLE
INSERT INTO wfprev.silviculture_method(silviculture_method_guid, silviculture_technique_guid, silviculture_method_code, system_start_timestamp, system_end_timestamp, revision_count, create_user, create_date, update_user, update_date)
VALUES 
('8bb35b8f-66a0-41c0-b5cc-008f08c93d45', '4bdcf317-6a4c-4dfc-a64f-c671483d40a9', 'HANDS', CURRENT_DATE, '9999-12-31', 0, 'WFPREV-836', CURRENT_DATE, 'WFPREV-836', CURRENT_DATE),
('39f48b72-74ee-48ad-b353-1ade7d1f3d56', '4bdcf317-6a4c-4dfc-a64f-c671483d40a9', 'POLE', CURRENT_DATE, '9999-12-31', 0, 'WFPREV-836', CURRENT_DATE, 'WFPREV-836', CURRENT_DATE);

-- PR -> ME -> MOCUT
INSERT INTO wfprev.silviculture_method(silviculture_method_guid, silviculture_technique_guid, silviculture_method_code, system_start_timestamp, system_end_timestamp, revision_count, create_user, create_date, update_user, update_date)
VALUES ('30687130-9f28-4081-a18a-8b1f8c766cb6', 'ce4eb68f-6fe4-4f37-88b6-f85311664402', 'MOCUT', CURRENT_DATE, '9999-12-31', 0, 'WFPREV-836', CURRENT_DATE, 'WFPREV-836', CURRENT_DATE);

-- SP -> BU -> SPOT
INSERT INTO wfprev.silviculture_method(silviculture_method_guid, silviculture_technique_guid, silviculture_method_code, system_start_timestamp, system_end_timestamp, revision_count, create_user, create_date, update_user, update_date)
VALUES ('6b225a8c-564e-4557-8537-75b7961d532c', 'c16e9997-c927-47f2-91b1-e85bdf381b66', 'SPOT', CURRENT_DATE, '9999-12-31', 0, 'WFPREV-836', CURRENT_DATE, 'WFPREV-836', CURRENT_DATE);

-- SP -> MA -> HAND, MANCT, PILE, POWER, SNAG
INSERT INTO wfprev.silviculture_method(silviculture_method_guid, silviculture_technique_guid, silviculture_method_code, system_start_timestamp, system_end_timestamp, revision_count, create_user, create_date, update_user, update_date)
VALUES 
('a56fbf3d-591f-4196-bbcc-1cb961ec828b', 'd07210a4-f721-453d-84e9-345791d4ea50', 'HAND', CURRENT_DATE, '9999-12-31', 0, 'WFPREV-836', CURRENT_DATE, 'WFPREV-836', CURRENT_DATE),
('4a5b6c7d-8e9f-0a1b-2c3d-4e5f6a7b8c9d', 'd07210a4-f721-453d-84e9-345791d4ea50', 'MANCT', CURRENT_DATE, '9999-12-31', 0, 'WFPREV-836', CURRENT_DATE, 'WFPREV-836', CURRENT_DATE),
('5b6c7d8e-9f0a-1b2c-3d4e-5f6a7b8c9d0e', 'd07210a4-f721-453d-84e9-345791d4ea50', 'PILE', CURRENT_DATE, '9999-12-31', 0, 'WFPREV-836', CURRENT_DATE, 'WFPREV-836', CURRENT_DATE),
('6c7d8e9f-0a1b-2c3d-4e5f-6a7b8c9d0e1f', 'd07210a4-f721-453d-84e9-345791d4ea50', 'POWER', CURRENT_DATE, '9999-12-31', 0, 'WFPREV-836', CURRENT_DATE, 'WFPREV-836', CURRENT_DATE),
('7d8e9f0a-1b2c-3d4e-5f6a-7b8c9d0e1f2a', 'd07210a4-f721-453d-84e9-345791d4ea50', 'SNAG', CURRENT_DATE, '9999-12-31', 0, 'WFPREV-836', CURRENT_DATE, 'WFPREV-836', CURRENT_DATE);

-- SP -> ME -> GUARD, HARV, MDOWN, PILE, MULCH
INSERT INTO wfprev.silviculture_method(silviculture_method_guid, silviculture_technique_guid, silviculture_method_code, system_start_timestamp, system_end_timestamp, revision_count, create_user, create_date, update_user, update_date)
VALUES 
('a9987354-b03b-4762-9890-c15da5102cde', 'bcf19d89-a141-4877-8e89-c9d5dd80951f', 'GUARD', CURRENT_DATE, '9999-12-31', 0, 'WFPREV-836', CURRENT_DATE, 'WFPREV-836', CURRENT_DATE),
('9f0a1b2c-3d4e-5f6a-7b8c-9d0e1f2a3b4c', 'bcf19d89-a141-4877-8e89-c9d5dd80951f', 'HARV', CURRENT_DATE, '9999-12-31', 0, 'WFPREV-836', CURRENT_DATE, 'WFPREV-836', CURRENT_DATE),
('0a1b2c3d-4e5f-6a7b-8c9d-0e1f2a3b4c5d', 'bcf19d89-a141-4877-8e89-c9d5dd80951f', 'MDOWN', CURRENT_DATE, '9999-12-31', 0, 'WFPREV-836', CURRENT_DATE, 'WFPREV-836', CURRENT_DATE),
('1b2c3d4e-5f6a-7b8c-9d0e-1f2a3b4c5d6e', 'bcf19d89-a141-4877-8e89-c9d5dd80951f', 'PILE', CURRENT_DATE, '9999-12-31', 0, 'WFPREV-836', CURRENT_DATE, 'WFPREV-836', CURRENT_DATE),
('2c3d4e5f-6a7b-8c9d-0e1f-2a3b4c5d6e7f', 'bcf19d89-a141-4877-8e89-c9d5dd80951f', 'MULCH', CURRENT_DATE, '9999-12-31', 0, 'WFPREV-836', CURRENT_DATE, 'WFPREV-836', CURRENT_DATE);

-- SU -> FG -> MULTI, PLOT
INSERT INTO wfprev.silviculture_method(silviculture_method_guid, silviculture_technique_guid, silviculture_method_code, system_start_timestamp, system_end_timestamp, revision_count, create_user, create_date, update_user, update_date)
VALUES 
('3913b4ed-ea3e-4797-9498-996e93252a44', 'fad19705-9729-4af4-a0f6-116ef3ee2a59', 'MULTI', CURRENT_DATE, '9999-12-31', 0, 'WFPREV-836', CURRENT_DATE, 'WFPREV-836', CURRENT_DATE),
('4e5f6a7b-8c9d-0e1f-2a3b-4c5d6e7f8a9b', 'fad19705-9729-4af4-a0f6-116ef3ee2a59', 'PLOT', CURRENT_DATE, '9999-12-31', 0, 'WFPREV-836', CURRENT_DATE, 'WFPREV-836', CURRENT_DATE);

-- SU -> FH -> LAYOT, PLOT
INSERT INTO wfprev.silviculture_method(silviculture_method_guid, silviculture_technique_guid, silviculture_method_code, system_start_timestamp, system_end_timestamp, revision_count, create_user, create_date, update_user, update_date)
VALUES 
('9611e4e9-baef-4c62-84be-1fab1099955d', 'dd90a5fd-e583-4faf-b296-0fd402bcc135', 'LAYOT', CURRENT_DATE, '9999-12-31', 0, 'WFPREV-836', CURRENT_DATE, 'WFPREV-836', CURRENT_DATE),
('6a7b8c9d-0e1f-2a3b-4c5d-6e7f8a9b0c1d', 'dd90a5fd-e583-4faf-b296-0fd402bcc135', 'PLOT', CURRENT_DATE, '9999-12-31', 0, 'WFPREV-836', CURRENT_DATE, 'WFPREV-836', CURRENT_DATE);

-- SU -> HZ -> WALK
INSERT INTO wfprev.silviculture_method(silviculture_method_guid, silviculture_technique_guid, silviculture_method_code, system_start_timestamp, system_end_timestamp, revision_count, create_user, create_date, update_user, update_date)
VALUES ('2e3e3d88-b1cc-4184-afef-b784f8216225', '2c3d4e5f-6a7b-4c8d-0e1f-2a3b4c5d6e7f', 'WALK', CURRENT_DATE, '9999-12-31', 0, 'WFPREV-836', CURRENT_DATE, 'WFPREV-836', CURRENT_DATE);

-- SU -> JS -> PLOT, WALK
INSERT INTO wfprev.silviculture_method(silviculture_method_guid, silviculture_technique_guid, silviculture_method_code, system_start_timestamp, system_end_timestamp, revision_count, create_user, create_date, update_user, update_date)
VALUES 
('fabbee66-2c12-41af-b37a-249f6dca41e9', '89f075b6-fcbc-45b4-bb34-7f8e34d35783', 'PLOT', CURRENT_DATE, '9999-12-31', 0, 'WFPREV-836', CURRENT_DATE, 'WFPREV-836', CURRENT_DATE),
('9d0e1f2a-3b4c-5d6e-7f8a-9b0c1d2e3f4a', '89f075b6-fcbc-45b4-bb34-7f8e34d35783', 'WALK', CURRENT_DATE, '9999-12-31', 0, 'WFPREV-836', CURRENT_DATE, 'WFPREV-836', CURRENT_DATE);

-- SU -> MO -> SLBD
INSERT INTO wfprev.silviculture_method(silviculture_method_guid, silviculture_technique_guid, silviculture_method_code, system_start_timestamp, system_end_timestamp, revision_count, create_user, create_date, update_user, update_date)
VALUES ('c581100e-f551-4d5e-9036-6bd7d5d1dd21', '8bc0682e-f3d6-49b9-8830-5d74f6a94c3f', 'SLBD', CURRENT_DATE, '9999-12-31', 0, 'WFPREV-836', CURRENT_DATE, 'WFPREV-836', CURRENT_DATE);

-- SU -> PH -> PLOT, WALK
INSERT INTO wfprev.silviculture_method(silviculture_method_guid, silviculture_technique_guid, silviculture_method_code, system_start_timestamp, system_end_timestamp, revision_count, create_user, create_date, update_user, update_date)
VALUES 
('2a590a67-be8f-4e40-adb0-20d08e530d1a', '5f6a7b8c-9d0e-1f2a-3b4c-8a9d0e1f2b3c', 'PLOT', CURRENT_DATE, '9999-12-31', 0, 'WFPREV-836', CURRENT_DATE, 'WFPREV-836', CURRENT_DATE),
('32d3b3e9-23ff-4e50-833c-bbbc5671c872', '5f6a7b8c-9d0e-1f2a-3b4c-8a9d0e1f2b3c', 'WALK', CURRENT_DATE, '9999-12-31', 0, 'WFPREV-836', CURRENT_DATE, 'WFPREV-836', CURRENT_DATE);

-- SU -> PL -> PLOT
INSERT INTO wfprev.silviculture_method(silviculture_method_guid, silviculture_technique_guid, silviculture_method_code, system_start_timestamp, system_end_timestamp, revision_count, create_user, create_date, update_user, update_date)
VALUES ('f0aa3bed-32b8-41a3-ae64-d4629aaef189', 'a536cc9c-d7c7-42ca-85bf-918d21a04d15', 'PLOT', CURRENT_DATE, '9999-12-31', 0, 'WFPREV-836', CURRENT_DATE, 'WFPREV-836', CURRENT_DATE);

-- SU -> PR -> WALK
INSERT INTO wfprev.silviculture_method(silviculture_method_guid, silviculture_technique_guid, silviculture_method_code, system_start_timestamp, system_end_timestamp, revision_count, create_user, create_date, update_user, update_date)
VALUES ('f1011d64-e94f-4e8d-9776-a0523c51be20', '6476d9c2-52da-4804-9923-9442c6e4d3eb', 'WALK', CURRENT_DATE, '9999-12-31', 0, 'WFPREV-836', CURRENT_DATE, 'WFPREV-836', CURRENT_DATE);

-- SU -> RE -> WALK
INSERT INTO wfprev.silviculture_method(silviculture_method_guid, silviculture_technique_guid, silviculture_method_code, system_start_timestamp, system_end_timestamp, revision_count, create_user, create_date, update_user, update_date)
VALUES ('8d5f3948-2c85-4ffe-a856-6f0e26303f1f', 'fe42fbf6-1bc5-488a-8435-2340ef68b489', 'WALK', CURRENT_DATE, '9999-12-31', 0, 'WFPREV-836', CURRENT_DATE, 'WFPREV-836', CURRENT_DATE);

-- SU -> RG -> MULTI, PLOT
INSERT INTO wfprev.silviculture_method(silviculture_method_guid, silviculture_technique_guid, silviculture_method_code, system_start_timestamp, system_end_timestamp, revision_count, create_user, create_date, update_user, update_date)
VALUES 
('ce035103-0f44-4ab6-89e6-550baf7f7fed', '75a90d66-74ef-402f-9a3f-756f17fa3957', 'MULTI', CURRENT_DATE, '9999-12-31', 0, 'WFPREV-836', CURRENT_DATE, 'WFPREV-836', CURRENT_DATE),
('2de753d0-2842-4237-8f17-f40be7489859', '75a90d66-74ef-402f-9a3f-756f17fa3957', 'PLOT', CURRENT_DATE, '9999-12-31', 0, 'WFPREV-836', CURRENT_DATE, 'WFPREV-836', CURRENT_DATE);

-- SU -> SM -> PLOT, WALK
INSERT INTO wfprev.silviculture_method(silviculture_method_guid, silviculture_technique_guid, silviculture_method_code, system_start_timestamp, system_end_timestamp, revision_count, create_user, create_date, update_user, update_date)
VALUES 
('500a93ac-9dbe-4df5-8685-3fda3f8e1807', 'aa7578a4-5aec-43a9-b8ce-cd77fd461722', 'PLOT', CURRENT_DATE, '9999-12-31', 0, 'WFPREV-836', CURRENT_DATE, 'WFPREV-836', CURRENT_DATE),
('3ecf2ad3-9e0f-4d50-8d6e-40d9b093fa6d', 'aa7578a4-5aec-43a9-b8ce-cd77fd461722', 'WALK', CURRENT_DATE, '9999-12-31', 0, 'WFPREV-836', CURRENT_DATE, 'WFPREV-836', CURRENT_DATE);

-- SU -> SR -> PLOT, WALK
INSERT INTO wfprev.silviculture_method(silviculture_method_guid, silviculture_technique_guid, silviculture_method_code, system_start_timestamp, system_end_timestamp, revision_count, create_user, create_date, update_user, update_date)
VALUES 
('2ba78e25-fae0-41db-8359-2295947a3886', 'f982ecfd-61d6-4c60-91b1-69cfe5c3a085', 'PLOT', CURRENT_DATE, '9999-12-31', 0, 'WFPREV-836', CURRENT_DATE, 'WFPREV-836', CURRENT_DATE),
('b78dc242-6050-4284-b0ca-73d4b125603d', 'f982ecfd-61d6-4c60-91b1-69cfe5c3a085', 'WALK', CURRENT_DATE, '9999-12-31', 0, 'WFPREV-836', CURRENT_DATE, 'WFPREV-836', CURRENT_DATE);
