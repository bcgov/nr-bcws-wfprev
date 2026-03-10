import { HttpClient } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { catchError, map, Observable, of, throwError } from "rxjs";
import { AppConfigService } from "src/app/services/app-config.service";
import { TokenService } from "src/app/services/token.service";
import { CodeTableNames, WildfireOrgUnitTypeCodes } from "src/app/utils/constants";

/** Sort helpers */
const byDisplayOrder = (a: any, b: any) => (a.displayOrder ?? 0) - (b.displayOrder ?? 0);
const byName = (a: any, b: any) =>
  (a.orgUnitName ?? a.programAreaName ?? a.description ?? '')
    .toLowerCase()
    .localeCompare(
      (b.orgUnitName ?? b.programAreaName ?? b.description ?? '').toLowerCase()
    );

@Injectable({
  providedIn: 'root',
})
export class CodeTableServices {
  private codeTableCache: { [key: string]: any } = {}; // Cache for code tables
  constructor(
    private readonly appConfigService: AppConfigService,
    private readonly httpClient: HttpClient,
    private readonly tokenService: TokenService,
  ) {}

  fetchCodeTable(codeTableName: string): Observable<any> {
    // Check if the code table is already cached
    if (this.codeTableCache[codeTableName]) {
      return of(this.codeTableCache[codeTableName]); // Return cached data
    }

    // If not cached, fetch from the API
    const url = `${this.appConfigService.getConfig().rest['wfprev']
    }/wfprev-api/codes/${codeTableName}`;
    return this.httpClient.get(
      url, {
        headers: {
          Authorization: `Bearer ${this.tokenService.getOauthToken()}`,
        }
      }
    ).pipe(
      map((response: any) => {
        this.codeTableCache[codeTableName] = response; // Cache the response
        return response;
      }),
      catchError((error) => {
        console.error("Error fetching code table", error);
        return throwError(() => new Error("Failed to get code table"));
      })
    );
  }

  // ─── Typed, pre-sorted convenience methods ───────────────────────────────

  /** Objective types sorted by displayOrder ASC */
  getObjectiveTypeCodes(): Observable<any[]> {
    return this.fetchCodeTable(CodeTableNames.OBJECTIVE_TYPE_CODE).pipe(
      map(data => [...(data?._embedded?.objectiveTypeCode ?? [])].sort(byDisplayOrder))
    );
  }

  /** Project types sorted by displayOrder ASC */
  getProjectTypeCodes(): Observable<any[]> {
    return this.fetchCodeTable(CodeTableNames.PROJECT_TYPE_CODE).pipe(
      map(data => [...(data?._embedded?.projectTypeCode ?? [])].sort(byDisplayOrder))
    );
  }

  /** Plan fiscal status codes sorted by displayOrder ASC */
  getPlanFiscalStatusCodes(): Observable<any[]> {
    return this.fetchCodeTable(CodeTableNames.PLAN_FISCAL_STATUS_CODE).pipe(
      map(data => [...(data?._embedded?.planFiscalStatusCode ?? [])].sort(byDisplayOrder))
    );
  }

  /** Activity category codes sorted by displayOrder ASC */
  getActivityCategoryCodes(): Observable<any[]> {
    return this.fetchCodeTable(CodeTableNames.ACTIVITY_CATEGORY_CODE).pipe(
      map(data => [...(data?._embedded?.activityCategoryCode ?? [])].sort(byDisplayOrder))
    );
  }

  /** Contract phase codes sorted by displayOrder ASC */
  getContractPhaseCodes(): Observable<any[]> {
    return this.fetchCodeTable(CodeTableNames.CONTRACT_PHASE_CODE).pipe(
      map(data => [...(data?._embedded?.contractPhaseCode ?? [])].sort(byDisplayOrder))
    );
  }

  /** Funding source codes sorted by displayOrder ASC */
  getFundingSourceCodes(): Observable<any[]> {
    return this.fetchCodeTable(CodeTableNames.FUNDING_SOURCE_CODE).pipe(
      map(data => [...(data?._embedded?.fundingSourceCode ?? [])].sort(byDisplayOrder))
    );
  }

  /** Reporting period codes sorted by displayOrder ASC */
  getReportingPeriodCodes(): Observable<any[]> {
    return this.fetchCodeTable(CodeTableNames.REPORTING_PERIOD_CODE).pipe(
      map(data => [...(data?._embedded?.reportingPeriodCode ?? [])].sort(byDisplayOrder))
    );
  }

  /** Progress status codes sorted by displayOrder ASC */
  getProgressStatusCodes(): Observable<any[]> {
    return this.fetchCodeTable(CodeTableNames.PROGRESS_STATUS_CODE).pipe(
      map(data => [...(data?._embedded?.progressStatusCode ?? [])].sort(byDisplayOrder))
    );
  }

  /** Program areas sorted alphabetically by programAreaName */
  getProgramAreaCodes(): Observable<any[]> {
    return this.fetchCodeTable(CodeTableNames.PROGRAM_AREA_CODE).pipe(
      map(data => [...(data?._embedded?.programArea ?? [])].sort(byName))
    );
  }

  /** Forest regions sorted alphabetically by orgUnitName */
  getForestRegionCodes(): Observable<any[]> {
    return this.fetchCodeTable(CodeTableNames.FOREST_REGION_CODE).pipe(
      map(data => [...(data?._embedded?.forestRegionCode ?? [])].sort(byName))
    );
  }

  /** Forest districts sorted alphabetically by orgUnitName */
  getForestDistrictCodes(): Observable<any[]> {
    return this.fetchCodeTable(CodeTableNames.FOREST_DISTRICT_CODE).pipe(
      map(data => [...(data?._embedded?.forestDistrictCode ?? [])].sort(byName))
    );
  }

  /** BC Parks regions sorted alphabetically by orgUnitName */
  getBcParksRegionCodes(): Observable<any[]> {
    return this.fetchCodeTable(CodeTableNames.BC_PARKS_REGION_CODE).pipe(
      map(data => [...(data?._embedded?.bcParksRegionCode ?? [])].sort(byName))
    );
  }

  /** BC Parks sections sorted alphabetically by orgUnitName */
  getBcParksSectionCodes(): Observable<any[]> {
    return this.fetchCodeTable(CodeTableNames.BC_PARKS_SECTION_CODE).pipe(
      map(data => [...(data?._embedded?.bcParksSectionCode ?? [])].sort(byName))
    );
  }

  /** Wildfire org units filtered to fire centres, sorted alphabetically by orgUnitName */
  getFireCentres(): Observable<any[]> {
    return this.fetchCodeTable(CodeTableNames.WILDFIRE_ORG_UNIT).pipe(
      map(data => {
        const orgUnits: any[] = data?._embedded?.wildfireOrgUnit ?? [];
        return orgUnits
          .filter(u => u.wildfireOrgUnitTypeCode?.wildfireOrgUnitTypeCode === WildfireOrgUnitTypeCodes.FIRE_CENTRE)
          .sort(byName);
      })
    );
  }
}
