import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { AppConfigService } from 'src/app/services/app-config.service';
import { TokenService } from 'src/app/services/token.service';
import { ReportJobService } from './report-job.service';

describe('ReportJobService', () => {
  let service: ReportJobService;
  let http: HttpTestingController;
  const base = 'http://mock-api.com/wfprev-api/reports/jobs';

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        ReportJobService,
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: AppConfigService, useValue: { getConfig: () => ({ rest: { wfprev: 'http://mock-api.com' } }) } },
        { provide: TokenService, useValue: { getOauthToken: () => 'token' } }
      ]
    });
    service = TestBed.inject(ReportJobService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('starts a job', () => {
    service.create({ reportType: 'PROJECT_CSV', projectFilter: {}, description: 'd', exportGroupGuid: 'g' }).subscribe();

    const req = http.expectOne(base);
    expect(req.request.method).toBe('POST');
    expect(req.request.body.exportGroupGuid).toBe('g');
    expect(req.request.headers.get('Authorization')).toBe('Bearer token');
    req.flush({});
  });

  it('lists jobs', () => {
    service.list().subscribe(jobs => expect(jobs.length).toBe(1));

    const req = http.expectOne(base);
    expect(req.request.method).toBe('GET');
    req.flush([{ jobGuid: 'a' }]);
  });

  it('asks for a download link, retries and clears', () => {
    service.downloadUrl('a').subscribe();
    service.retry('a').subscribe();
    service.clearFinished().subscribe();

    expect(http.expectOne(`${base}/a/download-url`).request.method).toBe('POST');
    expect(http.expectOne(`${base}/a/retry`).request.method).toBe('POST');
    expect(http.expectOne(`${base}/clear`).request.method).toBe('POST');
  });

  it('passes the HTTP error through', () => {
    let status = 0;
    service.downloadUrl('a').subscribe({ error: err => status = err.status });

    http.expectOne(`${base}/a/download-url`).flush({ error: 'gone' }, { status: 409, statusText: 'Conflict' });

    expect(status).toBe(409);
  });
});
