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
  evaluationCriteriaCreatedSuccess: 'Evaluation Criteria Created Successfully.',
  evaluationCriteriaCreatedFailure: 'Failed to Create Evaluation Criteria.',
  evaluationCriteriaUpdatedSuccess: 'Evaluation Criteria Updated Successfully.',
  evaluationCriteriaUpdatedFailure: 'Failed to Update Evaluation Criteria.',
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
  CANCELLED: 'CANCELLED',
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

export const FiscalActionLabels = {
  SUBMIT: 'Submit',
  REVERT_TO_DRAFT: 'Revert to Draft',
  SET_IN_PROGRESS: 'Set to In Progress',
  SET_COMPLETE: 'Set to Complete',
  CANCEL_FISCAL: 'Cancel Fiscal',
  DELETE: 'Delete Fiscal Year',
};

export const FiscalStatuses = {
  DRAFT: 'DRAFT',
  PROPOSED: 'PROPOSED',
  PREPARED: 'PREPARED',
  IN_PROGRESS: 'IN_PROG',
  COMPLETE: 'COMPLETE',
  CANCELLED: 'CANCELLED',
};

export const ModalTitles = {
  CONFIRM_CANCEL_TITLE: 'Confirm Cancel',
  CONFIRM_UNSAVE_TITLE: 'Confirm Unsave',
  DELETE_ATTACHMENT_TITLE: 'Delete Attachment',
  DELETE_FISCAL_YEAR_TITLE: 'Delete Fiscal Year',
  DELETE_ACTIVITY_TITLE: 'Delete Activity',
  DUPLICATE_FOUND_TITLE: 'Duplicate Found'
};

export const ModalMessages = {
  CONFIRM_CANCEL_MESSAGE: 'Are you sure you want to cancel? This information will not be saved.',
  DUPLICATE_FOUND_MESSAGE: 'This Project already exists: ',
  CONFIRM_UNSAVE_MESSAGE: 'Are you sure you want to leave this page? The changes you made will not be saved.',
  DELETE_ATTACHMENT_MESSAGE: 'Are you sure you want to delete this file? ',
}
