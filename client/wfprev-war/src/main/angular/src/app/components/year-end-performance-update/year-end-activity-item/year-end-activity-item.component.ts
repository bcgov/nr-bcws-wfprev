import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, OnChanges, Output, SimpleChanges } from '@angular/core';
import { FormBuilder, FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MomentDateAdapter } from '@angular/material-moment-adapter';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { DateAdapter, MAT_DATE_FORMATS, MAT_DATE_LOCALE, MatNativeDateModule } from '@angular/material/core';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { NgxCurrency } from '@dintecom/ngx-currency';
import { merge } from 'rxjs';
import { Messages, NumericLimits } from '../../../utils/constants';
import { nonNegativeValidator } from '../../../utils/validators';
import { ProjectFilesComponent } from '../../edit-project/project-details/project-files/project-files.component';
import { ActivityHeaderComponent } from '../../shared/activity-header/activity-header.component';
import { IconDisplayFieldComponent } from '../../shared/icon-display-field/icon-display-field.component';
import { TextareaComponent } from '../../shared/textarea/textarea.component';
import { ActivityStatus, ActivityStatusOptions } from '../../models';

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
    NgxCurrency,
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
  statusOptions = ActivityStatusOptions;
  isEmpty = (val: any) => val == null || val === '';

  constructor(private readonly fb: FormBuilder) { }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['activity'] && this.activity) {
      this.initForm();
    }
  }

  initForm() {
    const isCarryForwardInd = this.activity.isCarryForwardInd || false;
    const outstandingObligationsInd = this.activity.outstandingObligationsInd || false;
    const isCancelledStatus = this.activity.activityStatusCode?.activityStatusCode === ActivityStatus.Cancelled;

    const finalOutcomeValidators = [Validators.maxLength(500)];
    if (isCarryForwardInd) finalOutcomeValidators.push(Validators.required);

    const activityCommentValidators = [Validators.maxLength(500)];
    if (outstandingObligationsInd && !isCancelledStatus) activityCommentValidators.push(Validators.required);

    const isMissingInfo = this.isActivityMissingInfo(this.activity);

    this.form = this.fb.group({
      activityGuid: [this.activity.activityGuid],
      activityStatusCode: [this.activity.activityStatusCode?.activityStatusCode || '', Validators.required],
      reportedSpendAmount: [this.activity.reportedSpendAmount ?? '', [
        ...(isCancelledStatus ? [] : [Validators.required]),
        Validators.max(NumericLimits.MAX_NUMBER),
        nonNegativeValidator(() => this.form)
      ]],
      completedAreaHa: [this.activity.completedAreaHa ?? '', [
        ...(isCancelledStatus ? [] : [Validators.required]),
        Validators.max(NumericLimits.MAX_NUMBER),
        nonNegativeValidator(() => this.form)
      ]],
      activityDateRange: this.fb.group({
        activityStartDate: [this.activity.activityStartDate || '', Validators.required],
        activityEndDate: [this.activity.activityEndDate || '', Validators.required]
      }),
      isCarryForwardInd: [isCarryForwardInd],
      finalOutcomeComments: [this.activity.finalOutcomeComments || '', finalOutcomeValidators],
      outstandingObligationsInd: [outstandingObligationsInd],
      activityComment: [this.activity.activityComment || '', activityCommentValidators],
      isMissingInfo: [isMissingInfo]
    });

    // Default completedAreaHa to 0 if not cancelled and no value returned from API
    if (this.activity.activityStatusCode?.activityStatusCode !== ActivityStatus.Cancelled &&
      this.activity.completedAreaHa == null) {
      this.form.get('completedAreaHa')?.setValue(0);
    }

    // Default reportedSpendAmount to 0 if not cancelled and no value returned from API
    if (this.activity.activityStatusCode?.activityStatusCode !== ActivityStatus.Cancelled &&
      this.activity.reportedSpendAmount == null) {
      this.form.get('reportedSpendAmount')?.setValue(0);
    }

    // Update conditional validations
    this.form.get('isCarryForwardInd')?.valueChanges.subscribe(val => {
      const ctrl = this.form.get('finalOutcomeComments');
      if (val) ctrl?.setValidators([Validators.required, Validators.maxLength(500)]);
      else ctrl?.setValidators([Validators.maxLength(500)]);
      ctrl?.updateValueAndValidity();
    });

    // When outstanding obligations checkbox changes, conditionally require the description field.
    // If outstanding obligations are checked and status is not Cancelled, description is required.
    // Cancelled activities never require the obligations description regardless of checkbox state.
    this.form.get('outstandingObligationsInd')?.valueChanges.subscribe(val => {
      const ctrl = this.form.get('activityComment');
      const isCancelled = this.form.get('activityStatusCode')?.value === ActivityStatus.Cancelled;
      if (val && !isCancelled) ctrl?.setValidators([Validators.required, Validators.maxLength(500)]);
      else ctrl?.setValidators([Validators.maxLength(500)]);
      ctrl?.updateValueAndValidity();
      this.updateMissingInfo();
    });

    // When activity status changes, toggle required validators based on whether status is Cancelled.
    // Cancelled activities do not require spend, hectares, or obligations description.
    this.form.get('activityStatusCode')?.valueChanges.subscribe(status => {
      const isCancelled = status === ActivityStatus.Cancelled;
      const spendCtrl = this.form.get('reportedSpendAmount');
      const haCtrl = this.form.get('completedAreaHa');
      const activityCommentCtrl = this.form.get('activityComment');

      if (isCancelled) {
        // Remove required validators — Cancelled activities only need status, dates, and final outcome comments
        spendCtrl?.removeValidators(Validators.required);
        haCtrl?.removeValidators(Validators.required);
        activityCommentCtrl?.removeValidators(Validators.required);
      } else {
        // Restore required validators for all other statuses
        spendCtrl?.addValidators(Validators.required);
        haCtrl?.addValidators(Validators.required);
        // Only re-add required to activityComment if outstanding obligations is checked
        if (this.form.get('outstandingObligationsInd')?.value) {
          activityCommentCtrl?.addValidators(Validators.required);
        }
      }

      spendCtrl?.updateValueAndValidity();
      haCtrl?.updateValueAndValidity();
      activityCommentCtrl?.updateValueAndValidity();
      this.updateMissingInfo();
    })

    this.form.get('activityComment')?.valueChanges.subscribe(() => {
      this.updateMissingInfo();
    });

    this.form.get('finalOutcomeComments')?.valueChanges.subscribe(() => {
      this.updateMissingInfo();
    });

    // display missing info badge if any of the required fields are missing or invalid
    const missingInfoFields = ['activityStatusCode', 'reportedSpendAmount', 'completedAreaHa'];
    const dateGroup = this.form.get('activityDateRange');

    merge(
      ...missingInfoFields.map(f => this.form.get(f)!.valueChanges),
      dateGroup!.get('activityStartDate')!.valueChanges,
      dateGroup!.get('activityEndDate')!.valueChanges
    ).subscribe(() => {
      this.updateMissingInfo();
    });

    if (this.isReadonly) {
      this.form.disable();
    }

    this.updateMissingInfo();
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

  onFilesUpdated(): void {
    this.filesUpdated.emit();
    this.updateMissingInfo();
  }

  updateMissingInfo(): void {
    // isSpatialAddedInd and isResultsReportableInd are not present in the form, get them from the API
    const activity = {
      ...this.form.getRawValue(),
      isSpatialAddedInd: this.activity.isSpatialAddedInd,
      isResultsReportableInd: this.activity.isResultsReportableInd
    };
    const isMissingInfo = this.isActivityMissingInfo(activity);
    this.form.get('isMissingInfo')?.setValue(isMissingInfo, { emitEvent: false });
  }

  // Determines whether an activity is missing required information based on its status.
  // Routes to the appropriate validation method depending on the activity status code.
  // Returns true (missing info) if no status code is set.
  isActivityMissingInfo(activity: any) {
    const code = activity?.activityStatusCode?.activityStatusCode ?? activity?.activityStatusCode;
    if (!code) return true;
    if ([ActivityStatus.InProgress, ActivityStatus.SubstantiallyComplete, ActivityStatus.Completed].includes(code)) {
      return this.isActivityMissingInformation(activity);
    } else if (code === ActivityStatus.Deferred) {
      return this.isDeferredActivityMissingInfo(activity);
    } else if (code === ActivityStatus.Cancelled) {
      return this.isCancelledActivityMissingInfo(activity);
    }
    return false;
  }

  // Validates required fields for InProgress, SubstantiallyComplete, and Completed activities.
  // All fields are required. Additionally:
  // - If reportedSpendAmount is $0, finalOutcomeComments must be provided to explain why.
  // - If isResultsReportableInd is true, a spatial file must be uploaded.
  // - If outstandingObligationsInd is checked, activityComment must be provided.
  isActivityMissingInformation(activity: any): boolean {
    const activityStatusCodeEmpty = this.isEmpty(activity.activityStatusCode);
    const reportedSpendAmountEmpty = this.isEmpty(activity.reportedSpendAmount) && activity.reportedSpendAmount !== 0;
    const completedHectaresEmpty = this.isEmpty(activity.completedAreaHa) && activity.completedAreaHa !== 0;
    // $0 spend requires a comment explaining the reason
    const reportedSpendZeroMissingComments = activity.reportedSpendAmount === 0 && this.isEmpty(activity.finalOutcomeComments);
    // Spatial file required only when activity is results-reportable
    const spatialMissing = !activity.isSpatialAddedInd && activity.isResultsReportableInd;
    // Obligations description required only when outstanding obligations is checked
    const obligationMissingDesc = activity.outstandingObligationsInd && this.isEmpty(activity.activityComment);
    const startDateMissing = this.isEmpty(activity.activityDateRange?.activityStartDate ?? activity.activityStartDate);
    const endDateMissing = this.isEmpty(activity.activityDateRange?.activityEndDate ?? activity.activityEndDate);

    return activityStatusCodeEmpty || reportedSpendAmountEmpty || completedHectaresEmpty || reportedSpendZeroMissingComments || spatialMissing || obligationMissingDesc || startDateMissing || endDateMissing;
  }

  // Validates required fields for Deferred activities.
  // Spend and hectares default to $0/0 but cannot be blank.
  // Final outcome comments are always required to explain the deferral reason.
  // Obligations description required only when outstanding obligations is checked.
  isDeferredActivityMissingInfo(activity: any): boolean {
    const activityStatusCodeEmpty = this.isEmpty(activity.activityStatusCode);
    // 0 is a valid value — only blank/null counts as missing
    const reportedSpendAmountEmpty = this.isEmpty(activity.reportedSpendAmount) && activity.reportedSpendAmount !== 0;
    const completedHectaresEmpty = this.isEmpty(activity.completedAreaHa) && activity.completedAreaHa !== 0;
    // Always required for deferred — must explain why activity was deferred
    const finalOutcomeCommentsEmpty = this.isEmpty(activity.finalOutcomeComments);
    const obligationsNoDescription = activity.outstandingObligationsInd && this.isEmpty(activity.activityComment);
    const startDateMissing = this.isEmpty(activity.activityDateRange?.activityStartDate ?? activity.activityStartDate);
    const endDateMissing = this.isEmpty(activity.activityDateRange?.activityEndDate ?? activity.actityEndDate);

    return activityStatusCodeEmpty || reportedSpendAmountEmpty || completedHectaresEmpty || finalOutcomeCommentsEmpty || obligationsNoDescription || startDateMissing || endDateMissing;
  }

  // Validates required fields for Cancelled activities.
  // Spend and hectares are not required for cancelled activities.
  // Final outcome comments are always required to explain the cancellation reason.
  // Outstanding obligations description is not required regardless of checkbox state.
  isCancelledActivityMissingInfo(activity: any): boolean {
    const activityStatusCodeEmpty = this.isEmpty(activity.activityStatusCode);
    // Always required for cancelled — must explain why activity was cancelled
    const finalOutcomeCommentsEmpty = this.isEmpty(activity.finalOutcomeComments);
    const startDateMissing = this.isEmpty(activity.activityDateRange?.activityStartDate ?? activity.activityStartDate);
    const endDateMissing = this.isEmpty(activity.activityDateRange?.activityEndDate ?? activity.actityEndDate);

    return activityStatusCodeEmpty || finalOutcomeCommentsEmpty || startDateMissing || endDateMissing;
  }
}
