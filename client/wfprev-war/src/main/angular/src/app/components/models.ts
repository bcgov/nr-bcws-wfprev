export interface Project {
  bcParksRegionOrgUnitId: number;
  bcParksSectionOrgUnitId: number;
  closestCommunityName: string;
  createDate?: string;
  createUser?: string;
  fireCentreOrgUnitId: number;
  forestAreaCode: {
    forestAreaCode: string;
  };
  forestDistrictOrgUnitId: number;
  forestRegionOrgUnitId: number;
  generalScopeCode: {
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
  projectTypeCode: {
    projectTypeCode: string;
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
}
