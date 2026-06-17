import { ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { PerformanceUpdateHeaderComponent } from './performance-update-header.component';
import { YearEndPerformanceUpdateExtended, ProgressStatus, ReportingPeriod, UpdateGeneralStatus } from '../../models';

class MockResizeObserver {
  observe() {}
  unobserve() {}
  disconnect() {}
}

const mockUpdate: YearEndPerformanceUpdateExtended = {
  submittedTimestamp: new Date('2026-06-11T00:00:00Z'),
  reportingPeriod: ReportingPeriod.Q2,
  progressStatusCode: ProgressStatus.OnTrack,
  updateGeneralStatus: UpdateGeneralStatus.Draft,
  submittedByUserid: '',
  submittedByGuid: '',
  generalUpdateComment: '',
  submittedBy: 'Test User',
  forecastAmount: 10000,
  forecastAdjustmentAmount: 0,
  previousForecastAmount: 0,
  forecastAdjustmentRationale: '',
  budgetHighRiskAmount: 0,
  budgetHighRiskRationale: '',
  budgetMediumRiskAmount: 0,
  budgetMediumRiskRationale: '',
  budgetLowRiskAmount: 0,
  budgetLowRiskRationale: '',
  budgetCompletedAmount: 0,
  budgetCompletedDescription: '',
  totalAmount: 0,
  isCarryForwardInd: false,
  outstandingObligationsInd: false,
  statusManagementStatus: UpdateGeneralStatus.Cancelled,
  fiscalYearFormatted: '',
  updateDate: new Date('2026-06-11T00:00:00Z')
};

describe('PerformanceUpdateHeaderComponent', () => {
  let component: PerformanceUpdateHeaderComponent;
  let fixture: ComponentFixture<PerformanceUpdateHeaderComponent>;

  beforeAll(() => {
    window.ResizeObserver = window.ResizeObserver || MockResizeObserver as any;
  });

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PerformanceUpdateHeaderComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(PerformanceUpdateHeaderComponent);
    component = fixture.componentInstance;
    component.update = mockUpdate;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should render the submitted date', () => {
    fixture.detectChanges();
    const date = fixture.debugElement.query(By.css('.performance-update-title'));
    expect(date).toBeTruthy();
  });

  it('should show the Q2 reporting period label', () => {
    fixture.detectChanges();
    const text = fixture.nativeElement.textContent;
    expect(text).toContain('End of Q2');
  });

  it('should show on-track badge when progressStatusCode is OnTrack', () => {
    fixture.detectChanges();
    const badge = fixture.debugElement.query(By.css('wfprev-status-badge[type="on-track"]'));
    expect(badge).toBeTruthy();
  });

  it('should not show delayed badge when progressStatusCode is OnTrack', () => {
    fixture.detectChanges();
    const badge = fixture.debugElement.query(By.css('wfprev-status-badge[type="delayed"]'));
    expect(badge).toBeNull();
  });

  it('should show forecast-increased badge when forecastAdjustmentAmount is positive', () => {
    component.update = { ...mockUpdate, forecastAdjustmentAmount: 500 };
    fixture.detectChanges();
    const badge = fixture.debugElement.query(By.css('wfprev-status-badge[type="forecast-increased"]'));
    expect(badge).toBeTruthy();
  });

  it('should show forecast-decreased badge when forecastAdjustmentAmount is negative', () => {
    component.update = { ...mockUpdate, forecastAdjustmentAmount: -500 };
    fixture.detectChanges();
    const badge = fixture.debugElement.query(By.css('wfprev-status-badge[type="forecast-decreased"]'));
    expect(badge).toBeTruthy();
  });

  it('should not show forecast badges when forecastAdjustmentAmount is zero', () => {
    fixture.detectChanges();
    const increased = fixture.debugElement.query(By.css('wfprev-status-badge[type="forecast-increased"]'));
    const decreased = fixture.debugElement.query(By.css('wfprev-status-badge[type="forecast-decreased"]'));
    expect(increased).toBeNull();
    expect(decreased).toBeNull();
  });

  it('should show carry forward badge when isCarryForwardInd is true', () => {
    component.update = { ...mockUpdate, isCarryForwardInd: true };
    fixture.detectChanges();
    const badges = fixture.debugElement.queryAll(By.css('wfprev-status-badge[type="warning"]'));
    const labels = badges.map(b => b.attributes['label']);
    expect(labels).toContain('Carry Forward');
  });

  it('should show outstanding obligations badge when outstandingObligationsInd is true', () => {
    component.update = { ...mockUpdate, outstandingObligationsInd: true };
    fixture.detectChanges();
    const badges = fixture.debugElement.queryAll(By.css('wfprev-status-badge[type="warning"]'));
    const labels = badges.map(b => b.attributes['label']);
    expect(labels).toContain('Outstanding Obligations');
  });

  it('should show draft status badge when updateGeneralStatus is Draft', () => {
    fixture.detectChanges();
    const badge = fixture.debugElement.query(By.css('wfprev-status-badge[type="status-draft"]'));
    expect(badge).toBeTruthy();
  });

  it('should show complete status badge when updateGeneralStatus is Complete', () => {
    component.update = { ...mockUpdate, updateGeneralStatus: UpdateGeneralStatus.Complete };
    fixture.detectChanges();
    const badge = fixture.debugElement.query(By.css('wfprev-status-badge[type="status-complete"]'));
    expect(badge).toBeTruthy();
  });

  it('should not have stack-badges class initially', () => {
    fixture.detectChanges();
    const content = fixture.debugElement.query(By.css('.performance-update-header-content'));
    expect(content.nativeElement.classList.contains('stack-badges')).toBeFalse();
  });

  it('should add stack-badges class when shouldStack is true', () => {
    component.shouldStack = true;
    fixture.detectChanges();
    const content = fixture.debugElement.query(By.css('.performance-update-header-content'));
    expect(content.nativeElement.classList.contains('stack-badges')).toBeTrue();
  });

  it('should show date inside title-section when shouldStack is true', () => {
    component.shouldStack = true;
    fixture.detectChanges();
    const titleSection = fixture.debugElement.query(By.css('.title-section'));
    const date = titleSection.query(By.css('.performance-update-title'));
    expect(date).toBeTruthy();
  });

  it('should show date inside title-group and not inside title-section when shouldStack is false', () => {
    component.shouldStack = false;
    fixture.detectChanges();
    const titleSection = fixture.debugElement.query(By.css('.title-section'));
    const dateInTitleSection = titleSection.query(By.css('.performance-update-title'));
    const allDates = fixture.debugElement.queryAll(By.css('.performance-update-title'));
    expect(dateInTitleSection).toBeNull();
    expect(allDates.length).toBe(1);
  });

  describe('checkOverlap', () => {
    it('should set shouldStack to true when estimated width exceeds host width', () => {
      fixture.detectChanges();
      const host = fixture.nativeElement;
      spyOn(host, 'getBoundingClientRect').and.returnValue({ width: 300 } as DOMRect);
      const headerLeft = host.querySelector('.header-left');
      const indicators = host.querySelector('.header-indicators-group');
      Object.defineProperty(headerLeft, 'scrollWidth', { get: () => 200, configurable: true });
      Object.defineProperty(indicators, 'scrollWidth', { get: () => 200, configurable: true });
      component.checkOverlap();
      expect(component.shouldStack).toBeTrue();
    });

    it('should set shouldStack to false when there is enough room', () => {
      fixture.detectChanges();
      component.shouldStack = false;
      const host = fixture.nativeElement;
      spyOn(host, 'getBoundingClientRect').and.returnValue({ width: 1200 } as DOMRect);
      component.checkOverlap();
      expect(component.shouldStack).toBeFalse();
    });
  });
});