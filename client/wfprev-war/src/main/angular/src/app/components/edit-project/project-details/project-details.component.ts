import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { AfterViewInit, Component, EventEmitter, OnInit, Output  } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatExpansionModule } from '@angular/material/expansion';
import { ActivatedRoute } from '@angular/router';
import L from 'leaflet';
import { ProjectService } from 'src/app/services/project-services';
import { CodeTableServices } from 'src/app/services/code-table-services';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Messages } from 'src/app/utils/messages';

@Component({
  selector: 'app-project-details',
  standalone: true,
  imports: [ReactiveFormsModule,MatExpansionModule,CommonModule],
  templateUrl: './project-details.component.html',
  styleUrl: './project-details.component.scss'
})
export class ProjectDetailsComponent implements OnInit, AfterViewInit{
  @Output() projectNameChange = new EventEmitter<string>();

  private map: L.Map | undefined;
  private projectGuid = '';
  messages = Messages;
  detailsForm: FormGroup = this.fb.group({});
  originalFormValues: any = {};
  latLongForm: FormGroup = this.fb.group({
    latitude: [''],
    longitude: [''],
  });

  projectDetail: any;
  projectTypeCode: any[] = [];
  programAreaCode: any[] = [];
  forestRegionCode: any[] = [];
  forestDistrictCode: any[] = [];
  bcParksRegionCode: any[] = [];
  bcParksSectionCode: any[] = [];

  constructor(
    private readonly fb: FormBuilder,
    private readonly route: ActivatedRoute,
    private readonly projectService: ProjectService,
    private readonly codeTableService: CodeTableServices,
    private readonly snackbarService: MatSnackBar,
  ) {}

  ngOnInit(): void {
    this.initializeForm();
    this.loadCodeTables();
    this.loadProjectDetails();
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
      primaryObjective: [''],
      secondaryObjective: [''],
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
        this.populateFormWithProjectDetails(data);
        this.originalFormValues = this.detailsForm.getRawValue();
        this.latLongForm.patchValue({
          latitude: data.latitude,
          longitude: data.longitude,
        });
        if (data.latitude && data.longitude) {
          this.updateMap(data.latitude, data.longitude);
        }
      },
      error: (err) => {
        console.error('Error fetching project details:', err);
        this.projectDetail = null;
      },
    });
  }

  populateFormWithProjectDetails(data: any): void {
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
      primaryObjective: data.primaryObjective,
      secondaryObjective: data.secondaryObjective,
      secondaryObjectiveRationale: data.secondaryObjectiveRationale,
      bcParksRegionOrgUnitId: data.bcParksRegionOrgUnitId,
      bcParksSectionOrgUnitId: data.bcParksSectionOrgUnitId,
      projectDescription: data.projectDescription,
      latitude: data.latitude,
      longitude: data.longitude,
    });
  }

  loadCodeTables(): void {
    const codeTables = [
      { name: 'projectTypeCodes', embeddedKey: 'projectTypeCode' },
      { name: 'programAreaCodes', embeddedKey: 'programAreaCode' },
      { name: 'forestRegionCodes', embeddedKey: 'forestRegionCode' },
      { name: 'forestDistrictCodes', embeddedKey: 'forestDistrictCode' },
      { name: 'bcParksRegionCodes', embeddedKey: 'bcParksRegionCode' },
      { name: 'bcParksSectionCodes', embeddedKey: 'bcParksSectionCode' },
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
    }
  }

  updateMap(latitude: number, longitude: number): void {
    if (this.map) {
      this.map.setView([latitude, longitude], 13);
      L.marker([latitude, longitude]).addTo(this.map);
      return;
    }
  
    this.map = L.map('map', {
      center: [latitude, longitude],
      zoom: 13,
      zoomControl: false,
    });
  
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: '© OpenStreetMap contributors',
    }).addTo(this.map);
  
    L.marker([latitude, longitude]).addTo(this.map);
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
    if (this.map) {
      return;
    }
    this.map = L.map('map', {
      zoom: 13,
      zoomControl: false, 
    });

    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: '© OpenStreetMap contributors',
    }).addTo(this.map);
    

    // Add a marker at the project's coordinates
    // Bind a popup to the marker
    // marker.bindPopup('Project Location: ' + this.sampleData.projectName).openPopup();
  }

  onSave(): void {
    if (this.detailsForm.valid) {
      this.projectDetail;
      const updatedProject = {
        ...this.projectDetail,
        ...this.detailsForm.value,
        projectTypeCode: this.projectTypeCode.find(
          (item) => item.projectTypeCode === this.detailsForm.get('projectTypeCode')?.value
        ),
      };
      this.projectService.updateProject(this.projectGuid, updatedProject).subscribe({
        next: (response) => {
          this.snackbarService.open(
            this.messages.projectUpdatedSuccess,
            'OK',
            { duration: 100000, panelClass: 'snackbar-success' },
          );
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

  onSaveLatLong(): void {
    if (this.latLongForm.valid) {
      const { latitude, longitude } = this.latLongForm.value;

      // Update the map and backend
      this.updateMap(latitude, longitude);
      console.log('Latitude/Longitude saved:', latitude, longitude);
    } else {
      console.error('Latitude/Longitude form is invalid!');
    }
  }

  onCancel(): void {
    // Reset form to original values
    this.detailsForm.reset(this.originalFormValues);
  }
}
