
delete from wfprev.reporting_period_code;

insert into wfprev.reporting_period_code (reporting_period_code, description, display_order, effective_date, expiry_date, revision_count, create_user, create_date, update_user, update_date)
values ('CUSTOM', 'Other', 99, to_date('01-01-2000', 'dd-mm-yyyy'), to_date('31-12-9999', 'dd-mm-yyyy'), 0, 'DATA_LOAD', to_date('13-01-2025', 'dd-mm-yyyy'), 'DATA_LOAD', to_date('13-01-2025', 'dd-mm-yyyy'));
insert into wfprev.reporting_period_code (reporting_period_code, description, display_order, effective_date, expiry_date, revision_count, create_user, create_date, update_user, update_date)
values ('Q1', 'End of First Quarter', 3, to_date('01-01-2000', 'dd-mm-yyyy'), to_date('31-12-9999', 'dd-mm-yyyy'), 0, 'DATA_LOAD', to_date('13-01-2025', 'dd-mm-yyyy'), 'DATA_LOAD', to_date('13-01-2025', 'dd-mm-yyyy'));
insert into wfprev.reporting_period_code (reporting_period_code, description, display_order, effective_date, expiry_date, revision_count, create_user, create_date, update_user, update_date)
values ('Q2', 'End of Second Quarter', 4, to_date('01-01-2000', 'dd-mm-yyyy'), to_date('31-12-9999', 'dd-mm-yyyy'), 0, 'DATA_LOAD', to_date('13-01-2025', 'dd-mm-yyyy'), 'DATA_LOAD', to_date('13-01-2025', 'dd-mm-yyyy'));
insert into wfprev.reporting_period_code (reporting_period_code, description, display_order, effective_date, expiry_date, revision_count, create_user, create_date, update_user, update_date)
values ('Q3', 'End of Third Quarter', 5, to_date('01-01-2000', 'dd-mm-yyyy'), to_date('31-12-9999', 'dd-mm-yyyy'), 0, 'DATA_LOAD', to_date('13-01-2025', 'dd-mm-yyyy'), 'DATA_LOAD', to_date('13-01-2025', 'dd-mm-yyyy'));
insert into wfprev.reporting_period_code (reporting_period_code, description, display_order, effective_date, expiry_date, revision_count, create_user, create_date, update_user, update_date)
values ('MARCH7', 'March 7', 6, to_date('01-01-2000', 'dd-mm-yyyy'), to_date('31-12-9999', 'dd-mm-yyyy'), 0, 'DATA_LOAD', to_date('13-01-2025', 'dd-mm-yyyy'), 'DATA_LOAD', to_date('13-01-2025', 'dd-mm-yyyy'));

