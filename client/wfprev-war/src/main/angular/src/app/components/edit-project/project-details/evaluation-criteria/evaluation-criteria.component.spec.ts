import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormBuilder } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialog, MatDialogRef } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { of, throwError } from 'rxjs';
import { EvaluationCriteriaDialogComponent } from 'src/app/components/evaluation-criteria-dialog/evaluation-criteria-dialog.component';
import { CodeTableServices } from 'src/app/services/code-table-services';
import { ProjectService } from 'src/app/services/project-services';

describe('EvaluationCriteriaDialogComponent', () => {
  let component: EvaluationCriteriaDialogComponent;
  let fixture: ComponentFixture<EvaluationCriteriaDialogComponent>;
  let mockDialog: jasmine.SpyObj<MatDialog>;
  let mockDialogRef: jasmine.SpyObj<MatDialogRef<EvaluationCriteriaDialogComponent>>;
  let mockCodeTableServices: jasmine.SpyObj<CodeTableServices>;
  let mockProjectService: jasmine.SpyObj<ProjectService>;
  let mockSnackBar: jasmine.SpyObj<MatSnackBar>;

  beforeEach(async () => {
    mockDialog = jasmine.createSpyObj('MatDialog', ['open']);
    mockDialogRef = jasmine.createSpyObj('MatDialogRef', ['close']);
    mockCodeTableServices = jasmine.createSpyObj('CodeTableServices', ['fetchCodeTable']);
    mockProjectService = jasmine.createSpyObj('ProjectService', ['createEvaluationCriteriaSummary', 'updateEvaluationCriteriaSummary']);
    mockSnackBar = jasmine.createSpyObj('MatSnackBar', ['open']);

    await TestBed.configureTestingModule({
      imports: [EvaluationCriteriaDialogComponent],
      providers: [
        FormBuilder,
        { provide: MatDialog, useValue: mockDialog },
        { provide: MatDialogRef, useValue: mockDialogRef },
        { provide: CodeTableServices, useValue: mockCodeTableServices },
        { provide: ProjectService, useValue: mockProjectService },
        { provide: MatSnackBar, useValue: mockSnackBar },
        { provide: MAT_DIALOG_DATA, useValue: { project: { projectGuid: '123', projectTypeCode: { projectTypeCode: 'FUEL_MGMT' } } } }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(EvaluationCriteriaDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize form', () => {
    component.initializeForm();
    expect(component.criteriaForm).toBeDefined();
  });

  it('should setup value change handlers', () => {
    component.initializeForm();
    component.setupValueChangeHandlers();
    component.criteriaForm.get('wuiRiskClassCode')?.setValue('1');
    component.criteriaForm.get('localWuiRiskClassCode')?.setValue('2');
    expect(component.coarseTotal).toBe(2);
  });

  it('should assign code table data', () => {
    const evalData = {
      _embedded: {
        evaluationCriteriaCode: [{ projectTypeCode: 'FUEL_MGMT', weightedRank: 1 }]
      }
    };
    component.assignCodeTableData('evaluationCriteriaCode', evalData);
    expect(component.mediumFilters.length).toBe(1);

    const wuiData = {
      _embedded: {
        wuiRiskClassRank: [{ wuiRiskClassCode: 'A', weightedRank: 1 }]
      }
    };
    component.assignCodeTableData('wuiRiskClassCode', wuiData);
    expect(component.wuiRiskClassCode.length).toBe(1);
  });

  it('should toggle medium selection', () => {
    component.mediumFilters = [{ evaluationCriteriaGuid: 'guid1', weightedRank: 1 }];
    const event = { target: { checked: true } } as any;
    component.toggleMedium('guid1', event);
    expect(component.selectedMedium.has('guid1')).toBeTrue();
  });

  it('should toggle fine selection', () => {
    component.fineFilters = [{ evaluationCriteriaGuid: 'guid1', weightedRank: 0 }];
    const event = { target: { checked: true } } as any;
    component.toggleFine('guid1', event);
    expect(component.selectedFine.has('guid1')).toBeTrue();
  });

  it('should calculate medium total', () => {
    component.mediumFilters = [{ evaluationCriteriaGuid: '1', weightedRank: 3 }];
    component.selectedMedium.add('1');
    component.calculateMediumTotal();
    expect(component.mediumTotal).toBe(3);
  });

  it('should calculate fine total', () => {
    component.fineFilters = [{ evaluationCriteriaGuid: '1', weightedRank: 2 }];
    component.selectedFine.add('1');
    component.calculateFineTotal();
    expect(component.fineTotal).toBe(2);
  });

  it('should update coarse total', () => {
    component.initializeForm();
    component.criteriaForm.get('wuiRiskClassCode')?.setValue('1');
    component.criteriaForm.get('localWuiRiskClassCode')?.setValue('');
    component.updateCoarseTotal();
    expect(component.coarseTotal).toBe(1);
  });

  it('should create evaluation criteria summary', () => {
    component.initializeForm();
    spyOn(component as any, 'buildEvaluationCriteriaSummaryModel').and.returnValue({});
    mockProjectService.createEvaluationCriteriaSummary.and.returnValue(of({}));
    component.onSave();
    expect(mockProjectService.createEvaluationCriteriaSummary).toHaveBeenCalled();
    expect(mockDialogRef.close).toHaveBeenCalled();
  });

  it('should handle error on create', () => {
    component.initializeForm();
    spyOn(component as any, 'buildEvaluationCriteriaSummaryModel').and.returnValue({});
    mockProjectService.createEvaluationCriteriaSummary.and.returnValue(throwError(() => new Error()));
    component.onSave();
    expect(mockProjectService.createEvaluationCriteriaSummary).toHaveBeenCalled();
  });

  it('should update evaluation criteria summary', () => {
    component.initializeForm();
    component.data.evaluationCriteriaSummary = { evaluationCriteriaSummaryGuid: 'guid' } as any;
    spyOn(component as any, 'buildEvaluationCriteriaSummaryModel').and.returnValue({});
    mockProjectService.updateEvaluationCriteriaSummary.and.returnValue(of({}));
    component.onSave();
    expect(mockProjectService.updateEvaluationCriteriaSummary).toHaveBeenCalled();
    expect(mockDialogRef.close).toHaveBeenCalled();
  });

  it('should handle error on update', () => {
    component.initializeForm();
    component.data.evaluationCriteriaSummary = { evaluationCriteriaSummaryGuid: 'guid' } as any;
    spyOn(component as any, 'buildEvaluationCriteriaSummaryModel').and.returnValue({});
    mockProjectService.updateEvaluationCriteriaSummary.and.returnValue(throwError(() => new Error()));
    component.onSave();
    expect(mockProjectService.updateEvaluationCriteriaSummary).toHaveBeenCalled();
  });

  it('should build evaluation criteria summary model', () => {
    component.initializeForm();
    const result = component.buildEvaluationCriteriaSummaryModel();
    expect(result).toBeDefined();
  });

  it('should format code label', () => {
    expect(component.formatCodeLabel('A_B')).toBe('A B');
    expect(component.formatCodeLabel(undefined)).toBe('');
  });

  it('should cancel dialog', () => {
    const afterClosed$ = of(true);
    mockDialog.open.and.returnValue({ afterClosed: () => afterClosed$ } as MatDialogRef<any>);
    component.onCancel();
    expect(mockDialog.open).toHaveBeenCalled();
    expect(mockDialogRef.close).toHaveBeenCalled();
  });
});
