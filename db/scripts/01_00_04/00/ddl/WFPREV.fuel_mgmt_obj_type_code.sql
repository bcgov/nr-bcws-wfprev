/* ---------------------------------------------------- */
/*  Generated by Enterprise Architect Version 12.0 		*/
/*  Created On : 17-Dec-2024 11:28:27 AM 				*/
/*  DBMS       : PostgreSQL 						*/
/* ---------------------------------------------------- */

/* Create Tables */

CREATE TABLE "wfprev"."fuel_mgmt_obj_type_code"
(
	"fuel_mgmt_obj_type_code" varchar(10)	 NOT NULL,    -- fuel_mgmt_obj_type_code: Fuel Management Objective Type Code defines the types of objectives that are available for a Fuel Management Request.  Values are:  	- Government Objective 	- Project Level Objective
	"description" varchar(200)	 NOT NULL,    -- DESCRIPTION is the display quality description of the code value.
	"display_order" decimal(3) NULL,    -- DISPLAY ORDER is to allow non alphabetic sorting e.g. M T W Th F S S.
	"effective_date" DATE NOT NULL DEFAULT CURRENT_DATE,    -- EFFECTIVE_DATE is the date code value becomes effective.
	"expiry_date" DATE NOT NULL DEFAULT '9999-12-31',    -- EXPIRY_DATE is the date code value expires.
	"revision_count" decimal(10) NOT NULL DEFAULT 0,    -- REVISION_COUNT is the number of times that the row of data has been changed. The column is used for optimistic locking via application code.
	"create_user" varchar(64)	 NOT NULL,    -- CREATE_USER is an audit column that indicates the user that created the record.
	"create_date" DATE NOT NULL DEFAULT CURRENT_TIMESTAMP,    -- CREATE_DATE is the date and time the row of data was created.
	"update_user" varchar(64)	 NOT NULL,    -- UPDATE_USER is an audit column that indicates the user that updated the record.
	"update_date" DATE NOT NULL DEFAULT CURRENT_TIMESTAMP    -- UPDATE_DATE is the date and time the row of data was updated.
)
TABLESPACE	PG_DEFAULT
;

/* Create Table Comments, Sequences for Autonumber Columns */

COMMENT ON TABLE "wfprev"."fuel_mgmt_obj_type_code"
	IS 'Fuel Management Objective Type Code defines the types of objectives that are available for a Fuel Management Request.  Values are:  	- Government Objective 	- Project Level Objective'
;

COMMENT ON COLUMN "wfprev"."fuel_mgmt_obj_type_code"."fuel_mgmt_obj_type_code"
	IS 'fuel_mgmt_obj_type_code: Fuel Management Objective Type Code defines the types of objectives that are available for a Fuel Management Request.  Values are:  	- Government Objective 	- Project Level Objective'
;

COMMENT ON COLUMN "wfprev"."fuel_mgmt_obj_type_code"."description"
	IS 'DESCRIPTION is the display quality description of the code value.'
;

COMMENT ON COLUMN "wfprev"."fuel_mgmt_obj_type_code"."display_order"
	IS 'DISPLAY ORDER is to allow non alphabetic sorting e.g. M T W Th F S S.'
;

COMMENT ON COLUMN "wfprev"."fuel_mgmt_obj_type_code"."effective_date"
	IS 'EFFECTIVE_DATE is the date code value becomes effective.'
;

COMMENT ON COLUMN "wfprev"."fuel_mgmt_obj_type_code"."expiry_date"
	IS 'EXPIRY_DATE is the date code value expires.'
;

COMMENT ON COLUMN "wfprev"."fuel_mgmt_obj_type_code"."revision_count"
	IS 'REVISION_COUNT is the number of times that the row of data has been changed. The column is used for optimistic locking via application code.'
;

COMMENT ON COLUMN "wfprev"."fuel_mgmt_obj_type_code"."create_user"
	IS 'CREATE_USER is an audit column that indicates the user that created the record.'
;

COMMENT ON COLUMN "wfprev"."fuel_mgmt_obj_type_code"."create_date"
	IS 'CREATE_DATE is the date and time the row of data was created.'
;

COMMENT ON COLUMN "wfprev"."fuel_mgmt_obj_type_code"."update_user"
	IS 'UPDATE_USER is an audit column that indicates the user that updated the record.'
;

COMMENT ON COLUMN "wfprev"."fuel_mgmt_obj_type_code"."update_date"
	IS 'UPDATE_DATE is the date and time the row of data was updated.'
;

/* Create Primary Keys, Indexes, Uniques, Checks */

ALTER TABLE "wfprev"."fuel_mgmt_obj_type_code" ADD CONSTRAINT "fmotc_pk"
	PRIMARY KEY ("fuel_mgmt_obj_type_code")
;