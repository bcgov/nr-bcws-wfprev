import { Injector } from "@angular/core";
import { AppConfigService } from "../services/app-config.service";
import { TokenService } from 'src/app/services/token.service';
import { DateTimeProvider, OAuthLogger } from "angular-oauth2-oidc";

export enum ResourcesRoutes {
    LANDING = '',
    MAP = 'map',
    LIST = 'list',
    ERROR_PAGE = 'error-page'
}

export function appInitializerFactory(injector: Injector) {
    const appConfig = injector.get(AppConfigService);
    appConfig.configEmitter.subscribe(config => {
      const tokenService = injector.get(TokenService);
    });
    return () => appConfig.loadAppConfig();
}

export class CustomOAuthLogger extends OAuthLogger {
    override debug(message?: any, ...optionalParams: any[]): void {
      throw new Error('Method not implemented.');
    }
    override info(message?: any, ...optionalParams: any[]): void {
      throw new Error('Method not implemented.');
    }
    override warn(message?: any, ...optionalParams: any[]): void {
      throw new Error('Method not implemented.');
    }
    override error(message?: any, ...optionalParams: any[]): void {
      throw new Error('Method not implemented.');
    }
    log(message: string): void {
      console.log('OAuthLogger:', message);
    }
  }
  
  export class CustomDateTimeProvider extends DateTimeProvider {
    override now(): number {
      throw new Error('Method not implemented.');
    }
    override new(): Date {
      throw new Error('Method not implemented.');
    }  
  }