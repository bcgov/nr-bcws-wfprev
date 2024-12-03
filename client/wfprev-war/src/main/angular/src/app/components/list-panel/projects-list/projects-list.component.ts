import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatExpansionModule } from '@angular/material/expansion';

@Component({
  selector: 'app-projects-list',
  standalone: true,
  imports: [MatSlideToggleModule,CommonModule,MatExpansionModule], // Add FormsModule here
  templateUrl: './projects-list.component.html',
  styleUrls: ['./projects-list.component.scss'], // Corrected to 'styleUrls'
})
export class ProjectsListComponent {
  sortOptions = [
    { label: 'Name', value: 'name' },
    { label: 'Date', value: 'date' },
  ];

  selectedSort = '';
  syncWithMap = false; 
  resultCount = 3; 

  projectList = [
    {
      projectName: 'Project Name 1',
      region: 'Region Text',
      totalHectares: 72,
      businessArea: 'BC Parks',
      businessAreaLead: 'Name',
      siteUnit: 'Text',
    },
    {
      projectName: 'Project Name 2',
      region: 'Another Region',
      totalHectares: 54,
      businessArea: 'Wildlife Area',
      businessAreaLead: 'Lead Name',
      siteUnit: 'Unit Name',
    },
    {
      projectName: 'Project Name 3',
      region: 'Third Region',
      totalHectares: 88,
      businessArea: 'Regional Park',
      businessAreaLead: 'Leader Name',
      siteUnit: 'Unit Text',
    },
  ];

  onSortChange(event:any): void {
    console.log('Sort changed to:', this.selectedSort);
  }

  onToggleChange(): void {
    console.log('Sync with map:', this.syncWithMap ? 'On' : 'Off');
  }
}
