
CREATE INDEX  IF NOT EXISTS index_prj_pln_fy_bus_comment_trigram
ON wfprev.project_plan_fiscal
USING gin (business_area_comment gin_trgm_ops);