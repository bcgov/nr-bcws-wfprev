import { HttpClient } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { catchError, map, Observable, of, throwError } from "rxjs";
import { AppConfigService } from "src/app/services/app-config.service";
import { TokenService } from "src/app/services/token.service";

@Injectable({
  providedIn: 'root',
})
export class CodeTableServices {
  private codeTableCache: { [key: string]: any } = {}; // Cache for code tables
  private fireCentresCache: any | null = null;
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

  fetchFireCentres(): Observable<any> {
    if (this.fireCentresCache) {
      return of(this.fireCentresCache);
    }

    const url = `${this.appConfigService.getConfig().rest['openmaps'] 
    }/geo/pub/WHSE_LEGAL_ADMIN_BOUNDARIES.DRP_MOF_FIRE_CENTRES_SP/ows?service=WFS&version=1.1.0&request=GetFeature&srsName=EPSG:4326&typename=pub:WHSE_LEGAL_ADMIN_BOUNDARIES.DRP_MOF_FIRE_CENTRES_SP&outputformat=application/json&propertyName=(MOF_FIRE_CENTRE_ID,MOF_FIRE_CENTRE_NAME)`;

    return this.httpClient.get(
      url, {
        headers: {
          Authorization: `Bearer ${this.tokenService.getOauthToken()}`,
        }
      }
    ).pipe(
      map((response: any) => {
      this.fireCentresCache = response;
      return response;
      }),
      catchError((error) => {
        console.error("Error fetching fire centres from openmaps", error);
        return throwError(() => new Error("Failed to get fire centres"));
      })
    )
  }
}
