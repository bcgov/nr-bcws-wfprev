import { Injector } from "@angular/core";
import { AppConfigService } from "../services/app-config.service";
import { TokenService } from 'src/app/services/token.service';
import { DateTimeProvider, OAuthLogger } from "angular-oauth2-oidc";

export enum ResourcesRoutes {
  LANDING = '',
  MAP = 'map',
  LIST = 'list',
  ERROR_PAGE = 'error-page',
  EDIT_PROJECT = 'edit-project'
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
