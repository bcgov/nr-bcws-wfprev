

insert into wfprev.progress_status_code (progress_status_code, description, display_order, effective_date, expiry_date, revision_count, create_user, create_date, update_user, update_date)
values ('ON_TRACK', 'On Track', 1, to_date('01-01-2000', 'dd-mm-yyyy'), to_date('31-12-9999', 'dd-mm-yyyy'), 0, 'DATA_LOAD', to_date('13-01-2025', 'dd-mm-yyyy'), 'DATA_LOAD', to_date('13-01-2025', 'dd-mm-yyyy'));
insert into wfprev.progress_status_code (progress_status_code, description, display_order, effective_date, expiry_date, revision_count, create_user, create_date, update_user, update_date)
values ('DEFERRED', 'Deferred', 2, to_date('01-01-2000', 'dd-mm-yyyy'), to_date('31-12-9999', 'dd-mm-yyyy'), 0, 'DATA_LOAD', to_date('13-01-2025', 'dd-mm-yyyy'), 'DATA_LOAD', to_date('13-01-2025', 'dd-mm-yyyy'));
insert into wfprev.progress_status_code (progress_status_code, description, display_order, effective_date, expiry_date, revision_count, create_user, create_date, update_user, update_date)
values ('DELAYED', 'Delayed', 3, to_date('01-01-2000', 'dd-mm-yyyy'), to_date('31-12-9999', 'dd-mm-yyyy'), 0, 'DATA_LOAD', to_date('13-01-2025', 'dd-mm-yyyy'), 'DATA_LOAD', to_date('13-01-2025', 'dd-mm-yyyy'));
insert into wfprev.progress_status_code (progress_status_code, description, display_order, effective_date, expiry_date, revision_count, create_user, create_date, update_user, update_date)
values ('CANCELLED', 'Cancelled', 4, to_date('01-01-2000', 'dd-mm-yyyy'), to_date('31-12-9999', 'dd-mm-yyyy'), 0, 'DATA_LOAD', to_date('13-01-2025', 'dd-mm-yyyy'), 'DATA_LOAD', to_date('13-01-2025', 'dd-mm-yyyy'));

