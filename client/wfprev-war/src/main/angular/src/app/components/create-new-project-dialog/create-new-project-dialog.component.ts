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

  businessAreas : any[] = [];
  forestRegions : any[] = [];
  forestDistricts : any[] = [];
  bcParksRegions : any[] = [];
  bcParksSections: any[] = [];
  allBcParksSections: any[] = []; // To hold all sections initially

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
    this.projectForm.get('bcParksRegion')?.valueChanges.subscribe((regionId: number) => {
      if (regionId) {
        this.projectForm.get('bcParksSection')?.enable();
        this.bcParksSections = this.allBcParksSections.filter(
          (section) => section.parentOrgUnitId === regionId.toString()
        );
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
        this.businessAreas = data._embedded.programArea;
      },
      error: (err) => {
        console.error(err);
      },
    });

    this.codeTableService.fetchCodeTable('forestRegionCodes').subscribe({
      next: (data) => {
        this.forestRegions = data._embedded.forestRegionCode; // Assign data to the component property
      },
      error: (err) => {
        console.error(err);
      },
    });

    this.codeTableService.fetchCodeTable('forestDistrictCodes').subscribe({
      next: (data) => {
        this.forestDistricts = data._embedded.forestDistrictCode;
      },
      error: (err) => {
        console.error(err);
      },
    });

    this.codeTableService.fetchCodeTable('bcParksRegionCodes').subscribe({
      next: (data) => {
        this.bcParksRegions = data._embedded.bcParksRegionCode;
      },
      error: (err) => {
        console.error(err);
      },
    });

    this.codeTableService.fetchCodeTable('bcParksSectionCodes').subscribe({
      next: (data) => {
        this.allBcParksSections = data._embedded.bcParksSectionCode; // Store all sections initially
      },
      error: (err) => {
        console.error(err);
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
      const newProject: Project = {
        projectName: this.projectForm.get('projectName')?.value ?? '',
        programAreaGuid: this.projectForm.get('businessArea')?.value ?? '',
        forestRegionOrgUnitId: Number(this.projectForm.get('forestRegion')?.value) || 0,
        forestDistrictOrgUnitId: Number(this.projectForm.get('forestDistrict')?.value) || 0,
        bcParksRegionOrgUnitId: Number(this.projectForm.get('bcParksRegion')?.value) || 0,
        bcParksSectionOrgUnitId: Number(this.projectForm.get('bcParksSection')?.value) || 0,
        projectLead: this.projectForm.get('projectLead')?.value ?? '',
        projectLeadEmailAddress: this.projectForm.get('projectLeadEmail')?.value ?? '',
        siteUnitName: this.projectForm.get('siteUnitName')?.value ?? '',
        closestCommunityName: this.projectForm.get('closestCommunity')?.value ?? '',
        fireCentreOrgUnitId: this.projectForm.get('fireCentre')?.value ?? 0,
        generalScopeCode: {
          generalScopeCode: "SL_ACT"
        },
        projectTypeCode: {
          projectTypeCode: "FUEL_MGMT"
        },
        projectDescription: this.projectForm.get('projectDescription')?.value ?? '',
        projectNumber: this.projectForm.get('projectNumber')?.value ?? '',
        totalFundingRequestAmount:
          this.projectForm.get('totalFundingRequestAmount')?.value ?? '',
        totalAllocatedAmount: this.projectForm.get('totalAllocatedAmount')?.value ?? '',
        totalPlannedProjectSizeHa:
          this.projectForm.get('totalPlannedProjectSizeHa')?.value ?? '',
        totalPlannedCostPerHectare:
          this.projectForm.get('totalPlannedCostPerHectare')?.value ?? '',
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
