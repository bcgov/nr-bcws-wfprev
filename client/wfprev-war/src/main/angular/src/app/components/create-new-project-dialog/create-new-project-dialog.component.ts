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
import {
  validateLatLong,
} from 'src/app/utils/tools';
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
  [key: string]: any; // Add this line to allow dynamic properties
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
  objectiveTypes: any[] = [];

  constructor(
    private readonly fb: FormBuilder,
    private readonly dialog: MatDialog,
    private readonly dialogRef: MatDialogRef<CreateNewProjectDialogComponent>,
    private readonly snackbarService: MatSnackBar,
    private readonly projectService: ProjectService,
    private readonly codeTableService: CodeTableServices

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
      primaryObjective: ['', [Validators.required]],
      secondaryObjective: [''],
      secondaryObjectiveRationale: ['',[Validators.maxLength(50)]],
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
    this.loadCodeTables(); // Call the helper method to load code tables
  }

  loadCodeTables(): void {
    const codeTables = [
      { name: 'programAreaCodes', property: 'businessAreas', embeddedKey: 'programArea' },
      { name: 'forestRegionCodes', property: 'forestRegions', embeddedKey: 'forestRegionCode' },
      { name: 'forestDistrictCodes', property: 'forestDistricts', embeddedKey: 'forestDistrictCode' },
      { name: 'bcParksRegionCodes', property: 'bcParksRegions', embeddedKey: 'bcParksRegionCode' },
      { name: 'bcParksSectionCodes', property: 'allBcParksSections', embeddedKey: 'bcParksSectionCode' },
      { name: 'objectiveTypeCodes', property: 'objectiveTypes', embeddedKey: 'objectiveTypeCode' },

    ];
  
    codeTables.forEach((table) => {
      this.codeTableService.fetchCodeTable(table.name).subscribe({
        next: (data) => {
          this[table.property] = data?._embedded?.[table.embeddedKey] || [];
          if (table.name === 'objectiveTypeCodes') {
            const defaultObjective = this.objectiveTypes.find(
              (type) => type.objectiveTypeCode === 'WRR'
            );
      
            if (defaultObjective) {
              this.projectForm.get('primaryObjective')?.setValue('WRR');
            }
          }
        },
        error: (err) => {
          console.error(`Error fetching ${table.name}`, err);
        },
      });
    });
  }
  getErrorMessage(controlName: string): string | null {
    const control = this.projectForm.get(controlName);
    if (!control?.errors) return null;

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
      const latLong = this.projectForm.get('latLong')?.value ?? '';
      let validatedLatLong;
    
      if (latLong) {
        validatedLatLong = validateLatLong(latLong);
        if (!validatedLatLong) {
          // Show an error message if latLong is invalid
          this.snackbarService.open(
            'Invalid latitude and longitude. Please ensure it is in the correct format and within BC boundaries.',
            'OK',
            { duration: 5000, panelClass: 'snackbar-error' }
          );
          return; // Exit the method if latLong is invalid
        }
      }    

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
        forestAreaCode: {
          forestAreaCode: "COAST",
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
        primaryObjectiveTypeCode: {
          objectiveTypeCode: this.projectForm.get('primaryObjective')?.value
        },
        secondaryObjectiveTypeCode: {
          objectiveTypeCode: this.projectForm.get('secondaryObjective')?.value
        },
        secondaryObjectiveRationale: this.projectForm.get('secondaryObjectiveRationale')?.value,
        
        isMultiFiscalYearProj: false,
        ...(validatedLatLong && {
          latitude: Number(validatedLatLong.latitude),
          longitude: Number(validatedLatLong.longitude),
        }), // Conditionally include latitude and longitude
      };
      
      this.projectService.createProject(newProject).subscribe({
        next: (response) => {
          this.snackbarService.open(
            this.messages.projectCreatedSuccess,
            'OK',
            { duration: 5000, panelClass: 'snackbar-success' },
          );
          this.dialogRef.close({ success: true });
        },
        error: (err) =>{
            this.snackbarService.open(
              this.messages.projectCreatedFailure,
              'OK',
              { duration: 5000, panelClass: 'snackbar-error' }
            );
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
