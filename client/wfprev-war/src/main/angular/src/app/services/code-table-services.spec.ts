import { TestBed, waitForAsync } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { AppConfigService } from 'src/app/services/app-config.service';
import { TokenService } from 'src/app/services/token.service';
import { CodeTableServices } from 'src/app/services/code-table-services';

describe('CodeTableServices', () => {
  let service: CodeTableServices;
  let httpMock: HttpTestingController;
  let mockAppConfigService: jasmine.SpyObj<AppConfigService>;
  let mockTokenService: jasmine.SpyObj<TokenService>;

  const mockConfig = {
    rest: {
      wfprev: 'http://mock-api.com',
      openmaps: 'http://mock-api.com'
      
    },
    application: {
      lazyAuthenticate: true,
      enableLocalStorageToken: true,
      localStorageTokenKey: 'oauth',
      allowLocalExpiredToken: false,
      baseUrl: 'http://mock-base-url.com',
      acronym: 'TEST',
      version: '1.0.0',
      environment: 'test',
      remiPlannerEmailAddress: 'test@example.com'
    },
    webade: {
      oauth2Url: 'http://mock-oauth-url.com',
      clientId: 'mock-client-id',
      authScopes: 'mock-scope',
      checkTokenUrl: 'http://mock-check-token-url.com',
      enableCheckToken: false,
    },
    mapServices: {
      geoserverApiBaseUrl: 'http://geoserver.test',
      wfnewsApiBaseUrl: 'http://wfnews.test',
      wfnewsApiKey: 'fake-api-key'
    }
  };

  beforeEach(() => {
    mockAppConfigService = jasmine.createSpyObj('AppConfigService', ['getConfig']);
    mockTokenService = jasmine.createSpyObj('TokenService', ['getOauthToken']);

    mockAppConfigService.getConfig.and.returnValue(mockConfig);
    mockTokenService.getOauthToken.and.returnValue('mock-token');

    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [
        CodeTableServices,
        { provide: AppConfigService, useValue: mockAppConfigService },
        { provide: TokenService, useValue: mockTokenService },
      ]
    });

    service = TestBed.inject(CodeTableServices);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should fetch a code table from the API when not cached', () => {
    const codeTableName = 'testCodeTable';
    const mockResponse = [{ code: '001', name: 'Test Code' }];

    service.fetchCodeTable(codeTableName).subscribe((response) => {
      expect(response).toEqual(mockResponse);
    });

    const req = httpMock.expectOne(`http://mock-api.com/wfprev-api/codes/${codeTableName}`);
    expect(req.request.method).toBe('GET');
    expect(req.request.headers.get('Authorization')).toBe('Bearer mock-token');
    req.flush(mockResponse);
  });

  it('should return cached code table data if available', () => {
    const codeTableName = 'cachedCodeTable';
    const cachedData = [{ code: '002', name: 'Cached Code' }];

    // Simulate caching
    (service as any).codeTableCache[codeTableName] = cachedData;

    service.fetchCodeTable(codeTableName).subscribe((response) => {
      expect(response).toEqual(cachedData);
    });

    // Ensure no API call was made
    httpMock.expectNone(`http://mock-api.com/wfprev-api/codes/${codeTableName}`);
  });

  it('should handle errors when fetching a code table', () => {
    const codeTableName = 'errorCodeTable';

    service.fetchCodeTable(codeTableName).subscribe({
      next: () => fail('Should have failed with an error'),
      error: (error) => {
        expect(error.message).toBe('Failed to get code table');
      }
    });

    const req = httpMock.expectOne(`http://mock-api.com/wfprev-api/codes/${codeTableName}`);
    expect(req.request.method).toBe('GET');
    req.flush('Error', { status: 500, statusText: 'Server Error' });
  });

  it('should cache the response after fetching a code table', () => {
    const codeTableName = 'newCodeTable';
    const mockResponse = [{ code: '003', name: 'New Code' }];

    service.fetchCodeTable(codeTableName).subscribe((response) => {
      expect(response).toEqual(mockResponse);
      expect((service as any).codeTableCache[codeTableName]).toEqual(mockResponse);
    });

    const req = httpMock.expectOne(`http://mock-api.com/wfprev-api/codes/${codeTableName}`);
    expect(req.request.method).toBe('GET');
    req.flush(mockResponse);
  });
});
