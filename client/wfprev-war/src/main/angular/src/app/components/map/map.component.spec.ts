
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { ElementRef, NO_ERRORS_SCHEMA } from '@angular/core';
import { ComponentFixture, fakeAsync, flush, flushMicrotasks, TestBed, tick } from '@angular/core/testing';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { ActivatedRoute } from '@angular/router';
import * as L from 'leaflet';
import { of, Subject } from 'rxjs';
import { Project } from 'src/app/components/models';
import { AppConfigService } from 'src/app/services/app-config.service';
import { CodeTableServices } from 'src/app/services/code-table-services';
import { MapConfigService } from 'src/app/services/map-config.service';
import { MapService } from 'src/app/services/map.service';
import { ProjectService } from 'src/app/services/project-services';
import { BC_BOUNDS } from 'src/app/utils/constants';
import { MapComponent } from './map.component';
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
        closePopup: jasmine.createSpy('closePopup'),
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
      getLayers: jasmine.createSpy('getLayers').and.returnValue([]),
    });
    spyOn(L.Control.prototype, 'addTo').and.callFake(function (this: any) {
      return this;
    });

    mapConfigServiceMock = jasmine.createSpyObj<MapConfigService>('MapConfigService', ['getMapConfig']);
    mapServiceMock = jasmine.createSpyObj<MapService>('MapService', ['getMapIndex', 'setMapIndex', 'createSMK', 'getSMKInstance', 'clearSMKInstance', 'setContainerId', 'destroySMK', 'createProjectBoundaryLayer', 'createActivityBoundaryLayer']);
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

  describe('ngOnDestroy', () => {
    it('should destroy SMK instance and clear it from the map service', () => {
      const destroySpy = jasmine.createSpy('destroy');
      const smkMock = {
        destroy: destroySpy,
        $viewer: { map: {} }
      };

      mapServiceMock.getSMKInstance.and.returnValue(smkMock);

      component.ngOnDestroy();

      expect(mapServiceMock.destroySMK).toHaveBeenCalled();
    });

    it('should only call clearSMKInstance if smk has no destroy method', () => {
      const smkMock = {
        $viewer: { map: {} }
      };

      mapServiceMock.getSMKInstance.and.returnValue(smkMock);

      component.ngOnDestroy();

      expect(mapServiceMock.destroySMK).toHaveBeenCalled();
    });

    it('should do nothing if no smk instance exists', () => {
      mapServiceMock.getSMKInstance.and.returnValue(null);

      component.ngOnDestroy();

      expect(mapServiceMock.destroySMK).toHaveBeenCalled();
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
        closePopup: jasmine.createSpy(),
      };
      mapServiceMock.getSMKInstance.and.returnValue({ $viewer: { map: mockMap } });
      (component as any).markersClusterGroup = {
        clearLayers: jasmine.createSpy(),
        addLayer: jasmine.createSpy(),
        getLayers: jasmine.createSpy().and.returnValue([]),
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

  describe('boundary layer creation', () => {
    let mockMap: any;

    beforeEach(() => {
      // minimal map with addLayer/removeLayer/hasLayer
      mockMap = {
        addLayer: jasmine.createSpy('addLayer'),
        removeLayer: jasmine.createSpy('removeLayer'),
        hasLayer: jasmine.createSpy('hasLayer').and.returnValue(false),
        closePopup: jasmine.createSpy('closePopup')
      };
      // make SMK return our mock map
      mapServiceMock.getSMKInstance.and.returnValue({ $viewer: { map: mockMap } });

      // provide a cluster group so updateProjectMarkersFromLocations runs fully
      (component as any).markersClusterGroup = {
        clearLayers: jasmine.createSpy('clearLayers'),
        addLayer: jasmine.createSpy('addLayer'),
        closePopup: jasmine.createSpy('closePopup'),
        getLayers: jasmine.createSpy('getLayers').and.returnValue([]),
      };


      // default returns for factory methods
      mapServiceMock.createProjectBoundaryLayer.and.returnValue({ id: 'proj-layer' } as any);
      mapServiceMock.createActivityBoundaryLayer.and.returnValue({ id: 'act-layer' } as any);
    });

    it('creates project & activity layers with unique projectGuids and adds them to map', () => {
      const locs = [
        { projectGuid: 'a', latitude: 1, longitude: 2 },
        { projectGuid: 'a', latitude: 3, longitude: 4 },
        { projectGuid: 'b', latitude: 5, longitude: 6 },
        { projectGuid: undefined, latitude: 7, longitude: 8 }, // invalid -> filtered out
      ];

      (component as any).updateProjectMarkersFromLocations(locs as any);

      // verify MapService layer factories are called with correct args
      expect(mapServiceMock.createProjectBoundaryLayer)
        .toHaveBeenCalledWith(mockMap, ['a', 'b']);
      expect(mapServiceMock.createActivityBoundaryLayer)
        .toHaveBeenCalledWith(mockMap, ['a', 'b'], component.currentFiscalYear);

      // and the returned layers are added to the map
      expect(mockMap.addLayer).toHaveBeenCalledWith(jasmine.objectContaining({ id: 'proj-layer' }));
      expect(mockMap.addLayer).toHaveBeenCalledWith(jasmine.objectContaining({ id: 'act-layer' }));
    });

    it('removes existing layers before adding new ones', () => {
      // seed existing layers on the component
      const oldProj = { id: 'old-proj' } as any;
      const oldAct = { id: 'old-act' } as any;
      (component as any).projectBoundaryLayer = oldProj;
      (component as any).activityBoundaryLayer = oldAct;

      // make map think they are currently present
      mockMap.hasLayer.and.callFake((l: any) => l === oldProj || l === oldAct);

      const locs = [
        { projectGuid: 'x', latitude: 49, longitude: -123 },
      ];

      (component as any).updateProjectMarkersFromLocations(locs as any);

      expect(mockMap.removeLayer).toHaveBeenCalledWith(oldProj);
      expect(mockMap.removeLayer).toHaveBeenCalledWith(oldAct);
      expect(mapServiceMock.createProjectBoundaryLayer).toHaveBeenCalled();
      expect(mapServiceMock.createActivityBoundaryLayer).toHaveBeenCalled();
      expect(mockMap.addLayer).toHaveBeenCalledTimes(2);
    });

    it('when no valid locations: removes layers and sets them to null (no new layer creation)', () => {
      // seed existing layers
      const oldProj = { id: 'old-proj' } as any;
      const oldAct = { id: 'old-act' } as any;
      (component as any).projectBoundaryLayer = oldProj;
      (component as any).activityBoundaryLayer = oldAct;

      mockMap.hasLayer.and.callFake((l: any) => l === oldProj || l === oldAct);

      const empty: any[] = [];
      (component as any).updateProjectMarkersFromLocations(empty);

      expect(mockMap.removeLayer).toHaveBeenCalledWith(oldProj);
      expect(mockMap.removeLayer).toHaveBeenCalledWith(oldAct);
      expect(mapServiceMock.createProjectBoundaryLayer).not.toHaveBeenCalled();
      expect(mapServiceMock.createActivityBoundaryLayer).not.toHaveBeenCalled();
      expect((component as any).projectBoundaryLayer).toBeNull();
      expect((component as any).activityBoundaryLayer).toBeNull();
    });

    it('passes currentFiscalYear to createActivityBoundaryLayer', () => {
      component.currentFiscalYear = 2030;

      const locs = [{ projectGuid: 'p', latitude: 1, longitude: 1 }];

      (component as any).updateProjectMarkersFromLocations(locs as any);

      expect(mapServiceMock.createActivityBoundaryLayer)
        .toHaveBeenCalledWith(mockMap, ['p'], 2030);
    });
  });

  describe('teardownActiveUI()', () => {
    let mapMock: any;

    beforeEach(() => {
      mapMock = { closePopup: jasmine.createSpy('closePopup') };
      mapServiceMock.getSMKInstance.and.returnValue({ $viewer: { map: mapMock } });
    });

    it('closes global popup and removes active marker via cluster group when present', () => {
      const activeMarkerSpy = jasmine.createSpyObj<L.Marker>('Marker', [
        'unbindPopup', 'remove'
      ]);
      Object.setPrototypeOf(activeMarkerSpy, L.Marker.prototype);

      const clusterMock = {
        addLayer: jasmine.createSpy('addLayer'),
        clearLayers: jasmine.createSpy('clearLayers'),
        removeLayer: jasmine.createSpy('removeLayer'),
        hasLayer: jasmine.createSpy('hasLayer').and.returnValue(true),
        getLayers: jasmine.createSpy('getLayers').and.returnValue([]),
      };

      (component as any).markersClusterGroup = clusterMock as any;
      (component as any).activeMarker = activeMarkerSpy;

      (component as any).teardownActiveUI();

      expect(mapMock.closePopup).toHaveBeenCalled();
      expect(activeMarkerSpy.unbindPopup).toHaveBeenCalled();
      expect(clusterMock.removeLayer).toHaveBeenCalledWith(activeMarkerSpy);
      expect((component as any).activeMarker).toBeNull();
    });

    it('closes global popup and removes active marker directly when not in cluster', () => {
      const activeMarkerSpy = jasmine.createSpyObj<L.Marker>('Marker', [
        'unbindPopup', 'remove'
      ]);
      Object.setPrototypeOf(activeMarkerSpy, L.Marker.prototype);

      const clusterMock = {
        addLayer: jasmine.createSpy('addLayer'),
        clearLayers: jasmine.createSpy('clearLayers'),
        removeLayer: jasmine.createSpy('removeLayer'),
        hasLayer: jasmine.createSpy('hasLayer').and.returnValue(false),
        getLayers: jasmine.createSpy('getLayers').and.returnValue([]),
      };

      (component as any).markersClusterGroup = clusterMock as any;
      (component as any).activeMarker = activeMarkerSpy;

      (component as any).teardownActiveUI();

      expect(mapMock.closePopup).toHaveBeenCalled();
      expect(activeMarkerSpy.unbindPopup).toHaveBeenCalled();
      expect(activeMarkerSpy.remove).toHaveBeenCalled();
      expect(clusterMock.removeLayer).not.toHaveBeenCalled();
      expect((component as any).activeMarker).toBeNull();
    });
  });

  describe('updateProjectMarkersFromLocations() teardown before clear', () => {
    it('unbinds/closes popups and removes listeners for each marker before clearing cluster', () => {
      const mapMock = { addLayer: () => { }, removeLayer: () => { }, closePopup: () => { } };
      mapServiceMock.getSMKInstance.and.returnValue({ $viewer: { map: mapMock } });

      const m1 = jasmine.createSpyObj<L.Marker>('Marker', ['unbindPopup', 'closePopup', 'off']);
      const m2 = jasmine.createSpyObj<L.Marker>('Marker', ['unbindPopup', 'closePopup', 'off']);
      Object.setPrototypeOf(m1, L.Marker.prototype);
      Object.setPrototypeOf(m2, L.Marker.prototype);

      (component as any).markersClusterGroup = {
        addLayer: jasmine.createSpy('addLayer'),
        clearLayers: jasmine.createSpy('clearLayers'),
        removeLayer: jasmine.createSpy('removeLayer'),
        hasLayer: jasmine.createSpy('hasLayer').and.returnValue(false),
        getLayers: jasmine.createSpy('getLayers').and.returnValue([m1, m2]),
      } as any;

      (component as any).updateProjectMarkersFromLocations([]);

      expect(m1.unbindPopup).toHaveBeenCalled();
      expect(m1.closePopup).toHaveBeenCalled();
      expect(m1.off).toHaveBeenCalled();

      expect(m2.unbindPopup).toHaveBeenCalled();
      expect(m2.closePopup).toHaveBeenCalled();
      expect(m2.off).toHaveBeenCalled();

      expect((component as any).markersClusterGroup.clearLayers).toHaveBeenCalled();
    });
  });

  describe('filtering removes selected/active project', () => {
    it('tears down active marker and closes map popup when selected project is no longer in results', () => {
      const mapMock = {
        addLayer: jasmine.createSpy('addLayer'),
        removeLayer: jasmine.createSpy('removeLayer'),
        closePopup: jasmine.createSpy('closePopup'),
      };
      mapServiceMock.getSMKInstance.and.returnValue({ $viewer: { map: mapMock } });

      const activeMarkerSpy = jasmine.createSpyObj<L.Marker>('Marker', ['unbindPopup', 'remove']);
      Object.setPrototypeOf(activeMarkerSpy, L.Marker.prototype);

      const clusterMock = {
        addLayer: jasmine.createSpy('addLayer'),
        clearLayers: jasmine.createSpy('clearLayers'),
        removeLayer: jasmine.createSpy('removeLayer'),
        hasLayer: jasmine.createSpy('hasLayer').and.returnValue(true),
        getLayers: jasmine.createSpy('getLayers').and.returnValue([]),
      };

      (component as any).markersClusterGroup = clusterMock as any;
      (component as any).activeMarker = activeMarkerSpy;
      (component as any).selectedProject = { projectGuid: 'to-remove' };

      (component as any).updateProjectMarkersFromLocations([
        { projectGuid: 'test-guid', latitude: 1, longitude: 2 },
      ]);

      expect(mapMock.closePopup).toHaveBeenCalled();
      expect(clusterMock.removeLayer).toHaveBeenCalledWith(activeMarkerSpy);
      expect((component as any).activeMarker).toBeNull();
    });
  });

});
