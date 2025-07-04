import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { MAT_DIALOG_DATA, MatDialogRef, MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ReactiveFormsModule } from '@angular/forms';
import { EvaluationCriteriaDialogComponent } from './evaluation-criteria-dialog.component';
import { ProjectService } from 'src/app/services/project-services';
import { CodeTableServices } from 'src/app/services/code-table-services';
import { Messages } from 'src/app/utils/constants';

describe('EvaluationCriteriaDialogComponent', () => {
  let component: EvaluationCriteriaDialogComponent;
  let fixture: ComponentFixture<EvaluationCriteriaDialogComponent>;
  let mockProjectService: jasmine.SpyObj<ProjectService>;
  let mockCodeTableServices: jasmine.SpyObj<CodeTableServices>;
  let mockDialog: jasmine.SpyObj<MatDialog>;
  let mockDialogRef: jasmine.SpyObj<MatDialogRef<EvaluationCriteriaDialogComponent>>;
  let mockSnackbar: jasmine.SpyObj<MatSnackBar>;

  const mockData = {
    project: {
      projectGuid: '123',
      projectTypeCode: { projectTypeCode: 'FUEL_MGMT' }
    }
  };

  beforeEach(async () => {
    mockProjectService = jasmine.createSpyObj('ProjectService', [
      'createEvaluationCriteriaSummary',
      'updateEvaluationCriteriaSummary'
    ]);

    mockCodeTableServices = jasmine.createSpyObj('CodeTableServices', ['fetchCodeTable']);
    mockDialog = jasmine.createSpyObj('MatDialog', ['open']);
    mockDialogRef = jasmine.createSpyObj('MatDialogRef', ['close']);
    mockSnackbar = jasmine.createSpyObj('MatSnackBar', ['open']);

    await TestBed.configureTestingModule({
      imports: [EvaluationCriteriaDialogComponent, ReactiveFormsModule],
      providers: [
        { provide: ProjectService, useValue: mockProjectService },
        { provide: CodeTableServices, useValue: mockCodeTableServices },
        { provide: MatDialog, useValue: mockDialog },
        { provide: MatDialogRef, useValue: mockDialogRef },
        { provide: MatSnackBar, useValue: mockSnackbar },
        { provide: MAT_DIALOG_DATA, useValue: mockData }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(EvaluationCriteriaDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize form and load code tables on ngOnInit', () => {
    const evalCodes = { _embedded: { evaluationCriteriaCode: [{ projectTypeCode: 'FUEL_MGMT', weightedRank: 1, evaluationCriteriaGuid: 'guid1' }] } };
    const wuiCodes = { _embedded: { wuiRiskClassRank: [{ wuiRiskClassCode: 'WUI1', weightedRank: 1 }] } };
    mockCodeTableServices.fetchCodeTable.and.callFake((key: string) => of(key === 'evaluationCriteriaCodes' ? evalCodes : wuiCodes));

    component.ngOnInit();

    expect(component.criteriaForm).toBeTruthy();
    expect(mockCodeTableServices.fetchCodeTable).toHaveBeenCalledWith('evaluationCriteriaCodes');
    expect(mockCodeTableServices.fetchCodeTable).toHaveBeenCalledWith('wuiRiskClassCodes');
  });

  it('should assign code table data', () => {
    const data = { _embedded: { evaluationCriteriaCode: [{ projectTypeCode: 'FUEL_MGMT', weightedRank: 2 }], wuiRiskClassRank: [{ wuiRiskClassCode: 'WUI', weightedRank: 1 }] } };
    component.assignCodeTableData('evaluationCriteriaCode', data);
    expect(component.mediumFilters.length).toBe(1);

    component.assignCodeTableData('wuiRiskClassCode', data);
    expect(component.wuiRiskClassCode.length).toBe(1);
  });

  it('should toggleMedium and calculateMediumTotal', () => {
    component.mediumFilters = [{ evaluationCriteriaGuid: 'g1', weightedRank: 2 }];
    const event = { target: { checked: true } } as any;
    component.toggleMedium('g1', event);
    expect(component.selectedMedium.has('g1')).toBeTrue();
    expect(component.mediumTotal).toBe(2);
  });

  it('should toggleFine and calculateFineTotal', () => {
    component.fineFilters = [{ evaluationCriteriaGuid: 'g2', weightedRank: 1 }];
    const event = { target: { checked: true } } as any;
    component.toggleFine('g2', event);
    expect(component.selectedFine.has('g2')).toBeTrue();
    expect(component.fineTotal).toBe(1);
  });

  it('should update coarse total based on form values', () => {
    component.initializeForm();
    component.criteriaForm.patchValue({ wuiRiskClassCode: 2 });
    component.updateCoarseTotal();
    expect(component.coarseTotal).toBe(2);

    component.criteriaForm.patchValue({ localWuiRiskClassCode: 3 });
    component.updateCoarseTotal();
    expect(component.coarseTotal).toBe(3);
  });

  it('should call onSave and create evaluation criteria summary', () => {
    component.initializeForm();
    component.criteriaForm.patchValue({ wuiRiskClassCode: 1 });
    mockProjectService.createEvaluationCriteriaSummary.and.returnValue(of({}));
    component.onSave();
    expect(mockProjectService.createEvaluationCriteriaSummary).toHaveBeenCalled();
  });

  it('should call onSave and update evaluation criteria summary', () => {
    component.initializeForm();
    component.criteriaForm.patchValue({ wuiRiskClassCode: 1 });
    component.data.evaluationCriteriaSummary = { evaluationCriteriaSummaryGuid: 'guid' } as any;
    mockProjectService.updateEvaluationCriteriaSummary.and.returnValue(of({}));
    component.onSave();
    expect(mockProjectService.updateEvaluationCriteriaSummary).toHaveBeenCalled();
  });

  it('should handle invalid form onSave', () => {
    component.criteriaForm = { valid: false } as any;
    spyOn(console, 'warn');
    component.onSave();
    expect(console.warn).toHaveBeenCalledWith('Form is invalid, not saving.');
  });

  it('should call onCancel and close dialog if confirmed', () => {
    mockDialog.open.and.returnValue({ afterClosed: () => of(true) } as any);
    component.onCancel();
    expect(mockDialog.open).toHaveBeenCalled();
  });

  it('should format code label', () => {
    const formatted = component.formatCodeLabel('CODE_TEST');
    expect(formatted).toBe('CODE TEST');
    expect(component.formatCodeLabel(undefined)).toBe('');
  });

  it('should build evaluation criteria summary model', () => {
    component.initializeForm();
    const model = component.buildEvaluationCriteriaSummaryModel();
    expect(model).toBeTruthy();
  });

  it('should prefill from evaluation criteria summary', () => {
    component.wuiRiskClassCode = [{ wuiRiskClassCode: 'WUI', weightedRank: 1 }];
    component.data.evaluationCriteriaSummary = {
      wuiRiskClassCode: { wuiRiskClassCode: 'WUI' },
      localWuiRiskClassCode: { wuiRiskClassCode: 'WUI' },
      evaluationCriteriaSectionSummaries: [
        {
          evaluationCriteriaSectionCode: { evaluationCriteriaSectionCode: 'MEDIUM_FLT' },
          evaluationCriteriaSelected: [{ evaluationCriteriaGuid: 'g1', isEvaluationCriteriaSelectedInd: true }]
        }
      ]
    } as any;
    component.initializeForm();
    component.prefillFromEvaluationCriteriaSummary();
    expect(component.selectedMedium.has('g1')).toBeTrue();
  });

  it('should handle error when loading code tables', () => {
    spyOn(console, 'error');
    mockCodeTableServices.fetchCodeTable.and.returnValue(throwError(() => new Error('fail')));
    component.loadCodeTablesAndPrefill();
    expect(console.error).toHaveBeenCalled();
  });
});
