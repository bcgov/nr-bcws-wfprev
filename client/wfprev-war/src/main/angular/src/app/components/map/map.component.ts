import { AfterViewInit, ChangeDetectorRef, Component, ElementRef, OnDestroy, ViewChild, EnvironmentInjector, createComponent } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { SearchFilterComponent } from 'src/app/components/search-filter/search-filter.component';
import { MapConfigService } from 'src/app/services/map-config.service';
import { MapService } from 'src/app/services/map.service';
import { LeafletLegendService, getBluePinIcon, getActivePinIcon } from 'src/app/utils/tools';
import { SharedService } from 'src/app/services/shared-service';
import * as L from 'leaflet';
import '@maplibre/maplibre-gl-leaflet';
import { ProjectPopupComponent } from 'src/app/components/project-popup/project-popup.component';
import { Project, ProjectLocation } from 'src/app/components/models';
import { ResizablePanelComponent } from 'src/app/components/resizable-panel/resizable-panel.component';
import { BC_BOUNDS } from 'src/app/utils/constants';
import { ProjectService } from 'src/app/services/project-services';

@Component({
  selector: 'app-map',
  standalone: true,
  imports: [ResizablePanelComponent, SearchFilterComponent, ProjectPopupComponent],
  templateUrl: './map.component.html',
  styleUrl: './map.component.scss'
})
export class MapComponent implements AfterViewInit, OnDestroy {
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
  private markersClusterGroup: L.MarkerClusterGroup | null = null;
  private readonly projectMarkerMap = new Map<string, L.Marker>();
  private activeMarker: L.Marker | null = null;
  private selectedProject: Project | undefined;
  private readonly activityBoundaryGroup: L.LayerGroup = L.layerGroup();
  private readonly projectBoundaryGroup: L.LayerGroup = L.layerGroup();
  private readonly featureCache = new Map<string, Project>();
  private projectBoundaryLayer: L.Layer | null = null;
  private activityBoundaryLayer: L.Layer | null = null;
  legendControl: L.Control | null = null;
  // fiscal year starts April 1
  currentFiscalYear = new Date().getMonth() >= 3
    ? new Date().getFullYear()
    : new Date().getFullYear() - 1;
  constructor(
    protected cdr: ChangeDetectorRef,
    private readonly mapService: MapService,
    private readonly mapConfigService: MapConfigService,
    private readonly route: ActivatedRoute,
    private readonly sharedService: SharedService,
    private readonly injector: EnvironmentInjector,
    private readonly projectService: ProjectService
  ) { }

  // promise will not be awaited by angular if void is not returned
  ngOnDestroy(): void {
    (async () => {
      try {
        await this.mapService.destroySMK();
      } catch (err) {
        console.error('Error destroying SMK:', err);
      }
    })();
  }

  ngAfterViewInit(): void {
    if (!this.mapContainer?.nativeElement) {
      console.error('Map container is not available.');
      return;
    }

    this.mapIndex = this.mapService.getMapIndex();
    this.mapService.setMapIndex(this.mapIndex + 1);
    this.mapService.setContainerId('map');

    // Handle open/close project popups
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

      if (!map) return;

      const bcBounds: L.LatLngBoundsExpression = BC_BOUNDS;
      map.fitBounds(bcBounds);

      // Add legend + marker cluster
      const legendHelper = new LeafletLegendService();
      this.legendControl = legendHelper.addLegend(map, this.fiscalColorMap);

      this.markersClusterGroup = L.markerClusterGroup({
        showCoverageOnHover: false,
        iconCreateFunction: (cluster) =>
          L.divIcon({
            html: `<div class="cluster-icon"><span>${cluster.getChildCount()}</span></div>`,
            className: 'custom-marker-cluster',
            iconSize: L.point(40, 40),
          }),
      });

      map.addLayer(this.markersClusterGroup);
      this.isMapReady = true;

      // Initial load of project locations
      const currentFilters = this.sharedService.currentFilters || {};
      this.fetchAndUpdateProjectLocations(currentFilters);

      this.sharedService.filters$.subscribe((filters) => {
        if (!this.isMapReady) return;
        this.fetchAndUpdateProjectLocations(filters || {});
      });
    });

    // Handle selected project to open popup
    this.sharedService.selectedProject$.subscribe((project) => {
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

  clone<T>(o: T): T {
    return structuredClone(o);
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

  safelyClearLayerGroup(group: L.LayerGroup, label: string): void {
    try {
      const rawLayers = (group as L.LayerGroup & { _layers?: Record<string, L.Layer> })._layers;
      if (!rawLayers) return;

      // remove only valid layers
      for (const layer of Object.values(rawLayers)) {
        if (layer && typeof layer.remove === 'function' && typeof layer.on === 'function') {
          group.removeLayer(layer);
        } else {
          console.warn(`[Map] Skipping invalid layer inside ${label}`, layer);
        }
      }
    } catch (err) {
      console.error(`[Map] Failed to safely clear ${label}:`, err);
    }
  }

  private updateProjectMarkersFromLocations(locations: ProjectLocation[]): void {
    this.teardownActiveUI();

    const smk = this.mapService.getSMKInstance();
    const map = smk?.$viewer?.map;

    if (!map || !this.markersClusterGroup) {
      console.warn('Map cannot update markers');
      return;
    }

    if (this.markersClusterGroup) {
      this.markersClusterGroup.getLayers().forEach(l => {
        if (l instanceof L.Marker) {
          l.unbindPopup(); 
          l.closePopup(); 
          l.off(); 
        }
      });
      this.markersClusterGroup.clearLayers();
    }
    this.projectMarkerMap.clear();
    this.activeMarker = null;

    const validLocations = locations.filter(
      (loc): loc is Required<ProjectLocation> =>
        !!loc.projectGuid && loc.latitude !== undefined && loc.longitude !== undefined
    );

    for (const loc of validLocations) {
      try {
        const marker = L.marker([loc.latitude, loc.longitude], {
          icon: getBluePinIcon(),
        });

        marker.on('mouseover', () => {
          if (!loc.projectGuid) return;
          if (this.featureCache.has(loc.projectGuid)) return;

          this.projectService.getFeatureByProjectGuid(loc.projectGuid).subscribe({
            next: (project) => {
              if (project) {
                this.featureCache.set(loc.projectGuid, project);
                console.log(`prefetched project: ${project.projectName || loc.projectGuid}`);
              }
            },
            error: (err) => {
              console.error('Error prefetching feature on hover:', err);
            },
          });
        });

        marker.on('click', () => {
          const projectGuid = loc.projectGuid;
          if (!projectGuid) return;

          const handleAndShow = (project: Project) => {
            this.handleProjectClick(project);

            const popupDiv = document.createElement('div');
            const cmpRef = createComponent(ProjectPopupComponent, {
              environmentInjector: this.injector,
            });
            cmpRef.instance.project = project;
            cmpRef.instance.map = map;
            cmpRef.hostView.detectChanges();
            popupDiv.appendChild(cmpRef.location.nativeElement);

            marker.unbindPopup();
            marker.bindPopup(popupDiv, {
              maxWidth: 486,
              minWidth: 0,
              autoPan: true,
            });

            if (this.activeMarker && this.activeMarker !== marker) {
              this.activeMarker.setIcon(getBluePinIcon());
            }
            marker.setIcon(getActivePinIcon());
            this.activeMarker = marker;

            marker.openPopup();
          };

          const cached = this.featureCache.get(projectGuid);
          if (cached) {
            handleAndShow(cached);
            return;
          }

          this.projectService.getFeatureByProjectGuid(projectGuid).subscribe({
            next: (project) => {
              if (project) {
                this.featureCache.set(projectGuid, project);
                handleAndShow(project);
              }
            },
            error: (err) => {
              console.error('Map error fetching project on click:', err);
            },
          });
        });

        marker.on('mouseout', () => marker.closeTooltip());

        this.projectMarkerMap.set(loc.projectGuid, marker);
        this.markersClusterGroup.addLayer(marker);

      } catch (err) {
        console.error('Map failed to add marker for location:', loc, err);
      }
    }

    if (validLocations.length > 0) {
      const projectGuids = Array.from(
        new Set(validLocations.map(v => v.projectGuid))
      );

      if (this.projectBoundaryLayer && map.hasLayer(this.projectBoundaryLayer)) {
        map.removeLayer(this.projectBoundaryLayer);
      }
      if (this.activityBoundaryLayer && map.hasLayer(this.activityBoundaryLayer)) {
        map.removeLayer(this.activityBoundaryLayer);
      }

      if (projectGuids.length > 0) {
        this.projectBoundaryLayer = this.mapService.createProjectBoundaryLayer(map, projectGuids);
        this.activityBoundaryLayer = this.mapService.createActivityBoundaryLayer(map, projectGuids, this.currentFiscalYear);
        map.addLayer(this.projectBoundaryLayer);
        map.addLayer(this.activityBoundaryLayer);
      } else {
        this.projectBoundaryLayer = null;
        this.activityBoundaryLayer = null;
      }
    } else {
      console.log('[Map] No valid project locations to add markers for.');
      map.removeLayer(this.projectBoundaryLayer);
      map.removeLayer(this.activityBoundaryLayer);
      this.projectBoundaryLayer = null;
      this.activityBoundaryLayer = null;
    }

    console.log(`Map added ${validLocations.length} project location markers`);
  }



  private handleProjectClick(project: Project): void {
    const currentList = this.sharedService.currentDisplayedProjects;
    const alreadyInList = currentList.some(p => p.projectGuid === project.projectGuid);

    if (!alreadyInList) {
      const updated = [...currentList, project];
      this.sharedService.updateDisplayedProjects(updated);
    }

    this.sharedService.selectProject(project);
    this.openPopupForProject(project);
  }

  private fetchAndUpdateProjectLocations(filters: any): void {
    this.projectService.getProjectLocations(filters).subscribe({
      next: (locations: ProjectLocation[]) => {
        if (!locations || locations.length === 0) {
          console.warn('[Map] No project locations found.');
        }
        this.updateProjectMarkersFromLocations(locations);
      },
      error: (err) => {
        console.error('Error fetching project locations:', err);
      },
    });
  }

  // Close any open popup and fully remove the active marker so no DOM is orphaned
  private teardownActiveUI(): void {
    const smk = this.mapService.getSMKInstance();
    const map: L.Map | undefined = smk?.$viewer?.map;

    map?.closePopup();

    // Remove the active marker and unbind popup to drop DOM from panes
    if (this.activeMarker) {
      this.activeMarker.unbindPopup();
      if (this.markersClusterGroup?.hasLayer(this.activeMarker)) {
        this.markersClusterGroup.removeLayer(this.activeMarker);
      } else {
        this.activeMarker.remove();
      }
    }
    this.activeMarker = null;
  }

}
