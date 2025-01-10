import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatExpansionModule } from '@angular/material/expansion';
import { Router } from '@angular/router';
import { ResourcesRoutes } from 'src/app/utils';
import { ProjectService } from 'src/app/services/project-services';
import { CodeTableServices } from 'src/app/services/code-table-services';
import { CreateNewProjectDialogComponent } from 'src/app/components/create-new-project-dialog/create-new-project-dialog.component';
import { MatDialog } from '@angular/material/dialog';

@Component({
  selector: 'app-projects-list',
  standalone: true,
  imports: [MatSlideToggleModule,CommonModule,MatExpansionModule], // Add FormsModule here
  templateUrl: './projects-list.component.html',
  styleUrls: ['./projects-list.component.scss'], // Corrected to 'styleUrls'
})
export class ProjectsListComponent implements OnInit {
  [key: string]: any;
  projectList : any[] = [];
  programAreaCode: any[] = [];
  forestRegionCode: any[] = [];
  constructor(
    private readonly router: Router,
    private readonly projectService: ProjectService,
    private readonly codeTableService: CodeTableServices,
    private readonly dialog: MatDialog
    
  ) {
  }
  ngOnInit(): void {
    this.loadCodeTables();
    this.loadProjects();
  }

  loadCodeTables(): void {
    const codeTables = [
      { name: 'programAreaCodes', property: 'businessAreas', embeddedKey: 'programArea' },
      { name: 'forestRegionCodes', property: 'forestRegions', embeddedKey: 'forestRegionCode' },
    ];
  
    codeTables.forEach((table) => {
      this.codeTableService.fetchCodeTable(table.name).subscribe({
        next: (data) => {
          if (table.name === 'programAreaCodes') {
            this.programAreaCode = data._embedded.programArea;
          } else if (table.name === 'forestRegionCodes') {
            this.forestRegionCode = data._embedded.forestRegionCode;
          }
        },
        error: (err) => {
          console.error(`Error fetching ${table.name}`, err);
  
          // Explicitly set the property to an empty array on error
          if (table.name === 'programAreaCodes') {
            this.programAreaCode = [];
          } else if (table.name === 'forestRegionCodes') {
            this.forestRegionCode = [];
          }
        },
      });
    });
  }
  

  loadProjects() {
    this.projectService.fetchProjects().subscribe({
      next: (data) => {
        this.projectList = data._embedded?.project;
      },
      error: (err) => {
        console.error('Error fetching projects:', err);
        this.projectList = [];
      }
    });
  }
  sortOptions = [
    { label: 'Name (A-Z)', value: 'ascending' },
    { label: 'Name (Z-A)', value: 'descending' },
  ];

  selectedSort = '';
  syncWithMap = false; 
  resultCount = 3; 
  
  fiscalYearActivityTypes = ['Clearing','Burning','Pruning']
  

  onSortChange(event:any): void {
    this.selectedSort = event.target.value;
  }

  editProject(project: any, event:Event) {
    event.stopPropagation();
    this.router.navigate([ResourcesRoutes.EDIT_PROJECT], {
      queryParams: { projectGuid: project.projectGuid}
    });
  }
  
  getDescription(codeTable: string, code: number | string): string {
    const table = this[codeTable];
    if (!table) return 'Unknown'; // Return 'Unknown' if the table is not loaded
  
    let entry;
  
    if (codeTable === 'programAreaCode') {
      // Search by programAreaGuid if the codeTable is programAreaCode
      entry = table.find((item: any) => item.programAreaGuid === code);
      return entry ? entry.programAreaName : 'Unknown'
    } else {
      // Default to searching by orgUnitId
      entry = table.find((item: any) => item.orgUnitId === code);
    }
  
    return entry ? entry.orgUnitName : 'Unknown';
  }

  createNewProject(): void {
    const dialogRef = this.dialog.open(CreateNewProjectDialogComponent, {
      width: '880px',
      disableClose: true,
      hasBackdrop: true,
    });
      // Subscribe to the afterClosed method to handle the result
    dialogRef.afterClosed().subscribe((result) => {
      if (result && result.success === true) {
        this.loadProjects();
      }
    });
  }
  
}
