import { Position } from 'geojson';

export interface Project {
  bcParksRegionOrgUnitId: number;
  bcParksSectionOrgUnitId: number;
  closestCommunityName: string;
  createDate?: string;
  createUser?: string;
  fireCentreOrgUnitId: number;
  forestAreaCode?: {
    forestAreaCode: string;
  };
  forestDistrictOrgUnitId: number;
  forestRegionOrgUnitId: number;
  generalScopeCode?: {
    generalScopeCode: string;
  };
  isMultiFiscalYearProj: boolean;
  programAreaGuid: string;
  projectDescription: string;
  projectGuid: string;
  projectLead: string;
  projectLeadEmailAddress: string;
  projectName: string;
  projectNumber: number;
  projectTypeCode?: {
    projectTypeCode?: string;
  };
  revisionCount?: number;
  siteUnitName: string;
  totalActualAmount: number;
  totalAllocatedAmount: number;
  totalFundingRequestAmount: number;
  totalPlannedCostPerHectare: number;
  totalPlannedProjectSizeHa: number;
  updateDate?: string;
  updateUser?: string;
  latitude?: number;
  longitude?: number;
  primaryObjectiveTypeCode?: {
    objectiveTypeCode: string;
  };
  secondaryObjectiveTypeCode?: {
    objectiveTypeCode: string;
  }
  secondaryObjectiveRationale?: string;
  projectFiscals?: ProjectFiscalExtended[];
  resultsProjectCode?: string;
  lastUpdatedTimestamp?: string;
}

export interface ProjectFiscal {
  projectGuid: string;
  activityCategoryCode: string;
  fiscalYear: number;
  projectPlanStatusCode: string;
  planFiscalStatusCode: PlanFiscalStatusCodeModel;
  projectFiscalName: string;
  isApprovedInd: boolean;
  isDelayedInd: boolean;
  totalCostEstimateAmount: number;
  fiscalPlannedProjectSizeHa: number;
  fiscalPlannedCostPerHaAmt: number;
  fiscalReportedSpendAmount: number;
  fiscalActualAmount: number;
  fiscalActualCostPerHaAmt: number;
  firstNationsDelivPartInd: boolean;
  firstNationsEngagementInd: boolean;
  projectPlanFiscalGuid?: string;
  projectFiscalDescription?: string;
  businessAreaComment?: string;
  estimatedClwrrAllocAmount?: number;
  fiscalAncillaryFundAmount?: number;
  fiscalCompletedSizeHa?: number;
  firstNationsPartner?: string;
  resultsNumber?: string;
  resultsOpeningId?: string;
  resultsContactEmail?: string;
  submittedByName?: string;
  submittedByUserGuid?: string;
  submittedByUserUserid?: string;
  submissionTimestamp?: string;
  fiscalForecastAmount?: number;
  cfsProjectCode?: string;
  otherPartner?: string;
  proposalTypeCode?: string;
  ancillaryFundingProvider?: string;
  endorsementCode?: EndorsementCodeModel;
  endorsementComment?: string;
  endorsementTimestamp?: string;
  endorsementEvalTimestamp?: string;
  endorserName?: string;
  endorserUserGuid?: string;
  endorserUserUserid?: string;
  approverName?: string;
  approverUserGuid?: string;
  approverUserUserid?: string;
  approvedTimestamp?: string;
  accomplishmentsComment?: string;
  delayRationale?: string;
  abandonedRationale?: string;
  lastProgressUpdateTimestamp?: string;
  endorseApprUpdateName?: string;
  endorseApprUpdateUserGuid?: string;
  endorseApprUpdateUserid?: string;
  endorseApprUpdatedTimestamp?: string;
}


export interface ActivityModel {
  activityGuid?: string;
  projectPlanFiscalGuid?: string;
  activityStatusCode?: string;
  silvicultureBaseGuid?: string;
  silvicultureTechniqueGuid?: string;
  silvicultureMethodGuid?: string;
  riskRatingCode?: string;
  contractPhaseCode?: string;
  activityFundingSourceGuid?: string;
  activityName?: string;
  activityDescription?: string;
  activityStartDate?: string; // ISO 8601 format: "YYYY-MM-DDTHH:mm:ss.SSSZ"
  activityEndDate?: string; // ISO 8601 format
  plannedSpendAmount?: number;
  plannedTreatmentAreaHa?: number;
  reportedSpendAmount?: number;
  completedAreaHa?: number;
  isResultsReportableInd?: boolean;
  outstandingObligationsInd?: boolean;
  activityComment?: string;
  isSpatialAddedInd?: boolean;
  createDate?: string; // ISO 8601 format, e.g., "2025-02-06T23:56:53.663+00:00"
  lastUpdatedTimestamp?: string;
}

export interface FileAttachment {
  fileAttachmentGuid?: string;
  sourceObjectNameCode?: {
    sourceObjectNameCode: string;
  };
  sourceObjectUniqueId?: string;
  documentPath?: string;
  fileIdentifier?: string;
  wildfireYear?: number;
  attachmentContentTypeCode?: {
    attachmentContentTypeCode: string;
  };
  attachmentDescription?: string;
  attachmentReadOnlyInd?: boolean;
  createDate?: string;
  systemStartTimestamp?: string;
  uploadedByTimestamp?: string;
}

export interface ProjectBoundary {
  projectGuid?: string;
  systemStartTimestamp?: string;
  systemEndTimestamp?: string;
  mappingLabel?: string;
  collectionDate?: string;
  collectionMethod?: string;
  collectorName?: string;
  boundarySizeHa?: number;
  boundaryComment?: string;
  boundaryGeometry?: Geometry;
  locationGeometry?: [number, number];
}

export interface ActivityBoundary {
  activityGuid?: string;
  systemStartTimestamp?: string;
  systemEndTimestamp?: string;
  plannedSpendAmount?: number;
  mappingLabel?: string;
  collectionDate?: string;
  collectionMethod?: string;
  collectorName?: string;
  boundarySizeHa?: number;
  boundaryComment?: string;
  geometry?: Geometry;
}

export interface Geometry {
  type?: "MultiPolygon";
  coordinates?: Position[][][];
}

export interface ProjectFile {
  fileAttachmentGuid?: string,
  attachmentType?: string,
  fileName?: string,
  fileType?: string,
  uploadedBy?: string,
  uploadedDate?: string,
  polygonHectares?: string,
  description?: string,
  fileIdentifier?: string,
  documentPath?: string,
  attachmentContentTypeCode?: {
    attachmentContentTypeCode?: string;
  };
  sourceObjectUniqueId?: string
}

export interface FeaturesResponse {
  projects?: ProjectExtended[];
  currentPage?: number;
  pageSize?: number;
  totalItems?: number;
  totalPages?: number;
}

export interface ProjectLocation {
  projectGuid?: string
  latitude?: number;
  longitude?: number
}

export interface ProjectExtended extends Project {
  projectFiscals?: ProjectFiscalExtended[];
}

export interface ProjectFiscalExtended extends ProjectFiscal {
  activities?: ActivityWithBoundaries[];
}

export interface ActivityWithBoundaries extends ActivityModel {
  activityBoundaries?: ActivityBoundary[];
}

export interface ProjectTypeCodeModel {
  projectTypeCode: string;
  description: string;
  displayOrder: number;
  effectiveDate: string;
  expiryDate: string;
}

export interface ProgramAreaModel {
  programAreaGuid: string;
  programAreaName: string;
}

export interface PlanFiscalStatusCodeModel {
  planFiscalStatusCode?: string;
  description?: string;
  displayOrder?: number;
  effectiveDate?: string;
  expiryDate?: string;
}

export interface ActivityCategoryCodeModel {
  activityCategoryCode?: string;
  description?: string;
  displayOrder?: number;
  effectiveDate?: string;
  expiryDate?: string;
}

export interface BcParksSectionCodeModel {
  bcParksOrgUnitTypeCode?: string;
  effectiveDate?: string;
  expiryDate?: string;
  orgUnitId?: number;
  orgUnitName?: string;
  parentOrgUnitId?: string;
}

export interface ForestDistrictCodeModel {
  forestOrgUnitTypeCode?: string;
  effectiveDate?: string;
  expiryDate?: string;
  orgUnitId?: number;
  orgUnitName?: string;
  parentOrgUnitId?: string;
}

export interface EvaluationCriteriaCodeModel {
  evaluationCriteriaGuid?: string;
  projectTypeCode?: string;
  criteriaLabel?: string;
  evalCriteriaSectCode?: string;
  weightedRank?: number;
}

export interface WuiRiskClassCodeModel {
  wuiRiskClassRankGuid?: string;
  wuiRiskClassCode?: string;
  weightedRank?: number;
}

export interface EvaluationCriteriaSummaryModel {
  evaluationCriteriaSummaryGuid?: string;
  projectGuid?: string;
  wuiRiskClassCode?: WuiRiskClassCodeModel;
  localWuiRiskClassCode?: WuiRiskClassCodeModel;
  wuiRiskClassComment?: string;
  localWuiRiskClassRationale?: string;
  isOutsideWuiInd?: boolean;
  totalFilterScore?: number;
  evaluationCriteriaSectionSummaries?: EvaluationCriteriaSectionSummaryModel[];
  updateDate?: string;
  updateUser?: string;
  lastUpdatedTimestamp?: string;
}

export interface EvaluationCriteriaSectionSummaryModel {
  evaluationCriteriaSectionSummaryGuid?: string;
  evaluationCriteriaSectionCode?: EvaluationCriteriaSectionCodeModel;
  evaluationCriteriaSummaryGuid?: string;
  filterSectionScore?: number;
  filterSectionComment?: string;
  evaluationCriteriaSelected?: EvaluationCriteriaSelectedModel[];
}

export interface EvaluationCriteriaSectionCodeModel {
  evaluationCriteriaSectionCode?: string;
  description?: string;
  displayOrder?: number;
  effectiveDate?: string;
  expiryDate?: string;
}

export interface EvaluationCriteriaSelectedModel {
  evaluationCriteriaSelectedGuid?: string;
  evaluationCriteriaGuid?: string;
  evaluationCriteriaSectionSummaryGuid?: string;
  isEvaluationCriteriaSelectedInd?: boolean;
}

export interface EndorsementCodeModel {
  endorsementCode?: string;
  description?: string;
  displayOrder?: number;
  effectiveDate?: string;
  expiryDate?: string;
}

export interface MapServices {
  geoserverApiBaseUrl: string;
  wfnewsApiBaseUrl: string;
  wfnewsApiKey: string;
}

export interface LayerSettings {
  geoserverApiBaseUrl: string;
  wfnewsApiBaseUrl: string;
  wfnewsApiKey: string;
  openmaps: string;
}

export type ReportType = 'csv' | 'xlsx';

export interface ReportProject {
  projectGuid: string;
  projectFiscalGuids?: string[];
}

export interface ProjectFilter {
  programAreaGuid?: string[];
  fiscalYear?: string[];
  forestRegionOrgUnitId?: string[];
  forestDistrictOrgUnitId?: string[];
  fireCentreOrgUnitId?: string[];
  activityCategoryCode?: string[];
  planFiscalStatusCode?: string[];
  projectTypeCode?: string[];
  searchText?: string;
  sortBy?: string;
  sortDirection?: string;
}

export interface ReportRequest {
  reportType: ReportType;
  projects?: ReportProject[];
  projectFilter?: ProjectFilter;
}

export interface NewPerformanceUpdate {

  reportingPeriod: ReportingPeriod,
  progressStatusCode: ProgressStatus,

  generalUpdateComment: string,

  forecastAmount: number,
  forecastAdjustmentRationale: string,

  budgetHighRiskAmount: number,
  budgetHighRiskRationale: string,
  budgetMediumRiskAmount: number,
  budgetMediumRiskRationale: string,
  budgetLowRiskAmount: number,
  budgetLowRiskRationale: string,
  budgetCompletedAmount: number,
  budgetCompletedDescription: string,
}

export interface PerformanceUpdate {

  submittedTimestamp: Date;

  reportingPeriod: ReportingPeriod;
  progressStatusCode: ProgressStatus;
  updateGeneralStatus: UpdateGeneralStatus;

  submittedByUserid: String;
  submittedByGuid: String;

  generalUpdateComment: String;
  submittedBy: String;

  forecastAmount: number;
  forecastAdjustmentAmount: number;
  previousForecastAmount: number;
  forecastAdjustmentRationale: string;

  budgetHighRiskAmount: number;
  budgetHighRiskRationale: string;

  budgetMediumRiskAmount: number;
  budgetMediumRiskRationale: string;

  budgetLowRiskAmount: number;
  budgetLowRiskRationale: string;

  budgetCompletedAmount: number;
  budgetCompletedDescription: string;

  totalAmount: number;
}

export enum ForecastStatus {
  ChangedDecreased = "CHANGED_DECREASED",
  ChangedIncreased = "CHANGED_INCREASED",
  NonChanged = "NON_CHANGED"
}

export enum ProgressStatus {
  Cancelled = "CANCELLED",
  Deferred = "DEFERRED",
  Delayed = "DELAYED",
  OnTrack = "ON_TRACK"
}

export interface ProgressStatusCode {
  description: string;
  displayOrder: number;
  progressStatusCode: ProgressStatus;
}

export enum UpdateGeneralStatus {
  Cancelled = "CANCELLED",
  Complete = "COMPLETE",
  Draft = "DRAFT",
  InProgress = "IN_PROG",
  Prepared = "PREPARED",
  Proposed = "PROPOSED"
}

export enum ReportingPeriod {
  Custom = "CUSTOM",
  March7 = "MARCH7",
  Q1 = "Q1",
  Q2 = "Q2",
  Q3 = "Q3",
  Q4 = "Q4"
}

export interface ReportingPeriodCode {
  description: string;
  displayOrder: number;
  reportingPeriodCode: ReportingPeriod;
}

export interface Option<T> {
  value: T,
  description: string
}
