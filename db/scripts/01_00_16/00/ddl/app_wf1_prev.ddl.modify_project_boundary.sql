ALTER TABLE wfprev.project_boundary DROP COLUMN boundary_geometry;
ALTER TABLE wfprev.project_boundary ADD COLUMN boundary_geometry geometry(MultiPolygon, 4326);
COMMENT ON COLUMN "wfprev"."project_boundary"."boundary_geometry"
  IS 'Boundary Geometry is the geographic area that is the boundary of the project.'
;

ALTER TABLE wfprev.project_boundary_audit DROP COLUMN boundary_geometry;
ALTER TABLE wfprev.project_boundary_audit ADD COLUMN boundary_geometry geometry(MultiPolygon, 4326);
COMMENT ON COLUMN "wfprev"."project_boundary_audit"."boundary_geometry"
  IS 'Boundary Geometry is the geographic area that is the boundary of the project.'
;