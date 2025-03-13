CREATE OR REPLACE FUNCTION wfprev.fnc_audit_project_boundary() RETURNS TRIGGER AS $$
DECLARE 
  v_audit_action VARCHAR(10); 
BEGIN 
  v_audit_action := TG_OP; 
  IF ((TG_OP = 'INSERT') OR (TG_OP = 'UPDATE')) THEN 
    INSERT INTO wfprev.project_boundary_audit( 
      audit_table_sequence, 
      audit_action_code, 
      project_boundary_guid,
      project_guid,
      system_start_timestamp,
      system_end_timestamp,
      mapping_label,
      collection_date,
      collection_method,
      collector_name,
      boundary_size_ha,
      boundary_comment,
      location_geometry,
      boundary_geometry,
      revision_count,
      create_user,
      create_date,
      update_user,
      update_date 
    ) 
    VALUES ( 
      nextval('wfprev.prev_audit_table_seq'),
      v_audit_action, 
      NEW.project_boundary_guid,
      NEW.project_guid,
      NEW.system_start_timestamp,
      NEW.system_end_timestamp,
      NEW.mapping_label,
      NEW.collection_date,
      NEW.collection_method,
      NEW.collector_name,
      NEW.boundary_size_ha,
      NEW.boundary_comment,
      NEW.location_geometry,
      NEW.boundary_geometry,
      NEW.revision_count,
      NEW.create_user,
      NEW.create_date,
      NEW.update_user,
      NEW.update_date 
    ); 
    RETURN NEW; 

  ELSIF (TG_OP = 'DELETE') THEN 
    INSERT INTO wfprev.project_boundary_audit( 
      audit_table_sequence, 
      audit_action_code, 
      project_boundary_guid,
      project_guid,
      system_start_timestamp,
      system_end_timestamp,
      mapping_label,
      collection_date,
      collection_method,
      collector_name,
      boundary_size_ha,
      boundary_comment,
      location_geometry,
      boundary_geometry,
      revision_count,
      create_user,
      create_date,
      update_user,
      update_date 
    ) 
    VALUES ( 
      nextval('wfprev.prev_audit_table_seq'),
      v_audit_action, 
      OLD.project_boundary_guid,
      OLD.project_guid,
      OLD.system_start_timestamp,
      OLD.system_end_timestamp,
      OLD.mapping_label,
      OLD.collection_date,
      OLD.collection_method,
      OLD.collector_name,
      OLD.boundary_size_ha,
      OLD.boundary_comment,
      OLD.location_geometry,
      OLD.boundary_geometry,
      OLD.revision_count,
      OLD.create_user,
      OLD.create_date,
      OLD.update_user,
      OLD.update_date 
    ); 
    RETURN OLD; 

  ELSE 
    RAISE WARNING '[wfprev.fnc_audit_project_boundary] - Other action occurred: %, at %',TG_OP,now(); 
    RETURN NULL; 
  END IF; 

EXCEPTION 
  WHEN data_exception THEN 
      RAISE WARNING '[wfprev.fnc_audit_project_boundary] - UDF ERROR [DATA EXCEPTION] - SQLSTATE: %, SQLERRM: %',SQLSTATE,SQLERRM; 
      RETURN NULL; 
  WHEN unique_violation THEN 
      RAISE WARNING '[wfprev.fnc_audit_project_boundary] - UDF ERROR [UNIQUE] - SQLSTATE: %, SQLERRM: %',SQLSTATE,SQLERRM; 
      RETURN NULL; 
  WHEN others THEN 
      RAISE WARNING '[wfprev.fnc_audit_project_boundary] - UDF ERROR [OTHER] - SQLSTATE: %, SQLERRM: %',SQLSTATE,SQLERRM; 
      RETURN NULL; 
END; 
$$ LANGUAGE plpgsql; 


CREATE OR REPLACE TRIGGER trigger_audit_project_boundary
AFTER INSERT OR UPDATE OR DELETE ON wfprev.project_boundary 
FOR EACH ROW EXECUTE PROCEDURE wfprev.fnc_audit_project_boundary();
