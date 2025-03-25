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
import { ZipReader, BlobReader, TextWriter, BlobWriter, Uint8ArrayWriter } from '@zip.js/zip.js';
import * as fgdb from 'fgdb';
import { Buffer } from 'buffer';
import process from 'process';
import JSZip from 'jszip';
(window as any).process = process;


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

  openFileUploadModal() {
    const dialogRef = this.dialog.open(AddAttachmentComponent, {
      width: '1000px',
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result?.file) {
        this.uploadFile(result.file)
      }
    })
  }

  // uploadFile(file: File): void {
  //   // if file type is not set, set it in a new file
  //   const fileType = file?.name?.split('.').pop()?.toLowerCase();
  //   if(!file.type) file = new File([file], file.name, { type: fileType });
  //   this.projectService.uploadDocument({ file }).pipe(
  //     switchMap((response) => {
  //       if (!response) {
  //         console.error('Upload failed: No response from uploadDocument');
  //         return EMPTY;
  //       }

  //       const attachment: FileAttachment = {
  //         sourceObjectNameCode: { sourceObjectNameCode: "PROJECT" },
  //         sourceObjectUniqueId: this.projectGuid,
  //         documentPath: response.filePath,
  //         fileIdentifier: response.fileId,
  //         attachmentContentTypeCode: { attachmentContentTypeCode: "OTHER" },
  //         attachmentDescription: "DESCRIPTION",
  //         attachmentReadOnlyInd: false
  //       };

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
          const gdbEntries = entries.filter(entry => entry.filename.endsWith('.gdbfile') ||
            entry.filename.endsWith('.gdbtable') ||
            entry.filename.endsWith('.gdbblx'));

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

          if (gdbEntries.length > 0) {
            const fgdb = require('fgdb');
            let files: File[] = []
            const extractedFiles: { [key: string]: ArrayBuffer } = {};
            try {
              const zipReader = new ZipReader(new BlobReader(file));
              const entries = await zipReader.getEntries();

              // Locate the .gdb folder within the .zip archive
              // const gdbFolder = entries.find(entry => entry.filename.endsWith('.gdb/'));

              // if (!gdbFolder) {
              //   throw new Error('No .gdb folder found inside the ZIP archive.');
              // }

              // Filter out spatial data files within the .gdb folder
              // const gdbEntries = entries.filter(entry =>
              // (entry.filename.endsWith('.gdbtable') || entry.filename.endsWith('.gdbindexes') ||
              //   entry.filename.endsWith('.gdbgeom')));
              const gdbEntries = entries;

              if (gdbEntries.length === 0) {
                throw new Error('No spatial data files found in the .gdb folder.');
              }

              const extractedFiles: { [key: string]: ArrayBuffer } = {};

              // Read each file
              for (const entry of gdbEntries) {
                if (entry.getData) {  // Ensure getData() is defined before calling it
                  const fileData = await entry.getData(new BlobWriter());
                  const buffer = await fileData.arrayBuffer();
                  extractedFiles[entry.filename] = buffer;
                } else {
                  console.warn(`Skipping file ${entry.filename} as it has no getData() method.`);
                }
              }

              // Convert extracted files into a zip (in-memory)
              const zip = new JSZip();
              Object.entries(extractedFiles).forEach(([filename, data]) => {
                zip.file(filename, data);
              });

              const zipContent = await zip.generateAsync({ type: 'arraybuffer' });
              console.log('ZIP Content:', zipContent);
              // Process the Geodatabase
              const geodatabase = await fgdb(extractedFiles);
              console.log('Geodatabase Tables:', Object.keys(geodatabase.tables));

              // Extract coordinates
              if (geodatabase.tables) {
                const spatialData = geodatabase.tables['your_spatial_table_name']; // Adjust as needed
                console.log('Spatial Data:', spatialData);
              }

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

  deleteFile(file: any) {
    //to do
  }

  downloadFile(file: any) {
    // to do
  }
}
