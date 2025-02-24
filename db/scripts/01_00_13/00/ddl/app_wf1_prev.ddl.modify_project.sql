ALTER TABLE wfprev.project DROP CONSTRAINT prjct_fndsrc_primary_fk;
DROP INDEX wfprev.prjct_fndsrc_primary_idx;

ALTER TABLE wfprev.project DROP COLUMN PRIMARY_FUNDING_SOURCE_GUID;

ALTER TABLE wfprev.project ADD COLUMN "funding_stream_guid" UUID NULL;
COMMENT ON COLUMN "wfprev"."project"."funding_stream_guid"
	IS 'funding_stream_guid: Is a foreign key to funding_stream: Funding Stream defines the funding streams that provides monies to fund projects. Values are:  	- Fuels Management 	- Cultural and Prescribed Burn.   Note: The funding streams currently align 1 to 1 with project type, but this can change in the future to a larger set of funding streams.'
;

CREATE INDEX "prjct_fndstrm_idx" ON "wfprev"."project" ("funding_stream_guid" ASC)
;

ALTER TABLE "wfprev"."project" ADD CONSTRAINT "prjct_fndstrm_fk"
	FOREIGN KEY ("funding_stream_guid") REFERENCES "wfprev"."funding_stream" ("funding_stream_guid") ON DELETE No Action ON UPDATE No Action
;