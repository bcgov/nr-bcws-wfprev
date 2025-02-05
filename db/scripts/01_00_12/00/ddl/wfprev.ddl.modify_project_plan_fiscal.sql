ALTER TABLE "wfprev"."project_plan_fiscal" ADD COLUMN "proposal_type_code" varchar(10)	 NULL;

COMMENT ON COLUMN "wfprev"."project_plan_fiscal"."proposal_type_code"
	IS 'proposal_type_code: Is a foreign key to proposal_type_code: Proposal Type Code is used to define what type of proposal is being made for a project in a fiscal year.  Values are:  	- New 	- In Progress 	- Carry Over'
;

/* Create Foreign Key Constraints */


ALTER TABLE "wfprev"."project_plan_fiscal" ADD CONSTRAINT "prjpfy_prptcd_fk"
	FOREIGN KEY ("proposal_type_code") REFERENCES "wfprev"."proposal_type_code" ("proposal_type_code") ON DELETE No Action ON UPDATE No Action
;

ALTER TABLE "wfprev"."project_plan_fiscal_audit" ADD COLUMN "proposal_type_code" varchar(10)	 NULL;

COMMENT ON COLUMN "wfprev"."project_plan_fiscal_audit"."proposal_type_code"
	IS 'proposal_type_code: Is a foreign key to proposal_type_code: Proposal Type Code is used to define what type of proposal is being made for a project in a fiscal year.  Values are:  	- New 	- In Progress 	- Carry Over'
;
