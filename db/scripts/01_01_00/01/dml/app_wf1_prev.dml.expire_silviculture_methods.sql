-- WFPREV-836: Expire Silviculture Methods

-- BR -> BU -> PBURN
UPDATE wfprev.silviculture_method
SET system_end_timestamp = CURRENT_TIMESTAMP, update_user = 'WFPREV-836', update_date = CURRENT_DATE
WHERE silviculture_method_guid = '1a230400-564c-4d42-9b61-2c2f5be65d13';

-- BR -> ME -> BURY, CHAUL, CSCAT, MULCH
UPDATE wfprev.silviculture_method
SET system_end_timestamp = CURRENT_TIMESTAMP, update_user = 'WFPREV-836', update_date = CURRENT_DATE
WHERE silviculture_method_guid IN (
    'c1d9c556-3fe0-40ec-a8cc-4a017ce6273e',
    '0348dd64-ef7e-4927-911d-3a9e9e830b8d',
    '24712f79-f819-441b-87a0-5b7b776da7e9',
    '5fe86566-c980-45ea-b7ab-3eafba719078'
);

-- PR -> ME -> BURY, CHAUL, CSCAT, MULCH
UPDATE wfprev.silviculture_method
SET system_end_timestamp = CURRENT_TIMESTAMP, update_user = 'WFPREV-836', update_date = CURRENT_DATE
WHERE silviculture_method_guid IN (
    '936794e5-423a-4d55-82a3-8cb59692423a',
    '174e7788-698a-4bb4-a37c-6facea27d674',
    '937c0ef6-7836-43f2-9b28-e687393e6cd3',
    '744d5080-a4a6-43b0-9209-e10fc746578d'
);
