import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router, ActivatedRoute } from '@angular/router';
import { YearEndPerformanceUpdateComponent } from './year-end-performance-update.component';
import { PermissionsService } from 'src/app/services/permissions.service';
import { ResourcesRoutes } from 'src/app/utils';
import { MatSnackBar } from '@angular/material/snack-bar';
import { CodeTableServices } from 'src/app/services/code-table-services';
import { ProjectService } from 'src/app/services/project-services';
import { of, throwError } from 'rxjs';

describe('YearEndPerformanceUpdateComponent', () => {
  let component: YearEndPerformanceUpdateComponent;
  let fixture: ComponentFixture<YearEndPerformanceUpdateComponent>;
  let mockRouter: jasmine.SpyObj<Router>;
  let mockActivatedRoute: any;
  let mockPermissionsService: jasmine.SpyObj<PermissionsService>;
  let mockProjectService: jasmine.SpyObj<ProjectService>;
  let mockCodeTableService: jasmine.SpyObj<CodeTableServices>;
  let mockSnackBar: jasmine.SpyObj<MatSnackBar>;

  beforeEach(async () => {
    mockRouter = jasmine.createSpyObj('Router', ['navigate']);
    mockPermissionsService = jasmine.createSpyObj('PermissionsService', ['hasAction']);
    mockProjectService = jasmine.createSpyObj('ProjectService', [
      'getProjectFiscalByProjectPlanFiscalGuid',
      'getFiscalActivities',
      'getAllFiscalCloseouts',
      'submitFiscalCloseout',
      'updateFiscalActivities'
    ]);
    mockCodeTableService = jasmine.createSpyObj('CodeTableServices', ['fetchCodeTable']);
    mockSnackBar = jasmine.createSpyObj('MatSnackBar', ['open']);
    mockProjectService.getProjectFiscalByProjectPlanFiscalGuid.and.returnValue(of({}));
    mockProjectService.getFiscalActivities.and.returnValue(of({ _embedded: { activities: [] } }));
    mockProjectService.getAllFiscalCloseouts.and.returnValue(of({ _embedded: { fiscalCloseouts: [] } }));
    mockCodeTableService.fetchCodeTable.and.returnValue(of({ _embedded: { planFiscalStatusCode: [] } }));

    mockActivatedRoute = {
      snapshot: {
        queryParamMap: {
          get: (key: string) => {
            if (key === 'projectGuid') return 'test-project-guid';
            if (key === 'fiscalGuid') return 'test-fiscal-guid';
            return null;
          }
        }
      }
    };

    await TestBed.configureTestingModule({
      imports: [YearEndPerformanceUpdateComponent],
      providers: [
        { provide: Router, useValue: mockRouter },
        { provide: ActivatedRoute, useValue: mockActivatedRoute },
        { provide: PermissionsService, useValue: mockPermissionsService },
        { provide: ProjectService, useValue: mockProjectService },
        { provide: CodeTableServices, useValue: mockCodeTableService },
        { provide: MatSnackBar, useValue: mockSnackBar }
      ]
    }).compileComponents();
  });

  describe('authorized user', () => {
    beforeEach(() => {
      mockPermissionsService.hasAction.and.returnValue(true);
      fixture = TestBed.createComponent(YearEndPerformanceUpdateComponent);
      component = fixture.componentInstance;
      fixture.detectChanges();
    });

    it('should create', () => {
      expect(component).toBeTruthy();
    });

    it('should initialize projectGuid and fiscalGuid from query params', () => {
      expect(component.projectGuid).toBe('test-project-guid');
      expect(component.fiscalGuid).toBe('test-fiscal-guid');
    });

    it('should navigate back to project on goBack()', () => {
      component.goBack();
      expect(mockRouter.navigate).toHaveBeenCalledWith(['/' + ResourcesRoutes.EDIT_PROJECT], {
        queryParams: {
          projectGuid: 'test-project-guid',
          fiscalGuid: 'test-fiscal-guid'
        }
      });
    });

    it('should call submitFiscalCloseout with correct payload on onSubmitSummary', () => {
      component.fiscalData = {
        endorsementCode: { endorsementCode: 'NOT_ENDORS' },
        fiscalReportedSpendAmount: 0,
        fiscalActualAmount: 0,
        fiscalCompletedSizeHa: 0
      } as any;

      component.summaryForm.patchValue({
        planFiscalStatusCode: 'COMPLETE',
        fiscalReportedSpendAmount: 100,
        fiscalActualAmount: 200,
        fiscalCompletedSizeHa: 50,
        outcomeComment: 'Test outcome',
        spatialFileUploaded: true,
        acknowledgement: true
      });

      mockProjectService.submitFiscalCloseout.and.returnValue(of({}));

      component.onSubmitSummary();

      expect(mockProjectService.submitFiscalCloseout).toHaveBeenCalledWith(
        'test-project-guid',
        'test-fiscal-guid',
        jasmine.objectContaining({
          closeout: { outcomeComment: 'Test outcome' },
          projectFiscal: jasmine.objectContaining({
            fiscalReportedSpendAmount: 100,
            fiscalActualAmount: 200,
            fiscalCompletedSizeHa: 50,
            planFiscalStatusCode: { planFiscalStatusCode: 'COMPLETE' },
            endorsementCode: { endorsementCode: 'NOT_ENDORS' }
          }),
          activities: []
        })
      );
    });

    it('should show success snackbar after successful submit', () => {
      component.fiscalData = {
        planFiscalStatusCode: { planFiscalStatusCode: 'COMPLETE' },
        endorsementCode: null
      } as any;

      component.summaryForm.patchValue({
        planFiscalStatusCode: 'COMPLETE',
        outcomeComment: 'Test outcome',
        spatialFileUploaded: true,
        acknowledgement: true
      });

      mockProjectService.submitFiscalCloseout.and.returnValue(of({}));

      component.onSubmitSummary();

      expect(mockSnackBar.open).toHaveBeenCalledWith(
        'Year End Update saved successfully',
        'Close',
        { duration: 3000, panelClass: 'snackbar-success' }
      );
    });

    it('should show error snackbar on submit failure', () => {
      component.fiscalData = {
        planFiscalStatusCode: 'COMPLETE',
        endorsementCode: null
      } as any;

      component.summaryForm.patchValue({
        planFiscalStatusCode: 'COMPLETE',
        outcomeComment: 'Test outcome',
        spatialFileUploaded: true,
        acknowledgement: true
      });

      mockProjectService.submitFiscalCloseout.and.returnValue(
        throwError(() => new Error('Submit failed'))
      );

      component.onSubmitSummary();

      expect(mockSnackBar.open).toHaveBeenCalledWith(
        'Failed to save Year End Update',
        'Close',
        { duration: 3000, panelClass: 'snackbar-error' }
      );
    });

    it('should return early if fiscalData is not set', () => {
      component.fiscalData = undefined;

      component.onSubmitSummary();

      expect(mockProjectService.submitFiscalCloseout).not.toHaveBeenCalled();
    });

    it('should set isSavingSummary to false after submit completes', () => {
      component.fiscalData = {
        planFiscalStatusCode: { planFiscalStatusCode: 'COMPLETE' },
        endorsementCode: null
      } as any;

      mockProjectService.submitFiscalCloseout.and.returnValue(of({}));

      component.onSubmitSummary();

      expect(component.isSavingSummary).toBeFalse();
    });
  });

  describe('unauthorized user', () => {
    beforeEach(() => {
      mockPermissionsService.hasAction.and.returnValue(false);
      fixture = TestBed.createComponent(YearEndPerformanceUpdateComponent);
      component = fixture.componentInstance;
      fixture.detectChanges();
    });

    it('should redirect unauthorized user to error page', () => {
      expect(mockRouter.navigate).toHaveBeenCalledWith(['/' + ResourcesRoutes.ERROR_PAGE]);
    });
  });
});
