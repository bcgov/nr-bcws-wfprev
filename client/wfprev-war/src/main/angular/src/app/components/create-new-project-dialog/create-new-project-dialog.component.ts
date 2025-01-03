import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatDialog , MatDialogRef } from '@angular/material/dialog';
import { ConfirmationDialogComponent } from 'src/app/components/confirmation-dialog/confirmation-dialog.component';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Messages } from 'src/app/utils/messages';
import { ProjectService } from 'src/app/services/project-services';
import { CodeTableServices } from 'src/app/services/code-table-services';
import { Project } from 'src/app/components/models';
@Component({
  selector: 'app-create-new-project-dialog',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    CommonModule,
  ],
  templateUrl: './create-new-project-dialog.component.html',
  styleUrls: ['./create-new-project-dialog.component.scss']
})
export class CreateNewProjectDialogComponent implements OnInit {
  projectForm: FormGroup;
  messages = Messages;
  // Regions and sections mapping
  regionToSections: { [key: string]: string[] } = {
    'Northern': ['Omineca', 'Peace', 'Skeena'],
    'Thompson Cariboo': ['Cariboo', 'Thompson'],
    'Kootenay Okanagan': ['Kootenay', 'Okanagan'],
    'South Coast': ['South Coast'],
    'West Coast': ['Central Coast/North Island', 'Haida Gwaii/South Island']
  };

  businessAreas = [4, 5, 7]; // Example data
  forestRegions = [1, 3, 5]; // Example data
  forestDistricts = [4, 66, 4442]; // Example data
  bcParksRegions = Object.keys(this.regionToSections);
  bcParksSections: string[] = []; // Dynamically updated based on the selected region

  constructor(
    private readonly fb: FormBuilder,
    private readonly dialog: MatDialog,
    private readonly dialogRef: MatDialogRef<CreateNewProjectDialogComponent>,
    private readonly snackbarService: MatSnackBar,
    private projectService: ProjectService,
    private codeTableService: CodeTableServices

  ) {
    this.projectForm = this.fb.group({
      projectName: ['', [Validators.required, Validators.maxLength(50)]],
      latLong: ['', [Validators.maxLength(25)]],
      businessArea: ['', [Validators.required]],
      forestRegion: ['', [Validators.required]],
      forestDistrict: ['', [Validators.required]],
      bcParksRegion: ['', [Validators.required]],
      bcParksSection: [{ value: '', disabled: true }, Validators.required],
      projectLead: ['', [Validators.maxLength(50)]],
      projectLeadEmail: ['', [Validators.email, Validators.maxLength(50)]],
      siteUnitName: ['', [Validators.maxLength(50)]],
      closestCommunity: ['', [Validators.required, Validators.maxLength(50)]],
  });

  // Dynamically enable/disable bcParksSection based on bcParksRegion selection
  this.projectForm.get('bcParksRegion')?.valueChanges.subscribe((region: string | number) => {
    if (region) {
      this.projectForm.get('bcParksSection')?.enable();
      this.bcParksSections = this.regionToSections[region] || [];
    } else {
      this.projectForm.get('bcParksSection')?.reset();
      this.projectForm.get('bcParksSection')?.disable();
      this.bcParksSections = [];
    }
  });
  }
  ngOnInit(): void {
    // Fetch code tables
    
    this.codeTableService.fetchCodeTable('programAreaCodes').subscribe({
      next: (data) => {
        this.forestDistricts = data;
      },
      error: (err) => {
        console.error(err);
        this.snackbarService.open(
          'Failed to load business area codes. Please try again later.',
          'OK',
          { duration: 5000, panelClass: 'snackbar-error' }
        );
      },
    });

    this.codeTableService.fetchCodeTable('forestRegionCodes').subscribe({
      next: (data) => {
        this.forestRegions = data; // Assign data to the component property
      },
      error: (err) => {
        console.error(err);
        this.snackbarService.open(
          'Failed to load forest region codes. Please try again later.',
          'OK',
          { duration: 5000, panelClass: 'snackbar-error' }
        );
      },
    });

    this.codeTableService.fetchCodeTable('forestDistrictCodes').subscribe({
      next: (data) => {
        this.forestDistricts = data;
      },
      error: (err) => {
        console.error(err);
        this.snackbarService.open(
          'Failed to load forest districts codes. Please try again later.',
          'OK',
          { duration: 5000, panelClass: 'snackbar-error' }
        );
      },
    });

    this.codeTableService.fetchCodeTable('bcParksRegionCodes').subscribe({
      next: (data) => {
        this.forestDistricts = data;
      },
      error: (err) => {
        console.error(err);
        this.snackbarService.open(
          'Failed to load bc parks region codes. Please try again later.',
          'OK',
          { duration: 5000, panelClass: 'snackbar-error' }
        );
      },
    });
  }
  getErrorMessage(controlName: string): string | null {
    const control = this.projectForm.get(controlName);
    if (!control || !control.errors) return null;

    if (control.hasError('required')) {
      return this.messages.requiredField;
    }
    if (control.hasError('maxlength')) {
      return this.messages.maxLengthExceeded;
    }
    if (control.hasError('email')) {
      return this.messages.invalidEmail;
    }

    return null; // No errors
  }

  onCreate(): void {
    if (this.projectForm.valid) {
      console.log(this.projectForm.value);
      const newProject: Project = {
        projectName: this.projectForm.get('projectName')?.value ?? '',
        programAreaGuid: '27602cd9-4b6e-9be0-e063-690a0a0afb50', // change this to form value once codeTable returns value;
        forestRegionOrgUnitId: 11, // change this to form value once codeTable returns value;
        forestDistrictOrgUnitId: 4442, // change this to form value once codeTable returns value;
        bcParksRegionOrgUnitId:  0, // change this to form value once codeTable returns value;
        bcParksSectionOrgUnitId: 10, // change this to form value once codeTable returns value;
        projectLead: this.projectForm.get('projectLead')?.value ?? '',
        projectLeadEmailAddress: this.projectForm.get('projectLeadEmail')?.value ?? '',
        siteUnitName: "Vancouver Forest Unit", // change this to form value once codeTable returns value;
        closestCommunityName: this.projectForm.get('closestCommunity')?.value ?? '',
        fireCentreOrgUnitId: this.projectForm.get('fireCentre')?.value ?? 0,
        forestAreaCode: {
          forestAreaCode: "WEST" // change this to form value once codeTable returns value;
        },
        generalScopeCode: {
          generalScopeCode: "SL_ACT" // change this to form value once codeTable returns value;
        },
        projectTypeCode: {
          projectTypeCode: "FUEL_MGMT" // change this to form value once codeTable returns value;
        },
        projectDescription: this.projectForm.get('projectDescription')?.value ?? '',
        projectNumber: this.projectForm.get('projectNumber')?.value ?? 0,
        totalFundingRequestAmount:
          this.projectForm.get('totalFundingRequestAmount')?.value ?? 0,
        totalAllocatedAmount: this.projectForm.get('totalAllocatedAmount')?.value ?? 0,
        totalPlannedProjectSizeHa:
          this.projectForm.get('totalPlannedProjectSizeHa')?.value ?? 0,
        totalPlannedCostPerHectare:
          this.projectForm.get('totalPlannedCostPerHectare')?.value ?? 0,
        totalActualAmount: this.projectForm.get('totalActualAmount')?.value ?? 0,
        isMultiFiscalYearProj: false,
      };
      
      this.projectService.createProject(newProject).subscribe({
        next: (response) => {
          this.snackbarService.open(
            this.messages.projectCreatedSuccess,
            'OK',
            { duration: 100000, panelClass: 'snackbar-success' },
          );
          this.dialogRef.close(this.projectForm.value);
        },
        error: (err) =>{
          if (err.status === 500 && err.error.message.includes('duplicate')) {
            this.dialog.open(ConfirmationDialogComponent, {
              data: {
                indicator: 'duplicate-project',
                projectName: '',
              },
              width: '500px',
            });
          }
          else{
            this.snackbarService.open(
              "Create project failed",
              'OK',
              { duration: 5000, panelClass: 'snackbar-error' }
            );
          }
        }
      })
    }
  }

  onCancel(): void {
    const dialogRef = this.dialog.open(ConfirmationDialogComponent, {
      data: {
        indicator: 'confirm-cancel',
      },
      width: '500px',
    });

    dialogRef.afterClosed().subscribe((result: any) => {
      if (result) {
        this.dialogRef.close(); // Close the "Create New Project" dialog
      }
    });
  }
}
