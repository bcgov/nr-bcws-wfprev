import { Component, Inject } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogActions, MatDialogClose, MatDialogContent } from '@angular/material/dialog';
import { MatAccordion, MatExpansionPanel } from "@angular/material/expansion";
import { MatTab, MatTabGroup } from "@angular/material/tabs";
import { Messages } from 'src/app/utils/constants';
import { ProgressStatus, ReportingPeriod, SelectionOption } from '../models';
import { InputFieldComponent } from "../shared/input-field/input-field.component";
import { SelectFieldComponent } from '../shared/select-field/select-field.component';
import { TextareaComponent } from "../shared/textarea/textarea.component";
import { ProjectService } from 'src/app/services/project-services';

@Component({
  selector: 'app-performance-update-modal-window',
  standalone: true,
  imports: [SelectFieldComponent, MatTabGroup, MatTab, MatAccordion, MatExpansionPanel, MatDialogContent, MatDialogActions, MatDialogClose, TextareaComponent, InputFieldComponent],
  templateUrl: './performance-update-modal-window.component.html',
  styleUrl: './performance-update-modal-window.component.scss'
})
export class PerformanceUpdateModalWindowComponent {

  messages = Messages;

  form = new FormGroup({
    reportingPeriod: new FormControl(ReportingPeriod.Q1),
    progressStatus: new FormControl(ProgressStatus.Delayed),
    generalUpdates: new FormControl(""),
    revisedForecast: new FormControl<number | null>(null, {
      validators: [
        Validators.required,
        Validators.min(0),
        Validators.max(9999)
      ]
    }),
    forecastRationale: new FormControl(""),
    highRisk: new FormControl<number | null>(null, {
      validators: [
        Validators.required,
        Validators.min(0),
        Validators.max(9999)
      ]
    }),
    highRiskDescription: new FormControl(""),

    mediumRisk: new FormControl<number | null>(null, {
      validators: [
        Validators.required,
        Validators.min(0),
        Validators.max(9999)
      ]
    }),
    mediumRiskDescription: new FormControl(""),

    lowRisk: new FormControl<number | null>(null, {
      validators: [
        Validators.required,
        Validators.min(0),
        Validators.max(9999)
      ]
    }),
    lowRiskDescription: new FormControl(""),
    
    complete: new FormControl<number | null>(null, {
      validators: [
        Validators.required,
        Validators.min(0),
        Validators.max(9999)
      ]
    }),
    completeDescription: new FormControl(""),

    total: new FormControl("")
  });

  reportingPeriod: SelectionOption<ReportingPeriod>[] = [
    { value: ReportingPeriod.Q1, description: 'End of Q1' },
    { value: ReportingPeriod.Q2, description: 'End of Q2' },
    { value: ReportingPeriod.Q3, description: 'End of Q3' },
    { value: ReportingPeriod.Q4, description: 'End of Q4' }
  ]

  progressStatus: SelectionOption<ProgressStatus>[] = [
    { value: ProgressStatus.Delayed, description: 'Delayed' },
    { value: ProgressStatus.OnTrack, description: 'On track' },
    { value: ProgressStatus.Deffered, description: 'Deffered' },
    { value: ProgressStatus.Cancelled, description: 'Cancelled' }
  ]

  constructor(
    @Inject(MAT_DIALOG_DATA) public data: any,
    private readonly projectService: ProjectService
  ) { }

  get reportingPeriodControl(): FormControl {
    return this.form.get('reportingPeriod') as FormControl;
  }

  get progressStatusControl(): FormControl {
    return this.form.get('progressStatus') as FormControl;
  }

  get generalUpdates(): FormControl {
    return this.form.get('generalUpdates') as FormControl;
  }

  get revisedForecast(): FormControl {
    return this.form.get('revisedForecast') as FormControl;
  }

  get forecastRationale(): FormControl {
    return this.form.get('forecastRationale') as FormControl;
  }

  get highRisk(): FormControl {
    return this.form.get('highRisk') as FormControl;
  }
  
  get highRiskDescription(): FormControl {
    return this.form.get('highRiskDescription') as FormControl;
  }

  get mediumRisk(): FormControl {
    return this.form.get('mediumRisk') as FormControl;
  }
  
  get mediumRiskDescription(): FormControl {
    return this.form.get('mediumRiskDescription') as FormControl;
  }
  
  get lowRisk(): FormControl {
    return this.form.get('lowRisk') as FormControl;
  }
  
  get lowRiskDescription(): FormControl {
    return this.form.get('lowRiskDescription') as FormControl;
  }get 
  
  complete(): FormControl {
    return this.form.get('complete') as FormControl;
  }
  
  get completeDescription(): FormControl {
    return this.form.get('completeDescription') as FormControl;
  }
  
  get total(): FormControl {
    return this.form.get('total') as FormControl;
  }

}
