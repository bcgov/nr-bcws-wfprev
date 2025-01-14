import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatExpansionModule } from '@angular/material/expansion';
import { Router } from '@angular/router';
import { ResourcesRoutes } from 'src/app/utils';
import { ProjectService } from 'src/app/services/project-services';
import { CodeTableServices } from 'src/app/services/code-table-services';
import { CreateNewProjectDialogComponent } from 'src/app/components/create-new-project-dialog/create-new-project-dialog.component';
import { MatDialog } from '@angular/material/dialog';
import { SmkApi } from 'src/app/utils/smk';
import L from 'leaflet';
import 'leaflet.markercluster';

type LonLat = [ number, number ]
type Smk = any
type SmkPromise = Promise< Smk >

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
  smkApi: any;
  private smkInstancePromise: SmkPromise = Promise.resolve({ /* Default Smk object */ } as Smk);
  constructor(
    private readonly router: Router,
    private readonly projectService: ProjectService,
    private readonly codeTableService: CodeTableServices,
    private readonly dialog: MatDialog

  ) {
  }
  ngOnInit(): void {
    if(!this.smkApi) {
      this.smkApi = new SmkApi(Event);
    }
    this.loadCodeTables();
    this.loadProjects();
    setTimeout(() => { this.loadCoordinatesOnMap(); }, 3000)
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
        this.projectList = data._embedded?.project;
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
     if(this.projectList?.length > 0){
      const coords = this.projectList.map((project: any) => ({
        latitude: project.latitude,
        longitude: project.longitude,
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
    // const coords = [
    //   { latitude: 49.282730, longitude: -123.120735 },
    //   { latitude: 50.676610, longitude: -120.339023 },
    //   { latitude: 53.912579, longitude: -122.747772 },
    //   { latitude: 54.018349, longitude: -123.995628 },
    //   { latitude: 48.428318, longitude: -123.364953 },
    //   { latitude: 49.887918, longitude: -119.495902 },
    // ];
  
    // Add markers to the cluster group
    coords.forEach(coord => {
      const marker = L.marker([coord.latitude, coord.longitude], {
        icon: L.icon({
          iconUrl: '/assets/blue-pin-drop.svg',
          iconSize: [30, 50],
          iconAnchor: [12, 41],
          popupAnchor: [1, -34],
        }),
      });
  
      markersCluster.addLayer(marker);
    });
  
    // Add the cluster group to the map
    map?.$viewer?.map.addLayer(markersCluster);
  }
}

  leaflet() { return window[ 'L' ] }
  toPoint( lonLat: LonLat ): any {
    return (window as any)[ 'turf' ].point( lonLat )
}
 

  putHighlight( lonLat: LonLat ): SmkPromise {
    const SMK = (window as any)['SMK'];
    console.log("highlighting")
    const L = this.leaflet();
    return this.clearHighlight().then( ( smk ) => {
      SMK.MAP.map.showFeature.showFeature( 'openstreetmaps', this.toPoint( lonLat ), {
            pointToLayer: function ( geojson: any, latlng: any) {
                return L.marker( latlng, {
                    icon: L.icon( {
                        iconUrl: 'assets/data/wfprev-example.png',
                        iconAnchor: [16, 16]
                    } )
                } )
            }
        } )
        return smk
    } )
}

clearHighlight(): SmkPromise {
  return this.smkInstancePromise.then( ( smk ) => {
      smk.showFeature( 'openstreetmaps' )
      return smk
  } )
}

getActiveMap(smk: any | null = null) {
  const SMK = smk || (window as any)['SMK'];
  const key1 = Object.keys(SMK.MAP);
  const key = Object.keys(SMK.MAP)[Object.keys(SMK.MAP).length - 1];
  if (key) {
    const map = SMK.MAP[key];
    map.$viewer.map._layersMaxZoom = 20;
    return map;
  }
  // Sort of a fail-safe if the object doesn't have a key to force-retry with the window SMK object
  else {
    const smkMap = SMK.MAP;
    const lastKey = Object.keys(smkMap).pop(); // Safely gets the last key
    if (lastKey) {
        return smkMap[lastKey]; // Use the key to access the value
    }
}
}



}
