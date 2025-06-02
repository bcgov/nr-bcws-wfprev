import { TestBed } from '@angular/core/testing';
import { MapService } from './map.service';

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
          tools: [{
            type: 'baseMaps'
          }]
        }]
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
            tools: [{
              type: 'baseMaps'
            }]
          }
        ]
      });
    });

    it('should throw error if config is not an array', async () => {
      const option = {
        config: 'not an array'
      };

      await expectAsync(service.createSMK(option)).toBeRejected();
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

});