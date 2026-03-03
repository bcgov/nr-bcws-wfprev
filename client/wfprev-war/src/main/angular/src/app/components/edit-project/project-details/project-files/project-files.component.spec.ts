import { ChangeDetectorRef } from '@angular/core';
import { ComponentFixture, fakeAsync, TestBed, tick } from '@angular/core/testing';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar, MatSnackBarRef, SimpleSnackBar } from '@angular/material/snack-bar';
import { ActivatedRoute } from '@angular/router';
import { Position } from 'geojson';
import { of, throwError } from 'rxjs';
import { AddAttachmentComponent } from 'src/app/components/add-attachment/add-attachment.component';
import { ConfirmationDialogComponent } from 'src/app/components/confirmation-dialog/confirmation-dialog.component';
import { FileAttachment, ProjectFile } from 'src/app/components/models';
import { AttachmentService } from 'src/app/services/attachment-service';
import { ProjectService } from 'src/app/services/project-services';
import { SpatialService } from 'src/app/services/spatial-services';
import { Messages, ModalMessages, ModalTitles } from 'src/app/utils/constants';
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
  let mockSnackRef: MatSnackBarRef<SimpleSnackBar>;
  const mockProjectGuid = 'test-project-guid';

  beforeEach(async () => {
    mockProjectService = jasmine.createSpyObj('ProjectService', [
      'uploadDocument',
      'getProjectBoundaries',
      'createProjectBoundary',
      'deleteProjectBoundary',
      'downloadDocument',
      'getActivityBoundaries',
      'deleteActivityBoundary',
      'createActivityBoundary'
    ]);
    mockSnackbar = jasmine.createSpyObj('MatSnackBar', ['open']);
    mockDialog = jasmine.createSpyObj('MatDialog', ['open']);
    mockAttachmentService = jasmine.createSpyObj('AttachmentService', [
      'createProjectAttachment',
      'getProjectAttachments',
      'deleteProjectAttachment',
      'createActivityAttachment',
      'getActivityAttachments',
      'deleteActivityAttachments'
    ]);
    mockSpatialService = jasmine.createSpyObj('SpatialService', ['extractCoordinates']);
    mockCdr = jasmine.createSpyObj('ChangeDetectorRef', ['detectChanges']);

    // Setup default mock return values to prevent subscribe errors
    mockAttachmentService.getProjectAttachments.and.returnValue(of({ _embedded: {} }));
    mockAttachmentService.getActivityAttachments.and.returnValue(of({ _embedded: {} }));
    mockProjectService.getProjectBoundaries.and.returnValue(of({ _embedded: {} }));
    mockProjectService.getActivityBoundaries.and.returnValue(of({ _embedded: {} }));
    mockSpatialService.extractCoordinates.and.returnValue(Promise.resolve([]));
    mockAttachmentService.createProjectAttachment.and.returnValue(of({}));
    mockAttachmentService.createActivityAttachment.and.returnValue(of({}));
    mockAttachmentService.deleteProjectAttachment.and.returnValue(of({}));
    mockAttachmentService.deleteActivityAttachments.and.returnValue(of({}));
    mockProjectService.createProjectBoundary.and.returnValue(of({}));
    mockProjectService.createActivityBoundary.and.returnValue(of({}));
    mockProjectService.deleteProjectBoundary.and.returnValue(of({}));
    mockProjectService.deleteActivityBoundary.and.returnValue(of({}));
    mockProjectService.uploadDocument.and.returnValue(of({}));
    mockSnackRef = { dismiss: jasmine.createSpy('dismiss') } as any;
    mockSnackbar.open.and.returnValue(mockSnackRef);
    await TestBed.configureTestingModule({
      imports: [],
      providers: [
        { provide: ProjectService, useValue: mockProjectService },
        { provide: MatSnackBar, useValue: mockSnackbar },
        { provide: MatDialog, useValue: mockDialog },
        { provide: AttachmentService, useValue: mockAttachmentService },
        { provide: SpatialService, useValue: mockSpatialService },
        { provide: ChangeDetectorRef, useValue: mockCdr },
        { provide: ActivatedRoute, useValue: { snapshot: { queryParamMap: { get: () => 'test-project-guid' } } } },
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
    it('should call loadActivityAttachments when activityGuid and fiscalGuid are present', () => {
      component.activityGuid = 'activity-guid';
      component.fiscalGuid = 'fiscal-guid';

      spyOn(component, 'loadActivityAttachments');

      component.ngOnInit();

      expect(component.loadActivityAttachments).toHaveBeenCalled();
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

  describe('loadActivityAttachments', () => {
    const mockActivityGuid = 'test-activity-guid';
    const mockFiscalGuid = 'test-fiscal-guid';

    beforeEach(() => {
      component.projectGuid = mockProjectGuid;
      component.activityGuid = mockActivityGuid;
      component.fiscalGuid = mockFiscalGuid;
    });

    it('should load activity attachments and boundaries successfully', () => {
      const mockBoundaries = [
        { activityBoundaryGuid: 'abc', boundarySizeHa: 25 }
      ];

      const mockFiles = [
        { fileName: 'file1.txt', sourceObjectUniqueId: 'abc', uploadedByTimestamp: '2024-01-01T00:00:00Z' }
      ];

      const mockBoundaryResponse = {
        _embedded: { activityBoundary: mockBoundaries }
      };

      const mockAttachmentResponse = {
        _embedded: { fileAttachment: mockFiles }
      };

      mockProjectService.getActivityBoundaries.and.returnValue(of(mockBoundaryResponse));
      mockAttachmentService.getActivityAttachments.and.returnValue(of(mockAttachmentResponse));

      component.loadActivityAttachments();

      expect(mockProjectService.getActivityBoundaries).toHaveBeenCalledWith(mockProjectGuid, mockFiscalGuid, mockActivityGuid);
      expect(mockAttachmentService.getActivityAttachments).toHaveBeenCalledWith(mockProjectGuid, mockFiscalGuid, mockActivityGuid);
    });

    it('should handle case where boundaries are empty', () => {
      const mockBoundaryResponse = { _embedded: { activityBoundary: [] } };
      const mockAttachmentResponse = {
        _embedded: {
          fileAttachment: [{ fileName: 'test.txt', uploadedByTimestamp: '2024-01-01T00:00:00Z' }]
        }
      };

      mockProjectService.getActivityBoundaries.and.returnValue(of(mockBoundaryResponse));
      mockAttachmentService.getActivityAttachments.and.returnValue(of(mockAttachmentResponse));

      component.loadActivityAttachments();

      expect(component.projectFiles.length).toBe(1);
      expect(component.projectFiles[0].polygonHectares).toBeNull();
    });

    it('should handle missing fileAttachment array', () => {
      const mockBoundaryResponse = { _embedded: { activityBoundary: [] } };
      const mockAttachmentResponse = { _embedded: {} };

      mockProjectService.getActivityBoundaries.and.returnValue(of(mockBoundaryResponse));
      mockAttachmentService.getActivityAttachments.and.returnValue(of(mockAttachmentResponse));

      spyOn(console, 'error');
      component.loadActivityAttachments();

      expect(component.projectFiles.length).toBe(0);
      expect(console.error).toHaveBeenCalled();
    });

    it('should show snackbar on getActivityAttachments error', () => {
      const mockBoundaryResponse = { _embedded: { activityBoundary: [] } };

      mockProjectService.getActivityBoundaries.and.returnValue(of(mockBoundaryResponse));
      mockAttachmentService.getActivityAttachments.and.returnValue(
        throwError(() => new Error('Failed to load attachments'))
      );

      component.loadActivityAttachments();

      expect(mockSnackbar.open).toHaveBeenCalledWith(
        'Failed to load activity attachments.',
        'Close',
        jasmine.any(Object)
      );
    });

    it('should log error on getActivityBoundaries error', () => {
      mockProjectService.getActivityBoundaries.and.returnValue(
        throwError(() => new Error('Failed to load boundaries'))
      );

      spyOn(console, 'error');
      component.loadActivityAttachments();

      expect(console.error).toHaveBeenCalledWith('Failed to load activity boundaries:', jasmine.any(Error));
    });

    it('should handle missing fileAttachment array', () => {
      mockProjectService.getActivityBoundaries.and.returnValue(of({ _embedded: { activityBoundary: [] } }));
      mockAttachmentService.getActivityAttachments.and.returnValue(of({ _embedded: {} }));

      spyOn(console, 'error');
      component.loadActivityAttachments();

      expect(component.projectFiles.length).toBe(0);
    });

    it('should not run if fiscalGuid or activityGuid is missing', () => {
      component.fiscalGuid = '';
      component.activityGuid = '';

      component.loadActivityAttachments();

      expect(mockProjectService.getActivityBoundaries).not.toHaveBeenCalled();
      expect(mockAttachmentService.getActivityAttachments).not.toHaveBeenCalled();
    });

    it('should set description and call uploadFile when both are returned from modal', () => {
      const mockFile = new File(['dummy'], 'file.txt');
      const description = 'my description';

      mockDialog.open.and.returnValue({
        afterClosed: () => of({ file: mockFile, description, type: 'Activity Polygon' }),
      } as any);

      spyOn(component, 'uploadFile').and.stub();

      component.openFileUploadModal();

      expect(component.attachmentDescription).toBe(description);
      expect(component.uploadFile).toHaveBeenCalledWith(mockFile, 'Activity Polygon');
    });
  });


  describe('openFileUploadModal', () => {
    it('should open file upload modal and call uploadFile if a file is selected', () => {
      const mockFile = new File(['content'], 'test-file.txt', { type: 'text/plain' });
      mockDialog.open.and.returnValue({
        afterClosed: () => of({ file: mockFile, type: 'Activity Polygon' }),
      } as any);

      spyOn(component, 'uploadFile').and.stub();

      component.openFileUploadModal();
      expect(mockDialog.open).toHaveBeenCalledWith(AddAttachmentComponent, {
        width: '1000px',
        data: { indicator: 'project-files' },
      });
      expect(component.uploadFile).toHaveBeenCalledWith(mockFile, 'Activity Polygon');
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

      component.uploadFile(mockFile, 'Activity Polygon');

      expect(mockProjectService.uploadDocument).toHaveBeenCalledWith({ file: mockFile });
      expect(component.uploadAttachment).toHaveBeenCalledWith(mockFile, response, 'Activity Polygon', mockSnackRef);
    });

    it('should handle file upload error', () => {
      const mockFile = new File(['content'], 'test-file.txt', { type: 'text/plain' });
      mockProjectService.uploadDocument.and.returnValue(throwError(() => new Error('Upload failed')));

      component.uploadFile(mockFile, 'Activity Polygon');

      expect(mockProjectService.uploadDocument).toHaveBeenCalledWith({ file: mockFile });
      expect(mockSnackbar.open).toHaveBeenCalledWith(
        'Could not reach file upload server.',
        'Close',
        jasmine.any(Object)
      );
    });
  });

  describe('uploadAttachment', () => {
    it('should handle spatial file upload and create project boundary', fakeAsync(() => {
      const mockFile = new File(['dummy content'], 'test.kml', {
        type: 'application/vnd.google-earth.kml+xml',
      });

      const mockFileUploadResp = { fileId: 'mock-file-id' };
      const mockBoundaryResp = { projectBoundaryGuid: 'mock-boundary-id' };
      const mockCoordinates = [[[[0, 0], [1, 1], [1, 0], [0, 0]]]];

      mockSpatialService.extractCoordinates.and.returnValue(Promise.resolve(mockCoordinates));
      mockProjectService.createProjectBoundary.and.returnValue(of(mockBoundaryResp));
      mockAttachmentService.createProjectAttachment.and.returnValue(of({}));

      component.uploadedBy = 'test-user';
      component.attachmentDescription = 'Test spatial file';

      component.uploadAttachment(mockFile, mockFileUploadResp, 'kml', mockSnackRef);

      tick();
      fixture.detectChanges();

      expect(mockSpatialService.extractCoordinates).toHaveBeenCalledWith(mockFile);
      expect(mockProjectService.createProjectBoundary).toHaveBeenCalledWith(mockProjectGuid, {
        projectGuid: mockProjectGuid,
        systemStartTimestamp: jasmine.any(String),
        systemEndTimestamp: jasmine.any(String),
        collectionDate: jasmine.any(String),
        collectorName: 'test-user',
        boundaryGeometry: {
          type: 'MultiPolygon',
          coordinates: mockCoordinates
        }
      });

      expect(mockAttachmentService.createProjectAttachment).toHaveBeenCalledWith(mockProjectGuid, {
        sourceObjectNameCode: { sourceObjectNameCode: 'PROJECT' },
        sourceObjectUniqueId: 'mock-boundary-id',
        documentPath: 'test.kml',
        fileIdentifier: 'mock-file-id',
        attachmentContentTypeCode: { attachmentContentTypeCode: 'kml' },
        attachmentDescription: 'Test spatial file',
        attachmentReadOnlyInd: false
      });
    }));

    it('should call finishWithoutGeometry for unsupported extension', () => {
      const mockFile = new File([''], 'test.txt', { type: 'text/plain' });
      spyOn(component, 'finishWithoutGeometry');

      component.uploadAttachment(mockFile, {}, 'OTHER', mockSnackRef);

      expect(component.finishWithoutGeometry).toHaveBeenCalled();
    });

    it('should handle spatial file upload and create activity boundary when in activity context', fakeAsync(() => {
      const mockFile = new File(['dummy content'], 'test.kml', {
        type: 'application/vnd.google-earth.kml+xml',
      });

      const mockFileUploadResp = { fileId: 'mock-file-id' };
      const mockBoundaryResp = { activityBoundaryGuid: 'mock-boundary-id' };
      const mockCoordinates = [[[[0, 0], [1, 1], [1, 0], [0, 0]]]];

      mockSpatialService.extractCoordinates.and.returnValue(Promise.resolve(mockCoordinates));
      mockProjectService.createActivityBoundary.and.returnValue(of(mockBoundaryResp));
      mockAttachmentService.createActivityAttachment.and.returnValue(of({}));
      mockProjectService.getActivityBoundaries.and.returnValue(of([]));
      mockAttachmentService.getActivityAttachments.and.returnValue(of([]));

      spyOnProperty(component, 'isActivityContext', 'get').and.returnValue(true);
      component.projectGuid = 'mock-project-guid';
      component.fiscalGuid = 'mock-fiscal-guid';
      component.activityGuid = 'mock-activity-guid';
      component.uploadedBy = 'test-user';
      component.attachmentDescription = 'Test spatial file';
      component.uploadAttachment(mockFile, mockFileUploadResp, 'kml', mockSnackRef);

      tick();
      fixture.detectChanges();

      expect(mockSpatialService.extractCoordinates).toHaveBeenCalledWith(mockFile);
      expect(mockProjectService.createActivityBoundary).toHaveBeenCalledWith(
        'mock-project-guid',
        'mock-fiscal-guid',
        'mock-activity-guid',
        jasmine.objectContaining({
          activityGuid: 'mock-activity-guid',
          collectorName: 'test-user',
          geometry: {
            type: 'MultiPolygon',
            coordinates: mockCoordinates
          }
        })
      );

      expect(mockAttachmentService.createActivityAttachment).toHaveBeenCalledWith(
        'mock-project-guid',
        'mock-fiscal-guid',
        'mock-activity-guid',
        jasmine.objectContaining({
          sourceObjectNameCode: { sourceObjectNameCode: 'TREATMENT_ACTIVITY' },
          sourceObjectUniqueId: 'mock-boundary-id',
          documentPath: 'test.kml',
          fileIdentifier: 'mock-file-id',
          attachmentContentTypeCode: { attachmentContentTypeCode: 'kml' },
          attachmentDescription: 'Test spatial file',
          attachmentReadOnlyInd: false
        })
      );
    }));
  });

  describe('createProjectBoundary', () => {
    it('should create project boundary successfully', () => {
      const mockFile = new File(['content'], 'test-file.txt', { type: 'text/plain' });
      const coordinates: Position[][][] = [
        [
          [
            [123, 456]
          ]
        ]
      ];
      component.uploadedBy = 'test-user';

      mockProjectService.createProjectBoundary.and.returnValue(of({ success: true }));

      component.createProjectBoundary(mockFile, coordinates);

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
        Messages.fileUploadSuccess,
        'Close',
        jasmine.any(Object)
      );
    });

    it('should handle boundary creation error', () => {
      const mockFile = new File(['content'], 'test-file.txt', { type: 'text/plain' });
      const coordinates: Position[][][] = [
        [
          [
            [123, 456]
          ]
        ]
      ];

      spyOn(console, 'error');

      mockProjectService.createProjectBoundary.and.returnValue(
        throwError(() => new Error('Failed to create boundary'))
      );

      component.createProjectBoundary(mockFile, coordinates);

      expect(mockProjectService.createProjectBoundary).toHaveBeenCalled();
      expect(console.error).toHaveBeenCalledWith(
        'Failed to upload project geometry: ',
        jasmine.any(Error)
      );
    });
  });

  describe('deleteFile', () => {
    it('should open confirmation dialog and delete file when confirmed', () => {
      const mockProjectGuid = 'mock-guid';
      const mockProjectFile: ProjectFile = {
        fileAttachmentGuid: 'test-guid',
        fileName: 'test-file.txt',
        attachmentContentTypeCode: { attachmentContentTypeCode: 'MAP' },
        sourceObjectUniqueId: 'boundary-guid'
      };

      const mockBoundary = {
        projectBoundaryGuid: 'boundary-guid',
        systemStartTimestamp: new Date().toISOString()
      };

      mockDialog.open.and.returnValue({
        afterClosed: () => of(true)
      } as any);

      mockAttachmentService.deleteProjectAttachment.and.returnValue(of({}));
      mockProjectService.getProjectBoundaries.and.returnValue(of({
        _embedded: { projectBoundary: [mockBoundary] }
      }));
      mockProjectService.deleteProjectBoundary.and.returnValue(of({}));
      mockSnackbar.open.and.stub();

      component.projectGuid = mockProjectGuid;
      component.projectFiles = [mockProjectFile];
      component.dataSource.data = [mockProjectFile];

      component.deleteFile(mockProjectFile);

      expect(mockDialog.open).toHaveBeenCalledWith(
        ConfirmationDialogComponent,
        jasmine.objectContaining({
          data: {
            indicator: 'delete-attachment', title: ModalTitles.DELETE_ATTACHMENT_TITLE,
            message: ModalMessages.DELETE_ATTACHMENT_MESSAGE
          },
          width: '600px'
        })
      );

      expect(mockSnackbar.open).toHaveBeenCalledWith(
        Messages.fileDeleteInProgress,
        'Close',
        jasmine.objectContaining({ panelClass: 'snackbar-info' })
      );

      expect(mockAttachmentService.deleteProjectAttachment).toHaveBeenCalledWith(
        mockProjectGuid,
        'test-guid'
      );

      expect(mockProjectService.deleteProjectBoundary).toHaveBeenCalledWith(mockProjectGuid, 'boundary-guid');

      expect(component.projectFiles.length).toBe(0);
      expect(component.dataSource.data.length).toBe(0);

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
        Messages.fileDeleteInProgress,
        'Close',
        jasmine.objectContaining({ panelClass: 'snackbar-info' })
      );
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
      const mockFile: FileAttachment = {
        fileIdentifier: '123',
        documentPath: 'test.txt',
        attachmentReadOnlyInd: false
      };

      mockProjectService.downloadDocument.and.returnValue(of(new Blob(['test-content'], { type: 'text/plain' })));

      expect(() => component.downloadFile(mockFile)).not.toThrow();
    });

    it('should show snackbar error if file download fails', () => {
      const mockFile = { fileIdentifier: 'abc', documentPath: 'file.txt' };
      const mockError = new Error('Failed to download');

      mockProjectService.downloadDocument.and.returnValue(throwError(() => mockError));
      spyOn(console, 'error');

      component.downloadFile(mockFile);

      expect(console.error).toHaveBeenCalledWith('Download failed', mockError);
      expect(mockSnackbar.open).toHaveBeenCalledWith(
        Messages.fileDownloadFailure,
        'Close',
        jasmine.any(Object)
      );
    });
  });

  it('should show error if uploaded file has no extension', () => {
    const mockFile = new File(['content'], 'file.', { type: 'text/plain' });

    component.uploadAttachment(mockFile, { fileId: 'some-id' }, 'Activity Polygon', mockSnackRef);

    expect(mockSnackbar.open).toHaveBeenCalledWith(
      'The spatial file was not uploaded because the file format is not accepted.',
      'Close',
      jasmine.any(Object)
    );
  });

  describe('isActivityContext', () => {
    it('should return true if activityGuid and fiscalGuid are set', () => {
      component.activityGuid = 'activity';
      component.fiscalGuid = 'fiscal';
      expect(component.isActivityContext).toBeTrue();
    });

    it('should return false if either activityGuid or fiscalGuid is missing', () => {
      component.activityGuid = '';
      component.fiscalGuid = 'fiscal';
      expect(component.isActivityContext).toBeFalse();
    });
  });

  describe('loadActivityAttachments', () => {

    it('should set description and call uploadFile when both are returned from modal', () => {
      const mockFile = new File(['dummy'], 'file.txt');
      const description = 'my description';

      mockDialog.open.and.returnValue({
        afterClosed: () => of({ file: mockFile, description, type: 'Activity Polygon' }),
      } as any);

      spyOn(component, 'uploadFile').and.stub();

      component.openFileUploadModal();

      expect(component.attachmentDescription).toBe(description);
      expect(component.uploadFile).toHaveBeenCalledWith(mockFile, 'Activity Polygon');
    });
  });

  describe('createActivityBoundary', () => {
    const mockFile = new File(['dummy'], 'shape.geojson');
    const mockCoordinates: Position[][][] = [
      [[[123.45, 67.89], [123.46, 67.90], [123.47, 67.91], [123.45, 67.89]]]
    ];

    beforeEach(() => {
      component.projectGuid = 'project-guid';
      component.fiscalGuid = 'fiscal-guid';
      component.activityGuid = 'activity-guid';
      component.uploadedBy = 'user@example.com';

      mockProjectService.createActivityBoundary = jasmine.createSpy().and.returnValue(of({}));
      spyOn(component, 'loadActivityAttachments');
      spyOn(component.filesUpdated, 'emit');
    });

    it('should successfully create activity boundary and show snackbar', () => {
      component.createActivityBoundary(mockFile, mockCoordinates);

      expect(mockProjectService.createActivityBoundary).toHaveBeenCalledWith(
        'project-guid',
        'fiscal-guid',
        'activity-guid',
        jasmine.objectContaining({
          activityGuid: 'activity-guid',
          collectorName: 'user@example.com',
          geometry: {
            type: 'MultiPolygon',
            coordinates: mockCoordinates
          }
        })
      );

      expect(mockSnackbar.open).toHaveBeenCalledWith(
        Messages.fileUploadSuccess,
        'Close',
        jasmine.any(Object)
      );
      expect(component.loadActivityAttachments).toHaveBeenCalled();
      expect(component.filesUpdated.emit).toHaveBeenCalled();
    });

    it('should log error if activity boundary creation fails', () => {
      mockProjectService.createActivityBoundary = jasmine.createSpy().and.returnValue(
        throwError(() => new Error('server error'))
      );
      spyOn(console, 'error');

      component.createActivityBoundary(mockFile, mockCoordinates);

      expect(console.error).toHaveBeenCalledWith(
        'Failed to upload activity boundary: ',
        jasmine.any(Error)
      );
    });
  });

  it('should skip boundary deletion when file is not MAP type', () => {
    component.activityGuid = 'activity-guid';
    component.fiscalGuid = 'fiscal-guid';
    component.projectGuid = 'project-guid';

    const mockFile = {
      fileAttachmentGuid: 'test-guid',
      attachmentContentTypeCode: { attachmentContentTypeCode: 'DOCUMENT' }
    } as ProjectFile;

    mockDialog.open.and.returnValue({ afterClosed: () => of(true) } as any);
    mockAttachmentService.deleteActivityAttachments.and.returnValue(of({}));

    spyOn(component.filesUpdated, 'emit');
    spyOn(component, 'loadActivityAttachments');

    mockProjectService.getActivityBoundaries.calls.reset();

    component.projectFiles = [mockFile];
    component.dataSource.data = [mockFile];

    component.deleteFile(mockFile);

    expect(mockAttachmentService.deleteActivityAttachments).toHaveBeenCalled();
    expect(mockProjectService.getActivityBoundaries).not.toHaveBeenCalled(); // ✅ safe
    expect(component.filesUpdated.emit).toHaveBeenCalled();
    expect(component.loadActivityAttachments).toHaveBeenCalled();
  });


  it('should delete activity attachment and latest boundary successfully when confirmed', (done) => {
    component.activityGuid = 'activity-guid';
    component.fiscalGuid = 'fiscal-guid';
    component.projectGuid = 'project-guid';

    const mockFile = {
      fileAttachmentGuid: 'test-guid',
      attachmentContentTypeCode: { attachmentContentTypeCode: 'MAP' },
      sourceObjectUniqueId: 'boundary-guid'
    } as ProjectFile;
    const mockBoundary = {
      activityBoundaryGuid: 'boundary-guid',
      systemStartTimestamp: new Date().toISOString()
    };

    mockDialog.open.and.returnValue({ afterClosed: () => of(true) } as any);
    mockAttachmentService.deleteActivityAttachments = jasmine.createSpy().and.returnValue(of({}));
    mockProjectService.getActivityBoundaries = jasmine.createSpy().and.returnValue(of({
      _embedded: { activityBoundary: [mockBoundary] }
    }));
    mockProjectService.deleteActivityBoundary = jasmine.createSpy().and.returnValue(of({}));

    spyOn(component.filesUpdated, 'emit');
    spyOn(component, 'loadActivityAttachments');

    component.projectFiles = [mockFile];
    component.dataSource.data = [mockFile];

    component.deleteFile(mockFile);

    setTimeout(() => {
      expect(mockAttachmentService.deleteActivityAttachments).toHaveBeenCalledWith(
        'project-guid', 'fiscal-guid', 'activity-guid', 'test-guid'
      );
      expect(mockProjectService.deleteActivityBoundary).toHaveBeenCalledWith(
        'project-guid', 'fiscal-guid', 'activity-guid', 'boundary-guid'
      );
      expect(component.filesUpdated.emit).toHaveBeenCalled();
      expect(mockSnackbar.open).toHaveBeenCalledWith(
        'File has been deleted successfully.',
        'Close',
        jasmine.any(Object)
      );
      expect(component.loadActivityAttachments).toHaveBeenCalled();
      done();
    }, 0);
  });

  it('should show snackbar error if deleting activity attachment fails', () => {
    component.activityGuid = 'activity-guid';
    component.fiscalGuid = 'fiscal-guid';
    component.projectGuid = 'project-guid';

    const mockFile = { fileAttachmentGuid: 'test-guid' } as ProjectFile;

    mockDialog.open.and.returnValue({ afterClosed: () => of(true) } as any);
    mockAttachmentService.deleteActivityAttachments = jasmine.createSpy().and.returnValue(
      throwError(() => new Error('delete failed'))
    );

    spyOn(console, 'error');
    component.deleteFile(mockFile);

    expect(mockSnackbar.open).toHaveBeenCalledWith(
      'Failed to delete the file. Please try again.',
      'Close',
      jasmine.any(Object)
    );
    expect(console.error).toHaveBeenCalled();
  });

  it('should handle no boundaries found after deleting attachment', () => {
    component.activityGuid = 'activity-guid';
    component.fiscalGuid = 'fiscal-guid';
    component.projectGuid = 'project-guid';
    const mockFile = {
      fileAttachmentGuid: 'test-guid',
      attachmentContentTypeCode: { attachmentContentTypeCode: 'MAP' },
      sourceObjectUniqueId: 'boundary-guid'
    } as ProjectFile;

    mockDialog.open.and.returnValue({ afterClosed: () => of(true) } as any);
    mockAttachmentService.deleteActivityAttachments.and.returnValue(of({}));
    mockProjectService.getActivityBoundaries.and.returnValue(of({
      _embedded: { activityBoundary: [] }
    }));

    spyOn(component, 'loadActivityAttachments').and.callThrough();
    component.deleteFile(mockFile);

    expect(mockAttachmentService.deleteActivityAttachments).toHaveBeenCalled();
    expect(mockProjectService.getActivityBoundaries).toHaveBeenCalled();
    // Implementation doesn't log "No boundaries found" anymore, removing expectation
  });

  describe('finishWithoutGeometry', () => {
    beforeEach(() => {
      spyOn(component.filesUpdated, 'emit');
      spyOn(component, 'loadActivityAttachments');
      spyOn(component, 'loadProjectAttachments');
    });

    it('should show snackbar, call loadActivityAttachments and emit event when isActivityContext is true', () => {
      component.fiscalGuid = 'fiscal-guid';
      component.activityGuid = 'activity-guid';

      const mockFile = new File(['dummy'], 'test.doc');
      const mockUploadResp = { fileId: 'abc123' };
      const mockType = 'DOCUMENT';

      mockAttachmentService.createActivityAttachment.and.returnValue(of({}));

      component.finishWithoutGeometry(mockFile, mockUploadResp, mockType);

      expect(mockSnackbar.open).toHaveBeenCalledWith(
        Messages.fileUploadSuccess,
        'Close',
        jasmine.objectContaining({ duration: 5000, panelClass: 'snackbar-success' })
      );
      expect(component.loadActivityAttachments).toHaveBeenCalled();
      expect(component.loadProjectAttachments).not.toHaveBeenCalled();
      expect(component.filesUpdated.emit).toHaveBeenCalled();
    });


    it('should show snackbar, call loadProjectAttachments and emit event when isActivityContext is false', () => {
      component.fiscalGuid = '';
      component.activityGuid = '';

      const mockFile = new File(['dummy'], 'test.pdf');
      const mockUploadResp = { fileId: 'xyz456' };
      const mockType = 'DOCUMENT';

      mockAttachmentService.createProjectAttachment.and.returnValue(of({}));

      component.finishWithoutGeometry(mockFile, mockUploadResp, mockType);

      expect(mockSnackbar.open).toHaveBeenCalledWith(
        Messages.fileUploadSuccess,
        'Close',
        jasmine.objectContaining({ duration: 5000, panelClass: 'snackbar-success' })
      );
      expect(component.loadProjectAttachments).toHaveBeenCalled();
      expect(component.loadActivityAttachments).not.toHaveBeenCalled();
      expect(component.filesUpdated.emit).toHaveBeenCalled();
    });
  });

  describe('translateAttachmentType', () => {
    it('should return "Activity Polygon" when description is "Map" and isActivityContext is true', () => {
      component.activityGuid = 'activity-guid';
      component.fiscalGuid = 'fiscal-guid';

      const result = component.translateAttachmentType('Map');
      expect(result).toBe('Activity Polygon');
    });

    it('should return "Project Boundary" when description is "Map" and isActivityContext is false', () => {
      component.activityGuid = '';
      component.fiscalGuid = '';

      const result = component.translateAttachmentType('Map');
      expect(result).toBe('Project Boundary');
    });

    it('should return "Prescription" when description is "Document"', () => {
      const result = component.translateAttachmentType('Document');
      expect(result).toBe('Prescription');
    });


    it('should create activity attachment and handle success in activity context', () => {
      component.projectGuid = 'project-guid';
      component.fiscalGuid = 'fiscal-guid';
      component.activityGuid = 'activity-guid';
      component.attachmentDescription = 'Test Description';

      const mockFile = new File(['data'], 'test.kml');
      const mockUploadResp = { fileId: 'abc123' };
      const mockType = 'kml';

      const expectedAttachment = jasmine.objectContaining({
        sourceObjectNameCode: { sourceObjectNameCode: 'TREATMENT_ACTIVITY' },
        sourceObjectUniqueId: 'activity-guid',
        documentPath: 'test.kml',
        fileIdentifier: 'abc123',
        attachmentContentTypeCode: { attachmentContentTypeCode: 'kml' },
        attachmentDescription: 'Test Description',
        attachmentReadOnlyInd: false
      });

      mockAttachmentService.createActivityAttachment.and.returnValue(of({}));

      spyOn(component.filesUpdated, 'emit');
      spyOn(component, 'loadActivityAttachments');

      component.finishWithoutGeometry(mockFile, mockUploadResp, mockType);

      expect(mockAttachmentService.createActivityAttachment).toHaveBeenCalledWith(
        'project-guid', 'fiscal-guid', 'activity-guid', expectedAttachment
      );

      expect(mockSnackbar.open).toHaveBeenCalledWith(
        Messages.fileUploadSuccess, 'Close', jasmine.any(Object)
      );

      expect(component.loadActivityAttachments).toHaveBeenCalled();
      expect(component.filesUpdated.emit).toHaveBeenCalled();
    });

    it('should show progress and success snackbars on successful download', () => {
      const mockFile: FileAttachment = {
        fileIdentifier: '123',
        documentPath: 'test.txt',
        attachmentReadOnlyInd: false
      };

      const mockBlob = new Blob(['test'], { type: 'text/plain' });

      const mockSnackRef = { dismiss: jasmine.createSpy('dismiss') } as any;
      mockSnackbar.open.and.returnValues(mockSnackRef, mockSnackRef);

      mockProjectService.downloadDocument.and.returnValue(of(mockBlob));

      component.downloadFile(mockFile);

      expect(mockSnackbar.open).toHaveBeenCalledWith(
        Messages.fileDownloadInProgress,
        'Close',
        jasmine.objectContaining({ duration: undefined, panelClass: 'snackbar-info' })
      );

      expect(mockSnackRef.dismiss).toHaveBeenCalled();
      expect(mockSnackbar.open).toHaveBeenCalledWith(
        Messages.fileDownloadSuccess,
        'Close',
        jasmine.objectContaining({ duration: 5000, panelClass: 'snackbar-success' })
      );
    });

    it('should show progress and error snackbars on download failure', () => {
      const mockFile: FileAttachment = {
        fileIdentifier: '123',
        documentPath: 'test.txt',
        attachmentReadOnlyInd: false
      };

      const mockError = new Error('download failed');
      const mockSnackRef = { dismiss: jasmine.createSpy('dismiss') } as any;
      mockSnackbar.open.and.returnValues(mockSnackRef, mockSnackRef);

      mockProjectService.downloadDocument.and.returnValue(throwError(() => mockError));
      spyOn(console, 'error');

      component.downloadFile(mockFile);

      expect(mockSnackbar.open).toHaveBeenCalledWith(
        Messages.fileDownloadInProgress,
        'Close',
        jasmine.any(Object)
      );

      expect(mockSnackRef.dismiss).toHaveBeenCalled();
      expect(mockSnackbar.open).toHaveBeenCalledWith(
        Messages.fileDownloadFailure,
        'Close',
        jasmine.any(Object)
      );
      expect(console.error).toHaveBeenCalledWith('Download failed', mockError);
    });

  });

  describe('hasAttachments', () => {
    it('returns false when projectFiles is an empty array', () => {
      component.projectFiles = [];
      expect(component.hasAttachments).toBeFalse();
    });

    it('returns true when projectFiles has items', () => {
      component.projectFiles = [{ fileName: 'a.txt' } as ProjectFile];
      expect(component.hasAttachments).toBeTrue();
    });

    it('returns false when projectFiles is undefined/null', () => {
      (component as any).projectFiles = undefined;
      expect(component.hasAttachments).toBeFalse();

      (component as any).projectFiles = null;
      expect(component.hasAttachments).toBeFalse();
    });

    it('reflects changes if the same array reference is mutated', () => {
      component.projectFiles = [];
      expect(component.hasAttachments).toBeFalse();

      component.projectFiles.push({ fileName: 'b.txt' } as ProjectFile);
      expect(component.hasAttachments).toBeTrue();
    });
  });


});