import { TestBed } from '@angular/core/testing';
import { Router, ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';
import { AuthGuard } from './auth-guard';
import { TokenService } from '../token.service';
import { of, Observable } from 'rxjs';

describe('AuthGuard', () => {
  let authGuard: AuthGuard;
  let mockTokenService: any;
  let mockRouter: any;

  beforeEach(() => {
    mockTokenService = {
      credentialsEmitter: of({}),
      doesUserHaveApplicationPermissions: jasmine.createSpy('doesUserHaveApplicationPermissions')
    };

    mockRouter = {
      navigate: jasmine.createSpy('navigate')
    };

    TestBed.configureTestingModule({
      providers: [
        AuthGuard,
        { provide: TokenService, useValue: mockTokenService },
        { provide: Router, useValue: mockRouter }
      ]
    });

    authGuard = TestBed.inject(AuthGuard);
  });

  describe('canActivate', () => {
    let mockActivatedRouteSnapshot: ActivatedRouteSnapshot;
    let mockRouterStateSnapshot: RouterStateSnapshot;

    beforeEach(() => {
      mockActivatedRouteSnapshot = {} as ActivatedRouteSnapshot;
      mockRouterStateSnapshot = {
        url: '/test-url'
      } as RouterStateSnapshot;
    });

    it('should return true when user has required permissions', () => {
      // Setup: User has permissions
      (authGuard as any)['credentials'] = { someCredential: 'value' };
      mockTokenService.doesUserHaveApplicationPermissions.and.returnValue(true);

      // Call canActivate
      const result = authGuard.canActivate(mockActivatedRouteSnapshot, mockRouterStateSnapshot);

      // Verify
      if (result instanceof Observable) {
        result.subscribe(value => {
          expect(value).toBeTruthy();
        });
      } else {
        expect(result).toBeTruthy();
      }
      expect(mockTokenService.doesUserHaveApplicationPermissions).toHaveBeenCalledWith([]);
      expect(mockRouter.navigate).not.toHaveBeenCalled();
    });

    it('should return false and redirect when user lacks permissions', () => {
      // Setup: User lacks permissions
      (authGuard as any)['credentials'] = { someCredential: 'value' };
      mockTokenService.doesUserHaveApplicationPermissions.and.returnValue(false);

      // Spy on redirectToErrorPage method
      spyOn(authGuard, 'redirectToErrorPage').and.callThrough();

      // Call canActivate
      const result = authGuard.canActivate(mockActivatedRouteSnapshot, mockRouterStateSnapshot);

      // Verify
      if (result instanceof Observable) {
        result.subscribe(value => {
          expect(value).toBeFalsy();
        });
      } else {
        expect(result).toBeFalsy();
      }
      expect(mockTokenService.doesUserHaveApplicationPermissions).toHaveBeenCalledWith([]);
      expect(authGuard.redirectToErrorPage).toHaveBeenCalled();
      expect(mockRouter.navigate).toHaveBeenCalledWith([{ outlets: { root: ['unauthorized'] } }]);
    });

    it('should return false when no credentials are present', () => {
      // Setup: No credentials
      (authGuard as any)['credentials'] = null;
      mockTokenService.doesUserHaveApplicationPermissions.and.returnValue(false);

      // Spy on redirectToErrorPage method
      spyOn(authGuard, 'redirectToErrorPage').and.callThrough();

      // Call canActivate
      const result = authGuard.canActivate(mockActivatedRouteSnapshot, mockRouterStateSnapshot);

      // Verify
      if (result instanceof Observable) {
        result.subscribe(value => {
          expect(value).toBeFalsy();
        });
      } else {
        expect(result).toBeFalsy();
      }
      expect(mockTokenService.doesUserHaveApplicationPermissions).toHaveBeenCalledWith([]);
      expect(authGuard.redirectToErrorPage).toHaveBeenCalled();
      expect(mockRouter.navigate).toHaveBeenCalledWith([{ outlets: { root: ['unauthorized'] } }]);
    });
  });

  describe('checkLogin', () => {
    it('should return true when user has permissions', () => {
      // Setup: User has permissions
      (authGuard as any)['credentials'] = { someCredential: 'value' };
      mockTokenService.doesUserHaveApplicationPermissions.and.returnValue(true);

      // Call checkLogin
      const result = authGuard.checkLogin('/test-url', []);

      // Verify
      expect(result).toBeTruthy();
      expect(mockTokenService.doesUserHaveApplicationPermissions).toHaveBeenCalledWith([]);
      expect(mockRouter.navigate).not.toHaveBeenCalled();
    });

    it('should return false and redirect when user lacks permissions', () => {
      // Setup: User lacks permissions
      (authGuard as any)['credentials'] = { someCredential: 'value' };
      mockTokenService.doesUserHaveApplicationPermissions.and.returnValue(false);

      // Spy on redirectToErrorPage method
      spyOn(authGuard, 'redirectToErrorPage').and.callThrough();

      // Call checkLogin
      const result = authGuard.checkLogin('/test-url', []);

      // Verify
      expect(result).toBeFalsy();
      expect(mockTokenService.doesUserHaveApplicationPermissions).toHaveBeenCalledWith([]);
      expect(authGuard.redirectToErrorPage).toHaveBeenCalled();
      expect(mockRouter.navigate).toHaveBeenCalledWith([{ outlets: { root: ['unauthorized'] } }]);
    });
  });

  describe('redirectToErrorPage', () => {
    it('should navigate to unauthorized page', () => {
      // Call redirectToErrorPage
      authGuard.redirectToErrorPage();

      // Verify navigation
      expect(mockRouter.navigate).toHaveBeenCalledWith([{ outlets: { root: ['unauthorized'] } }]);
    });
  });

  describe('Credentials Subscription', () => {
    it('should update credentials when credentialsEmitter emits', () => {
      // Create a test credentials object
      const testCredentials = { user: 'test' };

      // Simulate credentials emission
      mockTokenService.credentialsEmitter = of(testCredentials);

      // Recreate the guard to trigger subscription
      authGuard = new AuthGuard(mockTokenService, mockRouter);

      // Verify credentials are updated
      expect((authGuard as any)['credentials']).toEqual(testCredentials);
    });
  });
});