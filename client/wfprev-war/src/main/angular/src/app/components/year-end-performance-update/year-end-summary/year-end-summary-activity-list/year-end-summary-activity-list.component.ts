import { Component, Input } from '@angular/core';
import { YearEndActivityViewModel } from '../../../models';
import { YearEndSummaryActivityItemComponent } from '../year-end-summary-activity-item/year-end-summary-activity-item.component';

@Component({
  selector: 'wfprev-year-end-summary-activity-list',
  imports: [YearEndSummaryActivityItemComponent],
  templateUrl: './year-end-summary-activity-list.component.html',
  styleUrl: './year-end-summary-activity-list.component.scss',
  standalone: true
})
export class YearEndSummaryActivityListComponent {
  @Input() activities: YearEndActivityViewModel[] = [];
  @Input() fiscalGuid!: string;
}