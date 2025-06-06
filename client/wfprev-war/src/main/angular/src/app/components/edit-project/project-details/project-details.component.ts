import { TextFieldModule } from '@angular/cdk/text-field';
import { CommonModule } from '@angular/common';
import { AfterViewInit, Component, EventEmitter, OnDestroy, OnInit, Output, ViewChild } from '@angular/core';
import { FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatTooltip } from '@angular/material/tooltip';
import { ActivatedRoute } from '@angular/router';
import L from 'leaflet';
import { Observable } from 'rxjs';
import { ConfirmationDialogComponent } from 'src/app/components/confirmation-dialog/confirmation-dialog.component';
import { FiscalYearProjectsComponent } from 'src/app/components/edit-project/project-details/fiscal-year-projects/fiscal-year-projects.component';
import { ProjectFilesComponent } from 'src/app/components/edit-project/project-details/project-files/project-files.component';
import { CodeTableServices } from 'src/app/services/code-table-services';
import { ProjectService } from 'src/app/services/project-services';
import { CodeTableKeys, Messages } from 'src/app/utils/constants';
import {
  formatLatLong,
  getBluePinIcon,
  trimLatLong,
  validateLatLong,
} from 'src/app/utils/tools';
import { ExpansionIndicatorComponent } from '../../shared/expansion-indicator/expansion-indicator.component';

@Component({
  selector: 'wfprev-project-details',
  standalone: true,
  imports: [ReactiveFormsModule, MatExpansionModule, CommonModule, FormsModule, FiscalYearProjectsComponent, ProjectFilesComponent, MatTooltip, TextFieldModule, ExpansionIndicatorComponent],
  templateUrl: './project-details.component.html',
  styleUrl: './project-details.component.scss'
})
export class ProjectDetailsComponent implements OnInit, AfterViewInit, OnDestroy {
  @ViewChild(FiscalYearProjectsComponent) fiscalYearProjectsComponent!: FiscalYearProjectsComponent;
  @Output() projectNameChange = new EventEmitter<string>();

  private map: L.Map | undefined;
  private marker: L.Marker | undefined;
  boundaryLayer: L.GeoJSON | null = null;
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
  fireCentres: any[] = [];
  readonly CodeTableKeys = CodeTableKeys;
  
  constructor(
    private readonly fb: FormBuilder,
    private route: ActivatedRoute,
    private projectService: ProjectService,
    private readonly codeTableService: CodeTableServices,
    public snackbarService: MatSnackBar,
    public dialog: MatDialog,

  ) { }

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

  refreshFiscalData(): void {
    if (this.fiscalYearProjectsComponent) {
      this.fiscalYearProjectsComponent.loadProjectFiscals();
    }
  }

  private initializeForm(): void {
    this.detailsForm = this.fb.group({
      projectTypeCode: ['', [Validators.required]],
      fundingStream: [''],
      programAreaGuid: ['', [Validators.required]],
      projectLead: ['', [Validators.required, Validators.maxLength(50)]],
      projectLeadEmailAddress: ['', [Validators.email, Validators.maxLength(50)]],
      siteUnitName: ['', [Validators.maxLength(50)]],
      closestCommunityName: ['', [Validators.required, Validators.maxLength(50)]],
      forestRegionOrgUnitId: ['', [Validators.required]],
      forestDistrictOrgUnitId: [''],
      primaryObjectiveTypeCode: ['', [Validators.required]],
      secondaryObjectiveTypeCode: [''],
      secondaryObjectiveRationale: ['', [Validators.maxLength(50)]],
      bcParksRegionOrgUnitId: [''],
      bcParksSectionOrgUnitId: [''],
      fireCentreId: ['', [Validators.required]],
      latitude: [''],
      longitude: [''],
    });
    this.latLongForm = this.fb.group({
      latitude: ['', Validators.required],
      longitude: ['', Validators.required],
    });
  }

  loadProjectDetails(): void {
    this.projectGuid = this.route.snapshot?.queryParamMap?.get('projectGuid') ?? '';
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

  onFilesUpdated(): void {
    this.loadProjectDetails();
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

    this.loadFireCentres();
  }

  loadFireCentres(): void {
    this.codeTableService.fetchFireCentres().subscribe({
      next: (response) => {
        this.fireCentres = response?.features ?? [];
      },
      error: (error) => {
        console.error('Failed to load fire centres', error);
      }
    });
  }
  assignCodeTableData(key: string, data: any): void {
    switch (key) {
      case 'projectTypeCode':
        this.projectTypeCode = data._embedded.projectTypeCode ?? [];
        break;
      case 'programAreaCode':
        this.programAreaCode = data._embedded.programArea ?? [];
        break;
      case 'forestRegionCode':
        this.forestRegionCode = data._embedded.forestRegionCode ?? [];
        break;
      case 'forestDistrictCode':
        this.forestDistrictCode = data._embedded.forestDistrictCode ?? [];
        break;
      case 'bcParksRegionCode':
        this.bcParksRegionCode = data._embedded.bcParksRegionCode ?? [];
        break;
      case 'bcParksSectionCode':
        this.bcParksSectionCode = data._embedded.bcParksSectionCode ?? [];
        break;
      case 'objectiveTypeCode':
        this.objectiveTypeCode = data._embedded.objectiveTypeCode ?? [];
        break;
    }
  }

  updateMap(latitude: number, longitude: number): void {
    if (this.map) {
      if (this.marker) {
        this.map.removeLayer(this.marker);
      }
      const teardropIcon = getBluePinIcon();
      this.marker = L.marker([latitude, longitude], { icon: teardropIcon }).addTo(this.map);

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


      this.marker = L.marker([latitude, longitude], {
        icon: getBluePinIcon()
      }).addTo(this.map);

    }

    if (this.projectGuid) {
      this.projectService.getProjectBoundaries(this.projectGuid).subscribe({
        next: (boundaryResponse) => {
          const boundaries = boundaryResponse?._embedded?.projectBoundary;

          // Remove old boundary layer if it exists
          if (this.boundaryLayer && this.map) {
            this.map.removeLayer(this.boundaryLayer);
            this.boundaryLayer = null;
          }

          if (boundaries && boundaries.length > 0) {
            const latestBoundary = boundaries.sort((a: any, b: any) =>
              new Date(b.systemStartTimestamp).getTime() - new Date(a.systemStartTimestamp).getTime()
            )[0];

            const boundaryGeometry = latestBoundary.boundaryGeometry;

            if (boundaryGeometry && this.map) {
              // Create new GeoJSON layer
              this.boundaryLayer = L.geoJSON(boundaryGeometry, {
                style: {
                  color: '#000000',
                  weight: 3,
                  opacity: 1,
                  fillOpacity: 0
                }
              }).addTo(this.map);

              // Fit the map view to the new boundary
              if (this.boundaryLayer?.getBounds()?.isValid()) {
                this.map.fitBounds(this.boundaryLayer.getBounds());
              }
            }
          }
        },
        error: (error) => {
          console.error('Error fetching project boundaries', error);
        }
      });
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
      projectTypeCode: data.projectTypeCode?.projectTypeCode ?? '',
      fundingStream: data.fundingStream,
      programAreaGuid: data.programAreaGuid ?? '',
      projectLead: data.projectLead,
      projectLeadEmailAddress: data.projectLeadEmailAddress,
      projectDescription: data.projectDescription,
      siteUnitName: data.siteUnitName,
      closestCommunityName: data.closestCommunityName,
      forestRegionOrgUnitId: data.forestRegionOrgUnitId === 0 ? '' : data.forestRegionOrgUnitId ?? '',
      forestDistrictOrgUnitId: data.forestDistrictOrgUnitId === 0 ? '' : data.forestDistrictOrgUnitId ?? '',
      primaryObjectiveTypeCode: data.primaryObjectiveTypeCode?.objectiveTypeCode ?? '',
      secondaryObjectiveTypeCode: data.secondaryObjectiveTypeCode?.objectiveTypeCode ?? '',
      secondaryObjectiveRationale: data.secondaryObjectiveRationale,
      bcParksRegionOrgUnitId: data.bcParksRegionOrgUnitId === 0 ? '' : data.bcParksRegionOrgUnitId ?? '',
      bcParksSectionOrgUnitId: data.bcParksSectionOrgUnitId === 0 ? '' : data.bcParksSectionOrgUnitId ?? '',
      fireCentreId: data.fireCentreOrgUnitId,
      latitude: data.latitude,
      longitude: data.longitude,
    });
    console.log(this.detailsForm)
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
        fireCentreOrgUnitId: Number(this.detailsForm.get('fireCentreId')?.value),
        projectTypeCode: this.detailsForm.get('projectTypeCode')?.value
          ? { projectTypeCode: this.detailsForm.get('projectTypeCode')?.value } : this.projectDetail.projectTypeCode,
        primaryObjectiveTypeCode: {
          objectiveTypeCode: this.detailsForm.get('primaryObjectiveTypeCode')?.value
            ? this.detailsForm.get('primaryObjectiveTypeCode')?.value
            : this.projectDetail.primaryObjectiveTypeCode?.objectiveTypeCode
        },
      };
      const secondaryObjectiveValue = this.detailsForm.get('secondaryObjectiveTypeCode')?.value;
      updatedProject.secondaryObjectiveTypeCode = secondaryObjectiveValue
        ? { objectiveTypeCode: secondaryObjectiveValue }
        : null;
      console.log(JSON.stringify(updatedProject))
      this.projectService.updateProject(this.projectGuid, updatedProject).subscribe({
        next: () => {
          this.snackbarService.open(
            this.messages.projectUpdatedSuccess,
            'OK',
            { duration: 10000, panelClass: 'snackbar-success' }
          );
          this.projectService.getProjectByProjectGuid(this.projectGuid).subscribe({
            next: (data) => {
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
      latitude: trimLatLong(Number(latitude)),
      longitude: trimLatLong(Number(longitude)),
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

  public isFormDirty(): boolean {
    return this.detailsForm.dirty || this.isProjectDescriptionDirty || this.isLatLongDirty;
  }


  canDeactivate(): Observable<boolean> | boolean {
    if (this.isFormDirty()) {
      const dialogRef = this.dialog.open(ConfirmationDialogComponent, {
        data: { indicator: 'confirm-unsave' },
        width: '500px',
      });
      return dialogRef.afterClosed();
    }
    return true;
  }
  getCodeDescription(controlName: string): string | null {
    const value = this.detailsForm.get(controlName)?.value;

    switch (controlName) {
      case CodeTableKeys.PROJECT_TYPE_CODE:
        return this.projectTypeCode.find(item => item.projectTypeCode === value)?.description ?? null;

      case CodeTableKeys.PROGRAM_AREA_GUID:
        return this.programAreaCode.find(item => item.programAreaGuid === value)?.programAreaName ?? null;

      case CodeTableKeys.FOREST_REGION_ORG_UNIT_ID:
        return this.forestRegionCode.find(item => item.orgUnitId === value)?.orgUnitName ?? null;

      case CodeTableKeys.FOREST_DISTRICT_ORG_UNIT_ID:
        return this.forestDistrictCode.find(item => item.orgUnitId === value)?.orgUnitName ?? null;

      case CodeTableKeys.BC_PARKS_REGION_ORG_UNIT_ID:
        return this.bcParksRegionCode.find(item => item.orgUnitId === value)?.orgUnitName ?? null;

      case CodeTableKeys.BC_PARKS_SECTION_ORG_UNIT_ID:
        return this.bcParksSectionCode.find(item => item.orgUnitId === value)?.orgUnitName ?? null;

      case CodeTableKeys.FIRE_CENTRE_ID:
        return this.fireCentres.find(item => item.properties.MOF_FIRE_CENTRE_ID === value)?.properties.MOF_FIRE_CENTRE_NAME ?? null;

      case CodeTableKeys.PRIMARY_OBJECTIVE_TYPE_CODE:
      case CodeTableKeys.SECONDARY_OBJECTIVE_TYPE_CODE:
        return this.objectiveTypeCode.find(item => item.objectiveTypeCode === value)?.description ?? null;

      default:
        return null;
    }
  }

  getErrorMessage(controlName: string): string | null {
    const control = this.detailsForm.get(controlName);
    if (!control?.errors) return null;

    if (control.hasError('maxlength')) {
      return this.messages.maxLengthExceeded;
    }
    if (control.hasError('email')) {
      return this.messages.invalidEmail;
    }

    return null;
  }
}
