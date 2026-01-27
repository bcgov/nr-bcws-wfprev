import { Component, Inject } from '@angular/core';
import { AbstractControl, FormControl, FormGroup, ValidationErrors, ValidatorFn, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialog, MatDialogClose, MatDialogRef } from '@angular/material/dialog';
import { Messages, ModalMessages, ModalTitles, NumericLimits } from 'src/app/utils/constants';
import { ForecastStatus, PerformanceUpdate, ProgressStatus, ReportingPeriod, Option, UpdateGeneralStatus } from '../models';
import { InputFieldComponent } from "../shared/input-field/input-field.component";
import { SelectFieldComponent } from '../shared/select-field/select-field.component';
import { TextareaComponent } from "../shared/textarea/textarea.component";
import { ProjectService } from 'src/app/services/project-services';
import { ConfirmationDialogComponent } from '../confirmation-dialog/confirmation-dialog.component';
import { CommonModule } from '@angular/common';
import { MatSnackBar } from '@angular/material/snack-bar';

@Component({
  selector: ' wfprev-performance-update-modal-window',
  standalone: true,
  imports: [CommonModule, SelectFieldComponent, MatDialogClose, TextareaComponent, InputFieldComponent],
  templateUrl: './wfprev-performance-update-modal-window.component.html',
  styleUrl: './wfprev-performance-update-modal-window.component.scss'
})
export class PerformanceUpdateModalWindowComponent {

  messages = Messages;

  form = new FormGroup({
    reportingPeriod: new FormControl(null, { validators: [Validators.required] }),
    progressStatus: new FormControl(ProgressStatus.OnTrack),
    generalUpdates: new FormControl("", { validators: [Validators.required] }),
    currentForecast: new FormControl<number>(this.data.currentForecast, {
      validators: [
        Validators.min(0),
        Validators.max(NumericLimits.MAX_NUMBER)
      ]
    }),

    revisedForecast: new FormControl<number | null>(null, {
      validators: [
        Validators.min(0),
        Validators.max(NumericLimits.MAX_NUMBER)
      ]
    }),

    forecastRationale: new FormControl("", { validators: [Validators.required] }),

    highRisk: new FormControl<number | null>(null, {
      validators: [
        Validators.min(0),
        Validators.max(NumericLimits.MAX_NUMBER)
      ]
    }),
    highRiskDescription: new FormControl({ value: '', disabled: true }, { validators: [Validators.required] }),

    mediumRisk: new FormControl<number | null>(null, {
      validators: [
        Validators.min(0),
        Validators.max(NumericLimits.MAX_NUMBER)
      ]
    }),
    mediumRiskDescription: new FormControl({ value: '', disabled: true }, { validators: [Validators.required] }),

    lowRisk: new FormControl<number | null>(null, {
      validators: [
        Validators.min(0),
        Validators.max(NumericLimits.MAX_NUMBER)
      ]
    }),
    lowRiskDescription: new FormControl({ value: '', disabled: true }, { validators: [Validators.required] }),

    complete: new FormControl<number | null>(null, {
      validators: [
        Validators.min(0),
        Validators.max(NumericLimits.MAX_NUMBER)
      ]
    }),
    completeDescription: new FormControl({ value: '', disabled: true }, { validators: [Validators.required] })
  }, {
    validators: this.matchTotalValidator()
  });

  totalAmount = 0;

  reportingPeriod: Option<ReportingPeriod>[] = [
    { value: ReportingPeriod.Q1, description: 'End of Q1' },
    { value: ReportingPeriod.Q2, description: 'End of Q2' },
    { value: ReportingPeriod.Q3, description: 'End of Q3' },
    { value: ReportingPeriod.Q4, description: 'End of Q4' }
  ]

  progressStatus: Option<ProgressStatus>[] = [
    { value: ProgressStatus.Delayed, description: 'Delayed' },
    { value: ProgressStatus.OnTrack, description: 'On track' },
    { value: ProgressStatus.Deffered, description: 'Deffered' },
    { value: ProgressStatus.Cancelled, description: 'Cancelled' }
  ]

  constructor(
    @Inject(MAT_DIALOG_DATA) public data: any,
    public dialog: MatDialog,
    private readonly dialogRef: MatDialogRef<PerformanceUpdateModalWindowComponent>,
    private readonly projectService: ProjectService,
    private readonly snackbarService: MatSnackBar
  ) {
    this.bindAmountValidation();
  }

  private matchTotalValidator(): ValidatorFn {
    return (control: AbstractControl): ValidationErrors | null => {

      const revisedForecastCtrl = control.get('revisedForecast');
      const currentForecastCtrl = control.get('currentForecast');

      if (revisedForecastCtrl?.value) {
        const entered = Number(revisedForecastCtrl.value);
        return entered === this.totalAmount ? null : { totalMismatch: true };
      } else if (currentForecastCtrl?.value) {
        const entered = Number(currentForecastCtrl.value);
        return entered === this.totalAmount ? null : { totalMismatch: true };
      }
      
      console.warn('currentForecast and revisedForecastCtrl control not found');
      return null;
    };
  }

  private calculateTotalAmount() {
    
    this.totalAmount =
        (Number(this.highRiskControl.value) || 0) +
        (Number(this.mediumRiskControl.value) || 0) +
        (Number(this.lowRiskControl.value) || 0) +
        (Number(this.completeControl.value) || 0);
        this.form.updateValueAndValidity({ onlySelf: true });
  }

  private bindAmountValidation() {
    this.highRiskControl.valueChanges.subscribe(value => {
      if (value > 0) {
        this.highRiskDescriptionControl.enable();
      } else {
        this.highRiskDescriptionControl.reset();
        this.highRiskDescriptionControl.disable();
      }
      this.calculateTotalAmount();
    });

    this.mediumRiskControl.valueChanges.subscribe(value => {
      if (value > 0) {
        this.mediumRiskDescriptionControl.enable();
      } else {
        this.mediumRiskDescriptionControl.reset();
        this.mediumRiskDescriptionControl.disable();
      }
      this.calculateTotalAmount();
    });

    this.lowRiskControl.valueChanges.subscribe(value => {
      if (value > 0) {
        this.lowRiskDescriptionControl.enable();
      } else {
        this.lowRiskDescriptionControl.reset();
        this.lowRiskDescriptionControl.disable();
      }
      this.calculateTotalAmount();
    });

    this.completeControl.valueChanges.subscribe(value => {
      if (value > 0) {
        this.completeDescriptionControl.enable();
      } else {
        this.completeDescriptionControl.reset();
        this.completeDescriptionControl.disable();
      }
      this.calculateTotalAmount();
    });

  }

  get reportingPeriodControl(): FormControl {
    return this.form.get('reportingPeriod') as FormControl;
  }

  get progressStatusControl(): FormControl {
    return this.form.get('progressStatus') as FormControl;
  }

  get generalUpdatesControl(): FormControl {
    return this.form.get('generalUpdates') as FormControl;
  }

  get revisedForecastControl(): FormControl {
    return this.form.get('revisedForecast') as FormControl;
  }

  get forecastRationaleControl(): FormControl {
    return this.form.get('forecastRationale') as FormControl;
  }

  get highRiskControl(): FormControl {
    return this.form.get('highRisk') as FormControl;
  }

  get highRiskDescriptionControl(): FormControl {
    return this.form.get('highRiskDescription') as FormControl;
  }

  get mediumRiskControl(): FormControl {
    return this.form.get('mediumRisk') as FormControl;
  }

  get mediumRiskDescriptionControl(): FormControl {
    return this.form.get('mediumRiskDescription') as FormControl;
  }

  get lowRiskControl(): FormControl {
    return this.form.get('lowRisk') as FormControl;
  }

  get lowRiskDescriptionControl(): FormControl {
    return this.form.get('lowRiskDescription') as FormControl;
  }

  get completeControl(): FormControl {
    return this.form.get('complete') as FormControl;
  }

  get completeDescriptionControl(): FormControl {
    return this.form.get('completeDescription') as FormControl;
  }

  // get totalControl(): FormControl {
  //   return this.form.get('total') as FormControl;
  // }

  onCancel(): void {
    if (this.form.dirty) {
      const dialogRef = this.dialog.open(ConfirmationDialogComponent, {
        data: {
          indicator: 'confirm-unsave',
          title: ModalTitles.CONFIRM_CANCEL_TITLE,
          message: ModalMessages.CONFIRM_CANCEL_MESSAGE
        },
        width: '600px',
      });

      dialogRef.afterClosed().subscribe((result: boolean) => {
        if (result) {
          this.dialogRef.close();
        }
      });
    } else {
      this.dialogRef.close();
    }
  }

  onSave(): void {


    const newUpdate: PerformanceUpdate = {
      submittedTimestamp: '',
      reportingPeriod: this.reportingPeriodControl.value,
      progressStatusCode: this.progressStatusControl.value,
      forecastStatus: ForecastStatus.NotSetUP,
      updateGeneralStatus: UpdateGeneralStatus.NotSetUP,

      generalUpdateComment: this.generalUpdatesControl.value,
      submittedBy: "",

      forecastAmount: this.revisedForecastControl.value,
      forecastAdjustmentAmount: 0,
      forecastAdjustmentRationale: this.forecastRationaleControl.value,

      budgetHighRiskAmount: this.highRiskControl.value,
      budgetHighRiskRationale: this.highRiskDescriptionControl.value,
      budgetMediumRiskAmount: this.mediumRiskControl.value,
      budgetMediumRiskRationale: this.mediumRiskDescriptionControl.value,
      budgetLowRiskAmount: this.lowRiskControl.value,
      budgetLowRiskRationale: this.lowRiskDescriptionControl.value,
      budgetCompletedAmount: this.completeControl.value,
      budgetCompletedDescription: this.completeDescriptionControl.value,

      totalAmount: 0
    }

    this.projectService.savePerformanceUpdates(this.data.projectGuid, this.data.fiscalGuid, newUpdate).subscribe(
      {
        next: (data) => {
          // this.updates = data;
          console.info('Performence Saved');
          this.dialogRef.close();
        },
        error: (error) => {
          console.error('Error saving performance updates:', error);

          this.snackbarService.open(
            'Failed to save performance updates. Please try again later.',
            'OK',
            { duration: 5000, panelClass: 'snackbar-error' }
          );
        }
      }
    );
  }
}
