import { ComponentFixture, TestBed } from '@angular/core/testing';

import { EvaluationCriteriaComponent } from './evaluation-criteria.component';

describe('EvaluationCriteriaComponent', () => {
  let component: EvaluationCriteriaComponent;
  let fixture: ComponentFixture<EvaluationCriteriaComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [EvaluationCriteriaComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(EvaluationCriteriaComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
