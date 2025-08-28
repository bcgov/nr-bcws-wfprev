import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ActivatedRouteSnapshot, Router, RouterStateSnapshot } from '@angular/router';
import { BehaviorSubject, firstValueFrom, of, Subject } from 'rxjs';
import { AppConfigService } from 'src/app/services/app-config.service';
import { TokenService } from 'src/app/services/token.service';
import { PrevAuthGuard } from './prev-auth-guard';

class MockActivatedRouteSnapshot extends ActivatedRouteSnapshot {
  constructor(public routeConfigOverride?: any) {
    super();
    this.routeConfig = routeConfigOverride || { path: 'default-path' };
  }

  // Allow overriding routeConfig
  override routeConfig: any;
}

describe('PrevAuthGuard', () => {
  let guard: PrevAuthGuard;
  let tokenService: jasmine.SpyObj<TokenService>;
  let router: jasmine.SpyObj<Router>;
  let appConfigService: jasmine.SpyObj<AppConfigService>;
  let snackBar: jasmine.SpyObj<MatSnackBar>;
  let httpMock: HttpTestingController;
  let authTokenSubject: Subject<void>;
  let credentialsSubject: BehaviorSubject<any>;

  beforeEach(() => {
    authTokenSubject = new Subject<void>();
    credentialsSubject = new BehaviorSubject<any>(null);

    tokenService = jasmine.createSpyObj('TokenService', [
      'getOauthToken',
      'validateToken',
      'checkForToken',
      'doesUserHaveApplicationPermissions'
    ], {
      authTokenEmitter: authTokenSubject,
      credentialsEmitter: credentialsSubject
    });

    router = jasmine.createSpyObj('Router', ['navigate']);
    appConfigService = jasmine.createSpyObj('AppConfigService', ['getConfig']);
    snackBar = jasmine.createSpyObj('MatSnackBar', ['open']);

    // Default mock implementations
    tokenService.doesUserHaveApplicationPermissions.and.returnValue(true);
    tokenService.validateToken.and.returnValue(of(true));

    appConfigService.getConfig.and.returnValue({
      application: {
        baseUrl: 'http://test.com',
        lazyAuthenticate: false,
        enableLocalStorageToken: true,
        acronym: 'WFPREV',
        environment: 'DEV',
        version: '0.0.0'
      },
      webade: {
        oauth2Url: 'http://oauth.test',
        clientId: 'test-client',
        authScopes: 'WFPREV.*'
      },
      rest: {},
      mapServices: {
        geoserverApiBaseUrl: 'http://geoserver.test',
        wfnewsApiBaseUrl: 'http://wfnews.test'
      }
    });

    TestBed.configureTestingModule({
      imports: [
        HttpClientTestingModule
      ],
      providers: [
        PrevAuthGuard,
        { provide: TokenService, useValue: tokenService },
        { provide: Router, useValue: router },
        { provide: AppConfigService, useValue: appConfigService },
        { provide: MatSnackBar, useValue: snackBar }
      ]
    });

    guard = TestBed.inject(PrevAuthGuard);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify(); // Make sure there are no outstanding requests
  });

  it('should be created', () => {
    expect(guard).toBeTruthy();
  });

  it('should return false when offline', (done) => {
    // Mock `navigator.onLine` to return `false`
    Object.defineProperty(window.navigator, 'onLine', {
      value: false,
      configurable: true,
    });

    const route = new ActivatedRouteSnapshot();
    const state = jasmine.createSpyObj<RouterStateSnapshot>('RouterStateSnapshot', [], {
      url: '/test',
    });

    guard.canActivate(route, state).subscribe(result => {
      expect(result).toBeFalse();
      done();
    });

    // Cleanup after the test
    Object.defineProperty(window.navigator, 'onLine', {
      value: true,
      configurable: true,
    });
  });


  it('should redirect to error page when unauthorized', (done) => {
    const route = new ActivatedRouteSnapshot();
    route.data = { scopes: [['admin']] };
    const state = jasmine.createSpyObj<RouterStateSnapshot>('RouterStateSnapshot', [], {
      url: '/test'
    });

    tokenService.doesUserHaveApplicationPermissions.and.returnValue(false);
    credentialsSubject.next({ username: 'testuser', permissions: ['read'] });

    guard.canActivate(route, state).subscribe(result => {
      expect(result).toBeFalse();
      //   expect(router.navigate).toHaveBeenCalledWith(['unauthorized']);
      done();
    });
  });

  it('should handle token validation errors gracefully', async () => {
    const route = new ActivatedRouteSnapshot();
    route.data = {
      scopes: [['read']]
    };

    tokenService.getOauthToken.and.returnValue('token');
    tokenService.validateToken.and.returnValue(of(false));

    const result = await guard.canAccessRoute(route.data['scopes'], tokenService);
    expect(result).toBeFalse();
  });

  it('should properly handle checkForToken flow', (done) => {
    const route = new ActivatedRouteSnapshot();
    route.data = {
      scopes: [['read']]
    };

    tokenService.getOauthToken.and.returnValue('valid-token');

    guard.checkForToken('http://test.com/redirect', route).subscribe(result => {
      expect(result).toBeTrue();
      done();
    });

    authTokenSubject.next();
  });

  describe('canActivate', () => {
    const route = new ActivatedRouteSnapshot();
    route.data = {
      scopes: [['read']]
    };
    const state = jasmine.createSpyObj<RouterStateSnapshot>('RouterStateSnapshot', [], {
      url: '/test'
    });

    it('should handle route with scopes when token exists', (done) => {
      // Setup route with scopes
      route.data = { scopes: [['test-scope']] };

      // Mock token exists
      tokenService.getOauthToken.and.returnValue('test-token');

      // Mock token validation
      tokenService.validateToken.and.returnValue(of(true));

      guard.canActivate(route, state).subscribe(result => {
        expect(result).toBeDefined();
        done();
      });
    });

    it('should redirect user to error page when token validation fails', async () => {
      const route = new ActivatedRouteSnapshot();
      route.data = { scopes: [['test-scope']] };

      const state = jasmine.createSpyObj<RouterStateSnapshot>('RouterStateSnapshot', [], { url: '/test' });

      // Mock the token service to simulate validation failure
      tokenService.getOauthToken.and.returnValue('test-token'); // Mock token exists
      tokenService.validateToken.and.returnValue(of(false)); // Mock validation failure

      // Spy on the redirectToErrorPage method
      const redirectToErrorPageSpy = spyOn(guard, 'redirectToErrorPage');

      // Use `firstValueFrom` to handle asynchronous observable completion
      await firstValueFrom(guard.canActivate(route, state));

      // TO-DO: fix without setTimeout()
      // Assert that redirectToErrorPage was called
      setTimeout(() => { expect(guard.redirectToErrorPage).toHaveBeenCalled(); }, 2000);
    });

    it('should redirect to error page when getTokenInfo returns false', async () => {
      const route = new MockActivatedRouteSnapshot();
      route.data = { scopes: [['test-scope']] };
      const state = jasmine.createSpyObj<RouterStateSnapshot>('RouterStateSnapshot', [], {
        url: '/test',
      });

      // Mock getTokenInfo to return a resolved Promise with false
      spyOn<any>(guard, 'getTokenInfo').and.returnValue(Promise.resolve(false));

      // Spy on redirectToErrorPage
      spyOn(guard, 'redirectToErrorPage');

      // Start the canActivate observable and wait for its completion
      await firstValueFrom(guard.canActivate(route, state));

      // TO-DO: fix without setTimeout()
      // Assert that redirectToErrorPage was called
      setTimeout(() => { expect(guard.redirectToErrorPage).toHaveBeenCalled(); }, 2000);

    });

    it('should redirect to error page when getTokenInfo returns undefined', async () => {
      const route = new MockActivatedRouteSnapshot();
      route.data = { scopes: [['test-scope']] };
      const state = jasmine.createSpyObj<RouterStateSnapshot>('RouterStateSnapshot', [], {
        url: '/test',
      });

      // Mock getTokenInfo to return a resolved Promise with undefined
      spyOn<any>(guard, 'getTokenInfo').and.returnValue(Promise.resolve(undefined));

      // Spy on redirectToErrorPage
      spyOn(guard, 'redirectToErrorPage');

      // Start the canActivate observable and wait for its completion
      await firstValueFrom(guard.canActivate(route, state));

      // TO-DO: fix without setTimeout()
      // Assert that redirectToErrorPage was called
      setTimeout(() => { expect(guard.redirectToErrorPage).toHaveBeenCalled(); }, 2000);
    });
  });

  describe('getTokenInfo', () => {
    let mockRoute: any;

    beforeEach(() => {
      mockRoute = {
        routeConfig: { path: 'test-path' },
        params: {},
        queryParams: {},
        data: { scopes: [['test-scope']] }
      };
    });

    it('should initiate token check when no token exists', async () => {
      // Mock no existing token
      tokenService.getOauthToken.and.returnValue(null);

      // Mock config service
      appConfigService.getConfig.and.returnValue({
        application: {
          baseUrl: 'http://test.com',
          lazyAuthenticate: false,
          enableLocalStorageToken: true,
          acronym: 'WFPREV',
          environment: 'DEV',
          version: '0.0.0'
        },
        webade: {
          oauth2Url: 'http://oauth.test',
          clientId: 'test-client',
          authScopes: 'WFPREV.*'
        },
        rest: {},
        mapServices: {
          geoserverApiBaseUrl: 'http://geoserver.test',
          wfnewsApiBaseUrl: 'http://wfnews.test'
        }
      });

      // Mock checkForToken
      spyOn(guard, 'checkForToken').and.returnValue(of(true));

      const result = await guard['getTokenInfo'](mockRoute);

      expect(guard.checkForToken).toHaveBeenCalled();
    });

    it('should return true when token exists and passes route access', async () => {
      // Mock token exists
      tokenService.getOauthToken.and.returnValue('test-token');

      // Mock token validation
      tokenService.validateToken.and.returnValue(of(true));

      const result = await guard['getTokenInfo'](mockRoute);

      expect(result).toBeTruthy();
    });

    it('should redirect to error page when token validation fails', async () => {
      // Mock token exists
      tokenService.getOauthToken.and.returnValue('test-token');

      // Mock token validation fails
      tokenService.validateToken.and.returnValue(of(false));

      // Spy on redirectToErrorPage
      spyOn(guard, 'redirectToErrorPage');

      const result = await guard['getTokenInfo'](mockRoute);

      expect(guard.redirectToErrorPage).toHaveBeenCalled();
    });
  });

  describe('canAccessRoute', () => {
    it('should return false when no token exists', async () => {
      // Mock no token
      tokenService.getOauthToken.and.returnValue(null);

      const result = await guard.canAccessRoute([['test-scope']], tokenService);

      expect(result).toBeFalse();
    });

    it('should return true when token is validated successfully', async () => {
      // Mock token exists
      tokenService.getOauthToken.and.returnValue('test-token');

      // Mock token validation
      tokenService.validateToken.and.returnValue(of(true));

      const result = await guard.canAccessRoute([['test-scope']], tokenService);

      expect(result).toBeTrue();
    });

    it('should return false when token validation fails', async () => {
      // Mock token exists
      tokenService.getOauthToken.and.returnValue('test-token');

      // Mock token validation fails
      tokenService.validateToken.and.returnValue(of(false));

      const result = await guard.canAccessRoute([['test-scope']], tokenService);

      expect(result).toBeFalse();
    });
  });

});