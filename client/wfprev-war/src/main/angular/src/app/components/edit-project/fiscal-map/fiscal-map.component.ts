import { AfterViewInit, Component, Input, OnChanges, OnDestroy, OnInit, SimpleChanges } from '@angular/core';
import { ActivatedRoute, Router, UrlTree } from '@angular/router';
import * as L from 'leaflet';
import { forkJoin, map } from 'rxjs';
import { ProjectService } from 'src/app/services/project-services';
import { ResourcesRoutes } from 'src/app/utils';
import { LeafletLegendService, createFullPageControl } from 'src/app/utils/tools';

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
    protected router: Router,
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

    const legendHelper = new LeafletLegendService();
    legendHelper.addLegend(this.map!, this.fiscalColorMap);
    createFullPageControl(() => this.openFullMap()).addTo(this.map!);
  }

  openFullMap(): void {
    if (!this.allActivityBoundaries || this.allActivityBoundaries.length === 0) {
      return;
    }
  
    const latLngs: L.LatLng[] = [];
  
    this.allActivityBoundaries.forEach(entry => {
      entry.boundary?.forEach((item: any) => {
        const geometry = item.geometry;
        const layer = L.geoJSON(geometry);
        const layerBounds = layer.getBounds();
  
        latLngs.push(layerBounds.getSouthWest());
        latLngs.push(layerBounds.getNorthEast());
      });
    });
  
    if (latLngs.length === 0) return;
  
    const bounds = L.latLngBounds(latLngs);
    const bbox = [
      bounds.getWest().toFixed(6),
      bounds.getSouth().toFixed(6),
      bounds.getEast().toFixed(6),
      bounds.getNorth().toFixed(6),
    ].join(',');
    
    const urlTree: UrlTree = this.router.createUrlTree([ResourcesRoutes.MAP], {
      queryParams: { bbox }
    });
  
    const fullUrl = window.location.origin + this.router.serializeUrl(urlTree);
    window.open(fullUrl, '_blank');
    
  }
}
