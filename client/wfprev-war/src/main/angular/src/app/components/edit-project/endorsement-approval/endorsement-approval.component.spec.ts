import { ComponentFixture, fakeAsync, TestBed, tick } from '@angular/core/testing';
import { MatDialog } from '@angular/material/dialog';
import { of } from 'rxjs';
import { ProjectFiscal } from 'src/app/components/models';
import { EndorsementCode, FiscalStatuses } from 'src/app/utils/constants';
import { EndorsementApprovalComponent } from './endorsement-approval.component';

describe('EndorsementApprovalComponent', () => {
  let component: EndorsementApprovalComponent;
  let fixture: ComponentFixture<EndorsementApprovalComponent>;

  const mockFiscal: ProjectFiscal = {
    activityCategoryCode: 'CATEGORY',
    approvedTimestamp: '2023-10-11T00:00:00Z',
    approverName: 'Bob',
    businessAreaComment: 'Approved',
    endorsementComment: 'Looks good',
    endorsementTimestamp: '2023-10-10T00:00:00Z',
    endorserName: 'Alice',
    firstNationsDelivPartInd: false,
    firstNationsEngagementInd: false,
    fiscalActualAmount: 600,
    fiscalActualCostPerHaAmt: 12,
    fiscalPlannedCostPerHaAmt: 20,
    fiscalPlannedProjectSizeHa: 50,
    fiscalReportedSpendAmount: 500,
    fiscalYear: 2024,
    isApprovedInd: true,
    isDelayedInd: false,
    planFiscalStatusCode: { planFiscalStatusCode: 'ACTIVE' },
    projectFiscalName: 'Test Fiscal',
    projectGuid: '123',
    projectPlanStatusCode: 'ACTIVE',
    totalCostEstimateAmount: 1000,
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [EndorsementApprovalComponent],
      providers: [
        {
          provide: MatDialog,
          useValue: {
            open: jasmine.createSpy('open').and.returnValue({
              afterClosed: () => of(true)
            })
          }
        }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(EndorsementApprovalComponent);
    component = fixture.componentInstance;
    component.currentUser = 'Test User';
    component.fiscal = mockFiscal;
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
      approvedTimestamp: undefined,
      approverName: undefined,
      businessAreaComment: '',
      endorsementComment: '',
      endorsementTimestamp: undefined,
      endorserName: undefined,
      isApprovedInd: false,
    };

    component.fiscal = noEndorseOrApproval;

    component.ngOnChanges({
      fiscal: {
        currentValue: noEndorseOrApproval,
        firstChange: true,
        isFirstChange: () => true,
        previousValue: null,
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

  it('should emit saveEndorsement with updated fiscal on save', async () => {
    spyOn<any>(component, 'confirmStatusChange').and.returnValue(Promise.resolve(true));
    const emitSpy = spyOn(component.saveEndorsement, 'emit');
    component.fiscal = mockFiscal;
    component.endorsementApprovalForm.patchValue({
      approvalComment: 'New approval',
      approvalDate: new Date('2024-02-01'),
      approveFiscalActivity: true,
      endorseFiscalActivity: true,
      endorsementComment: 'New endorsement',
      endorsementDate: new Date('2024-01-01'),
    });

    await component.onSave();

    const emittedFiscal = emitSpy.calls.mostRecent()!.args[0];
    expect(emittedFiscal!.endorserName).toBe('Alice');
    expect(emittedFiscal!.endorsementComment).toBe('New endorsement');
    expect(emittedFiscal!.isApprovedInd).toBeTrue();
    expect(emittedFiscal!.businessAreaComment).toBe('New approval');
  });

  it('should set planFiscalStatusCode to PROPOSED if endorsement removed', async () => {
    spyOn<any>(component, 'confirmStatusChange').and.returnValue(Promise.resolve(true));

    const emitSpy = spyOn(component.saveEndorsement, 'emit');

    component.fiscal = { ...mockFiscal, planFiscalStatusCode: { planFiscalStatusCode: 'ACTIVE' } };
    component.endorsementApprovalForm.patchValue({
      approveFiscalActivity: true,
      endorseFiscalActivity: false,
    });

    await component.onSave();

    const emittedFiscal = emitSpy.calls.mostRecent()!.args[0];
    expect(emittedFiscal!.planFiscalStatusCode?.planFiscalStatusCode).toBe(FiscalStatuses.PROPOSED);
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
        firstChange: false,
        isFirstChange: () => false,
        previousValue: true,
      }
    });

    expect(component.isSaving).toBeFalse();
  });

  it('resets to PROPOSED and clears fields when endorsement removed (confirm = true)', async () => {
    spyOn<any>(component, 'confirmStatusChange').and.returnValue(Promise.resolve(true));
    const emitSpy = spyOn(component.saveEndorsement, 'emit');

    component.fiscal = {
      ...mockFiscal,
      planFiscalStatusCode: { planFiscalStatusCode: 'ACTIVE' },
    };

    // endorsementRemoved = true; approval still checked
    component.endorsementApprovalForm.patchValue({
      approvalComment: 'keep approval',
      approvalDate: new Date('2024-02-01'),
      approveFiscalActivity: true,
      endorseFiscalActivity: false,
    });

    // Act
    await component.onSave();

    // Assert
    expect(emitSpy).toHaveBeenCalled();
    const { args } = emitSpy.calls.mostRecent();
    const payload = args[0] as ProjectFiscal;

    // Forced PROPOSED
    expect(payload.planFiscalStatusCode.planFiscalStatusCode).toBe(FiscalStatuses.PROPOSED);

    // Endorsement cleared
    expect(payload.endorserName).toBeUndefined();
    expect(payload.endorsementTimestamp).toBeUndefined();
    expect(payload.endorsementCode).toEqual({ endorsementCode: EndorsementCode.NOT_ENDORS });
    expect(payload.endorsementComment).toBeUndefined();
    expect(payload.endorsementEvalTimestamp).toBeUndefined();
    expect(payload.endorserUserGuid).toBeUndefined();
    expect(payload.endorserUserUserid).toBeUndefined();
    expect(payload.endorseApprUpdateUserid).toBeUndefined();
    expect(payload.endorseApprUpdatedTimestamp).toBeUndefined();
  });

  it('resets to proposed and clears fields when approval removed (confirm = true)', async () => {
    spyOn<any>(component, 'confirmStatusChange').and.returnValue(Promise.resolve(true));
    const emitSpy = spyOn(component.saveEndorsement, 'emit');

    component.fiscal = {
      ...mockFiscal,
      planFiscalStatusCode: { planFiscalStatusCode: 'ACTIVE' },
    };

    component.endorsementApprovalForm.patchValue({
      approveFiscalActivity: false,
      endorseFiscalActivity: true,
      endorsementComment: 'endorse!',
      endorsementDate: new Date('2024-01-01'),
    });

    await component.onSave();

    expect(emitSpy).toHaveBeenCalled();
    const { args } = emitSpy.calls.mostRecent();
    const payload = args[0] as ProjectFiscal;

    expect(payload.planFiscalStatusCode.planFiscalStatusCode).toBe(FiscalStatuses.PROPOSED);
    expect(payload.isApprovedInd).toBeFalse();
    expect(payload.approvedTimestamp).toBeUndefined();
    expect(payload.approverName).toBeUndefined();
    expect(payload.approverUserGuid).toBeUndefined();
    expect(payload.approverUserUserid).toBeUndefined();
    expect(payload.businessAreaComment).toBeUndefined();
  });

  it('does not emit when user cancels the revert confirmation', async () => {
    spyOn<any>(component, 'confirmStatusChange').and.returnValue(Promise.resolve(false));
    const emitSpy = spyOn(component.saveEndorsement, 'emit');
    component.fiscal = { ...mockFiscal, planFiscalStatusCode: { planFiscalStatusCode: 'ACTIVE' } };
    component.endorsementApprovalForm.patchValue({
      approveFiscalActivity: true,
      endorseFiscalActivity: false,
    });

    await component.onSave();

    expect(emitSpy).not.toHaveBeenCalled();
  });

  it('calls confirmStatusChange with the current status when reverting', async () => {
    const confirmSpy = spyOn<any>(component, 'confirmStatusChange').and.returnValue(Promise.resolve(true));
    const emitSpy = spyOn(component.saveEndorsement, 'emit');

    component.fiscal = {
      ...mockFiscal,
      planFiscalStatusCode: { planFiscalStatusCode: 'ACTIVE' },
    };

    component.endorsementApprovalForm.patchValue({
      approveFiscalActivity: true,
      endorseFiscalActivity: false,
    });

    await component.onSave();

    expect(confirmSpy).toHaveBeenCalledWith('ACTIVE', 'Proposed');
    expect(emitSpy).toHaveBeenCalled();
  });

  it('should disable form when isReadonly is true', () => {
    component.isReadonly = true;
    component.fiscal = { ...mockFiscal };
    component.ngOnChanges({
      fiscal: {
        currentValue: component.fiscal,
        previousValue: null,
        firstChange: true,
        isFirstChange: () => true
      }
    });
    expect(component.endorsementApprovalForm.disabled).toBeTrue();
  });

});
