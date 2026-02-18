import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { TokenService } from 'src/app/services/token.service';
import { AddAttachmentComponent } from './add-attachment.component';

describe('AddAttachmentComponent', () => {
  let component: AddAttachmentComponent;
  let fixture: ComponentFixture<AddAttachmentComponent>;
  let mockDialogRef: jasmine.SpyObj<MatDialogRef<AddAttachmentComponent>>;
  let mockTokenService: jasmine.SpyObj<TokenService>;

  beforeEach(async () => {
    mockDialogRef = jasmine.createSpyObj('MatDialogRef', ['close']);
    mockTokenService = jasmine.createSpyObj('TokenService', ['getToken']);

    await TestBed.configureTestingModule({
      imports: [AddAttachmentComponent],
      providers: [
        { provide: MatDialogRef, useValue: mockDialogRef },
        { provide: TokenService, useValue: mockTokenService },
        { provide: MAT_DIALOG_DATA, useValue: { indicator: 'project-files', name: 'Test Name' } },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(AddAttachmentComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize with default values', () => {
    expect(component.selectedFile).toBeNull();
    expect(component.selectedFileName).toBe('');
    expect(component.attachmentType).toBe('MAP');
    expect(component.description).toBe('');
    expect(component.attachmentTypes).toEqual([
      { label: 'Gross Project Area Boundary', value: 'MAP' }
    ]);
    expect(component.isDescriptionTooLong).toBeFalse();
  });

  it('should handle file selection correctly', () => {
    const mockFile = new File(['dummy content'], 'test-file.txt', { type: 'text/plain' });
    const event = { target: { files: [mockFile] } } as any;

    component.onFileSelected(event);

    expect(component.selectedFile).toBe(mockFile);
    expect(component.selectedFileName).toBe('test-file.txt');
  });

  it('should check description length and set flag when exceeding 150 characters', () => {
    component.description = 'a'.repeat(151);
    component.checkDescriptionLength();
    expect(component.isDescriptionTooLong).toBeTrue();

    component.description = 'a'.repeat(150);
    component.checkDescriptionLength();
    expect(component.isDescriptionTooLong).toBeFalse();
  });

  it('should return false if form is invalid', () => {
    component.selectedFileName = '';
    component.attachmentType = '';
    component.description = '';

    expect(component.isFormValid()).toBeFalse();

    component.selectedFileName = 'file.txt';
    expect(component.isFormValid()).toBeFalse(); // Missing attachmentType and description

    component.attachmentType = 'Other';
    component.description = 'Short description';
    expect(component.isFormValid()).toBeTrue();
  });

  it('should close dialog with false on goBack()', () => {
    component.onGoBack();
    expect(mockDialogRef.close).toHaveBeenCalledWith(false);
  });

  it('should close dialog with file data on confirm if form is valid', () => {
    const mockFile = new File(['content'], 'test-file.txt', { type: 'text/plain' });
    component.selectedFile = mockFile;
    component.selectedFileName = 'test-file.txt';
    component.attachmentType = 'Other';
    component.description = 'Valid description';

    component.onConfirm();

    expect(mockDialogRef.close).toHaveBeenCalledWith({
      file: mockFile,
      filename: 'test-file.txt',
      type: 'Other',
      description: 'Valid description',
    });
  });

  it('should not close dialog if form is invalid on confirm', () => {
    component.selectedFile = null;
    component.selectedFileName = '';
    component.attachmentType = '';
    component.description = '';

    component.onConfirm();

    expect(mockDialogRef.close).not.toHaveBeenCalled();
  });

  it('should simulate clicking file input on chooseFile()', () => {
    const fileInput = document.createElement('input');
    fileInput.id = 'fileInput';
    spyOn(document, 'getElementById').and.returnValue(fileInput);
    spyOn(fileInput, 'click');

    component.chooseFile();

    expect(fileInput.click).toHaveBeenCalled();
  });

  it('should return "No File Chosen" if no file is selected', () => {
    expect(component.getFileName()).toBe('No File Chosen');
  });

  it('should return selected file name if a file is chosen', () => {
    component.selectedFileName = 'test-file.txt';
    expect(component.getFileName()).toBe('test-file.txt');
  });

  it('should return correct file types for "Gross Project Area Boundary"', () => {
    component.attachmentType = 'MAP';
    const result = component.getAcceptedFileTypes();
    expect(result).toBe('.kml,.kmz,.shp,.gdb,.zip');
  });

  it('should return correct file types for "Other"', () => {
    component.attachmentType = 'Other';
    const result = component.getAcceptedFileTypes();
    expect(result).toBe('');
  });

  it('should return an empty string for unknown attachmentType', () => {
    component.attachmentType = 'Unknown';
    const result = component.getAcceptedFileTypes();
    expect(result).toBe('');
  });
  it('should initialize attachment types for activity-files', async () => {
    fixture.destroy();
    await TestBed.resetTestingModule().configureTestingModule({
      imports: [AddAttachmentComponent],
      providers: [
        { provide: MatDialogRef, useValue: mockDialogRef },
        { provide: TokenService, useValue: mockTokenService },
        { provide: MAT_DIALOG_DATA, useValue: { indicator: 'activity-files', name: 'Activity' } },
      ],
    }).compileComponents();

    const newFixture = TestBed.createComponent(AddAttachmentComponent);
    const newComponent = newFixture.componentInstance;
    newFixture.detectChanges();

    expect(newComponent.attachmentTypes).toEqual([
      { label: 'Activity Polygon', value: 'MAP' },
      { label: 'Other', value: 'OTHER' },
      { label: 'Prescription', value: 'DOCUMENT' },
    ]);
    expect(newComponent.attachmentType).toBe('MAP');
  });

  it('should return empty accepted file types if attachmentType is empty', () => {
    component.attachmentType = '';
    const result = component.getAcceptedFileTypes();
    expect(result).toBe('');
  });

  it('should not confirm if description exceeds 150 characters', () => {
    const mockFile = new File(['content'], 'file.txt');
    component.selectedFile = mockFile;
    component.selectedFileName = 'file.txt';
    component.attachmentType = 'MAP';
    component.description = 'a'.repeat(151); // invalid length of text

    component.onConfirm();

    expect(mockDialogRef.close).not.toHaveBeenCalled();
  });



});
