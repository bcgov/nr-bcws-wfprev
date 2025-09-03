import { AfterViewInit, ChangeDetectorRef, Component, ElementRef, OnDestroy, ViewChild, EnvironmentInjector, createComponent } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { SearchFilterComponent } from 'src/app/components/search-filter/search-filter.component';
import { MapConfigService } from 'src/app/services/map-config.service';
import { MapService } from 'src/app/services/map.service';
import { LeafletLegendService, getBluePinIcon,  getActivePinIcon, getFiscalYearColor } from 'src/app/utils/tools';
import { SharedService } from 'src/app/services/shared-service';
import * as L from 'leaflet';
import { ProjectPopupComponent } from 'src/app/components/project-popup/project-popup.component';
import { Project } from 'src/app/components/models';
import { ResizablePanelComponent } from 'src/app/components/resizable-panel/resizable-panel.component';
import { BC_BOUNDS, MapColors } from 'src/app/utils/constants';
import { Geometry, GeometryCollection } from 'geojson';
@Component({
  selector: 'app-map',
  standalone: true,
  imports: [ResizablePanelComponent, SearchFilterComponent, ProjectPopupComponent],
  templateUrl: './map.component.html',
  styleUrl: './map.component.scss'
})
export class MapComponent implements AfterViewInit, OnDestroy  {
  @ViewChild('mapContainer', { static: false }) mapContainer!: ElementRef;
  mapConfig: any[] = [];
  mapIndex = 0;
  panelContent: string = `
    The goal of the BC Wildfire Service (BCWS) Prevention Program is to reduce the negative impacts of wildfire on public safety, property, the environment and the economy using the seven disciplines of the FireSmart program.
    <br>
    British Columbia is experiencing a serious and sustained increase in extreme wildfire behaviour and fire events particularly in the wildland-urban interface. More human activity and development is taking place in or near forests, creating greater consequences for the socioeconomic health and safety of citizens and visitors. At the same time, the impacts of climate change are increasing, fire size and severity are increasing, and fire seasons are becoming longer. Prevention is more than stopping new, human-caused fires. FireSmart is based on the premise of shared responsibility and on promoting the integration and collaboration of wildfire prevention and mitigation efforts. All partners and stakeholders have a role to play.
  `;

  fiscalColorMap: Record<'past' | 'present' | 'future', string> = {
    past: '#7570B3',
    present: '#1B9E77',
    future: '#E7298A'
  };

  MAP_COMMANDS = {
    OPEN: 'open',
    CLOSE: 'close'
  } as const;

  private isMapReady = false;
  private latestProjects: any[] = [];
  private hasClusterBeenAddedToMap = false;
  private markersClusterGroup: L.MarkerClusterGroup | null = null;
  private projectMarkerMap = new Map<string, L.Marker>();
  private activeMarker: L.Marker | null = null;
  private selectedProject: Project | undefined;
  private activityBoundaryGroup: L.LayerGroup = L.layerGroup();
  private projectBoundaryGroup: L.LayerGroup = L.layerGroup();
  legendControl: L.Control | null = null;
  currentFiscalYear = new Date().getFullYear();
  constructor(
    protected cdr: ChangeDetectorRef,
    private readonly mapService: MapService,
    private readonly mapConfigService: MapConfigService,
    private readonly route: ActivatedRoute,
    private readonly sharedService: SharedService,
    private readonly injector: EnvironmentInjector
  ) {}

  ngOnDestroy(): void {
    const smk = this.mapService.getSMKInstance();
    if (typeof smk?.destroy === 'function') {
      smk.destroy();
    }
    this.mapService.clearSMKInstance();
  }


    
ngAfterViewInit(): void {
  if (!this.mapContainer?.nativeElement) {
    console.error('Map container is not available.');
    return;
  }

  this.mapIndex = this.mapService.getMapIndex();
  this.mapService.setMapIndex(this.mapIndex + 1);

  this.sharedService.displayedProjects$.subscribe(projects => {
    this.latestProjects = projects;
    if (this.isMapReady) {
      this.updateMarkers(projects);
    }
  });

  this.sharedService.mapCommand$.subscribe(({ action, project }) => {
    if (action === this.MAP_COMMANDS.CLOSE) {
      this.closePopupForProject(project);
    } else if (action === this.MAP_COMMANDS.OPEN) {
      this.openPopupForProject(project);
    }
  });

  this.initMap().then(() => {
    const smk = this.mapService.getSMKInstance();
    const map = smk?.$viewer?.map;

      // Use SMK's own refresh mechanism
    const bustSmkLayerPromises = () => {
      const visibleIds = smk.$viewer.layerIds
        .filter((id: any) => smk.$viewer.isDisplayContextItemVisible(id));

      const cacheKeys = Object.keys(smk.$viewer.layerIdPromise || {});
      visibleIds.forEach((id: string) => {
        const k = cacheKeys.find(k => k.includes(id));
        if (k) smk.$viewer.layerIdPromise[k] = null;
      });
    };

    map.on('zoomstart', () => {
      bustSmkLayerPromises();
    });
    map.on('movestart', () => {
      bustSmkLayerPromises();
    });

    map.on('zoomend', async () => {
      await smk.$viewer.updateLayersVisible();
      this.togglePolygonLayers(map.getZoom());
    });
    map.on('moveend', async () => {
      await smk.$viewer.updateLayersVisible();
      this.togglePolygonLayers(map.getZoom());
    });


    const bcBounds: L.LatLngBoundsExpression = BC_BOUNDS;

    if (map && typeof (map).fitBounds === 'function') {
      (map as L.Map).fitBounds(bcBounds);
    } else {
      console.warn('Map fitBounds not available on map; skipping initial bounds.');
    }
    map.addLayer(this.activityBoundaryGroup);
    map.addLayer(this.projectBoundaryGroup);

    if (map) {
      const legendHelper = new LeafletLegendService();
      this.legendControl = legendHelper.addLegend(map, this.fiscalColorMap)

      this.markersClusterGroup = L.markerClusterGroup({
        showCoverageOnHover: false,
        iconCreateFunction: (cluster) => {
          const count = cluster.getChildCount();
          return L.divIcon({
            html: `<div class="cluster-icon"><span>${count}</span></div>`,
            className: 'custom-marker-cluster',
            iconSize: L.point(40, 40),
          });
        }
      });

      map.addLayer(this.markersClusterGroup);
      this.hasClusterBeenAddedToMap = true;
    }

    this.isMapReady = true;
    this.updateMarkers(this.latestProjects);
  });

    this.sharedService.selectedProject$.subscribe(project => {
      if (!project && this.selectedProject) {
        const previous = this.selectedProject;
        this.selectedProject = undefined;
        this.closePopupForProject(previous);
        return;
      }

      this.selectedProject = project;

      if (project) {
        this.openPopupForProject(project);
      }
    });
    
}

  private async initMap(): Promise<void> {
    try {
      const baseConfig = this.clone(this.mapConfig);
      const mapState = await this.mapConfigService.getMapConfig();
      baseConfig.push(mapState);
      this.mapConfig = this.buildMapConfig(baseConfig);

      await this.mapService.createSMK({
        id: this.mapIndex,
        containerSel: this.mapContainer.nativeElement,
        config: this.mapConfig,
      });
    } catch (error) {
      console.error('Error loading map:', error);
    }
  }

  private buildMapConfig(baseConfig: any): object[] {
    const deviceConfig = { viewer: { device: 'desktop' } };
    return [...baseConfig, deviceConfig, 'theme=wf', '?'];
  }

  clone(o: any) {
    return JSON.parse(JSON.stringify(o));
  }

  updateMarkers(projects: any[]) {
    const smk = this.mapService.getSMKInstance();
    const map = smk?.$viewer?.map;

    if (!map || !this.markersClusterGroup) {
      console.warn('[Map] Skipping updateMarkers — map or cluster group not ready');
      return;
    }

    try {
      this.markersClusterGroup.clearLayers();
      this.projectMarkerMap.clear();
      this.activeMarker = null;
      
    } catch (err) {
      console.error('[Map] Error clearing markers:', err);
    }

  this.safelyClearLayerGroup(this.projectBoundaryGroup, 'projectBoundaryGroup');
  this.safelyClearLayerGroup(this.activityBoundaryGroup, 'activityBoundaryGroup');

    projects
      .filter(p => p.latitude != null && p.longitude != null)
      .forEach(project => {
        try {
          const marker = L.marker([project.latitude, project.longitude], {
            icon: getBluePinIcon()
          });

          this.projectMarkerMap.set(project.projectGuid, marker);

          const popupDiv = document.createElement('div');
          const cmpRef = createComponent(ProjectPopupComponent, {
            environmentInjector: this.injector
          });

          cmpRef.instance.project = project;
          cmpRef.instance.map = map;
          cmpRef.hostView.detectChanges();
          popupDiv.appendChild(cmpRef.location.nativeElement);

          marker.bindPopup(popupDiv, {
            maxWidth: 486,
            minWidth: 0,
            autoPan: true,
          });

          marker.on('click', () => {
            this.sharedService.selectProject(project);
          });

          this.markersClusterGroup!.addLayer(marker);

          this.plotProjectBoundary(project);
          this.plotActivityBoundaries(project, this.currentFiscalYear);

        } catch (err) {
          console.error('Map Failed to add marker:', project, err);
        }
      });

      this.togglePolygonLayers(map.getZoom());
  }

  openPopupForProject(project: Project): void {
    if (this.legendControl) {
      this.legendControl.getContainer()?.classList.add('hidden'); 
    }
    if (
      project?.latitude == null ||
      project?.longitude == null ||
      !this.markersClusterGroup
    ) {
      return;
    }

    const smk = this.mapService.getSMKInstance();
    const map = smk?.$viewer?.map;
    if (!map) return;

    const targetMarker = this.markersClusterGroup
      .getLayers()
      .find((layer): layer is L.Marker => {
        return (
          layer instanceof L.Marker &&
          Math.abs(layer.getLatLng().lat - project.latitude!) < 0.0001 &&
          Math.abs(layer.getLatLng().lng - project.longitude!) < 0.0001
        );
      });

    if (targetMarker) {
      // Reset previous active marker icon
      if (this.activeMarker && this.activeMarker !== targetMarker) {
        this.activeMarker.setIcon(getBluePinIcon()
        );
      }

      // Set active icon for selected marker
      targetMarker.setIcon(getActivePinIcon());

      this.activeMarker = targetMarker;

      this.markersClusterGroup.zoomToShowLayer(targetMarker, () => {
        targetMarker.openPopup();

        targetMarker.off('popupclose'); 

        targetMarker.on('popupclose', () => {
          targetMarker.setIcon(getBluePinIcon());

          if (this.selectedProject?.projectGuid === project.projectGuid) {
            this.sharedService.selectProject();
          }
        });
        requestAnimationFrame(() => {
          map.invalidateSize();
        });
      });
    }
  }

  public closePopupForProject(project: Project): void {
    if (this.legendControl) {
      this.legendControl.getContainer()?.classList.remove('hidden');
    }
    const marker = this.projectMarkerMap.get(project.projectGuid);
    if (marker) {
      marker.closePopup();

      marker.setIcon(getBluePinIcon());
      // if it's the active marker, reset reference
      if (this.activeMarker === marker) {
        this.activeMarker = null;
      }
    }
  }
addGeoJsonToLayer(geometry: any, layerGroup: L.LayerGroup, options: L.GeoJSONOptions) {
  if (!geometry?.type || !geometry?.coordinates) return;

  try {
    const geometries: Geometry[] = geometry.type === 'GeometryCollection'
      ? (geometry as GeometryCollection).geometries
      : [geometry as Geometry];

    geometries.forEach((geom: Geometry) => this.addGeometryToLayerGroup(geom, layerGroup, options));
  } catch (err) {
    console.warn('[Map] Invalid GeoJSON geometry skipped:', geometry, err);
  }
}

addGeometryToLayerGroup(geom: any, layerGroup: L.LayerGroup, options: L.GeoJSONOptions): void {
  const layer = L.geoJSON(geom, options);
  if (layer && typeof layer.addTo === 'function') {
    layer.addTo(layerGroup);
  } else {
    console.warn('[Map] Failed to add layer. Possibly invalid geometry:', geom);
  }
}

plotProjectBoundary(project: any): void {
  project.projectBoundaries?.forEach((boundary: any) => {
    const geometry = boundary.boundaryGeometry;
    this.addGeoJsonToLayer(geometry, this.projectBoundaryGroup, {
      style: {
        color: MapColors.PROJECT_BOUNDARY,
        weight: 2,
        fillOpacity: 0.1,
      }
    });
  });
}

plotActivityBoundaries(project: any, currentFiscalYear: number): void {
  project.projectFiscals?.forEach((fiscal: any) => {
    const fiscalYear = fiscal.fiscalYear;
    const color = getFiscalYearColor(fiscalYear, currentFiscalYear);

    fiscal.activities?.forEach((activity: any) => {
      activity.activityBoundaries?.forEach((ab: any) => {
        const geometry = ab.activityGeometry;
        this.addGeoJsonToLayer(geometry, this.activityBoundaryGroup, {
          style: {
            color,
            weight: 2,
            fillOpacity: 0.1,
          }
        });
      });
    });
  });
}

safelyClearLayerGroup(group: L.LayerGroup, label: string): void {
  try {
    const rawLayers = (group as L.LayerGroup & { _layers?: Record<string, L.Layer> })._layers;
    if (!rawLayers) return;

    // remove only valid layers
    Object.values(rawLayers).forEach((layer) => {
      if (layer && typeof layer.remove === 'function' && typeof layer.on === 'function') {
        group.removeLayer(layer);
      } else {
        console.warn(`[Map] Skipping invalid layer inside ${label}`, layer);
      }
    });
  } catch (err) {
    console.error(`[Map] Failed to safely clear ${label}:`, err);
  }
}

togglePolygonLayers(zoomLevel: number): void {
  // only shows these polygons after zoom level 10
  const map = this.mapService.getSMKInstance()?.$viewer?.map;
  if (!map) return;
  if (zoomLevel >= 10) {
    if (!map.hasLayer(this.projectBoundaryGroup)) {
      map.addLayer(this.projectBoundaryGroup);
    }
    if (!map.hasLayer(this.activityBoundaryGroup)) {
      map.addLayer(this.activityBoundaryGroup);
    }
  } else {
    map.removeLayer(this.projectBoundaryGroup);
    map.removeLayer(this.activityBoundaryGroup);
  }
}

}
