import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ProjectFiscalsComponent } from './project-fiscals.component';
import { ActivatedRoute } from '@angular/router';
import { Observable, of, throwError } from 'rxjs';
import { ProjectService } from 'src/app/services/project-services';
import { CodeTableServices } from 'src/app/services/code-table-services';
import { MatSnackBar } from '@angular/material/snack-bar';
import { FormBuilder, FormGroup } from '@angular/forms';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { ConfirmationDialogComponent } from 'src/app/components/confirmation-dialog/confirmation-dialog.component';
import { Component } from '@angular/core';

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
    selector: 'app-fiscal-map',
    template: '<div></div>'
  })
  class MockFiscalMapComponent {
    ngOnInit() {}
    ngAfterViewInit() {}
    initMap() {}
  }
  @Component({
    selector: 'app-activities',
    template: ''
  })
  class MockActivitiesComponent {
    isFormDirty = () => false;
  }
  
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [BrowserAnimationsModule],
      declarations: [MockFiscalMapComponent, MockActivitiesComponent],
      providers: [
        { provide: ActivatedRoute, useValue: { snapshot: { queryParamMap: { get: () => 'test-guid' } } } },
        { provide: ProjectService, useValue: mockProjectService },
        { provide: CodeTableServices, useValue: mockCodeTableServices },
        { provide: MatSnackBar, useValue: mockSnackBar },
        FormBuilder,
      ],
    })
    .overrideComponent(ProjectFiscalsComponent, {
      set: {
        imports: []  // Remove the real FiscalMapComponent and ActivitiesComponent
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

  it('should generate fiscal years correctly', () => {
    component.generateFiscalYears();
    expect(component.fiscalYears.length).toBe(11);
    expect(component.fiscalYears[0]).toBe(`${new Date().getFullYear() - 5}/${(new Date().getFullYear() - 4).toString().slice(-2)}`);
  });

  it('should load code tables successfully', () => {
    mockCodeTableServices.fetchCodeTable.calls.reset(); // ✅ Reset call count
    component.loadCodeTables();
    expect(mockCodeTableServices.fetchCodeTable).toHaveBeenCalledTimes(3);
  });

  it('should handle errors in loading code tables', () => {
    mockCodeTableServices.fetchCodeTable.calls.reset(); // ✅ Reset call count
    mockCodeTableServices.fetchCodeTable.and.returnValue(throwError(() => new Error('Error fetching data')));
    component.loadCodeTables();
    expect(mockCodeTableServices.fetchCodeTable).toHaveBeenCalledTimes(3);
  });

  it('should load project fiscals', () => {
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
  
    expect(mockProjectService.getProjectFiscalsByProjectGuid).toHaveBeenCalledWith('test-guid');
    expect(component.projectFiscals.length).toBeGreaterThan(0); // ✅ Should now have at least one fiscal
    expect(component.fiscalForms.length).toBe(component.projectFiscals.length); // ✅ Forms should match project fiscals count
  });
  

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
  
    // Ensure mock `createProjectFiscal` returns success
    mockProjectService.createProjectFiscal.and.returnValue(of({})); // ✅ Fix: Return success response
  
    component.onSaveFiscal(0);
  
    expect(mockProjectService.createProjectFiscal).toHaveBeenCalled();
    expect(mockSnackBar.open).toHaveBeenCalledWith(
      component.messages.projectFiscalCreatedSuccess,
      'OK',
      { duration: 5000, panelClass: 'snackbar-success' } // ✅ Ensure correct snackbar message
    );
    expect(component.loadProjectFiscals).toHaveBeenCalled();
  });

  it('should handle errors when saving a new fiscal', () => {
    mockProjectService.createProjectFiscal.and.returnValue(throwError(() => new Error('API Error')));
    component.onSaveFiscal(0);
    expect(mockSnackBar.open).toHaveBeenCalledWith(component.messages.projectFiscalCreatedFailure, 'OK', {
      duration: 5000,
      panelClass: 'snackbar-error',
    });
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
    expect(component.loadProjectFiscals).toHaveBeenCalled();
  });
  

  it('should handle errors when updating an existing fiscal', () => {
    // ✅ Ensure projectFiscals is initialized before setting properties
    component.projectFiscals = [{ projectPlanFiscalGuid: 'existing-guid' }];
  
    mockProjectService.updateProjectFiscal.and.returnValue(throwError(() => new Error('API Error')));
  
    component.onSaveFiscal(0);
  
    expect(mockSnackBar.open).toHaveBeenCalledWith(
      component.messages.projectFiscalUpdatedFailure,
      'OK',
      { duration: 5000, panelClass: 'snackbar-error' } // ✅ Ensure correct error message is tested
    );
  });

  it('should clear all fields for a new fiscal entry on cancel', () => {
    component.projectFiscals = [{ fiscalYear: '', projectFiscalName: '', projectGuid: 'test-guid' }];
    component.fiscalForms = [component.createFiscalForm(component.projectFiscals[0])];

    spyOn(component.fiscalForms[0], 'reset');

    component.onCancelFiscal(0);

    expect(component.fiscalForms[0].reset).toHaveBeenCalled();
  });

  it('should revert to original values for an existing fiscal entry on cancel', () => {
    component.projectFiscals = [{ projectPlanFiscalGuid: 'existing-guid', fiscalYear: 2023, projectFiscalName: 'Test Fiscal' }];
    component.originalFiscalValues = [{ projectPlanFiscalGuid: 'existing-guid', fiscalYear: 2023, projectFiscalName: 'Test Fiscal (Original)' }];
    component.fiscalForms = [component.createFiscalForm(component.projectFiscals[0])];

    spyOn(component.fiscalForms[0], 'patchValue');
    spyOn(component.fiscalForms[0], 'markAsPristine');
    spyOn(component.fiscalForms[0], 'markAsUntouched');

    component.onCancelFiscal(0);

    expect(component.fiscalForms[0].patchValue).toHaveBeenCalledWith(component.originalFiscalValues[0]);
    expect(component.fiscalForms[0].markAsPristine).toHaveBeenCalled();
    expect(component.fiscalForms[0].markAsUntouched).toHaveBeenCalled();
  });

  it('should not fail if onCancelFiscal() is called with an invalid index', () => {
    component.projectFiscals = [];
    component.fiscalForms = [];

    expect(() => component.onCancelFiscal(0)).not.toThrow();
  });

  it('should open confirmation dialog when deleting a fiscal year', () => {
    spyOn(component.dialog, 'open').and.returnValue({
      afterClosed: () => of(true) // Simulate user clicking "Confirm"
    } as any);
  
    component.projectFiscals = [{ projectPlanFiscalGuid: 'test-guid' }];
    component.deleteFiscalYear({ value: component.projectFiscals[0] },0);
  
    expect(component.dialog.open).toHaveBeenCalledWith(ConfirmationDialogComponent, {
      data: { indicator: 'confirm-delete' },
      width: '500px',
    });
  });
  
  it('should delete a fiscal year after confirmation', () => {
    spyOn(component.dialog, 'open').and.returnValue({
      afterClosed: () => of(true) // Simulate user clicking "Confirm"
    } as any);
    spyOn(component, 'loadProjectFiscals');
  
    mockProjectService.deleteProjectFiscalByProjectPlanFiscalGuid = jasmine.createSpy().and.returnValue(of({}));
  
    component.projectFiscals = [{ projectPlanFiscalGuid: 'test-guid' }];
    component.deleteFiscalYear({ value: component.projectFiscals[0]},0);
  
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
    component.deleteFiscalYear({ value: component.projectFiscals[0] },0);
  
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
          data: { indicator: 'confirm-unsave' },
          width: '500px',
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

  it('should sort fiscalYears, activityCategoryCode, planFiscalStatusCode, and ancillaryFundingSourceCode in loadDropdownOptions', () => {
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
    component.ancillaryFundingSourceCode = [
      { fundingSourceName: 'Funding Z' },
      { fundingSourceName: 'Funding X' },
      { fundingSourceName: 'Funding Y' }
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
    expect(component.ancillaryFundingSourceCode).toEqual([
      { fundingSourceName: 'Funding X' },
      { fundingSourceName: 'Funding Y' },
      { fundingSourceName: 'Funding Z' }
    ]);
  });

  it('should not throw error if loadDropdownOptions is called with empty arrays', () => {
    component.fiscalYears = [];
    component.activityCategoryCode = [];
    component.planFiscalStatusCode = [];
    component.ancillaryFundingSourceCode = [];
  
    expect(() => component.loadDropdownOptions()).not.toThrow();
    expect(component.fiscalYears).toEqual([]);
    expect(component.activityCategoryCode).toEqual([]);
    expect(component.planFiscalStatusCode).toEqual([]);
    expect(component.ancillaryFundingSourceCode).toEqual([]);
  });

  it('should handle null or undefined values in loadDropdownOptions without errors', () => {
    component.fiscalYears = null as any;
    component.activityCategoryCode = null as any;
    component.planFiscalStatusCode = null as any;
    component.ancillaryFundingSourceCode = null as any;
  
    expect(() => component.loadDropdownOptions()).not.toThrow();
    expect(component.fiscalYears).toEqual([]);
    expect(component.activityCategoryCode).toEqual([]);
    expect(component.planFiscalStatusCode).toEqual([]);
    expect(component.ancillaryFundingSourceCode).toEqual([]);
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
});
