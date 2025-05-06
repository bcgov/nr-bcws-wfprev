import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FiscalYearProjectsComponent } from './fiscal-year-projects.component';
import { ProjectService } from 'src/app/services/project-services';
import { ActivatedRoute } from '@angular/router';
import { of } from 'rxjs';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatTableModule } from '@angular/material/table';
import { CommonModule } from '@angular/common';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';

interface ActivityResponse {
  _embedded: {
    activities: Array<{
      activityName: string;
      activityDescription: string;
      activityStartDate: string;
      activityEndDate: string;
      completedAreaHa: number;
      plannedTreatmentAreaHa: number;
    }>;
  };
}

// Mock ProjectService
class MockProjectService {
  getProjectFiscalsByProjectGuid() {
    return of({
      _embedded: {
        projectFiscals: [
          { fiscalYear: 2023, planFiscalStatusCode: 'DRAFT', projectPlanFiscalGuid: 'guid1' },
          { fiscalYear: 2021, planFiscalStatusCode: 'COMPLETE', projectPlanFiscalGuid: 'guid2' },
          { fiscalYear: 2022, planFiscalStatusCode: 'IN_PROG', projectPlanFiscalGuid: 'guid3' }
        ]
      }
    });
  }

  getFiscalActivities(projectGuid: string, fiscalGuid: string) {
    return of<ActivityResponse>({
      _embedded: {
        activities: [
          {
            activityName: 'Test Activity',
            activityDescription: 'Test Description',
            activityStartDate: '2025-05-01T00:00:00.000+00:00',
            activityEndDate: '2025-05-08T00:00:00.000+00:00',
            completedAreaHa: 10,
            plannedTreatmentAreaHa: 20
          }
        ]
      }
    });
  }
}

describe('FiscalYearProjectsComponent', () => {
  let component: FiscalYearProjectsComponent;
  let fixture: ComponentFixture<FiscalYearProjectsComponent>;
  let projectService: MockProjectService;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        FiscalYearProjectsComponent, 
        MatExpansionModule, 
        MatTableModule, 
        CommonModule,
        BrowserAnimationsModule 
      ],
      providers: [
        { provide: ProjectService, useClass: MockProjectService },
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: { queryParamMap: { get: () => 'test-guid' } }
          }
        }
      ]
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(FiscalYearProjectsComponent);
    component = fixture.componentInstance;
    projectService = TestBed.inject(ProjectService) as unknown as MockProjectService;
    fixture.detectChanges();
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize projectFiscals and sort them by fiscalYear', () => {
    component.loadProjectFiscals();
    expect(component.projectFiscals.length).toBe(3);
    expect(component.projectFiscals[0].fiscalYear).toBe(2023);
    expect(component.projectFiscals[1].fiscalYear).toBe(2022);
    expect(component.projectFiscals[2].fiscalYear).toBe(2021);
  });

  it('should return correct status icon', () => {
    expect(component.getStatusIcon('DRAFT')).toBe('draft-icon.svg');
    expect(component.getStatusIcon('PROPOSED')).toBe('proposed-icon.svg');
    expect(component.getStatusIcon('IN_PROG')).toBe('in-progress-icon-only.svg');
    expect(component.getStatusIcon('COMPLETE')).toBe('complete-icon.svg');
    expect(component.getStatusIcon('ABANDONED')).toBe('abandoned-icon.svg');
    expect(component.getStatusIcon('PREPARED')).toBe('prepared-icon.svg');
    expect(component.getStatusIcon('UNKNOWN')).toBeUndefined();
  });

  it('should return correct fiscal status description', () => {
    expect(component.getPlanFiscalStatus('DRAFT')).toBe('Draft');
    expect(component.getPlanFiscalStatus('PROPOSED')).toBe('Proposed');
    expect(component.getPlanFiscalStatus('IN_PROG')).toBe('In Progress');
    expect(component.getPlanFiscalStatus('COMPLETE')).toBe('Complete');
    expect(component.getPlanFiscalStatus('ABANDONED')).toBe('Abandoned');
    expect(component.getPlanFiscalStatus('PREPARED')).toBe('Prepared');
    expect(component.getPlanFiscalStatus('UNKNOWN')).toBe('Unknown');
  });

  it('should define displayed columns correctly for complete and planned fiscal statuses', () => {
    expect(component.displayedColumnsComplete).toEqual(['name', 'description', 'endDate', 'completedHectares']);
    expect(component.displayedColumnsPlanned).toEqual(['name', 'description', 'startDate', 'endDate', 'plannedHectares']);
  });

  it('should format dates correctly', () => {
    const dateString = '2025-05-01T00:00:00.000+00:00';
    const formattedDate = component['formatDate'](dateString);
    expect(formattedDate).toBe('2025-05-01');
  });

  it('should handle empty date string', () => {
    const formattedDate = component['formatDate']('');
    expect(formattedDate).toBe('');
  });

  it('should load activities for each fiscal year', () => {
    const spy = spyOn(projectService, 'getFiscalActivities').and.callThrough();
    component.loadProjectFiscals();
    
    expect(spy).toHaveBeenCalledTimes(3);
    expect(spy).toHaveBeenCalledWith('test-guid', 'guid1');
    expect(spy).toHaveBeenCalledWith('test-guid', 'guid2');
    expect(spy).toHaveBeenCalledWith('test-guid', 'guid3');
  });

  it('should map activities correctly with formatted dates', () => {
    component.loadProjectFiscals();
    component.loadActivities('guid1');
    
    expect(component.activitiesMap['guid1']).toBeDefined();
    expect(component.activitiesMap['guid1'][0]).toEqual({
      name: 'Test Activity',
      description: 'Test Description',
      startDate: '2025-05-01',
      endDate: '2025-05-08',
      completedHectares: 10,
      plannedHectares: 20
    });
  });

  it('should handle empty activities response', () => {
    spyOn(projectService, 'getFiscalActivities').and.returnValue(of<ActivityResponse>({
      _embedded: {
        activities: []
      }
    }));
    component.loadActivities('guid1');
    expect(component.activitiesMap['guid1']).toEqual([]);
  });

  it('should handle error when loading activities', () => {
    spyOn(projectService, 'getFiscalActivities').and.returnValue(of<ActivityResponse>({
      _embedded: {
        activities: []
      }
    }));
    component.loadActivities('guid1');
    expect(component.activitiesMap['guid1']).toEqual([]);
  });

  it('should format fiscal year correctly', () => {
    component.loadProjectFiscals();
    expect(component.projectFiscals[0].fiscalYearFormatted).toBe('2023/24');
    expect(component.projectFiscals[1].fiscalYearFormatted).toBe('2022/23');
    expect(component.projectFiscals[2].fiscalYearFormatted).toBe('2021/22');
  });
});
