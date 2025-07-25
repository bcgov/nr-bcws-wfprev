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
    component.data.evaluationCriteriaSummary = undefined;
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

  it('should build evaluation criteria summary model for create mode (isCreate=true)', () => {
    component.fineFilters = [
      { evaluationCriteriaGuid: 'fine-guid-1', weightedRank: 1 } as any
    ];
    component.mediumFilters = [
      { evaluationCriteriaGuid: 'medium-guid-1', weightedRank: 2 } as any
    ];

    component.selectedFine.add('fine-guid-1');
    component.selectedMedium.add('medium-guid-1');

    component.initializeForm();
    const model = component.buildEvaluationCriteriaSummaryModel(undefined, true);

    const fine = model.evaluationCriteriaSectionSummaries!.find(
      s => s!.evaluationCriteriaSectionCode!.evaluationCriteriaSectionCode === 'FINE_FLT'
    )!;
    const medium = model.evaluationCriteriaSectionSummaries!.find(
      s => s!.evaluationCriteriaSectionCode!.evaluationCriteriaSectionCode === 'MEDIUM_FLT'
    )!;

    expect(fine).toBeDefined();
    expect(fine!.evaluationCriteriaSelected).toBeDefined();
    expect(fine!.evaluationCriteriaSelected![0].evaluationCriteriaSelectedGuid).toBeUndefined();
    expect(fine!.evaluationCriteriaSelected![0].evaluationCriteriaGuid).toBe('fine-guid-1');
    expect(fine!.evaluationCriteriaSelected![0].isEvaluationCriteriaSelectedInd).toBeTrue();

    expect(medium).toBeDefined();
    expect(medium!.evaluationCriteriaSelected).toBeDefined();
    expect(medium!.evaluationCriteriaSelected![0].evaluationCriteriaSelectedGuid).toBeUndefined();
    expect(medium!.evaluationCriteriaSelected![0].evaluationCriteriaGuid).toBe('medium-guid-1');
    expect(medium!.evaluationCriteriaSelected![0].isEvaluationCriteriaSelectedInd).toBeTrue();

  });

  it('should build evaluation criteria summary model for update mode with existing selections', () => {
    component.fineFilters = [
      { evaluationCriteriaGuid: 'fine-guid-2', weightedRank: 1 } as any
    ];
    component.mediumFilters = [
      { evaluationCriteriaGuid: 'medium-guid-2', weightedRank: 2 } as any
    ];

    const existingSummary = {
      evaluationCriteriaSummaryGuid: 'summary-guid',
      evaluationCriteriaSectionSummaries: [
        {
          evaluationCriteriaSectionCode: { evaluationCriteriaSectionCode: 'FINE_FLT' },
          evaluationCriteriaSectionSummaryGuid: 'fine-section-guid',
          evaluationCriteriaSelected: [
            { evaluationCriteriaGuid: 'fine-guid-2', evaluationCriteriaSelectedGuid: 'fine-selection-guid' }
          ]
        },
        {
          evaluationCriteriaSectionCode: { evaluationCriteriaSectionCode: 'MEDIUM_FLT' },
          evaluationCriteriaSectionSummaryGuid: 'medium-section-guid',
          evaluationCriteriaSelected: [
            { evaluationCriteriaGuid: 'medium-guid-2', evaluationCriteriaSelectedGuid: 'medium-selection-guid' }
          ]
        }
      ]
    } as any;

    component.selectedFine.add('fine-guid-2');
    component.selectedMedium.add('medium-guid-2');

    component.initializeForm();
    const model = component.buildEvaluationCriteriaSummaryModel(existingSummary, false);

    const fine = model.evaluationCriteriaSectionSummaries!.find(
      s => s!.evaluationCriteriaSectionCode!.evaluationCriteriaSectionCode === 'FINE_FLT'
    )!;
    const medium = model.evaluationCriteriaSectionSummaries!.find(
      s => s!.evaluationCriteriaSectionCode!.evaluationCriteriaSectionCode === 'MEDIUM_FLT'
    )!;

    expect(fine).toBeDefined();
    expect(fine!.evaluationCriteriaSelected).toBeDefined();
    expect(fine!.evaluationCriteriaSelected![0].evaluationCriteriaSelectedGuid).toBe('fine-selection-guid');
    expect(fine!.evaluationCriteriaSelected![0].evaluationCriteriaSectionSummaryGuid).toBe('fine-section-guid');
    expect(fine!.evaluationCriteriaSelected![0].isEvaluationCriteriaSelectedInd).toBeTrue();

    expect(medium).toBeDefined();
    expect(medium!.evaluationCriteriaSelected).toBeDefined();
    expect(medium!.evaluationCriteriaSelected![0].evaluationCriteriaSelectedGuid).toBe('medium-selection-guid');
    expect(medium!.evaluationCriteriaSelected![0].evaluationCriteriaSectionSummaryGuid).toBe('medium-section-guid');
    expect(medium!.evaluationCriteriaSelected![0].isEvaluationCriteriaSelectedInd).toBeTrue();

  });

  it('should build evaluation criteria summary model for update mode with no existing selections', () => {
    component.fineFilters = [
      { evaluationCriteriaGuid: 'fine-guid-3', weightedRank: 1 } as any
    ];
    component.mediumFilters = [
      { evaluationCriteriaGuid: 'medium-guid-3', weightedRank: 2 } as any
    ];

    const existingSummary = {
      evaluationCriteriaSummaryGuid: 'summary-guid',
      evaluationCriteriaSectionSummaries: []
    } as any;

    component.selectedFine.add('fine-guid-3');
    component.selectedMedium.add('medium-guid-3');

    component.initializeForm();
    const model = component.buildEvaluationCriteriaSummaryModel(existingSummary, false);

    const fine = model.evaluationCriteriaSectionSummaries!.find(
      s => s!.evaluationCriteriaSectionCode!.evaluationCriteriaSectionCode === 'FINE_FLT'
    )!;
    const medium = model.evaluationCriteriaSectionSummaries!.find(
      s => s!.evaluationCriteriaSectionCode!.evaluationCriteriaSectionCode === 'MEDIUM_FLT'
    )!;

    expect(fine).toBeDefined();
    expect(fine!.evaluationCriteriaSelected).toBeDefined();
    expect(fine!.evaluationCriteriaSelected![0].evaluationCriteriaSelectedGuid).toBeUndefined();
    expect(fine!.evaluationCriteriaSelected![0].isEvaluationCriteriaSelectedInd).toBeTrue();

    expect(medium).toBeDefined();
    expect(medium.evaluationCriteriaSelected).toBeDefined();
    expect(medium.evaluationCriteriaSelected![0].evaluationCriteriaSelectedGuid).toBeUndefined();
    expect(medium.evaluationCriteriaSelected![0].isEvaluationCriteriaSelectedInd).toBeTrue();
  });

  it('should prefill fine filters from evaluation criteria summary', () => {
    component.wuiRiskClassCode = [{ wuiRiskClassCode: 'WUI', weightedRank: 1 }];

    component.data.evaluationCriteriaSummary = {
      wuiRiskClassCode: { wuiRiskClassCode: 'WUI' },
      localWuiRiskClassCode: { wuiRiskClassCode: 'WUI' },
      evaluationCriteriaSectionSummaries: [
        {
          evaluationCriteriaSectionCode: { evaluationCriteriaSectionCode: 'FINE_FLT' },
          filterSectionComment: 'Fine filter comment',
          filterSectionScore: 5,
          evaluationCriteriaSelected: [
            { evaluationCriteriaGuid: 'fine-guid', isEvaluationCriteriaSelectedInd: true }
          ]
        }
      ]
    } as any;

    component.initializeForm();
    component.prefillFromEvaluationCriteriaSummary();

    expect(component.selectedFine.has('fine-guid')).toBeTrue();
    expect(component.fineTotal).toBe(5);
    expect(component.criteriaForm.get('fineFilterComments')?.value).toBe('Fine filter comment');
  });

  it('should assign medium, fine, and riskClassLocation filters when projectType is CULT_RX_FR', async () => {
    component.data = {
      project: {
        projectTypeCode: { projectTypeCode: 'CULT_RX_FR' }
      }
    } as any;

    component.evaluationCriteriaCode = [
      {
        evaluationCriteriaGuid: 'guid-1',
        evalCriteriaSectCode: 'BDF',
        weightedRank: 2
      },
      {
        evaluationCriteriaGuid: 'guid-2',
        evalCriteriaSectCode: 'CI',
        weightedRank: 0.5
      },
      {
        evaluationCriteriaGuid: 'guid-3',
        evalCriteriaSectCode: 'RCL',
        weightedRank: 3
      }
    ] as any;

    component.assignCodeTableData('evaluationCriteriaCode', {
      _embedded: { evaluationCriteriaCode: component.evaluationCriteriaCode }
    });

    fixture.detectChanges();
    await fixture.whenStable();

    expect(component.mediumFilters.length).toBe(0);
    expect(component.fineFilters.length).toBe(0);
    expect(component.riskClassLocationFilters.length).toBe(0);
  });

  it('should toggleCoarse and updateCoarseTotalFromCheckboxes', () => {
    component.riskClassLocationFilters = [{ evaluationCriteriaGuid: 'coarse1', weightedRank: 2 } as any];
    const event = { target: { checked: true } } as any;
    component.toggleCoarse('coarse1', event);
    expect(component.selectedCoarse.has('coarse1')).toBeTrue();
    expect(component.coarseTotal).toBe(2);

    const eventOff = { target: { checked: false } } as any;
    component.toggleCoarse('coarse1', eventOff);
    expect(component.selectedCoarse.has('coarse1')).toBeFalse();
  });

  it('should calculateCoarseTotal correctly', () => {
    component.riskClassLocationFilters = [
      { evaluationCriteriaGuid: 'c1', weightedRank: 1 },
      { evaluationCriteriaGuid: 'c2', weightedRank: 2 }
    ] as any;
    component.selectedCoarse = new Set(['c1', 'c2']);
    component.calculateCoarseTotal();
    expect(component.coarseTotal).toBe(3);
  });

  it('should build RCL section when isOutsideOfWuiOn is true', () => {
    component.isOutsideOfWuiOn = true;
    component.initializeForm();
    component.criteriaForm.patchValue({ localWuiRiskClassRationale: 'Some rationale' });
    component.riskClassLocationFilters = [
      { evaluationCriteriaGuid: 'rcl1', weightedRank: 2 } as any
    ];
    component.selectedCoarse.add('rcl1');
    const existingSummary = {
      evaluationCriteriaSummaryGuid: 'summary-guid',
      evaluationCriteriaSectionSummaries: [
        {
          evaluationCriteriaSectionCode: { evaluationCriteriaSectionCode: 'RCL' },
          evaluationCriteriaSectionSummaryGuid: 'rcl-section-guid',
          evaluationCriteriaSelected: [
            { evaluationCriteriaGuid: 'rcl1', evaluationCriteriaSelectedGuid: 'existing-guid' }
          ]
        }
      ]
    } as any;

    const result = component.buildEvaluationCriteriaSummaryModel(existingSummary);
    const rcl = result.evaluationCriteriaSectionSummaries?.find(
      s => s.evaluationCriteriaSectionCode?.evaluationCriteriaSectionCode === 'RCL'
    );
    expect(rcl).toBeDefined();
    expect(rcl?.evaluationCriteriaSelected?.[0].isEvaluationCriteriaSelectedInd).toBeTrue();
  });

  it('should disable WUI controls if isOutsideWuiInd is true in summary', () => {
    component.wuiRiskClassCode = [{ wuiRiskClassCode: 'WUI', weightedRank: 1 }];
    component.initializeForm();
    component.data.evaluationCriteriaSummary = {
      isOutsideWuiInd: true
    } as any;
    component.prefillFromEvaluationCriteriaSummary();
    expect(component.criteriaForm.get('wuiRiskClassCode')?.disabled).toBeTrue();
    expect(component.criteriaForm.get('localWuiRiskClassCode')?.disabled).toBeTrue();
  });

  it('should return correct sectionTitles for FUEL_MGMT', () => {
    component.data.project.projectTypeCode = { projectTypeCode: 'FUEL_MGMT' };
    const titles = component.sectionTitles;
    expect(titles.section1).toBe('Coarse Filters');
    expect(titles.totalLabel('section1')).toContain('Coarse Filters');
    expect(titles.commentLabel('section1')).toContain('Local WUI Risk Class Rationale');
  });

  it('should toggleOutsideOfWui on and off', () => {
    component.initializeForm();
    component.riskClassLocationFilters = [{ evaluationCriteriaGuid: 'rcl-guid', weightedRank: 1 }];
    component.selectedCoarse.add('rcl-guid');

    component.toggleOutsideOfWui(true);
    expect(component.isOutsideOfWuiOn).toBeTrue();
    expect(component.criteriaForm.get('wuiRiskClassCode')?.disabled).toBeTrue();
    expect(component.selectedCoarse.size).toBe(0);

    component.toggleOutsideOfWui(false);
    expect(component.isOutsideOfWuiOn).toBeFalse();
    expect(component.criteriaForm.get('wuiRiskClassCode')?.enabled).toBeTrue();
  });

  it('should updateCoarseTotalFromDropdowns correctly', () => {
    component.initializeForm();
    component.criteriaForm.patchValue({
      wuiRiskClassCode: 2,
      localWuiRiskClassCode: 3
    });

    component.updateCoarseTotalFromDropdowns();
    expect(component.coarseTotal).toBe(5);
  });


});
