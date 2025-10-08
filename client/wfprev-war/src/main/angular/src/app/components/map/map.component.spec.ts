
import { ComponentFixture, TestBed, fakeAsync, flush, flushMicrotasks, tick } from '@angular/core/testing';
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
import { BC_BOUNDS } from 'src/app/utils/constants';
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

  getProjectLocations() {
    return of([]);
  }

  getFeatureByProjectGuid(projectGuid: string) {
    return of({ projectGuid, projectName: 'Mocked Project' });
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

  beforeAll(() => jasmine.getEnv().allowRespy(true));
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
        fitBounds: jasmine.createSpy('fitBounds'),
        controls: { bottomleft: { addTo: jasmine.createSpy('addTo') } },
      },
      layerIds: ['a', 'b'],
      isDisplayContextItemVisible: jasmine.createSpy('isVis').and.returnValue(true),
      layerIdPromise: { 'a@foo': {}, 'b@foo': {} },
      updateLayersVisible: jasmine.createSpy('updateLayersVisible')
        .and.returnValue(Promise.resolve()),
      refreshLayers: jasmine.createSpy('refreshLayers')
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
    mapServiceMock = jasmine.createSpyObj<MapService>('MapService', ['getMapIndex', 'setMapIndex', 'createSMK', 'getSMKInstance','clearSMKInstance']);
    mapContainer = jasmine.createSpyObj('ElementRef', ['nativeElement']);

    mapConfigServiceMock.getMapConfig.and.returnValue(Promise.resolve({ theme: 'testTheme' }));
    mapServiceMock.getSMKInstance.and.returnValue(createMockSMKInstance());
    mapServiceMock.createSMK.and.returnValue(Promise.resolve());

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

      flushMicrotasks();
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

    component.ngOnDestroy();

    expect(destroySpy).toHaveBeenCalled();
    expect(mapServiceMock.clearSMKInstance).toHaveBeenCalled();
  });

  it('should only call clearSMKInstance if smk has no destroy method', () => {
    const smkMock = {
      $viewer: { map: {} }
    };

    mapServiceMock.getSMKInstance.and.returnValue(smkMock);

    component.ngOnDestroy();

    expect(mapServiceMock.clearSMKInstance).toHaveBeenCalled();
  });

  it('should do nothing if no smk instance exists', () => {
    mapServiceMock.getSMKInstance.and.returnValue(null);

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
      component['projectMarkerMap'].clear();

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
    (component['projectBoundaryGroup'] as any)._layers = {};
    (component['activityBoundaryGroup'] as any)._layers = {};
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
      jasmine.any(L.LayerGroup),
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
      jasmine.any(L.LayerGroup),
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
    expect(mockMap.addLayer).toHaveBeenCalledWith(jasmine.any(L.LayerGroup));
  });
  
  it('togglePolygonLayers() should remove layers at zoom < 10', () => {
    const mockMap = {
      removeLayer: jasmine.createSpy(),
    };

    mapServiceMock.getSMKInstance.and.returnValue({
      $viewer: { map: mockMap },
    });

    component.togglePolygonLayers(5);

    expect(mockMap.removeLayer).toHaveBeenCalledWith(component['projectBoundaryGroup']);
    expect(mockMap.removeLayer).toHaveBeenCalledWith(component['activityBoundaryGroup']);
    expect(mockMap.removeLayer).toHaveBeenCalledTimes(2);
  });

  it('togglePolygonLayers() should do nothing if map is undefined', () => {
    mapServiceMock.getSMKInstance.and.returnValue(undefined);
    const result = component.togglePolygonLayers(12);
    expect(result).toBeUndefined();
  });
});

  describe('safelyClearLayerGroup()', () => {
    it('should remove valid layers from the group and skip invalid ones', () => {
      const validLayer1 = jasmine.createSpyObj('Layer', ['remove', 'on']);
      const validLayer2 = jasmine.createSpyObj('Layer', ['remove', 'on']);
      const invalidLayer = {};

      const mockGroup = {
        _layers: {
          '1': validLayer1,
          '2': validLayer2,
          '3': invalidLayer
        },
        removeLayer: jasmine.createSpy('removeLayer')
      } as unknown as L.LayerGroup;

      const consoleWarnSpy = spyOn(console, 'warn');

      component['safelyClearLayerGroup'](mockGroup, 'testGroup');

      expect(mockGroup.removeLayer).toHaveBeenCalledWith(validLayer1);
      expect(mockGroup.removeLayer).toHaveBeenCalledWith(validLayer2);

      expect(consoleWarnSpy).toHaveBeenCalledWith('[Map] Skipping invalid layer inside testGroup', invalidLayer);
    });

    it('should handle group with undefined _layers gracefully', () => {
      const mockGroup = {} as unknown as L.LayerGroup;

      expect(() => {
        component['safelyClearLayerGroup'](mockGroup, 'emptyGroup');
      }).not.toThrow();
    });
  });

  describe('zoom + fitBounds', () => {
    it('calls fitBounds when available', fakeAsync(() => {
      mapContainer.nativeElement = document.createElement('div');
      component.mapContainer = mapContainer;

      const smk = createMockSMKInstance();
      (smk.$viewer.map as any).fitBounds = jasmine.createSpy('fitBounds');

      mapServiceMock.getSMKInstance.and.returnValue(smk);
      mapConfigServiceMock.getMapConfig.and.returnValue(Promise.resolve({}));

      component.ngAfterViewInit();
      tick();

      expect((smk.$viewer.map as any).fitBounds).toHaveBeenCalledWith(BC_BOUNDS);
    }));
  });

  describe('updateProjectMarkersFromLocations', () => {
    let mockMap: any;

    afterEach(() => {
      jasmine.getEnv().allowRespy(true);
    });
    beforeEach(() => {
      mockMap = {
        addLayer: jasmine.createSpy(),
        removeLayer: jasmine.createSpy(),
      };
      mapServiceMock.getSMKInstance.and.returnValue({ $viewer: { map: mockMap } });
      (component as any).markersClusterGroup = {
        clearLayers: jasmine.createSpy(),
        addLayer: jasmine.createSpy(),
      };
      spyOn(console, 'log');
      spyOn(console, 'error');
      spyOn(console, 'warn');
    });

    it('should skip if map or cluster group missing', () => {
      (component as any).markersClusterGroup = null;
      component['updateProjectMarkersFromLocations']([{ projectGuid: '1', latitude: 1, longitude: 2 }]);
      expect(console.warn).toHaveBeenCalledWith('Map cannot update markers');
    });

    it('should add valid markers and prefetch feature on hover', () => {
      const handleClickSpy = spyOn<any>(component, 'handleProjectClick');
      const loc = { projectGuid: 'p1', latitude: 1, longitude: 2 };

      const subSpy = spyOn(component['projectService'], 'getFeatureByProjectGuid')
        .and.returnValue(of(createMockProject({ projectGuid: 'p1', projectName: 'Test' })));

      component['updateProjectMarkersFromLocations']([loc]);

      const marker = component['projectMarkerMap'].get('p1')!;
      expect(marker).toBeDefined();

      marker.fire('mouseover');

      expect(subSpy).toHaveBeenCalled();
      expect(handleClickSpy).not.toHaveBeenCalled(); 
    });


    it('should use cached project on click without re-fetching', () => {
      const loc = { projectGuid: 'cached', latitude: 1, longitude: 2 };
      component['featureCache'].set('cached', { projectGuid: 'cached', projectName: 'Cached' } as Project);
      const handleSpy = spyOn<any>(component, 'handleProjectClick');
      component['updateProjectMarkersFromLocations']([loc]);
      const marker = component['projectMarkerMap'].get('cached')!;
      marker.fire('click');
      expect(handleSpy).toHaveBeenCalled();
    });
  });

  describe('handleProjectClick', () => {
    beforeEach(() => {
      spyOnProperty(component['sharedService'], 'currentDisplayedProjects', 'get').and.returnValue([]);
      spyOn(component['sharedService'], 'updateDisplayedProjects');
      spyOn(component['sharedService'], 'selectProject');
      spyOn(component, 'openPopupForProject');
    });

    it('should add project if not already in displayed list', () => {
      const project = createMockProject();
      component['handleProjectClick'](project);
      expect(component['sharedService'].updateDisplayedProjects).toHaveBeenCalled();
      expect(component['sharedService'].selectProject).toHaveBeenCalledWith(project);
    });

    it('should not add duplicate projects', () => {
      const project = createMockProject();
      spyOnProperty(component['sharedService'], 'currentDisplayedProjects', 'get').and.returnValue([project]);
      component['handleProjectClick'](project);
      expect(component['sharedService'].updateDisplayedProjects).not.toHaveBeenCalled();
    });
  });

  describe('fetchAndUpdateProjectLocations', () => {
    it('should warn when no locations found', () => {
      const warnSpy = spyOn(console, 'warn');
      const spyUpdate = spyOn<any>(component, 'updateProjectMarkersFromLocations');
      spyOn(component['projectService'], 'getProjectLocations').and.returnValue(of([]));

      component['fetchAndUpdateProjectLocations']({});
      expect(warnSpy).toHaveBeenCalledWith('[Map] No project locations found.');
      expect(spyUpdate).toHaveBeenCalledWith([]);
    });

    it('should handle errors gracefully', () => {
      const errorSpy = spyOn(console, 'error');
      spyOn(component['projectService'], 'getProjectLocations').and.returnValue({
        subscribe: (obs: any) => obs.error('boom')
      } as any);
      component['fetchAndUpdateProjectLocations']({});
      expect(errorSpy).toHaveBeenCalledWith('Error fetching project locations:', 'boom');
    });
  });

  describe('addGeometryToLayerGroup', () => {
    it('should add valid geometry to layer group', () => {
      const mockLayer = { addTo: jasmine.createSpy('addTo') };
      spyOn(L, 'geoJSON').and.returnValue(mockLayer as any);
      component.addGeometryToLayerGroup({ type: 'Polygon' }, {} as any, {});
      expect(mockLayer.addTo).toHaveBeenCalled();
    });

    it('should warn if invalid geometry', () => {
      spyOn(L, 'geoJSON').and.returnValue({} as any);
      const warnSpy = spyOn(console, 'warn');
      component.addGeometryToLayerGroup({ type: 'Invalid' }, {} as any, {});
      expect(warnSpy).toHaveBeenCalled();
    });
  });

  it('addGeoJsonToLayer should skip invalid geometry', () => {
    const spy = spyOn(console, 'warn');
    component.addGeoJsonToLayer(undefined as any, {} as any, {});
    expect(spy).toHaveBeenCalled();
  });



});
