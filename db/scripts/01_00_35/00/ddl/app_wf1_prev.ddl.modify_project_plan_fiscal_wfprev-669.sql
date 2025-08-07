-- Constraint: prjpfy_uk

ALTER TABLE IF EXISTS wfprev.project_plan_fiscal DROP CONSTRAINT IF EXISTS prjpfy_uk;

ALTER TABLE IF EXISTS wfprev.project_plan_fiscal 
  ADD CONSTRAINT "prjpfy_uk" UNIQUE ("project_guid","activity_category_code","fiscal_year","project_fiscal_name","project_fiscal_description","business_area_comment"); 