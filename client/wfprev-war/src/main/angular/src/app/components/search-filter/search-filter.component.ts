import { Component } from '@angular/core';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { FormsModule } from '@angular/forms';
import { MatInputModule } from '@angular/material/input';
import { CommonModule } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
@Component({
  selector: 'app-search-filter',
  standalone: true,
  imports: [
    FormsModule,
    MatFormFieldModule,
    MatSelectModule,
    MatInputModule,
    MatIconModule,
    MatButtonModule,
    CommonModule
  ],
  templateUrl: './search-filter.component.html',
  styleUrl: './search-filter.component.scss'
})
export class SearchFilterComponent {
  searchText: string = '';

  // Options for each filter dropdown
  businessAreaOptions = [
    { label: 'BC Parks', value: 'bc-parks' },
    { label: 'Ministry of Forests', value: 'forests' },
    { label: 'Wildfire Service', value: 'wildfire' }
  ];

  fiscalYearOptions = [
    { label: '2023/2024', value: '2023' },
    { label: '2022/2023', value: '2022' },
    { label: '2021/2022', value: '2021' }
  ];

  activityOptions = [
    { label: 'Operational Treatment', value: 'treatment' },
    { label: 'Planning', value: 'planning' },
    { label: 'Monitoring', value: 'monitoring' }
  ];

  forestRegionOptions = [
    { label: 'Thompson-Okanagan', value: 'thompson-okanagan' },
    { label: 'Skeena West', value: 'skeena-west' }
  ];

  forestDistrictOptions = [
    { label: 'North Coast Skeena', value: 'north-coast' },
    { label: 'South Coast', value: 'south-coast' }
  ];

  fireCentreOptions = [
    { label: 'Coastal', value: 'coastal' },
    { label: 'Cariboo', value: 'cariboo' },
    { label: 'Kamloops', value: 'kamloops' }
  ];

  // Selected values
  selectedBusinessArea: string[] = [];
  selectedFiscalYears: string[] = [];
  selectedActivity: string[] = [];
  selectedForestRegion: string[] = [];
  selectedForestDistrict: string[] = [];
  selectedFireCentre: string[] = [];

  // Methods
  onSearch() {
    console.log('Searching for:', this.searchText);
  }

  onReset() {
    this.searchText = '';
    this.selectedBusinessArea = [];
    this.selectedFiscalYears = [];
    this.selectedActivity = [];
    this.selectedForestRegion = [];
    this.selectedForestDistrict = [];
    this.selectedFireCentre = [];
  }
}
