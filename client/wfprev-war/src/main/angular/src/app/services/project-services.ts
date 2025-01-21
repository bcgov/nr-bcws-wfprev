import { HttpClient } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { catchError, map, Observable,throwError } from "rxjs";
import { Project } from "src/app/components/models";
import { AppConfigService } from "src/app/services/app-config.service";
import { TokenService } from "src/app/services/token.service";

@Injectable({
    providedIn: 'root',
  })

export class ProjectService {
    constructor(
        private readonly appConfigService: AppConfigService,
        private readonly httpClient: HttpClient,
        private readonly tokenService: TokenService,
    ){
    }

    fetchProjects(): Observable<any> {
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

    getProjectByProjectGuid(projectGuid: string): Observable<any> {
        const baseUrl = `${this.appConfigService.getConfig().rest['wfprev']}/wfprev-api/projects`;
        const url = `${baseUrl}/${projectGuid}`;
    
        return this.httpClient.get(url, {
            headers: {
                Authorization: `Bearer ${this.tokenService.getOauthToken()}`,
            }
        }).pipe(
            map((response: any) => response),
            catchError((error) => {
                console.error("Error fetching project details", error);
                return throwError(() => new Error("Failed to fetch project details"));
            })
        );
    }

    updateProject(projectGuid: string, projectData:Project): Observable<any> {
        const baseUrl = `${this.appConfigService.getConfig().rest['wfprev']}/wfprev-api/projects`;
        const url = `${baseUrl}/${projectGuid}`;
        return this.httpClient.put(url, projectData,{
            headers: {
                Authorization: `Bearer ${this.tokenService.getOauthToken()}`,
            }
        }).pipe(
            map((response: any) => response),
            catchError((error) => {
                console.error("Error update project", error);
                return throwError(() => new Error("Failed to update project"));
            })
        );
    }

    getProjectFiscalsByProjectGuid(projectGuid: string): Observable<any> {
        const baseUrl = `${this.appConfigService.getConfig().rest['wfprev']}/wfprev-api/projects`;
        const url = `${baseUrl}/${projectGuid}/projectFiscals`;
            
        return this.httpClient.get(url, {
            headers: {
                Authorization: `Bearer ${this.tokenService.getOauthToken()}`,
            }
        }).pipe(
            map((response: any) => response),
            catchError((error) => {
                console.error("Error fetching project fiscals", error);
                return throwError(() => new Error("Failed to fetch project fiscals"));
            })
        );
    }
    
 }