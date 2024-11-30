import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { MatDialog, MatDialogRef } from '@angular/material/dialog';
import { of } from 'rxjs';
import { CreateNewProjectDialogComponent } from './create-new-project-dialog.component';
import { ConfirmationDialogComponent } from '../confirmation-dialog/confirmation-dialog.component';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { Messages } from 'src/app/utils/messages';


describe('CreateNewProjectDialogComponent', () => {
  let component: CreateNewProjectDialogComponent;
  let fixture: ComponentFixture<CreateNewProjectDialogComponent>;
  let mockDialog: jasmine.SpyObj<MatDialog>;
  let mockDialogRef: jasmine.SpyObj<MatDialogRef<CreateNewProjectDialogComponent>>;

  beforeEach(async () => {
    mockDialog = jasmine.createSpyObj('MatDialog', ['open']);
    mockDialogRef = jasmine.createSpyObj('MatDialogRef', ['close']);

    await TestBed.configureTestingModule({
      imports: [ReactiveFormsModule, CreateNewProjectDialogComponent, BrowserAnimationsModule ],
      providers: [
        { provide: MatDialog, useValue: mockDialog },
        { provide: MatDialogRef, useValue: mockDialogRef },
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
    component.projectForm.get('bcParksRegion')?.setValue('Northern');
    fixture.detectChanges();

    const bcParksSectionControl = component.projectForm.get('bcParksSection');
    expect(bcParksSectionControl?.enabled).toBeTrue();
    expect(component.bcParksSections).toEqual(['Omineca', 'Peace', 'Skeena']);
  });

  it('should reset and disable bcParksSection when no region is selected', () => {
    component.projectForm.get('bcParksRegion')?.setValue(null);
    fixture.detectChanges();

    const bcParksSectionControl = component.projectForm.get('bcParksSection');
    expect(bcParksSectionControl?.disabled).toBeTrue();
    expect(component.bcParksSections).toEqual([]);
  });

  it('should open confirmation dialog on cancel', () => {
    const mockAfterClosed = of(true);
    mockDialog.open.and.returnValue({
      afterClosed: () => mockAfterClosed,
    } as any);

    component.onCancel();
    expect(mockDialog.open).toHaveBeenCalledWith(ConfirmationDialogComponent, {
      data: { indicator: 'confirm-cancel' },
      width: '500px',
    });
  });

  it('should close the dialog if confirmation dialog returns true', () => {
    const mockAfterClosed = of(true);
    mockDialog.open.and.returnValue({
      afterClosed: () => mockAfterClosed,
    } as any);

    component.onCancel();
    expect(mockDialog.open).toHaveBeenCalled();
    mockAfterClosed.subscribe(() => {
      expect(mockDialogRef.close).toHaveBeenCalled();
    });
  });

  it('should not close the dialog if confirmation dialog returns false', () => {
    const mockAfterClosed = of(false);
    mockDialog.open.and.returnValue({
      afterClosed: () => mockAfterClosed,
    } as any);

    component.onCancel();
    expect(mockDialog.open).toHaveBeenCalled();
    mockAfterClosed.subscribe(() => {
      expect(mockDialogRef.close).not.toHaveBeenCalled();
    });
  });

  it('should close the dialog on successful form submission', () => {
    component.projectForm.patchValue({
      projectName: 'Test Project',
      latLong: '123.456',
      businessArea: 'Area 1',
      forestRegion: 'Region 1',
      forestDistrict: 'District 1',
      bcParksRegion: 'Northern',
      bcParksSection: 'Omineca',
      projectLead: 'John Doe',
      projectLeadEmail: 'john.doe@example.com',
      siteUnitName: 'Unit 1',
      closestCommunity: 'Community 1',
    });

    component.onCreate();
    expect(mockDialogRef.close).toHaveBeenCalledWith(component.projectForm.value);
  });

  it('should not close the dialog if the form is invalid', () => {
    component.projectForm.patchValue({
      projectName: '',
    });

    component.onCreate();
    expect(mockDialogRef.close).not.toHaveBeenCalled();
  });
  it('should return the correct error message for required fields', () => {
    component.projectForm.get('projectName')?.setErrors({ required: true });
    const errorMessage = component.getErrorMessage('projectName');
    expect(errorMessage).toBe(Messages.requiredField);
  });
  
  it('should return the correct error message for maxlength errors', () => {
    component.projectForm.get('projectName')?.setErrors({ maxlength: true });
    const errorMessage = component.getErrorMessage('projectName');
    expect(errorMessage).toBe(Messages.maxLengthExceeded);
  });
  
  it('should return the correct error message for email format errors', () => {
    component.projectForm.get('projectLeadEmail')?.setErrors({ email: true });
    const errorMessage = component.getErrorMessage('projectLeadEmail');
    expect(errorMessage).toBe(Messages.invalidEmail);
  });
  
  it('should dynamically display error messages in the template', () => {
    const projectNameControl = component.projectForm.get('projectName');
    projectNameControl?.setErrors({ maxlength: true });
    projectNameControl?.markAsTouched();
    fixture.detectChanges();
  
    const errorElement = fixture.nativeElement.querySelector('.form-field .error');
    expect(errorElement.textContent.trim()).toBe(Messages.maxLengthExceeded);
  });
  
  it('should show a snackbar message on successful form submission', () => {
    const mockSnackbar = spyOn(component['snackbarService'], 'open');
    component.projectForm.patchValue({
      projectName: 'Test Project',
      latLong: '123.456',
      businessArea: 'Area 1',
      forestRegion: 'Region 1',
      forestDistrict: 'District 1',
      bcParksRegion: 'Northern',
      bcParksSection: 'Omineca',
      projectLead: 'John Doe',
      projectLeadEmail: 'john.doe@example.com',
      siteUnitName: 'Unit 1',
      closestCommunity: 'Community 1',
    });
  
    component.onCreate();
  
    expect(mockSnackbar).toHaveBeenCalledWith(
      Messages.projectCreatedSuccess,
      'OK',
      { duration: 100000, panelClass: 'snackbar-success' }
    );
  });
});
