/* ---------------------------------------------------- */
/*  Generated by Enterprise Architect Version 12.0 		*/
/*  Created On : 25-Jun-2025 3:35:24 PM 				*/
/*  DBMS       : PostgreSQL 						*/
/* ---------------------------------------------------- */

/* Create Tables */

CREATE TABLE "wfprev"."eval_criteria_sect_summ"
(
	"eval_criteria_sect_summ_guid" UUID NOT NULL,    -- eval_criteria_sect_summ_guid is a unique identifier for the record.
	"eval_criteria_sect_code" varchar(10)	 NOT NULL,    -- eval_criteria_sect_code: Is a foreign key to eval_criteria_sect_code: Objective Filter Level Code defines a set of filter levels that an objective can be defined for: Values are:  	- Coarse Filter 	- Medium Filter 	- Fine Filter 	- Risk Class & Location 	- Burn Development and Feasibility 	- Collective Impact
	"eval_criteria_summary_guid" UUID NOT NULL,    -- eval_criteria_summary_guid: Is a foreign key to eval_criteria_summary: Evaluation Criteria Summary summarizes project filters which are used to evaluate criteria to be accomplished by a project.   Evaluation criteria filters are used to rank and prioritize projects.
	"filter_section_score" decimal(6,2) NOT NULL,    -- Filter Section Score is a score calculated by summing the weighted ranks of evaluation criteria that are selected as being aligned for the project.
	"filter_section_comment" varchar(4000)	 NULL,    -- Filter Section Comment contains the rationale or additional comments regarding the filter section for the project.
	"revision_count" decimal(10) NOT NULL DEFAULT 0,    -- REVISION_COUNT is the number of times that the row of data has been changed. The column is used for optimistic locking via application code.
	"create_user" varchar(64)	 NOT NULL,    -- CREATE_USER is an audit column that indicates the user that created the record.
	"create_date" DATE NOT NULL DEFAULT CURRENT_TIMESTAMP,    -- CREATE_DATE is the date and time the row of data was created.
	"update_user" varchar(64)	 NOT NULL,    -- UPDATE_USER is an audit column that indicates the user that updated the record.
	"update_date" DATE NOT NULL DEFAULT CURRENT_TIMESTAMP    -- UPDATE_DATE is the date and time the row of data was updated.
)
TABLESPACE	PG_DEFAULT
;

/* Create Table Comments, Sequences for Autonumber Columns */

COMMENT ON TABLE "wfprev"."eval_criteria_sect_summ"
	IS 'Evaluation Criteria Section Summary is a summary of details for a evaluation criteria section, such as the total scores for the section.'
;

COMMENT ON COLUMN "wfprev"."eval_criteria_sect_summ"."eval_criteria_sect_summ_guid"
	IS 'eval_criteria_sect_summ_guid is a unique identifier for the record.'
;

COMMENT ON COLUMN "wfprev"."eval_criteria_sect_summ"."eval_criteria_sect_code"
	IS 'eval_criteria_sect_code: Is a foreign key to eval_criteria_sect_code: Objective Filter Level Code defines a set of filter levels that an objective can be defined for: Values are:  	- Coarse Filter 	- Medium Filter 	- Fine Filter 	- Risk Class & Location 	- Burn Development and Feasibility 	- Collective Impact'
;

COMMENT ON COLUMN "wfprev"."eval_criteria_sect_summ"."eval_criteria_summary_guid"
	IS 'eval_criteria_summary_guid: Is a foreign key to eval_criteria_summary: Evaluation Criteria Summary summarizes project filters which are used to evaluate criteria to be accomplished by a project.   Evaluation criteria filters are used to rank and prioritize projects.'
;

COMMENT ON COLUMN "wfprev"."eval_criteria_sect_summ"."filter_section_score"
	IS 'Filter Section Score is a score calculated by summing the weighted ranks of evaluation criteria that are selected as being aligned for the project.'
;

COMMENT ON COLUMN "wfprev"."eval_criteria_sect_summ"."filter_section_comment"
	IS 'Filter Section Comment contains the rationale or additional comments regarding the filter section for the project.'
;

COMMENT ON COLUMN "wfprev"."eval_criteria_sect_summ"."revision_count"
	IS 'REVISION_COUNT is the number of times that the row of data has been changed. The column is used for optimistic locking via application code.'
;

COMMENT ON COLUMN "wfprev"."eval_criteria_sect_summ"."create_user"
	IS 'CREATE_USER is an audit column that indicates the user that created the record.'
;

COMMENT ON COLUMN "wfprev"."eval_criteria_sect_summ"."create_date"
	IS 'CREATE_DATE is the date and time the row of data was created.'
;

COMMENT ON COLUMN "wfprev"."eval_criteria_sect_summ"."update_user"
	IS 'UPDATE_USER is an audit column that indicates the user that updated the record.'
;

COMMENT ON COLUMN "wfprev"."eval_criteria_sect_summ"."update_date"
	IS 'UPDATE_DATE is the date and time the row of data was updated.'
;

/* Create Primary Keys, Indexes, Uniques, Checks */

ALTER TABLE "wfprev"."eval_criteria_sect_summ" ADD CONSTRAINT "ecsum_pk"
	PRIMARY KEY ("eval_criteria_sect_summ_guid")
;

ALTER TABLE "wfprev"."eval_criteria_sect_summ" ADD CONSTRAINT "ecsum_uk" UNIQUE ("eval_criteria_sect_code","eval_criteria_summary_guid")
;

CREATE INDEX "ecsum_ecscd_idx" ON "wfprev"."eval_criteria_sect_summ" ("eval_criteria_sect_code" ASC)
;

CREATE INDEX "ecsum_ecpsum_idx" ON "wfprev"."eval_criteria_sect_summ" ("eval_criteria_summary_guid" ASC)
;

/* Create Foreign Key Constraints */

ALTER TABLE "wfprev"."eval_criteria_sect_summ" ADD CONSTRAINT "ecsum_ecscd_fk"
	FOREIGN KEY ("eval_criteria_sect_code") REFERENCES "wfprev"."eval_criteria_sect_code" ("eval_criteria_sect_code") ON DELETE No Action ON UPDATE No Action
;

ALTER TABLE "wfprev"."eval_criteria_sect_summ" ADD CONSTRAINT "ecsum_ecpsum_fk"
	FOREIGN KEY ("eval_criteria_summary_guid") REFERENCES "wfprev"."eval_criteria_summary" ("eval_criteria_summary_guid") ON DELETE No Action ON UPDATE No Action
;