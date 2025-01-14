/* ---------------------------------------------------- */
/*  Generated by Enterprise Architect Version 12.0 		*/
/*  Created On : 10-Jan-2025 3:59:35 PM 				*/
/*  DBMS       : PostgreSQL 						*/
/* ---------------------------------------------------- */

/* Create Tables */

CREATE TABLE "wfprev"."activity"
(
	"activity_guid" UUID NOT NULL,    -- activity_guid is a unique identifier for the record.
	"project_plan_fiscal_guid" UUID NOT NULL,    -- project_plan_fiscal_guid: Is a foreign key to project_plan_fiscal: Project Plan Fiscal is used to store a plan for the work to be done in a fiscal year for the project. 
	"activity_status_code" varchar(10)	 NOT NULL,    -- activity_status_code: Is a foreign key to activity_status_code: Activity Status Code is the status of an activity for a fiscal year. Values are:  	- Active 	- Completed 	- Deleted 	- Archived
	"silviculture_base_guid" UUID NULL,    -- silviculture_base_guid: Is a foreign key to silviculture_base: Silviculture Base is a list of Silviculture bases that are mapped to a project type.
	"silviculture_technique_guid" UUID NULL,    -- silviculture_technique_guid: Is a foreign key to silviculture_technique: Silviculture Technique is a list of Silviculture techniques that are mapped to a project type and Silviculture base.
	"silviculture_method_guid" UUID NULL,    -- silviculture_method_guid: Is a foreign key to silviculture_method: Silviculture Method is a list of Silviculture methods that are mapped to a project type and Silviculture technique.  
	"risk_rating_code" varchar(10)	 NOT NULL,    -- risk_rating_code: Is a foreign key to risk_rating_code: Risk Rating Code are ratings that determine the level of risk of a project not spending allocated budget.  Values:  	- Low Risk 	- Moderate Risk 	- High Risk
	"contract_phase_code" varchar(10)	 NULL,    -- contract_phase_code: Is a foreign key to contract_phase_code: Contract Phase Code is used to track the general status of contracts that are out for bid to suppliers to complete treatment work for the project. Values are:  	- Contract Out For Bid 	- Contract Awarded 	- Multiple Contracts Out For Bid 	- Multiple Contracts Awarded
	"activity_funding_source_guid" UUID NULL,    -- activity_funding_source_guid: Is a foreign key to activity_funding_source: Activity Funding Source is a list of agencies/organizations that are sources of activity funding. The funding source provides the budget for the activity.  Example Funding Sources:  	- WRR - Wildfire Risk Reduction 	- BCP - BC Parks 	- FEP - Forest Employment Program 	- FESBC - Forest Enhancement Society of BC 	- CFS - FireSmart Community Funding Supports
	"activity_name" varchar(4000)	 NOT NULL,    -- Activity Name is the name of the treatment activity.  If the Is_Results_Reportable_Ind is true, then an Activity Base must be selected.  If the Is_Results_Reportable_Ind is true then the Activity Name is blank, then the activity name will be derived from a combination of the selected Activity Base, Activity Technique, and Activity Method. 
	"activity_description" varchar(4000)	 NULL,    -- Activity Description is a description of the treatment activity to be performed.
	"activity_start_date" DATE NULL,    -- Activity Treatment Start Date is the date the treatment starts.
	"activity_end_date" DATE NOT NULL,    -- Activity Treatment End Date is the date the treatment ends.
	"planned_spend_amount" decimal(15,2) NULL,
	"planned_treatment_area_ha" decimal(15,4) NOT NULL,    -- Planned Treatment Area Ha is the area planed where the treatment activity will be applied.
	"reported_spend_amount" decimal(15,2) NULL,    -- Reported Spend Amount is the amount reported in CFS that has been spent on the activity.
	"completed_area_ha" decimal(15,4) NULL,    -- Completed Area Ha is the area that a treatment has been completed by the applied activity.
	"is_results_reportable_ind" boolean NOT NULL DEFAULT 'N',    -- Is RESULTS Reportable Ind indicates whether the treatment activity is reportable in the RESULTS system (Y) or not (N).  If reportable, then staff will enter treatment details into the RESULTS system.
	"outstanding_obligations_ind" boolean NOT NULL DEFAULT 'N',    -- Outstanding Obligations Ind indicates whether there are outstanding obligations for the treatment activity (Y) or not (N). As activities draw to an end, there may be some uncompleted work for which there is an obligation to complete at another time. This field indicates if there are such obligations.
	"activity_comment" varchar(4000)	 NULL,    -- Treatment Activity Comment is used to document activity comments including outstanding obligations.
	"is_spatial_added_ind" boolean NOT NULL DEFAULT 'N',    -- Is Spatial Added Ind indicates whether spatial data for the proposal has been added to the activity (Y) or not (N).
	"revision_count" decimal(10) NOT NULL DEFAULT 0,    -- REVISION_COUNT is the number of times that the row of data has been changed. The column is used for optimistic locking via application code.
	"create_user" varchar(64)	 NOT NULL,    -- CREATE_USER is an audit column that indicates the user that created the record.
	"create_date" DATE NOT NULL DEFAULT CURRENT_TIMESTAMP,    -- CREATE_DATE is the date and time the row of data was created.
	"update_user" varchar(64)	 NOT NULL,    -- UPDATE_USER is an audit column that indicates the user that updated the record.
	"update_date" DATE NOT NULL DEFAULT CURRENT_TIMESTAMP    -- UPDATE_DATE is the date and time the row of data was updated.
)
TABLESPACE	PG_DEFAULT
;

/* Create Table Comments, Sequences for Autonumber Columns */

COMMENT ON TABLE "wfprev"."activity"
	IS 'Activity is used to track the treatment activities that are planned for the project for the fiscal year.'
;

COMMENT ON COLUMN "wfprev"."activity"."activity_guid"
	IS 'activity_guid is a unique identifier for the record.'
;

COMMENT ON COLUMN "wfprev"."activity"."project_plan_fiscal_guid"
	IS 'project_plan_fiscal_guid: Is a foreign key to project_plan_fiscal: Project Plan Fiscal is used to store a plan for the work to be done in a fiscal year for the project. '
;

COMMENT ON COLUMN "wfprev"."activity"."activity_status_code"
	IS 'activity_status_code: Is a foreign key to activity_status_code: Activity Status Code is the status of an activity for a fiscal year. Values are:  	- Active 	- Completed 	- Deleted 	- Archived'
;

COMMENT ON COLUMN "wfprev"."activity"."silviculture_base_guid"
	IS 'silviculture_base_guid: Is a foreign key to silviculture_base: Silviculture Base is a list of Silviculture bases that are mapped to a project type.'
;

COMMENT ON COLUMN "wfprev"."activity"."silviculture_technique_guid"
	IS 'silviculture_technique_guid: Is a foreign key to silviculture_technique: Silviculture Technique is a list of Silviculture techniques that are mapped to a project type and Silviculture base.'
;

COMMENT ON COLUMN "wfprev"."activity"."silviculture_method_guid"
	IS 'silviculture_method_guid: Is a foreign key to silviculture_method: Silviculture Method is a list of Silviculture methods that are mapped to a project type and Silviculture technique.  '
;

COMMENT ON COLUMN "wfprev"."activity"."risk_rating_code"
	IS 'risk_rating_code: Is a foreign key to risk_rating_code: Risk Rating Code are ratings that determine the level of risk of a project not spending allocated budget.  Values:  	- Low Risk 	- Moderate Risk 	- High Risk'
;

COMMENT ON COLUMN "wfprev"."activity"."contract_phase_code"
	IS 'contract_phase_code: Is a foreign key to contract_phase_code: Contract Phase Code is used to track the general status of contracts that are out for bid to suppliers to complete treatment work for the project. Values are:  	- Contract Out For Bid 	- Contract Awarded 	- Multiple Contracts Out For Bid 	- Multiple Contracts Awarded'
;

COMMENT ON COLUMN "wfprev"."activity"."activity_funding_source_guid"
	IS 'activity_funding_source_guid: Is a foreign key to activity_funding_source: Activity Funding Source is a list of agencies/organizations that are sources of activity funding. The funding source provides the budget for the activity.  Example Funding Sources:  	- WRR - Wildfire Risk Reduction 	- BCP - BC Parks 	- FEP - Forest Employment Program 	- FESBC - Forest Enhancement Society of BC 	- CFS - FireSmart Community Funding Supports'
;

COMMENT ON COLUMN "wfprev"."activity"."activity_name"
	IS 'Activity Name is the name of the treatment activity.  If the Is_Results_Reportable_Ind is true, then an Activity Base must be selected.  If the Is_Results_Reportable_Ind is true then the Activity Name is blank, then the activity name will be derived from a combination of the selected Activity Base, Activity Technique, and Activity Method. '
;

COMMENT ON COLUMN "wfprev"."activity"."activity_description"
	IS 'Activity Description is a description of the treatment activity to be performed.'
;

COMMENT ON COLUMN "wfprev"."activity"."activity_start_date"
	IS 'Activity Treatment Start Date is the date the treatment starts.'
;

COMMENT ON COLUMN "wfprev"."activity"."activity_end_date"
	IS 'Activity Treatment End Date is the date the treatment ends.'
;

COMMENT ON COLUMN "wfprev"."activity"."planned_treatment_area_ha"
	IS 'Planned Treatment Area Ha is the area planed where the treatment activity will be applied.'
;

COMMENT ON COLUMN "wfprev"."activity"."reported_spend_amount"
	IS 'Reported Spend Amount is the amount reported in CFS that has been spent on the activity.'
;

COMMENT ON COLUMN "wfprev"."activity"."completed_area_ha"
	IS 'Completed Area Ha is the area that a treatment has been completed by the applied activity.'
;

COMMENT ON COLUMN "wfprev"."activity"."is_results_reportable_ind"
	IS 'Is RESULTS Reportable Ind indicates whether the treatment activity is reportable in the RESULTS system (Y) or not (N).  If reportable, then staff will enter treatment details into the RESULTS system.'
;

COMMENT ON COLUMN "wfprev"."activity"."outstanding_obligations_ind"
	IS 'Outstanding Obligations Ind indicates whether there are outstanding obligations for the treatment activity (Y) or not (N). As activities draw to an end, there may be some uncompleted work for which there is an obligation to complete at another time. This field indicates if there are such obligations.'
;

COMMENT ON COLUMN "wfprev"."activity"."activity_comment"
	IS 'Treatment Activity Comment is used to document activity comments including outstanding obligations.'
;

COMMENT ON COLUMN "wfprev"."activity"."is_spatial_added_ind"
	IS 'Is Spatial Added Ind indicates whether spatial data for the proposal has been added to the activity (Y) or not (N).'
;

COMMENT ON COLUMN "wfprev"."activity"."revision_count"
	IS 'REVISION_COUNT is the number of times that the row of data has been changed. The column is used for optimistic locking via application code.'
;

COMMENT ON COLUMN "wfprev"."activity"."create_user"
	IS 'CREATE_USER is an audit column that indicates the user that created the record.'
;

COMMENT ON COLUMN "wfprev"."activity"."create_date"
	IS 'CREATE_DATE is the date and time the row of data was created.'
;

COMMENT ON COLUMN "wfprev"."activity"."update_user"
	IS 'UPDATE_USER is an audit column that indicates the user that updated the record.'
;

COMMENT ON COLUMN "wfprev"."activity"."update_date"
	IS 'UPDATE_DATE is the date and time the row of data was updated.'
;

/* Create Primary Keys, Indexes, Uniques, Checks */

ALTER TABLE "wfprev"."activity" ADD CONSTRAINT "actvty_pk"
	PRIMARY KEY ("activity_guid")
;

ALTER TABLE "wfprev"."activity" ADD CONSTRAINT "actvty_uk" UNIQUE ("project_plan_fiscal_guid")
;

CREATE INDEX "actvty_actscd_idx" ON "wfprev"."activity" ("activity_status_code" ASC)
;

CREATE INDEX "actvty_slvbase_idx" ON "wfprev"."activity" ("silviculture_base_guid" ASC)
;

CREATE INDEX "actvty_prjpfy_idx" ON "wfprev"."activity" ("project_plan_fiscal_guid" ASC)
;

CREATE INDEX "actvty_slvtech_idx" ON "wfprev"."activity" ("silviculture_technique_guid" ASC)
;

CREATE INDEX "actvty_slvmethod_idx" ON "wfprev"."activity" ("silviculture_method_guid" ASC)
;

CREATE INDEX "actvty_rrcd_idx" ON "wfprev"."activity" ("risk_rating_code" ASC)
;

CREATE INDEX "actvty_cphcd_idx" ON "wfprev"."activity" ("contract_phase_code" ASC)
;

CREATE INDEX "actvty_actfsrc_idx" ON "wfprev"."activity" ("activity_funding_source_guid" ASC)
;

/* Create Foreign Key Constraints */

ALTER TABLE "wfprev"."activity" ADD CONSTRAINT "actvty_actscd_fk"
	FOREIGN KEY ("activity_status_code") REFERENCES "wfprev"."activity_status_code" ("activity_status_code") ON DELETE No Action ON UPDATE No Action
;

ALTER TABLE "wfprev"."activity" ADD CONSTRAINT "actvty_slvbase_fk"
	FOREIGN KEY ("silviculture_base_guid") REFERENCES "wfprev"."silviculture_base" ("silviculture_base_guid") ON DELETE No Action ON UPDATE No Action
;

ALTER TABLE "wfprev"."activity" ADD CONSTRAINT "actvty_prjpfy_fk"
	FOREIGN KEY ("project_plan_fiscal_guid") REFERENCES "wfprev"."project_plan_fiscal" ("project_plan_fiscal_guid") ON DELETE No Action ON UPDATE No Action
;

ALTER TABLE "wfprev"."activity" ADD CONSTRAINT "actvty_slvtech_fk"
	FOREIGN KEY ("silviculture_technique_guid") REFERENCES "wfprev"."silviculture_technique" ("silviculture_technique_guid") ON DELETE No Action ON UPDATE No Action
;

ALTER TABLE "wfprev"."activity" ADD CONSTRAINT "actvty_slvmethod_fk"
	FOREIGN KEY ("silviculture_method_guid") REFERENCES "wfprev"."silviculture_method" ("silviculture_method_guid") ON DELETE No Action ON UPDATE No Action
;

ALTER TABLE "wfprev"."activity" ADD CONSTRAINT "actvty_rrcd_fk"
	FOREIGN KEY ("risk_rating_code") REFERENCES "wfprev"."risk_rating_code" ("risk_rating_code") ON DELETE No Action ON UPDATE No Action
;

ALTER TABLE "wfprev"."activity" ADD CONSTRAINT "actvty_cphcd_fk"
	FOREIGN KEY ("contract_phase_code") REFERENCES "wfprev"."contract_phase_code" ("contract_phase_code") ON DELETE No Action ON UPDATE No Action
;

ALTER TABLE "wfprev"."activity" ADD CONSTRAINT "actvty_actfsrc_fk"
	FOREIGN KEY ("activity_funding_source_guid") REFERENCES "wfprev"."activity_funding_source" ("activity_funding_source_guid") ON DELETE No Action ON UPDATE No Action
;