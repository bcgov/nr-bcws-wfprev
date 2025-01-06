import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MapComponent } from './map.component';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import * as L from 'leaflet';
import { AppConfigService } from 'src/app/services/app-config.service';
import { of } from 'rxjs';

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

  beforeEach(async () => {
    mapMock = {
      fitBounds: jasmine.createSpy('fitBounds'),
      invalidateSize: jasmine.createSpy('invalidateSize'),
      addLayer: jasmine.createSpy('addLayer'),
    };

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
