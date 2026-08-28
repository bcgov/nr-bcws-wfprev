import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { MatIconModule, MatIconRegistry } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarRef, SimpleSnackBar } from '@angular/material/snack-bar';
import { MatTableDataSource, MatTableModule } from '@angular/material/table';
import { MatTooltipModule } from '@angular/material/tooltip';
import { DomSanitizer } from '@angular/platform-browser';
import { ActivatedRoute } from '@angular/router';
import { Position } from 'geojson';
import { catchError, finalize, forkJoin, map, of, throwError } from 'rxjs';
import { AddAttachmentComponent } from 'src/app/components/add-attachment/add-attachment.component';
import { ConfirmationDialogComponent } from 'src/app/components/confirmation-dialog/confirmation-dialog.component';
import { DetailedErrorMessageComponent } from 'src/app/components/detailed-error-message/detailed-error-message.component';
import { ActivityBoundary, AttachmentTypeCode, FileAttachment, ProjectBoundary, ProjectFile } from 'src/app/components/models';
import { IconButtonComponent } from 'src/app/components/shared/icon-button/icon-button.component';
import { AttachmentService } from 'src/app/services/attachment-service';
import { FileViewerService } from 'src/app/services/file-viewer.service';
import { ProjectService } from 'src/app/services/project-services';
import { SpatialService } from 'src/app/services/spatial-services';
import { Messages, ModalMessages, ModalTitles } from 'src/app/utils/constants';

@Component({
  selector: 'wfprev-project-files',
  standalone: true,
  imports: [MatTableModule, MatTooltipModule, CommonModule, IconButtonComponent, MatProgressSpinnerModule, MatIconModule],
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
  @Input() isTooltipDisabled: boolean = true;
  @Input() required: boolean = false;
  @Input() title: string = 'Files';
  @Input() showViewButton: boolean = false;
  @Input() showDeleteButton: boolean = true;
  @Input() isSummaryView: boolean = false;
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
    private readonly iconRegistry: MatIconRegistry,
    private readonly sanitizer: DomSanitizer,
    private fileViewerService: FileViewerService,
  ) {
    this.iconRegistry.addSvgIcon(
      'download',
      this.sanitizer.bypassSecurityTrustResourceUrl('assets/icons/download.svg')
    );
    this.iconRegistry.addSvgIcon(
      'view',
      this.sanitizer.bypassSecurityTrustResourceUrl('assets/icons/view.svg')
    );
    this.iconRegistry.addSvgIcon(
      'delete',
      this.sanitizer.bypassSecurityTrustResourceUrl('assets/icons/delete.svg')
    );
  }

  messages = Messages;
  displayedColumns: string[] = [
    'attachmentType',
    'fileName',
    'fileType',
    'uploadedBy',
    'uploadedDate',
    'polygonHectares',
    'description',
    'download'
  ];

  ngOnInit(): void {
    if (this.showViewButton) {
      this.displayedColumns.push('view');
    }
    if (this.showDeleteButton && !this.isSummaryView) {
      this.displayedColumns.push('delete');
    }

    this.loadFiles();
  }

  private get fileContext() {
    return this.isActivityContext
      ? {
        fetchAttachments: () => this.attachmentService.getActivityAttachments(this.projectGuid, this.fiscalGuid, this.activityGuid),
        fetchBoundaries: () => this.projectService.getActivityBoundaries(this.projectGuid, this.fiscalGuid, this.activityGuid),
        boundaryKey: 'activityBoundary',
        guidOf: (b: any) => b?.activityBoundaryGuid,
        geometryOf: (b: any) => b?.geometry,
        deleteBoundary: (guid: string) => this.projectService.deleteActivityBoundary(this.projectGuid, this.fiscalGuid, this.activityGuid, guid),
        loadErrorMessage: 'Failed to load activity attachments.',
        boundaryErrorMessage: 'Failed to load activity boundaries:'
      }
      : {
        fetchAttachments: () => this.attachmentService.getProjectAttachments(this.projectGuid),
        fetchBoundaries: () => this.projectService.getProjectBoundaries(this.projectGuid),
        boundaryKey: 'projectBoundary',
        guidOf: (b: any) => b?.projectBoundaryGuid,
        geometryOf: (b: any) => b?.boundaryGeometry,
        deleteBoundary: (guid: string) => this.projectService.deleteProjectBoundary(this.projectGuid, guid),
        loadErrorMessage: 'Failed to load project attachments.',
        boundaryErrorMessage: 'Failed to load project boundaries:'
      };
  }

  loadFiles(): void {
    if (this.isActivityContext) {
      this.projectGuid = this.route.snapshot?.queryParamMap?.get('projectGuid') ?? this.projectGuid;
    }
    if (!this.projectGuid) {
      this.isLoading = false;
      return;
    }

    const ctx = this.fileContext;
    this.isLoading = true;
    let attachmentsFailed = false;

    // Attachments and boundaries are fetched independently on purpose. Nesting the boundary
    // call inside the attachment response meant that an activity with no attachments never
    // fetched its boundaries at all - HATEOAS omits _embedded entirely for empty collections -
    // which is exactly the case an orphaned boundary falls into.
    forkJoin({
      attachments: ctx.fetchAttachments().pipe(catchError(() => { attachmentsFailed = true; return of(null); })),
      boundaries: ctx.fetchBoundaries().pipe(catchError((err: any) => {
        console.error(ctx.boundaryErrorMessage, err);
        return of(null);
      }))
    })
      .pipe(finalize(() => this.isLoading = false))
      .subscribe(({ attachments, boundaries }: any) => {
        if (attachmentsFailed) {
          this.snackbarService.open(ctx.loadErrorMessage, 'Close', {
            duration: 5000,
            panelClass: 'snackbar-error',
          });
        }

        this.projectFiles = this.buildRows(
          attachments?._embedded?.fileAttachment ?? [],
          boundaries?._embedded?.[ctx.boundaryKey] ?? [],
          ctx
        );
        this.dataSource.data = [...this.projectFiles];
      });
  }

  private buildRows(attachments: any[], boundaries: any[], ctx: any): ProjectFile[] {
    const sorted = [...attachments].sort((a: any, b: any) =>
      new Date(b.uploadedByTimestamp ?? 0).getTime() - new Date(a.uploadedByTimestamp ?? 0).getTime());

    const boundaryByGuid = new Map<string, any>();
    boundaries.forEach((b: any) => {
      const guid = ctx.guidOf(b);
      if (guid) boundaryByGuid.set(guid, b);
    });

    const claimed = new Set<string>();
    const rows: ProjectFile[] = sorted.map((file: any) => {
      const boundary = file.sourceObjectUniqueId ? boundaryByGuid.get(file.sourceObjectUniqueId) : undefined;
      if (boundary) claimed.add(file.sourceObjectUniqueId);
      return {
        ...file,
        polygonHectares: boundary?.boundarySizeHa ?? undefined,
        boundaryGeometry: boundary ? ctx.geometryOf(boundary) : undefined
      };
    });

    // Boundaries that no attachment points at. Without a row the geometry is unreachable:
    // the delete button lives on the attachment row, so the polygon stays on the map forever
    // with no way to remove it. Showing it lets a user clean it up themselves.
    const orphans: ProjectFile[] = boundaries
      .filter((b: any) => {
        const guid = ctx.guidOf(b);
        return guid && !claimed.has(guid);
      })
      .map((b: any) => this.buildOrphanBoundaryRow(b, ctx));

    return [...rows, ...orphans];
  }

  private buildOrphanBoundaryRow(boundary: any, ctx: any): ProjectFile {
    return {
      attachmentContentTypeCode: { attachmentContentTypeCode: 'MAP', description: 'Map' },
      sourceObjectUniqueId: ctx.guidOf(boundary),
      polygonHectares: boundary?.boundarySizeHa ?? undefined,
      boundaryGeometry: ctx.geometryOf(boundary),
      uploadedByUserId: boundary?.createUser ?? boundary?.collectorName,
      uploadedByTimestamp: boundary?.systemStartTimestamp ?? boundary?.createDate,
      attachmentDescription: Messages.orphanBoundaryDescription,
      isOrphanBoundary: true
    } as ProjectFile;
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

  uploadFile(file: File, type: AttachmentTypeCode): void {
    const snackRef = this.snackbarService.open(Messages.fileUploadInProgress, 'Close', {
      duration: undefined,
      panelClass: 'snackbar-info',
    });

    if (this.isValidFileExtension(file, type)) {
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

  private isValidFileExtension(file: File, type: AttachmentTypeCode): boolean {
    const name = file.name;
    const lastDot = name.lastIndexOf('.');

    if (lastDot === -1) {
      this.snackbarService.openFromComponent(DetailedErrorMessageComponent, {
        ...this.errorMessageContext,
        data: {
          ...this.errorMessageContext.data,
          reasons: ['The selected file format is not supported. Unable to determine file type.']
        }
      }
      );
      return false;
    }

    const fileExtension = name.substring(lastDot + 1).toLowerCase();

    if (type === 'MAP' && !fileExtension.match(/zip|gdb|kml|kmz|shp/)) {
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

  uploadAttachment(file: File, fileUploadResp: any, type: AttachmentTypeCode, snackRef: MatSnackBarRef<SimpleSnackBar>): void {
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
                this.loadFiles();
                this.filesUpdated.emit();
              },
              error: (err) => {
                // The boundary is already saved. Without this rollback it would be stranded:
                // no attachment means no row in the files table and no way to delete it.
                snackRef.dismiss();
                console.error('Failed to create activity attachment', err);
                this.rollbackBoundary(boundaryGuid, 'activity');
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
                this.loadFiles();
                this.filesUpdated.emit();
              },
              error: (err) => {
                // See the activity path: roll the boundary back so a failed upload
                // leaves nothing behind.
                snackRef.dismiss();
                console.error('Failed to create project attachment', err);
                this.rollbackBoundary(boundaryGuid, 'project');
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

  finishWithoutGeometry(file: File, fileUploadResp: any, type: AttachmentTypeCode) {
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
        this.loadFiles();
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
        this.loadFiles();
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
        this.loadFiles();
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
        // An orphan row has no attachment behind it - delete the boundary on its own.
        if (fileToDelete?.isOrphanBoundary) {
          this.deleteOrphanBoundary(fileToDelete);
          return;
        }

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
                      this.loadFiles();
                    },
                    error: (err) => {
                      console.error('Failed to delete activity boundary', err);
                      this.snackbarService.open('Failed to delete the boundary.', 'Close', {
                        duration: 5000,
                        panelClass: 'snackbar-warning',
                      });
                      // Still reload attachments as the file might be gone
                      this.loadFiles();
                    }
                  });
                } else {
                  this.filesUpdated.emit();
                  this.snackbarService.open('File has been deleted successfully.', 'Close', {
                    duration: 5000,
                    panelClass: 'snackbar-success',
                  });
                  this.loadFiles();
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
                      this.loadFiles();
                    },
                    error: (err) => {
                      console.error('Failed to delete project boundary', err);
                      this.snackbarService.open('Failed to delete the boundary.', 'Close', {
                        duration: 5000,
                        panelClass: 'snackbar-warning',
                      });
                      this.loadFiles();
                    }
                  });
                } else {
                  this.filesUpdated.emit();
                  this.snackbarService.open('File has been deleted successfully.', 'Close', {
                    duration: 5000,
                    panelClass: 'snackbar-success',
                  });
                  this.loadFiles();
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

  /**
   * Undo a half-completed spatial upload. The boundary and its attachment are two separate
   * calls with no transaction between them, so when the second fails the first has to be
   * removed explicitly or it becomes an orphan.
   */
  private rollbackBoundary(boundaryGuid: string | undefined, context: 'activity' | 'project'): void {
    const reasons = ['The spatial file could not be attached to this record.'];

    if (!boundaryGuid) {
      this.showUploadFailure(reasons);
      return;
    }

    const delete$ = context === 'activity'
      ? this.projectService.deleteActivityBoundary(this.projectGuid, this.fiscalGuid, this.activityGuid, boundaryGuid)
      : this.projectService.deleteProjectBoundary(this.projectGuid, boundaryGuid);

    delete$.subscribe({
      next: () => {
        this.showUploadFailure(reasons);
        this.loadFiles();
      },
      error: (rollbackErr: any) => {
        // Rollback itself failed - the boundary is now orphaned. It will still show in the
        // files table as a spatial-without-a-file row, so the user can remove it manually.
        console.error('Failed to roll back boundary after attachment failure', rollbackErr);
        this.showUploadFailure([...reasons, Messages.boundaryRollbackFailed]);
        this.loadFiles();
      }
    });
  }

  private showUploadFailure(reasons: string[]): void {
    this.snackbarService.openFromComponent(DetailedErrorMessageComponent, {
      ...this.errorMessageContext,
      data: {
        ...this.errorMessageContext.data,
        reasons
      }
    });
  }

  private deleteOrphanBoundary(fileToDelete: ProjectFile): void {
    const guid = fileToDelete.sourceObjectUniqueId;
    if (!guid) {
      this.snackbarService.open('Failed to delete the spatial due to missing GUID.', 'Close', {
        duration: 5000,
        panelClass: 'snackbar-error',
      });
      return;
    }

    this.fileContext.deleteBoundary(guid).subscribe({
      next: () => {
        this.snackbarService.open(Messages.orphanBoundaryDeleted, 'Close', {
          duration: 5000,
          panelClass: 'snackbar-success',
        });
        this.filesUpdated.emit();
        this.loadFiles();
      },
      error: (err: any) => {
        console.error('Failed to delete orphaned boundary', err);
        this.snackbarService.open('Failed to delete the spatial.', 'Close', {
          duration: 5000,
          panelClass: 'snackbar-error',
        });
        this.loadFiles();
      }
    });
  }

  /**
   * Orphan rows have no fileIdentifier - the attachment row that held it is gone and there is
   * no file_attachment audit table to recover it from - so the file cannot be fetched even
   * though WFDM still holds it. Disable the control rather than letting it silently no-op.
   */
  canDownloadFile(file: ProjectFile): boolean {
    return !file?.isOrphanBoundary && !!file?.fileIdentifier;
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

  canViewFile(file: ProjectFile): boolean {
    return this.fileViewerService.canView(file);
  }

  viewFile(file: ProjectFile): void {
    this.fileViewerService.viewFile(file);
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
