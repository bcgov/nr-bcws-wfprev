import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PerformanceUpdatesComponent } from './wfprev-performance-updates.component';

describe('PerformanceUpdatesComponent', () => {
  let component: PerformanceUpdatesComponent;
  let fixture: ComponentFixture<PerformanceUpdatesComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PerformanceUpdatesComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(PerformanceUpdatesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
