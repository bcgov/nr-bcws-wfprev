import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatDialogRef } from '@angular/material/dialog';

@Component({
  selector: 'app-create-new-project-dialog',
  standalone: true,
  imports: [ReactiveFormsModule,CommonModule],
  templateUrl: './create-new-project-dialog.component.html',
  styleUrls: ['./create-new-project-dialog.component.scss']
})
export class CreateNewProjectDialogComponent {
  projectForm: FormGroup;
  businessAreas = ['Area 1', 'Area 2', 'Area 3']; // Example data
  forestRegions = ['Region 1', 'Region 2', 'Region 3']; // Example data
  forestDistricts = ['District 1', 'District 2', 'District 3']; // Example data
  bcParksRegions = ['Park Region 1', 'Park Region 2']; // Example data
  bcParksSections = ['Section 1', 'Section 2']; // Example data

  constructor(
    private fb: FormBuilder,
    private dialogRef: MatDialogRef<CreateNewProjectDialogComponent>
  ) {
    this.projectForm = this.fb.group({
      projectName: ['', Validators.required],
      latLong: [''],
      businessArea: ['', Validators.required],
      forestRegion: ['', Validators.required],
      forestDistrict: ['', Validators.required],
      bcParksRegion: ['', Validators.required],
      bcParksSection: ['', Validators.required],
      projectLead: [''],
      projectLeadEmail: ['', Validators.email],
      siteUnitName: [''],
      closestCommunity: ['', Validators.required],
    });
  }

  onCreate(): void {
    if (this.projectForm.valid) {
      console.log(this.projectForm.value);
      this.dialogRef.close(this.projectForm.value);
    }
  }

  onCancel(): void {
    this.dialogRef.close();
  }
}
