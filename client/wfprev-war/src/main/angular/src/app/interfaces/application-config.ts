export interface PollingConfig {
    [prop: string]: number;
  }
  
  export interface ApplicationPollingConfig {
    [prop: string]: PollingConfig;
  }
  
  export interface PagingConfig {
    [prop: string]: number;
  }
  
  export interface Application {
    acronym: string;
    version: string;
    baseUrl: string;
    environment: string;
    buildNumber?: string;
    polling: ApplicationPollingConfig;
    maxListPageSize: PagingConfig;
    lazyAuthenticate?: boolean;
    enableLocalStorageToken?: boolean;
    allowLocalExpiredToken?: boolean;
    localStorageTokenKey?: string;
    
  }
  
  export interface IndividualAppConfig {
    url: string;
    authentication?: string;
    user?: string;
    password?: string;
    themeHash?: string;
    scriptsHash?: string;
  }
  
  export interface ExternalAppConfig {
    [prop: string]: IndividualAppConfig;
  }
  
  export interface CauseCodeConfig {
      [prop: string]: IndividualAppConfig;
  }
  
  export interface UserPreferenceConfig {
    [prop: string]: IndividualAppConfig;
  }
  
  export interface RestConfig {
    [prop: string]: string;
  }
  
  export interface WebADE {
    oauth2Url: string;
    clientId: string;
    authScopes: string;
    enableCheckToken?: boolean;
    checkTokenUrl?: string;
  }
  
  export interface ControlsConfig {
    zoomControl: boolean;
    attributionControl: boolean;
  }
  
  /**
   * Config for basemap chooser UI interface.
   */
  export interface BasemapConfig {
    title: string;
    thumbnail: string;
  }
  
  export interface LayerServiceConfig {
    name: string;
    url: string;
  }
  
  export interface LayerAttribute {
    name: string;
    title: string;
  }
  
  export interface LayerConfig {
    id?: string;
    title: string;
    folder: [string];
    service: string;
    visible: boolean;
    layers: string;
    attributes?: [LayerAttribute];
  }
  
  export interface MapConfig {
    center: [number, number];
    zoom: number;
    leafOpt: ControlsConfig;
    basemaps: [string];
  }
  
  export interface LayerSettings {
    layerServices: {
      [prop: string]: LayerServiceConfig;
    };
    layers: [LayerConfig];
  }
  
  export interface MapServiceConfig {
    map: MapConfig;
    basemapUI: {
      [prop: string]: BasemapConfig;
    };
    layerSettings: LayerSettings;
  }
  
  export interface ApplicationConfig {
    application: Application;
    externalAppConfig: ExternalAppConfig;
    rest: RestConfig;
    webade: WebADE;
    mapServiceConfig: MapServiceConfig;
    causeCodeConfig: CauseCodeConfig;
    userPreferences?: any;
  }
  