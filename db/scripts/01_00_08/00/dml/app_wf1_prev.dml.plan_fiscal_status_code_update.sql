UPDATE wfprev.plan_fiscal_status_code
SET plan_fiscal_status_code = 'PREPARED',
    description = 'Prepared'
WHERE plan_fiscal_status_code = 'PLANNED';
