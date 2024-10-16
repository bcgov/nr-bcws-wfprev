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

  constructor(    
    protected cdr: ChangeDetectorRef,
  ) {}

  ngAfterViewInit(): void {
    this.initMap();
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
