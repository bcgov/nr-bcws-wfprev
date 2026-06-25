import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, Output, OnInit, OnChanges, SimpleChanges } from '@angular/core';
import { FormGroup, FormControl, ReactiveFormsModule } from '@angular/forms';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatInputModule } from '@angular/material/input';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { NgxCurrency } from '@dintecom/ngx-currency';
import { ProjectFilesComponent } from '../../project-details/project-files/project-files.component';
import { IconButtonComponent } from '../../../shared/icon-button/icon-button.component';
import { TextareaComponent } from '../../../shared/textarea/textarea.component';
import { TimestampComponent } from '../../../shared/timestamp/timestamp.component';
import { ActivityHeaderComponent } from '../../../shared/activity-header/activity-header.component';
import { Messages } from '../../../../utils/constants';

@Component({
  selector: 'wfprev-fiscal-activity-item',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatExpansionModule,
    MatSlideToggleModule,
    MatDatepickerModule,
    MatInputModule,
    MatCheckboxModule,
    ProjectFilesComponent,
    TimestampComponent,
    TextareaComponent,
    NgxCurrency,
    MatProgressSpinnerModule,
    ActivityHeaderComponent
  ],
  templateUrl: './fiscal-activity-item.component.html',
  styleUrl: './fiscal-activity-item.component.scss'
})
export class FiscalActivityItemComponent implements OnInit, OnChanges {
  @Input() activityForm!: FormGroup;
  @Input() activity: any;
  @Input() index!: number;
  @Input() isExpanded: boolean = false;
  @Input() isReadonly: boolean = false;
  @Input() fiscalGuid: string = '';
  @Input() isActivityDirty: boolean = false;
  @Input() isActivitySaving: boolean = false;
  @Input() canDelete: boolean = false;
  @Input() deleteIconUrl: string = '';

  @Input() silvicultureBaseCode: any[] = [];
  @Input() contractPhaseCode: any[] = [];
  @Input() fundingSourceCode: any[] = [];

  @Output() expandedChange = new EventEmitter<boolean>();
  @Output() toggleResultsReportable = new EventEmitter<void>();
  @Output() toggleActivityStatus = new EventEmitter<void>();
  @Output() delete = new EventEmitter<void>();
  @Output() cancel = new EventEmitter<void>();
  @Output() save = new EventEmitter<void>();
  @Output() filesUpdated = new EventEmitter<void>();

  messages = Messages;

  statusOptions = [
    { value: 'ACTIVE', label: 'Active' },
    { value: 'COMPLETED', label: 'Completed' },
    { value: 'CANCELLED', label: 'Cancelled' },
    { value: 'DEFERRED', label: 'Deferred' },
    { value: 'SUBS_COMPL', label: 'Substantially Complete' }
  ];

  getControl(controlName: string): FormControl {
    return this.activityForm.get(controlName) as FormControl;
  }

  ngOnInit() {
    this.updateFormDisableState();
  }

  ngOnChanges(changes: SimpleChanges) {
    if (changes['isReadonly'] || changes['activityForm']) {
      this.updateFormDisableState();
    }
  }

  updateFormDisableState() {
    if (this.isReadonly) {
      this.activityForm.disable({ emitEvent: false });
    } else {
      this.activityForm.enable({ emitEvent: false });
    }
  }

  getActivityTitle(): string {
    const activityData = this.activityForm.getRawValue();
    if (!activityData) return '';

    if (activityData.isResultsReportableInd) {
      const parts: string[] = [];
      const base = this.silvicultureBaseCode?.find(b => b.silvicultureBaseGuid === activityData.silvicultureBaseGuid)?.description;
      const techniqueGuid = activityData.silvicultureTechniqueGuid;
      const methodGuid = activityData.silvicultureMethodGuid;
      
      const filteredTechniques = this.activityForm.get('filteredTechniqueCode')?.value || [];
      const filteredMethods = this.activityForm.get('filteredMethodCode')?.value || [];
      
      const technique = filteredTechniques.find((t: any) => t.silvicultureTechniqueGuid === techniqueGuid)?.description;
      const method = filteredMethods.find((m: any) => m.silvicultureMethodGuid === methodGuid)?.description;

      if (base) parts.push(base);
      if (technique) parts.push(technique);
      if (method) parts.push(method);

      return parts.length ? parts.join(' - ') : '';
    }

    return activityData.activityName?.trim() || '';
  }
}
