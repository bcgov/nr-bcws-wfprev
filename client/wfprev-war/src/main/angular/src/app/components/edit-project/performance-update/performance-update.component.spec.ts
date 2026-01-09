import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PerformanceUpdateComponent } from './performance-update.component';

describe('PerformanceUpdateComponent', () => {
  let component: PerformanceUpdateComponent;
  let fixture: ComponentFixture<PerformanceUpdateComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PerformanceUpdateComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(PerformanceUpdateComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
