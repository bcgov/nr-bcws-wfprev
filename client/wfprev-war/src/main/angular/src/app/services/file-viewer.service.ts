import { Injectable } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ProjectFile } from '../components/models';
import { SpatialViewerDialogComponent } from '../components/spatial-viewer-dialog/spatial-viewer-dialog.component';
import { ProjectService } from './project-services';

@Injectable({
  providedIn: 'root'
})
export class FileViewerService {
  constructor(
    private dialog: MatDialog,
    private projectService: ProjectService,
    private snackbar: MatSnackBar
  ) { }

  /**
   * Main entry point to view a project file.
   * Only spatial files (kml, kmz, shp, gdb, zip) are supported and will open the spatial map dialog.
   */
  public viewFile(file: ProjectFile): void {
    if (!file || !file.fileAttachmentGuid || !this.isSpatialFile(file)) {
      return;
    }

    this.openSpatialViewer(file);
  }

  public isSpatialFile(file: ProjectFile): boolean {
    const spatialExtensions = ['.kml', '.kmz', '.shp', '.gdb', '.zip'];
    const fileName = (file.fileName || file.documentPath || '').toLowerCase();
    return spatialExtensions.some(ext => fileName.endsWith(ext));
  }

  public canView(file: ProjectFile): boolean {
    if (!file || !file.fileAttachmentGuid) {
      return false;
    }

    return this.isSpatialFile(file);
  }

  private openSpatialViewer(file: ProjectFile): void {
    this.dialog.open(SpatialViewerDialogComponent, {
      width: '800px',
      height: '600px',
      data: {
        file: file
      },
      panelClass: 'spatial-viewer-panel'
    });
  }
}
