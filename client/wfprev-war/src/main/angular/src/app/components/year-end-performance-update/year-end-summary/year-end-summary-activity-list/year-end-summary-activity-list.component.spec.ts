import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Component, Input } from '@angular/core';
import { YearEndSummaryActivityListComponent } from './year-end-summary-activity-list.component';
import { YearEndActivityViewModel } from '../../../models';

@Component({ selector: 'wfprev-year-end-summary-activity-item', template: '', standalone: true })
class MockYearEndSummaryActivityItemComponent {
  @Input() activity!: YearEndActivityViewModel;
  @Input() fiscalGuid!: string;
}

const mockActivity = (guid: string): YearEndActivityViewModel => ({
  data: {
    activityGuid: guid,
    activityName: `Activity ${guid}`,
  }
} as any);

describe('YearEndSummaryActivityListComponent', () => {
  let component: YearEndSummaryActivityListComponent;
  let fixture: ComponentFixture<YearEndSummaryActivityListComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [YearEndSummaryActivityListComponent],
    })
      .overrideComponent(YearEndSummaryActivityListComponent, {
        set: { imports: [MockYearEndSummaryActivityItemComponent] }
      })
      .compileComponents();

    fixture = TestBed.createComponent(YearEndSummaryActivityListComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    component.activities = [];
    component.fiscalGuid = 'fiscal-guid-123';
    fixture.detectChanges();
    expect(component).toBeTruthy();
  });

  it('should display the Activities title', () => {
    component.activities = [];
    component.fiscalGuid = 'fiscal-guid-123';
    fixture.detectChanges();
    const el = fixture.nativeElement.querySelector('.activity-list-title');
    expect(el.textContent).toContain('Activities:');
  });

  it('should render no activity items when activities is empty', () => {
    component.activities = [];
    component.fiscalGuid = 'fiscal-guid-123';
    fixture.detectChanges();
    const items = fixture.nativeElement.querySelectorAll('wfprev-year-end-summary-activity-item');
    expect(items.length).toBe(0);
  });

  it('should render one activity item', () => {
    component.activities = [mockActivity('guid-1')];
    component.fiscalGuid = 'fiscal-guid-123';
    fixture.detectChanges();
    const items = fixture.nativeElement.querySelectorAll('wfprev-year-end-summary-activity-item');
    expect(items.length).toBe(1);
  });

  it('should render multiple activity items', () => {
    component.activities = [mockActivity('guid-1'), mockActivity('guid-2'), mockActivity('guid-3')];
    component.fiscalGuid = 'fiscal-guid-123';
    fixture.detectChanges();
    const items = fixture.nativeElement.querySelectorAll('wfprev-year-end-summary-activity-item');
    expect(items.length).toBe(3);
  });

  it('should pass fiscalGuid to each activity item', () => {
    component.activities = [mockActivity('guid-1'), mockActivity('guid-2')];
    component.fiscalGuid = 'fiscal-guid-123';
    fixture.detectChanges();
    const items = fixture.debugElement.queryAll(
      sel => sel.name === 'wfprev-year-end-summary-activity-item'
    );
    items.forEach(item => {
      expect(item.componentInstance.fiscalGuid).toBe('fiscal-guid-123');
    });
  });

  it('should pass each activity to the correct item', () => {
    const activities = [mockActivity('guid-1'), mockActivity('guid-2')];
    component.activities = activities;
    component.fiscalGuid = 'fiscal-guid-123';
    fixture.detectChanges();
    const items = fixture.debugElement.queryAll(
      sel => sel.name === 'wfprev-year-end-summary-activity-item'
    );
    expect(items[0].componentInstance.activity).toEqual(activities[0]);
    expect(items[1].componentInstance.activity).toEqual(activities[1]);
  });
});