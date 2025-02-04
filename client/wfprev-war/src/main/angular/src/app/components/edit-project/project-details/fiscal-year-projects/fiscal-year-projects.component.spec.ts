import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FiscalYearProjectsComponent } from './fiscal-year-projects.component';
import { ProjectService } from 'src/app/services/project-services';
import { ActivatedRoute } from '@angular/router';
import { of } from 'rxjs';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatTableModule } from '@angular/material/table';
import { CommonModule } from '@angular/common';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';

// Mock ProjectService
class MockProjectService {
  getProjectFiscalsByProjectGuid() {
    return of({
      _embedded: {
        projectFiscals: [
          { fiscalYear: 2023, planFiscalStatusCode: 'DRAFT' },
          { fiscalYear: 2021, planFiscalStatusCode: 'COMPLETE' },
          { fiscalYear: 2022, planFiscalStatusCode: 'IN_PROG' }
        ]
      }
    });
  }
}

describe('FiscalYearProjectsComponent', () => {
  let component: FiscalYearProjectsComponent;
  let fixture: ComponentFixture<FiscalYearProjectsComponent>;

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
    fixture.detectChanges();
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize projectFiscals and sort them by fiscalYear', () => {
    component.loadProjectFiscals();
    expect(component.projectFiscals.length).toBe(3);
    expect(component.projectFiscals[0].fiscalYear).toBe(2021);
    expect(component.projectFiscals[1].fiscalYear).toBe(2022);
    expect(component.projectFiscals[2].fiscalYear).toBe(2023);
  });

  it('should return correct status icon', () => {
    expect(component.getStatusIcon('DRAFT')).toBe('draft-icon.svg');
    expect(component.getStatusIcon('PROPOSED')).toBe('proposed-icon.svg');
    expect(component.getStatusIcon('IN_PROG')).toBe('in-progress-icon.svg');
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
});
