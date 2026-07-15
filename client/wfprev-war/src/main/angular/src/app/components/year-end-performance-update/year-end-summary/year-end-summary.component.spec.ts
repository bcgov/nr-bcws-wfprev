import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Component, Input } from '@angular/core';
import { YearEndSummaryComponent } from './year-end-summary.component';
import { ProjectService } from 'src/app/services/project-services';
import { YearEndActivityViewModel } from '../../models';
import { of, throwError } from 'rxjs';
import { TokenService } from 'src/app/services/token.service';

@Component({ selector: 'wfprev-year-end-summary-activity-list', template: '', standalone: true })
class MockYearEndSummaryActivityListComponent {
  @Input() activities: YearEndActivityViewModel[] = [];
  @Input() fiscalGuid!: string;
}

const mockCloseout = { outcomeComment: 'Great outcome.', submittedByName: 'John Doe' };
const mockProjectFiscal = {
  fiscalReportedSpendAmount: 10000,
  fiscalActualAmount: 9500,
  fiscalCompletedSizeHa: 42
};
const mockActivities = [
  { activityGuid: 'act-1', activityName: 'Activity 1' },
  { activityGuid: 'act-2', activityName: 'Activity 2' }
];

const mockForkJoinResponse = {
  closeouts: { _embedded: { fiscalCloseouts: [mockCloseout] } },
  projectFiscal: mockProjectFiscal,
  activities: { _embedded: { activities: mockActivities } }
};

describe('YearEndSummaryComponent', () => {
  let component: YearEndSummaryComponent;
  let fixture: ComponentFixture<YearEndSummaryComponent>;
  let projectServiceSpy: jasmine.SpyObj<ProjectService>;
  let tokenServiceSpy: jasmine.SpyObj<TokenService>;

  beforeEach(async () => {
    projectServiceSpy = jasmine.createSpyObj('ProjectService', [
      'getAllFiscalCloseouts',
      'getProjectFiscalByProjectPlanFiscalGuid',
      'getFiscalActivities'
    ]);

    projectServiceSpy.getAllFiscalCloseouts.and.returnValue(of(mockForkJoinResponse.closeouts) as any);
    projectServiceSpy.getProjectFiscalByProjectPlanFiscalGuid.and.returnValue(of(mockForkJoinResponse.projectFiscal) as any);
    projectServiceSpy.getFiscalActivities.and.returnValue(of(mockForkJoinResponse.activities) as any);
    tokenServiceSpy = jasmine.createSpyObj('TokenService', ['getUserFullName']);
    tokenServiceSpy.getUserFullName.and.returnValue('Jane Smith');

    await TestBed.configureTestingModule({
      imports: [YearEndSummaryComponent],
      providers: [
        { provide: ProjectService, useValue: projectServiceSpy },
        { provide: TokenService, useValue: tokenServiceSpy }
      ]
    })
      .overrideComponent(YearEndSummaryComponent, {
        set: { imports: [MockYearEndSummaryActivityListComponent] }
      })
      .compileComponents();

    fixture = TestBed.createComponent(YearEndSummaryComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    component.projectGuid = 'proj-1';
    component.fiscalGuid = 'fiscal-1';
    fixture.detectChanges();
    expect(component).toBeTruthy();
  });

  describe('populateYearEndSummary', () => {
    it('should not call services when projectGuid is missing', () => {
      component.projectGuid = '';
      component.fiscalGuid = 'fiscal-1';
      fixture.detectChanges();
      expect(projectServiceSpy.getAllFiscalCloseouts).not.toHaveBeenCalled();
    });

    it('should not call services when fiscalGuid is missing', () => {
      component.projectGuid = 'proj-1';
      component.fiscalGuid = '';
      fixture.detectChanges();
      expect(projectServiceSpy.getAllFiscalCloseouts).not.toHaveBeenCalled();
    });

    it('should call all three services with correct guids', () => {
      component.projectGuid = 'proj-1';
      component.fiscalGuid = 'fiscal-1';
      fixture.detectChanges();
      expect(projectServiceSpy.getAllFiscalCloseouts).toHaveBeenCalledWith('proj-1', 'fiscal-1');
      expect(projectServiceSpy.getProjectFiscalByProjectPlanFiscalGuid).toHaveBeenCalledWith('proj-1', 'fiscal-1');
      expect(projectServiceSpy.getFiscalActivities).toHaveBeenCalledWith('proj-1', 'fiscal-1');
    });

    it('should populate summary on success', () => {
      component.projectGuid = 'proj-1';
      component.fiscalGuid = 'fiscal-1';
      fixture.detectChanges();
      expect(component.summary).toBeDefined();
      expect(component.summary?.closeout).toEqual(mockCloseout);
      expect(component.summary?.projectFiscal).toEqual(mockProjectFiscal as any);
      expect(component.summary?.activities.length).toBe(2);
    });

    it('should map activities to YearEndActivityViewModel', () => {
      component.projectGuid = 'proj-1';
      component.fiscalGuid = 'fiscal-1';
      fixture.detectChanges();
      const activities = component.summary?.activities ?? [];
      expect(activities[0].data).toEqual(mockActivities[0]);
      expect(activities[0].isExpanded).toBeFalse();
      expect(activities[1].data).toEqual(mockActivities[1]);
      expect(activities[1].isExpanded).toBeFalse();
    });

    it('should set closeout to undefined when fiscalCloseouts is empty', () => {
      projectServiceSpy.getAllFiscalCloseouts.and.returnValue(
        of({ _embedded: { fiscalCloseouts: [] } }) as any
      );
      component.projectGuid = 'proj-1';
      component.fiscalGuid = 'fiscal-1';
      fixture.detectChanges();
      expect(component.summary?.closeout).toBeUndefined();
    });

    it('should set activities to empty array when _embedded is missing', () => {
      projectServiceSpy.getFiscalActivities.and.returnValue(of({}) as any);
      component.projectGuid = 'proj-1';
      component.fiscalGuid = 'fiscal-1';
      fixture.detectChanges();
      expect(component.summary?.activities).toEqual([]);
    });

    it('should log error and not set summary on failure', () => {
      spyOn(console, 'error');
      projectServiceSpy.getAllFiscalCloseouts.and.returnValue(throwError(() => new Error('Network error')));
      component.projectGuid = 'proj-1';
      component.fiscalGuid = 'fiscal-1';
      fixture.detectChanges();
      expect(component.summary).toBeUndefined();
      expect(console.error).toHaveBeenCalled();
    });
  });

  describe('template', () => {
    it('should not render summary content when summary is undefined', () => {
      component.projectGuid = '';
      component.fiscalGuid = '';
      fixture.detectChanges();
      const el = fixture.nativeElement.querySelector('.summary-totals');
      expect(el).toBeFalsy();
    });

    it('should render summary totals', () => {
      component.projectGuid = 'proj-1';
      component.fiscalGuid = 'fiscal-1';
      fixture.detectChanges();
      const el = fixture.nativeElement.querySelector('.summary-totals');
      expect(el.textContent).toContain('10000');
      expect(el.textContent).toContain('9500');
      expect(el.textContent).toContain('42');
    });

    it('should render outcome comment', () => {
      component.projectGuid = 'proj-1';
      component.fiscalGuid = 'fiscal-1';
      fixture.detectChanges();
      const el = fixture.nativeElement.querySelector('.outcomes-content');
      expect(el.textContent).toContain('Great outcome.');
    });

    it('should render activity list with correct fiscalGuid', () => {
      component.projectGuid = 'proj-1';
      component.fiscalGuid = 'fiscal-1';
      fixture.detectChanges();
      const el = fixture.debugElement.query(
        sel => sel.name === 'wfprev-year-end-summary-activity-list'
      );
      expect(el).toBeTruthy();
      expect(el.componentInstance.fiscalGuid).toBe('fiscal-1');
      expect(el.componentInstance.activities.length).toBe(2);
    });
  });

  describe('submittedByName', () => {
    it('should return closeout submittedByName when present', () => {
      component.projectGuid = 'proj-1';
      component.fiscalGuid = 'fiscal-1';
      fixture.detectChanges();
      expect(component.submittedByName).toBe('John Doe');
    });

    it('should fall back to tokenService when closeout is undefined', () => {
      projectServiceSpy.getAllFiscalCloseouts.and.returnValue(
        of({ _embedded: { fiscalCloseouts: [] } }) as any
      );
      component.projectGuid = 'proj-1';
      component.fiscalGuid = 'fiscal-1';
      fixture.detectChanges();
      expect(component.submittedByName).toBe('Jane Smith');
    });

    it('should return empty string when neither closeout nor tokenService have a name', () => {
      projectServiceSpy.getAllFiscalCloseouts.and.returnValue(
        of({ _embedded: { fiscalCloseouts: [] } }) as any
      );
      tokenServiceSpy.getUserFullName.and.returnValue('');
      component.projectGuid = 'proj-1';
      component.fiscalGuid = 'fiscal-1';
      fixture.detectChanges();
      expect(component.submittedByName).toBe('');
    });
  });
});