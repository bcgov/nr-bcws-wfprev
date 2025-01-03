import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatExpansionModule } from '@angular/material/expansion';
import { ActivatedRoute, Router } from '@angular/router';
import { ResourcesRoutes } from 'src/app/utils';
import { ProjectService } from 'src/app/services/project-services';

@Component({
  selector: 'app-projects-list',
  standalone: true,
  imports: [MatSlideToggleModule,CommonModule,MatExpansionModule], // Add FormsModule here
  templateUrl: './projects-list.component.html',
  styleUrls: ['./projects-list.component.scss'], // Corrected to 'styleUrls'
})
export class ProjectsListComponent implements OnInit {
  projectList : any[] = [];
  constructor(
    private router: Router,
    private projectService: ProjectService
  ) {
  }
  ngOnInit(): void {
    this.projectService.fetchProjects().subscribe({
      next: (data) => {
        console.log('Projects fetched successfully:', data);
        this.projectList = data._embedded?.project;
      },
      error: (err) => {
        console.error('Error fetching projects:', err);
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
    console.log('Sort changed to:', this.selectedSort);
  }

  onSyncMapToggleChange(event: any): void {
    this.syncWithMap = event.checked;
    console.log('Sync with map:', this.syncWithMap ? 'On' : 'Off');
  }

  editProject(project: any, event:Event) {
    event.stopPropagation();
    this.router.navigate([ResourcesRoutes.EDIT_PROJECT], {
      queryParams: { projectNumber: project.projectNumber, name: project.projectName}
    });
  }
  
}
