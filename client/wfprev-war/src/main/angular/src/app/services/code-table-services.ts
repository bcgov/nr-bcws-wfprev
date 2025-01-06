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
}
