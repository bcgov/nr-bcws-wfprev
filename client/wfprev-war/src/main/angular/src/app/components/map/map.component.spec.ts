
import { ComponentFixture, TestBed, fakeAsync, flush, tick } from '@angular/core/testing';
import { MapComponent } from './map.component';
import { MapConfigService } from 'src/app/services/map-config.service';
import { MapService } from 'src/app/services/map.service';
import { ResizablePanelComponent } from 'src/app/components/resizable-panel/resizable-panel.component';
import { of } from 'rxjs';
import { ElementRef } from '@angular/core';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { AppConfigService } from 'src/app/services/app-config.service';
import { CodeTableServices } from 'src/app/services/code-table-services';
import { ProjectService } from 'src/app/services/project-services';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { ActivatedRoute } from '@angular/router';
import * as L from 'leaflet';

class MockAppConfigService {
  getConfig() {
    return {
      application: {
        baseUrl: 'http://localhost',
        lazyAuthenticate: false,
        enableLocalStorageToken: false,
      },
    };
  }
  loadAppConfig() {
    return Promise.resolve(true);
  }
}

class MockCodeTableServices {
  fetchCodeTable() {
    return of({ wfprev: 'mockedData' });
  }
  fetchFireCentres() {
    return of({ _embedded: { fireCentre: [] } });
  }
  fetchForestRegions() {
    return of({ _embedded: { forestRegion: [] } });
  }
  fetchFireZones() {
    return of({ _embedded: { fireZone: [] } });
  }
  fetchForestDistricts() {
    return of({ _embedded: { forestDistrict: [] } });
  }
  fetchBCParksRegions() {
    return of({ _embedded: { bcParksRegion: [] } });
  }
  fetchBCParksSections() {
    return of({ _embedded: { bcParksSection: [] } });
  }
  fetchActivityCategories() {
    return of({ _embedded: { activityCategory: [] } });
  }
  fetchPlanFiscalStatuses() {
    return of({ _embedded: { planFiscalStatus: [] } });
  }
}


class MockProjectService {
  fetchProjects() {
    return of({ wfprev: 'mockedData' });
  }
  getFeatures() {
    return of({ projects: [] });
  }
  getProjectFiscalsByProjectGuid() {
    return of({ _embedded: { projectFiscals: [] } });
  }
  getProjectByProjectGuid() {
    return of({ projectGuid: 'test-guid' });
  }
}

describe('MapComponent', () => {
  let component: MapComponent;
  let fixture: ComponentFixture<MapComponent>;
  let mapConfigServiceMock: jasmine.SpyObj<MapConfigService>;
  let mapServiceMock: jasmine.SpyObj<MapService>;
  let mapContainer: jasmine.SpyObj<ElementRef>;

  const createMockSMKInstance = () => ({
    $viewer: {
      map: {
        addLayer: jasmine.createSpy('addLayer'),
        on: jasmine.createSpy('on'),
        controls: {
          bottomleft: {
            addTo: jasmine.createSpy('addTo'),
          },
        },
      }
    }
  });

  beforeEach(() => {
    (L as any).markerClusterGroup = () => ({
      addLayer: jasmine.createSpy('addLayer'),
      clearLayers: jasmine.createSpy('clearLayers'),
    });
    spyOn(L.Control.prototype, 'addTo').and.callFake(function (this: any) {
      return this;
    });

    mapConfigServiceMock = jasmine.createSpyObj<MapConfigService>('MapConfigService', ['getMapConfig']);
    mapServiceMock = jasmine.createSpyObj<MapService>('MapService', ['getMapIndex', 'setMapIndex', 'createSMK', 'getSMKInstance']);
    mapContainer = jasmine.createSpyObj('ElementRef', ['nativeElement']);

    mapConfigServiceMock.getMapConfig.and.returnValue(Promise.resolve({ theme: 'testTheme' }));
    mapServiceMock.getSMKInstance.and.returnValue(createMockSMKInstance());

    TestBed.configureTestingModule({
      imports: [
        MapComponent,
        ResizablePanelComponent,
        HttpClientTestingModule,
        BrowserAnimationsModule,
      ],
      providers: [
        { provide: MapConfigService, useValue: mapConfigServiceMock },
        { provide: MapService, useValue: mapServiceMock },
        { provide: AppConfigService, useClass: MockAppConfigService },
        { provide: CodeTableServices, useClass: MockCodeTableServices },
        { provide: ProjectService, useClass: MockProjectService },
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: {
              queryParamMap: {
                get: (key: string) => {
                  if (key === 'bbox') return '10,20,30,40';
                  return null;
                }
              }
            }
          }
        }
      ],
      schemas: [NO_ERRORS_SCHEMA],
    });

    fixture = TestBed.createComponent(MapComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();

    spyOn(console, 'error');
  });

  describe('ngAfterViewInit', () => {
    it('should initialize map if mapContainer is available', fakeAsync(() => {
      mapContainer.nativeElement = document.createElement('div');
      component.mapContainer = mapContainer;

      const mockConfig = { theme: 'testTheme' };
      mapConfigServiceMock.getMapConfig.and.returnValue(Promise.resolve(mockConfig));
      mapServiceMock.getMapIndex.and.returnValue(0);
      mapServiceMock.getSMKInstance.and.returnValue(createMockSMKInstance());

      component.ngAfterViewInit();

      tick();
      flush();

      expect(mapServiceMock.getMapIndex).toHaveBeenCalled();
      expect(mapServiceMock.setMapIndex).toHaveBeenCalledWith(1);
      expect(mapServiceMock.createSMK).toHaveBeenCalled();
    }));
  });


  describe('initMap', () => {
    it('should initialize map with correct config and device settings', fakeAsync(() => {
      mapContainer.nativeElement = document.createElement('div');
      component.mapContainer = mapContainer;

      const mockConfig = { theme: 'testTheme' }; // ✅ Same object
      mapConfigServiceMock.getMapConfig.and.returnValue(Promise.resolve(mockConfig));
      mapServiceMock.getMapIndex.and.returnValue(1);
      mapServiceMock.getSMKInstance.and.returnValue(createMockSMKInstance());

      component.ngAfterViewInit(); // ✅ Ensures initMap gets called

      tick();
      flush();

      expect(component.mapConfig).toEqual([
        mockConfig,
        { viewer: { device: 'desktop' } },
        'theme=wf',
        '?',
      ]);
      expect(mapServiceMock.createSMK).toHaveBeenCalled();
    }));
  });


  it('should handle errors while loading mapConfig', fakeAsync(() => {
    mapContainer.nativeElement = document.createElement('div');
    component.mapContainer = mapContainer;

    mapConfigServiceMock.getMapConfig.and.returnValue(Promise.reject('Config Load Error'));

    component.ngAfterViewInit();
    tick();
    flush();

    expect(console.error).toHaveBeenCalledWith('Error loading map:', 'Config Load Error');
  }));

  describe('clone', () => {
    it('should return a deep clone of an object', () => {
      const original = { prop: 'value' };
      const clone = component.clone(original);

      expect(clone).not.toBe(original);
      expect(clone.prop).toBe('value');
    });
  });

  describe('mapIndex and mapService', () => {
    it('should update mapIndex after getting map index', fakeAsync(() => {
      mapContainer.nativeElement = document.createElement('div');
      component.mapContainer = mapContainer;

      mapServiceMock.getMapIndex.and.returnValue(5);
      mapServiceMock.getSMKInstance.and.returnValue(createMockSMKInstance());

      component.ngAfterViewInit();
      tick();
      flush();

      expect(component.mapIndex).toBe(5);
      expect(mapServiceMock.setMapIndex).toHaveBeenCalledWith(6);
    }));
  });

  it('should exit early and log error if mapContainer is missing', () => {
    component.mapContainer = null as any;

    component.ngAfterViewInit();

    expect(console.error).toHaveBeenCalledWith('Map container is not available.');
  });

  it('should skip updateMarkers if map or markersClusterGroup is not ready', () => {
    component['markersClusterGroup'] = null as any;
    const consoleWarnSpy = spyOn(console, 'warn');

    component.updateMarkers([{ latitude: 50, longitude: -120 }]);

    expect(consoleWarnSpy).toHaveBeenCalledWith('[Map] Skipping updateMarkers — map or cluster group not ready');
  });


});
