ALTER TABLE wfprev.project_boundary DROP COLUMN location_geometry;
ALTER TABLE wfprev.project_boundary ADD COLUMN location_geometry geometry(Point, 4326);
COMMENT ON COLUMN "wfprev"."project_boundary"."location_geometry"
	IS 'Location Geometry is a point location using lat long, which is the centroid off the boundary geometry. The location geometry is used to place a pin on a map for the project location. '
;

ALTER TABLE wfprev.project_boundary_audit DROP COLUMN location_geometry;
ALTER TABLE wfprev.project_boundary_audit ADD COLUMN location_geometry geometry(Point, 4326);

COMMENT ON COLUMN "wfprev"."project_boundary_audit"."location_geometry"
	IS 'Location Geometry is a point location using lat long, which is the centroid off the boundary geometry. The location geometry is used to place a pin on a map for the project location. '
;