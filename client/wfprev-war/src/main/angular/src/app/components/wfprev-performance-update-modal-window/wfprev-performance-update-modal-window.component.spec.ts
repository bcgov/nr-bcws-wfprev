import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PerformanceUpdateModalWindowComponent } from './wfprev-performance-update-modal-window.component';

describe('PerformanceUpdateModalWindowComponent', () => {
  let component: PerformanceUpdateModalWindowComponent;
  let fixture: ComponentFixture<PerformanceUpdateModalWindowComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PerformanceUpdateModalWindowComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(PerformanceUpdateModalWindowComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
