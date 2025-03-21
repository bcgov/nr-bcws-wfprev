import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatTableModule } from '@angular/material/table';
import { EMPTY, from, switchMap } from 'rxjs';
import { AddAttachmentComponent } from 'src/app/components/add-attachment/add-attachment.component';
import { FileAttachment, ProjectBoundary } from 'src/app/components/models';
import { AttachmentService } from 'src/app/services/attachment-service';
import { ProjectService } from 'src/app/services/project-services';
import { SpatialService } from 'src/app/services/spatial-services';
import { Messages } from 'src/app/utils/messages';

@Component({
  selector: 'app-project-files',
  standalone: true,
  imports: [CommonModule,MatTableModule],
  templateUrl: './project-files.component.html',
  styleUrl: './project-files.component.scss'
})
export class ProjectFilesComponent {
  @Input() projectGuid: string = '';

  constructor(
    public projectService: ProjectService,
    private readonly snackbarService: MatSnackBar,
    public readonly dialog: MatDialog,
    public attachmentService: AttachmentService,
    public spatialService: SpatialService
  )
  {}

  messages = Messages;  
  displayedColumns: string[] = [
    'attachmentType',
    'fileName',
    'fileType',
    'uploadedBy',
    'uploadedDate',
    'polygonHectares',
    'description',
    'download',
    'delete'
  ];

  // hardcode table, will be replaced by the attachment api data
  projectFiles = [
    {
      attachmentType: 'Activity Polygon',
      fileName: 'Polygon234',
      fileType: 'KMZ',
      uploadedBy: 'IDIR\\LULI',
      uploadedDate: '2024-12-05',
      polygonHectares: 22555,
      description: 'Lorem ipsum dolor sit amet, consectetur adipiscing elit.'
    }
  ];

  openFileUploadModal(){
    const dialogRef = this.dialog.open(AddAttachmentComponent, {
      width: '1000px',
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result?.file) {
        this.uploadFile(result.file)
      }
    })
  }

  uploadFile(file: File): void {
    this.projectService.uploadDocument({ file }).pipe(
      switchMap((response) => {
        if (!response) {
          console.error('Upload failed: No response from uploadDocument');
          return EMPTY;
        }
  
        const attachment: FileAttachment = {
          sourceObjectNameCode: { sourceObjectNameCode: "PROJECT" },
          sourceObjectUniqueId: this.projectGuid,
          documentPath: response.filePath,
          fileIdentifier: response.fileId,
          attachmentContentTypeCode: { attachmentContentTypeCode: "OTHER" },
          attachmentDescription: "DESCRIPTION",
          attachmentReadOnlyInd: false
        };
  
        return this.attachmentService.createProjectAttachment(this.projectGuid, attachment).pipe(
          switchMap(() => from(this.extractCoordinates(file))) // Extract coordinates
        );
      }),
      switchMap((coordinates) => {
        if (!coordinates || coordinates.length === 0) {
          console.log('File is not a spatial file. Skipping project boundary creation.');
          return EMPTY; // Skip createProjectBoundary
        }
  
        const projectBoundary: ProjectBoundary = {
          projectGuid: this.projectGuid,
          systemStartTimestamp: "2026-01-01",
          systemEndTimestamp: "2026-12-31",
          mappingLabel: "Test mapping label",
          collectionDate: "2026-01-15",
          collectionMethod: "Test collection method",
          collectorName: "Test_user",
          boundarySizeHa: 200.5,
          boundaryComment: "Test activity boundary comment",
          boundaryGeometry: {
            type: "Polygon",
            coordinates: coordinates as number[][][]
          },
          locationGeometry: coordinates[0][0] as [number, number]
        };
  
        return this.projectService.createProjectBoundary(this.projectGuid, projectBoundary);
      })
    ).subscribe({
      next: () => console.log('Project boundary created successfully'),
      error: (error) => console.error('Error during file upload or boundary creation:', error),
    });
  }
  
  private async extractCoordinates(file: File): Promise<number[][][]> {
    const fileType = file.name.split('.').pop()?.toLowerCase();

    try {
      if (fileType === 'kml') {
        return this.spatialService.extractKMLCoordinates(await file.text()) as number[][][];
      } else if (fileType === 'kmz') {
        return await this.spatialService.extractKMZCoordinates(file) as number[][][];
      } else if (fileType === 'shp') {
        return await this.spatialService.extractSHPCoordinates(file) as number[][][];
      } else {
        console.error('Unsupported file type:', fileType);
        return [];
      }
    } catch (error) {
      console.error('Error extracting coordinates:', error);
      return [];
    }
  }

  deleteFile(file:any){
    //to do
  }

  downloadFile(file:any){
    // to do
  }
}
