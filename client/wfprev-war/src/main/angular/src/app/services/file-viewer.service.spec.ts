import { TestBed } from '@angular/core/testing';
import { MatDialog } from '@angular/material/dialog';
import { ProjectFile } from '../components/models';
import { SpatialViewerDialogComponent } from '../components/spatial-viewer-dialog/spatial-viewer-dialog.component';
import { FileViewerService } from './file-viewer.service';

describe('FileViewerService', () => {
  let service: FileViewerService;
  let dialogSpy: jasmine.SpyObj<MatDialog>;

  beforeEach(() => {
    dialogSpy = jasmine.createSpyObj('MatDialog', ['open']);

    TestBed.configureTestingModule({
      providers: [
        FileViewerService,
        { provide: MatDialog, useValue: dialogSpy },
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
    });

  });
});
