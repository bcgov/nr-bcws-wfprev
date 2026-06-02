import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, OnChanges, Output, SimpleChanges } from '@angular/core';
import { FormBuilder, FormGroup, FormControl, ReactiveFormsModule, Validators, AbstractControl, ValidationErrors } from '@angular/forms';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatIconModule } from '@angular/material/icon';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatInputModule } from '@angular/material/input';
import { DateAdapter, MAT_DATE_FORMATS, MAT_DATE_LOCALE, MatNativeDateModule } from '@angular/material/core';
import { MomentDateAdapter } from '@angular/material-moment-adapter';
import { NgxCurrencyDirective } from 'ngx-currency';
import { ProjectFilesComponent } from '../../edit-project/project-details/project-files/project-files.component';
import { TextareaComponent } from '../../shared/textarea/textarea.component';
import { ActivityHeaderComponent } from '../../shared/activity-header/activity-header.component';
import { Messages, NumericLimits } from '../../../utils/constants';
import { IconDisplayFieldComponent } from '../../shared/icon-display-field/icon-display-field.component';
import { nonZeroUnlessCancelledValidator } from '../../../utils/validators';

export const CUSTOM_DATE_FORMATS = {
  display: {
    dateInput: 'YYYY/MM/DD',
    monthYearLabel: 'YYYY MMM',
    dateA11yLabel: 'LL',
    monthYearA11yLabel: 'YYYY MMMM',
  },
  parse: { dateInput: 'YYYY/MM/DD' },
};

@Component({
  selector: 'wfprev-year-end-activity-item',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatExpansionModule,
    MatCheckboxModule,
    MatIconModule,
    MatDatepickerModule,
    MatInputModule,
    MatNativeDateModule,
    ProjectFilesComponent,
    TextareaComponent,
    NgxCurrencyDirective,
    ActivityHeaderComponent,
    IconDisplayFieldComponent
  ],
  templateUrl: './year-end-activity-item.component.html',
  styleUrl: './year-end-activity-item.component.scss',
  providers: [
    { provide: MAT_DATE_FORMATS, useValue: CUSTOM_DATE_FORMATS },
    { provide: MAT_DATE_LOCALE, useValue: 'en-CA' },
    { provide: DateAdapter, useClass: MomentDateAdapter, deps: [MAT_DATE_LOCALE] }
  ]
})
export class YearEndActivityItemComponent implements OnChanges {
  @Input() activity: any;
  @Input() index!: number;
  @Input() isExpanded: boolean = false;
  @Input() isReadonly: boolean = false;
  @Input() fiscalGuid: string = '';

  @Output() expandedChange = new EventEmitter<boolean>();
  @Output() save = new EventEmitter<any>();
  @Output() filesUpdated = new EventEmitter<void>();

  form!: FormGroup;
  messages = Messages;
  statusOptions = [
    { value: 'ACTIVE', label: 'Active' },
    { value: 'COMPLETED', label: 'Completed' },
    { value: 'CANCELLED', label: 'Cancelled' },
    { value: 'DEFERRED', label: 'Deferred' },
    { value: 'SUBSTANTIALLY_COMPLETE', label: 'Substantially Complete' }
  ];

  constructor(private fb: FormBuilder) {}

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['activity'] && this.activity) {
      this.initForm();
    }
  }

  initForm() {
    const isCarryForwardInd = this.activity.isCarryForwardInd || false;
    const outstandingObligationsInd = this.activity.outstandingObligationsInd || false;

    const finalOutcomeValidators = [Validators.maxLength(500)];
    if (isCarryForwardInd) finalOutcomeValidators.push(Validators.required);

    const activityCommentValidators = [Validators.maxLength(500)];
    if (outstandingObligationsInd) activityCommentValidators.push(Validators.required);

    this.form = this.fb.group({
      activityGuid: [this.activity.activityGuid],
      activityStatusCode: [this.activity.activityStatusCode?.activityStatusCode || '', Validators.required],
      reportedSpendAmount: [this.activity.reportedSpendAmount ?? '', [
        Validators.required, 
        Validators.max(NumericLimits.MAX_NUMBER),
        nonZeroUnlessCancelledValidator(() => this.form)
      ]],
      completedAreaHa: [this.activity.completedAreaHa ?? '', [
        Validators.required, 
        Validators.max(NumericLimits.MAX_NUMBER),
        nonZeroUnlessCancelledValidator(() => this.form)
      ]],
      activityDateRange: this.fb.group({
        activityStartDate: [this.activity.activityStartDate || '', Validators.required],
        activityEndDate: [this.activity.activityEndDate || '', Validators.required]
      }),
      isCarryForwardInd: [isCarryForwardInd],
      finalOutcomeComments: [this.activity.finalOutcomeComments || '', finalOutcomeValidators],
      outstandingObligationsInd: [outstandingObligationsInd],
      activityComment: [this.activity.activityComment || '', activityCommentValidators]
    });

    // Update conditional validations
    this.form.get('isCarryForwardInd')?.valueChanges.subscribe(val => {
      const ctrl = this.form.get('finalOutcomeComments');
      if (val) ctrl?.setValidators([Validators.required, Validators.maxLength(500)]);
      else ctrl?.setValidators([Validators.maxLength(500)]);
      ctrl?.updateValueAndValidity();
    });

    this.form.get('outstandingObligationsInd')?.valueChanges.subscribe(val => {
      const ctrl = this.form.get('activityComment');
      if (val) ctrl?.setValidators([Validators.required, Validators.maxLength(500)]);
      else ctrl?.setValidators([Validators.maxLength(500)]);
      ctrl?.updateValueAndValidity();
    });

    this.form.get('activityStatusCode')?.valueChanges.subscribe(() => {
      this.form.get('reportedSpendAmount')?.updateValueAndValidity();
      this.form.get('completedAreaHa')?.updateValueAndValidity();
    });

    if (this.isReadonly) {
      this.form.disable();
    }
  }

  getControl(controlName: string): FormControl {
    return this.form.get(controlName) as FormControl;
  }

  onSave() {
    if (this.form.valid) {
      const rawValue = this.form.getRawValue();
      const updatedData = {
        ...rawValue,
        activityStartDate: rawValue.activityDateRange?.activityStartDate ? new Date(rawValue.activityDateRange.activityStartDate).toISOString() : null,
        activityEndDate: rawValue.activityDateRange?.activityEndDate ? new Date(rawValue.activityDateRange.activityEndDate).toISOString() : null
      };
      delete updatedData.activityDateRange;
      this.save.emit({ ...this.activity, ...updatedData });
    }
  }
}
