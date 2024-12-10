import { ComponentFixture, TestBed } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { AppComponent } from './app.component';
import { Router } from '@angular/router';
import { By } from '@angular/platform-browser';
import { Location } from '@angular/common';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { AppConfigService } from './services/app-config.service';
import { ApplicationConfig } from './interfaces/application-config';
import { LibraryConfig } from './config/library-config';

// Create a mock configuration
const mockApplicationConfig: ApplicationConfig = {
  application: { acronym: "WFPREV", version: "0.0.0", baseUrl: "", environment: "DEV" },
  rest: {},
  webade: { oauth2Url: "", clientId: "", authScopes: ""}

};

// Mock AppConfigService
class MockAppConfigService {
  private appConfig: ApplicationConfig = mockApplicationConfig;

  async loadAppConfig(): Promise<void> {
    // Simulate config loading
    return Promise.resolve();
  }

  getConfig(): ApplicationConfig {
    return this.appConfig;
  }
}

const mockLibraryConfig = {
  configurationPath: '/mock/config/path'
};


describe('AppComponent', () => {
  let router: Router;
  let location: Location;
  let fixture: ComponentFixture<AppComponent>;
  let app: AppComponent;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RouterTestingModule.withRoutes([])], // Setup RouterTestingModule properly
      declarations: [AppComponent],
      providers: [provideHttpClient(), // Add HTTP client provider
      provideHttpClientTesting(),
      { 
        provide: AppConfigService, 
        useClass: MockAppConfigService 
      },
      { 
        provide: LibraryConfig, 
        useValue: mockLibraryConfig 
      }]
    }).compileComponents();

    router = TestBed.inject(Router);
    location = TestBed.inject(Location);
    fixture = TestBed.createComponent(AppComponent);
    app = fixture.componentInstance;
    router.initialNavigation();
  });

  it('should create the app', () => {
    expect(app).toBeTruthy();
  });

  it('should navigate to "list" route and set active route', async () => {
    spyOn(router, 'navigate');
    app.setActive('list');
    expect(app.activeRoute).toBe('list');
    expect(router.navigate).toHaveBeenCalledWith(['list']);
  });

  it('should navigate to "map" route and set active route', async () => {
    spyOn(router, 'navigate');
    app.setActive('map');
    expect(app.activeRoute).toBe('map');
    expect(router.navigate).toHaveBeenCalledWith(['map']);
  });

  it('should add "active" class to the map menu item when activeRoute is "map"', () => {
    app.activeRoute = 'map';
    fixture.detectChanges();

    const mapMenuItem = fixture.debugElement.query(
      By.css('.span-class[class="span-class active"]')  // Or more simply: '.span-class.active'
    );

    expect(mapMenuItem).not.toBeNull();
    expect(mapMenuItem.nativeElement.classList).toContain('active');
  });
});
