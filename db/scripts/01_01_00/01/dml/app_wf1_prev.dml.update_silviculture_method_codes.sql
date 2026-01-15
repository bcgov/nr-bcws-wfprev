-- WFPREV-836: Update Silviculture Method Code Descriptions

UPDATE wfprev.silviculture_method_code SET description = 'Bury Surface Fuels', update_user = 'WFPREV-836', update_date = CURRENT_DATE WHERE silviculture_method_code = 'BURY';
UPDATE wfprev.silviculture_method_code SET description = 'Chipping and Haul from site', update_user = 'WFPREV-836', update_date = CURRENT_DATE WHERE silviculture_method_code = 'CHAUL';
UPDATE wfprev.silviculture_method_code SET description = 'Chipping and Scatter', update_user = 'WFPREV-836', update_date = CURRENT_DATE WHERE silviculture_method_code = 'CSCAT';
