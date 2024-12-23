ALTER TABLE "wfprev"."project" ADD COLUMN "primary_objective_type_code" varchar(10);
COMMENT ON COLUMN "wfprev"."project"."primary_objective_type_code"
	IS 'primary_objective_type_code: Is a foreign key to objective_type_code: Objective Type Code is the set of objectives that are desired outcomes for a project.  Values are:  	- Wildfire Risk Reduction 	- Critical Infrastructure 	- Ecosystem Restoration 	- Egress/Evacuation 	- Forest Health 	- Hazard Abatement 	- Range Habitat Management 	- Reconciliation 	- Wildfire Habitat Management 	- Other';
CREATE INDEX "prjct_objtcd_primary_idx" ON "wfprev"."project" ("primary_objective_type_code" ASC);
ALTER TABLE "wfprev"."project" ADD CONSTRAINT "prjct_objtcd_primary_fk"
	FOREIGN KEY ("objective_type_code") REFERENCES "wfprev"."objective_type_code" ("objective_type_code") ON DELETE No Action ON UPDATE No Action;

ALTER TABLE "wfprev"."project" ADD COLUMN "secondary_objective_type_code" varchar(10);
COMMENT ON COLUMN "wfprev"."project"."secondary_objective_type_code"
	IS 'secondary_objective_type_code: Is a foreign key to objective_type_code: Objective Type Code is the set of objectives that are desired outcomes for a project.  Values are:  	- Wildfire Risk Reduction 	- Critical Infrastructure 	- Ecosystem Restoration 	- Egress/Evacuation 	- Forest Health 	- Hazard Abatement 	- Range Habitat Management 	- Reconciliation 	- Wildfire Habitat Management 	- Other';
CREATE INDEX "prjct_objtcd_secondary_idx" ON "wfprev"."project" ("secondary_objective_type_code" ASC);
ALTER TABLE "wfprev"."project" ADD CONSTRAINT "prjct_objtcd_secondary_fk"
	FOREIGN KEY ("objective_type_code") REFERENCES "wfprev"."objective_type_code" ("objective_type_code") ON DELETE No Action ON UPDATE No Action;

ALTER TABLE "wfprev"."project" ADD COLUMN  "secondary_objective_rationale" varchar(200);
COMMENT ON COLUMN "wfprev"."project"."secondary_objective_rationale"
	IS 'Secondary Objective Rationale may be supplied when a Secondary Objective Type code is specified.';

ALTER TABLE "wfprev"."project" ADD COLUMN "tertiary_objective_type_code" varchar(10);
COMMENT ON COLUMN "wfprev"."project"."tertiary_objective_type_code"
	IS 'tertiary_objective_type_code: Is a foreign key to objective_type_code: Objective Type Code is the set of objectives that are desired outcomes for a project.  Values are:  	- Wildfire Risk Reduction 	- Critical Infrastructure 	- Ecosystem Restoration 	- Egress/Evacuation 	- Forest Health 	- Hazard Abatement 	- Range Habitat Management 	- Reconciliation 	- Wildfire Habitat Management 	- Other';
CREATE INDEX "prjct_objtcd_tertiary_idx" ON "wfprev"."project" ("tertiary_objective_type_code" ASC);
ALTER TABLE "wfprev"."project" ADD CONSTRAINT "prjct_objtcd_tertiary_fk"
	FOREIGN KEY ("objective_type_code") REFERENCES "wfprev"."objective_type_code" ("objective_type_code") ON DELETE No Action ON UPDATE No Action;

ALTER TABLE "wfprev"."project" ADD COLUMN "tertiary_objective_rationale" varchar(200);
COMMENT ON COLUMN "wfprev"."project"."tertiary_objective_rationale"
	IS 'Tertiary Objective Rationale may be supplied when a Tertiary Objective Type code is specified.';




