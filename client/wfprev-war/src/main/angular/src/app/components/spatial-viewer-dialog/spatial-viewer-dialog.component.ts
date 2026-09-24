import { Component, Inject, OnInit, AfterViewInit, OnDestroy } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { ProjectFile } from '../models';
import * as L from 'leaflet';
import { MiniMap, MiniMapService, MiniMapView, boundsOf } from 'src/app/services/mini-map.service';

import { CommonModule } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';

@Component({
  selector: 'app-spatial-viewer-dialog',
  templateUrl: './spatial-viewer-dialog.component.html',
  styleUrls: ['./spatial-viewer-dialog.component.scss'],
  standalone: true,
  imports: [CommonModule, MatIconModule, MatButtonModule]
})
export class SpatialViewerDialogComponent implements OnInit, AfterViewInit, OnDestroy {
  public file: ProjectFile;
  private miniMap: MiniMap | undefined;
  private geometryLayer: L.GeoJSON | undefined;
  private destroyed = false;

  constructor(
    public dialogRef: MatDialogRef<SpatialViewerDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: { file: ProjectFile },
    private readonly miniMapService: MiniMapService
  ) {
    this.file = data.file;
  }

  ngOnInit(): void {}

  ngAfterViewInit(): void {
    this.initMap();
  }

  ngOnDestroy(): void {
    this.destroyed = true;
    // Release the map, including the vector basemap's WebGL context, each time the dialog closes
    this.miniMapService.destroy(this.miniMap);
  }

  private async initMap(): Promise<void> {
    let miniMap: MiniMap;
    try {
      miniMap = await this.miniMapService.create(document.getElementById('spatial-viewer-map')!, this.initialView());
    } catch (err) {
      console.error('Error loading map', err);
      return;
    }

    // The dialog was closed while the map was being created
    if (this.destroyed) {
      this.miniMapService.destroy(miniMap);
      return;
    }
    this.miniMap = miniMap;
    const { map, leaflet } = miniMap;

    // If we have geometry, plot it
    if (this.file.boundaryGeometry && this.file.boundaryGeometry.coordinates) {
      try {
        // A KML/KMZ (or shapefile) can hold many polygons, and a polygon can have holes, so
        // the stored geometry is a MultiPolygon. Hand the whole geometry to Leaflet rather
        // than lifting a single ring out of it - it already expects GeoJSON [lon, lat] order
        // and draws every part and interior ring. See WFPREV-1201.
        this.geometryLayer = leaflet.geoJSON(this.file.boundaryGeometry as GeoJSON.MultiPolygon, {
          style: {
            color: '#1A5A96', // product primary blue
            weight: 2,
            fillColor: '#1A5A96',
            fillOpacity: 0.3
          }
        }).addTo(map);
      } catch (err) {
        console.error('Error rendering spatial geometry on map', err);
      }
    }
  }

  // Every part of the geometry. A file without one leaves the map on SMK's view of BC.
  private initialView(): MiniMapView | undefined {
    const bounds = this.file.boundaryGeometry?.coordinates ? boundsOf([this.file.boundaryGeometry]) : undefined;
    return bounds && { bounds };
  }

  onClose(): void {
    this.dialogRef.close();
  }
}
