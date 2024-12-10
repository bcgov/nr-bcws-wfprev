import { AfterViewInit, ChangeDetectorRef, Component } from '@angular/core';
import { ResizablePanelComponent } from 'src/app/components/resizable-panel/resizable-panel.component';
import * as L from 'leaflet';

@Component({
  selector: 'app-map',
  standalone: true,
  imports: [ResizablePanelComponent],
  templateUrl: './map.component.html',
  styleUrl: './map.component.scss'
})
export class MapComponent implements AfterViewInit{
  private map: L.Map | undefined;
  
  panelContent: string = `
    The goal of the BC Wildfire Service (BCWS) Prevention Program is to reduce the negative impacts of wildfire on public safety, property, the environment and the economy using the seven disciplines of the FireSmart program.
    <br>
    British Columbia is experiencing a serious and sustained increase in extreme wildfire behaviour and fire events particularly in the wildland-urban interface. More human activity and development is taking place in or near forests, creating greater consequences for the socioeconomic health and safety of citizens and visitors. At the same time, the impacts of climate change are increasing, fire size and severity are increasing, and fire seasons are becoming longer. Prevention is more than stopping new, human-caused fires. FireSmart is based on the premise of shared responsibility and on promoting the integration and collaboration of wildfire prevention and mitigation efforts. All partners and stakeholders have a role to play.
  `;

  constructor(    
    protected cdr: ChangeDetectorRef,
  ) {}

  ngAfterViewInit(): void {
    setTimeout(() => {
      this.initMap();
    });
  }

  private initMap(): void {
    // Initialize the map and set its view
    this.map = L.map('map');
    const bcBounds: L.LatLngBoundsExpression = [
      [48.3, -139.1],
      [60.0, -114.0]
    ];
    this.map.fitBounds(bcBounds);

    // Add a tile layer to the map (this is the OpenStreetMap layer)
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
    }).addTo(this.map);
  }

  onPanelResized(): void {
    if (this.map) {
      this.map.invalidateSize();  // Inform Leaflet to recalculate map size
      this.cdr.markForCheck();
    }
  }
}
