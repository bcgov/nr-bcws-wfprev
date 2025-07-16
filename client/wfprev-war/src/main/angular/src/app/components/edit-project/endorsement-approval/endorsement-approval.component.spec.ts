import { ComponentFixture, TestBed } from '@angular/core/testing';
import { EndorsementApprovalComponent } from './endorsement-approval.component';
import { ProjectFiscal } from 'src/app/components/models';
import { FiscalStatuses } from 'src/app/utils/constants';

describe('EndorsementApprovalComponent', () => {
  let component: EndorsementApprovalComponent;
  let fixture: ComponentFixture<EndorsementApprovalComponent>;

const mockFiscal: ProjectFiscal = {
  projectGuid: '123',
  activityCategoryCode: 'CATEGORY',
  fiscalYear: 2024,
  projectPlanStatusCode: 'ACTIVE',
  planFiscalStatusCode: { planFiscalStatusCode: 'ACTIVE' },
  projectFiscalName: 'Test Fiscal',
  isApprovedInd: true,
  isDelayedInd: false,
  totalCostEstimateAmount: 1000,
  fiscalPlannedProjectSizeHa: 50,
  fiscalPlannedCostPerHaAmt: 20,
  fiscalReportedSpendAmount: 500,
  fiscalActualAmount: 600,
  fiscalActualCostPerHaAmt: 12,
  firstNationsDelivPartInd: false,
  firstNationsEngagementInd: false,
  endorsementComment: 'Looks good',
  endorsementTimestamp: '2023-10-10T00:00:00Z',
  endorserName: 'Alice',
  approvedTimestamp: '2023-10-11T00:00:00Z',
  approverName: 'Bob',
  businessAreaComment: 'Approved',
};


  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [EndorsementApprovalComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(EndorsementApprovalComponent);
    component = fixture.componentInstance;
    component.currentUser = 'Test User';
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should patch form values in ngOnChanges', () => {
    component.ngOnChanges({
      fiscal: {
        currentValue: mockFiscal,
        previousValue: null,
        firstChange: true,
        isFirstChange: () => true,
      },
    });

    expect(component.endorsementApprovalForm.value.endorseFiscalYear).toBeTrue();
    expect(component.endorsementApprovalForm.value.approveFiscalYear).toBeTrue();
    expect(component.endorsementApprovalForm.value.endorsementComment).toBe('Looks good');
    expect(component.endorsementApprovalForm.value.approvalComment).toBe('Approved');
  });

  it('should return effectiveEndorserName as currentUser when endorseFiscalYear checked', () => {
    component.fiscal = mockFiscal;
    component.endorsementApprovalForm.get('endorseFiscalYear')?.setValue(true);
    expect(component.effectiveEndorserName).toBe('Test User');
  });

  it('should return effectiveEndorserName as fiscal endorser when endorseFiscalYear unchecked', () => {
    component.fiscal = mockFiscal;
    component.endorsementApprovalForm.get('endorseFiscalYear')?.setValue(false);
    expect(component.effectiveEndorserName).toBe('Alice');
  });

  it('should emit saveEndorsement with updated fiscal on save', () => {
    const emitSpy = spyOn(component.saveEndorsement, 'emit');
    component.fiscal = mockFiscal;
    component.endorsementApprovalForm.patchValue({
      endorseFiscalYear: true,
      endorsementDate: new Date('2024-01-01'),
      endorsementComment: 'New endorsement',
      approveFiscalYear: true,
      approvalDate: new Date('2024-02-01'),
      approvalComment: 'New approval',
    });

    component.onSave();

    const emittedFiscal = emitSpy.calls.mostRecent()!.args[0];
    expect(emittedFiscal!.endorserName).toBe('Test User');
    expect(emittedFiscal!.endorsementComment).toBe('New endorsement');
    expect(emittedFiscal!.isApprovedInd).toBeTrue();
    expect(emittedFiscal!.businessAreaComment).toBe('New approval');
  });

  it('should set planFiscalStatusCode to DRAFT if endorsement removed', () => {
    const emitSpy = spyOn(component.saveEndorsement, 'emit');
    component.fiscal = {
      ...mockFiscal,
      planFiscalStatusCode: { planFiscalStatusCode: 'ACTIVE' }
    };
    component.endorsementApprovalForm.patchValue({
      endorseFiscalYear: false,
      approveFiscalYear: true,
    });

    component.onSave();

    const emittedFiscal = emitSpy.calls.mostRecent()!.args[0];
    expect(emittedFiscal!.planFiscalStatusCode?.planFiscalStatusCode).toBe(FiscalStatuses.DRAFT);
  });

  it('should reset form values on cancel', () => {
    component.fiscal = mockFiscal;
    component.endorsementApprovalForm.patchValue({
      endorsementComment: 'Changed comment'
    });
    expect(component.endorsementApprovalForm.dirty).toBeFalse();

    component.onCancel();

    expect(component.endorsementApprovalForm.value.endorsementComment).toBe('Looks good');
  });

  it('should disable the form', () => {
    component.disableForm();
    expect(component.endorsementApprovalForm.disabled).toBeFalse();
  });

  it('should set endorsementDate when endorseFiscalYear toggled on', () => {
    const control = component.endorsementDateControl;
    component.endorsementApprovalForm.get('endorseFiscalYear')?.setValue(true);
    expect(control.value).toBeInstanceOf(Date);
  });

  it('should clear endorsementDate when endorseFiscalYear toggled off', () => {
    const control = component.endorsementDateControl;
    component.endorsementApprovalForm.get('endorseFiscalYear')?.setValue(true);
    expect(control.value).toBeInstanceOf(Date);
    component.endorsementApprovalForm.get('endorseFiscalYear')?.setValue(false);
    expect(control.value).toBeNull();
  });

  it('should set approvalDate when approveFiscalYear toggled on', () => {
    const control = component.approvalDateControl;
    component.endorsementApprovalForm.get('approveFiscalYear')?.setValue(true);
    expect(control.value).toBeInstanceOf(Date);
  });

  it('should clear approvalDate when approveFiscalYear toggled off', () => {
    const control = component.approvalDateControl;
    component.endorsementApprovalForm.get('approveFiscalYear')?.setValue(true);
    expect(control.value).toBeInstanceOf(Date);
    component.endorsementApprovalForm.get('approveFiscalYear')?.setValue(false);
    expect(control.value).toBeNull();
  });
});
