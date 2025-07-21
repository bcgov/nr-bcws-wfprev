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
import { ProjectFiscalsComponent } from './project-fiscals/project-fiscals.component';
import { EditProjectTabIndexes } from 'src/app/utils';

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
      has: (key: string) => ['tab', 'fiscalGuid', 'name'].includes(key),
      get: (key: string) => {
        if (key === 'tab') return 'fiscal';
        if (key === 'fiscalGuid') return 'abc-123';
        if (key === 'name') return 'Test Project';
        return null;
      },
      getAll: () => [],
      keys: [],
    };

    mockActivatedRoute = {
      queryParamMap: of(mockParamMap),
      snapshot: {
        queryParamMap: mockParamMap
      } as any
    };

    await TestBed.configureTestingModule({
      imports: [EditProjectComponent, BrowserAnimationsModule, HttpClientModule],
      providers: [
        { provide: ActivatedRoute, useValue: mockActivatedRoute },
        { provide: AppConfigService, useClass: MockAppConfigService },
        { provide: MockProjectService, useClass: MockProjectService },
        { provide: Router, useValue: { navigate: jasmine.createSpy('navigate') } },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(EditProjectComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should activate Fiscal tab and call loadFiscalComponent if tab=fiscal in query params', () => {
    const spy = spyOn(component, 'loadFiscalComponent');

    component.ngOnInit();

    expect(component.focusedFiscalId).toEqual('abc-123');
    expect(component.selectedTabIndex).toEqual(EditProjectTabIndexes.Fiscal);
    expect(spy).toHaveBeenCalled();
  });

//   fdescribe('EditProjectComponent', () => {
//   it('should render the Details tab', async () => {
//     component.selectedTabIndex = EditProjectTabIndexes.Details;
//     fixture.detectChanges();

//     await fixture.whenStable();  // ✅ wait for tabs to render
//     fixture.detectChanges();     // ✅ render the tab contents

//     const detailsTab = fixture.debugElement.query(By.css('.details-tab'));
//     expect(detailsTab).toBeTruthy();
//   });
// });

  // fdescribe('EditProjectComponent', () => {
  //   it('should display ProjectDetailsComponent inside the Details tab', async () => {
  //     component.selectedTabIndex = EditProjectTabIndexes.Details;
  //     fixture.detectChanges();

  //     await fixture.whenStable();
  //     fixture.detectChanges(); // run again after stabilization

  //     const projectDetailsComponent = fixture.debugElement.query(By.directive(ProjectDetailsComponent));
  //     expect(projectDetailsComponent).toBeTruthy();
  //   });
  // });

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

  it('should load ProjectFiscalsComponent and assign focusedFiscalId in loadFiscalsTab', () => {
    const mockComponentRef = {
      instance: {}
    } as any;

    const mockContainer = {
      clear: jasmine.createSpy('clear'),
      createComponent: jasmine.createSpy('createComponent').and.returnValue(mockComponentRef)
    };

    component.fiscalsContainer = mockContainer as any;
    component.focusedFiscalId = 'abc-123';

    component.loadFiscalsTab();

    expect(mockContainer.clear).toHaveBeenCalled();
    expect(mockContainer.createComponent).toHaveBeenCalledWith(ProjectFiscalsComponent);
    expect(mockComponentRef.instance.focusedFiscalId).toBe('abc-123');
  });

  it('should update URL with fiscal tab and fiscalGuid when switching to fiscal tab', () => {
    const navigateSpy = component['router'].navigate as jasmine.Spy;

    component.projectFiscalsComponentRef = {
      instance: {
        currentFiscalGuid: 'abc-123'
      }
    } as any;

    component.onTabChange({ index: 1 });

    expect(component.selectedTabIndex).toBe(1);
    expect(navigateSpy).toHaveBeenCalledWith([], {
      relativeTo: component['route'],
      queryParams: jasmine.objectContaining({
        tab: 'fiscal',
        fiscalGuid: 'abc-123'
      }),
      queryParamsHandling: 'merge'
    });
  });

  it('should update URL and remove fiscalGuid when switching to details tab', () => {
    const navigateSpy = component['router'].navigate as jasmine.Spy;

    component.projectFiscalsComponentRef = {
      instance: {
        currentFiscalGuid: 'abc-123'
      }
    } as any;

    component.onTabChange({ index: 0 });

    expect(component.selectedTabIndex).toBe(0);
    expect(navigateSpy).toHaveBeenCalledWith([], {
      relativeTo: component['route'],
      queryParams: jasmine.objectContaining({
        tab: 'details',
        fiscalGuid: null
      }),
      queryParamsHandling: 'merge'
    });
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
            }),
            snapshot: {
              queryParamMap: {
                has: () => false,
                get: () => null,
                getAll: () => [],
                keys: []
              }
            }
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

  it('should initialize ProjectFiscalsComponent in loadFiscalComponent', async () => {
    const mockInstance = {
      focusedFiscalId: null,
      loadProjectFiscals: jasmine.createSpy('loadProjectFiscals')
    };

    const mockComponentRef = { instance: mockInstance };
    const mockContainer = {
      clear: jasmine.createSpy('clear'),
      createComponent: jasmine.createSpy('createComponent').and.returnValue(mockComponentRef)
    };

    component.fiscalsContainer = mockContainer as any;
    component.projectFiscalsComponentRef = null;
    component.focusedFiscalId = 'abc-123';

    spyOn(component, 'getProjectFiscalsComponent').and.returnValue(
      Promise.resolve({ ProjectFiscalsComponent })
    );

    await component.loadFiscalComponent();

    expect(mockContainer.clear).toHaveBeenCalled();
    expect(mockContainer.createComponent).toHaveBeenCalledWith(ProjectFiscalsComponent);
    expect(mockInstance.focusedFiscalId as any).toBe('abc-123');
    expect(mockInstance.loadProjectFiscals).toHaveBeenCalled();
  });
});