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
  projectGuid?: string;
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
}

export interface ProjectFiscal {
  projectGuid?: string;
  projectPlanFiscalGuid?: string;
  activityCategoryCode?: string;
  fiscalYear?: number;
  projectPlanStatusCode?: string;
  planFiscalStatusCode?: string;
  projectFiscalName?: string;
  projectFiscalDescription?: string;
  businessAreaComment?: string;
  estimatedClwrrAllocAmount?: number;
  fiscalAncillaryFundAmount?: number;
  fiscalPlannedProjectSizeHa?: number;
  fiscalPlannedCostPerHaAmt?: number;
  fiscalReportedSpendAmount?: number;
  fiscalActualAmount?: number;
  fiscalCompletedSizeHa?: number;
  fiscalActualCostPerHaAmt?: number;
  firstNationsDelivPartInd?: boolean;
  firstNationsEngagementInd?: boolean;
  firstNationsPartner?: string;
  resultsNumber?: string;
  resultsOpeningId?: string;
  resultsContactEmail?: string;
  submittedByName?: string;
  submittedByUserGuid?: string;
  submittedByUserUserid?: string;
  submissionTimestamp?: string;
  isApprovedInd?: boolean;
  isDelayedInd?: boolean;
  fiscalForecastAmount?: number;
  totalCostEstimateAmount?: number;
  cfsProjectCode?: string;
  ancillaryFundingSourceGuid?: string;
  otherPartner?: string
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
  attachmentReadOnlyInd: boolean;
  createDate?: string;
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
}


