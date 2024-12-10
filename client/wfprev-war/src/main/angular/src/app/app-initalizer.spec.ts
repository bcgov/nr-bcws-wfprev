import { TestBed } from '@angular/core/testing';
import { AppConfigService } from 'src/app/services/app-config.service';
import { initializeAppConfig } from './app-initializer';

describe('initializeAppConfig', () => {
  let appConfigService: jasmine.SpyObj<AppConfigService>;

  beforeEach(() => {
    // Create a spy object for AppConfigService
    appConfigService = jasmine.createSpyObj('AppConfigService', ['loadAppConfig']);

    // Configure TestBed
    TestBed.configureTestingModule({
      providers: [
        { provide: AppConfigService, useValue: appConfigService }
      ]
    });
  });

  it('should resolve the promise when config is successfully loaded', async () => {
    // Mock loadAppConfig to return a resolved promise
    appConfigService.loadAppConfig.and.returnValue(Promise.resolve());

    // Initialize app config with the mock service
    const initFunction = initializeAppConfig(appConfigService);
    await expectAsync(initFunction()).toBeResolved();

    // Ensure loadAppConfig was called
    expect(appConfigService.loadAppConfig).toHaveBeenCalled();
  });

  it('should reject the promise when config loading fails', async () => {
    const error = new Error('Config loading failed');
    // Mock loadAppConfig to return a rejected promise
    appConfigService.loadAppConfig.and.returnValue(Promise.reject(error));

    // Initialize app config with the mock service
    const initFunction = initializeAppConfig(appConfigService);
    await expectAsync(initFunction()).toBeRejectedWith(error);

    // Ensure loadAppConfig was called
    expect(appConfigService.loadAppConfig).toHaveBeenCalled();
  });
});