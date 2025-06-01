import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatExpansionModule } from '@angular/material/expansion';
import { Router } from '@angular/router';
import { ResourcesRoutes, getActiveMap } from 'src/app/utils';
import { ProjectService } from 'src/app/services/project-services';
import { CodeTableServices } from 'src/app/services/code-table-services';
import { CreateNewProjectDialogComponent } from 'src/app/components/create-new-project-dialog/create-new-project-dialog.component';
import { MatDialog } from '@angular/material/dialog';
import L from 'leaflet';
import 'leaflet.markercluster';
import { SharedCodeTableService } from 'src/app/services/shared-code-table.service';
import { SharedService } from 'src/app/services/shared-service';
import { MatTooltipModule } from '@angular/material/tooltip';
import { getFiscalYearDisplay } from 'src/app/utils/tools';


@Component({
  selector: 'app-projects-list',
  standalone: true,
  imports: [MatSlideToggleModule, CommonModule, MatExpansionModule, MatTooltipModule],
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
  markerPolygons: Map<L.Marker, L.Polygon[]> = new Map();
  activeMarker: L.Marker | null = null;
  getActiveMap = getActiveMap
  allProjects: any[] = [];
  displayedProjects: any[] = [];
  pageSize = 25;
  currentPage = 0;
  isLoading = false;
  selectedProjectGuid: string | null = null;
  getFiscalYearDisplay = getFiscalYearDisplay;
  expandedPanels: Record<string, boolean> = {};
  constructor(
    private readonly router: Router,
    private readonly projectService: ProjectService,
    private readonly codeTableService: CodeTableServices,
    private readonly dialog: MatDialog,
    private readonly sharedCodeTableService: SharedCodeTableService,
    public readonly sharedService: SharedService,
    private readonly cdr: ChangeDetectorRef
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
      { name: 'programAreaCodes', property: 'businessAreas', embeddedKey: 'programArea' },
      { name: 'forestRegionCodes', property: 'forestRegions', embeddedKey: 'forestRegionCode' },
      { name: 'forestDistrictCodes', property: 'forestDistricts', embeddedKey: 'forestDistrictCode' },
      { name: 'bcParksRegionCodes', property: 'bcParksRegions', embeddedKey: 'bcParksRegionCode' },
      { name: 'bcParksSectionCodes', property: 'bcParksSections', embeddedKey: 'bcParksSectionCode' },
      { name: 'planFiscalStatusCodes', property: 'planFiscalStatusCode', embeddedKey: 'planFiscalStatusCode' },
      { name: 'activityCategoryCodes', property: 'activityCategoryCode', embeddedKey: 'activityCategoryCode' },
      { name: 'projectTypeCodes', property: 'projectTypeCode', embeddedKey: 'projectTypeCode' }

    ];
    const loaded: any = {};
    let loadedCount = 0;
    const totalTables = codeTables.length;
  

    codeTables.forEach((table) => {
      this.codeTableService.fetchCodeTable(table.name).subscribe({
        next: (data) => {
          if (table.name === 'programAreaCodes') {
            this.programAreaCode = data._embedded.programArea;
          } else if (table.name === 'forestRegionCodes') {
            this.forestRegionCode = data._embedded.forestRegionCode;
          } else if (table.name === 'forestDistrictCodes') {
            this.forestDistrictCode = data._embedded.forestDistrictCode;
          } else if (table.name === 'bcParksRegionCodes') {
            this.bcParksRegionCode = data._embedded.bcParksRegionCode;
          } else if (table.name === 'bcParksSectionCodes') {
            this.bcParksSectionCode = data._embedded.bcParksSectionCode;
          } else if (table.name === 'planFiscalStatusCodes') {
            this.planFiscalStatusCode = data._embedded.planFiscalStatusCode;
          } else if (table.name === 'activityCategoryCodes') {
            this.activityCategoryCode = data._embedded.activityCategoryCode;
          } else if (table.name === 'projectTypeCodes') {
            this.projectTypeCode = data._embedded.projectTypeCode;
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
          if (table.name === 'programAreaCodes') {
            this.programAreaCode = [];
          } else if (table.name === 'forestRegionCodes') {
            this.forestRegionCode = [];
          } else if (table.name === 'forestDistrictCodes') {
            this.forestDistrictCode = [];
          } else if (table.name === 'bcParksRegionCodes') {
            this.bcParksRegionCode = [];
          } else if (table.name === 'bcParksSectionCodes') {
            this.bcParksSectionCode = [];
          } else if (table.name === 'planFiscalStatusCodes') {
            this.planFiscalStatusCode = [];
          } else if (table.name === 'activityCategoryCodes') {
            this.activityCategoryCode = [];
          } else if (table.name === 'projectTypeCodes') {
            this.activityCategoryCode = [];
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
    this.selectedSort = event.target.value;
  
    if (this.selectedSort === 'ascending') {
      this.allProjects.sort((a, b) => a.projectName.localeCompare(b.projectName));
    } else if (this.selectedSort === 'descending') {
      this.allProjects.sort((a, b) => b.projectName.localeCompare(a.projectName));
    }
  
    const totalVisible = (this.currentPage + 1) * this.pageSize;
    this.displayedProjects = this.allProjects.slice(0, totalVisible);
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
      case 'programAreaCode':
        entry = table.find((item: any) => item.programAreaGuid === code);
        return entry ? entry.programAreaName : '';
      case 'forestRegionCode':
      case 'forestDistrictCode':
      case 'bcParksRegionCode':
      case 'bcParksSectionCode':
        entry = table.find((item: any) => item.orgUnitId === code);
        return entry ? entry.orgUnitName : '';
  
      case 'planFiscalStatusCode':
        entry = table.find((item: any) => item.planFiscalStatusCode === code);
        return entry ? entry.description : '';
  
      case 'activityCategoryCode':
        entry = table.find((item: any) => item.activityCategoryCode === code);
        return entry ? entry.description : '';

      case 'projectTypeCode':
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
        this.activeMarker.setIcon(
          L.icon({
            iconUrl: '/assets/blue-pin-drop.svg', // Reset to the default icon
            iconSize: [30, 50],
            iconAnchor: [12, 41],
            popupAnchor: [1, -34],
          })
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

  onScroll() {
    if (this.isLoading) return;
    const nextPage = this.currentPage + 1;
    const start = nextPage * this.pageSize;
    const end = start + this.pageSize;
    if (start < this.allProjects.length) {
      this.displayedProjects = this.displayedProjects.concat(this.allProjects.slice(start, end));
      this.currentPage = nextPage;
      this.sharedService.updateDisplayedProjects(this.displayedProjects);
    }
  }

  handleScroll(event: any) {
    const element = event.target;
    if (element.scrollHeight - element.scrollTop <= element.clientHeight + 10) {
      this.onScroll();
    }
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
    this.currentPage = 0;
    this.displayedProjects = this.allProjects.slice(0, this.pageSize);
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
    const clickedInsideChevron = (event.target as HTMLElement).closest('.custom-indicator');
    if (!clickedInsideChevron) {
      event.stopPropagation();
      this.onListItemClick(project);
    }
  }


  togglePanel(guid: string, event: MouseEvent): void {
    event.stopPropagation();
    this.expandedPanels[guid] = !this.expandedPanels[guid];
  }


  onListItemClick(project: any): void {
    if (this.selectedProjectGuid === project.projectGuid) {
          //deselect
      this.selectedProjectGuid = null;
      this.sharedService.selectProject(undefined);
      this.sharedService.triggerMapCommand('close', project);
    } else {
        // select
      this.selectedProjectGuid = project.projectGuid;
      this.sharedService.selectProject(project);
    }
  }
}
