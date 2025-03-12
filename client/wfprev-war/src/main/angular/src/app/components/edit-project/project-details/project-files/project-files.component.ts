import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatTableModule } from '@angular/material/table';
import { AddAttachmentComponent } from 'src/app/components/add-attachment/add-attachment.component';
import { ProjectService } from 'src/app/services/project-services';
import { Messages } from 'src/app/utils/messages';

@Component({
  selector: 'app-project-files',
  standalone: true,
  imports: [CommonModule,MatTableModule],
  templateUrl: './project-files.component.html',
  styleUrl: './project-files.component.scss'
})
export class ProjectFilesComponent {
  constructor(
    public projectService: ProjectService,
    private readonly snackbarService: MatSnackBar,
    public readonly dialog: MatDialog)
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

  uploadFile(file: File): void{
    this.projectService.uploadDocument({
      file
    }).subscribe({
      next: (response) => {
        this.snackbarService.open(
          this.messages.fileUploadSuccess,
          'OK',
          { duration: 5000, panelClass: 'snackbar-success' },
        );
      },
      error: () => {
      }
    })
  }

  deleteFile(file:any){
    //to do
  }

  downloadFile(file:any){
    // to do
  }
}
