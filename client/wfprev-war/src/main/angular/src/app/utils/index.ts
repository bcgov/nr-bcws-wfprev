import { Injector } from "@angular/core";
import { AppConfigService } from "../services/app-config.service";
import { TokenService } from 'src/app/services/token.service';

export enum ResourcesRoutes {
  LANDING = '',
  MAP = 'map',
  LIST = 'list',
  ERROR_PAGE = 'error-page'
}

export function appInitializerFactory(injector: Injector) {
  const appConfig = injector.get(AppConfigService);
  appConfig.configEmitter.subscribe(config => {
    injector.get(TokenService);
  });
  return () => appConfig.loadAppConfig();
}
