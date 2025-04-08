import { AfterViewInit, Component, Input, OnChanges, OnDestroy, OnInit, SimpleChanges } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import * as L from 'leaflet';
import { forkJoin, map } from 'rxjs';
import { ProjectService } from 'src/app/services/project-services';

@Component({
  selector: 'app-fiscal-map',
  standalone: true,
  imports: [],
  templateUrl: './fiscal-map.component.html',
  styleUrl: './fiscal-map.component.scss'
})
export class FiscalMapComponent implements AfterViewInit, OnDestroy, OnInit {
    @Input() fiscalGuid: any = '';
  
  currentFiscalYear = new Date().getMonth() >= 3
    ? new Date().getFullYear()
    : new Date().getFullYear() - 1;
  
  fiscalColorMap: Record<'past' | 'present' | 'future', string> = {
    past: '#7570B3',
    present: '#1B9E77',
    future: '#E7298A'
  };
  
  constructor(
    private projectService: ProjectService,
    private route: ActivatedRoute,
  ) {}

  private map: L.Map | undefined;
  projectGuid = '';
  projectFiscals: any[] = [];
  allActivities: any[] = [];
  allActivityBoundaries: any[] = [];
  projectLatitude = '';
  projectLongitude = '';
  ngOnInit(): void{
    this.getAllActivitiesBoundaries();
  }

  ngAfterViewInit(): void {
    setTimeout(() => {
      this.initMap();
    });
  }
  
  ngOnDestroy(): void {
    if (this.map) {
      this.map.remove();
    }
  }

  getProjectCoordinates() {
    this.projectService.getProjectByProjectGuid(this.projectGuid).subscribe(project => {
      if (project.latitude && project.longitude) {
        this.projectLatitude = project.latitude;
        this.projectLongitude = project.longitude;
        if (this.map) {
          const lat = parseFloat(this.projectLatitude);
          const lng = parseFloat(this.projectLongitude);
  
          const marker = L.marker([lat, lng]).addTo(this.map);
  
          this.map.setView([lat, lng], 14); 
        }
      }
    })
  }
  getAllActivitiesBoundaries() {
    this.projectGuid = this.route.snapshot?.queryParamMap?.get('projectGuid') || '';
  
    if (this.projectGuid) {
      this.projectService.getProjectFiscalsByProjectGuid(this.projectGuid).subscribe((data) => {
        this.projectFiscals = (data._embedded?.projectFiscals || []).sort(
          (a: { fiscalYear: number }, b: { fiscalYear: number }) => a.fiscalYear - b.fiscalYear
        );
  
        const activityRequests = this.projectFiscals.map(fiscal =>
          this.projectService.getFiscalActivities(this.projectGuid, fiscal.projectPlanFiscalGuid).pipe(
            map((response: any) => {
              const activities = response?._embedded?.activities || [];
              return activities.map((activity: any) => ({
                ...activity,
                fiscalYear: fiscal.fiscalYear,
                projectPlanFiscalGuid: fiscal.projectPlanFiscalGuid
              }));
            })
          )
        );
  
        forkJoin(activityRequests).subscribe((allActivityArrays) => {
          const allActivities = allActivityArrays.flat();
  
          if (allActivities.length === 0) {
            // no activites at all.
            this.getProjectCoordinates(); 
            return;
          }
          const boundaryRequests = allActivities.map(activity =>
            this.projectService
              .getActivityBoundaries(this.projectGuid, activity.projectPlanFiscalGuid, activity.activityGuid)
              .pipe(
                map(boundary => boundary ? ({
                  activityGuid: activity.activityGuid,
                  fiscalYear: activity.fiscalYear,
                  boundary: boundary?._embedded?.activityBoundary
                }) : null),
              )
          );
  
          forkJoin(boundaryRequests).subscribe((allResults) => {
            this.allActivityBoundaries = allResults.filter(r => 
              r !== null && r.boundary && Object.keys(r.boundary).length > 0
            );

            if (this.allActivityBoundaries.length > 0 && this.map) {
              this.plotBoundariesOnMap(this.allActivityBoundaries)
            } else{
              this.getProjectCoordinates();
            }
          });
        });
      });
    }
  }
  
  plotBoundariesOnMap(boundaries: any[]): void {
    const currentFiscalPolygons: L.Layer[] = [];
  
    boundaries.forEach(boundaryEntry => {
      const fiscalYear = boundaryEntry.fiscalYear;
      let color = '';
  
      if (fiscalYear < this.currentFiscalYear) {
        color = this.fiscalColorMap.past;
      } else if (fiscalYear === this.currentFiscalYear) {
        color = this.fiscalColorMap.present;
      } else {
        color = this.fiscalColorMap.future;
      }
  
      boundaryEntry.boundary.forEach((item: any) => {
        const geometry = item.geometry;
        if (!geometry) return;
  
        const geoJsonOptions: L.GeoJSONOptions = {
          style: {
            color,
            weight: 2,
            fillOpacity: 0.1
          }
        };
  
        const addToMap = (geom: any) => {
          const layer = L.geoJSON(geom, geoJsonOptions).addTo(this.map!);
          if (fiscalYear === this.currentFiscalYear) {
            currentFiscalPolygons.push(layer);
          }
        };
  
        if (geometry.type === 'GeometryCollection') {
          geometry.geometries.forEach((subGeom: any) => addToMap(subGeom));
        } else {
          addToMap(geometry);
        }
      });
    });
  
    // Zoom to current fiscal year polygons
    if (currentFiscalPolygons.length > 0) {
      const group = L.featureGroup(currentFiscalPolygons);
      this.map!.fitBounds(group.getBounds(), { padding: [20, 20] });
    }
  }
  

  initMap(): void {
    const mapContainer = document.getElementById('fiscalMap');
    if (mapContainer && (mapContainer as any)._leaflet_id != null) {
      (mapContainer as any)._leaflet_id = null;
    }

    this.map = L.map('fiscalMap', {
      center: [49.00005, -124.0001], // Center of the boundary
      zoom: 14,
      zoomControl: true,
    });

    (this.map.zoomControl as L.Control.Zoom).setPosition('topright');

    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: '© OpenStreetMap contributors'
    }).addTo(this.map);

    this.addLegend();
  }

  addLegend(): void {
    const legend = (L.control as any)({ position: 'bottomleft' });
  
    legend.onAdd = () => {
      const div = L.DomUtil.create('div', 'legend');
      div.innerHTML = `
        <div class="legend-title">Polygon Colour Legend</div>
        <div class="legend-item">
          <span class="legend-color" style="background-color: #3f3f3f;"></span>
          Gross Project Boundary
        </div>
        <div class="legend-item">
          <span class="legend-color" style="background-color: ${this.fiscalColorMap.past};"></span>
          Past Fiscal Activities
        </div>
        <div class="legend-item">
          <span class="legend-color" style="background-color: ${this.fiscalColorMap.present};"></span>
          Present Fiscal Activities
        </div>
        <div class="legend-item">
          <span class="legend-color" style="background-color: ${this.fiscalColorMap.future};"></span>
          Future Fiscal Activities
        </div>
      `;
      return div;
    };
  
    legend.addTo(this.map!);
  }
  
  
}
