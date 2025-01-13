
CREATE INDEX CONCURRENTLY index_prj_pln_fy_bus_comment_trigram
ON wfprev.project_plan_fiscal
USING gin (business_area_comment gin_trgm_ops);

CREATE INDEX CONCURRENTLY index_prj_pln_fy_cfs_prj_code_trigram
ON wfprev.project_plan_fiscal
USING gin (cfs_project_code gin_trgm_ops);

CREATE INDEX CONCURRENTLY index_prj_pln_fy_fn_partner_trigram
ON wfprev.project_plan_fiscal
USING gin (first_nations_partner gin_trgm_ops);

CREATE INDEX CONCURRENTLY index_prj_pln_fy_oth_partner_trigram
ON wfprev.project_plan_fiscal
USING gin (other_partner gin_trgm_ops);

CREATE INDEX CONCURRENTLY index_prj_pln_fy_prj_fiscal_desc_trigram
ON wfprev.project_plan_fiscal
USING gin (project_fiscal_description gin_trgm_ops);

CREATE INDEX CONCURRENTLY index_prj_pln_fy_prj_fiscal_name_trigram
ON wfprev.project_plan_fiscal
USING gin (project_fiscal_name gin_trgm_ops);

CREATE INDEX CONCURRENTLY index_prj_pln_fy_results_no_trigram
ON wfprev.project_plan_fiscal
USING gin (results_number gin_trgm_ops);

CREATE INDEX CONCURRENTLY index_project_close_community_trigram
ON wfprev.project
USING gin (closest_community_name gin_trgm_ops);

CREATE INDEX CONCURRENTLY index_project_prj_desc_trigram
ON wfprev.project
USING gin (project_description gin_trgm_ops);

CREATE INDEX CONCURRENTLY index_project_prj_lead_trigram
ON wfprev.project
USING gin (project_lead gin_trgm_ops);

CREATE INDEX CONCURRENTLY index_project_prj_name_trigram
ON wfprev.project
USING gin (project_name gin_trgm_ops);

CREATE INDEX CONCURRENTLY index_project_site_unit_name_trigram
ON wfprev.project
USING gin (site_unit_name gin_trgm_ops);