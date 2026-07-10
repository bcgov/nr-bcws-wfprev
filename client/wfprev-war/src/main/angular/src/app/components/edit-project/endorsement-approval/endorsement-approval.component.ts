
import { Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges } from '@angular/core';
import { FormControl, FormGroup, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatDialog } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatTooltipModule } from '@angular/material/tooltip';
import { firstValueFrom } from 'rxjs';
import { ProjectFiscal } from 'src/app/components/models';
import { DetailsContainerComponent } from 'src/app/components/shared/details-container/details-container.component';
import { TimestampComponent } from 'src/app/components/shared/timestamp/timestamp.component';
import { capitalizeFirstLetter } from 'src/app/utils';
import { EndorsementCode, FiscalStatuses } from 'src/app/utils/constants';
import { getUtcIsoTimestamp } from 'src/app/utils/tools';
import { ConfirmationDialogComponent } from '../../confirmation-dialog/confirmation-dialog.component';
import { EndorsementApprovalSectionComponent } from './endorsement-approval-section/endorsement-approval-section.component';

@Component({
    selector: 'wfprev-endorsement-approval',
    standalone: true,
    imports: [
    ReactiveFormsModule,
    FormsModule,
    MatCheckboxModule,
    MatFormFieldModule,
    MatInputModule,
    MatDatepickerModule,
    MatButtonModule,
    DetailsContainerComponent,
    TimestampComponent,
    MatTooltipModule,
    EndorsementApprovalSectionComponent
],
    templateUrl: './endorsement-approval.component.html',
    styleUrl: './endorsement-approval.component.scss'
})
export class EndorsementApprovalComponent implements OnChanges, OnInit {

  @Input() fiscal!: ProjectFiscal;
  @Input() currentUser!: string;
  @Input() currentIdir!: string;
  @Input() isSaving = false;
  @Input() isReadonly = false;
  @Output() saveEndorsement = new EventEmitter<ProjectFiscal>();

  readonly draftTooltip = 'Submit your Draft Fiscal Activity using the Actions button to enable Endorsements and Approvals.';

  endorsementApprovalForm = new FormGroup({
    endorseFiscalActivity: new FormControl<boolean | null>(false),
    endorsementDate: new FormControl<Date | null>(null),
    endorsementComment: new FormControl<string | null>(''),
    approveFiscalActivity: new FormControl<boolean | null>(false),
    approvalDate: new FormControl<Date | null>(null),
    approvalComment: new FormControl<string | null>(''),
    bcwsHQApproveFiscalActivity: new FormControl<boolean | null>(false),
    bcwsHQApprovalDate: new FormControl<Date | null>(null),
    bcwsHQApprovalComment: new FormControl<string | null>(''),
  });

  constructor(private readonly dialog: MatDialog) { }

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

    this.endorsementApprovalForm.get('bcwsHQApproveFiscalActivity')?.valueChanges.subscribe((checked) => {
      const isChecked = !!checked;

      if (isChecked) {
        this.bcwsHQApprovalDateControl.setValue(new Date());
      }

      this.toggleControl(this.bcwsHQApprovalDateControl, isChecked);
      this.toggleControl(this.bcwsHQApprovalCommentControl, isChecked);
    });
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['fiscal']?.currentValue) {
      const fiscal = changes['fiscal'].currentValue as ProjectFiscal;
      const endorseChecked = !!fiscal.endorserName;
      const approveChecked = !!fiscal.isApprovedInd;
      const bcwsHQApproveChecked = !!fiscal.isBcwsHQApprovedInd;

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
        approvalComment: fiscal.businessAreaComment ?? '',
        bcwsHQApproveFiscalActivity: bcwsHQApproveChecked,
        bcwsHQApprovalDate: bcwsHQApproveChecked && fiscal.bcwsHQApprovedTimestamp
          ? new Date(fiscal.bcwsHQApprovedTimestamp)
          : null,
        bcwsHQApprovalComment: fiscal.bcwsHQApprovedComment ?? ''
      });

      if (this.isReadonly || this.isCardDisabled) {
        this.endorsementApprovalForm.disable({ emitEvent: false });
        return;
      } else {
        this.endorsementApprovalForm.enable({ emitEvent: false });
      }
      this.toggleControl(this.endorsementDateControl, endorseChecked);
      this.toggleControl(this.endorsementCommentControl, endorseChecked);
      this.toggleControl(this.approvalDateControl, approveChecked);
      this.toggleControl(this.approvalCommentControl, approveChecked);
      this.toggleControl(this.bcwsHQApprovalDateControl, bcwsHQApproveChecked);
      this.toggleControl(this.bcwsHQApprovalCommentControl, bcwsHQApproveChecked);

      this.endorsementApprovalForm.markAsPristine();
    }
    if (changes['isSaving'] && changes['isSaving'].currentValue === false) {
      this.isSaving = false;
    }
  }

  get effectiveEndorserName(): string {
    // use the endorserName if set
    if (this.fiscal?.endorserName) {
      return this.fiscal.endorserName;
    }
    // dirty = user actually clicked the checkbox (not just loaded from the API)
    // We need both, otherwise a fiscal that loads pre-checked (no endorser name saved) would wrongly show currentUser
    const control = this.endorsementApprovalForm.get('endorseFiscalActivity');
    return control?.value && control?.dirty ? this.currentUser : '';
  }

  get effectiveApproverName(): string {
    // use the approverName if set
    if (this.fiscal?.approverName) {
      return this.fiscal.approverName;
    }
    // dirty = user actually clicked the checkbox (not just loaded from the API)
    // We need both, otherwise a fiscal that loads pre-checked (no approver name saved) would wrongly show currentUser
    const control = this.endorsementApprovalForm.get('approveFiscalActivity');
    return control?.value && control?.dirty ? this.currentUser : '';
  }

  get effectiveBcwsHQApproverName(): string {
    // use the approverName if set
    if (this.fiscal?.bcwsHQApproverName) {
      return this.fiscal.bcwsHQApproverName;
    }
    // dirty = user actually clicked the checkbox (not just loaded from the API)
    // We need both, otherwise a fiscal that loads pre-checked (no approver name saved) would wrongly show currentUser
    const control = this.endorsementApprovalForm.get('bcwsHQApproveFiscalActivity');
    return control?.value && control?.dirty ? this.currentUser : '';
  }

  async onSave() {
    if (this.isSaving) return;
    const formValue = this.endorsementApprovalForm.value;

    const endorsementRemoved = !formValue.endorseFiscalActivity;
    const approvalRemoved = !formValue.approveFiscalActivity;
    const bcwsHQAprovalRemoved = !formValue.bcwsHQApproveFiscalActivity;

    const currentStatusCode = this.fiscal?.planFiscalStatusCode?.planFiscalStatusCode;
    const resetToProposedEndorsementRemoved = endorsementRemoved && currentStatusCode !== FiscalStatuses.DRAFT && currentStatusCode !== FiscalStatuses.PROPOSED;
    const resetToProposedApprovalRemoved = approvalRemoved && currentStatusCode !== FiscalStatuses.DRAFT && currentStatusCode !== FiscalStatuses.PROPOSED;
    const resetToProposedBcwsHQApprovalRemoved = bcwsHQAprovalRemoved && currentStatusCode !== FiscalStatuses.DRAFT && currentStatusCode !== FiscalStatuses.PROPOSED;

    if (resetToProposedEndorsementRemoved || resetToProposedApprovalRemoved || resetToProposedBcwsHQApprovalRemoved) {
      const confirmed = await this.confirmStatusChange(currentStatusCode ?? '', 'Proposed');
      if (!confirmed) return; // user cancelled
    } else if (!endorsementRemoved && !approvalRemoved && !bcwsHQAprovalRemoved) {
      const confirmed = await this.confirmStatusChange(currentStatusCode ?? '', 'Prepared');
      if (!confirmed) return; // user cancelled
    }

    const currentUtc = getUtcIsoTimestamp();
    const updatedFiscal: ProjectFiscal = {
      ...this.fiscal,

      // Endorsement logic
      endorserName: formValue.endorseFiscalActivity
        ? (this.fiscal.endorserName ?? this.currentUser)
        : undefined,
      endorsementTimestamp: formValue.endorseFiscalActivity && formValue.endorsementDate
        ? formValue.endorsementDate.toISOString()
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
      
      // BCWS HQ Approval logic
      isBcwsHQApprovedInd: !!formValue.bcwsHQApproveFiscalActivity,
      bcwsHQApprovedTimestamp: formValue.bcwsHQApproveFiscalActivity && formValue.bcwsHQApprovalDate
        ? new Date(formValue.bcwsHQApprovalDate).toISOString()
        : undefined,
      bcwsHQApproverName: formValue.bcwsHQApproveFiscalActivity ? this.currentUser : undefined,
      bcwsHQApprovedComment: formValue.bcwsHQApprovalComment ?? undefined,

      planFiscalStatusCode: this.fiscal.planFiscalStatusCode,
      endorseApprUpdateUserid: this.currentIdir,
      endorseApprUpdatedTimestamp: currentUtc,
    };

    // Status logic: (return to PROPOSED if removed and not DRAFT/PROPOSED)
    // if reverting to proposed, clear out endorsement or approval fields depending on which has been unselected
    if (resetToProposedEndorsementRemoved) {
      updatedFiscal.planFiscalStatusCode = { planFiscalStatusCode: FiscalStatuses.PROPOSED };
      updatedFiscal.endorserName = undefined;
      updatedFiscal.endorsementTimestamp = undefined;
      updatedFiscal.endorsementCode = { endorsementCode: EndorsementCode.NOT_ENDORS };
      updatedFiscal.endorsementComment = undefined;
      updatedFiscal.endorsementEvalTimestamp = undefined;
      updatedFiscal.endorserUserGuid = undefined;
      updatedFiscal.endorserUserUserid = undefined;
      updatedFiscal.endorseApprUpdateUserid = undefined;
      updatedFiscal.endorseApprUpdatedTimestamp = undefined;
    } 
    
    if (resetToProposedApprovalRemoved) {
      updatedFiscal.planFiscalStatusCode = { planFiscalStatusCode: FiscalStatuses.PROPOSED };
      updatedFiscal.isApprovedInd = false;
      updatedFiscal.approvedTimestamp = undefined;
      updatedFiscal.approverName = undefined;
      updatedFiscal.approverUserGuid = undefined;
      updatedFiscal.approverUserUserid = undefined;
      updatedFiscal.businessAreaComment = undefined;
    }

    if (resetToProposedBcwsHQApprovalRemoved) {
      updatedFiscal.planFiscalStatusCode = { planFiscalStatusCode: FiscalStatuses.PROPOSED };
      updatedFiscal.isBcwsHQApprovedInd = false;
      updatedFiscal.bcwsHQApprovedTimestamp = undefined;
      updatedFiscal.bcwsHQApproverName = undefined;
      updatedFiscal.bcwsHQApproverUserGuid = undefined;
      updatedFiscal.bcwsHQApproverUserUserid = undefined;
      updatedFiscal.bcwsHQApprovedComment = undefined;
    }

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

   get bcwsHQApprovalDateControl(): FormControl {
    return this.endorsementApprovalForm.get('bcwsHQApprovalDate') as FormControl;
  }

  get bcwsHQApprovalCommentControl(): FormControl {
    return this.endorsementApprovalForm.get('bcwsHQApprovalComment') as FormControl;
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

    if (shouldEnable && !this.isReadonly) {
      control.enable();
    } else {
      control.setValue(null);
      control.disable();
    }
  }

  async confirmStatusChange(currentStatusCode: string, newStatusCode: string): Promise<boolean> {
    if (!currentStatusCode || !newStatusCode) return false;
    
    // Parse status to plain English if it is IN_PROG
    // The remaining status do not require such parsing
    const currentStatus = currentStatusCode === 'IN_PROG' ? "In Progress" : capitalizeFirstLetter(currentStatusCode);
    const message = `You are about to change the status of this Fiscal Activity from ${currentStatus} to ${newStatusCode}. Do you wish to continue?`

    const dialogRef = this.dialog.open(ConfirmationDialogComponent, {
      data: {
        indicator: 'confirm-fiscal-status-update',
        title: `Confirm Change to ${newStatusCode}`,
        message: message
      },
      width: '600px',
    });
    
    const result = await firstValueFrom(dialogRef.afterClosed());
    return !!result;
  }

}
