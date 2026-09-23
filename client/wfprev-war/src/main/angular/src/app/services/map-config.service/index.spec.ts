import { TestBed } from '@angular/core/testing';
import { MapConfigService } from '../map-config.service';
import { mapConfigBase, miniMapConfig } from './map.config';
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

  const loadedConfig: any = {
    application: {
      baseUrl: 'http://test.com',
      lazyAuthenticate: false,
      enableLocalStorageToken: true,
      acronym: 'WFPREV',
      environment: 'DEV',
      version: '0.0.0',
      remiPlannerEmailAddress: 'test@example.com'
    },
    webade: {
      oauth2Url: 'http://oauth.test',
      clientId: 'test-client',
      authScopes: 'WFPREV.*'
    },
    rest: {},
    mapServices: {
      geoserverApiBaseUrl: 'http://geoserver.test',
      wfnewsApiBaseUrl: 'http://wfnews.test',
      wfnewsApiKey: 'fake-api-key'
    }
  };
  const notLoaded = () => { throw new Error('Configuration not loaded. Please call loadAppConfig() first.'); };

  it('should use the config the app loaded at startup, without fetching it again', async () => {
    appConfigService.getConfig.and.returnValue(loadedConfig);

    expect(await service.getMapConfig()).toBeDefined();
    expect(await service.getBaseConfig()).toBeDefined();
    expect(await service.getLayersConfig()).toBeDefined();

    expect(appConfigService.loadAppConfig).not.toHaveBeenCalled();
  });

  it('should load the config when it has not been loaded yet', async () => {
    let loaded = false;
    appConfigService.getConfig.and.callFake(() => (loaded ? loadedConfig : notLoaded()));
    appConfigService.loadAppConfig.and.callFake(async () => { loaded = true; });

    const result = await service.getMapConfig();

    expect(appConfigService.loadAppConfig).toHaveBeenCalledTimes(1);
    expect(result).toBeDefined();
  });

  it('should handle errors from AppConfigService.loadAppConfig', async () => {
    const mockError = new Error('Load config error');
    appConfigService.getConfig.and.callFake(notLoaded);
    appConfigService.loadAppConfig.and.returnValue(Promise.reject(mockError));

    await expectAsync(service.getMapConfig()).toBeRejectedWith(mockError);
  });
});

describe('mapConfigBase', () => {
  it('should default to ESRI Topographic and list only the approved basemaps, with static previews', () => {
    const viewer: any = mapConfigBase({}).viewer;

    expect(viewer.baseMap).toBe('topographic-v2');
    expect(viewer.baseMapConfig).toEqual([
      { id: 'topographic-v2', order: 1, optionImageUrl: 'assets/basemaps/topographic-v2.png' },
      { id: 'bc-basemap-hillshade', order: 2, optionImageUrl: 'assets/basemaps/bc-basemap-hillshade.png' },
      { id: 'imagery-v2', order: 3, optionImageUrl: 'assets/basemaps/imagery-v2.png' },
    ]);
  });
});

describe('miniMapConfig', () => {
  it('should offer the same basemaps as the main map', () => {
    const viewer: any = miniMapConfig().viewer;
    const mainViewer: any = mapConfigBase({}).viewer;

    expect(viewer.baseMap).toBe(mainViewer.baseMap);
    expect(viewer.baseMapConfig).toEqual(mainViewer.baseMapConfig);
    expect(miniMapConfig().tools).toContain(
      { type: 'baseMaps', enabled: true, choices: ['topographic-v2', 'bc-basemap-hillshade', 'imagery-v2'] }
    );
  });

  it('should lay out the map for a desktop, however narrow, in the main map\'s theme', () => {
    const viewer: any = miniMapConfig().viewer;

    expect(viewer.device).toBe('desktop');
    expect(viewer.themes).toEqual(['wf']);
  });

  it('should pan and zoom, and turn off the search and location tools SMK turns on', () => {
    const tools = miniMapConfig().tools;

    expect(tools).toContain({ type: 'pan', enabled: true });
    expect(tools).toContain(jasmine.objectContaining({ type: 'zoom', enabled: true, control: true }));
    expect(tools).toContain({ type: 'search', enabled: false });
    expect(tools).toContain({ type: 'location', enabled: false });
    expect(tools.map(tool => tool.type)).not.toContain('layers');
  });

  it('should give every map its own copy, since SMK marks up the config it is given', () => {
    const baseMapsTool = () => miniMapConfig().tools.find(tool => tool.type === 'baseMaps');

    expect(miniMapConfig()).not.toBe(miniMapConfig());
    expect(baseMapsTool()).not.toBe(baseMapsTool());
  });
});
