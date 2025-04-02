import { Injectable } from "@angular/core";
import { catchError, Observable, throwError } from "rxjs";
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
        ){}
    

    createProjectAttachment(projectGuid: string, attachment: FileAttachment): Observable<any> {
            const baseUrl = `${this.appConfigService.getConfig().rest['wfprev']}/wfprev-api/projects`;
            const url = `${baseUrl}/${projectGuid}/attachments`;
            console.log(attachment)
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

}