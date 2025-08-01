import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatDialog } from '@angular/material/dialog';
import { MatTooltipModule } from '@angular/material/tooltip';
import { Router } from '@angular/router';
import L from 'leaflet';
import 'leaflet.markercluster';
import { CreateNewProjectDialogComponent } from 'src/app/components/create-new-project-dialog/create-new-project-dialog.component';
import { CodeTableServices } from 'src/app/services/code-table-services';
import { ProjectService } from 'src/app/services/project-services';
import { SharedCodeTableService } from 'src/app/services/shared-code-table.service';
import { SharedService } from 'src/app/services/shared-service';
import { getBluePinIcon, getFiscalYearDisplay, PlanFiscalStatusIcons } from 'src/app/utils/tools';
import { ResourcesRoutes, getActiveMap } from 'src/app/utils';
import { ExpansionIndicatorComponent } from '../../shared/expansion-indicator/expansion-indicator.component';
import { IconButtonComponent } from 'src/app/components/shared/icon-button/icon-button.component';
import { MatSelectModule } from '@angular/material/select';
import { StatusBadgeComponent } from 'src/app/components/shared/status-badge/status-badge.component';
import { CodeTableKeys, CodeTableNames, DownloadFileExtensions, DownloadTypes, Messages, WildfireOrgUnitTypeCodes } from 'src/app/utils/constants';
import { DownloadButtonComponent } from 'src/app/components/shared/download-button/download-button.component';
import { MatSnackBar } from '@angular/material/snack-bar';


@Component({
  selector: 'wfprev-projects-list',
  standalone: true,
  imports: [MatSlideToggleModule, CommonModule, MatExpansionModule, MatTooltipModule, ExpansionIndicatorComponent, IconButtonComponent, MatSelectModule, StatusBadgeComponent, DownloadButtonComponent],
  templateUrl: './projects-list.component.html',
  styleUrls: ['./projects-list.component.scss'],
})
export class ProjectsListComponent implements OnInit {
  [key: string]: any;
  projectList: any[] = [];
  programAreaCode: any[] = [];
  forestRegionCode: any[] = [];
  forestDistrictCode: any[] = [];
  bcParksRegionCode: any[] = [];
  bcParksSectionCode: any[] = [];
  planFiscalStatusCode: any[] = [];
  activityCategoryCode: any[] = [];
  projectTypeCode: any[] = [];
  fireCentreCodes: any[] = [];
  markerPolygons: Map<L.Marker, L.Polygon[]> = new Map();
  activeMarker: L.Marker | null = null;
  getActiveMap = getActiveMap
  allProjects: any[] = [];
  displayedProjects: any[] = [];
  isLoading = false;
  selectedProjectGuid: string | null = null;
  getFiscalYearDisplay = getFiscalYearDisplay;
  expandedPanels: Record<string, boolean> = {};
  formats = [DownloadTypes.CSV, DownloadTypes.EXCEL];
  constructor(
    private readonly router: Router,
    private readonly projectService: ProjectService,
    private readonly codeTableService: CodeTableServices,
    private readonly dialog: MatDialog,
    private readonly sharedCodeTableService: SharedCodeTableService,
    public readonly sharedService: SharedService,
    private readonly cdr: ChangeDetectorRef,
    private readonly snackbarService: MatSnackBar
  ) {
  }
  ngOnInit(): void {
    this.loadCodeTables();
    this.loadProjects();

    this.sharedService.selectedProject$.subscribe(project => {
      this.selectedProjectGuid = project?.projectGuid ?? null;
      this.cdr.markForCheck();
    });

    this.sharedService.filters$.subscribe(filters => {
      if (filters) {
        this.isLoading = true;
        this.projectService.getFeatures(filters).subscribe({
          next: (data) => this.processProjectsResponse(data),
          error: (err) => this.handleProjectError(err),
        });
      }
    });
  }

  loadCodeTables(): void {
    const codeTables = [
      { name: CodeTableNames.PROGRAM_AREA_CODE, property: CodeTableKeys.BUSINESS_AREAS, embeddedKey: CodeTableKeys.PROGRAM_AREA },
      { name: CodeTableNames.FOREST_REGION_CODE, property: CodeTableKeys.FOREST_REGIONS, embeddedKey: CodeTableKeys.FOREST_REGION_CODE},
      { name: CodeTableNames.FOREST_DISTRICT_CODE, property: CodeTableKeys.FOREST_DISTRICTS, embeddedKey: CodeTableKeys.FOREST_DISTRICT_CODE },
      { name: CodeTableNames.BC_PARKS_REGION_CODE, property: CodeTableKeys.BC_PARKS_REGIONS, embeddedKey: CodeTableKeys.BC_PARKS_REGION_CODE },
      { name: CodeTableNames.BC_PARKS_SECTION_CODE, property: CodeTableKeys.BC_PARKS_SECTIONS, embeddedKey: CodeTableKeys.BC_PARKS_SECTION_CODE },
      { name: CodeTableNames.PLAN_FISCAL_STATUS_CODE, property: CodeTableKeys.PLAN_FISCAL_STATUS_CODE, embeddedKey: CodeTableKeys.PLAN_FISCAL_STATUS_CODE },
      { name: CodeTableNames.ACTIVITY_CATEGORY_CODE, property: CodeTableKeys.ACTIVITY_CATEGORY_CODE, embeddedKey: CodeTableKeys.ACTIVITY_CATEGORY_CODE },
      { name: CodeTableNames.PROJECT_TYPE_CODE, property: CodeTableKeys.PROJECT_TYPE_CODE, embeddedKey: CodeTableKeys.PROJECT_TYPE_CODE },
      { name: CodeTableNames.WILDFIRE_ORG_UNIT, property: CodeTableKeys.WILDFIRE_ORG_UNIT, embeddedKey: CodeTableKeys.WILDFIRE_ORG_UNIT }

    ];
    const loaded: any = {};
    let loadedCount = 0;
    const totalTables = codeTables.length;


    codeTables.forEach((table) => {
      this.codeTableService.fetchCodeTable(table.name).subscribe({
        next: (data) => {
          if (table.name === CodeTableNames.PROGRAM_AREA_CODE) {
            this.programAreaCode = data._embedded.programArea;
          } else if (table.name === CodeTableNames.FOREST_REGION_CODE) {
            this.forestRegionCode = data._embedded.forestRegionCode;
          } else if (table.name === CodeTableNames.FOREST_DISTRICT_CODE) {
            this.forestDistrictCode = data._embedded.forestDistrictCode;
          } else if (table.name === CodeTableNames.BC_PARKS_REGION_CODE) {
            this.bcParksRegionCode = data._embedded.bcParksRegionCode;
          } else if (table.name === CodeTableNames.BC_PARKS_SECTION_CODE) {
            this.bcParksSectionCode = data._embedded.bcParksSectionCode;
          } else if (table.name === CodeTableNames.PLAN_FISCAL_STATUS_CODE) {
            this.planFiscalStatusCode = data._embedded.planFiscalStatusCode;
          } else if (table.name === CodeTableNames.ACTIVITY_CATEGORY_CODE) {
            this.activityCategoryCode = data._embedded.activityCategoryCode;
          } else if (table.name === CodeTableNames.PROJECT_TYPE_CODE) {
            this.projectTypeCode = data._embedded.projectTypeCode;
          } else if (table.name === CodeTableNames.WILDFIRE_ORG_UNIT) {
            const orgUnits = data._embedded.wildfireOrgUnit ?? [];
            const fireCentres = orgUnits.filter(
              (unit: any) => unit.wildfireOrgUnitTypeCode?.wildfireOrgUnitTypeCode === WildfireOrgUnitTypeCodes.FIRE_CENTRE
            );
            this.fireCentreCodes = fireCentres;
          }

          const items = data._embedded?.[table.embeddedKey] ?? [];
          this[table.property] = items;
          loaded[table.property] = items;

          loadedCount++;
          if (loadedCount === totalTables) {
            this.sharedCodeTableService.updateCodeTables(loaded);
          }

        },
        error: (err) => {
          console.error(`Error fetching ${table.name}`, err);

          // Explicitly set the property to an empty array on error
          if (table.name === CodeTableNames.PROGRAM_AREA_CODE) {
            this.programAreaCode = [];
          } else if (table.name === CodeTableNames.FOREST_REGION_CODE) {
            this.forestRegionCode = [];
          } else if (table.name === CodeTableNames.FOREST_DISTRICT_CODE) {
            this.forestDistrictCode = [];
          } else if (table.name === CodeTableNames.BC_PARKS_REGION_CODE) {
            this.bcParksRegionCode = [];
          } else if (table.name === CodeTableNames.BC_PARKS_SECTION_CODE) {
            this.bcParksSectionCode = [];
          } else if (table.name === CodeTableNames.PLAN_FISCAL_STATUS_CODE) {
            this.planFiscalStatusCode = [];
          } else if (table.name === CodeTableNames.ACTIVITY_CATEGORY_CODE) {
            this.activityCategoryCode = [];
          } else if (table.name === CodeTableNames.PROJECT_TYPE_CODE) {
            this.activityCategoryCode = [];
          } else if (table.name === CodeTableNames.WILDFIRE_ORG_UNIT) {
            this.fireCentreCodes = [];
          }

          this[table.property] = [];
          loaded[table.property] = [];

          loadedCount++;
          if (loadedCount === totalTables) {
            this.sharedCodeTableService.updateCodeTables(loaded);
          }
        },
      });
    });
  }

  sortOptions = [
    { label: 'Name (A-Z)', value: 'ascending' },
    { label: 'Name (Z-A)', value: 'descending' },
  ];

  selectedSort = '';
  syncWithMap = false;
  resultCount = 3;

  fiscalYearActivityTypes = ['Clearing', 'Burning', 'Pruning']

  loadProjects(): void {
    this.isLoading = true;
    this.projectService.getFeatures().subscribe({
      next: (data) => this.processProjectsResponse(data),
      error: (err) => this.handleProjectError(err),
    });
  }

  onSortChange(event: any): void {
    this.selectedSort = event.value;

    if (this.selectedSort === 'ascending') {
      this.allProjects.sort((a, b) => a.projectName.localeCompare(b.projectName));
    } else if (this.selectedSort === 'descending') {
      this.allProjects.sort((a, b) => b.projectName.localeCompare(a.projectName));
    }
    this.displayedProjects = this.allProjects;
    this.sharedService.updateDisplayedProjects(this.displayedProjects);
  }


  editProject(project: any, event: Event) {
    event.stopPropagation();
    this.router.navigate([ResourcesRoutes.EDIT_PROJECT], {
      queryParams: { projectGuid: project.projectGuid }
    });
  }

  getDescription(codeTable: string, code: number | string): string {
    const table = this[codeTable];
    if (!table || !code) return '';

    let entry;

    switch (codeTable) {
      case CodeTableKeys.PROGRAM_AREA_CODE:
        entry = table.find((item: any) => item.programAreaGuid === code);
        return entry ? entry.programAreaName : '';
      case CodeTableKeys.FOREST_REGION_CODE:
      case CodeTableKeys.FOREST_DISTRICT_CODE:
      case CodeTableKeys.BC_PARKS_REGION_CODE:
      case CodeTableKeys.BC_PARKS_SECTION_CODE:
        entry = table.find((item: any) => item.orgUnitId === code);
        return entry ? entry.orgUnitName : '';

      case CodeTableKeys.PLAN_FISCAL_STATUS_CODE:
        entry = table.find((item: any) => item.planFiscalStatusCode === code);
        return entry ? entry.description : '';

      case CodeTableKeys.ACTIVITY_CATEGORY_CODE:
        entry = table.find((item: any) => item.activityCategoryCode === code);
        return entry ? entry.description : '';

      case CodeTableKeys.PROJECT_TYPE_CODE:
        entry = table.find((item: any) => item.projectTypeCode === code);
        return entry ? entry.description : '';
      default:
        return '';
    }
  }


  createNewProject(): void {
    const dialogRef = this.dialog.open(CreateNewProjectDialogComponent, {
      width: '1000px',
      disableClose: true,
      hasBackdrop: true,
    });
    // Subscribe to the afterClosed method to handle the result
    dialogRef.afterClosed().subscribe((result) => {
      if (result && result.success === true && result.projectGuid) {
        //if the result has projectGuid return, navigate immediately
        this.router.navigate([ResourcesRoutes.EDIT_PROJECT], {
          queryParams: { projectGuid: result.projectGuid }
        });
      } else if (result && result.success === true) {
        //if no projectGuid returned, reload projects normally
        this.loadProjects();
      }
    });
  }

  loadCoordinatesOnMap() {
    if (this.displayedProjects?.length > 0) {
      const coords = this.displayedProjects
        .filter((project: any) => project.latitude != null && project.longitude != null) // Check if both latitude and longitude are defined and not null
        .map((project: any) => ({
          latitude: project.latitude,
          longitude: project.longitude
        }));

      const markersCluster = L.markerClusterGroup({
        showCoverageOnHover: false,
        iconCreateFunction: (cluster) => {
          const count = cluster.getChildCount(); // Get the number of markers in the cluster
          return L.divIcon({
            html: `<div class="cluster-icon"><span>${count}</span></div>`, // Custom HTML structure
            className: 'custom-marker-cluster', // Custom CSS class
            iconSize: L.point(40, 40), // Size of the cluster marker
          });
        },
      });

      const map = this.getActiveMap();
      const markerStates: Map<L.Marker, boolean> = new Map(); // Track active state for each marker

      // Helper function to create a marker with the specified icon URL
      const createMarkerIcon = (iconUrl: string) => {
        return L.icon({
          iconUrl,
          iconSize: [30, 50],
          iconAnchor: [12, 41],
          popupAnchor: [1, -34],
        });
      };

      const createActiveMarkerIcon = (iconUrl: string) => {
        return L.icon({
          iconUrl,
          iconSize: [50, 70],
          iconAnchor: [20, 51],
          popupAnchor: [1, -34],
        });
      };

      const self = this;

      // Helper function to generate random polygon points. This will be replaced by /features endpoint in API
      const generatePolygonPoints = (
        center: { latitude: number; longitude: number },
        radius: number = 50, // Average radius of 15 km
        variance: number = 5  // Variance of ±5 km
      ) => {
        const points = [];
        for (let i = 0; i < 50; i++) {
          const angle = (i / 50) * Math.PI * 2; // Angle in radians
          const randomFactor = self.getSecureRandomNumber(); // Secure random value
          const offset = radius + randomFactor * variance - variance / 2; // Randomize radius
          const lat = center.latitude + offset * Math.sin(angle) / 111; // Approx 111 km per degree latitude
          const lng = center.longitude + offset * Math.cos(angle) / (111 * Math.cos(center.latitude * (Math.PI / 180)));
          points.push([lat, lng]);
        }
        points.push(points[0]); // Close the polygon
        return points;
      };
      // Define colors for the polygons
      const colors = ['#7570B3', '#56B193', '#E7298A', '#474543'];

      // Add markers and polygons for each coordinate
      coords.forEach((coord) => {
        const marker = L.marker([coord.latitude, coord.longitude], {
          icon: createMarkerIcon('/assets/blue-pin-drop.svg'),
        });

        // Initialize the marker state
        markerStates.set(marker, false);

        // Generate polygons for this marker
        const polygons: L.Polygon[] = [];
        colors.forEach((color, colorIndex) => {
          const radius = 0.05 + colorIndex * 0.03; // Increment radius for each layer
          const variance = 0.01 + colorIndex * 0.005; // Increment variance for each layer

          const polygonLayer = L.polygon(
            generatePolygonPoints(coord, radius, variance) as any,
            {
              color,
              weight: 2,
              fillOpacity: 0.1,
            }
          );

          polygons.push(polygonLayer);
          map?.$viewer?.map.addLayer(polygonLayer);
        });

        this.markerPolygons.set(marker, polygons);

        marker.on('click', () => {
          const isActive = markerStates.get(marker);

          if (isActive) {
            // If active, reset marker and polygons
            marker.setIcon(createMarkerIcon('/assets/blue-pin-drop.svg'));
            markerStates.set(marker, false);

            const associatedPolygons = this.markerPolygons.get(marker);
            associatedPolygons?.forEach((polygon) => polygon.setStyle({ weight: 2 }));
          } else {
            // If not active, activate marker and bold polygons
            marker.setIcon(createActiveMarkerIcon('/assets/active-pin-drop.svg'));
            markerStates.set(marker, true);

            const associatedPolygons = this.markerPolygons.get(marker);
            associatedPolygons?.forEach((polygon) => polygon.setStyle({ weight: 5 }));
          }
        });

        markersCluster.addLayer(marker);
      });

      // Listen for clicks on the map to reset all markers and polygons
      map?.$viewer?.map.on('click', () => {
        markerStates.forEach((isActive, marker) => {
          if (isActive) {
            marker.setIcon(createMarkerIcon('/assets/blue-pin-drop.svg'));
            markerStates.set(marker, false);

            const associatedPolygons = this.markerPolygons.get(marker);
            associatedPolygons?.forEach((polygon) => polygon.setStyle({ weight: 2 }));
          }
        });
      });

      // Add the cluster group to the map
      map?.$viewer?.map.addLayer(markersCluster);
    }
  }

  getSecureRandomNumber() {
    const array = new Uint32Array(1);
    window.crypto.getRandomValues(array);
    return array[0] / (0xFFFFFFFF + 1); // Normalize to [0, 1)
  }

  // Temporary function, to be replaced with SMK layer config when /features endpoint exists in the API
  highlightProjectPolygons(project: any) {
    const coord = { latitude: project.latitude, longitude: project.longitude };

    // Convert markerPolygons keys (MapIterator) into an array and find the marker
    const marker = [...this.markerPolygons.keys()].find(
      (m) =>
        Math.abs(m.getLatLng().lat - coord.latitude) < 0.0001 && // Use tolerance for comparison
        Math.abs(m.getLatLng().lng - coord.longitude) < 0.0001
    );

    if (marker) {
      // Reset the previously active marker and polygons if any
      if (this.activeMarker) {
        this.activeMarker.setIcon(getBluePinIcon()
        );

        const associatedPolygons = this.markerPolygons.get(this.activeMarker);
        associatedPolygons?.forEach((polygon) => polygon.setStyle({ weight: 2 }));
      }

      // Highlight the new marker and polygons
      marker.setIcon(
        L.icon({
          iconUrl: '/assets/active-pin-drop.svg', // Highlighted icon
          iconSize: [50, 70],
          iconAnchor: [20, 51],
          popupAnchor: [1, -34],
        })
      );

      const associatedPolygons = this.markerPolygons.get(marker);
      associatedPolygons?.forEach((polygon) => polygon.setStyle({ weight: 5 }));

      // Update the active marker
      this.activeMarker = marker;
    }
  }

  // Helper to strip a suffix from a string (e.g., ' Forest Region' or ' Forest District')
  stripSuffix(value: string, suffix: string): string {
    if (!value) return '';
    return value.endsWith(suffix) ? value.slice(0, -suffix.length).trim() : value;
  }

  getProjectFiscalYearRange(project: any): string | null {
    if (!project?.projectFiscals?.length) return null;

    const uniqueYears = Array.from(
      new Set(project.projectFiscals.map((f: any) => f.fiscalYear))
    ).filter((y): y is number => typeof y === 'number').sort((a, b) => a - b);

    if (uniqueYears.length === 0) return null;

    const formatYear = (year: number): string => {
      const next = (year + 1) % 100;
      return `${year}/${next.toString().padStart(2, '0')}`;
    };

    if (uniqueYears.length === 1) {
      return formatYear(uniqueYears[0]);
    } else {
      return `${formatYear(uniqueYears[0])} - ${formatYear(uniqueYears[uniqueYears.length - 1])}`;
    }
  }

  processProjectsResponse(data: any): void {
    this.allProjects = (data.projects ?? []).sort((a: any, b: any) =>
      a.projectName.localeCompare(b.projectName)
    );
    this.displayedProjects = this.allProjects;
    this.sharedService.updateDisplayedProjects(this.displayedProjects);
    this.isLoading = false;
  }

  handleProjectError(err: any): void {
    console.error('Error fetching features:', err);
    this.allProjects = [];
    this.displayedProjects = [];
    this.isLoading = false;
  }

  getSortedProjectFiscalsDesc(project: any): any[] {
    if (!project?.projectFiscals?.length) return [];
    return [...project.projectFiscals].sort((a, b) => b.fiscalYear - a.fiscalYear);
  }

  onHeaderClick(event: MouseEvent, project: any): void {
    const clickedInsideExpansionIcon = (event.target as HTMLElement).closest('.custom-indicator');
    if (!clickedInsideExpansionIcon) {
      event.stopPropagation();
      this.onListItemClick(project);
    }
  }

  onListItemClick(project: any): void {
    if (this.selectedProjectGuid === project.projectGuid) {
      //deselect
      this.selectedProjectGuid = null;
      this.sharedService.selectProject();
      this.sharedService.triggerMapCommand('close', project);
    } else {
      // select
      this.selectedProjectGuid = project.projectGuid;
      this.sharedService.selectProject(project);
    }
  }

  getStatusIcon(status: string) {
    return PlanFiscalStatusIcons[status];
  }

  onDownload(type: string): void {
    const projectGuids = this.displayedProjects.map(p => p.projectGuid);
    this.downloadProjects(projectGuids, type);
  }

  downloadProjects(projectGuids: string[], type: string): void {
    const snackRef = this.snackbarService.open(Messages.fileDownloadInProgress, 'Close', {
      duration: undefined,
      panelClass: 'snackbar-info'
    });

    this.projectService.downloadProjects(projectGuids, type).subscribe({
      next: (blob) => {
        snackRef.dismiss();
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        const extension = type === DownloadTypes.EXCEL
          ? DownloadFileExtensions.EXCEL
          : DownloadFileExtensions.CSV;
        a.download = `projects.${extension}`;
        a.href = url;
        a.click();
        window.URL.revokeObjectURL(url);

        this.snackbarService.open(Messages.fileDownloadSuccess, 'Close', {
          duration: 5000,
          panelClass: 'snackbar-success'
        });
      },
      error: (err) => {
        snackRef.dismiss();
        console.error('Download failed', err);
        this.snackbarService.open(Messages.fileDownloadFailure, 'Close', {
          duration: 5000,
          panelClass: 'snackbar-error'
        });
      }
    });
  }

}
