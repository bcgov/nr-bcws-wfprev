import { ClipboardModule } from '@angular/cdk/clipboard';
import { Component, OnInit, inject } from '@angular/core';
import { MatIconModule, MatIconRegistry } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTooltipModule } from '@angular/material/tooltip';
import { DomSanitizer } from '@angular/platform-browser';
import { ReportJob } from 'src/app/components/models';
import { DownloadTrayService, TrayExport } from 'src/app/services/download-tray.service';
import { DownloadTrayText, ReportJobStatuses } from 'src/app/utils/constants';
import { filterDescriptionLines, filterSummary, formatRequestTime } from 'src/app/utils/report-export-format';

// Icons are inlined by the registry, so they take the surrounding text colour.
const TRAY_ICONS: Record<string, string> = {
  'chevron-down': 'assets/collapsed-indicator.svg',
  'chevron-up': 'assets/expanded-indicator.svg',
  close: 'assets/exit-icon.svg',
  download: 'assets/icons/download.svg',
  file: 'assets/icons/file.svg',
  info: 'assets/detail-icon.svg',
  refresh: 'assets/icons/refresh.svg'
};

/**
 * The download tray: report exports in progress and finished, bottom right on every page.
 * Mounted once in the app shell.
 */
@Component({
  selector: 'wfprev-download-tray',
  standalone: true,
  imports: [MatTooltipModule, MatMenuModule, MatIconModule, MatProgressSpinnerModule, ClipboardModule],
  templateUrl: './download-tray.component.html',
  styleUrls: ['./download-tray.component.scss']
})
export class DownloadTrayComponent implements OnInit {
  readonly tray = inject(DownloadTrayService);
  readonly DownloadTrayText = DownloadTrayText;
  readonly ReportJobStatuses = ReportJobStatuses;

  constructor() {
    const iconRegistry = inject(MatIconRegistry);
    const sanitizer = inject(DomSanitizer);
    for (const [name, url] of Object.entries(TRAY_ICONS)) {
      iconRegistry.addSvgIcon(name, sanitizer.bypassSecurityTrustResourceUrl(url));
    }
  }

  ngOnInit(): void {
    this.tray.init();
  }

  time(tray: TrayExport): string {
    return formatRequestTime(tray.requestTimestamp);
  }

  summary(tray: TrayExport): string {
    return filterSummary(tray.description);
  }

  tooltip(tray: TrayExport): string {
    const lines = filterDescriptionLines(tray.description);
    return lines.length
      ? lines.map(line => line.label ? `${line.label}: ${line.values}` : line.values).join('\n')
      : DownloadTrayText.NO_FILTERS;
  }

  fileLabel(job: ReportJob): string {
    return job.status === ReportJobStatuses.NO_FILES ? DownloadTrayText.NO_SPATIAL_FILES : job.fileName;
  }

  statusText(job: ReportJob): string {
    switch (job.status) {
      case ReportJobStatuses.PREPARING: return DownloadTrayText.PREPARING;
      case ReportJobStatuses.FAILED: return DownloadTrayText.FAILED;
      case ReportJobStatuses.NO_FILES: return DownloadTrayText.NO_SPATIAL_FILES_STATUS;
      default:
        if (job.expired) return DownloadTrayText.EXPIRED;
        return job.downloaded ? DownloadTrayText.SAVED : DownloadTrayText.READY;
    }
  }

  isSavable(job: ReportJob): boolean {
    return job.status === ReportJobStatuses.READY && !job.expired;
  }

  /** A failed file that retrying can fix, or an expired one that can be run again. */
  isRetryable(job: ReportJob): boolean {
    return (job.status === ReportJobStatuses.FAILED && job.retryable !== false)
      || (job.status === ReportJobStatuses.READY && job.expired);
  }
}
