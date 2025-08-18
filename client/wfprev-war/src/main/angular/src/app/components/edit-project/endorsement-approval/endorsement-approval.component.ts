import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, OnChanges, Output, SimpleChanges } from '@angular/core';
import { ReactiveFormsModule, FormControl, FormGroup, FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatTooltipModule } from '@angular/material/tooltip';
import { ProjectFiscal } from 'src/app/components/models';
import { CheckboxComponent } from 'src/app/components/shared/checkbox/checkbox.component';
import { DatePickerComponent } from 'src/app/components/shared/date-picker/date-picker.component';
import { DetailsContainerComponent } from 'src/app/components/shared/details-container/details-container.component';
import { ReadOnlyFieldComponent } from 'src/app/components/shared/read-only-field/read-only-field.component';
import { TextareaComponent } from 'src/app/components/shared/textarea/textarea.component';
import { TimestampComponent } from 'src/app/components/shared/timestamp/timestamp.component';
import { EndorsementCode, FiscalStatuses } from 'src/app/utils/constants';
import { getLocalIsoTimestamp } from 'src/app/utils/tools';

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
    TextareaComponent,
    TimestampComponent,
    MatTooltipModule,
  ],
  templateUrl: './endorsement-approval.component.html',
  styleUrl: './endorsement-approval.component.scss'
})
export class EndorsementApprovalComponent implements OnChanges {

  @Input() fiscal!: ProjectFiscal;
  @Input() currentUser!: string;
  @Input() currentIdir!: string;
  @Output() saveEndorsement = new EventEmitter<ProjectFiscal>();

  readonly draftTooltip = 'Submit your Draft Fiscal Activity using the Actions button to enable Endorsements and Approvals.';

  endorsementApprovalForm = new FormGroup({
    endorseFiscalActivity: new FormControl<boolean | null>(false),
    endorsementDate: new FormControl<Date | null>(null),
    endorsementComment: new FormControl<string | null>(''),
    approveFiscalActivity: new FormControl<boolean | null>(false),
    approvalDate: new FormControl<Date | null>(null),
    approvalComment: new FormControl<string | null>(''),
  });

  ngOnInit(): void {
    this.endorsementApprovalForm.get('endorseFiscalActivity')?.valueChanges.subscribe((checked) => {
      const isChecked = !!checked;

      if (isChecked) {
        this.endorsementDateControl.setValue(new Date());
      }

      this.toggleControl(this.endorsementDateControl, isChecked);
      this.toggleControl(this.endorsementCommentControl, isChecked);
    });

    this.endorsementApprovalForm.get('approveFiscalActivity')?.valueChanges.subscribe((checked) => {
      const isChecked = !!checked;

      if (isChecked) {
        this.approvalDateControl.setValue(new Date());
      }

      this.toggleControl(this.approvalDateControl, isChecked);
      this.toggleControl(this.approvalCommentControl, isChecked);
    });
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['fiscal']?.currentValue) {
      const fiscal = changes['fiscal'].currentValue as ProjectFiscal;
      const endorseChecked = !!fiscal.endorserName;
      const approveChecked = !!fiscal.isApprovedInd;

      this.endorsementApprovalForm.patchValue({
        endorseFiscalActivity: endorseChecked,
        endorsementDate: endorseChecked && fiscal.endorsementTimestamp
          ? new Date(fiscal.endorsementTimestamp)
          : null,
        endorsementComment: fiscal.endorsementComment ?? '',
        approveFiscalActivity: approveChecked,
        approvalDate: approveChecked && fiscal.approvedTimestamp
          ? new Date(fiscal.approvedTimestamp)
          : null,
        approvalComment: fiscal.businessAreaComment ?? ''
      });
      
      if (this.isCardDisabled) {
        this.endorsementApprovalForm.disable({ emitEvent: false });
        return;
      } else {
        this.endorsementApprovalForm.enable({ emitEvent: false });
      }
      this.toggleControl(this.endorsementDateControl, endorseChecked);
      this.toggleControl(this.endorsementCommentControl, endorseChecked);
      this.toggleControl(this.approvalDateControl, approveChecked);
      this.toggleControl(this.approvalCommentControl, approveChecked);

      this.endorsementApprovalForm.markAsPristine();
    }
  }

  get effectiveEndorserName(): string {
    const checked = this.endorsementApprovalForm.get('endorseFiscalActivity')?.value;
    return checked ? this.currentUser : '';
  }

  onSave() {
    const formValue = this.endorsementApprovalForm.value;

    const endorsementRemoved = !formValue.endorseFiscalActivity;
    const approvalRemoved = !formValue.approveFiscalActivity;
    const currentStatusCode = this.fiscal?.planFiscalStatusCode?.planFiscalStatusCode;
    const shouldResetToPrepared =
      (endorsementRemoved || approvalRemoved) &&
      currentStatusCode !== FiscalStatuses.DRAFT &&
      currentStatusCode !== FiscalStatuses.PROPOSED;

    const currentIso = getLocalIsoTimestamp();
    const updatedFiscal: ProjectFiscal = {
      ...this.fiscal,

      // Endorsement logic
      endorserName: formValue.endorseFiscalActivity ? this.currentUser : undefined,
      endorsementTimestamp: formValue.endorseFiscalActivity && formValue.endorsementDate
        ? getLocalIsoTimestamp(formValue.endorsementDate)
        : undefined,
      endorsementCode: formValue.endorseFiscalActivity
        ? { endorsementCode: EndorsementCode.ENDORSED }
        : undefined,
      endorsementComment: formValue.endorsementComment ?? undefined,

      // Approval logic
      isApprovedInd: !!formValue.approveFiscalActivity,
      approvedTimestamp: formValue.approveFiscalActivity && formValue.approvalDate
        ? new Date(formValue.approvalDate).toISOString()
        : undefined,
      approverName: formValue.approveFiscalActivity ? this.currentUser : undefined,
      businessAreaComment: formValue.approvalComment ?? undefined,

      // Status logic: (return to DRAFT if removed and not DRAFT/PROPOSED)
      planFiscalStatusCode: shouldResetToPrepared
        ? { planFiscalStatusCode: FiscalStatuses.DRAFT }
        : this.fiscal.planFiscalStatusCode,
      endorseApprUpdateUserid: this.currentIdir,
      endorseApprUpdatedTimestamp: currentIso,
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

  get statusCode(): string | undefined {
    return this.fiscal?.planFiscalStatusCode?.planFiscalStatusCode;
  }

  get isDraft(): boolean {
    return this.statusCode === FiscalStatuses.DRAFT;
  }

  get isCardDisabled(): boolean {
    // disabled in Draft, Complete, Cancelled
    return this.isDraft
      || this.statusCode === FiscalStatuses.COMPLETE
      || this.statusCode === FiscalStatuses.CANCELLED;
  }

  get showDraftTooltip(): boolean {
    // tooltip only in draft
    return this.isDraft;
  }
  toggleControl(control: FormControl | null, shouldEnable: boolean): void {
    if (!control) return;

    if (shouldEnable) {
      control.enable();
    } else {
      control.setValue(null);
      control.disable();
    }
  }

}
