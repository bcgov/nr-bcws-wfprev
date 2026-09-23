import { TestBed } from '@angular/core/testing';
import * as L from 'leaflet';
import { LOADING_CLASS, MINI_MAP_CLASS, MiniMapService, MiniMapView, boundsOf, showView } from './mini-map.service';
import { MapService } from './map.service';
import { miniMapConfig } from './map-config.service/map.config';

describe('MiniMapService', () => {
  let service: MiniMapService;
  let mapService: jasmine.SpyObj<MapService>;
  let container: HTMLElement;
  let map: L.Map;
  let smk: { $viewer: { map: L.Map }; destroy: jasmine.Spy };
  let windowLeaflet: any;
  // Stands in for SMK's copy of Leaflet, which SMK puts on window.L
  const smkLeaflet = { stand: 'in' };

  beforeEach(() => {
    windowLeaflet = (window as any).L;
    (window as any).L = smkLeaflet;

    container = document.createElement('div');
    container.style.width = '400px';
    container.style.height = '300px';
    document.body.appendChild(container);
    // Leaflet pins a container whose styles it can't see to position: relative, as it does outside the page
    map = L.map(container).setView([50, -120], 10);
    map.setMaxBounds([[0, -180], [90, -90]]);
    smk = { $viewer: { map }, destroy: jasmine.createSpy('destroy') };

    mapService = jasmine.createSpyObj('MapService', ['initSMK']);
    mapService.initSMK.and.returnValue(Promise.resolve(smk));

    TestBed.configureTestingModule({
      providers: [{ provide: MapService, useValue: mapService }]
    });
    service = TestBed.inject(MiniMapService);
  });

  afterEach(() => {
    map.remove();
    container.remove();
    (window as any).L = windowLeaflet;
  });

  describe('create', () => {
    it('creates an SMK map in the container with the mini map config', async () => {
      await service.create(container);

      expect(mapService.initSMK).toHaveBeenCalledOnceWith(jasmine.objectContaining({
        containerSel: container,
        config: [miniMapConfig()],
      }));
    });

    it('gives every mini map its own id, since SMK reuses its default ids', async () => {
      await service.create(container);
      await service.create(container);

      const ids = mapService.initSMK.calls.allArgs().map(([option]) => option.id);
      expect(ids).toEqual(['mini-map-1', 'mini-map-2']);
    });

    it('resolves with the SMK map, its Leaflet map, and SMK\'s copy of Leaflet', async () => {
      const miniMap = await service.create(container);

      expect(miniMap.smk).toBe(smk);
      expect(miniMap.map).toBe(map);
      expect(miniMap.leaflet as any).toBe(smkLeaflet);
    });

    it('keeps the mini maps\' zoom limit of 18', async () => {
      await service.create(container);

      expect(map.getMaxZoom()).toBe(18);
    });

    it('drops the bounds MapService gives every SMK map, which the mini maps never had', async () => {
      await service.create(container);

      expect(map.options.maxBounds).toBeFalsy();
    });

    it('lets SMK position the map, for a map created in a tab that is not showing', async () => {
      expect(container.style.position).toBe('relative');

      await service.create(container);

      expect(container.style.position).toBe('');
    });

    it('fails when SMK cannot create the map', async () => {
      mapService.initSMK.and.returnValue(Promise.reject(new Error('SMK failed')));

      await expectAsync(service.create(container)).toBeRejectedWithError('SMK failed');
      expect(container.classList).not.toContain(LOADING_CLASS);
    });

    it('opens the map on the view', async () => {
      await service.create(container, { center: [49.5, -119.6], zoom: 13 });

      expect(map.getZoom()).toBe(13);
      expect(map.getCenter().lat).toBeCloseTo(49.5, 4);
      expect(map.getCenter().lng).toBeCloseTo(-119.6, 4);
    });

    it('creates the map while the view loads, and keeps it hidden until it shows the view', async () => {
      let finishLoading!: (view: MiniMapView) => void;
      const creating = service.create(container, new Promise<MiniMapView>(resolve => finishLoading = resolve));
      await Promise.resolve();

      expect(mapService.initSMK).toHaveBeenCalled();
      expect(container.classList).toContain(LOADING_CLASS);

      finishLoading({ center: [49.5, -119.6], zoom: 13 });
      await creating;

      expect(map.getZoom()).toBe(13);
      // Then fades in
      expect(container.classList).not.toContain(LOADING_CLASS);
      expect(container.classList).toContain(MINI_MAP_CLASS);
    });

    it('leaves the map on SMK\'s view when there is none to show', async () => {
      await service.create(container, Promise.resolve(undefined));

      expect(map.getZoom()).toBe(10);
      expect(container.classList).not.toContain(LOADING_CLASS);
    });

    it('still creates the map when the view fails to load', async () => {
      const miniMap = await service.create(container, Promise.reject(new Error('offline')));

      expect(miniMap.map).toBe(map);
      expect(map.getZoom()).toBe(10);
      expect(container.classList).not.toContain(LOADING_CLASS);
    });
  });

  describe('showView', () => {
    it('fits an area, with padding, without animating', () => {
      const fitBounds = spyOn(map, 'fitBounds').and.callThrough();

      showView(map, { bounds: [[49, -120], [50, -119]], padding: 20 });

      expect(fitBounds).toHaveBeenCalledWith([[49, -120], [50, -119]], jasmine.objectContaining({ padding: [20, 20], animate: false }));
      expect(map.getBounds().contains([[49, -120], [50, -119]])).toBeTrue();
    });

    it('centres on a point at a zoom level, without animating', () => {
      const setView = spyOn(map, 'setView').and.callThrough();

      showView(map, { center: [49.5, -119.6], zoom: 14 });

      expect(setView).toHaveBeenCalledWith([49.5, -119.6], 14, jasmine.objectContaining({ animate: false }));
    });
  });

  describe('boundsOf', () => {
    const square = (west: number, south: number) => ({
      type: 'Polygon',
      coordinates: [[[west, south], [west + 1, south], [west + 1, south + 1], [west, south + 1], [west, south]]],
    });

    it('covers every geometry, as [lat, lng] pairs', () => {
      expect(boundsOf([square(-125, 49), square(-120, 52)])).toEqual([[49, -125], [53, -119]]);
    });

    it('reads geometry collections', () => {
      const collection = { type: 'GeometryCollection', geometries: [square(-125, 49), square(-120, 52)] };

      expect(boundsOf([collection])).toEqual([[49, -125], [53, -119]]);
    });

    it('leaves out missing and unreadable geometries', () => {
      expect(boundsOf([null, undefined, { type: 'Nonsense' }, square(-125, 49)])).toEqual([[49, -125], [50, -124]]);
    });

    it('is undefined when there is nothing to cover', () => {
      expect(boundsOf([])).toBeUndefined();
      expect(boundsOf([null, { type: 'Polygon', coordinates: [] }])).toBeUndefined();
    });
  });

  describe('destroy', () => {
    it('destroys the SMK map', async () => {
      const miniMap = await service.create(container);

      service.destroy(miniMap);

      expect(smk.destroy).toHaveBeenCalled();
    });

    it('does nothing when there is no map yet', () => {
      expect(() => service.destroy(undefined)).not.toThrow();
    });

    it('logs rather than throws when SMK fails to destroy the map', async () => {
      const miniMap = await service.create(container);
      smk.destroy.and.throwError('already gone');
      const consoleError = spyOn(console, 'error');

      service.destroy(miniMap);

      expect(consoleError).toHaveBeenCalledWith('Error occurred during SMK destruction:', jasmine.any(Error));
    });
  });
});
