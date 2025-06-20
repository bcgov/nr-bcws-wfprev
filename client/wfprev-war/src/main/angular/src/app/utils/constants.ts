export const Messages = {
  requiredField: 'This field is required.',
  maxLengthExceeded: 'Exceeds number of allowed characters.',
  invalidEmail: 'Please enter a valid email address.',
  positiveNumber: 'Please enter positive amounts',
  projectCreatedSuccess: 'Project Created Successfully',
  projectCreatedFailure: 'Create Project Failed',
  projectUpdatedSuccess: 'Project Updated Successfully',
  projectUpdatedFailure: 'Project Update Failed',
  confirmCancel: 'Are you sure you want to cancel?',
  projectFiscalCreatedSuccess: 'Project Fiscal Created Successfully',
  projectFiscalCreatedFailure: 'Create Project Fiscal Failed',
  projectFiscalUpdatedSuccess: 'Project Fiscal Updated Successfully',
  projectFiscalUpdatedFailure: 'Project Fiscal Update Failed',
  projectFiscalDeletedSuccess: 'Project Fiscal Deleted Successfully',
  projectFiscalDeletedFailure: 'Project Fiscal Deleted Failed',
  activityCreatedSuccess: 'Activity Created Successfully',
  activityUpdatedSuccess: 'Activity Updated Successfully',
  activityDeletedSuccess: 'Activity Deleted Successfully',
  activityCreatedFailure: 'Activity Created Failed',
  activityUpdatedFailure: 'Activity Update Failed',
  activityDeletedFailure: 'Activity Deleted Failed',
  fileUploadSuccess: 'File Uploaded Successfully',
  fileUploadFailure: 'File Uploaded Failed',
};

export const CodeTableKeys = {
  ACTIVITY_CATEGORY_CODE: 'activityCategoryCode',
  PLAN_FISCAL_STATUS_CODE: 'planFiscalStatusCode',
  PROPOSAL_TYPE_CODE: 'proposalTypeCode',
  PROJECT_TYPE_CODE: 'projectTypeCode',
  PROGRAM_AREA_GUID: 'programAreaGuid',
  FOREST_REGION_ORG_UNIT_ID: 'forestRegionOrgUnitId',
  FOREST_DISTRICT_ORG_UNIT_ID: 'forestDistrictOrgUnitId',
  BC_PARKS_REGION_ORG_UNIT_ID: 'bcParksRegionOrgUnitId',
  BC_PARKS_SECTION_ORG_UNIT_ID: 'bcParksSectionOrgUnitId',
  FIRE_CENTRE_ID: 'fireCentreId',
  PRIMARY_OBJECTIVE_TYPE_CODE: 'primaryObjectiveTypeCode',
  SECONDARY_OBJECTIVE_TYPE_CODE: 'secondaryObjectiveTypeCode',
  PROGRAM_AREA_CODE: 'programAreaCode'
};

export const PlanFiscalStatus = {
  DRAFT: 'DRAFT',
  PROPOSED: 'PROPOSED',
  IN_PROG: 'IN_PROG',
  COMPLETE: 'COMPLETE',
  ABANDONED: 'ABANDONED',
  PREPARED: 'PREPARED'
};

export const FiscalYearColors: Record<'past' | 'present' | 'future', string> = {
  past: '#7570B3',
  present: '#1B9E77',
  future: '#E7298A'
};

export const MapColors = {
  PROJECT_BOUNDARY: '#3f3f3f',
};

export const EnvironmentIndicators: { [key: string]: string } = {
  LOCAL: 'WFLOCAL',
  DEV: 'WFDEV',
  TEST: 'WFTST'
};