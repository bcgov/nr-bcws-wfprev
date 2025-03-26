import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, Input } from '@angular/core';
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
import { ZipReader, BlobReader, TextWriter, BlobWriter, Uint8ArrayWriter } from '@zip.js/zip.js';
import * as fgdb from 'fgdb';
import { Buffer } from 'buffer';
import process from 'process';
import JSZip from 'jszip';
import { MatTableDataSource } from '@angular/material/table';
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
        this.uploadAttachment(result.file)
      }
    })
  }

  // Define allowed file types (excluding PNG)
private allowedFileTypes: string[] = ['kml', 'kmz', 'zip', 'shp', 'gdb'];

uploadAttachment(file: File): void {
  // Extract file extension and validate it
  const fileExtension = file.name.split('.').pop()?.toLowerCase();

  if (!fileExtension || !this.allowedFileTypes.includes(fileExtension)) {
    this.snackbarService.open('  The spatial file was not uploaded because the file format is not accepted. Only KML, KMZ, SHP, and GDB are allowed.', 'Close', {
      duration: 5000,
      panelClass: ['snackbar-error'],
    });
    return;
  }

  // Create attachment object
  const attachment: FileAttachment = {
    sourceObjectNameCode: { sourceObjectNameCode: "PROJECT" },
    sourceObjectUniqueId: this.projectGuid,
    documentPath: file.name,
    fileIdentifier: Math.random().toFixed(5),
    attachmentContentTypeCode: { attachmentContentTypeCode: "OTHER" },
    attachmentDescription: "DESCRIPTION",
    attachmentReadOnlyInd: false
  };

  // Send to the backend
  this.attachmentService.createProjectAttachment(this.projectGuid, attachment).subscribe({
    next: (response) => {
      if (response) {
        const newProjectFile = {
          attachmentType: response.attachmentType || 'Project Polygon',
          fileName: response.documentPath || file.name,
          fileType: response.fileType || 'DOCUMENT',
          uploadedBy: response.uploadedBy || 'IDIR\\SSYLVER',
          uploadedDate: response.uploadedDate || new Date().toISOString().split('T')[0],
          polygonHectares: response.polygonHectares || 0,
          description: response.description || 'description',
        };

        // Update the list and refresh the table
        this.projectFiles.push(newProjectFile);
        this.dataSource.data = [...this.projectFiles];

        // Show success snackbar
        this.snackbarService.open('File uploaded successfully.', 'Close', {
          duration: 5000,
          panelClass: ['snackbar-success'],
        });
      }
    },
    error: () => {
      // Show error snackbar
      this.snackbarService.open('Failed to upload file. Please try again.', 'Close', {
        duration: 5000,
        panelClass: ['snackbar-error'],
      });
    },
  });
}

  //       return this.attachmentService.createProjectAttachment(this.projectGuid, attachment).pipe(
  //         switchMap(() => from(this.extractCoordinates(file))) // Extract coordinates
  //       );
  //     }),
  //     switchMap((coordinates) => {
  //       if (!coordinates || coordinates.length === 0) {
  //         console.log('File is not a spatial file. Skipping project boundary creation.');
  //         return EMPTY; // Skip createProjectBoundary
  //       }

  //       const projectBoundary: ProjectBoundary = {
  //         projectGuid: this.projectGuid,
  //         systemStartTimestamp: "2026-01-01",
  //         systemEndTimestamp: "2026-12-31",
  //         mappingLabel: "Test mapping label",
  //         collectionDate: "2026-01-15",
  //         collectionMethod: "Test collection method",
  //         collectorName: "Test_user",
  //         boundarySizeHa: 200.5,
  //         boundaryComment: "Test activity boundary comment",
  //         boundaryGeometry: {
  //           type: "Polygon",
  //           coordinates: coordinates as number[][][]
  //         },
  //         locationGeometry: coordinates[0][0] as [number, number]
  //       };

  //       return this.projectService.createProjectBoundary(this.projectGuid, projectBoundary);
  //     })
  //   ).subscribe({
  //     next: () => console.log('Project boundary created successfully'),
  //     error: (error) => console.error('Error during file upload or boundary creation:', error),
  //   });
  // }

  // private async extractCoordinates(file: File): Promise<number[][][]> {
  //   const fileType = file.name.split('.').pop()?.toLowerCase();

  //   try {
  //     if (fileType === 'kml') {
  //       return this.spatialService.extractKMLCoordinates(await file.text()) as number[][][];
  //     } else if (fileType === 'kmz') {
  //       return await this.spatialService.extractKMZCoordinates(file) as number[][][];
  //     } else if (fileType === 'shp') {
  //       return await this.spatialService.extractSHPCoordinates(file) as number[][][];
  //     } else {
  //       console.error('Unsupported file type:', fileType);
  //       return [];
  //     }
  //   } catch (error) {
  //     console.error('Error extracting coordinates:', error);
  //     return []; 
  //   }
  // }


  private async extractCoordinates(file: File): Promise<number[][][]> {
    const fileType = file.name.split('.').pop()?.toLowerCase();
    try {
      if (fileType === 'kml') {
        return this.spatialService.extractKMLCoordinates(await file.text()) as number[][][];
      } else if (fileType === 'kmz') {
        return await this.spatialService.extractKMZCoordinates(file) as number[][][];
      } else if (fileType === 'shp') {
        return await this.spatialService.extractSHPCoordinates(file) as number[][][];
      } else if (fileType === 'zip' || fileType === 'gdb') {
        const zipReader = new ZipReader(new BlobReader(file));
        const entries = await zipReader.getEntries();
        try {
          const kmlEntry = entries.find(entry => entry.filename.endsWith('.kml'));
          const kmzEntry = entries.find(entry => entry.filename.endsWith('.kmz'));
          const shpEntry = entries.find(entry => entry.filename.endsWith('.shp'));
          const dbfEntry = entries.find(entry => entry.filename.endsWith('.dbf'));
          const containsGdbTable = entries.some(entry => entry.filename.includes('.gdbtable'));

          const gdbEntries = containsGdbTable ? entries : undefined;

          if (kmlEntry) {
            const extractedKML = await kmlEntry.getData?.(new BlobWriter());
            if (extractedKML) {
              return await this.spatialService.extractKMZCoordinates(
                new File([extractedKML], 'extracted.kml')
              ) as number[][][];
            }
          }

          if (kmzEntry) {
            const extractedKMZ = await kmzEntry.getData?.(new BlobWriter());
            if (extractedKMZ) {
              return await this.spatialService.extractKMZCoordinates(
                new File([extractedKMZ], 'extracted.kmz')
              ) as number[][][];
            }
          }

          if (shpEntry && dbfEntry) {
            return await this.spatialService.extractSHPCoordinates(file) as number[][][];
          }

          if (gdbEntries) {
            try {
              const fgdb = require('fgdb');
              fgdb(file)
              .then((geojsonData: any) => {
                console.log('Extracted GeoJSON:', geojsonData);
                this.snackbarService.open('GeoJSON extraction successful!', 'Close', { duration: 3000 });
              })
              .catch((error: any) => {
                console.error('Error extracting GeoJSON:', error);
                this.snackbarService.open('Failed to extract GeoJSON', 'Close', { duration: 3000 });
              });

            } catch (error) {
              console.error('Error processing Geodatabase:', error);
            }
          }


          console.error('No supported spatial files found in ZIP or GDB.');
          return [];
        } finally {
          // Always close the zip reader to free up resources 
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

  // Helper method to read file as ArrayBuffer
  private readFileAsArrayBuffer(file: File): Promise<ArrayBuffer> {
    return new Promise((resolve, reject) => {
      const reader = new FileReader();
      reader.onload = () => resolve(reader.result as ArrayBuffer);
      reader.onerror = reject;
      reader.readAsArrayBuffer(file);
    });
  }

  // Concatenate multiple ArrayBuffers
  private concatenateBuffers(buffers: ArrayBuffer[]): ArrayBuffer {
    const totalLength = buffers.reduce((acc, buffer) => acc + buffer.byteLength, 0);
    const combinedBuffer = new Uint8Array(totalLength);

    let offset = 0;
    for (const buffer of buffers) {
      combinedBuffer.set(new Uint8Array(buffer), offset);
      offset += buffer.byteLength;
    }

    return combinedBuffer.buffer;
  }


  uploadFile(file: File) {
    this.extractCoordinates(file)
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
