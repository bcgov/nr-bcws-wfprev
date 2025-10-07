import { HttpClient, HttpRequest, HttpHeaders, HttpEventType, HttpResponse, HttpParams } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { UUID } from "angular2-uuid";
import { catchError, map, Observable, throwError } from "rxjs";
import { ActivityBoundary, EvaluationCriteriaSummaryModel, FeaturesResponse, Project, ProjectBoundary, ProjectFiscal, ProjectLocation, ReportRequest } from "src/app/components/models";
import { AppConfigService } from "src/app/services/app-config.service";
import { TokenService } from "src/app/services/token.service";

export const UPLOAD_DIRECTORY = '/WFPREV/uploads';

@Injectable({
    providedIn: 'root',
})

export class ProjectService {
    userGuid: string | undefined;

    constructor(
        private readonly appConfigService: AppConfigService,
        private readonly httpClient: HttpClient,
        private readonly tokenService: TokenService,
    ) {
        this.tokenService.credentialsEmitter.subscribe((cred) => {
            this.userGuid = cred.userGuid ? cred.userGuid : cred.user_guid;
        });
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

    updateProject(projectGuid: string, projectData: Project): Observable<any> {
        const baseUrl = `${this.appConfigService.getConfig().rest['wfprev']}/wfprev-api/projects`;
        const url = `${baseUrl}/${projectGuid}`;
        return this.httpClient.put(url, projectData, {
            headers: {
                Authorization: `Bearer ${this.tokenService.getOauthToken()}`,
            }
        }).pipe(
            map((response: any) => response),
            catchError((error) => {
                console.error("Error update project", error);
                return throwError(() => error);
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
        return this.httpClient.put(url, projectFiscal, {
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
        return this.httpClient.put(url, activities, {
            headers: {
                Authorization: `Bearer ${this.tokenService.getOauthToken()}`,
            }
        }).pipe(
            map((response: any) => response),
            catchError((error) => {
                console.error("Error update activities", error);
                return throwError(() => new Error("Failed to update activities"))
            })
        )
    }

    createFiscalActivity(projectGuid: string, projectPlanFiscalGuid: string, activity: any): Observable<any> {
        const baseUrl = `${this.appConfigService.getConfig().rest['wfprev']}/wfprev-api/projects`;
        const url = `${baseUrl}/${projectGuid}/projectFiscals/${projectPlanFiscalGuid}/activities`;
        return this.httpClient.post(url, activity, {
            headers: {
                Authorization: `Bearer ${this.tokenService.getOauthToken()}`,
            }
        }).pipe(
            map((response: any) => response),
            catchError((error) => {
                console.error("Error create activities", error);
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


    getActivityBoundaries(projectGuid: string, projectPlanFiscalGuid: string, activityGuid: string): Observable<any> {
        const baseUrl = `${this.appConfigService.getConfig().rest['wfprev']}/wfprev-api/projects`;
        const url = `${baseUrl}/${projectGuid}/projectFiscals/${projectPlanFiscalGuid}/activities/${activityGuid}/activityBoundary`;

        return this.httpClient.get(url, {
            headers: {
                Authorization: `Bearer ${this.tokenService.getOauthToken()}`,
            }
        }).pipe(
            map((response: any) => response),
            catchError((error) => {
                console.error("Error fetching activity boundaries", error);
                return throwError(() => new Error("Failed to fetch activity boundaries"));
            })
        );
    }

    createActivityBoundary(projectGuid: string, fiscalGuid: string, activityGuid: string, activityBoundary: ActivityBoundary): Observable<any> {
        const baseUrl = `${this.appConfigService.getConfig().rest['wfprev']}/wfprev-api/projects`;
        const url = `${baseUrl}/${projectGuid}/projectFiscals/${fiscalGuid}/activities/${activityGuid}/activityBoundary`;
        return this.httpClient.post<any>(
            url,
            activityBoundary,
            {
                headers: {
                    Authorization: `Bearer ${this.tokenService.getOauthToken()}`,
                }
            }
        ).pipe(
            catchError((error) => {
                console.error("Error creating activity boundary", error);
                return throwError(() => new Error("Failed to create activity boundary"));
            })
        );
    }

    deleteActivityBoundary(projectGuid: string, fiscalGuid: string, actiityGuid: string, activityBoundaryGuid: string): Observable<any> {
        const baseUrl = `${this.appConfigService.getConfig().rest['wfprev']}/wfprev-api/projects`;
        const url = `${baseUrl}/${projectGuid}/projectFiscals/${fiscalGuid}/activities/${actiityGuid}/activityBoundary/${activityBoundaryGuid}`;

        return this.httpClient.delete(url, {
            headers: {
                Authorization: `Bearer ${this.tokenService.getOauthToken()}`,
            }
        }).pipe(
            map((response: any) => response),
            catchError((error) => {
                console.error("Error deleting activity boundary", error);
                return throwError(() => new Error("Failed to delete activity boundary"));
            })
        );
    }

    getProjectBoundaries(projectGuid: string): Observable<any> {
        const baseUrl = `${this.appConfigService.getConfig().rest['wfprev']}/wfprev-api/projects`;
        const url = `${baseUrl}/${projectGuid}/projectBoundary`;

        return this.httpClient.get(url, {
            headers: {
                Authorization: `Bearer ${this.tokenService.getOauthToken()}`,
            }
        }).pipe(
            map((response: any) => response),
            catchError((error) => {
                console.error("Error fetching project boundaries", error);
                return throwError(() => new Error("Failed to fetch project boundaries"));
            })
        );
    }

    createProjectBoundary(projectGuid: string, projectBoundary: ProjectBoundary): Observable<any> {
        const baseUrl = `${this.appConfigService.getConfig().rest['wfprev']}/wfprev-api/projects`;
        const url = `${baseUrl}/${projectGuid}/projectBoundary`;
        return this.httpClient.post<any>(
            url,
            projectBoundary,
            {
                headers: {
                    Authorization: `Bearer ${this.tokenService.getOauthToken()}`,
                }
            }
        ).pipe(
            catchError((error) => {
                console.error("Error creating project boundary", error);
                return throwError(() => new Error("Failed to create project boundary"));
            })
        );
    }

    deleteProjectBoundary(projectGuid: string, projectBoundaryGuid: string): Observable<any> {
        const baseUrl = `${this.appConfigService.getConfig().rest['wfprev']}/wfprev-api/projects`;
        const url = `${baseUrl}/${projectGuid}/projectBoundary/${projectBoundaryGuid}`;

        return this.httpClient.delete(url, {
            headers: {
                Authorization: `Bearer ${this.tokenService.getOauthToken()}`,
            }
        }).pipe(
            map((response: any) => response),
            catchError((error) => {
                console.error("Error deleting project boundary", error);
                return throwError(() => new Error("Failed to delete project boundary"));
            })
        );
    }

    uploadDocument({
        file,
        fileName = file.name,
        userId = this.userGuid,
        uploadDirectory = UPLOAD_DIRECTORY,
        onProgress = () => { }
    }: {
        file: File;
        fileName?: string;
        userId?: string;
        uploadDirectory?: string;
        onProgress?: (percent: number, loaded: number, total: number) => void;
    }): Observable<any> {
        if (!file) {
            return throwError(() => new Error("No file provided for upload"));
        }

        // Generate unique file path
        const uniqueFileName = `${UUID.UUID()}--${fileName}`;
        const filePath = `${uploadDirectory}/users/${userId}/${uniqueFileName}`;

        // Detect if it's a KML file
        const isKml = file.name.toLowerCase().endsWith('.kml');

        // Manually set content type if it's missing or incorrect
        const contentType = isKml
            ? 'application/vnd.google-earth.kml+xml'
            : file.type || 'application/octet-stream';

        // Wrap the file in a Blob with correct content type
        const typedFile = new Blob([file], { type: contentType });

        // Update metadata
        const metadata = [
            {
                '@type': 'http://resources.wfdm.nrs.gov.bc.ca/fileMetadataResource', // NOSONAR
                'type': 'http://resources.wfdm.nrs.gov.bc.ca/fileMetadataResource', // NOSONAR
                'metadataName': 'actual-filename',
                'metadataValue': file.name
            },
            {
                '@type': 'http://resources.wfdm.nrs.gov.bc.ca/fileMetadataResource', // NOSONAR
                'type': 'http://resources.wfdm.nrs.gov.bc.ca/fileMetadataResource', // NOSONAR
                'metadataName': 'content-type',
                'metadataValue': contentType
            }
        ];


        const fileDetails = {
            '@type': 'http://resources.wfdm.nrs.gov.bc.ca/fileDetails', // NOSONAR
            type: "http://resources.wfdm.nrs.gov.bc.ca/fileDetails", // NOSONAR
            fileSize: file.size,
            filePath: filePath,
            security: [],
            metadata: metadata,
            fileType: 'DOCUMENT'
        };

        // Prepare FormData
        const formData = new FormData();
        formData.append(
            'resource',
            new Blob([JSON.stringify(fileDetails)], { type: 'application/json' })
        );
        formData.append('file', typedFile, file.name);
        formData.append('file', file);

        const url = `${this.appConfigService.getConfig().rest['wfdm']}/documents`;
        const headers = new HttpHeaders({
            Authorization: `Bearer ${this.tokenService.getOauthToken()}`,

        });

        // Make HTTP POST request with progress tracking
        const req = new HttpRequest('POST', url, formData, {
            headers: headers,
            reportProgress: true,
            responseType: 'json'
        });


        return this.httpClient.request(req).pipe(
            map((event) => {
                if (event.type === HttpEventType.UploadProgress) {
                    const percentDone = Math.round((100 * event.loaded) / (event.total || file.size));
                    onProgress(percentDone, event.loaded, event.total || file.size);
                } else if (event instanceof HttpResponse) {
                    onProgress(100, file.size, file.size);
                    return event.body; // Successfully uploaded
                }
                return null;
            }),
            catchError((error) => {
                console.error("Error uploading document", error);
                return throwError(() => new Error("Failed to upload document"));
            })
        );
    }

    downloadDocument(fileId: string): Observable<Blob> {
        const url = `${this.appConfigService.getConfig().rest['wfdm']}/documents/${fileId}/bytes`;
        const headers = new HttpHeaders({
            Authorization: `Bearer ${this.tokenService.getOauthToken()}`,
        });

        return this.httpClient.get(url, {
            headers: headers,
            responseType: 'blob'
        });
    }

    getFeatures(
        params?: {
            programAreaGuid?: string[];
            fiscalYear?: string[];
            activityCategoryCode?: string[];
            planFiscalStatusCode?: string[];
            forestRegionOrgUnitId?: string[];
            forestDistrictOrgUnitId?: string[];
            fireCentreOrgUnitId?: string[];
            searchText?: string;
        },
        pageNumber: number = 1,
        pageRowCount: number = 20,
        sortBy?: string,
        sortDirection?: string
        ): Observable<FeaturesResponse> {
        const baseUrl = `${this.appConfigService.getConfig().rest['wfprev']}/wfprev-api/features`;

        let httpParams = new HttpParams()
            .set('pageNumber', pageNumber)
            .set('pageRowCount', pageRowCount);

        if (sortBy) httpParams = httpParams.set('sortBy', sortBy);
        if (sortDirection) httpParams = httpParams.set('sortDirection', sortDirection);

        if (params) {
            for (const key in params) {
            const value = params[key as keyof typeof params];
            if (Array.isArray(value)) {
                value.forEach(v => (httpParams = httpParams.append(key, v)));
            } else if (value) {
                httpParams = httpParams.set(key, value);
            }
            }
        }

        return this.httpClient.get<FeaturesResponse>(baseUrl, {
            headers: {
            Authorization: `Bearer ${this.tokenService.getOauthToken()}`,
            },
            params: httpParams,
        }).pipe(
            catchError((error) => {
            console.error('Error fetching features', error);
            return throwError(() => new Error('Failed to fetch features'));
            })
        );
    }

    getFeatureByProjectGuid(projectGuid: string): Observable<Project | null> {
        const baseUrl = `${this.appConfigService.getConfig().rest['wfprev']}/wfprev-api/features`;
        const params = new HttpParams().set('projectGuid', projectGuid);

        return this.httpClient.get<any>(baseUrl, {
            headers: { Authorization: `Bearer ${this.tokenService.getOauthToken()}` },
            params,
        }).pipe(
            map(response => {
                if (response?.project) {
                    return response.project;
                }

                const embedded = response?._embedded?.project ?? [];
                return embedded.length > 0 ? embedded[0] : null;
            }),
            catchError(error => {
                console.error('Error fetching feature by projectGuid', error);
                return throwError(() => new Error('Failed to fetch feature by projectGuid'));
            })
        );
    }
    getProjectLocations(
        params?: {
            programAreaGuid?: string[];
            fiscalYear?: string[];
            activityCategoryCode?: string[];
            planFiscalStatusCode?: string[];
            forestRegionOrgUnitId?: string[];
            forestDistrictOrgUnitId?: string[];
            fireCentreOrgUnitId?: string[];
            projectTypeCode?: string[];
            searchText?: string;
        }
        ): Observable<ProjectLocation[]> {
        const baseUrl = `${this.appConfigService.getConfig().rest['wfprev']}/wfprev-api/project-locations`;

        let httpParams = new HttpParams();
        if (params) {
            for (const key in params) {
            const value = params[key as keyof typeof params];
            if (Array.isArray(value)) {
                value.forEach(v => (httpParams = httpParams.append(key, v)));
            } else if (value) {
                httpParams = httpParams.set(key, value);
            }
            }
        }

        return this.httpClient.get<any>(baseUrl, {
            headers: {
            Authorization: `Bearer ${this.tokenService.getOauthToken()}`,
            },
            params: httpParams,
        }).pipe(
            map(response => {
            const locations = response?._embedded?.project ?? [];
            return locations as ProjectLocation[];
            }),
            catchError(error => {
            console.error('Error fetching project locations', error);
            return throwError(() => new Error('Failed to fetch project locations'));
            })
        );
    }

    getEvaluationCriteriaSummaries(projectGuid: string): Observable<any> {
        const baseUrl = `${this.appConfigService.getConfig().rest['wfprev']}/wfprev-api/projects`;
        const url = `${baseUrl}/${projectGuid}/evaluationCriteriaSummary`;

        return this.httpClient.get(url, {
            headers: {
                Authorization: `Bearer ${this.tokenService.getOauthToken()}`,
            }
        }).pipe(
            map((response: any) => response),
            catchError((error) => {
                console.error("Error fetching evaluation criteria summaries", error);
                return throwError(() => new Error("Failed to fetch evaluation criteria summaries"));
            })
        );
    }

    createEvaluationCriteriaSummary(
        projectGuid: string,
        criteriaSummary: EvaluationCriteriaSummaryModel
    ): Observable<EvaluationCriteriaSummaryModel> {
        const baseUrl = `${this.appConfigService.getConfig().rest['wfprev']}/wfprev-api/projects`;
        const url = `${baseUrl}/${projectGuid}/evaluationCriteriaSummary`;

        return this.httpClient.post<EvaluationCriteriaSummaryModel>(
            url,
            criteriaSummary,
            {
                headers: {
                    Authorization: `Bearer ${this.tokenService.getOauthToken()}`
                }
            }
        ).pipe(
            map(response => response),
            catchError(error => {
                console.error("Error creating evaluation criteria summary", error);
                return throwError(() => new Error("Failed to create evaluation criteria summary"));
            })
        );
    }

    updateEvaluationCriteriaSummary(
        projectGuid: string,
        summaryGuid: string,
        criteriaSummary: EvaluationCriteriaSummaryModel
    ): Observable<EvaluationCriteriaSummaryModel> {
        const url = `${this.appConfigService.getConfig().rest['wfprev']}/wfprev-api/projects/${projectGuid}/evaluationCriteriaSummary/${summaryGuid}`;

        return this.httpClient.put<EvaluationCriteriaSummaryModel>(url, criteriaSummary, {
            headers: {
                Authorization: `Bearer ${this.tokenService.getOauthToken()}`,
            }
        }).pipe(
            catchError(error => {
                console.error("Error updating evaluation criteria summary", error);
                return throwError(() => new Error("Failed to update evaluation criteria summary"));
            })
        );
    }

    
    deleteEvaluationCriteriaSummary(projectGuid: string, EvaluationCriteriaSummaryGuid: string): Observable<any> {
        const baseUrl = `${this.appConfigService.getConfig().rest['wfprev']}/wfprev-api/projects`;
        const url = `${baseUrl}/${projectGuid}/evaluationCriteriaSummary/${EvaluationCriteriaSummaryGuid}`;

        return this.httpClient.delete(url, {
            headers: {
                Authorization: `Bearer ${this.tokenService.getOauthToken()}`,
            }
        }).pipe(
            map((response: any) => response),
            catchError((error) => {
                console.error("Error delete evaluation criteria", error);
                return throwError(() => new Error("Failed to delete evaluation criteria"));
            })
        );
    }


    downloadProjects(body: ReportRequest): Observable<Blob> {
        const url = `${this.appConfigService.getConfig().rest['wfprev']}/wfprev-api/reports`;

        return this.httpClient.post<Blob>(url, body, {
            headers: {
                Authorization: `Bearer ${this.tokenService.getOauthToken()}`,
                'Content-Type': 'application/json',
                'Accept': 'application/octet-stream'
            },
            responseType: 'blob' as 'json' 
        }).pipe(
            catchError((error) => {
                console.error('Error downloading projects', error);
                return throwError(() => new Error('Failed to download projects'));
            })
        );
    }
}