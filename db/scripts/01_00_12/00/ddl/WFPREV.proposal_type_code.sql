/* ---------------------------------------------------- */
/*  Generated by Enterprise Architect Version 12.0 		*/
/*  Created On : 30-Jan-2025 12:03:53 PM 				*/
/*  DBMS       : PostgreSQL 						*/
/* ---------------------------------------------------- */

/* Create Tables */

CREATE TABLE "wfprev"."proposal_type_code"
(
	"proposal_type_code" varchar(10)	 NOT NULL,    -- proposal_type_code: Proposal Type Code is used to define what type of proposal is being made for a project in a fiscal year.  Values are:  	- New 	- In Progress 	- Carry Over
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

COMMENT ON TABLE "wfprev"."proposal_type_code"
	IS 'Proposal Type Code is used to define what type of proposal is being made for a project in a fiscal year.  Values are:  	- New 	- In Progress 	- Carry Over'
;

COMMENT ON COLUMN "wfprev"."proposal_type_code"."proposal_type_code"
	IS 'proposal_type_code: Proposal Type Code is used to define what type of proposal is being made for a project in a fiscal year.  Values are:  	- New 	- In Progress 	- Carry Over'
;

COMMENT ON COLUMN "wfprev"."proposal_type_code"."description"
	IS 'DESCRIPTION is the display quality description of the code value.'
;

COMMENT ON COLUMN "wfprev"."proposal_type_code"."display_order"
	IS 'DISPLAY ORDER is to allow non alphabetic sorting e.g. M T W Th F S S.'
;

COMMENT ON COLUMN "wfprev"."proposal_type_code"."effective_date"
	IS 'EFFECTIVE_DATE is the date code value becomes effective.'
;

COMMENT ON COLUMN "wfprev"."proposal_type_code"."expiry_date"
	IS 'EXPIRY_DATE is the date code value expires.'
;

COMMENT ON COLUMN "wfprev"."proposal_type_code"."revision_count"
	IS 'REVISION_COUNT is the number of times that the row of data has been changed. The column is used for optimistic locking via application code.'
;

COMMENT ON COLUMN "wfprev"."proposal_type_code"."create_user"
	IS 'CREATE_USER is an audit column that indicates the user that created the record.'
;

COMMENT ON COLUMN "wfprev"."proposal_type_code"."create_date"
	IS 'CREATE_DATE is the date and time the row of data was created.'
;

COMMENT ON COLUMN "wfprev"."proposal_type_code"."update_user"
	IS 'UPDATE_USER is an audit column that indicates the user that updated the record.'
;

COMMENT ON COLUMN "wfprev"."proposal_type_code"."update_date"
	IS 'UPDATE_DATE is the date and time the row of data was updated.'
;

/* Create Primary Keys, Indexes, Uniques, Checks */

ALTER TABLE "wfprev"."proposal_type_code" ADD CONSTRAINT "prptcd_pk"
	PRIMARY KEY ("proposal_type_code")
;