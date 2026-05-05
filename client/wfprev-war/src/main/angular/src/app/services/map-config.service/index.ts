import { Injectable, inject } from '@angular/core';
import { mapConfig, mapConfigBase, mapConfigLayers } from './map.config';
import { AppConfigService } from '../app-config.service';
import { TokenService } from '../token.service';
import { firstValueFrom } from 'rxjs';

export type MapServices = Record<string, string>;

@Injectable()
export class MapConfigService {
  private readonly appConfig = inject(AppConfigService);
  private readonly tokenService = inject(TokenService);


  async getMapConfig(): Promise<any> {
    await this.appConfig.loadAppConfig();
    const cfg = this.appConfig.getConfig();
    const token = await firstValueFrom(this.tokenService.authTokenEmitter);

    const mergedServices = {
      ...cfg.mapServices,
      openmaps: cfg.rest['openmaps'],
    };

    return mapConfig(mergedServices, token);
  }

  async getBaseConfig(): Promise<any> {
    await this.appConfig.loadAppConfig();
    const cfg = this.appConfig.getConfig();
    const token = await firstValueFrom(this.tokenService.authTokenEmitter);
    const mergedServices = { ...cfg.mapServices, openmaps: cfg.rest['openmaps'] };
    return mapConfigBase(mergedServices, token);
  }

  async getLayersConfig(): Promise<any> {
    await this.appConfig.loadAppConfig();
    const cfg = this.appConfig.getConfig();
    const token = await firstValueFrom(this.tokenService.authTokenEmitter);
    const mergedServices = { ...cfg.mapServices, openmaps: cfg.rest['openmaps'] };
    return mapConfigLayers(mergedServices, token);
  }
}