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

  beforeEach(async () => {
    // Add fake map container to DOM
    const container = document.createElement('div');
    container.setAttribute('id', 'fiscalMap');
    document.body.appendChild(container);

    originalLControl = L.control;

    mockMapInstance = {
      setView: jasmine.createSpy('setView'),
      fitBounds: jasmine.createSpy('fitBounds'),
      remove: jasmine.createSpy('remove'),
      addLayer: jasmine.createSpy('addLayer'),
      zoomControl: {
        setPosition: jasmine.createSpy('setPosition')
      }
    };

    spyOn(L, 'map').and.returnValue(mockMapInstance);
    spyOn(L, 'marker').and.returnValue({ addTo: jasmine.createSpy('addTo') } as any);
    spyOn(L, 'tileLayer').and.returnValue({ addTo: jasmine.createSpy('addTo') } as any);
    spyOn(L, 'geoJSON').and.returnValue({ addTo: jasmine.createSpy('addTo') } as any);
    spyOn(L, 'featureGroup').and.returnValue({
      getBounds: () => ({})
    } as any);
    (L as any).control = jasmine.createSpy('control').and.returnValue({
      addTo: jasmine.createSpy('addTo'),
      onAdd: jasmine.createSpy('onAdd')
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
});
