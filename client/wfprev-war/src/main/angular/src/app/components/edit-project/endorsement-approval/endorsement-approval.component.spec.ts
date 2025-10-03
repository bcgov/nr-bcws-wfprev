import { ComponentFixture, fakeAsync, TestBed, tick } from '@angular/core/testing';
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

  it('should set and enable endorsement controls when checkbox is checked', fakeAsync(() => {
    component.ngOnInit();
    fixture.detectChanges();
    tick();

    const form = component.endorsementApprovalForm;
    const endorseControl = form.get('endorseFiscalActivity');

    endorseControl?.setValue(true);
    tick();

    expect(component.endorsementDateControl.enabled).toBeTrue();
    expect(component.endorsementCommentControl.enabled).toBeTrue();
    expect(component.endorsementDateControl.value).toBeInstanceOf(Date);
  }));

  it('should patch and disable controls in ngOnChanges if no endorsement or approval', () => {
    const noEndorseOrApproval: ProjectFiscal = {
      ...mockFiscal,
      endorserName: undefined,
      endorsementTimestamp: undefined,
      endorsementComment: '',
      isApprovedInd: false,
      approvedTimestamp: undefined,
      approverName: undefined,
      businessAreaComment: '',
    };

    component.fiscal = noEndorseOrApproval;

    component.ngOnChanges({
      fiscal: {
        currentValue: noEndorseOrApproval,
        previousValue: null,
        firstChange: true,
        isFirstChange: () => true,
      }
    });

    expect(component.endorsementApprovalForm.value.endorseFiscalActivity).toBeFalse();
    expect(component.endorsementDateControl.enabled).toBeFalse();
    expect(component.endorsementCommentControl.enabled).toBeFalse();

    expect(component.endorsementApprovalForm.value.approveFiscalActivity).toBeFalse();
    expect(component.approvalDateControl.enabled).toBeFalse();
    expect(component.approvalCommentControl.enabled).toBeFalse();

    expect(component.endorsementApprovalForm.pristine).toBeTrue();
  });


  it('should return effectiveEndorserName as fiscal endorser when present', () => {
    component.fiscal = mockFiscal;
    component.endorsementApprovalForm.get('endorseFiscalActivity')?.setValue(true);
    expect(component.effectiveEndorserName).toBe('Alice');
  });

  it('should return currentUser when no fiscal endorser and endorseFiscalActivity checked', () => {
    const noEndorserFiscal: ProjectFiscal = { ...mockFiscal, endorserName: undefined };
    component.fiscal = noEndorserFiscal;
    component.endorsementApprovalForm.get('endorseFiscalActivity')?.setValue(true);
    expect(component.effectiveEndorserName).toBe('Test User');
  });

  it('should return empty string when no fiscal endorser and endorseFiscalActivity unchecked', () => {
    const noEndorserFiscal: ProjectFiscal = { ...mockFiscal, endorserName: undefined };
    component.fiscal = noEndorserFiscal;
    component.endorsementApprovalForm.get('endorseFiscalActivity')?.setValue(false);
    expect(component.effectiveEndorserName).toBe('');
  });
  
  it('should emit saveEndorsement with updated fiscal on save', () => {
    const emitSpy = spyOn(component.saveEndorsement, 'emit');
    component.fiscal = mockFiscal;
    component.endorsementApprovalForm.patchValue({
      endorseFiscalActivity: true,
      endorsementDate: new Date('2024-01-01'),
      endorsementComment: 'New endorsement',
      approveFiscalActivity: true,
      approvalDate: new Date('2024-02-01'),
      approvalComment: 'New approval',
    });

    component.onSave();

    const emittedFiscal = emitSpy.calls.mostRecent()!.args[0];
    expect(emittedFiscal!.endorserName).toBe('Alice');
    expect(emittedFiscal!.endorsementComment).toBe('New endorsement');
    expect(emittedFiscal!.isApprovedInd).toBeTrue();
    expect(emittedFiscal!.businessAreaComment).toBe('New approval');
  });

  it('should set endorserName to currentUser if none exists and endorsement is checked', () => {
    const emitSpy = spyOn(component.saveEndorsement, 'emit');
    component.fiscal = { ...mockFiscal, endorserName: undefined };
    component.endorsementApprovalForm.patchValue({
      endorseFiscalActivity: true,
      endorsementDate: new Date('2024-01-01'),
    });

    component.onSave();

    const emittedFiscal = emitSpy.calls.mostRecent()!.args[0];
    expect(emittedFiscal!.endorserName).toBe('Test User');
  });

  it('should set planFiscalStatusCode to DRAFT if endorsement removed', () => {
    const emitSpy = spyOn(component.saveEndorsement, 'emit');
    component.fiscal = {
      ...mockFiscal,
      planFiscalStatusCode: { planFiscalStatusCode: 'ACTIVE' }
    };
    component.endorsementApprovalForm.patchValue({
      endorseFiscalActivity: false,
      approveFiscalActivity: true,
    });

    component.onSave();

    const emittedFiscal = emitSpy.calls.mostRecent()!.args[0];
    expect(emittedFiscal!.planFiscalStatusCode?.planFiscalStatusCode).toBe(FiscalStatuses.DRAFT);
  });

  it('should disable the form', () => {
    component.disableForm();
    expect(component.endorsementApprovalForm.disabled).toBeTrue();
  });

  it('should set endorsementDate when endorseFiscalActivity toggled on', () => {
    const control = component.endorsementDateControl;
    component.endorsementApprovalForm.get('endorseFiscalActivity')?.setValue(true);
    expect(control.value).toBeInstanceOf(Date);
  });

  it('should clear endorsementDate when endorseFiscalActivity toggled off', () => {
    const control = component.endorsementDateControl;
    component.endorsementApprovalForm.get('endorseFiscalActivity')?.setValue(true);
    expect(control.value).toBeInstanceOf(Date);
    component.endorsementApprovalForm.get('endorseFiscalActivity')?.setValue(false);
    expect(control.value).toBeNull();
  });

  it('should set approvalDate when approveFiscalActivity toggled on', () => {
    const control = component.approvalDateControl;
    component.endorsementApprovalForm.get('approveFiscalActivity')?.setValue(true);
    expect(control.value).toBeInstanceOf(Date);
  });

  it('should clear approvalDate when approveFiscalActivity toggled off', () => {
    const control = component.approvalDateControl;
    component.endorsementApprovalForm.get('approveFiscalActivity')?.setValue(true);
    expect(control.value).toBeInstanceOf(Date);
    component.endorsementApprovalForm.get('approveFiscalActivity')?.setValue(false);
    expect(control.value).toBeNull();
  });

  it('statusCode returns the nested planFiscalStatusCode value', () => {
    component.fiscal = { ...mockFiscal, planFiscalStatusCode: { planFiscalStatusCode: FiscalStatuses.PREPARED } };
    expect(component.statusCode).toBe(FiscalStatuses.PREPARED);
  });

  it('statusCode returns undefined when fiscal or status is missing', () => {
    component.fiscal = { ...mockFiscal, planFiscalStatusCode: undefined as any };
    expect(component.statusCode).toBeUndefined();

    component.fiscal = undefined as any;
    expect(component.statusCode).toBeUndefined();
  });

  it('isDraft is true only when status is DRAFT', () => {
    component.fiscal = { ...mockFiscal, planFiscalStatusCode: { planFiscalStatusCode: FiscalStatuses.DRAFT } };
    expect(component.isDraft).toBeTrue();

    component.fiscal = { ...mockFiscal, planFiscalStatusCode: { planFiscalStatusCode: FiscalStatuses.PREPARED } };
    expect(component.isDraft).toBeFalse();
  });

  it('isCardDisabled is true in DRAFT, COMPLETE, and CANCELLED', () => {
    component.fiscal = { ...mockFiscal, planFiscalStatusCode: { planFiscalStatusCode: FiscalStatuses.DRAFT } };
    expect(component.isCardDisabled).toBeTrue();

    component.fiscal = { ...mockFiscal, planFiscalStatusCode: { planFiscalStatusCode: FiscalStatuses.COMPLETE } };
    expect(component.isCardDisabled).toBeTrue();

    component.fiscal = { ...mockFiscal, planFiscalStatusCode: { planFiscalStatusCode: FiscalStatuses.CANCELLED } };
    expect(component.isCardDisabled).toBeTrue();
  });

  it('isCardDisabled is false in PROPOSED, PREPARED, and IN_PROGRESS', () => {
    component.fiscal = { ...mockFiscal, planFiscalStatusCode: { planFiscalStatusCode: FiscalStatuses.PROPOSED } };
    expect(component.isCardDisabled).toBeFalse();

    component.fiscal = { ...mockFiscal, planFiscalStatusCode: { planFiscalStatusCode: FiscalStatuses.PREPARED } };
    expect(component.isCardDisabled).toBeFalse();

    component.fiscal = { ...mockFiscal, planFiscalStatusCode: { planFiscalStatusCode: FiscalStatuses.IN_PROGRESS } };
    expect(component.isCardDisabled).toBeFalse();
  });

  it('showDraftTooltip mirrors isDraft (true only in DRAFT)', () => {
    component.fiscal = { ...mockFiscal, planFiscalStatusCode: { planFiscalStatusCode: FiscalStatuses.DRAFT } };
    expect(component.showDraftTooltip).toBeTrue();

    component.fiscal = { ...mockFiscal, planFiscalStatusCode: { planFiscalStatusCode: FiscalStatuses.COMPLETE } };
    expect(component.showDraftTooltip).toBeFalse();

    component.fiscal = { ...mockFiscal, planFiscalStatusCode: { planFiscalStatusCode: FiscalStatuses.PREPARED } };
    expect(component.showDraftTooltip).toBeFalse();
  });

  it('should reset isSaving when isSaving change goes from true to false', () => {
    component.isSaving = true;
    component.ngOnChanges({
      isSaving: {
        currentValue: false,
        previousValue: true,
        firstChange: false,
        isFirstChange: () => false,
      }
    });

    expect(component.isSaving).toBeFalse();
  });
});
