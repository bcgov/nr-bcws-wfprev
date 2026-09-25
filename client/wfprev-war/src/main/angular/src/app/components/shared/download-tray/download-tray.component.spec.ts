import { computed, signal } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { ReportJob } from 'src/app/components/models';
import { DownloadTrayService, TrayState, groupExports } from 'src/app/services/download-tray.service';
import { DownloadTrayComponent } from './download-tray.component';

function job(overrides: Partial<ReportJob> = {}): ReportJob {
  return {
    jobGuid: 'job-1',
    exportGroupGuid: 'group-1',
    reportType: 'RESULTS_XLSX',
    fileName: 'ReMi_RESULTS.xlsx',
    description: 'Search: fuel break\nFire centres: Coastal Fire Centre',
    status: 'READY',
    expired: false,
    downloaded: false,
    reference: 'ABCD1234',
    requestTimestamp: new Date().toISOString(),
    ...overrides
  };
}

describe('DownloadTrayComponent', () => {
  let fixture: ComponentFixture<DownloadTrayComponent>;
  let tray: any;

  beforeEach(async () => {
    const jobs = signal<ReportJob[]>([]);
    tray = {
      jobs,
      visible: signal(true),
      expanded: signal(true),
      announcement: signal(''),
      exports: computed(() => groupExports(jobs())),
      state: signal<TrayState>('ready'),
      hasFinished: computed(() => jobs().some(j => j.status !== 'PREPARING')),
      init: jasmine.createSpy('init'),
      save: jasmine.createSpy('save'),
      retry: jasmine.createSpy('retry'),
      runAgain: jasmine.createSpy('runAgain'),
      clearFinished: jasmine.createSpy('clearFinished'),
      toggleExpanded: jasmine.createSpy('toggleExpanded'),
      hide: jasmine.createSpy('hide')
    };

    await TestBed.configureTestingModule({
      imports: [DownloadTrayComponent, NoopAnimationsModule],
      providers: [{ provide: DownloadTrayService, useValue: tray }]
    }).compileComponents();

    fixture = TestBed.createComponent(DownloadTrayComponent);
  });

  function render(jobs: ReportJob[]): HTMLElement {
    tray.jobs.set(jobs);
    fixture.detectChanges();
    return fixture.nativeElement as HTMLElement;
  }

  const text = (root: HTMLElement, selector: string) =>
    Array.from(root.querySelectorAll(selector)).map(el => el.textContent?.replace(/\s+/g, ' ').trim());

  it('loads the list when created', () => {
    render([]);
    expect(tray.init).toHaveBeenCalled();
  });

  it('renders nothing while hidden', () => {
    tray.visible.set(false);
    const root = render([job()]);
    expect(root.querySelector('.download-tray')).toBeNull();
  });

  it('shows each download with its time, filter summary and files', () => {
    const root = render([
      job({ status: 'READY' }),
      job({ jobGuid: 'zip', reportType: 'RESULTS_SPATIAL', fileName: 'ReMi_RESULTS_Spatial.zip', status: 'PREPARING' })
    ]);

    expect(text(root, '.tray-title')).toEqual(['Downloads']);
    expect(text(root, '.export-filters')).toEqual(['“fuel break” · Coastal Fire Centre']);
    expect(text(root, '.file-name')).toEqual(['ReMi_RESULTS.xlsx', 'ReMi_RESULTS_Spatial.zip']);
    expect(text(root, '.file-status')).toEqual(['Ready', 'Preparing…']);
    expect(root.querySelectorAll('.file-spinner').length).toBe(1);
  });

  it('saves a ready file with a button named for it', () => {
    const root = render([job({ status: 'READY' })]);
    const save = root.querySelector('button[aria-label="Save ReMi_RESULTS.xlsx"]') as HTMLButtonElement;

    save.click();

    expect(tray.save).toHaveBeenCalledWith(jasmine.objectContaining({ jobGuid: 'job-1' }));
  });

  it('offers Save again once a file has been saved', () => {
    const root = render([job({ status: 'READY', downloaded: true })]);

    expect(text(root, '.file-status')).toEqual(['Saved']);
    expect(root.querySelector('button[aria-label="Save ReMi_RESULTS.xlsx again"]')).not.toBeNull();
  });

  it('offers Retry for a failure retrying can fix, with the details behind a link', () => {
    const root = render([job({ status: 'FAILED', retryable: true, errorMessage: 'Something went wrong. Try again.' })]);

    expect(text(root, '.file-status')[0]).toContain('Failed');
    expect(root.querySelector('button[aria-label="Why ReMi_RESULTS.xlsx failed"]')).not.toBeNull();
    (root.querySelector('button[aria-label="Retry ReMi_RESULTS.xlsx"]') as HTMLButtonElement).click();
    expect(tray.retry).toHaveBeenCalled();
  });

  it('shows the message and reference in the failure details', () => {
    const root = render([job({ status: 'FAILED', retryable: false, errorMessage: 'No fiscal data matches these filters.' })]);

    (root.querySelector('button[aria-label="Why ReMi_RESULTS.xlsx failed"]') as HTMLButtonElement).click();
    fixture.detectChanges();

    const panel = document.querySelector('.details-panel') as HTMLElement;
    expect(panel.textContent).toContain('No fiscal data matches these filters.');
    expect(panel.textContent).toContain('ABCD1234');
    expect(root.querySelector('button[aria-label="Retry ReMi_RESULTS.xlsx"]')).toBeNull();
  });

  it('shows a spatial export with nothing to include without a button', () => {
    const root = render([job({ reportType: 'RESULTS_SPATIAL', fileName: 'ReMi_RESULTS_Spatial.zip', status: 'NO_FILES' })]);

    expect(text(root, '.file-name')).toEqual(['No spatial files']);
    expect(root.querySelectorAll('.tray-file .file-action').length).toBe(0);
  });

  it('collapses an expired download to its heading with Run again', () => {
    const root = render([job({ status: 'READY', expired: true })]);

    expect(text(root, '.export-time')[0]).toContain('Expired');
    expect(root.querySelector('.tray-file')).toBeNull();
    (root.querySelector('.export-heading .file-action') as HTMLButtonElement).click();
    expect(tray.runAgain).toHaveBeenCalled();
  });

  it('collapses, hides and clears from the header and footer', () => {
    const root = render([job()]);

    (root.querySelector('button[aria-label="Collapse downloads"]') as HTMLButtonElement).click();
    (root.querySelector('button[aria-label="Hide downloads"]') as HTMLButtonElement).click();
    (root.querySelector('.clear-button') as HTMLButtonElement).click();

    expect(tray.toggleExpanded).toHaveBeenCalled();
    expect(tray.hide).toHaveBeenCalled();
    expect(tray.clearFinished).toHaveBeenCalled();
  });

  it('shows only the header when collapsed', () => {
    tray.expanded.set(false);
    const root = render([job()]);

    expect(root.querySelector('.tray-body')).toBeNull();
    expect(root.querySelector('button[aria-label="Expand downloads"]')).not.toBeNull();
  });

  it('announces finished files to screen readers', () => {
    tray.announcement.set('ReMi_RESULTS.xlsx is ready to save.');
    const root = render([job()]);

    expect(root.querySelector('[aria-live="polite"]')?.textContent).toContain('ReMi_RESULTS.xlsx is ready to save.');
  });
});
