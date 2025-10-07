import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ResizablePanelComponent } from './resizable-panel.component';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { Component } from '@angular/core';
import { AppConfigService } from 'src/app/services/app-config.service';

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

// Mock ProjectService
class MockProjectService {
  // Add mock methods if needed
}

// Mock MapComponent that uses ResizablePanelComponent
@Component({
  selector: 'app-map',
  template: `
    <div class="map-container">
      <wfprev-resizable-panel (panelResized)="onPanelResized()">
        <div [innerHTML]="panelContent"></div>
      </wfprev-resizable-panel>
      <div class="map-content">
        <h2>Mock Map Component</h2>
        <div id="map"></div>
      </div>
    </div>
  `,
})
class MockMapComponent {
  panelContent: string = `<p>Mock Panel Content</p>`;
  async loadAppConfig(): Promise<void> {
    // Simulate config loading
    return Promise.resolve();
  }

  onPanelResized(): void {
    // Mock method for panel resize handling
  }
}

describe('ResizablePanelComponent', () => {
  let component: ResizablePanelComponent;
  let fixture: ComponentFixture<ResizablePanelComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        BrowserAnimationsModule, // Angular Material animations
        HttpClientTestingModule, // Mock HTTP dependencies
        ResizablePanelComponent, // Import the standalone component
      ],
      providers: [
        { provide: AppConfigService, useClass: MockAppConfigService }, // Provide mock AppConfigService
        { provide: MockProjectService, useClass: MockProjectService }, // Provide mock ProjectService
      ],
    }).compileComponents();

    // Simulate configuration loading
    const appConfigService = TestBed.inject(AppConfigService); // Correctly inject the mock service
    await appConfigService.loadAppConfig();

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

  it('should delegate scroll event to projectList.onScroll', () => {
    component.projectList = {
      onScroll: jasmine.createSpy('onScroll')
    } as any;

    const mockEvent = new Event('scroll');

    component.onParentScroll(mockEvent);

    expect(component.projectList.onScroll).toHaveBeenCalledWith(mockEvent);
  });
});
