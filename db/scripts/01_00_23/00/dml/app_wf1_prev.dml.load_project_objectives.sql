INSERT INTO wfprev.evaluation_criteria(
  evaluation_criteria_guid, 
  criteria_label, 
  system_start_timestamp, system_end_timestamp, 
  eval_criteria_sect_code, project_type_code, 
  weighted_rank, display_order, 
  revision_count, create_user, create_date, update_user, update_date)
  VALUES (gen_random_uuid(), 
  'Treatment with several TUs moving onto next Unit', 
  to_date('01-01-2025', 'dd-mm-yyyy'), to_date('31-12-9999', 'dd-mm-yyyy'), 
  'MEDIUM_FLT', 'FUEL_MGMT', 
  12, 5, 
  0, 'DATA_LOAD', CURRENT_DATE, 'DATA_LOAD', CURRENT_DATE);

INSERT INTO wfprev.evaluation_criteria(
  evaluation_criteria_guid, 
  criteria_label, 
  system_start_timestamp, system_end_timestamp, 
  eval_criteria_sect_code, project_type_code, 
  weighted_rank, display_order, 
  revision_count, create_user, create_date, update_user, update_date)
  VALUES (gen_random_uuid(), 
  'Adjacent Fuel Management Treatments', 
  to_date('01-01-2025', 'dd-mm-yyyy'), to_date('31-12-9999', 'dd-mm-yyyy'), 
  'MEDIUM_FLT', 'FUEL_MGMT', 
  11, 10, 
  0, 'DATA_LOAD', CURRENT_DATE, 'DATA_LOAD', CURRENT_DATE);

INSERT INTO wfprev.evaluation_criteria(
  evaluation_criteria_guid, 
  criteria_label, 
  system_start_timestamp, system_end_timestamp, 
  eval_criteria_sect_code, project_type_code, 
  weighted_rank, display_order, 
  revision_count, create_user, create_date, update_user, update_date)
  VALUES (gen_random_uuid(), 
  'Prescription Complete - Next phase is Treatment', 
  to_date('01-01-2025', 'dd-mm-yyyy'), to_date('31-12-9999', 'dd-mm-yyyy'), 
  'MEDIUM_FLT', 'FUEL_MGMT', 
  10, 15, 
  0, 'DATA_LOAD', CURRENT_DATE, 'DATA_LOAD', CURRENT_DATE);

INSERT INTO wfprev.evaluation_criteria(
  evaluation_criteria_guid, 
  criteria_label, 
  system_start_timestamp, system_end_timestamp, 
  eval_criteria_sect_code, project_type_code, 
  weighted_rank, display_order, 
  revision_count, create_user, create_date, update_user, update_date)
  VALUES (gen_random_uuid(), 
  'Prescription Complete - Next phase Rx Development', 
  to_date('01-01-2025', 'dd-mm-yyyy'), to_date('31-12-9999', 'dd-mm-yyyy'), 
  'MEDIUM_FLT', 'FUEL_MGMT', 
  9, 20, 
  0, 'DATA_LOAD', CURRENT_DATE, 'DATA_LOAD', CURRENT_DATE);

INSERT INTO wfprev.evaluation_criteria(
  evaluation_criteria_guid, 
  criteria_label, 
  system_start_timestamp, system_end_timestamp, 
  eval_criteria_sect_code, project_type_code, 
  weighted_rank, display_order, 
  revision_count, create_user, create_date, update_user, update_date)
  VALUES (gen_random_uuid(), 
  'Mitigates Risk around or adjacent to Critical Infrastructure including Watersheds', 
  to_date('01-01-2025', 'dd-mm-yyyy'), to_date('31-12-9999', 'dd-mm-yyyy'), 
  'MEDIUM_FLT', 'FUEL_MGMT', 
  8, 25, 
  0, 'DATA_LOAD', CURRENT_DATE, 'DATA_LOAD', CURRENT_DATE);

INSERT INTO wfprev.evaluation_criteria(
  evaluation_criteria_guid, 
  criteria_label, 
  system_start_timestamp, system_end_timestamp, 
  eval_criteria_sect_code, project_type_code, 
  weighted_rank, display_order, 
  revision_count, create_user, create_date, update_user, update_date)
  VALUES (gen_random_uuid(), 
  'Improve Egress/Evac Routes', 
  to_date('01-01-2025', 'dd-mm-yyyy'), to_date('31-12-9999', 'dd-mm-yyyy'), 
  'MEDIUM_FLT', 'FUEL_MGMT', 
  7, 30, 
  0, 'DATA_LOAD', CURRENT_DATE, 'DATA_LOAD', CURRENT_DATE);

INSERT INTO wfprev.evaluation_criteria(
  evaluation_criteria_guid, 
  criteria_label, 
  system_start_timestamp, system_end_timestamp, 
  eval_criteria_sect_code, project_type_code, 
  weighted_rank, display_order, 
  revision_count, create_user, create_date, update_user, update_date)
  VALUES (gen_random_uuid(), 
  'Maximize Funding Linkages', 
  to_date('01-01-2025', 'dd-mm-yyyy'), to_date('31-12-9999', 'dd-mm-yyyy'), 
  'MEDIUM_FLT', 'FUEL_MGMT', 
  6, 35, 
  0, 'DATA_LOAD', CURRENT_DATE, 'DATA_LOAD', CURRENT_DATE);

INSERT INTO wfprev.evaluation_criteria(
  evaluation_criteria_guid, 
  criteria_label, 
  system_start_timestamp, system_end_timestamp, 
  eval_criteria_sect_code, project_type_code, 
  weighted_rank, display_order, 
  revision_count, create_user, create_date, update_user, update_date)
  VALUES (gen_random_uuid(), 
  'Supports Reconcilliation with Indigenous peoples', 
  to_date('01-01-2025', 'dd-mm-yyyy'), to_date('31-12-9999', 'dd-mm-yyyy'), 
  'MEDIUM_FLT', 'FUEL_MGMT', 
  5, 40, 
  0, 'DATA_LOAD', CURRENT_DATE, 'DATA_LOAD', CURRENT_DATE);


INSERT INTO wfprev.evaluation_criteria(
  evaluation_criteria_guid, 
  criteria_label, 
  system_start_timestamp, system_end_timestamp, 
  eval_criteria_sect_code, project_type_code, 
  weighted_rank, display_order, 
  revision_count, create_user, create_date, update_user, update_date)
  VALUES (gen_random_uuid(), 
  'PSTA Class Greater than 7', 
  to_date('01-01-2025', 'dd-mm-yyyy'), to_date('31-12-9999', 'dd-mm-yyyy'), 
  'FINE_FLT', 'FUEL_MGMT', 
  0.39, 5, 
  0, 'DATA_LOAD', CURRENT_DATE, 'DATA_LOAD', CURRENT_DATE);

  INSERT INTO wfprev.evaluation_criteria(
  evaluation_criteria_guid, 
  criteria_label, 
  system_start_timestamp, system_end_timestamp, 
  eval_criteria_sect_code, project_type_code, 
  weighted_rank, display_order, 
  revision_count, create_user, create_date, update_user, update_date)
  VALUES (gen_random_uuid(), 
  'Part of Larger Risk Reduction Strategy', 
  to_date('01-01-2025', 'dd-mm-yyyy'), to_date('31-12-9999', 'dd-mm-yyyy'), 
  'FINE_FLT', 'FUEL_MGMT', 
  0.3, 10, 
  0, 'DATA_LOAD', CURRENT_DATE, 'DATA_LOAD', CURRENT_DATE);

  INSERT INTO wfprev.evaluation_criteria(
  evaluation_criteria_guid, 
  criteria_label, 
  system_start_timestamp, system_end_timestamp, 
  eval_criteria_sect_code, project_type_code, 
  weighted_rank, display_order, 
  revision_count, create_user, create_date, update_user, update_date)
  VALUES (gen_random_uuid(), 
  'Cost Effectiveness', 
  to_date('01-01-2025', 'dd-mm-yyyy'), to_date('31-12-9999', 'dd-mm-yyyy'), 
  'FINE_FLT', 'FUEL_MGMT', 
  0.2, 15, 
  0, 'DATA_LOAD', CURRENT_DATE, 'DATA_LOAD', CURRENT_DATE);

  INSERT INTO wfprev.evaluation_criteria(
  evaluation_criteria_guid, 
  criteria_label, 
  system_start_timestamp, system_end_timestamp, 
  eval_criteria_sect_code, project_type_code, 
  weighted_rank, display_order, 
  revision_count, create_user, create_date, update_user, update_date)
  VALUES (gen_random_uuid(), 
  'WUI WRR Plans with AOI that expands outside of the WUI', 
  to_date('01-01-2025', 'dd-mm-yyyy'), to_date('31-12-9999', 'dd-mm-yyyy'), 
  'FINE_FLT', 'FUEL_MGMT', 
  0.1, 20, 
  0, 'DATA_LOAD', CURRENT_DATE, 'DATA_LOAD', CURRENT_DATE);





  INSERT INTO wfprev.evaluation_criteria(
  evaluation_criteria_guid, 
  criteria_label, 
  system_start_timestamp, system_end_timestamp, 
  eval_criteria_sect_code, project_type_code, 
  weighted_rank, display_order, 
  revision_count, create_user, create_date, update_user, update_date)
  VALUES (gen_random_uuid(), 
  'Part of critical corridor that supports or connectcs areas to enhance landscape resiliency and risk', 
  to_date('01-01-2025', 'dd-mm-yyyy'), to_date('31-12-9999', 'dd-mm-yyyy'), 
  'RCL', 'CULT_RX_FR', 
  4, 5, 
  0, 'DATA_LOAD', CURRENT_DATE, 'DATA_LOAD', CURRENT_DATE);

  INSERT INTO wfprev.evaluation_criteria(
  evaluation_criteria_guid, 
  criteria_label, 
  system_start_timestamp, system_end_timestamp, 
  eval_criteria_sect_code, project_type_code, 
  weighted_rank, display_order, 
  revision_count, create_user, create_date, update_user, update_date)
  VALUES (gen_random_uuid(), 
  'Strategic location to support future fire suppression such as anchor points or slow fire spread', 
  to_date('01-01-2025', 'dd-mm-yyyy'), to_date('31-12-9999', 'dd-mm-yyyy'), 
  'RCL', 'CULT_RX_FR', 
  4, 10, 
  0, 'DATA_LOAD', CURRENT_DATE, 'DATA_LOAD', CURRENT_DATE);

  INSERT INTO wfprev.evaluation_criteria(
  evaluation_criteria_guid, 
  criteria_label, 
  system_start_timestamp, system_end_timestamp, 
  eval_criteria_sect_code, project_type_code, 
  weighted_rank, display_order, 
  revision_count, create_user, create_date, update_user, update_date)
  VALUES (gen_random_uuid(), 
  'Clear defensible boundaries that support safe and effective burn operations', 
  to_date('01-01-2025', 'dd-mm-yyyy'), to_date('31-12-9999', 'dd-mm-yyyy'), 
  'BDF', 'CULT_RX_FR', 
  1.25, 5, 
  0, 'DATA_LOAD', CURRENT_DATE, 'DATA_LOAD', CURRENT_DATE);

  INSERT INTO wfprev.evaluation_criteria(
  evaluation_criteria_guid, 
  criteria_label, 
  system_start_timestamp, system_end_timestamp, 
  eval_criteria_sect_code, project_type_code, 
  weighted_rank, display_order, 
  revision_count, create_user, create_date, update_user, update_date)
  VALUES (gen_random_uuid(), 
  'Holding considerations reduce the need for extensive resources and lowers operational costs', 
  to_date('01-01-2025', 'dd-mm-yyyy'), to_date('31-12-9999', 'dd-mm-yyyy'), 
  'BDF', 'CULT_RX_FR', 
  1.25, 10, 
  0, 'DATA_LOAD', CURRENT_DATE, 'DATA_LOAD', CURRENT_DATE);

  INSERT INTO wfprev.evaluation_criteria(
  evaluation_criteria_guid, 
  criteria_label, 
  system_start_timestamp, system_end_timestamp, 
  eval_criteria_sect_code, project_type_code, 
  weighted_rank, display_order, 
  revision_count, create_user, create_date, update_user, update_date)
  VALUES (gen_random_uuid(), 
  'Protection of values is straightforward and has low or accepatable fire risk', 
  to_date('01-01-2025', 'dd-mm-yyyy'), to_date('31-12-9999', 'dd-mm-yyyy'), 
  'BDF', 'CULT_RX_FR', 
  1, 15, 
  0, 'DATA_LOAD', CURRENT_DATE, 'DATA_LOAD', CURRENT_DATE);

  INSERT INTO wfprev.evaluation_criteria(
  evaluation_criteria_guid, 
  criteria_label, 
  system_start_timestamp, system_end_timestamp, 
  eval_criteria_sect_code, project_type_code, 
  weighted_rank, display_order, 
  revision_count, create_user, create_date, update_user, update_date)
  VALUES (gen_random_uuid(), 
  'Burn design supports lower-cost follow-up burns for long-term maintenance', 
  to_date('01-01-2025', 'dd-mm-yyyy'), to_date('31-12-9999', 'dd-mm-yyyy'), 
  'BDF', 'CULT_RX_FR', 
  1, 25, 
  0, 'DATA_LOAD', CURRENT_DATE, 'DATA_LOAD', CURRENT_DATE);

  INSERT INTO wfprev.evaluation_criteria(
  evaluation_criteria_guid, 
  criteria_label, 
  system_start_timestamp, system_end_timestamp, 
  eval_criteria_sect_code, project_type_code, 
  weighted_rank, display_order, 
  revision_count, create_user, create_date, update_user, update_date)
  VALUES (gen_random_uuid(), 
  'Reliable access to the unit for personnel, equipment, and emergency response', 
  to_date('01-01-2025', 'dd-mm-yyyy'), to_date('31-12-9999', 'dd-mm-yyyy'), 
  'BDF', 'CULT_RX_FR', 
  0.5, 30, 
  0, 'DATA_LOAD', CURRENT_DATE, 'DATA_LOAD', CURRENT_DATE);

  INSERT INTO wfprev.evaluation_criteria(
  evaluation_criteria_guid, 
  criteria_label, 
  system_start_timestamp, system_end_timestamp, 
  eval_criteria_sect_code, project_type_code, 
  weighted_rank, display_order, 
  revision_count, create_user, create_date, update_user, update_date)
  VALUES (gen_random_uuid(), 
  'Coordination with local Indigenous or non-Indigenous Fire Service', 
  to_date('01-01-2025', 'dd-mm-yyyy'), to_date('31-12-9999', 'dd-mm-yyyy'), 
  'COLL_IMP', 'CULT_RX_FR', 
  1, 5, 
  0, 'DATA_LOAD', CURRENT_DATE, 'DATA_LOAD', CURRENT_DATE);

  INSERT INTO wfprev.evaluation_criteria(
  evaluation_criteria_guid, 
  criteria_label, 
  system_start_timestamp, system_end_timestamp, 
  eval_criteria_sect_code, project_type_code, 
  weighted_rank, display_order, 
  revision_count, create_user, create_date, update_user, update_date)
  VALUES (gen_random_uuid(), 
  'First Nations development or co-development', 
  to_date('01-01-2025', 'dd-mm-yyyy'), to_date('31-12-9999', 'dd-mm-yyyy'), 
  'COLL_IMP', 'CULT_RX_FR', 
  .75, 10, 
  0, 'DATA_LOAD', CURRENT_DATE, 'DATA_LOAD', CURRENT_DATE);

  INSERT INTO wfprev.evaluation_criteria(
  evaluation_criteria_guid, 
  criteria_label, 
  system_start_timestamp, system_end_timestamp, 
  eval_criteria_sect_code, project_type_code, 
  weighted_rank, display_order, 
  revision_count, create_user, create_date, update_user, update_date)
  VALUES (gen_random_uuid(), 
  'Active or ongoing community support', 
  to_date('01-01-2025', 'dd-mm-yyyy'), to_date('31-12-9999', 'dd-mm-yyyy'), 
  'COLL_IMP', 'CULT_RX_FR', 
  .5, 15, 
  0, 'DATA_LOAD', CURRENT_DATE, 'DATA_LOAD', CURRENT_DATE);

  INSERT INTO wfprev.evaluation_criteria(
  evaluation_criteria_guid, 
  criteria_label, 
  system_start_timestamp, system_end_timestamp, 
  eval_criteria_sect_code, project_type_code, 
  weighted_rank, display_order, 
  revision_count, create_user, create_date, update_user, update_date)
  VALUES (gen_random_uuid(), 
  'Provincial Strategic Threat Analysis (PSTA) is high or extreme so greater than 7', 
  to_date('01-01-2025', 'dd-mm-yyyy'), to_date('31-12-9999', 'dd-mm-yyyy'), 
  'COLL_IMP', 'CULT_RX_FR', 
  .5, 20, 
  0, 'DATA_LOAD', CURRENT_DATE, 'DATA_LOAD', CURRENT_DATE);

  INSERT INTO wfprev.evaluation_criteria(
  evaluation_criteria_guid, 
  criteria_label, 
  system_start_timestamp, system_end_timestamp, 
  eval_criteria_sect_code, project_type_code, 
  weighted_rank, display_order, 
  revision_count, create_user, create_date, update_user, update_date)
  VALUES (gen_random_uuid(), 
  'Connected to higher level planning (FLP, SLP, First Nations stewardship plans)', 
  to_date('01-01-2025', 'dd-mm-yyyy'), to_date('31-12-9999', 'dd-mm-yyyy'), 
  'COLL_IMP', 'CULT_RX_FR', 
  .25, 25, 
  0, 'DATA_LOAD', CURRENT_DATE, 'DATA_LOAD', CURRENT_DATE);












  