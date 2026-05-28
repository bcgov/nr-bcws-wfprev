import { ComponentFixture, TestBed, fakeAsync, tick, discardPeriodicTasks } from '@angular/core/testing';
import { FiscalMapComponent } from './fiscal-map.component';
import { ProjectService } from 'src/app/services/project-services';
import { ActivatedRoute, Router } from '@angular/router';
import { of } from 'rxjs';
import * as L from 'leaflet';
import { leafletProxy } from 'src/app/services/leaflet-proxy';

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

const mockRouter = {
  createUrlTree: jasmine.createSpy('createUrlTree').and.callFake((commands: any[], extras: any) => {
    return { commands, queryParams: extras?.queryParams };
  }),
  serializeUrl: jasmine.createSpy('serializeUrl').and.callFake((urlTree: any) => {
    if (urlTree?.queryParams?.bbox) {
      return `/map?bbox=${urlTree.queryParams.bbox}`;
    }
    return '/map';
  })
};

describe('FiscalMapComponent', () => {
  let component: FiscalMapComponent;
  let fixture: ComponentFixture<FiscalMapComponent>;
  let mockMapInstance: any;
  let geoJsonAddToSpy: jasmine.Spy;

  beforeEach(async () => {
    // Create and attach the map container before anything runs
    const container = document.createElement('div');
    container.setAttribute('id', 'fiscalMap');
    document.body.appendChild(container);

    geoJsonAddToSpy = jasmine.createSpy('addTo').and.returnValue({});

    mockMapInstance = {
      setView: jasmine.createSpy('setView'),
      fitBounds: jasmine.createSpy('fitBounds'),
      remove: jasmine.createSpy('remove'),
      addLayer: jasmine.createSpy('addLayer'),
      on: jasmine.createSpy('on'),
      invalidateSize: jasmine.createSpy('invalidateSize'),
      zoomControl: {
        setPosition: jasmine.createSpy('setPosition')
      },
      _controlCorners: {
        bottomleft: document.createElement('div'),
        bottomright: document.createElement('div'),
        topleft: document.createElement('div'),
        topright: document.createElement('div')
      }
    };

    spyOn(leafletProxy, 'map').and.returnValue(mockMapInstance);
    spyOn(leafletProxy, 'marker').and.returnValue({ addTo: jasmine.createSpy('addTo') } as any);
    spyOn(leafletProxy, 'tileLayer').and.returnValue({ addTo: jasmine.createSpy('addTo') } as any);
    spyOn(leafletProxy, 'geoJSON').and.returnValue({ addTo: geoJsonAddToSpy, getBounds: () => L.latLngBounds([[48, -125], [49, -123]]) } as any);
    spyOn(leafletProxy, 'featureGroup').and.returnValue({ getBounds: () => ({}) } as any);
    spyOn(L.Control.prototype, 'addTo').and.callFake(function(this: any) { return this; });

    await TestBed.configureTestingModule({
      imports: [FiscalMapComponent],
      providers: [
        { provide: ProjectService, useValue: new MockProjectService() },
        { provide: ActivatedRoute, useValue: mockActivatedRoute },
        { provide: Router, useValue: mockRouter }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(FiscalMapComponent);
    component = fixture.componentInstance;
  });

  afterEach(fakeAsync(() => {
    tick(); 
    discardPeriodicTasks();
    const container = document.getElementById('fiscalMap');
    if (container) container.remove();
    fixture.destroy();
  }));

  it('should create', fakeAsync(() => {
    fixture.detectChanges();
    tick();
    expect(component).toBeTruthy();
  }));

  it('should call getAllActivitiesBoundaries on init', () => {
    const spy = spyOn(component as any, 'getAllActivitiesBoundaries');
    component.ngOnInit();
    expect(spy).toHaveBeenCalled();
  });

  it('should initialize map in ngAfterViewInit', fakeAsync(() => {
    const spy = spyOn<any>(component, 'initMap');
    component.ngAfterViewInit();
    tick();
    expect(spy).toHaveBeenCalled();
  }));

  it('should get project boundaries and select the latest one', fakeAsync(() => {
    const mockProjectBoundaries = {
      _embedded: {
        projectBoundary: [
          { id: '1', geometry: { type: 'Polygon', coordinates: [[]] }, systemStartTimestamp: '2023-01-01T12:00:00Z' },
          { id: '2', geometry: { type: 'Polygon', coordinates: [[]] }, systemStartTimestamp: '2023-02-01T12:00:00Z' },
          { id: '3', geometry: { type: 'Polygon', coordinates: [[]] }, systemStartTimestamp: '2022-12-01T12:00:00Z' }
        ]
      }
    };

    const projectService = TestBed.inject(ProjectService);
    (projectService.getProjectBoundaries as jasmine.Spy).and.returnValue(of(mockProjectBoundaries));

    spyOn(component, 'plotProjectBoundary');
    component.map = mockMapInstance;

    component.getProjectBoundary();
    tick();

    expect(component.projectGuid).toBe('mock-project-guid');
    expect(projectService.getProjectBoundaries).toHaveBeenCalledWith('mock-project-guid');
    expect(component.projectBoundary.length).toBe(1);
    expect(component.projectBoundary[0].id).toBe('2');
    expect(component.plotProjectBoundary).toHaveBeenCalledWith(component.projectBoundary);
  }));

  it('should handle empty project boundaries', fakeAsync(() => {
    const projectService = TestBed.inject(ProjectService);
    (projectService.getProjectBoundaries as jasmine.Spy).and.returnValue(of({
      _embedded: { projectBoundary: [] }
    }));

    spyOn(component, 'plotProjectBoundary');
    component.map = mockMapInstance;

    component.getProjectBoundary();
    tick();

    expect(projectService.getProjectBoundaries).toHaveBeenCalledWith('mock-project-guid');
    expect(component.projectBoundary).toEqual([]);
    expect(component.plotProjectBoundary).not.toHaveBeenCalled();
  }));

  it('should fetch and mark project coordinates', fakeAsync(() => {
    (component as any).map = mockMapInstance;
    component['projectGuid'] = 'mock-project-guid';
    component.getProjectCoordinates();
    tick();
    expect((component as any).projectLatitude).toBe('48.4284');
    expect(mockMapInstance.setView).toHaveBeenCalled();
  }));

  it('should call getProjectCoordinates when there are no activities', fakeAsync(() => {
    const spy = spyOn(component, 'getProjectCoordinates');

    const svc = TestBed.inject(ProjectService) as unknown as MockProjectService;
    svc.getProjectFiscalsByProjectGuid.and.returnValue(of({
      _embedded: {
        projectFiscals: [{ fiscalYear: 2023, projectPlanFiscalGuid: 'fiscal-1' }]
      }
    }));
    svc.getFiscalActivities.and.returnValue(of({
      _embedded: { activities: [] }
    }));

    fixture.detectChanges();
    tick();
    component['projectGuid'] = 'mock-project-guid';
    component.getAllActivitiesBoundaries();
    tick();

    expect(spy).toHaveBeenCalled();
  }));

  it('should plot activity boundaries for different fiscal activities', fakeAsync(() => {
    (component as any).map = { fitBounds: jasmine.createSpy('fitBounds'), remove: jasmine.createSpy('remove') };

    component.plotActivityBoundariesOnMap([
      { fiscalYear: component.currentFiscalYear - 1, boundary: [{ geometry: { type: 'Polygon', coordinates: [] } }] },
      { fiscalYear: component.currentFiscalYear,     boundary: [{ geometry: { type: 'Polygon', coordinates: [] } }] },
      { fiscalYear: component.currentFiscalYear + 1, boundary: [{ geometry: { type: 'Polygon', coordinates: [] } }] }
    ]);
    tick();

    expect(leafletProxy.geoJSON).toHaveBeenCalledTimes(3);
    expect(geoJsonAddToSpy).toHaveBeenCalledTimes(3);
  }));

  it('should handle GeometryCollection in plotActivityBoundariesOnMap', fakeAsync(() => {
    (component as any).map = mockMapInstance;

    component.plotActivityBoundariesOnMap([{
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
    }]);
    tick();

    expect(leafletProxy.geoJSON).toHaveBeenCalledTimes(2);
    expect(geoJsonAddToSpy).toHaveBeenCalledTimes(2);
  }));

  it('should skip boundary items without geometry in plotActivityBoundariesOnMap', fakeAsync(() => {
    (component as any).map = mockMapInstance;

    component.plotActivityBoundariesOnMap([{
      fiscalYear: component.currentFiscalYear,
      boundary: [{ notGeometry: true }]
    }]);
    tick();

    expect(leafletProxy.geoJSON).not.toHaveBeenCalled();
  }));

  it('should plot project boundary with normal and GeometryCollection', fakeAsync(() => {
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
    tick();

    expect(leafletProxy.geoJSON).toHaveBeenCalledTimes(3);
    expect(geoJsonAddToSpy).toHaveBeenCalledTimes(3);
  }));

  it('should handle forkJoin results correctly in getAllActivitiesBoundaries', fakeAsync(() => {
    component['projectGuid'] = 'mock-guid';
    component['map'] = mockMapInstance;

    const mockFiscal = { fiscalYear: component.currentFiscalYear, projectPlanFiscalGuid: 'fiscal-1' };
    const mockActivity = { activityGuid: 'activity-1', fiscalYear: mockFiscal.fiscalYear, projectPlanFiscalGuid: mockFiscal.projectPlanFiscalGuid };
    const mockBoundary = { _embedded: { activityBoundary: [{ geometry: { type: 'Polygon', coordinates: [] } }] } };

    const projectService = TestBed.inject(ProjectService) as unknown as MockProjectService;
    projectService.getProjectFiscalsByProjectGuid.and.returnValue(of({ _embedded: { projectFiscals: [mockFiscal] } }));
    projectService.getFiscalActivities.and.returnValue(of({ _embedded: { activities: [mockActivity] } }));
    projectService.getActivityBoundaries.and.returnValue(of(mockBoundary));

    component['projectBoundary'] = [];

    const plotSpy = spyOn(component as any, 'plotActivityBoundariesOnMap');
    const coordSpy = spyOn(component, 'getProjectCoordinates');

    fixture.detectChanges();
    tick();

    expect(projectService.getProjectFiscalsByProjectGuid).toHaveBeenCalled();
    expect(projectService.getFiscalActivities).toHaveBeenCalled();
    expect(projectService.getActivityBoundaries).toHaveBeenCalled();
    expect(plotSpy).toHaveBeenCalledWith(jasmine.any(Array));
    expect(coordSpy).not.toHaveBeenCalled();
  }));

  it('should call getProjectCoordinates if no polygons exist', fakeAsync(() => {
    component['projectGuid'] = 'mock-guid';
    component['map'] = mockMapInstance;

    const mockFiscal = { fiscalYear: component.currentFiscalYear, projectPlanFiscalGuid: 'fiscal-1' };
    const mockActivity = { activityGuid: 'activity-1', fiscalYear: mockFiscal.fiscalYear, projectPlanFiscalGuid: mockFiscal.projectPlanFiscalGuid };

    const projectService = TestBed.inject(ProjectService) as unknown as MockProjectService;
    projectService.getProjectFiscalsByProjectGuid.and.returnValue(of({ _embedded: { projectFiscals: [mockFiscal] } }));
    projectService.getFiscalActivities.and.returnValue(of({ _embedded: { activities: [mockActivity] } }));
    projectService.getActivityBoundaries.and.returnValue(of({ _embedded: { activityBoundary: [] } }));

    component['projectBoundary'] = [];

    const plotSpy = spyOn(component as any, 'plotActivityBoundariesOnMap');
    const coordSpy = spyOn(component, 'getProjectCoordinates');

    fixture.detectChanges();
    tick();

    expect(plotSpy).not.toHaveBeenCalled();
    expect(coordSpy).toHaveBeenCalled();
  }));

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
    const activity = { activityGuid: 'act-1', fiscalYear: 2022 };
    const boundary = { _embedded: { activityBoundary: [{ geometry: { type: 'Polygon' } }] } };

    const result = (component as any).mapActivityBoundary(boundary, activity);
    expect(result).toEqual({
      activityGuid: 'act-1',
      fiscalYear: 2022,
      boundary: boundary._embedded.activityBoundary
    });
  });

  it('should return null if boundary is null', () => {
    const result = (component as any).mapActivityBoundary(null, { activityGuid: 'act-1', fiscalYear: 2022 });
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
      (leafletProxy.geoJSON as jasmine.Spy).and.callFake(() => ({
        getBounds: () => L.latLngBounds([[48, -125], [49, -123]])
      }) as any);

      component['allActivityBoundaries'] = [{ boundary: [{ geometry: { type: 'Polygon', coordinates: [] } }] }];
      component['projectBoundary'] = [{ boundaryGeometry: { type: 'Polygon', coordinates: [] } }];

      component.openFullMap();

      expect(window.open).toHaveBeenCalled();
      const url = (window.open as jasmine.Spy).calls.mostRecent().args[0];
      expect(url).toContain('/map?bbox=');
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

  it('should exit early in getAllActivitiesBoundaries if projectGuid is missing', () => {
    const route = TestBed.inject(ActivatedRoute);
    spyOn(route.snapshot.queryParamMap, 'get').and.returnValue(null);

    const spy = spyOn(component as any, 'handleFiscalsResponse');
    component.getAllActivitiesBoundaries();

    expect(spy).not.toHaveBeenCalled();
  });

  it('should sort projectFiscals by fiscalYear in handleFiscalsResponse', fakeAsync(() => {
    const mockData = {
      _embedded: {
        projectFiscals: [
          { fiscalYear: 2025, projectPlanFiscalGuid: 'f3' },
          { fiscalYear: 2023, projectPlanFiscalGuid: 'f1' },
          { fiscalYear: 2024, projectPlanFiscalGuid: 'f2' }
        ]
      }
    };

    const service = TestBed.inject(ProjectService) as unknown as MockProjectService;
    service.getFiscalActivities.and.returnValue(of({ _embedded: { activities: [] } }));

    (component as any).handleFiscalsResponse(mockData);
    tick();

    expect(component['projectFiscals'].map((f: any) => f.fiscalYear)).toEqual([2023, 2024, 2025]);
  }));

  it('should skip items without geometry in plotProjectBoundary', () => {
    (component as any).map = mockMapInstance;

    component.plotProjectBoundary([
      { boundaryGeometry: null },
      { noGeometry: true },
      {}
    ]);

    expect(leafletProxy.geoJSON).not.toHaveBeenCalled();
  });

  it('should plot project boundaries inside plotActivityBoundariesOnMap', fakeAsync(() => {
    (component as any).map = { fitBounds: jasmine.createSpy('fitBounds'), remove: jasmine.createSpy('remove') };

    component['projectBoundary'] = [
      { boundaryGeometry: { type: 'Polygon', coordinates: [] } }
    ];

    component.plotActivityBoundariesOnMap([{
      fiscalYear: component.currentFiscalYear,
      boundary: [{ geometry: { type: 'Polygon', coordinates: [] } }]
    }]);
    tick();

    expect(leafletProxy.geoJSON).toHaveBeenCalledWith(
      jasmine.objectContaining({ type: 'Polygon' }),
      jasmine.objectContaining({ style: jasmine.objectContaining({ color: '#3f3f3f' }) })
    );
  }));
});