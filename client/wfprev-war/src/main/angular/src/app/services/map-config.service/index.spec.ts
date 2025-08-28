import { TestBed } from '@angular/core/testing';
import { MapConfigService } from '../map-config.service';
import { AppConfigService } from '../app-config.service';
import { TokenService } from '../token.service';
import { of } from 'rxjs';

describe('MapConfigService', () => {
  let service: MapConfigService;
  let appConfigService: jasmine.SpyObj<AppConfigService>;

  beforeEach(() => {
    const appConfigSpy = jasmine.createSpyObj<AppConfigService>(
      'AppConfigService',
      ['loadAppConfig', 'getConfig']
    );

    const tokenServiceStub: Partial<TokenService> = {
      authTokenEmitter: of('test-token'),
    } as TokenService;

    TestBed.configureTestingModule({
      providers: [
        MapConfigService,
        { provide: AppConfigService, useValue: appConfigSpy },
        { provide: TokenService, useValue: tokenServiceStub },
      ],
    });

    service = TestBed.inject(MapConfigService);
    appConfigService = TestBed.inject(AppConfigService) as jasmine.SpyObj<AppConfigService>;
  });

  it('should handle errors from AppConfigService.loadAppConfig', async () => {
    const mockError = new Error('Load config error');
    appConfigService.loadAppConfig.and.returnValue(Promise.reject(mockError));
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
        geoserverBaseUrl: 'http://geoserver.test',
        wfnewsBaseUrl: 'http://wfnews.test'
      }
    });

    await expectAsync(service.getMapConfig()).toBeRejectedWith(mockError);
    expect(appConfigService.loadAppConfig).toHaveBeenCalled();
  });

  it('should build map config when loadAppConfig resolves', async () => {
    appConfigService.loadAppConfig.and.returnValue(Promise.resolve());
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
          geoserverBaseUrl: 'http://geoserver.test',
          wfnewsBaseUrl: 'http://wfnews.test'
        }
    });

    const result = await service.getMapConfig();

    expect(appConfigService.loadAppConfig).toHaveBeenCalled();
    expect(appConfigService.getConfig).toHaveBeenCalled();
    expect(result).toBeDefined();
  });
});
