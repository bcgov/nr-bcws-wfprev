import { TestBed } from '@angular/core/testing';
import { MapConfigService } from '../map-config.service';
import { AppConfigService } from '../app-config.service';

describe('MapConfigService', () => {
  let service: MapConfigService;
  let appConfigService: jasmine.SpyObj<AppConfigService>;

  beforeEach(() => {
    const appConfigSpy = jasmine.createSpyObj('AppConfigService', ['loadAppConfig']);
    TestBed.configureTestingModule({
      providers: [
        MapConfigService,
        { provide: AppConfigService, useValue: appConfigSpy },
      ],
    });

    service = TestBed.inject(MapConfigService);
    appConfigService = TestBed.inject(AppConfigService) as jasmine.SpyObj<AppConfigService>;
  });

  it('should handle errors from AppConfigService.loadAppConfig', async () => {
    // Arrange: Mock a rejection from AppConfigService.loadAppConfig
    const mockError = new Error('Load config error');
    appConfigService.loadAppConfig.and.returnValue(Promise.reject(mockError));

    // Act & Assert
    await expectAsync(service.getMapConfig()).toBeRejectedWith(mockError);

    expect(appConfigService.loadAppConfig).toHaveBeenCalled();
  });
});
