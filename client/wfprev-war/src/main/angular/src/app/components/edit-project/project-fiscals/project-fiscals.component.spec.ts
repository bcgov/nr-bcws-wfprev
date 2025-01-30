import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ProjectFiscalsComponent } from './project-fiscals.component';
import { ActivatedRoute } from '@angular/router';
import { of, throwError } from 'rxjs';
import { ProjectService } from 'src/app/services/project-services';
import { CodeTableServices } from 'src/app/services/code-table-services';
import { MatSnackBar } from '@angular/material/snack-bar';
import { FormBuilder } from '@angular/forms';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';

const mockProjectService = {
  getProjectFiscalsByProjectGuid: jasmine.createSpy('getProjectFiscalsByProjectGuid').and.returnValue(
    of({ _embedded: { projectFiscals: [{ fiscalYear: 2023, projectFiscalName: 'Test Fiscal' }] } })
  ),
  updateProjectFiscal: jasmine.createSpy('updateProjectFiscal').and.returnValue(of({})),
  createProjectFiscal: jasmine.createSpy('createProjectFiscal').and.returnValue(of({})),
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

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ProjectFiscalsComponent, BrowserAnimationsModule],
      providers: [
        { provide: ActivatedRoute, useValue: { snapshot: { queryParamMap: { get: () => 'test-guid' } } } },
        { provide: ProjectService, useValue: mockProjectService },
        { provide: CodeTableServices, useValue: mockCodeTableServices },
        { provide: MatSnackBar, useValue: mockSnackBar },
        FormBuilder,
      ],
    }).compileComponents();

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
});
