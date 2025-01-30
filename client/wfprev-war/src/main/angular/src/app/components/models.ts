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
  secondaryObjectiveRationale? :string;
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
