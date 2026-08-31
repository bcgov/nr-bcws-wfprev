-- WFPREV-1223
-- Makes wfprev.activity.is_spatial_added_ind derived data owned by the database.
--
-- The flag was previously maintained by ActivityBoundaryController, which recomputed it
-- after the transaction that deleted the boundary had already committed. Any failure in
-- between left the spatial deleted and the flag set, with no path back to a correct value.
--
-- This trigger recomputes the flag from wfprev.activity_boundary whenever a boundary is
-- added or removed, so it cannot drift regardless of which code path made the change.

CREATE OR REPLACE FUNCTION wfprev.fnc_sync_activity_spatial_ind() RETURNS TRIGGER AS $$
DECLARE
  v_activity_guid UUID;
BEGIN
  -- NEW is unassigned in a DELETE trigger; referencing it raises. Branch explicitly.
  IF (TG_OP = 'DELETE') THEN
    v_activity_guid := OLD.activity_guid;
  ELSE
    v_activity_guid := NEW.activity_guid;
  END IF;

  UPDATE wfprev.activity a
  SET is_spatial_added_ind = EXISTS (
        SELECT 1 FROM wfprev.activity_boundary ab
        WHERE ab.activity_guid = v_activity_guid)
  WHERE a.activity_guid = v_activity_guid
    -- no-op writes would churn revision history and generate empty audit rows
    AND a.is_spatial_added_ind IS DISTINCT FROM EXISTS (
        SELECT 1 FROM wfprev.activity_boundary ab
        WHERE ab.activity_guid = v_activity_guid);

  -- an UPDATE that moved a boundary between activities must correct both sides
  IF (TG_OP = 'UPDATE' AND OLD.activity_guid IS DISTINCT FROM NEW.activity_guid) THEN
    UPDATE wfprev.activity a
    SET is_spatial_added_ind = EXISTS (
          SELECT 1 FROM wfprev.activity_boundary ab
          WHERE ab.activity_guid = OLD.activity_guid)
    WHERE a.activity_guid = OLD.activity_guid
      AND a.is_spatial_added_ind IS DISTINCT FROM EXISTS (
          SELECT 1 FROM wfprev.activity_boundary ab
          WHERE ab.activity_guid = OLD.activity_guid);
  END IF;

  RETURN NULL;  -- AFTER trigger; the return value is ignored
END;
$$ LANGUAGE plpgsql;


CREATE OR REPLACE TRIGGER trigger_sync_activity_spatial_ind
AFTER INSERT OR UPDATE OR DELETE ON wfprev.activity_boundary
FOR EACH ROW EXECUTE PROCEDURE wfprev.fnc_sync_activity_spatial_ind();


COMMENT ON FUNCTION wfprev.fnc_sync_activity_spatial_ind()
  IS 'WFPREV-1223: keeps wfprev.activity.is_spatial_added_ind in sync with the presence of rows in wfprev.activity_boundary. The flag is derived data - do not set it from application code.';
