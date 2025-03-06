import { HttpClient } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { catchError, map, Observable,throwError } from "rxjs";
import { Project, ProjectFiscal } from "src/app/components/models";
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

    createProjectFiscal(projectGuid: string, projectFiscal: ProjectFiscal): Observable<any> {
        const baseUrl = `${this.appConfigService.getConfig().rest['wfprev']}/wfprev-api/projects`;
        const url = `${baseUrl}/${projectGuid}/projectFiscals`;
        return this.httpClient.post<any>(
            url,
            projectFiscal,
            {
                headers: {
                    Authorization: `Bearer ${this.tokenService.getOauthToken()}`,
                }
            }
        ).pipe(
            catchError((error) => {
                console.error("Error creating project fiscal", error);
                return throwError(() => new Error("Failed to create project fiscal"));
            })
        );
    }

    updateProjectFiscal(projectGuid: string, projectPlanFiscalGuid: string, projectFiscal: ProjectFiscal): Observable<any> {
        const baseUrl = `${this.appConfigService.getConfig().rest['wfprev']}/wfprev-api/projects`;
        const url = `${baseUrl}/${projectGuid}/projectFiscals/${projectPlanFiscalGuid}`;
        return this.httpClient.put(url, projectFiscal,{
            headers: {
                Authorization: `Bearer ${this.tokenService.getOauthToken()}`,
            }
        }).pipe(
            map((response: any) => response),
            catchError((error) => {
                console.error("Error update project fiscal", error);
                return throwError(() => new Error("Failed to update project fiscal"));
            })
        );
    }

    deleteProjectFiscalByProjectPlanFiscalGuid(projectGuid: string, projectPlanFiscalGuid: string): Observable<any> {
        const baseUrl = `${this.appConfigService.getConfig().rest['wfprev']}/wfprev-api/projects`;
        const url = `${baseUrl}/${projectGuid}/projectFiscals/${projectPlanFiscalGuid}`;
            
        return this.httpClient.delete(url, {
            headers: {
                Authorization: `Bearer ${this.tokenService.getOauthToken()}`,
            }
        }).pipe(
            map((response: any) => response),
            catchError((error) => {
                console.error("Error delete project fiscals", error);
                return throwError(() => new Error("Failed to delete project fiscals"));
            })
        );
    }

    getFiscalActivities(projectGuid: string, projectPlanFiscalGuid: string): Observable<any> {
        const baseUrl = `${this.appConfigService.getConfig().rest['wfprev']}/wfprev-api/projects`;
        const url = `${baseUrl}/${projectGuid}/projectFiscals/${projectPlanFiscalGuid}/activities`;
            
        return this.httpClient.get(url, {
            headers: {
                Authorization: `Bearer ${this.tokenService.getOauthToken()}`,
            }
        }).pipe(
            map((response: any) => response),
            catchError((error) => {
                console.error("Error fetching activities", error);
                return throwError(() => new Error("Failed to fetch activities"));
            })
        );
    }

    updateFiscalActivities(projectGuid: string, projectPlanFiscalGuid: string, activityGuid: string, activities: any): Observable<any> {
        const baseUrl = `${this.appConfigService.getConfig().rest['wfprev']}/wfprev-api/projects`;
        const url = `${baseUrl}/${projectGuid}/projectFiscals/${projectPlanFiscalGuid}/activities/${activityGuid}`;
        return this.httpClient.put(url,activities,{
            headers: {
                Authorization: `Bearer ${this.tokenService.getOauthToken()}`,
            }
        }).pipe (
            map((response: any) => response),
            catchError((error) => {
                console.error("Error update activities",error);
                return throwError(() => new Error("Failed to update activities"))
            })
        )
    }

    createFiscalActivity(projectGuid: string, projectPlanFiscalGuid: string, activity: any): Observable<any> {
        const baseUrl = `${this.appConfigService.getConfig().rest['wfprev']}/wfprev-api/projects`;
        const url = `${baseUrl}/${projectGuid}/projectFiscals/${projectPlanFiscalGuid}/activities`;
        return this.httpClient.post(url,activity,{
            headers: {
                Authorization: `Bearer ${this.tokenService.getOauthToken()}`,
            }
        }).pipe (
            map((response: any) => response),
            catchError((error) => {
                console.error("Error create activities",error);
                return throwError(() => new Error("Failed to create activities"))
            })
        )
    }

    deleteActivity(projectGuid: string, projectPlanFiscalGuid: string, actiityGuid: string): Observable<any> {
        const baseUrl = `${this.appConfigService.getConfig().rest['wfprev']}/wfprev-api/projects`;
        const url = `${baseUrl}/${projectGuid}/projectFiscals/${projectPlanFiscalGuid}/activities/${actiityGuid}`;
            
        return this.httpClient.delete(url, {
            headers: {
                Authorization: `Bearer ${this.tokenService.getOauthToken()}`,
            }
        }).pipe(
            map((response: any) => response),
            catchError((error) => {
                console.error("Error delete activity", error);
                return throwError(() => new Error("Failed to delete activity"));
            })
        );
    }
 }