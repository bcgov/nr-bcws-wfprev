import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { LibraryConfig } from '../config/library-config';
import { ApplicationConfig } from '../interfaces/application-config';
import { AppConfigService } from './app-config.service';

describe('AppConfigService', () => {
  let service: AppConfigService;
  let httpMock: HttpTestingController;

  const mockLibraryConfig: LibraryConfig = {
    configurationPath: 'assets/data/appConfig.json',
  };

  const mockConfig: ApplicationConfig = {
    application: {
      baseUrl: 'http://test.com',
      lazyAuthenticate: false,
      enableLocalStorageToken: true,
      acronym: 'TEST',
      environment: 'DEV',
      version: '1.0.0',
      remiPlannerEmailAddress: 'test@example.com'
    },
    webade: {
      oauth2Url: 'http://oauth.test',
      clientId: 'test-client',
      authScopes: 'TEST.*',
    },
    rest: {},
    mapServices: {
      geoserverApiBaseUrl: 'http://geoserver.test',
      wfnewsApiBaseUrl: 'http://wfnews.test',
      wfnewsApiKey: 'fake-api-key'
    }
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [
        AppConfigService,
        { provide: LibraryConfig, useValue: mockLibraryConfig },
      ],
    });

    service = TestBed.inject(AppConfigService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should load configuration successfully', async () => {
    const loadConfigPromise = service.loadAppConfig();

    const req = httpMock.expectOne(mockLibraryConfig.configurationPath);
    expect(req.request.method).toBe('GET');
    req.flush(mockConfig);

    await loadConfigPromise;

    expect(service.getConfig()).toEqual(mockConfig);
  });

  it('should throw an error if configuration is not loaded', () => {
    expect(() => service.getConfig()).toThrowError(
      'Configuration not loaded. Please call loadAppConfig() first.'
    );
  });

  it('should handle HTTP error when loading configuration', async () => {
    const loadConfigPromise = service.loadAppConfig();

    const req = httpMock.expectOne(mockLibraryConfig.configurationPath);
    req.error(new ErrorEvent('Network error'), { status: 500, statusText: 'Internal Server Error' });

    await expectAsync(loadConfigPromise).toBeRejectedWithError(
      'Failed to load application configuration: Http failure response for /assets/data/appConfig.json: 500 Internal Server Error'
    );
  });

  it('should notify configEmitter on successful configuration load', async () => {
    const configSpy = jasmine.createSpy('configEmitter');
    service.configEmitter.subscribe(configSpy);

    const loadConfigPromise = service.loadAppConfig();

    const req = httpMock.expectOne(mockLibraryConfig.configurationPath);
    req.flush(mockConfig);

    await loadConfigPromise;

    expect(configSpy).toHaveBeenCalledWith(mockConfig);
  });

  it('should notify configEmitter of errors when loading fails', async () => {
    const configSpy = jasmine.createSpy('configEmitter');
    const errorSpy = jasmine.createSpy('error');
    service.configEmitter.subscribe(configSpy, errorSpy);

    const loadConfigPromise = service.loadAppConfig();

    const req = httpMock.expectOne(mockLibraryConfig.configurationPath);
    req.error(new ErrorEvent('Network error'), { status: 500, statusText: 'Internal Server Error' });

    await expectAsync(loadConfigPromise).toBeRejected();

    expect(configSpy).not.toHaveBeenCalled();
    expect(errorSpy).toHaveBeenCalled();
  });

  it('should throw an error when no data is returned from configuration', async () => {
    const loadConfigPromise = service.loadAppConfig();
    const req = httpMock.expectOne(mockLibraryConfig.configurationPath);

    // Flush with null to simulate no data
    req.flush(null);

    await expectAsync(loadConfigPromise).toBeRejectedWithError(
      'Failed to load application configuration: No data returned from application config'
    );
  });
});