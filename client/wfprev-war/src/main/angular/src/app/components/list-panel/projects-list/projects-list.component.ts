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
    { label: 'Name (A-Z)', value: 'ascending' },
    { label: 'Name (Z-A)', value: 'descending' },
  ];

  selectedSort = '';
  syncWithMap = false; 
  resultCount = 3; 


  // MOCK UP DATA TO MACTCH UP THE REAL DATA MODEL
  projectList = [
    {
      projectTypeCode: {
        projectTypeCode: "FUEL_MGMT",
      },
      projectNumber: 12345,
      siteUnitName: "Vancouver Forest Unit",
      forestAreaCode: {
        forestAreaCode: "WEST",
      },
      generalScopeCode: {
        generalScopeCode: "SL_ACT",
      },
      programAreaGuid: "27602cd9-4b6e-9be0-e063-690a0a0afb50",
      projectName: "Sample Forest Management Project",
      projectLead: "Jane Smith",
      projectLeadEmailAddress: "jane.smith@example.com",
      projectDescription: "This is a comprehensive forest management project focusing on sustainable practices",
      closestCommunityName: "Vancouver",
      totalFundingRequestAmount: 100000.0,
      totalAllocatedAmount: 95000.0,
      totalPlannedProjectSizeHa: 500.0,
      totalPlannedCostPerHectare: 200.0,
      totalActualAmount: 0.0,
      isMultiFiscalYearProj: false,
      forestRegionOrgUnitId: 1001,
      forestDistrictOrgUnitId: 2001,
      fireCentreOrgUnitId: 3001,
      bcParksRegionOrgUnitId: 4001,
      bcParksSectionOrgUnitId: 5001,
    },
    {
      projectTypeCode: {
        projectTypeCode: "WLD_MGMT",
      },
      projectNumber: 67890,
      siteUnitName: "Kelowna Wildlife Zone",
      forestAreaCode: {
        forestAreaCode: "EAST",
      },
      generalScopeCode: {
        generalScopeCode: "WL_ACT",
      },
      programAreaGuid: "58672bcd-3e7f-8cd1-e053-680a0a0afc40",
      projectName: "Sustainable Fuel Management Initiative",
      projectLead: "John Doe",
      projectLeadEmailAddress: "john.doe@example.com",
      projectDescription: "An initiative to promote sustainable wildlife and fuel management practices",
      closestCommunityName: "Kelowna",
      totalFundingRequestAmount: 75000.0,
      totalAllocatedAmount: 70000.0,
      totalPlannedProjectSizeHa: 300.0,
      totalPlannedCostPerHectare: 250.0,
      totalActualAmount: 0.0,
      isMultiFiscalYearProj: true,
      forestRegionOrgUnitId: 1101,
      forestDistrictOrgUnitId: 2101,
      fireCentreOrgUnitId: 3101,
      bcParksRegionOrgUnitId: 4101,
      bcParksSectionOrgUnitId: 5101,
    },
    {
      projectTypeCode: {
        projectTypeCode: "URB_FOREST",
      },
      projectNumber: 11223,
      siteUnitName: "Prince George Forest Sector",
      forestAreaCode: {
        forestAreaCode: "NORTH",
      },
      generalScopeCode: {
        generalScopeCode: "UF_REV",
      },
      programAreaGuid: "19762acd-7d8a-4fe2-e043-680a0b0afc11",
      projectName: "Urban Forest Revitalization Program",
      projectLead: "Alice Brown",
      projectLeadEmailAddress: "alice.brown@example.com",
      projectDescription: "A program aimed at revitalizing urban forests in northern regions",
      closestCommunityName: "Prince George",
      totalFundingRequestAmount: 120000.0,
      totalAllocatedAmount: 115000.0,
      totalPlannedProjectSizeHa: 750.0,
      totalPlannedCostPerHectare: 160.0,
      totalActualAmount: 0.0,
      isMultiFiscalYearProj: true,
      forestRegionOrgUnitId: 1201,
      forestDistrictOrgUnitId: 2201,
      fireCentreOrgUnitId: 3201,
      bcParksRegionOrgUnitId: 4201,
      bcParksSectionOrgUnitId: 5201,
    },
  ];
  
  fiscalYearActivityTypes = ['Clearning','Burning','Pruning']
  

  onSortChange(event:any): void {
    console.log('Sort changed to:', this.selectedSort);
  }

  onToggleChange(): void {
    console.log('Sync with map:', this.syncWithMap ? 'On' : 'Off');
  }
}
