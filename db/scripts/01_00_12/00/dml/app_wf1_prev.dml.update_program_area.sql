UPDATE wfprev.program_area
SET program_area_name = 'BC Parks (BCP)',
    update_user='WFPREV-281',
    update_date=current_date
WHERE program_area_name = 'PC Parks (BCP)';