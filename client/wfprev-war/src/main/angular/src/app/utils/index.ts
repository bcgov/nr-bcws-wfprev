import { Injector } from "@angular/core";
import { AppConfigService } from "../services/app-config.service";
import { TokenService } from 'src/app/services/token.service';
import { DateTimeProvider, OAuthLogger } from "angular-oauth2-oidc";
import L from "leaflet";

export enum ResourcesRoutes {
  LANDING = '',
  MAP = 'map',
  LIST = 'list',
  ERROR_PAGE = 'error-page',
  EDIT_PROJECT = 'edit-project'
}

export enum EditProjectTabIndexes {
  Details = 0,
  Fiscal = 1
}

export function appInitializerFactory(injector: Injector) {
  const appConfig = injector.get(AppConfigService);
  appConfig.configEmitter.subscribe(config => {
    injector.get(TokenService);
  });
  return () => appConfig.loadAppConfig();
}

// overriding Core UI dependencies
export class CustomOAuthLogger extends OAuthLogger {
  override debug(message?: any, ...optionalParams: any[]): void {
    console.debug('OAuthLogger Debug:', message);
  }
  override info(message?: any, ...optionalParams: any[]): void {
    console.info('OAuthLogger Info:', message);
  }
  override warn(message?: any, ...optionalParams: any[]): void {
    console.warn('OAuthLogger Warn:', message);
  }
  override error(message?: any, ...optionalParams: any[]): void {
    console.error('OAuthLogger Error:', message);
  }
  log(message: string): void {
    console.log('OAuthLogger Log:', message);
  }
}
export class CustomDateTimeProvider extends DateTimeProvider {
  override now(): number {
    return new Date() as unknown as number;
  }
  override new(): Date {
    return new Date();
  }
}

export function getActiveMap(smk: any | null = null) {
    const SMK = smk || (window as any)['SMK'];
    const key = Object.keys(SMK.MAP)[Object.keys(SMK.MAP).length - 1];
    if (key) {
      const map = SMK.MAP[key];
      map.$viewer.map._layersMaxZoom = 20;
      return map;
    }
    // Sort of a fail-safe if the object doesn't have a key to force-retry with the window SMK object
    else {
      const smkMap = SMK.MAP;
      const lastKey = Object.keys(smkMap).pop(); // Safely gets the last key
      if (lastKey) {
          return smkMap[lastKey]; // Use the key to access the value
      }
    }
}

export function capitalizeFirstLetter(status: string): string {
    if (!status) return '';
    return status.toLowerCase().split(' ').map(word => word.charAt(0).toUpperCase() + word.slice(1)).join(' ');
  }

  export function createMaplibreLayer(opts: any): L.Layer {
    return (L as any).maplibreGL(opts);
  }

  export function createMap(id: string, options: L.MapOptions): L.Map {
    return L.map(id, options);
  }

  export function createTileLayer(url: string, options: L.TileLayerOptions): L.TileLayer {
    return L.tileLayer(url, options);
  }

  export function createMarker(latlng: L.LatLngExpression, options?: L.MarkerOptions): L.Marker {
    return L.marker(latlng, options);
  }

  export function createGeoJSON(geom: any, options?: L.GeoJSONOptions): L.GeoJSON {
    return L.geoJSON(geom, options);
  }

  export function createFeatureGroup(layers?: L.Layer[]): L.FeatureGroup {
    return L.featureGroup(layers);
  }
