import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, Input } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatTableDataSource, MatTableModule } from '@angular/material/table';
import { BlobReader, BlobWriter, ZipReader } from '@zip.js/zip.js';
import process from 'process';
import { catchError, lastValueFrom, map, of, tap, throwError } from 'rxjs';
import { AddAttachmentComponent } from 'src/app/components/add-attachment/add-attachment.component';
import { FileAttachment, ProjectBoundary } from 'src/app/components/models';
import { AttachmentService } from 'src/app/services/attachment-service';
import { ProjectService } from 'src/app/services/project-services';
import { SpatialService } from 'src/app/services/spatial-services';
import { Messages } from 'src/app/utils/messages';

(window as any).process = process;

interface ProjectFile {
  attachmentType: string;
  fileName: string;
  fileType: string;
  uploadedBy: string;
  uploadedDate: string;
  polygonHectares: number;
  description: string;
}

@Component({
  selector: 'app-project-files',
  standalone: true,
  imports: [CommonModule, MatTableModule],
  templateUrl: './project-files.component.html',
  styleUrl: './project-files.component.scss'
})
export class ProjectFilesComponent {
  @Input() projectGuid: string = '';
  attachmentDescription: string = '';
  uploadedBy = '';

  constructor(
    public projectService: ProjectService,
    private readonly snackbarService: MatSnackBar,
    public readonly dialog: MatDialog,
    public attachmentService: AttachmentService,
    public spatialService: SpatialService,
    private cdr: ChangeDetectorRef
  ) { }

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
  projectFiles: ProjectFile[] = [];

  dataSource = new MatTableDataSource<ProjectFile>(this.projectFiles);

  openFileUploadModal() {
    const dialogRef = this.dialog.open(AddAttachmentComponent, {
      width: '1000px',
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result?.file) {
        this.uploadFile(result.file)
      }
      if (result?.description) {
        this.attachmentDescription = result.description;
      }
    })
  }

  // Define allowed file types (excluding PNG)
  private allowedFileTypes: string[] = ['kml', 'kmz', 'zip', 'shp', 'gdb'];

  uploadFile(file: File): void {
    this.projectService.uploadDocument({
      file
    }).subscribe({
      next: (response) => {
        if (response)
          this.uploadAttachment(file, response)
      },
      error: (err) => {
        this.snackbarService.open('  The spatial file was not uploaded.', 'Close', {
          duration: 5000,
          panelClass: ['snackbar-error'],
        });
      }
    })
  }

  uploadAttachment(file: File, response: any): void {
    // Extract file extension and validate it
    const fileExtension = file.name.split('.').pop()?.toLowerCase();

    if (!fileExtension || !this.allowedFileTypes.includes(fileExtension)) {
      this.snackbarService.open('  The spatial file was not uploaded because the file format is not accepted. Only KML, KMZ, SHP, and GDB are allowed.', 'Close', {
        duration: 10000,
        panelClass: ['snackbar-error'],
      });
      return;
    }

    // Create attachment object
    const attachment: FileAttachment = {
      sourceObjectNameCode: { sourceObjectNameCode: "PROJECT" },
      sourceObjectUniqueId: this.projectGuid,
      documentPath: file.name,
      fileIdentifier: response.fileId,
      attachmentContentTypeCode: { attachmentContentTypeCode: "DOCUMENT" },
      attachmentDescription: this.attachmentDescription,
      attachmentReadOnlyInd: false
    };

    // Send to the backend
    this.attachmentService.createProjectAttachment(this.projectGuid, attachment).subscribe({
      next: (response) => {
        if (response) {
          this.uploadedBy = response?.uploadedByUserId;
          const newProjectFile = {
            attachmentType: 'Project Polygon',
            fileName: response.documentPath || file.name,
            fileType: 'DOCUMENT',
            uploadedBy: response.uploadedByUserId,
            uploadedDate: response.uploadedByTimestamp,
            polygonHectares: response.polygonHectares || 0,
            description: this.attachmentDescription,
          };

          // Update the list and refresh the table
          this.projectFiles = [newProjectFile]
          this.dataSource.data = [...this.projectFiles];

          // Show success snackbar
          this.extractCoordinates(file).then(response => {
            if (response) {
              console.log(response)
              this.updateProjectBoundary(file, response)
              this.snackbarService.open('File uploaded successfully.', 'Close', {
                duration: 10000,
                panelClass: 'snackbar-success',
              });
            }
          })
        }
      },
      error: () => {
        // Show error snackbar
        this.snackbarService.open('Failed to upload file. Please try again.', 'Close', {
          duration: 10000,
          panelClass: 'snackbar-error',
        });
      },
    });
  }

  updateProjectBoundary(file: File, response: number[][]) {
    const now = new Date();
    const futureDate = new Date(now);
    futureDate.setFullYear(futureDate.getFullYear() + 1);
  
    const boundary: ProjectBoundary = {
      projectGuid: this.projectGuid,
      systemStartTimestamp: now.toISOString(),
      systemEndTimestamp: futureDate.toISOString(),
      collectionDate: now.toISOString().split('T')[0],
      collectorName: this.uploadedBy,
      boundarySizeHa: 0,
      boundaryGeometry: {
        type: "MultiPolygon",
        coordinates: response,
      },
      //actual centroid will be calculated in API 
      //sum of all project and activity polygons - WFPREV-146
      locationGeometry: [-124, 49]
    };
  
    // Ensure the HTTP request is triggered by subscribing
    this.projectService.createProjectBoundary(this.projectGuid, boundary).pipe(
      map((resp: any) => resp),
      catchError((error) => {
        console.error("Error creating project boundary", error);
        return throwError(() => new Error("Failed to create project boundary"));
      })
    ).subscribe({
      next: (resp) => {
        console.log('Project boundary created successfully:', resp);
  
        // Show success notification
        this.snackbarService.open('Project boundary updated successfully.', 'Close', {
          duration: 5000,
          panelClass: 'snackbar-success',
        });
      },
      error: (error) => {
        this.snackbarService.open('Failed to update project boundary.', 'Close', {
          duration: 5000,
          panelClass: 'snackbar-error',
        });
      },
      complete: () => console.log('Project boundary update completed.')
    });
  }

  private async extractCoordinates(file: File): Promise<number[][]> {
    const fileType = file.name.split('.').pop()?.toLowerCase();
    try {
      if (fileType === 'kml') {
        return this.spatialService.extractKMLCoordinates(await file.text()) as unknown as number[][];
      } else if (fileType === 'zip' || fileType === 'gdb' || fileType === 'kmz') {
        const zipReader = new ZipReader(new BlobReader(file));
        const entries = await zipReader.getEntries();
        try {
          const kmlEntry = entries.find(entry => entry.filename.endsWith('.kml'));
          const shpEntry = entries.find(entry => entry.filename.endsWith('.shp'));
          const dbfEntry = entries.find(entry => entry.filename.endsWith('.dbf'));
          const containsGdbTable = entries.some(entry => entry.filename.includes('.gdbtable'));

          const gdbEntries = containsGdbTable ? entries : undefined;

          if (kmlEntry) {
            const extractedKML = await kmlEntry.getData?.(new BlobWriter());
            if (extractedKML) {
              return await this.spatialService.extractKMZCoordinates(
                new File([extractedKML], 'extracted.kml')
              ) as unknown as number[][];
            }
          }

          else if (shpEntry && dbfEntry) {
            return await this.spatialService.extractSHPCoordinates(file) as unknown as number[][];
          }

          else if (gdbEntries) {
            try {
              const geojsonData = await lastValueFrom(
                this.spatialService.extractGDBGeometry(file).pipe(
                  tap((data: any) => {
                    console.log("Full GeoJSON Data:", data);
                  }),
                  catchError((error: any) => {
                    console.error("Error extracting GeoJSON:", error);
                    return of(null); 
                  })
                )
              );
          
              if (geojsonData) {
                console.log("Extracted Coordinates:", geojsonData.coordinates);
                return geojsonData[0].coordinates;
              }
            } catch (error) {
              console.error("Error processing Geodatabase:", error);
            }
          }

          console.error('No supported spatial files found in ZIP or GDB.');
          return [];
        } finally {
          await zipReader.close();
        }
      } else {
        console.error('Unsupported file type:', fileType);
        return [];
      }
    } catch (error) {
      console.error('Error extracting coordinates:', error);
      return [];
    }
  }

  deleteFile(fileToDelete: ProjectFile): void {
    // Remove the file by filtering out the one to delete
    this.projectFiles = this.projectFiles.filter(file => file !== fileToDelete);

    // Update the MatTable data source to reflect the change
    this.dataSource.data = [...this.projectFiles];

    console.log('Updated project files:', this.projectFiles);
  }

  downloadFile(file: any) {
    // to do
  }
}
