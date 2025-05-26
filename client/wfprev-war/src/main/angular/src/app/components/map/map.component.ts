import { AfterViewInit, ChangeDetectorRef, Component, ElementRef, OnDestroy, ViewChild } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { ResizablePanelComponent } from 'src/app/components/resizable-panel/resizable-panel.component';
import { SearchFilterComponent } from 'src/app/components/search-filter/search-filter.component';
import { MapConfigService } from 'src/app/services/map-config.service';
import { MapService } from 'src/app/services/map.service';
import { LeafletLegendService } from 'src/app/utils/tools';
import { SharedService } from 'src/app/services/shared-service';
import * as L from 'leaflet';

@Component({
  selector: 'app-map',
  standalone: true,
  imports: [ResizablePanelComponent, SearchFilterComponent],
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

  private isMapReady = false;
  private latestProjects: any[] = [];
  private hasClusterBeenAddedToMap = false;
  private markersClusterGroup: L.MarkerClusterGroup | null = null;

  constructor(
    protected cdr: ChangeDetectorRef,
    private readonly mapService: MapService,
    private readonly mapConfigService: MapConfigService,
    private readonly route: ActivatedRoute,
    private readonly sharedService: SharedService
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

  this.initMap().then(() => {
    const smk = this.mapService.getSMKInstance();
    const map = smk?.$viewer?.map;

    if (map) {
      const legendHelper = new LeafletLegendService();
      legendHelper.addLegend(map, this.fiscalColorMap);

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
  } catch (err) {
    console.error('[Map] Error clearing markers:', err);
  }

  projects
    .filter(p => p.latitude != null && p.longitude != null)
    .forEach(project => {
      try {
        const marker = L.marker([project.latitude, project.longitude], {
          icon: L.icon({
            iconUrl: '/assets/blue-pin-drop.svg',
            iconSize: [30, 50],
            iconAnchor: [12, 41],
            popupAnchor: [1, -34],
          })
        });

        // Bind a popup with project info
        const popupContent = `
          <b>${project.projectName}</b><br>
          Type: ${project.projectTypeCode || 'N/A'}<br>
          Status: ${project.planFiscalStatusCode || 'N/A'}<br>
          Fiscal Years:  1991<br>
          Latitude: ${project.latitude}<br>
          Longitude: ${project.longitude}
        `;
        marker.bindPopup(this.getProjectPopupHTML(project));

        this.markersClusterGroup!.addLayer(marker); 
      } catch (err) {
        console.error('[Map] Failed to add marker:', project, err);
      }
    });
}

private getProjectPopupHTML(project: any): string {
  const formatCurrency = (val: number | undefined) =>
    val != null ? `$${val.toLocaleString()}` : 'N/A';
  const fiscalBlocks = (project.projectFiscals || []).map((fiscal: any) => `
    <div style="border:1px solid #ccc; padding:8px; margin:4px 0;">
      <strong>Fiscal Year:</strong> ${fiscal.fiscalYear || 'N/A'}<br>
      <strong>Fiscal Name:</strong> ${fiscal.projectFiscalName || 'N/A'}<br>
      <strong>Completed Hectares:</strong> ${fiscal.fiscalCompletedSizeHa ?? 'N/A'} Ha<br>
      <strong>Planned Hectares:</strong> ${fiscal.fiscalPlannedProjectSizeHa ?? 'N/A'} Ha<br>
      <strong>Actual Spend:</strong> ${formatCurrency(fiscal.fiscalActualAmount)}<br>
      <strong>Forecast Amount:</strong> ${formatCurrency(fiscal.fiscalForecastAmount)}<br>
      <strong>Status:</strong> ${fiscal.planFiscalStatusCode || 'N/A'}
    </div>
  `).join('');

  return `
    <div>
      <h3>${project.projectName || 'Project Name'}</h3>
      <div><strong>Project Type:</strong> ${project.projectTypeDescription || 'N/A'}</div>
      <div><strong>Business Area:</strong> ${project.programAreaName || 'N/A'}</div>
      <div><strong>Nearest Community:</strong> ${project.nearestCommunity || 'N/A'}</div>
      <div style="margin:8px 0;"><strong>Project Description:</strong><br>${project.projectDescription || 'No description available.'}</div>
      <div><strong>Fiscal Years:</strong></div>
      ${fiscalBlocks || '<div>No fiscal data available.</div>'}
      <button onclick="window.location.href='${window.location.origin}/edit-project?projectGuid=${project.projectGuid}'">
        View Details
      </button>
    </div>
  `;
}


}
