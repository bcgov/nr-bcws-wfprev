import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MapComponent } from './map.component';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import * as L from 'leaflet';
import { AppConfigService } from 'src/app/services/app-config.service';
import { of } from 'rxjs';
import { ChangeDetectorRef } from '@angular/core';

// Mock ApplicationConfig
const mockApplicationConfig = {
  application: {
    baseUrl: 'http://test.com',
    lazyAuthenticate: false, // Ensure this property is defined
    enableLocalStorageToken: true,
    acronym: 'TEST',
    environment: 'DEV',
    version: '1.0.0',
  },
  webade: {
    oauth2Url: 'http://oauth.test',
    clientId: 'test-client',
    authScopes: 'TEST.*',
  },
  rest: {},
};

// Mock AppConfigService
class MockAppConfigService {
  private appConfig = mockApplicationConfig;

  loadAppConfig(): Promise<void> {
    return Promise.resolve(); // Simulate successful configuration loading
  }

  getConfig(): any {
    return this.appConfig; // Return mock configuration
  }
}

describe('MapComponent', () => {
  let component: MapComponent;
  let fixture: ComponentFixture<MapComponent>;
  let mapMock: Partial<L.Map>;
  let cdrMock: jasmine.SpyObj<ChangeDetectorRef>;
  let mockAppConfigService: jasmine.SpyObj<AppConfigService>;

  beforeEach(async () => {
    mapMock = {
      fitBounds: jasmine.createSpy('fitBounds'),
      invalidateSize: jasmine.createSpy('invalidateSize'),
      addLayer: jasmine.createSpy('addLayer'),
    };
  
    cdrMock = jasmine.createSpyObj('ChangeDetectorRef', ['markForCheck']);
    mockAppConfigService = jasmine.createSpyObj('AppConfigService', ['getConfig']);
    mockAppConfigService.getConfig.and.returnValue({
      application: {
        acronym: 'TEST',
        version: '1.0',
        baseUrl: 'https://test.example.com',
        environment: 'test',
        lazyAuthenticate: false,
        enableLocalStorageToken: true,
        allowLocalExpiredToken: false,
        localStorageTokenKey: 'test-token-key'
      },
      rest: {
        someServiceUrl: 'https://rest.example.com'
      },
      webade: {
        oauth2Url: 'https://auth.example.com',
        clientId: 'test-client-id',
        authScopes: 'read write',
        enableCheckToken: true,
        checkTokenUrl: 'https://auth.example.com/check-token'
      }
    });
  
    spyOn(L, 'map').and.returnValue(mapMock as L.Map);
  
    await TestBed.configureTestingModule({
      imports: [
        MapComponent,
        BrowserAnimationsModule,
        HttpClientTestingModule,
      ],
      providers: [
        { provide: AppConfigService, useClass: MockAppConfigService },
      ],
    }).compileComponents();

    // Simulate configuration loading
    const appConfigService = TestBed.inject(AppConfigService);
    await appConfigService.loadAppConfig();

    fixture = TestBed.createComponent(MapComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize the Leaflet map', () => {
    expect(L.map).toHaveBeenCalledWith('map');
    expect(mapMock.fitBounds).toHaveBeenCalledWith([
      [48.3, -139.1],
      [60.0, -114.0],
    ]);
  });

  it('should call invalidateSize() on panel resize', () => {
    component['map'] = mapMock as L.Map;
    component.onPanelResized();
    expect(mapMock.invalidateSize).toHaveBeenCalled();
  });
});
