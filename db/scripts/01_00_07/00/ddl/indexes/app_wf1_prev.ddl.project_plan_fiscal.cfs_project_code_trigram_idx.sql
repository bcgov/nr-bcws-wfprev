CREATE INDEX CONCURRENTLY IF NOT EXISTS index_prj_pln_fy_cfs_prj_code_trigram
ON wfprev.project_plan_fiscal
USING gin (cfs_project_code gin_trgm_ops);