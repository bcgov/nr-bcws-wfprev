UPDATE wfprev.silviculture_base_code SET
   description = 'Survey',
   revision_count = revision_count+1,
   update_user='WFPREV-342',
   update_date = current_date
WHERE silviculture_base_code = 'SU'; 
