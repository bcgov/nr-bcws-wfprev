update wfprev.activity_status_code set expiry_date = current_date, update_date = current_date, revision_count = revision_count + 1, update_user = 'WFPREV-1031' where activity_status_code in ('DELETED', 'ARCHIVED');

update wfprev.activity_status_code set description = 'In Progress', display_order = 1, revision_count = revision_count + 1, update_user = 'WFPREV-1031', update_date = current_date where activity_status_code = 'ACTIVE';
update wfprev.activity_status_code set display_order = 3, revision_count = revision_count + 1, update_user = 'WFPREV-1031', update_date = current_date where activity_status_code = 'COMPLETED';

insert into wfprev.activity_status_code (activity_status_code, description, display_order, effective_date, expiry_date, revision_count, create_user, create_date, update_user, update_date)
values ('SUBS_COMPL', 'Substantially Complete', 2, to_date('01-01-2000', 'dd-mm-yyyy'), to_date('31-12-9999', 'dd-mm-yyyy'), 0, 'WFPREV-1031', current_date, 'WFPREV-1031', current_date);
insert into wfprev.activity_status_code (activity_status_code, description, display_order, effective_date, expiry_date, revision_count, create_user, create_date, update_user, update_date)
values ('DEFERRED', 'Deferred', 4, to_date('01-01-2000', 'dd-mm-yyyy'), to_date('31-12-9999', 'dd-mm-yyyy'), 0, 'WFPREV-1031', current_date, 'WFPREV-1031', current_date);
insert into wfprev.activity_status_code (activity_status_code, description, display_order, effective_date, expiry_date, revision_count, create_user, create_date, update_user, update_date)
values ('CANCELLED', 'Cancelled', 5, to_date('01-01-2000', 'dd-mm-yyyy'), to_date('31-12-9999', 'dd-mm-yyyy'), 0, 'WFPREV-1031', current_date, 'WFPREV-1031', current_date);

