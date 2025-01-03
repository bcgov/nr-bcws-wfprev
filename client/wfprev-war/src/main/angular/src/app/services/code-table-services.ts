import { HttpClient } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { catchError, map, Observable,throwError } from "rxjs";
import { AppConfigService } from "src/app/services/app-config.service";
import { TokenService } from "src/app/services/token.service";

@Injectable({
    providedIn: 'root',
  })

export class CodeTableServices {
    constructor(
        private appConfigService: AppConfigService,
        private httpClient: HttpClient,
        private tokenService: TokenService,
    ){
    }

    fetchCodeTable(codeTableName:string): Observable<any> {
        const url = `${this.appConfigService.getConfig().rest['wfprev']
        }/wfprev-api/codes/${codeTableName}`;
    
        return this.httpClient.get(
            url, {
                headers: {
                    Authorization: `Bearer ${this.tokenService.getOauthToken()}`,
                }
            }
        ).pipe(
            map((response: any) => response),
            catchError((error) => {
                console.error("Error fetching code table", error);
                return throwError(() => new Error("Failed to get code table"));
            })
        );
    }
}