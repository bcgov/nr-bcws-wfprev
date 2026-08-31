import { Injectable } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { ProjectFile } from '../components/models';
import { SpatialViewerDialogComponent } from '../components/spatial-viewer-dialog/spatial-viewer-dialog.component';

@Injectable({
  providedIn: 'root'
})
export class FileViewerService {
  constructor(
    private readonly dialog: MatDialog
  ) { }

  /**
   * Main entry point to view a project file.
   * Only spatial files (kml, kmz, shp, gdb, zip) are supported and will open the spatial map dialog.
   */
  public viewFile(file: ProjectFile): void {
    if (!this.canView(file)) {
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
    // An orphaned boundary row has no attachment and no file name to inspect, but it does
    // carry the geometry - which is the only thing the viewer needs. Seeing the polygon is
    // how a user decides whether to delete it. See WFPREV-1223.
    if (file?.isOrphanBoundary) {
      return !!file.boundaryGeometry;
    }

    if (!file?.fileAttachmentGuid) {
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
