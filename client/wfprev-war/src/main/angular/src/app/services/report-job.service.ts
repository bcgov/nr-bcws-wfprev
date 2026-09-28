import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ReportJob, ReportJobDownloadUrl, ReportJobRequest } from 'src/app/components/models';
import { AppConfigService } from 'src/app/services/app-config.service';
import { TokenService } from 'src/app/services/token.service';

/**
 * Report exports run as background jobs on the API. Errors are passed through as
 * HttpErrorResponse so callers can tell them apart.
 */
@Injectable({ providedIn: 'root' })
export class ReportJobService {

  constructor(
    private readonly appConfigService: AppConfigService,
    private readonly httpClient: HttpClient,
    private readonly tokenService: TokenService,
  ) {}

  create(body: ReportJobRequest): Observable<ReportJob> {
    return this.httpClient.post<ReportJob>(this.url(), body, { headers: this.headers() });
  }

  list(): Observable<ReportJob[]> {
    return this.httpClient.get<ReportJob[]>(this.url(), { headers: this.headers() });
  }

  downloadUrl(jobGuid: string): Observable<ReportJobDownloadUrl> {
    return this.httpClient.post<ReportJobDownloadUrl>(`${this.url()}/${jobGuid}/download-url`, null, { headers: this.headers() });
  }

  retry(jobGuid: string): Observable<ReportJob> {
    return this.httpClient.post<ReportJob>(`${this.url()}/${jobGuid}/retry`, null, { headers: this.headers() });
  }

  clearFinished(): Observable<void> {
    return this.httpClient.post<void>(`${this.url()}/clear`, null, { headers: this.headers() });
  }

  private url(): string {
    return `${this.appConfigService.getConfig().rest['wfprev']}/wfprev-api/reports/jobs`;
  }

  private headers(): Record<string, string> {
    return { Authorization: `Bearer ${this.tokenService.getOauthToken()}` };
  }
}
