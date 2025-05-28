import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule, Validators } from '@angular/forms';
import { MatDialog, MatDialogRef } from '@angular/material/dialog';
import { of, throwError } from 'rxjs';
import { CreateNewProjectDialogComponent } from './create-new-project-dialog.component';
import { ConfirmationDialogComponent } from '../confirmation-dialog/confirmation-dialog.component';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { Messages } from 'src/app/utils/constants';
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
    mockCodeTableService = jasmine.createSpyObj('CodeTableServices', [
      'fetchCodeTable',
      'fetchFireCentres'
    ]);
    mockCodeTableService.fetchFireCentres.and.returnValue(of([{ name: 'Fire Centre 1' }]));

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
      projectType: '',
      projectName: '',
      latLong: '',
      businessArea: '',
      forestRegion: '',
      forestDistrict: '',
      bcParksRegion: '',
      bcParksSection: '',
      fireCentre: '',
      projectLead: '',
      projectLeadEmail: '',
      siteUnitName: '',
      closestCommunity: '',
      primaryObjective : '',
      secondaryObjective: '',
      secondaryObjectiveRationale: '',
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
    mockProjectService.createProject.and.returnValue(of({ projectGuid: '999999' }));
  
    // Populate the form with valid values
    component.projectForm.patchValue({
      projectType: 'FUEL_MGMT', // Required field
      projectName: 'New Project', // Required field
      businessArea: 'Area 1', // Required field
      forestRegion: 1, // Required field
      forestDistrict: 2, // Required field
      bcParksRegion: 3, // Required field
      bcParksSection: 4, // Required field
      fireCentre: 5,// Required field
      projectLead: 'John Doe', // Optional field
      projectLeadEmail: 'john.doe@example.com', // Optional field
      siteUnitName: 'Unit 1', // Optional field
      closestCommunity: 'Community 1', // Required field
      primaryObjective: 'WRR' // Required field
    });
  
    // Call the function to create a project
    component.onCreate();
  
    // Assertions
    expect(mockProjectService.createProject).toHaveBeenCalled(); // Ensure createProject was called
    expect(mockSnackbarService.open).toHaveBeenCalledWith(
      Messages.projectCreatedSuccess,
      'OK',
      { duration: 5000, panelClass: 'snackbar-success' }
    ); // Ensure snackbar was called
    expect(mockDialogRef.close).toHaveBeenCalledWith({ success: true, projectGuid: '999999' });
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

  it('should not create a new project if the form is invalid', () => {
    component.projectForm.get('projectName')?.setValue(''); // Invalid since it's required
  
    component.onCreate();
  
    expect(mockProjectService.createProject).not.toHaveBeenCalled();
    expect(mockSnackbarService.open).not.toHaveBeenCalled();
    expect(mockDialogRef.close).not.toHaveBeenCalled();
  });

  it('should update bcParksSections when a bcParksRegion is selected', () => {
    // Mock data for allBcParksSections
    component.allBcParksSections = [
      { parentOrgUnitId: '1', name: 'Section 1' },
      { parentOrgUnitId: '2', name: 'Section 2' },
    ];
  
    // Set bcParksRegion value to 1
    component.projectForm.get('bcParksRegion')?.setValue('1');
    fixture.detectChanges();
  
    // Check if bcParksSections is updated correctly
    expect(component.bcParksSections).toEqual([{ parentOrgUnitId: '1', name: 'Section 1' }]);
  
    // Set bcParksRegion value to 2
    component.projectForm.get('bcParksRegion')?.setValue('2');
    fixture.detectChanges();
  
    // Check if bcParksSections is updated correctly
    expect(component.bcParksSections).toEqual([{ parentOrgUnitId: '2', name: 'Section 2' }]);
  });

  it('should validate latLong and set latitude and longitude correctly', () => {
    component.projectForm.patchValue({
      projectType: 'FUEL_MGMT',
      projectName: 'New Project',
      latLong: '48.484245, -123.332177',
      businessArea: 'Area 1',
      forestRegion: 1,
      forestDistrict: 2,
      bcParksRegion: 3,
      bcParksSection: 4,
      fireCentre: 5,
      projectLead: 'John Doe',
      projectLeadEmail: 'john.doe@example.com',
      siteUnitName: 'Unit 1',
      closestCommunity: 'Community 1',
      primaryObjective: 'WRR'
    });
  
    mockProjectService.createProject.and.returnValue(of({}));
  
    component.onCreate();
  
    expect(mockProjectService.createProject).toHaveBeenCalledWith(
      jasmine.objectContaining({
        latitude: 48.484245,
        longitude: -123.332177,
      })
    );
  });
  
  it('should show an error when latLong is outside BC boundaries', () => {
    component.projectForm.patchValue({
      projectType: 'FUEL_MGMT',
      projectName: 'New Project',
      businessArea: 'Area 1',
      forestRegion: 1,
      forestDistrict: 2,
      bcParksRegion: 3,
      bcParksSection: 4,
      fireCentre: 5,
      projectLead: 'John Doe',
      projectLeadEmail: 'john.doe@example.com',
      siteUnitName: 'Unit 1',
      closestCommunity: 'Community 1',
      latLong: '70.123456, -123.332177', // Invalid latitude
      primaryObjective: 'WRR'
    });
  
    component.onCreate();
  
    expect(mockSnackbarService.open).toHaveBeenCalledWith(
      'Invalid latitude and longitude. Please ensure it is in the correct format and within BC boundaries.',
      'OK',
      { duration: 5000, panelClass: 'snackbar-error' }
    );
  
    expect(mockProjectService.createProject).not.toHaveBeenCalled();
  });
  

  it('should handle latLong with boundary values correctly', () => {
    component.projectForm.patchValue({
      projectType: 'FUEL_MGMT',
      projectName: 'New Project',
      businessArea: 'Area 1',
      forestRegion: 1,
      forestDistrict: 2,
      bcParksRegion: 3,
      bcParksSection: 4,
      fireCentre: 5,
      projectLead: 'John Doe',
      projectLeadEmail: 'john.doe@example.com',
      siteUnitName: 'Unit 1',
      closestCommunity: 'Community 1',
      latLong: '48.3, -139', // Boundary value for BC
      primaryObjective: 'WRR'
    });
  
    mockProjectService.createProject.and.returnValue(of({}));
  
    component.onCreate();
  
    const expectedLatitude = 48.3;
    const expectedLongitude = -139;
  
    expect(mockProjectService.createProject).toHaveBeenCalledWith(
      jasmine.objectContaining({
        latitude: expectedLatitude,
        longitude: expectedLongitude,
      })
    );
  });

  it('should show an error when latLong is in an invalid format', () => {
    component.projectForm.patchValue({
      projectType: 'FUEL_MGMT',
      projectName: 'New Project',
      businessArea: 'Area 1',
      forestRegion: 1,
      forestDistrict: 2,
      bcParksRegion: 3,
      bcParksSection: 4,
      fireCentre: 5,
      projectLead: 'John Doe',
      projectLeadEmail: 'john.doe@example.com',
      siteUnitName: 'Unit 1',
      closestCommunity: 'Community 1',
      latLong: 'invalid, format', // Invalid latLong format
      primaryObjective: 'WRR'
    });
  
    component.onCreate();
  
    expect(mockSnackbarService.open).toHaveBeenCalledWith(
      'Invalid latitude and longitude. Please ensure it is in the correct format and within BC boundaries.',
      'OK',
      { duration: 5000, panelClass: 'snackbar-error' }
    );
  
    expect(mockProjectService.createProject).not.toHaveBeenCalled();
  });
  
  it('should handle error while fetching code tables', () => {
    mockCodeTableService.fetchCodeTable.and.returnValue(throwError(() => new Error('Network error')));
  
    component.loadCodeTables();
  
    expect(mockCodeTableService.fetchCodeTable).toHaveBeenCalledWith('programAreaCodes');
    expect(mockCodeTableService.fetchCodeTable).toHaveBeenCalledWith('forestRegionCodes');
    // Add assertions for console.error or fallback logic
  });
  
  it('should close dialog on cancel confirmation', () => {
    const mockAfterClosed = of(true); // User confirmed
    mockDialog.open.and.returnValue({ afterClosed: () => mockAfterClosed } as any);
  
    component.onCancel();
  
    mockAfterClosed.subscribe(() => {
      expect(mockDialogRef.close).toHaveBeenCalled();
    });
  });

  it('should create project with only required fields', () => {
    component.projectForm.patchValue({
      projectType: 'FUEL_MGMT',
      projectName: 'Required Project',
      businessArea: 'Area 1',
      forestRegion: 1,
      forestDistrict: 2,
      bcParksRegion: 3,
      bcParksSection: 4,
      fireCentre: 5,
      closestCommunity: 'Community 1',
      primaryObjective: 'WRR',
      projectLead: 'Test Lead'
    });
  
    mockProjectService.createProject.and.returnValue(of({}));
  
    component.onCreate();
  
    expect(mockProjectService.createProject).toHaveBeenCalledWith(
      jasmine.objectContaining({
        projectName: 'Required Project',
      })
    );
  });

  it('should return null if there are no errors', () => {
    component.projectForm.get('projectName')?.setErrors(null);
  
    const errorMessage = component.getErrorMessage('projectName');
  
    expect(errorMessage).toBeNull();
  });

  it('should return null if there are no errors on the form control', () => {
    // Arrange: Ensure no errors are set for the 'projectName' control
    component.projectForm.get('projectName')?.setErrors(null); // Clear errors
    
    // Act: Call the getErrorMessage method
    const errorMessage = component.getErrorMessage('projectName');
    
    // Assert: Expect the method to return null
    expect(errorMessage).toBeNull();
  });
  
  it('should return required field message when "required" error exists', () => {
    component.projectForm.get('projectName')?.setErrors({ required: true });
  
    const errorMessage = component.getErrorMessage('projectName');
  
    expect(errorMessage).toBe(Messages.requiredField);
  });
  
  it('should return max length exceeded message when "maxlength" error exists', () => {
    component.projectForm.get('projectName')?.setErrors({ maxlength: true });
  
    const errorMessage = component.getErrorMessage('projectName');
  
    expect(errorMessage).toBe(Messages.maxLengthExceeded);
  });
  
  it('should return invalid email message when "email" error exists', () => {
    // Arrange: Set 'email' error on the control
    component.projectForm.get('projectLeadEmail')?.setErrors({ email: true });
  
    // Act: Call the getErrorMessage method
    const errorMessage = component.getErrorMessage('projectLeadEmail');
  
    // Assert: Expect the invalid email error message
    expect(errorMessage).toBe(Messages.invalidEmail);
  });

  it('should show an error snackbar if creating a project fails', () => {
    // Arrange: Simulate the createProject API call failing
    mockProjectService.createProject.and.returnValue(throwError(() => new Error('Failed to create project')));
  
    // Populate the form with valid values
    component.projectForm.patchValue({
      projectType: 'FUEL_MGMT',
      projectName: 'New Project',
      businessArea: 'Area 1',
      forestRegion: 1,
      forestDistrict: 2,
      bcParksRegion: 3,
      bcParksSection: 4,
      fireCentre: 5,
      projectLead: 'John Doe',
      projectLeadEmail: 'john.doe@example.com',
      siteUnitName: 'Unit 1',
      closestCommunity: 'Community 1',
      primaryObjective: 'WRR'
    });
  
    // Act: Call the method to create a project
    component.onCreate();
  
    // Assert
    expect(mockProjectService.createProject).toHaveBeenCalled(); // Ensure createProject was called
    expect(mockSnackbarService.open).toHaveBeenCalledWith(
      component.messages.projectCreatedFailure,
      'OK',
      { duration: 5000, panelClass: 'snackbar-error' }
    ); // Ensure the error snackbar was displayed
  });

  it('should set bcParksRegion as required when BC Parks business area is selected', () => {
    component.businessAreas = [
      { programAreaGuid: 'bcp-guid', programAreaName: 'BC Parks (BCP)' },
      { programAreaGuid: 'other-guid', programAreaName: 'Other Area' }
    ];
  
    component.projectForm.get('businessArea')?.setValue('bcp-guid');
    fixture.detectChanges();
  
    const bcParksRegionControl = component.projectForm.get('bcParksRegion');
    bcParksRegionControl?.setValue('some-region');
    bcParksRegionControl?.markAsTouched();
  
    expect(bcParksRegionControl?.hasError('required')).toBeFalse();
    bcParksRegionControl?.setValue('');
    fixture.detectChanges();
    expect(bcParksRegionControl?.hasError('required')).toBeTrue();
  });
  
  it('should clear bcParksRegion required validator when a non-BC Parks business area is selected', () => {
    component.businessAreas = [
      { programAreaGuid: 'bcp-guid', programAreaName: 'BC Parks (BCP)' },
      { programAreaGuid: 'non-bcp-guid', programAreaName: 'Other Area' }
    ];
  
    // Initially select BC Parks to trigger required
    component.projectForm.get('businessArea')?.setValue('bcp-guid');
    fixture.detectChanges();
  
    const bcParksRegionControl = component.projectForm.get('bcParksRegion');
    expect(bcParksRegionControl?.validator).toBeTruthy();
  
    // Switch to a non-BCP area
    component.projectForm.get('businessArea')?.setValue('non-bcp-guid');
    fixture.detectChanges();
  
    // Assert required validator is gone
    expect(bcParksRegionControl?.hasValidator?.(Validators.required)).toBeFalse();
    expect(bcParksRegionControl?.validator).toBeFalsy();
  });

  it('should show required error for fireCentre when empty', () => {
    const control = component.projectForm.get('fireCentre');
    control?.setValue('');
    control?.markAsTouched();
    control?.updateValueAndValidity();
  
    expect(control?.hasError('required')).toBeTrue();
  });

  it('should set default projectType to FUEL_MGMT when projectTypeCodes includes it', () => {
    const mockProjectTypeResponse = {
      _embedded: {
        projectTypeCode: [
          { projectTypeCode: 'FUEL_MGMT', name: 'Fuel Management' },
          { projectTypeCode: 'OTHER', name: 'Other' }
        ]
      }
    };
  
    mockCodeTableService.fetchCodeTable.and.callFake((name: string) => {
      if (name === 'projectTypeCodes') {
        return of(mockProjectTypeResponse);
      }
      return of({ _embedded: [] });
    });
  
    component.loadCodeTables();
    fixture.detectChanges();
  
    expect(component.projectForm.get('projectType')?.value).toBe('FUEL_MGMT');
  });

  it('should set default primaryObjective to WRR when objectiveTypeCodes includes it', () => {
    const mockObjectiveTypeResponse = {
      _embedded: {
        objectiveTypeCode: [
          { objectiveTypeCode: 'WRR', name: 'Watershed Restoration' },
          { objectiveTypeCode: 'OTHER', name: 'Other' }
        ]
      }
    };
  
    mockCodeTableService.fetchCodeTable.and.callFake((name: string) => {
      if (name === 'objectiveTypeCodes') {
        return of(mockObjectiveTypeResponse);
      }
      return of({ _embedded: [] });
    });
  
    component.loadCodeTables();
    fixture.detectChanges();
  
    expect(component.projectForm.get('primaryObjective')?.value).toBe('WRR');
  });

  it('should return project type description if found', () => {
    component.projectTypes = [{ projectTypeCode: 'FUEL', description: 'Fuel Management' }];
    expect(component.getProjectTypeCode('FUEL')).toBe('Fuel Management');
  });
  
  it('should return input if project type not found', () => {
    component.projectTypes = [];
    expect(component.getProjectTypeCode('UNKNOWN')).toBe('UNKNOWN');
  });

  it('should return business area name if found', () => {
    component.businessAreas = [{ programAreaGuid: '123', programAreaName: 'BC Parks (BCP)' }];
    expect(component.getBusinessAreaCode('123')).toBe('BC Parks (BCP)');
  });
  
  it('should return input if business area not found', () => {
    component.businessAreas = [];
    expect(component.getBusinessAreaCode('999')).toBe('999');
  });

  it('should return forest region name if found', () => {
    component.forestRegions = [{ orgUnitId: 1, orgUnitName: 'Region A' }];
    expect(component.getForestRegionCode('1')).toBe('Region A');
  });
  
  it('should return input if forest region not found', () => {
    component.forestRegions = [];
    expect(component.getForestRegionCode('2')).toBe('2');
  });

  it('should return forest district name if found', () => {
    component.forestDistricts = [{ orgUnitId: 1, orgUnitName: 'District A' }];
    expect(component.getForestDistrictCode('1')).toBe('District A');
  });
  
  it('should return input if forest district not found', () => {
    component.forestDistricts = [];
    expect(component.getForestDistrictCode('2')).toBe('2');
  });

  it('should return BC Parks region name if found', () => {
    component.bcParksRegions = [{ orgUnitId: 1, orgUnitName: 'Parks Region' }];
    expect(component.getBcParksRegionCode('1')).toBe('Parks Region');
  });

  it('should return BC Parks section name if found', () => {
    component.bcParksSections = [{ orgUnitId: 10, orgUnitName: 'Parks Section' }];
    expect(component.getBcParksSectionCode('10')).toBe('Parks Section');
  });
  
  it('should return fire centre name if found', () => {
    component.fireCentres = [{
      properties: { MOF_FIRE_CENTRE_ID: 5, MOF_FIRE_CENTRE_NAME: 'Kamloops Fire Centre' }
    }];
    expect(component.getFireCentreCode('5')).toBe('Kamloops Fire Centre');
  });
  
  it('should return input if fire centre not found', () => {
    component.fireCentres = [];
    expect(component.getFireCentreCode('999')).toBe('999');
  });
  
  it('should return objective description if found', () => {
    component.objectiveTypes = [{ objectiveTypeCode: 'WRR', description: 'Wildfire Risk Reduction' }];
    expect(component.getObjectiveCode('WRR')).toBe('Wildfire Risk Reduction');
  });
  
});
