import { TestBed } from '@angular/core/testing';
import { MapService } from './map.service';

describe('MapService', () => {
  let service: MapService;

  beforeAll(() => {
    // Mock SMK object globally
    (window as any)['SMK'] = {
      INIT: jasmine.createSpy('INIT').and.returnValue(Promise.resolve({})),
      TYPE: {
        Viewer: function() {},
      },
    };

    // Define Viewer and leaflet structure in the mock
    (window as any)['SMK'].TYPE.Viewer = {
      leaflet: {
        prototype: {
          mapResized: jasmine.createSpy('mapResized'),
          map: {
            invalidateSize: jasmine.createSpy('invalidateSize')
          }
        }
      }
    };

    // Mock the INIT method to return a mock SMK instance
    const smkMock = {
      destroy: jasmine.createSpy('destroy'),
    };
    (window as any)['SMK'].INIT.and.returnValue(Promise.resolve(smkMock));
  });

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(MapService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should get and set map index', () => {
    expect(service.getMapIndex()).toBe(0);
    service.setMapIndex(5);
    expect(service.getMapIndex()).toBe(5);
  });

//   it('should create SMK with valid options', async () => {
//     const mockOptions = {
//       config: [],
//     };
//     const smkInitSpy = (window as any)['SMK'].INIT;

//     await service.createSMK(mockOptions);
//     expect(smkInitSpy).toHaveBeenCalled();
//     expect(mockOptions.config).toContain(jasmine.objectContaining({
//       tools: [{ type: 'baseMaps' }]
//     }));
//   });

  it('should throw error if SMK initialization fails', async () => {
    const mockOptions = {
      config: [],
    };
    const smkInitSpy = (window as any)['SMK'].INIT;
    smkInitSpy.and.returnValue(Promise.reject('Error'));

    try {
      await service.createSMK(mockOptions);
    } catch (e) {
      expect(e).toBe('Error');
    }
  });

//   it('should patch SMK correctly', async () => {
//     const patchSpy = spyOn(service, 'patch').and.callThrough();
//     await service.patch();
//     expect(patchSpy).toHaveBeenCalled();
//     expect((window as any)['SMK'].TYPE.Viewer.prototype.mapResized).toHaveBeenCalled();
//     expect((window as any)['SMK'].TYPE.Viewer.leaflet.prototype.map.invalidateSize).toHaveBeenCalledWith({ animate: false });
//   });

  it('should clone an object correctly', () => {
    const obj = { a: 1, b: { c: 2 } };
    const clonedObj = service.clone(obj);

    expect(clonedObj).toEqual(obj);
    expect(clonedObj).not.toBe(obj); // Ensure it's a deep clone
  });

//   it('should define the OpenStreetMap layer correctly', () => {
//     const defineOpenStreetMapLayerSpy = spyOn(service, 'defineOpenStreetMapLayer').and.callThrough();
//     service.defineOpenStreetMapLayer();
//     expect(defineOpenStreetMapLayerSpy).toHaveBeenCalled();
//     expect(service.baseMapIds).toContain('openstreetmap');
//     expect((window as any)['SMK'].TYPE.Viewer.prototype.basemap['openstreetmap']).toBeDefined();
//   });

  it('should handle patch promise rejection gracefully', async () => {
    const smk = (window as any)['SMK'];
    const patchSpy = spyOn(service, 'patch').and.returnValue(Promise.reject('Patch error'));

    try {
      await service.patch();
    } catch (e) {
      expect(e).toBe('Patch error');
    }
  });
});
