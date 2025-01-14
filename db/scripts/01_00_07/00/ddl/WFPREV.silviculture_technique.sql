/* ---------------------------------------------------- */
/*  Generated by Enterprise Architect Version 12.0 		*/
/*  Created On : 10-Jan-2025 4:03:47 PM 				*/
/*  DBMS       : PostgreSQL 						*/
/* ---------------------------------------------------- */

/* Create Tables */

CREATE TABLE "wfprev"."silviculture_technique"
(
	"silviculture_technique_guid" UUID NOT NULL,    -- silviculture_technique_guid is a unique identifier for the record.
	"silviculture_base_guid" UUID NOT NULL,    -- silviculture_base_guid: Is a foreign key to silviculture_base: Silviculture Base is a list of Silviculture bases that are mapped to a project type.
	"silviculture_technique_code" varchar(10)	 NOT NULL,    -- silviculture_technique_code: Is a foreign key to silviculture_technique_code: Silviculture Technique is a list of Silviculture techniques that can be applied as a treatment activity.  Taken from THE.SILV_TECHNIQUE_CODE values are:  AE	Aerial BI	Biological BL	Backlog Silviculture Prescription BR	Brushing BU	Burn CA	Chemical Air CB	Climb CC	Cone Collection CG	Chemical Ground CL	Road Clearing CS	Cone Survey CT	Commercial Thinning DD	Site Deg/Disturbance DE	Deactivation DR	Drainage FE	Fertilization FG	Free Growing FH	Forest Health FI	Fire FN	Forest Nutrient FP	Fill Planting GR	Ground GS	Grass Seeding HV	Harvest HZ	Hazard JS	Juvenile Spacing MA	Manual ME	Mechanical MS	Mineral Soil Disturbance NA	Natural OF	Office Review OG	Organic Ground (e.g. sewage sludge, fish mort, pulp sludge, et c.) PA	Pay PH	Pre-Harvest Silviculture Prescription PL	Planting PO	Post Harvest Inspection PR	Pruning RA	Regeneration Performance Assessment RE	Reconnaissance RG	Regen/Stocking RO	Roadside RP	Re-Planting RR	Road Rehab RT	Root Rot SE	Seedling Protection SI	Silviculture Prescription SL	Slide SM	Stand Management Prescription SP	Site Preparation SR	Site Rehabilitation SU	Survival SX	Silviculture Trials TP	Trapping TR	Traverse UP	Road Upgrading WT	Wildlife Tree
	"system_start_timestamp" TIMESTAMP NOT NULL,    -- System Start Timestamp is the date and time with millisecond accuracy when the record becomes effective.   The System Start Timestamp and System End Timestamp fields are managed as a set by the system and is not exposed to the end user. When an update is made to the data in the record, the system will be responsible for expiring the old record and creating a new record with the updated data. 
	"system_end_timestamp" TIMESTAMP NOT NULL,    -- System End Timestamp is the date and time with millisecond accuracy when the record becomes effective. The current instance of the record will be set to Dec 31, 9999 by default.  The System Start Timestamp and System End Timestamp fields are managed as a set by the system and is not exposed to the end user. When an update is made to the data in the record, the system will be responsible for expiring the old record and creating a new record with the updated data. 
	"revision_count" decimal(10) NOT NULL DEFAULT 0,    -- REVISION_COUNT is the number of times that the row of data has been changed. The column is used for optimistic locking via application code.
	"create_user" varchar(64)	 NOT NULL,    -- CREATE_USER is an audit column that indicates the user that created the record.
	"create_date" DATE NOT NULL DEFAULT CURRENT_TIMESTAMP,    -- CREATE_DATE is the date and time the row of data was created.
	"update_user" varchar(64)	 NOT NULL,    -- UPDATE_USER is an audit column that indicates the user that updated the record.
	"update_date" DATE NOT NULL DEFAULT CURRENT_TIMESTAMP    -- UPDATE_DATE is the date and time the row of data was updated.
)
TABLESPACE	PG_DEFAULT
;

/* Create Table Comments, Sequences for Autonumber Columns */

COMMENT ON TABLE "wfprev"."silviculture_technique"
	IS 'Silviculture Technique is a list of Silviculture techniques that are mapped to a project type and Silviculture base.'
;

COMMENT ON COLUMN "wfprev"."silviculture_technique"."silviculture_technique_guid"
	IS 'silviculture_technique_guid is a unique identifier for the record.'
;

COMMENT ON COLUMN "wfprev"."silviculture_technique"."silviculture_base_guid"
	IS 'silviculture_base_guid: Is a foreign key to silviculture_base: Silviculture Base is a list of Silviculture bases that are mapped to a project type.'
;

COMMENT ON COLUMN "wfprev"."silviculture_technique"."silviculture_technique_code"
	IS 'silviculture_technique_code: Is a foreign key to silviculture_technique_code: Silviculture Technique is a list of Silviculture techniques that can be applied as a treatment activity.  Taken from THE.SILV_TECHNIQUE_CODE values are:  AE	Aerial BI	Biological BL	Backlog Silviculture Prescription BR	Brushing BU	Burn CA	Chemical Air CB	Climb CC	Cone Collection CG	Chemical Ground CL	Road Clearing CS	Cone Survey CT	Commercial Thinning DD	Site Deg/Disturbance DE	Deactivation DR	Drainage FE	Fertilization FG	Free Growing FH	Forest Health FI	Fire FN	Forest Nutrient FP	Fill Planting GR	Ground GS	Grass Seeding HV	Harvest HZ	Hazard JS	Juvenile Spacing MA	Manual ME	Mechanical MS	Mineral Soil Disturbance NA	Natural OF	Office Review OG	Organic Ground (e.g. sewage sludge, fish mort, pulp sludge, et c.) PA	Pay PH	Pre-Harvest Silviculture Prescription PL	Planting PO	Post Harvest Inspection PR	Pruning RA	Regeneration Performance Assessment RE	Reconnaissance RG	Regen/Stocking RO	Roadside RP	Re-Planting RR	Road Rehab RT	Root Rot SE	Seedling Protection SI	Silviculture Prescription SL	Slide SM	Stand Management Prescription SP	Site Preparation SR	Site Rehabilitation SU	Survival SX	Silviculture Trials TP	Trapping TR	Traverse UP	Road Upgrading WT	Wildlife Tree'
;

COMMENT ON COLUMN "wfprev"."silviculture_technique"."system_start_timestamp"
	IS 'System Start Timestamp is the date and time with millisecond accuracy when the record becomes effective.   The System Start Timestamp and System End Timestamp fields are managed as a set by the system and is not exposed to the end user. When an update is made to the data in the record, the system will be responsible for expiring the old record and creating a new record with the updated data. '
;

COMMENT ON COLUMN "wfprev"."silviculture_technique"."system_end_timestamp"
	IS 'System End Timestamp is the date and time with millisecond accuracy when the record becomes effective. The current instance of the record will be set to Dec 31, 9999 by default.  The System Start Timestamp and System End Timestamp fields are managed as a set by the system and is not exposed to the end user. When an update is made to the data in the record, the system will be responsible for expiring the old record and creating a new record with the updated data. '
;

COMMENT ON COLUMN "wfprev"."silviculture_technique"."revision_count"
	IS 'REVISION_COUNT is the number of times that the row of data has been changed. The column is used for optimistic locking via application code.'
;

COMMENT ON COLUMN "wfprev"."silviculture_technique"."create_user"
	IS 'CREATE_USER is an audit column that indicates the user that created the record.'
;

COMMENT ON COLUMN "wfprev"."silviculture_technique"."create_date"
	IS 'CREATE_DATE is the date and time the row of data was created.'
;

COMMENT ON COLUMN "wfprev"."silviculture_technique"."update_user"
	IS 'UPDATE_USER is an audit column that indicates the user that updated the record.'
;

COMMENT ON COLUMN "wfprev"."silviculture_technique"."update_date"
	IS 'UPDATE_DATE is the date and time the row of data was updated.'
;

/* Create Primary Keys, Indexes, Uniques, Checks */

ALTER TABLE "wfprev"."silviculture_technique" ADD CONSTRAINT "slvtech_pk"
	PRIMARY KEY ("silviculture_technique_guid")
;

ALTER TABLE "wfprev"."silviculture_technique" ADD CONSTRAINT "slvtech_uk" UNIQUE ("silviculture_base_guid","silviculture_technique_code","system_start_timestamp","system_end_timestamp")
;

CREATE INDEX "slvtech_slvbase_idx" ON "wfprev"."silviculture_technique" ("silviculture_base_guid" ASC)
;

CREATE INDEX "slvtech_slvtchcd_idx" ON "wfprev"."silviculture_technique" ("silviculture_technique_code" ASC)
;

/* Create Foreign Key Constraints */

ALTER TABLE "wfprev"."silviculture_technique" ADD CONSTRAINT "slvtech_slvbase_fk"
	FOREIGN KEY ("silviculture_base_guid") REFERENCES "wfprev"."silviculture_base" ("silviculture_base_guid") ON DELETE No Action ON UPDATE No Action
;

ALTER TABLE "wfprev"."silviculture_technique" ADD CONSTRAINT "slvtech_slvtchcd_fk"
	FOREIGN KEY ("silviculture_technique_code") REFERENCES "wfprev"."silviculture_technique_code" ("silviculture_technique_code") ON DELETE No Action ON UPDATE No Action
;