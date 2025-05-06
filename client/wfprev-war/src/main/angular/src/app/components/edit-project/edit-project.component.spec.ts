import { ComponentFixture, TestBed } from '@angular/core/testing';
import { EditProjectComponent } from './edit-project.component';
import { ActivatedRoute, ParamMap } from '@angular/router';
import { of } from 'rxjs';
import { By } from '@angular/platform-browser';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { ProjectDetailsComponent } from 'src/app/components/edit-project/project-details/project-details.component';
import { HttpClientModule } from '@angular/common/http';
import { AppConfigService } from 'src/app/services/app-config.service';
import { Router } from '@angular/router';

const mockApplicationConfig = {
  application: {
    baseUrl: 'http://test.com',
    lazyAuthenticate: false,
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
  private appConfig = {
    application: {
      baseUrl: 'http://test.com',
      lazyAuthenticate: false,
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

  loadAppConfig(): Promise<void> {
    return Promise.resolve();
  }

  getConfig(): any {
    return this.appConfig;
  }
}

class MockProjectService {

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
        { provide: AppConfigService, useClass: MockAppConfigService },
        { provide: MockProjectService, useClass: MockProjectService },
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
    component.projectFiscalsComponentRef = {} as any;
    component.onTabChange({ index: 1 });
  
    expect(component.fiscalsContainer).toBeTruthy();
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
    component.projectDetailsComponent = jasmine.createSpyObj<ProjectDetailsComponent>('ProjectDetailsComponent', ['refreshFiscalData']);
    component.onTabChange({ index: 0 });
  
    expect(component.projectDetailsComponent.refreshFiscalData).toHaveBeenCalled();
  });
  
});

describe('EditProjectComponent (extended coverage)', () => {
  let component: EditProjectComponent;
  let fixture: ComponentFixture<EditProjectComponent>;
  let router: Router;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [EditProjectComponent, BrowserAnimationsModule, HttpClientModule],
      providers: [
        { provide: Router, useValue: { navigate: jasmine.createSpy('navigate') } },
        {
          provide: ActivatedRoute,
          useValue: {
            queryParamMap: of({
              has: () => false,
              get: () => null,
              getAll: () => [],
              keys: []
            })
          }
        },
        { provide: AppConfigService, useClass: MockAppConfigService },
      ]
      
    }).compileComponents();

    fixture = TestBed.createComponent(EditProjectComponent);
    component = fixture.componentInstance;
    router = TestBed.inject(Router);
    fixture.detectChanges();
  });

  it('should have projectName as null by default', () => {
    expect(component.projectName).toBeNull();
  });

  it('should have activeRoute as empty string by default', () => {
    expect(component.activeRoute).toBe('');
  });

  it('should set activeRoute and navigate to list', () => {
    component.setActive('list');
    expect(component.activeRoute).toBe('list');
    expect(router.navigate).toHaveBeenCalledWith([jasmine.anything()]);
  });

  it('should set activeRoute and navigate to map', () => {
    component.setActive('map');
    expect(component.activeRoute).toBe('map');
    expect(router.navigate).toHaveBeenCalledWith([jasmine.anything()]);
  });

  it('should set activeRoute but not navigate for unknown menuItem', () => {
    component.setActive('unknown');
    expect(component.activeRoute).toBe('unknown');
    expect((router.navigate as jasmine.Spy).calls.count()).toBe(0);
  });

  it('should return true from canDeactivate if both components are not dirty', () => {
    component.projectDetailsComponent = { isFormDirty: () => false } as any;
    component.projectFiscalsComponentRef = { instance: { isFormDirty: () => false } } as any;
    expect(component.canDeactivate()).toBe(true);
  });

  it('should call canDeactivate on projectDetailsComponent if dirty', () => {
    const mockCanDeactivate = jasmine.createSpy().and.returnValue(true);
    component.projectDetailsComponent = {
      isFormDirty: () => true,
      canDeactivate: mockCanDeactivate
    } as any;
    expect(component.canDeactivate()).toBe(true);
    expect(mockCanDeactivate).toHaveBeenCalled();
  });

  it('should call canDeactivate on projectFiscalsComponentRef if dirty', () => {
    const mockCanDeactivate = jasmine.createSpy().and.returnValue(true);
    component.projectFiscalsComponentRef = {
      instance: {
        isFormDirty: () => true,
        canDeactivate: mockCanDeactivate
      }
    } as any;
    expect(component.canDeactivate()).toBe(true);
    expect(mockCanDeactivate).toHaveBeenCalled();
  });
});