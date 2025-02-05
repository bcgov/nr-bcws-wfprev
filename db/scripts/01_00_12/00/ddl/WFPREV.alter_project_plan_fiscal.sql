ALTER TABLE wfprev.project_plan_fiscal 
ADD COLUMN funding_source_guid UUID NULL,
ADD CONSTRAINT prjpfy_fsrc_fk 
FOREIGN KEY (funding_source_guid) 
REFERENCES wfprev.funding_source (funding_source_guid) 
ON DELETE NO ACTION ON UPDATE NO ACTION;