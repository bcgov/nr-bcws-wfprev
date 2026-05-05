import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarRef, SimpleSnackBar } from '@angular/material/snack-bar';
import { MatTableDataSource, MatTableModule } from '@angular/material/table';
import { MatTooltipModule } from '@angular/material/tooltip';
import { ActivatedRoute } from '@angular/router';
import { Position } from 'geojson';
import { catchError, finalize, map, throwError } from 'rxjs';
import { AddAttachmentComponent } from 'src/app/components/add-attachment/add-attachment.component';
import { ConfirmationDialogComponent } from 'src/app/components/confirmation-dialog/confirmation-dialog.component';
import { DetailedErrorMessageComponent } from 'src/app/components/detailed-error-message/detailed-error-message.component';
import { ActivityBoundary, FileAttachment, ProjectBoundary, ProjectFile } from 'src/app/components/models';
import { IconButtonComponent } from 'src/app/components/shared/icon-button/icon-button.component';
import { AttachmentService } from 'src/app/services/attachment-service';
import { ProjectService } from 'src/app/services/project-services';
import { SpatialService } from 'src/app/services/spatial-services';
import { Messages, ModalMessages, ModalTitles } from 'src/app/utils/constants';

@Component({
  selector: 'wfprev-project-files',
  standalone: true,
  imports: [MatTableModule, MatTooltipModule, CommonModule, IconButtonComponent, MatProgressSpinnerModule],
  templateUrl: './project-files.component.html',
  styleUrls: ['./project-files.component.scss']
})
export class ProjectFilesComponent implements OnInit {
  @Output() filesUpdated = new EventEmitter<void>();
  @Input() projectGuid: string = '';
  @Input() activityGuid: string = '';
  @Input() fiscalGuid: string = '';
  @Input() isReadonly: boolean = false;
  @Input() isDisabledButton: boolean = false;
  attachmentDescription: string = '';
  uploadedBy = '';

  projectFiles: ProjectFile[] = [];
  dataSource = new MatTableDataSource<ProjectFile>(this.projectFiles);
  downloadingFileId: string | null = null;
  isLoading = true;

  errorMessageContext = {
    data: {
      title: 'Spatial File Failed to Save',
      messageBefore: "The file that you are uploading failed to save. To view errors click on 'View Details' button on this warning to see additional error details.",
      messageAfter: 'The file that you are uploading failed to save due to the following errors:'
    },
    duration: undefined,
    panelClass: ['detailed-error-message']
  }

  constructor(
    public projectService: ProjectService,
    private readonly snackbarService: MatSnackBar,
    public readonly dialog: MatDialog,
    public attachmentService: AttachmentService,
    public spatialService: SpatialService,
    private readonly route: ActivatedRoute,
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
    } else {
      this.isLoading = false;
    }
  }

  loadProjectAttachments(): void {
    if (this.projectGuid) {
      this.isLoading = true;
      this.attachmentService.getProjectAttachments(this.projectGuid)
        .pipe(finalize(() => this.isLoading = false))
        .subscribe({
          next: (response: any) => {
            // ensure the latest attachment is displayed
            if (response?._embedded?.fileAttachment && Array.isArray(response._embedded.fileAttachment)) {
              const fileAttachments = response._embedded.fileAttachment.sort((a: FileAttachment, b: FileAttachment) => {
                return new Date(b.uploadedByTimestamp ?? 0).getTime() - new Date(a.uploadedByTimestamp ?? 0).getTime();
              });

              // Set initial files without boundaries
              this.projectFiles = [...fileAttachments];
              this.dataSource.data = [...this.projectFiles];

              this.projectService.getProjectBoundaries(this.projectGuid).subscribe({
                next: (boundaryResponse: any) => {
                  const boundaries = boundaryResponse?._embedded?.projectBoundary;

                  if (boundaries && boundaries.length > 0) {
                    const boundarySizeMap = new Map<string, number>();
                    boundaries.forEach((boundary: { projectBoundaryGuid: string; boundarySizeHa: number }) => {
                      boundarySizeMap.set(boundary.projectBoundaryGuid, boundary.boundarySizeHa);
                    });

                    this.projectFiles = fileAttachments.map((file: FileAttachment) => ({
                      ...file,
                      polygonHectares: file.sourceObjectUniqueId ? boundarySizeMap.get(file.sourceObjectUniqueId) ?? null : null
                    }));

                    this.dataSource.data = [...this.projectFiles];
                  }
                },
                error: (error) => {
                  console.error('Error fetching project boundaries', error);
                }
              });
            } else {
              console.error('Expected an array of project files inside _embedded.fileAttachment, but got:', response);
              this.projectFiles = [];
              this.dataSource.data = [];
            }
          },
          error: (err) => {
            this.snackbarService.open('Failed to load project attachments.', 'Close', {
              duration: 5000,
              panelClass: 'snackbar-error',
            });
          }
        });
    } else {
      this.isLoading = false;
    }
  }

  loadActivityAttachments(): void {
    if (!this.fiscalGuid || !this.activityGuid) {
      this.isLoading = false;
      return;
    }
    this.projectGuid = this.route.snapshot?.queryParamMap?.get('projectGuid') ?? '';

    this.isLoading = true;
    this.attachmentService.getActivityAttachments(this.projectGuid, this.fiscalGuid, this.activityGuid)
      .pipe(finalize(() => this.isLoading = false))
      .subscribe({
        next: (response: any) => {
          if (response?._embedded?.fileAttachment && Array.isArray(response._embedded.fileAttachment)) {
            const fileAttachments = response._embedded.fileAttachment.sort((a: FileAttachment, b: FileAttachment) => {
              const timeA = new Date(a.uploadedByTimestamp ?? 0).getTime();
              const timeB = new Date(b.uploadedByTimestamp ?? 0).getTime();
              return timeB - timeA; // latest first
            });

            // Set initial files
            this.projectFiles = [...fileAttachments];
            this.dataSource.data = [...this.projectFiles];

            // Fetch activity boundaries to enrich files with hectares
            this.projectService.getActivityBoundaries(this.projectGuid, this.fiscalGuid, this.activityGuid).subscribe({
              next: (boundaryResponse: any) => {
                const boundaries = boundaryResponse?._embedded?.activityBoundary;

                if (boundaries && boundaries.length > 0) {
                  const boundarySizeMap = new Map<string, number>();
                  boundaries.forEach((boundary: { activityBoundaryGuid: string, boundarySizeHa: number }) => {
                    boundarySizeMap.set(boundary.activityBoundaryGuid, boundary.boundarySizeHa);
                  });

                  this.projectFiles = fileAttachments.map((file: FileAttachment) => ({
                    ...file,
                    polygonHectares: file.sourceObjectUniqueId ? boundarySizeMap.get(file.sourceObjectUniqueId) ?? null : null
                  }));

                  this.dataSource.data = [...this.projectFiles];
                }
              },
              error: (error) => {
                console.error('Failed to load activity boundaries:', error);
              }
            });
          } else {
            console.error('Expected an array of activity files inside _embedded.fileAttachment, but got:', response);
            this.projectFiles = [];
            this.dataSource.data = [];
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
        const selectedType = result.type;
        this.uploadFile(result.file, selectedType);
      }
      if (result?.description) {
        this.attachmentDescription = result.description;
      }
    })
  }

  uploadFile(file: File, type: string): void {
    const snackRef = this.snackbarService.open(Messages.fileUploadInProgress, 'Close', {
      duration: undefined,
      panelClass: 'snackbar-info',
    });

    if(this.isValidFileExtension(file)) {
      this.projectService.uploadDocument({ file }).subscribe({
        next: (response) => {
          if (response) {
            this.uploadAttachment(file, response, type, snackRef);
          }
        },
        error: () => {
          this.snackbarService.openFromComponent(DetailedErrorMessageComponent, {
            ...this.errorMessageContext,
            data: {
              ...this.errorMessageContext.data,
              reasons: ['Could not reach file upload server.']
            }
          }
          );
        }
      });
    }
  }

  private isValidFileExtension(file: File): boolean {
    const name = file.name;
    const lastDot = name.lastIndexOf('.');
  
    if (lastDot === -1) {
      this.snackbarService.openFromComponent(DetailedErrorMessageComponent, {
        ...this.errorMessageContext,
        data: {
          ...this.errorMessageContext.data,
          reasons: ['The selected file format is not supported. Please upload a valid file type (KML, KMZ, ZIP, GDB, SHP).']
        }
      }
      );
      return false;
    } 
    
    const fileExtension = name.substring(lastDot + 1).toLowerCase();

    if (!fileExtension.match(/zip|gdb|kml|kmz|shp/)) {
      this.snackbarService.openFromComponent(DetailedErrorMessageComponent, {
        ...this.errorMessageContext,
        data: {
          ...this.errorMessageContext.data,
          reasons: ['The selected file format is not supported. Please upload a valid file type (KML, KMZ, ZIP, GDB, SHP).']
        }
      }
      );
      return false;
    }

    return true;
  }

  uploadAttachment(file: File, fileUploadResp: any, type: string, snackRef: MatSnackBarRef<SimpleSnackBar>): void {
    const fileExtension = file.name.split('.').pop()?.toLowerCase();
    if (!fileExtension) {
      this.snackbarService.openFromComponent(DetailedErrorMessageComponent, {
        ...this.errorMessageContext,
        data: {
          ...this.errorMessageContext.data,
          reasons: ['The spatial file was not uploaded because the file format is not accepted.']
        }
      }
      );
      return;
    }

    if ((type === 'OTHER' || type === 'DOCUMENT') || !fileExtension.match(/zip|gdb|kml|kmz|shp/)) {
      this.finishWithoutGeometry(file, fileUploadResp, type);
      return;
    }

    this.spatialService.extractCoordinates(file).then((geometry) => {
      if (!geometry) {
        snackRef.dismiss();
        this.snackbarService.openFromComponent(DetailedErrorMessageComponent, {
          ...this.errorMessageContext,
          data: {
            ...this.errorMessageContext.data,
            reasons: ['Could not extract geometry from spatial file.']
          }
        }
        );
        return;
      }

      const now = new Date();
      const futureDate = new Date(now);
      futureDate.setFullYear(futureDate.getFullYear() + 1);

      if (this.isActivityContext && this.projectGuid) {
        // Create Activity boundary 
        const activityBoundary: ActivityBoundary = {
          activityGuid: this.activityGuid,
          systemStartTimestamp: now.toISOString(),
          systemEndTimestamp: futureDate.toISOString(),
          collectionDate: now.toISOString().split('T')[0],
          collectorName: this.uploadedBy,
          plannedSpendAmount: 20000,
          boundarySizeHa: 0,
          geometry: {
            type: "MultiPolygon",
            coordinates: geometry,
          }
        };

        this.projectService.createActivityBoundary(this.projectGuid, this.fiscalGuid, this.activityGuid, activityBoundary).subscribe({
          next: (boundaryResp) => {
            const boundaryGuid = boundaryResp?.activityBoundaryGuid;
            const attachment: FileAttachment = {
              sourceObjectNameCode: { sourceObjectNameCode: "TREATMENT_ACTIVITY" },
              sourceObjectUniqueId: boundaryGuid,
              documentPath: file.name,
              fileIdentifier: fileUploadResp.fileId,
              attachmentContentTypeCode: { attachmentContentTypeCode: type },
              attachmentDescription: this.attachmentDescription,
              attachmentReadOnlyInd: false,
            };

            this.attachmentService.createActivityAttachment(this.projectGuid, this.fiscalGuid, this.activityGuid, attachment).subscribe({
              next: () => {
                snackRef.dismiss();
                this.snackbarService.open(Messages.fileUploadSuccess, 'Close', {
                  duration: 5000,
                  panelClass: 'snackbar-success',
                });
                this.loadActivityAttachments();
                this.filesUpdated.emit();
              }
            });
          },
          error: (err) => {
            console.error('Failed to create activity boundary', err);
          }
        });

      } else {
        // Create Project boundary
        const projectBoundary: ProjectBoundary = {
          projectGuid: this.projectGuid,
          systemStartTimestamp: now.toISOString(),
          systemEndTimestamp: futureDate.toISOString(),
          collectionDate: now.toISOString().split('T')[0],
          collectorName: this.uploadedBy,
          boundaryGeometry: {
            type: "MultiPolygon",
            coordinates: geometry,
          }
        };

        this.projectService.createProjectBoundary(this.projectGuid, projectBoundary).subscribe({
          next: (boundaryResp) => {
            const boundaryGuid = boundaryResp?.projectBoundaryGuid;
            const attachment: FileAttachment = {
              sourceObjectNameCode: { sourceObjectNameCode: "PROJECT" },
              sourceObjectUniqueId: boundaryGuid,
              documentPath: file.name,
              fileIdentifier: fileUploadResp.fileId,
              attachmentContentTypeCode: { attachmentContentTypeCode: type },
              attachmentDescription: this.attachmentDescription,
              attachmentReadOnlyInd: false,
            };

            this.attachmentService.createProjectAttachment(this.projectGuid, attachment).subscribe({
              next: () => {
                snackRef.dismiss();
                this.snackbarService.open(Messages.fileUploadSuccess, 'Close', {
                  duration: 5000,
                  panelClass: 'snackbar-success',
                });
                this.loadProjectAttachments();
                this.filesUpdated.emit();
              },
              error: (err) => {
                console.error('Failed to create project attachment', err);
              }
            });
          },
          error: (err) => {
            console.error('Failed to create project boundary', err);
          }
        });
      }
    }).catch((error) => {
      snackRef.dismiss();
      console.error('Error extracting coordinates:', error);
      this.snackbarService.openFromComponent(DetailedErrorMessageComponent, {
        ...this.errorMessageContext,
        data: {
          ...this.errorMessageContext.data,
          reasons: ['Failed to process spatial file. ' + error.message]
        }
      }
      );
    });
  }

  finishWithoutGeometry(file: File, fileUploadResp: any, type: string) {
    const attachment: FileAttachment = {
      sourceObjectNameCode: { sourceObjectNameCode: this.isActivityContext ? 'TREATMENT_ACTIVITY' : 'PROJECT' },
      sourceObjectUniqueId: this.isActivityContext ? this.activityGuid : this.projectGuid,
      documentPath: file.name,
      fileIdentifier: fileUploadResp.fileId,
      attachmentContentTypeCode: { attachmentContentTypeCode: type },
      attachmentDescription: this.attachmentDescription,
      attachmentReadOnlyInd: false,
    };

    const create$ = this.isActivityContext
      ? this.attachmentService.createActivityAttachment(this.projectGuid, this.fiscalGuid, this.activityGuid, attachment)
      : this.attachmentService.createProjectAttachment(this.projectGuid, attachment);

    create$.subscribe({
      next: () => {
        this.snackbarService.open(Messages.fileUploadSuccess, 'Close', {
          duration: 5000,
          panelClass: 'snackbar-success',
        });
        this.isActivityContext ? this.loadActivityAttachments() : this.loadProjectAttachments();
        this.filesUpdated.emit();
      },
      error: (err) => {
        console.error('Failed to create attachment', err);
        this.snackbarService.openFromComponent(DetailedErrorMessageComponent, {
          ...this.errorMessageContext,
          data: {
            ...this.errorMessageContext.data,
            reasons: ['Failed to create attachment.']
          }
        }
        );
      }
    });
  }


  createProjectBoundary(file: File, response: Position[][][]) {
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
      map((resp: ProjectBoundary) => resp),
      catchError((error) => {
        console.error("Error creating project boundary", error);
        return throwError(() => new Error("Failed to create project boundary"));
      })
    ).subscribe({
      next: () => {
        this.snackbarService.open(Messages.fileUploadSuccess, 'Close', {
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

  createActivityBoundary(file: File, response: Position[][][]) {
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
      boundarySizeHa: 0,// hardcode for now
      geometry: {
        type: "MultiPolygon",
        coordinates: response,
      }
    };

    this.projectService.createActivityBoundary(this.projectGuid, this.fiscalGuid, this.activityGuid, boundary).pipe(
      map((resp: ActivityBoundary) => resp),
      catchError((error) => {
        console.error("Error creating activity boundary", error);
        return throwError(() => new Error("Failed to create activity boundary"));
      })
    ).subscribe({
      next: () => {
        this.snackbarService.open(Messages.fileUploadSuccess, 'Close', {
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
      data: {
        indicator: 'delete-attachment',
        title: ModalTitles.DELETE_ATTACHMENT_TITLE,
        message: ModalMessages.DELETE_ATTACHMENT_MESSAGE
      },
      width: '600px',
    });

    dialogRef.afterClosed().subscribe((confirmed: boolean) => {
      if (confirmed) {
        this.snackbarService.open(Messages.fileDeleteInProgress, 'Close', {
          duration: undefined,
          panelClass: 'snackbar-info',
        });
        if (fileToDelete?.fileAttachmentGuid) {

          if (this.isActivityContext) {
            // delete activity attachment
            this.attachmentService.deleteActivityAttachments(this.projectGuid, this.fiscalGuid, this.activityGuid, fileToDelete.fileAttachmentGuid).subscribe({
              next: () => {
                this.projectFiles = this.projectFiles.filter(file => file !== fileToDelete);
                this.dataSource.data = [...this.projectFiles];

                const typeCode = fileToDelete.attachmentContentTypeCode?.attachmentContentTypeCode;
                if (typeCode === 'MAP' && fileToDelete.sourceObjectUniqueId) {
                  // only run boundary deletion logic for MAP files
                  this.projectService.deleteActivityBoundary(this.projectGuid, this.fiscalGuid, this.activityGuid, fileToDelete.sourceObjectUniqueId).subscribe({
                    next: () => {
                      this.filesUpdated.emit();
                      // Show success message in snackbar
                      this.snackbarService.open('File has been deleted successfully.', 'Close', {
                        duration: 5000,
                        panelClass: 'snackbar-success',
                      });
                      this.loadActivityAttachments();
                    },
                    error: (err) => {
                      console.error('Failed to delete activity boundary', err);
                      this.snackbarService.open('Failed to delete the boundary.', 'Close', {
                        duration: 5000,
                        panelClass: 'snackbar-warning',
                      });
                      // Still reload attachments as the file might be gone
                      this.loadActivityAttachments();
                    }
                  });
                } else {
                  this.filesUpdated.emit();
                  this.snackbarService.open('File has been deleted successfully.', 'Close', {
                    duration: 5000,
                    panelClass: 'snackbar-success',
                  });
                  this.loadActivityAttachments();
                }
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
            // delete project attachment
            this.attachmentService.deleteProjectAttachment(this.projectGuid, fileToDelete.fileAttachmentGuid).subscribe({
              next: () => {
                this.projectFiles = this.projectFiles.filter(file => file !== fileToDelete);
                this.dataSource.data = [...this.projectFiles];

                const typeCode = fileToDelete.attachmentContentTypeCode?.attachmentContentTypeCode;
                if (typeCode === 'MAP' && fileToDelete.sourceObjectUniqueId) {
                  this.projectService.deleteProjectBoundary(this.projectGuid, fileToDelete.sourceObjectUniqueId).subscribe({
                    next: () => {
                      this.filesUpdated.emit();
                      // Show success message in snackbar
                      this.snackbarService.open('File has been deleted successfully.', 'Close', {
                        duration: 5000,
                        panelClass: 'snackbar-success',
                      });
                      this.loadProjectAttachments();
                    },
                    error: (err) => {
                      console.error('Failed to delete project boundary', err);
                      this.snackbarService.open('Failed to delete the boundary.', 'Close', {
                        duration: 5000,
                        panelClass: 'snackbar-warning',
                      });
                      this.loadProjectAttachments();
                    }
                  });
                } else {
                  this.filesUpdated.emit();
                  this.snackbarService.open('File has been deleted successfully.', 'Close', {
                    duration: 5000,
                    panelClass: 'snackbar-success',
                  });
                  this.loadProjectAttachments();
                }
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
          }
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

  downloadFile(file: FileAttachment): void {
    if (file.fileIdentifier) {
      this.downloadingFileId = file.fileIdentifier;
      const snackRef = this.snackbarService.open(Messages.fileDownloadInProgress, 'Close', {
        duration: undefined,
        panelClass: 'snackbar-info'
      });
      this.projectService.downloadDocument(file.fileIdentifier).subscribe({
        next: (blob: Blob) => {
          snackRef.dismiss();
          this.downloadingFileId = null;
          const url = window.URL.createObjectURL(blob);
          const link = document.createElement('a');
          link.href = url;
          link.download = file.documentPath ?? 'downloaded-file'; // fallback filename
          document.body.appendChild(link);
          link.click();
          document.body.removeChild(link);
          window.URL.revokeObjectURL(url);
          this.snackbarService.open(Messages.fileDownloadSuccess, 'Close', {
            duration: 5000,
            panelClass: 'snackbar-success',
          });
        },
        error: (err) => {
          snackRef.dismiss();
          this.downloadingFileId = null;
          console.error('Download failed', err);
          this.snackbarService.open(Messages.fileDownloadFailure, 'Close', {
            duration: 5000,
            panelClass: 'snackbar-error',
          });
        }
      });
    } else {
      console.error('The file has no file Id');
      this.snackbarService.open(Messages.fileDownloadFailure, 'Close', {
        duration: 5000,
        panelClass: 'snackbar-error',
      });
    }
  }

  isDownloading(file: FileAttachment): boolean {
    return this.downloadingFileId === file.fileIdentifier;
  }

  translateAttachmentType(description: string): string {
    switch (description) {
      case 'Map':
        return this.isActivityContext ? 'Activity Polygon' : 'Project Boundary';
      case 'Document':
        return 'Prescription';
      default:
        return description;
    }
  }

  public get hasAttachments(): boolean {
    return Array.isArray(this.projectFiles) && this.projectFiles.length > 0;
  }
}
