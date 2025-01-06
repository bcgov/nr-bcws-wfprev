import { ComponentFixture, TestBed } from '@angular/core/testing';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { AppConfigService } from 'src/app/services/app-config.service';
import { LibraryConfig } from 'src/app/config/library-config';
import { ResizablePanelComponent } from 'src/app/components/resizable-panel/resizable-panel.component';

// Mock configuration
const mockApplicationConfig = {
  application: { acronym: "WFPREV", version: "0.0.0", baseUrl: "", environment: "DEV" },
  rest: {},
  webade: { oauth2Url: "", clientId: "", authScopes: "" },
};

const mockLibraryConfig = {
  configurationPath: '/mock/config/path',
};

// Mock AppConfigService
class MockAppConfigService {
  private appConfig = mockApplicationConfig;

  async loadAppConfig(): Promise<void> {
    return Promise.resolve();
  }

  getConfig(): any {
    return this.appConfig;
  }
}

describe('ResizablePanelComponent', () => {
  let component: ResizablePanelComponent;
  let fixture: ComponentFixture<ResizablePanelComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        BrowserAnimationsModule, // Necessary for Angular Material animations
      ],
      providers: [
        provideHttpClient(), // Add HTTP client provider
        provideHttpClientTesting(), // Add HTTP client testing provider
        { 
          provide: AppConfigService, 
          useClass: MockAppConfigService, 
        },
        { 
          provide: LibraryConfig, 
          useValue: mockLibraryConfig, 
        },
      ],
    }).compileComponents();

    const appConfigService = TestBed.inject(AppConfigService);
    await appConfigService.loadAppConfig(); // Simulate configuration loading

    fixture = TestBed.createComponent(ResizablePanelComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize with default values', () => {
    expect(component.panelWidth).toBe('50vw'); // Default panel width
    expect(component.breakpoints).toEqual([5, 50, 90]); // Default breakpoints
    expect(component.selectedTabIndex).toBe(0); // Default selected tab index
  });

  it('should resize the panel and emit panelResized event', () => {
    spyOn(component.panelResized, 'emit'); // Spy on the EventEmitter
    component.resizePanel(75); // Resize to 75vw
    expect(component.panelWidth).toBe('75vw');
    expect(component.panelResized.emit).toHaveBeenCalled(); // Ensure the event is emitted
  });

  it('should update selectedTabIndex when selectTab is called', () => {
    component.selectTab(2); // Select the third tab
    expect(component.selectedTabIndex).toBe(2); // Check updated tab index
  });
});
