import { Component, NO_ERRORS_SCHEMA } from '@angular/core';
import { ComponentFixture, fakeAsync, TestBed, tick } from '@angular/core/testing';
import { FormBuilder, FormGroup } from '@angular/forms';
import { MatSnackBar } from '@angular/material/snack-bar';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { ActivatedRoute } from '@angular/router';
import { Observable, of, throwError } from 'rxjs';
import { ConfirmationDialogComponent } from 'src/app/components/confirmation-dialog/confirmation-dialog.component';
import { ProjectFiscal } from 'src/app/components/models';
import { CodeTableServices } from 'src/app/services/code-table-services';
import { ProjectService } from 'src/app/services/project-services';
import { TokenService } from 'src/app/services/token.service';
import { EndorsementCode, ModalMessages, ModalTitles } from 'src/app/utils/constants';
import * as Tools from 'src/app/utils/tools';
import { PlanFiscalStatusIcons } from 'src/app/utils/tools';
import { ProjectFiscalsComponent } from './project-fiscals.component';

const mockProjectService = {
  getProjectFiscalsByProjectGuid: jasmine.createSpy('getProjectFiscalsByProjectGuid').and.returnValue(
    of({ _embedded: { projectFiscals: [{ fiscalYear: 2023, projectFiscalName: 'Test Fiscal' }] } })
  ),
  updateProjectFiscal: jasmine.createSpy('updateProjectFiscal').and.returnValue(of({})),
  createProjectFiscal: jasmine.createSpy('createProjectFiscal').and.returnValue(of({})),
  deleteProjectFiscalByProjectPlanFiscalGuid: jasmine.createSpy('deleteProjectFiscalByProjectPlanFiscalGuid').and.returnValue(of({})),
  getFiscalActivities: jasmine.createSpy('getFiscalActivities').and.returnValue(of({
    _embedded: { activities: [] }
  })),
  getProjectByProjectGuid: jasmine.createSpy('getProjectByProjectGuid').and.returnValue(of({
    latitude: '48.4284',
    longitude: '-123.3656'
  })),
  getProjectBoundaries: jasmine.createSpy('getProjectBoundaries').and.returnValue(
    of({ _embedded: { projectBoundary: [] } })
  ),
};

const mockCodeTableServices = {
  fetchCodeTable: jasmine.createSpy('fetchCodeTable').and.returnValue(of({ _embedded: {} })),
};

const mockSnackBar = {
  open: jasmine.createSpy('open'),
};

describe('ProjectFiscalsComponent', () => {
  let component: ProjectFiscalsComponent;
  let fixture: ComponentFixture<ProjectFiscalsComponent>;

  @Component({
    selector: 'wfprev-fiscal-map',
    template: '<div></div>'
  })
  class MockFiscalMapComponent {
    ngOnInit() { }
    ngAfterViewInit() { }
    initMap() { }
  }
  @Component({
    selector: 'wfprev-activities',
    template: ''
  })
  class MockActivitiesComponent {
    isFormDirty = () => false;
  }

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [BrowserAnimationsModule],
      declarations: [MockFiscalMapComponent, MockActivitiesComponent],
      schemas: [NO_ERRORS_SCHEMA],
      providers: [
        {
          provide: TokenService, useValue: {
            getUserFullName: jasmine.createSpy('getUserFullName').and.returnValue('Test User'),
            getIdir: jasmine.createSpy('getIdir').and.returnValue('IDIR123')
          }
        },
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: {
              queryParamMap: {
                get: (key: string) => {
                  if (key === 'projectGuid') return 'test-guid';
                  if (key === 'fiscalGuid') return 'fiscal-guid-123';
                  return null;
                }
              }
            },
            queryParamMap: of({
              get: (key: string) => {
                if (key === 'projectGuid') return 'test-guid';
                if (key === 'fiscalGuid') return 'fiscal-guid-123';
                return null;
              }
            })
          }
        },
        { provide: ProjectService, useValue: mockProjectService },
        { provide: CodeTableServices, useValue: mockCodeTableServices },
        { provide: MatSnackBar, useValue: mockSnackBar },
        FormBuilder,
      ],
    })
      .overrideComponent(ProjectFiscalsComponent, {
        set: {
          imports: [],  // Remove the real FiscalMapComponent and ActivitiesComponent
          schemas: [NO_ERRORS_SCHEMA]
        }
      })
      .compileComponents();

    fixture = TestBed.createComponent(ProjectFiscalsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should generate fiscal activities correctly', () => {
    component.generateFiscalYears();
    expect(component.fiscalYears.length).toBe(11);
    expect(component.fiscalYears[0]).toBe(`${new Date().getFullYear() - 5}/${(new Date().getFullYear() - 4).toString().slice(-2)}`);
  });

  it('should load code tables successfully', () => {
    mockCodeTableServices.fetchCodeTable.calls.reset(); // ✅ Reset call count
    component.loadCodeTables();
    expect(mockCodeTableServices.fetchCodeTable).toHaveBeenCalledTimes(3);
  });

  it('should sort proposalTypeCode in loadDropdownOptions', () => {
    component.proposalTypeCode = [
      { description: 'Proposal C' },
      { description: 'Proposal A' },
      { description: 'Proposal B' }
    ];

    component.loadDropdownOptions();

    expect(component.proposalTypeCode).toEqual([
      { description: 'Proposal A' },
      { description: 'Proposal B' },
      { description: 'Proposal C' }
    ]);
  });

  it('should handle errors in loading code tables', () => {
    mockCodeTableServices.fetchCodeTable.calls.reset(); // ✅ Reset call count
    mockCodeTableServices.fetchCodeTable.and.returnValue(throwError(() => new Error('Error fetching data')));
    component.loadCodeTables();
    expect(mockCodeTableServices.fetchCodeTable).toHaveBeenCalledTimes(3);
  });

  it('should load project fiscals', fakeAsync(() => {
    // ✅ Ensure the mock returns a valid response
    mockProjectService.getProjectFiscalsByProjectGuid.and.returnValue(
      of({
        _embedded: {
          projectFiscals: [
            { fiscalYear: 2023, projectFiscalName: 'Test Fiscal' }
          ]
        }
      })
    );

    component.loadProjectFiscals();
    tick();

    expect(mockProjectService.getProjectFiscalsByProjectGuid).toHaveBeenCalledWith('test-guid');
    expect(component.projectFiscals.length).toBeGreaterThan(0); // ✅ Should now have at least one fiscal
    expect(component.fiscalForms.length).toBe(component.projectFiscals.length); // ✅ Forms should match project fiscals count
  }));


  it('should handle errors in loading project fiscals', () => {
    mockProjectService.getProjectFiscalsByProjectGuid.and.returnValue(throwError(() => new Error('API Error')));
    component.loadProjectFiscals();
    expect(component.projectFiscals.length).toBe(0);
  });

  it('should add a new fiscal', () => {
    component.projectFiscals = []; // ✅ Ensure projectFiscals starts empty
    component.projectGuid = 'test-guid'; // ✅ Ensure projectGuid is set before calling the method

    component.addNewFiscal();

    expect(component.projectFiscals.length).toBe(1); // ✅ Should increase from 0 to 1
    expect(component.selectedTabIndex).toBe(0); // ✅ Should select the first added fiscal
  });

  it('should save a new fiscal', () => {
    spyOn(component, 'loadProjectFiscals');

    const mockFiscal = {
      projectGuid: 'test-guid',
      projectFiscalName: 'Test Fiscal',
      fiscalYear: 2023,
      planFiscalStatusCode: { planFiscalStatusCode: 'DRAFT' },
      endorserName: 'Test User'
    };
    component.projectFiscals = [mockFiscal];
    component.fiscalForms = [component.createFiscalForm(mockFiscal)];

    mockProjectService.createProjectFiscal.and.returnValue(of({}));

    component.onSaveFiscal(0);

    expect(mockProjectService.createProjectFiscal).toHaveBeenCalled();
    expect(mockSnackBar.open).toHaveBeenCalledWith(
      component.messages.projectFiscalCreatedSuccess,
      'OK',
      { duration: 5000, panelClass: 'snackbar-success' }
    );
    expect(component.loadProjectFiscals).toHaveBeenCalled();
  });

  it('should handle errors when saving a new fiscal', () => {
    mockProjectService.createProjectFiscal.and.returnValue(throwError(() => new Error('API Error')));

    // ✅ Make sure there is at least one fiscal object with required properties
    component.projectFiscals = [
      {
        projectGuid: 'test-guid',
        projectFiscalName: 'Test Fiscal',
        fiscalYear: 2023,
        planFiscalStatusCode: { planFiscalStatusCode: 'DRAFT' }
      }
    ];

    component.fiscalForms = [component.createFiscalForm(component.projectFiscals[0])];

    component.onSaveFiscal(0);

    expect(mockSnackBar.open).toHaveBeenCalledWith(
      component.messages.projectFiscalCreatedFailure,
      'OK',
      {
        duration: 5000,
        panelClass: 'snackbar-error',
      }
    );
  });

  it('should update an existing fiscal', () => {
    spyOn(component, 'loadProjectFiscals');

    // ✅ Ensure updateProjectFiscal returns success
    mockProjectService.updateProjectFiscal.and.returnValue(of({}));

    component.projectFiscals = [{ projectPlanFiscalGuid: 'existing-guid' }]; // ✅ Ensure a valid fiscal object exists

    component.onSaveFiscal(0);

    expect(mockProjectService.updateProjectFiscal).toHaveBeenCalled();
    expect(mockSnackBar.open).toHaveBeenCalledWith(
      component.messages.projectFiscalUpdatedSuccess,
      'OK',
      { duration: 5000, panelClass: 'snackbar-success' } // ✅ Ensure correct success message
    );
    component.onSaveFiscal(0);
    expect(component.loadProjectFiscals).toHaveBeenCalled();
  });


  it('should handle errors when updating an existing fiscal', fakeAsync(() => {
    // ✅ Ensure projectFiscals is initialized before setting properties
    component.projectFiscals = [{ projectPlanFiscalGuid: 'existing-guid' }];

    mockProjectService.updateProjectFiscal.and.returnValue(throwError(() => new Error('API Error')));

    component.onSaveFiscal(0);

    expect(mockSnackBar.open).toHaveBeenCalledWith(
      component.messages.projectFiscalUpdatedFailure,
      'OK',
      { duration: 5000, panelClass: 'snackbar-error' } // ✅ Ensure correct error message is tested
    );
  }));

  it('should clear all fields for a new fiscal entry on cancel', () => {
    component.projectFiscals = [{ fiscalYear: '', projectFiscalName: '', projectGuid: 'test-guid' }];
    component.fiscalForms = [component.createFiscalForm(component.projectFiscals[0])];

    spyOn(component.fiscalForms[0], 'reset');

    component.onCancelFiscal(0);

    expect(component.fiscalForms[0].reset).toHaveBeenCalled();
  });

  it('should revert to original values for an existing fiscal entry on cancel', () => {
    component.projectFiscals = [
      { projectPlanFiscalGuid: 'existing-guid', fiscalYear: 2023, projectFiscalName: 'Test Fiscal' }
    ];
    component.originalFiscalValues = [
      {
        projectPlanFiscalGuid: 'existing-guid',
        fiscalYear: 2023,
        projectFiscalName: 'Test Fiscal (Original)',
        planFiscalStatusCode: { planFiscalStatusCode: 'DRAFT', description: 'Draft' }
      }
    ];
    component.fiscalForms = [component.createFiscalForm(component.projectFiscals[0])];

    spyOn(component.fiscalForms[0], 'patchValue');
    spyOn(component.fiscalForms[0], 'markAsPristine');
    spyOn(component.fiscalForms[0], 'markAsUntouched');

    component.onCancelFiscal(0);

    expect(component.fiscalForms[0].patchValue).toHaveBeenCalledWith(
      jasmine.objectContaining({
        projectPlanFiscalGuid: 'existing-guid',
        fiscalYear: 2023,
        projectFiscalName: 'Test Fiscal (Original)',
        planFiscalStatusCode: 'DRAFT',
      }),
      { emitEvent: false }
    );
    expect(component.fiscalForms[0].markAsPristine).toHaveBeenCalled();
    expect(component.fiscalForms[0].markAsUntouched).toHaveBeenCalled();
  });


  it('should not fail if onCancelFiscal() is called with an invalid index', () => {
    component.projectFiscals = [];
    component.fiscalForms = [];

    expect(() => component.onCancelFiscal(0)).not.toThrow();
  });

  it('should open confirmation dialog when deleting a fiscal activity', () => {
    spyOn(component.dialog, 'open').and.returnValue({
      afterClosed: () => of(true) // Simulate user clicking "Confirm"
    } as any);

    component.projectFiscals = [{
      projectPlanFiscalGuid: 'test-guid',
      fiscalYear: 2025,
      projectFiscalName: 'My Plan'
    }];
    component.selectedTabIndex = 0;

    component.deleteFiscalYear({ value: component.projectFiscals[0] }, 0);

    expect(component.dialog.open).toHaveBeenCalledWith(ConfirmationDialogComponent, {
      data: {
        indicator: 'delete-fiscal-year',
        title: ModalTitles.DELETE_FISCAL_YEAR_TITLE,
        message: `Are you sure you want to delete My Plan:2025/26? This action cannot be reversed and will immediately remove the Fiscal Activity from the Project scope.`
      },
      width: '600px',
    });
  });


  it('should delete a fiscal activity after confirmation', () => {
    spyOn(component.dialog, 'open').and.returnValue({
      afterClosed: () => of(true) // Simulate user clicking "Confirm"
    } as any);
    spyOn(component, 'loadProjectFiscals');

    mockProjectService.deleteProjectFiscalByProjectPlanFiscalGuid = jasmine.createSpy().and.returnValue(of({}));

    component.projectFiscals = [{ projectPlanFiscalGuid: 'test-guid' }];
    component.deleteFiscalYear({ value: component.projectFiscals[0] }, 0);

    expect(mockProjectService.deleteProjectFiscalByProjectPlanFiscalGuid).toHaveBeenCalledWith('test-guid', 'test-guid');
    expect(mockSnackBar.open).toHaveBeenCalledWith(
      component.messages.projectFiscalDeletedSuccess,
      'OK',
      { duration: 5000, panelClass: 'snackbar-success' }
    );
    expect(component.loadProjectFiscals).toHaveBeenCalledWith(true);
  });

  it('should show error snackbar if deletion fails', () => {
    spyOn(component.dialog, 'open').and.returnValue({
      afterClosed: () => of(true) // Simulate user clicking "Confirm"
    } as any);

    mockProjectService.deleteProjectFiscalByProjectPlanFiscalGuid = jasmine.createSpy().and.returnValue(
      throwError(() => new Error('API Error'))
    );

    component.projectFiscals = [{ projectPlanFiscalGuid: 'test-guid' }];
    component.deleteFiscalYear({ value: component.projectFiscals[0] }, 0);

    expect(mockSnackBar.open).toHaveBeenCalledWith(
      component.messages.projectFiscalDeletedFailure,
      'OK',
      { duration: 5000, panelClass: 'snackbar-error' }
    );
  });

  it('should return true for isUndeletable if isApprovedInd is true', () => {
    const form = { value: { isApprovedInd: true } };
    expect(component.isUndeletable(form)).toBe(true);
  });

  it('should return false for isUndeletable if isApprovedInd is false', () => {
    const form = { value: { isApprovedInd: false } };
    expect(component.isUndeletable(form)).toBe(false);
  });

  it('should return false for isUndeletable if isApprovedInd is undefined', () => {
    const form = { value: {} };
    expect(component.isUndeletable(form)).toBe(false);
  });

  it('should return false for isUndeletable if form is null', () => {
    expect(component.isUndeletable(null)).toBe(false);
  });

  it('should return false if no forms are dirty', () => {
    component.fiscalForms = [new FormGroup({})]; // No fields, not dirty
    expect(component.isFormDirty()).toBe(false);
  });

  it('should return true if at least one form is dirty', () => {
    const form1 = new FormGroup({});
    spyOnProperty(form1, 'dirty', 'get').and.returnValue(true);

    const form2 = new FormGroup({});
    spyOnProperty(form2, 'dirty', 'get').and.returnValue(false);

    component.fiscalForms = [form1, form2];

    expect(component.isFormDirty()).toBe(true);
  });

  it('should return true from canDeactivate() if no forms are dirty', () => {
    spyOn(component, 'isFormDirty').and.returnValue(false);
    expect(component.canDeactivate()).toBe(true);
  });

  it('should open a confirmation dialog if forms are dirty and return false when user cancels', (done) => {
    spyOn(component, 'isFormDirty').and.returnValue(true);

    const mockDialogRef = { afterClosed: () => of(false) };
    spyOn(component.dialog, 'open').and.returnValue(mockDialogRef as any);

    const result = component.canDeactivate();

    if (result instanceof Observable) {
      result.subscribe((value: boolean) => {
        expect(component.dialog.open).toHaveBeenCalledWith(ConfirmationDialogComponent, {
          data: {
            indicator: 'confirm-unsave',
            title: ModalTitles.CONFIRM_UNSAVE_TITLE,
            message: ModalMessages.CONFIRM_UNSAVE_MESSAGE
          },
          width: '600px',
        });
        expect(value).toBe(false);
        done();
      });
    } else {
      fail('Expected an Observable but received something else');
    }
  });

  it('should allow navigation if user confirms in the dialog', (done) => {
    spyOn(component, 'isFormDirty').and.returnValue(true);

    const mockDialogRef = { afterClosed: () => of(true) };
    spyOn(component.dialog, 'open').and.returnValue(mockDialogRef as any);

    const result = component.canDeactivate();

    if (result instanceof Observable) {
      result.subscribe((value: boolean) => {
        expect(value).toBe(true);
        done();
      });
    } else {
      fail('Expected an Observable but received something else');
    }
  });

  it('should not allow negative values for fiscalPlannedProjectSizeHa', () => {
    component.fiscalForms[0] = component.createFiscalForm();
    const control = component.fiscalForms[0].get('fiscalPlannedProjectSizeHa');

    control?.setValue(-10);
    expect(control?.valid).toBeFalse();
    expect(control?.hasError('min')).toBeTrue();

    control?.setValue(10);
    expect(control?.valid).toBeTrue();
  });

  it('should not allow negative values for totalCostEstimateAmount', () => {
    component.fiscalForms[0] = component.createFiscalForm();
    const control = component.fiscalForms[0].get('totalCostEstimateAmount');

    control?.setValue(-100);
    expect(control?.valid).toBeFalse();
    expect(control?.hasError('min')).toBeTrue();

    control?.setValue(100);
    expect(control?.valid).toBeTrue();
  });

  it('should not allow negative values for fiscalForecastAmount', () => {
    component.fiscalForms[0] = component.createFiscalForm();
    const control = component.fiscalForms[0].get('fiscalForecastAmount');

    control?.setValue(-500);
    expect(control?.valid).toBeFalse();
    expect(control?.hasError('min')).toBeTrue();

    control?.setValue(500);
    expect(control?.valid).toBeTrue();
  });

  it('should sort fiscalYears, activityCategoryCode, planFiscalStatusCode in loadDropdownOptions', () => {
    // Mock unsorted data
    component.fiscalYears = ['2025/26', '2023/24', '2024/25'];
    component.activityCategoryCode = [
      { description: 'Category C' },
      { description: 'Category A' },
      { description: 'Category B' }
    ];
    component.planFiscalStatusCode = [
      { description: 'Status 2' },
      { description: 'Status 1' }
    ];

    component.loadDropdownOptions();

    expect(component.fiscalYears).toEqual(['2023/24', '2024/25', '2025/26']);
    expect(component.activityCategoryCode).toEqual([
      { description: 'Category A' },
      { description: 'Category B' },
      { description: 'Category C' }
    ]);
    expect(component.planFiscalStatusCode).toEqual([
      { description: 'Status 1' },
      { description: 'Status 2' }
    ]);
  });

  it('should not throw error if loadDropdownOptions is called with empty arrays', () => {
    component.fiscalYears = [];
    component.activityCategoryCode = [];
    component.planFiscalStatusCode = [];

    expect(() => component.loadDropdownOptions()).not.toThrow();
    expect(component.fiscalYears).toEqual([]);
    expect(component.activityCategoryCode).toEqual([]);
    expect(component.planFiscalStatusCode).toEqual([]);
  });

  it('should handle null or undefined values in loadDropdownOptions without errors', () => {
    component.fiscalYears = null as any;
    component.activityCategoryCode = null as any;
    component.planFiscalStatusCode = null as any;

    expect(() => component.loadDropdownOptions()).not.toThrow();
    expect(component.fiscalYears).toEqual([]);
    expect(component.activityCategoryCode).toEqual([]);
    expect(component.planFiscalStatusCode).toEqual([]);
  });

  it('should refresh boundaries on map when onBoundariesChanged is called', () => {
    component.fiscalMapComponent = {
      getAllActivitiesBoundaries: jasmine.createSpy('getAllActivitiesBoundaries')
    } as any;

    component.onBoundariesChanged();

    expect(component.fiscalMapComponent.getAllActivitiesBoundaries).toHaveBeenCalled();
  });

  it('should update selectedTabIndex and currentFiscalGuid on tab change', () => {
    const updateSpy = spyOn(component, 'updateCurrentFiscalGuid');
    component.onTabChange(2);

    expect(component.selectedTabIndex).toBe(2);
    expect(updateSpy).toHaveBeenCalled();
  });

  it('should return correct description from getCodeDescription()', () => {
    component.selectedTabIndex = 0;

    component.activityCategoryCode = [
      { activityCategoryCode: 'CAT1', description: 'Activity A' }
    ];
    component.planFiscalStatusCode = [
      { planFiscalStatusCode: 'PFS1', description: 'Draft' }
    ];
    component.proposalTypeCode = [
      { proposalTypeCode: 'NEW', description: 'New Proposal' }
    ];

    component.fiscalForms = [
      component.createFiscalForm({
        activityCategoryCode: 'CAT1',
        planFiscalStatusCode: { planFiscalStatusCode: 'PFS1' },
        proposalTypeCode: 'NEW'
      })
    ];

    expect(component.getCodeDescription('activityCategoryCode')).toBe('Activity A');
    expect(component.getCodeDescription('planFiscalStatusCode')).toBe('Draft');
    expect(component.getCodeDescription('proposalTypeCode')).toBe('New Proposal');
  });

  it('should return null from getCodeDescription() if control value not matched', () => {
    component.selectedTabIndex = 0;
    component.activityCategoryCode = [];
    component.planFiscalStatusCode = [];
    component.proposalTypeCode = [];

    component.fiscalForms = [
      component.createFiscalForm({
        activityCategoryCode: 'UNKNOWN',
        planFiscalStatusCode: 'UNKNOWN',
        proposalTypeCode: 'UNKNOWN'
      })
    ];

    expect(component.getCodeDescription('activityCategoryCode')).toBeNull();
    expect(component.getCodeDescription('planFiscalStatusCode')).toBeNull();
    expect(component.getCodeDescription('proposalTypeCode')).toBeNull();
  });

  it('should return null from getCodeDescription() if form is missing', () => {
    component.fiscalForms = [];
    component.selectedTabIndex = 0;

    expect(component.getCodeDescription('activityCategoryCode')).toBeNull();
  });

  it('should sort project fiscals by fiscalYear descending and then by projectFiscalName ascending', fakeAsync(() => {
    const unsortedFiscals = [
      { fiscalYear: 2025, projectFiscalName: 'B Plan' },
      { fiscalYear: 2024, projectFiscalName: 'X Plan' },
      { fiscalYear: 2025, projectFiscalName: 'A Plan' },
      { fiscalYear: 2023, projectFiscalName: 'Z Plan' }
    ];

    const expectedSorted = [
      { fiscalYear: 2025, projectFiscalName: 'A Plan' },
      { fiscalYear: 2025, projectFiscalName: 'B Plan' },
      { fiscalYear: 2024, projectFiscalName: 'X Plan' },
      { fiscalYear: 2023, projectFiscalName: 'Z Plan' }
    ];

    mockProjectService.getProjectFiscalsByProjectGuid.and.returnValue(
      of({
        _embedded: { projectFiscals: unsortedFiscals }
      })
    );

    component.loadProjectFiscals();
    tick();

    const sortedNames = component.projectFiscals.map(f => f.projectFiscalName);
    expect(sortedNames).toEqual(expectedSorted.map(f => f.projectFiscalName));
  }));

  it('should handle null or undefined projectFiscalName values when sorting', fakeAsync(() => {
    const unsortedFiscals = [
      { fiscalYear: 2025, projectFiscalName: null },
      { fiscalYear: 2025, projectFiscalName: undefined },
      { fiscalYear: 2025, projectFiscalName: 'Alpha' },
      { fiscalYear: 2025, projectFiscalName: 'Beta' }
    ];

    const expectedSorted = [
      { fiscalYear: 2025, projectFiscalName: '' },
      { fiscalYear: 2025, projectFiscalName: '' },
      { fiscalYear: 2025, projectFiscalName: 'Alpha' },
      { fiscalYear: 2025, projectFiscalName: 'Beta' }
    ];

    mockProjectService.getProjectFiscalsByProjectGuid.and.returnValue(
      of({
        _embedded: { projectFiscals: unsortedFiscals }
      })
    );

    component.loadProjectFiscals();
    tick();

    const sortedNames = component.projectFiscals.map(f => f.projectFiscalName ?? '');
    expect(sortedNames).toEqual(expectedSorted.map(f => f.projectFiscalName));
  }));

  it('should remove unsaved draft fiscal after confirmation', () => {
    spyOn(component.dialog, 'open').and.returnValue({
      afterClosed: () => of(true) // Simulate user clicking "Confirm"
    } as any);

    component.projectFiscals = [{
      fiscalYear: 2025,
      projectFiscalName: 'Unsaved Draft' // No projectPlanFiscalGuid = unsaved
    }];
    component.fiscalForms = [component.createFiscalForm(component.projectFiscals[0])];
    component.selectedTabIndex = 0;

    component.deleteFiscalYear({ value: component.projectFiscals[0] }, 0);

    expect(component.projectFiscals.length).toBe(0);
    expect(component.fiscalForms.length).toBe(0);
    expect(component.selectedTabIndex).toBe(0);
    expect(mockSnackBar.open).toHaveBeenCalledWith(
      component.messages.projectFiscalDeletedSuccess,
      'OK',
      { duration: 5000, panelClass: 'snackbar-success' }
    );
  });

  it('should get FormControl from fiscalForms', () => {
    const form = component['fb'].group({ testField: [] });
    component.fiscalForms = [form];
    const control = component.getFiscalControl(0, 'testField');
    expect(control).toBeTruthy();
  });

  it('should return default fiscal data with expected values', () => {
    component.projectGuid = 'test-guid';

    const result = component.getDefaultFiscalData();

    expect(result).toEqual({
      fiscalYear: 0,
      projectFiscalName: '',
      projectGuid: 'test-guid',
      planFiscalStatusCode: {
        planFiscalStatusCode: 'DRAFT',
        description: 'Draft'
      },
      proposalTypeCode: 'NEW',
      projectPlanStatusCode: 'ACTIVE',
      activityCategoryCode: '',
      fiscalPlannedProjectSizeHa: 0,
      fiscalPlannedCostPerHaAmt: 0,
      fiscalReportedSpendAmount: 0,
      fiscalActualAmount: 0,
      fiscalActualCostPerHaAmt: 0,
      firstNationsEngagementInd: false,
      firstNationsDelivPartInd: false,
      isApprovedInd: false,
      isDelayedInd: false,
      totalCostEstimateAmount: 0
    });
  });

  it('should return the correct status description', () => {
    component.planFiscalStatusCode = [
      { planFiscalStatusCode: 'DRAFT', description: 'Draft Description' }
    ];

    const form = component.createFiscalForm({
      planFiscalStatusCode: 'DRAFT'
    });

    component.fiscalForms = [form];

    const result = component.getStatusDescription(0);

    expect(result).toBe('Draft Description');
  });

  it('should return null if no matching status description is found', () => {
    component.planFiscalStatusCode = [];

    const form = component.createFiscalForm({ planFiscalStatusCode: 'UNKNOWN' });
    component.fiscalForms = [form];

    const result = component.getStatusDescription(0);

    expect(result).toBeNull();
  });

  it('should return null if form is missing', () => {
    component.fiscalForms = [];

    const result = component.getStatusDescription(0);

    expect(result).toBeNull();
  });

  it('should return the correct status icon', () => {
    const result = component.getStatusIcon('DRAFT');
    expect(result).toBe(PlanFiscalStatusIcons['DRAFT']);
  });

  it('should return undefined for unknown status', () => {
    const result = component.getStatusIcon('INVALID_STATUS');
    expect(result).toBeUndefined();
  });

  it('should update fiscal status and call onSaveFiscal()', () => {
    const form = component.createFiscalForm();
    component.fiscalForms = [form];

    const dialogRefSpy = jasmine.createSpyObj('MatDialogRef', ['afterClosed']);
    dialogRefSpy.afterClosed.and.returnValue(of(true));

    spyOn(component.dialog, 'open').and.returnValue(dialogRefSpy);

    spyOn(component, 'onSaveFiscal');

    component.updateFiscalStatus(0, 'PROPOSED');

    expect(form.get('planFiscalStatusCode')?.value).toBe('PROPOSED');
    expect(form.dirty).toBeTrue();
    expect(form.touched).toBeTrue();
    expect(component.onSaveFiscal).toHaveBeenCalledWith(0);
  });

  it('should do nothing if form is missing', () => {
    component.fiscalForms = [];
    expect(() => component.updateFiscalStatus(0, 'PROPOSED')).not.toThrow();
  });

  it('should update fiscal without promotion if not endorsed and approved', () => {
    spyOn(component, 'loadProjectFiscals');
    component.projectFiscals = [{
      projectPlanFiscalGuid: 'guid-1'
    }];
    component.selectedTabIndex = 0;
    const updatedFiscal = {
      isApprovedInd: false,
      endorsementCode: { endorsementCode: EndorsementCode.NOT_ENDORS },
      planFiscalStatusCode: { planFiscalStatusCode: 'DRAFT' }
    } as ProjectFiscal;
    mockProjectService.updateProjectFiscal.and.returnValue(of({}));
    component.onSaveEndorsement(updatedFiscal);
    expect(mockProjectService.updateProjectFiscal).toHaveBeenCalledWith(
      'test-guid',
      'guid-1',
      jasmine.objectContaining({ isApprovedInd: false })
    );
    expect(mockSnackBar.open).toHaveBeenCalledWith(
      component.messages.projectFiscalUpdatedSuccess,
      'OK',
      { duration: 5000, panelClass: 'snackbar-success' }
    );
    expect(component.loadProjectFiscals).toHaveBeenCalledWith(true);
  });

  it('should show error snackbar if updateProjectFiscal fails', () => {
    component.projectFiscals = [{
      projectPlanFiscalGuid: 'guid-1'
    }];
    component.selectedTabIndex = 0;
    const updatedFiscal = {
      isApprovedInd: false
    } as ProjectFiscal;
    mockProjectService.updateProjectFiscal.and.returnValue(
      throwError(() => new Error('API Error'))
    );
    component.onSaveEndorsement(updatedFiscal);
    expect(mockSnackBar.open).toHaveBeenCalledWith(
      component.messages.projectFiscalUpdatedFailure,
      'OK',
      { duration: 5000, panelClass: 'snackbar-error' }
    );
  });

  afterEach(() => {
    try { jasmine.clock().uninstall(); } catch { }
  });
  it('should build submission fields with currentIdir and local ISO timestamp', () => {
    jasmine.clock().install();
    jasmine.clock().mockDate(new Date('2025-08-08T19:34:56.000Z'));

    component.currentIdir = 'IDIR123';

    const expectedTs = Tools.getUtcIsoTimestamp();
    const result = component.buildSubmissionFields();

    expect(result).toEqual({
      submittedByUserUserid: 'IDIR123',
      submissionTimestamp: expectedTs,
    });

    jasmine.clock().uninstall();
  });

  describe('patchStatusCode()', () => {
    const call = (val: any) => (component as any).patchStatusCode(val);

    it('returns the string code as is', () => {
      expect(call('DRAFT')).toBe('DRAFT');
    });

    it('extracts code from object', () => {
      expect(call({ planFiscalStatusCode: 'CANCELLED' })).toBe('CANCELLED');
    });

    it('returns empty string for null or undefined', () => {
      expect(call(null)).toBe('');
      expect(call(undefined)).toBe('');
    });
  });

  it('selects tab by newFiscalGuid when provided', fakeAsync(() => {
    mockProjectService.getProjectFiscalsByProjectGuid.and.returnValue(of({
      _embedded: {
        projectFiscals: [
          { projectPlanFiscalGuid: 'A', fiscalYear: 2024, projectFiscalName: 'A Plan' },
          { projectPlanFiscalGuid: 'B', fiscalYear: 2025, projectFiscalName: 'B Plan' },
        ]
      }
    }));
    component.loadProjectFiscals(false, 'B');
    tick();
    expect(component.selectedTabIndex).toBe(0);
  }));

  it('selects tab by focusedFiscalId when no newFiscalGuid', fakeAsync(() => {
    mockProjectService.getProjectFiscalsByProjectGuid.and.returnValue(of({
      _embedded: {
        projectFiscals: [
          { projectPlanFiscalGuid: 'X1', fiscalYear: 2025 },
          { projectPlanFiscalGuid: 'X2', fiscalYear: 2024 },
        ]
      }
    }));
    component.focusedFiscalId = 'X2';
    component.loadProjectFiscals();
    tick();
    expect(component.selectedTabIndex).toBe(1);
  }));

  it('falls back to previousTabIndex or jump to 0', fakeAsync(() => {
    mockProjectService.getProjectFiscalsByProjectGuid.and.returnValue(of({
      _embedded: { projectFiscals: [{ projectPlanFiscalGuid: 'Y1', fiscalYear: 2025 }] }
    }));
    component.selectedTabIndex = 5;
    component.loadProjectFiscals();
    tick();
    expect(component.selectedTabIndex).toBe(0);
  }));

  it('should block deletion and show snackbar if activities exist', () => {
    mockSnackBar.open.calls.reset();
    mockProjectService.deleteProjectFiscalByProjectPlanFiscalGuid.calls.reset();

    // ✅ Mock dialog.open to auto-confirm
    spyOn(component.dialog, 'open').and.returnValue({
      afterClosed: () => of(true) // Simulates user clicking "Confirm"
    } as any);

    // ✅ Provide activities that match the fiscalGuid
    (component as any).activitiesComponent = {
      activities: [{ projectPlanFiscalGuid: 'test-guid' }]
    };

    component.projectFiscals = [{ projectPlanFiscalGuid: 'test-guid' }];
    component.selectedTabIndex = 0;

    fixture.detectChanges();

    component.deleteFiscalYear({ value: component.projectFiscals[0] }, 0);

    expect(mockSnackBar.open).toHaveBeenCalledOnceWith(
      component.messages.fiscalActivityDeletedFailure,
      'OK',
      { duration: 5000, panelClass: 'snackbar-error' }
    );

    expect(mockProjectService.deleteProjectFiscalByProjectPlanFiscalGuid)
      .not.toHaveBeenCalled();
  });


  it('should proceed with deletion if there are no activities', () => {
    spyOn(component.dialog, 'open').and.returnValue({
      afterClosed: () => of(true)
    } as any);
    spyOn(component, 'loadProjectFiscals');

    component.activitiesComponent = {
      activities: []
    } as any;

    mockProjectService.deleteProjectFiscalByProjectPlanFiscalGuid = jasmine.createSpy().and.returnValue(of({}));

    component.projectFiscals = [{ projectPlanFiscalGuid: 'test-guid' }];
    component.deleteFiscalYear({ value: component.projectFiscals[0] }, 0);

    expect(mockProjectService.deleteProjectFiscalByProjectPlanFiscalGuid).toHaveBeenCalledWith('test-guid', 'test-guid');
    expect(mockSnackBar.open).toHaveBeenCalledWith(
      component.messages.projectFiscalDeletedSuccess,
      'OK',
      { duration: 5000, panelClass: 'snackbar-success' }
    );
  });

  it('updates existing fiscal TO DRAFT and resets endorsement/approval fields', () => {
    jasmine.clock().install();
    jasmine.clock().mockDate(new Date('2025-08-08T19:34:56.000Z'));

    const existing = {
      projectGuid: 'test-guid',
      projectPlanFiscalGuid: 'existing-guid',
      projectFiscalName: 'Existing Plan',
      fiscalYear: 2025,
      endorserName: 'Endorser X',
      endorsementTimestamp: '2025-08-01T10:00:00Z',
      endorsementCode: { endorsementCode: EndorsementCode.ENDORSED },
      endorsementComment: 'Looks good',
      approverName: 'Approver Y',
      approvedTimestamp: '2025-08-02T09:00:00Z',
      isApprovedInd: true,
      businessAreaComment: 'Original BAC',
      planFiscalStatusCode: { planFiscalStatusCode: 'PROPOSED' },
      activityCategoryCode: 'CAT1',
    };

    component.projectFiscals = [existing];
    const form = component.createFiscalForm(existing);
    form.get('planFiscalStatusCode')!.setValue('DRAFT'); // trigger isUpdateToDraft
    component.fiscalForms = [form];

    mockProjectService.updateProjectFiscal.and.returnValue(of({}));

    component.onSaveFiscal(0);

    const [, , payload] = mockProjectService.updateProjectFiscal.calls.mostRecent()
      .args as [string, string, ProjectFiscal];

    expect(payload.planFiscalStatusCode).toEqual({ planFiscalStatusCode: 'DRAFT' });
    expect(payload.businessAreaComment).toBeUndefined();
    expect(payload.endorserName).toBeUndefined();
    expect(payload.endorsementTimestamp).toBeUndefined();
    expect(payload.endorsementCode).toEqual({ endorsementCode: EndorsementCode.NOT_ENDORS });
    expect(payload.endorsementComment).toBeUndefined();
    expect(payload.approverName).toBeUndefined();
    expect(payload.approvedTimestamp).toBeUndefined();
    expect(payload.isApprovedInd).toBeFalse();
    expect(payload.submissionTimestamp).toBe('2025-08-08T19:34:56.000Z');

    jasmine.clock().uninstall();
  });

  it('updates an existing fiscal to NON-DRAFT and preserves endorsement/approval fields', () => {
    jasmine.clock().install();
    jasmine.clock().mockDate(new Date('2025-08-08T19:34:56.000Z'));

    const existing = {
      projectGuid: 'test-guid',
      projectPlanFiscalGuid: 'existing-guid',
      projectFiscalName: 'Existing Plan',
      fiscalYear: 2025,
      activityCategoryCode: 'CAT1',
      endorserName: 'Endorser X',
      endorsementTimestamp: '2025-08-01T10:00:00Z',
      endorsementCode: { endorsementCode: EndorsementCode.ENDORSED },
      endorsementComment: 'Looks good',
      approverName: 'Approver Y',
      approvedTimestamp: '2025-08-02T09:00:00Z',
      isApprovedInd: true,
      businessAreaComment: 'Original BAC',
      planFiscalStatusCode: { planFiscalStatusCode: 'DRAFT' },
    };

    component.projectFiscals = [existing];

    const form = component.createFiscalForm(existing);
    form.get('planFiscalStatusCode')!.setValue('PROPOSED'); // triggers isUpdateToDraft = false
    component.fiscalForms = [form];

    mockProjectService.updateProjectFiscal.and.returnValue(of({}));

    component.onSaveFiscal(0);

    const [, , payload] = mockProjectService.updateProjectFiscal.calls.mostRecent()
      .args as [string, string, ProjectFiscal];

    expect(payload.planFiscalStatusCode).toEqual({ planFiscalStatusCode: 'PROPOSED' });
    expect(payload.businessAreaComment).toBe('Original BAC');
    expect(payload.endorserName).toBe('Endorser X');
    expect(payload.endorsementTimestamp).toBe('2025-08-01T10:00:00Z');
    expect(payload.endorsementCode).toEqual({ endorsementCode: EndorsementCode.ENDORSED });
    expect(payload.endorsementComment).toBe('Looks good');
    expect(payload.approverName).toBe('Approver Y');
    expect(payload.approvedTimestamp).toBe('2025-08-02T09:00:00Z');
    expect(payload.isApprovedInd).toBeTrue();
    expect(payload.submissionTimestamp).toBe('2025-08-08T19:34:56.000Z');

    jasmine.clock().uninstall();
  });

  describe('isCurrentFiscalReadonly', () => {
    it('should return false if there is no current fiscal', () => {
      component.projectFiscals = [];
      component.selectedTabIndex = 0;

      expect(component.isCurrentFiscalReadonly()).toBeFalse();
    });

    it('should return true if status is COMPLETE', () => {
      component.projectFiscals = [
        { planFiscalStatusCode: { planFiscalStatusCode: component.FiscalStatuses.COMPLETE } }
      ];
      component.selectedTabIndex = 0;

      expect(component.isCurrentFiscalReadonly()).toBeTrue();
    });

    it('should return true if status is CANCELLED', () => {
      component.projectFiscals = [
        { planFiscalStatusCode: { planFiscalStatusCode: component.FiscalStatuses.CANCELLED } }
      ];
      component.selectedTabIndex = 0;

      expect(component.isCurrentFiscalReadonly()).toBeTrue();
    });

    it('should return false for other statuses (e.g., DRAFT)', () => {
      component.projectFiscals = [
        { planFiscalStatusCode: { planFiscalStatusCode: component.FiscalStatuses.DRAFT } }
      ];
      component.selectedTabIndex = 0;

      expect(component.isCurrentFiscalReadonly()).toBeFalse();
    });
  });


});
