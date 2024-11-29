import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatDialog , MatDialogRef } from '@angular/material/dialog';
import { ConfirmationDialogComponent } from 'src/app/components/confirmation-dialog/confirmation-dialog.component';
import { MatSnackBar } from '@angular/material/snack-bar';

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
export class CreateNewProjectDialogComponent {
  projectForm: FormGroup;

  // Regions and sections mapping
  regionToSections: { [key: string]: string[] } = {
    'Northern': ['Omineca', 'Peace', 'Skeena'],
    'Thompson Cariboo': ['Cariboo', 'Thompson'],
    'Kootenay Okanagan': ['Kootenay', 'Okanagan'],
    'South Coast': ['South Coast'],
    'West Coast': ['Central Coast/North Island', 'Haida Gwaii/South Island']
  };

  businessAreas = ['Area 1', 'Area 2', 'Area 3']; // Example data
  forestRegions = ['Region 1', 'Region 2', 'Region 3']; // Example data
  forestDistricts = ['District 1', 'District 2', 'District 3']; // Example data
  bcParksRegions = Object.keys(this.regionToSections);
  bcParksSections: string[] = []; // Dynamically updated based on the selected region

  constructor(
    private readonly fb: FormBuilder,
    private readonly dialog: MatDialog,
    private readonly dialogRef: MatDialogRef<CreateNewProjectDialogComponent>,
    private readonly snackbarService: MatSnackBar,

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

  onCreate(): void {
    if (this.projectForm.valid) {
      console.log(this.projectForm.value);
      //call POST endpoint, 
      // if return 500 error with duplicate project name error message, 

      // this.dialog.open(ConfirmationDialogComponent, {
      //   data: {
      //     indicator: 'duplicate-project',
      //     projectName: '',
      //   },
      //   width: '500px',
      // });

      //OK will return the user to the Modal and allow further editing. just close the Modal for now
      this.snackbarService.open(
        'Project Created Successfully',
        'OK',
        { duration: 100000, panelClass: 'snackbar-success' },
      )
      this.dialogRef.close(this.projectForm.value);
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
