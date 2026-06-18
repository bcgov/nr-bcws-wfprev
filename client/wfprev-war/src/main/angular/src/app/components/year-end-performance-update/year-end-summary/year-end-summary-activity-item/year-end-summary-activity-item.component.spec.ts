import { ComponentFixture, TestBed } from '@angular/core/testing';
import { YearEndSummaryActivityItemComponent } from './year-end-summary-activity-item.component';
import { YearEndActivityViewModel } from '../../../models';
import { Component, Input } from '@angular/core';

@Component({ selector: 'wfprev-status-badge', template: '', standalone: true })
class MockStatusBadgeComponent {
  @Input() type: string = '';
  @Input() label: string = '';
}

@Component({ selector: 'wfprev-project-files', template: '', standalone: true })
class MockProjectFilesComponent {
  @Input() activityGuid: string = '';
  @Input() isReadonly: boolean = false;
  @Input() isSummaryView: boolean = false;
  @Input() showViewButton: boolean = false;
  @Input() fiscalGuid: string = '';
}

const mockActivity = (overrides: any = {}): YearEndActivityViewModel => ({
  data: {
    activityName: 'Test Activity',
    activityStatusCode: { activityStatusCode: 'ACTIVE' },
    isResultsReportableInd: false,
    completedAreaHa: 10,
    reportedSpendAmount: 5000,
    finalOutcomeComments: 'All good.',
    outstandingObligationsInd: false,
    isCarryForwardInd: false,
    activityComment: '',
    activityGuid: 'guid-123',
    ...overrides
  }
} as any);

describe('YearEndSummaryActivityItemComponent', () => {
  let component: YearEndSummaryActivityItemComponent;
  let fixture: ComponentFixture<YearEndSummaryActivityItemComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [YearEndSummaryActivityItemComponent],
    })
      .overrideComponent(YearEndSummaryActivityItemComponent, {
        set: { imports: [MockStatusBadgeComponent, MockProjectFilesComponent] }
      })
      .compileComponents();

    fixture = TestBed.createComponent(YearEndSummaryActivityItemComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    component.activity = mockActivity();
    component.fiscalGuid = 'fiscal-guid-123';
    fixture.detectChanges();
    expect(component).toBeTruthy();
  });

  it('should display activity name', () => {
    component.activity = mockActivity();
    component.fiscalGuid = 'fiscal-guid-123';
    fixture.detectChanges();
    const el = fixture.nativeElement.querySelector('.activity-name');
    expect(el.textContent).toContain('Test Activity');
  });

  it('should display completed hectares', () => {
    component.activity = mockActivity();
    component.fiscalGuid = 'fiscal-guid-123';
    fixture.detectChanges();
    const el = fixture.nativeElement.querySelector('.activity-metrics');
    expect(el.textContent).toContain('10');
  });

  it('should display reported spend amount', () => {
    component.activity = mockActivity();
    component.fiscalGuid = 'fiscal-guid-123';
    fixture.detectChanges();
    const el = fixture.nativeElement.querySelector('.activity-metrics');
    expect(el.textContent).toContain('5000');
  });

  it('should display final outcome comments', () => {
    component.activity = mockActivity();
    component.fiscalGuid = 'fiscal-guid-123';
    fixture.detectChanges();
    const el = fixture.nativeElement.querySelector('.final-outcome-content');
    expect(el.textContent).toContain('All good.');
  });

  describe('statusBadgeType', () => {
    it('should return status-in-progress for ACTIVE', () => {
      component.activity = mockActivity({ activityStatusCode: { activityStatusCode: 'ACTIVE' } });
      expect(component.statusBadgeType).toBe('status-in-progress');
    });

    it('should return deferred-filled for DEFERRED', () => {
      component.activity = mockActivity({ activityStatusCode: { activityStatusCode: 'DEFERRED' } });
      expect(component.statusBadgeType).toBe('deferred-filled');
    });

    it('should return cancelled for CANCELLED', () => {
      component.activity = mockActivity({ activityStatusCode: { activityStatusCode: 'CANCELLED' } });
      expect(component.statusBadgeType).toBe('cancelled');
    });

    it('should return status-complete for COMPLETED', () => {
      component.activity = mockActivity({ activityStatusCode: { activityStatusCode: 'COMPLETED' } });
      expect(component.statusBadgeType).toBe('status-complete');
    });

    it('should return substantially-complete for SUBS_COMPL', () => {
      component.activity = mockActivity({ activityStatusCode: { activityStatusCode: 'SUBS_COMPL' } });
      expect(component.statusBadgeType).toBe('substantially-complete');
    });

    it('should return null for unknown status code', () => {
      component.activity = mockActivity({ activityStatusCode: { activityStatusCode: 'UNKNOWN' } });
      expect(component.statusBadgeType).toBeNull();
    });

    it('should return null when activityStatusCode is undefined', () => {
      component.activity = mockActivity({ activityStatusCode: undefined });
      expect(component.statusBadgeType).toBeNull();
    });

    it('should render status badge when statusBadgeType is set', () => {
      component.activity = mockActivity({ activityStatusCode: { activityStatusCode: 'ACTIVE' } });
      component.fiscalGuid = 'fiscal-guid-123';
      fixture.detectChanges();
      const badge = fixture.nativeElement.querySelector('wfprev-status-badge');
      expect(badge).toBeTruthy();
    });

    it('should not render status badge when statusBadgeType is null', () => {
      component.activity = mockActivity({ activityStatusCode: undefined });
      component.fiscalGuid = 'fiscal-guid-123';
      fixture.detectChanges();
      const badge = fixture.nativeElement.querySelector('.activity-status wfprev-status-badge');
      expect(badge).toBeFalsy();
    });
  });

  describe('RESULTS Reportable indicator', () => {
    it('should show when isResultsReportableInd is true', () => {
      component.activity = mockActivity({ isResultsReportableInd: true });
      component.fiscalGuid = 'fiscal-guid-123';
      fixture.detectChanges();
      const el = fixture.nativeElement.querySelector('.indicator-item');
      expect(el).toBeTruthy();
      expect(el.textContent).toContain('RESULTS Reportable');
    });

    it('should not show when isResultsReportableInd is false', () => {
      component.activity = mockActivity({ isResultsReportableInd: false });
      component.fiscalGuid = 'fiscal-guid-123';
      fixture.detectChanges();
      const el = fixture.nativeElement.querySelector('.indicator-item');
      expect(el).toBeFalsy();
    });
  });

  describe('obligation badges', () => {
    it('should show outstanding obligations badge', () => {
      component.activity = mockActivity({ outstandingObligationsInd: true });
      component.fiscalGuid = 'fiscal-guid-123';
      fixture.detectChanges();
      const badges = fixture.nativeElement.querySelectorAll('.obligation-badges wfprev-status-badge');
      expect(badges.length).toBeGreaterThan(0);
    });

    it('should show carry forward badge', () => {
      component.activity = mockActivity({ isCarryForwardInd: true });
      component.fiscalGuid = 'fiscal-guid-123';
      fixture.detectChanges();
      const badges = fixture.nativeElement.querySelectorAll('.obligation-badges wfprev-status-badge');
      expect(badges.length).toBeGreaterThan(0);
    });

    it('should show carry forward block when either flag is set', () => {
      component.activity = mockActivity({ outstandingObligationsInd: true, activityComment: 'Plan to resolve.' });
      component.fiscalGuid = 'fiscal-guid-123';
      fixture.detectChanges();
      const el = fixture.nativeElement.querySelector('.carry-forward-content');
      expect(el.textContent).toContain('Plan to resolve.');
    });

    it('should not show obligation section when both flags are false', () => {
      component.activity = mockActivity({ outstandingObligationsInd: false, isCarryForwardInd: false });
      component.fiscalGuid = 'fiscal-guid-123';
      fixture.detectChanges();
      const el = fixture.nativeElement.querySelector('.obligation-badges');
      expect(el).toBeFalsy();
    });
  });

  describe('project files', () => {
    it('should render project files when activityGuid and fiscalGuid are set', () => {
      component.activity = mockActivity();
      component.fiscalGuid = 'fiscal-guid-123';
      fixture.detectChanges();
      const el = fixture.nativeElement.querySelector('wfprev-project-files');
      expect(el).toBeTruthy();
    });

    it('should not render project files when activityGuid is missing', () => {
      component.activity = mockActivity({ activityGuid: null });
      component.fiscalGuid = 'fiscal-guid-123';
      fixture.detectChanges();
      const el = fixture.nativeElement.querySelector('wfprev-project-files');
      expect(el).toBeFalsy();
    });

    it('should not render project files when fiscalGuid is missing', () => {
      component.activity = mockActivity();
      component.fiscalGuid = '';
      fixture.detectChanges();
      const el = fixture.nativeElement.querySelector('wfprev-project-files');
      expect(el).toBeFalsy();
    });
  });
});