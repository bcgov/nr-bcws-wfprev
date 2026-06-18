import { ComponentFixture, TestBed } from '@angular/core/testing';
import { PerformanceUpdateSummaryComponent } from './performance-update-summary.component';
import { YearEndPerformanceUpdateExtended } from '../../models';

const mockUpdate: YearEndPerformanceUpdateExtended = {
  submittedBy: 'John Doe',
  generalUpdateComment: 'Progress is on track.',
  forecastAmount: 50000,
  forecastAdjustmentAmount: 5000,
  forecastAdjustmentRationale: 'Additional resources required.',
  budgetHighRiskAmount: 10000,
  budgetHighRiskRationale: 'Supply chain delays.',
  budgetMediumRiskAmount: 7000,
  budgetMediumRiskRationale: 'Weather conditions.',
  budgetLowRiskAmount: 3000,
  budgetLowRiskRationale: 'Minor scheduling conflicts.',
  budgetCompletedAmount: 30000,
  budgetCompletedDescription: 'Phase 1 complete.',
  totalAmount: 50000
} as any;

describe('PerformanceUpdateSummaryComponent', () => {
  let component: PerformanceUpdateSummaryComponent;
  let fixture: ComponentFixture<PerformanceUpdateSummaryComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PerformanceUpdateSummaryComponent]
    }).compileComponents();

    fixture = TestBed.createComponent(PerformanceUpdateSummaryComponent);
    component = fixture.componentInstance;
    component.update = { ...mockUpdate };
    fixture.detectChanges();
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should display submittedBy', () => {
    const el = fixture.nativeElement.querySelector('.submitted-by');
    expect(el.textContent).toContain('John Doe');
  });

  it('should display generalUpdateComment', () => {
    const el = fixture.nativeElement.querySelector('.general-updates-container');
    expect(el.textContent).toContain('Progress is on track.');
  });

  it('should display forecast amounts', () => {
    const el = fixture.nativeElement.querySelector('.budget-adjustment-details');
    expect(el.textContent).toContain('$50,000.00');
    expect(el.textContent).toContain('$5,000.00');
  });

  it('should display forecastAdjustmentRationale', () => {
    const el = fixture.nativeElement.querySelector('.budget-adjustment-details');
    expect(el.textContent).toContain('Additional resources required.');
  });

  it('should show forecast-increased badge when forecastAdjustmentAmount is positive', () => {
    component.update = { ...mockUpdate, forecastAdjustmentAmount: 5000 };
    fixture.detectChanges();
    const badge = fixture.nativeElement.querySelector('wfprev-status-badge[type="forecast-increased"]');
    expect(badge).toBeTruthy();
  });

  it('should display budget risk amounts and rationales', () => {
    const el = fixture.nativeElement.querySelector('.budget-breakdown-table');
    expect(el.textContent).toContain('$10,000.00');
    expect(el.textContent).toContain('Supply chain delays.');
    expect(el.textContent).toContain('$7,000.00');
    expect(el.textContent).toContain('Weather conditions.');
    expect(el.textContent).toContain('$3,000.00');
    expect(el.textContent).toContain('Minor scheduling conflicts.');
    expect(el.textContent).toContain('$30,000.00');
    expect(el.textContent).toContain('Phase 1 complete.');
  });

  it('should display total amount', () => {
    const el = fixture.nativeElement.querySelector('.budget-breakdown-last-row');
    expect(el.textContent).toContain('$50,000.00');
  });
});