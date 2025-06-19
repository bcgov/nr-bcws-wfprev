INSERT INTO wfprev.project_objective(
  project_objective_guid, 
  objective_label, 
  system_start_timestamp, system_end_timestamp, 
  objective_filter_level_code, project_type_code, 
  weighted_rank, display_order, 
  revision_count, create_user, create_date, update_user, update_date)
  VALUES (gen_random_uuid(), 
  'Treatment with several TUs moving onto next Unit', 
  to_date('01-01-2025', 'dd-mm-yyyy'), to_date('31-12-9999', 'dd-mm-yyyy'), 
  'MEDIUM_FLT', 'FUEL_MGMT', 
  12, 5, 
  0, 'DATA_LOAD', CURRENT_DATE, 'DATA_LOAD', CURRENT_DATE);

INSERT INTO wfprev.project_objective(
  project_objective_guid, 
  objective_label, 
  system_start_timestamp, system_end_timestamp, 
  objective_filter_level_code, project_type_code, 
  weighted_rank, display_order, 
  revision_count, create_user, create_date, update_user, update_date)
  VALUES (gen_random_uuid(), 
  'Adjacent Fuel Management Treatments', 
  to_date('01-01-2025', 'dd-mm-yyyy'), to_date('31-12-9999', 'dd-mm-yyyy'), 
  'MEDIUM_FLT', 'FUEL_MGMT', 
  11, 10, 
  0, 'DATA_LOAD', CURRENT_DATE, 'DATA_LOAD', CURRENT_DATE);

INSERT INTO wfprev.project_objective(
  project_objective_guid, 
  objective_label, 
  system_start_timestamp, system_end_timestamp, 
  objective_filter_level_code, project_type_code, 
  weighted_rank, display_order, 
  revision_count, create_user, create_date, update_user, update_date)
  VALUES (gen_random_uuid(), 
  'Prescription Complete - Next phase is Treatment', 
  to_date('01-01-2025', 'dd-mm-yyyy'), to_date('31-12-9999', 'dd-mm-yyyy'), 
  'MEDIUM_FLT', 'FUEL_MGMT', 
  10, 15, 
  0, 'DATA_LOAD', CURRENT_DATE, 'DATA_LOAD', CURRENT_DATE);

INSERT INTO wfprev.project_objective(
  project_objective_guid, 
  objective_label, 
  system_start_timestamp, system_end_timestamp, 
  objective_filter_level_code, project_type_code, 
  weighted_rank, display_order, 
  revision_count, create_user, create_date, update_user, update_date)
  VALUES (gen_random_uuid(), 
  'Prescription Complete - Next phase Rx Development', 
  to_date('01-01-2025', 'dd-mm-yyyy'), to_date('31-12-9999', 'dd-mm-yyyy'), 
  'MEDIUM_FLT', 'FUEL_MGMT', 
  9, 20, 
  0, 'DATA_LOAD', CURRENT_DATE, 'DATA_LOAD', CURRENT_DATE);

INSERT INTO wfprev.project_objective(
  project_objective_guid, 
  objective_label, 
  system_start_timestamp, system_end_timestamp, 
  objective_filter_level_code, project_type_code, 
  weighted_rank, display_order, 
  revision_count, create_user, create_date, update_user, update_date)
  VALUES (gen_random_uuid(), 
  'Mitigates Risk around or adjacent to Critical Infrastructure including Watersheds', 
  to_date('01-01-2025', 'dd-mm-yyyy'), to_date('31-12-9999', 'dd-mm-yyyy'), 
  'MEDIUM_FLT', 'FUEL_MGMT', 
  8, 25, 
  0, 'DATA_LOAD', CURRENT_DATE, 'DATA_LOAD', CURRENT_DATE);

INSERT INTO wfprev.project_objective(
  project_objective_guid, 
  objective_label, 
  system_start_timestamp, system_end_timestamp, 
  objective_filter_level_code, project_type_code, 
  weighted_rank, display_order, 
  revision_count, create_user, create_date, update_user, update_date)
  VALUES (gen_random_uuid(), 
  'Improve Egress/Evac Routes', 
  to_date('01-01-2025', 'dd-mm-yyyy'), to_date('31-12-9999', 'dd-mm-yyyy'), 
  'MEDIUM_FLT', 'FUEL_MGMT', 
  7, 30, 
  0, 'DATA_LOAD', CURRENT_DATE, 'DATA_LOAD', CURRENT_DATE);

INSERT INTO wfprev.project_objective(
  project_objective_guid, 
  objective_label, 
  system_start_timestamp, system_end_timestamp, 
  objective_filter_level_code, project_type_code, 
  weighted_rank, display_order, 
  revision_count, create_user, create_date, update_user, update_date)
  VALUES (gen_random_uuid(), 
  'Maximize Funding Linkages', 
  to_date('01-01-2025', 'dd-mm-yyyy'), to_date('31-12-9999', 'dd-mm-yyyy'), 
  'MEDIUM_FLT', 'FUEL_MGMT', 
  6, 35, 
  0, 'DATA_LOAD', CURRENT_DATE, 'DATA_LOAD', CURRENT_DATE);

INSERT INTO wfprev.project_objective(
  project_objective_guid, 
  objective_label, 
  system_start_timestamp, system_end_timestamp, 
  objective_filter_level_code, project_type_code, 
  weighted_rank, display_order, 
  revision_count, create_user, create_date, update_user, update_date)
  VALUES (gen_random_uuid(), 
  'Supports Reconcilliation with Indigenous peoples', 
  to_date('01-01-2025', 'dd-mm-yyyy'), to_date('31-12-9999', 'dd-mm-yyyy'), 
  'MEDIUM_FLT', 'FUEL_MGMT', 
  5, 40, 
  0, 'DATA_LOAD', CURRENT_DATE, 'DATA_LOAD', CURRENT_DATE);


INSERT INTO wfprev.project_objective(
  project_objective_guid, 
  objective_label, 
  system_start_timestamp, system_end_timestamp, 
  objective_filter_level_code, project_type_code, 
  weighted_rank, display_order, 
  revision_count, create_user, create_date, update_user, update_date)
  VALUES (gen_random_uuid(), 
  'PSTA Class Greater than 7', 
  to_date('01-01-2025', 'dd-mm-yyyy'), to_date('31-12-9999', 'dd-mm-yyyy'), 
  'FINE_FLT', 'FUEL_MGMT', 
  0.39, 5, 
  0, 'DATA_LOAD', CURRENT_DATE, 'DATA_LOAD', CURRENT_DATE);

  INSERT INTO wfprev.project_objective(
  project_objective_guid, 
  objective_label, 
  system_start_timestamp, system_end_timestamp, 
  objective_filter_level_code, project_type_code, 
  weighted_rank, display_order, 
  revision_count, create_user, create_date, update_user, update_date)
  VALUES (gen_random_uuid(), 
  'Part of Larger Risk Reduction Strategy', 
  to_date('01-01-2025', 'dd-mm-yyyy'), to_date('31-12-9999', 'dd-mm-yyyy'), 
  'FINE_FLT', 'FUEL_MGMT', 
  0.3, 10, 
  0, 'DATA_LOAD', CURRENT_DATE, 'DATA_LOAD', CURRENT_DATE);

  INSERT INTO wfprev.project_objective(
  project_objective_guid, 
  objective_label, 
  system_start_timestamp, system_end_timestamp, 
  objective_filter_level_code, project_type_code, 
  weighted_rank, display_order, 
  revision_count, create_user, create_date, update_user, update_date)
  VALUES (gen_random_uuid(), 
  'Cost Effectiveness', 
  to_date('01-01-2025', 'dd-mm-yyyy'), to_date('31-12-9999', 'dd-mm-yyyy'), 
  'FINE_FLT', 'FUEL_MGMT', 
  0.2, 15, 
  0, 'DATA_LOAD', CURRENT_DATE, 'DATA_LOAD', CURRENT_DATE);

  INSERT INTO wfprev.project_objective(
  project_objective_guid, 
  objective_label, 
  system_start_timestamp, system_end_timestamp, 
  objective_filter_level_code, project_type_code, 
  weighted_rank, display_order, 
  revision_count, create_user, create_date, update_user, update_date)
  VALUES (gen_random_uuid(), 
  'WUI WRR Plans with AOI that expands outside of the WUI', 
  to_date('01-01-2025', 'dd-mm-yyyy'), to_date('31-12-9999', 'dd-mm-yyyy'), 
  'FINE_FLT', 'FUEL_MGMT', 
  0.1, 20, 
  0, 'DATA_LOAD', CURRENT_DATE, 'DATA_LOAD', CURRENT_DATE);


















  