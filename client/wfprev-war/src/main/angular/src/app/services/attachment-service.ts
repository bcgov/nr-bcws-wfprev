import { Injectable } from "@angular/core";
import { catchError, map, Observable, throwError } from "rxjs";
import { AppConfigService } from "./app-config.service";
import { HttpClient } from "@angular/common/http";
import { TokenService } from "./token.service";
import { FileAttachment } from "../components/models";

@Injectable({
    providedIn: 'root',
})

export class AttachmentService {

    constructor(
        private readonly appConfigService: AppConfigService,
        private readonly httpClient: HttpClient,
        private readonly tokenService: TokenService,
    ) { }


    createProjectAttachment(projectGuid: string, attachment: FileAttachment): Observable<any> {
        const baseUrl = `${this.appConfigService.getConfig().rest['wfprev']}/wfprev-api/projects`;
        const url = `${baseUrl}/${projectGuid}/attachments`;
        return this.httpClient.post<any>(
            url,
            attachment,
            {
                headers: {
                    Authorization: `Bearer ${this.tokenService.getOauthToken()}`,
                }
            }
        ).pipe(
            catchError((error) => {
                console.error("Error creating project attachment", error);
                return throwError(() => new Error("Failed to create project attachment"));
            })
        );
    }

    getProjectAttachments(projectGuid: string): Observable<any> {
        const baseUrl = `${this.appConfigService.getConfig().rest['wfprev']}/wfprev-api/projects`;
        const url = `${baseUrl}/${projectGuid}/attachments`;

        return this.httpClient.get(url, {
            headers: {
                Authorization: `Bearer ${this.tokenService.getOauthToken()}`,
            }
        }).pipe(
            map((response: any) => response),
            catchError((error) => {
                console.error("Error fetching project attachments", error);
                return throwError(() => new Error("Failed to fetch project attachments"));
            })
        );
    }

    deleteProjectAttachment(projectGuid: string, attachmentGuid: string): Observable<any> {
        const baseUrl = `${this.appConfigService.getConfig().rest['wfprev']}/wfprev-api/projects`;
        const url = `${baseUrl}/${projectGuid}/attachments/${attachmentGuid}`;

        return this.httpClient.delete(url, {
            headers: {
                Authorization: `Bearer ${this.tokenService.getOauthToken()}`,
            }
        }).pipe(
            map((response: any) => response),
            catchError((error) => {
                console.error("Error deleting project attachment", error);
                return throwError(() => new Error("Failed to delete project attachment"));
            })
        );
    }

    createActivityAttachment(projectGuid: string, projectPlanFiscalGuid: string, activityGuid: string, attachment: FileAttachment): Observable<any> {
        const baseUrl = `${this.appConfigService.getConfig().rest['wfprev']}/wfprev-api/projects`;
        const url = `${baseUrl}/${projectGuid}/projectFiscals/${projectPlanFiscalGuid}/activities/${activityGuid}/attachments`;
        return this.httpClient.post<any>(
            url,
            attachment,
            {
                headers: {
                    Authorization: `Bearer ${this.tokenService.getOauthToken()}`,
                }
            }
        ).pipe(
            catchError((error) => {
                console.error("Error creating activity attachment", error);
                return throwError(() => new Error("Failed to create activity attachment"));
            })
        );
    }

    getActivityAttachments(projectGuid: string, projectPlanFiscalGuid: string, activityGuid: string): Observable<any> {
        const baseUrl = `${this.appConfigService.getConfig().rest['wfprev']}/wfprev-api/projects`;
        const url = `${baseUrl}/${projectGuid}/projectFiscals/${projectPlanFiscalGuid}/activities/${activityGuid}/attachments`;
        
        return this.httpClient.get(url, {
            headers: {
                Authorization: `Bearer ${this.tokenService.getOauthToken()}`,
            }
        }).pipe(
            map((response: any) => response),
            catchError((error) => {
                console.error("Error fetching activity attachments", error);
                return throwError(() => new Error("Failed to fetch activity attachments"));
            })
        )
    }

    deleteActivityAttachments(projectGuid: string, projectPlanFiscalGuid: string, activityGuid: string, activityFileAttachmentGuid: string): Observable<any> {
        const baseUrl = `${this.appConfigService.getConfig().rest['wfprev']}/wfprev-api/projects`;
        const url = `${baseUrl}/${projectGuid}/projectFiscals/${projectPlanFiscalGuid}/activities/${activityGuid}/attachments/${activityFileAttachmentGuid}`;

        return this.httpClient.delete(url, {
            headers: {
                Authorization: `Bearer ${this.tokenService.getOauthToken()}`,
            }
        }).pipe(
            map((response: any) => response),
            catchError((error) => {
                console.error("Error delete activity attachments", error);
                return throwError(() => new Error("Failed to delete activity attachments"));
            })
        );
    }
}