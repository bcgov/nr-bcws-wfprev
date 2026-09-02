import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { ProjectFile } from '../models';
import { SpatialViewerDialogComponent } from './spatial-viewer-dialog.component';

const twoPolygons: ProjectFile = {
  fileName: 'two-polygons.kmz',
  boundaryGeometry: {
    type: 'MultiPolygon',
    coordinates: [
      [[[-127.6476, 53.7267], [-127.6576, 53.7267], [-127.6576, 53.7367], [-127.6476, 53.7267]]],
      [[[-120.6408, 50.5343], [-120.6508, 50.5343], [-120.6508, 50.5443], [-120.6408, 50.5343]]]
    ]
  }
};

describe('SpatialViewerDialogComponent', () => {
  let fixture: ComponentFixture<SpatialViewerDialogComponent>;
  let component: SpatialViewerDialogComponent;
  const dialogRef = { close: jasmine.createSpy('close') };

  async function setup(file: ProjectFile) {
    TestBed.resetTestingModule();
    await TestBed.configureTestingModule({
      imports: [SpatialViewerDialogComponent, BrowserAnimationsModule],
      providers: [
        { provide: MatDialogRef, useValue: dialogRef },
        { provide: MAT_DIALOG_DATA, useValue: { file } }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(SpatialViewerDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }

  it('renders every polygon of a multi-polygon geometry, not just the first', async () => {
    await setup(twoPolygons);

    const layer = (component as any).geometryLayer;
    expect(layer).toBeTruthy();

    // Leaflet renders a MultiPolygon as one polygon layer holding both parts.
    const parts = layer.getLayers()[0].getLatLngs();
    expect(parts.length).toBe(2);

    // The fitted bounds have to span both polygons, not only the first.
    const bounds = layer.getBounds();
    expect(bounds.getWest()).toBeCloseTo(-127.6576, 4);
    expect(bounds.getEast()).toBeCloseTo(-120.6408, 4);
  });

  it('does not plot anything when the file carries no geometry', async () => {
    await setup({ fileName: 'no-geometry.kmz' });

    expect((component as any).geometryLayer).toBeUndefined();
  });

  it('closes the dialog', async () => {
    await setup(twoPolygons);
    component.onClose();
    expect(dialogRef.close).toHaveBeenCalled();
  });
});
