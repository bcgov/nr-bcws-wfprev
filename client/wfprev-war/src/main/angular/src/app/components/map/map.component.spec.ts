import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
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

// Mock AppConfigService
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
    return Promise.resolve(true); // Mock loadAppConfig to return a resolved promise
  }
}

// Mock CodeTableServices
class MockCodeTableServices {
  fetchCodeTable() {
    return of({ wfprev: 'mockedData' }); // Mock fetchCodeTable to return an observable
  }
}

// Mock ProjectService
class MockProjectService {
  fetchProjects() {
    return of({ wfprev: 'mockedData' }); // Mock fetchProjects to return an observable
  }
}

fdescribe('MapComponent', () => {
  let component: MapComponent;
  let fixture: ComponentFixture<MapComponent>;
  let mapConfigServiceMock: jasmine.SpyObj<MapConfigService>;
  let mapServiceMock: jasmine.SpyObj<MapService>;
  let mapContainer: jasmine.SpyObj<ElementRef>;

  beforeEach(() => {
    mapConfigServiceMock = jasmine.createSpyObj<MapConfigService>('MapConfigService', ['getMapConfig']);
    mapServiceMock = jasmine.createSpyObj<MapService>('MapService', ['getMapIndex', 'setMapIndex', 'createSMK']);
    mapContainer = jasmine.createSpyObj('ElementRef', ['nativeElement']);
    mapConfigServiceMock.getMapConfig.and.returnValue(Promise.resolve({ theme: 'testTheme' }));

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
      ],
      schemas: [NO_ERRORS_SCHEMA],
    });

    fixture = TestBed.createComponent(MapComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();

    spyOn(console, 'error'); // Spy on console.error for error handling tests
  });

  describe('ngAfterViewInit', () => {
    it('should initialize map if mapContainer is available', fakeAsync(() => {
      mapContainer.nativeElement = document.createElement('div');
      component.mapContainer = mapContainer;

      mapServiceMock.getMapIndex.and.returnValue(0);
      mapConfigServiceMock.getMapConfig.and.returnValue(Promise.resolve('mockConfig'));

      component.ngAfterViewInit();
      tick(); // Simulate async completion

      expect(mapServiceMock.getMapIndex).toHaveBeenCalled();
      expect(mapServiceMock.setMapIndex).toHaveBeenCalledWith(1);
      expect(mapServiceMock.createSMK).toHaveBeenCalled();
    }));

    it('should not initialize map if mapContainer is not available', fakeAsync(() => {
      component.mapContainer = mapContainer; // No nativeElement available

      component.ngAfterViewInit();
      tick(); // Simulate async completion

      expect(mapServiceMock.createSMK).not.toHaveBeenCalled();
    }));
  });

  describe('initMap', () => {
    it('should initialize map with correct config and device settings', fakeAsync(() => {
      mapContainer.nativeElement = document.createElement('div');
      component.mapContainer = mapContainer;

      const mockConfig = { theme: 'testTheme' };
      mapConfigServiceMock.getMapConfig.and.returnValue(Promise.resolve(mockConfig));
      mapServiceMock.getMapIndex.and.returnValue(1);

      component.ngAfterViewInit();
      tick(); // Simulate async completion

      expect(component.mapConfig).toEqual([
        mockConfig,
        { viewer: { device: 'desktop' } },
        'theme=wf',
        '?',
      ]);
      expect(mapServiceMock.createSMK).toHaveBeenCalled();
    }));

    it('should handle errors while loading mapConfig', fakeAsync(() => {
      mapContainer.nativeElement = document.createElement('div');
      component.mapContainer = mapContainer;

      mapConfigServiceMock.getMapConfig.and.returnValue(Promise.reject('Config Load Error'));

      component.ngAfterViewInit();
      tick(); // Simulate async completion

      expect(console.error).toHaveBeenCalledWith('Error loading map:', 'Config Load Error');
    }));
  });

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
      mapServiceMock.getMapIndex.and.returnValue(5);

      component.ngAfterViewInit();
      tick(); // Simulate async completion

      expect(component.mapIndex).toBe(6);
      expect(mapServiceMock.setMapIndex).toHaveBeenCalledWith(6);
    }));
  });
});
