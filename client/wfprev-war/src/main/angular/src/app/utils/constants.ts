export const Messages = {
  requiredField: 'This field is required.',
  maxLengthExceeded: 'Maximum character limit has been reached.',
  invalidEmail: 'Please enter a valid email address.',
  positiveNumber: 'Please enter positive amounts',
  projectCreatedSuccess: 'Project Created Successfully',
  projectCreatedFailure: 'Create Project Failed',
  projectUpdatedSuccess: 'Project Updated Successfully',
  projectUpdatedFailure: 'Project Update Failed',
  projectNameDuplicate: 'Project Name already exists',
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
  activityWithAttachmentDeleteFailure: 'Delete Failed. Remove Uploaded Files to Delete Activity.',
  fileUploadSuccess: 'File Uploaded Successfully',
  fileUploadFailure: 'File Uploaded Failed',
  fileUploadInProgress: 'File upload in progress, Please wait',
  evaluationCriteriaCreatedSuccess: 'Evaluation Criteria Created Successfully.',
  evaluationCriteriaCreatedFailure: 'Failed to Create Evaluation Criteria.',
  evaluationCriteriaUpdatedSuccess: 'Evaluation Criteria Updated Successfully.',
  evaluationCriteriaUpdatedFailure: 'Failed to Update Evaluation Criteria.',
  fileDownloadFailure: 'Error: File download unsuccessful',
  fileDownloadSuccess: 'File downloaded successfully.',
  fileDownloadInProgress: 'File download in progress. Please wait',
  fileDownloadRequiresFilter: 'Filters must be applied to download file.',
  fiscalActivityDeletedFailure: 'Delete Failed. Remove Activities to Delete Fiscal Activity',
  projectTypeCannotUpdateAfterEndorsed: 'Project Type cannot be changed after any Fiscal Activity has been Endorsed and Approved'
};

export const CodeTableKeys = {
  ACTIVITY_CATEGORY_CODE: 'activityCategoryCode',
  PLAN_FISCAL_STATUS_CODE: 'planFiscalStatusCode',
  PROPOSAL_TYPE_CODE: 'proposalTypeCode',
  PROJECT_TYPE_CODE: 'projectTypeCode',
  PROGRAM_AREA_GUID: 'programAreaGuid',
  PROGRAM_AREA: 'programArea',
  PROGRAM_AREA_CODE: 'programAreaCode',
  BUSINESS_AREAS: 'businessAreas',
  FOREST_REGION_CODE: 'forestRegionCode',
  FOREST_REGION_ORG_UNIT_ID: 'forestRegionOrgUnitId',
  FOREST_REGIONS: 'forestRegions',
  FOREST_DISTRICT_CODE: 'forestDistrictCode',
  FOREST_DISTRICT_ORG_UNIT_ID: 'forestDistrictOrgUnitId',
  FOREST_DISTRICTS: 'forestDistricts',
  BC_PARKS_REGION_CODE: 'bcParksRegionCode',
  BC_PARKS_REGION_ORG_UNIT_ID: 'bcParksRegionOrgUnitId',
  BC_PARKS_REGIONS: 'bcParksRegions',
  BC_PARKS_SECTION_CODE: 'bcParksSectionCode',
  BC_PARKS_SECTION_ORG_UNIT_ID: 'bcParksSectionOrgUnitId',
  BC_PARKS_SECTIONS: 'bcParksSections',
  OBJECTIVE_TYPE_CODE: 'objectiveTypeCode',
  PRIMARY_OBJECTIVE_TYPE_CODE: 'primaryObjectiveTypeCode',
  SECONDARY_OBJECTIVE_TYPE_CODE: 'secondaryObjectiveTypeCode',
  WILDFIRE_ORG_UNIT: 'wildfireOrgUnit',
  WILDFIRE_ORG_UNIT_ID: 'wildfireOrgUnitId',
};

export const CodeTableNames = {
  ACTIVITY_CATEGORY_CODE: 'activityCategoryCodes',
  PLAN_FISCAL_STATUS_CODE: 'planFiscalStatusCodes',
  PROPOSAL_TYPE_CODE: 'proposalTypeCodes',
  PROJECT_TYPE_CODE: 'projectTypeCodes',
  PROGRAM_AREA_CODE: 'programAreaCodes',
  FOREST_REGION_CODE: 'forestRegionCodes',
  FOREST_DISTRICT_CODE: 'forestDistrictCodes',
  BC_PARKS_REGION_CODE: 'bcParksRegionCodes',
  BC_PARKS_SECTION_CODE: 'bcParksSectionCodes',
  OBJECTIVE_TYPE_CODE: 'objectiveTypeCodes',
  WILDFIRE_ORG_UNIT: 'wildfireOrgUnits'
}

export const ProjectTypeCodes = {
  FUEL_MANAGEMENT: 'FUEL_MGMT',
  CULTURAL_PRESCRIBED_FIRE: 'CULT_RX_FR'
}

export const WildfireOrgUnitTypeCodes = {
  HEADQUARTERS: 'HDQ',
  FIRE_CENTRE: 'FRC',
  ZONE: 'ZNE'
}

export const ObjectiveTypeCodes = {
  WRR: 'WRR',
  CRIT_INFRA: 'CRIT_INFRA',
  ECO_REST: 'ECO_REST',
  EGRSS_EVAC: 'EGRSS_EVAC',
  FOR_HEALTH: 'FOR_HEALTH',
  HAZ_ABATE: 'HAZ_ABATE',
  RNG_HAB_MG: 'RNG_HAB_MG',
  RECONCIL: 'RECONCIL',
  WLD_HAB_MG: 'WLD_HAB_MG',
  OTHER: 'OTHER'
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
  WFINT: 'WFINT',
  WFDLV: 'WFDLV',
  TEST: 'WFTST',
  WFTST: 'WFTST'
};

export const FiscalActionLabels = {
  SUBMIT: 'Submit',
  REVERT_TO_DRAFT: 'Revert to Draft',
  SET_IN_PROGRESS: 'Set to In Progress',
  SET_COMPLETE: 'Set to Complete',
  CANCEL_FISCAL: 'Cancel Fiscal',
  DELETE: 'Delete Fiscal Activity',
};

export const FiscalStatuses = {
  DRAFT: 'DRAFT',
  PROPOSED: 'PROPOSED',
  PREPARED: 'PREPARED',
  IN_PROGRESS: 'IN_PROG',
  COMPLETE: 'COMPLETE',
  CANCELLED: 'CANCELLED',
};

export const CUSTOM_DATE_FORMATS = {
  parse: { dateInput: 'YYYY-MM-DD' },
  display: {
    dateInput: 'YYYY-MM-DD',
    monthYearLabel: 'YYYY MMM',
    dateA11yLabel: 'LL',
    monthYearA11yLabel: 'YYYY MMMM',
  },
};
export const ModalTitles = {
  CONFIRM_CANCEL_TITLE: 'Confirm Cancel',
  CONFIRM_UNSAVE_TITLE: 'Unsaved Changes',
  DELETE_ATTACHMENT_TITLE: 'Delete Attachment',
  DELETE_FISCAL_YEAR_TITLE: 'Delete Fiscal Activity',
  DELETE_ACTIVITY_TITLE: 'Delete Activity',
  DUPLICATE_FOUND_TITLE: 'Duplicate Found',
  CHANGE_PROJECT_TYPE: 'Change Project Type'
};

export const ModalMessages = {
  CONFIRM_CANCEL_MESSAGE: 'Are you sure you want to cancel? This information will not be saved.',
  DUPLICATE_FOUND_MESSAGE: 'This Project already exists: ',
  CONFIRM_UNSAVE_MESSAGE: 'Are you sure you want to leave this page? The changes you made will not be saved.',
  DELETE_ATTACHMENT_MESSAGE: 'Are you sure you want to delete this file? ',
  CONFIRM_DELETE_EVALUACTION_CRITERIA: 'Are you sure you want to change the Project Type? This action cannot be reversed and will immediately delete any existing Evaluation Criteria values.'
}

export const EndorsementCode = {
  NOT_ENDORS: 'NOT_ENDORS',
  ENDORSED: 'ENDORSED',
  FOLLOW_UP: 'FOLLOW_UP'
}

export const ProjectTypes = {
  FUEL_MANAGEMENT: 'FUEL_MGMT',
  CULTURAL_PRESCRIBED_FIRE: 'CULT_RX_FR'
};

export const EvaluationCriteriaSectionCodes = {
  COARSE_FILTER: 'COARSE_FLT',
  MEDIUM_FILTER: 'MEDIUM_FLT',
  FINE_FILTER: 'FINE_FLT',
  RISK_CLASS_LOCATION: 'RCL',
  BURN_DEVELOPMENT_FEASIBILITY: 'BDF',
  COLLECTIVE_IMPACT: 'COLL_IMP'
};

export const DownloadTypes = {
  EXCEL: 'xlsx',
  CSV: 'csv'
};

export const DownloadFileExtensions = {
  EXCEL: 'xlsx',
  CSV: 'zip',
};

export const NumericLimits = {
  MAX_NUMBER: 99999999999
};

export const BC_BOUNDS: L.LatLngBoundsLiteral = [
  [47.60393449638617, -139.1782824917356], // south, west
  [60.593907018763396, -110.35337939457779] // north, east
];