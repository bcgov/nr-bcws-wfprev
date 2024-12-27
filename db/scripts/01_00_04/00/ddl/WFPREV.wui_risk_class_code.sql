/* ---------------------------------------------------- */
/*  Generated by Enterprise Architect Version 12.0 		*/
/*  Created On : 10-Dec-2024 4:41:38 PM 				*/
/*  DBMS       : PostgreSQL 						*/
/* ---------------------------------------------------- */

/* Create Tables */

CREATE TABLE "wfprev"."wui_risk_class_code"
(
	"wui_risk_class_code" varchar(10)	 NOT NULL,    -- wui_risk_class_code: Wildland Urban Interface Risk Class Code are the set of code used to assess the risk of wildfires interfacing with urban environments.  Values are:   	- WUI RC 1 	- WUI RC 2 	- WUI RC 3 	- WUI RC 4 	- WUI RC 5
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

COMMENT ON TABLE "wfprev"."wui_risk_class_code"
	IS 'Wildland Urban Interface Risk Class Code are the set of code used to assess the risk of wildfires interfacing with urban environments.  Values are:   	- WUI RC 1 	- WUI RC 2 	- WUI RC 3 	- WUI RC 4 	- WUI RC 5'
;

COMMENT ON COLUMN "wfprev"."wui_risk_class_code"."wui_risk_class_code"
	IS 'wui_risk_class_code: Wildland Urban Interface Risk Class Code are the set of code used to assess the risk of wildfires interfacing with urban environments.  Values are:   	- WUI RC 1 	- WUI RC 2 	- WUI RC 3 	- WUI RC 4 	- WUI RC 5'
;

COMMENT ON COLUMN "wfprev"."wui_risk_class_code"."description"
	IS 'DESCRIPTION is the display quality description of the code value.'
;

COMMENT ON COLUMN "wfprev"."wui_risk_class_code"."display_order"
	IS 'DISPLAY ORDER is to allow non alphabetic sorting e.g. M T W Th F S S.'
;

COMMENT ON COLUMN "wfprev"."wui_risk_class_code"."effective_date"
	IS 'EFFECTIVE_DATE is the date code value becomes effective.'
;

COMMENT ON COLUMN "wfprev"."wui_risk_class_code"."expiry_date"
	IS 'EXPIRY_DATE is the date code value expires.'
;

COMMENT ON COLUMN "wfprev"."wui_risk_class_code"."revision_count"
	IS 'REVISION_COUNT is the number of times that the row of data has been changed. The column is used for optimistic locking via application code.'
;

COMMENT ON COLUMN "wfprev"."wui_risk_class_code"."create_user"
	IS 'CREATE_USER is an audit column that indicates the user that created the record.'
;

COMMENT ON COLUMN "wfprev"."wui_risk_class_code"."create_date"
	IS 'CREATE_DATE is the date and time the row of data was created.'
;

COMMENT ON COLUMN "wfprev"."wui_risk_class_code"."update_user"
	IS 'UPDATE_USER is an audit column that indicates the user that updated the record.'
;

COMMENT ON COLUMN "wfprev"."wui_risk_class_code"."update_date"
	IS 'UPDATE_DATE is the date and time the row of data was updated.'
;

/* Create Primary Keys, Indexes, Uniques, Checks */

ALTER TABLE "wfprev"."wui_risk_class_code" ADD CONSTRAINT "wuirccd_pk"
	PRIMARY KEY ("wui_risk_class_code")
;