
import { ComponentFixture, TestBed, fakeAsync, flush, tick } from '@angular/core/testing';
import { MapComponent } from './map.component';
import { MapConfigService } from 'src/app/services/map-config.service';
import { MapService } from 'src/app/services/map.service';
import { of, Subject } from 'rxjs';
import { ElementRef } from '@angular/core';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { AppConfigService } from 'src/app/services/app-config.service';
import { CodeTableServices } from 'src/app/services/code-table-services';
import { ProjectService } from 'src/app/services/project-services';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { ActivatedRoute } from '@angular/router';
import * as L from 'leaflet';
import { Project } from 'src/app/components/models';
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

function createMockProject(overrides: Partial<Project> = {}): Project {
  return {
    bcParksRegionOrgUnitId: 1,
    bcParksSectionOrgUnitId: 1,
    closestCommunityName: 'Test Town',
    fireCentreOrgUnitId: 1,
    forestDistrictOrgUnitId: 1,
    forestRegionOrgUnitId: 1,
    isMultiFiscalYearProj: false,
    programAreaGuid: 'program-123',
    projectDescription: 'Test description',
    projectGuid: 'test-guid',
    projectLead: 'Jane Doe',
    projectLeadEmailAddress: 'jane@example.com',
    projectName: 'Test Project',
    projectNumber: 12345,
    siteUnitName: 'Site A',
    totalActualAmount: 1000,
    totalAllocatedAmount: 1000,
    totalFundingRequestAmount: 500,
    totalPlannedCostPerHectare: 100,
    totalPlannedProjectSizeHa: 10,
    ...overrides
  };
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
      removeLayer: jasmine.createSpy('removeLayer'),
      on: jasmine.createSpy('on'),
      getZoom: jasmine.createSpy('getZoom').and.returnValue(10),
      hasLayer: jasmine.createSpy('hasLayer').and.returnValue(false), 
      eachLayer: jasmine.createSpy('eachLayer'),
      getCenter: jasmine.createSpy('getCenter'),
      setView: jasmine.createSpy('setView'),
      controls: {
        bottomleft: {
          addTo: jasmine.createSpy('addTo'),
        },
      },
    },
  },
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
    mapServiceMock = jasmine.createSpyObj<MapService>('MapService', ['getMapIndex', 'setMapIndex', 'createSMK', 'getSMKInstance','clearSMKInstance']);
    mapContainer = jasmine.createSpyObj('ElementRef', ['nativeElement']);

    mapConfigServiceMock.getMapConfig.and.returnValue(Promise.resolve({ theme: 'testTheme' }));
    mapServiceMock.getSMKInstance.and.returnValue(createMockSMKInstance());

    TestBed.configureTestingModule({
      imports: [
        MapComponent,
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

      const mockConfig = { theme: 'testTheme' };
      mapConfigServiceMock.getMapConfig.and.returnValue(Promise.resolve(mockConfig));
      mapServiceMock.getMapIndex.and.returnValue(1);
      mapServiceMock.getSMKInstance.and.returnValue(createMockSMKInstance());

      component.ngAfterViewInit();

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

  describe('ngOnDestroy', () => {
  it('should destroy SMK instance and clear it from the map service', () => {
    const destroySpy = jasmine.createSpy('destroy');
    const smkMock = {
      destroy: destroySpy,
      $viewer: { map: {} }
    };

    mapServiceMock.getSMKInstance.and.returnValue(smkMock);
    mapServiceMock.clearSMKInstance = jasmine.createSpy('clearSMKInstance');

    component.ngOnDestroy();

    expect(destroySpy).toHaveBeenCalled();
    expect(mapServiceMock.clearSMKInstance).toHaveBeenCalled();
  });

  it('should only call clearSMKInstance if smk has no destroy method', () => {
    const smkMock = {
      $viewer: { map: {} }
    };

    mapServiceMock.getSMKInstance.and.returnValue(smkMock);
    mapServiceMock.clearSMKInstance = jasmine.createSpy('clearSMKInstance');

    component.ngOnDestroy();

    expect(mapServiceMock.clearSMKInstance).toHaveBeenCalled();
  });

  it('should do nothing if no smk instance exists', () => {
    mapServiceMock.getSMKInstance.and.returnValue(null);
    mapServiceMock.clearSMKInstance = jasmine.createSpy('clearSMKInstance');

    component.ngOnDestroy();

    expect(mapServiceMock.clearSMKInstance).toHaveBeenCalled();
  });
});

  describe('Marker popup behavior', () => {
    let mapMock: any;
    let markerSpy: jasmine.SpyObj<L.Marker>;
    let mockProject: any;

    beforeEach(() => {
      mapContainer.nativeElement = document.createElement('div');
      component.mapContainer = mapContainer;

      markerSpy = jasmine.createSpyObj<L.Marker>('Marker', [
        'bindPopup',
        'on',
        'setIcon',
        'closePopup',
        'openPopup',
        'off',
      ]);
      markerSpy.getLatLng = jasmine.createSpy().and.returnValue({
        lat: 50,
        lng: -120,
      });
      Object.setPrototypeOf(markerSpy, L.Marker.prototype);
      
      mapMock = {
        addLayer: jasmine.createSpy('addLayer'),
        invalidateSize: jasmine.createSpy('invalidateSize'),
        getZoom: jasmine.createSpy('getZoom').and.returnValue(10),
        hasLayer: jasmine.createSpy('hasLayer').and.returnValue(false),
        removeLayer: jasmine.createSpy('removeLayer'),
      };

      const mockClusterGroup = {
        getLayers: () => [markerSpy],
        zoomToShowLayer: (marker: L.Marker, cb: () => void) => cb(),
        addLayer: jasmine.createSpy('addLayer'),
        clearLayers: jasmine.createSpy('clearLayers'),
      };

      component['markersClusterGroup'] = mockClusterGroup as any;
      component['projectMarkerMap'] = new Map();

      spyOn(component['sharedService'], 'selectProject');

      mapServiceMock.getSMKInstance.and.returnValue({
        $viewer: { map: mapMock }
      });

      mockProject = {
        projectGuid: 'abc-123',
        latitude: 50,
        longitude: -120,
      };

    });

    it('should add marker and bind popup in updateMarkers()', () => {
      component.updateMarkers([mockProject]);

      expect(component['projectMarkerMap'].get('abc-123')).toBeDefined();
      expect(component['markersClusterGroup']?.addLayer).toHaveBeenCalled();
    });

    it('should open popup and set active marker icon in openPopupForProject()', () => {
      component['projectMarkerMap'].set(mockProject.projectGuid, markerSpy);
      
      component['activeMarker'] = null;

      component.openPopupForProject(mockProject);

      expect(markerSpy.setIcon).toHaveBeenCalledWith(
        jasmine.objectContaining({
          options: jasmine.objectContaining({
            iconUrl: '/assets/active-pin-drop.svg',
          }),
        })
      );
      expect(markerSpy.openPopup).toHaveBeenCalled();
      expect(component['activeMarker'] as unknown as L.Marker).toBe(markerSpy);
    });

    it('should close popup and reset icon in closePopupForProject()', () => {
      component['projectMarkerMap'].set(mockProject.projectGuid, markerSpy);
      component['activeMarker'] = markerSpy;

      component.closePopupForProject(mockProject);

      expect(markerSpy.closePopup).toHaveBeenCalled();
      expect(markerSpy.setIcon).toHaveBeenCalledWith(
        jasmine.objectContaining({
          options: jasmine.objectContaining({
            iconUrl: '/assets/blue-pin-drop.svg',
          }),
        })
      );
      expect(component['activeMarker']).toBeNull();
    });
  });
  
  describe('selectedProject$ behavior', () => {
    let selectedProjectSubject: Subject<any>;
    let closeSpy: jasmine.Spy;
    let openSpy: jasmine.Spy;

    beforeEach(() => {
      selectedProjectSubject = new Subject<any>();
      (component as any).sharedService.selectedProject$ = selectedProjectSubject.asObservable();

      closeSpy = spyOn(component, 'closePopupForProject');
      openSpy = spyOn(component, 'openPopupForProject');
    });

    it('should call closePopupForProject when project is undefined and selectedProject is set', fakeAsync(() => {
      const prevProject = createMockProject({ projectGuid: 'test-guid' });
      component['selectedProject'] = prevProject;

      component.ngAfterViewInit();
      tick();

      selectedProjectSubject.next(undefined);
      tick();

      expect(closeSpy).toHaveBeenCalledWith(prevProject);
      expect(component['selectedProject']).toBeUndefined();
    }));

    it('should call openPopupForProject when a new project is selected', fakeAsync(() => {
      const newProject = createMockProject({ projectGuid: 'new-guid' });
      component['selectedProject'] = undefined;

      component.ngAfterViewInit();
      tick();

      selectedProjectSubject.next(newProject);
      tick();

      expect(openSpy).toHaveBeenCalledWith(newProject);
      expect(component['selectedProject'] as Project | undefined).toEqual(newProject);
    }));

    it('should do nothing if both current and incoming projects are undefined', fakeAsync(() => {
      component['selectedProject'] = undefined;

      component.ngAfterViewInit();
      tick();

      selectedProjectSubject.next(undefined);
      tick();

      expect(closeSpy).not.toHaveBeenCalled();
      expect(openSpy).not.toHaveBeenCalled();
    }));
    
    it('should assign selectedProject and call openPopupForProject if project is truthy', fakeAsync(() => {
      const testProject = createMockProject({
        projectGuid: '123',
        latitude: 50,
        longitude: -120,
      });

      component['selectedProject'] = undefined;

      component.ngAfterViewInit();
      tick();

      selectedProjectSubject.next(testProject);
      tick();

      expect(component['selectedProject'] as Project | undefined).toEqual(testProject);
      expect(openSpy).toHaveBeenCalledWith(testProject);
    }));

  });

 describe('Polygon Layer Methods', () => {
  let layerGroup: any;

  beforeEach(() => {
    // Create a mock LayerGroup with addLayer
    layerGroup = {
      addLayer: jasmine.createSpy('addLayer'),
    };

    // Assign to component's layer groups
    component['projectBoundaryGroup'] = layerGroup as any;
    component['activityBoundaryGroup'] = layerGroup as any;
  });

  it('addGeoJsonToLayer() should handle GeometryCollection correctly', () => {
    const mockGeoJsonLayer = {
      addTo: jasmine.createSpy('addTo'),
    };

    const geoJsonSpy = spyOn(L, 'geoJSON').and.returnValue(mockGeoJsonLayer as unknown as L.GeoJSON);

    const geometry = {
      type: 'GeometryCollection',
      geometries: [
        { type: 'Polygon', coordinates: [[[0, 0], [1, 1], [2, 2], [0, 0]]] },
        { type: 'Point', coordinates: [1, 1] },
      ],
    };

    const options = { style: { color: 'blue' } };

    component.addGeoJsonToLayer(geometry, layerGroup, options);

    expect(geoJsonSpy).toHaveBeenCalledTimes(0);
    expect(mockGeoJsonLayer.addTo).toHaveBeenCalledTimes(0);
  });

  it('plotProjectBoundary() should call addGeoJsonToLayer for each project boundary', () => {
    const geometry = { type: 'Polygon', coordinates: [[[0, 0], [1, 1], [2, 2], [0, 0]]] };
    const project = {
      projectBoundaries: [
        { boundaryGeometry: geometry },
        { boundaryGeometry: geometry }
      ]
    };

    const spy = spyOn(component, 'addGeoJsonToLayer');
    component.plotProjectBoundary(project);

    expect(spy).toHaveBeenCalledTimes(2);
    expect(spy).toHaveBeenCalledWith(
      geometry,
      layerGroup,
      jasmine.objectContaining({ style: jasmine.anything() })
    );
  });

  it('plotActivityBoundaries() should call addGeoJsonToLayer for each activity geometry', () => {
    const geometry = { type: 'Polygon', coordinates: [[[0, 0], [1, 1], [2, 2], [0, 0]]] };
    const project = {
      projectFiscals: [
        {
          fiscalYear: 2024,
          activities: [
            { activityBoundaries: [{ activityGeometry: geometry }] },
            { activityBoundaries: [{ activityGeometry: geometry }] },
          ]
        }
      ]
    };

    const spy = spyOn(component, 'addGeoJsonToLayer');
    component.plotActivityBoundaries(project, 2025);

    expect(spy).toHaveBeenCalledTimes(2);
    expect(spy).toHaveBeenCalledWith(
      geometry,
      layerGroup,
      jasmine.objectContaining({ style: jasmine.anything() })
    );
  });

  it('togglePolygonLayers() should add layers at zoom >= 10', () => {
    const mockMap = {
      hasLayer: jasmine.createSpy().and.returnValue(false),
      addLayer: jasmine.createSpy(),
    };

    mapServiceMock.getSMKInstance.and.returnValue({
      $viewer: { map: mockMap }
    });

    component.togglePolygonLayers(10);

    expect(mockMap.hasLayer).toHaveBeenCalledTimes(2);
    expect(mockMap.addLayer).toHaveBeenCalledWith(layerGroup);
  });

  it('togglePolygonLayers() should remove layers at zoom < 10', () => {
    const mockMap = {
      removeLayer: jasmine.createSpy()
    };

    mapServiceMock.getSMKInstance.and.returnValue({
      $viewer: { map: mockMap }
    });

    component.togglePolygonLayers(5);

    expect(mockMap.removeLayer).toHaveBeenCalledWith(layerGroup);
    expect(mockMap.removeLayer).toHaveBeenCalledTimes(2);
  });

  it('togglePolygonLayers() should do nothing if map is undefined', () => {
    mapServiceMock.getSMKInstance.and.returnValue(undefined);
    const result = component.togglePolygonLayers(12);
    expect(result).toBeUndefined();
  });
});


});
