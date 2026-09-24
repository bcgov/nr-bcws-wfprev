import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { FiscalMapComponent } from './fiscal-map.component';
import { ProjectService } from 'src/app/services/project-services';
import { MiniMap, MiniMapService } from 'src/app/services/mini-map.service';
import { ActivatedRoute } from '@angular/router';
import * as L from 'leaflet';
import { of, throwError } from 'rxjs';

class MockProjectService {
  getProjectFiscalsByProjectGuid = jasmine.createSpy().and.returnValue(of({
    _embedded: { projectFiscals: [] }
  }));

  getFiscalActivities = jasmine.createSpy().and.returnValue(of({
    _embedded: { activities: [] }
  }));

  getActivityBoundaries = jasmine.createSpy().and.returnValue(of({
    _embedded: { activityBoundary: [] }
  }));

  getProjectByProjectGuid = jasmine.createSpy().and.returnValue(of({
    latitude: '48.4284',
    longitude: '-123.3656'
  }));

  getProjectBoundaries = jasmine.createSpy().and.returnValue(of({
    _embedded: { projectBoundary: [] }
  }));
}

const mockActivatedRoute = {
  snapshot: {
    queryParamMap: {
      get: () => 'mock-project-guid'
    }
  }
};

// A 1° square with its south-west corner at the point
const square = (west: number, south: number) => ({
  type: 'Polygon',
  coordinates: [[[west, south], [west + 1, south], [west + 1, south + 1], [west, south + 1], [west, south]]]
});

describe('FiscalMapComponent', () => {
  let component: FiscalMapComponent;
  let fixture: ComponentFixture<FiscalMapComponent>;
  let mockMapInstance: any;
  let miniMap: MiniMap;
  let miniMapService: jasmine.SpyObj<MiniMapService>;
  let projectService: MockProjectService;
  let geoJsonAddToSpy: jasmine.Spy;

  beforeEach(fakeAsync(async () => {
    const container = document.createElement('div');
    container.setAttribute('id', 'fiscalMap');
    document.body.appendChild(container);

    mockMapInstance = {
      setView: jasmine.createSpy('setView'),
      fitBounds: jasmine.createSpy('fitBounds'),
      remove: jasmine.createSpy('remove'),
      addLayer: jasmine.createSpy('addLayer'),
      on: jasmine.createSpy('on'),
      zoomControl: { setPosition: jasmine.createSpy('setPosition') },
      _controlCorners: {
        bottomleft: document.createElement('div'),
        bottomright: document.createElement('div'),
        topleft: document.createElement('div'),
        topright: document.createElement('div')
      }
    };

    geoJsonAddToSpy = jasmine.createSpy('addTo').and.returnValue({});
    miniMap = { smk: {}, map: mockMapInstance, leaflet: L };
    miniMapService = jasmine.createSpyObj('MiniMapService', ['create', 'destroy']);
    projectService = new MockProjectService();

    await TestBed.configureTestingModule({
      imports: [FiscalMapComponent],
      providers: [
        { provide: ProjectService, useValue: projectService },
        { provide: ActivatedRoute, useValue: mockActivatedRoute },
        { provide: MiniMapService, useValue: miniMapService }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(FiscalMapComponent);
    component = fixture.componentInstance;

    spyOn(component as any, 'initMap');
    spyOn(component as any, 'createMap').and.returnValue(Promise.resolve(miniMap));
    spyOn(component as any, 'createMarker').and.returnValue({ addTo: jasmine.createSpy('addTo') });
    spyOn(component as any, 'createGeoJSON').and.returnValue({ addTo: geoJsonAddToSpy });

    // The fixture initializes the component by itself once a test awaits; do it now, loading nothing, so tests
    // choose what loads
    spyOn(component, 'loadMapData').and.resolveTo();
    fixture.detectChanges();
    tick();
  }));

  afterEach(fakeAsync(() => {
    tick(0);
    document.getElementById('fiscalMap')?.remove();
    fixture.destroy();
  }));

  // One fiscal with one activity, whose boundary is the geometry
  function withActivityBoundary(geometry: any): void {
    projectService.getProjectFiscalsByProjectGuid.and.returnValue(of({
      _embedded: { projectFiscals: [{ fiscalYear: component.currentFiscalYear, projectPlanFiscalGuid: 'fiscal-1' }] }
    }));
    projectService.getFiscalActivities.and.returnValue(of({
      _embedded: { activities: [{ activityGuid: 'activity-1' }] }
    }));
    projectService.getActivityBoundaries.and.returnValue(of({
      _embedded: { activityBoundary: [{ geometry }] }
    }));
  }

  it('should create', () => {
    fixture.detectChanges();
    expect(component).toBeTruthy();
  });

  it('should call getAllActivitiesBoundaries on init', () => {
    (component.loadMapData as jasmine.Spy).and.callThrough();
    const spy = spyOn(component as any, 'getAllActivitiesBoundaries');
    component.ngOnInit();
    expect(spy).toHaveBeenCalled();
  });

  it('should initialize map in ngAfterViewInit', fakeAsync(() => {
    component.ngAfterViewInit();
    tick();
    expect((component as any).initMap).toHaveBeenCalled();
  }));

  it('should get project boundaries and select the latest one', async () => {
    const mockProjectBoundaries = {
      _embedded: {
        projectBoundary: [
          {
            id: '1',
            geometry: { type: 'Polygon', coordinates: [[]] },
            systemStartTimestamp: '2023-01-01T12:00:00Z'
          },
          {
            id: '2',
            geometry: { type: 'Polygon', coordinates: [[]] },
            systemStartTimestamp: '2023-02-01T12:00:00Z'
          },
          {
            id: '3',
            geometry: { type: 'Polygon', coordinates: [[]] },
            systemStartTimestamp: '2022-12-01T12:00:00Z'
          }
        ]
      }
    };
    projectService.getProjectBoundaries.and.returnValue(of(mockProjectBoundaries));
    component.projectGuid = 'mock-project-guid';

    await component.getProjectBoundary();

    expect(projectService.getProjectBoundaries).toHaveBeenCalledWith('mock-project-guid');
    expect(component.projectBoundary.length).toBe(1);
    expect(component.projectBoundary[0].id).toBe('2');
  });

  it('should handle empty project boundaries', async () => {
    component.projectGuid = 'mock-project-guid';

    await component.getProjectBoundary();

    expect(projectService.getProjectBoundaries).toHaveBeenCalledWith('mock-project-guid');
    expect(component.projectBoundary).toEqual([]);
  });

  it('should log rather than throw when the project boundary fails to load', async () => {
    const consoleError = spyOn(console, 'error');
    projectService.getProjectBoundaries.and.returnValue(throwError(() => new Error('offline')));

    await component.getProjectBoundary();

    expect(consoleError).toHaveBeenCalledWith('Error loading the project boundary:', jasmine.any(Error));
    expect(component.projectBoundary).toEqual([]);
  });

  it('should keep the project coordinates, to mark once the map exists', async () => {
    component.projectGuid = 'mock-project-guid';

    await component.getProjectCoordinates();

    expect(component.projectLatitude).toBe('48.4284');
    expect(component.projectLongitude).toBe('-123.3656');
    expect(component.createMarker).not.toHaveBeenCalled();
  });

  it('should load the latest boundary of each activity', async () => {
    withActivityBoundary(square(-125, 49));
    component.projectGuid = 'mock-project-guid';

    await component.getAllActivitiesBoundaries();

    expect(projectService.getFiscalActivities).toHaveBeenCalledWith('mock-project-guid', 'fiscal-1');
    expect(projectService.getActivityBoundaries).toHaveBeenCalledWith('mock-project-guid', 'fiscal-1', 'activity-1');
    expect(component.allActivityBoundaries).toEqual([
      { activityGuid: 'activity-1', fiscalYear: component.currentFiscalYear, boundary: [{ geometry: square(-125, 49) }] }
    ]);
  });

  it('should finish loading activity boundaries for a project with no fiscals', async () => {
    component.projectGuid = 'mock-project-guid';

    await component.getAllActivitiesBoundaries();

    expect(projectService.getFiscalActivities).not.toHaveBeenCalled();
    expect(component.allActivityBoundaries).toEqual([]);
  });

  it('should log rather than throw when the activity boundaries fail to load', async () => {
    const consoleError = spyOn(console, 'error');
    projectService.getProjectFiscalsByProjectGuid.and.returnValue(throwError(() => new Error('offline')));

    await component.getAllActivitiesBoundaries();

    expect(consoleError).toHaveBeenCalledWith('Error loading the activity boundaries:', jasmine.any(Error));
    expect(component.allActivityBoundaries).toEqual([]);
  });

  describe('loadMapData', () => {
    beforeEach(() => {
      (component.loadMapData as jasmine.Spy).and.callThrough();
    });

    it('should load the project and activity boundaries', async () => {
      withActivityBoundary(square(-125, 49));
      projectService.getProjectBoundaries.and.returnValue(of({
        _embedded: { projectBoundary: [{ boundaryGeometry: square(-120, 52) }] }
      }));

      await component.loadMapData();

      expect(component.projectGuid).toBe('mock-project-guid');
      expect(component.projectBoundary.length).toBe(1);
      expect(component.allActivityBoundaries.length).toBe(1);
      expect(projectService.getProjectByProjectGuid).not.toHaveBeenCalled();
    });

    it('should load the project location when there are no boundaries', async () => {
      await component.loadMapData();

      expect(component.projectLatitude).toBe('48.4284');
      expect(component.projectLongitude).toBe('-123.3656');
    });

    it('should not load anything without a project', async () => {
      const route = TestBed.inject(ActivatedRoute);
      spyOn(route.snapshot.queryParamMap, 'get').and.returnValue(null);

      await component.loadMapData();

      expect(projectService.getProjectBoundaries).not.toHaveBeenCalled();
      expect(projectService.getProjectFiscalsByProjectGuid).not.toHaveBeenCalled();
    });
  });

  describe('initialView', () => {
    it('should cover every activity and project boundary', () => {
      component.allActivityBoundaries = [{ fiscalYear: 2024, boundary: [{ geometry: square(-125, 49) }] }];
      component.projectBoundary = [{ boundaryGeometry: square(-120, 52) }];

      expect(component.initialView()).toEqual({ bounds: [[49, -125], [53, -119]], padding: 20 });
    });

    it('should show the project location when there are no boundaries', () => {
      component.projectLatitude = '48.4284';
      component.projectLongitude = '-123.3656';

      expect(component.initialView()).toEqual({ center: [48.4284, -123.3656], zoom: 14 });
    });

    it('should leave the map on SMK\'s view of BC when there is nothing to show', () => {
      expect(component.initialView()).toBeUndefined();
    });
  });

  it('should plot activity boundaries for different fiscal activities, without moving the map', () => {
    (component as any).map = mockMapInstance;

    component.plotActivityBoundariesOnMap([
      {
        fiscalYear: component.currentFiscalYear - 1,
        boundary: [{ geometry: { type: 'Polygon', coordinates: [] } }]
      },
      {
        fiscalYear: component.currentFiscalYear,
        boundary: [{ geometry: { type: 'Polygon', coordinates: [] } }]
      },
      {
        fiscalYear: component.currentFiscalYear + 1,
        boundary: [{ geometry: { type: 'Polygon', coordinates: [] } }]
      }
    ]);

    expect(component.createGeoJSON).toHaveBeenCalledTimes(3);
    expect(geoJsonAddToSpy).toHaveBeenCalledTimes(3);
    expect(mockMapInstance.fitBounds).not.toHaveBeenCalled();
  });

  it('should handle GeometryCollection in plotActivityBoundariesOnMap', () => {
    (component as any).map = mockMapInstance;

    const mockBoundaries = [
      {
        fiscalYear: component.currentFiscalYear,
        boundary: [{
          geometry: {
            type: 'GeometryCollection',
            geometries: [
              { type: 'Polygon', coordinates: [] },
              { type: 'Polygon', coordinates: [] }
            ]
          }
        }]
      }
    ];

    component.plotActivityBoundariesOnMap(mockBoundaries);

    expect(component.createGeoJSON).toHaveBeenCalledTimes(2);
    expect(geoJsonAddToSpy).toHaveBeenCalledTimes(2);
  });


  it('should skip boundary items without geometry in plotActivityBoundariesOnMap', () => {
    (component as any).map = mockMapInstance;

    component.plotActivityBoundariesOnMap([
      {
        fiscalYear: component.currentFiscalYear,
        boundary: [{ notGeometry: true }]
      }
    ]);

    expect(component.createGeoJSON).not.toHaveBeenCalled();
  });

  it('should plot project boundary with normal and GeometryCollection, without moving the map', () => {
    (component as any).map = mockMapInstance;

    const boundary = [
      { boundaryGeometry: { type: 'Polygon', coordinates: [] } },
      {
        boundaryGeometry: {
          type: 'GeometryCollection',
          geometries: [
            { type: 'Polygon', coordinates: [] },
            { type: 'Polygon', coordinates: [] }
          ]
        }
      },
      { noGeometry: true }
    ];

    component.plotProjectBoundary(boundary);
    expect(component.createGeoJSON).toHaveBeenCalledTimes(3);
    expect(geoJsonAddToSpy).toHaveBeenCalledTimes(3);
    expect(mockMapInstance.fitBounds).not.toHaveBeenCalled();
  });

  it('should correctly map activities with fiscal data', () => {
    const fiscal = { fiscalYear: 2023, projectPlanFiscalGuid: 'fiscal-guid' };
    const response = {
      _embedded: {
        activities: [
          { activityGuid: 'a1', name: 'Activity 1' },
          { activityGuid: 'a2', name: 'Activity 2' }
        ]
      }
    };

    const result = (component as any).mapFiscalActivities(response, fiscal);
    expect(result.length).toBe(2);
    expect(result[0]).toEqual(jasmine.objectContaining({
      activityGuid: 'a1',
      fiscalYear: 2023,
      projectPlanFiscalGuid: 'fiscal-guid'
    }));
  });

  it('should correctly map activity boundary if boundary exists', () => {
    const activity = {
      activityGuid: 'act-1',
      fiscalYear: 2022
    };
    const boundary = {
      _embedded: {
        activityBoundary: [{ geometry: { type: 'Polygon' } }]
      }
    };

    const result = (component as any).mapActivityBoundary(boundary, activity);
    expect(result).toEqual({
      activityGuid: 'act-1',
      fiscalYear: 2022,
      boundary: boundary._embedded.activityBoundary
    });
  });

  it('should return null if boundary is null', () => {
    const activity = {
      activityGuid: 'act-1',
      fiscalYear: 2022
    };
    const result = (component as any).mapActivityBoundary(null, activity);
    expect(result).toBeNull();
  });


  describe('openFullMap()', () => {
    let originalWindowOpen: any;

    beforeEach(() => {
      originalWindowOpen = window.open;
      window.open = jasmine.createSpy('open');
    });

    afterEach(() => {
      window.open = originalWindowOpen;
    });

    it('should open map with bbox from boundaries', () => {
      component['allActivityBoundaries'] = [
        {
          boundary: [{ geometry: square(-125, 48) }]
        }
      ];

      component['projectBoundary'] = [
        {
          boundaryGeometry: square(-124, 48)
        }
      ];

      component.openFullMap();

      expect(window.open).toHaveBeenCalled();
      const url = (window.open as jasmine.Spy).calls.mostRecent().args[0];
      expect(url).toContain('/map?bbox=-125.000000,48.000000,-123.000000,49.000000');
    });

    it('should open map with bbox from coordinates if no boundaries', () => {
      component['projectLatitude'] = '48.4284';
      component['projectLongitude'] = '-123.3656';
      component['allActivityBoundaries'] = [];
      component['projectBoundary'] = [];

      component.openFullMap();

      expect(window.open).toHaveBeenCalled();
      const url = (window.open as jasmine.Spy).calls.mostRecent().args[0];
      expect(url).toContain('/map?bbox=');
    });

    it('should open map without bbox if no boundaries or coordinates', () => {
      component['projectLatitude'] = '';
      component['projectLongitude'] = '';
      component['allActivityBoundaries'] = [];
      component['projectBoundary'] = [];

      component.openFullMap();

      expect(window.open).toHaveBeenCalledWith(jasmine.stringMatching(/\/map$/), '_blank');
    });
  });

  it('should sort projectFiscals by fiscalYear in handleFiscalsResponse', () => {
    const mockData = {
      _embedded: {
        projectFiscals: [
          { fiscalYear: 2025, projectPlanFiscalGuid: 'f3' },
          { fiscalYear: 2023, projectPlanFiscalGuid: 'f1' },
          { fiscalYear: 2024, projectPlanFiscalGuid: 'f2' }
        ]
      }
    };

    (component as any).handleFiscalsResponse(mockData);

    expect(component['projectFiscals'].map((f: any) => f.fiscalYear)).toEqual([2023, 2024, 2025]);
  });

  it('should skip items without geometry in plotProjectBoundary', () => {
    (component as any).map = mockMapInstance;

    const boundaries = [
      { boundaryGeometry: null },
      { noGeometry: true },
      {}
    ];

    component.plotProjectBoundary(boundaries);

    expect(component.createGeoJSON).not.toHaveBeenCalled();
  });

  it('should remove the map when the page is left', () => {
    component['miniMap'] = miniMap;

    component.ngOnDestroy();

    expect(miniMapService.destroy).toHaveBeenCalledWith(miniMap);
  });

  describe('initMap', () => {
    beforeEach(() => {
      (component.initMap as jasmine.Spy).and.callThrough();
      (component.loadMapData as jasmine.Spy).and.callThrough();
    });

    it('should create an SMK mini map in the fiscal map container', async () => {
      await component.initMap();

      expect(component.createMap).toHaveBeenCalledWith(document.getElementById('fiscalMap')!, jasmine.any(Promise));
      expect(component.map).toBe(mockMapInstance);
    });

    it('should open the map on the boundaries, once they have loaded', async () => {
      withActivityBoundary(square(-125, 49));
      component.ngOnInit();

      await component.initMap();
      const view = await (component.createMap as jasmine.Spy).calls.mostRecent().args[1];

      expect(view).toEqual({ bounds: [[49, -125], [50, -124]], padding: 20 });
    });

    it('should not move the map once it has opened', async () => {
      withActivityBoundary(square(-125, 49));
      component.ngOnInit();

      await component.initMap();

      expect(mockMapInstance.fitBounds).not.toHaveBeenCalled();
      expect(mockMapInstance.setView).not.toHaveBeenCalled();
    });

    it('should draw on the map with SMK\'s Leaflet', async () => {
      const smkLeaflet = { ...L, marker: jasmine.createSpy('marker').and.returnValue({ addTo: () => ({}) }) };
      (component.createMarker as jasmine.Spy).and.callThrough();
      (component.createMap as jasmine.Spy).and.returnValue(Promise.resolve({ ...miniMap, leaflet: smkLeaflet }));
      await component.initMap();

      component.createMarker([48.4, -123.4]);

      expect(smkLeaflet.marker).toHaveBeenCalledWith([48.4, -123.4], undefined);
    });

    it('should draw the boundaries that loaded while the map was being created', async () => {
      const plotProjectBoundary = spyOn(component, 'plotProjectBoundary');
      const plotActivityBoundaries = spyOn(component, 'plotActivityBoundariesOnMap');
      component.projectBoundary = [{ boundaryGeometry: { type: 'Polygon', coordinates: [] } }];
      component.allActivityBoundaries = [{ fiscalYear: 2024, boundary: [] }];

      await component.initMap();

      expect(plotProjectBoundary).toHaveBeenCalledWith(component.projectBoundary);
      expect(plotActivityBoundaries).toHaveBeenCalledWith(component.allActivityBoundaries);
      expect(component.createMarker).not.toHaveBeenCalled();
    });

    it('should mark the project location that loaded while the map was being created', async () => {
      component.projectLatitude = '48.4284';
      component.projectLongitude = '-123.3656';

      await component.initMap();

      expect(component.createMarker).toHaveBeenCalledWith([48.4284, -123.3656], jasmine.any(Object));
    });

    it('should remove a map that was still being created when the page was left', async () => {
      let finishCreating!: (created: MiniMap) => void;
      (component.createMap as jasmine.Spy).and.returnValue(new Promise(resolve => finishCreating = resolve));

      const initializing = component.initMap();
      component.ngOnDestroy();
      finishCreating(miniMap);
      await initializing;

      expect(miniMapService.destroy).toHaveBeenCalledWith(miniMap);
      expect(component.map).toBeUndefined();
    });

    it('should log rather than throw when the map cannot be created', async () => {
      const consoleError = spyOn(console, 'error');
      (component.createMap as jasmine.Spy).and.returnValue(Promise.reject(new Error('SMK failed')));

      await component.initMap();

      expect(consoleError).toHaveBeenCalledWith('Error loading map:', jasmine.any(Error));
      expect(component.map).toBeUndefined();
    });
  });
});
