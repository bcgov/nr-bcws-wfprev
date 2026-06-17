import { ComponentFixture, TestBed } from '@angular/core/testing';

import { YearEndSummaryActivityListComponent } from './year-end-summary-activity-list.component';

describe('YearEndSummaryActivityListComponent', () => {
  let component: YearEndSummaryActivityListComponent;
  let fixture: ComponentFixture<YearEndSummaryActivityListComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [YearEndSummaryActivityListComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(YearEndSummaryActivityListComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
