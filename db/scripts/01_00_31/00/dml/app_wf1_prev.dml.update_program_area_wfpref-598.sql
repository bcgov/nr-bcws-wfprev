UPDATE wfprev.program_area SET  program_area_name = 'BC Parks', 
     revision_count = revision_count+1,
     update_user = 'WFPREV-598',
     update_date = CURRENT_TIMESTAMP
   WHERE program_area_name = 'BC Parks (BCP)';

UPDATE wfprev.program_area SET  program_area_name = 'Ministry of Water, Land and Resource Stewardship - KBR', 
     revision_count = revision_count+1,
     update_user = 'WFPREV-598',
     update_date = CURRENT_TIMESTAMP
   WHERE program_area_name = 'Ministry of Water, Land and Resource Stewardship (WLRS)';

UPDATE wfprev.program_area SET  program_area_name = 'Mountain Resorts Branch', 
     revision_count = revision_count+1,
     update_user = 'WFPREV-598',
     update_date = CURRENT_TIMESTAMP
   WHERE program_area_name = 'Mountain Resorts Branch (MRB)';

