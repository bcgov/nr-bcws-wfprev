import { TestBed } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { Router } from '@angular/router';
import { PrevAuthGuard } from './services/util/prev-auth-guard';
import { ResourcesRoutes } from 'src/app/utils';
import { ErrorPageComponent } from './components/error-page/error-page/error-page.component';
import { ROUTING } from './app.routing'; 

describe('App Routing Module', () => {
  let router: Router;

  // Mock the PrevAuthGuard service
  class MockPrevAuthGuard {
    canActivate() {
      return true; // Allow navigation for testing purposes
    }
  }

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [
        RouterTestingModule.withRoutes([]),
        ROUTING // Import the actual routing module
      ],
      providers: [
        { provide: PrevAuthGuard, useClass: MockPrevAuthGuard } // Provide the mock guard
      ]
    });
    router = TestBed.inject(Router);
  });

  it('should have correct routes defined', () => {
    const routes = router.config;
    expect(routes.length).toBe(4);
  });

  it('should define route for LIST path', () => {
    const listRoute = router.config.find(r => r.path === ResourcesRoutes.LIST);
    expect(listRoute).toBeTruthy();
    expect(listRoute?.path).toBe(ResourcesRoutes.LIST);
    expect(listRoute?.data).toEqual({ scopes: [['WFPREV.WFPREV_ADMIN']] });
  });

  it('should define route for MAP path', () => {
    const mapRoute = router.config.find(r => r.path === ResourcesRoutes.MAP);
    expect(mapRoute).toBeTruthy();
    expect(mapRoute?.path).toBe(ResourcesRoutes.MAP);
    expect(mapRoute?.data).toEqual({ scopes: [['WFPREV.WFPREV_ADMIN']] });
  });

  it('should define route for ERROR_PAGE path', () => {
    const errorPageRoute = router.config.find(r => r.path === ResourcesRoutes.ERROR_PAGE);
    expect(errorPageRoute).toBeTruthy();
    expect(errorPageRoute?.path).toBe(ResourcesRoutes.ERROR_PAGE);
    expect(errorPageRoute?.component).toBe(ErrorPageComponent);
    expect(errorPageRoute?.pathMatch).toBe('full');
  });

  it('should define default redirect route to LANDING', () => {
    const defaultRoute = router.config.find(r => r.path === '');
    expect(defaultRoute).toBeTruthy();
    expect(defaultRoute?.redirectTo).toBe(ResourcesRoutes.LANDING);
    expect(defaultRoute?.data).toEqual({ scopes: [['WFPREV.WFPREV_ADMIN']] });
  });

});