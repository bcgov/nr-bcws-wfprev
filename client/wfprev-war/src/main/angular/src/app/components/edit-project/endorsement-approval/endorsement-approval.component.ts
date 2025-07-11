import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, OnChanges, Output, SimpleChanges } from '@angular/core';
import { ReactiveFormsModule, FormControl, FormGroup, FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { ProjectFiscal } from 'src/app/components/models';
import { CheckboxComponent } from 'src/app/components/shared/checkbox/checkbox.component';
import { DatePickerComponent } from 'src/app/components/shared/date-picker/date-picker.component';
import { DetailsContainerComponent } from 'src/app/components/shared/details-container/details-container.component';
import { ReadOnlyFieldComponent } from 'src/app/components/shared/read-only-field/read-only-field.component';
import { TextareaComponent } from 'src/app/components/shared/textarea/textarea.component';

@Component({
  selector: 'wfprev-endorsement-approval',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    FormsModule,
    MatCheckboxModule,
    MatFormFieldModule,
    MatInputModule,
    MatDatepickerModule,
    MatButtonModule,
    DetailsContainerComponent,
    CheckboxComponent,
    DatePickerComponent,
    ReadOnlyFieldComponent,
    TextareaComponent
  ],
  templateUrl: './endorsement-approval.component.html',
  styleUrl: './endorsement-approval.component.scss'
})
export class EndorsementApprovalComponent implements OnChanges {

  @Input() fiscal!: ProjectFiscal;
  @Input() currentUser!: string;
  @Output() saveEndorsement = new EventEmitter<ProjectFiscal>(); 

  endorsementApprovalForm = new FormGroup({
    endorseFiscalYear: new FormControl<boolean | null>(false),
    endorsementDate: new FormControl<Date | null>(null),
    endorsementComment: new FormControl<string | null>(''),
    approvalDate: new FormControl<Date | null>(null),
    approvalComment: new FormControl<string | null>(''),
  });

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['fiscal'] && changes['fiscal'].currentValue) {
      const fiscal = changes['fiscal'].currentValue as ProjectFiscal;

      this.endorsementApprovalForm.patchValue({
        endorseFiscalYear: !!fiscal.isApprovedInd,
        endorsementDate: fiscal.endorsementTimestamp
          ? new Date(fiscal.endorsementTimestamp)
          : null,
        endorsementComment: fiscal.endorsementComment ?? '',
        approvalDate: fiscal.approvedTimestamp
          ? new Date(fiscal.approvedTimestamp)
          : null,
        approvalComment: fiscal.businessAreaComment ?? ''
      });

      this.endorsementApprovalForm.markAsPristine();
    }
  }


  onSave() {
    const formValue = this.endorsementApprovalForm.value;

    const updatedFiscal: ProjectFiscal = {
      ...this.fiscal,

      // Update endorsement
      isApprovedInd: !!formValue.endorseFiscalYear,
      endorsementComment: formValue.endorsementComment ?? undefined,
      endorsementTimestamp: formValue.endorsementDate
        ? new Date(formValue.endorsementDate).toISOString()
        : undefined,
      endorserName: formValue.endorsementDate ? this.currentUser : undefined,

      // Update approval
      businessAreaComment: formValue.approvalComment ?? undefined,
      approvedTimestamp: formValue.approvalDate
        ? new Date(formValue.approvalDate).toISOString()
        : undefined,
      approverName: formValue.approvalDate ? this.currentUser : undefined,
    };

    this.saveEndorsement.emit(updatedFiscal);

    this.endorsementApprovalForm.markAsPristine();
  }


  onCancel() {
    this.ngOnChanges({
      fiscal: {
        currentValue: this.fiscal,
        previousValue: null,
        firstChange: false,
        isFirstChange: () => false,
      }
    });
  }


  disableForm() {
    this.endorsementApprovalForm.disable();
  }

  get endorsementDateControl(): FormControl {
    return this.endorsementApprovalForm.get('endorsementDate') as FormControl;
  }

  get endorsementCommentControl(): FormControl {
    return this.endorsementApprovalForm.get('endorsementComment') as FormControl;
  }

  get approvalDateControl(): FormControl {
    return this.endorsementApprovalForm.get('approvalDate') as FormControl;
  }
  
  get approvalCommentControl(): FormControl {
    return this.endorsementApprovalForm.get('approvalComment') as FormControl;
  }
}
