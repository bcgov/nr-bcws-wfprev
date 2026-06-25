import { Component, Input, Output, EventEmitter, ViewChildren, QueryList } from '@angular/core';
import { CommonModule } from '@angular/common';
import { YearEndActivityItemComponent } from '../year-end-activity-item/year-end-activity-item.component';
import { YearEndActivityViewModel } from 'src/app/components/models';

@Component({
  selector: 'wfprev-year-end-activities',
  standalone: true,
  imports: [CommonModule, YearEndActivityItemComponent],
  templateUrl: './year-end-activities.component.html',
  styleUrl: './year-end-activities.component.scss'
})
export class YearEndActivitiesComponent {
  @Input() activityViews: YearEndActivityViewModel[] = [];
  @Input() fiscalGuid: string = '';

  @Output() saveActivity = new EventEmitter<{ view: YearEndActivityViewModel, event: any }>();
  @Output() filesUpdated = new EventEmitter<void>();

  @ViewChildren(YearEndActivityItemComponent) activityItems!: QueryList<YearEndActivityItemComponent>;

  getActivityData(): any[] {
    if (!this.activityItems) return [];
    return this.activityItems.map(item => ({
      ...item.activity,
      ...item.form.getRawValue()
    }));
  }

  isAllValid(): boolean {
    if (!this.activityItems) return true;
    return this.activityItems.toArray().every(item => item.form.valid && !item.form.getRawValue().isMissingInfo);
  }
}
