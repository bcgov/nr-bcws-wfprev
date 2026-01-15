-- WFPREV-836: Add Silviculture Method Code

INSERT INTO wfprev.silviculture_method_code (silviculture_method_code, description, display_order, effective_date, expiry_date, create_user, create_date, update_user, update_date)
VALUES 
('SLBD', 'Stand Level Biodiversity', 90, CURRENT_DATE, '9999-12-31', 'WFPREV-836', CURRENT_DATE, 'WFPREV-836', CURRENT_DATE);
