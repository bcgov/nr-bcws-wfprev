
insert into wfprev.activity_category_code (activity_category_code, description, display_order, effective_date, expiry_date, revision_count, create_user, create_date, update_user, update_date)
values ('WUIWRRPLAN', 'WUI WRR Plan', 1, to_date('01-01-2000', 'dd-mm-yyyy'), to_date('31-12-9999', 'dd-mm-yyyy'), 0, 'DATA_LOAD', to_date('07-01-2025', 'dd-mm-yyyy'), 'DATA_LOAD', to_date('07-01-2025', 'dd-mm-yyyy'));

insert into wfprev.activity_category_code (activity_category_code, description, display_order, effective_date, expiry_date, revision_count, create_user, create_date, update_user, update_date)
values ('OPER_TREAT', 'Operational Treatment ', 3, to_date('01-01-2000', 'dd-mm-yyyy'), to_date('31-12-9999', 'dd-mm-yyyy'), 0, 'DATA_LOAD', to_date('07-01-2025', 'dd-mm-yyyy'), 'DATA_LOAD', to_date('07-01-2025', 'dd-mm-yyyy'));

DELETE FROM wfprev.activity_category_code acc
WHERE acc.activity_category_code = 'TACT_PLAN';

UPDATE wfprev.activity_category_code
SET display_order = display_order + 1
WHERE activity_category_code IN ('MNT_SURVEY','MNT_OP_TRT','OTH_ASSSRV','OTH_ADMIN');


insert into wfprev.activity_category_code (activity_category_code, description, display_order, effective_date, expiry_date, revision_count, create_user, create_date, update_user, update_date)
values ('PLN_DEV', 'CRx: Planning and Development', 20, to_date('01-01-2000', 'dd-mm-yyyy'), to_date('31-12-9999', 'dd-mm-yyyy'), 0, 'DATA_LOAD', to_date('07-01-2025', 'dd-mm-yyyy'), 'DATA_LOAD', to_date('07-01-2025', 'dd-mm-yyyy'));

insert into wfprev.activity_category_code (activity_category_code, description, display_order, effective_date, expiry_date, revision_count, create_user, create_date, update_user, update_date)
values ('COORD', 'CRx: Coordination', 21, to_date('01-01-2000', 'dd-mm-yyyy'), to_date('31-12-9999', 'dd-mm-yyyy'), 0, 'DATA_LOAD', to_date('07-01-2025', 'dd-mm-yyyy'), 'DATA_LOAD', to_date('07-01-2025', 'dd-mm-yyyy'));

insert into wfprev.activity_category_code (activity_category_code, description, display_order, effective_date, expiry_date, revision_count, create_user, create_date, update_user, update_date)
values ('IMPL_DELIV', 'CRx: Implementation and Delivery', 22, to_date('01-01-2000', 'dd-mm-yyyy'), to_date('31-12-9999', 'dd-mm-yyyy'), 0, 'DATA_LOAD', to_date('07-01-2025', 'dd-mm-yyyy'), 'DATA_LOAD', to_date('07-01-2025', 'dd-mm-yyyy'));

insert into wfprev.activity_category_code (activity_category_code, description, display_order, effective_date, expiry_date, revision_count, create_user, create_date, update_user, update_date)
values ('MON_MAINT', 'CRx: Monitoring and Maintenance', 23, to_date('01-01-2000', 'dd-mm-yyyy'), to_date('31-12-9999', 'dd-mm-yyyy'), 0, 'DATA_LOAD', to_date('07-01-2025', 'dd-mm-yyyy'), 'DATA_LOAD', to_date('07-01-2025', 'dd-mm-yyyy'));

