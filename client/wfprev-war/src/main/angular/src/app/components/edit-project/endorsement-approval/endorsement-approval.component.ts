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
import { EndorsementCode, FiscalStatuses } from 'src/app/utils/constants';

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
    approveFiscalYear: new FormControl<boolean | null>(false),
    approvalDate: new FormControl<Date | null>(null),
    approvalComment: new FormControl<string | null>(''),
  });

  ngOnInit(): void {
    this.endorsementApprovalForm.get('endorseFiscalYear')?.valueChanges.subscribe((checked) => {
      if (checked) {
        this.endorsementDateControl.setValue(new Date());
      } else {
        this.endorsementDateControl.setValue(null);
      }
    });

    this.endorsementApprovalForm.get('approveFiscalYear')?.valueChanges.subscribe((checked) => {
      if (checked) {
        this.approvalDateControl.setValue(new Date());
      } else {
        this.approvalDateControl.setValue(null);
      }
    });
  }


  ngOnChanges(changes: SimpleChanges): void {
    if (changes['fiscal']?.currentValue) {
      const fiscal = changes['fiscal'].currentValue as ProjectFiscal;
      const endorseChecked = !!fiscal.endorserName;
      const approveChecked = !!fiscal.isApprovedInd;

      this.endorsementApprovalForm.patchValue({
        endorseFiscalYear: endorseChecked,
        endorsementDate: endorseChecked && fiscal.endorsementTimestamp
          ? new Date(fiscal.endorsementTimestamp)
          : null,
        endorsementComment: fiscal.endorsementComment ?? '',
        approveFiscalYear: approveChecked,
        approvalDate: approveChecked && fiscal.approvedTimestamp
          ? new Date(fiscal.approvedTimestamp)
          : null,
        approvalComment: fiscal.businessAreaComment ?? ''
      });

      this.endorsementApprovalForm.markAsPristine();
    }
  }



  get effectiveEndorserName(): string {
    const checked = this.endorsementApprovalForm.get('endorseFiscalYear')?.value;
    return checked ? this.currentUser : (this.fiscal?.endorserName || '');
  }


  onSave() {
    const formValue = this.endorsementApprovalForm.value;

    const endorsementRemoved = !formValue.endorseFiscalYear;
    const approvalRemoved = !formValue.approveFiscalYear;
    const currentStatusCode = this.fiscal?.planFiscalStatusCode?.planFiscalStatusCode;
    const shouldResetToPrepared =
      (endorsementRemoved || approvalRemoved) &&
      currentStatusCode !== FiscalStatuses.DRAFT &&
      currentStatusCode !== FiscalStatuses.PROPOSED;

    const updatedFiscal: ProjectFiscal = {
      ...this.fiscal,

      // Endorsement logic
      endorserName: formValue.endorseFiscalYear ? this.currentUser : undefined,
      endorsementTimestamp: formValue.endorseFiscalYear && formValue.endorsementDate
        ? new Date(formValue.endorsementDate).toISOString()
        : undefined,
      endorsementCode: formValue.endorseFiscalYear
        ? { endorsementCode: EndorsementCode.ENDORSED }
        : undefined,
      endorsementComment: formValue.endorsementComment ?? undefined,

      // Approval logic
      isApprovedInd: !!formValue.approveFiscalYear,
      approvedTimestamp: formValue.approveFiscalYear && formValue.approvalDate
        ? new Date(formValue.approvalDate).toISOString()
        : undefined,
      approverName: formValue.approveFiscalYear ? this.currentUser : undefined,
      businessAreaComment: formValue.approvalComment ?? undefined,

       // Status logic: (return to DRAFT if removed and not DRAFT/PROPOSED)
      planFiscalStatusCode: shouldResetToPrepared
        ? { planFiscalStatusCode: FiscalStatuses.DRAFT }
        : this.fiscal.planFiscalStatusCode,
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
