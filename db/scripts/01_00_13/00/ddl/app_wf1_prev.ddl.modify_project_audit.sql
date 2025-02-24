ALTER TABLE wfprev.project_audit DROP COLUMN PRIMARY_FUNDING_SOURCE;

ALTER TABLE wfprev.project_audit ADD COLUMN "funding_stream_guid" UUID NULL;
COMMENT ON COLUMN "wfprev"."project_audit"."funding_stream_guid"
	IS 'funding_stream_guid: Is a foreign key to funding_stream: Funding Stream defines the funding streams that provides monies to fund projects. Values are:  	- Fuels Management 	- Cultural and Prescribed Burn.   Note: The funding streams currently align 1 to 1 with project type, but this can change in the future to a larger set of funding streams.'
;