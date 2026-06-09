import { TestBed } from '@angular/core/testing';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { FileViewerService } from './file-viewer.service';
import { ProjectService } from './project-services';
import { ProjectFile } from '../components/models';
import { of, throwError } from 'rxjs';
import { SpatialViewerDialogComponent } from '../components/spatial-viewer-dialog/spatial-viewer-dialog.component';

describe('FileViewerService', () => {
  let service: FileViewerService;
  let dialogSpy: jasmine.SpyObj<MatDialog>;
  let projectServiceSpy: jasmine.SpyObj<ProjectService>;
  let snackbarSpy: jasmine.SpyObj<MatSnackBar>;

  beforeEach(() => {
    dialogSpy = jasmine.createSpyObj('MatDialog', ['open']);
    projectServiceSpy = jasmine.createSpyObj('ProjectService', ['downloadDocument']);
    snackbarSpy = jasmine.createSpyObj('MatSnackBar', ['open']);

    TestBed.configureTestingModule({
      providers: [
        FileViewerService,
        { provide: MatDialog, useValue: dialogSpy },
        { provide: ProjectService, useValue: projectServiceSpy },
        { provide: MatSnackBar, useValue: snackbarSpy }
      ]
    });
    service = TestBed.inject(FileViewerService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  describe('isSpatialFile', () => {
    it('should return true for kml, kmz, shp, gdb, zip', () => {
      expect(service.isSpatialFile({ fileName: 'test.kml' } as ProjectFile)).toBeTrue();
      expect(service.isSpatialFile({ fileName: 'data.shp' } as ProjectFile)).toBeTrue();
      expect(service.isSpatialFile({ fileName: 'file.ZIP' } as ProjectFile)).toBeTrue();
    });

    it('should return false for pdf, png, txt', () => {
      expect(service.isSpatialFile({ fileName: 'doc.pdf' } as ProjectFile)).toBeFalse();
      expect(service.isSpatialFile({ fileName: 'image.png' } as ProjectFile)).toBeFalse();
    });
  });

  describe('viewFile', () => {
    let windowOpenSpy: jasmine.Spy;
    let createObjectURLSpy: jasmine.Spy;

    beforeEach(() => {
      windowOpenSpy = spyOn(window, 'open');
      createObjectURLSpy = spyOn(URL, 'createObjectURL').and.returnValue('blob:fake-url');
    });

    it('should open spatial viewer dialog for spatial files', () => {
      const file: ProjectFile = { fileAttachmentGuid: '123', fileName: 'test.shp' };
      service.viewFile(file);

      expect(dialogSpy.open).toHaveBeenCalledWith(SpatialViewerDialogComponent, jasmine.objectContaining({
        data: { file }
      }));
      expect(projectServiceSpy.downloadDocument).not.toHaveBeenCalled();
    });

    it('should download and open in new tab for non-spatial files', () => {
      const file: ProjectFile = { fileAttachmentGuid: '123', fileName: 'test.pdf' };
      const fakeBlob = new Blob(['dummy content'], { type: 'application/pdf' });
      projectServiceSpy.downloadDocument.and.returnValue(of(fakeBlob));

      service.viewFile(file);

      expect(projectServiceSpy.downloadDocument).toHaveBeenCalledWith('123');
      expect(createObjectURLSpy).toHaveBeenCalled();
      expect(windowOpenSpy).toHaveBeenCalledWith('blob:fake-url', '_blank');
    });

    it('should show snackbar error on download failure', () => {
      const file: ProjectFile = { fileAttachmentGuid: '123', fileName: 'test.pdf' };
      projectServiceSpy.downloadDocument.and.returnValue(throwError(() => new Error('Network error')));

      service.viewFile(file);

      expect(snackbarSpy.open).toHaveBeenCalledWith('Failed to load file.', 'Close', { duration: 3000 });
      expect(windowOpenSpy).not.toHaveBeenCalled();
    });
  });
});
