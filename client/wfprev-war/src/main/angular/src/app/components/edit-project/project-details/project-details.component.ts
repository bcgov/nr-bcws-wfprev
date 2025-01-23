import { CommonModule } from '@angular/common';
import { AfterViewInit, Component, EventEmitter, OnInit, Output  } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatExpansionModule } from '@angular/material/expansion';
import { ActivatedRoute } from '@angular/router';
import L from 'leaflet';
import { ProjectService } from 'src/app/services/project-services';
import { CodeTableServices } from 'src/app/services/code-table-services';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Messages } from 'src/app/utils/messages';
import { FormsModule } from '@angular/forms';
import {
  validateLatLong,
  formatLatLong,
} from 'src/app/utils/tools';
import { OnDestroy } from '@angular/core';

@Component({
  selector: 'app-project-details',
  standalone: true,
  imports: [ReactiveFormsModule,MatExpansionModule,CommonModule,FormsModule],
  templateUrl: './project-details.component.html',
  styleUrl: './project-details.component.scss'
})
export class ProjectDetailsComponent implements OnInit, AfterViewInit, OnDestroy{
  @Output() projectNameChange = new EventEmitter<string>();

  private map: L.Map | undefined;
  private marker: L.Marker | undefined;
  projectGuid = '';
  messages = Messages;
  detailsForm: FormGroup = this.fb.group({});
  originalFormValues: any = {};
  latLongForm: FormGroup = this.fb.group({
    latitude: [''],
    longitude: [''],
  });

  projectDetail: any;
  projectDescription: string = '';
  isProjectDescriptionDirty: boolean = false;
  latLong: string = ''; 
  isLatLongDirty: boolean = false;
  isLatLongValid: boolean = false;
  projectTypeCode: any[] = [];
  programAreaCode: any[] = [];
  forestRegionCode: any[] = [];
  forestDistrictCode: any[] = [];
  bcParksRegionCode: any[] = [];
  bcParksSectionCode: any[] = [];
  objectiveTypeCode: any[] = [];

  constructor(
    private readonly fb: FormBuilder,
    private route: ActivatedRoute,
    private projectService: ProjectService,
    private readonly codeTableService: CodeTableServices,
    public snackbarService: MatSnackBar,
  ) {}

  ngOnInit(): void {
    this.initializeForm();
    this.loadCodeTables();
    this.loadProjectDetails();
  }

  ngOnDestroy(): void {
    if (this.map) {
      this.map.remove(); // Clean up the map
    }
  }

  private initializeForm(): void {
    this.detailsForm = this.fb.group({
      projectTypeCode: ['', [Validators.required]],
      fundingStream: [''],
      programAreaGuid: ['', [Validators.required]],
      projectLead: [''],
      projectLeadEmailAddress: ['', [Validators.email]],
      siteUnitName: [''],
      closestCommunityName: ['', [Validators.required]],
      forestRegionOrgUnitId: [''],
      forestDistrictOrgUnitId: [''],
      primaryObjectiveTypeCode: [''],
      secondaryObjectiveTypeCode: [''],
      secondaryObjectiveRationale: [''],
      bcParksRegionOrgUnitId: [''],
      bcParksSectionOrgUnitId: [''],
      projectDescription: [''],
      latitude: [''],
      longitude: [''],
    });
    this.latLongForm = this.fb.group({
      latitude: ['', Validators.required],
      longitude: ['', Validators.required],
    });
  }

  loadProjectDetails(): void {
    this.projectGuid = this.route.snapshot?.queryParamMap?.get('projectGuid') || '';
    if (!this.projectGuid) return;
  
    this.projectService.getProjectByProjectGuid(this.projectGuid).subscribe({
      next: (data) => {
        this.projectDetail = data;
        this.projectNameChange.emit(data.projectName);
        if (data.latitude && data.longitude) {
          this.latLong = formatLatLong(data.latitude, data.longitude);
          this.updateMap(data.latitude, data.longitude);
        }
        this.isLatLongDirty = false;
        this.populateFormWithProjectDetails(data);
        this.originalFormValues = this.detailsForm.getRawValue();
        this.projectDescription = data.projectDescription;
        this.isProjectDescriptionDirty = false; 
        this.latLongForm.patchValue({
          latitude: data.latitude,
          longitude: data.longitude,
        });
      },
      error: (err) => {
        console.error('Error fetching project details:', err);
        this.projectDetail = null;
      },
    });
  }
  private callValidateLatLong(value: string) {
    return validateLatLong(value);
  }
  
  onLatLongChange(newLatLong: string): void {
    this.latLong = newLatLong;
    this.isLatLongDirty = true; // Always mark as dirty regardless of validity
  }
  

  populateFormWithProjectDetails(data: any): void {
    this.patchFormValues(data);
  }

  loadCodeTables(): void {
    const codeTables = [
      { name: 'projectTypeCodes', embeddedKey: 'projectTypeCode' },
      { name: 'programAreaCodes', embeddedKey: 'programAreaCode' },
      { name: 'forestRegionCodes', embeddedKey: 'forestRegionCode' },
      { name: 'forestDistrictCodes', embeddedKey: 'forestDistrictCode' },
      { name: 'bcParksRegionCodes', embeddedKey: 'bcParksRegionCode' },
      { name: 'bcParksSectionCodes', embeddedKey: 'bcParksSectionCode' },
      { name: 'objectiveTypeCodes', embeddedKey: 'objectiveTypeCode' },

    ];
  
    codeTables.forEach((table) => {
      this.codeTableService.fetchCodeTable(table.name).subscribe({
        next: (data) => {
          this.assignCodeTableData(table.embeddedKey, data);
        },
        error: (err) => {
          console.error(`Error fetching ${table.name}`, err);
          this.assignCodeTableData(table.embeddedKey, []); // Assign empty array on error
        },
      });
    });
  }
  assignCodeTableData(key: string, data: any): void {
    switch (key) {
      case 'projectTypeCode':
        this.projectTypeCode = data._embedded.projectTypeCode || [];
        break;
      case 'programAreaCode':
        this.programAreaCode = data._embedded.programArea || [];
        break;
      case 'forestRegionCode':
        this.forestRegionCode = data._embedded.forestRegionCode || [];
        break;
      case 'forestDistrictCode':
        this.forestDistrictCode = data._embedded.forestDistrictCode || [];
        break;
      case 'bcParksRegionCode':
        this.bcParksRegionCode = data._embedded.bcParksRegionCode || [];
        break;
      case 'bcParksSectionCode':
        this.bcParksSectionCode = data._embedded.bcParksSectionCode || [];
        break;
      case 'objectiveTypeCode':
        this.objectiveTypeCode = data._embedded.objectiveTypeCode || [];
        break;
    }
  }

  updateMap(latitude: number, longitude: number): void {
    if (this.map) {
      if (this.marker) {
        this.map.removeLayer(this.marker);
      }
      this.marker = L.marker([latitude, longitude]).addTo(this.map);
      this.map.setView([latitude, longitude], 13); // Update the map view
    } else {
      // Initialize the map if it hasn't been created
      this.map = L.map('map', {
        center: [latitude, longitude],
        zoom: 13,
        zoomControl: false,
      });
      
      L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        attribution: '© OpenStreetMap contributors',
      }).addTo(this.map);
      
      this.marker = L.marker([latitude, longitude]).addTo(this.map); // Add the marker
    }
  }
  
  
  ngAfterViewInit(): void {
    setTimeout(() => {
      this.initMap();
    });
  }

  combineCoordinates(latitude: number | string, longitude: number | string): string {
    if (!latitude || !longitude) {
      return '';
    }
    return `${latitude}, ${longitude}`;
  }

  initMap(): void {
    const defaultBounds: L.LatLngBoundsExpression = [
      [48.3, -139.1], // Southwest corner of BC
      [60.0, -114.0], // Northeast corner of BC
    ];
  
    if (!this.map) {
      this.map = L.map('map', {
        zoomControl: false,
      });
  
      L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        attribution: '© OpenStreetMap contributors',
      }).addTo(this.map);
  
      this.map.fitBounds(defaultBounds); // Default view for the BC region
    }
  }

  patchFormValues(data: any): void {
    this.detailsForm.patchValue({
      projectTypeCode: data.projectTypeCode?.projectTypeCode || '',
      fundingStream: data.fundingStream,
      programAreaGuid: data.programAreaGuid || '',
      projectLead: data.projectLead,
      projectLeadEmailAddress: data.projectLeadEmailAddress,
      siteUnitName: data.siteUnitName,
      closestCommunityName: data.closestCommunityName,
      forestRegionOrgUnitId: data.forestRegionOrgUnitId,
      forestDistrictOrgUnitId: data.forestDistrictOrgUnitId,
      primaryObjectiveTypeCode: data.primaryObjectiveTypeCode?.objectiveTypeCode || '',
      secondaryObjectiveTypeCode: data.secondaryObjectiveTypeCode?.objectiveTypeCode,
      secondaryObjectiveRationale: data.secondaryObjectiveRationale,
      bcParksRegionOrgUnitId: data.bcParksRegionOrgUnitId,
      bcParksSectionOrgUnitId: data.bcParksSectionOrgUnitId,
      latitude: data.latitude,
      longitude: data.longitude,
    });
  }

  onSave(): void {
    if (this.detailsForm.valid) {
        const updatedProject = {
          ...this.projectDetail,
          ...this.detailsForm.value,
          forestRegionOrgUnitId: Number(this.detailsForm.get('forestRegionOrgUnitId')?.value),
          forestDistrictOrgUnitId: Number(this.detailsForm.get('forestDistrictOrgUnitId')?.value),
          bcParksRegionOrgUnitId: Number(this.detailsForm.get('bcParksRegionOrgUnitId')?.value),
          bcParksSectionOrgUnitId: Number(this.detailsForm.get('bcParksSectionOrgUnitId')?.value),
          projectTypeCode: this.detailsForm.get('projectTypeCode')?.value
          ? { projectTypeCode: this.detailsForm.get('projectTypeCode')?.value} : this.projectDetail.projectTypeCode,
          forestAreaCode: {
            forestAreaCode: "COAST",
          },
          primaryObjectiveTypeCode: {
            objectiveTypeCode: this.detailsForm.get('primaryObjectiveTypeCode')?.value 
                ? this.detailsForm.get('primaryObjectiveTypeCode')?.value 
                : this.projectDetail.primaryObjectiveTypeCode?.objectiveTypeCode
          },
          tertiaryObjectiveTypeCode: {
            objectiveTypeCode: 'FOR_HEALTH'
          }      
        };
        const secondaryObjectiveValue = this.detailsForm.get('secondaryObjectiveTypeCode')?.value;
        if (secondaryObjectiveValue) {
          updatedProject.secondaryObjectiveTypeCode = {
            objectiveTypeCode: secondaryObjectiveValue,
          };
        }
        this.projectService.updateProject(this.projectGuid, updatedProject).subscribe({
          next: () => {
            this.snackbarService.open(
              this.messages.projectUpdatedSuccess,
              'OK',
              { duration: 10000, panelClass: 'snackbar-success' }
            );
            this.projectService.getProjectByProjectGuid(this.projectGuid).subscribe({
              next :(data) =>{
                this.projectDetail = data; // Update the local projectDetail
                this.patchFormValues(data); // Update the form with the latest data
                this.originalFormValues = this.detailsForm.getRawValue(); // Update original form values
                this.detailsForm.markAsPristine(); // Mark the form as pristine
              }
            })
          },
          error: (error) => {
            this.snackbarService.open(
              this.messages.projectUpdatedFailure,
              'OK',
              { duration: 5000, panelClass: 'snackbar-error' }
            );
          },
        });
    } else {
      console.error('Form is invalid!');
    }
  }

  onProjectDescriptionChange(newDescription: string): void {
    this.isProjectDescriptionDirty = this.projectDescription !== this.projectDetail?.projectDescription;
  }

  onCancelProjectDescription(): void {
    this.isProjectDescriptionDirty = false; 
    if (this.projectDetail) {
      this.projectDescription = this.projectDetail.projectDescription;
      this.isProjectDescriptionDirty = false; 
    }
  }

  onSaveProjectDescription(): void {
    if (this.isProjectDescriptionDirty) {
      const updatedProject = {
        ...this.projectDetail,
        projectDescription: this.projectDescription,
      };

      this.projectService.updateProject(this.projectGuid, updatedProject).subscribe({
        next: () => {
          this.snackbarService.open(
            this.messages.projectUpdatedSuccess,
            'OK',
            { duration: 10000, panelClass: 'snackbar-success' }
          );
          // Re-fetch the project details
          this.projectService.getProjectByProjectGuid(this.projectGuid).subscribe({
            next: (data) => {
              this.projectDetail = data;
              this.projectDescription = data.projectDescription; // Update local description
              this.isProjectDescriptionDirty = false; // Reset dirty flag
            },
            error: (err) => {
              console.error('Error fetching updated project details:', err);
            },
          });
        },
        error: (error) => {
          this.snackbarService.open(
            this.messages.projectUpdatedFailure,
            'OK',
            { duration: 5000, panelClass: 'snackbar-error' }
          );
        },
      });
    }
  }
  

  onSaveLatLong(): void {
    const parsed = validateLatLong(this.latLong);

    if (!parsed) {
      this.isLatLongValid = false;
      this.snackbarService.open(
        'Invalid latitude/longitude. Please ensure it is in the correct format and within BC boundaries.',
        'OK',
        { duration: 5000, panelClass: 'snackbar-error' }
      );
      return;
    }

    this.isLatLongValid = true;

    const { latitude, longitude } = parsed;
    const updatedProject = {
      ...this.projectDetail,
      latitude,
      longitude,
    };
  
    this.projectService.updateProject(this.projectGuid, updatedProject).subscribe({
      next: () => {
        this.snackbarService.open(
          this.messages.projectUpdatedSuccess,
          'OK',
          { duration: 3000, panelClass: 'snackbar-success' }
        );
        this.projectService.getProjectByProjectGuid(this.projectGuid).subscribe({
          next: (data) => {
            this.projectDetail = data;
            this.latLong = formatLatLong(data.latitude, data.longitude);
            this.isLatLongDirty = false;
            this.updateMap(data.latitude, data.longitude);
          },
          error: (err) => {
            console.error('Error fetching updated project details:', err);
          },
        });
      },
      error: (err) => {
        console.error('Error saving latitude/longitude:', err);
        this.snackbarService.open(
          this.messages.projectUpdatedFailure,
          'OK',
          { duration: 3000, panelClass: 'snackbar-error' }
        );
      },
    });
  }

  onCancel(): void {
    // Reset form to original values
    this.detailsForm.reset(this.originalFormValues);
  }
}
