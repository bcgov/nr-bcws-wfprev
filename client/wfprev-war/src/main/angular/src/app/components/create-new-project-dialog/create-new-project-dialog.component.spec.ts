import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { MatDialog, MatDialogRef } from '@angular/material/dialog';
import { of, throwError } from 'rxjs';
import { CreateNewProjectDialogComponent } from './create-new-project-dialog.component';
import { ConfirmationDialogComponent } from '../confirmation-dialog/confirmation-dialog.component';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { Messages } from 'src/app/utils/messages';
import { ProjectService } from 'src/app/services/project-services';
import { CodeTableServices } from 'src/app/services/code-table-services';
import { MatSnackBar } from '@angular/material/snack-bar';

describe('CreateNewProjectDialogComponent', () => {
  let component: CreateNewProjectDialogComponent;
  let fixture: ComponentFixture<CreateNewProjectDialogComponent>;
  let mockDialog: jasmine.SpyObj<MatDialog>;
  let mockDialogRef: jasmine.SpyObj<MatDialogRef<CreateNewProjectDialogComponent>>;
  let mockProjectService: jasmine.SpyObj<ProjectService>;
  let mockCodeTableService: jasmine.SpyObj<CodeTableServices>;
  let mockSnackbarService: jasmine.SpyObj<MatSnackBar>;

  beforeEach(async () => {
    mockDialog = jasmine.createSpyObj('MatDialog', ['open']);
    mockDialogRef = jasmine.createSpyObj('MatDialogRef', ['close']);
    mockProjectService = jasmine.createSpyObj('ProjectService', ['createProject']);
    mockCodeTableService = jasmine.createSpyObj('CodeTableServices', ['fetchCodeTable']);
    mockSnackbarService = jasmine.createSpyObj('MatSnackBar', ['open']);

    mockCodeTableService.fetchCodeTable.and.callFake((name: string) => {
      switch (name) {
        case 'programAreaCodes':
          return of({ _embedded: { programArea: [{ name: 'Program Area 1' }] } });
        case 'forestRegionCodes':
          return of({ _embedded: { forestRegion: [{ name: 'Forest Region 1' }] } });
        case 'bcParksRegionCodes':
          return of({ _embedded: { bcParksRegionCode: [{ name: 'BC Parks Region 1' }] } });
        case 'bcParksSectionCodes':
          return of({ _embedded: { bcParksSectionCode: [{ parentOrgUnitId: 1, name: 'BC Section 1' }] } });
        default:
          return of({ _embedded: [] });
      }
    });

    await TestBed.configureTestingModule({
      imports: [ReactiveFormsModule, CreateNewProjectDialogComponent, BrowserAnimationsModule],
      providers: [
        { provide: MatDialog, useValue: mockDialog },
        { provide: MatDialogRef, useValue: mockDialogRef },
        { provide: ProjectService, useValue: mockProjectService },
        { provide: CodeTableServices, useValue: mockCodeTableService },
        { provide: MatSnackBar, useValue: mockSnackbarService },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(CreateNewProjectDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize the form with default values', () => {
    const formValues = component.projectForm.getRawValue();
    expect(formValues).toEqual({
      projectName: '',
      latLong: '',
      businessArea: '',
      forestRegion: '',
      forestDistrict: '',
      bcParksRegion: '',
      bcParksSection: '',
      projectLead: '',
      projectLeadEmail: '',
      siteUnitName: '',
      closestCommunity: '',
    });
  });

  it('should disable bcParksSection by default', () => {
    const bcParksSectionControl = component.projectForm.get('bcParksSection');
    expect(bcParksSectionControl?.disabled).toBeTrue();
  });

  it('should enable bcParksSection when a region is selected', () => {
    component.projectForm.get('bcParksRegion')?.setValue(1);
    fixture.detectChanges();

    expect(component.projectForm.get('bcParksSection')?.enabled).toBeTrue();
  });

  it('should reset and disable bcParksSection when no region is selected', () => {
    component.projectForm.get('bcParksRegion')?.setValue(null); // Simulate no region selected
    fixture.detectChanges();

    expect(component.projectForm.get('bcParksSection')?.disabled).toBeTrue();
    expect(component.bcParksSections).toEqual([]);
  });

  it('should fetch and set code tables on initialization', () => {
    // Mocking the responses for fetchCodeTable
    mockCodeTableService.fetchCodeTable.and.callFake((name: string) => {
      switch (name) {
        case 'programAreaCodes':
          return of({ _embedded: { programArea: [{ name: 'Program Area 1' }] } });
        case 'forestRegionCodes':
          return of({ _embedded: { forestRegionCode: [{ name: 'Forest Region 1' }] } });
        default:
          return of({ _embedded: [] });
      }
    });
  
    // Trigger the loadCodeTables method
    component.loadCodeTables();
    fixture.detectChanges();
  
    // Verify that the service was called with correct table names
    expect(mockCodeTableService.fetchCodeTable).toHaveBeenCalledWith('programAreaCodes');
    expect(mockCodeTableService.fetchCodeTable).toHaveBeenCalledWith('forestRegionCodes');
  
    // Verify the component's state is updated correctly
    expect(component.businessAreas).toEqual([{ name: 'Program Area 1' }]);
    expect(component.forestRegions).toEqual([{ name: 'Forest Region 1' }]);
  });
  

  it('should display correct error messages', () => {
    component.projectForm.get('projectName')?.setErrors({ required: true });
    expect(component.getErrorMessage('projectName')).toBe(Messages.requiredField);

    component.projectForm.get('projectLeadEmail')?.setErrors({ email: true });
    expect(component.getErrorMessage('projectLeadEmail')).toBe(Messages.invalidEmail);
  });

  it('should create a new project and close dialog on success', () => {
    // Mock createProject to simulate a successful API response
    mockProjectService.createProject.and.returnValue(of({}));
  
    // Populate the form with valid values
    component.projectForm.patchValue({
      projectName: 'New Project', // Required field
      businessArea: 'Area 1', // Required field
      forestRegion: 1, // Required field
      forestDistrict: 2, // Required field
      bcParksRegion: 3, // Required field
      bcParksSection: 4, // Required field
      projectLead: 'John Doe', // Optional field
      projectLeadEmail: 'john.doe@example.com', // Optional field
      siteUnitName: 'Unit 1', // Optional field
      closestCommunity: 'Community 1', // Required field
    });
  
    // Call the function to create a project
    component.onCreate();
  
    // Assertions
    expect(mockProjectService.createProject).toHaveBeenCalled(); // Ensure createProject was called
    expect(mockSnackbarService.open).toHaveBeenCalledWith(
      Messages.projectCreatedSuccess,
      'OK',
      { duration: 100000, panelClass: 'snackbar-success' }
    ); // Ensure snackbar was called
    expect(mockDialogRef.close).toHaveBeenCalledWith({ success: true }); // Ensure the dialog was closed
  });
  
  // Future task
  // it('should handle duplicate project error during creation', () => {
  //   mockProjectService.createProject.and.returnValue(
  //     throwError({ status: 500, error: { message: 'duplicate' } })
  //   );

  //   component.onCreate();

  //   expect(mockDialog.open).toHaveBeenCalledWith(ConfirmationDialogComponent, {
  //     data: { indicator: 'duplicate-project', projectName: '' },
  //     width: '500px',
  //   });
  // });

  it('should open confirmation dialog on cancel', () => {
    const mockAfterClosed = of(true);
    mockDialog.open.and.returnValue({ afterClosed: () => mockAfterClosed } as any);

    component.onCancel();

    expect(mockDialog.open).toHaveBeenCalledWith(ConfirmationDialogComponent, {
      data: { indicator: 'confirm-cancel' },
      width: '500px',
    });

    mockAfterClosed.subscribe(() => {
      expect(mockDialogRef.close).toHaveBeenCalled();
    });
  });

  it('should not close dialog if cancel confirmation returns false', () => {
    const mockAfterClosed = of(false);
    mockDialog.open.and.returnValue({ afterClosed: () => mockAfterClosed } as any);

    component.onCancel();

    expect(mockDialog.open).toHaveBeenCalled();
    mockAfterClosed.subscribe(() => {
      expect(mockDialogRef.close).not.toHaveBeenCalled();
    });
  });
});
