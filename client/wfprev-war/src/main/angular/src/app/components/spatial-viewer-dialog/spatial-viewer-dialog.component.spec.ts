import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import * as L from 'leaflet';
import { ProjectFile } from '../models';
import { SpatialViewerDialogComponent } from './spatial-viewer-dialog.component';
import { MiniMap, MiniMapService, MiniMapView, showView } from 'src/app/services/mini-map.service';

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

const onePolygon: ProjectFile = {
  fileName: 'one-polygon.kmz',
  boundaryGeometry: {
    type: 'MultiPolygon',
    coordinates: [
      [[[-120.6408, 50.5343], [-120.6508, 50.5343], [-120.6508, 50.5443], [-120.6408, 50.5343]]]
    ]
  }
};

const nextFrames = () => new Promise(resolve => setTimeout(resolve, 500));

describe('SpatialViewerDialogComponent', () => {
  let fixture: ComponentFixture<SpatialViewerDialogComponent>;
  let component: SpatialViewerDialogComponent;
  const dialogRef = { close: jasmine.createSpy('close') };
  let miniMapService: jasmine.SpyObj<MiniMapService>;
  let miniMap: MiniMap | undefined;

  // Stands in for MiniMapService, whose SMK map starts on BC and is moved to the view before it is shown
  const createMiniMap = async (container: HTMLElement, view?: MiniMapView): Promise<MiniMap> => {
    const map = L.map(container).setView([54.6, -124.8], 4);
    // Something on the map that Leaflet animates when it zooms, as SMK's basemap is
    L.polyline([[54, -125], [54.1, -125.1]]).addTo(map);
    if (view) {
      showView(map, view);
    }
    miniMap = { smk: {}, map, leaflet: L };
    return miniMap;
  };

  async function setup(file: ProjectFile, create: (container: HTMLElement, view?: any) => Promise<MiniMap> = createMiniMap) {
    TestBed.resetTestingModule();
    miniMap = undefined;
    miniMapService = jasmine.createSpyObj('MiniMapService', ['create', 'destroy']);
    miniMapService.create.and.callFake(create);
    miniMapService.destroy.and.callFake(created => created?.map.remove());

    await TestBed.configureTestingModule({
      imports: [SpatialViewerDialogComponent, BrowserAnimationsModule],
      providers: [
        { provide: MatDialogRef, useValue: dialogRef },
        { provide: MAT_DIALOG_DATA, useValue: { file } },
        { provide: MiniMapService, useValue: miniMapService }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(SpatialViewerDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
    await fixture.whenStable();
  }

  afterEach(() => fixture?.destroy());

  it('creates an SMK mini map in the dialog\'s map container, showing every part of the geometry', async () => {
    await setup(twoPolygons);

    const [container, view] = miniMapService.create.calls.mostRecent().args as [HTMLElement, any];
    expect(miniMapService.create).toHaveBeenCalledTimes(1);
    expect(container).toBe(fixture.nativeElement.querySelector('#spatial-viewer-map'));
    const [[south, west], [north, east]] = view.bounds;
    expect(west).toBeCloseTo(-127.6576, 4);
    expect(east).toBeCloseTo(-120.6408, 4);
    expect(south).toBeCloseTo(50.5343, 4);
    expect(north).toBeCloseTo(53.7367, 4);
  });

  it('renders every polygon of a multi-polygon geometry, not just the first', async () => {
    await setup(twoPolygons);

    const layer = (component as any).geometryLayer;
    expect(layer).toBeTruthy();

    // Leaflet renders a MultiPolygon as one polygon layer holding both parts.
    const parts = layer.getLayers()[0].getLatLngs();
    expect(parts.length).toBe(2);

    const bounds = layer.getBounds();
    expect(bounds.getWest()).toBeCloseTo(-127.6576, 4);
    expect(bounds.getEast()).toBeCloseTo(-120.6408, 4);
  });

  it('draws the geometry with the Leaflet the map was created with', async () => {
    const geoJSON = jasmine.createSpy('geoJSON').and.callFake(L.geoJSON);
    await setup(twoPolygons, async (container, view) => {
      const created = await createMiniMap(container, view);
      return { ...created, leaflet: { ...L, geoJSON } as typeof L };
    });

    expect(geoJSON).toHaveBeenCalledWith(twoPolygons.boundaryGeometry as any, jasmine.any(Object));
  });

  it('keeps the map fitted to the geometry once it settles', async () => {
    await setup(onePolygon);
    await nextFrames();

    const map = miniMap!.map;
    expect(map.getZoom()).toBeGreaterThan(10);
    expect(map.getBounds().contains((component as any).geometryLayer.getBounds())).toBeTrue();
  });

  it('leaves the map on SMK\'s view of BC when the file carries no geometry', async () => {
    await setup({ fileName: 'no-geometry.kmz' });

    expect(miniMapService.create).toHaveBeenCalledOnceWith(jasmine.any(HTMLElement), undefined);
    expect((component as any).geometryLayer).toBeUndefined();
  });

  it('removes the map when the dialog is destroyed', async () => {
    await setup(twoPolygons);

    fixture.destroy();

    expect(miniMapService.destroy).toHaveBeenCalledWith(miniMap);
  });

  it('removes a map that was still being created when the dialog closed', async () => {
    let finishCreating!: () => void;
    await setup(twoPolygons, (container, view) => new Promise(resolve => {
      finishCreating = () => resolve(createMiniMap(container, view));
    }));

    fixture.destroy();
    finishCreating();
    await nextFrames();

    expect(miniMapService.destroy).toHaveBeenCalledWith(miniMap);
    expect((component as any).geometryLayer).toBeUndefined();
  });

  it('logs rather than throws when the map cannot be created', async () => {
    const consoleError = spyOn(console, 'error');

    await setup(twoPolygons, () => Promise.reject(new Error('SMK failed')));

    expect(consoleError).toHaveBeenCalledWith('Error loading map', jasmine.any(Error));
  });

  it('closes the dialog', async () => {
    await setup(twoPolygons);
    component.onClose();
    expect(dialogRef.close).toHaveBeenCalled();
  });
});
