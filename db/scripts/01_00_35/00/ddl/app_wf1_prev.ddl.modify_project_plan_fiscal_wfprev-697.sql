
ALTER TABLE wfprev.project_plan_fiscal ADD COLUMN "endorse_appr_update_name" varchar(100)  NULL;
ALTER TABLE wfprev.project_plan_fiscal ADD COLUMN "endorse_appr_update_user_guid" varchar(100)  NULL;
ALTER TABLE wfprev.project_plan_fiscal ADD COLUMN "endorse_appr_update_userid" varchar(100)  NULL;
ALTER TABLE wfprev.project_plan_fiscal ADD COLUMN "endorse_appr_updated_timestamp" TIMESTAMP  NULL;

COMMENT ON COLUMN "wfprev"."project_plan_fiscal"."endorse_appr_update_name"
  IS 'Endorsement Approval Update Name is the name of the user that last made changes to endorsement or approval details.'
;

COMMENT ON COLUMN "wfprev"."project_plan_fiscal"."endorse_appr_update_user_guid"
  IS 'Endorsement Approval Update User Guid is the user guid of the user that last made changes to endorsement or approval details.'
;

COMMENT ON COLUMN "wfprev"."project_plan_fiscal"."endorse_appr_update_userid"
  IS 'Endorsement Approval Update Userid is the userid of the user that last made changes to endorsement or approval details.'
;

COMMENT ON COLUMN "wfprev"."project_plan_fiscal"."endorse_appr_updated_timestamp"
  IS 'Endorsement Approval Update Timestamp is the last date and time a user made changes to endorsement or approval details.'
;

ALTER TABLE wfprev.project_plan_fiscal_audit ADD COLUMN "endorse_appr_update_name" varchar(100)  NULL;
ALTER TABLE wfprev.project_plan_fiscal_audit ADD COLUMN "endorse_appr_update_user_guid" varchar(100)  NULL;
ALTER TABLE wfprev.project_plan_fiscal_audit ADD COLUMN "endorse_appr_update_userid" varchar(100)  NULL;
ALTER TABLE wfprev.project_plan_fiscal_audit ADD COLUMN "endorse_appr_updated_timestamp" TIMESTAMP  NULL;

COMMENT ON COLUMN "wfprev"."project_plan_fiscal_audit"."endorse_appr_update_name"
  IS 'Endorsement Approval Update Name is the name of the user that last made changes to endorsement or approval details.'
;

COMMENT ON COLUMN "wfprev"."project_plan_fiscal_audit"."endorse_appr_update_user_guid"
  IS 'Endorsement Approval Update User Guid is the user guid of the user that last made changes to endorsement or approval details.'
;

COMMENT ON COLUMN "wfprev"."project_plan_fiscal_audit"."endorse_appr_update_userid"
  IS 'Endorsement Approval Update Userid is the userid of the user that last made changes to endorsement or approval details.'
;

COMMENT ON COLUMN "wfprev"."project_plan_fiscal_audit"."endorse_appr_updated_timestamp"
  IS 'Endorsement Approval Update Timestamp is the last date and time a user made changes to endorsement or approval details.'
;

