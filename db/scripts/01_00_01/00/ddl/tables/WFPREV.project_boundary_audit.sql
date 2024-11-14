/* ---------------------------------------------------- */
/*  Generated by Enterprise Architect Version 12.0 		*/
/*  Created On : 14-Nov-2024 10:36:10 AM 				*/
/*  DBMS       : PostgreSQL 						*/
/* ---------------------------------------------------- */

/* Create Tables */

CREATE TABLE "wfprev"."project_boundary_audit"
(
	"audit_table_sequence" decimal(15) NOT NULL,    -- Added by transform - intent is make this sequence noncached so order is maintained
	"audit_action_code" varchar(10)	 NOT NULL,    -- Contains the reason the audit record is written - Insert/Update/Delete/Conversion
	"project_boundary_guid"  UUID NULL,    -- project_boundary_guid is a unique identifier for the record.
	"project_guid" UUID NOT NULL,    -- project_guid: Is a foreign key to project: Project is used to track Prevention Projects which are created to reduce risks due to forest fires. 
	"system_start_timestamp" TIMESTAMP NOT NULL,    -- System Start Timestamp is the date and time with millisecond accuracy when the record becomes effective.   The System Start Timestamp and System End Timestamp fields are managed as a set by the system and is not exposed to the end user. When an update is made to the data in the record, the system will be responsible for expiring the old record and creating a new record with the updated data. 
	"system_end_timestamp" TIMESTAMP NOT NULL,    -- System End Timestamp is the date and time with millisecond accuracy when the record becomes effective. The current instance of the record will be set to Dec 31, 9999 by default.  The System Start Timestamp and System End Timestamp fields are managed as a set by the system and is not exposed to the end user. When an update is made to the data in the record, the system will be responsible for expiring the old record and creating a new record with the updated data. 
	"mapping_label" varchar(250)	 NULL,    -- Mapping Label is a label assigned to the polygon for downstream mapping purposes.
	"collection_date" DATE NOT NULL,    -- Collection Date is the date that the polygon was originally captured/created.
	"collection_method" varchar(4000)	 NULL,    -- Collection Method describes the method used to collect the Geometry data.
	"collector_name" varchar(100)	 NULL,    -- Collector Name is the name of the person who established  the project boundary polygon. 
	"boundary_size_ha" decimal(19,4) NOT NULL,    -- Incident Size Ha is the size of the project measured in hectares, to three decimals (e.g., 0.001).
	"boundary_comment" varchar(2000)	 NULL,    -- Boundary Comment provides additional information related to the collection of the polygon data.
	"location_geometry" POLYGON NOT NULL,    -- Location Geometry is a point location using lat long, which is the centroid off the boundary geometry. The location geometry is used to place a pin on a map for the project location. 
	"boundary_geometry" POLYGON NOT NULL,    -- Boundary Geometry is the geographic area that is the boundary of the project.
	"revision_count" decimal(10) NOT NULL DEFAULT 0,    -- REVISION_COUNT is the number of times that the row of data has been changed. The column is used for optimistic locking via application code.
	"create_user" varchar(64)	 NOT NULL,    -- CREATE_USER is an audit column that indicates the user that created the record.
	"create_date" DATE NOT NULL DEFAULT CURRENT_TIMESTAMP,    -- CREATE_DATE is the date and time the row of data was created.
	"update_user" varchar(64)	 NOT NULL,    -- UPDATE_USER is an audit column that indicates the user that updated the record.
	"update_date" DATE NOT NULL DEFAULT CURRENT_TIMESTAMP    -- UPDATE_DATE is the date and time the row of data was updated.
)
TABLESPACE	PG_DEFAULT
;

/* Create Table Comments, Sequences for Autonumber Columns */

COMMENT ON COLUMN "wfprev"."project_boundary_audit"."audit_table_sequence"
	IS 'Added by transform - intent is make this sequence noncached so order is maintained'
;

COMMENT ON COLUMN "wfprev"."project_boundary_audit"."audit_action_code"
	IS 'Contains the reason the audit record is written - Insert/Update/Delete/Conversion'
;

COMMENT ON COLUMN "wfprev"."project_boundary_audit"."project_boundary_guid"
	IS 'project_boundary_guid is a unique identifier for the record.'
;

COMMENT ON COLUMN "wfprev"."project_boundary_audit"."project_guid"
	IS 'project_guid: Is a foreign key to project: Project is used to track Prevention Projects which are created to reduce risks due to forest fires. '
;

COMMENT ON COLUMN "wfprev"."project_boundary_audit"."system_start_timestamp"
	IS 'System Start Timestamp is the date and time with millisecond accuracy when the record becomes effective.   The System Start Timestamp and System End Timestamp fields are managed as a set by the system and is not exposed to the end user. When an update is made to the data in the record, the system will be responsible for expiring the old record and creating a new record with the updated data. '
;

COMMENT ON COLUMN "wfprev"."project_boundary_audit"."system_end_timestamp"
	IS 'System End Timestamp is the date and time with millisecond accuracy when the record becomes effective. The current instance of the record will be set to Dec 31, 9999 by default.  The System Start Timestamp and System End Timestamp fields are managed as a set by the system and is not exposed to the end user. When an update is made to the data in the record, the system will be responsible for expiring the old record and creating a new record with the updated data. '
;

COMMENT ON COLUMN "wfprev"."project_boundary_audit"."mapping_label"
	IS 'Mapping Label is a label assigned to the polygon for downstream mapping purposes.'
;

COMMENT ON COLUMN "wfprev"."project_boundary_audit"."collection_date"
	IS 'Collection Date is the date that the polygon was originally captured/created.'
;

COMMENT ON COLUMN "wfprev"."project_boundary_audit"."collection_method"
	IS 'Collection Method describes the method used to collect the Geometry data.'
;

COMMENT ON COLUMN "wfprev"."project_boundary_audit"."collector_name"
	IS 'Collector Name is the name of the person who established  the project boundary polygon. '
;

COMMENT ON COLUMN "wfprev"."project_boundary_audit"."boundary_size_ha"
	IS 'Incident Size Ha is the size of the project measured in hectares, to three decimals (e.g., 0.001).'
;

COMMENT ON COLUMN "wfprev"."project_boundary_audit"."boundary_comment"
	IS 'Boundary Comment provides additional information related to the collection of the polygon data.'
;

COMMENT ON COLUMN "wfprev"."project_boundary_audit"."location_geometry"
	IS 'Location Geometry is a point location using lat long, which is the centroid off the boundary geometry. The location geometry is used to place a pin on a map for the project location. '
;

COMMENT ON COLUMN "wfprev"."project_boundary_audit"."boundary_geometry"
	IS 'Boundary Geometry is the geographic area that is the boundary of the project.'
;

COMMENT ON COLUMN "wfprev"."project_boundary_audit"."revision_count"
	IS 'REVISION_COUNT is the number of times that the row of data has been changed. The column is used for optimistic locking via application code.'
;

COMMENT ON COLUMN "wfprev"."project_boundary_audit"."create_user"
	IS 'CREATE_USER is an audit column that indicates the user that created the record.'
;

COMMENT ON COLUMN "wfprev"."project_boundary_audit"."create_date"
	IS 'CREATE_DATE is the date and time the row of data was created.'
;

COMMENT ON COLUMN "wfprev"."project_boundary_audit"."update_user"
	IS 'UPDATE_USER is an audit column that indicates the user that updated the record.'
;

COMMENT ON COLUMN "wfprev"."project_boundary_audit"."update_date"
	IS 'UPDATE_DATE is the date and time the row of data was updated.'
;

/* Create Primary Keys, Indexes, Uniques, Checks */

ALTER TABLE "wfprev"."project_boundary_audit" ADD CONSTRAINT "prjbnd_aud_pk"
	PRIMARY KEY ()
;