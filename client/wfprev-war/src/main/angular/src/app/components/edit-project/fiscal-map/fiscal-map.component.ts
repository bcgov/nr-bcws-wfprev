import { AfterViewInit, Component, HostListener, Input, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute, Router, UrlTree } from '@angular/router';
import * as L from 'leaflet';
import { firstValueFrom, forkJoin, map } from 'rxjs';
import { FileAttachment } from 'src/app/components/models';
import { MiniMap, MiniMapService, MiniMapView, boundsOf } from 'src/app/services/mini-map.service';
import { ProjectService } from 'src/app/services/project-services';
import { ResourcesRoutes } from 'src/app/utils';
import { LeafletLegendService, createFullPageControl, getBluePinIcon } from 'src/app/utils/tools';

@Component({
    selector: 'wfprev-fiscal-map',
    standalone: true,
    imports: [],
    templateUrl: './fiscal-map.component.html',
    styleUrl: './fiscal-map.component.scss'
})
export class FiscalMapComponent implements AfterViewInit, OnDestroy, OnInit {
    @Input() fiscalGuid: any = '';
    @Input() selectedFiscalYear: number = new Date().getFullYear();
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
    private readonly miniMapService: MiniMapService,
  ) {}

  map: L.Map | undefined;
  // SMK's copy of Leaflet once the map exists; everything drawn on the map is built with it (see MiniMap.leaflet)
  private leaflet: typeof L = L;
  private miniMap: MiniMap | undefined;
  private destroyed = false;
  // Settles once what the map shows has loaded; the map opens on it
  private mapData: Promise<void> = Promise.resolve();

  @HostListener('window:resize', ['$event'])
  onResize(event: any) {
    this.refreshMap();
  }

  refreshMap() {
    if (this.map) {
      this.map.invalidateSize();
    }
  }
  // Created with the map
  private activityBoundaryGroup: L.LayerGroup | undefined;
  private projectBoundaryGroup: L.LayerGroup | undefined;

  projectGuid = '';
  projectFiscals: any[] = [];
  allActivities: any[] = [];
  allActivityBoundaries: any[] = [];
  projectBoundary: any[] = [];
  projectLatitude = '';
  projectLongitude = '';

  ngOnInit(): void{
    this.mapData = this.loadMapData();
  }

  ngAfterViewInit(): void {
    setTimeout(() => {
      this.initMap();
    });
  }

  ngOnDestroy(): void {
    this.destroyed = true;
    this.miniMapService.destroy(this.miniMap);
  }

  /**
   * Loads what the map shows: the project boundary and the activity boundaries, and the project location when there
   * are neither. Never rejects; whatever fails to load is left out.
   */
  async loadMapData(): Promise<void> {
    this.projectGuid = this.route.snapshot?.queryParamMap?.get('projectGuid') ?? '';
    if (!this.projectGuid) return;

    await Promise.all([this.getProjectBoundary(), this.getAllActivitiesBoundaries()]);
    if (this.projectBoundary.length === 0 && this.allActivityBoundaries.length === 0) {
      await this.getProjectCoordinates();
    }
  }

  async getProjectCoordinates(): Promise<void> {
    try {
      const project = await firstValueFrom(this.projectService.getProjectByProjectGuid(this.projectGuid));
      if (project?.latitude && project?.longitude) {
        this.projectLatitude = project.latitude;
        this.projectLongitude = project.longitude;
      }
    } catch (error) {
      console.error('Error loading the project location:', error);
    }
  }

  private plotProjectLocation(): void {
    const lat = Number.parseFloat(this.projectLatitude);
    const lng = Number.parseFloat(this.projectLongitude);

    const teardropIcon = getBluePinIcon()
    this.createMarker([lat, lng], { icon: teardropIcon }).addTo(this.map!);
  }

  async getProjectBoundary(): Promise<void> {
    try {
      const data = await firstValueFrom(this.projectService.getProjectBoundaries(this.projectGuid));
      const boundaries = data?._embedded?.projectBoundary ?? [];
      if (boundaries.length > 0) {
        // Sort boundaries by systemStartTimestamp descending and pick the latest
        const latestBoundary = boundaries.sort((a: { systemStartTimestamp: string | number | Date; }, b: { systemStartTimestamp: string | number | Date; }) =>
          new Date(b.systemStartTimestamp).getTime() - new Date(a.systemStartTimestamp).getTime()
        )[0];

        this.projectBoundary = [latestBoundary];
      }
    } catch (error) {
      console.error('Error loading the project boundary:', error);
    }
  }

  async getAllActivitiesBoundaries(): Promise<void> {
    try {
      const fiscals = await firstValueFrom(this.projectService.getProjectFiscalsByProjectGuid(this.projectGuid));
      const activities = await this.handleFiscalsResponse(fiscals);
      const boundaries = await this.handleActivitiesResponse(activities);
      this.handleBoundariesResponse(boundaries);
    } catch (error) {
      console.error('Error loading the activity boundaries:', error);
    }
  }

  // Every activity of every fiscal
  private handleFiscalsResponse(data: any): Promise<any[]> {
    this.projectFiscals = (data?._embedded?.projectFiscals ?? []).sort(
      (a: { fiscalYear: number }, b: { fiscalYear: number }) => a.fiscalYear - b.fiscalYear
    );

    const activityRequests = this.projectFiscals.map(fiscal =>
      this.projectService.getFiscalActivities(this.projectGuid, fiscal.projectPlanFiscalGuid).pipe(
        map(response => this.mapFiscalActivities(response, fiscal))
      )
    );

    // forkJoin of no requests completes without a value
    return firstValueFrom(forkJoin(activityRequests).pipe(map(arrays => arrays.flat())), { defaultValue: [] });
  }

  private mapFiscalActivities(response: any, fiscal: any): any[] {
    const activities = response?._embedded?.activities ?? [];
    return activities.map((activity: any) => ({
      ...activity,
      fiscalYear: fiscal.fiscalYear,
      projectPlanFiscalGuid: fiscal.projectPlanFiscalGuid
    }));
  }

  // The boundaries of each activity
  private handleActivitiesResponse(allActivities: any[]): Promise<any[]> {
    const boundaryRequests = allActivities.map(activity =>
      this.projectService
        .getActivityBoundaries(this.projectGuid, activity.projectPlanFiscalGuid, activity.activityGuid)
        .pipe(
          map(boundary => this.mapActivityBoundary(boundary, activity))
        )
    );

    return firstValueFrom(forkJoin(boundaryRequests), { defaultValue: [] });
  }

  private mapActivityBoundary(boundary: any, activity: any): any {
    return boundary ? {
      activityGuid: activity.activityGuid,
      fiscalYear: activity.fiscalYear,
      boundary: boundary?._embedded?.activityBoundary
    } : null;
  }

  private handleBoundariesResponse(results: any[]): void {
    // Filter out nulls or empty boundary arrays
    const validResults = results.filter(r => r?.boundary && r.boundary.length > 0);
    // For each activity, keep only the latest boundary
    const dedupedResults: any[] = [];
    const seenActivityGuids = new Set<string>();

    for (const result of validResults) {
      const { activityGuid, fiscalYear, boundary } = result;

      if (seenActivityGuids.has(activityGuid)) continue;
      seenActivityGuids.add(activityGuid);
      // Get the latest boundary for this activity based on systemStartTimestamp
      const latestBoundary = boundary.reduce((latest: FileAttachment, current: FileAttachment) => {
        return new Date(current.systemStartTimestamp ?? 0) > new Date(latest.systemStartTimestamp ?? 0)
          ? current
          : latest;
      });

      dedupedResults.push({
        activityGuid,
        fiscalYear,
        boundary: [latestBoundary], // preserve original structure
      });
    }

    this.allActivityBoundaries = dedupedResults;
  }

  plotActivityBoundariesOnMap(boundaries: any[]): void {
    for (const boundaryEntry of boundaries) {
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
            weight: fiscalYear === this.selectedFiscalYear ? 4 : 2,
            fillOpacity: 0.1
          }
        };

        const addToMap = (geom: any) => {
          this.createGeoJSON(geom, geoJsonOptions).addTo(this.activityBoundaryGroup!);
        };

        if (geometry.type === 'GeometryCollection') {
          for (const subGeom of geometry.geometries) {
            addToMap(subGeom);
          }
        } else {
          addToMap(geometry);
        }
      }
    };
  }

  plotProjectBoundary(boundary: any[]): void {
    this.projectBoundaryGroup?.clearLayers();

    for (const item of boundary) {
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
        this.createGeoJSON(geom, geoJsonOptions).addTo(this.projectBoundaryGroup!);
      };

      if (geometry.type === 'GeometryCollection') {
        for (const subGeom of geometry.geometries) {
          addToMap(subGeom);
        }
      } else {
        addToMap(geometry);
      }
    };
  }

  // Every activity and project boundary, or the project location when there are none. Otherwise the map stays on
  // SMK's view of BC.
  initialView(): MiniMapView | undefined {
    const bounds = this.boundaryBounds();
    if (bounds) {
      return { bounds, padding: 20 };
    }
    if (this.projectLatitude && this.projectLongitude) {
      return { center: [Number.parseFloat(this.projectLatitude), Number.parseFloat(this.projectLongitude)], zoom: 14 };
    }
    return undefined;
  }

  private boundaryBounds(): L.LatLngBoundsLiteral | undefined {
    return boundsOf([
      ...this.allActivityBoundaries.flatMap(entry => (entry.boundary ?? []).map((item: any) => item.geometry)),
      ...this.projectBoundary.map(item => item.boundaryGeometry),
    ]);
  }

  async initMap(): Promise<void> {
    const mapContainer = document.getElementById('fiscalMap');
    if (!mapContainer) return;

    let miniMap: MiniMap;
    try {
      // The map is created while the boundaries load, and opens on them
      miniMap = await this.createMap(mapContainer, this.mapData.then(() => this.initialView()));
    } catch (error) {
      console.error('Error loading map:', error);
      return;
    }

    // The page was left while the map was being created
    if (this.destroyed) {
      this.miniMapService.destroy(miniMap);
      return;
    }
    this.miniMap = miniMap;
    this.map = miniMap.map;
    this.leaflet = miniMap.leaflet;

    this.activityBoundaryGroup = this.createLayerGroup().addTo(this.map);
    this.projectBoundaryGroup = this.createLayerGroup().addTo(this.map);

    const legendHelper = new LeafletLegendService();
    legendHelper.addLegend(this.map, this.fiscalColorMap);
    createFullPageControl(() => this.openFullMap()).addTo(this.map);

    if (this.projectBoundary.length > 0) {
      this.plotProjectBoundary(this.projectBoundary);
    }
    if (this.allActivityBoundaries.length > 0) {
      this.plotActivityBoundariesOnMap(this.allActivityBoundaries);
    }
    if (this.projectLatitude && this.projectLongitude) {
      this.plotProjectLocation();
    }
  }

  openFullMap(): void {
    // handle this based on following steps and scenarios.
    // 1-2. Activity and project boundaries
    const boundaryBounds = this.boundaryBounds();

    let urlTree: UrlTree;
  
    // 3. If there are any polygons, use combined bounds
    if (boundaryBounds) {
      const bounds = L.latLngBounds(boundaryBounds);
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
      const lat = Number.parseFloat(this.projectLatitude);
      const lng = Number.parseFloat(this.projectLongitude);
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

  createMap(container: HTMLElement, view?: Promise<MiniMapView | undefined>): Promise<MiniMap> {
    return this.miniMapService.create(container, view);
  }

  createGeoJSON(geom: any, options?: any): L.GeoJSON {
    return this.leaflet.geoJSON(geom, options);
  }

  createMarker(latlng: L.LatLngExpression, options?: L.MarkerOptions): L.Marker {
    return this.leaflet.marker(latlng, options);
  }

  createLayerGroup(): L.LayerGroup {
    return this.leaflet.layerGroup();
  }
  
  
}
