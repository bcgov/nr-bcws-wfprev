import { AfterViewInit, ChangeDetectorRef, Component, ElementRef, ViewChild } from '@angular/core';
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
  imports: [ResizablePanelComponent,SearchFilterComponent],
  templateUrl: './map.component.html',
  styleUrl: './map.component.scss'
})
export class MapComponent implements AfterViewInit {
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

  private markersLayerGroup = L.layerGroup();

  constructor(
    protected cdr: ChangeDetectorRef,
    private readonly mapService: MapService,
    private readonly mapConfigService: MapConfigService,
    private readonly route: ActivatedRoute,
    private readonly sharedService: SharedService
  ) { }

  ngAfterViewInit(): void {
    if (this.mapContainer?.nativeElement) {
      this.initMap()
        .then(() => {
          this.mapIndex = this.mapService.getMapIndex() + 1;
          this.mapService.setMapIndex(this.mapIndex);

          setTimeout(() => {
            const bboxParam = this.route.snapshot.queryParamMap.get('bbox');
          
            if (!bboxParam) return;
          
            const bboxParts = bboxParam.split(',').map(parseFloat);
            if (bboxParts.length !== 4 || bboxParts.some(isNaN)) {
              console.warn('Invalid bbox query parameter:', bboxParam);
              return;
            }
          
            const [west, south, east, north] = bboxParts;
          
            try {
              const smk = this.mapService.getSMKInstance();
          
              if (!smk || !smk.$viewer || !smk.$viewer.map || !smk.$viewer.map.fitBounds) {
                console.warn('Could not access Leaflet map inside SMK.');
                return;
              }
          
              smk.$viewer.map.fitBounds([
                [south, west],
                [north, east]
              ], {
                padding: [20, 20]
              });
            } catch (error) {
              console.error('Error while fitting map bounds from bbox:', error);
            }
          }, 500);

          const smk = this.mapService.getSMKInstance();
          const map = smk?.$viewer?.map;

          if (map) {
            const legendHelper = new LeafletLegendService();
            legendHelper.addLegend(map, this.fiscalColorMap);
          }
          
        })
        .catch((error) => console.error('Error initializing map:', error));

        const map = this.mapService.getSMKInstance()?.$viewer?.map;

        if (map) {
          const legendHelper = new LeafletLegendService();
          legendHelper.addLegend(map, this.fiscalColorMap);

          // Add the marker layer group to the map
          this.markersLayerGroup.addTo(map);

          // Subscribe to project updates
          this.sharedService.displayedProjects$.subscribe(projects => {
            this.updateMarkers(projects);
          });
        }
    } else {
      console.error('Map container is not available.');
    }
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
    const map = this.mapService.getSMKInstance()?.$viewer?.map;
    if (!map) return;

    this.markersLayerGroup.clearLayers(); // Remove old markers

    projects
      .filter(p => p.latitude != null && p.longitude != null)
      .forEach(project => {
        const marker = L.marker([project.latitude, project.longitude], {
          icon: L.icon({
            iconUrl: '/assets/blue-pin-drop.svg',
            iconSize: [30, 50],
            iconAnchor: [12, 41],
            popupAnchor: [1, -34],
          })
        });
        this.markersLayerGroup.addLayer(marker);
      });
  }
}

