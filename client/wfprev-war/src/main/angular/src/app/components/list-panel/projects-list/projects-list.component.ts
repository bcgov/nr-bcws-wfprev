import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
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

@Component({
  selector: 'app-projects-list',
  standalone: true,
  imports: [MatSlideToggleModule, CommonModule, MatExpansionModule], // Add FormsModule here
  templateUrl: './projects-list.component.html',
  styleUrls: ['./projects-list.component.scss'], // Corrected to 'styleUrls'
})
export class ProjectsListComponent implements OnInit {
  [key: string]: any;
  projectList: any[] = [];
  programAreaCode: any[] = [];
  forestRegionCode: any[] = [];
  markerPolygons: Map<L.Marker, L.Polygon[]> = new Map();
  activeMarker: L.Marker | null = null;
  getActiveMap = getActiveMap
  constructor(
    private readonly router: Router,
    private readonly projectService: ProjectService,
    private readonly codeTableService: CodeTableServices,
    private readonly dialog: MatDialog

  ) {
  }
  ngOnInit(): void {
    this.loadCodeTables();
    this.loadProjects();
    setTimeout(() => { this.loadCoordinatesOnMap(); }, 3000);
    console.log('Initialized projectList:', this.projectList);
  }

  loadCodeTables(): void {
    const codeTables = [
      { name: 'programAreaCodes', property: 'businessAreas', embeddedKey: 'programArea' },
      { name: 'forestRegionCodes', property: 'forestRegions', embeddedKey: 'forestRegionCode' },
    ];

    codeTables.forEach((table) => {
      this.codeTableService.fetchCodeTable(table.name).subscribe({
        next: (data) => {
          if (table.name === 'programAreaCodes') {
            this.programAreaCode = data._embedded.programArea;
          } else if (table.name === 'forestRegionCodes') {
            this.forestRegionCode = data._embedded.forestRegionCode;
          }
        },
        error: (err) => {
          console.error(`Error fetching ${table.name}`, err);

          // Explicitly set the property to an empty array on error
          if (table.name === 'programAreaCodes') {
            this.programAreaCode = [];
          } else if (table.name === 'forestRegionCodes') {
            this.forestRegionCode = [];
          }
        },
      });
    });
  }


  loadProjects() {
    this.projectService.fetchProjects().subscribe({
      next: (data) => {
        this.projectList = data._embedded?.project || [];
      },
      error: (err) => {
        console.error('Error fetching projects:', err);
        this.projectList = [];
      }
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


  onSortChange(event: any): void {
    this.selectedSort = event.target.value;
  }

  editProject(project: any, event: Event) {
    event.stopPropagation();
    this.router.navigate([ResourcesRoutes.EDIT_PROJECT], {
      queryParams: { projectNumber: project.projectNumber, name: project.projectName }
    });
  }

  getDescription(codeTable: string, code: number | string): string {
    const table = this[codeTable];
    if (!table) return 'Unknown'; // Return 'Unknown' if the table is not loaded

    let entry;

    if (codeTable === 'programAreaCode') {
      // Search by programAreaGuid if the codeTable is programAreaCode
      entry = table.find((item: any) => item.programAreaGuid === code);
      return entry ? entry.programAreaName : 'Unknown'
    } else {
      // Default to searching by orgUnitId
      entry = table.find((item: any) => item.orgUnitId === code);
    }

    return entry ? entry.orgUnitName : 'Unknown';
  }

  createNewProject(): void {
    const dialogRef = this.dialog.open(CreateNewProjectDialogComponent, {
      width: '880px',
      disableClose: true,
      hasBackdrop: true,
    });
    // Subscribe to the afterClosed method to handle the result
    dialogRef.afterClosed().subscribe((result) => {
      if (result && result.success === true) {
        this.loadProjects();
      }
    });
  }

  loadCoordinatesOnMap() {
    if (this.projectList?.length > 0) {
      const coords = this.projectList
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

      // Helper function to generate polygon points
      const generatePolygonPoints = (
        center: { latitude: number; longitude: number },
        radius: number = 50, // Average radius of 15 km
        variance: number = 5  // Variance of ±5 km
      ) => {
        const points = [];
        for (let i = 0; i < 50; i++) {
          const angle = (i / 50) * Math.PI * 2; // Angle in radians
          const offset = radius + Math.random() * variance - variance / 2; // Randomize radius
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

}
