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

describe('MapComponent', () => {
  let component: MapComponent;
  let fixture: ComponentFixture<MapComponent>;
  let mapConfigServiceMock: jasmine.SpyObj<MapConfigService>;
  let mapServiceMock: jasmine.SpyObj<MapService>;
  let mapContainer: jasmine.SpyObj<ElementRef>;

  beforeEach(() => {
    mapConfigServiceMock = jasmine.createSpyObj<MapConfigService>('MapConfigService', ['getMapConfig']);
    mapServiceMock = jasmine.createSpyObj<MapService>('MapService', ['getMapIndex', 'setMapIndex', 'createSMK','getSMKInstance']);
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

    spyOn(console, 'error'); // Spy on console.error for error handling tests
  });

  describe('ngAfterViewInit', () => {
    it('should initialize map if mapContainer is available', fakeAsync(() => {
      mapContainer.nativeElement = document.createElement('div');
      component.mapContainer = mapContainer;
    
      mapServiceMock.getMapIndex.and.returnValue(0);
      mapServiceMock.getSMKInstance.and.returnValue({
        $viewer: {
          map: {
            getBounds: () => ({
              pad: () => ({
                toBBoxString: () => 'mocked',
              }),
            }),
            fitBounds: jasmine.createSpy('fitBounds'),
          },
        },
      });
      
    
      mapConfigServiceMock.getMapConfig.and.returnValue(Promise.resolve('mockConfig'));
    
      component.ngAfterViewInit();
      tick();
      tick(500);  // simulate setTimeout
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
  
      mapServiceMock.getSMKInstance.and.returnValue({
        $viewer: {
          map: {
            getBounds: () => ({
              pad: () => ({
                toBBoxString: () => 'mocked',
              }),
            }),
            fitBounds: jasmine.createSpy('fitBounds'),
          },
        },
      });
  
      component.ngAfterViewInit();
      tick();      // resolves mapConfig promise
      tick(500);   // handles setTimeout in ngAfterViewInit
      flush();     // clears timers if any remain
  
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
      tick(500);
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
      mapServiceMock.getMapIndex.and.returnValue(5);
      mapServiceMock.getSMKInstance.and.returnValue({ $viewer: { map: { getBounds: () => ({ pad: () => [] }), fitBounds: () => {} } } });
  
      component.ngAfterViewInit();
      tick();
      tick(500);
  
      expect(component.mapIndex).toBe(6);
      expect(mapServiceMock.setMapIndex).toHaveBeenCalledWith(6);
    }));
  });
  
});
