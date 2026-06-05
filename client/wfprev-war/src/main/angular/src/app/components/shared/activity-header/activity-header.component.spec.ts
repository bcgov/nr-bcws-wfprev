import { ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { NgZone, ChangeDetectorRef } from '@angular/core';
import { ActivityHeaderComponent } from './activity-header.component';

class MockResizeObserver {
  observe() {}
  unobserve() {}
  disconnect() {}
}

describe('ActivityHeaderComponent', () => {
  let component: ActivityHeaderComponent;
  let fixture: ComponentFixture<ActivityHeaderComponent>;

  beforeAll(() => {
    window.ResizeObserver = window.ResizeObserver || MockResizeObserver as any;
  });

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ActivityHeaderComponent]
    }).compileComponents();

    fixture = TestBed.createComponent(ActivityHeaderComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should display the title', () => {
    component.title = 'Test Activity Title';
    fixture.detectChanges();
    const titleEl = fixture.debugElement.query(By.css('.activity-title')).nativeElement;
    expect(titleEl.textContent.trim()).toBe('Test Activity Title');
  });

  it('should show results reportable indicator', () => {
    component.isResultsReportable = true;
    fixture.detectChanges();
    const indicator = fixture.debugElement.query(By.css('img[alt="RESULTS Reportable"]'));
    expect(indicator).toBeTruthy();
  });

  it('should show outstanding obligations badge', () => {
    component.hasOutstandingObligations = true;
    fixture.detectChanges();
    const badge = fixture.debugElement.query(By.css('wfprev-status-badge[label="Outstanding Obligations"]'));
    expect(badge).toBeTruthy();
  });

  it('should show carry forward badge', () => {
    component.isCarryForward = true;
    fixture.detectChanges();
    const badge = fixture.debugElement.query(By.css('wfprev-status-badge[label="Carry Forward"]'));
    expect(badge).toBeTruthy();
  });

  it('should show spatial added / not added badge', () => {
    component.isSpatialAdded = true;
    fixture.detectChanges();
    const badge = fixture.debugElement.query(By.css('wfprev-status-badge[type="spatial-added"]'));
    expect(badge).toBeTruthy();
  }); 

  it('should show spatial added / not added badge when isSpatialAdded is false', () => {
    component.isSpatialAdded = false;
    fixture.detectChanges();
    const badge = fixture.debugElement.query(By.css('wfprev-status-badge[type="spatial-not-added"]'));
    expect(badge).toBeTruthy();
  });

  it('should show status complete badge when statusCode is COMPLETED', () => {
    component.statusCode = 'COMPLETED';
    fixture.detectChanges();
    const badge = fixture.debugElement.query(By.css('wfprev-status-badge[type="status-complete"]'));
    expect(badge).toBeTruthy();
  });

  it('should show status cancelled badge when statusCode is CANCELLED', () => {
    component.statusCode = 'CANCELLED';
    fixture.detectChanges();
    const badge = fixture.debugElement.query(By.css('wfprev-status-badge[type="status-cancelled"]'));
    expect(badge).toBeTruthy();
  });

  it('should stack badges when shouldStack is true', () => {
    component.shouldStack = true;
    fixture.detectChanges();
    const contentEl = fixture.debugElement.query(By.css('.activity-header-content')).nativeElement;
    expect(contentEl.classList.contains('stack-badges')).toBeTrue();
  });
});
