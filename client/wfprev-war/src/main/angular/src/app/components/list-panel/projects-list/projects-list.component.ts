import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatExpansionModule } from '@angular/material/expansion';
import { ActivatedRoute, Router } from '@angular/router';
import { ResourcesRoutes } from 'src/app/utils';
import { ProjectService } from 'src/app/services/project-services';
import { CodeTableServices } from 'src/app/services/code-table-services';

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
    private router: Router,
    private projectService: ProjectService,
    private codeTableService: CodeTableServices
  ) {
  }
  ngOnInit(): void {
    this.loadCodeTables();
    this.projectService.fetchProjects().subscribe({
      next: (data) => {
        this.projectList = data._embedded?.project;
      },
      error: (err) => {
        console.error('Error fetching projects:', err);
      }
    });
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
            this.programAreaCode = data._embedded.programArea
          } else if (table.name === 'forestRegionCodes') {
            this.forestRegionCode = data._embedded.forestRegionCode
          }
        },
        error: (err) => {
          console.error(`Error fetching ${table.name}`, err);
        },
      });
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

  onSyncMapToggleChange(event: any): void {
    this.syncWithMap = event.checked;
  }

  editProject(project: any, event:Event) {
    event.stopPropagation();
    this.router.navigate([ResourcesRoutes.EDIT_PROJECT], {
      queryParams: { projectNumber: project.projectNumber, name: project.projectName}
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
}
