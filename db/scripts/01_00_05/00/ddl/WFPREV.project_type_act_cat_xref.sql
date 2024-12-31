/* ---------------------------------------------------- */
/*  Generated by Enterprise Architect Version 12.0 		*/
/*  Created On : 20-Dec-2024 4:25:40 PM 				*/
/*  DBMS       : PostgreSQL 						*/
/* ---------------------------------------------------- */

/* Create Tables */

CREATE TABLE "wfprev"."project_type_act_cat_xref"
(
	"project_type_act_cat_xref_guid" UUID NOT NULL,    -- project_type_act_cat_xref_guid is a unique identifier for the record.
	"project_type_code" varchar(10)	 NOT NULL,    -- project_type_code: Is a foreign key to project_type_code: Project Type Code defines the type of the project.  Values are:   	- Fuel Management  	- Cultural & Prescribed Fire
	"activity_category_code" varchar(10)	 NOT NULL,    -- activity_category_code: Is a foreign key to activity_category_code: Activity Category Code is the set of activity categories that may be performed for a project.   Values are:  	- Tactical Planning 	- Prescription Development 	- Maintenance - Survey 	- Maintenance - Operational Treatment 	- Other (Assessments/Surveys) 	- Other (Administration) 	- Integrated Fuel Management Planning.
	"revision_count" decimal(10) NOT NULL DEFAULT 0,    -- REVISION_COUNT is the number of times that the row of data has been changed. The column is used for optimistic locking via application code.
	"create_user" varchar(64)	 NOT NULL,    -- CREATE_USER is an audit column that indicates the user that created the record.
	"create_date" DATE NOT NULL DEFAULT CURRENT_TIMESTAMP,    -- CREATE_DATE is the date and time the row of data was created.
	"update_user" varchar(64)	 NOT NULL,    -- UPDATE_USER is an audit column that indicates the user that updated the record.
	"update_date" DATE NOT NULL DEFAULT CURRENT_TIMESTAMP    -- UPDATE_DATE is the date and time the row of data was updated.
)
TABLESPACE	PG_DEFAULT
;

/* Create Table Comments, Sequences for Autonumber Columns */

COMMENT ON TABLE "wfprev"."project_type_act_cat_xref"
	IS 'Project Type Activity Category Xref is a mapping of activity categories that are available for each project type.'
;

COMMENT ON COLUMN "wfprev"."project_type_act_cat_xref"."project_type_act_cat_xref_guid"
	IS 'project_type_act_cat_xref_guid is a unique identifier for the record.'
;

COMMENT ON COLUMN "wfprev"."project_type_act_cat_xref"."project_type_code"
	IS 'project_type_code: Is a foreign key to project_type_code: Project Type Code defines the type of the project.  Values are:   	- Fuel Management  	- Cultural & Prescribed Fire'
;

COMMENT ON COLUMN "wfprev"."project_type_act_cat_xref"."activity_category_code"
	IS 'activity_category_code: Is a foreign key to activity_category_code: Activity Category Code is the set of activity categories that may be performed for a project.   Values are:  	- Tactical Planning 	- Prescription Development 	- Maintenance - Survey 	- Maintenance - Operational Treatment 	- Other (Assessments/Surveys) 	- Other (Administration) 	- Integrated Fuel Management Planning.'
;

COMMENT ON COLUMN "wfprev"."project_type_act_cat_xref"."revision_count"
	IS 'REVISION_COUNT is the number of times that the row of data has been changed. The column is used for optimistic locking via application code.'
;

COMMENT ON COLUMN "wfprev"."project_type_act_cat_xref"."create_user"
	IS 'CREATE_USER is an audit column that indicates the user that created the record.'
;

COMMENT ON COLUMN "wfprev"."project_type_act_cat_xref"."create_date"
	IS 'CREATE_DATE is the date and time the row of data was created.'
;

COMMENT ON COLUMN "wfprev"."project_type_act_cat_xref"."update_user"
	IS 'UPDATE_USER is an audit column that indicates the user that updated the record.'
;

COMMENT ON COLUMN "wfprev"."project_type_act_cat_xref"."update_date"
	IS 'UPDATE_DATE is the date and time the row of data was updated.'
;

/* Create Primary Keys, Indexes, Uniques, Checks */

ALTER TABLE "wfprev"."project_type_act_cat_xref" ADD CONSTRAINT "ptacx_pk"
	PRIMARY KEY ("project_type_act_cat_xref_guid")
;

ALTER TABLE "wfprev"."project_type_act_cat_xref" ADD CONSTRAINT "ptacx_uk" UNIQUE ("project_type_code","activity_category_code")
;

CREATE INDEX "ptacx_prjtcd_idx" ON "wfprev"."project_type_act_cat_xref" ("project_type_code" ASC)
;

CREATE INDEX "ptacx_actcatcd_idx" ON "wfprev"."project_type_act_cat_xref" ("activity_category_code" ASC)
;

/* Create Foreign Key Constraints */

ALTER TABLE "wfprev"."project_type_act_cat_xref" ADD CONSTRAINT "ptacx_prjtcd_fk"
	FOREIGN KEY ("project_type_code") REFERENCES "wfprev"."project_type_code" ("project_type_code") ON DELETE No Action ON UPDATE No Action
;

ALTER TABLE "wfprev"."project_type_act_cat_xref" ADD CONSTRAINT "ptacx_actcatcd_fk"
	FOREIGN KEY ("activity_category_code") REFERENCES "wfprev"."activity_category_code" ("activity_category_code") ON DELETE No Action ON UPDATE No Action
;