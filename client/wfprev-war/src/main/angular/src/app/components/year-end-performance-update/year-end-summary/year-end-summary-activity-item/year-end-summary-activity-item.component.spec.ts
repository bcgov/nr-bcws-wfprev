import { ComponentFixture, TestBed } from '@angular/core/testing';

import { YearEndSummaryActivityItemComponent } from './year-end-summary-activity-item.component';

describe('YearEndSummaryActivityItemComponent', () => {
  let component: YearEndSummaryActivityItemComponent;
  let fixture: ComponentFixture<YearEndSummaryActivityItemComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [YearEndSummaryActivityItemComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(YearEndSummaryActivityItemComponent);
    component = fixture.componentInstance;
    component.activity = { data: {} } as any;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
