import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { MatDialog, MatDialogRef } from '@angular/material/dialog';
import { of } from 'rxjs';
import { CreateNewProjectDialogComponent } from './create-new-project-dialog.component';
import { ConfirmationDialogComponent } from '../confirmation-dialog/confirmation-dialog.component';

describe('CreateNewProjectDialogComponent', () => {
  let component: CreateNewProjectDialogComponent;
  let fixture: ComponentFixture<CreateNewProjectDialogComponent>;
  let mockDialog: jasmine.SpyObj<MatDialog>;
  let mockDialogRef: jasmine.SpyObj<MatDialogRef<CreateNewProjectDialogComponent>>;

  beforeEach(async () => {
    mockDialog = jasmine.createSpyObj('MatDialog', ['open']);
    mockDialogRef = jasmine.createSpyObj('MatDialogRef', ['close']);

    await TestBed.configureTestingModule({
      imports: [ReactiveFormsModule, CreateNewProjectDialogComponent],
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
    const formValues = component.projectForm.value;
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
});
