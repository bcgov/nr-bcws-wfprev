CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE OR REPLACE FUNCTION wfprev.uuid_namespace() RETURNS uuid
LANGUAGE sql IMMUTABLE AS $$
  SELECT '9a1f9b6d-2b9b-4c35-9f7f-7a2b9d7c1a11'::uuid
$$;
COMMENT ON FUNCTION wfprev.uuid_namespace() IS
  'Fixed namespace UUID for deterministic v5 keys in WFPREV report views';
