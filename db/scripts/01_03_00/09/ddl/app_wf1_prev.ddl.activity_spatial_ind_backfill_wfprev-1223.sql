-- WFPREV-1223
-- One-time backfill to accompany trigger_sync_activity_spatial_ind.
--
-- Idempotent and non-destructive. It only ever sets is_spatial_added_ind to what
-- wfprev.activity_boundary already says, and touches no other table. Re-running it is a
-- no-op. Rows already correct are skipped by the WHERE clause, so this does not generate
-- audit rows or revision churn for activities that were never affected.

UPDATE wfprev.activity a
SET is_spatial_added_ind = EXISTS (
      SELECT 1 FROM wfprev.activity_boundary ab
      WHERE ab.activity_guid = a.activity_guid)
WHERE a.is_spatial_added_ind IS DISTINCT FROM EXISTS (
      SELECT 1 FROM wfprev.activity_boundary ab
      WHERE ab.activity_guid = a.activity_guid);
