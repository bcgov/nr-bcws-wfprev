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
      maxZoom: 20,
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
        // Reverse coordinates from [lon, lat] to [lat, lon] for Leaflet
        const coordinates = this.file.boundaryGeometry.coordinates[0][0].map(coord => [coord[1], coord[0]] as [number, number]);
        
        const polygon = L.polygon(coordinates, {
          color: '#1A5A96', // product primary blue
          weight: 2,
          fillColor: '#1A5A96',
          fillOpacity: 0.3
        }).addTo(this.map);

        // Fit map bounds to the polygon
        this.map.fitBounds(polygon.getBounds());
      } catch (err) {
        console.error('Error rendering spatial geometry on map', err);
      }
    }
  }

  onClose(): void {
    this.dialogRef.close();
  }
}
