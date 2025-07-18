import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MatDialog } from '@angular/material/dialog';
import { of, throwError } from 'rxjs';
import { EvaluationCriteriaComponent } from './evaluation-criteria.component';
import { ProjectService } from 'src/app/services/project-services';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { EvaluationCriteriaSummaryModel } from 'src/app/components/models';
import { EvaluationCriteriaSectionCodes, ProjectTypes } from 'src/app/utils/constants';

describe('EvaluationCriteriaComponent', () => {
  let component: EvaluationCriteriaComponent;
  let fixture: ComponentFixture<EvaluationCriteriaComponent>;
  let mockDialog: jasmine.SpyObj<MatDialog>;
  let mockProjectService: jasmine.SpyObj<ProjectService>;

  beforeEach(async () => {
    mockDialog = jasmine.createSpyObj('MatDialog', ['open']);
    mockProjectService = jasmine.createSpyObj('ProjectService', ['getEvaluationCriteriaSummaries']);

    await TestBed.configureTestingModule({
      imports: [EvaluationCriteriaComponent, BrowserAnimationsModule],
      providers: [
        { provide: MatDialog, useValue: mockDialog },
        { provide: ProjectService, useValue: mockProjectService }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(EvaluationCriteriaComponent);
    component = fixture.componentInstance;
    component.project = { projectGuid: 'test-guid' } as any;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load evaluation criteria summary on ngOnChanges', () => {
    const mockSummary: EvaluationCriteriaSummaryModel = {
      evaluationCriteriaSummaryGuid: '1'
    } as any;
    mockProjectService.getEvaluationCriteriaSummaries.and.returnValue(
      of({
        _embedded: {
          eval_criteria_summary: [mockSummary]
        }
      })
    );

    component.ngOnChanges({
      project: {
        currentValue: component.project,
        previousValue: null,
        firstChange: true,
        isFirstChange: () => true
      }
    });

    expect(mockProjectService.getEvaluationCriteriaSummaries).toHaveBeenCalledWith('test-guid');
    expect(component.evaluationCriteriaSummary).toEqual(mockSummary);
  });

  it('should handle error when loading evaluation criteria summary', () => {
    mockProjectService.getEvaluationCriteriaSummaries.and.returnValue(throwError(() => new Error()));
    component.loadEvaluationCriteriaSummaries();
    expect(mockProjectService.getEvaluationCriteriaSummaries).toHaveBeenCalled();
  });

  it('should open dialog with summary when available', () => {
    const mockAfterClosed = of(true);
    const dialogRef = { afterClosed: () => mockAfterClosed };
    mockDialog.open.and.returnValue(dialogRef as any);

    component.evaluationCriteriaSummary = { evaluationCriteriaSummaryGuid: '1' } as any;
    spyOn(component, 'loadEvaluationCriteriaSummaries');

    component.openEvaluationCriteriaPopUp();

    expect(mockDialog.open).toHaveBeenCalled();
    expect(component.loadEvaluationCriteriaSummaries).toHaveBeenCalled();
  });

  it('should open dialog with null summary when none available', () => {
    const mockAfterClosed = of(false);
    const dialogRef = { afterClosed: () => mockAfterClosed };
    mockDialog.open.and.returnValue(dialogRef as any);

    component.evaluationCriteriaSummary = null;

    component.openEvaluationCriteriaPopUp();

    expect(mockDialog.open).toHaveBeenCalled();
  });

  it('should get section total', () => {
    const summary = {
      evaluationCriteriaSectionSummaries: [
        { evaluationCriteriaSectionCode: { evaluationCriteriaSectionCode: 'A' }, filterSectionScore: 5 }
      ]
    };
    const result = component.getSectionTotal(summary, 'A');
    expect(result).toBe(5);
  });

  it('should return 0 if section not found in getSectionTotal', () => {
    const summary = { evaluationCriteriaSectionSummaries: [] };
    const result = component.getSectionTotal(summary, 'X');
    expect(result).toBe(0);
  });

  it('should get section comment', () => {
    const summary = {
      evaluationCriteriaSectionSummaries: [
        { evaluationCriteriaSectionCode: { evaluationCriteriaSectionCode: 'A' }, filterSectionComment: 'Comment' }
      ]
    };
    const result = component.getSectionComment(summary, 'A');
    expect(result).toBe('Comment');
  });

  it('should return null for empty comment', () => {
    const summary = {
      evaluationCriteriaSectionSummaries: [
        { evaluationCriteriaSectionCode: { evaluationCriteriaSectionCode: 'A' }, filterSectionComment: '  ' }
      ]
    };
    const result = component.getSectionComment(summary, 'A');
    expect(result).toBeNull();
  });

  it('should get calculated total with all values', () => {
    const summary = {
      localWuiRiskClassCode: { weightedRank: 2 },
      wuiRiskClassCode: { weightedRank: 3 },
      evaluationCriteriaSectionSummaries: [
        { evaluationCriteriaSectionCode: { evaluationCriteriaSectionCode: 'MEDIUM_FLT' }, filterSectionScore: 4 },
        { evaluationCriteriaSectionCode: { evaluationCriteriaSectionCode: 'FINE_FLT' }, filterSectionScore: 5 }
      ]
    };
    const result = component.getCalculatedTotal(summary);
    expect(result).toBe(2 + 4 + 5);
  });

  it('should return 0 calculated total if summary is null', () => {
    const result = component.getCalculatedTotal(null);
    expect(result).toBe(0);
  });

  it('should format code label', () => {
    expect(component.formatCodeLabel('A_B')).toBe('A B');
    expect(component.formatCodeLabel(undefined)).toBe('');
  });

  it('should get coarse total from RCL section if prescribed fire and isOutsideWuiInd is true', () => {
    component.project = {
      projectGuid: 'test-guid',
      projectTypeCode: { projectTypeCode: ProjectTypes.CULTURAL_PRESCRIBED_FIRE }
    } as any;

    const summary = {
      isOutsideWuiInd: true,
      evaluationCriteriaSectionSummaries: [
        { 
          evaluationCriteriaSectionCode: { evaluationCriteriaSectionCode: EvaluationCriteriaSectionCodes.RISK_CLASS_LOCATION }, 
          filterSectionScore: 8 
        }
      ]
    };

    const result = component.getCoarseTotal(summary);
    expect(result).toBe(8);
  });

  it('should get coarse total from dropdown if prescribed fire and isOutsideWuiInd is false', () => {
    component.project = {
      projectGuid: 'test-guid',
      projectTypeCode: { projectTypeCode: ProjectTypes.CULTURAL_PRESCRIBED_FIRE }
    } as any;

    const summary = {
      isOutsideWuiInd: false,
      localWuiRiskClassCode: { weightedRank: 2 },
      wuiRiskClassCode: { weightedRank: 3 },
      evaluationCriteriaSectionSummaries: [
        { 
          evaluationCriteriaSectionCode: { evaluationCriteriaSectionCode: EvaluationCriteriaSectionCodes.RISK_CLASS_LOCATION }, 
          filterSectionScore: 10 
        }
      ]
    };

    const result = component.getCoarseTotal(summary);
    expect(result).toBe(2);
  });

  it('should get coarse total from dropdown if not prescribed fire', () => {
    component.project = {
      projectGuid: 'test-guid',
      projectTypeCode: { projectTypeCode: ProjectTypes.FUEL_MANAGEMENT }
    } as any;

    const summary = {
      localWuiRiskClassCode: { weightedRank: 1.5 },
      wuiRiskClassCode: { weightedRank: 2 }
    };

    const result = component.getCoarseTotal(summary);
    expect(result).toBe(1.5);
  });

  it('should return true for isFuelManagement when projectType is FUEL_MANAGEMENT', () => {
    component.project = {
      projectTypeCode: { projectTypeCode: ProjectTypes.FUEL_MANAGEMENT }
    } as any;

    expect(component.isFuelManagement).toBeTrue();
  });

  it('should return correct evaluationLabels for prescribed fire project', () => {
    component.project = {
      projectTypeCode: { projectTypeCode: ProjectTypes.CULTURAL_PRESCRIBED_FIRE }
    } as any;

    const labels = component.evaluationLabels;

    expect(labels.coarse).toBe('Risk Class and Location');
    expect(labels.medium).toBe('Burn Development and Feasibility');
    expect(labels.fine).toBe('Collective Impact');
    expect(labels.comments.medium).toBe('Comments');
    expect(labels.comments.fine).toBe('Comments');
    expect(labels.comments.rationale).toBe('Comments');
  });

  it('should return correct evaluationLabels for fuel management project', () => {
    component.project = {
      projectTypeCode: { projectTypeCode: ProjectTypes.FUEL_MANAGEMENT }
    } as any;

    const labels = component.evaluationLabels;

    expect(labels.coarse).toBe('Coarse Filter Total');
    expect(labels.medium).toBe('Medium Filter Total');
    expect(labels.fine).toBe('Fine Filter Total');
    expect(labels.comments.medium).toBe('Medium Filter Comments');
    expect(labels.comments.fine).toBe('Fine Filter Comments');
    expect(labels.comments.rationale).toBe('Local WUI Risk Class Rationale');
  });

});
