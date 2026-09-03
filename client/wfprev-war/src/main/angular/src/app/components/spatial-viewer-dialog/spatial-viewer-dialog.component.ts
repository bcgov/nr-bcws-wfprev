import { Component, Inject, OnInit, AfterViewInit } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { ProjectFile } from '../models';
import * as L from 'leaflet';

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
export class SpatialViewerDialogComponent implements OnInit, AfterViewInit {
  public file: ProjectFile;
  private map: L.Map | undefined;
  private geometryLayer: L.GeoJSON | undefined;

  constructor(
    public dialogRef: MatDialogRef<SpatialViewerDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: { file: ProjectFile }
  ) {
    this.file = data.file;
  }

  ngOnInit(): void {}

  ngAfterViewInit(): void {
    this.initMap();
  }

  private initMap(): void {
    // Initialize map
    this.map = L.map('spatial-viewer-map', {
      zoomControl: false,
      maxZoom: 18,
      minZoom: 4
    }).setView([53.7267, -127.6476], 5); // Default center of BC

    L.control.zoom({ position: 'topright' }).addTo(this.map);

    // Add base layer (matching main map default)
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: '© OpenStreetMap contributors'
    }).addTo(this.map);

    // If we have geometry, plot it
    if (this.file.boundaryGeometry && this.file.boundaryGeometry.coordinates) {
      try {
        // A KML/KMZ (or shapefile) can hold many polygons, and a polygon can have holes, so
        // the stored geometry is a MultiPolygon. Hand the whole geometry to Leaflet rather
        // than lifting a single ring out of it - it already expects GeoJSON [lon, lat] order
        // and draws every part and interior ring. See WFPREV-1201.
        this.geometryLayer = this.createGeoJSON(this.file.boundaryGeometry, {
          style: {
            color: '#1A5A96', // product primary blue
            weight: 2,
            fillColor: '#1A5A96',
            fillOpacity: 0.3
          }
        }).addTo(this.map);

        // Fit map bounds to every part of the geometry
        const bounds = this.geometryLayer.getBounds();
        if (bounds.isValid()) {
          this.map.fitBounds(bounds);
        }
      } catch (err) {
        console.error('Error rendering spatial geometry on map', err);
      }
    }
  }

  createGeoJSON(geom: any, options?: L.GeoJSONOptions): L.GeoJSON {
    return L.geoJSON(geom, options);
  }

  onClose(): void {
    this.dialogRef.close();
  }
}
