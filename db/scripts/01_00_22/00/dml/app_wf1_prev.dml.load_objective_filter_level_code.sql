-- Loading OBJECTIVE_FILTER_LEVEL_CODE...
insert into wfprev.objective_filter_level_code (objective_filter_level_code, description, display_order, effective_date, expiry_date, revision_count, create_user, create_date, update_user, update_date)
values ('COARSE_FLT', 'Coarse Filter', 1, to_date('01-01-2020', 'dd-mm-yyyy'), to_date('31-12-9999', 'dd-mm-yyyy'), 0, 'DATA_LOAD', to_date('20-11-2024', 'dd-mm-yyyy'), 'DATA_LOAD', to_date('20-11-2024', 'dd-mm-yyyy'));
insert into wfprev.objective_filter_level_code (objective_filter_level_code, description, display_order, effective_date, expiry_date, revision_count, create_user, create_date, update_user, update_date)
values ('MEDIUM_FLT', 'Medium Filter', 2, to_date('01-01-2020', 'dd-mm-yyyy'), to_date('31-12-9999', 'dd-mm-yyyy'), 0, 'DATA_LOAD', to_date('20-11-2024', 'dd-mm-yyyy'), 'DATA_LOAD', to_date('20-11-2024', 'dd-mm-yyyy'));
insert into wfprev.objective_filter_level_code (objective_filter_level_code, description, display_order, effective_date, expiry_date, revision_count, create_user, create_date, update_user, update_date)
values ('FINE_FLT', 'Fine Filter', 3, to_date('01-01-2020', 'dd-mm-yyyy'), to_date('31-12-9999', 'dd-mm-yyyy'), 0, 'DATA_LOAD', to_date('20-11-2024', 'dd-mm-yyyy'), 'DATA_LOAD', to_date('20-11-2024', 'dd-mm-yyyy'));

