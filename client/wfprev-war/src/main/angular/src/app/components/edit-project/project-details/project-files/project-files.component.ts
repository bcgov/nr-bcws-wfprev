import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatTableDataSource, MatTableModule } from '@angular/material/table';
import { catchError, map, throwError } from 'rxjs';
import { AddAttachmentComponent } from 'src/app/components/add-attachment/add-attachment.component';
import { ConfirmationDialogComponent } from 'src/app/components/confirmation-dialog/confirmation-dialog.component';
import { ActivityBoundary, FileAttachment, ProjectBoundary, ProjectFile } from 'src/app/components/models';
import { AttachmentService } from 'src/app/services/attachment-service';
import { ProjectService } from 'src/app/services/project-services';
import { SpatialService } from 'src/app/services/spatial-services';
import { Messages } from 'src/app/utils/messages';
import { Position } from 'geojson';
import { ActivatedRoute } from '@angular/router';

@Component({
  selector: 'app-project-files',
  standalone: true,
  imports: [MatTableModule, CommonModule],
  templateUrl: './project-files.component.html',
  styleUrls: ['./project-files.component.scss']
})
export class ProjectFilesComponent implements OnInit {
  @Output() filesUpdated = new EventEmitter<void>();
  @Input() projectGuid: string = '';
  @Input() activityGuid: string = '';
  @Input() fiscalGuid: string = '';
  attachmentDescription: string = '';
  uploadedBy = '';

  projectFiles: ProjectFile[] = [];
  dataSource = new MatTableDataSource<ProjectFile>(this.projectFiles);

  constructor(
    public projectService: ProjectService,
    private readonly snackbarService: MatSnackBar,
    public readonly dialog: MatDialog,
    public attachmentService: AttachmentService,
    public spatialService: SpatialService,
    private route: ActivatedRoute,
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
    if (this.activityGuid && this.fiscalGuid) {
      this.loadActivityAttachments();
    } else if (this.projectGuid) {
      this.loadProjectAttachments();
    }
  }

  loadProjectAttachments(): void {
    if (this.projectGuid) {
      this.attachmentService.getProjectAttachments(this.projectGuid).subscribe({
        next: (response) => {
          // ensure the latest attachment is displayed
          if (response?._embedded?.fileAttachment && Array.isArray(response._embedded.fileAttachment)) {
            const fileAttachment = response._embedded.fileAttachment.reduce((latest: any, current: any) => {
              const latestTime = new Date(latest.uploadedByTimestamp || 0).getTime();
              const currentTime = new Date(current.uploadedByTimestamp || 0).getTime();
              return currentTime > latestTime ? current : latest;
            });

            // Now, call getProjectBoundaries to get the boundary data
            this.projectService.getProjectBoundaries(this.projectGuid).subscribe({
              next: (boundaryResponse) => {
                const boundaries = boundaryResponse?._embedded?.projectBoundary;
                let boundarySizeHa = undefined;

                if (boundaries && boundaries.length > 0) {
                  // Sort boundaries by systemStartTimestamp in descending order
                  const latestBoundary = boundaries.sort((a: { systemStartTimestamp: string | number | Date; }, b: { systemStartTimestamp: string | number | Date; }) =>
                    new Date(b.systemStartTimestamp).getTime() - new Date(a.systemStartTimestamp).getTime()
                  )[0];
                  boundarySizeHa = latestBoundary.boundarySizeHa;
                }

                if (boundarySizeHa !== undefined) {
                  // Set the polygonHectares from the boundarySizeHa
                  fileAttachment.polygonHectares = boundarySizeHa;
                } else {
                  console.error('boundarySizeHa not found in project boundaries');
                }

                // Push the fileAttachment with the updated polygonHectares to projectFiles
                this.projectFiles = [];
                this.projectFiles.push(fileAttachment);

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

  loadActivityAttachments(): void {
    if (!this.fiscalGuid || !this.activityGuid) return;
    this.projectGuid = this.route.snapshot?.queryParamMap?.get('projectGuid') || '';
    // Find projectPlanFiscalGuid from the activityGuid
    this.attachmentService.getActivityAttachments(this.projectGuid, this.fiscalGuid, this.activityGuid).subscribe({
      next: (response) => {
        if (response?._embedded?.fileAttachment && Array.isArray(response._embedded.fileAttachment)) {
          const fileAttachment = response._embedded.fileAttachment.reduce((latest: any, current: any) => {
            const latestTime = new Date(latest.uploadedByTimestamp || 0).getTime();
            const currentTime = new Date(current.uploadedByTimestamp || 0).getTime();
            return currentTime > latestTime ? current : latest;
          });
  
          this.projectFiles = [fileAttachment];
          this.dataSource.data = [...this.projectFiles];
        } else {
          console.error('Expected an array of activity files inside _embedded.fileAttachment, but got:', response);
          this.projectFiles = [];
        }
      },
      error: (err) => {
        this.snackbarService.open('Failed to load activity attachments.', 'Close', {
          duration: 5000,
          panelClass: 'snackbar-error',
        });
      }
    });
  }
  
  openFileUploadModal() {
    const dialogRef = this.dialog.open(AddAttachmentComponent, {
      width: '1000px',
      data: {
        indicator: this.isActivityContext ? 'activity-files' : 'project-files'
      }
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
        this.snackbarService.open('Could not reach file upload server.', 'Close', {
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
      sourceObjectNameCode: { sourceObjectNameCode: this.isActivityContext? "ACTIVITY" : "PROJECT" },
      sourceObjectUniqueId: this.isActivityContext? this.projectGuid : this.activityGuid,
      documentPath: file.name,
      fileIdentifier: response.fileId,
      attachmentContentTypeCode: { attachmentContentTypeCode: "DOCUMENT" },
      attachmentDescription: this.attachmentDescription,
      attachmentReadOnlyInd: false,
    };

    if (this.isActivityContext && this.projectGuid) {
      // Activity level
      this.attachmentService.createActivityAttachment(this.projectGuid, this.fiscalGuid, this.activityGuid, attachment).subscribe({
        next: (response) => {
          if (response) {
            this.uploadedBy = response?.uploadedByUserId;

            this.spatialService.extractCoordinates(file).then(response => {
              if (response) {
                this.updateActivityBoundary(file, response)
              }
            })
          }
        }
      })
    } else {
      // Project level
        this.attachmentService.createProjectAttachment(this.projectGuid, attachment).subscribe({
          next: (response) => {
            if (response) {
              this.uploadedBy = response?.uploadedByUserId;

              this.spatialService.extractCoordinates(file).then(response => {
                if (response) {
                  this.updateProjectBoundary(file, response)
                }
              })
            }
          },
          error: (error) => {
            console.log('Failed to upload attachment: ', error)
          },
        });
      }
  }

  updateProjectBoundary(file: File, response: Position[][][]) {
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
        this.loadProjectAttachments();
        this.filesUpdated.emit();
      },
      error: (error) => {
        console.error('Failed to upload project geometry: ', error)
      }
    });
  }

  updateActivityBoundary(file: File, response: Position[][][]) {
    const now = new Date();
    const futureDate = new Date(now);
    futureDate.setFullYear(futureDate.getFullYear() + 1);

    const boundary: ActivityBoundary = {
      activityGuid: this.activityGuid,
      systemStartTimestamp: now.toISOString(),
      systemEndTimestamp: futureDate.toISOString(),
      collectionDate: now.toISOString().split('T')[0],
      collectorName: this.uploadedBy,
      plannedSpendAmount: 20000, // hardcode for now, should use the value from activity
      geometry: {
        type: "MultiPolygon",
        coordinates: response,
      }
    };

    this.projectService.createActivityBoundary(this.projectGuid, this.fiscalGuid, this.activityGuid, boundary).pipe(
      map((resp: any) => resp),
      catchError((error) => {
        console.error("Error creating activity boundary", error);
        return throwError(() => new Error("Failed to create activity boundary"));
      })
    ).subscribe({
      next: () => {
        this.snackbarService.open('File uploaded successfully.', 'Close', {
          duration: 5000,
          panelClass: 'snackbar-success',
        });
        this.loadActivityAttachments();
        this.filesUpdated.emit();
      },
      error: (error) => {
        console.error('Failed to upload activity boundary: ', error)
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

              this.projectService.getProjectBoundaries(this.projectGuid).subscribe(response => {
                const boundaries = response?._embedded?.projectBoundary;

                if (boundaries && boundaries.length > 0) {
                  const latest = boundaries.sort((a: any, b: any) =>
                    new Date(b.systemStartTimestamp).getTime() - new Date(a.systemStartTimestamp).getTime()
                  )[0];

                  const boundaryGuid = latest.projectBoundaryGuid;
                  this.projectService.deleteProjectBoundary(this.projectGuid, boundaryGuid).subscribe({
                    next: () => {
                      this.filesUpdated.emit();
                      // Show success message in snackbar
                      this.snackbarService.open('File has been deleted successfully.', 'Close', {
                        duration: 5000,
                        panelClass: 'snackbar-success',
                      });
                      this.loadProjectAttachments();
                    }
                  })

                } else {
                  console.log('No boundaries found');
                }

              })
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

  get isActivityContext(): boolean {
    return !!this.activityGuid && !!this.fiscalGuid;
  }

  downloadFile(file: any) {
    // Implementation for file download
  }
}
