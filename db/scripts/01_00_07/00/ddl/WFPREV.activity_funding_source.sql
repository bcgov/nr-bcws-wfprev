/* ---------------------------------------------------- */
/*  Generated by Enterprise Architect Version 12.0 		*/
/*  Created On : 07-Jan-2025 11:49:29 AM 				*/
/*  DBMS       : PostgreSQL 						*/
/* ---------------------------------------------------- */

/* Create Tables */

CREATE TABLE "wfprev"."activity_funding_source"
(
	"activity_funding_source_guid" UUID NOT NULL,    -- activity_funding_source_guid is a unique identifier for the record.
	"funding_source_abbreviation" varchar(20)	 NOT NULL,    -- Funding Source Abbreviation is the abbreviation used to identify a funding source.
	"system_start_timestamp" TIMESTAMP NOT NULL,    -- System Start Timestamp is the date and time with millisecond accuracy when the record becomes effective.   The System Start Timestamp and System End Timestamp fields are managed as a set by the system and is not exposed to the end user. When an update is made to the data in the record, the system will be responsible for expiring the old record and creating a new record with the updated data. 
	"system_end_timestamp" TIMESTAMP NOT NULL,    -- System End Timestamp is the date and time with millisecond accuracy when the record becomes effective. The current instance of the record will be set to Dec 31, 9999 by default.  The System Start Timestamp and System End Timestamp fields are managed as a set by the system and is not exposed to the end user. When an update is made to the data in the record, the system will be responsible for expiring the old record and creating a new record with the updated data. 
	"funding_source_name" varchar(50)	 NOT NULL,    -- Funding Source Name is the full display name of a funding source.
	"revision_count" decimal(10) NOT NULL DEFAULT 0,    -- REVISION_COUNT is the number of times that the row of data has been changed. The column is used for optimistic locking via application code.
	"create_user" varchar(64)	 NOT NULL,    -- CREATE_USER is an audit column that indicates the user that created the record.
	"create_date" DATE NOT NULL DEFAULT CURRENT_TIMESTAMP,    -- CREATE_DATE is the date and time the row of data was created.
	"update_user" varchar(64)	 NOT NULL,    -- UPDATE_USER is an audit column that indicates the user that updated the record.
	"update_date" DATE NOT NULL DEFAULT CURRENT_TIMESTAMP    -- UPDATE_DATE is the date and time the row of data was updated.
)
TABLESPACE	PG_DEFAULT
;

/* Create Table Comments, Sequences for Autonumber Columns */

COMMENT ON TABLE "wfprev"."activity_funding_source"
	IS 'Activity Funding Source is a list of agencies/organizations that are sources of activity funding. The funding source provides the budget for the activity.  Example Funding Sources:  	- WRR - Wildfire Risk Reduction 	- BCP - BC Parks 	- FEP - Forest Employment Program 	- FESBC - Forest Enhancement Society of BC 	- CFS - FireSmart Community Funding Supports'
;

COMMENT ON COLUMN "wfprev"."activity_funding_source"."activity_funding_source_guid"
	IS 'activity_funding_source_guid is a unique identifier for the record.'
;

COMMENT ON COLUMN "wfprev"."activity_funding_source"."funding_source_abbreviation"
	IS 'Funding Source Abbreviation is the abbreviation used to identify a funding source.'
;

COMMENT ON COLUMN "wfprev"."activity_funding_source"."system_start_timestamp"
	IS 'System Start Timestamp is the date and time with millisecond accuracy when the record becomes effective.   The System Start Timestamp and System End Timestamp fields are managed as a set by the system and is not exposed to the end user. When an update is made to the data in the record, the system will be responsible for expiring the old record and creating a new record with the updated data. '
;

COMMENT ON COLUMN "wfprev"."activity_funding_source"."system_end_timestamp"
	IS 'System End Timestamp is the date and time with millisecond accuracy when the record becomes effective. The current instance of the record will be set to Dec 31, 9999 by default.  The System Start Timestamp and System End Timestamp fields are managed as a set by the system and is not exposed to the end user. When an update is made to the data in the record, the system will be responsible for expiring the old record and creating a new record with the updated data. '
;

COMMENT ON COLUMN "wfprev"."activity_funding_source"."funding_source_name"
	IS 'Funding Source Name is the full display name of a funding source.'
;

COMMENT ON COLUMN "wfprev"."activity_funding_source"."revision_count"
	IS 'REVISION_COUNT is the number of times that the row of data has been changed. The column is used for optimistic locking via application code.'
;

COMMENT ON COLUMN "wfprev"."activity_funding_source"."create_user"
	IS 'CREATE_USER is an audit column that indicates the user that created the record.'
;

COMMENT ON COLUMN "wfprev"."activity_funding_source"."create_date"
	IS 'CREATE_DATE is the date and time the row of data was created.'
;

COMMENT ON COLUMN "wfprev"."activity_funding_source"."update_user"
	IS 'UPDATE_USER is an audit column that indicates the user that updated the record.'
;

COMMENT ON COLUMN "wfprev"."activity_funding_source"."update_date"
	IS 'UPDATE_DATE is the date and time the row of data was updated.'
;

/* Create Primary Keys, Indexes, Uniques, Checks */

ALTER TABLE "wfprev"."activity_funding_source" ADD CONSTRAINT "actfsrc_pk"
	PRIMARY KEY ("activity_funding_source_guid")
;

ALTER TABLE "wfprev"."activity_funding_source" ADD CONSTRAINT "actfsrc_uk" UNIQUE ("funding_source_abbreviation","system_start_timestamp","system_end_timestamp")
;