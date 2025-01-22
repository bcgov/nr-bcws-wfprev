ALTER TABLE "wfprev"."activity" DROP CONSTRAINT "actvty_uk";

ALTER TABLE "wfprev"."activity" ADD CONSTRAINT "actvty_uk" UNIQUE ("project_plan_fiscal_guid","activity_name","activity_start_date","activity_end_date");