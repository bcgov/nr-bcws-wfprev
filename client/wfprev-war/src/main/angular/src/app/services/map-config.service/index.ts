import { Injectable } from '@angular/core';
import { mapConfig, mapConfigBase, mapConfigLayers } from './map.config';
import { AppConfigService } from '../app-config.service';
import { TokenService } from '../token.service';
import { ApplicationConfig } from '../../interfaces/application-config';
import { firstValueFrom } from 'rxjs';

export type MapServices = Record<string, string>;

@Injectable()
export class MapConfigService {
  constructor(
    private readonly appConfig: AppConfigService,
    private readonly tokenService: TokenService
  ) {}

  async getMapConfig(): Promise<any> {
    const { services, token } = await this.services();
    return mapConfig(services, token);
  }

  async getBaseConfig(): Promise<any> {
    const { services, token } = await this.services();
    return mapConfigBase(services, token);
  }

  async getLayersConfig(): Promise<any> {
    const { services, token } = await this.services();
    return mapConfigLayers(services, token);
  }

  private async services(): Promise<{ services: MapServices; token: string }> {
    const cfg = await this.config();
    const token = await firstValueFrom(this.tokenService.authTokenEmitter);
    return { services: { ...cfg.mapServices, openmaps: cfg.rest['openmaps'] }, token };
  }

  // The app loads its config as it starts (app-initializer), so the map needn't fetch it again before it's built
  private async config(): Promise<ApplicationConfig> {
    try {
      return this.appConfig.getConfig();
    } catch {
      await this.appConfig.loadAppConfig();
      return this.appConfig.getConfig();
    }
  }
}