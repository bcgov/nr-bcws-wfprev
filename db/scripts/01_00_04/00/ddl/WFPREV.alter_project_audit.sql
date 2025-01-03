
ALTER TABLE "wfprev"."project_audit" ADD  COLUMN 
	"primary_objective_type_code" varchar(10)	 NULL;    -- primary_objective_type_code: Is a foreign key to objective_type_code: Objective Type Code is the set of objectives that are desired outcomes for a project.  Values are:  	- Wildfire Risk Reduction 	- Critical Infrastructure 	- Ecosystem Restoration 	- Egress/Evacuation 	- Forest Health 	- Hazard Abatement 	- Range Habitat Management 	- Reconciliation 	- Wildfire Habitat Management 	- Other
COMMENT ON COLUMN "wfprev"."project_audit"."primary_objective_type_code"
	IS 'primary_objective_type_code: Is a foreign key to objective_type_code: Objective Type Code is the set of objectives that are desired outcomes for a project.  Values are:  	- Wildfire Risk Reduction 	- Critical Infrastructure 	- Ecosystem Restoration 	- Egress/Evacuation 	- Forest Health 	- Hazard Abatement 	- Range Habitat Management 	- Reconciliation 	- Wildfire Habitat Management 	- Other';

ALTER TABLE "wfprev"."project_audit" ADD  COLUMN 
	"primary_funding_source_guid" UUID NULL;    -- primary_funding_source: Is a foreign key to funding_source: Funding Source is a list of agencies/organizations that are sources of project funding. The funding source provides the budget for the project. Example Funding Sources:  	- WRR - Wildfire Risk Reduction 	- BCP - BC Parks 	- FEP - Forest Employment Program 	- FESBC - Forest Enhancement Society of BC 	- CFS - FireSmart Community Funding Supports   Potential Secondary Funding Sources  	- CLWRR - Crown Land Wildfire Risk Reduction 	- Forest District Org Unit 	- Community Resiliency Investment 	- ER - Ecosytem Restoration
COMMENT ON COLUMN "wfprev"."project_audit"."primary_funding_source_guid"
	IS 'primary_funding_source_guid: Is a foreign key to funding_source: Funding Source is a list of agencies/organizations that are sources of project funding. The funding source provides the budget for the project. Example Funding Sources:  	- WRR - Wildfire Risk Reduction 	- BCP - BC Parks 	- FEP - Forest Employment Program 	- FESBC - Forest Enhancement Society of BC 	- CFS - FireSmart Community Funding Supports   Potential Secondary Funding Sources  	- CLWRR - Crown Land Wildfire Risk Reduction 	- Forest District Org Unit 	- Community Resiliency Investment 	- ER - Ecosytem Restoration';

ALTER TABLE "wfprev"."project_audit" ADD  COLUMN 
	"secondary_objective_type_code" varchar(10)	 NULL;    -- secondary_objective_type_code: Is a foreign key to objective_type_code: Objective Type Code is the set of objectives that are desired outcomes for a project.  Values are:  	- Wildfire Risk Reduction 	- Critical Infrastructure 	- Ecosystem Restoration 	- Egress/Evacuation 	- Forest Health 	- Hazard Abatement 	- Range Habitat Management 	- Reconciliation 	- Wildfire Habitat Management 	- Other
COMMENT ON COLUMN "wfprev"."project_audit"."secondary_objective_type_code"
	IS 'secondary_objective_type_code: Is a foreign key to objective_type_code: Objective Type Code is the set of objectives that are desired outcomes for a project.  Values are:  	- Wildfire Risk Reduction 	- Critical Infrastructure 	- Ecosystem Restoration 	- Egress/Evacuation 	- Forest Health 	- Hazard Abatement 	- Range Habitat Management 	- Reconciliation 	- Wildfire Habitat Management 	- Other';

ALTER TABLE "wfprev"."project_audit" ADD  COLUMN 
	"secondary_objective_rationale" varchar(200)	 NULL;    -- Secondary Objective Rationale may be supplied when a Secondary Objective Type code is specified.
COMMENT ON COLUMN "wfprev"."project_audit"."secondary_objective_rationale"
	IS 'Secondary Objective Rationale may be supplied when a Secondary Objective Type code is specified.';

ALTER TABLE "wfprev"."project_audit" ADD  COLUMN 
	"tertiary_objective_type_code" varchar(10)	 NULL;    -- tertiary_objective_type_code: Is a foreign key to objective_type_code: Objective Type Code is the set of objectives that are desired outcomes for a project.  Values are:  	- Wildfire Risk Reduction 	- Critical Infrastructure 	- Ecosystem Restoration 	- Egress/Evacuation 	- Forest Health 	- Hazard Abatement 	- Range Habitat Management 	- Reconciliation 	- Wildfire Habitat Management 	- Other
COMMENT ON COLUMN "wfprev"."project_audit"."tertiary_objective_type_code"
	IS 'tertiary_objective_type_code: Is a foreign key to objective_type_code: Objective Type Code is the set of objectives that are desired outcomes for a project.  Values are:  	- Wildfire Risk Reduction 	- Critical Infrastructure 	- Ecosystem Restoration 	- Egress/Evacuation 	- Forest Health 	- Hazard Abatement 	- Range Habitat Management 	- Reconciliation 	- Wildfire Habitat Management 	- Other';

ALTER TABLE "wfprev"."project_audit" ADD  COLUMN 
	"tertiary_objective_rationale" varchar(200)	 NULL;    -- Tertiary Objective Rationale may be supplied when a Tertiary Objective Type code is specified.
COMMENT ON COLUMN "wfprev"."project_audit"."tertiary_objective_rationale"
	IS 'Tertiary Objective Rationale may be supplied when a Tertiary Objective Type code is specified.';