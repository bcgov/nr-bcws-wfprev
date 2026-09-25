import { TestBed } from '@angular/core/testing';
import { MapService } from './map.service';
import { BC_BOUNDS } from 'src/app/utils/constants';
import { TokenService } from './token.service';
import { AppConfigService } from './app-config.service';
import { SmkService } from './smk.service';

describe('MapService', () => {
  let service: MapService;
  let mockWindow: any;
  let mockSMK: any;
  let mockL: any;
  let maplibreSpy: jasmine.Spy;
  let smkServiceMock: { baseUrl: string; load: jasmine.Spy };

  beforeEach(() => {
    // Mock SMK
    mockSMK = {
      INIT: jasmine.createSpy('INIT').and.returnValue(Promise.resolve({
        destroy: jasmine.createSpy('destroy')
      })),
      HANDLER: {
        set: jasmine.createSpy('set')
      },
      TYPE: {
        Viewer: {
          leaflet: {
            prototype: {
              initialize: jasmine.createSpy('initialize'),
              mapResized: jasmine.createSpy('mapResized'),
              map: {
                invalidateSize: jasmine.createSpy('invalidateSize'),
                setMaxBounds: jasmine.createSpy('setMaxBounds'),
                setMaxZoom: jasmine.createSpy('setMaxZoom')
              }
            }
          }
        }
      }
    };

    // Mock L (Leaflet)
    mockL = {
      tileLayer: jasmine.createSpy('tileLayer').and.returnValue({}),
      latLng: jasmine.createSpy('latLng').and.returnValue({}),
      latLngBounds: jasmine.createSpy('latLngBounds').and.returnValue({})
    };

    // Setup window mock with correct test environment URL
    mockWindow = {
      SMK: mockSMK,
      L: mockL,
      location: {
        protocol: 'http:',
        host: 'localhost:9876'  // Updated to match test environment
      }
    };

    // Replace window with mock
    (window as any)['SMK'] = mockSMK;
    (window as any)['L'] = mockL;

    smkServiceMock = {
      baseUrl: `${window.location.protocol}//${window.location.host}/assets/smk/`,
      load: jasmine.createSpy('load').and.returnValue(Promise.resolve(mockSMK))
    };

    TestBed.configureTestingModule({
      providers: [
        MapService,
        { provide: SmkService, useValue: smkServiceMock },
        {
          provide: TokenService,
          useValue: { getOauthToken: jasmine.createSpy('getOauthToken').and.returnValue('TEST_TOKEN') }
        },
        {
          provide: AppConfigService,
          useValue: { getConfig: () => ({ rest: { wfprev: 'http://localhost:9876' } }) }
        }
      ]
    });

    service = TestBed.inject(MapService);
    maplibreSpy = spyOn(service, 'createMaplibreGLLayer')
      .and.callFake((opts: any) => ({ __opts: opts }));
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  describe('getMapIndex and setMapIndex', () => {
    it('should get and set map index correctly', () => {
      expect(service.getMapIndex()).toBe(0);
      service.setMapIndex(1);
      expect(service.getMapIndex()).toBe(1);
    });
  });

  describe('createSMK', () => {
    beforeEach(async () => {
      // Reset the INIT spy call count before each test
      mockSMK.INIT.calls.reset();
      // Mock patch to avoid the initial INIT call
      spyOn(service, 'patch').and.returnValue(Promise.resolve());
    });

    it('should initialize SMK with default config if none provided', async () => {
      const option = {};
      await service.createSMK(option);

      // We only care about the last call to INIT (the actual createSMK call)
      expect(mockSMK.INIT).toHaveBeenCalledWith({
        baseUrl: `${window.location.protocol}//${window.location.host}/assets/smk/`,
        config: [{
          tools: [
            { type: 'baseMaps', enabled: true, choices: ['topographic-v2', 'bc-basemap-hillshade', 'imagery-v2'] },
            {
              type: 'bespoke',
              instance: 'full-extent',
              title: 'Zoom to Full Extent',
              enabled: true,
              position: 'actionbar',
              showTitle: false,
              showPanel: false,
              icon: 'zoom_out_map',
              order: 3,
            },
          ],
        }],
      });

    });

    it('should handle existing config array', async () => {
      const option = {
        config: [{ existingConfig: true }]
      };
      await service.createSMK(option);

      expect(mockSMK.INIT).toHaveBeenCalledWith({
        baseUrl: `${window.location.protocol}//${window.location.host}/assets/smk/`,
        config: [
          { existingConfig: true },
          {
            tools: [
              { type: 'baseMaps', enabled: true, choices: ['topographic-v2', 'bc-basemap-hillshade', 'imagery-v2'] },
              {
                type: 'bespoke',
                instance: 'full-extent',
                title: 'Zoom to Full Extent',
                enabled: true,
                position: 'actionbar',
                showTitle: false,
                showPanel: false,
                icon: 'zoom_out_map',
                order: 3,
              },
            ],
          },
        ],
      });
    });


    it('should throw error if config is not an array', async () => {
      spyOn(console, 'error');
      const option = {
        config: 'not an array'
      };

      await expectAsync(service.createSMK(option)).toBeRejected();
    });

    it('should toggle layer visibilities and call updateLayersVisible when $viewer exists', async () => {
      const setItemVisible = jasmine.createSpy('setItemVisible');
      const updateLayersVisible = jasmine.createSpy('updateLayersVisible').and.returnValue(Promise.resolve());

      const smkInstanceMock = {
        destroy: jasmine.createSpy('destroy'),
        $viewer: {
          displayContext: {
            layers: { setItemVisible },
          },
          updateLayersVisible,
        },
      };

      mockSMK.INIT.and.returnValue(Promise.resolve(smkInstanceMock));

      const option: any = {};

      await service.createSMK(option);

      expect(setItemVisible.calls.count()).toBe(8);
      expect(setItemVisible).toHaveBeenCalledWith('ministry-of-forests-regions', true);
      expect(setItemVisible).toHaveBeenCalledWith('ministry-of-forests-districts', false);
      expect(setItemVisible).toHaveBeenCalledWith('wildfire-org-unit-fire-centre', false);
      expect(setItemVisible).toHaveBeenCalledWith('fire-perimeters', false);
      expect(setItemVisible).toHaveBeenCalledWith('active-wildfires-out-of-control', false);
      expect(setItemVisible).toHaveBeenCalledWith('active-wildfires-holding', false);
      expect(setItemVisible).toHaveBeenCalledWith('active-wildfires-under-control', false);
      expect(setItemVisible).toHaveBeenCalledWith('active-wildfires-out', false);

      expect(updateLayersVisible).toHaveBeenCalled();

      expect(service.getSMKInstance()).toBe(smkInstanceMock);
    });
  });

  describe('initSMK', () => {
    beforeEach(() => {
      mockSMK.INIT.calls.reset();
      spyOn(service, 'patch').and.returnValue(Promise.resolve());
    });

    afterEach(() => {
      delete mockSMK.BOOT;
    });

    it('should patch SMK, then create the map from SMK\'s assets', async () => {
      await service.initSMK({ id: 'mini-map-1', config: [] });

      expect(service.patch).toHaveBeenCalled();
      expect(mockSMK.INIT).toHaveBeenCalledOnceWith({
        baseUrl: smkServiceMock.baseUrl,
        id: 'mini-map-1',
        config: [],
      });
    });

    it('should not fail because an earlier map failed to initialize', async () => {
      // SMK creates each map once the one before it (SMK.BOOT) has, and fails it if that one failed
      mockSMK.BOOT = Promise.reject(new Error('earlier map failed'));
      mockSMK.INIT.and.callFake(() => mockSMK.BOOT = mockSMK.BOOT.then(() => 'this map'));

      await expectAsync(service.initSMK({})).toBeResolvedTo('this map');
    });

    it('should not create a map when SMK fails to load', async () => {
      (service.patch as jasmine.Spy).and.returnValue(Promise.reject(new Error('offline')));

      await expectAsync(service.initSMK({})).toBeRejectedWithError('offline');
      expect(mockSMK.INIT).not.toHaveBeenCalled();
    });
  });

  describe('patch', () => {
    it('should load SMK and patch it without creating a map', async () => {
      const originalInitialize = mockSMK.TYPE.Viewer.leaflet.prototype.initialize;

      await service.patch();

      expect(smkServiceMock.load).toHaveBeenCalled();
      expect(mockSMK.INIT).not.toHaveBeenCalled();
      expect(mockSMK.HANDLER.set).toHaveBeenCalledWith('BespokeTool--full-extent', 'triggered', jasmine.any(Function));
      expect(mockSMK.TYPE.Viewer.leaflet.prototype.initialize).not.toBe(originalInitialize);
    });

    it('should load and patch SMK only once, however many maps are created', async () => {
      const originalInitialize = mockSMK.TYPE.Viewer.leaflet.prototype.initialize;
      const viewer = { map: { setMaxBounds: jasmine.createSpy('setMaxBounds'), setMaxZoom: jasmine.createSpy('setMaxZoom') } };

      await service.patch();
      await service.patch();
      mockSMK.TYPE.Viewer.leaflet.prototype.initialize.call(viewer, {});

      expect(smkServiceMock.load).toHaveBeenCalledTimes(1);
      expect(mockSMK.HANDLER.set).toHaveBeenCalledTimes(1);
      // Wrapped once: the original initializer runs once per viewer, not once per patch
      expect(originalInitialize).toHaveBeenCalledTimes(1);
      expect(viewer.map.setMaxZoom).toHaveBeenCalledWith(19);
    });

    it('should try again after a failed load', async () => {
      spyOn(console, 'error');
      smkServiceMock.load.and.returnValues(Promise.reject(new Error('offline')), Promise.resolve(mockSMK));

      await expectAsync(service.patch()).toBeRejected();
      await expectAsync(service.patch()).toBeResolved();
      expect(smkServiceMock.load).toHaveBeenCalledTimes(2);
    });

    it('should not register any basemap of its own', async () => {
      await service.patch();

      expect(mockL.tileLayer).not.toHaveBeenCalled();
    });

    it("should show and hide layers without SMK's 200ms wait, still handling changes made together at once", async () => {
      const refreshLayers = jasmine.createSpy('refreshLayers').and.returnValue('refreshed');
      mockSMK.TYPE.Viewer.leaflet.prototype.refreshLayers = refreshLayers;

      await service.patch();
      const viewer = {};
      const result = mockSMK.TYPE.Viewer.leaflet.prototype.refreshLayers.call(viewer);
      mockSMK.TYPE.Viewer.leaflet.prototype.refreshLayers.call(viewer, 500);

      expect(result).toBe('refreshed');
      expect(refreshLayers.calls.argsFor(0)).toEqual([1]);
      expect(refreshLayers.calls.first().object).toBe(viewer);
      expect(refreshLayers.calls.argsFor(1)).toEqual([500]);
    });

    it('should turn off the jQuery animations SMK fades each new map in with, which SMK.INIT waits for', async () => {
      const g = globalThis as any;
      const original = g.jQuery;
      g.jQuery = { fx: { off: false } };
      try {
        await service.patch();

        expect(g.jQuery.fx.off).toBeTrue();
      } finally {
        g.jQuery = original;
      }
    });
  });

  describe('detachSMK and reattachSMK', () => {
    let frame: HTMLElement;
    let smk: any;

    const newContainer = () => {
      const div = document.createElement('div');
      div.id = 'map';
      document.body.appendChild(div);
      return div;
    };

    beforeEach(async () => {
      spyOn(service, 'patch').and.returnValue(Promise.resolve());
      smk = { destroy: jasmine.createSpy('destroy'), $viewer: { map: { invalidateSize: jasmine.createSpy('invalidateSize') } } };
      mockSMK.INIT.and.returnValue(Promise.resolve(smk));
      frame = newContainer();
      frame.style.width = '300px';
      frame.style.height = '200px';
      await service.createSMK({ containerSel: frame, config: [] });
    });

    afterEach(() => {
      frame.remove();
      document.querySelectorAll('#map').forEach(e => e.remove());
      [...document.body.children].forEach(e => (e as HTMLElement).style?.left === '-10000px' && e.remove());
    });

    it('should park the map off screen at its size, then put it back in place of the next page\'s container', () => {
      service.detachSMK();

      expect(frame.parentElement!.style.left).toBe('-10000px');
      expect(frame.id).toBe('');
      expect(frame.style.width).toBe('300px');

      const next = newContainer();
      const reused = service.reattachSMK(next);

      expect(reused).toBe(smk);
      expect(next.isConnected).toBeFalse();
      expect(frame.parentElement).toBe(document.body);
      expect(frame.id).toBe('map');
      expect(frame.style.width).toBe('');
      expect(smk.$viewer.map.invalidateSize).toHaveBeenCalled();
      expect(smk.destroy).not.toHaveBeenCalled();
    });

    it('should have nothing to reattach before a map has been parked', () => {
      expect(service.reattachSMK(newContainer())).toBeNull();
    });

    it('should build a new map rather than reuse one whose layers carry an old auth token', () => {
      service.detachSMK();
      (TestBed.inject(TokenService).getOauthToken as jasmine.Spy).and.returnValue('NEW_TOKEN');

      expect(service.reattachSMK(newContainer())).toBeNull();
      expect(smk.destroy).toHaveBeenCalled();
      expect(frame.isConnected).toBeFalse();
      expect(service.getSMKInstance()).toBeNull();
    });
  });

  describe('clone', () => {
    it('should create deep copy of object', () => {
      const original = {
        nested: {
          value: 42
        }
      };
      const cloned = service.clone(original);

      expect(cloned).toEqual(original);
      expect(cloned).not.toBe(original);
      expect(cloned.nested).not.toBe(original.nested);
    });
  });

  describe('clearSMKInstance', () => {
    it('should clear the smkInstance by setting it to null', () => {
      (service as any).smkInstance = { some: 'value' };
      expect(service.getSMKInstance()).not.toBeNull();
      service.clearSMKInstance();
      expect(service.getSMKInstance()).toBeNull();
    });
  });

  describe('current fire year wildfire layers', () => {
    let originalFetch: any;
    let createObjUrlSpy: jasmine.Spy;
    let blobs: Blob[];
    let originalCreate: jasmine.Spy;
    let VectorLeaflet: any;

    const featureCollection = {
      type: 'FeatureCollection',
      features: [
        { properties: { FIRE_YEAR: 2024 }, geometry: null },
        { properties: { FIRE_YEAR: 2023 }, geometry: null },
        { properties: { fire_year: '2024' }, geometry: null },
      ],
    };

    beforeEach(() => {
      // Fix current fire year to a known value
      spyOn(service, 'getCurrentFireYear').and.returnValue(2024);

      blobs = [];
      createObjUrlSpy = spyOn(URL, 'createObjectURL').and.callFake((blob: Blob) => {
        blobs.push(blob);
        return 'blob:test-url';
      });

      originalFetch = (window as any).fetch;
      (window as any).fetch = jasmine.createSpy('fetch').and.callFake(() =>
        Promise.resolve(new Response(JSON.stringify(featureCollection), { status: 200 }))
      );

      originalCreate = jasmine.createSpy('create').and.returnValue(Promise.resolve('leaflet layer'));
      VectorLeaflet = { create: originalCreate };
      service.installCurrentFireYearPatch({ TYPE: { Layer: { vector: { leaflet: VectorLeaflet } } } });
    });

    afterEach(() => {
      (window as any).fetch = originalFetch;
    });

    it("keeps only the current fire year's features, fetched with the layer's headers", async () => {
      const url = await service.currentFireYearDataUrl('/api/wf/ooc', { apikey: 'KEY' });

      expect(url).toBe('blob:test-url');
      expect((window as any).fetch).toHaveBeenCalledOnceWith('/api/wf/ooc', { headers: { apikey: 'KEY' } });
      const kept = JSON.parse(await blobs[0].text());
      expect(kept.features).toEqual([featureCollection.features[0], featureCollection.features[2]]);
    });

    it('keeps the original URL, and so every fire, when the data cannot be read', async () => {
      (window as any).fetch.and.returnValue(Promise.resolve(new Response(null, { status: 403 })));

      await expectAsync(service.currentFireYearDataUrl('/api/wf/uc')).toBeResolvedTo('/api/wf/uc');
      expect(createObjUrlSpy).not.toHaveBeenCalled();
    });

    it("filters a wildfire layer's data as SMK creates the layer, when it is first shown", async () => {
      const layer = { config: { id: 'active-wildfires-holding', currentFireYearOnly: true, dataUrl: '/api/wf/hold', header: { apikey: 'KEY' } } };

      const created = await VectorLeaflet.create.call('viewer', [layer], 3);

      expect(created).toBe('leaflet layer');
      expect(layer.config.dataUrl).toBe('blob:test-url');
      expect(originalCreate).toHaveBeenCalledOnceWith([layer], 3);
      expect(originalCreate.calls.mostRecent().object).toBe('viewer');
    });

    it('leaves other vector layers alone', async () => {
      const layer = { config: { id: 'some-other-layer', dataUrl: '/api/other' } };

      await VectorLeaflet.create([layer]);

      expect(layer.config.dataUrl).toBe('/api/other');
      expect((window as any).fetch).not.toHaveBeenCalled();
      expect(originalCreate).toHaveBeenCalled();
    });

    it('fetches the data only once, however often the layer is created', async () => {
      const layer = { config: { currentFireYearOnly: true, dataUrl: '/api/wf/out' } };

      await VectorLeaflet.create([layer]);
      await VectorLeaflet.create([layer]);

      expect((window as any).fetch).toHaveBeenCalledTimes(1);
    });

    it('patches SMK only once', async () => {
      const patched = VectorLeaflet.create;
      service.installCurrentFireYearPatch({ TYPE: { Layer: { vector: { leaflet: VectorLeaflet } } } });

      expect(VectorLeaflet.create).toBe(patched);
    });
  });

  describe('vector basemap pane', () => {
    let VectorTileLayer: any;
    let originalOnAdd: jasmine.Spy;
    let panes: Record<string, any>;
    let map: any;

    beforeEach(() => {
      originalOnAdd = jasmine.createSpy('onAdd').and.returnValue('added');
      VectorTileLayer = { prototype: { onAdd: originalOnAdd }, mergeOptions: jasmine.createSpy('mergeOptions') };
      panes = {};
      map = {
        getPane: (name: string) => panes[name],
        createPane: jasmine.createSpy('createPane').and.callFake((name: string) => (panes[name] = { style: {} })),
      };
      service.installBasemapPanePatch({ esri: { Vector: { VectorTileLayer } } });
    });

    it('draws vector basemaps in their own pane, between the raster tiles and the map layers', () => {
      const layer = {};
      const added = VectorTileLayer.prototype.onAdd.call(layer, map);

      expect(VectorTileLayer.mergeOptions).toHaveBeenCalledOnceWith({ pane: 'wf-basemap-vector' });
      expect(panes['wf-basemap-vector'].style).toEqual({ zIndex: '250', pointerEvents: 'none' });
      expect(added).toBe('added');
      expect(originalOnAdd).toHaveBeenCalledOnceWith(map);
      expect(originalOnAdd.calls.mostRecent().object).toBe(layer);
    });

    it('creates the pane once per map', () => {
      VectorTileLayer.prototype.onAdd.call({}, map);
      VectorTileLayer.prototype.onAdd.call({}, map);

      expect(map.createPane).toHaveBeenCalledTimes(1);
    });

    it('patches the layer only once', () => {
      const patched = VectorTileLayer.prototype.onAdd;
      service.installBasemapPanePatch({ esri: { Vector: { VectorTileLayer } } });

      expect(VectorTileLayer.prototype.onAdd).toBe(patched);
      expect(VectorTileLayer.mergeOptions).toHaveBeenCalledTimes(1);
    });

    it('does nothing without esri-leaflet-vector', () => {
      expect(() => service.installBasemapPanePatch({})).not.toThrow();
    });
  });

  describe('addLayersToExistingSMKInstance', () => {
    it('registers the layers and shows Regions, leaving SMK to create each layer when it is first shown', async () => {
      const displayContext = {
        changedVisibility: jasmine.createSpy('changedVisibility'),
        setItemVisible: jasmine.createSpy('setItemVisible'),
      };
      mockSMK.TYPE.LayerDisplayContext = jasmine.createSpy('LayerDisplayContext').and.returnValue(displayContext);
      const viewer = {
        addLayer: jasmine.createSpy('addLayer'),
        createViewerLayer: jasmine.createSpy('createViewerLayer'),
        updateLayersVisible: jasmine.createSpy('updateLayersVisible').and.returnValue(Promise.resolve()),
        layerId: {},
        displayContext: {} as any,
      };
      (service as any).smkInstance = { $viewer: viewer };
      const layers = [{ id: 'ministry-of-forests-regions' }, { id: 'active-wildfires-holding', type: 'vector' }];

      await service.addLayersToExistingSMKInstance({ layers });

      expect(viewer.addLayer).toHaveBeenCalledTimes(2);
      expect(displayContext.setItemVisible).toHaveBeenCalledWith('ministry-of-forests-regions', true);
      expect(displayContext.setItemVisible).toHaveBeenCalledWith('active-wildfires-holding', false);
      expect(viewer.updateLayersVisible).toHaveBeenCalled();
      expect(viewer.createViewerLayer).not.toHaveBeenCalled();
    });
  });

  describe('installAuthenticatedLegendPatch', () => {
    let patchFn: (SMK: any) => void;
    let originalFetch: any;
    let originalFileReader: any;
    let originalImage: any;

    beforeEach(() => {
      // Resolve the patch function now that `service` is initialized by outer beforeEach
      patchFn =
        ((service as any)?.installAuthenticatedLegendPatch as ((SMK: any) => void)) ||
        ((window as any).installAuthenticatedLegendPatch as ((SMK: any) => void));

      // Minimal SMK scaffold
      (window as any).SMK = (window as any).SMK || {};
      (window as any).SMK.TYPE = {
        Layer: {
          wms: {
            leaflet: function () { },
          },
        },
      };

      // Prototype we’ll patch
      (window as any).SMK.TYPE.Layer.wms.leaflet.prototype = {
        initLegends: jasmine.createSpy('initLegends').and.returnValue(Promise.resolve([{ url: 'orig.png' }])),
        config: {
          serviceUrl: 'https://wms.example.com/wms',
          layerName: 'My:Layer',
          styleName: 'default',
          header: { Authorization: 'Bearer X' },
          legend: { someOpt: true },
        },
      };

      // Mock fetch -> tiny "png" blob
      originalFetch = (window as any).fetch;
      (window as any).fetch = jasmine.createSpy('fetch').and.callFake((_url: string) => {
        const bytes = new Uint8Array([137, 80, 78, 71]);
        const blob = new Blob([bytes], { type: 'image/png' });
        return Promise.resolve(new Response(blob, { status: 200 }));
      });

      // Mock FileReader -> immediate onload with data URL
      originalFileReader = (window as any).FileReader;
      (window as any).FileReader = function () { } as any;
      (window as any).FileReader.prototype.readAsDataURL = function (_blob: Blob) {
        setTimeout(() => this.onload && this.onload({} as any), 0);
      };
      Object.defineProperty((window as any).FileReader.prototype, 'result', {
        get: () => 'data:image/png;base64,AAA',
      });

      // Mock Image -> immediate onload with dimensions
      originalImage = (window as any).Image;
      (window as any).Image = function () {
        return {
          set src(_v: string) {
            setTimeout(() => this.onload && this.onload({} as any), 0);
          },
          onload: null as any,
          onerror: null as any,
          width: 16,
          height: 10,
        };
      } as any;
    });

    afterEach(() => {
      (window as any).fetch = originalFetch;
      (window as any).FileReader = originalFileReader;
      (window as any).Image = originalImage;
    });

    it('patches WMS legend fetch to include headers and return inline legend', async () => {
      expect(typeof patchFn).toBe('function');

      // Apply the patch
      patchFn((window as any).SMK);

      const proto = (window as any).SMK.TYPE.Layer.wms.leaflet.prototype;

      // Guard flag set
      expect((proto as any).__authLegendPatched).toBeTrue();

      // Call patched init
      const legends = await proto.initLegends();
      expect(Array.isArray(legends)).toBeTrue();
      expect(legends[0].url).toBe('data:image/png;base64,AAA');
      expect(legends[0].width).toBe(16);
      expect(legends[0].height).toBe(10);

      // Fetch used with auth headers
      expect((window as any).fetch).toHaveBeenCalled();
      const [, opts] = (window as any).fetch.calls.mostRecent().args;
      expect(opts.headers).toEqual({ Authorization: 'Bearer X' });
    });

    it('falls back to original initLegends on HTTP error', async () => {
      (window as any).fetch = jasmine
        .createSpy('fetch')
        .and.returnValue(Promise.resolve(new Response(null, { status: 500 })));

      patchFn((window as any).SMK);

      const proto = (window as any).SMK.TYPE.Layer.wms.leaflet.prototype;
      const legends = await proto.initLegends();

      expect(legends).toEqual([{ url: 'orig.png' }]); // fallback path
    });
  });

  describe('makeOnlyRegionsVisible', () => {
    // Loose node type just for tests so TS accepts visible/isVisible on literals
    type LayerNode = {
      id?: string;
      visible?: boolean;
      isVisible?: boolean;
      layers?: LayerNode[];
      entries?: LayerNode[];
    };

    it('hides all layers except ministry-of-forests-regions', () => {
      const option: {
        layers: LayerNode[];
        config: { layers?: LayerNode[]; entries?: LayerNode[] }[];
      } = {
        layers: [
          { id: 'ministry-of-forests-regions' },
          { id: 'ministry-of-forests-districts' },
        ],
        config: [
          {
            layers: [
              { id: 'wildfire-org-unit-fire-centre' },
              { id: 'fire-perimeters' },
            ],
            entries: [
              { id: 'active-wildfires-out-of-control' },
              { id: 'active-wildfires-holding' },
            ],
          },
        ],
      };

      (service as any).makeOnlyRegionsVisible(option);

      // top-level layers
      expect(option.layers[0]!.visible).toBeTrue();
      expect(option.layers[0]!.isVisible).toBeTrue();
      expect(option.layers[1]!.visible).toBeFalse();
      expect(option.layers[1]!.isVisible).toBeFalse();

      // nested config layers
      expect(option.config[0].layers![0]!.visible).toBeFalse();
      expect(option.config[0].layers![0]!.isVisible).toBeFalse();
      expect(option.config[0].layers![1]!.visible).toBeFalse();

      // entries
      expect(option.config[0].entries![0]!.visible).toBeFalse();
      expect(option.config[0].entries![1]!.visible).toBeFalse();
    });

    it('handles nested groups by hiding the group and applying rules to children', () => {
      const option: { config: { layers: LayerNode[] }[] } = {
        config: [
          {
            layers: [
              {
                // group/folder
                layers: [
                  { id: 'ministry-of-forests-regions' },
                  { id: 'active-wildfires-out-of-control' },
                ],
              },
            ],
          },
        ],
      };

      (service as any).makeOnlyRegionsVisible(option);

      const group = option.config[0].layers[0] as LayerNode;
      expect(group.visible).toBeFalse();
      expect(group.isVisible).toBeFalse();

      // children processed
      expect(group.layers![0]!.visible).toBeTrue();   // regions shown
      expect(group.layers![0]!.isVisible).toBeTrue();
      expect(group.layers![1]!.visible).toBeFalse();  // others hidden
      expect(group.layers![1]!.isVisible).toBeFalse();
    });

    it('is a no-op when there are no layers/entries in option', () => {
      const option: { layers?: LayerNode[]; config?: { layers?: LayerNode[]; entries?: LayerNode[] }[] } = {};
      (service as any).makeOnlyRegionsVisible(option);
      expect(option.layers).toBeUndefined();
      expect(option.config).toBeUndefined();
    });
  });

  it('should register the full-extent handler and call fitBounds when triggered', async () => {
    const fitBounds = jasmine.createSpy('fitBounds');
    const viewerMock = { map: { fitBounds } };
    const smkMock = { $viewer: viewerMock };

    await service.patch();

    expect(mockSMK.HANDLER.set).toHaveBeenCalledWith(
      'BespokeTool--full-extent',
      'triggered',
      jasmine.any(Function)
    );

    const handlerFn = mockSMK.HANDLER.set.calls.mostRecent().args[2];

    handlerFn(smkMock, {});

    expect(fitBounds).toHaveBeenCalledWith(BC_BOUNDS, { animate: true });
  });

  describe('destroySMK', () => {
    let errSpy: jasmine.Spy;

    beforeEach(() => {
      errSpy = spyOn(console, 'error'); // keep logs quiet in tests
    });

    it('returns immediately when there is no smkInstance', async () => {
      (service as any).smkInstance = null;
      const clearSpy = spyOn(service, 'clearSMKInstance').and.callThrough();

      await service.destroySMK();

      expect(clearSpy).not.toHaveBeenCalled();
      expect(errSpy).not.toHaveBeenCalled();
    });

    it('calls destroy() when present and clears the instance', async () => {
      const destroy = jasmine.createSpy('destroy');
      (service as any).smkInstance = { destroy };
      const clearSpy = spyOn(service, 'clearSMKInstance').and.callThrough();

      await service.destroySMK();

      expect(destroy).toHaveBeenCalled();
      expect(clearSpy).toHaveBeenCalled();
      expect(service.getSMKInstance()).toBeNull();
    });

    it('is safe when destroy() is missing (still clears)', async () => {
      (service as any).smkInstance = { foo: 'bar' };
      const clearSpy = spyOn(service, 'clearSMKInstance').and.callThrough();

      await service.destroySMK();

      expect(clearSpy).toHaveBeenCalled();
      expect(service.getSMKInstance()).toBeNull();
    });

    it('catches errors from destroy() but still clears and scrubs container', async () => {
      const destroy = jasmine.createSpy('destroy').and.callFake(() => { throw new Error('boom'); });
      (service as any).smkInstance = { destroy };

      // Arrange: a container to scrub
      const el = document.createElement('div');
      el.id = 'map';
      el.appendChild(document.createElement('span')); // something to prove it gets replaced
      document.body.appendChild(el);
      service.setContainerId('map');

      const clearSpy = spyOn(service, 'clearSMKInstance').and.callThrough();

      // Act
      await service.destroySMK();

      // Assert
      expect(destroy).toHaveBeenCalled();                // attempted
      expect(errSpy).toHaveBeenCalled();                 // error logged
      expect(clearSpy).toHaveBeenCalled();               // cleared anyway
      expect(service.getSMKInstance()).toBeNull();       // instance gone

      // The node should be replaced (no child span now)
      const after = document.getElementById('map')!;
      expect(after).toBeTruthy();
      expect(after.children.length).toBe(0);
    });

    it('scrubs the container element when present', async () => {
      const destroy = jasmine.createSpy('destroy');
      (service as any).smkInstance = { destroy };

      const el = document.createElement('div');
      el.id = 'map';
      el.appendChild(document.createElement('span'));
      document.body.appendChild(el);
      service.setContainerId('map');

      await service.destroySMK();

      const after = document.getElementById('map')!;
      expect(after).toBeTruthy();
      expect(after.children.length).toBe(0);
    });

    it('does not throw if a containerId is set but element is missing', async () => {
      const destroy = jasmine.createSpy('destroy');
      (service as any).smkInstance = { destroy };

      service.setContainerId('no-such-element'); // element not in DOM

      await service.destroySMK();

      // No uncaught exception
      expect(destroy).toHaveBeenCalled();
      expect(service.getSMKInstance()).toBeNull();
    });
  });

  describe('createProjectBoundaryLayer', () => {
    let mapMock: any;
    let paneStore: Record<string, any>;

    beforeEach(() => {
      paneStore = {};
      mapMock = {
        getPane: jasmine.createSpy('getPane').and.callFake((name: string) => paneStore[name] || null),
        createPane: jasmine.createSpy('createPane').and.callFake((name: string) => {
          paneStore[name] = { style: {} };
          return paneStore[name];
        })
      };
    });

    it('ensures pane and uses z-index 401', () => {
      service.createProjectBoundaryLayer(mapMock, { programAreaGuids: ['g1'] });

      expect(mapMock.getPane).toHaveBeenCalledWith('pane-project-boundary-gl');
      expect(mapMock.createPane).toHaveBeenCalledWith('pane-project-boundary-gl');
      expect(paneStore['pane-project-boundary-gl'].style.zIndex).toBe('401');
    });

    it('calls L.maplibreGL with proper style, tiles and minzoom', () => {
      const layer = service.createProjectBoundaryLayer(mapMock, { programAreaGuids: ['g1', 'g2'] });
      const args = (layer as any).__opts;

      expect(args.pane).toBe('pane-project-boundary-gl');
      const tiles: string[] = args.style.sources.projectBoundary.tiles;
      expect(tiles.length).toBe(1);
      expect(tiles[0]).toContain('/tiles/project_boundary/{z}/{x}/{y}.mvt');
      expect(tiles[0]).toContain('programAreaGuids=g1');
      expect(tiles[0]).toContain('programAreaGuids=g2');
      const ids = args.style.layers.map((l: any) => l.id);
      expect(ids).toContain('project-boundary-fill');
      expect(ids).toContain('project-boundary-line');
      expect((layer as any).__opts).toBeDefined();
    });

    it('transformRequest attaches Authorization ONLY for API base URL', () => {
      const layer = service.createProjectBoundaryLayer(mapMock, { programAreaGuids: ['g1'] });
      const opts = (layer as any).__opts;
      const tr = opts.transformRequest as (u: string) => any;

      const apiUrl = 'http://localhost:9876/wfprev-api/tiles/project_boundary/1/2/3.mvt';
      const res1 = tr(apiUrl);
      expect(res1.url).toBe(apiUrl);
      expect(res1.headers).toEqual({ Authorization: 'Bearer TEST_TOKEN' });

      const otherUrl = 'https://example.com/some.json';
      const res2 = tr(otherUrl);
      expect(res2.url).toBe(otherUrl);
      expect(res2.headers).toBeUndefined();
    });
  });

  describe('createActivityBoundaryLayer', () => {
    let mapMock: any;
    let paneStore: Record<string, any>;

    beforeEach(() => {
      paneStore = {};
      mapMock = {
        getPane: jasmine.createSpy('getPane').and.callFake((name: string) => paneStore[name] || null),
        createPane: jasmine.createSpy('createPane').and.callFake((name: string) => {
          paneStore[name] = { style: {} };
          return paneStore[name];
        })
      };
    });

    it('ensures pane and uses z-index 400', () => {
      service.createActivityBoundaryLayer(mapMock, { programAreaGuids: ['g1'] }, 2025);

      expect(mapMock.getPane).toHaveBeenCalledWith('pane-activity-boundary-gl');
      expect(mapMock.createPane).toHaveBeenCalledWith('pane-activity-boundary-gl');
      expect(paneStore['pane-activity-boundary-gl'].style.zIndex).toBe('400');
    });

    it('calls L.maplibreGL with proper style, tiles and minzoom', () => {
      const layer = service.createActivityBoundaryLayer(mapMock, { programAreaGuids: ['g1', 'g2'] }, 2026);
      const args = (layer as any).__opts;

      // pane
      expect(args.pane).toBe('pane-activity-boundary-gl');

      // tiles url with query string in the source
      const tiles: string[] = args.style.sources.activityBoundary.tiles;
      expect(tiles.length).toBe(1);
      expect(tiles[0]).toContain('/tiles/activity_boundary/{z}/{x}/{y}.mvt');
      expect(tiles[0]).toContain('programAreaGuids=g1');
      expect(tiles[0]).toContain('programAreaGuids=g2');

      // layers present
      const ids = args.style.layers.map((l: any) => l.id);
      expect(ids).toContain('activity-boundary-fill');
      expect(ids).toContain('activity-boundary-line');
    });

    it('line layer uses fiscal-year based color expression with provided currentFiscalYear', () => {
      const currentFY = 2024;
      const layer = service.createActivityBoundaryLayer(mapMock, { programAreaGuids: ['g1'] }, currentFY);
      const args = (layer as any).__opts;

      const line = args.style.layers.find((l: any) => l.id === 'activity-boundary-line');
      expect(line).toBeTruthy();

      const paint = line.paint || {};
      expect(paint['line-width']).toBe(2);

      const expr = paint['line-color'];
      expect(Array.isArray(expr)).toBeTrue();
      expect(expr[0]).toBe('case');

      // First condition should be a '<' compare to currentFY
      const cond1 = expr[1];
      expect(Array.isArray(cond1)).toBeTrue();
      expect(cond1[0]).toBe('<');
      expect(cond1[1][0]).toBe('to-number');
      expect(cond1[1][1][0]).toBe('get');
      expect(cond1[1][1][1]).toBe('fiscal_year');
      expect(cond1[2]).toBe(currentFY);

      // Second condition should be a '>' compare to currentFY
      const cond2 = expr[3];
      expect(Array.isArray(cond2)).toBeTrue();
      expect(cond2[0]).toBe('>');
      expect(cond2[1][0]).toBe('to-number');
      expect(cond2[1][1][0]).toBe('get');
      expect(cond2[1][1][1]).toBe('fiscal_year');
      expect(cond2[2]).toBe(currentFY);

      expect(expr.length).toBe(6);
    });

    it('transformRequest attaches Authorization ONLY for API base URL', () => {
      const layer = service.createActivityBoundaryLayer(mapMock, { programAreaGuids: ['g1'] }, 2025);
      const opts = (layer as any).__opts;
      const tr = opts.transformRequest as (u: string) => any;

      // API URL → includes headers
      const apiUrl = 'http://localhost:9876/wfprev-api/tiles/activity_boundary/1/2/3.mvt';
      const res1 = tr(apiUrl);
      expect(res1.url).toBe(apiUrl);
      expect(res1.headers).toEqual({ Authorization: 'Bearer TEST_TOKEN' });

      // Non-API URL → no headers
      const otherUrl = 'https://example.com/other.json';
      const res2 = tr(otherUrl);
      expect(res2.url).toBe(otherUrl);
      expect(res2.headers).toBeUndefined();
    });
  });


});