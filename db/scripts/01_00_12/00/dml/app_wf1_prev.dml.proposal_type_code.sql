
insert into wfprev.proposal_type_code (proposal_type_code, description, display_order, effective_date, expiry_date, revision_count, create_user, create_date, update_user, update_date)
values ('NEW', 'New', 1, to_date('01-01-2020', 'dd-mm-yyyy'), to_date('31-12-9999', 'dd-mm-yyyy'), 0, 'DATA_LOAD', to_date('20-11-2024', 'dd-mm-yyyy'), 'DATA_LOAD', to_date('20-11-2024', 'dd-mm-yyyy'));
insert into wfprev.proposal_type_code (proposal_type_code, description, display_order, effective_date, expiry_date, revision_count, create_user, create_date, update_user, update_date)
values ('IN_PROG', 'In Progresss', 2, to_date('01-01-2020', 'dd-mm-yyyy'), to_date('31-12-9999', 'dd-mm-yyyy'), 0, 'DATA_LOAD', to_date('20-11-2024', 'dd-mm-yyyy'), 'DATA_LOAD', to_date('20-11-2024', 'dd-mm-yyyy'));
insert into wfprev.proposal_type_code (proposal_type_code, description, display_order, effective_date, expiry_date, revision_count, create_user, create_date, update_user, update_date)
values ('CARRYOVER', 'Carry Over', 3, to_date('01-01-2020', 'dd-mm-yyyy'), to_date('31-12-9999', 'dd-mm-yyyy'), 0, 'DATA_LOAD', to_date('20-11-2024', 'dd-mm-yyyy'), 'DATA_LOAD', to_date('20-11-2024', 'dd-mm-yyyy'));

