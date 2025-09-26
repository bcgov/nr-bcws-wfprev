import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ErrorPageComponent } from './error-page.component';
import { AppConfigService } from 'src/app/services/app-config.service';

describe('ErrorPageComponent', () => {
  let component: ErrorPageComponent;
  let fixture: ComponentFixture<ErrorPageComponent>;
  let mockAppConfigService: jasmine.SpyObj<AppConfigService>;

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

  beforeEach(async () => {
    mockAppConfigService = jasmine.createSpyObj<AppConfigService>('AppConfigService', ['getConfig']);
    mockAppConfigService.getConfig.and.returnValue(mockConfig);
    await TestBed.configureTestingModule({
      imports: [ErrorPageComponent],
      providers: [
        { provide: AppConfigService, useValue: mockAppConfigService }
      ]
    })
      .compileComponents();

    fixture = TestBed.createComponent(ErrorPageComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load remiPlannerEmailAddress from AppConfigService', () => {
    expect(component.remiPlannerEmailAddress).toBe('test@example.com');
    expect(mockAppConfigService.getConfig).toHaveBeenCalled();
  });

  it('should default remiPlannerEmailAddress to empty string if not in config', () => {
    mockAppConfigService.getConfig.and.returnValue({
      ...mockConfig,
      application: {
        ...mockConfig.application,
        remiPlannerEmailAddress: undefined
      }
    });

    const localFixture = TestBed.createComponent(ErrorPageComponent);
    const localComponent = localFixture.componentInstance;
    localFixture.detectChanges();

    expect(localComponent.remiPlannerEmailAddress).toBe('');
    expect(mockAppConfigService.getConfig).toHaveBeenCalled();
  });

});
