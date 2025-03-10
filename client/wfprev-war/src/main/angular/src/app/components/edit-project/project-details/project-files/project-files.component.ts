import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { MatTableModule } from '@angular/material/table';
import { AddAttachmentComponent } from 'src/app/components/add-attachment/add-attachment.component';

@Component({
  selector: 'app-project-files',
  standalone: true,
  imports: [CommonModule,MatTableModule],
  templateUrl: './project-files.component.html',
  styleUrl: './project-files.component.scss'
})
export class ProjectFilesComponent {
  constructor(
    public readonly dialog: MatDialog)
  {
  }
  displayedColumns: string[] = ['name', 'description', 'endDate', 'completedHectares'];

  projectFiles = [
    { name: 'Site Prep', description: 'Preparing the site for seeding', startDate:'2024-02-15', endDate: '2024-03-15', completedHectares: 50 },
    { name: 'Seeding', description: 'Planting seeds for reforestation', endDate: '2024-05-10', completedHectares: 100 },
    { name: 'Monitoring', description: 'Tracking vegetation growth', endDate: '2024-08-25', completedHectares: 75 },
  ];

  openFileUploadModal(){
    const dialogRef = this.dialog.open(AddAttachmentComponent, {
      width: '1000px',
    });
  }
}
