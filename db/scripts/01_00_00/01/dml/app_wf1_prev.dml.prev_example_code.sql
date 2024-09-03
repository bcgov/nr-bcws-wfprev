insert into "wfprev"."example_code" (example_code, description, display_order, effective_date, expiry_date, revision_count, create_user, create_date, update_user, update_date)
values ('DRAFT', 'Draft', 1, to_date('01-01-1950', 'dd-mm-yyyy'), to_date('31-12-9999', 'dd-mm-yyyy'), 0, 'ETL_DATA_LOAD', to_date('01-06-2018', 'dd-mm-yyyy'), 'ETL_DATA_LOAD', to_date('01-06-2018', 'dd-mm-yyyy'));

insert into "wfprev"."example_code" (example_code, description, display_order, effective_date, expiry_date, revision_count, create_user, create_date, update_user, update_date)
values ('PUBLISHED', 'Published', 2, to_date('01-01-1950', 'dd-mm-yyyy'), to_date('31-12-9999', 'dd-mm-yyyy'), 0, 'ETL_DATA_LOAD', to_date('01-06-2018', 'dd-mm-yyyy'), 'ETL_DATA_LOAD', to_date('01-06-2018', 'dd-mm-yyyy'));

insert into "wfprev"."example_code" (example_code, description, display_order, effective_date, expiry_date, revision_count, create_user, create_date, update_user, update_date)
values ('RESCINDED', 'Rescinded', 3, to_date('01-01-1950', 'dd-mm-yyyy'), to_date('31-12-9999', 'dd-mm-yyyy'), 0, 'ETL_DATA_LOAD', to_date('01-06-2018', 'dd-mm-yyyy'), 'ETL_DATA_LOAD', to_date('01-06-2018', 'dd-mm-yyyy'));

insert into "wfprev"."example_code" (example_code, description, display_order, effective_date, expiry_date, revision_count, create_user, create_date, update_user, update_date)
values ('ARCHIVED', 'Archived', 4, to_date('01-01-1950', 'dd-mm-yyyy'), to_date('31-12-9999', 'dd-mm-yyyy'), 0, 'ETL_DATA_LOAD', to_date('01-06-2018', 'dd-mm-yyyy'), 'ETL_DATA_LOAD', to_date('01-06-2018', 'dd-mm-yyyy'));
