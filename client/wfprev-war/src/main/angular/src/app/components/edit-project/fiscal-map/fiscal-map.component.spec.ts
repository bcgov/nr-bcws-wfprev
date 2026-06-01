import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { FiscalMapComponent } from './fiscal-map.component';
import { ProjectService } from 'src/app/services/project-services';
import { ActivatedRoute } from '@angular/router';
import { of } from 'rxjs';

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

describe('FiscalMapComponent', () => {
  let component: FiscalMapComponent;
  let fixture: ComponentFixture<FiscalMapComponent>;
  let mockMapInstance: any;
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

    await TestBed.configureTestingModule({
      imports: [FiscalMapComponent],
      providers: [
        { provide: ProjectService, useValue: new MockProjectService() },
        { provide: ActivatedRoute, useValue: mockActivatedRoute }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(FiscalMapComponent);
    component = fixture.componentInstance;

    spyOn(component as any, 'initMap');
    spyOn(component as any, 'createMap').and.returnValue(mockMapInstance);
    spyOn(component as any, 'createTileLayer').and.returnValue({ addTo: jasmine.createSpy('addTo') });
    spyOn(component as any, 'createMarker').and.returnValue({ addTo: jasmine.createSpy('addTo') });
    spyOn(component as any, 'createGeoJSON').and.returnValue({ addTo: geoJsonAddToSpy });
    spyOn(component as any, 'createFeatureGroup').and.returnValue({ getBounds: () => ({}) });
  }));

  afterEach(fakeAsync(() => {
    tick(0); 
    document.getElementById('fiscalMap')?.remove();
    fixture.destroy();
  }));

  it('should create', () => {
    fixture.detectChanges();
    expect(component).toBeTruthy();
  });

  it('should call getAllActivitiesBoundaries on init', () => {
    const spy = spyOn(component as any, 'getAllActivitiesBoundaries');
    component.ngOnInit();
    expect(spy).toHaveBeenCalled();
  });

  it('should initialize map in ngAfterViewInit', fakeAsync(() => {
    component.ngAfterViewInit();
    tick();
    expect((component as any).initMap).toHaveBeenCalled();
  }));

  it('should get project boundaries and select the latest one', () => {
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
  
    const projectService = TestBed.inject(ProjectService);
    (projectService.getProjectBoundaries as jasmine.Spy).and.returnValue(of(mockProjectBoundaries));
  
    spyOn(component, 'plotProjectBoundary');
    
    component.map = mockMapInstance;
  
    component.getProjectBoundary();
  
    expect(component.projectGuid).toBe('mock-project-guid');
    
    expect(projectService.getProjectBoundaries).toHaveBeenCalledWith('mock-project-guid');
  
    expect(component.projectBoundary.length).toBe(1);
    expect(component.projectBoundary[0].id).toBe('2');
    
    expect(component.plotProjectBoundary).toHaveBeenCalledWith(component.projectBoundary);
  });
  
  it('should handle empty project boundaries', () => {
    const mockEmptyBoundaries = {
      _embedded: {
        projectBoundary: []
      }
    };
  
    const projectService = TestBed.inject(ProjectService);
    (projectService.getProjectBoundaries as jasmine.Spy).and.returnValue(of(mockEmptyBoundaries));
  
    spyOn(component, 'plotProjectBoundary');
    
    component.map = mockMapInstance;
  
    component.getProjectBoundary();
    expect(projectService.getProjectBoundaries).toHaveBeenCalledWith('mock-project-guid');
  
    expect(component.projectBoundary).toEqual([]);
    expect(component.plotProjectBoundary).not.toHaveBeenCalled();
  });

  it('should fetch and mark project coordinates', () => {
    (component as any).map = mockMapInstance;
    component['projectGuid'] = 'mock-project-guid';
    component.getProjectCoordinates();
    expect((component as any).projectLatitude).toBe('48.4284');
    expect(mockMapInstance.setView).toHaveBeenCalled();
  });

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
    component['projectGuid'] = 'mock-project-guid';
    component.getAllActivitiesBoundaries();
    tick();
    expect(spy).toHaveBeenCalled();
  }));

  it('should plot activity boundaries for different fiscal activities', () => {
    const mockFitBounds = jasmine.createSpy('fitBounds');
    const mockRemove = jasmine.createSpy('remove');
    (component as any).map = { fitBounds: mockFitBounds, remove: mockRemove };
  
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
    expect(mockFitBounds).toHaveBeenCalled();
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

  it('should plot project boundary with normal and GeometryCollection', () => {
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
  });
  
  it('should handle forkJoin results correctly in getAllActivitiesBoundaries', fakeAsync(() => {
    const projectService = TestBed.inject(ProjectService) as unknown as MockProjectService;

    const mockFiscal = { fiscalYear: component.currentFiscalYear, projectPlanFiscalGuid: 'fiscal-1' };
    const mockActivity = { activityGuid: 'activity-1', fiscalYear: component.currentFiscalYear, projectPlanFiscalGuid: 'fiscal-1' };

    projectService.getProjectFiscalsByProjectGuid.and.returnValue(of({
      _embedded: { projectFiscals: [mockFiscal] }
    }));
    projectService.getFiscalActivities.and.returnValue(of({
      _embedded: { activities: [mockActivity] }
    }));
    projectService.getActivityBoundaries.and.returnValue(of({
      _embedded: { activityBoundary: [{ geometry: { type: 'Polygon', coordinates: [] } }] }
    }));

    component.map = mockMapInstance;
    component['projectBoundary'] = [];
    component['projectGuid'] = 'mock-project-guid';

    const plotSpy = spyOn(component as any, 'plotActivityBoundariesOnMap');
    const coordSpy = spyOn(component, 'getProjectCoordinates');

    component.getAllActivitiesBoundaries();
    tick();

    expect(projectService.getFiscalActivities).toHaveBeenCalled();
    expect(projectService.getActivityBoundaries).toHaveBeenCalled();
    expect(plotSpy).toHaveBeenCalledWith(jasmine.any(Array));
    expect(coordSpy).not.toHaveBeenCalled();
  }));

  it('should call getProjectCoordinates if no polygons exist', fakeAsync(() => {
    const projectService = TestBed.inject(ProjectService) as unknown as MockProjectService;

    const mockFiscal = { fiscalYear: component.currentFiscalYear, projectPlanFiscalGuid: 'fiscal-1' };
    const mockActivity = { activityGuid: 'activity-1', fiscalYear: component.currentFiscalYear, projectPlanFiscalGuid: 'fiscal-1' };

    projectService.getProjectFiscalsByProjectGuid.and.returnValue(of({
      _embedded: { projectFiscals: [mockFiscal] }
    }));
    projectService.getFiscalActivities.and.returnValue(of({
      _embedded: { activities: [mockActivity] }
    }));
    projectService.getActivityBoundaries.and.returnValue(of({
      _embedded: { activityBoundary: [] }
    }));

    component.map = mockMapInstance;
    component['projectBoundary'] = [];
    component['projectGuid'] = 'mock-project-guid';

    const plotSpy = spyOn(component as any, 'plotActivityBoundariesOnMap');
    const coordSpy = spyOn(component, 'getProjectCoordinates');

    component.getAllActivitiesBoundaries();
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
      const fakeBounds = {
        getSouthWest: () => ({ lat: 48, lng: -125 }),
        getNorthEast: () => ({ lat: 49, lng: -123 }),
        getWest: () => -125,
        getSouth: () => 48,
        getEast: () => -123,
        getNorth: () => 49,
      };

      (component as any).createGeoJSON.and.callFake(() => ({
        getBounds: () => fakeBounds
      }));
  
      component['allActivityBoundaries'] = [
        {
          boundary: [{ geometry: { type: 'Polygon', coordinates: [] } }]
        }
      ];
  
      component['projectBoundary'] = [
        {
          geometry: { type: 'Polygon', coordinates: [] }
        }
      ];
  
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
    
    // Fix here — treat `get` as a method
    spyOn(route.snapshot.queryParamMap, 'get').and.returnValue(null);
  
    const spy = spyOn(component as any, 'handleFiscalsResponse');
    
    component.getAllActivitiesBoundaries();
  
    expect(spy).not.toHaveBeenCalled();
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
  
    const service = TestBed.inject(ProjectService) as unknown as MockProjectService;
    service.getFiscalActivities.and.callFake((_guid: string, fiscalGuid: string) => {
      return of({ _embedded: { activities: [] } });
    });
  
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
  
  it('should plot project boundaries inside plotActivityBoundariesOnMap', () => {
    const mockFitBounds = jasmine.createSpy('fitBounds');
    const mockRemove = jasmine.createSpy('remove');

    (component as any).map = { fitBounds: mockFitBounds, remove: mockRemove };
    
    const activityBoundaries = [
      {
        fiscalYear: component.currentFiscalYear,
        boundary: [
          { geometry: { type: 'Polygon', coordinates: [] } }
        ]
      }
    ];
  
    component['projectBoundary'] = [
      { boundaryGeometry: { type: 'Polygon', coordinates: [] } }
    ];
  
    component.plotActivityBoundariesOnMap(activityBoundaries);
  
    expect(component.createGeoJSON).toHaveBeenCalledWith(
      jasmine.objectContaining({ type: 'Polygon' }),
      jasmine.objectContaining({
        style: jasmine.objectContaining({ color: '#3f3f3f' })
      })
    );
  });
  

});
