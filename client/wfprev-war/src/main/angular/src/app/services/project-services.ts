import { HttpClient } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { catchError, map, Observable,throwError } from "rxjs";
import { AppConfigService } from "src/app/services/app-config.service";
import { TokenService } from "src/app/services/token.service";

@Injectable({
    providedIn: 'root',
  })

export class ProjectService {
    constructor(
        private appConfigService: AppConfigService,
        private httpClient: HttpClient,
        private tokenService: TokenService,
    ){
    }

    fetchProjects(): Observable<any> {
        console.log("Fetching projects...");
        const url = `${this.appConfigService.getConfig().rest['wfprev']
        }/wfprev-api/projects`;
    
        return this.httpClient.get(
            url, {
                headers: {
                    Authorization: `Bearer ${this.tokenService.getOauthToken()}`,
                }
            }
        ).pipe(
            map((response: any) => response),
            catchError((error) => {
                console.error("Error fetching projects", error);
                return throwError(() => new Error("Failed to fetch projects"));
            })
        );
    }

    createProject(project: any): Observable<any> {
        const url = `${this.appConfigService.getConfig().rest['wfprev']}/wfprev-api/projects`;
        return this.httpClient.post<any>(
            url,
            project,
            {
                headers: {
                    Authorization: `Bearer ${this.tokenService.getOauthToken()}`,
                }
            }
        );
    }
}