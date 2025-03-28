ALTER TABLE wfprev.activity_boundary DROP COLUMN geometry;
ALTER TABLE wfprev.activity_boundary ADD COLUMN geometry geometry(MultiPolygon, 4326);
COMMENT ON COLUMN "wfprev"."activity_boundary"."geometry"
  IS 'Geometry is the geographic area that is the boundary of the activity.'
;
