import { Injectable } from '@angular/core';
import { mapConfig } from './map.config';
import { AppConfigService } from '../app-config.service';
import { TokenService } from '../token.service';
import { firstValueFrom } from 'rxjs';

export type MapServices = Record<string, string>;

@Injectable()
export class MapConfigService {
  constructor(
    private readonly appConfig: AppConfigService,
    private readonly tokenService: TokenService
  ) {}

  async getMapConfig(): Promise<any> {
  await this.appConfig.loadAppConfig();
  const cfg = this.appConfig.getConfig();

  const token = await firstValueFrom(this.tokenService.authTokenEmitter);

  return mapConfig(cfg.mapServices as unknown as MapServices, token);
  }
}