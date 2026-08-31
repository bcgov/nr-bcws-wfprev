export const Messages = {
  activityCreatedFailure: 'Activity Created Failed',
  activityCreatedSuccess: 'Activity Created Successfully',
  activityDeletedFailure: 'Activity Deleted Failed',
  activityDeletedSuccess: 'Activity Deleted Successfully',
  activityUpdatedFailure: 'Activity Update Failed',
  activityUpdatedSuccess: 'Activity Updated Successfully',
  activityWithAttachmentDeleteFailure: 'Delete Failed. Remove Uploaded Files to Delete Activity.',
  boundaryRollbackFailed: 'The file could not be saved and its spatial could not be cleaned up. Please delete the leftover spatial from the file list.',
  confirmCancel: 'Are you sure you want to cancel?',
  evaluationCriteriaCreatedFailure: 'Failed to Create Evaluation Criteria.',
  evaluationCriteriaCreatedSuccess: 'Evaluation Criteria Created Successfully.',
  evaluationCriteriaUpdatedFailure: 'Failed to Update Evaluation Criteria.',
  evaluationCriteriaUpdatedSuccess: 'Evaluation Criteria Updated Successfully.',
  fileDeleteInProgress: 'File deletion in progress, Please wait',
  fileDownloadFailure: 'Error: File download unsuccessful',
  fileDownloadInProgress: 'File download in progress. Please wait',
  fileDownloadRequiresFilter: 'Filters must be applied to download file.',
  fileDownloadSuccess: 'File downloaded successfully.',
  fileUploadFailure: 'File Uploaded Failed',
  fileUploadInProgress: 'File upload in progress, Please wait',
  fileUploadSuccess: 'File Uploaded Successfully',
  fiscalActivityDeletedFailure: 'Delete Failed. Remove Activities to Delete Fiscal Activity',
  invalidEmail: 'Please enter a valid email address.',
  maxLengthExceeded: 'Maximum character limit has been reached.',
  orphanBoundaryDeleted: 'Spatial has been deleted successfully.',
  orphanBoundaryDescription: 'Spatial without an attached file associated.',
  orphanBoundaryDownloadTooltip: 'The original uploaded file is no longer linked to this polygon.',
  positiveNumber: 'Please enter positive amounts',
  projectCreatedFailure: 'Create Project Failed',
  projectCreatedSuccess: 'Project Created Successfully',
  projectFiscalCreatedFailure: 'Create Project Fiscal Failed',
  projectFiscalCreatedSuccess: 'Project Fiscal Created Successfully',
  projectFiscalDeletedFailure: 'Project Fiscal Deleted Failed',
  projectFiscalDeletedSuccess: 'Project Fiscal Deleted Successfully',
  projectFiscalUpdatedFailure: 'Project Fiscal Update Failed',
  projectFiscalUpdatedSuccess: 'Project Fiscal Updated Successfully',
  projectNameDuplicate: 'Project Name already exists',
  projectTypeCannotUpdateAfterEndorsed: 'Project Type cannot be changed after any Fiscal Activity has been Endorsed and Approved',
  projectUpdatedFailure: 'Project Update Failed',
  projectUpdatedSuccess: 'Project Updated Successfully',
  requiredField: 'This field is required.'
};

export const CodeTableKeys = {
  ACTIVITY_CATEGORY_CODE: 'activityCategoryCode',
  BC_PARKS_REGION_CODE: 'bcParksRegionCode',
  BC_PARKS_REGION_ORG_UNIT_ID: 'bcParksRegionOrgUnitId',
  BC_PARKS_REGIONS: 'bcParksRegions',
  BC_PARKS_SECTION_CODE: 'bcParksSectionCode',
  BC_PARKS_SECTION_ORG_UNIT_ID: 'bcParksSectionOrgUnitId',
  BC_PARKS_SECTIONS: 'bcParksSections',
  BUSINESS_AREAS: 'businessAreas',
  FOREST_DISTRICT_CODE: 'forestDistrictCode',
  FOREST_DISTRICT_ORG_UNIT_ID: 'forestDistrictOrgUnitId',
  FOREST_DISTRICTS: 'forestDistricts',
  FOREST_REGION_CODE: 'forestRegionCode',
  FOREST_REGION_ORG_UNIT_ID: 'forestRegionOrgUnitId',
  FOREST_REGIONS: 'forestRegions',
  OBJECTIVE_TYPE_CODE: 'objectiveTypeCode',
  PLAN_FISCAL_STATUS_CODE: 'planFiscalStatusCode',
  PRIMARY_OBJECTIVE_TYPE_CODE: 'primaryObjectiveTypeCode',
  PROGRAM_AREA: 'programArea',
  PROGRAM_AREA_CODE: 'programAreaCode',
  PROGRAM_AREA_GUID: 'programAreaGuid',
  PROJECT_TYPE_CODE: 'projectTypeCode',
  PROPOSAL_TYPE_CODE: 'proposalTypeCode',
  REPORTING_PERIOD_CODE: 'reportingPeriodCode',
  SECONDARY_OBJECTIVE_TYPE_CODE: 'secondaryObjectiveTypeCode',
  WILDFIRE_ORG_UNIT: 'wildfireOrgUnit',
  WILDFIRE_ORG_UNIT_ID: 'wildfireOrgUnitId',
};

export const CodeTableNames = {
  ACTIVITY_CATEGORY_CODE: 'activityCategoryCodes',
  BC_PARKS_REGION_CODE: 'bcParksRegionCodes',
  BC_PARKS_SECTION_CODE: 'bcParksSectionCodes',
  CONTRACT_PHASE_CODE: 'contractPhaseCodes',
  FOREST_DISTRICT_CODE: 'forestDistrictCodes',
  FOREST_REGION_CODE: 'forestRegionCodes',
  FUNDING_SOURCE_CODE: 'fundingSourceCodes',
  OBJECTIVE_TYPE_CODE: 'objectiveTypeCodes',
  PLAN_FISCAL_STATUS_CODE: 'planFiscalStatusCodes',
  PROGRAM_AREA_CODE: 'programAreaCodes',
  PROGRESS_STATUS_CODE: 'progressStatusCodes',
  PROJECT_TYPE_CODE: 'projectTypeCodes',
  PROPOSAL_TYPE_CODE: 'proposalTypeCodes',
  REPORTING_PERIOD_CODE: 'reportingPeriodCodes',
  WILDFIRE_ORG_UNIT: 'wildfireOrgUnits',
}

export const ProjectTypeCodes = {
  CULTURAL_PRESCRIBED_FIRE: 'CULT_RX_FR',
  FUEL_MANAGEMENT: 'FUEL_MGMT'
}

export const WildfireOrgUnitTypeCodes = {
  FIRE_CENTRE: 'FRC',
  HEADQUARTERS: 'HDQ',
  ZONE: 'ZNE'
}

export const ObjectiveTypeCodes = {
  CRIT_INFRA: 'CRIT_INFRA',
  ECO_REST: 'ECO_REST',
  EGRSS_EVAC: 'EGRSS_EVAC',
  FOR_HEALTH: 'FOR_HEALTH',
  HAZ_ABATE: 'HAZ_ABATE',
  OTHER: 'OTHER',
  RECONCIL: 'RECONCIL',
  RNG_HAB_MG: 'RNG_HAB_MG',
  SILV_TREAT: 'SILV_TREAT',
  WLD_HAB_MG: 'WLD_HAB_MG',
  WRR: 'WRR'
};

export const PlanFiscalStatus = {
  CANCELLED: 'CANCELLED',
  COMPLETE: 'COMPLETE',
  DRAFT: 'DRAFT',
  IN_PROG: 'IN_PROG',
  PREPARED: 'PREPARED',
  PROPOSED: 'PROPOSED'
};

export const FiscalYearColors: Record<'past' | 'present' | 'future', string> = {
  future: '#E7298A',
  past: '#7570B3',
  present: '#1B9E77'
};

export const MapColors = {
  PROJECT_BOUNDARY: '#3f3f3f',
};

export const EnvironmentIndicators: { [key: string]: string } = {
  DEV: 'WFDEV',
  LOCAL: 'WFLOCAL',
  TEST: 'WFTST',
  WFDLV: 'WFDLV',
  WFINT: 'WFINT',
  WFTST: 'WFTST'
};

export const FiscalActionLabels = {
  CANCEL_FISCAL: 'Cancel Fiscal',
  DELETE: 'Delete Fiscal Activity',
  REVERT_TO_DRAFT: 'Revert to Draft',
  SET_IN_PROG: 'Set to In Progress',
  SUBMIT: 'Submit',
  YEAR_END_UPDATE: 'Year End Update'
};

export const FiscalActions = {
  DELETE: 'DELETE',
  YEAR_END_CANCEL: 'YEAR_END_CANCEL',
  YEAR_END_UPDATE: 'YEAR_END_UPDATE'
};

export const FiscalStatuses = {
  CANCELLED: 'CANCELLED',
  COMPLETE: 'COMPLETE',
  DRAFT: 'DRAFT',
  IN_PROGRESS: 'IN_PROG',
  PREPARED: 'PREPARED',
  PROPOSED: 'PROPOSED',
};

export const StatusManagementStatuses = {
  CANCELLED: 'CANCELLED',
  COMPLETE: 'COMPLETE',
};

export const CUSTOM_DATE_FORMATS = {
  display: {
    dateInput: 'YYYY-MM-DD',
    monthYearLabel: 'YYYY MMM',
    dateA11yLabel: 'LL',
    monthYearA11yLabel: 'YYYY MMMM',
  },
  parse: { dateInput: 'YYYY-MM-DD' },
};
export const ModalTitles = {
  CHANGE_PROJECT_TYPE: 'Change Project Type',
  CONFIRM_CANCEL_TITLE: 'Confirm Cancel',
  CONFIRM_UNSAVE_TITLE: 'Unsaved Changes',
  DELETE_ACTIVITY_TITLE: 'Delete Activity',
  DELETE_ATTACHMENT_TITLE: 'Delete Attachment',
  DELETE_FISCAL_YEAR_TITLE: 'Delete Fiscal Activity',
  DUPLICATE_FOUND_TITLE: 'Duplicate Found'
};

export const ModalMessages = {
  CONFIRM_CANCEL_MESSAGE: 'Are you sure you want to cancel? This information will not be saved.',
  CONFIRM_CANCEL_PERFORMANCE_UPDATE_MODAL_WINDOW_MESSAGE: 'Are you sure you want to leave this page? The changes you made will not be saved.',
  CONFIRM_DELETE_EVALUACTION_CRITERIA: 'Are you sure you want to change the Project Type? This action cannot be reversed and will immediately delete any existing Evaluation Criteria values.',
  CONFIRM_UNSAVE_MESSAGE: 'Are you sure you want to leave this page? The changes you made will not be saved.',
  DELETE_ATTACHMENT_MESSAGE: 'Are you sure you want to delete this file? ',
  DUPLICATE_FOUND_MESSAGE: 'This Project already exists: '
}

export const EndorsementCode = {
  ENDORSED: 'ENDORSED',
  FOLLOW_UP: 'FOLLOW_UP',
  NOT_ENDORS: 'NOT_ENDORS'
}

export const ProjectTypes = {
  CULTURAL_PRESCRIBED_FIRE: 'CULT_RX_FR',
  FUEL_MANAGEMENT: 'FUEL_MGMT'
};

export const EvaluationCriteriaSectionCodes = {
  BURN_DEVELOPMENT_FEASIBILITY: 'BDF',
  COARSE_FILTER: 'COARSE_FLT',
  COLLECTIVE_IMPACT: 'COLL_IMP',
  FINE_FILTER: 'FINE_FLT',
  MEDIUM_FILTER: 'MEDIUM_FLT',
  RISK_CLASS_LOCATION: 'RCL'
};

export const DownloadTypes = {
  CSV: 'csv',
  EXCEL: 'xlsx'
};

export const DownloadFileExtensions = {
  CSV: 'zip',
  EXCEL: 'xlsx',
};

export const NumericLimits = {
  MAX_NUMBER: 99999999999
};

export const BC_BOUNDS: L.LatLngBoundsLiteral = [
  [47.60393449638617, -139.1782824917356], // south, west
  [60.593907018763396, -110.35337939457779] // north, east
];