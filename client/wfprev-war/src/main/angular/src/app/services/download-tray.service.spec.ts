import { TestBed, fakeAsync, flush, tick } from '@angular/core/testing';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Subject, of, throwError } from 'rxjs';
import { ReportJob } from 'src/app/components/models';
import { PermissionsService } from 'src/app/services/permissions.service';
import { ReportJobService } from 'src/app/services/report-job.service';
import { TokenService } from 'src/app/services/token.service';
import { Messages } from 'src/app/utils/constants';
import { DownloadTrayService, groupExports } from './download-tray.service';

function job(overrides: Partial<ReportJob> = {}): ReportJob {
  return {
    jobGuid: 'job-1',
    exportGroupGuid: 'group-1',
    reportType: 'RESULTS_XLSX',
    fileName: 'ReMi_RESULTS.xlsx',
    description: 'Fire centres: Coastal Fire Centre',
    status: 'PREPARING',
    expired: false,
    downloaded: false,
    reference: 'JOB1',
    requestTimestamp: '2026-09-24T21:14:03Z',
    ...overrides
  };
}

describe('DownloadTrayService', () => {
  let service: DownloadTrayService;
  let api: jasmine.SpyObj<ReportJobService>;
  let snackbar: jasmine.SpyObj<MatSnackBar>;
  let credentials: Subject<any>;
  let permissions: jasmine.SpyObj<PermissionsService>;

  beforeEach(() => {
    api = jasmine.createSpyObj('ReportJobService', ['create', 'list', 'downloadUrl', 'retry', 'clearFinished']);
    api.list.and.returnValue(of([]));
    snackbar = jasmine.createSpyObj('MatSnackBar', ['open']);
    credentials = new Subject<any>();
    permissions = jasmine.createSpyObj('PermissionsService', ['hasAction']);
    permissions.hasAction.and.returnValue(true);

    TestBed.configureTestingModule({
      providers: [
        DownloadTrayService,
        { provide: ReportJobService, useValue: api },
        { provide: MatSnackBar, useValue: snackbar },
        { provide: PermissionsService, useValue: permissions },
        { provide: TokenService, useValue: { credentialsEmitter: credentials.asObservable() } }
      ]
    });
    service = TestBed.inject(DownloadTrayService);
    spyOn(service, 'openLink');
    spyOn(console, 'error');
  });

  afterEach(() => service.ngOnDestroy());

  describe('start', () => {
    it('starts one job per file in one export group and opens the tray', fakeAsync(() => {
      api.create.and.callFake(body => of(job({ jobGuid: body.reportType, reportType: body.reportType, exportGroupGuid: body.exportGroupGuid })));

      service.start(['RESULTS_XLSX', 'RESULTS_SPATIAL'], { reportType: 'RESULTS_XLSX', projectFilter: { fiscalYears: ['2026'] } }, 'Fiscal years: FY 2026/27');

      const bodies = api.create.calls.allArgs().map(args => args[0]);
      expect(bodies.map(b => b.reportType)).toEqual(['RESULTS_XLSX', 'RESULTS_SPATIAL']);
      expect(bodies[0].exportGroupGuid).toBeTruthy();
      expect(bodies[1].exportGroupGuid).toBe(bodies[0].exportGroupGuid);
      expect(bodies.every(b => b.description === 'Fiscal years: FY 2026/27')).toBeTrue();
      expect(bodies.every(b => b.projectFilter?.fiscalYears?.[0] === '2026')).toBeTrue();
      expect(service.visible()).toBeTrue();
      expect(service.expanded()).toBeTrue();
      expect(service.jobs().length).toBe(2);
      expect(service.state()).toBe('preparing');
      discardPolls();
    }));

    it('tells the user when a file could not be started', fakeAsync(() => {
      api.create.and.returnValue(throwError(() => ({ status: 500 })));

      service.start(['PROJECT_CSV'], { reportType: 'PROJECT_CSV', projectFilter: {} }, '');

      expect(snackbar.open).toHaveBeenCalledWith(Messages.fileDownloadStartFailure, 'Close', jasmine.any(Object));
      expect(service.jobs().length).toBe(0);
      expect(service.visible()).toBeFalse();
    }));
  });

  describe('polling', () => {
    it('polls while a file is being prepared, then announces it and stops', fakeAsync(() => {
      api.create.and.returnValue(of(job()));
      service.start(['RESULTS_XLSX'], { reportType: 'RESULTS_XLSX', projectFilter: {} }, '');
      api.list.and.returnValue(of([job()]));

      tick(3000);
      expect(api.list).toHaveBeenCalledTimes(1);

      api.list.and.returnValue(of([job({ status: 'READY' })]));
      service.expanded.set(false);
      tick(3000);

      expect(api.list).toHaveBeenCalledTimes(2);
      expect(service.state()).toBe('ready');
      expect(service.expanded()).toBeTrue();
      expect(service.announcement()).toContain('ReMi_RESULTS.xlsx is ready to save.');

      tick(60000);
      expect(api.list).toHaveBeenCalledTimes(2);
    }));

    it('slows to every 10 seconds after a minute', fakeAsync(() => {
      api.create.and.returnValue(of(job()));
      api.list.and.returnValue(of([job()]));
      service.start(['RESULTS_XLSX'], { reportType: 'RESULTS_XLSX', projectFilter: {} }, '');

      tick(60000);
      const callsInFirstMinute = api.list.calls.count();
      expect(callsInFirstMinute).toBe(20);

      tick(9000);
      expect(api.list.calls.count()).toBe(callsInFirstMinute);
      tick(1000);
      expect(api.list.calls.count()).toBe(callsInFirstMinute + 1);
      discardPolls();
    }));
  });

  describe('init', () => {
    it('loads the list after sign-in and shows the tray when something needs attention', fakeAsync(() => {
      api.list.and.returnValue(of([job({ status: 'READY' })]));

      service.init();
      expect(api.list).not.toHaveBeenCalled();
      credentials.next({});

      expect(api.list).toHaveBeenCalledTimes(1);
      expect(service.visible()).toBeTrue();
    }));

    it('stays hidden when everything has already been saved', fakeAsync(() => {
      api.list.and.returnValue(of([job({ status: 'READY', downloaded: true })]));

      service.init();
      credentials.next({});

      expect(service.visible()).toBeFalse();
    }));

    it('does nothing for users who cannot export', () => {
      permissions.hasAction.and.returnValue(false);

      service.init();
      credentials.next({});

      expect(api.list).not.toHaveBeenCalled();
    });
  });

  describe('show and hide', () => {
    it('reopens a hidden, collapsed tray with a fresh list', () => {
      api.list.and.returnValue(of([job({ status: 'READY', downloaded: true })]));
      service.expanded.set(false);

      service.show();

      expect(service.visible()).toBeTrue();
      expect(service.expanded()).toBeTrue();
      expect(api.list).toHaveBeenCalledTimes(1);
      expect(service.jobs().length).toBe(1);
    });

    it('toggles between shown and hidden', () => {
      service.toggleVisible();
      expect(service.visible()).toBeTrue();

      service.toggleVisible();
      expect(service.visible()).toBeFalse();
    });
  });

  describe('save', () => {
    it('opens a fresh link and marks the file saved', () => {
      service.jobs.set([job({ status: 'READY' })]);
      api.downloadUrl.and.returnValue(of({ url: 'https://s3.example/signed', fileName: 'ReMi_RESULTS.xlsx' }));

      service.save(service.jobs()[0]);

      expect(api.downloadUrl).toHaveBeenCalledWith('job-1');
      expect(service.openLink).toHaveBeenCalledWith('https://s3.example/signed');
      expect(service.jobs()[0].downloaded).toBeTrue();
    });

    it('reloads the list when the file has expired in the meantime', () => {
      service.jobs.set([job({ status: 'READY' })]);
      api.downloadUrl.and.returnValue(throwError(() => ({ status: 409 })));
      api.list.and.returnValue(of([job({ status: 'READY', expired: true })]));

      service.save(service.jobs()[0]);

      expect(service.openLink).not.toHaveBeenCalled();
      expect(api.list).toHaveBeenCalled();
      expect(service.jobs()[0].expired).toBeTrue();
    });

    it('says so when the link cannot be fetched', () => {
      service.jobs.set([job({ status: 'READY' })]);
      api.downloadUrl.and.returnValue(throwError(() => ({ status: 500 })));

      service.save(service.jobs()[0]);

      expect(snackbar.open).toHaveBeenCalledWith(Messages.fileDownloadFailure, 'Close', jasmine.any(Object));
    });
  });

  describe('retry and run again', () => {
    it('replaces the failed file with the new job and starts polling', fakeAsync(() => {
      const failed = job({ jobGuid: 'old', status: 'FAILED', retryable: true });
      service.jobs.set([failed, job({ jobGuid: 'other', reportType: 'RESULTS_SPATIAL', status: 'READY' })]);
      api.retry.and.returnValue(of(job({ jobGuid: 'new' })));

      service.retry(failed);

      expect(api.retry).toHaveBeenCalledWith('old');
      expect(service.jobs().map(j => j.jobGuid)).toEqual(['new', 'other']);
      expect(service.state()).toBe('preparing');
      discardPolls();
    }));

    it('runs every finished file of an expired export again', fakeAsync(() => {
      service.jobs.set([
        job({ jobGuid: 'xlsx', status: 'READY', expired: true }),
        job({ jobGuid: 'zip', reportType: 'RESULTS_SPATIAL', status: 'NO_FILES' })
      ]);
      api.retry.and.callFake(guid => of(job({ jobGuid: `${guid}-again` })));

      service.runAgain(service.exports()[0]);

      expect(api.retry.calls.allArgs().map(args => args[0])).toEqual(['xlsx', 'zip']);
      discardPolls();
    }));
  });

  it('clearFinished keeps only files still being prepared', () => {
    service.jobs.set([job({ jobGuid: 'a' }), job({ jobGuid: 'b', status: 'READY' }), job({ jobGuid: 'c', status: 'FAILED' })]);
    api.clearFinished.and.returnValue(of(undefined));

    service.clearFinished();

    expect(service.jobs().map(j => j.jobGuid)).toEqual(['a']);
  });

  it('state shows failures before files ready to save', () => {
    service.jobs.set([job({ status: 'READY' }), job({ jobGuid: 'b', status: 'FAILED' })]);
    expect(service.state()).toBe('failed');

    service.jobs.set([job({ status: 'READY', downloaded: true })]);
    expect(service.state()).toBe('done');
  });

  describe('groupExports', () => {
    it('groups files by download, newest download first, workbooks before ZIPs', () => {
      const exports = groupExports([
        job({ jobGuid: 'zip', reportType: 'RESULTS_SPATIAL', exportGroupGuid: 'old' , requestTimestamp: '2026-09-24T10:00:00Z' }),
        job({ jobGuid: 'fiscal', reportType: 'PROJECT_XLSX', exportGroupGuid: 'new', requestTimestamp: '2026-09-24T12:00:00Z' }),
        job({ jobGuid: 'xlsx', reportType: 'RESULTS_XLSX', exportGroupGuid: 'old', requestTimestamp: '2026-09-24T10:00:00Z' })
      ]);

      expect(exports.map(e => e.exportGroupGuid)).toEqual(['new', 'old']);
      expect(exports[1].jobs.map(j => j.jobGuid)).toEqual(['xlsx', 'zip']);
    });

    it('keeps the time of the first request when a file is retried', () => {
      const [download] = groupExports([
        job({ jobGuid: 'retried', requestTimestamp: '2026-09-24T11:00:00Z' }),
        job({ jobGuid: 'zip', reportType: 'RESULTS_SPATIAL', requestTimestamp: '2026-09-24T10:00:00Z' })
      ]);

      expect(download.requestTimestamp).toBe('2026-09-24T10:00:00Z');
    });

    it('treats a download as expired once none of its files can be saved', () => {
      expect(groupExports([job({ status: 'READY', expired: true }), job({ jobGuid: 'z', status: 'NO_FILES' })])[0].expired).toBeTrue();
      expect(groupExports([job({ status: 'READY', expired: true }), job({ jobGuid: 'z', status: 'FAILED' })])[0].expired).toBeFalse();
      expect(groupExports([job({ status: 'NO_FILES' })])[0].expired).toBeFalse();
    });
  });

  /** Stops the poll timer so fakeAsync ends cleanly. */
  function discardPolls() {
    service.ngOnDestroy();
    flush();
  }
});
