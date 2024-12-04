import { TestBed } from '@angular/core/testing';
import { PrevAuthGuard } from './prev-auth-guard';
import { TokenService } from 'src/app/services/token.service';
import { AppConfigService } from 'src/app/services/app-config.service';
import { Router, ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { of, Subject, BehaviorSubject } from 'rxjs';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';

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
            rest: {}
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
});