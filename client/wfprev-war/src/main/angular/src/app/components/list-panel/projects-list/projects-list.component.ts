import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';

@Component({
  selector: 'app-projects-list',
  standalone: true,
  imports: [MatSlideToggleModule,CommonModule], // Add FormsModule here
  templateUrl: './projects-list.component.html',
  styleUrls: ['./projects-list.component.scss'], // Corrected to 'styleUrls'
})
export class ProjectsListComponent {
  sortOptions = [
    { label: 'Select...', value: '' },
    { label: 'Name', value: 'name' },
    { label: 'Date', value: 'date' },
  ];

  selectedSort = '';
  syncWithMap = false; 
  resultCount = 37; 

  onSortChange(event:any): void {
    console.log('Sort changed to:', this.selectedSort);
  }

  onToggleChange(): void {
    console.log('Sync with map:', this.syncWithMap ? 'On' : 'Off');
  }
}
