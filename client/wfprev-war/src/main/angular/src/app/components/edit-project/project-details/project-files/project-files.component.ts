import { Component, Input, OnInit } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatDialog } from '@angular/material/dialog';
import { MatTableDataSource, MatTableModule } from '@angular/material/table';
import { AttachmentService } from 'src/app/services/attachment-service';
import { ProjectService } from 'src/app/services/project-services';
import { FileAttachment, ProjectBoundary, ProjectFile } from 'src/app/components/models';
import { AddAttachmentComponent } from 'src/app/components/add-attachment/add-attachment.component';
import { ConfirmationDialogComponent } from 'src/app/components/confirmation-dialog/confirmation-dialog.component';
import { Messages } from 'src/app/utils/messages';
import { CommonModule } from '@angular/common';
import { SpatialService } from 'src/app/services/spatial-services';
import { BlobReader, BlobWriter, ZipReader } from '@zip.js/zip.js';
import { catchError, lastValueFrom, map, of, throwError } from 'rxjs';

@Component({
  selector: 'app-project-files',
  standalone: true,
  imports: [MatTableModule, CommonModule],
  templateUrl: './project-files.component.html',
  styleUrls: ['./project-files.component.scss']
})
export class ProjectFilesComponent implements OnInit {
  @Input() projectGuid: string = '';
  attachmentDescription: string = '';
  uploadedBy = '';

  projectFiles: ProjectFile[] = [];
  dataSource = new MatTableDataSource<ProjectFile>(this.projectFiles);

  constructor(
    public projectService: ProjectService,
    private readonly snackbarService: MatSnackBar,
    public readonly dialog: MatDialog,
    public attachmentService: AttachmentService,
    public spatialService: SpatialService
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

  ngOnInit(): void {
    this.loadProjectAttachments();
  }

  loadProjectAttachments(): void {
    if (this.projectGuid) {
      this.attachmentService.getProjectAttachments(this.projectGuid).subscribe({
        next: (response) => {
          console.log('Project Attachments Response:', response);

          if (response?._embedded?.fileAttachment && Array.isArray(response._embedded.fileAttachment)) {
            const fileAttachment = response._embedded.fileAttachment[0];

            // Now, call getProjectBoundaries to get the boundary data
            this.projectService.getProjectBoundaries(this.projectGuid).subscribe({
              next: (boundaryResponse) => {
                console.log('Project Boundaries Response:', boundaryResponse);

                // Check if the response has the projectBoundary array and access the first boundary
                const boundarySizeHa = boundaryResponse?._embedded?.projectBoundary?.[0]?.boundarySizeHa;

                if (boundarySizeHa !== undefined) {
                  // Set the polygonHectares from the boundarySizeHa
                  fileAttachment.polygonHectares = boundarySizeHa;

                  // Push the fileAttachment with the updated polygonHectares to projectFiles
                  this.projectFiles = [];
                  this.projectFiles.push(fileAttachment);
                  console.log('Updated Project Files:', this.projectFiles);
                } else {
                  console.error('boundarySizeHa not found in project boundaries');
                }

                // Refresh the table data source
                this.dataSource.data = []
                this.dataSource.data = [...this.projectFiles];
              },
              error: (error) => {
                console.error('Error fetching project boundaries', error);
              }
            });
          } else {
            console.error('Expected an array of project files inside _embedded.fileAttachment, but got:', response);
            this.projectFiles = [];
          }
        },
        error: (err) => {
          this.snackbarService.open('Failed to load project attachments.', 'Close', {
            duration: 5000,
            panelClass: 'snackbar-error',
          });
        },
      });
    }
  }

  openFileUploadModal() {
    const dialogRef = this.dialog.open(AddAttachmentComponent, {
      width: '1000px',
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result?.file) {
        this.uploadFile(result.file);
      }
      if (result?.description) {
        this.attachmentDescription = result.description;
      }
    })
  }

  uploadFile(file: File): void {
    this.projectService.uploadDocument({ file }).subscribe({
      next: (response) => {
        if (response) {
          this.uploadAttachment(file, response);
        }
      },
      error: () => {
        this.snackbarService.open('The spatial file was not uploaded.', 'Close', {
          duration: 5000,
          panelClass: 'snackbar-error',
        });
      }
    });
  }

  uploadAttachment(file: File, response: any): void {
    const fileExtension = file.name.split('.').pop()?.toLowerCase();

    if (!fileExtension) {
      this.snackbarService.open('The spatial file was not uploaded because the file format is not accepted.', 'Close', {
        duration: 10000,
        panelClass: ['snackbar-error'],
      });
      return;
    }

    const attachment: FileAttachment = {
      sourceObjectNameCode: { sourceObjectNameCode: "PROJECT" },
      sourceObjectUniqueId: this.projectGuid,
      documentPath: file.name,
      fileIdentifier: response.fileId,
      attachmentContentTypeCode: { attachmentContentTypeCode: "DOCUMENT" },
      attachmentDescription: this.attachmentDescription,
      attachmentReadOnlyInd: false,
    };

    this.attachmentService.createProjectAttachment(this.projectGuid, attachment).subscribe({
      next: (response) => {
        if (response) {
          this.uploadedBy = response?.uploadedByUserId;

          this.spatialService.extractCoordinates(file).then(response => {
            if (response) {
              this.updateProjectBoundary(file, response)
            }
          })

          this.loadProjectAttachments();
        }
      },
      error: () => {
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
      boundaryGeometry: {
        type: "MultiPolygon",
        coordinates: response,
      }
    };

    this.projectService.createProjectBoundary(this.projectGuid, boundary).pipe(
      map((resp: any) => resp),
      catchError((error) => {
        console.error("Error creating project boundary", error);
        return throwError(() => new Error("Failed to create project boundary"));
      })
    ).subscribe({
      next: () => {
        this.snackbarService.open('File uploaded successfully.', 'Close', {
          duration: 5000,
          panelClass: 'snackbar-success',
        });
      },
      error: () => {
        this.snackbarService.open('Failed to update project boundary.', 'Close', {
          duration: 5000,
          panelClass: 'snackbar-error',
        });
      }
    });
  }

  deleteFile(fileToDelete: ProjectFile): void {
    // Open the confirmation dialog
    const dialogRef = this.dialog.open(ConfirmationDialogComponent, {
      data: { indicator: 'confirm-delete-attachment' },
      width: '500px',
    });
  
    dialogRef.afterClosed().subscribe((confirmed: boolean) => {
      if (confirmed) {
        if (fileToDelete?.fileAttachmentGuid) {
          this.attachmentService.deleteProjectAttachment(this.projectGuid, fileToDelete.fileAttachmentGuid).subscribe({
            next: () => {
              this.projectFiles = this.projectFiles.filter(file => file !== fileToDelete);
              this.dataSource.data = [...this.projectFiles];
  
              // Show success message in snackbar
              this.snackbarService.open('File has been deleted successfully.', 'Close', {
                duration: 5000,
                panelClass: 'snackbar-success',
              });
            },
            error: (error) => {
              // Handle any error during the deletion process
              console.error('Error deleting the file:', error);
              this.snackbarService.open('Failed to delete the file. Please try again.', 'Close', {
                duration: 5000,
                panelClass: 'snackbar-error',
              });
            }
          });
        } else {
          // If fileAttachmentGuid is not defined, handle the case gracefully
          console.error('File attachment GUID is missing or undefined');
          this.snackbarService.open('Failed to delete the file due to missing GUID.', 'Close', {
            duration: 5000,
            panelClass: 'snackbar-error',
          });
        }
      }
    });
  }
  

  downloadFile(file: any) {
    // Implementation for file download
  }
}
