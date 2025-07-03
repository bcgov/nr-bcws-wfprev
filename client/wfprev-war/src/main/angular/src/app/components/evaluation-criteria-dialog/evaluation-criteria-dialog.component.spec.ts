import { ComponentFixture, TestBed } from '@angular/core/testing';

import { EvaluationCriteriaDialogComponent } from './evaluation-criteria-dialog.component';

describe('EvaluationCriteriaDialogComponent', () => {
  let component: EvaluationCriteriaDialogComponent;
  let fixture: ComponentFixture<EvaluationCriteriaDialogComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [EvaluationCriteriaDialogComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(EvaluationCriteriaDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
