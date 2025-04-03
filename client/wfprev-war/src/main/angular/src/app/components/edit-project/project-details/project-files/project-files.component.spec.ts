import { ChangeDetectorRef } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { of, throwError } from 'rxjs';
import { AddAttachmentComponent } from 'src/app/components/add-attachment/add-attachment.component';
import { ConfirmationDialogComponent } from 'src/app/components/confirmation-dialog/confirmation-dialog.component';
import { ProjectFile } from 'src/app/components/models';
import { AttachmentService } from 'src/app/services/attachment-service';
import { ProjectService } from 'src/app/services/project-services';
import { SpatialService } from 'src/app/services/spatial-services';
import { ProjectFilesComponent } from './project-files.component';

describe('ProjectFilesComponent', () => {
  let component: ProjectFilesComponent;
  let fixture: ComponentFixture<ProjectFilesComponent>;
  let mockProjectService: jasmine.SpyObj<ProjectService>;
  let mockSnackbar: jasmine.SpyObj<MatSnackBar>;
  let mockDialog: jasmine.SpyObj<MatDialog>;
  let mockAttachmentService: jasmine.SpyObj<AttachmentService>;
  let mockSpatialService: jasmine.SpyObj<SpatialService>;
  let mockCdr: jasmine.SpyObj<ChangeDetectorRef>;

  const mockProjectGuid = 'test-project-guid';

  beforeEach(async () => {
    mockProjectService = jasmine.createSpyObj('ProjectService', [
      'uploadDocument', 
      'getProjectBoundaries', 
      'createProjectBoundary'
    ]);
    mockSnackbar = jasmine.createSpyObj('MatSnackBar', ['open']);
    mockDialog = jasmine.createSpyObj('MatDialog', ['open']);
    mockAttachmentService = jasmine.createSpyObj('AttachmentService', [
      'createProjectAttachment', 
      'getProjectAttachments',
      'deleteProjectAttachment'
    ]);
    mockSpatialService = jasmine.createSpyObj('SpatialService', ['extractCoordinates']);
    mockCdr = jasmine.createSpyObj('ChangeDetectorRef', ['detectChanges']);

    // Setup default mock return values to prevent subscribe errors
    mockAttachmentService.getProjectAttachments.and.returnValue(of({ _embedded: {} }));
    mockProjectService.getProjectBoundaries.and.returnValue(of({ _embedded: {} }));
    mockSpatialService.extractCoordinates.and.returnValue(Promise.resolve([]));
    mockAttachmentService.createProjectAttachment.and.returnValue(of({}));
    mockAttachmentService.deleteProjectAttachment.and.returnValue(of({}));
    mockProjectService.createProjectBoundary.and.returnValue(of({}));
    mockProjectService.uploadDocument.and.returnValue(of({}));

    await TestBed.configureTestingModule({
      imports: [],
      providers: [
        { provide: ProjectService, useValue: mockProjectService },
        { provide: MatSnackBar, useValue: mockSnackbar },
        { provide: MatDialog, useValue: mockDialog },
        { provide: AttachmentService, useValue: mockAttachmentService },
        { provide: SpatialService, useValue: mockSpatialService },
        { provide: ChangeDetectorRef, useValue: mockCdr },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(ProjectFilesComponent);
    component = fixture.componentInstance;
    component.projectGuid = mockProjectGuid;
  });

  it('should create the component', () => {
    fixture.detectChanges();
    expect(component).toBeTruthy();
  });

  it('should have predefined columns', () => {
    expect(component.displayedColumns).toEqual([
      'attachmentType',
      'fileName',
      'fileType',
      'uploadedBy',
      'uploadedDate',
      'polygonHectares',
      'description',
      'download',
      'delete'
    ]);
  });

  describe('ngOnInit', () => {
    it('should call loadProjectAttachments on initialization', () => {
      spyOn(component, 'loadProjectAttachments');
      component.ngOnInit();
      expect(component.loadProjectAttachments).toHaveBeenCalled();
    });
  });

  describe('loadProjectAttachments', () => {
    it('should load project attachments successfully with boundary data', () => {
      const mockAttachmentResponse = {
        _embedded: {
          fileAttachment: [{ fileName: 'test.txt' }]
        }
      };

      const mockBoundaryResponse = {
        _embedded: {
          projectBoundary: [{ boundarySizeHa: 100 }]
        }
      };

      mockAttachmentService.getProjectAttachments.and.returnValue(of(mockAttachmentResponse));
      mockProjectService.getProjectBoundaries.and.returnValue(of(mockBoundaryResponse));

      component.loadProjectAttachments();

      expect(mockAttachmentService.getProjectAttachments).toHaveBeenCalledWith(mockProjectGuid);
      expect(mockProjectService.getProjectBoundaries).toHaveBeenCalledWith(mockProjectGuid);
    });

    it('should handle missing boundary data', () => {
      const mockAttachmentResponse = {
        _embedded: {
          fileAttachment: [{ fileName: 'test.txt' }]
        }
      };

      const mockBoundaryResponse = {
        _embedded: {
          projectBoundary: []
        }
      };

      mockAttachmentService.getProjectAttachments.and.returnValue(of(mockAttachmentResponse));
      mockProjectService.getProjectBoundaries.and.returnValue(of(mockBoundaryResponse));

      component.loadProjectAttachments();

      expect(mockAttachmentService.getProjectAttachments).toHaveBeenCalledWith(mockProjectGuid);
      expect(mockProjectService.getProjectBoundaries).toHaveBeenCalledWith(mockProjectGuid);
    });

    it('should handle missing fileAttachment array', () => {
      const mockAttachmentResponse = {
        _embedded: {}
      };

      mockAttachmentService.getProjectAttachments.and.returnValue(of(mockAttachmentResponse));

      component.loadProjectAttachments();

      expect(mockAttachmentService.getProjectAttachments).toHaveBeenCalledWith(mockProjectGuid);
    });

    it('should show error snackbar on attachment service error', () => {
      mockAttachmentService.getProjectAttachments.and.returnValue(
        throwError(() => new Error('Failed to load attachments'))
      );

      component.loadProjectAttachments();

      expect(mockAttachmentService.getProjectAttachments).toHaveBeenCalledWith(mockProjectGuid);
      expect(mockSnackbar.open).toHaveBeenCalledWith(
        'Failed to load project attachments.',
        'Close',
        jasmine.any(Object)
      );
    });

    it('should handle error when fetching project boundaries', () => {
      const mockAttachmentResponse = {
        _embedded: {
          fileAttachment: [{ fileName: 'test.txt' }]
        }
      };

      mockAttachmentService.getProjectAttachments.and.returnValue(of(mockAttachmentResponse));
      mockProjectService.getProjectBoundaries.and.returnValue(
        throwError(() => new Error('Failed to load boundaries'))
      );

      spyOn(console, 'error');
      component.loadProjectAttachments();

      expect(mockAttachmentService.getProjectAttachments).toHaveBeenCalledWith(mockProjectGuid);
      expect(mockProjectService.getProjectBoundaries).toHaveBeenCalledWith(mockProjectGuid);
      expect(console.error).toHaveBeenCalled();
    });
  });

  describe('openFileUploadModal', () => {
    it('should open file upload modal and call uploadFile if a file is selected', () => {
      const mockFile = new File(['content'], 'test-file.txt', { type: 'text/plain' });
      mockDialog.open.and.returnValue({
        afterClosed: () => of({ file: mockFile }),
      } as any);
      
      spyOn(component, 'uploadFile').and.stub();

      component.openFileUploadModal();
      expect(mockDialog.open).toHaveBeenCalledWith(AddAttachmentComponent, { width: '1000px' });
      expect(component.uploadFile).toHaveBeenCalledWith(mockFile);
    });

    it('should not call uploadFile if modal is closed without a file', () => {
      mockDialog.open.and.returnValue({
        afterClosed: () => of(null), // Simulating modal closed without selecting a file
      } as any);

      spyOn(component, 'uploadFile').and.stub();

      component.openFileUploadModal();
      expect(mockDialog.open).toHaveBeenCalled();
      expect(component.uploadFile).not.toHaveBeenCalled();
    });

    it('should set attachmentDescription if it is provided', () => {
      const description = 'Test description';
      mockDialog.open.and.returnValue({
        afterClosed: () => of({ description }),
      } as any);

      // Prevent loadProjectAttachments from being called
      spyOn(component, 'loadProjectAttachments').and.stub();

      component.openFileUploadModal();
      expect(component.attachmentDescription).toBe(description);
    });
  });

  describe('uploadFile', () => {
    it('should call uploadAttachment on successful file upload', () => {
      const mockFile = new File(['content'], 'test-file.txt', { type: 'text/plain' });
      const response = { fileId: 'test-file-id' };
      mockProjectService.uploadDocument.and.returnValue(of(response));
      
      spyOn(component, 'uploadAttachment').and.stub();

      component.uploadFile(mockFile);
      
      expect(mockProjectService.uploadDocument).toHaveBeenCalledWith({ file: mockFile });
      expect(component.uploadAttachment).toHaveBeenCalledWith(mockFile, response);
    });

    it('should handle file upload error', () => {
      const mockFile = new File(['content'], 'test-file.txt', { type: 'text/plain' });
      mockProjectService.uploadDocument.and.returnValue(throwError(() => new Error('Upload failed')));

      component.uploadFile(mockFile);

      expect(mockProjectService.uploadDocument).toHaveBeenCalledWith({ file: mockFile });
      expect(mockSnackbar.open).toHaveBeenCalledWith(
        'The spatial file was not uploaded.',
        'Close',
        jasmine.any(Object)
      );
    });
  });

  describe('uploadAttachment', () => {
    it('should create project attachment and extract coordinates on success', () => {
      const mockFile = new File(['content'], 'test-file.txt', { type: 'text/plain' });
      const response = { fileId: 'test-file-id' };
      const attachmentResponse = { uploadedByUserId: 'test-user' };
      const coordinates = [[123, 456]];

      mockAttachmentService.createProjectAttachment.and.returnValue(of(attachmentResponse));
      mockSpatialService.extractCoordinates.and.returnValue(Promise.resolve(coordinates));
      
      spyOn(component, 'updateProjectBoundary').and.stub();
      spyOn(component, 'loadProjectAttachments').and.stub();

      component.uploadAttachment(mockFile, response);

      expect(mockAttachmentService.createProjectAttachment).toHaveBeenCalledWith(
        mockProjectGuid,
        jasmine.objectContaining({
          fileIdentifier: 'test-file-id',
          documentPath: 'test-file.txt'
        })
      );

      // We need to use fixture.whenStable() for promises
      fixture.whenStable().then(() => {
        expect(component.uploadedBy).toBe('test-user');
        expect(mockSpatialService.extractCoordinates).toHaveBeenCalledWith(mockFile);
        expect(component.updateProjectBoundary).toHaveBeenCalledWith(mockFile, coordinates);
        expect(component.loadProjectAttachments).toHaveBeenCalled();
      });
    });

    it('should handle create attachment error', () => {
      const mockFile = new File(['content'], 'test-file.txt', { type: 'text/plain' });
      const response = { fileId: 'test-file-id' };

      mockAttachmentService.createProjectAttachment.and.returnValue(
        throwError(() => new Error('Failed to create attachment'))
      );

      component.uploadAttachment(mockFile, response);

      expect(mockAttachmentService.createProjectAttachment).toHaveBeenCalled();
      expect(mockSnackbar.open).toHaveBeenCalledWith(
        'Failed to upload file. Please try again.',
        'Close',
        jasmine.any(Object)
      );
    });
  });

  describe('updateProjectBoundary', () => {
    it('should create project boundary successfully', () => {
      const mockFile = new File(['content'], 'test-file.txt', { type: 'text/plain' });
      const coordinates = [[123, 456]];
      component.uploadedBy = 'test-user';

      mockProjectService.createProjectBoundary.and.returnValue(of({ success: true }));

      component.updateProjectBoundary(mockFile, coordinates);

      expect(mockProjectService.createProjectBoundary).toHaveBeenCalledWith(
        mockProjectGuid,
        jasmine.objectContaining({
          projectGuid: mockProjectGuid,
          collectorName: 'test-user',
          boundaryGeometry: {
            type: 'MultiPolygon',
            coordinates: coordinates
          }
        })
      );

      expect(mockSnackbar.open).toHaveBeenCalledWith(
        'File uploaded successfully.',
        'Close',
        jasmine.any(Object)
      );
    });

    it('should handle boundary creation error', () => {
      const mockFile = new File(['content'], 'test-file.txt', { type: 'text/plain' });
      const coordinates = [[123, 456]];

      mockProjectService.createProjectBoundary.and.returnValue(
        throwError(() => new Error('Failed to create boundary'))
      );

      component.updateProjectBoundary(mockFile, coordinates);

      expect(mockProjectService.createProjectBoundary).toHaveBeenCalled();
      expect(mockSnackbar.open).toHaveBeenCalledWith(
        'Failed to update project boundary.',
        'Close',
        jasmine.any(Object)
      );
    });
  });

  describe('deleteFile', () => {
    it('should open confirmation dialog and delete file when confirmed', () => {
      const mockProjectFile = { 
        fileAttachmentGuid: 'test-guid',
        fileName: 'test-file.txt'
      } as ProjectFile;
      
      mockDialog.open.and.returnValue({
        afterClosed: () => of(true) // Simulating user confirming deletion
      } as any);
      
      mockAttachmentService.deleteProjectAttachment.and.returnValue(of({}));
      
      component.projectFiles = [mockProjectFile];
      component.dataSource.data = component.projectFiles;
      
      component.deleteFile(mockProjectFile);
      
      expect(mockDialog.open).toHaveBeenCalledWith(
        ConfirmationDialogComponent, 
        jasmine.objectContaining({
          data: { indicator: 'confirm-delete-attachment' }
        })
      );
      
      expect(mockAttachmentService.deleteProjectAttachment).toHaveBeenCalledWith(
        mockProjectGuid, 
        'test-guid'
      );
      
      expect(component.projectFiles.length).toBe(0);
      expect(mockSnackbar.open).toHaveBeenCalledWith(
        'File has been deleted successfully.',
        'Close',
        jasmine.any(Object)
      );
    });
    
    it('should not delete file when dialog is canceled', () => {
      const mockProjectFile = { 
        fileAttachmentGuid: 'test-guid',
        fileName: 'test-file.txt'
      } as ProjectFile;
      
      mockDialog.open.and.returnValue({
        afterClosed: () => of(false) // Simulating user canceling deletion
      } as any);
      
      component.projectFiles = [mockProjectFile];
      
      component.deleteFile(mockProjectFile);
      
      expect(mockDialog.open).toHaveBeenCalled();
      expect(mockAttachmentService.deleteProjectAttachment).not.toHaveBeenCalled();
      expect(component.projectFiles.length).toBe(1);
    });
    
    it('should handle error when deleting file', () => {
      const mockProjectFile = { 
        fileAttachmentGuid: 'test-guid',
        fileName: 'test-file.txt'
      } as ProjectFile;
      
      mockDialog.open.and.returnValue({
        afterClosed: () => of(true)
      } as any);
      
      mockAttachmentService.deleteProjectAttachment.and.returnValue(
        throwError(() => new Error('Delete failed'))
      );
      
      spyOn(console, 'error');
      component.projectFiles = [mockProjectFile];
      
      component.deleteFile(mockProjectFile);
      
      expect(mockAttachmentService.deleteProjectAttachment).toHaveBeenCalled();
      expect(console.error).toHaveBeenCalled();
      expect(mockSnackbar.open).toHaveBeenCalledWith(
        'Failed to delete the file. Please try again.',
        'Close',
        jasmine.any(Object)
      );
    });
    
    it('should handle missing fileAttachmentGuid', () => {
      const mockProjectFile = { 
        fileName: 'test-file.txt'
      } as ProjectFile;
      
      mockDialog.open.and.returnValue({
        afterClosed: () => of(true)
      } as any);
      
      spyOn(console, 'error');
      
      component.deleteFile(mockProjectFile);
      
      expect(mockDialog.open).toHaveBeenCalled();
      expect(console.error).toHaveBeenCalled();
      expect(mockSnackbar.open).toHaveBeenCalledWith(
        'Failed to delete the file due to missing GUID.',
        'Close',
        jasmine.any(Object)
      );
    });
  });
  
  describe('downloadFile', () => {
    it('should have a downloadFile method', () => {
      expect(component.downloadFile).toBeDefined();
      
      // Since the implementation is empty, just test that it can be called
      const mockFile = { fileName: 'test.txt' };
      expect(() => component.downloadFile(mockFile)).not.toThrow();
    });
  });
});