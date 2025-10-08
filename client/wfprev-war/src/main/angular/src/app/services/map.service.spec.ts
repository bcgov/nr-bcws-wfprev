import { TestBed } from '@angular/core/testing';
import { MapService } from './map.service';
import { BC_BOUNDS } from 'src/app/utils/constants';

describe('MapService', () => {
  let service: MapService;
  let mockWindow: any;
  let mockSMK: any;
  let mockL: any;

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
          },
          prototype: {
            basemap: {}
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

    TestBed.configureTestingModule({
      providers: [MapService]
    });

    service = TestBed.inject(MapService);
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
            { type: 'baseMaps' },
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
              { type: 'baseMaps' },
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

  describe('patch', () => {
    let mockTemp: any;

    beforeEach(() => {
      mockTemp = {
        style: {},
        parentElement: {
          removeChild: jasmine.createSpy('removeChild')
        }
      };
      spyOn(document, 'createElement').and.returnValue(mockTemp);
      spyOn(document.body, 'appendChild');
    });

    it('should patch SMK successfully', async () => {
      await service.patch();

      // Verify temporary div creation and styling
      expect(document.createElement).toHaveBeenCalledWith('div');
      expect(mockTemp.style.display).toBe('none');
      expect(mockTemp.style.visibility).toBe('hidden');
      expect(document.body.appendChild).toHaveBeenCalledWith(mockTemp);

      // Verify SMK initialization with correct parameters
      expect(mockSMK.INIT).toHaveBeenCalledWith({
        id: 999,
        containerSel: mockTemp,
        baseUrl: `${window.location.protocol}//${window.location.host}/assets/smk/`,
        config: 'show-tool=bespoke'
      });

      // Verify cleanup
      expect(mockTemp.parentElement.removeChild).toHaveBeenCalledWith(mockTemp);
    });

    it('should define OpenStreetMap layer', async () => {
      await service.patch();

      expect(mockL.tileLayer).toHaveBeenCalledWith(
        'https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png',
        { maxZoom: 19 }
      );
      expect(service.baseMapIds).toContain('openstreetmap');
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

  describe('defineOpenStreetMapLayer', () => {
    it('should define OpenStreetMap layer correctly', () => {
      service.defineOpenStreetMapLayer();

      expect(mockL.tileLayer).toHaveBeenCalledWith(
        'https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png',
        { maxZoom: 19 }
      );
      expect(service.baseMapIds).toContain('openstreetmap');
      expect(mockSMK.TYPE.Viewer.prototype.basemap['openstreetmap']).toBeDefined();
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

  describe('filterWildfireLayersByCurrentYear', () => {
  const callFn = async (option: any) =>
    (service as any).filterWildfireLayersByCurrentYear(option);

  let originalFetch: any;
  let createObjUrlSpy: jasmine.Spy;

  beforeEach(() => {
    // Fix current fire year to a known value
    spyOn(service as any, 'getCurrentFireYear').and.returnValue(2024);

    // Stub URL.createObjectURL so we can assert deterministic values
    createObjUrlSpy = spyOn(URL, 'createObjectURL').and.callFake(() => 'blob://test-url');

    // Mock fetch
    originalFetch = (window as any).fetch;
    (window as any).fetch = jasmine
      .createSpy('fetch')
      .and.callFake((_url: string, _opts: any) =>
        Promise.resolve(new Response(
          JSON.stringify({
            type: 'FeatureCollection',
            features: [
              { properties: { FIRE_YEAR: 2024 }, geometry: null },
              { properties: { FIRE_YEAR: 2023 }, geometry: null },
              { properties: { fire_year: '2024' }, geometry: null },
            ],
          }),
          { status: 200, headers: { 'Content-Type': 'application/json' } }
        ))
      );
  });

  afterEach(() => {
    (window as any).fetch = originalFetch;
  });

  it('filters features by current fire year and swaps to a blob dataUrl', async () => {
    const option = {
      config: [{
        layers: [
          { id: 'active-wildfires-out-of-control', type: 'vector', dataUrl: '/api/wf/ooc', header: { Authorization: 'Bearer X' } },
          { id: 'active-wildfires-holding', type: 'vector', dataUrl: '/api/wf/hold', header: { Authorization: 'Bearer X' } },
          { id: 'some-other-layer', type: 'vector', dataUrl: '/api/other' },
        ],
      }],
    };

    await callFn(option);

    const ooc = option.config[0].layers[0];
    const hold = option.config[0].layers[1];
    const other = option.config[0].layers[2];

    // Blob URL is applied to wildfire layers only
    expect(ooc.dataUrl).toBe('blob://test-url');
    expect(hold.dataUrl).toBe('blob://test-url');
    // non-target layer untouched
    expect(other.dataUrl).toBe('/api/other');

    // headers removed for blob URLs
    expect(ooc.header).toBeUndefined();
    expect(hold.header).toBeUndefined();

    // We created blob URLs
    expect(createObjUrlSpy).toHaveBeenCalled();
    // Fetch called twice (two target layers)
    expect((window as any).fetch).toHaveBeenCalledTimes(2);
  });

  it('leaves layer untouched when fetch fails', async () => {
    (window as any).fetch = jasmine
      .createSpy('fetch')
      .and.returnValue(Promise.resolve(new Response(null, { status: 403 })));

    const option = {
      config: [{
        layers: [{ id: 'active-wildfires-under-control', type: 'vector', dataUrl: '/api/wf/uc', header: { k: 'v' } }],
      }],
    };

    await callFn(option);

    const lyr = option.config[0].layers[0];
    expect(lyr.dataUrl).toBe('/api/wf/uc'); 
    expect(lyr.header).toEqual({ k: 'v' }); 
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
          leaflet: function () {},
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
    (window as any).FileReader = function () {} as any;
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

});