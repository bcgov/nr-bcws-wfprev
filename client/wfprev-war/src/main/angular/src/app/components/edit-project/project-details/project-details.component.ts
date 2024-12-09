import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';

@Component({
  selector: 'app-project-details',
  standalone: true,
  imports: [ReactiveFormsModule],
  templateUrl: './project-details.component.html',
  styleUrl: './project-details.component.scss'
})
export class ProjectDetailsComponent implements OnInit{
  detailsForm!: FormGroup;

  sampleData = {
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
    projectDescription:
      "This is a comprehensive forest management project focusing on sustainable practices",
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
  };

  constructor(private fb: FormBuilder) {}

  ngOnInit(): void {
    this.detailsForm = this.fb.group({
      projectLead: [this.sampleData.projectLead, [Validators.required]],
      projectLeadEmailAddress: [
        this.sampleData.projectLeadEmailAddress,
        [Validators.required, Validators.email],
      ],
      projectTypeCode: [
        this.sampleData.projectTypeCode.projectTypeCode,
        [Validators.required],
      ],
      businessArea: [
        this.sampleData.projectTypeCode.projectTypeCode,
        [Validators.required],
      ],
      forestRegion: [
        this.sampleData.forestRegionOrgUnitId,
        [Validators.required]
      ],
      forestDistrict: [this.sampleData.forestDistrictOrgUnitId],
      bcParksRegion: [this.sampleData.bcParksRegionOrgUnitId],
      bcParksDistrict: [this.sampleData.bcParksSectionOrgUnitId],
      siteUnitName: [this.sampleData.siteUnitName, [Validators.required]],
      closestCommunityName: [this.sampleData.closestCommunityName],
      fundingStream: [Validators.required],
      totalFundingRequestAmount: [
        this.sampleData.totalFundingRequestAmount,
        [Validators.required, Validators.min(0)],
      ],
      totalAllocatedAmount: [
        this.sampleData.totalAllocatedAmount,
        [Validators.required, Validators.min(0)],
      ],
      projectDescription: [this.sampleData.projectDescription],
    });
  }

  onSave(): void {
    if (this.detailsForm.valid) {
      console.log('Form Data:', this.detailsForm.value);
      // Submit the form data or perform actions here
    } else {
      console.error('Form is invalid!');
    }
  }

  onCancel(): void {
    this.detailsForm.reset();
  }
}
