import { AfterViewInit, ChangeDetectorRef, Component, ElementRef, Input, ViewChild } from '@angular/core';
import { ResizablePanelComponent } from 'src/app/components/resizable-panel/resizable-panel.component';
import { MapConfigService } from 'src/app/services/map-config.service';
import { MapService } from 'src/app/services/map.service';

@Component({
  selector: 'app-map',
  standalone: true,
  imports: [ResizablePanelComponent],
  templateUrl: './map.component.html',
  styleUrl: './map.component.scss'
})
export class MapComponent implements AfterViewInit{
  @ViewChild('mapContainer', { static: false }) mapContainer!: ElementRef;
  mapConfig: any[] = [];
  
  panelContent: string = `
    The goal of the BC Wildfire Service (BCWS) Prevention Program is to reduce the negative impacts of wildfire on public safety, property, the environment and the economy using the seven disciplines of the FireSmart program.
    <br>
    British Columbia is experiencing a serious and sustained increase in extreme wildfire behaviour and fire events particularly in the wildland-urban interface. More human activity and development is taking place in or near forests, creating greater consequences for the socioeconomic health and safety of citizens and visitors. At the same time, the impacts of climate change are increasing, fire size and severity are increasing, and fire seasons are becoming longer. Prevention is more than stopping new, human-caused fires. FireSmart is based on the premise of shared responsibility and on promoting the integration and collaboration of wildfire prevention and mitigation efforts. All partners and stakeholders have a role to play.
  `;

  constructor(    
    protected cdr: ChangeDetectorRef,
    private readonly mapService: MapService,
    private readonly mapConfigService: MapConfigService
  ) {}

  ngAfterViewInit(): void {
    this.initMap();
  }

  private initMap(): void {
    const self = this;
    const mapConfig = this.clone(this.mapConfig);

    this.mapConfigService
    .getMapConfig()
    .then((mapState) => {
      mapConfig.push(mapState);
    })
    .then(() => {
      const deviceConfig = { viewer: { device: 'desktop' } };
      this.mapConfig = [...mapConfig, deviceConfig, 'theme=wf', '?'];
      console.log('mapConfig: ', JSON.stringify(this.mapConfig));
    })
    .catch((error) => {
      console.error('Error loading map:', error);
    });

    this.mapService.createSMK({
      id: 'map',
      containerSel: self.mapContainer.nativeElement,
      config: mapConfig,
    })

    const SMK = (window as any)['SMK'];
    
  }

  clone(o: any) {
    return JSON.parse(JSON.stringify(o));
  }
}

