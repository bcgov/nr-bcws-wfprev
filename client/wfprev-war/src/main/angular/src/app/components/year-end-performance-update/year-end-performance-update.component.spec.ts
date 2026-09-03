import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router, ActivatedRoute } from '@angular/router';
import { YearEndPerformanceUpdateComponent } from './year-end-performance-update.component';
import { PermissionsService } from 'src/app/services/permissions.service';
import { ResourcesRoutes } from 'src/app/utils';
import { MatSnackBar } from '@angular/material/snack-bar';
import { CodeTableServices } from 'src/app/services/code-table-services';
import { ProjectService } from 'src/app/services/project-services';
import { of, throwError } from 'rxjs';
import { MatDialog } from '@angular/material/dialog';

describe('YearEndPerformanceUpdateComponent', () => {
  let component: YearEndPerformanceUpdateComponent;
  let fixture: ComponentFixture<YearEndPerformanceUpdateComponent>;
  let mockRouter: jasmine.SpyObj<Router>;
  let mockActivatedRoute: any;
  let mockPermissionsService: jasmine.SpyObj<PermissionsService>;
  let mockProjectService: jasmine.SpyObj<ProjectService>;
  let mockCodeTableService: jasmine.SpyObj<CodeTableServices>;
  let mockSnackBar: jasmine.SpyObj<MatSnackBar>;
  let mockDialog: jasmine.SpyObj<MatDialog>;

  beforeEach(async () => {
    mockRouter = jasmine.createSpyObj('Router', ['navigate']);
    mockPermissionsService = jasmine.createSpyObj('PermissionsService', ['hasAction']);
    mockProjectService = jasmine.createSpyObj('ProjectService', [
      'getProjectFiscalByProjectPlanFiscalGuid',
      'getFiscalActivities',
      'getAllFiscalCloseouts',
      'submitFiscalCloseout',
      'updateFiscalActivities',
      'getProjectByProjectGuid'
    ]);
    mockCodeTableService = jasmine.createSpyObj('CodeTableServices', ['fetchCodeTable']);
    mockSnackBar = jasmine.createSpyObj('MatSnackBar', ['open']);
    mockProjectService.getProjectFiscalByProjectPlanFiscalGuid.and.returnValue(of({}));
    mockProjectService.getFiscalActivities.and.returnValue(of({ _embedded: { activities: [] } }));
    mockProjectService.getAllFiscalCloseouts.and.returnValue(of({ _embedded: { fiscalCloseouts: [] } }));
    mockCodeTableService.fetchCodeTable.and.returnValue(of({ _embedded: { planFiscalStatusCode: [] } }));
    mockProjectService.getProjectByProjectGuid.and.returnValue(of({ projectName: 'Test Project' }));
    mockDialog = jasmine.createSpyObj('MatDialog', ['open']);

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
        { provide: MatSnackBar, useValue: mockSnackBar },
        { provide: MatDialog, useValue: mockDialog }
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

    describe('fiscal metric badges', () => {
      const getBadgeValue = (label: string): string => {
        const fields = Array.from(fixture.nativeElement.querySelectorAll('wfprev-icon-display-field')) as HTMLElement[];
        const badge = fields.find(
          field => field.querySelector('.field-label')?.textContent?.trim() === label
        );
        return badge?.querySelector('.field-val')?.textContent?.trim() ?? '';
      };

      const renderWithFiscalData = (fiscalData: any) => {
        mockProjectService.getProjectFiscalByProjectPlanFiscalGuid.and.returnValue(of(fiscalData));
        fixture = TestBed.createComponent(YearEndPerformanceUpdateComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
      };

      it('should display Forecast Amount with two decimal places', () => {
        renderWithFiscalData({ fiscalForecastAmount: 1234.56 } as any);
        expect(getBadgeValue('Forecast Amount')).toBe('$1,234.56');
      });

      it('should not round Forecast Amount to the nearest dollar', () => {
        renderWithFiscalData({ fiscalForecastAmount: 1234.5 } as any);
        expect(getBadgeValue('Forecast Amount')).toBe('$1,234.50');
      });

      it('should display Original Cost Estimate with two decimal places', () => {
        renderWithFiscalData({ totalCostEstimateAmount: 9876.54 } as any);
        expect(getBadgeValue('Original Cost Estimate')).toBe('$9,876.54');
      });

      it('should not round Original Cost Estimate to the nearest dollar', () => {
        renderWithFiscalData({ totalCostEstimateAmount: 9876.5 } as any);
        expect(getBadgeValue('Original Cost Estimate')).toBe('$9,876.50');
      });

      it('should pad whole dollar amounts to two decimal places', () => {
        renderWithFiscalData({ fiscalForecastAmount: 1000, totalCostEstimateAmount: 2000 } as any);
        expect(getBadgeValue('Forecast Amount')).toBe('$1,000.00');
        expect(getBadgeValue('Original Cost Estimate')).toBe('$2,000.00');
      });

      it('should fall back to $0.00 when amounts are not set', () => {
        renderWithFiscalData({} as any);
        expect(getBadgeValue('Forecast Amount')).toBe('$0.00');
        expect(getBadgeValue('Original Cost Estimate')).toBe('$0.00');
      });
    });

    it('should initialize projectGuid, fiscalGuid, and workflow from query params', () => {
      expect(component.projectGuid).toBe('test-project-guid');
      expect(component.fiscalGuid).toBe('test-fiscal-guid');
      expect(component.workflow).toBe('update');
    });

    it('should navigate back to project fiscal on goBack()', () => {
      component.goBack(true);
      expect(mockRouter.navigate).toHaveBeenCalledWith(['/' + ResourcesRoutes.EDIT_PROJECT], {
        queryParams: {
          projectGuid: 'test-project-guid',
          fiscalGuid: 'test-fiscal-guid',
          tab: 'fiscal'
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

    it('should navigate immediately when form is not dirty', () => {
      spyOn(component, 'isFormDirty').and.returnValue(false);

      component.goBack(true);

      expect(mockDialog.open).not.toHaveBeenCalled();
      expect(mockRouter.navigate).toHaveBeenCalledWith(['/' + ResourcesRoutes.EDIT_PROJECT], {
        queryParams: {
          projectGuid: 'test-project-guid',
          fiscalGuid: 'test-fiscal-guid',
          tab: 'fiscal'
        }
      });
    });

    it('should open confirmation dialog and navigate if confirmed when form is dirty', () => {
      spyOn(component, 'isFormDirty').and.returnValue(true);
      mockDialog.open.and.returnValue({
        afterClosed: () => of(true)
      } as any);

      component.goBack(true);

      expect(mockDialog.open).toHaveBeenCalled();
      expect(mockRouter.navigate).toHaveBeenCalledWith(['/' + ResourcesRoutes.EDIT_PROJECT], {
        queryParams: {
          projectGuid: 'test-project-guid',
          fiscalGuid: 'test-fiscal-guid',
          tab: 'fiscal'
        }
      });
    });

    it('should not navigate if confirmation dialog is cancelled when form is dirty', () => {
      spyOn(component, 'isFormDirty').and.returnValue(true);
      mockRouter.navigate.calls.reset();
      mockDialog.open.and.returnValue({
        afterClosed: () => of(false)
      } as any);

      component.goBack(true);

      expect(mockDialog.open).toHaveBeenCalled();
      expect(mockRouter.navigate).not.toHaveBeenCalled();
    });
  });

  describe('preserving state on a background reload', () => {
    beforeEach(() => {
      mockPermissionsService.hasAction.and.returnValue(true);
      fixture = TestBed.createComponent(YearEndPerformanceUpdateComponent);
      component = fixture.componentInstance;
      fixture.detectChanges();
    });

    const captureIsLoading = () => {
      const captured: { value?: boolean } = {};
      mockProjectService.getFiscalActivities.and.callFake(() => {
        captured.value = component.isLoading;
        return of({ _embedded: { activities: [] } });
      });
      return captured;
    };

    it('should keep the page mounted and leave the summary form alone when preserving state', () => {
      component.summaryForm.patchValue({
        fiscalReportedSpendAmount: 1234,
        outcomeComment: 'user typed this',
        acknowledgement: true
      });
      component.summaryForm.markAsDirty();
      spyOn(component, 'patchForm');
      const isLoadingDuringRequest = captureIsLoading();

      component.loadActivities(true);

      expect(isLoadingDuringRequest.value).toBeFalse();
      expect(component.patchForm).not.toHaveBeenCalled();
      expect(component.summaryForm.get('fiscalReportedSpendAmount')?.value).toBe(1234);
      expect(component.summaryForm.get('outcomeComment')?.value).toBe('user typed this');
      expect(component.summaryForm.get('acknowledgement')?.value).toBeTrue();
      expect(component.summaryForm.dirty).toBeTrue();
    });

    it('should show the spinner and repatch the summary form on a full reload', () => {
      spyOn(component, 'patchForm');
      const isLoadingDuringRequest = captureIsLoading();

      component.loadActivities();

      expect(isLoadingDuringRequest.value).toBeTrue();
      expect(component.patchForm).toHaveBeenCalled();
    });

    it('should preserve state when files are updated', () => {
      const loadActivitiesSpy = spyOn(component, 'loadActivities');

      component.onFilesUpdated('activity-1');

      expect(loadActivitiesSpy).toHaveBeenCalledWith(true);
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
