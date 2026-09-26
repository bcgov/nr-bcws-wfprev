import { ClipboardModule } from '@angular/cdk/clipboard';
import { Component, OnInit, inject } from '@angular/core';
import { MatMenuModule } from '@angular/material/menu';
import { MatTooltipModule } from '@angular/material/tooltip';
import { ReportJob } from 'src/app/components/models';
import { DownloadTrayService, TrayExport } from 'src/app/services/download-tray.service';
import { filterDescriptionLines, filterSummary, formatRequestTime } from 'src/app/utils/report-export-format';

/**
 * The download tray: report exports in progress and finished, bottom right on every page.
 * Mounted once in the app shell.
 */
@Component({
  selector: 'wfprev-download-tray',
  standalone: true,
  imports: [MatTooltipModule, MatMenuModule, ClipboardModule],
  templateUrl: './download-tray.component.html',
  styleUrls: ['./download-tray.component.scss']
})
export class DownloadTrayComponent implements OnInit {
  readonly tray = inject(DownloadTrayService);

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
      : 'All projects';
  }

  fileLabel(job: ReportJob): string {
    return job.status === 'NO_FILES' ? 'No spatial files' : job.fileName;
  }

  statusText(job: ReportJob): string {
    switch (job.status) {
      case 'PREPARING': return 'Preparing…';
      case 'FAILED': return 'Failed';
      case 'NO_FILES': return 'None of these projects have any';
      default:
        if (job.expired) return 'Expired';
        return job.downloaded ? 'Saved' : 'Ready';
    }
  }

  isSavable(job: ReportJob): boolean {
    return job.status === 'READY' && !job.expired;
  }

  /** A failed file that retrying can fix, or an expired one that can be run again. */
  isRetryable(job: ReportJob): boolean {
    return (job.status === 'FAILED' && job.retryable !== false) || (job.status === 'READY' && job.expired);
  }
}
