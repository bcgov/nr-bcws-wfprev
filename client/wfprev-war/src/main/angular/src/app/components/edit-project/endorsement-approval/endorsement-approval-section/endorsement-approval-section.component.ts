import { Component, Input } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { CheckboxComponent } from 'src/app/components/shared/checkbox/checkbox.component';
import { DatePickerComponent } from 'src/app/components/shared/date-picker/date-picker.component';
import { ReadOnlyFieldComponent } from 'src/app/components/shared/read-only-field/read-only-field.component';
import { TextareaComponent } from 'src/app/components/shared/textarea/textarea.component';

export interface EndorsementApprovalSectionConfig {
  checkboxLabel: string;
  datepickerLabel: string;
  readOnlyLabel: string;
  readOnlyValue: string;
  textareaLabel: string;
  textareaPlaceholder: string;
}

@Component({
  selector: 'wfprev-endorsement-section',
  standalone: true,
  imports: [ReactiveFormsModule, CheckboxComponent, DatePickerComponent, ReadOnlyFieldComponent, TextareaComponent],
  templateUrl: './endorsement-approval-section.component.html',
  styleUrl: './endorsement-approval-section.component.scss', 
})
export class EndorsementApprovalSectionComponent {
  @Input() config!: EndorsementApprovalSectionConfig;
  @Input() form!: FormGroup;
  @Input() checkboxKey!: string;
  @Input() dateKey!: string;
  @Input() commentKey!: string;

  get checkboxControl(): FormControl {
    return this.form.get(this.checkboxKey) as FormControl;
  }
  get dateControl(): FormControl {
    return this.form.get(this.dateKey) as FormControl;
  }
  get commentControl(): FormControl {
    return this.form.get(this.commentKey) as FormControl;
  }
}