import { AfterViewInit, Component, Input, OnDestroy, OnInit } from '@angular/core';
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
    readonly projectService: ProjectService,
    readonly route: ActivatedRoute,
    protected router: Router,
  ) {}

  private map: L.Map | undefined;
  projectGuid = '';
  projectFiscals: any[] = [];
  allActivities: any[] = [];
  allActivityBoundaries: any[] = [];
  projectBoundary: any[] = [];
  projectLatitude = '';
  projectLongitude = '';
  ngOnInit(): void{
    this.getProjectBoundary();
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
      if (project.latitude && project.longitude && this.map) {
        this.projectLatitude = project.latitude;
        this.projectLongitude = project.longitude;
        if (this.map) {
          const lat = parseFloat(this.projectLatitude);
          const lng = parseFloat(this.projectLongitude);
  
          L.marker([lat, lng]).addTo(this.map);
  
          this.map.setView([lat, lng], 14); 
        }
      }
    })
  }
  getProjectBoundary() {
    this.projectGuid = this.route.snapshot?.queryParamMap?.get('projectGuid') ?? '';
    if (this.projectGuid) {
      this.projectService.getProjectBoundaries(this.projectGuid).subscribe((data) => {
        const boundary = data?._embedded?.projectBoundary ?? [];
        this.projectBoundary = boundary;
  
        if (this.map && boundary.length > 0) {
          this.plotProjectBoundary(boundary);
        }
      });
    }
  }
  
  getAllActivitiesBoundaries(): void {
    this.projectGuid = this.route.snapshot?.queryParamMap?.get('projectGuid') ?? '';
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
    if (allActivities.length === 0) {
      this.getProjectCoordinates();
      return;
    }
  
    const boundaryRequests = allActivities.map(activity =>
      this.projectService
        .getActivityBoundaries(this.projectGuid, activity.projectPlanFiscalGuid, activity.activityGuid)
        .pipe(
          map(boundary => this.mapActivityBoundary(boundary, activity))
        )
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
    this.allActivityBoundaries = results.filter(r => r?.boundary && Object.keys(r.boundary).length > 0);
  
    const hasActivityPolygons = this.allActivityBoundaries.length > 0;
    const hasProjectPolygons = this.projectBoundary?.length > 0;
  
    if (hasActivityPolygons && this.map) {
      this.plotBoundariesOnMap(this.allActivityBoundaries);
    }
  
    if (!hasActivityPolygons && !hasProjectPolygons) {
      this.getProjectCoordinates();
    }
  }
  
  plotBoundariesOnMap(boundaries: any[]): void {
    const allFiscalPolygons: L.Layer[] = [];
  
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
  
      for (const item of boundaryEntry.boundary) {
        const geometry = item.geometry;
        if (!geometry) continue;
  
        const geoJsonOptions: L.GeoJSONOptions = {
          style: {
            color,
            weight: fiscalYear === this.currentFiscalYear ? 4 : 2,
            fillOpacity: 0.1
          }
        };
  
        const addToMap = (geom: any) => {
          const layer = L.geoJSON(geom, geoJsonOptions).addTo(this.map!);
          allFiscalPolygons.push(layer); //  Track all layers
        };
      
        if (geometry.type === 'GeometryCollection') {
          for (const subGeom of geometry.geometries) {
            addToMap(subGeom);
          }
        } else {
          addToMap(geometry);
        }
      }
    });
  
    //  Zoom to ALL fiscal year polygons
    if (allFiscalPolygons.length > 0) {
      const group = L.featureGroup(allFiscalPolygons);
      this.map!.fitBounds(group.getBounds(), { padding: [20, 20] });
    }
  }

  plotProjectBoundary(boundary: any[]): void {
    const layers: L.Layer[] = [];
  
    boundary.forEach((item: any) => {
      const geometry = item.boundaryGeometry;
      if (!geometry) return;
  
      const geoJsonOptions: L.GeoJSONOptions = {
        style: {
          color: '#3f3f3f',
          weight: 2,
          fillOpacity: 0.1
        }
      };
  
      const addToMap = (geom: any) => {
        const layer = L.geoJSON(geom, geoJsonOptions).addTo(this.map!);
        layers.push(layer);
      };
  
      if (geometry.type === 'GeometryCollection') {
        geometry.geometries.forEach((subGeom: any) => addToMap(subGeom));
      } else {
        addToMap(geometry);
      }
    });
  
    if (layers.length > 0 && this.map) {
      const group = L.featureGroup(layers);
      this.map.fitBounds(group.getBounds(), { padding: [20, 20] });
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

    (this.map.zoomControl).setPosition('topright');

    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: '© OpenStreetMap contributors'
    }).addTo(this.map);

    const legendHelper = new LeafletLegendService();
    legendHelper.addLegend(this.map, this.fiscalColorMap);
    const bcBounds = L.latLngBounds([48.3, -139.1], [60.0, -114.0]);
    this.map.fitBounds(bcBounds, { padding: [20, 20] });
    createFullPageControl(() => this.openFullMap()).addTo(this.map);
  }

  openFullMap(): void {
    const latLngs: L.LatLng[] = [];
    // handle this based on following steps and scenarios.
    // 1. Add activity boundaries
    this.allActivityBoundaries?.forEach(entry => {
      entry.boundary?.forEach((item: any) => {
        const geometry = item.geometry;
        const layer = L.geoJSON(geometry);
        const layerBounds = layer.getBounds();
        latLngs.push(layerBounds.getSouthWest());
        latLngs.push(layerBounds.getNorthEast());
      });
    });
  
    // 2. Add project boundaries
    this.projectBoundary?.forEach((item: any) => {
      const geometry = item.boundaryGeometry;
      const layer = L.geoJSON(geometry);
      const layerBounds = layer.getBounds();
      latLngs.push(layerBounds.getSouthWest());
      latLngs.push(layerBounds.getNorthEast());
    });
  
    let urlTree: UrlTree;
  
    // 3. If there are any polygons, use combined bounds
    if (latLngs.length > 0) {
      const bounds = L.latLngBounds(latLngs);
      const bbox = [
        bounds.getWest().toFixed(6),
        bounds.getSouth().toFixed(6),
        bounds.getEast().toFixed(6),
        bounds.getNorth().toFixed(6),
      ].join(',');
  
      urlTree = this.router.createUrlTree([ResourcesRoutes.MAP], {
        queryParams: { bbox }
      });
  
    } else if (this.projectLatitude && this.projectLongitude) {
      // 4. No polygons, but project has coordinates → zoom to small area around point
      const lat = parseFloat(this.projectLatitude);
      const lng = parseFloat(this.projectLongitude);
      const delta = 0.01;
  
      const bbox = [
        lng - delta,
        lat - delta,
        lng + delta,
        lat + delta,
      ].join(',');
  
      urlTree = this.router.createUrlTree([ResourcesRoutes.MAP], {
        queryParams: { bbox }
      });
  
    } else {
      // 5. No polygons AND no coordinates → just open map
      urlTree = this.router.createUrlTree([ResourcesRoutes.MAP]);
    }
  
    const fullUrl = window.location.origin + this.router.serializeUrl(urlTree);
    window.open(fullUrl, '_blank');
  }
  
  
}
