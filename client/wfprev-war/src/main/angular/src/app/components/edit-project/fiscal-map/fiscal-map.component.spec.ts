import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { FiscalMapComponent } from './fiscal-map.component';
import { ProjectService } from 'src/app/services/project-services';
import { ActivatedRoute } from '@angular/router';
import { of } from 'rxjs';
import * as L from 'leaflet';

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
  let originalLControl: any;

  let geoJsonAddToSpy: jasmine.Spy;

  beforeEach(async () => {
    // Add fake map container to DOM
    const container = document.createElement('div');
    container.setAttribute('id', 'fiscalMap');
    document.body.appendChild(container);
  
    originalLControl = L.control;
  
    geoJsonAddToSpy = jasmine.createSpy('addTo').and.returnValue({}); // shared spy
  
    mockMapInstance = {
      setView: jasmine.createSpy('setView'),
      fitBounds: jasmine.createSpy('fitBounds'),
      remove: jasmine.createSpy('remove'), // important for ngOnDestroy
      addLayer: jasmine.createSpy('addLayer'),
      on: jasmine.createSpy('on'),
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
  
    spyOn(L, 'map').and.returnValue(mockMapInstance);
    spyOn(L, 'marker').and.returnValue({ addTo: jasmine.createSpy('addTo') } as any);
    spyOn(L, 'tileLayer').and.returnValue({ addTo: jasmine.createSpy('addTo') } as any);
    spyOn(L, 'geoJSON').and.returnValue({ addTo: geoJsonAddToSpy } as any); // single spy
    spyOn(L, 'featureGroup').and.returnValue({
      getBounds: () => ({})
    } as any);
    spyOn(L as any, 'control').and.returnValue({
      addTo: jasmine.createSpy('addTo'),
      getPosition: jasmine.createSpy('getPosition'),
      setPosition: jasmine.createSpy('setPosition'),
      getContainer: jasmine.createSpy('getContainer'),
      remove: jasmine.createSpy('remove'),
      options: {}
    });
  
    const mockProjectService = new MockProjectService();
  
    await TestBed.configureTestingModule({
      imports: [FiscalMapComponent],
      providers: [
        { provide: ProjectService, useValue: mockProjectService },
        { provide: ActivatedRoute, useValue: mockActivatedRoute }
      ]
    }).compileComponents();
  
    fixture = TestBed.createComponent(FiscalMapComponent);
    component = fixture.componentInstance;
  });
  

  afterEach(() => {
    const container = document.getElementById('fiscalMap');
    if (container) container.remove();
    (L as any).control = originalLControl;
    fixture.destroy();
  });

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
    const spy = spyOn<any>(component, 'initMap');
    component.ngAfterViewInit();
    tick();
    expect(spy).toHaveBeenCalled();
  }));

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

  it('should plot activity boundaries for different fiscal years', () => {
    const mockFitBounds = jasmine.createSpy('fitBounds');
    const mockRemove = jasmine.createSpy('remove');
    (component as any).map = { fitBounds: mockFitBounds, remove: mockRemove };
  
    component.plotBoundariesOnMap([
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
  
    expect(L.geoJSON).toHaveBeenCalledTimes(3);
    expect(geoJsonAddToSpy).toHaveBeenCalledTimes(3);
    expect(mockFitBounds).toHaveBeenCalled();
  });
  
  

  it('should handle GeometryCollection in plotBoundariesOnMap', () => {
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
  
    component.plotBoundariesOnMap(mockBoundaries);
  
    expect(L.geoJSON).toHaveBeenCalledTimes(2);
    expect(geoJsonAddToSpy).toHaveBeenCalledTimes(2);
  });
  

  it('should skip boundary items without geometry in plotBoundariesOnMap', () => {
    (component as any).map = mockMapInstance;
  
    component.plotBoundariesOnMap([
      {
        fiscalYear: component.currentFiscalYear,
        boundary: [{ notGeometry: true }]
      }
    ]);
  
    expect(L.geoJSON).not.toHaveBeenCalled();
  });

  it('should plot project boundary with normal and GeometryCollection', () => {
    (component as any).map = mockMapInstance;
  
    const boundary = [
      { geometry: { type: 'Polygon', coordinates: [] } },
      {
        geometry: {
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
    expect(L.geoJSON).toHaveBeenCalledTimes(3);
    expect(geoJsonAddToSpy).toHaveBeenCalledTimes(3);
  });

  it('should handle forkJoin results correctly in getAllActivitiesBoundaries', fakeAsync(() => {
    const mockProjectGuid = 'mock-guid';
    component['projectGuid'] = mockProjectGuid;
    component['map'] = mockMapInstance;
  
    const mockFiscal = {
      fiscalYear: component.currentFiscalYear,
      projectPlanFiscalGuid: 'fiscal-1'
    };
  
    const mockActivity = {
      activityGuid: 'activity-1',
      fiscalYear: mockFiscal.fiscalYear,
      projectPlanFiscalGuid: mockFiscal.projectPlanFiscalGuid
    };
  
    const mockBoundary = {
      _embedded: {
        activityBoundary: [{ geometry: { type: 'Polygon', coordinates: [] } }]
      }
    };
  
    const projectService = TestBed.inject(ProjectService) as unknown as MockProjectService;
  
    // Override getProjectFiscalsByProjectGuid
    projectService.getProjectFiscalsByProjectGuid.and.returnValue(of({
      _embedded: {
        projectFiscals: [mockFiscal]
      }
    }));
  
    // Override getFiscalActivities
    projectService.getFiscalActivities.and.returnValue(of({
      _embedded: {
        activities: [mockActivity]
      }
    }));
  
    // Override getActivityBoundaries
    projectService.getActivityBoundaries.and.returnValue(of(mockBoundary));
  
    // Stub projectBoundary as empty
    component['projectBoundary'] = [];
  
    const plotSpy = spyOn(component as any, 'plotBoundariesOnMap');
    const coordSpy = spyOn(component, 'getProjectCoordinates');
  
    fixture.detectChanges(); // trigger ngOnInit()
    tick(); // resolve observables
  
    expect(projectService.getProjectFiscalsByProjectGuid).toHaveBeenCalled();
    expect(projectService.getFiscalActivities).toHaveBeenCalled();
    expect(projectService.getActivityBoundaries).toHaveBeenCalled();
    expect(plotSpy).toHaveBeenCalledWith(jasmine.any(Array));
    expect(coordSpy).not.toHaveBeenCalled();
  }));
  
  it('should call getProjectCoordinates if no polygons exist', fakeAsync(() => {
    const mockProjectGuid = 'mock-guid';
    component['projectGuid'] = mockProjectGuid;
    component['map'] = mockMapInstance;
  
    const mockFiscal = {
      fiscalYear: component.currentFiscalYear,
      projectPlanFiscalGuid: 'fiscal-1'
    };
  
    const mockActivity = {
      activityGuid: 'activity-1',
      fiscalYear: mockFiscal.fiscalYear,
      projectPlanFiscalGuid: mockFiscal.projectPlanFiscalGuid
    };
  
    const emptyBoundary = {
      _embedded: {
        activityBoundary: [] // no geometry
      }
    };
  
    const projectService = TestBed.inject(ProjectService) as unknown as MockProjectService;
  
    projectService.getProjectFiscalsByProjectGuid.and.returnValue(of({
      _embedded: {
        projectFiscals: [mockFiscal]
      }
    }));
  
    projectService.getFiscalActivities.and.returnValue(of({
      _embedded: {
        activities: [mockActivity]
      }
    }));
  
    projectService.getActivityBoundaries.and.returnValue(of(emptyBoundary));
  
    component['projectBoundary'] = []; // no project polygons
  
    const plotSpy = spyOn(component as any, 'plotBoundariesOnMap');
    const coordSpy = spyOn(component, 'getProjectCoordinates');
  
    fixture.detectChanges(); // trigger ngOnInit()
    tick(); // flush observables
  
    expect(plotSpy).not.toHaveBeenCalled();
    expect(coordSpy).toHaveBeenCalled();
  }));
  
  
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
      const mockGetBounds = jasmine.createSpy().and.returnValue(
        L.latLngBounds([[48, -125], [49, -123]])
      );
  
      (L.geoJSON as jasmine.Spy).and.callFake(() => ({
        getBounds: mockGetBounds
      }) as any);
  
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

});

