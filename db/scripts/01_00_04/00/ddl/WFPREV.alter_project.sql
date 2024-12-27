
ALTER TABLE "wfprev"."project" ADD  COLUMN
	"primary_funding_source_guid" UUID NULL;    -- primary_funding_source: Is a foreign key to funding_source: Funding Source is a list of agencies/organizations that are sources of project funding. The funding source provides the budget for the project. Example Funding Sources:  	- WRR - Wildfire Risk Reduction 	- BCP - BC Parks 	- FEP - Forest Employment Program 	- FESBC - Forest Enhancement Society of BC 	- CFS - FireSmart Community Funding Supports   Potential Secondary Funding Sources  	- CLWRR - Crown Land Wildfire Risk Reduction 	- Forest District Org Unit 	- Community Resiliency Investment 	- ER - Ecosytem Restoration

COMMENT ON COLUMN "wfprev"."project"."primary_funding_source_guid"
	IS 'primary_funding_source_guid: Is a foreign key to funding_source: Funding Source is a list of agencies/organizations that are sources of project funding. The funding source provides the budget for the project. Example Funding Sources:  	- WRR - Wildfire Risk Reduction 	- BCP - BC Parks 	- FEP - Forest Employment Program 	- FESBC - Forest Enhancement Society of BC 	- CFS - FireSmart Community Funding Supports   Potential Secondary Funding Sources  	- CLWRR - Crown Land Wildfire Risk Reduction 	- Forest District Org Unit 	- Community Resiliency Investment 	- ER - Ecosytem Restoration';

CREATE INDEX "prjct_fndsrc_primary_idx" ON "wfprev"."project" ("primary_funding_source_guid" ASC);

ALTER TABLE "wfprev"."project" ADD CONSTRAINT "prjct_fndsrc_primary_fk"
	FOREIGN KEY ("primary_funding_source_guid") REFERENCES "wfprev"."funding_source" ("funding_source_guid") ON DELETE No Action ON UPDATE No Action;