INSERT INTO wfprev.wui_risk_class_rank(
  wui_risk_class_rank_guid, 
  wui_risk_class_code, 
  system_start_timestamp, system_end_timestamp,
  weighted_rank, 
  revision_count, create_user, create_date, update_user, update_date)
  VALUES (
    gen_random_uuid(), 
    'WUI_RC_1', 
    to_date('01-01-2025', 'dd-mm-yyyy'), to_date('31-12-9999', 'dd-mm-yyyy'),
    10, 
    0, 'DATA_LOAD', CURRENT_DATE, 'DATA_LOAD', CURRENT_DATE);

INSERT INTO wfprev.wui_risk_class_rank(
  wui_risk_class_rank_guid, 
  wui_risk_class_code, 
  system_start_timestamp, system_end_timestamp,
  weighted_rank, 
  revision_count, create_user, create_date, update_user, update_date)
  VALUES (
    gen_random_uuid(), 
    'WUI_RC_2', 
    to_date('01-01-2025', 'dd-mm-yyyy'), to_date('31-12-9999', 'dd-mm-yyyy'),
    8, 
    0, 'DATA_LOAD', CURRENT_DATE, 'DATA_LOAD', CURRENT_DATE);    

INSERT INTO wfprev.wui_risk_class_rank(
  wui_risk_class_rank_guid, 
  wui_risk_class_code, 
  system_start_timestamp, system_end_timestamp,
  weighted_rank, 
  revision_count, create_user, create_date, update_user, update_date)
  VALUES (
    gen_random_uuid(), 
    'WUI_RC_3', 
    to_date('01-01-2025', 'dd-mm-yyyy'), to_date('31-12-9999', 'dd-mm-yyyy'),
    6, 
    0, 'DATA_LOAD', CURRENT_DATE, 'DATA_LOAD', CURRENT_DATE);  

INSERT INTO wfprev.wui_risk_class_rank(
  wui_risk_class_rank_guid, 
  wui_risk_class_code, 
  system_start_timestamp, system_end_timestamp,
  weighted_rank, 
  revision_count, create_user, create_date, update_user, update_date)
  VALUES (
    gen_random_uuid(), 
    'WUI_RC_4', 
    to_date('01-01-2025', 'dd-mm-yyyy'), to_date('31-12-9999', 'dd-mm-yyyy'),
    4, 
    0, 'DATA_LOAD', CURRENT_DATE, 'DATA_LOAD', CURRENT_DATE);  

INSERT INTO wfprev.wui_risk_class_rank(
  wui_risk_class_rank_guid, 
  wui_risk_class_code, 
  system_start_timestamp, system_end_timestamp,
  weighted_rank, 
  revision_count, create_user, create_date, update_user, update_date)
  VALUES (
    gen_random_uuid(), 
    'WUI_RC_5', 
    to_date('01-01-2025', 'dd-mm-yyyy'), to_date('31-12-9999', 'dd-mm-yyyy'),
    2, 
    0, 'DATA_LOAD', CURRENT_DATE, 'DATA_LOAD', CURRENT_DATE);  