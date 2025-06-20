/* ---------------------------------------------------- */
/*  Generated by Enterprise Architect Version 12.0 		*/
/*  Created On : 02-Jun-2025 4:04:58 PM 				*/
/*  DBMS       : PostgreSQL 						*/
/* ---------------------------------------------------- */

/* Create Tables */

CREATE TABLE "wfprev"."project_obj_selected"
(
	"project_obj_selected_guid" UUID NOT NULL,    -- project_obj_selected_guid is a unique identifier for the record.
	"project_objective_guid" UUID NOT NULL,    -- project_objective_guid: Is a foreign key to project_objective: Project Objective is a set of ranked objectives that can be associated to a Project.  The objectives are broken into categories, such as project alignment with government objectives and project level objectives.  The objectives are ranked by importance. A project will receive a project alignment score for each objective category which is used to rank the importance of a project.  Government Alignment Objectives are: 12 - Treatment with several TUs moving onto next Unit 11 - Adjacent Fuel Management Treatments, or new TU with Rx 10 - Prescription Complete - Next phase is treatment 9 - Tactical Plan Complete - New Phase Rx Development 8 - Mitigates Risk around or adjacent to Critical Infrastructure incl Watersheds 7 - Improve Egress/Evac Routes 6 - Maximize Funding Linkages 5 - Supports Reconciliation with Indigenous peoples  Project Levels Objectives are: 4 - PSTA Class Greater than 7 3 - Part of Larger Risk Reduction Strategy 2 - Cost Effectiveness 1 - WUI WRR Plans with AOI that expands outside of the WUI
	"project_obj_filter_summ_guid" UUID NOT NULL,    -- project_obj_filter_summ_guid: Is a foreign key to project_obj_filter_summ: Project Objective Filter Summary summarizes project filters which are used to evaluate objectives to be accomplished by a project.   Project objective filters are used to rank and prioritize projects.
	"objective_selected_ind" boolean NOT NULL DEFAULT 'N',    -- Objective Selected Ind indicates whether the objective mapped to the Fuel Management Request is selected (Y) or not (N).  If true then the weighted rank of the objective will count against the filter score.  If false then a value of 0 will be counted against the filter score.
	"revision_count" decimal(10) NOT NULL DEFAULT 0,    -- REVISION_COUNT is the number of times that the row of data has been changed. The column is used for optimistic locking via application code.
	"create_user" varchar(64)	 NOT NULL,    -- CREATE_USER is an audit column that indicates the user that created the record.
	"create_date" DATE NOT NULL DEFAULT CURRENT_TIMESTAMP,    -- CREATE_DATE is the date and time the row of data was created.
	"update_user" varchar(64)	 NOT NULL,    -- UPDATE_USER is an audit column that indicates the user that updated the record.
	"update_date" DATE NOT NULL DEFAULT CURRENT_TIMESTAMP    -- UPDATE_DATE is the date and time the row of data was updated.
)
TABLESPACE	PG_DEFAULT
;

/* Create Table Comments, Sequences for Autonumber Columns */

COMMENT ON TABLE "wfprev"."project_obj_selected"
	IS 'Project Objective Selected maps project objectives to a Project Objective Summary'
;

COMMENT ON COLUMN "wfprev"."project_obj_selected"."project_obj_selected_guid"
	IS 'project_obj_selected_guid is a unique identifier for the record.'
;

COMMENT ON COLUMN "wfprev"."project_obj_selected"."project_objective_guid"
	IS 'project_objective_guid: Is a foreign key to project_objective: Project Objective is a set of ranked objectives that can be associated to a Project.  The objectives are broken into categories, such as project alignment with government objectives and project level objectives.  The objectives are ranked by importance. A project will receive a project alignment score for each objective category which is used to rank the importance of a project.  Government Alignment Objectives are: 12 - Treatment with several TUs moving onto next Unit 11 - Adjacent Fuel Management Treatments, or new TU with Rx 10 - Prescription Complete - Next phase is treatment 9 - Tactical Plan Complete - New Phase Rx Development 8 - Mitigates Risk around or adjacent to Critical Infrastructure incl Watersheds 7 - Improve Egress/Evac Routes 6 - Maximize Funding Linkages 5 - Supports Reconciliation with Indigenous peoples  Project Levels Objectives are: 4 - PSTA Class Greater than 7 3 - Part of Larger Risk Reduction Strategy 2 - Cost Effectiveness 1 - WUI WRR Plans with AOI that expands outside of the WUI'
;

COMMENT ON COLUMN "wfprev"."project_obj_selected"."project_obj_filter_summ_guid"
	IS 'project_obj_filter_summ_guid: Is a foreign key to project_obj_filter_summ: Project Objective Filter Summary summarizes project filters which are used to evaluate objectives to be accomplished by a project.   Project objective filters are used to rank and prioritize projects.'
;

COMMENT ON COLUMN "wfprev"."project_obj_selected"."objective_selected_ind"
	IS 'Objective Selected Ind indicates whether the objective mapped to the Fuel Management Request is selected (Y) or not (N).  If true then the weighted rank of the objective will count against the filter score.  If false then a value of 0 will be counted against the filter score.'
;

COMMENT ON COLUMN "wfprev"."project_obj_selected"."revision_count"
	IS 'REVISION_COUNT is the number of times that the row of data has been changed. The column is used for optimistic locking via application code.'
;

COMMENT ON COLUMN "wfprev"."project_obj_selected"."create_user"
	IS 'CREATE_USER is an audit column that indicates the user that created the record.'
;

COMMENT ON COLUMN "wfprev"."project_obj_selected"."create_date"
	IS 'CREATE_DATE is the date and time the row of data was created.'
;

COMMENT ON COLUMN "wfprev"."project_obj_selected"."update_user"
	IS 'UPDATE_USER is an audit column that indicates the user that updated the record.'
;

COMMENT ON COLUMN "wfprev"."project_obj_selected"."update_date"
	IS 'UPDATE_DATE is the date and time the row of data was updated.'
;

/* Create Primary Keys, Indexes, Uniques, Checks */

ALTER TABLE "wfprev"."project_obj_selected" ADD CONSTRAINT "poslct_pk"
	PRIMARY KEY ("project_obj_selected_guid")
;

ALTER TABLE "wfprev"."project_obj_selected" ADD CONSTRAINT "poslct_uk" UNIQUE ("project_objective_guid","project_obj_filter_summ_guid")
;

CREATE INDEX "poslct_prjobj_idx" ON "wfprev"."project_obj_selected" ("project_objective_guid" ASC)
;

CREATE INDEX "poslct_pofs_idx" ON "wfprev"."project_obj_selected" ("project_obj_filter_summ_guid" ASC)
;

/* Create Foreign Key Constraints */

ALTER TABLE "wfprev"."project_obj_selected" ADD CONSTRAINT "poslct_prjobj_fk"
	FOREIGN KEY ("project_objective_guid") REFERENCES "wfprev"."project_objective" ("project_objective_guid") ON DELETE No Action ON UPDATE No Action
;

ALTER TABLE "wfprev"."project_obj_selected" ADD CONSTRAINT "poslct_pofs_fk"
	FOREIGN KEY ("project_obj_filter_summ_guid") REFERENCES "wfprev"."project_obj_filter_summ" ("project_obj_filter_summ_guid") ON DELETE No Action ON UPDATE No Action
;