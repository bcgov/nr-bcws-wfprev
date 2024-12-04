import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { TokenService } from './token.service';
import { AppConfigService } from './app-config.service';
import { OAuthService } from 'angular-oauth2-oidc';
import { Injector } from '@angular/core';
import { HttpHandler } from '@angular/common/http';
import { UUID } from 'angular2-uuid';

describe('TokenService', () => {
  let service: TokenService;
  let httpMock: HttpTestingController;
  let mockAppConfigService: any;
  let mockOAuthService: any;
  let mockInjector: any;

  // Mock configuration
  const mockConfig = {
    application: {
      lazyAuthenticate: false,
      enableLocalStorageToken: true,
      localStorageTokenKey: 'test-oauth',
      allowLocalExpiredToken: false,
      baseUrl: 'http://test.com'
    },
    webade: {
      oauth2Url: 'http://oauth.test',
      clientId: 'test-client',
      authScopes: ['GET_PROJECT', 'CREATE_PROJECT'],
      enableCheckToken: false, // Disable token checking for basic tests
      checkTokenUrl: 'http://check-token.test'
    }
  };

  beforeEach(() => {
    // Mock dependencies
    mockAppConfigService = {
      getConfig: jasmine.createSpy('getConfig').and.returnValue(mockConfig),
      loadConfig: jasmine.createSpy('loadConfig').and.returnValue(Promise.resolve(mockConfig))
    };

    mockOAuthService = {
      configure: jasmine.createSpy('configure'),
      initImplicitFlow: jasmine.createSpy('initImplicitFlow')
    };

    mockInjector = {
      get: jasmine.createSpy('get').and.callFake((token) => {
        if (token === OAuthService) return mockOAuthService;
        if (token === HttpHandler) return TestBed.inject(HttpHandler);
        throw new Error(`Unknown token: ${token}`);
      })
    };

    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [
        TokenService,
        { provide: AppConfigService, useValue: mockAppConfigService },
        { provide: Injector, useValue: mockInjector }
      ]
    });

    service = TestBed.inject(TokenService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    localStorage.clear();
    httpMock.verify();
  });

  describe('Constructor and Initial Config', () => {
    it('should initialize with default config values', () => {
      const minimalConfig = {
        application: {},
        webade: {
          oauth2Url: 'http://oauth.test',
          clientId: 'test-client',
          authScopes: ['GET_PROJECT']
        }
      };
      
      mockAppConfigService.getConfig.and.returnValue(minimalConfig);
      const newService = new TokenService(mockInjector, mockAppConfigService);
      
      expect(newService['LOCAL_STORAGE_KEY']).toBe('oauth');
      expect(newService['useLocalStore']).toBe(false);
    });
  });

  describe('checkForToken', () => {
    let service: TokenService;
  
    beforeEach(() => {
      service = TestBed.inject(TokenService);
    });
  
    it('should handle offline mode with a token in localStorage', async () => {
      // Mock `navigator.onLine` to return `false`
      Object.defineProperty(navigator, 'onLine', {
        value: false,
        configurable: true,
      });
  
      const mockToken = {
        access_token: 'test-token',
        exp: Math.floor(Date.now() / 1000) + 3600, // Valid token
      };
      localStorage.setItem('test-oauth', JSON.stringify(mockToken));
  
      spyOn(service as any, 'initAuthFromSession').and.callThrough();
  
      await service.checkForToken();
  
      expect((service as any).initAuthFromSession).toHaveBeenCalled();
      expect(localStorage.getItem('test-oauth')).toBeNull();
    });
  
    it('should reinitialize flow if no token is in localStorage', async () => {
      // Mock `navigator.onLine` to return `false`
      Object.defineProperty(navigator, 'onLine', {
        value: false,
        configurable: true,
      });
  
      spyOn(service as any, 'initImplicitFlow').and.callThrough();
  
      await service.checkForToken();
  
      expect((service as any).initImplicitFlow).toHaveBeenCalled();
    });
  
    it('should reinitialize flow if token is expired', async () => {
      // Mock `navigator.onLine` to return `false`
      Object.defineProperty(navigator, 'onLine', {
        value: false,
        configurable: true,
      });
  
      const expiredToken = {
        access_token: 'expired-token',
        exp: Math.floor(Date.now() / 1000) - 3600, // Expired token
      };
      localStorage.setItem('test-oauth', JSON.stringify(expiredToken));
  
      spyOn(service as any, 'initImplicitFlow').and.callThrough();
  
      await service.checkForToken();
  
      expect(localStorage.getItem('test-oauth')).toBeNull();
      expect((service as any).initImplicitFlow).toHaveBeenCalled();
    });
  });

  describe('Local Storage Handling', () => {
    beforeEach(() => {
      // Override config to disable token checking for these tests
      mockConfig.webade.enableCheckToken = false;
    });

    it('should store and retrieve token from localStorage', () => {
      const mockToken = {
        access_token: 'test-access-token.eyJ0ZXN0IjoidGVzdCJ9.test', // Adding JWT structure
        expires_in: 3600
      };

      service.initAuth(mockToken);

      const storedToken = localStorage.getItem('test-oauth');
      expect(storedToken).not.toBeNull();
      const parsedToken = JSON.parse(storedToken!);
      expect(parsedToken).toEqual({
        access_token: mockToken.access_token,
        expires_in: mockToken.expires_in
      });
    });

    it('should clear localStorage token', () => {
      localStorage.setItem('test-oauth', JSON.stringify({ access_token: 'old-token' }));
      service.clearLocalStorageToken();
      expect(localStorage.getItem('test-oauth')).toBeNull();
    });
  });

  describe('Token Validation', () => {
    beforeEach(() => {
      // Enable token checking for validation tests
      mockConfig.webade.enableCheckToken = true;
    });

    it('should validate token successfully', (done) => {
      const testToken = 'test-token';
      
      service.validateToken(testToken).subscribe(result => {
        expect(result).toBe(true);
        done();
      });

      const req = httpMock.expectOne(mockConfig.webade.checkTokenUrl);
      expect(req.request.method).toBe('GET');
      expect(req.request.headers.get('Authorization')).toBe('Bearer test-token');
      
      req.flush({}, { status: 200, statusText: 'OK' });
    });

    it('should handle validation failure', (done) => {
      service.validateToken(UUID.UUID()).subscribe(result => {
        expect(result).toBe(false);
        done();
      });

      const req = httpMock.expectOne(mockConfig.webade.checkTokenUrl);
      req.flush({}, { status: 401, statusText: 'Unauthorized' });
    });
  });

  describe('Token Expiration', () => {
    it('should correctly identify expired token', () => {
      const expiredToken = { exp: Math.floor(Date.now() / 1000) - 3600 };
      expect(service.isTokenExpired(expiredToken)).toBe(true);

      const validToken = { exp: Math.floor(Date.now() / 1000) + 3600 };
      expect(service.isTokenExpired(validToken)).toBe(false);
    });
  });

  describe('Permission Checking', () => {
    it('should check user permissions correctly', () => {
      service['tokenDetails'] = { 
        scope: ['GET_PROJECT', 'CREATE_PROJECT', 'execute'] 
      };

      expect(service.doesUserHaveApplicationPermissions(['GET_PROJECT', 'CREATE_PROJECT']))
        .toBe(true);
      expect(service.doesUserHaveApplicationPermissions(['GET_PROJECT', 'DELETE_PROJECT']))
        .toBe(false);
    });
  });

  describe('OAuth Flow', () => {
    it('should initiate implicit flow', () => {
      service['initImplicitFlow']();

      expect(mockOAuthService.configure).toHaveBeenCalledWith(jasmine.objectContaining({
        oidc: false,
        issuer: mockConfig.application.baseUrl,
        loginUrl: mockConfig.webade.oauth2Url,
        clientId: mockConfig.webade.clientId,
        scope: mockConfig.webade.authScopes
      }));
      expect(mockOAuthService.initImplicitFlow).toHaveBeenCalled();
    });
  });


describe('parseToken', () => {
  it('should parse and initialize token from hash', () => {
    const hash = '#access_token=test-token&expires_in=3600';
    spyOn(service, 'initAuth');

    (service as any).parseToken(hash);

    expect(service.initAuth).toHaveBeenCalledWith({
      access_token: 'test-token',
      expires_in: '3600',
    });
  });

  it('should handle invalid hash gracefully', () => {
    const invalidHash = '#invalid_token';
    spyOn(service, 'initAuth');

    (service as any).parseToken(invalidHash);

    expect(service.initAuth).not.toHaveBeenCalled();
  });
});

describe('initAuthFromSession', () => {
  it('should initialize authentication from localStorage', async () => {
    const mockToken = { access_token: 'test-token', exp: Math.floor(Date.now() / 1000) + 3600 };
    localStorage.setItem('test-oauth', JSON.stringify(mockToken));
    spyOn(service as any, 'initAndEmit').and.callThrough();

    await (service as any).initAuthFromSession();

    expect(service['initAndEmit']).toHaveBeenCalled();
  });

});

describe('getOauthToken', () => {
  it('should return the current OAuth token', () => {
    service['oauth'] = { access_token: 'test-token' };

    expect(service.getOauthToken()).toBe('test-token');
  });

  it('should return null if no token is present', () => {
    service['oauth'] = null;

    expect(service.getOauthToken()).toBeNull();
  });
});

describe('handleError', () => {
  it('should log and throw an error', () => {
    spyOn(console, 'error');
    spyOn(window, 'alert');

    expect(() => (service as any).handleError(new Error('Test error'), 'Custom message')).toThrowError('Test error');
    expect(console.error).toHaveBeenCalledWith('Unexpected error', jasmine.any(Error));
    expect(window.alert).toHaveBeenCalledWith('Custom message Error: Test error');
  });
});

describe('emitTokens', () => {
  it('should emit tokens and complete subjects', () => {
    service['oauth'] = { access_token: 'test-token' };
    service['tokenDetails'] = { scope: ['test-scope'] };
    
    spyOn(service['authToken'], 'next');
    spyOn(service['authToken'], 'complete');
    spyOn(service['credentials'], 'next');
    spyOn(service['credentials'], 'complete');
    
    service['emitTokens']();
    
    expect(service['authToken'].next).toHaveBeenCalledWith('test-token');
    expect(service['authToken'].complete).toHaveBeenCalled();
    expect(service['credentials'].next).toHaveBeenCalledWith({ scope: ['test-scope'] });
    expect(service['credentials'].complete).toHaveBeenCalled();
  });
});

});
