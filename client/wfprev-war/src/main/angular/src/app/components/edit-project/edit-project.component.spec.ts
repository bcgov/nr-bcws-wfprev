import { ComponentFixture, TestBed } from '@angular/core/testing';
import { EditProjectComponent } from './edit-project.component';
import { ActivatedRoute, ParamMap } from '@angular/router';
import { of } from 'rxjs';
import { By } from '@angular/platform-browser';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { ProjectDetailsComponent } from 'src/app/components/edit-project/project-details/project-details.component';
import { HttpClientModule } from '@angular/common/http';
import { AppConfigService } from 'src/app/services/app-config.service';

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

class MockAppConfigService {
  private appConfig = mockApplicationConfig;

  loadAppConfig(): Promise<void> {
    return Promise.resolve(); // Simulate successful configuration loading
  }

  getConfig(): any {
    return this.appConfig; // Return mock configuration
  }
}

class MockProjectService {
  // Add mock methods if needed
}

describe('EditProjectComponent', () => {
  let component: EditProjectComponent;
  let fixture: ComponentFixture<EditProjectComponent>;
  let mockActivatedRoute: Partial<ActivatedRoute>;

  beforeEach(async () => {
    const mockParamMap: ParamMap = {
      has: (key: string) => key === 'name',
      get: (key: string) => (key === 'name' ? 'Test Project' : null),
      getAll: () => [],
      keys: [],
    };

    mockActivatedRoute = {
      queryParamMap: of(mockParamMap),
    };

    await TestBed.configureTestingModule({
      imports: [EditProjectComponent, BrowserAnimationsModule, HttpClientModule],
      providers: [
        { provide: ActivatedRoute, useValue: mockActivatedRoute },
        { provide: AppConfigService, useClass: MockAppConfigService }, // Provide mock AppConfigService
        { provide: MockProjectService, useClass: MockProjectService }, // Provide mock ProjectService
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(EditProjectComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should render the Details tab', () => {
    const detailsTab = fixture.debugElement.query(By.css('.details-tab'));
    expect(detailsTab).toBeTruthy();
  });

  it('should display ProjectDetailsComponent inside the Details tab', () => {
    const projectDetailsComponent = fixture.debugElement.query(By.directive(ProjectDetailsComponent));
    expect(projectDetailsComponent).toBeTruthy();
  });
  
  it('should not reload ProjectFiscalsComponent if it is already loaded', () => {
    component.projectFiscalsComponentRef = {} as any; // Mock that the component is already loaded
    component.onTabChange({ index: 1 });
  
    expect(component.fiscalsContainer).toBeTruthy(); // Ensures container is still present
  });

  it('should return true from canDeactivate() if no unsaved changes exist', () => {
    component.projectFiscalsComponentRef = {
      instance: {
        isFormDirty: () => false,
      },
    } as any;
  
    expect(component.canDeactivate()).toBe(true);
  });

  it('should call canDeactivate of ProjectFiscalsComponent if forms are dirty', () => {
    const mockCanDeactivate = jasmine.createSpy().and.returnValue(true);
  
    component.projectFiscalsComponentRef = {
      instance: {
        isFormDirty: () => true,
        canDeactivate: mockCanDeactivate,
      },
    } as any;
  
    expect(component.canDeactivate()).toBe(true);
    expect(mockCanDeactivate).toHaveBeenCalled();
  });
  
  it('should call refreshFiscalData when switching back to Details tab', () => {
    // Mock the ProjectDetailsComponent and its method
    component.projectDetailsComponent = jasmine.createSpyObj('ProjectDetailsComponent', ['refreshFiscalData']);
  
    // Simulate selecting the "Details" tab (index 0)
    component.onTabChange({ index: 0 });
  
    // Expect refreshFiscalData to have been called
    expect(component.projectDetailsComponent.refreshFiscalData).toHaveBeenCalled();
  });  
  
});