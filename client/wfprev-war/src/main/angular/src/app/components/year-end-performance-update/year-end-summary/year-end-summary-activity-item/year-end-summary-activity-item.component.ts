import { Component, Input } from '@angular/core';
import { ProjectFilesComponent } from 'src/app/components/edit-project/project-details/project-files/project-files.component';
import { YearEndActivityViewModel } from '../../../models';
import { StatusBadgeComponent } from '../../../shared/status-badge/status-badge.component';

@Component({
  selector: 'wfprev-year-end-summary-activity-item',
  imports: [StatusBadgeComponent, ProjectFilesComponent],
  templateUrl: './year-end-summary-activity-item.component.html',
  styleUrl: './year-end-summary-activity-item.component.scss',
  standalone: true
})
export class YearEndSummaryActivityItemComponent {
  @Input() activity!: YearEndActivityViewModel;
  @Input() fiscalGuid!: string;

  get statusBadgeType(): string | null {
    const code = this.activity.data.activityStatusCode?.activityStatusCode;
    switch (code) {
      case 'ACTIVE': return 'status-in-progress'
      case 'DEFERRED': return 'deferred-filled';
      case 'CANCELLED': return 'cancelled';
      case 'COMPLETED': return 'status-complete';
      case 'SUBS_COMPL': return 'substantially-complete';
      default: return null;
    }
  }
}