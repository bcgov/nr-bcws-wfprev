-- WFPREV-1307
-- Maintains wfprev.prevention_layer_change_log for GeoServer layer synchronization.
--
-- Whenever a project or one of its child entities (project_plan_fiscal, activity,
-- project_boundary, activity_boundary) is inserted, updated, or deleted, this trigger
-- traces the change back to the parent project_guid and upserts the project's row
-- with the current timestamp.
--
-- If an update moves a child entity between parents (e.g. reassigning a fiscal or activity),
-- both the new and old project GUIDs are updated.

CREATE OR REPLACE FUNCTION wfprev.fnc_prevention_layer_change_log() RETURNS TRIGGER AS $$
DECLARE
  v_project_guid UUID;
  v_old_project_guid UUID;
BEGIN
  IF TG_TABLE_NAME = 'project' THEN
    IF (TG_OP = 'DELETE') THEN
      v_project_guid := OLD.project_guid;
    ELSE
      v_project_guid := NEW.project_guid;
      IF (TG_OP = 'UPDATE' AND OLD.project_guid IS DISTINCT FROM NEW.project_guid) THEN
        v_old_project_guid := OLD.project_guid;
      END IF;
    END IF;

  ELSIF TG_TABLE_NAME = 'project_boundary' THEN
    IF (TG_OP = 'DELETE') THEN
      v_project_guid := OLD.project_guid;
    ELSE
      v_project_guid := NEW.project_guid;
      IF (TG_OP = 'UPDATE' AND OLD.project_guid IS DISTINCT FROM NEW.project_guid) THEN
        v_old_project_guid := OLD.project_guid;
      END IF;
    END IF;

  ELSIF TG_TABLE_NAME = 'project_plan_fiscal' THEN
    IF (TG_OP = 'DELETE') THEN
      v_project_guid := OLD.project_guid;
    ELSE
      v_project_guid := NEW.project_guid;
      IF (TG_OP = 'UPDATE' AND OLD.project_guid IS DISTINCT FROM NEW.project_guid) THEN
        v_old_project_guid := OLD.project_guid;
      END IF;
    END IF;

  ELSIF TG_TABLE_NAME = 'activity' THEN
    IF (TG_OP = 'DELETE') THEN
      SELECT ppf.project_guid INTO v_project_guid
      FROM wfprev.project_plan_fiscal ppf
      WHERE ppf.project_plan_fiscal_guid = OLD.project_plan_fiscal_guid;
    ELSE
      SELECT ppf.project_guid INTO v_project_guid
      FROM wfprev.project_plan_fiscal ppf
      WHERE ppf.project_plan_fiscal_guid = NEW.project_plan_fiscal_guid;

      IF (TG_OP = 'UPDATE' AND OLD.project_plan_fiscal_guid IS DISTINCT FROM NEW.project_plan_fiscal_guid) THEN
        SELECT ppf.project_guid INTO v_old_project_guid
        FROM wfprev.project_plan_fiscal ppf
        WHERE ppf.project_plan_fiscal_guid = OLD.project_plan_fiscal_guid;
      END IF;
    END IF;

  ELSIF TG_TABLE_NAME = 'activity_boundary' THEN
    IF (TG_OP = 'DELETE') THEN
      SELECT ppf.project_guid INTO v_project_guid
      FROM wfprev.activity a
      JOIN wfprev.project_plan_fiscal ppf ON ppf.project_plan_fiscal_guid = a.project_plan_fiscal_guid
      WHERE a.activity_guid = OLD.activity_guid;
    ELSE
      SELECT ppf.project_guid INTO v_project_guid
      FROM wfprev.activity a
      JOIN wfprev.project_plan_fiscal ppf ON ppf.project_plan_fiscal_guid = a.project_plan_fiscal_guid
      WHERE a.activity_guid = NEW.activity_guid;

      IF (TG_OP = 'UPDATE' AND OLD.activity_guid IS DISTINCT FROM NEW.activity_guid) THEN
        SELECT ppf.project_guid INTO v_old_project_guid
        FROM wfprev.activity a
        JOIN wfprev.project_plan_fiscal ppf ON ppf.project_plan_fiscal_guid = a.project_plan_fiscal_guid
        WHERE a.activity_guid = OLD.activity_guid;
      END IF;
    END IF;
  END IF;

  IF v_project_guid IS NOT NULL THEN
    INSERT INTO wfprev.prevention_layer_change_log (project_guid, last_updated)
    VALUES (v_project_guid, (CURRENT_TIMESTAMP AT TIME ZONE 'UTC'))
    ON CONFLICT (project_guid) DO UPDATE
    SET last_updated = (CURRENT_TIMESTAMP AT TIME ZONE 'UTC');
  END IF;

  IF v_old_project_guid IS NOT NULL THEN
    INSERT INTO wfprev.prevention_layer_change_log (project_guid, last_updated)
    VALUES (v_old_project_guid, (CURRENT_TIMESTAMP AT TIME ZONE 'UTC'))
    ON CONFLICT (project_guid) DO UPDATE
    SET last_updated = (CURRENT_TIMESTAMP AT TIME ZONE 'UTC');
  END IF;

  RETURN NULL;
END;
$$ LANGUAGE plpgsql;

COMMENT ON FUNCTION wfprev.fnc_prevention_layer_change_log()
  IS 'WFPREV-1307: records project last_updated timestamps in wfprev.prevention_layer_change_log on changes to project, fiscal, activity, or boundary tables.';

CREATE OR REPLACE TRIGGER trigger_prevention_layer_change_log_project
AFTER INSERT OR UPDATE OR DELETE ON wfprev.project
FOR EACH ROW EXECUTE FUNCTION wfprev.fnc_prevention_layer_change_log();

CREATE OR REPLACE TRIGGER trigger_prevention_layer_change_log_project_boundary
AFTER INSERT OR UPDATE OR DELETE ON wfprev.project_boundary
FOR EACH ROW EXECUTE FUNCTION wfprev.fnc_prevention_layer_change_log();

CREATE OR REPLACE TRIGGER trigger_prevention_layer_change_log_project_plan_fiscal
AFTER INSERT OR UPDATE OR DELETE ON wfprev.project_plan_fiscal
FOR EACH ROW EXECUTE FUNCTION wfprev.fnc_prevention_layer_change_log();

CREATE OR REPLACE TRIGGER trigger_prevention_layer_change_log_activity
AFTER INSERT OR UPDATE OR DELETE ON wfprev.activity
FOR EACH ROW EXECUTE FUNCTION wfprev.fnc_prevention_layer_change_log();

CREATE OR REPLACE TRIGGER trigger_prevention_layer_change_log_activity_boundary
AFTER INSERT OR UPDATE OR DELETE ON wfprev.activity_boundary
FOR EACH ROW EXECUTE FUNCTION wfprev.fnc_prevention_layer_change_log();

