import { TextFieldModule } from '@angular/cdk/text-field';
import { CommonModule } from '@angular/common';
import { AfterViewInit, Component, EventEmitter, OnDestroy, OnInit, Output, ViewChild } from '@angular/core';
import { FormBuilder, FormControl, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatTooltip } from '@angular/material/tooltip';
import { ActivatedRoute } from '@angular/router';
import L from 'leaflet';
import { forkJoin, map, Observable } from 'rxjs';
import { ConfirmationDialogComponent } from 'src/app/components/confirmation-dialog/confirmation-dialog.component';
import { FiscalYearProjectsComponent } from 'src/app/components/edit-project/project-details/fiscal-year-projects/fiscal-year-projects.component';
import { ProjectFilesComponent } from 'src/app/components/edit-project/project-details/project-files/project-files.component';
import { CodeTableServices } from 'src/app/services/code-table-services';
import { ProjectService } from 'src/app/services/project-services';
import { CodeTableKeys, Messages, FiscalYearColors, ModalTitles, ModalMessages } from 'src/app/utils/constants';
import {
  formatLatLong,
  getBluePinIcon,
  trimLatLong,
  validateLatLong,
  LeafletLegendService
} from 'src/app/utils/tools';
import { ExpansionIndicatorComponent } from '../../shared/expansion-indicator/expansion-indicator.component';
import { BcParksSectionCodeModel, ForestDistrictCodeModel } from 'src/app/components/models';
import { SelectFieldComponent } from 'src/app/components/shared/select-field/select-field.component';
import { InputFieldComponent } from 'src/app/components/shared/input-field/input-field.component';
import { EvaluationCriteriaComponent } from 'src/app/components/edit-project/project-details/evaluation-criteria/evaluation-criteria.component';
@Component({
  selector: 'wfprev-project-details',
  standalone: true,
  imports: [ReactiveFormsModule, MatExpansionModule, CommonModule, FormsModule, FiscalYearProjectsComponent,
    ProjectFilesComponent, MatTooltip, TextFieldModule, ExpansionIndicatorComponent, SelectFieldComponent, InputFieldComponent,
    EvaluationCriteriaComponent],
  templateUrl: './project-details.component.html',
  styleUrl: './project-details.component.scss'
})
export class ProjectDetailsComponent implements OnInit, AfterViewInit, OnDestroy {
  @ViewChild(FiscalYearProjectsComponent) fiscalYearProjectsComponent!: FiscalYearProjectsComponent;
  @Output() projectNameChange = new EventEmitter<string>();

  private map: L.Map | undefined;
  private readonly activityBoundaryGroup: L.LayerGroup = L.layerGroup();
  private marker: L.Marker | undefined;
  private isMapReady = false;
  boundaryLayer: L.GeoJSON | null = null;
  projectGuid = '';
  messages = Messages;
  detailsForm: FormGroup = this.fb.group({});
  originalFormValues: any = {};
  latLongForm: FormGroup = this.fb.group({
    latitude: [''],
    longitude: [''],
  });
  fiscalColorMap = FiscalYearColors;
  projectDetail: any;
  projectDescription: string = '';
  isProjectDescriptionDirty: boolean = false;
  latLong: string = '';
  allBcParksSections: BcParksSectionCodeModel[] = [];
  allForestDistricts: ForestDistrictCodeModel[] = [];
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
  projectFiscals: any[] = [];
  allActivities: any[] = [];
  allActivityBoundaries: any[] = [];

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
    this.setupDropdownDependencies();
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
      secondaryObjectiveRationale: ['', [Validators.maxLength(300)]],
      bcParksRegionOrgUnitId: [''],
      bcParksSectionOrgUnitId: [''],
      wildfireOrgUnitId: ['', [Validators.required]],
      latitude: [''],
      longitude: [''],
      resultsProjectCode: ['', [Validators.maxLength(8)]]
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
      { name: 'wildfireOrgUnits', property: 'wildfireOrgUnits', embeddedKey: 'wildfireOrgUnit' },
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

    const sortByName = (arr: any[]) =>
      [...arr].sort((a, b) =>
        (a.orgUnitName ?? a.programAreaName ?? a.description ?? '').toLowerCase()
          .localeCompare((b.orgUnitName ?? b.programAreaName ?? b.description ?? '').toLowerCase())
      );

    switch (key) {
      case 'projectTypeCode':
        this.projectTypeCode = sortByName(data._embedded.projectTypeCode ?? []);
        break;
      case 'programAreaCode':
        this.programAreaCode = sortByName(data._embedded.programArea ?? []);
        break;
      case 'forestRegionCode':
        this.forestRegionCode = sortByName(data._embedded.forestRegionCode ?? []);
        break;
      case 'forestDistrictCode':
        this.allForestDistricts = sortByName(data._embedded.forestDistrictCode ?? []);
        this.forestDistrictCode = [...this.allForestDistricts];
        break;
      case 'bcParksRegionCode':
        this.bcParksRegionCode = sortByName(data._embedded.bcParksRegionCode ?? []);
        break;
      case 'bcParksSectionCode':
        this.allBcParksSections = sortByName(data._embedded.bcParksSectionCode ?? []);
        this.bcParksSectionCode = [...this.allBcParksSections];
        break;
      case 'objectiveTypeCode':
        this.objectiveTypeCode = sortByName(data._embedded.objectiveTypeCode ?? []);
        break;
      case 'wildfireOrgUnit': {
        // filter out org units that are not fire centres
        const orgUnits = data._embedded.wildfireOrgUnit ?? [];
        const fireCentres = orgUnits.filter(
          (unit: { wildfireOrgUnitTypeCode: { wildfireOrgUnitTypeCode: string } }) =>
            unit.wildfireOrgUnitTypeCode?.wildfireOrgUnitTypeCode === 'FRC'
        );
        this.fireCentres = sortByName(fireCentres);
        break;
      }
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
      this.getAllActivitiesBoundaries();
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
      this.activityBoundaryGroup.addTo(this.map);
      this.map.fitBounds(defaultBounds); // Default view for the BC region
      const legendHelper = new LeafletLegendService();
      legendHelper.addLegend(this.map, this.fiscalColorMap);
    }
    this.isMapReady = true;
    if (this.allActivityBoundaries.length > 0) {
      this.renderActivityBoundaries();
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
      wildfireOrgUnitId: data.fireCentreOrgUnitId,
      latitude: data.latitude,
      longitude: data.longitude,
      resultsProjectCode: data.resultsProjectCode
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
        fireCentreOrgUnitId: Number(this.detailsForm.get('wildfireOrgUnitId')?.value),
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
        data: {
          indicator: 'confirm-unsave',
          title: ModalTitles.CONFIRM_UNSAVE_TITLE,
          message: ModalMessages.CONFIRM_UNSAVE_MESSAGE
        },
        width: '600px',
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

      case CodeTableKeys.WILDFIRE_ORG_UNIT_ID:
        return this.fireCentres.find(item => item.orgUnitIdentifier == value)?.orgUnitName ?? null

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

  getAllActivitiesBoundaries(): void {
    if (!this.projectGuid) return;

    this.projectService.getProjectFiscalsByProjectGuid(this.projectGuid).subscribe(data =>
      this.handleFiscalsResponse(data)
    );
  }

  private handleFiscalsResponse(data: any): void {
    this.projectFiscals = (data._embedded?.projectFiscals ?? []).sort(
      (a: { fiscalYear: number }, b: { fiscalYear: number }) => a.fiscalYear - b.fiscalYear
    );

    const activityRequests = this.projectFiscals.map(fiscal =>
      this.projectService.getFiscalActivities(this.projectGuid, fiscal.projectPlanFiscalGuid).pipe(
        map(response => this.mapFiscalActivities(response, fiscal))
      )
    );

    forkJoin(activityRequests).subscribe(allActivityArrays =>
      this.handleActivitiesResponse(allActivityArrays.flat())
    );
  }

  private mapFiscalActivities(response: any, fiscal: any): any[] {
    const activities = response?._embedded?.activities ?? [];
    return activities.map((activity: any) => ({
      ...activity,
      fiscalYear: fiscal.fiscalYear,
      projectPlanFiscalGuid: fiscal.projectPlanFiscalGuid
    }));
  }

  private handleActivitiesResponse(allActivities: any[]): void {
    if (allActivities.length === 0) return;

    const boundaryRequests = allActivities.map(activity =>
      this.projectService
        .getActivityBoundaries(this.projectGuid, activity.projectPlanFiscalGuid, activity.activityGuid)
        .pipe(map(boundary => this.mapActivityBoundary(boundary, activity)))
    );

    forkJoin(boundaryRequests).subscribe(allResults =>
      this.handleBoundariesResponse(allResults)
    );
  }

  private mapActivityBoundary(boundary: any, activity: any): any {
    return boundary ? {
      activityGuid: activity.activityGuid,
      fiscalYear: activity.fiscalYear,
      boundary: boundary?._embedded?.activityBoundary
    } : null;
  }

  private handleBoundariesResponse(results: any[]): void {
    const validResults = results.filter(r => r?.boundary && r.boundary.length > 0);
    const dedupedResults: any[] = [];
    const seenActivityGuids = new Set<string>();

    for (const result of validResults) {
      const { activityGuid, fiscalYear, boundary } = result;

      if (seenActivityGuids.has(activityGuid)) continue;
      seenActivityGuids.add(activityGuid);

      const latestBoundary = boundary.reduce(
        (latest: { systemStartTimestamp?: string }, current: { systemStartTimestamp?: string }) =>
          new Date(current.systemStartTimestamp ?? 0) > new Date(latest.systemStartTimestamp ?? 0)
            ? current
            : latest
      );

      dedupedResults.push({
        activityGuid,
        fiscalYear,
        boundary: [latestBoundary],
      });
    }

    this.allActivityBoundaries = dedupedResults;
    if (this.isMapReady) {
      this.renderActivityBoundaries();
    }
  }

  renderActivityBoundaries(): void {
    if (!this.isMapReady || !this.map) return;

    this.activityBoundaryGroup.clearLayers();

    const currentFiscalYear = new Date().getFullYear();
    const allBounds: L.LatLngBounds[] = [];

    this.allActivityBoundaries.forEach(entry => {
      const fiscalYear = entry.fiscalYear;
      const color = this.getFiscalYearColor(fiscalYear, currentFiscalYear);

      entry.boundary.forEach((ab: any) => {
        const geometry = ab.geometry;
        if (!geometry?.type || !geometry?.coordinates) return;

        const geometries = geometry.type === 'GeometryCollection'
          ? geometry.geometries
          : [geometry];

        geometries.forEach((geom: any) => {
          const layer = L.geoJSON(geom, {
            style: {
              color,
              weight: 2,
              fillOpacity: 0.1,
            }
          });

          layer.addTo(this.activityBoundaryGroup);

          const bounds = layer.getBounds();
          if (bounds?.isValid()) {
            allBounds.push(bounds);
          }
        });
      });
    });

    if (allBounds.length > 0) {
      const combinedBounds = allBounds.reduce((acc, b) => acc.extend(b), allBounds[0]);
      this.map.fitBounds(combinedBounds);
    }
  }

  private getFiscalYearColor(fiscalYear: number, currentFiscalYear: number): string {
    if (fiscalYear < currentFiscalYear) return FiscalYearColors.past;
    if (fiscalYear === currentFiscalYear) return FiscalYearColors.present;
    return FiscalYearColors.future;
  }

  private setupDropdownDependencies(): void {
    this.detailsForm.get('forestRegionOrgUnitId')?.valueChanges.subscribe(regionId => {
      this.forestDistrictCode = this.allForestDistricts.filter(d => d.parentOrgUnitId == regionId);

      const selected = this.detailsForm.get('forestDistrictOrgUnitId')?.value;
      const validIds = this.forestDistrictCode.map(d => d.orgUnitId);
      if (!validIds.includes(selected)) {
        this.detailsForm.get('forestDistrictOrgUnitId')?.setValue('');
      }
    });

    this.detailsForm.get('bcParksRegionOrgUnitId')?.valueChanges.subscribe(regionId => {
      this.bcParksSectionCode = this.allBcParksSections.filter(d => d.parentOrgUnitId == regionId);

      if (regionId) {
        this.detailsForm.get('bcParksSectionOrgUnitId')?.enable();
      } else {
        this.detailsForm.get('bcParksSectionOrgUnitId')?.reset();
        this.detailsForm.get('bcParksSectionOrgUnitId')?.disable();
      }
    });
  }

  getControl(controlName: keyof typeof CodeTableKeys | string): FormControl {
    return this.detailsForm.get(controlName) as FormControl;
  }
}
