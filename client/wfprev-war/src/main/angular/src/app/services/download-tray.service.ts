import { Injectable, OnDestroy, computed, inject, signal } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';
import { UUID } from 'angular2-uuid';
import { forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { ReportJob, ReportRequest, ReportType } from 'src/app/components/models';
import { PermissionsService, WFPREV_ACTIONS } from 'src/app/services/permissions.service';
import { ReportJobService } from 'src/app/services/report-job.service';
import { TokenService } from 'src/app/services/token.service';
import { Messages } from 'src/app/utils/constants';

/** The files of one download, e.g. the RESULTS workbook and its spatial ZIP. */
export interface TrayExport {
  exportGroupGuid: string;
  requestTimestamp: string;
  description: string;
  jobs: ReportJob[];
  /** Every file is gone after the retention period; the export can be run again. */
  expired: boolean;
}

export type TrayState = 'preparing' | 'failed' | 'ready' | 'done';

/** Workbooks before ZIPs, as the download menu lists them. */
const FILE_ORDER: ReportType[] = ['PROJECT_XLSX', 'PROJECT_CSV', 'RESULTS_XLSX', 'RESULTS_CSV', 'RESULTS_SPATIAL'];
const FAST_POLL_MS = 3000;
const SLOW_POLL_MS = 10000;
const FAST_POLL_FOR_MS = 60000;

/**
 * The download tray's state. Report exports run on the server; this lists the user's recent ones,
 * polls while any file is being prepared, and hands out download links on request.
 */
@Injectable({ providedIn: 'root' })
export class DownloadTrayService implements OnDestroy {
  private readonly reportJobService = inject(ReportJobService);
  private readonly permissions = inject(PermissionsService);
  private readonly tokenService = inject(TokenService);
  private readonly snackbar = inject(MatSnackBar);

  readonly jobs = signal<ReportJob[]>([]);
  /** Whether the tray is on screen at all. */
  readonly visible = signal(false);
  readonly expanded = signal(true);
  /** Announced to screen readers through the tray's live region. */
  readonly announcement = signal('');

  readonly exports = computed<TrayExport[]>(() => groupExports(this.jobs()));

  readonly state = computed<TrayState>(() => {
    const jobs = this.jobs();
    if (jobs.some(job => job.status === 'PREPARING')) {
      return 'preparing';
    }
    if (jobs.some(job => job.status === 'FAILED')) {
      return 'failed';
    }
    if (jobs.some(job => job.status === 'READY' && !job.expired && !job.downloaded)) {
      return 'ready';
    }
    return 'done';
  });

  readonly hasFinished = computed(() => this.jobs().some(job => job.status !== 'PREPARING'));

  private pollTimer: ReturnType<typeof setTimeout> | null = null;
  private pollStartedAt = 0;
  private started = false;

  /** Loads the user's recent exports once they have signed in. Safe to call more than once. */
  init(): void {
    if (this.started) {
      return;
    }
    this.started = true;
    this.tokenService.credentialsEmitter.subscribe(() => {
      if (this.permissions.hasAction(WFPREV_ACTIONS.EXPORT_TO_RESULTS)) {
        this.refresh(true);
      }
    });
  }

  /**
   * Starts one job per report type, all in one export group so they show together.
   * The tray opens straight away with the files as "Preparing…".
   */
  start(reportTypes: ReportType[], body: ReportRequest, description: string): void {
    const exportGroupGuid = UUID.UUID();
    this.visible.set(true);
    this.expanded.set(true);
    forkJoin(reportTypes.map(reportType =>
      this.reportJobService.create({ ...body, reportType, description, exportGroupGuid }).pipe(
        catchError(error => {
          console.error(`Couldn't start the ${reportType} export`, error);
          return of(null);
        })
      )
    )).subscribe(created => {
      const jobs = created.filter((job): job is ReportJob => !!job);
      if (jobs.length < reportTypes.length) {
        this.snackbar.open(Messages.fileDownloadStartFailure, 'Close', { duration: 8000, panelClass: 'snackbar-error' });
      }
      if (jobs.length) {
        this.jobs.update(current => [...jobs, ...current]);
        this.schedulePoll(true);
      } else if (!this.jobs().length) {
        this.visible.set(false);
      }
    });
  }

  /** Save: fetch a fresh short-lived link and let the browser download it. */
  save(job: ReportJob): void {
    this.reportJobService.downloadUrl(job.jobGuid).subscribe({
      next: link => {
        this.openLink(link.url);
        this.replace(job.jobGuid, { ...job, downloaded: true });
      },
      error: error => {
        console.error('Download link failed', error);
        if (error?.status === 409) {
          // The file expired since the list was loaded.
          this.refresh(false);
        } else {
          this.snackbar.open(Messages.fileDownloadFailure, 'Close', { duration: 5000, panelClass: 'snackbar-error' });
        }
      }
    });
  }

  /** Retry one failed file, with the filters it was first requested with. */
  retry(job: ReportJob): void {
    this.reportJobService.retry(job.jobGuid).subscribe({
      next: fresh => {
        this.jobs.update(current => current.map(existing => existing.jobGuid === job.jobGuid ? fresh : existing));
        this.schedulePoll(true);
      },
      error: error => {
        console.error('Retry failed', error);
        this.snackbar.open(Messages.fileDownloadStartFailure, 'Close', { duration: 8000, panelClass: 'snackbar-error' });
      }
    });
  }

  /** Run again: every file of an expired export. */
  runAgain(tray: TrayExport): void {
    tray.jobs.filter(job => job.status !== 'PREPARING').forEach(job => this.retry(job));
  }

  clearFinished(): void {
    this.reportJobService.clearFinished().subscribe({
      next: () => {
        this.jobs.update(current => current.filter(job => job.status === 'PREPARING'));
        if (!this.jobs().length) {
          this.visible.set(false);
        }
      },
      error: error => console.error('Clear finished failed', error)
    });
  }

  toggleExpanded(): void {
    this.expanded.update(expanded => !expanded);
  }

  hide(): void {
    this.visible.set(false);
  }

  /** Reopens the tray after it was hidden, with a fresh list. */
  show(): void {
    this.visible.set(true);
    this.expanded.set(true);
    this.refresh(false);
  }

  toggleVisible(): void {
    if (this.visible()) {
      this.hide();
    } else {
      this.show();
    }
  }

  /** Reloads the list. On first load the tray only appears if there is something to show. */
  refresh(initial: boolean): void {
    this.reportJobService.list().subscribe({
      next: jobs => {
        this.announceFinished(this.jobs(), jobs);
        this.jobs.set(jobs);
        if (initial && jobs.some(job => job.status === 'PREPARING' || (job.status === 'READY' && !job.downloaded && !job.expired) || job.status === 'FAILED')) {
          this.visible.set(true);
        }
        this.schedulePoll(false);
      },
      error: error => {
        console.error('Loading downloads failed', error);
        this.schedulePoll(false);
      }
    });
  }

  ngOnDestroy(): void {
    this.clearPoll();
  }

  /** Separate so tests can stub it: opening the link starts the download. */
  openLink(url: string): void {
    const anchor = globalThis.document.createElement('a');
    anchor.href = url;
    anchor.rel = 'noopener';
    anchor.click();
  }

  private replace(jobGuid: string, job: ReportJob): void {
    this.jobs.update(current => current.map(existing => existing.jobGuid === jobGuid ? job : existing));
  }

  /** Polls every 3 s while anything is preparing, slowing to 10 s after a minute; stops when nothing is. */
  private schedulePoll(restartFast: boolean): void {
    this.clearPoll();
    if (!this.jobs().some(job => job.status === 'PREPARING')) {
      this.pollStartedAt = 0;
      return;
    }
    if (restartFast || !this.pollStartedAt) {
      this.pollStartedAt = Date.now();
    }
    const delay = Date.now() - this.pollStartedAt < FAST_POLL_FOR_MS ? FAST_POLL_MS : SLOW_POLL_MS;
    this.pollTimer = setTimeout(() => {
      this.pollTimer = null;
      this.refresh(false);
    }, delay);
  }

  private clearPoll(): void {
    if (this.pollTimer) {
      clearTimeout(this.pollTimer);
      this.pollTimer = null;
    }
  }

  /** Opens the tray and announces files that finished since the last poll. */
  private announceFinished(before: ReportJob[], after: ReportJob[]): void {
    const wasPreparing = new Set(before.filter(job => job.status === 'PREPARING').map(job => job.jobGuid));
    const finished = after.filter(job => wasPreparing.has(job.jobGuid) && job.status !== 'PREPARING');
    if (!finished.length) {
      return;
    }
    this.visible.set(true);
    this.expanded.set(true);
    this.announcement.set(finished.map(job => {
      switch (job.status) {
        case 'READY': return `${job.fileName} is ready to save.`;
        case 'FAILED': return `${job.fileName} failed.`;
        default: return `${job.fileName}: no files to include.`;
      }
    }).join(' '));
  }
}

/** Groups jobs into downloads, newest download first. */
export function groupExports(jobs: ReportJob[]): TrayExport[] {
  const groups = new Map<string, ReportJob[]>();
  for (const job of jobs) {
    const group = groups.get(job.exportGroupGuid) ?? [];
    group.push(job);
    groups.set(job.exportGroupGuid, group);
  }
  return Array.from(groups.entries())
    .map(([exportGroupGuid, groupJobs]) => {
      const sorted = [...groupJobs].sort((a, b) => FILE_ORDER.indexOf(a.reportType) - FILE_ORDER.indexOf(b.reportType));
      // The download's time is when it was first requested; a retried file keeps its place.
      const requestTimestamp = groupJobs.map(job => job.requestTimestamp).sort((a, b) => a.localeCompare(b))[0];
      return {
        exportGroupGuid,
        requestTimestamp,
        description: sorted[0].description,
        jobs: sorted,
        expired: sorted.every(job => job.expired || job.status === 'NO_FILES') && sorted.some(job => job.expired)
      };
    })
    .sort((a, b) => b.requestTimestamp.localeCompare(a.requestTimestamp));
}
